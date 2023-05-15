package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.bind.annotation.ContextParam;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TorderListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OrderApprovalPinpadVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private TorderListModel model;
	private TorderDAO oDao = new TorderDAO();
	
	private Session session;
	private Transaction transaction;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String productgroup;
	private String decisionmemo;
	private Integer total;
	private int branchlevel;
	private boolean isJAL = false;
	private boolean isOPR = false;

	private Muser oUser;
	private List<Torder> objSelected = new ArrayList<Torder>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Column colBranch;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("isOPR") String isOPR, @ExecutionArgParam("isJAL") String isJAL) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();

		productgroup = arg;

		if (productgroup.equals("TC") || productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)
				|| productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			colBranch.setVisible(true);
		} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
			colBranch.setVisible(false);
		} else {
			colBranch.setVisible(false);
		}

		if (isJAL != null) {
			this.isJAL = true;
		}

		if (isOPR != null) {
			this.isOPR = true;
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Torder>() {
			@Override
			public void render(Row row, final Torder data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						if (checked.isChecked())
							objSelected.add((Torder) checked.getAttribute("obj"));
						else
							objSelected.remove((Torder) checked.getAttribute("obj"));
					}
				});
				row.getChildren().add(check);
				
				A a = new A(data.getOrderid());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						Window win = new Window();
						map.put("obj", data);
						map.put("arg", arg);
						win = (Window) Executions.createComponents("/view/order/orderitem.zul", null, map);
						win.setWidth("50%");
						win.setClosable(true);
						win.doModal();
					}
				});
				if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER))
					row.getChildren().add(new Label(data.getOrderid() != null ? data.getOrderid() : "-"));
				else
					row.getChildren().add(a);
				row.getChildren().add(new Label(data.getMbranch() != null ? data.getMbranch().getBranchname() : "-"));
				row.getChildren().add(new Label(AppData.getProductgroupLabel(productgroup)));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getInserttime())));
				row.getChildren().add(new Label(
						data.getTotalcs() != null ? NumberFormat.getInstance().format(data.getTotalcs()) : "0"));
				row.getChildren().add(new Label(
						data.getTotalteller() != null ? NumberFormat.getInstance().format(data.getTotalteller()) : "0"));				
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getItemqty())));
				row.getChildren().add(new Label(data.getInsertedby()));
				row.getChildren().add(new Label(data.getMemono()));
				row.getChildren().add(new Label(data.getMemo() != null ? data.getMemo() : "-"));
				
				Button btnPemenuhan = new Button();
				btnPemenuhan.setLabel("Dipenuhi Sebagian");
				btnPemenuhan.setAutodisable("self");
				btnPemenuhan.setSclass("btn-light");
				btnPemenuhan.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btnPemenuhan.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("obj", data);
						Window win = (Window) Executions.createComponents("/view/order/orderpinpadadjustment.zul", null, map);
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								needsPageUpdate = true;
								refreshModel(pageStartNumber);
							}
						});
						win.setWidth("50%");
						win.setClosable(true);
						win.doModal();
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								doSearch();
							}
						});
					}
				});
				
				Div div = new Div();
				div.appendChild(btnPemenuhan);
				row.appendChild(div);
			}
		});
		doReset();
	}
	
	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			objSelected = new ArrayList<Torder>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Torder) chk.getAttribute("obj"));
				} else {
					chk.setChecked(false);
					objSelected.remove((Torder) chk.getAttribute("obj"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange({ "pageTotalSize", "total" })
	public void doSearch() {
		try {
			if (isJAL) {
				filter = "status = '" + AppUtils.STATUS_ORDER_WAITAPPROVALJAL + "' and productgroup = '" + productgroup
						+ "' ";
			} else if (isOPR) {
				filter = "status = '" + AppUtils.STATUS_ORDER_WAITAPPROVALOPR + "' and productgroup = '" + productgroup
						+ "' ";
			} else if (branchlevel == 2) {
				filter = "status = '" + AppUtils.STATUS_ORDER_WAITAPPROVALWIL + "' and productgroup = '" + productgroup
						+ "' and mbranchfk = " + oUser.getMbranch().getMbranchpk();
			} else if (branchlevel == 3) {
				filter = "status = '" + AppUtils.STATUS_ORDER_WAITAPPROVALCAB + "' and productgroup = '" + productgroup
						+ "' and mbranchfk = " + oUser.getMbranch().getMbranchpk();
			} else {
				filter = "status = '" + AppUtils.STATUS_ORDER_WAITAPPROVAL + "' and productgroup = '" + productgroup
						+ "'";
			}

			total = 0;
			for (Torder data : oDao.listNativeByFilter(filter, "torderpk")) {
				total = total + data.getTotalqty();
			}

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		decisionmemo = null;
		total = 0;
		objSelected = new ArrayList<Torder>();
		total = 0;
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "torderpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TorderListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (objSelected.size() > 0) {
			Session session = StoreHibernateUtil.openSession();
			Transaction transaction = session.beginTransaction();
			try {
				for (Torder obj : objSelected) {
					obj.setDecisionby(oUser.getUserid());
					obj.setDecisiontime(new Date());
					obj.setStatus(AppUtils.STATUS_ORDER_WAITSCANPRODUKSIOPR);
					
					Toutgoing toutgoing = new Toutgoing();
					toutgoing.setMproduct(obj.getMproduct());
					toutgoing.setTorder(obj);
					toutgoing.setEntryby(obj.getInsertedby());
					toutgoing.setEntrytime(new Date());
					toutgoing.setItemqty(obj.getTotalqty());
					toutgoing.setLastupdated(new Date());
					toutgoing.setOutgoingid(obj.getOrderid());
					toutgoing.setProductgroup(productgroup);
					toutgoing.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL);
					toutgoing.setUpdatedby(oUser.getUserid());
					toutgoing.setMemo(obj.getMemo());
					new ToutgoingDAO().save(session, toutgoing);
					
					oDao.save(session, obj);
	
					if (decisionmemo != null && decisionmemo.trim().length() > 0) {
						Tordermemo objMemo = new Tordermemo();
						objMemo.setMemo(decisionmemo);
						objMemo.setMemoby(oUser.getUsername());
						objMemo.setMemotime(new Date());
						objMemo.setTorder(obj);
						new TordermemoDAO().save(session, objMemo);
					}
					
					FlowHandler.doFlow(session, null, obj, decisionmemo, oUser.getUserid());
				}
				transaction.commit();
				Clients.showNotification("Submit data approval berhasil", "info", null, "middle_center", 3000);
			} catch (Exception e) {
				transaction.rollback();
				e.printStackTrace();
			} finally {
				session.close();
			}
	
			try {
				for (Torder obj : objSelected) {
						if (branchlevel == 2) {
							Mmenu mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'approval'");
							NotifHandler.doNotif(mmenu, obj.getMbranch(), obj.getProductgroup(),
									obj.getMbranch().getBranchlevel() - 1);
						} else if (branchlevel == 3) {
							Mmenu mmenu = null;
							if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD)) {
								mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/switching/switchinglist.zul' and menuparamvalue = 'req'");
							} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
								mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/order/orderapprovaldiv.zul' and menuparamvalue = 'jal'");
							} else {
								mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'req'");
							}
	
							if (obj.getOrderoutlet().equals("00")) {
								if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
									NotifHandler.doNotif(mmenu, obj.getMbranch(), obj.getProductgroup(),
											obj.getMbranch().getBranchlevel() - 1);
								} else {
									NotifHandler.doNotif(mmenu, obj.getMbranch(), obj.getProductgroup(),
											obj.getMbranch().getBranchlevel() - 2);
								}
							} else {
								NotifHandler.doNotif(mmenu, obj.getMbranch(), obj.getProductgroup(),
										obj.getMbranch().getBranchlevel());
							}
						} else {
							Mmenu mmenu = null;
							if (obj.getStatus().equals(AppUtils.STATUS_ORDER_WAITAPPROVALOPR)) {
								mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/order/orderapprovaldiv.zul' and menuparamvalue = 'opr'");
	
								NotifHandler.doNotif(mmenu, obj.getMbranch(), obj.getProductgroup(),
										oUser.getMbranch().getBranchlevel());
							} else if (obj.getStatus().equals(AppUtils.STATUS_ORDER_WAITSCANPRODUKSIOPR)) {
								mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'req'");
	
								NotifHandler.doNotif(mmenu, obj.getMbranch(), obj.getProductgroup(),
										oUser.getMbranch().getBranchlevel());
							} else {
								mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'approval'");
	
								NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getProductgroup(),
										oUser.getMbranch().getBranchlevel());
							}
						}
					
					if (branchlevel > 1) {
						Mmenu mmenu = new MmenuDAO().findByFilter(
								"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'approval'");
						NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(),
								oUser.getMbranch().getBranchlevel());
					} else {
						Mmenu mmenu = null;
						if (obj.getStatus().equals(AppUtils.STATUS_ORDER_WAITAPPROVALOPR)) {
							mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/order/orderapprovaldiv.zul' and menuparamvalue = 'jal'");
						} else if (obj.getStatus().equals(AppUtils.STATUS_ORDER_WAITSCANPRODUKSIOPR)) {
							mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/order/orderapprovaldiv.zul' and menuparamvalue = 'opr'");
						} else {
							mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'approvalopr'");
						}
						NotifHandler.delete(mmenu, obj.getMbranch(), obj.getProductgroup(),
								oUser.getMbranch().getBranchlevel());
					}
				}
				doReset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Silahkan pilih data untuk proses submit", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getDecisionmemo() {
		return decisionmemo;
	}

	public void setDecisionmemo(String decisionmemo) {
		this.decisionmemo = decisionmemo;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

}
