package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
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
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class ReportDivisiStockVm {

	private List<Mproducttype> objList = new ArrayList<Mproducttype>();
	private MproducttypeDAO oDao = new MproducttypeDAO();

	private String filter;
	private String arg;

	private String productgroup;
	private BigDecimal totalstock;
	private int totalrecord;
	private Mproducttype mproducttype;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	@Wire
	private Combobox cbProducttype;
	@Wire
	private Grid grid;
	@Wire
	private Column colInject, colUnused, colReserved;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg) {
		Selectors.wireComponents(view, this, false);

		this.arg = arg;
		productgroup = AppData.getProductgroupLabel(arg);

		if (arg.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
			colInject.setVisible(true);
			colReserved.setVisible(true);
		} else if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
			colUnused.setVisible(true);
			colReserved.setVisible(true);
		}

		doReset();
		grid.setRowRenderer(new RowRenderer<Mproducttype>() {
			@Override
			public void render(Row row, final Mproducttype data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getProducttype()));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getStockmin())));

				A aStockDlv = new A(
						data.getLaststock() != null ? NumberFormat.getInstance().format(data.getLaststock()) : "0");
				aStockDlv.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("producttype", data);
						Window win = new Window();
						if (data.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
							win = (Window) Executions.createComponents("/view/inventory/incomingsecuritiesdata.zul",
									null, map);
						} else if (data.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
							win = (Window) Executions.createComponents("/view/inventory/incomingpinpaddata.zul", null,
									map);
						} else if (data.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
							System.out.println("REPORT TOKEN");
							win = (Window) Executions.createComponents("/view/inventory/incomingtokendata.zul", null,
									map);
						}
						win.setWidth("70%");
						win.setClosable(true);
						win.doModal();

					}
				});
				if(!arg.equals(AppUtils.PRODUCTGROUP_CARD))
					row.getChildren().add(aStockDlv);
				else 
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getLaststock())));
				
				A aStockout = new A(
						data.getStockreserved() != null ? NumberFormat.getInstance().format(data.getStockreserved())
								: "0");
				aStockout.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("producttype", data);
						map.put("type", "outstanding");
						Window win = new Window();
						if (data.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
							win = (Window) Executions.createComponents("/view/inventory/incomingtokendata.zul", null,
									map);
						}if (data.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
							win = (Window) Executions.createComponents("/view/inventory/incomingpinpaddata.zul", null,
									map);
						}
						win.setWidth("45%");
						win.setClosable(true);
						win.doModal();

					}
				});
				row.getChildren().add(aStockout);

				A aStockInj = new A(
						data.getStockinjected() != null ? NumberFormat.getInstance().format(data.getStockinjected())
								: "0");
				aStockInj.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("producttype", data);
						map.put("type", "inject");
						Window win = new Window();
						if (data.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
							win = (Window) Executions.createComponents("/view/inventory/incomingtokendata.zul", null,
									map);
						}
						win.setWidth("45%");
						win.setClosable(true);
						win.doModal();

					}
				});
				row.getChildren().add(aStockInj);

				A aStockUnused = new A(
						data.getStockunused() != null ? NumberFormat.getInstance().format(data.getStockunused())
								: "0");
				aStockUnused.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("producttype", data);
						map.put("type", "unused");
						Window win = new Window();
						if (data.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
							win = (Window) Executions.createComponents("/view/inventory/incomingpinpaddata.zul", null,
									map);
						}
						win.setWidth("45%");
						win.setClosable(true);
						win.doModal();

					}
				});
				row.getChildren().add(aStockUnused);
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getLastupdated())));
			}
		});
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			filter = "productgroupcode = '" + arg.trim() + "'";

			if (mproducttype != null)
				filter += " and mproducttypepk = " + mproducttype.getMproducttypepk();

			refreshModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			totalstock = new BigDecimal(0);
			totalrecord = 0;
			objList = oDao.listByFilter(filter, "producttype");
			grid.setModel(new ListModelList<>(objList));

			for (Mproducttype data : objList) {
				totalstock = totalstock.add(new BigDecimal(data.getLaststock()));
			}

			totalrecord = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		mproducttype = null;
		cbProducttype.setValue(null);
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		doSearch();
	}

	@Command
	public void doExport() {
		try {
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
				cell.setCellValue("LAPORAN STOCK PRODUK");
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Tanggal");
				cell = row.createCell(1);
				cell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

				row = sheet.createRow(rownum++);

				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD))
					datamap.put(1, new Object[] { "No", "Tipe Product", "Pagu", "Stock", "Stock Tidak Terpakai",
							"Last Updated" });
				else if (arg.equals(AppUtils.PRODUCTGROUP_TOKEN))
					datamap.put(1,
							new Object[] { "No", "Tipe Product", "Pagu", "Stock", "Stock Outstanding", "Stock Injected", "Last Updated" });
				else
					datamap.put(1, new Object[] { "No", "Tipe Product", "Pagu", "Stock", "Last Updated" });
				no = 2;
				for (Mproducttype data : objList) {
					if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
						datamap.put(no,
								new Object[] { no - 1, data.getProducttype(),
										NumberFormat.getInstance().format(data.getStockmin()),
										NumberFormat.getInstance().format(data.getLaststock()),
										NumberFormat.getInstance().format(data.getStockunused()),
										dateLocalFormatter.format(data.getLastupdated()) });
					} else if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
						datamap.put(no,
								new Object[] { no - 1, data.getProducttype(),
										NumberFormat.getInstance().format(data.getStockmin()),
										NumberFormat.getInstance().format(data.getLaststock()),
										NumberFormat.getInstance().format(data.getStockinjected()),
										dateLocalFormatter.format(data.getLastupdated()) });
					} else {
						datamap.put(no,
								new Object[] { no - 1, data.getProducttype(),
										NumberFormat.getInstance().format(data.getStockmin()),
										NumberFormat.getInstance().format(data.getLaststock()),
										dateLocalFormatter.format(data.getLastupdated()) });
					} 
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
				String filename = "CIMS_STOCK_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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

	public ListModelList<Mproducttype> getMproducttypemodel() {
		ListModelList<Mproducttype> lm = null;
		try {
			lm = new ListModelList<Mproducttype>(AppData.getMproducttype("productgroupcode = '" + arg.trim() + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public BigDecimal getTotalstock() {
		return totalstock;
	}

	public void setTotalstock(BigDecimal totalstock) {
		this.totalstock = totalstock;
	}

	public int getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(int totalrecord) {
		this.totalrecord = totalrecord;
	}

	public Mproducttype getMproducttype() {
		return mproducttype;
	}

	public void setMproducttype(Mproducttype mproducttype) {
		this.mproducttype = mproducttype;
	}
}
