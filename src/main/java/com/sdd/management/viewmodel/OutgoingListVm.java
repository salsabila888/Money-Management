package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.model.ToutgoingListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;

public class OutgoingListVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private ToutgoingListModel model;

	private List<Toutgoing> objList = new ArrayList<>();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer year;
	private Integer month;
	private String status;
	private String productgroup;
	private String producttype;
	private String productname;
	private String productcode;
	private int branchlevel;

	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Column colBranch, colOutlet;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroup = arg;
		branchlevel = oUser.getMbranch().getBranchlevel();

		if (productgroup.equals("TC") || productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)
				|| productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			colBranch.setVisible(true);
			colOutlet.setVisible(true);
		} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
			colBranch.setVisible(false);
			colOutlet.setVisible(false);
		} else {
			colBranch.setVisible(false);
			colOutlet.setVisible(false);
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
			grid.setRowRenderer(new RowRenderer<Toutgoing>() {
				@Override
				public void render(Row row, final Toutgoing data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					A a = new A(data.getOutgoingid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("obj", data);
							map.put("arg", arg);
							win = (Window) Executions.createComponents("/view/inventory/outgoingdata.zul", null, map);
							win.setWidth("50%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(data.getMproduct().getMproducttype().getProducttype()));
					row.getChildren().add(new Label(data.getMproduct().getProductcode()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
					Label lblStatus = new Label(AppData.getStatusLabel(data.getStatus()));
					if (data.getStatus().equals(AppUtils.STATUS_INVENTORY_OUTGOINGDECLINE))
						lblStatus.setTooltiptext(data.getDecisionmemo());
					row.getChildren().add(lblStatus);
					row.getChildren().add(
							new Label(data.getTorder() != null && data.getTorder().getMbranch() != null? data.getTorder().getMbranch().getBranchname() : ""));
					row.getChildren().add(new Label(data.getTorder() != null && data.getTorder().getOrderoutlet() != null ? data.getTorder().getOrderoutlet() : ""));

					
					if (data.getTorder() != null) {
						System.out.println(data.getTorder());
						Button btnOrder = new Button("Lihat Data Pemesanan");
						btnOrder.setAutodisable("self");
						btnOrder.setClass("btn-default");
						btnOrder.setStyle(
								"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
						btnOrder.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("obj", data.getTorder());
								map.put("arg", arg);
								Window win = (Window) Executions.createComponents("/view/order/orderdetail.zul", null, map);
								win.setWidth("60%");
								win.setClosable(true);
								win.doModal();
							}
						});

						Div div = new Div();
						div.appendChild(btnOrder);
						row.appendChild(div);
					} else {
						row.getChildren().add(new Label("-"));
					}

					row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getEntrytime())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getItemqty())));
					row.getChildren().add(new Label(data.getEntryby()));
					row.getChildren().add(new Label(data.getDecisionby()));

					Button btnScan = new Button("Barang Keluar");
					btnScan.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnScan.setAutodisable("self");
					btnScan.setClass("btn btn-success btn-sm");
					btnScan.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);
							Window win = new Window();
							if (data.getProductgroup().trim().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
								map.put("PINPAD", "Y");
								win = (Window) Executions.createComponents("/view/inventory/outgoingscanpinpad.zul",
										null, map);
							} else if (data.getProductgroup().trim().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
								win = (Window) Executions.createComponents("/view/inventory/outgoingscantoken.zul",
										null, map);
							} else {
								map.put("arg", productgroup);
								if (branchlevel > 1) {
									win = (Window) Executions
											.createComponents("/view/inventory/outgoingscandocument.zul", null, map);
								} else {
									win = (Window) Executions
											.createComponents("/view/inventory/outgoingscandocumentold.zul", null, map);
								}
							}
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
								}
							});

							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
						}
					});
					if ((data.getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN)
							|| data.getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)
							|| data.getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT))
							&& data.getTorder().getStatus().equals(AppUtils.STATUS_INVENTORY_OUTGOINGWAITSCAN)) {
						row.getChildren().add(btnScan);
					} else
						row.getChildren().add(new Label());
				}
			});
		}

		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item = new Comboitem();
			item.setLabel(months[i]);
			item.setValue(i + 1);
			cbMonth.appendChild(item);
		}
		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		try {
			if (year != null && month != null) {
				filter = "extract(year from toutgoing.entrytime) = " + year + " and "
						+ "extract(month from toutgoing.entrytime) = " + month + " and toutgoing.productgroup = '"
						+ productgroup + "'";

				if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)
						|| productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
					filter += " and mbranchpk = " + oUser.getMbranch().getMbranchpk();
				} else {
					if (oUser.getMbranch().getBranchlevel() == 1) {
						if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
							filter += " and orderoutlet = '00' and orderlevel = "
									+ (oUser.getMbranch().getBranchlevel() + 1);
						}
					} else if (oUser.getMbranch().getBranchlevel() == 2) {
						filter += " and orderoutlet = '00' and orderlevel = "
								+ (oUser.getMbranch().getBranchlevel() + 1) + " and mregionfk = "
								+ oUser.getMbranch().getMregion().getMregionpk();
					} else if (oUser.getMbranch().getBranchlevel() == 3) {
						filter += " and orderoutlet != '00' and mbranchpk = " + oUser.getMbranch().getMbranchpk();
					}
				}
				if (status.length() > 0)
					filter += " and Toutgoing.status = '" + status + "'";
				if (producttype != null && producttype.trim().length() > 0) {
					if (filter.length() > 0)
						filter += " and ";
					filter += "mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
				}
				if (productname != null && productname.trim().length() > 0) {
					if (filter.length() > 0)
						filter += " and ";
					filter += "mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
				}
				if (productcode != null && productcode.trim().length() > 0) {
					if (filter.length() > 0)
						filter += " and ";
					filter += "mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
				}

				objList = new ToutgoingDAO().listFilter(filter, "toutgoingpk desc");

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
				Integer total = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("DAFTAR PEMENUHAN PERSEDIAAN " + AppData.getProductgroupLabel(productgroup));
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Periode");
				cell = row.createCell(1);
				cell.setCellValue(StringUtils.getMonthLabel(month) + " " + year);
				row = sheet.createRow(rownum++);

				/*
				 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
				 */
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "No Pemenuhan", "Kode Produk", "Jenis Produk", "Status", "Wilayah/Cabang",
						"Order Outlet", "Tanggal Pemesanan", "Jumlah",  "Requestor", "Pemutus" });
				no = 2;
				for (Toutgoing data : objList) {
					datamap.put(no, new Object[] { no - 1, data.getOutgoingid(), data.getMproduct().getProductcode(),
							data.getMproduct().getMproducttype().getProducttype(), AppData.getStatusLabel(data.getStatus()),
							data.getTorder().getMbranch().getBranchname(), data.getTorder().getOrderoutlet(), 
							datetimeLocalFormatter.format(data.getEntrytime()), 
							NumberFormat.getInstance().format(data.getItemqty()), data.getEntryby(), data.getDecisionby() });
					no++;
					total += data.getItemqty();
				}
				datamap.put(no, new Object[] { "TOTAL", "", "", "", "", "", "", "", total });
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
				String filename = "CAPTION_DAFTAR_OUTGOING_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
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

	@Command
	@NotifyChange("*")
	public void doReset() {
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		status = "";
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "toutgoingpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new ToutgoingListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}
}