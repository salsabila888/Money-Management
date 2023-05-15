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
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
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
import org.zkoss.zul.Button;
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

import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Vstatusembossbranch;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class EmbossBranchVm {

	private TembossbranchDAO oDao = new TembossbranchDAO();

	private Tembossproduct tembossproduct;
	private Vstatusembossbranch countstatus;
	
	private Date appdate;
	private Date appdateto;
	private Integer totaldata;
	private int totalallstatus;
	private String date;
	private String filter;
	private String branchname;
	private String titlestatus;

	List<Tembossbranch> objList = new ArrayList<>();
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Window winOrderbranch;
	@Wire
	private Groupbox gbHeader;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Div divReport;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("tembossproduct") final Tembossproduct tembossproduct,
			@ExecutionArgParam("date") String date, @ExecutionArgParam("appdate") Date appdate,
			@ExecutionArgParam("appdateto") Date appdateto, @ExecutionArgParam("isReport") String isReport)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		this.tembossproduct = tembossproduct;

		if (isReport != null && isReport.equals("Y")) {
			this.date = date;
			this.appdate = appdate;
			this.appdateto = appdateto;
			divReport.setVisible(true);
		}

		countstatus = oDao.coundStatusBranch("tembossproductfk = " + tembossproduct.getTembossproductpk());
		titlestatus = "ALL";

		doView("all");
		grid.setRowRenderer(new RowRenderer<Tembossbranch>() {

			@Override
			public void render(Row row, final Tembossbranch data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(
						new Label(data.getOrderdate() != null ? datelocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(
						data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
				Button btndetail = new Button("Detail");
				btndetail.setAutodisable("self");
				btndetail.setClass("btn btn-default btn-sm");
				btndetail.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("tembossbranch", data);

						Window win = (Window) Executions.createComponents("/view/emboss/embossdata.zul", null, map);
						win.setWidth("95%");
						win.setClosable(true);
						win.doModal();
					}
				});
				row.getChildren().add(btndetail);
				totaldata += data.getTotaldata() != null ? data.getTotaldata() : 0;
				BindUtils.postNotifyChange(null, null, EmbossBranchVm.this, "totaldata");
			}
		});
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			totaldata = 0;
			grid.setModel(new ListModelList<>(oDao.listByFilter(filter, "tembossbranchpk")));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doSearch() {
		if (branchname != null && branchname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mbranch.branchname like '" + branchname.trim().toUpperCase() + "%'";
		}
		refreshModel();
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winOrderbranch, null);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doView(@BindingParam("arg") String arg) throws Exception {

		if (arg.equals("all")) {
			filter = "tembossproductfk = " + tembossproduct.getTembossproductpk();
		} else if (arg.equals("pendingproduksi")) {
			filter = "tembossproductfk = " + tembossproduct.getTembossproductpk() + " and status = '"
					+ AppUtils.STATUSBRANCH_PENDINGPRODUKSI + "'";
			titlestatus = "PENDING PRODUKSI";
		} else if (arg.equals("pendingpaket")) {
			filter = "tembossproductfk = " + tembossproduct.getTembossproductpk() + " and status = '"
					+ AppUtils.STATUSBRANCH_PENDINGPAKET + "'";
			titlestatus = "PENDING PAKET";
		} else if (arg.equals("pendingdelivery")) {
			filter = "tembossproductfk = " + tembossproduct.getTembossproductpk() + " and status = '"
					+ AppUtils.STATUSBRANCH_PENDINGDELIVERY + "'";
			titlestatus = "PENDING DELIVERY";
		} else if (arg.equals("prosesproduksi")) {
			filter = "tembossproductfk = " + tembossproduct.getTembossproductpk() + " and status = '"
					+ AppUtils.STATUSBRANCH_PROSESPRODUKSI + "'";
			titlestatus = "PROSES PRODUKSI";
		} else if (arg.equals("prosespaket")) {
			filter = "tembossproductfk = " + tembossproduct.getTembossproductpk() + " and status = '"
					+ AppUtils.STATUSBRANCH_PROSESPAKET + "'";
			titlestatus = "PROSES PAKET";
		} else if (arg.equals("prosesdelivery")) {
			filter = "tembossproductfk = " + tembossproduct.getTembossproductpk() + " and status = '"
					+ AppUtils.STATUSBRANCH_PROSESDELIVERY + "'";
			titlestatus = "PROSES DELIVERY";
		} else if (arg.equals("delivered")) {
			filter = "tembossproductfk = " + tembossproduct.getTembossproductpk() + " and status = '"
					+ AppUtils.STATUSBRANCH_DELIVERED + "'";
			titlestatus = "DELIVERED";
		}

		refreshModel();

	}

	@Command
	public void doExport() {
		try {
			objList = oDao.listByFilter(filter, "tembossbranchpk");
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
				if (date.equals("D"))
					cell.setCellValue("Laporan Order Cabang Harian");
				else
					cell.setCellValue("Laporan Order Cabang Periode Bulanan");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				if (date.equals("D"))
					cell.setCellValue(
							"Tanggal " + new SimpleDateFormat("dd MMM yyyy").format(tembossproduct.getOrderdate()));
				else
					cell.setCellValue("Periode " + new SimpleDateFormat("dd MMM yyyy").format(appdate) + " s/d "
							+ appdateto != null ? new SimpleDateFormat("dd MMM yyyy").format(appdateto) : "-");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "Nama Cabang", "Tanggal Data", "Jumlah", "Status" });
				no = 2;
				for (Tembossbranch data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getMbranch().getBranchname(), datelocalFormatter.format(data.getOrderdate()),
									data != null ? data.getTotaldata() : 0, AppData.getStatusLabel(data.getStatus()) });
					no++;
				}
				datamap.put(no, new Object[] { "", "TOTAL", "", totaldata, "" });
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
				String filename = "CIMS_ORDER_DAILY_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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

	@Command
	@NotifyChange("*")
	public void doReset() {
		branchname = null;
		titlestatus = "ALL";
		doSearch();
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Tembossproduct getTembossproduct() {
		return tembossproduct;
	}

	public void setTembossproduct(Tembossproduct tembossproduct) {
		this.tembossproduct = tembossproduct;
	}

	public Vstatusembossbranch getCountstatus() {
		return countstatus;
	}

	public void setCountstatus(Vstatusembossbranch countstatus) {
		this.countstatus = countstatus;
	}

	public String getTitlestatus() {
		return titlestatus;
	}

	public void setTitlestatus(String titlestatus) {
		this.titlestatus = titlestatus;
	}

	public int getTotalallstatus() {
		return totalallstatus;
	}

	public void setTotalallstatus(int totalallstatus) {
		this.totalallstatus = totalallstatus;
	}

}
