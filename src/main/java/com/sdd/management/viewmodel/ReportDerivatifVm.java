package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
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
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TderivatifproductDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Tderivatifproduct;
import com.sdd.caption.model.TderivatifproductListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportDerivatifVm {

	private TderivatifproductListModel model;
	private TderivatifproductDAO oDao = new TderivatifproductDAO();
	//private Vstatusderivatif obj;

	private boolean needsPageUpdate;
	private int totaldata;
	private int pageTotalSize;
	private int pageStartNumber;
	private Date appdate;
	private Date appdateto;
	private String filter;
	private String orderby;
	private String productcode;
	private String productname;
	private String producttype;

	private Morg morg;

	private List<Tderivatifproduct> objList = new ArrayList<>();

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		try {
			doReset();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//obj = oDao.countStatus();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tderivatifproduct>() {

			@Override
			public void render(Row row, final Tderivatifproduct data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));

				row.getChildren().add(new Label(data.getTderivatif().getOrderno()));
				row.getChildren().add(new Label(data.getTderivatif().getMbranch().getBranchname()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));
				row.getChildren()
						.add(new Label(data != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				row.getChildren().add(new Label(AppData.getStatusDerivatifLabel(data.getTderivatif().getStatus())));
				row.getChildren().add(new Label(data.getTderivatif().getMemo()));
			}
		});

	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (appdate != null) {
				if (appdateto != null)
					filter = "tderivatif.orderdate between '" + dateFormatter.format(appdate) + "' and '"
							+ dateFormatter.format(appdateto) + "'";
				else
					filter = "tderivatif.orderdate = '" + dateFormatter.format(appdate) + "'";
			} else {
				Messagebox.show("Silahkan masukan tanggal awal terlebih dahulu.", "Warning", Messagebox.OK,
						Messagebox.EXCLAMATION);
			}

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);

			totaldata = 0;
			objList = oDao.sumTotalByFilter(filter);
			for (Tderivatifproduct data : objList) {
				totaldata = totaldata + data.getTotaldata();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange({ "pageTotalSize", "total" })
	public void refreshModel(int activePage) {
		try {
			orderby = "orderno, tderivatifproduct.orderdate";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new TderivatifproductListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
			if (needsPageUpdate) {
				pageTotalSize = model.getTotalSize(filter);
				needsPageUpdate = false;
			}
			paging.setTotalSize(pageTotalSize);
			grid.setModel(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*@Command
	@NotifyChange("*")
	public void doView(@BindingParam("arg") String arg) throws Exception {
		if (arg.equals("scan")) {
			filter = "status = " + AppUtils.STATUS_DERIVATIF_SCAN;
			titlestatus = "SCAN";
		} else if (arg.equals("crop")) {
			filter = "status = " + AppUtils.STATUS_DERIVATIF_CROP;
			titlestatus = "CROP";
		} else if (arg.equals("merge")) {
			filter = "status = " + AppUtils.STATUS_DERIVATIF_MERGE;
			titlestatus = "MERGING";
		} else if (arg.equals("perso")) {
			filter = "status in (" + AppUtils.STATUS_DERIVATIF_ORDERPERSO + "," + AppUtils.STATUS_DERIVATIF_ORDERPERSOAPPROVAL + ""
					+ "," + AppUtils.STATUS_DERIVATIF_ORDERPERSOINVENTORYAPPROVAL + ")";
			titlestatus = "PERSO";
		} else if (arg.equals("paket")) {
			filter = "status = " + AppUtils.STATUS_DERIVATIF_PAKET;
			titlestatus = "PAKET";
		} else if (arg.equals("delivery")) {
			filter = "status = " + AppUtils.STATUS_DERIVATIF_DELIVERY;
			titlestatus = "DELIVERY";
		}
		
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);

		totaldata = 0;
		objList = oDao.sumTotalByFilter(filter);
		for (Tderivatif data : objList) {
			totaldata = totaldata + data.getTotaldata();
		}
	}*/

	@Command
	@NotifyChange("*")
	public void doReset() {
		morg = null;
		productcode = null;
		productname = null;
		producttype = null;
		appdate = null;
		appdateto = null;
		totaldata = 0;
		objList = new ArrayList<>();
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
				cell.setCellValue("Laporan Order Berfoto Periode Bulanan");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Periode " + new SimpleDateFormat("dd MMM yyyy").format(appdate) + " s/d "
						+ appdateto != null ? new SimpleDateFormat("dd MMM yyyy").format(appdateto) : "-");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "No. Order", "Cabang", "Jenis Produk", "Tgl Cabang Order", "Jumlah Order", "Status", "Memo" });
				no = 2;
				for (Tderivatifproduct data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getTderivatif().getOrderno(), data.getTderivatif().getMbranch().getBranchname(),
									data.getMproduct().getProductname(), datelocalFormatter.format(data.getOrderdate()),
									data != null ? data.getTotaldata() : 0, AppData.getStatusDerivatifLabel(data.getTderivatif().getStatus()), data.getTderivatif().getMemo() });
					no++;
				}
				datamap.put(no, new Object[] { "", "TOTAL", "", "", "", totaldata, "" });
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 4) {
						XSSFCellStyle styleHeader = workbook.createCellStyle();
						styleHeader.setBorderTop(BorderStyle.MEDIUM);
						styleHeader.setBorderBottom(BorderStyle.MEDIUM);
						styleHeader.setBorderLeft(BorderStyle.MEDIUM);
						styleHeader.setBorderRight(BorderStyle.MEDIUM);
						styleHeader.setFillForegroundColor(IndexedColors.CORAL.getIndex());
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
				String filename = "CAPTION_BERFOTO_DAILY_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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

	public ListModelList<Morg> getMorgmodel() {
		ListModelList<Morg> lm = null;
		try {
			lm = new ListModelList<Morg>(AppData.getMorg());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
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

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public Morg getMorg() {
		return morg;
	}

	public void setMorg(Morg morg) {
		this.morg = morg;
	}

	public Date getAppdate() {
		return appdate;
	}

	public void setAppdate(Date appdate) {
		this.appdate = appdate;
	}

	public Date getAppdateto() {
		return appdateto;
	}

	public void setAppdateto(Date appdateto) {
		this.appdateto = appdateto;
	}

	/*public Vstatusderivatif getObj() {
		return obj;
	}

	public void setObj(Vstatusderivatif obj) {
		this.obj = obj;
	}*/


}
