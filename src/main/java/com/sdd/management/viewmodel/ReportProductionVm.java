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
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.model.TpersoListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportProductionVm {

	private TpersoListModel model;
	private TpersoDAO oDao = new TpersoDAO();

	private boolean needsPageUpdate;
	private Date orderdate;
	private Date persostarttime;
	private int totaldata;
	private int totalpaket;
	private int pageTotalSize;
	private int pageStartNumber;
	private Date appdate;
	private Date appdateto;
	private String filter;
	private String orderby;
	private String productcode;
	private String productname;
	private String persoid;
	private String time;

	private List<Tperso> objList = new ArrayList<>();
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Row monthly, daily;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		time = arg;
		doReset();

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tperso>() {

			@Override
			public void render(Row row, final Tperso data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				row.getChildren().add(new Label(data.getPersoid()));
				row.getChildren().add(new Label(data.getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(
						new Label(data.getOrderdate() != null ? dateLocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(data.getPersostartby()));
				row.getChildren()
						.add(new Label(data != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				row.getChildren()
						.add(new Label(data != null ? NumberFormat.getInstance().format(data.getTotalpaket()) : "0"));
			}
		});

	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (time.equals("D")) {
				if (persostarttime != null) {
					filter = "DATE(persostarttime) = '" + dateFormatter.format(persostarttime)
							+ "' and status not in ('" + AppUtils.STATUS_PERSO_PERSOWAITAPPROVAL + "', '"
							+ AppUtils.STATUS_PERSO_PERSODECLINE + "', '" + AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL
							+ "', '" + AppUtils.STATUS_PERSO_OUTGOINGDECLINE + "', '" + AppUtils.STATUS_PERSO_PRODUKSI
							+ "')";

					if (persoid != null && persoid.length() > 0)
						filter += " and persoid = '" + persoid + "'";
					if (productcode != null && productcode.length() > 0)
						filter += " and productcode = '" + productcode + "'";
					if (productname != null && productname.length() > 0)
						filter += " and productname = '" + productname + "'";
					if (orderdate != null)
						filter += " and orderdate = '" + dateFormatter.format(orderdate) + "'";

					needsPageUpdate = true;
					paging.setActivePage(0);
					pageStartNumber = 0;
					refreshModel(pageStartNumber);

					totaldata = 0;
					totalpaket = 0;
					objList = oDao.listByFilter(filter, "tpersopk");
					for (Tperso data : objList) {
						totaldata = totaldata + data.getTotaldata();
						totalpaket = totalpaket + data.getTotalpaket();
					}
				} else {
					Messagebox.show("Silahkan masukan tanggal data terlebih dahulu.", "Warning", Messagebox.OK,
							Messagebox.EXCLAMATION);
				}
			} else {
				if (appdate != null) {
					if (appdateto != null)
						filter = "persostarttime between '" + dateFormatter.format(appdate) + "' and '"
								+ dateFormatter.format(appdateto) + "'";
					else
						filter = "persostarttime = '" + dateFormatter.format(appdate) + "'";

					filter += " and status not in ('" + AppUtils.STATUS_PERSO_PERSOWAITAPPROVAL + "', '"
							+ AppUtils.STATUS_PERSO_PERSODECLINE + "', '" + AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL
							+ "', '" + AppUtils.STATUS_PERSO_OUTGOINGDECLINE + "', '" + AppUtils.STATUS_PERSO_PRODUKSI
							+ "')";

					if (persoid != null && persoid.length() > 0)
						filter += " and persoid like '%" + persoid + "%'";
					if (productcode != null && productcode.length() > 0)
						filter += " and productcode like '%" + productcode + "%'";
					if (productname != null && productname.length() > 0)
						filter += " and productname like '%" + productname + "%'";
					if (orderdate != null)
						filter += " and orderdate = '" + dateFormatter.format(orderdate) + "'";

					needsPageUpdate = true;
					paging.setActivePage(0);
					pageStartNumber = 0;
					refreshModel(pageStartNumber);

					totaldata = 0;
					totalpaket = 0;
					objList = oDao.listByFilter(filter, "tpersopk");
					for (Tperso data : objList) {
						totaldata = totaldata + data.getTotaldata();
						totalpaket = totalpaket + data.getTotalpaket();
					}
				} else {
					Messagebox.show("Silahkan masukan tanggal awal terlebih dahulu.", "Warning", Messagebox.OK,
							Messagebox.EXCLAMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange({ "pageTotalSize", "total" })
	public void refreshModel(int activePage) {
		try {
			orderby = "tpersopk";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new TpersoListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		productcode = null;
		productname = null;
		persoid = null;
		totaldata = 0;
		totalpaket = 0;
		objList = new ArrayList<>();
		orderdate = null;
		persostarttime = null;
		appdate = null;
		appdateto = null;

		if (time.equals("M"))
			daily.setVisible(false);
		else
			monthly.setVisible(false);

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
				if (time.equals("D"))
					cell.setCellValue("Laporan Order Harian");
				else
					cell.setCellValue("Laporan Order Periode Bulanan");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Laporan Produktivitas Produksi");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "Perso ID", "Kode Kartu", "Jenis Kartu", "Tgl Data", "Dibuat Oleh",
						"Total Data", "Total Paket" });
				no = 2;
				for (Tperso data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getPersoid(), data.getMproduct().getProductcode(),
									data.getMproduct().getProductname(), dateLocalFormatter.format(data.getOrderdate()),
									data.getPersostartby(), data != null ? data.getTotaldata() : 0,
									data != null ? data.getTotalpaket() : 0 });
					no++;
				}
				datamap.put(no, new Object[] { "", "TOTAL", "", "", "", "", totaldata, totalpaket });
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 6) {
						XSSFCellStyle styleHeader = workbook.createCellStyle();
						styleHeader.setBorderTop(BorderStyle.MEDIUM);
						styleHeader.setBorderBottom(BorderStyle.MEDIUM);
						styleHeader.setBorderLeft(BorderStyle.MEDIUM);
						styleHeader.setBorderRight(BorderStyle.MEDIUM);
						styleHeader.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
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
				String filename = "";
				if (time.equals("D"))
					filename = "CIMS_PRODUKSI_DAILY_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				else
					filename = "CIMS_PRODUKSI_MONTHLY_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
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

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
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

	public Date getPersostarttime() {
		return persostarttime;
	}

	public void setPersostarttime(Date persostarttime) {
		this.persostarttime = persostarttime;
	}

	public String getPersoid() {
		return persoid;
	}

	public void setPersoid(String persoid) {
		this.persoid = persoid;
	}

	public int getTotalpaket() {
		return totalpaket;
	}

	public void setTotalpaket(int totalpaket) {
		this.totalpaket = totalpaket;
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
