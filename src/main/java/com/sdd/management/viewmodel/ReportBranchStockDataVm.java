package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zkoss.zul.A;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MorgDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Vbranchstock;
import com.sdd.caption.model.TbranchstockListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportBranchStockDataVm {

	private TbranchstockListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;

	private TbranchstockDAO oDao = new TbranchstockDAO();
	private Vbranchstock obj;
	private String productcode;
	private String klncode;
	private String isinstant;
	private String productgroup;
	private Integer totaldata;
	private String type;

	private Map<String, String> mapOrg;
	List<Tbranchstock> objList = new ArrayList<>();

	@Wire
	private Window winReportbranchstockdata;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Vbranchstock obj,
			@ExecutionArgParam("isinstant") String isinstant, @ExecutionArgParam("productcode") String productcode)
			throws Exception {
		Selectors.wireComponents(view, this, false);

		this.obj = obj;
		this.isinstant = isinstant;
		mapOrg = AppData.getOrgmap();

		if (isinstant != null) {
			if (isinstant.equals("Y")) {
				totaldata = obj.getTotalinstant();
				type = "KARTU INSTANT";
			} else {
				totaldata = obj.getTotalnotinstant();
				type = "KARTU BERNAMA";
			}
		}

		if (productcode != null)
			this.productcode = productcode;

		productgroup = "KARTU";

		doReset();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		grid.setRowRenderer(new RowRenderer<Tbranchstock>() {

			@Override
			public void render(Row row, final Tbranchstock data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				row.getChildren().add(new Label(mapOrg.get(data.getMproduct().getMproducttype().getProductorg())));
				row.getChildren().add(new Label(data.getMproduct().getMproducttype().getProducttype()));
				row.getChildren().add(new Label(data.getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));

				A a = new A(
						data.getStockcabang() != null ? NumberFormat.getInstance().format(data.getStockcabang()) : "0");
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);

						Window win = new Window();
						Morg org = new MorgDAO().findById(data.getMproduct().getMproducttype().getProductorg());
						if (org.getIsneeddoc().equals("N")) {
							System.out.println("REGULAR");
							win = (Window) Executions.createComponents("/view/report/reportbranchstockdatadetail.zul",
									null, map);
						} else {
							System.out.println("DERIVATIF");
							win = (Window) Executions.createComponents("/view/report/reportbranchstockdrvdatadetail.zul",
									null, map);
						}
						win.setWidth("90%");
						win.setClosable(true);
						win.doModal();
					}
				});

				if(data.getStockcabang() == 0)
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getStockcabang())));
				else
					row.getChildren().add(a);
			}
		});
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "mbranchfk = " + obj.getMbranchpk() + " and mproduct.productgroup = '01'";

		if (isinstant != null) {
			filter += " and mproduct.isinstant = '" + isinstant + "'";
		}

		if (productcode != null && productcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
		}

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "mproduct.productcode";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TbranchstockListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winReportbranchstockdata, null);
		Events.postEvent(closeEvent);
	}

	@NotifyChange("*")
	public void doReset() {
		productcode = "";
		doSearch();
	}

	@Command
	public void doExport() {
		try {
			objList = oDao.listByFilter(filter, "mproduct.productcode");
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
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("Daftar Stock Cabang");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(0, new Object[] { "No", "ORG", "Tipe Produk", "Code Produk", "Jenis Produk", "Jumlah" });
				no = 2;
				for (Tbranchstock data : objList) {
					datamap.put(no,
							new Object[] { no - 1, mapOrg.get(data.getMproduct().getMproducttype().getProductorg()),
									data.getMproduct().getMproducttype().getProducttype(),
									data.getMproduct().getProductcode(), data.getMproduct().getProductname(),
									NumberFormat.getInstance().format(data.getStockcabang()) });
					no++;
				}
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 2) {
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
				String filename = "LIST_STOCK_CABANG" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
				workbook.write(out);
				out.close();

				Filedownload.save(new File(path + "/" + filename),
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			} else {
				Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	public Vbranchstock getObj() {
		return obj;
	}

	public void setObj(Vbranchstock obj) {
		this.obj = obj;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public String getKlncode() {
		return klncode;
	}

	public void setKlncode(String klncode) {
		this.klncode = klncode;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}
}
