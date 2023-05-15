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
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vreportorder;
import com.sdd.caption.model.VreportorderListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportOrderVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private VreportorderListModel model;
	private TembossbranchDAO oDao = new TembossbranchDAO();

	private boolean needsPageUpdate;
	private Date orderdate;
	private Date appdate;
	private Date appdateto;
	private int totaldata;
	private int pageTotalSize;
	private int pageStartNumber;
	private String filter;
	private String orderby;
	private String productcode;
	private String productname;
	private Date tgldata;
	private String branchname;
	private String time;

	private Morg morg;

	private List<Vreportorder> objList = new ArrayList<>();
	private Map<String, String> mapOrg;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

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
		oUser = (Muser) zkSession.getAttribute("oUser");

		time = arg;
		try {
			mapOrg = AppData.getOrgmap();
			doReset();
		} catch (Exception e) {
			e.printStackTrace();
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Vreportorder>() {

			@Override
			public void render(Row row, final Vreportorder data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				A a = new A(mapOrg.get(data.getOrg()));
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("Report", data);
						map.put("date", time);
						if (appdate != null)
							map.put("appdate", appdate);
						if (appdateto != null)
							map.put("appdateto", appdateto);
						map.put("isReport", "Y");
						Window win = (Window) Executions.createComponents("/view/emboss/embossdata.zul", null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.doModal();
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getProductname()));
				row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(data.getRegioncode()));
				row.getChildren().add(new Label(data.getRegionname()));
				row.getChildren().add(new Label(data.getBranchid()));
				row.getChildren().add(new Label(data.getBranchname()));
				row.getChildren()
						.add(new Label(data != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus().trim())));
			}
		});

	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (oUser != null) {
				if (time.equals("D")) {
					if (orderdate != null) {
						filter = "tembossbranch.orderdate = '" + dateFormatter.format(orderdate) + "'";

						if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 700)
							filter += " and 0=0";
						else if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) < 700
								&& Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600)
							filter += " and MREGIONPK = " + oUser.getMbranch().getMregion().getMregionpk();
						else
							filter += " and MBRANCHPK = " + oUser.getMbranch().getMbranchpk();

						if (morg != null) {
							filter += " and tembossproduct.org = '" + morg.getOrg() + "'";
						}
						if (productcode != null && productcode.length() > 0) {
							filter += " and tembossproduct.productcode like '%" + productcode.toUpperCase() + "%'";
						}
						if (productname != null && productname.length() > 0) {
							filter += " and mproduct.productname like '%" + productname.toUpperCase() + "%'";
						}

						if (tgldata != null) {
							filter += " and tembossbranch.orderdate = '" + tgldata + "'";
						}

						needsPageUpdate = true;
						paging.setActivePage(0);
						pageStartNumber = 0;
						refreshModel(pageStartNumber);

						totaldata = 0;
						objList = oDao.listTotalReport(filter);
						for (Vreportorder data : objList) {
							totaldata = totaldata + data.getTotaldata();
						}
					} else {
						Messagebox.show("Silahkan masukan tanggal data terlebih dahulu.", "Warning", Messagebox.OK,
								Messagebox.EXCLAMATION);
					}
				} else {
					if (appdate != null) {
						if (appdateto != null)
							filter = "tembossbranch.orderdate between '" + dateFormatter.format(appdate) + "' and '"
									+ dateFormatter.format(appdateto) + "'";
						else
							filter = "tembossbranch.orderdate = '" + dateFormatter.format(appdate) + "'";

						if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 700)
							filter += " and 0=0";
						else if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) < 700
								&& Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600)
							filter += " and MREGIONPK = " + oUser.getMbranch().getMregion().getMregionpk();
						else
							filter += " and MBRANCHPK = " + oUser.getMbranch().getMbranchpk();

						if (morg != null) {
							filter += " and tembossproduct.org = '" + morg.getOrg() + "'";
						}
						if (productcode != null && productcode.length() > 0) {
							filter += " and tembossproduct.productcode like '%" + productcode.toUpperCase() + "%'";
						}
						if (productname != null && productname.length() > 0) {
							filter += " and mproduct.productname like '%" + productname.toUpperCase() + "%'";
						}
						if (branchname != null && branchname.length() > 0) {
							filter += " and mbranch.branchname like '%" + branchname.toUpperCase() + "%'";
						}
						if (tgldata != null) {
							filter += " and tembossbranch.orderdate = '" + tgldata + "'";
						}

						needsPageUpdate = true;
						paging.setActivePage(0);
						pageStartNumber = 0;
						refreshModel(pageStartNumber);

						totaldata = 0;
						objList = oDao.listTotalReport(filter);
						for (Vreportorder data : objList) {
							totaldata = totaldata + data.getTotaldata();
						}
					} else {
						Messagebox.show("Silahkan masukan tanggal awal terlebih dahulu.", "Warning", Messagebox.OK,
								Messagebox.EXCLAMATION);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange({ "pageTotalSize", "total" })
	public void refreshModel(int activePage) {
		try {
			orderby = "tembossbranch.branchid, tembossbranch.orderdate";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new VreportorderListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		morg = null;
		productcode = null;
		productname = null;
		branchname = null;
		tgldata = null;
		totaldata = 0;
		objList = new ArrayList<>();
		/*
		 * appdate = null; appdateto = null; orderdate = null;
		 */

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
				if (time.equals("D"))
					cell.setCellValue("Tanggal " + new SimpleDateFormat("dd MMM yyyy").format(orderdate));
				else
					cell.setCellValue("Periode " + new SimpleDateFormat("dd MMM yyyy").format(appdate) + " s/d "
							+ appdateto != null ? new SimpleDateFormat("dd MMM yyyy").format(appdateto) : "-");
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Org");
				cell = row.createCell(1);
				cell.setCellValue(morg != null ? mapOrg.get(morg.getOrg()) : "ALL");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "Org", "Kode Kartu", "Jenis Kartu", "Tgl Data", "Kode Wilayah",
						"Nama Wilayah", "Kode Cabang", "Nama Cabang", "Total Data", "status" });
				no = 2;
				for (Vreportorder data : objList) {
					datamap.put(no,
							new Object[] { no - 1, mapOrg.get(data.getOrg()), data.getProductcode(),
									data.getProductname(), datelocalFormatter.format(data.getOrderdate()),
									data.getRegioncode(), data.getRegionname(), data.getBranchid(),
									data.getBranchname(), data != null ? data.getTotaldata() : 0, AppData.getStatusLabel(data.getStatus().trim()) });
					no++;
				}
				datamap.put(no, new Object[] { "", "TOTAL", "", "", "", "", "", "", "", totaldata, "" });
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

	public ListModelList<Morg> getMorgmodel() {
		ListModelList<Morg> lm = null;
		try {
			lm = new ListModelList<Morg>(AppData.getMorg());
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

	public Date getTgldata() {
		return tgldata;
	}

	public void setTgldata(Date tgldata) {
		this.tgldata = tgldata;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
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

}
