package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
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
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sdd.caption.dao.MsysparamDAO;
import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Vreportplan;
import com.sdd.caption.domain.Vsumbranchstock;
import com.sdd.caption.handler.BAPemusnahanDeposito;
import com.sdd.caption.handler.BAPemusnahanGiro;
import com.sdd.caption.handler.BAPemusnahanTabungan;
import com.sdd.caption.handler.BAPemusnahanWarkat;
import com.sdd.caption.model.TplanListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;

public class ReportPengadaanVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser; 
	
	List<Vreportplan> objList = new ArrayList<>();
	private TplanDAO oDao = new TplanDAO();

	private String productgroup, memono, planno;
	private int totalrecord;
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby, filter;
	private Integer year, month;

	private SimpleDateFormat datenormalFormatter = new SimpleDateFormat("dd-MM-YYYY");

	@Wire
	private Listbox listbox;
	@Wire
	private Groupbox gbFilter;
	@Wire
	private Button btnSearch, btnReset;
	@Wire
	private Combobox cbMonth, cbProductgroup;
	@Wire
	private Paging paging;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();
		
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (listbox != null) {
			listbox.setItemRenderer(new ListitemRenderer<Vreportplan>() {
				@Override
				public void render(Listitem item, final Vreportplan data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getMemono()));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(datenormalFormatter.format(data.getMemodate())));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(AppData.getProductgroupLabel(data.getProductgroup())));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getBranchname()));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getQty() != null ? NumberFormat.getInstance().format(data.getQty()): "0"));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getAnggaran() != null ? "Rp" + NumberFormat.getInstance().format(data.getAnggaran()) : "0" ));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getTotalprocess() != null ? NumberFormat.getInstance().format(data.getTotalprocess()): "0"));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getNominal() != null ? "Rp " + NumberFormat.getInstance().format(data.getNominal()) : "0"));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getTotalqty() != null ? NumberFormat.getInstance().format(data.getTotalqty()): "0"));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getNominalout() != null ? "Rp." +  NumberFormat.getInstance().format(data.getNominalout()) : "0"));
					item.appendChild(cell);
					
					Button btnIncoming = new Button();
					btnIncoming.setLabel("Data Persediaan");
					btnIncoming.setAutodisable("self");
					btnIncoming.setSclass("btn-light");
					btnIncoming.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnIncoming.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("memono", data.getMemono());
							map.put("arg", productgroup);
							map.put("isReport", "Y");
							Window win = (Window) Executions.createComponents("/view/inventory/incominglist.zul", null,
									map);
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
						}
					});
					
					Div div = new Div();
					div.appendChild(btnIncoming);
					cell = new Listcell();
					cell.appendChild(div);
					item.getChildren().add(cell);
				}
			});
			
			String[] months = new DateFormatSymbols().getMonths();
			for (int i = 0; i < months.length; i++) {
				Comboitem item = new Comboitem();
				item.setLabel(months[i]);
				item.setValue(i + 1);
				cbMonth.appendChild(item);
			}
		}
	}
	
	@Command
	public void doExportExcel() {
		try {
//			if (filter.length() > 0) {
//				List<Torder> listData = oDao.listNativeByFilter(filter, orderby);
				if (objList != null && objList.size() > 0) {
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
					Integer totalproses = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("REPORT PENGADAAN " + AppData.getProductgroupLabel(productgroup));
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
					datamap.put(1, new Object[] { "No", "No Memo", "Tanggal Memo", "Jenis Produk", "Cabang/Divisi", 
							"Jumlah", "Anggaran", "Total Proses", "Realisasi Anggaran",  "Total Outstanding", "Sisa Anggaran"});
					no = 2;
					for (Vreportplan obj : objList) {
						datamap.put(no,
								new Object[] { no - 1, obj.getMemono(), datenormalFormatter.format(obj.getMemodate()),
										AppData.getProductgroupLabel(obj.getProductgroup()), obj.getBranchname(), obj.getQty() != null ? NumberFormat.getInstance().format(obj.getQty()) : "-",
										obj.getAnggaran() != null ? NumberFormat.getInstance().format(obj.getAnggaran()) : "0", 
										obj.getTotalprocess() != null ? NumberFormat.getInstance().format(obj.getTotalprocess()): "0", 
										obj.getNominal() != null ? NumberFormat.getInstance().format(obj.getNominal()) : "0", 
										obj.getTotalqty() != null ? NumberFormat.getInstance().format(obj.getTotalqty()): "0",
										obj.getNominalout() != null ? NumberFormat.getInstance().format(obj.getNominalout()) : "0"});
						no++;
					}
//					datamap.put(no, new Object[] { "TOTAL", "", "", "", "", total, totalproses });
					Set<Integer> keyset = datamap.keySet();
					for (Integer key : keyset) {
						row = sheet.createRow(rownum++);
						Object[] objArr = datamap.get(key);
						cellnum = 0;
						if (rownum == 5) {
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
					String filename = "CAPTION_REPORT_PENGADAAN_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
							+ ".xlsx";
					FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
					workbook.write(out);
					out.close();

					Filedownload.save(new File(path + "/" + filename),
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				} else {
					Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	@Command
	public void doExport() {
		try {
			if (objList != null && objList.size() > 0) {
				String filename = "LETTER" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + ".pdf";
				Font font = new Font(Font.FontFamily.HELVETICA, 11);
				Font fontbold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
				Font fonttable = new Font(Font.FontFamily.HELVETICA, 7);
				Font fontheadertable = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
				String output = Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + filename);
				Document document = new Document(new Rectangle(PageSize.LETTER));
				PdfWriter.getInstance(document, new FileOutputStream(output));
				document.open();
				
				String[] hari = { "Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu" };
				String[] months = new DateFormatSymbols().getMonths();
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				
				PdfPTable table = null;
				PdfPCell cell = null;
				
				table = new PdfPTable(1);
				table.setWidthPercentage(100);
				cell = new PdfPCell(new Paragraph("Laporan Inisiasi Pengadaan Tahun " 
						+ cal.get(Calendar.YEAR) + " Posisi Bulan " +  months[(cal.get(Calendar.MONTH))] , font));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(PdfPCell.NO_BORDER);
			    cell.setPaddingBottom(5);
				table.addCell(cell);
				document.add(table);
				
				table = new PdfPTable(1);
				table.setWidthPercentage(100);
				cell = new PdfPCell(new Paragraph("Grup Produk : " + AppData.getProductgroupLabel(productgroup) , font));
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(PdfPCell.NO_BORDER);
			    cell.setPaddingBottom(5);
				table.addCell(cell);
				document.add(table);
				
				table = new PdfPTable(1);
				table.setWidthPercentage(100);
				table.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell = new PdfPCell(new Paragraph("  ", fontbold));
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBorder(PdfPCell.NO_BORDER);
			    cell.setPaddingBottom(5);
				table.addCell(cell);
				document.add(table);
				
				table = new PdfPTable(10);
				table.setWidthPercentage(100);
				table.setWidths(new int[] { 4, 9, 9, 13, 8, 13, 9, 13, 9, 13});
				cell = new PdfPCell(new Paragraph("No", fontheadertable));
				cell.setRowspan(2);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setUseBorderPadding(true);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("No Memo", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setRowspan(2);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Tanggal Memo", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setRowspan(2);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Cabang/Wilayah", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setRowspan(2);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Jumlah", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setRowspan(2);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Anggaran", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setRowspan(2);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Realisasi", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setColspan(2);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Outstanding", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setColspan(2);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Quantity", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Nominal", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Quantity", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				cell = new PdfPCell(new Paragraph("Nominal", fontheadertable));
				cell.setUseBorderPadding(true);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.GREEN);
				table.addCell(cell);
				document.add(table);
				
				int no = 1;
				for (Vreportplan obj : objList) {
					table = new PdfPTable(10);
					table.setHorizontalAlignment(Element.ALIGN_LEFT);
					table.setWidthPercentage(100);
					table.setWidths(new int[] { 4, 9, 9, 13, 8, 13, 9, 13, 9, 13});
					cell = new PdfPCell(new Paragraph(String.valueOf(no++), fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);				
					cell = new PdfPCell(new Paragraph(obj.getMemono(), fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(cell);	
					cell = new PdfPCell(new Paragraph(datenormalFormatter.format(obj.getMemodate()), fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(cell);	
					cell = new PdfPCell(new Paragraph(obj.getBranchname(), fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(cell);	
					cell = new PdfPCell(new Paragraph(obj.getQty() != null ? NumberFormat.getInstance().format(obj.getQty()) : "0", fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					cell = new PdfPCell(new Paragraph(obj.getAnggaran() != null ? "Rp " + NumberFormat.getInstance().format(obj.getAnggaran()) : "0", fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					cell = new PdfPCell(new Paragraph(obj.getTotalprocess() != null ? NumberFormat.getInstance().format(obj.getTotalprocess()) : "0", fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					cell = new PdfPCell(new Paragraph(obj.getNominal() != null ? "Rp " + NumberFormat.getInstance().format(obj.getNominal()) : "0", fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					cell = new PdfPCell(new Paragraph(obj.getTotalqty() != null ? NumberFormat.getInstance().format(obj.getTotalqty()) : "0", fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					cell = new PdfPCell(new Paragraph(obj.getNominalout() != null ? "Rp " + NumberFormat.getInstance().format(obj.getNominalout()) : "0", fonttable));
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					document.add(table);
				}
				document.close();

				Filedownload.save(
						new File(Executions.getCurrent().getDesktop().getWebApp()
								.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + filename)),
						"application/pdf");
			} else {
				Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		try {
			totalrecord = 0;
			objList = oDao.listReportplanning(filter, orderby);
			listbox.setModel(new ListModelList<>(objList));
			totalrecord = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (year != null && month != null) {
				filter = "extract(year from memodate) = " + year + " and " + "extract(month from memodate) = " + month
						+ " and tplan.productgroup = '" + productgroup + "'";
				
				if (productgroup != null)
					filter += " and tplan.productgroup = '" + productgroup + "'";
				}
			
			
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		memono = "";
		planno = "";
		cbProductgroup.setValue(null);
		productgroup = null;
		doSearch();
	}

	public String getMemono() {
		return memono;
	}

	public void setMemono(String memono) {
		this.memono = memono;
	}

	public String getPlanno() {
		return planno;
	}

	public void setPlanno(String planno) {
		this.planno = planno;
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

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public int getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(int totalrecord) {
		this.totalrecord = totalrecord;
	}



	public String getProductgroup() {
		return productgroup;
	}



	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}


}
