package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.zkoss.bind.BindUtils;
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
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sdd.caption.dao.MbranchproductgroupDAO;
import com.sdd.caption.dao.MproductgroupDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverycourier;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.model.TdeliveryListModel;
import com.sdd.caption.pojo.FmtReqAll;
import com.sdd.caption.services.RequestPOS;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.caption.utils.LetterDerivatifGenerator;
import com.sdd.caption.utils.LetterEmeraldGenerator;
import com.sdd.caption.utils.LetterPinMailer;
import com.sdd.caption.utils.LetterPinpad;
import com.sdd.caption.utils.LetterRegGenerator;
import com.sdd.caption.utils.LetterToken;
import com.sdd.utils.SysUtils;

public class DeliveryListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String filterproduct;
	private Integer year;
	private Integer month;
	private String status;
	private String branchcode;
	private String branchname;
	private String productgroup;
	private String vendorcode;
	private String dlvid;
	private String branchid;
	private Date processtime;
	private Integer totalselected;
	private Integer totaldataselected;
	private Map<Integer, Tdelivery> mapData;
	private String isauto;
	private Boolean isDlvCust;

	private Tdeliverycourier obj;
	private TdeliveryListModel model;
	private List<Tdelivery> objList;
	private List<Tdelivery> reportList;
	private FmtReqAll reqAll;
	private String title;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	private static final String PRODUCTGROUP_CARDPHOTO = "09";

	@Wire
	private Groupbox gbSearch;
	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Caption caption;
	@Wire
	private Tabbox tabbox;
	@Wire
	private Tabs tabs;
	@Wire
	private Div dlv, dlvCust;
	@Wire
	private Button btnSoftcopy, btnLetter;
	@Wire
	private Column colCardno, colHp, colAmount;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Tdeliverycourier obj, @ExecutionArgParam("isCourierlist") String isCourierlist,
			@ExecutionArgParam("arg") String arg) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		mapData = new HashMap<>();
		this.obj = obj;
		if (obj != null)
			gbSearch.setVisible(false);

		if (isCourierlist != null && isCourierlist.equals("Y")) {
			caption.setVisible(true);
		}

		if (arg != null && arg.equals("home")) {
			isDlvCust = true;
			tabbox.setVisible(false);
			dlvCust.setVisible(true);
			colCardno.setVisible(true);
			colHp.setVisible(true);
			colAmount.setVisible(false);
		} else {
			btnSoftcopy.setVisible(false);
			isDlvCust = false;
			dlv.setVisible(true);
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
				chkAll.setChecked(false);
			}
		});
		grid.setRowRenderer(new RowRenderer<Tdelivery>() {

			@Override
			public void render(Row row, final Tdelivery data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tdelivery obj = (Tdelivery) checked.getAttribute("obj");
						if (checked.isChecked()) {
							mapData.put(obj.getTdeliverypk(), obj);
							totaldataselected += obj.getTotaldata();
						} else {
							mapData.remove(obj.getTdeliverypk());
							totaldataselected -= obj.getTotaldata();
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, DeliveryListVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, DeliveryListVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTdeliverypk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);

				A a = new A(data.getDlvid());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);

						Window win = (Window) Executions.createComponents("/view/delivery/deliverydata.zul", null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								Boolean isSaved = (Boolean) event.getData();
								if (isSaved != null && isSaved) {
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
									BindUtils.postNotifyChange(null, null, DeliveryListVm.this, "pageTotalSize");
								}
							}
						});
						win.doModal();
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(data.getCardno() != null ? data.getCardno() : ""));
				row.getChildren().add(new Label(data.getHpno() != null ? data.getHpno() : ""));
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getProcesstime())));
				row.getChildren().add(new Label(data.getMbranch().getBranchid()));
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalamount())));
				row.getChildren().add(new Label(data.getLettertype()));
				row.getChildren().add(new Label(data.getMcouriervendor().getVendorcode()));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
				row.getChildren().add(
						new Label(data.getTglterima() != null ? dateLocalFormatter.format(data.getTglterima()) : ""));
				row.getChildren().add(new Label(data.getPenerima() != null ? data.getPenerima() : ""));
				a = new A(data.getFilename());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Sessions.getCurrent().setAttribute("reportPath",
								AppUtils.FILES_ROOT_PATH + AppUtils.POD_PATH + "/" + data.getFilename().trim());
						Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
					}
				});
				if (data.getFilename() != null)
					row.getChildren().add(a);
				else
					row.getChildren().add(new Label("-"));

				Button btnTracking = new Button("Tracking");
				btnTracking.setAutodisable("self");
				btnTracking.setSclass("btn-default");
				btnTracking.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btnTracking.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {

						if (data.getAwb() == null) {
							Messagebox.show("No AWB tidak ditemukan", WebApps.getCurrent().getAppName(), Messagebox.OK,
									Messagebox.INFORMATION);
						} else {
							if (data.getMcouriervendor().getIstracking() == null) {
								Messagebox.show("Istracking pada courier vendor belum di set",
										WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
							} else {
								if (data.getMcouriervendor().getIstracking().equals("Y")) {
									reqAll = new FmtReqAll();
									reqAll.setBarcode(data.getAwb());
									RequestPOS.getToken(data);
									String rsp = RequestPOS.getDetail(reqAll, data);
									JSONObject objRsp = new JSONObject(rsp);
									if (objRsp.isNull("response")) {
										Messagebox.show("No AWB belum terdaftar", WebApps.getCurrent().getAppName(),
												Messagebox.OK, Messagebox.INFORMATION);
									} else {
										Map<String, Object> map = new HashMap<String, Object>();
										map.put("obj", data);
										map.put("isTrack", "Y");
										Window win = (Window) Executions
												.createComponents("/view/tracking/onlinetracking.zul", null, map);
										win.setWidth("80%");
										win.setClosable(true);
										win.doModal();
									}
								} else {
									Messagebox.show("Courier vendor tidak mendukung online tracking",
											WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
								}
							}
						}
					}

				});

				Div div = new Div();
				div.appendChild(btnTracking);
				row.getChildren().add(div);
			}

		});
		doReset();
		doRenderTab();
	}

	private void doRenderTab() {
		try {
			List<Mproductgroup> listProductgroup = new ArrayList<Mproductgroup>();
			if (oUser.getMbranch().getBranchlevel() == 1) {
				String filterproduct = "";
				if (oUser.getMbranch().getBranchid().equals("723"))
					filterproduct = "mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and productgroupcode = '04'";
				else
					filterproduct = "mbranchfk = " + oUser.getMbranch().getMbranchpk();
				for (Mbranchproductgroup obj : new MbranchproductgroupDAO().listNativeByFilter(filterproduct,
						"mproductgroupfk asc")) {
					listProductgroup.add(obj.getMproductgroup());
				}
			} else {
				String filterbranch = "";
				if (oUser.getMbranch().getBranchlevel() == 2)
					filterbranch = "productgroupcode = '04'";
				else
					filterbranch = "productgroupcode in ('02', '03', '04')";
				listProductgroup = new MproductgroupDAO().listByFilter(filterbranch, "mproductgrouppk asc");
			}

			String firstproductgroup = "";
			for (Mproductgroup obj : listProductgroup) {
				if (firstproductgroup.trim().length() == 0)
					firstproductgroup = obj.getProductgroupcode();
				Tab tab = new Tab(obj.getProductgroup());
				tab.setAttribute("productgroup", obj.getProductgroupcode());
				tab.addEventListener(Events.ON_SELECT, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Tab tabselected = (Tab) event.getTarget();
						doTab((String) tabselected.getAttribute("productgroup"));
					}
				});
				tab.setParent(tabs);
			}

			if (obj != null)
				firstproductgroup = obj.getProductgroup();
			
			doTab(firstproductgroup);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doTab(@BindingParam("tab") String tab) {
		productgroup = tab;

		if (tab.equals(PRODUCTGROUP_CARDPHOTO)) {
			productgroup = "01";
			title = "Kartu Berfoto";
			filterproduct = " and isproductphoto = 'Y'";
		} else {
			title = AppData.getProductgroupLabel(productgroup);
			filterproduct = " and isproductphoto = 'N'";
		}

		if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
			btnLetter.setVisible(false);
		else
			btnLetter.setVisible(true);

		doSearch();
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tdelivery obj = (Tdelivery) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						chk.setChecked(true);
						mapData.put(obj.getTdeliverypk(), obj);
						totaldataselected += obj.getTotaldata();
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getTdeliverypk());
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

			Window win = (Window) Executions.createComponents("/view/delivery/deliveryselected.zul", null, map);
			win.setClosable(true);
			win.doModal();
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
								totalselected = 0;
								totaldataselected = 0;
								mapData = new HashMap<>();
								refreshModel(pageStartNumber);
								BindUtils.postNotifyChange(null, null, DeliveryListVm.this, "*");
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
				if (isDlvCust)
					productgroup = "01";

				filter = "tdelivery.productgroup = '" + productgroup + "' and tdeliverycourierfk = "
						+ obj.getTdeliverycourierpk() + " and branchpool = '" + oUser.getMbranch().getBranchid() + "'";

				if (isDlvCust)
					filter += " and isdlvcust = 'Y'";
				else
					filter += " and isdlvcust = 'N'";

				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
			} else if (processtime != null && productgroup != null) {
				if (isDlvCust)
					productgroup = "01";

				filter = "tdelivery.productgroup = '" + productgroup + "' and Date(tdelivery.processtime) = '"
						+ dateFormatter.format(processtime) + "'" + filterproduct + " and branchpool = '"
						+ oUser.getMbranch().getBranchid() + "'";

				if (isDlvCust)
					filter += " and isdlvcust = 'Y'";
				else
					filter += " and isdlvcust = 'N'";

				if (dlvid != null && dlvid.length() > 0)
					filter += " and dlvid like '%" + dlvid.trim().toUpperCase() + "%'";
				if (branchid != null && branchid.length() > 0)
					filter += " and branchid like '%" + branchid.trim().toUpperCase() + "%'";
				if (branchname != null && branchname.length() > 0)
					filter += " and mbranch.branchname like '%" + branchname.trim().toUpperCase() + "%'";
				if (vendorcode != null && vendorcode.length() > 0)
					filter += " and vendorcode like '%" + vendorcode.trim().toUpperCase() + "%'";
				if (status != null && status.length() > 0)
					filter += " and tdelivery.status like '%" + status.trim().toUpperCase() + "%'";
				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doExport() {
		reportList = new ArrayList<>();
		try {
			reportList = new TdeliveryDAO().listExport(filter, "branchid, Date(tdelivery.processtime) desc");
			if (reportList.size() > 0) {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet();
				XSSFCellStyle style = workbook.createCellStyle();
				style.setBorderTop(BorderStyle.MEDIUM);
				style.setBorderBottom(BorderStyle.MEDIUM);
				style.setBorderLeft(BorderStyle.MEDIUM);
				style.setBorderRight(BorderStyle.MEDIUM);

				int rownum = 0;
				int cellnum = 0;
				Integer no = 0;
				Integer total = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("Tanggal Pengiriman :");
				cell = row.createCell(1);
				cell.setCellValue(dateLocalFormatter.format(processtime));
				row = sheet.createRow(rownum++);

				/*
				 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
				 */
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "No Manifest", "Group Produk", "Tgl Proses", "Kode Cabang",
						"Nama Cabang", "Total Data", "Total Amount", "Surat", "Expedisi", "Status" });
				no = 2;
				for (Tdelivery data : reportList) {
					datamap.put(no, new Object[] { no - 1, data.getDlvid(),
							AppData.getProductgroupLabel(data.getProductgroup()),
							dateLocalFormatter.format(data.getProcesstime()), data.getMbranch().getBranchid(),
							data.getMbranch().getBranchname(), NumberFormat.getInstance().format(data.getTotaldata()),
							"Rp " + NumberFormat.getInstance().format(data.getTotalamount()), data.getLettertype(),
							data.getMcouriervendor().getVendorcode(), AppData.getStatusLabel(data.getStatus()) });
					no++;
					total++;
				}
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 3) {
						XSSFCellStyle styleHeader = workbook.createCellStyle();
						styleHeader.setBorderTop(BorderStyle.MEDIUM);
						styleHeader.setBorderBottom(BorderStyle.MEDIUM);
						styleHeader.setBorderLeft(BorderStyle.MEDIUM);
						styleHeader.setBorderRight(BorderStyle.MEDIUM);
						styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String) {
								cell.setCellValue((String) obj);
								cell.setCellStyle(styleHeader);
							} else if (obj instanceof Integer) {
								cell.setCellValue((Integer) obj);
								cell.setCellStyle(styleHeader);
							} else if (obj instanceof Double) {
								cell.setCellValue((Double) obj);
								cell.setCellStyle(styleHeader);
							}
						}
					} else {
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String) {
								cell.setCellValue((String) obj);
								cell.setCellStyle(style);
							} else if (obj instanceof Integer) {
								cell.setCellValue((Integer) obj);
								cell.setCellStyle(style);
							} else if (obj instanceof Double) {
								cell.setCellValue((Double) obj);
								cell.setCellStyle(style);
							}
						}
					}
				}

				String path = Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
				String filename = "CAPTION_DLV_" + AppData.getProductgroupLabel(productgroup)
						+ new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
				workbook.write(out);
				out.close();

				Filedownload.save(new File(path + "/" + filename),
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doGenerateSoftcopy() {
		reportList = new ArrayList<>();
		try {
			reportList = new TdeliveryDAO().listExport(
					"isdlvcust = 'Y' and penerima is null and tglterima is null and branchpool = '"
							+ oUser.getMbranch().getBranchid() + "'",
					"branchid, Date(tdelivery.processtime) desc, tdelivery.zipcode");
			if (reportList.size() > 0) {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet();
				XSSFCellStyle style = workbook.createCellStyle();
				style.setBorderTop(BorderStyle.MEDIUM);
				style.setBorderBottom(BorderStyle.MEDIUM);
				style.setBorderLeft(BorderStyle.MEDIUM);
				style.setBorderRight(BorderStyle.MEDIUM);

				int rownum = 0;
				int cellnum = 0;
				Integer no = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum);
				Cell cell = row.createCell(0);

				/*
				 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
				 */
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1,
						new Object[] { "NO UNIQ", "NO KARTU", "NAMA", "NO HP", "ALAMAT", "KOTA", "K POS", "WILAYAH",
								"FLIGHT", "TGL KIRIM", "KURIR", "STATUS", "KETERANGAN", "TGL SUKSES", "TGL REP",
								"PENERIMA", "TGLU", "RTN", "JENIS KIRIM", "STATUS 2", "TGL SUKSES 2", "PENERIMA 2",
								"ALAMAT 2", "KOTA 2", "K POS 2", "WILAYAH 2", "FLIGHT 2", "TGL SUKSES 2", "PENERIMA" });
				no = 2;
				for (Tdelivery data : reportList) {
					datamap.put(no, new Object[] { data.getDlvid(), data.getCardno(), data.getCustname(),
							data.getHpno() != null ? data.getHpno() : "",
							data.getAddress1() + " " + data.getAddress2() + " " + data.getAddress3(), data.getCity(),
							data.getZipcode(), "", "", dateLocalFormatter.format(data.getProcesstime()),
							data.getMcouriervendor().getVendorname(), "", "", "", "", "", "", "", "", "", "", "",
							data.getMbranch().getBranchaddress(), data.getMbranch().getBranchcity(),
							data.getMbranch().getZipcode(), "", "", "", "" });
					no++;
				}
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 1) {
						XSSFCellStyle styleHeader = workbook.createCellStyle();
						styleHeader.setBorderTop(BorderStyle.MEDIUM);
						styleHeader.setBorderBottom(BorderStyle.MEDIUM);
						styleHeader.setBorderLeft(BorderStyle.MEDIUM);
						styleHeader.setBorderRight(BorderStyle.MEDIUM);
						styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String) {
								cell.setCellValue((String) obj);
								cell.setCellStyle(styleHeader);
							} else if (obj instanceof Integer) {
								cell.setCellValue((Integer) obj);
								cell.setCellStyle(styleHeader);
							} else if (obj instanceof Double) {
								cell.setCellValue((Double) obj);
								cell.setCellStyle(styleHeader);
							}
						}
					} else {
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String) {
								cell.setCellValue((String) obj);
								cell.setCellStyle(style);
							} else if (obj instanceof Integer) {
								cell.setCellValue((Integer) obj);
								cell.setCellStyle(style);
							} else if (obj instanceof Double) {
								cell.setCellValue((Double) obj);
								cell.setCellStyle(style);
							}
						}
					}
				}

				String path = Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
				String filename = "SOFTCOPY_" + AppData.getProductgroupLabel(productgroup)
						+ new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
				workbook.write(out);
				out.close();

				Filedownload.save(new File(path + "/" + filename),
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		processtime = new Date();
		totalselected = 0;
		totaldataselected = 0;
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "mbranch.branchid, tdelivery.zipcode";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TdeliveryListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	public void doPrintLabel() {
		try {
			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				Map<String, Object> map = new HashMap<>();

				Window win = (Window) Executions.createComponents("/view/delivery/deliveryprintlabel.zul", null, map);
				win.setClosable(true);
				win.doModal();
				win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getData() != null) {
							@SuppressWarnings("unchecked")
							Map<String, Object> map = (Map<String, Object>) event.getData();
							isauto = (String) map.get("isauto");
							if (isauto.equals("Y"))
								doPrintLabelAuto();
							else {
								Map<String, Object> mapLabel = new HashMap<>();
								mapLabel.put("obj", obj);
								mapLabel.put("productgroup", productgroup);
								mapLabel.put("mapData", mapData);

								Window win = (Window) Executions.createComponents("/view/delivery/deliverylabel.zul",
										null, mapLabel);
								win.setClosable(true);
								win.doModal();
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
	public void doPrintLabelSurat() {
		try {
			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				Map<String, Object> map = new HashMap<>();

				Window win = (Window) Executions.createComponents("/view/delivery/deliveryprintlabel.zul", null, map);
				win.setClosable(true);
				win.doModal();
				win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getData() != null) {
							@SuppressWarnings("unchecked")
							Map<String, Object> map = (Map<String, Object>) event.getData();
							isauto = (String) map.get("isauto");
							if (isauto.equals("Y"))
								doPrintLabelSuratAuto();
							// doLabelBarcode();
							else {
								Map<String, Object> mapLabel = new HashMap<>();
								mapLabel.put("obj", obj);
								mapLabel.put("islabelsurat", "Y");
								mapLabel.put("productgroup", productgroup);
								mapLabel.put("mapData", mapData);

								Window win = (Window) Executions.createComponents("/view/delivery/deliverylabel.zul",
										null, mapLabel);
								win.setClosable(true);
								win.doModal();
							}
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doPrintLabelSuratAuto() {
		try {

			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				objList = new ArrayList<>();
				int total;
				for (Entry<Integer, Tdelivery> entry : mapData.entrySet()) {
					Tdelivery data = entry.getValue();
					if (productgroup.trim().equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
						total = 0;
						for (Tdeliverydata dlvdata : new TdeliverydataDAO()
								.listByFilter("tdeliveryfk = " + data.getTdeliverypk(), "tdeliverydatapk")) {
							total = total + dlvdata.getQuantity();
						}
						data.setTotaldata(total);
					}
					objList.add(data);
				}
				Collections.sort(objList, Tdelivery.branchidComparator);
				Map<String, String> parameters = new HashMap<>();

				if (productgroup.trim().equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
					parameters.put("PAKETTYPE", "SR");
				} else if (productgroup.trim().equals(AppUtils.PRODUCTGROUP_CARD))
					parameters.put("PAKETTYPE", "BC - BNICARD");

				zkSession.setAttribute("objList", objList);
				zkSession.setAttribute("parameters", parameters);
				if (isDlvCust) {
					zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(SysUtils.JASPER_PATH + "/labeldlvcust.jasper"));
				} else {
					if (productgroup.trim().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
						zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
								.getRealPath(SysUtils.JASPER_PATH + "/labelnosuratdoc.jasper"));
					} else {
						zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
								.getRealPath(SysUtils.JASPER_PATH + "/labelnosurat.jasper"));
					}
				}
				Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doPrintLabelAuto() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			objList = new ArrayList<>();
			for (Entry<Integer, Tdelivery> entry : mapData.entrySet()) {
				Tdelivery data = entry.getValue();
				objList.add(data);
			}
			Collections.sort(objList, Tdelivery.branchidComparator);
			Map<String, String> parameters = new HashMap<>();

			if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER))
				parameters.put("PAKETTYPE", "SR");
			else if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD))
				parameters.put("PAKETTYPE", "BC - BNICARD");
			zkSession.setAttribute("objList", objList);
			zkSession.setAttribute("parameters", parameters);
			if (productgroup.trim().equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
				zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(SysUtils.JASPER_PATH + "/labelpinmailerdlv.jasper"));
			} else if (productgroup.trim().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
				zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(SysUtils.JASPER_PATH + "/labeldlvdoc.jasper"));
			} else {
				zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(SysUtils.JASPER_PATH + "/labelnosurat.jasper"));
			}
			Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
		}
	}

	@Command
	public void doPrintLetter() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			try {

				List<Tdelivery> objList = new ArrayList<>();
				for (Entry<Integer, Tdelivery> entry : mapData.entrySet()) {
					Tdelivery data = entry.getValue();
					objList.add(data);
				}
				Collections.sort(objList, Tdelivery.branchidComparator);
				doLetterGenerator(objList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void doLetterGenerator(List<Tdelivery> listDelivery) throws Exception {
		try {
			String filename = "LETTER" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + ".pdf";
			Font font = new Font(Font.FontFamily.HELVETICA, 10);
			Font fontbold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
			Font fonttable = new Font(Font.FontFamily.HELVETICA, 9);
			Font fontheadertable = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
			String output = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + filename);
			Document document = new Document(new Rectangle(PageSize.A4), 72, 72, 72, 72);
			PdfWriter.getInstance(document, new FileOutputStream(output));
			document.open();

			for (Tdelivery obj : listDelivery) {
				if (productgroup.equals(PRODUCTGROUP_CARDPHOTO)) {
					LetterDerivatifGenerator.doGenerate(document, font, fonttable, obj);
				} else {
					if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_BWD)
							|| obj.getLettertype().equals(AppUtils.LETTERTYPE_PRE)
							|| obj.getLettertype().equals(AppUtils.LETTERTYPE_PRY)) {
						LetterEmeraldGenerator.doLetterEmeraldGenerator(document, font, fonttable, obj,
								obj.getLettertype());
						System.out.println("SURAT EMERALD");
					} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_SR)) {
						LetterPinMailer.doLetterPinMailerGenerator(document, font, fonttable, obj);
						System.out.println("SURAT PIN MAILER");
					} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_TREG)) {
						LetterToken.doLetterTokenGenerator(document, font, fonttable, fontbold, obj);
						System.out.println("SURAT TOKEN");
					} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_PREG)) {
						LetterPinpad.doLetterPinpadGenerator(document, font, fonttable, fontheadertable, obj);
						System.out.println("SURAT PINPAD");
					} else {
						LetterRegGenerator.doLetterRegGenerator(document, font, fonttable, obj);
						System.out.println("SURAT REGULER");
					}
				}
			}
			document.close();

			Filedownload.save(
					new File(Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + filename)),
					"application/pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doLabelBarcode() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			// TEST BARCODE
			try {
				objList = new ArrayList<>();
				for (Entry<Integer, Tdelivery> entry : mapData.entrySet()) {
					Tdelivery data = entry.getValue();
					objList.add(data);
				}
				Collections.sort(objList, Tdelivery.branchidComparator);

				String filename = "LABELDLV_BARCODE" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + ".pdf";
				String output = Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.QR_PATH + filename);

				Document doc = new Document(new Rectangle(PageSize.A4));
				PdfWriter.getInstance(doc, new FileOutputStream(output));

				doc.open();

				Font font = new Font(Font.FontFamily.HELVETICA, 10);
				PdfPTable table = null;
				PdfPCell cell = null;

				PdfPTable tablefoot = new PdfPTable(2);
				tablefoot.setWidthPercentage(100);
				tablefoot.setWidths(new int[] { 50, 50 });
				PdfPCell cellfoot1 = new PdfPCell();
				cellfoot1.setBorder(PdfPCell.NO_BORDER);
				PdfPCell cellfoot2 = null;
				int totalcell = 1;
				int totalpage = 1;
				for (Tdelivery data : objList) {

					System.out.println("TOTAL CELL AWAL : " + totalcell);
					table = new PdfPTable(1);
					table.setWidthPercentage(100);
					cell = new PdfPCell(new Paragraph(
							data.getMbranch().getBranchname() + "(" + data.getMbranch().getBranchid() + ")", font));
					cell.setBorder(PdfPCell.NO_BORDER);
					table.addCell(cell);
					cell = new PdfPCell(new Paragraph("Nomor :" + " " + data.getDlvid(), font));
					cell.setBorder(PdfPCell.NO_BORDER);
					table.addCell(cell);
					cell = new PdfPCell(new Paragraph("Tanggal Data :" + " "
							+ new SimpleDateFormat("dd MMMMM yyyy").format(data.getProcesstime()), font));
					cell.setBorder(PdfPCell.NO_BORDER);
					table.addCell(cell);
					cell = new PdfPCell(new Paragraph("Jumlah :" + " " + data.getTotaldata(), font));
					cell.setBorder(PdfPCell.NO_BORDER);
					table.addCell(cell);
					cell = new PdfPCell(new Paragraph(data.getMcouriervendor().getVendorcode(), font));
					cell.setBorder(PdfPCell.NO_BORDER);
					table.addCell(cell);

					if (totalcell == 1) {
						cellfoot1.addElement(table);
						tablefoot.addCell(cellfoot1);

						cellfoot2 = new PdfPCell();
						cellfoot2.setBorder(PdfPCell.NO_BORDER);

						totalcell++;
					} else {
						cellfoot2.addElement(table);
						tablefoot.addCell(cellfoot2);
						doc.add(tablefoot);

						table = new PdfPTable(1);
						table.setWidthPercentage(100);
						cell = new PdfPCell(new Paragraph("\n", font));
						cell.setBorder(PdfPCell.NO_BORDER);
						table.addCell(cell);
						doc.add(table);

						tablefoot = new PdfPTable(2);
						tablefoot.setWidthPercentage(100);
						tablefoot.setWidths(new int[] { 50, 50 });
						cellfoot1 = new PdfPCell();
						cellfoot1.setBorder(PdfPCell.NO_BORDER);

						totalcell--;
					}

					totalpage++;
					if (totalpage == 4) {
						doc.newPage();
					}
					System.out.println("TOTAL CELL : " + totalcell);
					/*
					 * com.itextpdf.text.pdf.Barcode128 code128 = new
					 * com.itextpdf.text.pdf.Barcode128(); code128.setGenerateChecksum(true);
					 * code128.setCode(data.getDlvid());
					 * 
					 * cell = new PdfPCell(code128.createImageWithBarcode(writer.getDirectContent(),
					 * null, null)); cell.setBorder(PdfPCell.NO_BORDER); table.addCell(cell);
					 */

				}
				doc.close();
				System.out.println("BARCODE SUKSES");
				Filedownload.save(
						new File(Executions.getCurrent().getDesktop().getWebApp()
								.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.QR_PATH + filename)),
						"application/pdf");
				// ----------------------------
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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

	public String getBranchcode() {
		return branchcode;
	}

	public void setBranchcode(String branchcode) {
		this.branchcode = branchcode;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public String getDlvid() {
		return dlvid;
	}

	public void setDlvid(String dlvid) {
		this.dlvid = dlvid;
	}

	public String getBranchid() {
		return branchid;
	}

	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}

	public Date getProcesstime() {
		return processtime;
	}

	public void setProcesstime(Date processtime) {
		this.processtime = processtime;
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

	public String getVendorcode() {
		return vendorcode;
	}

	public void setVendorcode(String vendorcode) {
		this.vendorcode = vendorcode;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getIsauto() {
		return isauto;
	}

	public void setIsauto(String isauto) {
		this.isauto = isauto;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
