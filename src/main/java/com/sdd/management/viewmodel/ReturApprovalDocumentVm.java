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
import com.sdd.caption.domain.Tplan;
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

public class ReturApprovalDocumentVm {
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
	private Integer branchlevel;
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

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("stats") String stats) throws ParseException {
		Selectors.wireComponents(view, this, false);
		productgroup = arg.trim();
		status = stats.trim();

		oUser = (Muser) zkSession.getAttribute("oUser");
		branchlevel = oUser.getMbranch().getBranchlevel();

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
					row.getChildren().add(new Label(data.getRegid()));
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
		if (branchlevel == 3) {
			filter = "status = '" + AppUtils.STATUS_RETUR_WAITAPPROVAL + "'";
		} else if (branchlevel == 2) {
			filter = "status = '" + AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH + "'";
		} else if (branchlevel == 1) {
			filter = "status = '" + AppUtils.STATUS_RETUR_WAITAPPROVALPFA + "'";
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
			System.out.println(objSelected.size());
			System.out.println(action);
			if (action != null && action.length() > 0) {
				if (action.equals(AppUtils.STATUS_APPROVED) || (action.equals(AppUtils.STATUS_DECLINE)
						&& decisionmemo != null && decisionmemo.trim().length() > 0)) {
					try {
						for (Treturn data : objSelected) {
							Session session = StoreHibernateUtil.openSession();
							Transaction transaction = session.beginTransaction();
							data.setDecisionby(oUser.getUsername());
							data.setDecisiontime(new Date());

							if (action.equals(AppUtils.STATUS_APPROVED)) {
								if (branchlevel == 3) {
									if (data.getMreturnreason().getIsDestroy().equals("Y")) {
										data.setStatus(AppUtils.STATUS_RETUR_DESTROYED);
									} else {
										data.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH);

										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalwil'");
										NotifHandler.doNotif(mmenu, data.getMbranch(),
												data.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel() - 1);
									}

									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'approval'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), data.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} else if (branchlevel == 2) {
									if (data.getMreturnreason().getIsDestroy().equals("Y")) {
										data.setStatus(AppUtils.STATUS_RETUR_DESTROYED);
									} else {
										if (data.getReturnlevel() == 3) {
											data.setStatus(AppUtils.STATUS_RETUR_PROCESSWIL);

											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/return/return.zul' and menuparamvalue = 'list'");
											NotifHandler.doNotif(mmenu, data.getMbranch(),
													data.getMproduct().getProductgroup(),
													oUser.getMbranch().getBranchlevel());
										} else {
											data.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVALPFA);

											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalpfa'");
											NotifHandler.doNotif(mmenu, data.getMbranch(),
													data.getMproduct().getProductgroup(),
													oUser.getMbranch().getBranchlevel());
										}
									}

									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalwil'");
									NotifHandler.delete(mmenu, data.getMbranch(), data.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} else {
									if (data.getReturnlevel() == 3) {
										data.setStatus(AppUtils.STATUS_RETUR_PROCESSPFA);

										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/return/return.zul' and menuparamvalue = 'list'");
										NotifHandler.doNotif(mmenu, data.getMbranch(),
												data.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									} else {
										data.setStatus(AppUtils.STATUS_RETUR_RETURNPFA);

										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/return/return.zul' and menuparamvalue = 'returpaket'");
										NotifHandler.doNotif(mmenu, data.getMbranch(),
												data.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									}

									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalpfa'");
									NotifHandler.delete(mmenu, data.getMbranch(), data.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								}

								List<Treturnitem> objList = new TreturnitemDAO()
										.listByFilter("treturnfk = " + data.getTreturnpk(), "treturnitempk");
								for (Treturnitem dataItem : objList) {
									Tbranchstockitem objStock = new TbranchstockitemDAO()
											.findByFilter("itemno = '" + dataItem.getItemno() + "' and status = '"
													+ dataItem.getItemstatus() + "'");
									if (objStock != null) {
										objStock.setStatus(data.getStatus());
										new TbranchstockitemDAO().save(session, objStock);

										Tbranchitemtrack objTrack = new Tbranchitemtrack();
										objTrack.setItemno(dataItem.getItemno());
										objTrack.setTracktime(new Date());
										objTrack.setTrackdesc(AppData.getStatusLabel(dataItem.getItemstatus()));
										objTrack.setProductgroup(dataItem.getTreturn().getMproduct().getProductgroup());
										objTrack.setMproduct(dataItem.getTreturn().getMproduct());
										objTrack.setTrackstatus(data.getStatus());
										new TbranchitemtrackDAO().save(session, objTrack);

										mapStock.put(objStock.getTbranchstock().getTbranchstockpk(),
												objStock.getTbranchstock());
									}
									dataItem.setItemstatus(data.getStatus());
									new TreturnitemDAO().save(session, dataItem);
								}

								for (Entry<Integer, Tbranchstock> entry : mapStock.entrySet()) {
									Tbranchstock stock = entry.getValue();
									if (data.getMreturnreason().getIsDestroy().equals("Y")) {
										stock.setStockdestroyed(stock.getStockdestroyed() + data.getItemqty());
										stock.setStockactivated(stock.getStockactivated() - data.getItemqty());
									}
									new TbranchstockDAO().save(session, stock);
								}

							} else {
								if (decisionmemo != null || !decisionmemo.equals("")
										|| decisionmemo.trim().length() > 0) {
									if (branchlevel == 3) {
										data.setStatus(AppUtils.STATUS_RETUR_DECLINE);

										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/return/return.zul' and menuparamvalue = 'approval'");
										NotifHandler.delete(mmenu, oUser.getMbranch(),
												data.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									} else if (branchlevel == 2) {
										data.setStatus(AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH);

										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalwil'");
										NotifHandler.delete(mmenu, data.getMbranch(),
												data.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									} else {
										data.setStatus(AppUtils.STATUS_RETUR_DECLINEAPPROVALPFA);

										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalpfa'");
										NotifHandler.delete(mmenu, data.getMbranch(),
												data.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									}

									List<Treturnitem> objList = new TreturnitemDAO()
											.listByFilter("treturnfk = " + data.getTreturnpk(), "treturnitempk");
									for (Treturnitem dataItem : objList) {
										Tbranchstockitem objStock = new TbranchstockitemDAO()
												.findByFilter("itemno = '" + dataItem.getItemno() + "' and status = '"
														+ dataItem.getItemstatus() + "'");
										if (objStock != null) {
											objStock.setStatus(AppUtils.STATUS_SERIALNO_ENTRY);
											new TbranchstockitemDAO().save(session, objStock);

											Tbranchitemtrack objTrack = new Tbranchitemtrack();
											objTrack.setItemno(dataItem.getItemno());
											objTrack.setTracktime(new Date());
											objTrack.setTrackdesc(AppData.getStatusLabel(dataItem.getItemstatus()));
											objTrack.setProductgroup(
													dataItem.getTreturn().getMproduct().getProductgroup());
											objTrack.setMproduct(dataItem.getTreturn().getMproduct());
											objTrack.setTrackstatus(data.getStatus());
											new TbranchitemtrackDAO().save(session, objTrack);
										}
										dataItem.setItemstatus(data.getStatus());
										new TreturnitemDAO().save(session, dataItem);
									}

									for (Entry<Integer, Tbranchstock> entry : mapStock.entrySet()) {
										Tbranchstock stock = entry.getValue();
										stock.setStockcabang(stock.getStockcabang() + data.getItemqty());
										stock.setStockactivated(stock.getStockactivated() - data.getItemqty());
										new TbranchstockDAO().save(session, stock);
									}
								}
							}
							oDao.save(session, data);

							Treturnmemo objMemo = new Treturnmemo();
							objMemo.setMemo(decisionmemo);
							objMemo.setMemoby(oUser.getUsername());
							objMemo.setMemotime(new Date());
							objMemo.setTreturn(data);
							new TreturnmemoDAO().save(session, objMemo);

							Treturntrack objrt = new Treturntrack();
							objrt.setTreturn(data);
							objrt.setTracktime(new Date());
							objrt.setTrackstatus(data.getStatus());
							objrt.setTrackdesc(AppData.getStatusLabel(objrt.getTrackstatus()));
							new TreturntrackDAO().save(session, objrt);

							transaction.commit();
							session.close();
						}
						
						if (action.equals(AppUtils.STATUS_APPROVED)) {
							Clients.showNotification("Proses persetujuan data berhasil disetujui", "info", null, "middle_center",
									3000);
						} else {
							Clients.showNotification("Proses persetujuan data berhasil ditolak", "info", null, "middle_center",
									3000);
						}
						
						doReset();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Messagebox.show("Anda harus mengisi Catatan Keputusan", "Info", Messagebox.OK, Messagebox.INFORMATION);
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

	public Integer getBranchlevel() {
		return branchlevel;
	}

	public void setBranchlevel(Integer branchlevel) {
		this.branchlevel = branchlevel;
	}

}