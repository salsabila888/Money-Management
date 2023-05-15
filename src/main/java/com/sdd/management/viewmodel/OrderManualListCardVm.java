package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.HibernateException;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Foot;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.handler.OrderPrintHandler;
import com.sdd.caption.handler.PinmailerHandler;
import com.sdd.caption.model.TorderListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;

public class OrderManualListCardVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private TorderListModel model;
	private TorderDAO oDao = new TorderDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private int totalcheck;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String orderid;
	private String title;
	private Date date;
	private Integer year;
	private Integer month;
	private String productgroup;
	private Integer totalselected;
	private Integer totaldataselected;
	private String ordertype;
	private Map<Integer, Torder> mapData = new HashMap<>();

	// private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Foot foot;
	@Wire
	private Button btnManifest;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		doResetListSelected();
		productgroup = arg;
		title = AppData.getProductgroupLabel(productgroup);

		foot.setVisible(true);

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
				chkAll.setChecked(false);
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
						Torder obj = (Torder) checked.getAttribute("obj");
						if (checked.isChecked()) {
							if (obj.getStatus().equals(AppUtils.STATUS_PRODUCTION_PRODUKSIWAITAPPROVAL)
									|| obj.getStatus().equals(AppUtils.STATUS_PRODUCTION_DECLINEPRODUKSI)
									|| obj.getStatus().equals(AppUtils.STATUS_INVENTORY_OUTGOINGDECLINE)
									|| obj.getStatus().equals(AppUtils.STATUS_ORDER_WAITAPPROVAL)) {
								checked.setChecked(false);
								Messagebox.show(
										"Data belum bisa dipilih karena dalam status "
												+ AppData.getStatusLabel(obj.getStatus()),
										"Info", Messagebox.OK, Messagebox.INFORMATION);
							} else {
								if (obj.getStatus().equals(AppUtils.STATUS_ORDER_PRODUKSI)) {
									totalcheck++;
									System.out.println("TOTAL CHECK : " + totalcheck);
								}
								mapData.put(data.getTorderpk(), data);
								totaldataselected += obj.getTotalqty();
							}
						} else {
							mapData.remove(data.getTorderpk());
							totaldataselected -= obj.getTotalqty();
							if (obj.getStatus().equals(AppUtils.STATUS_ORDER_PRODUKSI)) {
								totalcheck -= 1;
								System.out.println("TOTAL CHECK : " + totalcheck);
							}
						}
						totalselected = mapData.size();

						BindUtils.postNotifyChange(null, null, OrderManualListCardVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, OrderManualListCardVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTorderpk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				A a = new A(data.getOrderid());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						Window win = new Window();
						if (productgroup.equals("TC")) {
							map.put("obj", data);
							win = (Window) Executions.createComponents("/view/order/tokendata.zul", null, map);
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
							map.put("obj", data);
							win = (Window) Executions.createComponents("/view/order/pinpaddata.zul", null, map);
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
							map.put("obj", data.getTpinmailerfile());
							win = (Window) Executions.createComponents("/view/pinmailer/pinmailerbranch.zul", null,
									map);
						}
						win.setWidth("50%");
						win.setClosable(true);
						win.doModal();
					}
				});
				if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN) || productgroup.equals(AppUtils.PRODUCTGROUP_CARD))
					row.getChildren().add(new Label(data.getOrderid()));
				else
					row.getChildren().add(a);
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getInserttime())));
				row.getChildren().add(new Label(
						data.getTotalqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : "0"));
				row.getChildren().add(new Label(data.getMemo()));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));

			}

		});

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
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Torder> listData = oDao.listByFilter(filter, orderby);
				if (listData != null && listData.size() > 0) {
					XSSFWorkbook workbook = new XSSFWorkbook();
					XSSFSheet sheet = workbook.createSheet();

					int rownum = 0;
					int cellnum = 0;
					Integer no = 0;
					Integer total = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("Daftar Order " + AppData.getProductgroupLabel(productgroup));
					rownum++;
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Periode");
					cell = row.createCell(1);
					cell.setCellValue(StringUtils.getMonthLabel(month) + " " + year);
					row = sheet.createRow(rownum++);

					/*
					 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
					 */
					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1, new Object[] { "No", "No Order", "Tanggal Order", "Total Data", "Memo", "Status" });
					no = 2;
					for (Torder data : listData) {
						datamap.put(no,
								new Object[] { no - 1, data.getOrderid(),
										dateLocalFormatter.format(data.getInserttime()), data.getTotalqty(),
										data.getMemo(), AppData.getStatusLabel(data.getStatus()) });
						no++;
						total += data.getTotalqty();
					}
					datamap.put(no, new Object[] { "", "TOTAL", "", total });
					Set<Integer> keyset = datamap.keySet();
					for (Integer key : keyset) {
						row = sheet.createRow(rownum++);
						Object[] objArr = datamap.get(key);
						cellnum = 0;
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);
							else if (obj instanceof Double)
								cell.setCellValue((Double) obj);
						}
					}

					String path = Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
					String filename = "CAPTION_DAFTAR_ORDER_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
							+ ".xlsx";
					FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
					workbook.write(out);
					out.close();

					Filedownload.save(new File(path + "/" + filename),
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				} else {
					Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
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

	@NotifyChange({ "totalselected", "totaldataselected" })
	private void doResetListSelected() {
		totalselected = 0;
		totaldataselected = 0;
		totalcheck = 0;
		mapData = new HashMap<>();
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			int statusfail = 0;
			boolean branchInvalid = true;
			boolean productInvalid = true;
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Torder obj = (Torder) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						if (obj.getStatus().equals(AppUtils.STATUS_PRODUCTION_PRODUKSIWAITAPPROVAL)
								|| obj.getStatus().equals(AppUtils.STATUS_PRODUCTION_DECLINEPRODUKSI)
								|| obj.getStatus().equals(AppUtils.STATUS_INVENTORY_OUTGOINGDECLINE)) {
							chk.setChecked(false);
							statusfail++;
						} else {
							if (obj.getStatus().equals(AppUtils.STATUS_ORDER_PRODUKSI)) {
								totalcheck++;
								System.out.println("TOTAL CHECK : " + totalcheck);
							}
							chk.setChecked(true);
							mapData.put(obj.getTorderpk(), obj);
							totaldataselected += obj.getTotalqty();
						}
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getTotalqty());
						totaldataselected -= obj.getTotalqty();
						if (obj.getStatus().equals(AppUtils.STATUS_ORDER_PRODUKSI)) {
							totalcheck -= 1;
							System.out.println("TOTAL CHECK : " + totalcheck);
						}
					}
				}
			}
			if (statusfail > 0) {
				Messagebox.show("Ada beberapa data belum bisa dipilih karena dalam status belum approved oleh produksi",
						"Info", Messagebox.OK, Messagebox.INFORMATION);
			} else if (!branchInvalid) {
				Messagebox.show("Ada beberapa ID cabang yang belum terdaftar di Caption.",
						WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
			} else if (!productInvalid) {
				Messagebox.show("Ada beberapa kode produk yang belum terdaftar di Caption.",
						WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
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

			Window win = (Window) Executions.createComponents("/view/order/orderselected.zul", null, map);
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
								BindUtils.postNotifyChange(null, null, OrderManualListCardVm.this, "*");
							}
						}
					});
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	public void doDone() {
		try {
			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				Messagebox.show("Anda ingin melakukan update status done produksi?", "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
										Map<String, Object> mapResult = PinmailerHandler.doDone(mapData,
												(Muser) Sessions.getCurrent().getAttribute("oUser"));
										String error = (String) mapResult.get("error");
										int failed = (int) mapResult.get("failed");
										if (error != null) {
											Messagebox.show(error, WebApps.getCurrent().getAppName(), Messagebox.OK,
													Messagebox.ERROR);
										} else {
											if (failed > 0) {
												Messagebox.show(
														"Ada beberapa data belum diupdate karena status belum produksi",
														WebApps.getCurrent().getAppName(), Messagebox.OK,
														Messagebox.INFORMATION);
											} else {
												Messagebox.show("Update status done berhasil",
														WebApps.getCurrent().getAppName(), Messagebox.OK,
														Messagebox.INFORMATION);
											}
											refreshModel(pageStartNumber);
											doReset();
										}
									}
								}
							}
						});

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doBon() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
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
						List<Torder> objList = new ArrayList<>();
						for (Entry<Integer, Torder> entry : mapData.entrySet()) {
							Torder obj = entry.getValue();
							objList.add(obj);
						}

						OrderPrintHandler.doBonPrint(objList, format, productgroup);
					}
				}
			});
		}
	}

	@Command
	public void doManifestPrint() {
		try {
			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
			} else {
				Map<String, Object> map = new HashMap<>();
				map.put("isOrder", "Y");
				Window win = (Window) Executions.createComponents("/view/perso/persomanifestprint.zul", null, map);
				win.setClosable(true);
				win.doModal();
				win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@SuppressWarnings("unchecked")
					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getData() != null) {
							try {

								Map<String, Object> map = (Map<String, Object>) event.getData();
								String operators = (String) map.get("operators");

								List<Torder> objList = new ArrayList<>();
								for (Entry<Integer, Torder> entry : mapData.entrySet()) {
									Torder obj = entry.getValue();
									objList.add(obj);
								}
								Collections.sort(objList, Torder.fieldComparator);
								zkSession.setAttribute("objList", objList);

								Map<String, String> parameters = new HashMap<>();
								parameters.put("OPERATORS", operators);

								zkSession.setAttribute("parameters", parameters);
								if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD))
									zkSession.setAttribute("reportPath",
											Executions.getCurrent().getDesktop().getWebApp()
													.getRealPath(SysUtils.JASPER_PATH + "/manualmanifestorder.jasper"));
								else
									zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop()
											.getWebApp().getRealPath(SysUtils.JASPER_PATH + "/manifestnoncard.jasper"));

								Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
							} catch (HibernateException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (year != null && month != null) {
			filter = "productgroup = '" + productgroup + "' and extract(year from inserttime) = " + year + " and "
					+ "extract(month from inserttime) = " + month;
		}
		if (orderid != null && orderid.length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "orderid like '%" + orderid.trim().toUpperCase() + "%'";
		}
		if (ordertype != null && ordertype.length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "ordertype = '" + ordertype.trim().toUpperCase() + "'";
		}
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		orderid = null;
		date = new Date();
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		ordertype = "";

		doSearch();
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
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

	public String getProductgroup() {
		return productgroup;
	}

	public String getOrdertype() {
		return ordertype;
	}

	public void setOrdertype(String ordertype) {
		this.ordertype = ordertype;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
