package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TpinmailerdataDAO;
import com.sdd.caption.domain.Tpinmailerbranch;
import com.sdd.caption.domain.Tpinmailerdata;
import com.sdd.caption.model.TpinmailerdataListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class PinmailerDataVm {

	private TpinmailerdataListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;

	private Tpinmailerbranch obj;
	private TpinmailerdataDAO oDao = new TpinmailerdataDAO();
	private String cardno;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private List<Tpinmailerdata> objList = new ArrayList<>();

	@Wire
	private Window winOrderdata;
	@Wire
	private Groupbox gbBatchInfo;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Tpinmailerbranch obj) throws ParseException {
		Selectors.wireComponents(view, this, false);
		this.obj = obj;

		doReset();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		grid.setRowRenderer(new RowRenderer<Tpinmailerdata>() {

			@Override
			public void render(Row row, final Tpinmailerdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				row.getChildren().add(new Label(data.getCardno()));
				row.getChildren().add(new Label(data.getName()));
				row.getChildren().add(
						new Label(data.getOrderdate() == null ? "" : datelocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getMproduct() != null ? data.getMproduct().getProductname() : ""));
				row.getChildren().add(new Label(data.getSeqno()));
			}
		});
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpinmailerdatapk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpinmailerdataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		filter = "";
		if (obj != null) {
			filter = "tpinmailerbranchfk = " + obj.getTpinmailerbranchpk();
		}
		if (cardno != null && cardno.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "cardno like '%" + cardno.trim().toUpperCase() + "%'";
		}

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doExport() {
		try {
			objList = oDao.listByFilter(filter, orderby);
			if (objList.size() > 0) {
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
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum);
				Cell cell = row.createCell(0);

				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1,
						new Object[] { "No", "No Kartu", "Nama", "Tgl Data", "Kode Produk", "Jenis Produk", "Seq No" });
				no = 2;
				for (Tpinmailerdata data : objList) {
					datamap.put(no, new Object[] { no - 1, data.getCardno(), data.getName(),
							data.getOrderdate() == null ? "" : datelocalFormatter.format(data.getOrderdate()),
							data.getProductcode() != null ? data.getProductcode() : "",
							data.getMproduct() != null ? data.getMproduct().getProductname() : "", data.getSeqno() });
					no++;
					total++;
				}
				datamap.put(no, new Object[] { "TOTAL", total });
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
						styleHeader.setFillForegroundColor(IndexedColors.AQUA.getIndex());
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
				String filename = "DATA PINMAILER_" + obj.getMbranch().getBranchname() + "_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				FileOutputStream out = new FileOutputStream(new File(path + filename));
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
	public void doClose() {
		Event closeEvent = new Event("onClose", winOrderdata, null);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		cardno = "";
		doSearch();
	}

	public Tpinmailerbranch getObj() {
		return obj;
	}

	public void setObj(Tpinmailerbranch obj) {
		this.obj = obj;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

}
