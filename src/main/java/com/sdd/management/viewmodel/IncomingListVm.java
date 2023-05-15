package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.dao.TincomingvendorDAO;
import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.dao.TplanproductDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.domain.Tincomingvendor;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.domain.Tplanproduct;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TincomingListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class IncomingListVm {

	Session zkSession = Sessions.getCurrent();

	private org.hibernate.Session session;
	private Transaction transaction;

	private TincomingListModel model;
	private TincomingDAO oDao = new TincomingDAO();
	private TincomingvendorDAO venDao = new TincomingvendorDAO();

	private Muser oUser;

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
	private String planfk;
	private String list;
	private String memono;
	private Integer totaldata;
	private BigDecimal totalamount;
	private BigDecimal totalamountrealisasi;
	private boolean isReport = false;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Groupbox gbSearch;
	@Wire
	private Caption captIncoming;
	@Wire
	private Column colAction, colPrefix, colNum, colEndno;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("planfk") String planfkp, @ExecutionArgParam("list") String listp,
			@ExecutionArgParam("memono") String memono, @ExecutionArgParam("isReport") String isReport) throws ParseException {
		Selectors.wireComponents(view, this, false);
		productgroup = arg;
		this.planfk = planfkp;
		this.list = listp;
		this.memono = memono;
		oUser = (Muser) zkSession.getAttribute("oUser");

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
			colAction.setVisible(true);

		if (this.list == null)
			this.list = "0";

		if (planfk != null || memono != null) {
			gbSearch.setVisible(false);
			captIncoming.setVisible(true);
		} else {
			captIncoming.setVisible(false);
		}
		
		if (isReport != null && isReport.equals("Y") ) {
			this.isReport = true;
		}

		if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			colPrefix.setVisible(true);
			colNum.setVisible(true);
			colEndno.setVisible(true);
		} else {
			colPrefix.setVisible(false);
			colNum.setVisible(false);
			colEndno.setVisible(false);
		}

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tincoming>() {
				@Override
				public void render(Row row, final Tincoming data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					if (data.getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
						A a = new A(data.getIncomingid());
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("obj", data);
								map.put("arg", arg);
								Window win = (Window) Executions
										.createComponents("/view/inventory/incomingtokendata.zul", null, map);
								win.setWidth("45%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(a);
					} else if (data.getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
						A a = new A(data.getIncomingid());
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("obj", data);
								map.put("arg", arg);
								Window win = (Window) Executions
										.createComponents("/view/inventory/incomingpinpaddata.zul", null, map);
								win.setWidth("45%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(a);
					} else if (data.getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
						A a = new A(data.getIncomingid());
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("obj", data);
								map.put("arg", arg);
								Window win = (Window) Executions
										.createComponents("/view/inventory/incomingsecuritiesdata.zul", null, map);
								win.setWidth("45%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(a);
					} else
						row.getChildren().add(new Label(data.getIncomingid()));
					row.getChildren().add(new Label(data.getMproducttype().getProducttype()));
					row.getChildren().add(new Label(data.getPrefix() != null ? data.getPrefix() : "-"));
					row.getChildren().add(
							new Label(data.getItemstartno() != null ? String.valueOf(data.getItemstartno()) : "-"));
					row.getChildren()
							.add(new Label(data.getItemstartno() != null
									? String.valueOf(data.getItemstartno() + data.getItemqty() - 1)
									: "-"));
					row.getChildren().add(new Label(
							data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : ""));
					row.getChildren()
							.add(new Label(data.getHarga() != null
									? "Rp." + NumberFormat.getNumberInstance().format(data.getHarga())
									: "Rp.0"));
					row.getChildren()
							.add(new Label(data.getHarga() != null
									? "Rp." + NumberFormat.getNumberInstance()
											.format(data.getHarga().multiply(new BigDecimal(data.getItemqty())))
									: "Rp.0"));
					row.getChildren().add(new Label(
							data.getEntrytime() != null ? datetimeLocalFormatter.format(data.getEntrytime()) : "-"));
					Label lblStatus = new Label(AppData.getStatusLabel(data.getStatus()));
					if (data.getStatus().equals(AppUtils.STATUS_INVENTORY_INCOMINGDECLINE))
						lblStatus.setTooltiptext(data.getDecisionmemo());
					row.getChildren().add(lblStatus);
					row.getChildren().add(new Label(data.getEntryby() != null ? data.getEntryby() : "-"));
					row.getChildren().add(new Label(data.getDecisionby() != null ? data.getDecisionby() : "-"));
					row.getChildren()
							.add(new Label(data.getDecisiontime() != null
									? datetimeLocalFormatter.format(data.getDecisiontime())
									: "-"));
					row.getChildren().add(new Label(data.getDecisionmemo() != null ? data.getDecisionmemo() : "-"));
					row.getChildren().add(new Label(data.getSpkno() != null ? data.getSpkno() : "-"));
					row.getChildren().add(
							new Label(data.getSpkdate() != null ? dateLocalFormatter.format(data.getSpkdate()) : "-"));
					row.getChildren().add(new Label(data.getVendorletterno() != null ? data.getVendorletterno() : "-"));
					row.getChildren()
							.add(new Label(data.getVendorletterdate() != null
									? dateLocalFormatter.format(data.getVendorletterdate())
									: "-"));
					row.getChildren().add(new Label(data.getPksno() != null ? data.getPksno() : "-"));
					row.getChildren().add(
							new Label(data.getPksdate() != null ? dateLocalFormatter.format(data.getPksdate()) : "-"));
					row.getChildren().add(new Label(data.getMemo() != null ? data.getMemo() : "-"));
					row.getChildren().add(new Label(data.getManufacturedate() != null ? String.valueOf(data.getManufacturedate()) : "-"));
					if (data.getTplanfk() != null) {
						Tplan data2 = new TplanDAO().findByFilter("tplanpk = " + data.getTplanfk().getTplanpk());
						row.getChildren().add(new Label(data2 != null ? data2.getMemono() : "-"));
					} else {
						row.getChildren().add(new Label("-"));
					}

					if (data.getTplanfk() != null) {
						Button btnPlanning = new Button();
						btnPlanning.setLabel("Pengadaan");
						btnPlanning.setAutodisable("self");
						btnPlanning.setSclass("btn-light");
						btnPlanning.setStyle(
								"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
						btnPlanning.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<>();
								map.put("planfk", data.getTplanfk().getTplanpk());
								map.put("arg", productgroup);
								map.put("isPlan", 1);
								map.put("keypage", "2");
								Window win = (Window) Executions.createComponents("/view/planning/planninglist.zul",
										null, map);
								win.setWidth("70%");
								win.setClosable(true);
								win.doModal();
							}
						});

						if (list != "0") {
							Tplan dataplan = new TplanDAO().findByFilter("tplanpk = " + data.getTplanfk().getTplanpk());
							if (dataplan != null) {
								Div div = new Div();
								div.appendChild(btnPlanning);
								row.appendChild(div);
							} else {
								Div div = new Div();
								row.appendChild(div);
							}
						} else {
							Div div = new Div();
							row.appendChild(div);
						}
					} else {
						row.getChildren().add(new Label("-"));
					}

					Button btnEdit = new Button();
					btnEdit.setLabel("Edit");
					btnEdit.setAutodisable("self");
					btnEdit.setSclass("btn-light");
					btnEdit.setStyle(
							"border-radius: 8px; background-color: #eeba0b !important; color: #ffffff !important;");
					btnEdit.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);
							map.put("arg", arg);
							map.put("isEdit", "Y");
							Window win = null;
							if (data.getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
								win = (Window) Executions.createComponents("/view/inventory/incomingentrypinpad.zul",
										null, map);
							} else {
								win = (Window) Executions.createComponents("/view/inventory/incomingentry.zul", null,
										map);
							}

							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									doReset();
									BindUtils.postNotifyChange(null, null, IncomingListVm.this, "*");
								}
							});
						}
					});

					Button btnDelete = new Button();
					btnDelete.setLabel("Delete");
					btnDelete.setAutodisable("self");
					btnDelete.setSclass("btn-light");
					btnDelete.setStyle(
							"border-radius: 8px; background-color: #ce0012 !important; color: #ffffff !important;");
					btnDelete.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@SuppressWarnings({ "unchecked", "rawtypes" })
						@Override
						public void onEvent(Event event) throws Exception {
							Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
									Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

										@Override
										public void onEvent(Event event) throws Exception {
											if (event.getName().equals("onOK")) {
												try {
													session = StoreHibernateUtil.openSession();
													transaction = session.beginTransaction();

													if (data.getTplanfk() != null) {
														data.getTplanfk()
																.setTotalprocess(data.getTplanfk().getTotalprocess()
																		- data.getItemqty());
														data.getTplanfk().setTotalqty(
																data.getTplanfk().getTotalqty() + data.getItemqty());

														data.getTplanfk().setStatus(AppUtils.STATUS_PLANNING_APPROVED);
														new TplanDAO().save(session, data.getTplanfk());

														Tplanproduct tpp = new TplanproductDAO().findByFilter(
																"tplanfk = " + data.getTplanfk().getTplanpk()
																		+ " and mproducttypefk = "
																		+ data.getMproducttype().getMproducttypepk());

														if (tpp != null) {
															tpp.setTotalprocess(
																	tpp.getTotalprocess() - data.getItemqty());
															tpp.setUnitqty(tpp.getUnitqty() + data.getItemqty());
															new TplanproductDAO().save(session, tpp);
														}
													}
													oDao.delete(session, data);
													transaction.commit();
													session.close();

													Mmenu mmenu = new MmenuDAO().findByFilter(
															"menupath = '/view/inventory/incoming.zul' and menuparamvalue = 'approval'");
													NotifHandler.delete(mmenu, oUser.getMbranch(), arg,
															oUser.getMbranch().getBranchlevel());

													Clients.showNotification(Labels.getLabel("common.delete.success"),
															"info", null, "middle_center", 3000);

													BindUtils.postNotifyChange(null, null, IncomingListVm.this, "obj");
													BindUtils.postNotifyChange(null, null, IncomingListVm.this,
															"pageTotalSize");
												} catch (Exception e) {
													Messagebox.show("Error : " + e.getMessage(),
															WebApps.getCurrent().getAppName(), Messagebox.OK,
															Messagebox.ERROR);
													e.printStackTrace();
												}

											}
											needsPageUpdate = true;
											doReset();
										}
									});
						}
					});

					Div div = new Div();
					if (data.getStatus().equals(AppUtils.STATUS_INVENTORY_INCOMINGWAITAPPROVAL)
							&& oUser.getUserid().equals(data.getEntryby())) {
						div.appendChild(btnEdit);
						div.appendChild(btnDelete);
					}
					row.appendChild(div);

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
				filter = "extract(year from entrytime) = " + year + " and " + "extract(month from entrytime) = " + month
						+ " and tincoming.productgroup = '" + productgroup + "'";

				if (planfk != null) {
					filter += " and tplanfk = " + planfk;
				}
				
				if(productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
					filter += " and tplanfk is not null";
				
//				if (memono != null) {
//					filter += " and memono = '" + memono + "'";
//				}

				if (isReport) {
					filter = "memono = '" + memono + "'";
				}
				
				if (oUser.getMbranch().getBranchlevel() == 2) {
					filter += " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
				} else if (oUser.getMbranch().getBranchlevel() == 3) {
					filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();
				}
				if (status.length() > 0)
					filter += " and tincoming.status = '" + status + "'";
				if (producttype != null && producttype.trim().length() > 0) {
					if (filter.length() > 0)
						filter += " and ";
					filter += "mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
				}

				List<Tincoming> listData = oDao.listNativeByFilter(filter, "tincomingpk desc");
				totaldata = 0;
				totalamount = new BigDecimal(0);
				totalamountrealisasi = new BigDecimal(0);
				for (Tincoming tincoming : listData) {
					totaldata = totaldata + tincoming.getItemqty();
					totalamount = totalamount.add(tincoming.getHarga());
					totalamountrealisasi = totalamountrealisasi
							.add(tincoming.getHarga().multiply(new BigDecimal(tincoming.getItemqty())));
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
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		status = "";
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tincomingpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TincomingListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				System.out.println(filter);
				List<Tincomingvendor> listData = venDao.listNativeByFilter(filter, orderby);
				if (listData != null && listData.size() > 0) {
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
					BigDecimal totalnilai = new BigDecimal(0);
					String memono = "";
					String memodate = "";
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("DAFTAR PERSEDIAAN " + AppData.getProductgroupLabel(productgroup));
					rownum++;
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Periode");
					cell = row.createCell(1);
					cell.setCellValue(StringUtils.getMonthLabel(month) + " " + year);
					row = sheet.createRow(rownum++);

					if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
						Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
						datamap.put(1, new Object[] { "No", "No Persediaan", "Nama Produk", "Prefix", "No Seri Awal", "No Seri Akhir",
								"Jumlah Unit", "Harga Satuan (Rp)", "Total Nilai (Rp)", "No Memo Permintaan", "Tanggal Memo Permintaan", 
								"Pemutus", "Tanggal Keputusan", "Catatan Keputusan", "No SPK", "Tanggal SPK", "No PKS", "Tanggal PKS", 
								"Vendor Penyedia", "No Surat Vendor", "Tanggal Surat Vendor", "Tahun Produksi", "Tanggal Input Persediaan",
								"Inputer", "Status", "Memo" });
						no = 2;
						BigDecimal totalharga = new BigDecimal(0);
						for (Tincomingvendor data : listData) {
							if (data.getTincoming().getTplanfk() != null) {
								Tplan data2 = new TplanDAO().findByFilter("tplanpk = " + data.getTincoming().getTplanfk().getTplanpk());
								memono = data2.getMemono();
								memodate = dateLocalFormatter.format(data2.getMemodate());
							} 
							totalharga = data.getTincoming().getHarga().multiply(new BigDecimal(data.getTincoming().getItemqty()));
							totalnilai = totalnilai.add(totalharga);
							datamap.put(no, new Object[] { no - 1, data.getTincoming().getIncomingid(), data.getTincoming().getMproducttype().getProducttype(),
									data.getTincoming().getPrefix(), String.valueOf(data.getTincoming().getItemstartno()), 
									String.valueOf(data.getTincoming().getItemstartno() + data.getTincoming().getItemqty() - 1),
									NumberFormat.getInstance().format(data.getTincoming().getItemqty()), NumberFormat.getInstance().format(data.getTincoming().getHarga()),
									NumberFormat.getInstance().format(totalharga), memono, memodate, data.getTincoming().getDecisionby() != null ? data.getTincoming().getDecisionby() : "-",
									data.getTincoming().getDecisiontime() != null ? datetimeLocalFormatter.format(data.getTincoming().getDecisiontime()) : "-",
									data.getTincoming().getDecisionmemo() != null ? data.getTincoming().getDecisionmemo() : "-", 
									data.getTincoming().getSpkno(), dateLocalFormatter.format(data.getTincoming().getSpkdate()), 
									data.getTincoming().getPksno(),	dateLocalFormatter.format(data.getTincoming().getPksdate()),
									data.getSuppliername() != null ? data.getSuppliername() : "-", data.getTincoming().getVendorletterno(),
									dateLocalFormatter.format(data.getTincoming().getVendorletterdate()), data.getTincoming().getManufacturedate(), 
									dateLocalFormatter.format(data.getTincoming().getEntrytime()), data.getTincoming().getEntryby(),
									AppData.getStatusLabel(data.getTincoming().getStatus()), data.getTincoming().getMemo()});
							no++;
							total += data.getTincoming().getItemqty();
						}
						datamap.put(no, new Object[] { "", "TOTAL", "", "", "", "", NumberFormat.getInstance().format(total), "", NumberFormat.getInstance().format(totalnilai)});
						
						
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
					} else {
						Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
						datamap.put(1, new Object[] { "No", "No Persediaan", "Nama Produk", "Jumlah Unit", "Harga Satuan (Rp)", "Total Nilai (Rp)", 
								"No Memo Permintaan", "Tanggal Memo Permintaan", "Pemutus", "Tanggal Keputusan", "Catatan Keputusan", "No SPK", 
								"Tanggal SPK", "No PKS", "Tanggal PKS", "Vendor Penyedia", "No Surat Vendor", "Tanggal Surat Vendor", 
								"Tahun Produksi", "Tanggal Input Persediaan", "Inputer", "Status", "Memo" });
						no = 2;
						BigDecimal totalharga = new BigDecimal(0);
						for (Tincomingvendor data : listData) {
							if (data.getTincoming().getTplanfk() != null) {
								Tplan data2 = new TplanDAO().findByFilter("tplanpk = " + data.getTincoming().getTplanfk().getTplanpk());
								memono = data2.getMemono();
								memodate = dateLocalFormatter.format(data2.getMemodate());
							} 
							totalharga = data.getTincoming().getHarga().multiply(new BigDecimal(data.getTincoming().getItemqty()));
							totalnilai = totalnilai.add(totalharga);
							datamap.put(no, new Object[] { no - 1, data.getTincoming().getIncomingid(), data.getTincoming().getMproducttype().getProducttype(),
									NumberFormat.getInstance().format(data.getTincoming().getItemqty()), NumberFormat.getInstance().format(data.getTincoming().getHarga()),
									NumberFormat.getInstance().format(totalharga), memono, memodate, data.getTincoming().getDecisionby() != null ? data.getTincoming().getDecisionby() : "-",
									data.getTincoming().getDecisiontime() != null ? datetimeLocalFormatter.format(data.getTincoming().getDecisiontime()) : "-",
									data.getTincoming().getDecisionmemo() != null ? data.getTincoming().getDecisionmemo() : "-", 
									data.getTincoming().getSpkno(), dateLocalFormatter.format(data.getTincoming().getSpkdate()), 
									data.getTincoming().getPksno(),	dateLocalFormatter.format(data.getTincoming().getPksdate()),
									data.getSuppliername() != null ? data.getSuppliername() : "-", data.getTincoming().getVendorletterno(),
									dateLocalFormatter.format(data.getTincoming().getVendorletterdate()), data.getTincoming().getManufacturedate(), 
									dateLocalFormatter.format(data.getTincoming().getEntrytime()), data.getTincoming().getEntryby(),
									AppData.getStatusLabel(data.getTincoming().getStatus()), data.getTincoming().getMemo()});
							no++;
							total += data.getTincoming().getItemqty();
						}
						datamap.put(no, new Object[] { "", "TOTAL", "", NumberFormat.getInstance().format(total), "", NumberFormat.getInstance().format(totalnilai)});
						
						
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
					}


					String path = Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
					String filename = "CAPTION_DAFTAR_PERSEDIAAN_"
							+ new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
					FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
					workbook.write(out);
					out.close();
					Filedownload.save(new File(path + "/" + filename),
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				} else {
					Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
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

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public Muser getoUser() {
		return oUser;
	}

	public void setoUser(Muser oUser) {
		this.oUser = oUser;
	}

	public String getMemono() {
		return memono;
	}

	public void setMemono(String memono) {
		this.memono = memono;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public BigDecimal getTotalamount() {
		return totalamount;
	}

	public void setTotalamount(BigDecimal totalamount) {
		this.totalamount = totalamount;
	}

	public BigDecimal getTotalamountrealisasi() {
		return totalamountrealisasi;
	}

	public void setTotalamountrealisasi(BigDecimal totalamountrealisasi) {
		this.totalamountrealisasi = totalamountrealisasi;
	}
}