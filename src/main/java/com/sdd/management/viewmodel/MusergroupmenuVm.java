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
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MusergroupmenuDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Musergroup;
import com.sdd.caption.domain.Musergroupmenu;
import com.sdd.utils.db.StoreHibernateUtil;

public class MusergroupmenuVm {

	private Session session;
	private Transaction transaction;

	private MusergroupmenuDAO oDao = new MusergroupmenuDAO();

	private Musergroup obj;
	private List<Mmenu> listMmenu = new ArrayList<Mmenu>();
	private Map<Integer, Mmenu> map = new HashMap<Integer, Mmenu>();

	@Wire
	private Window winPopup;
	@Wire
	private Div divListMenu;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Musergroup obj) {
		Selectors.wireComponents(view, this, false);

		try {
			this.obj = obj;
			List<Musergroupmenu> objList = oDao.listByFilter("musergroup.musergrouppk = " + obj.getMusergrouppk(),
					"musergroupmenupk");
			for (Musergroupmenu data : objList) {
				map.put(data.getMmenu().getMmenupk(), data.getMmenu());
			}

			doListMenu();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doListMenu() {
		try {
			String menugroup = "";
			List<Mmenu> listMenu = new MmenuDAO().listByFilter("0=0", "menuorderno");
			Grid grid = null;
			Rows rows = null;
			for (Mmenu menu : listMenu) {
				if (!menugroup.equals(menu.getMenugroup())) {
					menugroup = menu.getMenugroup();
					divListMenu.appendChild(new Separator());
					/*
					 * Label lblGroup = new Label(menugroup);
					 * lblGroup.setStyle("font-weight: bold"); lblGroup.setParent(divListMenu);
					 */
					grid = new Grid();
					Columns columns = new Columns();
					Column column1 = new Column();
					column1.setStyle("background-color: #81bf3d !important; border-color: #f1f2f2 !important;");
					column1.setAlign("center");
					column1.setWidth("60px");
					final Checkbox chk = new Checkbox();
					chk.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							doCheckedall((Grid) chk.getParent().getParent().getParent(), chk.isChecked());
						}
					});
					column1.appendChild(chk);
					Column column2 = new Column(menugroup);
					column2.setStyle("background-color: #81bf3d !important; border-color: #f1f2f2 !important;");
					columns.appendChild(column1);
					columns.appendChild(column2);
					grid.appendChild(columns);
					rows = new Rows();
					grid.appendChild(rows);
					grid.setParent(divListMenu);
				}
				Row row = new Row();
				final Checkbox chk = new Checkbox();
				chk.setAttribute("obj", menu);
				chk.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						if (chk.isChecked()) {
							listMmenu.add((Mmenu) chk.getAttribute("obj"));
						} else {
							listMmenu.remove(chk.getAttribute("obj"));
						}
					}
				});
				if (map.get(menu.getMmenupk()) != null) {
					chk.setChecked(true);
					listMmenu.add(menu);
				}
				Label label = new Label(menu.getMenuname());
				row.appendChild(chk);
				row.appendChild(label);
				row.setParent(rows);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doCheckedall(Grid grid, boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(0);
				if (checked) {
					chk.setChecked(true);
					listMmenu.remove(chk.getAttribute("obj"));
					listMmenu.add((Mmenu) chk.getAttribute("obj"));
				} else {
					chk.setChecked(false);
					listMmenu.remove(chk.getAttribute("obj"));
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
			oDao.deleteBySQL(session, "musergroupfk = " + obj.getMusergrouppk());
			for (Mmenu data : listMmenu) {
				Musergroupmenu objForm = new Musergroupmenu();
				objForm.setMusergroup(obj);
				objForm.setMmenu(data);
				oDao.save(session, objForm);
			}
			transaction.commit();
			session.close();
			Clients.showNotification("Pengaturan Menu Sukses", "info", null, "middle_center", 3000);
			Event closeEvent = new Event("onClose", winPopup, null);
			Events.postEvent(closeEvent);
		} catch (HibernateException e) {
			transaction.rollback();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	public Musergroup getObj() {
		return obj;
	}

	public void setObj(Musergroup obj) {
		this.obj = obj;
	}

}
