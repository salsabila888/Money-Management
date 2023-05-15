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
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TrepairDAO;
import com.sdd.caption.dao.TrepairitemDAO;
import com.sdd.caption.dao.TrepairmemoDAO;
import com.sdd.caption.dao.TreturnitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Trepair;
import com.sdd.caption.domain.Trepairitem;
import com.sdd.caption.domain.Trepairmemo;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TrepairListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class RepairApprovalVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private TrepairDAO oDao = new TrepairDAO();

	private TrepairListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String action;
	private String status;
	private String decisionmemo;
	private String producttype;
	private int flushCounter;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	private Muser oUser;
	private List<Trepair> objSelected = new ArrayList<Trepair>();
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
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("stats") String stats)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
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
			grid.setRowRenderer(new RowRenderer<Trepair>() {
				@Override
				public void render(Row row, final Trepair data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked())
								objSelected.add((Trepair) checked.getAttribute("obj"));
							else
								objSelected.remove((Trepair) checked.getAttribute("obj"));
						}
					});
					row.getChildren().add(check);
					row.getChildren().add(new Label(data.getRegid()));
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
		if (status.equals(AppUtils.STATUS_REPAIR_WAITAPPROVAL)) {
			rba.setValue(AppUtils.STATUS_REPAIR_WAITAPPROVALREPAIROPR);
			rbd.setValue(AppUtils.STATUS_REPAIR_DECLINEAPPROVAL);
			filter = "productgroup = '" + AppUtils.PRODUCTGROUP_PINPAD + "' and status = '"
					+ AppUtils.STATUS_REPAIR_WAITAPPROVAL + "'";
		}
		else if (status.equals(AppUtils.STATUS_REPAIR_WAITAPPROVALREPAIROPR)) {
			rba.setValue(AppUtils.STATUS_REPAIR_PROCESSCHECKING);
			rbd.setValue(AppUtils.STATUS_REPAIR_DECLINEREPAIROPR);
			filter = "productgroup = '" + AppUtils.PRODUCTGROUP_PINPAD + "' and status = '"
					+ AppUtils.STATUS_REPAIR_WAITAPPROVALREPAIROPR + "'";
		}
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			objSelected = new ArrayList<Trepair>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Trepair) chk.getAttribute("obj"));
				} else {
					chk.setChecked(false);
					objSelected.remove((Trepair) chk.getAttribute("obj"));
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
		objSelected = new ArrayList<Trepair>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "trepairpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TrepairListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
				if (action.equals(AppUtils.STATUS_REPAIR_WAITAPPROVALREPAIROPR)
						|| action.equals(AppUtils.STATUS_REPAIR_DECLINEAPPROVAL) && decisionmemo != null && decisionmemo.trim().length() > 0
						|| action.equals(AppUtils.STATUS_REPAIR_PROCESSCHECKING)
						|| action.equals(AppUtils.STATUS_REPAIR_DECLINEREPAIROPR) && decisionmemo != null && decisionmemo.trim().length() > 0) {
					Session session = StoreHibernateUtil.openSession();
					Transaction transaction = session.beginTransaction();
					try {
						for (Trepair obj : objSelected) {
							if (action.equals(AppUtils.STATUS_REPAIR_WAITAPPROVAL)
									|| action.equals(AppUtils.STATUS_REPAIR_WAITAPPROVALREPAIROPR)
									|| action.equals(AppUtils.STATUS_REPAIR_PROCESSCHECKING)) {
								flushCounter = 0;
								if (action.equals(AppUtils.STATUS_REPAIR_WAITAPPROVALREPAIROPR)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
										obj.setStatus(AppUtils.STATUS_REPAIR_WAITAPPROVALREPAIROPR);
										
										List<Trepairitem> objList = new TrepairitemDAO().listByFilter("trepairfk = " + obj.getTrepairpk(), "trepairitempk");
										for (Trepairitem data : objList) {
											Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
													+ data.getItemno() + "' and status = '" + data.getItemstatus() + "'");
											if (objStock != null) {
												objStock.setStatus(obj.getStatus());
												new TbranchstockitemDAO().save(session, objStock);

												Tbranchitemtrack objTrack = new Tbranchitemtrack();
												objTrack.setItemno(data.getItemno());
												objTrack.setTracktime(new Date());
												objTrack.setTrackdesc(AppData.getStatusLabel(data.getItemstatus()));
												objTrack.setProductgroup(data.getTrepair().getMproduct().getProductgroup());
												objTrack.setMproduct(data.getTrepair().getMproduct());
												objTrack.setTrackstatus(obj.getStatus());
												new TbranchitemtrackDAO().save(session, objTrack);
											}
											data.setItemstatus(obj.getStatus());
											new TrepairitemDAO().save(session, data);
										}
										
									}
									
									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/repair/repairapproval.zul' and menuparamvalue = 'E2'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel() - 2);
									
									mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/repair/repairapproval.zul' and menuparamvalue = 'E1'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} else if (action.equals(AppUtils.STATUS_REPAIR_PROCESSCHECKING)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
										obj.setStatus(AppUtils.STATUS_REPAIR_PROCESSCHECKING);
									}
									
									List<Trepairitem> objList = new TrepairitemDAO().listByFilter("trepairfk = " + obj.getTrepairpk(), "trepairitempk");
									for (Trepairitem data : objList) {
										Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
												+ data.getItemno() + "' and status = '" + data.getItemstatus() + "'");
										System.out.println(objStock.getItemno());
										if (objStock != null) {
											objStock.setStatus(obj.getStatus());
											new TbranchstockitemDAO().save(session, objStock);

											Tbranchitemtrack objTrack = new Tbranchitemtrack();
											objTrack.setItemno(data.getItemno());
											objTrack.setTracktime(new Date());
											objTrack.setTrackdesc(AppData.getStatusLabel(data.getItemstatus()));
											objTrack.setProductgroup(data.getTrepair().getMproduct().getProductgroup());
											objTrack.setMproduct(data.getTrepair().getMproduct());
											objTrack.setTrackstatus(obj.getStatus());
											new TbranchitemtrackDAO().save(session, objTrack);
										}
										data.setItemstatus(obj.getStatus());
										new TrepairitemDAO().save(session, data);
									}
									
									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/repair/repairlist.zul'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
									
									mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/repair/repairapproval.zul' and menuparamvalue = 'E2'");
									NotifHandler.delete(mmenu, obj.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								}
								Trepairmemo objrm = new Trepairmemo();
								objrm.setTrepair(obj);
								objrm.setMemotime(new Date());
								objrm.setMemoby(oUser.getUsername());
								objrm.setMemo(decisionmemo);
								new TrepairmemoDAO().save(session, objrm);

								obj.setDecisionby(oUser.getUsername());
								obj.setDecisiontime(new Date());
								oDao.save(session, obj);

								transaction.commit();
								Clients.showNotification("Proses Approve data berhasil", "info", null, "middle_center",
										3000);
							} else if (action.equals(AppUtils.STATUS_REPAIR_DECLINEAPPROVAL)
									|| action.equals(AppUtils.STATUS_REPAIR_DECLINEREPAIROPR)) {
								flushCounter = 0;
								if (action.equals(AppUtils.STATUS_REPAIR_DECLINEAPPROVAL)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
										obj.setStatus(AppUtils.STATUS_REPAIR_DECLINEAPPROVAL);
									}
									
									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/repair/repairapproval.zul' and menuparamvalue = 'E1'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								}
								if (action.equals(AppUtils.STATUS_REPAIR_DECLINEREPAIROPR)) {
									if (obj.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
										obj.setStatus(AppUtils.STATUS_REPAIR_DECLINEREPAIROPR);
									}
									
									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/repair/repairapproval.zul' and menuparamvalue = 'E2'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								}
								Trepairmemo objrm = new Trepairmemo();
								objrm.setTrepair(obj);
								objrm.setMemotime(new Date());
								objrm.setMemoby(oUser.getUsername());
								objrm.setMemo(decisionmemo);
								new TrepairmemoDAO().save(session, objrm);

								obj.setDecisionby(oUser.getUsername());
								obj.setDecisiontime(new Date());
								oDao.save(session, obj);
								

								List<Trepairitem> objList = new TrepairitemDAO()
										.listByFilter("trepairfk = " + obj.getTrepairpk(), "trepairitempk");
								for (Trepairitem data : objList) {
									Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
											+ data.getItemno() + "' and status = '" + data.getItemstatus() + "'");
									if (objStock != null) {
										objStock.setStatus(AppUtils.STATUS_SERIALNO_ENTRY);
										new TbranchstockitemDAO().save(session, objStock);

										Tbranchitemtrack objTrack = new Tbranchitemtrack();
										objTrack.setItemno(data.getItemno());
										objTrack.setTracktime(new Date());
										objTrack.setTrackdesc(AppData.getStatusLabel(data.getItemstatus()));
										objTrack.setProductgroup(data.getTrepair().getMproduct().getProductgroup());
										objTrack.setMproduct(data.getTrepair().getMproduct());
										objTrack.setTrackstatus(obj.getStatus());
										new TbranchitemtrackDAO().save(session, objTrack);
									}
									data.setItemstatus(obj.getStatus());
									new TrepairitemDAO().save(session, data);
								}
								
								for (Entry<Integer, Tbranchstock> entry : mapStock.entrySet()) {
									Tbranchstock stock = entry.getValue();
									stock.setStockcabang(stock.getStockcabang() + obj.getItemqty());
									stock.setStockactivated(stock.getStockactivated() - obj.getItemqty());
									new TbranchstockDAO().save(session, stock);
								}

								transaction.commit();
								Clients.showNotification("Proses Decline Approval data berhasil", "info", null,
										"middle_center", 3000);
							}
						}
						doReset();
					} catch (Exception e) {
						transaction.rollback();
						e.printStackTrace();
					} finally {
						session.close();
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
}