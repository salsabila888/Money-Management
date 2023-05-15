package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.SpinnerNumberModel;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.io.Files;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderdocDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Torderdoc;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TorderListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OrderPinpadAdjustmentVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private TorderListModel model;

	private Session session;
	private Transaction transaction;
	
	private String orderby;
	private String filter;
	private String decisionmemo;
	
	private Integer totalcs;
	private Integer totalteller;
	private Integer totalqty;
	
	private Torder obj;
	private Muser oUser;
	
	private int pageStartNumber;
	private boolean needsPageUpdate;
	
	private TorderDAO oDao = new TorderDAO();	
	
//	@Wire
//	private Grid grid;
//	@Wire
//	private Paging paging;
	@Wire
	private Window winAdjustment;
	@Wire
	private Checkbox tipeC, tipeT;
	@Wire
	private Intbox intcs, inttel, inttotal;
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder torder) throws Exception {
		Selectors.wireComponents(view, this, false);	
		oUser = (Muser) zkSession.getAttribute("oUser");
		obj = torder;
		
//		paging.addEventListener("onPaging", new EventListener<Event>() {
//			@Override
//			public void onEvent(Event event) throws Exception {
//				PagingEvent pe = (PagingEvent) event;
//				pageStartNumber = pe.getActivePage();
//			}
//		});
		
//		if (grid != null) {
//			grid.setRowRenderer(new RowRenderer<Torder>() {
//
//				@Override
//				public void render(Row row, Torder data, int index)
//						throws Exception {
//					row.getChildren().add(new Label(String.valueOf(index + 1)));
//
//					Spinner spinnercs = new Spinner(data.getTotalcs());
//					spinnercs.setCols(3);
//					row.getChildren().add(spinnercs);
//					
//					Spinner spinnertel = new Spinner(data.getTotalcs());
//					spinnertel.setCols(3);
//					row.getChildren().add(spinnertel);
//					
//					Button btnEdit = new Button(Labels.getLabel("common.update"));	
//					btnEdit.setAutodisable("self");
//					btnEdit.setSclass("btn btn-default btn-sm");
//					btnEdit.setStyle(
//							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
//					btnEdit.addEventListener(Events.ON_CLICK, new EventListener() {
//
//						@Override
//						public void onEvent(Event event) throws Exception {
//							session = StoreHibernateUtil.openSession();
//							transaction = session.beginTransaction();
//							if (decisionmemo != null && decisionmemo.trim().length() > 0) {
//								System.out.println(decisionmemo);
//								if ((spinnercs.getValue() < data.getTotalcs()) && (spinnertel.getValue() < data.getTotalteller())) {
//									data.setTotalcs(spinnercs.getValue());
//									data.setTotalteller(spinnertel.getValue());
//									
//									Integer totalqty = 0;
//									totalqty = spinnercs.getValue() + spinnertel.getValue();
//									data.setTotalqty(totalqty);
//									data.setStatus(AppUtils.STATUS_ORDER_WAITSCANPRODUKSIOPR);
//									
//									Tordermemo objMemo = new Tordermemo();
//									objMemo.setMemo(decisionmemo);
//									objMemo.setMemoby(oUser.getUsername());
//									objMemo.setMemotime(new Date());
//									objMemo.setTorder(obj);
//									new TordermemoDAO().save(session, objMemo);
//									
//									Clients.showNotification("Perubahan jumlah pemenuhan pinpad berhasil tersimpan", "info", null, "middle_center", 3000);
//									Event closeEvent = new Event("onClose", winAdjustment, new Boolean(true));
//									Events.postEvent(closeEvent);
//									
//									Mmenu mmenu = new MmenuDAO()
//											.findByFilter("menupath = '/view/order/orderapprovaldiv.zul' and menuparamvalue = 'opr'");
//									NotifHandler.delete(mmenu, data.getMbranch(), obj.getProductgroup(),
//											oUser.getMbranch().getBranchlevel());
//									
//									mmenu = new MmenuDAO().findByFilter(
//											"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'req'");
//									NotifHandler.doNotif(mmenu, data.getMbranch(), obj.getProductgroup(),
//											oUser.getMbranch().getBranchlevel());
//								} else 
//									Messagebox.show("Jumlah pinpad cs dan teller harus kurang dari permintaan");
//							} else {
//								Messagebox.show("Anda harus mengisi kolom Catatan Pemenuhan", "Info", Messagebox.OK,
//										Messagebox.INFORMATION);
//							}
//							
//							BindUtils.postNotifyChange(null, null, OrderPinpadAdjustmentVm.this, "decisionmemo");
//							oDao.save(session, data);
//							transaction.commit();
//							session.close();
//						}
//					});											
//										
//					row.getChildren().add(btnEdit);										
//				}
//				
//			});
//		}	
		doReset();
	}
	
	public void doChecked() {
		if (tipeC.isChecked()) {
			intcs.setDisabled(false);
		} else {
			intcs.setDisabled(true);
		}
		
		if (tipeT.isChecked()) {
			inttel.setDisabled(false);
		} else {
			inttel.setDisabled(true);
		}
	}
	
	@Command
	@NotifyChange("totalqty")
	public void doChange() {
		totalqty = totalcs + totalteller;
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		decisionmemo = null;
		totalcs = obj.getTotalcs();
		totalteller = obj.getTotalteller();
		totalqty = obj.getTotalqty();
		doSearch();
	}
	
	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "mbranchfk =" + obj.getMbranch().getMbranchpk() + "and torderpk = " + obj.getTorderpk() + "";
		
