package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.dao.TswitchitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.domain.Tswitchitem;
import com.sdd.caption.handler.ItemTrackHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TbranchStockItemListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class CardSwitchingVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Tswitch obj;
	private Tbranchstock objStock;
	private TbranchStockItemListModel model;

	private TswitchDAO oDao = new TswitchDAO();
	private TbranchstockDAO stockDao = new TbranchstockDAO();
	private TbranchstockitemDAO itemDao = new TbranchstockitemDAO();

	private List<String> cardnoList = new ArrayList<>();
	private List<Tbranchstockitem> getdataList = new ArrayList<>();
	private Map<Integer, Tbranchstockitem> mapData = new HashMap<Integer, Tbranchstockitem>();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer totaldata;
	private String cardno;
	private Integer totalselected;

	@Wire
	private Paging paging;
	@Wire
	private Grid grid, gridGetdata;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Window winBranchGetData;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tswitch obj)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.obj = obj;
		doReset();

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		grid.setRowRenderer(new RowRenderer<Tbranchstockitem>() {

			@Override
			public void render(final Row row, final Tbranchstockitem data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tbranchstockitem ted = (Tbranchstockitem) checked.getAttribute("obj");
						if (checked.isChecked()) {
							if (!cardnoList.contains(ted.getItemno())) {
								mapData.put(ted.getTbranchstockitempk(), data);
								cardnoList.add(data.getItemno());
								check.setChecked(true);
							} else {
								Messagebox.show("No Kartu sudah terdaftar di get data.", "Info", Messagebox.OK,
										Messagebox.INFORMATION);
								check.setChecked(false);
							}
						} else {
							mapData.remove(ted.getTbranchstockitempk());
							cardnoList.remove(data.getItemno());
							check.setChecked(false);
						}
					}
				});
				if (mapData.get(data.getTbranchstockitempk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getItemno()));
				row.getChildren().add(new Label(data.getTbranchstock().getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getTbranchstock().getMproduct().getProductname()));
			}
		});

		gridGetdata.setRowRenderer(new RowRenderer<Tbranchstockitem>() {

			@Override
			public void render(final Row row, final Tbranchstockitem data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getItemno()));
				row.getChildren().add(new Label(data.getTbranchstock().getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getTbranchstock().getMproduct().getProductname()));

				Button btnCancel = new Button("Cancel");
				btnCancel.setSclass("btn btn-danger btn-sm");
				btnCancel.setAutodisable("self");
				btnCancel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
								Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

									@Override
									public void onEvent(Event event) throws Exception {
										if (event.getName().equals("onOK")) {
											getdataList.remove(data);

											cardnoList.remove(data.getItemno());
											gridGetdata.setModel(new ListModelList<>(getdataList));
											totaldata = getdataList.size();
											BindUtils.postNotifyChange(null, null, CardSwitchingVm.this, "totaldata");
										}
									}
								});
					}
				});

				row.getChildren().add(btnCancel);

				gridGetdata.getRows().insertBefore(row, gridGetdata.getRows().getFirstChild());
			}
		});
	}

	@Command
	@NotifyChange("totalselected")
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			int statusfail = 0;
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tbranchstockitem ted = (Tbranchstockitem) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						if (cardnoList.contains(ted.getItemno()) || cardnoList.size() >= obj.getItemqty()) {
							chk.setChecked(false);
							statusfail++;
						} else {
							chk.setChecked(true);
							mapData.put(ted.getTbranchstockitempk(), ted);
							cardnoList.add(ted.getItemno());
						}
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getItemqty());
						cardnoList.remove(ted.getItemno());
					}
				}
			}
			if (statusfail > 0) {
				Messagebox.show("Ada beberapa data belum bisa dipilih mohon diperiksa kembali", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
			totalselected = mapData.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("totaldata")
	public void doSave() {
		try {
			if (mapData.size() > 0) {
				if (mapData.size() <= obj.getItemqty()) {
					for (Entry<Integer, Tbranchstockitem> entry : mapData.entrySet()) {
						Tbranchstockitem data = entry.getValue();
						getdataList.add(data);
					}
					gridGetdata.setModel(new ListModelList<>(getdataList));
					totaldata = getdataList.size();
					gridGetdata.setFocus(true);
				} else {
					Messagebox.show("Jumlah data yang dipilih melebihi jumlah order", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Tidak ada data yang dipilih", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
			mapData = new HashMap<>();
			doSearch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSubmit() {
		if (totaldata.equals(0)) {
			Messagebox.show("Tidak ada data untuk di Submit", WebApps.getCurrent().getAppName(), Messagebox.OK,
					Messagebox.EXCLAMATION);
		} else {
			Messagebox.show("Apakah anda ingin menyelesaikan proses get data dan melanjutkan ke proses selanjutnya?",
					"Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
					new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								try {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();

									String status = "";
									if (!obj.getOutletreq().equals("00")) {
										status = AppUtils.STATUS_DELIVERY_DELIVERED;
									} else {
										status = AppUtils.STATUS_DELIVERY_PAKETDONE;
									}

									obj.setStatus(status);
									oDao.save(session, obj);

									obj.getTorder().setStatus(status);
									obj.getTorder().setTotalproses(getdataList.size());
									new TorderDAO().save(session, obj.getTorder());

									for (Tbranchstockitem data : getdataList) {
										data.setStatus(AppUtils.STATUS_SERIALNO_OUTINVENTORY);
										itemDao.save(session, data);

										Torderitem tori = new Torderitem();
										tori.setProductgroup(AppUtils.PRODUCTGROUP_DOCUMENT);
										tori.setItemno(data.getItemno());
										tori.setNumerator(data.getNumerator());
										tori.setTorder(obj.getTorder());
										new TorderitemDAO().save(session, tori);

										Tswitchitem objItem = new Tswitchitem();
										objItem.setItemno(data.getItemno());
										objItem.setTswitch(obj);
										new TswitchitemDAO().save(session, objItem);
										
										ItemTrackHandler.Run(data, session);
									}
									
									Tpaket paket = new Tpaket();
									paket.setMproduct(obj.getMproduct());
									paket.setOrderdate(obj.getTorder().getOrderdate());
									paket.setPaketid(
											new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_PAKET));
									paket.setProcessedby(oUser.getUserid());
									paket.setProcesstime(new Date());
									paket.setProductgroup(obj.getMproduct().getProductgroup());
									paket.setStatus(status);
									paket.setTotaldata(obj.getTorder().getTotalproses());
									paket.setTotaldone(1);
									paket.setTorder(obj.getTorder());
									paket.setTswitch(obj);
									paket.setBranchpool(oUser.getMbranch().getBranchid());
									new TpaketDAO().save(session, paket);

									Tpaketdata paketdata = new Tpaketdata();
									paketdata.setNopaket(new TcounterengineDAO().generateNopaket());
									paketdata.setIsdlv("N");
									if (!obj.getOutletreq().equals("00")) {
										paketdata.setIsdlv("Y");
									}
									paketdata.setMbranch(obj.getTorder().getMbranch());
									paketdata.setOrderdate(paket.getOrderdate());
									paketdata.setPaketfinishby(oUser.getUserid());
									paketdata.setPaketfinishtime(new Date());
									paketdata.setPaketstartby(oUser.getUserid());
									paketdata.setPaketstarttime(new Date());
									paketdata.setProductgroup(paket.getProductgroup());
									paketdata.setQuantity(obj.getTorder().getTotalproses());
									paketdata.setStatus(status);
									paketdata.setZipcode(null);
									paketdata.setTpaket(paket);
									new TpaketdataDAO().save(session, paketdata);

									objStock.setStockactivated(objStock.getStockactivated() + getdataList.size());
									objStock.setStockcabang(objStock.getStockcabang() - getdataList.size());
									stockDao.save(session, objStock);

									if (!obj.getOutletreq().equals("00")) {
										Tbranchstock objStockpool = stockDao
												.findByFilter("mbranchfk = " + obj.getTorder().getMbranch().getMbranchpk()
														+ " and mproductfk = " + obj.getTorder().getMproduct().getMproductpk()
														+ " and outlet = '" + obj.getOutletreq() + "'");
										if(objStockpool == null) {
											objStockpool = new Tbranchstock();
											objStockpool.setMbranch(obj.getTorder().getMbranch());
											objStockpool.setMproduct(obj.getTorder().getMproduct());
											objStockpool.setStockdelivered(0);
											objStockpool.setStockactivated(0);
											objStockpool.setStockreserved(0);
											objStockpool.setProductgroup(obj.getTorder().getProductgroup());
											objStockpool.setStockcabang(0);
											objStockpool.setOutlet(obj.getTorder().getOrderoutlet());
										}
										
										objStockpool.setStockcabang(objStockpool.getStockcabang() + getdataList.size());
										objStockpool.setStockdelivered(objStockpool.getStockdelivered() + getdataList.size());
										stockDao.save(session, objStockpool);
										
										for (Tbranchstockitem data : getdataList) {
											Tbranchstockitem objItem = new Tbranchstockitem();
											objItem.setItemno(data.getItemno());
											objItem.setProductgroup(data.getProductgroup());
											objItem.setStatus(AppUtils.STATUS_SERIALNO_ENTRY);
											objItem.setTbranchstock(objStockpool);
											itemDao.save(session, objItem);
											
											ItemTrackHandler.Run(data, session);
										}
									} else {
										Mmenu mmenu = new MmenuDAO()
												.findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									}

									transaction.commit();
									session.close();

									Clients.showNotification("Proses verified data order berhasil", "info", null,
											"middle_center", 3000);
									Event closeEvent = new Event("onClose", winBranchGetData, new Boolean(true));
									Events.postEvent(closeEvent);
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						}
					});
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		try {
			if (obj != null) {
				Tbranchstock stock = stockDao
						.findByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and mproductfk = "
								+ obj.getMproduct().getMproductpk() + " and outlet = '" + obj.getOutletpool() + "'");
				if (stock != null) {
					objStock = stock;
					filter = "tbranchstockfk = " + stock.getTbranchstockpk() + " and status = '"
							+ AppUtils.STATUS_SERIALNO_ENTRY + "'";
					;

					if (cardno != null && cardno.length() > 0) {
						if (filter.length() > 0)
							filter += " and ";
						filter += "itemno like '%" + cardno.trim().toUpperCase() + "%'";
					}
				}
				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "itemno";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TbranchStockItemListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		try {
			cardno = null;
			totaldata = 0;
			mapData = new HashMap<>();
			getdataList = new ArrayList<>();
			doSearch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Tswitch getObj() {
		return obj;
	}

	public void setObj(Tswitch obj) {
		this.obj = obj;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}
}
