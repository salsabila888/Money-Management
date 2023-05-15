/**
 * 
 */
package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.McouriervendorbranchDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Mcouriervendorbranch;
import com.sdd.utils.db.StoreHibernateUtil;

public class McouriervendorbranchVm {

	private Session session;
	private Transaction transaction;

	private McouriervendorbranchDAO oDao = new McouriervendorbranchDAO();

	private Mcouriervendor obj;
	private List<Mbranch> listSelected = new ArrayList<Mbranch>();
	private Map<Integer, Mbranch> map = new HashMap<Integer, Mbranch>();

	@Wire
	private Window winPopup;
	@Wire
	private Grid grid;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Mcouriervendor obj) {
		Selectors.wireComponents(view, this, false);

		try {
			this.obj = obj;
			List<Mcouriervendorbranch> objList = oDao.listByFilter(
					"mcouriervendor.mcouriervendorpk = " + obj.getMcouriervendorpk(),
					"mcouriervendorbranchpk");
			for (Mcouriervendorbranch data : objList) {
				map.put(data.getMbranch().getMbranchpk(), data.getMbranch());
			}
			List<Mbranch> listMbranch = new MbranchDAO().listByFilter("0=0", "mregion.regionid, branchname");
			grid.setModel(new ListModelList<>(listMbranch));
			grid.setRowRenderer(new RowRenderer<Mbranch>() {

				@Override
				public void render(Row row, Mbranch data, int index) throws Exception {
					row.getChildren()
					.add(new Label(
							String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getMregion() != null ? data.getMregion().getRegionname() : ""));
					row.getChildren().add(new Label(data.getBranchname()));
					Checkbox chkbox = new Checkbox();
					chkbox.setAttribute("obj", data);										
					chkbox.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox chkbox = (Checkbox) event.getTarget();
							Mbranch obj = (Mbranch) chkbox.getAttribute("obj");
							if (chkbox.isChecked()) {
								listSelected.remove(obj);
								listSelected.add(obj);
								map.put(obj.getMbranchpk(), obj);
							} else {
								listSelected.remove(obj);
								map.remove(obj.getMbranchpk());
							}
						}
					});
					
					if (map.get(data.getMbranchpk()) != null) {
						chkbox.setChecked(true);
					}
					
					row.getChildren().add(chkbox);
				}
			});								
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			//listSelected = new ArrayList<Mbranch>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(3);
				Mbranch obj = (Mbranch) chk.getAttribute("obj");
				if (checked) {
					if (map.get(obj.getMbranchpk()) == null) {
						chk.setChecked(true);
						listSelected.add((Mbranch) chk.getAttribute("obj"));	
						map.put(obj.getMbranchpk(), obj);
					}													
				} else {
					chk.setChecked(false);
					listSelected.remove((Mbranch) chk.getAttribute("obj"));
					map.remove(obj.getMbranchpk());
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void save() {
		try {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			oDao.deleteBySQL(session, "mcouriervendorfk = " + obj.getMcouriervendorpk());
			for (Mbranch data : listSelected) {
				Mcouriervendorbranch objForm = new Mcouriervendorbranch();
				objForm.setMcouriervendor(obj);
				objForm.setMbranch(data);
				oDao.save(session, objForm);
			}
			transaction.commit();
			session.close();
			Clients.showNotification("Pengaturan Area Expedisi berhasil disimpan", "info",
					null, "middle_center", 3000);
			Event closeEvent = new Event( "onClose", winPopup, null);
			Events.postEvent(closeEvent);
		} catch (HibernateException e) {
			transaction.rollback();
			Messagebox.show("Error : " + e.getMessage(), "Error",
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			Messagebox.show("Error : " + e.getMessage(), "Error",
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	public Mcouriervendor getObj() {
		return obj;
	}


	public void setObj(Mcouriervendor obj) {
		this.obj = obj;
	}

	
}
