package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Column;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Foot;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.model.TbranchStockItemListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportBranchStockItemVm {

	private TbranchStockItemListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;

	private Tbranchstock obj;
	private String productname, stocktype;
	private Integer itemqty;

	private Boolean isSaved;
	private String productgroup;
	private String productgroupname;
	private String arg;
	private String labeltype;
	private String pinpadtype;

	@Wire
	private Window winReturnItem;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Label lbTitle;
	@Wire
	private Column colType, colTID, colMID, colNasabah, colDateused;
	@Wire
	private Row rowType;
	@Wire
	private Foot foot;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Tbranchstock tbranchstock, @ExecutionArgParam("stocktype") String stctype)
			throws ParseException {

		Selectors.wireComponents(view, this, false);
		obj = tbranchstock;
		stocktype = stctype;

		if (stocktype.equals("current"))
			labeltype = "STOCK CABANG";
		else if (stocktype.equals("out"))
			labeltype = "OUTGOING";
		else if (stocktype.equals("destroyed"))
			labeltype = "DESTROYED";
		else
			labeltype = "INCOMING";

		productgroup = AppData.getProductgroupLabel(obj.getProductgroup()) + " - " + labeltype;
		productgroupname = AppData.getProductgroupLabel(obj.getProductgroup());

		if (productgroupname.equals(AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_PINPAD))) {
			colType.setVisible(true);
			colTID.setVisible(true);
			colMID.setVisible(true);
			rowType.setVisible(true);
			foot.setVisible(true);
			colNasabah.setVisible(false);
			colDateused.setVisible(false);
		} else {
			colType.setVisible(false);
			colTID.setVisible(false);
			colMID.setVisible(false);
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tbranchstockitem>() {

				@Override
				public void render(Row row, Tbranchstockitem data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren().add(new Label(data.getItemno() != null ? data.getItemno() : "-"));
					row.getChildren()
							.add(new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
//					row.getChildren().add(new Label(data.getNumerator()));
					if (productgroupname.equals(AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_PINPAD))) {
						row.getChildren().add(
								new Label(data.getTid() != null ? AppData.getPinpadtypeLabel(data.getTid()) : "-"));
					} else {
						row.getChildren().add(new Label("-"));
					}
					if (productgroupname.equals(AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_PINPAD))) {
						row.getChildren().add(
								new Label(data.getMid() != null ? AppData.getPinpadtypeLabel(data.getMid()) : "-"));
					} else {
						row.getChildren().add(new Label("-"));
					}
					if (productgroupname.equals(AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_PINPAD))) {
						row.getChildren().add(new Label(
								data.getPinpadtype() != null ? AppData.getPinpadtypeLabel(data.getPinpadtype()) : "-"));
					} else {
						row.getChildren().add(new Label("-"));
					}

					row.getChildren().add(new Label(data.getNasabah() != null ? data.getNasabah() : "-"));
					row.getChildren()
							.add(new Label(data.getDateactivated() != null
									? new SimpleDateFormat("dd-MM-yyyy").format(data.getDateactivated())
									: "-"));
				}
			});
		}
		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "tbranchstockfk = " + obj.getTbranchstockpk();
		if (stocktype.equals("current")) {
			filter += " and status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "'";
		} else if (stocktype.equals("out")) {
			filter += " and status not in ('" + AppUtils.STATUS_SERIALNO_ENTRY + "', '"
					+ AppUtils.STATUS_RETUR_DESTROYED + "')";
		} else if (stocktype.equals("destroyed")) {
			filter += " and status = '" + AppUtils.STATUS_RETUR_DESTROYED + "'";
		}

		if (pinpadtype.trim().length() > 0)
			filter += " and pinpadtype = '" + pinpadtype.trim() + "'";

		System.out.println("FILTER STOK CABANG : " + filter.toUpperCase());
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tbranchstockitempk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TbranchStockItemListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	public void doExport() {
		List<Tbranchstockitem> objList = new ArrayList<Tbranchstockitem>();
		try {
			objList = new TbranchstockitemDAO().listNativeByFilter(filter, "tbranchstockitempk");
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
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum);
				Cell cell = row.createCell(0);

				/*
				 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
				 */
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "NO", "NO SERI", "STATUS", "NASABAH" });
				no = 2;
				for (Tbranchstockitem data : objList) {
					datamap.put(no, new Object[] { no - 1, data.getItemno(), AppData.getStatusLabel(data.getStatus()),
							data.getNasabah() != null ? data.getNasabah() : "-" });
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
				String filename = "STOCK_" + AppData.getProductgroupLabel(productgroup)
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
	public void doClose() {
		Event closeEvent = new Event("onClose", winReturnItem, isSaved);
		Events.postEvent(closeEvent);
		stocktype = null;
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException {
//		productgroup = AppData.getProductgroupLabel(obj.getProductgroup());
		pinpadtype = "";
		doSearch();
	}

	public int getPageStartNumber() {
		return pageStartNumber;
	}

	public void setPageStartNumber(int pageStartNumber) {
		this.pageStartNumber = pageStartNumber;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Tbranchstock getObj() {
		return obj;
	}

	public void setObj(Tbranchstock obj) {
		this.obj = obj;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public Integer getItemqty() {
		return itemqty;
	}

	public void setItemqty(Integer itemqty) {
		this.itemqty = itemqty;
	}

	public Boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public Paging getPaging() {
		return paging;
	}

	public String getStocktype() {
		return stocktype;
	}

	public void setStocktype(String stocktype) {
		this.stocktype = stocktype;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

	public String getPinpadtype() {
		return pinpadtype;
	}

	public void setPinpadtype(String pinpadtype) {
		this.pinpadtype = pinpadtype;
	}

}
