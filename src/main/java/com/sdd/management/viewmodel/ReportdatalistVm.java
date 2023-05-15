package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
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
import org.zkoss.zel.impl.parser.ParseException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.domain.Mkolomreport;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.model.MkolomreportListModel;
import com.sdd.caption.model.TincomingListModel;
import com.sdd.caption.model.TplanListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ReportdatalistVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private Muser oUser;
	private MkolomreportListModel model;
	private TplanDAO planDao = new TplanDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby, filter, memono, spkno, incstatus;
	private Integer year, month, itemendno;

//	private String status, productgroup, producttype, keypage, selincused;
//	private Boolean isIncoming, isPlan;
//	private Boolean isPFA = false;
//	private String planno;

	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	List<Mkolomreport> oList = new ArrayList<>();

	@Wire
	private Window winReportdata;
//	@Wire
//	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Groupbox gbSearch;
//	@Wire
//	private Row rowStatus, rowStatusPFA, rowIncomingused;
	@Wire
	private Column clplano, clpinputtime, clproductgroup, clanggaran, cltotalqty, cltotalprocess, clmemono,
			clpdecisiontime, clpstatus, clincomingid, clientrytime, clientryby, clprefix, clitemstartno, clitemendno,
			clistatus, clsupplier, clspkno, clspkdate, clorderid, clorderdate, clorderoutlet, cloitemqty, clinserttime,
			cltotalproses, cloordertype, clostatus;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) Sessions.getCurrent().getAttribute("oUser");
		System.out.println("tes1");

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Mkolomreport>() {
				@Override
				public void render(Row row, Mkolomreport data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren().add(new Label(
							data.getMproducttypefk() != null ? data.getMproducttypefk().getProducttype() : "-"));
					row.getChildren().add(new Label(data.getPlanno() != null ? data.getPlanno() : "-"));
					row.getChildren()
							.add(new Label(data.getInputtime() != null ? data.getInputtime().toString() : "-"));
					row.getChildren()
							.add(new Label(data.getProductgroup() != null
									? AppData.getProductgroupLabel(data.getProductgroup())
									: "-"));
					row.getChildren().add(new Label(data.getAnggaran() != null ? data.getAnggaran().toString() : "-"));
					row.getChildren().add(new Label(data.getTotalqty() != null ? data.getTotalqty().toString() : "-"));
					row.getChildren()
							.add(new Label(data.getTotalprocess() != null ? data.getTotalprocess().toString() : "-"));
					row.getChildren().add(new Label(data.getMemono() != null ? data.getMemono() : "-"));
					row.getChildren()
							.add(new Label(data.getPdecisiontime() != null ? data.getPdecisiontime().toString() : "-"));
					row.getChildren().add(
							new Label(data.getPstatus() != null ? AppData.getStatusLabel(data.getPstatus()) : "-"));
//
					row.getChildren().add(new Label(data.getIncomingid() != null ? data.getIncomingid() : "-"));
					row.getChildren()
							.add(new Label(data.getIentrytime() != null ? data.getIentrytime().toString() : "-"));
					row.getChildren().add(new Label(data.getIentryby() != null ? data.getIentryby() : "-"));
					row.getChildren().add(new Label(data.getPrefix() != null ? data.getPrefix() : "-"));
					row.getChildren()
							.add(new Label(data.getItemstartno() != null ? data.getItemstartno().toString() : "-"));
					if (data.getItemstartno() != null && data.getItemqty() != null)
						itemendno = data.getItemstartno() + data.getItemqty() - 1;
					row.getChildren().add(new Label(itemendno.toString()));
					row.getChildren().add(
							new Label(data.getIstatus() != null ? AppData.getStatusLabel(data.getIstatus()) : "-"));
					row.getChildren().add(
							new Label(data.getMsupplierfk() != null ? data.getMsupplierfk().getSuppliername() : "-"));
					row.getChildren().add(new Label(data.getSpkno() != null ? data.getSpkno() : "-"));
					row.getChildren().add(new Label(data.getSpkdate() != null ? data.getSpkdate().toString() : "-"));
//
					row.getChildren().add(new Label(data.getOrderid() != null ? data.getOrderid() : "-"));
					row.getChildren()
							.add(new Label(data.getOrderdate() != null ? data.getOrderdate().toString() : "-"));
					row.getChildren().add(new Label(data.getOrderoutlet() != null ? data.getOrderoutlet() : "-"));
					row.getChildren().add(new Label(data.getItemqty() != null ? data.getItemqty().toString() : "0"));
					row.getChildren()
							.add(new Label(data.getInserttime() != null ? data.getInserttime().toString() : "-"));
					row.getChildren()
							.add(new Label(data.getTotalproses() != null ? data.getTotalproses().toString() : "-"));
					row.getChildren().add(new Label(data.getOordertype() != null ? data.getOordertype() : "-"));
					row.getChildren().add(
							new Label(data.getOstatus() != null ? AppData.getStatusLabel(data.getOstatus()) : "-"));
				}
			});

