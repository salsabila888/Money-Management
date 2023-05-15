package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.chart.Charts;
import org.zkoss.chart.Legend;
import org.zkoss.chart.Theme;
import org.zkoss.chart.Tooltip;
import org.zkoss.chart.model.CategoryModel;
import org.zkoss.chart.model.DefaultCategoryModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.DashboardDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MoutletDAO;
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
import com.sdd.caption.domain.Vproductprod;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TpersoListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PersoPlanVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpersoListModel model;
	private TpersoDAO tpersoDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private TembossbranchDAO embossbranchDao = new TembossbranchDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String charttitle;
	private String status;
	private Date date;
	private String filter;
	private String persoid;
	private String productcode;
	private String productname;
	private Date persodate;
	private Date orderdate;
	private Integer totalselected;
	private Integer totaldataselected;
	private String processtype;
	private Mpersovendor mpersovendor;
	private List<Vproductprod> oList;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	private Map<Integer, Tperso> mapData = new HashMap<>();
	// private Map<Date, Mholiday> mapHoliday = new HashMap<>();

	@Wire
	private Charts chart;
	@Wire
	private Charts chartSlaWarning;
	@Wire
	private Div divChartOrder;
	@Wire
	private Div divChartpendingreason;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Grid gridStatus;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doResetListSelected();
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
								mapData.put(data.getTpersopk(), data);
								totaldataselected += obj.getTotaldata();
							} else {
								mapData.remove(data.getTpersopk());
								totaldataselected -= obj.getTotaldata();
							}
							totalselected = mapData.size();
							BindUtils.postNotifyChange(null, null, PersoPlanVm.this, "totalselected");
							BindUtils.postNotifyChange(null, null, PersoPlanVm.this, "totaldataselected");
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
							map.put("iswrite", new Boolean(true));

							Window win = (Window) Executions.createComponents("/view/perso/persodata.zul", null, map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									Boolean isSaved = (Boolean) event.getData();
									if (isSaved != null && isSaved) {
										// doReset();
										needsPageUpdate = true;
										refreshModel(pageStartNumber);
										BindUtils.postNotifyChange(null, null, PersoPlanVm.this, "*");
									}
								}
							});
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getPersostarttime())));
					row.getChildren().add(new Label(data.getMproduct().getProductcode()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
					row.getChildren().add(new Label(
							data.getOrderdate() != null ? dateLocalFormatter.format(data.getOrderdate()) : ""));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
					row.getChildren().add(new Label(
							data.getMpersovendor() != null ? data.getMpersovendor().getVendorcode() : "INTERNAL"));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
				}

			});
		}
		doReset();
	}

	@NotifyChange("charttitle")
	@Command
	public void doChartOrder() {
		if (date != null) {
			try {
				charttitle = "Perso Plan Operator Mesin ";
				oList = new DashboardDAO().getDataPersoPlan(dateFormatter.format(date));

				CategoryModel model = new DefaultCategoryModel();
				for (Vproductprod obj : oList) {
					model.setValue(obj.getProductcode(), obj.getProductcode(), obj.getTotaldata());
				}

				chart.setModel(model);
				chart.getXAxis().setMin(0);
				chart.getXAxis().getTitle().setText("Jenis Order");
				chart.getYAxis().getTitle().setText("Jumlah Data");

				/*
				 * Legend legend = chart.getLegend(); legend.setLayout("vertical");
				 * legend.setAlign("right"); legend.setVerticalAlign("top"); legend.setX(-40);
				 * legend.setY(100); legend.setFloating(true); legend.setBorderWidth(1);
				 * legend.setShadow(true);
				 */

				Legend legend = chart.getLegend();
				legend.setLayout("vertical");
				legend.setAlign("right");
				legend.setVerticalAlign("middle");
				legend.setBorderWidth(0);

				Tooltip tooltip = chart.getTooltip();
				tooltip.setHeaderFormat("<span style=\"font-size:10px\">{point.key}</span><table>");
				tooltip.setPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td>"
						+ "<td style=\"padding:0\"><b>{point.y}</b></td></tr>");
				tooltip.setFooterFormat("</table>");
				tooltip.setShared(true);
				tooltip.setUseHTML(true);

				chart.getPlotOptions().getColumn().setPointPadding(0.2);
				chart.getPlotOptions().getColumn().setBorderWidth(0);

				chart.setTheme(Theme.GRID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@NotifyChange({ "totalselected", "totaldataselected" })
	private void doResetListSelected() {
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
	}

	@Command
	@NotifyChange("*")
	public void doDone() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			Messagebox.show("Anda ingin update status berhasil \nperso?", "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								System.out.println("PROCESSING...");
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
														"tperso.tpersopk = " + objForm.getTpersopk(), "tpersodatapk")) {
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
														paketdata.setNopaket(new TcounterengineDAO().generateNopaket());
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
														new TembossbranchDAO().save(session, data.getTembossbranch());

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
											} else {
												objForm.setPersofinishby(oUser.getUserid());
												objForm.setPersofinishtime(new Date());
												objForm.setStatus(AppUtils.STATUS_PERSO_DONE);
												objForm.setIsgetallpaket("N");
												tpersoDao.save(session, objForm);

												for (Tpersodata data : tpersodataDao.listByFilter(
														"tperso.tpersopk = " + objForm.getTpersopk(), "tpersodatapk")) {
													data.setStatus(AppUtils.STATUS_PERSO_DONE);
													data.setIsgetpaket("N");
													tpersodataDao.save(session, data);

													if (objForm.getTpersoupload() == null
															&& data.getTembossbranch() != null) {
														data.getTembossbranch()
																.setStatus(AppUtils.STATUSBRANCH_PENDINGPAKET);
														embossbranchDao.save(session, data.getTembossbranch());
													}

												}
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

										try {
											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketorder'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(), "01",
													oUser.getMbranch().getBranchlevel());
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									if (isError)
										Messagebox.show(strError, WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.ERROR);
									else
										Messagebox.show("Proses update status perso selesai",
												WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.INFORMATION);
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
									doResetListSelected();
									BindUtils.postNotifyChange(null, null, PersoPlanVm.this, "*");
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}

					});
		}
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
						chk.setChecked(true);
						mapData.put(obj.getTpersopk(), obj);
						totaldataselected += obj.getTotaldata();
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

	@Command
	public void doViewSelected() {
		if (mapData.size() > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("mapData", mapData);
			map.put("totalselected", totalselected);
			map.put("totaldataselected", totaldataselected);

			Window win = (Window) Executions.createComponents("/view/perso/persoselected.zul", null, map);
			win.setClosable(true);
			win.doModal();
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
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
								BindUtils.postNotifyChange(null, null, PersoPlanVm.this, "*");
							}
						}
					});
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		filter = "status = '" + AppUtils.STATUS_PERSO_PRODUKSI + "'";
		if (persoid != null && persoid.length() > 0)
			filter += " and persoid like '%" + persoid.trim().toUpperCase() + "%'";
		if (productcode != null && productcode.length() > 0)
			filter += " and productcode like '%" + productcode.trim().toUpperCase() + "%'";
		if (productname != null && productname.length() > 0)
			filter += " and productname like '%" + productname.trim().toUpperCase() + "%'";
		if (orderdate != null)
			filter += " and orderdate = '" + dateFormatter.format(orderdate) + "'";
		if (mpersovendor != null) {
			if (mpersovendor.getMpersovendorpk() == -1) {
				filter += " and mpersovendorfk is null";
			} else {
				filter += " and mpersovendorfk = " + mpersovendor.getMpersovendorpk();
			}
		}

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		date = new Date();
		status = "";
		doSearch();
		doChartOrder();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		String orderby = "orderdate";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpersoListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	public ListModelList<Mpersovendor> getMpersovendormodel() {
		ListModelList<Mpersovendor> lm = null;
		try {
			List objList = new ArrayList();
			Mpersovendor obj = new Mpersovendor();
			obj.setMpersovendorpk(-1);
			obj.setVendorcode("INTERNAL");
			obj.setVendorname("INTERNAL");
			objList.add(obj);
			objList.addAll(AppData.getMpersovendor());
			lm = new ListModelList<Mpersovendor>(objList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public String getCharttitle() {
		return charttitle;
	}

	public void setCharttitle(String charttitle) {
		this.charttitle = charttitle;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getPageStartNumber() {
		return pageStartNumber;
	}

	public void setPageStartNumber(int pageStartNumber) {
		this.pageStartNumber = pageStartNumber;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public boolean isNeedsPageUpdate() {
		return needsPageUpdate;
	}

	public void setNeedsPageUpdate(boolean needsPageUpdate) {
		this.needsPageUpdate = needsPageUpdate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Paging getPaging() {
		return paging;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getPersoid() {
		return persoid;
	}

	public void setPersoid(String persoid) {
		this.persoid = persoid;
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

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public String getProcesstype() {
		return processtype;
	}

	public void setProcesstype(String processtype) {
		this.processtype = processtype;
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
