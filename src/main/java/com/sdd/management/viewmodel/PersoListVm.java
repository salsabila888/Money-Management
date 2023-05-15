package com.sdd.caption.viewmodel;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MoutletDAO;
import com.sdd.caption.dao.MpersovendorDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Moutlet;
import com.sdd.caption.domain.Mpersovendor;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.domain.Vpersostatus;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.handler.PersoPrintHandler;
import com.sdd.caption.model.TpersoListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PersoListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpersoListModel model;

	private TpersoDAO tpersoDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private TembossbranchDAO embossbranchDao = new TembossbranchDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String persoid;
	private Integer year;
	private Integer month;
	private String status;
	private String productcode;
	private String productname;
	private Date orderdate;
	private Date persodate;
	private Integer totalselected;
	private Integer totaldataselected;
	private Mpersovendor mpersovendor;

	private List<Vpersostatus> listPersoStatus = new ArrayList<>();
	private Map<Integer, Tperso> mapData = new HashMap<>();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Grid gridStatus;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Tabs tabs;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		doResetListSelected();
		doTabSetup();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
				chkAll.setChecked(false);
			}
		});
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tperso>() {

				@Override
				public void render(Row row, final Tperso data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							Tperso obj = (Tperso) checked.getAttribute("obj");
							if (checked.isChecked()) {
								if (obj.getStatus().equals(AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL)
										|| obj.getStatus().equals(AppUtils.STATUS_PERSO_PRODUKSI)
										|| obj.getStatus().equals(AppUtils.STATUS_PERSO_DONE)) {
									mapData.put(data.getTpersopk(), data);
									totaldataselected += obj.getTotaldata();
								} else {
									checked.setChecked(false);
									Messagebox.show(
											"Manifest belum bisa diproses karena dalam status "
													+ AppData.getStatusLabel(obj.getStatus()),
											WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
								}
							} else {
								mapData.remove(data.getTpersopk());
								totaldataselected -= obj.getTotaldata();
							}
							totalselected = mapData.size();
							BindUtils.postNotifyChange(null, null, PersoListVm.this, "totalselected");
							BindUtils.postNotifyChange(null, null, PersoListVm.this, "totaldataselected");
						}
					});
					if (mapData.get(data.getTpersopk()) != null)
						check.setChecked(true);
					row.getChildren().add(check);
					A a = new A(data.getPersoid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);

							Window win = (Window) Executions.createComponents("/view/perso/persodata.zul", null, map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									needsPageUpdate = false;
									refreshModel(pageStartNumber);
									doSumStatus();
								}
							});
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getPersostarttime())));
					row.getChildren().add(new Label(data.getMproduct().getProductcode()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
					row.getChildren().add(new Label(
							data.getMpersovendor() != null ? data.getMpersovendor().getVendorcode() : "INTERNAL"));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
				}

			});
			gridStatus.setRowRenderer(new RowRenderer<Vpersostatus>() {

				@Override
				public void render(Row row, Vpersostatus data, int index) throws Exception {
					row.getChildren()
							.add(new Label(StringUtils.getMonthshortLabel(data.getMonth()) + " " + data.getYear()));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
					row.getChildren().add(new Label(String.valueOf(data.getCount())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getSum())));
				}
			});
		}

		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item = new Comboitem();
			item.setLabel(months[i]);
			item.setValue(i + 1);
			cbMonth.appendChild(item);
		}
		doReset();
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tperso obj = (Tperso) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						if (obj.getStatus().equals(AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL)
								|| obj.getStatus().equals(AppUtils.STATUS_PERSO_PRODUKSI)
								|| obj.getStatus().equals(AppUtils.STATUS_PERSO_DONE)) {
							chk.setChecked(true);
							mapData.put(obj.getTpersopk(), obj);
							totaldataselected += obj.getTotaldata();
						}
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getTpersopk());
						totaldataselected -= obj.getTotaldata();
					}
				}
			}
			totalselected = mapData.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doTabSetup() {
		try {
			Tab tab = new Tab("INTERNAL");
			tab.addEventListener(Events.ON_SELECT, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					mpersovendor = null;
					doSearch();
					doResetListSelected();
					chkAll.setChecked(false);
					BindUtils.postNotifyChange(null, null, PersoListVm.this, "*");
				}
			});

			tabs.appendChild(tab);

			for (Mpersovendor vendor : new MpersovendorDAO().listByFilter("0=0", "vendorcode")) {
				Tab tabv = new Tab(vendor.getVendorcode());
				tabv.addEventListener(Events.ON_SELECT, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						mpersovendor = vendor;
						doSearch();
						doResetListSelected();
						chkAll.setChecked(false);
						BindUtils.postNotifyChange(null, null, PersoListVm.this, "*");
					}
				});

				tabs.appendChild(tabv);
			}
			tabs.setTabindex(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange({ "totalselected", "totaldataselected" })
	private void doResetListSelected() {
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
	}

	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Tperso> listData = tpersoDao.listNativeByFilter(filter, orderby);
				if (listData.size() > 0) {
					PersoPrintHandler.doListPrint(listData, month, year, status);
				} else {
					Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK,
							Messagebox.INFORMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
		}
	}

	private void doSumStatus() {
		try {
			listPersoStatus = tpersoDao.listSumOutstangingPerso();
			gridStatus.setModel(new ListModelList<>(listPersoStatus));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doBon() {
		try {
			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				Map<String, Object> map = new HashMap<>();
				map.put("process", "Cetak Bon Kartu");
				Window win = (Window) Executions.createComponents("/view/export/exportformat.zul", null, map);
				win.setClosable(true);
				win.doModal();
				win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getData() != null) {
							@SuppressWarnings("unchecked")
							Map<String, Object> map = (Map<String, Object>) event.getData();
							String format = (String) map.get("format");
							List<Tperso> objList = new ArrayList<>();
							for (Entry<Integer, Tperso> entry : mapData.entrySet()) {
								Tperso obj = entry.getValue();
								objList.add(obj);
							}
							Collections.sort(objList, Tperso.fieldComparator);

							PersoPrintHandler.doBonPrint(objList, format);
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doManifestPrint() {
		try {
			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
			} else {
				List<Tperso> objList = new ArrayList<>();
				for (Entry<Integer, Tperso> entry : mapData.entrySet()) {
					Tperso obj = entry.getValue();
					objList.add(obj);
				}
				Collections.sort(objList, Tperso.fieldComparatorProductCode);

				if (mpersovendor == null) {
					Window win = (Window) Executions.createComponents("/view/perso/persomanifestprint.zul", null, null);
					win.setClosable(true);
					win.doModal();
					win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

						@SuppressWarnings("unchecked")
						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getData() != null) {
								try {
									Map<String, Object> map = (Map<String, Object>) event.getData();
									String format = (String) map.get("format");
									String operators = (String) map.get("operators");

									PersoPrintHandler.doManifestPrint(objList, format, operators);

								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});
				} else {
					Map<String, Object> map = new HashMap<>();
					map.put("process", "Cetak Manifest Perso");
					Window win = (Window) Executions.createComponents("/view/export/exportformat.zul", null, map);
					win.setClosable(true);
					win.doModal();
					win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getData() != null) {
								@SuppressWarnings("unchecked")
								Map<String, Object> map = (Map<String, Object>) event.getData();
								String format = (String) map.get("format");

								PersoPrintHandler.doManifestPrint(objList, format, mpersovendor.getVendorname());
							}
						}
					});
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doDone() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			boolean isValid = true;
			for (Entry<Integer, Tperso> entry : mapData.entrySet()) {
				Tperso obj = entry.getValue();
				if (!obj.getStatus().equals(AppUtils.STATUS_PERSO_PRODUKSI)) {
					isValid = false;
					Messagebox.show(
							"Proses update status tidak bisa \ndilakukan karena terdapat data \ndengan status bukan produksi. \nSilahkan periksa kembali \ndata-data yang anda pilih",
							WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
					break;
				}
			}

			if (isValid) {
				Messagebox.show("Anda ingin update status berhasil \nperso?", "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									Session session = null;
									Transaction transaction = null;
									try {
										boolean isError = false;
										String strError = "";
										for (Entry<Integer, Tperso> entry : mapData.entrySet()) {
											Tperso objForm = entry.getValue();
											session = StoreHibernateUtil.openSession();
											transaction = session.beginTransaction();
											try {
												if (objForm.getMproduct().getIsdlvhome().equals("Y")) {
													objForm.setPersofinishby(oUser.getUserid());
													objForm.setPersofinishtime(new Date());
													objForm.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
													objForm.setIsgetallpaket("Y");
													tpersoDao.save(session, objForm);

													Tpaket paket = new Tpaket();
													paket.setTperso(objForm);
													paket.setMproduct(objForm.getMproduct());
													paket.setTembossproduct(objForm.getTembossproduct());
													paket.setPaketid(new TcounterengineDAO()
															.generateYearMonthCounter(AppUtils.CE_PAKET));
													paket.setProductgroup(objForm.getMproduct().getProductgroup());
													paket.setTotaldone(0);
													paket.setOrderdate(objForm.getOrderdate());
													paket.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
													paket.setBranchpool(oUser.getMbranch().getBranchid());
													paket.setProcessedby(oUser.getUserid());
													paket.setProcesstime(new Date());

													int totalpaket = 0;
													for (Tpersodata data : tpersodataDao.listByFilter(
															"tperso.tpersopk = " + objForm.getTpersopk(),
															"tpersodatapk")) {
														data.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
														data.setIsgetpaket("Y");
														tpersodataDao.save(session, data);

														for (Tembossdata ted : new TembossdataDAO().listByFilter(
																"tembossbranchfk = "
																		+ data.getTembossbranch().getTembossbranchpk(),
																"productcode, branchid")) {

															Tpaketdata paketdata = new Tpaketdata();
															paketdata.setTpaket(paket);
															paketdata.setTembossbranch(data.getTembossbranch());
															paketdata.setNopaket(
																	new TcounterengineDAO().generateNopaket());
															paketdata.setProductgroup(
																	data.getTperso().getMproduct().getProductgroup());
															paketdata.setMbranch(data.getMbranch());
															paketdata.setOrderdate(data.getTperso().getOrderdate());
															paketdata.setQuantity(1);
															paketdata.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
															paketdata.setIsdlv("N");
															paketdata.setAccno(ted.getAccno());
															paketdata.setAddress1(ted.getAddress1());
															paketdata.setAddress2(ted.getAddress2());
															paketdata.setAddress3(ted.getAddress3());
															paketdata.setHpno(ted.getHpno());
															paketdata.setCardno(ted.getCardno());
															paketdata.setCustname(ted.getNameonid());

															List<Moutlet> city = new MoutletDAO().listByFilter(
																	"zipcode = '" + ted.getZipcode().trim() + "'",
																	"moutletpk");
															if (city.size() > 0)
																paketdata.setCity(city.get(0).getOutletcity());
															else
																paketdata.setCity("");

															paketdata.setZipcode(ted.getZipcode());
															paketdata.setPaketstartby(oUser.getUserid());
															paketdata.setPaketstarttime(new Date());
															new TpaketdataDAO().save(session, paketdata);

															data.setIsgetpaket("Y");
															tpersodataDao.save(session, data);

															if (objForm.getTpersoupload() == null) {
																data.getTembossbranch()
																		.setStatus(AppUtils.STATUSBRANCH_PROSESPAKET);
															}
															data.getTembossbranch().setDlvstarttime(new Date());
															new TembossbranchDAO().save(session,
																	data.getTembossbranch());

															totalpaket++;
														}
													}

													paket.setTotaldata(totalpaket);
													new TpaketDAO().save(session, paket);

													Mmenu mmenu = new MmenuDAO().findByFilter(
															"menupath = '/view/delivery/deliveryjobcustomer.zul'");
													NotifHandler.doNotif(mmenu, oUser.getMbranch(),
															AppUtils.PRODUCTGROUP_CARD,
															oUser.getMbranch().getBranchlevel());
													
													mmenu = new MmenuDAO().findByFilter(
															"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persolist'");
													NotifHandler.delete(mmenu, oUser.getMbranch(),
															AppUtils.PRODUCTGROUP_CARD,
															oUser.getMbranch().getBranchlevel());
												} else {
													objForm.setPersofinishby(oUser.getUserid());
													objForm.setPersofinishtime(new Date());
													objForm.setStatus(AppUtils.STATUS_PERSO_DONE);
													objForm.setIsgetallpaket("N");
													tpersoDao.save(session, objForm);

													for (Tpersodata data : tpersodataDao.listByFilter(
															"tperso.tpersopk = " + objForm.getTpersopk(),
															"tpersodatapk")) {
														data.setStatus(AppUtils.STATUS_PERSO_DONE);
														data.setIsgetpaket("N");
														tpersodataDao.save(session, data);

														if (objForm.getTpersoupload() == null) {
															data.getTembossbranch()
																	.setStatus(AppUtils.STATUSBRANCH_PENDINGPAKET);
															embossbranchDao.save(session, data.getTembossbranch());
														}

													}

													Mmenu mmenu = new MmenuDAO().findByFilter(
															"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketorder'");
													NotifHandler.doNotif(mmenu, oUser.getMbranch(),
															AppUtils.PRODUCTGROUP_CARD,
															oUser.getMbranch().getBranchlevel());

													mmenu = new MmenuDAO().findByFilter(
															"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persolist'");
													NotifHandler.delete(mmenu, oUser.getMbranch(),
															AppUtils.PRODUCTGROUP_CARD,
															oUser.getMbranch().getBranchlevel());
												}

												transaction.commit();
											} catch (HibernateException e) {
												transaction.rollback();
												isError = true;
												if (strError.length() > 0)
													strError += ". \n";
												strError += e.getMessage();
												e.printStackTrace();
											} catch (Exception e) {
												transaction.rollback();
												isError = true;
												if (strError.length() > 0)
													strError += ". \n";
												strError += e.getMessage();
												e.printStackTrace();
											} finally {
												session.close();
											}
										}
										if (isError)
											Messagebox.show(strError, WebApps.getCurrent().getAppName(), Messagebox.OK,
													Messagebox.ERROR);
										else
											Messagebox.show("Proses update status perso selesai",
													WebApps.getCurrent().getAppName(), Messagebox.OK,
													Messagebox.INFORMATION);
										needsPageUpdate = false;
										refreshModel(pageStartNumber);
										doSumStatus();
										doResetListSelected();
										BindUtils.postNotifyChange(null, null, PersoListVm.this, "*");
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}

						});
			}
		}
	}

	@NotifyChange("*")
	@Command
	public void doResetSelected() {
		if (mapData.size() > 0) {
			Messagebox.show("Anda ingin mereset data-data yang sudah anda pilih?", "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								doResetListSelected();
								refreshModel(pageStartNumber);
								BindUtils.postNotifyChange(null, null, PersoListVm.this, "*");
							}
						}
					});
		} else {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		if (year != null && month != null) {
			filter = "extract(year from persostarttime) = " + year + " and " + "extract(month from persostarttime) = "
					+ month + " and tderivatifproductfk is null";
			if (status.length() > 0)
				filter += " and status = '" + status + "'";
			if (persoid != null && persoid.length() > 0)
				filter += " and persoid like '%" + persoid.trim().toUpperCase() + "%'";
			if (productcode != null && productcode.length() > 0)
				filter += " and mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
			if (productname != null && productname.length() > 0)
				filter += " and mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
			if (orderdate != null)
				filter += " and tperso.orderdate = '" + dateFormatter.format(orderdate) + "'";
			if (persodate != null)
				filter += " and date(persostarttime) = '" + dateFormatter.format(persodate) + "'";
			if (mpersovendor == null)
				filter += " and mpersovendorfk is null";
			else
				filter += " and mpersovendorfk = " + mpersovendor.getMpersovendorpk();

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		status = "";
		doSearch();
		doSumStatus();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpersopk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpersoListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	/*
	 * public ListModelList<Mpersovendor> getMpersovendormodel() {
	 * ListModelList<Mpersovendor> lm = null; try { List objList = new ArrayList();
	 * Mpersovendor obj = new Mpersovendor(); obj.setMpersovendorpk(-1);
	 * obj.setVendorcode("INTERNAL"); obj.setVendorname("INTERNAL");
	 * objList.add(obj); objList.addAll(AppData.getMpersovendor()); lm = new
	 * ListModelList<Mpersovendor>(objList); } catch (Exception e) {
	 * e.printStackTrace(); } return lm; }
	 */

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getPersoid() {
		return persoid;
	}

	public void setPersoid(String persoid) {
		this.persoid = persoid;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(int totalselected) {
		this.totalselected = totalselected;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public Mpersovendor getMpersovendor() {
		return mpersovendor;
	}

	public void setMpersovendor(Mpersovendor mpersovendor) {
		this.mpersovendor = mpersovendor;
	}

	public Date getPersodate() {
		return persodate;
	}

	public void setPersodate(Date persodate) {
		this.persodate = persodate;
	}

}