//		String[] months = new DateFormatSymbols().getMonths();
//		for (int i = 0; i < months.length; i++) {
//			Comboitem item = new Comboitem();
//			item.setLabel(months[i]);
//			item.setValue(i + 1);
//			cbMonth.appendChild(item);
		}
		doReset();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		System.out.println("tes4");
		orderby = "inputtime desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MkolomreportListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		try {
			System.out.println("tes3");
//			if (year != null && month != null) {
//				filter = "extract(year from inputtime) = " + year + " and " + "extract(month from inputtime) = " + month;
			filter = "0=0";

			if (memono != null)
				filter += " and tplan.memono = '" + memono.trim() + "'";

			if (spkno != null)
				filter += " and tincoming.spkno = '" + spkno.trim() + "'";

//				if (status.length() > 0)
//					filter += " and status = '" + status + "'";
			//
//				if (producttype != null && producttype.trim().length() > 0) {
//					if (filter.length() > 0)
//						filter += " and mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
//				}
			//
//				if (selincused.equals("1") || "1".equals(selincused))
//					filter += " and incomingused = 1";
//				else if (selincused.equals("2") || "2".equals(selincused))
//					filter += " and incomingused is null";
			//
//				if (planno != null && planno.length() > 0)
//					filter += " and planno like '%" + planno.trim().toUpperCase() + "%'";
			//
//				if (isPFA) {
//					filter += " and status in ('" + AppUtils.STATUS_PLANNING_WAITAPPROVALPFA + "', '"
//							+ AppUtils.STATUS_PLANNING_DECLINEBYPFA + "', '" + AppUtils.STATUS_PLANNING_APPROVED + "')";
//				}
			//
//				if (isIncoming)
//					filter += " and status = '" + AppUtils.STATUS_PLANNING_APPROVED + "'";
			//
//				if (isPlan)
//					filter += " and tplanpk = '" + planfk + "'";
			//
