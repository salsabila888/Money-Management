package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TpaketdataDAO;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.model.TpaketListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DerivatifPaketListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private TpaketListModel model;
	private TpaketDAO oDao = new TpaketDAO();
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TembossdataDAO torderdataDao = new TembossdataDAO();

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

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

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
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
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
						BindUtils.postNotifyChange(null, null, DerivatifPaketListVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, DerivatifPaketListVm.this, "totaldataselected");
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
									BindUtils.postNotifyChange(null, null, DerivatifPaketListVm.this, "pageTotalSize");
								}
							}
						});
						win.doModal();
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
				row.getChildren().add(new Label(data.getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(
						new Label(data.getOrderdate() != null ? dateLocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(
						data.getProcesstime() != null ? dateLocalFormatter.format(data.getProcesstime()) : ""));
				row.getChildren().add(new Label(String.valueOf(data.getTotaldata())));
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
								BindUtils.postNotifyChange(null, null, DerivatifPaketListVm.this, "*");
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
					XSSFWorkbook workbook = new XSSFWorkbook();
					XSSFSheet sheet = workbook.createSheet();

					int rownum = 0;
					int cellnum = 0;
					Integer no = 0;
					total = 0;
					totaldone = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("Laporan Paket");
					rownum++;
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Tanggal");
					cell = row.createCell(1);
					cell.setCellValue(dateLocalFormatter.format(processtime));
					row = sheet.createRow(rownum++);

					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					rownum++;
					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1, new Object[] { "No", "No Paket", "Grup Produk", "Kode Produk", "Jenis Produk",
							"Tanggal Data", "Tanggal Produksi", "Total", "Status" });
					no = 2;
					for (Tpaket data : listData) {
						datamap.put(no,
								new Object[] { no - 1, data.getPaketid(), data.getProductgroup(), data.getMproduct().getProductcode(),
										data.getMproduct().getProductname(), dateLocalFormatter.format(data.getOrderdate()),
										dateLocalFormatter.format(data.getProcesstime()), data.getTotaldata(),
										data.getStatus() });
						no++;
						total += data.getTotaldata();
					}
					datamap.put(no, new Object[] { "", "", "TOTAL", "", "", "", "", total });
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
					String filename = "CAPTION_DAFTAR_PAKET_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
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

	@Command
	public void doPrintLabel() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			try {
				List<Tpaketdata> objList = new ArrayList<>();
				for (Entry<Integer, Tpaket> entry : mapData.entrySet()) {
					Tpaket data = entry.getValue();
					objList.addAll(tpaketdataDao.listByFilter("tpaketfk = " + data.getTpaketpk(), "tpaketdatapk"));
				}
				zkSession.setAttribute("objList", objList);
				zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(SysUtils.JASPER_PATH + "/labelpaket.jasper"));
				Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
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
									Session session = StoreHibernateUtil.openSession();
									Transaction transaction = null;
									try {
										for (Entry<Integer, Tpaket> entry : mapData.entrySet()) {
											Tpaket objPaket = entry.getValue();
											transaction = session.beginTransaction();

											objPaket.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
											oDao.save(session, objPaket);

											transaction.commit();
										}

										Messagebox.show("Proses update status done paket \nselesai", "Info",
												Messagebox.OK, Messagebox.INFORMATION);
										needsPageUpdate = true;
										refreshModel(pageStartNumber);
										doResetListSelected();
										BindUtils.postNotifyChange(null, null, DerivatifPaketListVm.this, "*");
									} catch (Exception e) {
										e.printStackTrace();
									}
									session.close();
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
					objList = tpaketdataDao.listByFilter("tpaketfk = " + data.getTpaketpk(), orderby);
					for(Tpaketdata paketdata : objList) {
						objList.add(paketdata);
					}
				}
				doPaketOrderdataGenerator(objList);
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
				filter = "tderivatifproduct is not null and orderdate = '" + dateFormatter.format(processtime) + "'";
			else
				filter = "Date(processtime) = '" + dateFormatter.format(processtime) + "'";
			if (status != null && status.length() > 0)
				filter += " and status = '" + status + "'";
			if (paketid != null && paketid.length() > 0)
				filter += " and paketid like '%" + paketid.trim().toUpperCase() + "%'";
			if (productcode != null && productcode.length() > 0)
				filter += " and productcode like '%" + productcode.trim().toUpperCase() + "%'";
			if (productname != null && productname.length() > 0)
				filter += " and productname like '%" + productname.trim().toUpperCase() + "%'";
			/*
			 * if (status!= null && status.length() > 0) filter += " and status = '" +
			 * status + "'";
			 */

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

	private void doPaketOrderdataGenerator(List<Tpaketdata> listPaket) throws Exception {
		try {
			String filename = "OUTPUT" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + ".pdf";
			Font font = new Font(Font.FontFamily.TIMES_ROMAN, 8);
			String output = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + filename);
			Document document = new Document(new Rectangle(PageSize.A4));
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(output));
			document.open();

			Integer mproductpk = null;
			for (Tpaketdata obj : listPaket) {
				document.newPage();

				int no = 1;
				String branchid = "";
				List<Tembossdata> objList = torderdataDao
						.listByFilter("tembossproductfk = " + obj.getTembossbranch().getTembossproduct().getTembossproductpk()
								+ " and tembossbranch = " + obj.getTembossbranch().getTembossbranchpk(), "torderdatapk desc");
				for (Tembossdata data : objList) {
					if (!data.getMbranch().getBranchid().equals(branchid)) {
						if (branchid.length() > 0)
							document.newPage();

						PdfPTable table = new PdfPTable(2);
						table.setHorizontalAlignment(Element.ALIGN_LEFT);
						table.setWidthPercentage(100);
						table.setWidths(new int[] { 30, 70 });
						PdfPCell cell1 = new PdfPCell(new Paragraph("WAKTU MANIFEST", font));
						cell1.setBorder(PdfPCell.NO_BORDER);
						PdfPCell cell2 = new PdfPCell(
								new Paragraph(": " + dateLocalFormatter.format(obj.getTpaket().getProcesstime()), font));
						cell2.setBorder(PdfPCell.NO_BORDER);
						table.addCell(cell1);
						table.addCell(cell2);
						document.add(table);
						table = new PdfPTable(2);
						table.setHorizontalAlignment(Element.ALIGN_LEFT);
						table.setWidthPercentage(100);
						table.setWidths(new int[] { 30, 70 });
						cell1 = new PdfPCell(new Paragraph("CABANG", font));
						cell1.setBorder(PdfPCell.NO_BORDER);
						cell2 = new PdfPCell(new Paragraph(
								": " + data.getBranchid() + "-" + data.getBranchname(),
								font));
						cell2.setBorder(PdfPCell.NO_BORDER);
						table.addCell(cell1);
						table.addCell(cell2);
						document.add(table);

						document.add(new Paragraph(" "));

						table = new PdfPTable(9);
						table.setHorizontalAlignment(Element.ALIGN_LEFT);
						table.setWidthPercentage(100);
						table.setWidths(new int[] { 4, 10, 14, 4, 18, 12, 16, 10, 12 });
						cell1 = new PdfPCell(new Paragraph("NO", font));
						cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell2 = new PdfPCell(new Paragraph("No Paket", font));
						cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
						PdfPCell cell3 = new PdfPCell(new Paragraph("ID PRODUK", font));
						cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
						PdfPCell cell4 = new PdfPCell(new Paragraph("KLN", font));
						cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
						PdfPCell cell5 = new PdfPCell(new Paragraph("NAMA", font));
						PdfPCell cell6 = new PdfPCell(new Paragraph("TGL DATA", font));
						cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
						PdfPCell cell7 = new PdfPCell(new Paragraph("PRODUK", font));
						PdfPCell cell8 = new PdfPCell(new Paragraph("KODE PRODUK", font));
						cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
						PdfPCell cell9 = new PdfPCell(new Paragraph("SEQ NUM", font));
						cell9.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.addCell(cell1);
						table.addCell(cell2);
						table.addCell(cell3);
						table.addCell(cell4);
						table.addCell(cell5);
						table.addCell(cell6);
						table.addCell(cell7);
						table.addCell(cell8);
						table.addCell(cell9);
						document.add(table);
						no = 1;
					}
					branchid = data.getMbranch().getBranchid();
					PdfPTable table = new PdfPTable(9);
					table.setHorizontalAlignment(Element.ALIGN_LEFT);
					table.setWidthPercentage(100);
					table.setWidths(new int[] { 4, 10, 14, 4, 18, 12, 16, 10, 12 });
					PdfPCell cell1 = new PdfPCell(new Paragraph(String.valueOf(no++), font));
					cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
					PdfPCell cell2 = new PdfPCell(new Paragraph(obj.getNopaket(), font));
					cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
					PdfPCell cell3 = new PdfPCell(new Paragraph(data.getCardno(), font));
					cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
					PdfPCell cell4 = new PdfPCell(new Paragraph(data.getKlncode(), font));
					cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
					PdfPCell cell5 = new PdfPCell(new Paragraph(data.getNameonid(), font));
					PdfPCell cell6 = new PdfPCell(new Paragraph(dateLocalFormatter.format(data.getOrderdate()), font));
					cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
					PdfPCell cell7 = new PdfPCell(new Paragraph(data.getMproduct().getProductname(), font));
					PdfPCell cell8 = new PdfPCell(new Paragraph(data.getProductcode(), font));
					cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
					PdfPCell cell9 = new PdfPCell(new Paragraph(data.getSeqno(), font));
					cell9.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(cell1);
					table.addCell(cell2);
					table.addCell(cell3);
					table.addCell(cell4);
					table.addCell(cell5);
					table.addCell(cell6);
					table.addCell(cell7);
					table.addCell(cell8);
					table.addCell(cell9);
					document.add(table);
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
