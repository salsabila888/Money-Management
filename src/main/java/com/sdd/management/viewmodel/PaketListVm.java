package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
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
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.handler.PaketManifestHandler;
import com.sdd.caption.model.TpaketListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PaketListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpaketListModel model;
	private TpaketDAO oDao = new TpaketDAO();
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TembossbranchDAO tembossbranchDao = new TembossbranchDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer year;
	private Integer month;
	private Integer day;
	private String paketid, productcode, productname;
	private Date processtime;
	private String type;
	private String status;
	private Integer totalselected;
	private Integer totaldataselected;
	private Map<Integer, Tpaket> mapData;
	private Date date;
	private Integer total;
	private Integer totaldone;

	List<Tpaketdata> objList;

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
	private Checkbox chkAll;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
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
		grid.setRowRenderer(new RowRenderer<Tpaket>() {

			@Override
			public void render(Row row, final Tpaket data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tpaket obj = (Tpaket) checked.getAttribute("obj");
						if (checked.isChecked()) {
							mapData.put(obj.getTpaketpk(), obj);
							totaldataselected += obj.getTotaldata();
						} else {
							mapData.remove(obj.getTpaketpk());
							totaldataselected -= obj.getTotaldata();
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, PaketListVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, PaketListVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTpaketpk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				A a = new A(data.getPaketid());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);

						Window win = (Window) Executions.createComponents("/view/delivery/paketdata.zul", null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								Boolean isSaved = (Boolean) event.getData();
								if (isSaved != null && isSaved) {
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
									BindUtils.postNotifyChange(null, null, PaketListVm.this, "pageTotalSize");
								}
							}
						});
						win.doModal();
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getProcesstime())));
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
				row.getChildren().add(new Label(data.getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getTperso().getPersofinishtime())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldone())));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
			}

		});

		doReset();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpaketpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpaketListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Tpaket obj = (Tpaket) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getTpaketpk(), obj);
							totaldataselected += obj.getTotaldata();
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getTpaketpk());
							totaldataselected -= obj.getTotaldata();
						}
					}
				}
				totalselected = mapData.size();
			}
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

	@NotifyChange("*")
	@Command
	public void doResetSelected() {
		if (mapData.size() > 0) {
			Messagebox.show("Anda ingin mereset data-data yang sudah anda pilih?", "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								totalselected = 0;
								totaldataselected = 0;
								mapData = new HashMap<>();
								refreshModel(pageStartNumber);
								BindUtils.postNotifyChange(null, null, PaketListVm.this, "*");
							}
						}
					});
		}
	}

	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Tpaket> listData = oDao.listNativeByFilter(filter, orderby);
				if (listData != null && listData.size() > 0) {
					PaketManifestHandler.doDataPrint(listData);
				} else {
					Messagebox.show("Data tidak tersedia", WebApps.getCurrent().getAppName(), Messagebox.OK,
							Messagebox.INFORMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
		}
	}

	@Command
	public void doPrintLabel() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			try {
				objList = new ArrayList<>();
				for (Entry<Integer, Tpaket> entry : mapData.entrySet()) {
					Tpaket data = entry.getValue();
					objList.addAll(tpaketdataDao.listByFilter("tpaketfk = " + data.getTpaketpk(), "orderdate"));
				}

				Collections.sort(objList, Tpaketdata.branchidComparator);
				PaketManifestHandler.doLabelPrint(objList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	public void doDoneSorting() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			boolean isValid = true;
			for (Entry<Integer, Tpaket> entry : mapData.entrySet()) {
				Tpaket obj = entry.getValue();
				if (!obj.getStatus().equals(AppUtils.STATUS_DELIVERY_PAKETPROSES)) {
					isValid = false;
					Messagebox.show(
							"Proses update status tidak bisa \ndilakukan karena terdapat data \ndengan status bukan proses paket. \nSilahkan periksa kembali \ndata-data yang anda pilih",
							"Info", Messagebox.OK, Messagebox.INFORMATION);
					break;
				}
			}
			if (isValid) {
				Messagebox.show("Anda ingin update status done?", "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL,
						Messagebox.QUESTION, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									String error = "";
									int totaldone;
									for (Entry<Integer, Tpaket> entry : mapData.entrySet()) {
										Session session = StoreHibernateUtil.openSession();
										Transaction transaction = session.beginTransaction();
										try {
											Tpaket objPaket = entry.getValue();
											objPaket.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);

											totaldone = 0;
											List<Tpaketdata> listPaketdata = tpaketdataDao.listByFilter(
													"tpaket.tpaketpk = " + objPaket.getTpaketpk() + " and isdlv != 'Y'",
													"tpaketdatapk");
											for (Tpaketdata data : listPaketdata) {
												data.setPaketfinishby(oUser.getUserid());
												data.setPaketfinishtime(new Date());
												data.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
												data.setIsdlv("N");
												tpaketdataDao.save(session, data);

												data.getTembossbranch()
														.setStatus(AppUtils.STATUSBRANCH_PENDINGDELIVERY);
												data.getTembossbranch().setDlvstarttime(new Date());
												tembossbranchDao.save(session, data.getTembossbranch());

												totaldone = totaldone + data.getQuantity();
												
												Mmenu mmenu = new MmenuDAO()
														.findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
												NotifHandler.doNotif(mmenu, oUser.getMbranch(), "01",
														oUser.getMbranch().getBranchlevel());
											}

											objPaket.setTotaldone(objPaket.getTotaldone() + totaldone);
											oDao.save(session, objPaket);

											objPaket.getTperso().setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
											new TpersoDAO().save(session, objPaket.getTperso());
											
											Mmenu mmenu = new MmenuDAO().findByFilter("menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketlist'");
											NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD,
													oUser.getMbranch().getBranchlevel());
											
											transaction.commit();
										} catch (HibernateException e) {
											transaction.rollback();
											e.printStackTrace();
											if (error.length() > 0)
												error += ". \n";
											error += e.getMessage();
										} catch (Exception e) {
											e.printStackTrace();
											if (error.length() > 0)
												error += ". \n";
											error += e.getMessage();
										} finally {
											session.close();
										}
									}

									if (error.length() > 0)
										Messagebox.show(error, WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.ERROR);
									else {
										Messagebox.show("Proses update status done paket selesai",
												WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.INFORMATION);
									}
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
									doResetListSelected();
									BindUtils.postNotifyChange(null, null, PaketListVm.this, "*");
								}
							}
						});
			}
		}
	}

	@Command
	public void doPrintOutput() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			try {
				List<Tpaketdata> objList = new ArrayList<>();
				for (Entry<Integer, Tpaket> entry : mapData.entrySet()) {
					Tpaket data = entry.getValue();
					objList.addAll(
							tpaketdataDao.listByFilter("tpaket.tpaketpk = " + data.getTpaketpk(), "tpaketdatapk"));
				}
				PaketManifestHandler.doOutputPrint(objList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (processtime != null) {
			if (type.equals("O"))
				filter = "orderdate = '" + dateFormatter.format(processtime) + "' and tpaket.productgroup = '"
						+ AppUtils.PRODUCTGROUP_CARD + "'";
			else
				filter = "Date(processtime) = '" + dateFormatter.format(processtime) + "' and tpaket.productgroup = '"
						+ AppUtils.PRODUCTGROUP_CARD + "'";
			if (paketid != null && paketid.length() > 0)
				filter += " and paketid like '%" + paketid.trim().toUpperCase() + "%'";
			if (productcode != null && productcode.length() > 0)
				filter += " and mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
			if (productname != null && productname.length() > 0)
				filter += " and mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
			if (status != null && status.length() > 0)
				filter += " and tpaket.status = '" + status + "'";
			else
				filter += " and tpaket.status != '" + AppUtils.STATUS_DELIVERY_PAKETORDER + "'";

			filter += " and tderivatifproductfk is null";

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		processtime = new Date();
		type = "P";
		status = AppUtils.STATUS_DELIVERY_PAKETPROSES;
		doSearch();
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
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

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public String getPaketid() {
		return paketid;
	}

	public Integer getTotaldone() {
		return totaldone;
	}

	public void setTotaldone(Integer totaldone) {
		this.totaldone = totaldone;
	}

	public void setPaketid(String paketid) {
		this.paketid = paketid;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getTotalselected() {
		return totalselected;
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

	public Date getProcesstime() {
		return processtime;
	}

	public void setProcesstime(Date processtime) {
		this.processtime = processtime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

}
