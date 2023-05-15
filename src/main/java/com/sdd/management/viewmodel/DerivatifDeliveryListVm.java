package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
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

import javax.swing.JTextArea;

import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
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
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sdd.caption.dao.McouriervendorDAO;
import com.sdd.caption.dao.MsysparamDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverycourier;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.model.TdeliveryListModel;
import com.sdd.caption.pojo.FmtReqAll;
import com.sdd.caption.services.RequestPOS;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.caption.utils.LetterEmeraldGenerator;
import com.sdd.caption.utils.LetterPinMailer;
import com.sdd.caption.utils.LetterPinpad;
import com.sdd.caption.utils.LetterRegGenerator;
import com.sdd.caption.utils.LetterToken;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;

public class DerivatifDeliveryListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private TdeliverydataDAO oDao = new TdeliverydataDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer year;
	private Integer month;
	private String status;
	private String branchcode;
	private String branchname;
	private String vendorcode;
	private String dlvid;
	private String branchid;
	private Date processtime;
	private Integer totalselected;
	private Integer totaldataselected;
	private Map<Integer, Tdelivery> mapData;
	private String isauto;

	private Tdeliverycourier obj;
	private Mcouriervendor courier;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	private TdeliveryListModel model;
	List<Tdelivery> objList;
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private FmtReqAll reqAll;

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

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Tdeliverycourier obj) throws ParseException {
		Selectors.wireComponents(view, this, false);
		mapData = new HashMap<>();
		this.obj = obj;
		if (obj != null)
			gbSearch.setVisible(false);
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
						BindUtils.postNotifyChange(null, null, DerivatifDeliveryListVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, DerivatifDeliveryListVm.this, "totaldataselected");
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
									BindUtils.postNotifyChange(null, null, DerivatifDeliveryListVm.this, "pageTotalSize");
								}
							}
						});
						win.doModal();
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getProcesstime())));
				row.getChildren().add(new Label(data.getMbranch().getBranchid()));
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(String.valueOf(data.getTotaldata())));
				row.getChildren().add(new Label(data.getLettertype()));
				row.getChildren().add(new Label(data.getMcouriervendor().getVendorcode()));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));

				Button btnTracking = new Button("Tracking");
				btnTracking.setAutodisable("self");
				btnTracking.setSclass("btn btn-warning");
				btnTracking.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {

						System.out.println("AWB : " + data.getAwb());
						if (data.getAwb() == null) {
							Messagebox.show("No AWB tidak ditemukan", "Info", Messagebox.OK, Messagebox.INFORMATION);
						} else {
							courier = new McouriervendorDAO().findByPk(data.getMcouriervendor().getMcouriervendorpk());
							reqAll = new FmtReqAll();
							reqAll.setBarcode(data.getAwb());
							String token = RequestPOS.getToken(data);
							String rsp = RequestPOS.getDetail(reqAll, data);
							JSONObject objRsp = new JSONObject(rsp);
							if (objRsp.isNull("response")) {
								Messagebox.show("No AWB belum terdaftar", "Info", Messagebox.OK, Messagebox.INFORMATION);
							} else {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("obj", data);
								map.put("isTrack", "Y");
								Window win = (Window) Executions.createComponents("/view/tracking/onlinetracking.zul",
										null, map);
								win.setWidth("80%");
								win.setClosable(true);
								win.doModal();
							}
						}
					}

				});

				Div div = new Div();
				div.appendChild(btnTracking);
				row.getChildren().add(div);
			}

		});

		/*
		 * String[] months = new DateFormatSymbols().getMonths(); for (int i = 0; i <
		 * months.length; i++) { Comboitem item = new Comboitem();
		 * item.setLabel(months[i]); item.setValue(i+1); cbMonth.appendChild(item); }
		 */

		doReset();
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
								BindUtils.postNotifyChange(null, null, DerivatifDeliveryListVm.this, "*");
							}
						}
					});
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		mapData = new HashMap<>();
		totaldataselected = 0;
		totalselected = 0;
		if (obj != null) {
			filter = "tdeliverycourierfk = " + obj.getTdeliverycourierpk();
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} else if (processtime != null) {
			filter = "Date(tdelivery.processtime) = '" + dateFormatter.format(processtime) + "'";
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
		orderby = "branchid, date(tdelivery.processtime) desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TdeliveryListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	/*@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Vdeliv> listData = oDao.listNativeByFilter(filter, "mbranch.branchid");
				if (listData != null && listData.size() > 0) {
					XSSFWorkbook workbook = new XSSFWorkbook();
					XSSFSheet sheet = workbook.createSheet();

					int rownum = 0;
					int cellnum = 0;
					Integer no = 0;
					Integer total = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("Daftar Delivery");
					rownum++;
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Tanggal");
					cell = row.createCell(1);
					cell.setCellValue(dateFormatter.format(processtime));
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Grup Produk");
					cell = row.createCell(1);
					cell.setCellValue(AppData.getProductgroupLabel(productgroup));
					row = sheet.createRow(rownum++);

					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					rownum++;
					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1, new Object[] { "No", "No Manifest", "Kode Produk", "Jenis Produk", "Tanggal Data",
							"Tanggal Proses", "Kode Cabang", "Nama Cabang", "Total", "Ekspedisi" });
					no = 2;
					for (Vdeliv data : listData) {
						datamap.put(no,
								new Object[] { no - 1, data.getTdelivery().getDlvid(), data.getProductcode(),
										data.getProductname(), dateFormatter.format(data.getOrderdate()),
										dateFormatter.format(data.getTdelivery().getProcesstime()),
										data.getTdelivery().getMbranch().getBranchid(), data.getBranchname(),
										data.getQuantity(), data.getTdelivery().getMcouriervendor().getVendorcode() });
						no++;
						total += data.getQuantity();
					}
					datamap.put(no, new Object[] { "", "TOTAL", "", "", "", "", "", "", total });
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
					String filename = "CAPTION_DAFTAR_DELIVERY_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
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
	}*/

	@Command
	public void doPrintLabel() {
		try {
			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
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
				Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
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
							else {
								Map<String, Object> mapLabel = new HashMap<>();
								mapLabel.put("obj", obj);
								mapLabel.put("islabelsurat", "Y");
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
		/*if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			objList = new ArrayList<>();
			for (Entry<Integer, Tdelivery> entry : mapData.entrySet()) {
				Tdelivery data = entry.getValue();
				objList.add(data);
			}
			//Collections.sort(objList, Tdelivery.branchidComparator);
			Map<String, String> parameters = new HashMap<>();

			if (productgroup.trim().equals(AppUtils.PRODUCTGROUP_PINMAILER))
				parameters.put("PAKETTYPE", "SR");
			else if (productgroup.trim().equals(AppUtils.PRODUCTGROUP_CARD))
				parameters.put("PAKETTYPE", "BC - BNICARD");
			zkSession.setAttribute("objList", objList);
			zkSession.setAttribute("parameters", parameters);
			zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(SysUtils.JASPER_PATH + "/labelnosurat.jasper"));
			Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
		}*/
	}

	@Command
	public void doPrintLabelAuto() {
		/*if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
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
			} else {
				zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(SysUtils.JASPER_PATH + "/labeldlv.jasper"));
			}
			Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
		}*/
	}

	@Command
	public void doPrintLetter() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
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
			Document document = new Document(new Rectangle(PageSize.A4));
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(output));
			document.open();

			for (Tdelivery obj : listDelivery) {
				System.out.println("PRODUK GROUP " + obj.getProductgroup() + ", TIPE SURAT " + obj.getLettertype());
				if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_BWD)
						|| obj.getLettertype().equals(AppUtils.LETTERTYPE_PRE)
						|| obj.getLettertype().equals(AppUtils.LETTERTYPE_PRY)) {
					LetterEmeraldGenerator.doLetterEmeraldGenerator(document, font, fonttable, obj, obj.getLettertype());
					System.out.println("SURAT KARTU SELAIN REG");
				} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_SR)) {
					System.out.println("SURAT PINMAILER");
					LetterPinMailer.doLetterPinMailerGenerator(document, font, fonttable, obj);
				} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_TREG)) {
					System.out.println("SURAT TOKEN");
					LetterToken.doLetterTokenGenerator(document, font, fonttable, fontbold, obj);
				} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_PREG)) {
					System.out.println("SURAT PINPAD");
					LetterPinpad.doLetterPinpadGenerator(document, font, fonttable, fontheadertable, obj);
				} else
					LetterRegGenerator.doLetterRegGenerator(document, font, fonttable, obj);
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

	public String getIsauto() {
		return isauto;
	}

	public void setIsauto(String isauto) {
		this.isauto = isauto;
	}

}