//				if (oUser.getMbranch().getBranchlevel() == 2) {
//					filter += " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
//				} else if (oUser.getMbranch().getBranchlevel() == 3) {
//					filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();
//				}

			if (filter.length() > 0) {
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
	@NotifyChange("*")
	public void doReset() {
		System.out.println("tes2");
//		year = Calendar.getInstance().get(Calendar.YEAR);
//		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		memono = null;
		spkno = null;
		oList = new ArrayList<>();
		grid.setModel(new ListModelList<>(oList));
	}

//	@Command
//	public void doExport() {
//		try {
//			if (filter.length() > 0) {
//				List<Tplan> listData = planDao.listByFilter(filter, orderby);
//				if (listData != null && listData.size() > 0) {
//					XSSFWorkbook workbook = new XSSFWorkbook();
//					XSSFSheet sheet = workbook.createSheet();
//					XSSFCellStyle style = workbook.createCellStyle();
//					style.setBorderTop(BorderStyle.MEDIUM);
//					style.setBorderBottom(BorderStyle.MEDIUM);
//					style.setBorderLeft(BorderStyle.MEDIUM);
//					style.setBorderRight(BorderStyle.MEDIUM);
//
//					int rownum = 0;
//					int cellnum = 0;
//					Integer no = 0;
//					Integer total = 0;
//					BigDecimal totalanggaran = new BigDecimal(0);
//					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
//					Cell cell = row.createCell(0);
//					cell.setCellValue("DAFTAR PLANNING " + AppData.getProductgroupLabel(productgroup));
//					rownum++;
//					row = sheet.createRow(rownum++);
//					cell = row.createCell(0);
//					cell.setCellValue("Periode");
//					cell = row.createCell(1);
//					cell.setCellValue(StringUtils.getMonthLabel(month) + " " + year);
//					row = sheet.createRow(rownum++);
//
//					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
//					datamap.put(1, new Object[] { "No", "Tanggal Input", "Product", "Anggaran", "Jumlah Unit",
//							"No Memo", "Tanggal Memo", "Status", "Inputer", "Approver", "Tanggal Approval" });
//					no = 2;
//					for (Tplan data : listData) {
//						datamap.put(no, new Object[] { no - 1, datetimeLocalFormatter.format(data.getInputtime()),
//								AppData.getProductgroupLabel(data.getProductgroup()),
//								"Rp." + NumberFormat.getInstance().format(data.getAnggaran()), data.getTotalqty(),
//								data.getMemono(), datetimeLocalFormatter.format(data.getMemodate()),
//								AppData.getStatusLabel(data.getStatus()), data.getInputer(),
//								data.getDecisionby() != null ? data.getDecisionby() : "-",
//								data.getDecisiontime() != null ? datetimeLocalFormatter.format(data.getDecisiontime())
//										: "-" });
//						no++;
//						total += data.getTotalqty();
//						totalanggaran = totalanggaran.add(data.getAnggaran());
//
//					}
//
//					datamap.put(no, new Object[] { "TOTAL", total });
//					Set<Integer> keyset = datamap.keySet();
//					for (Integer key : keyset) {
//						row = sheet.createRow(rownum++);
//						Object[] objArr = datamap.get(key);
//						cellnum = 0;
//						if (rownum == 5) {
//							XSSFCellStyle styleHeader = workbook.createCellStyle();
//							styleHeader.setBorderTop(BorderStyle.MEDIUM);
//							styleHeader.setBorderBottom(BorderStyle.MEDIUM);
//							styleHeader.setBorderLeft(BorderStyle.MEDIUM);
//							styleHeader.setBorderRight(BorderStyle.MEDIUM);
//							styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
//							styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
//							for (Object obj : objArr) {
//								cell = row.createCell(cellnum++);
//								if (obj instanceof String) {
//									cell.setCellValue((String) obj);
//									cell.setCellStyle(styleHeader);
//								} else if (obj instanceof Integer) {
//									cell.setCellValue((Integer) obj);
//									cell.setCellStyle(styleHeader);
//								} else if (obj instanceof Double) {
//									cell.setCellValue((Double) obj);
//									cell.setCellStyle(styleHeader);
//								}
//							}
//						} else {
//							for (Object obj : objArr) {
//								cell = row.createCell(cellnum++);
//								if (obj instanceof String) {
//									cell.setCellValue((String) obj);
//									cell.setCellStyle(style);
//								} else if (obj instanceof Integer) {
//									cell.setCellValue((Integer) obj);
//									cell.setCellStyle(style);
//								} else if (obj instanceof Double) {
//									cell.setCellValue((Double) obj);
//									cell.setCellStyle(style);
//								}
//							}
//						}
//					}
//
//					String path = Executions.getCurrent().getDesktop().getWebApp()
//							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
//					String filename = "CAPTION_DAFTAR_PLANNING_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
//							+ ".xlsx";
//					FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
//					workbook.write(out);
//					out.close();
//
//					Filedownload.save(new File(path + "/" + filename),
//							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//				} else {
//					Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
//		}
//	}

//	@Command
//	@NotifyChange("*")
//	public void doAdd(@BindingParam("item") String item) {
//		try {
//			Map<String, Object> map = new HashMap<>();
//			map.put("arg", arg);
//			map.put("isDetail", "Y");
//			Window win = (Window) Executions.createComponents("/view/planning/planningentry.zul", null, map);
//			win.setWidth("70%");
//			win.setClosable(true);
//			win.doModal();
//			win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
//
//				@Override
//				public void onEvent(Event event) throws Exception {
//					doReset();
//					BindUtils.postNotifyChange(null, null, ReportdatalistVm.this, "*");
//				}
//			});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getMemono() {
		return memono;
	}

	public void setMemono(String memono) {
		this.memono = memono;
	}

	public String getSpkno() {
		return spkno;
	}

	public void setSpkno(String spkno) {
		this.spkno = spkno;
	}
}