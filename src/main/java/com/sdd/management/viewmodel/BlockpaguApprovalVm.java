package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.MproducttypeListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class BlockpaguApprovalVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private MproducttypeListModel model;
	
	private MproducttypeDAO oDao = new MproducttypeDAO();
	
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;	
		
	private Muser oUser;
	private Map<String, String> mapOrg;
	private List<Mproducttype> objSelected = new ArrayList<Mproducttype>();
	
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view)
			throws ParseException {
		Selectors.wireComponents(view, this, false);					
		oUser = (Muser) zkSession.getAttribute("oUser");
		try {
			mapOrg = AppData.getOrgmap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Mproducttype>() {

				@Override
				public void render(Row row, final Mproducttype data, int index)
						throws Exception {
					row.getChildren()
							.add(new Label(
									String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
											+ index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked()) 
								objSelected.add((Mproducttype) checked.getAttribute("obj"));
							else objSelected.remove((Mproducttype) checked.getAttribute("obj"));
						}
					});
					row.getChildren().add(check);					
					row.getChildren().add(new Label(mapOrg.get(data.getProductorg())));	
					row.getChildren().add(new Label(data.getProductgroupname()));
					row.getChildren().add(new Label(data.getProducttype()));					
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getLaststock())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getStockmin())));
					row.getChildren().add(
							new Label(datetimeLocalFormatter.format(data.getBlockpagutime())));
				}

			});
		}
		doReset();
	}
	
	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "blockpagutime";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MproducttypeListModel(activePage, SysUtils.PAGESIZE, filter,
				orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}
	

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "isblockpagu = 'Y'";				
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}
	
	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			objSelected = new ArrayList<Mproducttype>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Mproducttype) chk.getAttribute("obj"));									
				} else {
					chk.setChecked(false);
					objSelected.remove((Mproducttype) chk.getAttribute("obj"));
				}				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		objSelected = new ArrayList<Mproducttype>();
		doSearch();
	}
	
	@Command
	@NotifyChange("*")
	public void doSave() {
		if (objSelected.size() > 0) {
			Session session = StoreHibernateUtil.openSession();
			Transaction transaction = session.beginTransaction();
			try {
				for (Mproducttype obj: objSelected) {
					obj.setIsblockpagu("N");
					obj.setUnblockpaguby(oUser.getUserid());
					obj.setUnblockpagutime(new Date());
					oDao.save(session, obj);												
				}		
				transaction.commit();
				Clients.showNotification(
						"Proses approval data berhasil",
						"info", null, "middle_center", 3000);
				doReset();
			} catch (Exception e) {
				transaction.rollback();
				e.printStackTrace();
			} finally {
				session.close();
			}				
		} else {
			Messagebox.show("Silahkan pilih data \nuntuk proses submit", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}		
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

}
