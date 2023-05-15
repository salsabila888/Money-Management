package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
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

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.dao.TreturnitemDAO;
import com.sdd.caption.dao.TreturnmemoDAO;
import com.sdd.caption.dao.TreturntrackDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.domain.Treturnmemo;
import com.sdd.caption.domain.Treturntrack;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TreturnListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ReturProductApprovalVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private TreturnDAO oDao = new TreturnDAO();
	private Tbranchstock objStock;

	private TreturnListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String action;
	private String status;
	private String decisionmemo;
	private String productgroup;
	private String producttype;
	private int flushCounter;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	private Muser oUser;
	private List<Treturn> objSelected = new ArrayList<Treturn>();
	private Map<Integer, Tbranchstock> mapStock = new HashMap<Integer, Tbranchstock>();

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Radiogroup rgapproval;
	@Wire
	private Radio rba, rbd;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("stats") String stats) throws ParseException {
		Selectors.wireComponents(view, this, false);
		productgroup = arg.trim();
		status = stats.trim();

		oUser = (Muser) zkSession.getAttribute("oUser");

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Treturn>() {
				@Override
				public void render(Row row, final Treturn data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked())
								objSelected.add((Treturn) checked.getAttribute("obj"));
							else
								objSelected.remove((Treturn) checked.getAttribute("obj"));
						}
					});
					row.getChildren().add(check);
					A a = new A(data.getRegid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("obj", data);
							win = (Window) Executions.createComponents("/view/return/returproductitem.zul", null, map);
							win.setWidth("50%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(data.getMreturnreason().getReturnreason()));
					row.getChildren()
							.add(new Label(AppData.getProductgroupLabel(data.getMproduct().getProductgroup())));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getInserttime())));
					row.getChildren().add(new Label(
							data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : "0"));
					row.getChildren().add(new Label(data.getInsertedby()));
				}
			});
		}
		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (status.equals(AppUtils.STATUS_RETUR_WAITAPPROVAL)) {
			rba.setValue(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH + AppUtils.STATUS_RETUR_WAITAPPROVALOPR);
			rbd.setValue(AppUtils.STATUS_RETUR_DECLINE);
			filter = "productgroup = '" + productgroup + "' and status = '" + AppUtils.STATUS_RETUR_WAITAPPROVAL + "'";
		} else if (status.equals(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH)) {
			rba.setValue(AppUtils.STATUS_RETUR_WAITAPPROVALPFA);
			rbd.setValue(AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH);
			filter = "productgroup = '" + productgroup + "' and status = '" + AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH
					+ "'";
		} else if (status.equals(AppUtils.STATUS_RETUR_WAITAPPROVALPFA)) {
			rba.setValue(AppUtils.STATUS_RETUR_APPROVALPFA);
			rbd.setValue(AppUtils.STATUS_RETUR_DECLINEAPPROVALPFA);
			filter = "productgroup = '" + productgroup + "' and status = '" + AppUtils.STATUS_RETUR_WAITAPPROVALPFA
					+ "'";
		} else if (status.equals(AppUtils.STATUS_RETUR_WAITAPPROVALOPR)) {
			rba.setValue(AppUtils.STATUS_RETUR_APPROVALOPR);
			rbd.setValue(AppUtils.STATUS_RETUR_DECLINEAPPROVALOPR);
			filter = "productgroup = '" + productgroup + "' and status = '" + AppUtils.STATUS_RETUR_WAITAPPROVALOPR
					+ "'";
		}
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			objSelected = new ArrayList<Treturn>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Treturn) chk.getAttribute("obj"));
				} else {
					chk.setChecked(false);
					objSelected.remove((Treturn) chk.getAttribute("obj"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		decisionmemo = null;
		rgapproval = null;
		action = null;
		objSelected = new ArrayList<Treturn>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "treturnpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TreturnListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
				if (action.equals(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH + AppUtils.STATUS_RETUR_WAITAPPROVALOPR)
						|| action.equals(AppUtils.STATUS_RETUR_DECLINE) && decisionmemo != null
								&& decisionmemo.trim().length() > 0
						|| action.equals(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH)
						|| action.equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH)
						|| action.equals(AppUtils.STATUS_RETUR_WAITAPPROVALPFA)
						|| action.equals(AppUtils.STATUS_RETUR_APPROVALPFA)
						|| action.equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALPFA)
						|| action.equals(AppUtils.STATUS_RETUR_WAITAPPROVALOPR)
						|| action.equals(AppUtils.STATUS_RETUR_APPROVALOPR)
						|| action.equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALOPR) && decisionmemo != null
								&& decisionmemo.trim().length() > 0) {

					Session session = null;
					Transaction transaction = null;
					try {
						for (Treturn obj : objSelected) {
							if (action.equals(
									AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH + AppUtils.STATUS_RETUR_WAITAPPROVALOPR)
									|| action.equals(AppUtils.STATUS_RETUR_WAITAPPROVALPFA)
									|| action.equals(AppUtils.STATUS_RETUR_APPROVALPFA)
									|| action.equals(AppUtils.STATUS_RETUR_APPROVALOPR)) {
								session = StoreHibernateUtil.openSession();
								transaction = session.beginTransaction();
								flushCounter = 0;
								if (action.equals(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH
										+ AppUtils.STATUS_RETUR_WAITAPPROVALOPR)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
										if (obj.getMreturnreason().getIsDestroy().equals("Y")) {
											obj.setStatus(AppUtils.STATUS_RETUR_DESTROYED);
										} else {
											obj.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVALOPR);
										}
									} else if (obj.getMproduct().getProductgroup()
											.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										if (obj.getMreturnreason().getIsDestroy().equals("Y")) {
											obj.setStatus(AppUtils.STATUS_RETUR_DESTROYED);
										} else {
											obj.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH);
										}
									}
								} else if (action.equals(AppUtils.STATUS_RETUR_WAITAPPROVALPFA)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										if (obj.getMreturnreason().getIsDestroy().equals("Y")) {
											obj.setStatus(AppUtils.STATUS_RETUR_DESTROYED);
										} else {
											obj.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVALPFA);
										}
									}
								} else if (action.equals(AppUtils.STATUS_RETUR_APPROVALPFA)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										obj.setStatus(AppUtils.STATUS_RETUR_RETURNPFA);
									}
								} else if (action.equals(AppUtils.STATUS_RETUR_APPROVALOPR)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
										obj.setStatus(AppUtils.STATUS_RETUR_RETURNOPR);
										List<Treturnitem> objList = new TreturnitemDAO()
												.listByFilter("treturnfk = " + obj.getTreturnpk(), "treturnitempk");
										for (Treturnitem data : objList) {
											Ttokenitem objStock = new TtokenitemDAO()
													.findByFilter("itemno = '" + data.getItemno() + "' ");
											System.out.println(objStock.getItemno());
											if (objStock != null) {
												objStock.setStatus(AppUtils.STATUS_SERIALNO_INJECTED);
												new TtokenitemDAO().save(session, objStock);
											}
										}

										Mproducttype objStock = obj.getMproduct().getMproducttype();
										objStock.setStockinjected(objStock.getStockinjected() + obj.getItemqty());
										new MproducttypeDAO().save(session, objStock);
									}
								}
								obj.setDecisionby(oUser.getUsername());
								obj.setDecisiontime(new Date());
								oDao.save(session, obj);

								Treturnmemo objMemo = new Treturnmemo();
								objMemo.setMemo(decisionmemo);
								objMemo.setMemoby(oUser.getUsername());
								objMemo.setMemotime(new Date());
								objMemo.setTreturn(obj);
								new TreturnmemoDAO().save(session, objMemo);

								Treturntrack objrt = new Treturntrack();
								objrt.setTreturn(obj);
								objrt.setTracktime(new Date());
								objrt.setTrackstatus(obj.getStatus());
								objrt.setTrackdesc(AppData.getStatusLabel(objrt.getTrackstatus()));
								new TreturntrackDAO().save(session, objrt);

								List<Treturnitem> objList = new TreturnitemDAO()
										.listByFilter("treturnfk = " + obj.getTreturnpk(), "treturnitempk");
								for (Treturnitem data : objList) {
									Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
											+ data.getItemno() + "' and status = '" + data.getItemstatus() + "'");
									if (objStock != null) {
										objStock.setStatus(obj.getStatus());
										new TbranchstockitemDAO().save(session, objStock);

										Tbranchitemtrack objTrack = new Tbranchitemtrack();
										objTrack.setItemno(data.getItemno());
										objTrack.setTracktime(new Date());
										objTrack.setTrackdesc(AppData.getStatusLabel(data.getItemstatus()));
										objTrack.setProductgroup(data.getTreturn().getMproduct().getProductgroup());
										objTrack.setMproduct(data.getTreturn().getMproduct());
										objTrack.setTrackstatus(obj.getStatus());
										new TbranchitemtrackDAO().save(session, objTrack);

										mapStock.put(objStock.getTbranchstock().getTbranchstockpk(),
												objStock.getTbranchstock());
									}
									data.setItemstatus(obj.getStatus());
									new TreturnitemDAO().save(session, data);
								}

								for (Entry<Integer, Tbranchstock> entry : mapStock.entrySet()) {
									Tbranchstock stock = entry.getValue();
									if (obj.getMreturnreason().getIsDestroy().equals("Y")) {
										stock.setStockdestroyed(stock.getStockdestroyed() + obj.getItemqty());
										stock.setStockactivated(stock.getStockactivated() - obj.getItemqty());
									}
									new TbranchstockDAO().save(session, stock);
								}

								transaction.commit();
								session.close();

								if (action.equals(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH
										+ AppUtils.STATUS_RETUR_WAITAPPROVALOPR)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										if (obj.getMreturnreason().getIsDestroy().equals("Y")) {
											Mmenu mmenu = new MmenuDAO()
													.findByFilter("menupath = '/view/return/destroylist.zul'");
											NotifHandler.doNotif(mmenu, obj.getMbranch(),
													obj.getMproduct().getProductgroup(),
													obj.getMbranch().getBranchlevel());
										} else {
											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalwil'");
											NotifHandler.doNotif(mmenu, obj.getMbranch(),
													obj.getMproduct().getProductgroup(),
													oUser.getMbranch().getBranchlevel() - 1);
										}

										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/return/return.zul' and menuparamvalue = 'approval'");
										NotifHandler.delete(mmenu, oUser.getMbranch(),
												obj.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									} else if (obj.getMproduct().getProductgroup()
											.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalopr'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(),
												obj.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel() - 2);

										mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/return/return.zul' and menuparamvalue = 'approval'");
										NotifHandler.delete(mmenu, oUser.getMbranch(),
												obj.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									}
								} else if (action.equals(AppUtils.STATUS_RETUR_WAITAPPROVALPFA)) {
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalpfa'");
									NotifHandler.doNotif(mmenu, obj.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());

									mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalwil'");
									NotifHandler.delete(mmenu, obj.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} else if (action.equals(AppUtils.STATUS_RETUR_APPROVALPFA)) {
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'returpaket'");
									NotifHandler.doNotif(mmenu, obj.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());

									mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalpfa'");
									NotifHandler.delete(mmenu, obj.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} else if (action.equals(AppUtils.STATUS_RETUR_APPROVALOPR)) {
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalopr'");
									NotifHandler.delete(mmenu, obj.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								}

								Clients.showNotification("Proses persetujuan data berhasil disetujui", "info", null,
										"middle_center", 3000);
							} else if (action.equals(AppUtils.STATUS_RETUR_DECLINE)
									|| action.equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH)
									|| action.equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALPFA)
									|| action.equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALOPR)) {
								session = StoreHibernateUtil.openSession();
								transaction = session.beginTransaction();
								flushCounter = 0;
								if (action.equals(AppUtils.STATUS_RETUR_DECLINE)) {
									flushCounter = 0;
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN)
											|| obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)
											|| obj.getMproduct().getProductgroup()
													.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										obj.setStatus(AppUtils.STATUS_RETUR_DECLINE);
									}
									
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'approval'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} else if (action.equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										obj.setStatus(AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH);
									}
								} else if (action.equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALPFA)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										obj.setStatus(AppUtils.STATUS_RETUR_DECLINEAPPROVALPFA);
									}
								} else if (action.equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALOPR)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN) || obj
											.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
										obj.setStatus(AppUtils.STATUS_RETUR_DECLINEAPPROVALOPR);
									}
									
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalopr'");
									NotifHandler.delete(mmenu, obj.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								}

								obj.setDecisionby(oUser.getUsername());
								obj.setDecisiontime(new Date());
								oDao.save(session, obj);

								Treturnmemo objMemo = new Treturnmemo();
								objMemo.setMemo(decisionmemo);
								objMemo.setMemoby(oUser.getUsername());
								objMemo.setMemotime(new Date());
								objMemo.setTreturn(obj);
								new TreturnmemoDAO().save(session, objMemo);

								Treturntrack objrt = new Treturntrack();
								objrt.setTreturn(obj);
								objrt.setTracktime(new Date());
								objrt.setTrackstatus(obj.getStatus());
								objrt.setTrackdesc(AppData.getStatusLabel(objrt.getTrackstatus()));
								new TreturntrackDAO().save(session, objrt);

								List<Treturnitem> objList = new TreturnitemDAO()
										.listByFilter("treturnfk = " + obj.getTreturnpk(), "treturnitempk");
								for (Treturnitem data : objList) {
									Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
											+ data.getItemno() + "' and status = '" + data.getItemstatus() + "'");
									if (objStock != null) {
										objStock.setStatus(AppUtils.STATUS_SERIALNO_ENTRY);
										new TbranchstockitemDAO().save(session, objStock);

										Tbranchitemtrack objTrack = new Tbranchitemtrack();
										objTrack.setItemno(data.getItemno());
										objTrack.setTracktime(new Date());
										objTrack.setTrackdesc(AppData.getStatusLabel(data.getItemstatus()));
										objTrack.setProductgroup(data.getTreturn().getMproduct().getProductgroup());
										objTrack.setMproduct(data.getTreturn().getMproduct());
										objTrack.setTrackstatus(obj.getStatus());
										new TbranchitemtrackDAO().save(session, objTrack);

										mapStock.put(objStock.getTbranchstock().getTbranchstockpk(),
												objStock.getTbranchstock());
									}
									data.setItemstatus(obj.getStatus());
									new TreturnitemDAO().save(session, data);
								}

								for (Entry<Integer, Tbranchstock> entry : mapStock.entrySet()) {
									Tbranchstock stock = entry.getValue();
									stock.setStockcabang(stock.getStockcabang() + obj.getItemqty());
									stock.setStockactivated(stock.getStockactivated() - obj.getItemqty());
									new TbranchstockDAO().save(session, stock);
								}


								transaction.commit();
								session.close();
								Clients.showNotification("Proses persetujuan data berhasil ditolak", "info", null,
										"middle_center", 3000);
							}
						}
						doReset();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Messagebox.show("Anda harus mengisi Catatan Keputusan", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Silahkan pilih action", "Info", Messagebox.OK, Messagebox.INFORMATION);
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public int getFlushCounter() {
		return flushCounter;
	}

	public void setFlushCounter(int flushCounter) {
		this.flushCounter = flushCounter;
	}

	public Tbranchstock getObjStock() {
		return objStock;
	}

	public void setObjStock(Tbranchstock objStock) {
		this.objStock = objStock;
	}
}