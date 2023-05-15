package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Vsumbranchstock;
import com.sdd.caption.domain.Vsumproductstock;
import com.sdd.caption.model.TbranchstockListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;

public class ReportOutletStockVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private TbranchstockDAO oDao = new TbranchstockDAO();
	private TbranchstockListModel model;

	List<Tbranchstock> objList;
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;

	private BigDecimal totalmasuk;
	private BigDecimal totalcabang;
	private BigDecimal totalkeluar;
	private BigDecimal totaldestroyed;
	private String productname;
	private String outlet;
	private Mbranch objBranch;
	private Vsumproductstock objProduct;

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("objBranch") Mbranch objBranch, @ExecutionArgParam("obj") Vsumproductstock objProduct)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		this.objBranch = objBranch;
		this.objProduct = objProduct;

		productname = AppData.getProductgroupLabel(arg) + " - " + objProduct.getProductname();

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tbranchstock>() {
				@Override
				public void render(Row row, final Tbranchstock data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getMbranch().getBranchname()));
					row.getChildren().add(new Label(data.getOutlet()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));

					A aStockDlv = new A(data.getStockdelivered() != null
							? NumberFormat.getInstance().format(data.getStockdelivered())
							: "0");
					aStockDlv.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("obj", data);
							map.put("arg", arg);
							map.put("stocktype", "in");
							Window win = (Window) Executions.createComponents("/view/report/reportbranchstockitem.zul",
									null, map);
							win.setWidth("75%");
							win.setClosable(true);
							win.doModal();

						}
					});
					row.getChildren().add(aStockDlv);

					A aStockAct = new A(data.getStockactivated() != null
							? NumberFormat.getInstance().format(data.getStockactivated())
							: "0");
					aStockAct.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("obj", data);
							map.put("arg", arg);
							map.put("stocktype", "out");
							Window win = (Window) Executions.createComponents("/view/report/reportbranchstockitem.zul",
									null, map);
							win.setWidth("75%");
							win.setClosable(true);
							win.doModal();

						}
					});
					row.getChildren().add(aStockAct);
					
					A aStockDes = new A(
							data.getStockcabang() != null ? NumberFormat.getInstance().format(data.getStockdestroyed())
									: "0");
					aStockDes.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("obj", data);
							map.put("stocktype", "destroyed");
							Window win = (Window) Executions.createComponents("/view/report/reportbranchstockitem.zul",
									null, map);
							win.setWidth("75%");
							win.setClosable(true);
							win.doModal();

						}
					});
					row.getChildren().add(aStockDes);

					A aStockCab = new A(
							data.getStockcabang() != null ? NumberFormat.getInstance().format(data.getStockcabang())
									: "0");
					aStockCab.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("obj", data);
							map.put("stocktype", "current");
							Window win = (Window) Executions.createComponents("/view/report/reportbranchstockitem.zul",
									null, map);
							win.setWidth("75%");
							win.setClosable(true);
							win.doModal();

						}
					});
					row.getChildren().add(aStockCab);
				}
			});
		}
		doReset();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TbranchstockListModel(activePage, SysUtils.PAGESIZE, filter, "branchid");
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}

		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange({ "pageTotalSize", "totalmasuk", "totalcabang", "totalkeluar" })
	public void doSearch() {
		try {
			if (oUser != null) {
				filter = "mbranchfk = " + objBranch.getMbranchpk() + " and mproductfk = " + objProduct.getMproductpk();

				if (outlet != null && !"".equals(outlet.trim()))
					filter += " and outlet = '" + outlet.trim() + "'";
				totalmasuk = new BigDecimal(0);
				totalcabang = new BigDecimal(0);
				totalkeluar = new BigDecimal(0);
				totaldestroyed = new BigDecimal(0);
				
				objList = oDao.listNativeListByFilter(filter, "tbranchstockpk");
				for (Tbranchstock data : objList) {
					totalmasuk = totalmasuk.add(new BigDecimal(data.getStockdelivered()));
					totalcabang = totalcabang.add(new BigDecimal(data.getStockcabang()));
					totalkeluar = totalkeluar.add(new BigDecimal(data.getStockactivated()));
					totaldestroyed = totaldestroyed.add(new BigDecimal(data.getStockdestroyed()));
				}

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
		outlet = null;
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
				cell.setCellValue("Laporan Stock Cabang");
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Cabang");
				cell = row.createCell(1);
				cell.setCellValue(objBranch.getBranchname());
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Tanggal");
				cell = row.createCell(1);
				cell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

				row = sheet.createRow(rownum++);

				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "Nama Cabang", "Nama Outlet", "Grup Produk", "Nama Produk",
						"Incoming", "Outgoing", "Destroyed", "Stock" });
				no = 2;
				for (Tbranchstock data : objList) {
					datamap.put(no, new Object[] { no - 1, data.getMbranch().getBranchname(), data.getOutlet(),
							AppData.getProductgroupLabel(data.getProductgroup()), data.getMproduct().getProductname(),
							data.getStockdelivered() != null
									? NumberFormat.getInstance().format(data.getStockdelivered())
									: "0",
							data.getStockactivated() != null
									? NumberFormat.getInstance().format(data.getStockactivated())
									: "0",
							data.getStockdestroyed() != null
									? NumberFormat.getInstance().format(data.getStockdestroyed())
									: "0",
							data.getStockcabang() != null ? NumberFormat.getInstance().format(data.getStockcabang())
									: "0" });
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
				String filename = "PRODUCT_BRANCHSTOCK_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
						+ ".xlsx";
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

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public BigDecimal getTotalmasuk() {
		return totalmasuk;
	}

	public void setTotalmasuk(BigDecimal totalmasuk) {
		this.totalmasuk = totalmasuk;
	}

	public BigDecimal getTotalcabang() {
		return totalcabang;
	}

	public void setTotalcabang(BigDecimal totalcabang) {
		this.totalcabang = totalcabang;
	}

	public BigDecimal getTotalkeluar() {
		return totalkeluar;
	}

	public void setTotalkeluar(BigDecimal totalkeluar) {
		this.totalkeluar = totalkeluar;
	}

	public String getOutlet() {
		return outlet;
	}

	public void setOutlet(String outlet) {
		this.outlet = outlet;
	}

	public BigDecimal getTotaldestroyed() {
		return totaldestroyed;
	}

	public void setTotaldestroyed(BigDecimal totaldestroyed) {
		this.totaldestroyed = totaldestroyed;
	}
}