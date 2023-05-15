package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.model.TorderitemListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class PinpadDataVm {

	private TorderitemListModel model;
	private TorderitemDAO oDao = new TorderitemDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;

	private Torder obj;

	@Wire
	private Groupbox gbBatchInfo;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder obj)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		this.obj = obj;
		doSearch();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Torderitem>() {

				@Override
				public void render(Row row, final Torderitem data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren().add(new Label(data.getTpinpaditem().getItemno()));
					row.getChildren().add(new Label(data.getTid()));
					row.getChildren().add(new Label(data.getMid()));
					row.getChildren().add(new Label(AppData.getPinpadtypeLabel(data.getPinpadtype())));
				}
			});
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "torderitempk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TorderitemListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (obj != null)
			filter = "torderfk = " + obj.getTorderpk();
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Torderitem> listData = oDao.listNativeByFilter(filter, orderby);
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
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("DAFTAR ITEM PEMESANAN PINPAD");
					rownum++;

					/*
					 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
					 */
					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1, new Object[] { "No", "No Pemesanan", "No Seri", "TID", "MID", "Tipe Pinpad"});
					no = 2;
					for (Torderitem data : listData) {
						datamap.put(no, new Object[] { no - 1, data.getTorder().getOrderid(), data.getItemno(), 
								data.getTid(), data.getMid(), AppData.getPinpadtypeLabel(data.getPinpadtype())});
						no++;
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
					String filename = "CAPTION_DAFTAR_ITEM_PEMESANAN"
							+ new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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
	@NotifyChange("*")
	public void doReset() throws ParseException {
		doSearch();
	}

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

}
