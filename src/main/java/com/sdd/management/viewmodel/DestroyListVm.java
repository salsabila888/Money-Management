
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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.HibernateException;
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
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mreturnreason;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.handler.BAPemusnahanDeposito;
import com.sdd.caption.handler.BAPemusnahanGiro;
import com.sdd.caption.handler.BAPemusnahanTabungan;
import com.sdd.caption.handler.BAPemusnahanToken;
import com.sdd.caption.handler.BAPemusnahanWarkat;
import com.sdd.caption.handler.VoucherGenerator;
import com.sdd.caption.model.TreturnListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DestroyListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private Treturnitem objItem;
	private Mreturnreason objReason;
	private TreturnDAO oDao = new TreturnDAO();

	private TreturnListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer year;
	private Integer month;
	private String status;
	private String arg;
	private String productgroup;
	private Integer totalselected;
	private Integer branchlevel;
	private Map<Integer, Treturn> mapData = new HashMap<Integer, Treturn>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();
		this.arg = arg;
		productgroup = arg;
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
							Treturn obj = (Treturn) checked.getAttribute("obj");
							if (checked.isChecked()) {
								mapData.put(obj.getTreturnpk(), obj);
							} else {
								mapData.remove(obj.getTreturnpk());
							}
							totalselected = mapData.size();
							BindUtils.postNotifyChange(null, null, DestroyListVm.this, "totalselected");
						}
					});
					if (mapData.get(data.getTreturnpk()) != null)
						check.setChecked(true);
					row.getChildren().add(check);

					A a = new A(data.getRegid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("obj", data);
							win = (Window) Executions.createComponents("/view/return/returproductitem.zul", null, map);
							win.setWidth("50%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.appendChild(new Label(
							data.getMbranch().getBranchname() != null ? data.getMbranch().getBranchname() : "-"));
					row.appendChild(new Label(data.getMbranch().getMregion().getRegionname() != null
							? data.getMbranch().getMregion().getRegionname()
							: "-"));
					row.appendChild(new Label(data.getMproduct().getMproducttype().getProducttype() != null
							? data.getMproduct().getMproducttype().getProducttype()
							: "-"));
					row.appendChild(new Label(
							data.getMproduct().getProductcode() != null ? data.getMproduct().getProductcode() : "-"));
					row.appendChild(new Label(
							data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : "-"));
					row.appendChild(new Label(data.getMreturnreason().getReturnreason() != null ? data.getMreturnreason().getReturnreason() : "-"));
					row.appendChild(
							new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
					row.appendChild(new Label(data.getInsertedby() != null ? data.getInsertedby() : "-"));
					row.appendChild(new Label(
							data.getInserttime() != null ? dateLocalFormatter.format(data.getInserttime()) : "-"));
					row.appendChild(new Label(data.getDecisionby() != null ? data.getDecisionby() : "-"));
					row.appendChild(new Label(
							data.getDecisiontime() != null ? dateLocalFormatter.format(data.getDecisiontime()) : "-"));
					Button btndel = new Button("Hapus");
					btndel.setAutodisable("self");
					btndel.setClass("btn btn-danger btn-sm");
					btndel.setStyle("border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btndel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
									Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

										@Override
										public void onEvent(Event event) throws Exception {
											if (event.getName().equals("onOK")) {
												Session session = StoreHibernateUtil.openSession();
												Transaction transaction = session.beginTransaction();
												try {
													oDao.delete(session, data);
													transaction.commit();
													Clients.showNotification("Hapus data berhasil.", "info", null,
															"middle_center", 3000);
												} catch (HibernateException e) {
													transaction.rollback();
													e.printStackTrace();
												} catch (Exception e) {
													e.printStackTrace();
												} finally {
													session.close();
												}

												doSearch();
												BindUtils.postNotifyChange(null, null, DestroyListVm.this,
														"pageTotalSize");
											}
										}
									});
						}

					});
					row.getChildren().add(btndel);
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
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (year != null && month != null) {
			filter = "extract(year from inserttime) = " + year + " and " + "extract(month from inserttime) = " 
					+ month + " and productgroup = '" + arg + "' and isdestroy = 'Y'";
			if (branchlevel == 3) {
				filter += "and returnlevel = '3'";
			} else if (branchlevel == 2) { 
				filter += " and status not in ('" + AppUtils.STATUS_RETUR_DECLINE + "')";
			} else if (branchlevel == 1) {
				filter += "and returnlevel = '2' and status not in ('" + AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH + "')";
			} 
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);

		}
	}

	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Treturn> listData = oDao.listNativeByFilter(filter, orderby);
				if (listData != null && listData.size() > 0) {
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
					cell.setCellValue("DAFTAR DESTROY SURAT BERHARGA");
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
					datamap.put(1, new Object[] { "No", "Retur ID", "Nama Cabang", "Nama Wilayah", "Nama Barang", 
							"Kode Barang", "Jumlah Unit", "Alasan Pengembalian", "Status", "Direquest oleh",
							"Tanggal Request", "Pemutus", "Tanggal Keputusan" });
					no = 2;
					for (Treturn data : listData) {
						datamap.put(no,
								new Object[] { no - 1, data.getRegid(), data.getMbranch().getBranchname(), data.getMbranch().getMregion().getRegionname(),
										data.getMproduct().getProductname(), data.getMproduct().getProductcode(),
										NumberFormat.getInstance().format(data.getItemqty()), data.getMreturnreason().getReturnreason(),
										AppData.getStatusLabel(data.getStatus()),
										data.getInsertedby(), dateLocalFormatter.format(data.getInserttime()),
										data.getDecisionby(), dateLocalFormatter.format(data.getDecisiontime()) });
						no++;
					}

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
					String filename = "CAPTION_DAFTAR_DESTROY_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
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
	@NotifyChange({ "totalselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Treturn obj = (Treturn) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						chk.setChecked(true);
						mapData.put(obj.getTreturnpk(), obj);
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getTreturnpk());
					}
				}
			}
			totalselected = mapData.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doPrintLetter() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			try {
				List<Treturn> objList = new ArrayList<>();
				for (Entry<Integer, Treturn> entry : mapData.entrySet()) {
					Treturn data = entry.getValue();
					objList.add(data);
				}
				doLetterGenerator(objList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void doLetterGenerator(List<Treturn> listReturn) throws Exception {
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

			for (Treturn obj : listReturn) {
				if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_DEPO)) {
					BAPemusnahanDeposito.doBAPemusnahanDeposito(document, font, fonttable, fontbold, obj, objItem,
							oUser);
					System.out.println("BILYET DEPOSITO");
				} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_TBG)) {
					BAPemusnahanTabungan.doBAPemusnahanTabungan(document, font, fonttable, fontbold, obj, objItem,
							oUser);
					System.out.println("BUKU TABUNGAN");
				} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_GIRO)) {
					BAPemusnahanGiro.doBAPemusnahanGiro(document, font, fonttable, fontbold, obj, objItem, oUser);
					System.out.println("CEK BILYET GIRO");
				} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_WKT)) {
					BAPemusnahanWarkat.doBAPemusnahanWarkat(document, font, fonttable, fontbold, obj, objItem, oUser);
					System.out.println("WARKAT GARANSI BANK");
				} else if (obj.getLettertype().trim().equals(AppUtils.LETTERTYPE_TKN)) {
					BAPemusnahanToken.doBAPemusnahanToken(document, font, fonttable, fontbold, obj, objItem, oUser);
					System.out.println("TOKEN");
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

	@Command
	@NotifyChange("*")
	public void doReset() {
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		status = "";
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Muser getoUser() {
		return oUser;
	}

	public void setoUser(Muser oUser) {
		this.oUser = oUser;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public Treturnitem getObjItem() {
		return objItem;
	}

	public void setObjItem(Treturnitem objItem) {
		this.objItem = objItem;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

}
