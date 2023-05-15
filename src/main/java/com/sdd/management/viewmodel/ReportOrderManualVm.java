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
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.model.TorderListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportOrderManualVm {

	private TorderListModel model;
	private TorderDAO oDao = new TorderDAO();
	private List<Torder> objList = new ArrayList<>();

	private boolean needsPageUpdate;
	private int totaldata;
	private int pageTotalSize;
	private int pageStartNumber;
	private Date appdate;
	private Date appdateto;
	private String filter;
	private String orderby;
	private String productgroup;
	private String ordertype;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Label caption;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("type") String ordertype) throws Exception {
		Selectors.wireComponents(view, this, false);

		productgroup = arg;
		this.ordertype = ordertype;
		
		caption.setValue("Laporan Order Manual " + AppData.getProductgroupTitle(productgroup));

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Torder>() {

			@Override
			public void render(Row row, final Torder data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));

				row.getChildren().add(new Label(data.getOrderid()));
				row.getChildren().add(new Label((data.getMbranch().getBranchid())));
				row.getChildren().add(new Label((data.getMbranch().getBranchname())));
				row.getChildren().add(new Label(datelocalFormatter.format(data.getInserttime())));
				row.getChildren()
				.add(new Label(data.getTotalcs() != null ? NumberFormat.getInstance().format(data.getTotalcs()) : "0"));
				row.getChildren()
				.add(new Label(data.getTotalteller() != null ? NumberFormat.getInstance().format(data.getTotalteller()) : "0"));
				row.getChildren()
						.add(new Label(data != null ? NumberFormat.getInstance().format(data.getItemqty()) : "0"));
				row.getChildren().add(new Label(data.getMemo()));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
			}
		});
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (appdate != null) {
				if (appdateto != null)
					filter = "orderdate between '" + dateFormatter.format(appdate) + "' and '"
							+ dateFormatter.format(appdateto) + "'";
				else
					filter = "orderdate = '" + dateFormatter.format(appdate) + "'";
				
				if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
					if (ordertype != null && ordertype.equals(AppUtils.ENTRYTYPE_MANUAL)) {
						filter += " and productgroup = '" + productgroup + "' and ordertype = '" + AppUtils.ENTRYTYPE_MANUAL
								+ "'";
					} else {
						filter += " and productgroup = '" + productgroup + "' and ordertype = '"
								+ AppUtils.ENTRYTYPE_MANUAL_BRANCH + "'";
					}
				} else {
					filter += " and productgroup = '" + productgroup + "'";
				}

				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
	
				totaldata = 0;
				objList = oDao.listByFilter(filter, "torderpk");
				for (Torder data : objList) {
					totaldata = totaldata + data.getItemqty();
				}
			} else {
				Messagebox.show("Silahkan masukan tanggal awal terlebih dahulu.", "Warning", Messagebox.OK,
						Messagebox.EXCLAMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange({ "pageTotalSize", "total" })
	public void refreshModel(int activePage) {
		try {
			orderby = "torderpk";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new TorderListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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

	@Command
	@NotifyChange("*")
	public void doReset() {
		totaldata = 0;
		objList = new ArrayList<>();
		grid.setModel(new ListModelList<>(objList));
		appdate = null;
		appdateto = null;
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
				if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
					if (ordertype != null && ordertype.equals(AppUtils.ENTRYTYPE_MANUAL_BRANCH))
						cell.setCellValue("Laporan Order " + AppData.getProductgroupTitle(productgroup)
								+ " Cabang");
					else
						cell.setCellValue("Laporan Order " + AppData.getProductgroupTitle(productgroup)
								+ " Produksi");
				} else {
					cell.setCellValue(
							"Laporan Order " + AppData.getProductgroupTitle(productgroup));
				}
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Periode " + new SimpleDateFormat("dd MMM yyyy").format(appdate) + " s/d "
						+ appdateto != null ? new SimpleDateFormat("dd MMM yyyy").format(appdateto) : "-");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "Order ID", "Kode Cabang", "Cabang", "Tanggal Order", 
						"Jumlah Pinpad CS", "Jumlah Pinpad Teller", "Total Pinpad", "Status" });
				no = 2;
				for (Torder data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getOrderid(), data.getMbranch().getBranchid(), data.getMbranch().getBranchname(),
									datelocalFormatter.format(data.getInserttime()), data.getTotalcs() != null ? data.getTotalcs() : 0,
									data.getTotalteller() != null ? data.getTotalteller() : 0, data.getItemqty() != null ? data.getItemqty() : 0,
									AppData.getStatusLabel(data.getStatus()) });
					no++;
				}
				datamap.put(no, new Object[] { "", "TOTAL", "", "", "", "", "", totaldata, "" });
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
				String filename = "CAPTION_ORDER_" + AppData.getProductgroupLabel(productgroup) + "_DAILY_"
						+ new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
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
}