//		needsPageUpdate = true;
//		paging.setActivePage(0);
//		pageStartNumber = 0;
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			if (decisionmemo != null && decisionmemo.trim().length() > 0) {
				System.out.println(decisionmemo);
				if ((totalcs < obj.getTotalcs()) || (totalteller < obj.getTotalteller())) {
					obj.setTotalcs(totalcs);
					obj.setTotalteller(totalteller);
					obj.setItemqty(totalqty);
					obj.setTotalqty(totalqty);
					obj.setStatus(AppUtils.STATUS_ORDER_WAITSCANPRODUKSIOPR);
					
					Tordermemo objMemo = new Tordermemo();
					objMemo.setMemo(decisionmemo);
					objMemo.setMemoby(oUser.getUsername());
					objMemo.setMemotime(new Date());
					objMemo.setTorder(obj);
					new TordermemoDAO().save(session, objMemo);
					
					Clients.showNotification("Perubahan jumlah pemenuhan pinpad berhasil tersimpan", "info", null, "middle_center", 3000);
					Event closeEvent = new Event("onClose", winAdjustment, new Boolean(true));
					Events.postEvent(closeEvent);
					
					Mmenu mmenu = new MmenuDAO()
							.findByFilter("menupath = '/view/order/orderapprovaldiv.zul' and menuparamvalue = 'opr'");
					NotifHandler.delete(mmenu, obj.getMbranch(), obj.getProductgroup(),
							oUser.getMbranch().getBranchlevel());
					
					mmenu = new MmenuDAO().findByFilter(
							"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'req'");
					NotifHandler.doNotif(mmenu, obj.getMbranch(), obj.getProductgroup(),
							oUser.getMbranch().getBranchlevel());
				} else 
					Messagebox.show("Jumlah pinpad cs dan teller harus kurang dari permintaan");
			} else {
				Messagebox.show("Anda harus mengisi kolom Catatan Pemenuhan", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
			
			BindUtils.postNotifyChange(null, null, OrderPinpadAdjustmentVm.this, "decisionmemo");
			oDao.save(session, obj);
			transaction.commit();
			session.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getPageStartNumber() {
		return pageStartNumber;
	}

	public void setPageStartNumber(int pageStartNumber) {
		this.pageStartNumber = pageStartNumber;
	}

	public boolean isNeedsPageUpdate() {
		return needsPageUpdate;
	}

	public void setNeedsPageUpdate(boolean needsPageUpdate) {
		this.needsPageUpdate = needsPageUpdate;
	}

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}

	public String getDecisionmemo() {
		return decisionmemo;
	}

	public void setDecisionmemo(String decisionmemo) {
		this.decisionmemo = decisionmemo;
	}

	public Integer getTotalcs() {
		return totalcs;
	}

	public void setTotalcs(Integer totalcs) {
		this.totalcs = totalcs;
	}

	public Integer getTotalteller() {
		return totalteller;
	}

	public void setTotalteller(Integer totalteller) {
		this.totalteller = totalteller;
	}

	public Integer getTotalqty() {
		return totalqty;
	}

	public void setTotalqty(Integer totalqty) {
		this.totalqty = totalqty;
	}	
	
}
