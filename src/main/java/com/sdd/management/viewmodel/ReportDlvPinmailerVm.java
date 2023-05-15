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
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mregion;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vreportdlv;
import com.sdd.caption.domain.Vreportdlvpinmailer;
import com.sdd.caption.model.VreportdlvpinmailerListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportDlvPinmailerVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private VreportdlvpinmailerListModel model;
	private TdeliveryDAO oDao = new TdeliveryDAO();

	private boolean needsPageUpdate;
	private Date orderdate;
	private Date processtime;
	private int totaldata;
	private int totalkartu;
	private int pageTotalSize;
	private int pageStartNumber;
	private Date appdate;
	private Date appdateto;
	private String branchid;
	private String branchname;
	private String nopaket;
	private String dlvid;
	private String filter;
	private String orderby;
	private String productcode;
	private String productname;
	private String time;
	private Mbranch mbranch;
	private Mregion mregion;

	private ListModelList<Mbranch> mbranchmodel;
	private List<Vreportdlvpinmailer> objList = new ArrayList<>();
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Row monthly, daily, rowRegion, rowBranch;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		
//		if (oUser.getMbranch().getBranchid().trim().equals("310")) {
//			rowBranch.setVisible(true);
//			rowRegion.setVisible(true);
//		} else {
//			rowBranch.setVisible(false);
//			rowRegion.setVisible(false);
//		}
		
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

		grid.setRowRenderer(new RowRenderer<Vreportdlvpinmailer>() {

			@Override
			public void render(Row row, final Vreportdlvpinmailer data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				row.getChildren().add(new Label(data.getDlvid()));
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
				row.getChildren().add(new Label(data.getBranchname()));
				row.getChildren().add(new Label(data.getNopaket()));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getProductname()));
				row.getChildren().add(new Label(data.getVendorcode()));
				row.getChildren().add(
						new Label(data.getOrderdate() != null ? dateLocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(
						data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "0"));
				row.getChildren().add(new Label(
						data.getProcesstime() != null ? dateLocalFormatter.format(data.getProcesstime()) : ""));
				row.getChildren().add(new Label(data.getPenerima() != null ? data.getPenerima() : ""));
				row.getChildren().add(
						new Label(data.getTglterima() != null ? dateLocalFormatter.format(data.getTglterima()) : ""));
				/*
				 * row.getChildren().add(new Label( data.getTotaldata() != null ?
				 * NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				 */
				
			}
		});

	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (time.equals("D")) {
				if (processtime != null) {
					filter = "DATE(tdelivery.processtime) = '" + dateFormatter.format(processtime) + "' and tdelivery.productgroup = '06'";
					
					if (dlvid != null && dlvid.length() > 0)
						filter += " and dlvid like '%" + dlvid + "%'";
					if (nopaket != null && nopaket.length() > 0)
						filter += " and nopaket like '%" + nopaket + "%'";
					if (productcode != null && productcode.length() > 0)
						filter += " and productcode like '%" + productcode + "%'";
					if (productname != null && productname.length() > 0)
						filter += " and productname like '%" + productname + "%'";
					if (mbranch != null)
						filter += " and branchname = '" + mbranch.getBranchname() + "'";
					if (mregion != null)
						filter += " and regionname = '" + mregion.getRegionname() + "'";

					needsPageUpdate = true;
					paging.setActivePage(0);
					pageStartNumber = 0;
					refreshModel(pageStartNumber);

					totaldata = 0;
					objList = oDao.ReportdlvPmByFilter(filter);
					for (Vreportdlvpinmailer data : objList) {
						totaldata = totaldata + data.getQuantity();
					}
				} else {
					Messagebox.show("Silahkan masukan tanggal data terlebih dahulu.", "Warning", Messagebox.OK,
							Messagebox.EXCLAMATION);
				}
			} else {
				if (appdate != null) {
					if (appdateto != null)
						filter = "tdelivery.processtime between '" + dateFormatter.format(appdate) + "' and '"
								+ dateFormatter.format(appdateto) + "'  and tdelivery.productgroup = '06'";
					else
						filter = "tdelivery.processtime = '" + dateFormatter.format(appdate) + "'  and tdelivery.productgroup = '06'";

					if (dlvid != null && dlvid.length() > 0)
						filter += " and dlvid like '%" + dlvid + "%'";
					if (nopaket != null && nopaket.length() > 0)
						filter += " and nopaket like '%" + nopaket + "%'";
					if (productcode != null && productcode.length() > 0)
						filter += " and productcode like '%" + productcode + "%'";
					if (productname != null && productname.length() > 0)
						filter += " and productname like '%" + productname + "%'";
					if (mbranch != null)
						filter += " and branchname = '" + mbranch.getBranchname() + "'";
					if (mregion != null)
						filter += " and regionname = '" + mregion.getRegionname() + "'";

					needsPageUpdate = true;
					paging.setActivePage(0);
					pageStartNumber = 0;
					refreshModel(pageStartNumber);

					totaldata = 0;
					totalkartu = 0;
					objList = oDao.ReportdlvPmByFilter(filter);
					for (Vreportdlvpinmailer data : objList) {
						totaldata = totaldata + data.getQuantity();
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
			orderby = "tdeliverydatapk";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new VreportdlvpinmailerListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		mbranch = null;
		productcode = null;
		productname = null;
		branchid = null;
		branchname = null;
		dlvid = null;
		nopaket = null;
		totaldata = 0;
		objList = new ArrayList<>();
		processtime = null;
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
					cell.setCellValue("Laporan Pengiriman Pin Mailer Harian");
				else
					cell.setCellValue("Laporan Pengiriman Pin Mailer Periode Bulanan");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Laporan Produktivitas Produksi");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1,
						new Object[] { "No", "No Manifest", "Group Produk", "Nama Cabang", "No Paket", "Kode Produk",
								"Jenis Produk", "Kode Vendor", "Tgl Data",
								"Total Kartu", "Tgl Proses", "Penerima", "Tgl Terima" });
				no = 2;
				for (Vreportdlvpinmailer data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getDlvid(),
									AppData.getProductgroupLabel(data.getProductgroup()), data.getBranchname(),
									data.getNopaket(), data.getProductcode(), data.getProductname(),
									data.getVendorcode(), dateLocalFormatter.format(data.getOrderdate()),
									data.getQuantity() != null ? data.getQuantity() : 0,
									dateLocalFormatter.format(data.getProcesstime()),
									data.getPenerima() != null ? data.getPenerima() : "",
									data.getTglterima() != null ? dateLocalFormatter.format(data.getTglterima()) : "" });
					no++;
				}
				datamap.put(no,
						new Object[] { "", "TOTAL", "", "", "", "", "", "", "", totaldata});
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
				String filename = "";
				if (time.equals("D"))
					filename = "CIMS_DLV_DAILY_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				else
					filename = "CIMS_DLV_MONTHLY_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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
	@NotifyChange("mbranchmodel")
	public void doBranchLoad(@BindingParam("item") Mregion item) {
		if (item != null) {
			try {
				mbranchmodel = new ListModelList<>(AppData.getMbranch("mregion.mregionpk = " + item.getMregionpk()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public ListModelList<Mregion> getMregionmodel() {
		ListModelList<Mregion> lm = null;
		try {
			lm = new ListModelList<Mregion>(AppData.getMregion());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
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

	public Date getProcesstime() {
		return processtime;
	}

	public void setProcesstime(Date processtime) {
		this.processtime = processtime;
	}

	public String getBranchid() {
		return branchid;
	}

	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
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

	public String getNopaket() {
		return nopaket;
	}

	public void setNopaket(String nopaket) {
		this.nopaket = nopaket;
	}

	public String getDlvid() {
		return dlvid;
	}

	public void setDlvid(String dlvid) {
		this.dlvid = dlvid;
	}

	public int getTotalkartu() {
		return totalkartu;
	}

	public void setTotalkartu(int totalkartu) {
		this.totalkartu = totalkartu;
	}

	public Mregion getMregion() {
		return mregion;
	}

	public void setMregion(Mregion mregion) {
		this.mregion = mregion;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public ListModelList<Mbranch> getMbranchmodel() {
		return mbranchmodel;
	}

	public void setMbranchmodel(ListModelList<Mbranch> mbranchmodel) {
		this.mbranchmodel = mbranchmodel;
	}

}
