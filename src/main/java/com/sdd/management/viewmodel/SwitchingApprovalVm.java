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
import org.zkoss.bind.BindUtils;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.dao.TswitchmemoDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.domain.Tswitchmemo;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TswitchListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class SwitchingApprovalVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private TswitchListModel model;

	private TswitchDAO oDao = new TswitchDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String switchby;
	private String filter;
	private String action;
	private String decisionmemo;
	private String arg;
	private String productgroup;

	private Muser oUser;
	private List<Tswitch> objSelected = new ArrayList<Tswitch>();
	private List<Mbranch> branchList = new ArrayList<Mbranch>();
	private Map<String, Mbranch> mapData = new HashMap<String, Mbranch>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Radiogroup rgapproval;
	@Wire
	private Radio rbd;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("productgroup") String productgroup) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		this.arg = arg;
		this.productgroup = productgroup;
		branchList = AppData.getMbranch();
		for (Mbranch data : branchList) {
			mapData.put(data.getBranchid(), data);
		}

		if (arg.equals("pool")) {
			rbd.setLabel("Decline");
		} else if (arg.equals("req")) {
			if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD)) {
				rbd.setLabel("Review Ulang");
			} else {
				rbd.setLabel("Decline");
			}
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tswitch>() {
			@Override
			public void render(Row row, final Tswitch data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						if (checked.isChecked())
							objSelected.add((Tswitch) checked.getAttribute("obj"));
						else
							objSelected.remove((Tswitch) checked.getAttribute("obj"));
					}
				});
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getRegid()));
				row.getChildren().add(new Label(data.getTorder().getMbranch().getBranchname()));
				row.getChildren().add(new Label(data.getTorder().getOrderoutlet()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(new Label(mapData.get(data.getBranchidpool()).getBranchname()));
				row.appendChild(new Label(data.getOutletpool() != null ? data.getOutletpool() : "-"));

				if (data.getTorder().getTorderpk() != null) {
					Button btnOrder = new Button("Lihat Data Pemesanan");
					btnOrder.setAutodisable("self");
					btnOrder.setClass("btn-default");
					btnOrder.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnOrder.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("obj", data.getTorder());
							map.put("arg", arg);
							Window win = (Window) Executions.createComponents("/view/order/orderdetail.zul", null, map);
							win.setWidth("60%");
							win.setClosable(true);
							win.doModal();
						}
					});

					Div div = new Div();
					div.appendChild(btnOrder);
					row.appendChild(div);
				} else {
					row.getChildren().add(new Label("-"));
				}

				Button btnMemo = new Button();
				btnMemo.setLabel("Memo");
				btnMemo.setAutodisable("self");
				btnMemo.setSclass("btn btn-default btn-sm");
				btnMemo.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btnMemo.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data.getTorder());
						Window win = (Window) Executions.createComponents("/view/order/ordermemo.zul", null, map);
						win.setWidth("70%");
						win.setClosable(true);
						win.doModal();
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								doReset();
								BindUtils.postNotifyChange(null, null, SwitchingApprovalVm.this, "*");
							}
						});
					}
				});
				row.getChildren().add(btnMemo);

				row.getChildren().add(new Label(dateLocalFormatter.format(data.getInserttime())));
				row.getChildren().add(new Label(
						data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : ""));
				row.getChildren().add(new Label(data.getInsertedby()));
			}
		});
		doReset();
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			objSelected = new ArrayList<Tswitch>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Tswitch) chk.getAttribute("obj"));
				} else {
					chk.setChecked(false);
					objSelected.remove((Tswitch) chk.getAttribute("obj"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {

		if (arg.equals("req"))
			filter = "status = '" + AppUtils.STATUS_SWITCH_WAITAPPROVAL + "' and branchidreq = '"
					+ oUser.getMbranch().getBranchid() + "'";
		else
			filter = "status = '" + AppUtils.STATUS_SWITCH_WAITAPPROVALPOOL + "' and branchidpool = '"
					+ oUser.getMbranch().getBranchid() + "'";

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		decisionmemo = null;
		action = null;
		objSelected = new ArrayList<Tswitch>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		switchby = "tswitchpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TswitchListModel(activePage, SysUtils.PAGESIZE, filter, switchby);
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
			if (action != null && action.length() > 0) {
				if (action.equals(AppUtils.STATUS_APPROVED) || (action.equals(AppUtils.STATUS_DECLINE)
						&& decisionmemo != null && decisionmemo.trim().length() > 0)) {
					Session session = StoreHibernateUtil.openSession();
					Transaction transaction = session.beginTransaction();
					try {
						for (Tswitch obj : objSelected) {
							if (action.equals(AppUtils.STATUS_APPROVED)) {
								obj.setDecisionby(oUser.getUserid());
								obj.setDecisiontime(new Date());
								if (arg.equals("req")) {
									if (oUser.getMbranch().getBranchlevel() < 3) {
										obj.setStatus(AppUtils.STATUS_SWITCH_WAITAPPROVALPOOL);
										obj.getTorder().setStatus(AppUtils.STATUS_SWITCH_WAITAPPROVALPOOL);
									} else {
										obj.setStatus(AppUtils.STATUS_SWITCH_HANDOVER);
										obj.getTorder().setStatus(AppUtils.STATUS_SWITCH_HANDOVER);
									}
								} else {
									obj.setStatus(AppUtils.STATUS_SWITCH_HANDOVER);
									obj.getTorder().setStatus(AppUtils.STATUS_SWITCH_HANDOVER);
								}

							} else if (action.equals(AppUtils.STATUS_DECLINE)) {
								if (arg.equals("req")) {
									if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										obj.setStatus(AppUtils.STATUS_SWITCH_DECLINEREQ);
										obj.getTorder().setStatus(AppUtils.STATUS_SWITCH_DECLINEREQ);
									} else {
										obj.setStatus(AppUtils.STATUS_SWITCH_REJECTEDREQ);
										obj.getTorder().setStatus(AppUtils.STATUS_SWITCH_REJECTEDREQ);
									}
								} else {
									obj.setStatus(AppUtils.STATUS_SWITCH_REJECTEDPOOL);
									obj.getTorder().setStatus(AppUtils.STATUS_SWITCH_REJECTEDPOOL);
								}
							}

							new TorderDAO().save(session, obj.getTorder());
							oDao.save(session, obj);

							if (decisionmemo != null && decisionmemo.trim().length() > 0) {
								Tswitchmemo objMemo = new Tswitchmemo();
								objMemo.setMemo(decisionmemo);
								objMemo.setMemoby(oUser.getUsername());
								objMemo.setMemotime(new Date());
								objMemo.setTswitch(obj);
								new TswitchmemoDAO().save(session, objMemo);

								Tordermemo orderMemo = new Tordermemo();
								orderMemo.setMemo(decisionmemo);
								orderMemo.setMemoby(oUser.getUsername());
								orderMemo.setMemotime(new Date());
								orderMemo.setTorder(obj.getTorder());
								new TordermemoDAO().save(session, orderMemo);
							}

							FlowHandler.doFlow(session, null, obj.getTorder(), decisionmemo, oUser.getUserid());
						}
						transaction.commit();
						session.close();

						for (Tswitch obj : objSelected) {
							if (arg.equals("req")) {
								if (action.equals(AppUtils.STATUS_APPROVED)) {
									if (!obj.getOutletreq().equals("00")) {
										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/switching/switching.zul' and menuparamvalue = 'listpool'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(),
												obj.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									} else {
										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/switching/switching.zul' and menuparamvalue = 'approvepool'");
										Mbranch branch = new MbranchDAO()
												.findByFilter("branchid = '" + obj.getBranchidpool() + "'");
										NotifHandler.doNotif(mmenu, branch, obj.getMproduct().getProductgroup(),
												branch.getBranchlevel());
									}
								} else {
									if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD)) {
										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/switching/switching.zul' and menuparamvalue = 'listreq'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(),
												obj.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									}
								}

								Mmenu mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/switching/switching.zul' and menuparamvalue = 'approvereq'");
								NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
										oUser.getMbranch().getBranchlevel());
							} else if (arg.equals("pool")) {
								Mmenu mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/switching/switching.zul' and menuparamvalue = 'listpool'");
								NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
										oUser.getMbranch().getBranchlevel());

								mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/switching/switching.zul' and menuparamvalue = 'approvepool'");
								Mbranch branch = new MbranchDAO()
										.findByFilter("branchid = '" + obj.getBranchidpool() + "'");
								NotifHandler.delete(mmenu, branch, obj.getMproduct().getProductgroup(),
										branch.getBranchlevel());
							}
						}
						Clients.showNotification("Submit data berhasil", "info", null, "middle_center", 3000);
						doReset();
					} catch (Exception e) {
						transaction.rollback();
						e.printStackTrace();
					}
				} else {
					Messagebox.show("Anda harus mengisi field Decision Memo", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Silahkan pilih action", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} else

		{
			Messagebox.show("Silahkan pilih data untuk proses submit", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDecisionmemo() {
		return decisionmemo;
	}

	public void setDecisionmemo(String decisionmemo) {
		this.decisionmemo = decisionmemo;
	}

}
