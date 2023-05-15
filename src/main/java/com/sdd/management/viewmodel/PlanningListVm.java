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
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zel.impl.parser.ParseException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
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
import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.dao.TrepairitemDAO;
import com.sdd.caption.dao.TreturnitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.domain.Trepairitem;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TplanListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PlanningListVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private Muser oUser;
	private TplanListModel model;
	private TplanDAO planDao = new TplanDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby, filter, planfk;
	private Integer year, month;

	private String status, productgroup, producttype, arg, keypage, selincused;
	private Boolean isIncoming, isPlan;
	private Boolean isPFA = false;
	private Boolean isDecline = false;
	private String planno;

	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Window winPlan;
	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Caption captIncoming;
	@Wire
	private Groupbox gbSearch;
	@Wire
	private Row rowStatus, rowStatusPFA, rowIncomingused;
	@Wire
	private Column colTotal, colJumlah;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("isIncoming") Boolean isIncomingp, @ExecutionArgParam("isPlan") Boolean isPlanp,
			@ExecutionArgParam("planfk") String planfkp, @ExecutionArgParam("isPFA") String isPFA,
			@ExecutionArgParam("isDecline") String isDecline, @ExecutionArgParam("keypage") String keypagep)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) Sessions.getCurrent().getAttribute("oUser");

		this.arg = arg;
		this.isIncoming = isIncomingp;
		this.isPlan = isPlanp;
		this.planfk = planfkp;
		this.keypage = keypagep;

		if (this.keypage == null)
			this.keypage = "0";

		if (isIncomingp == null)
			this.isIncoming = false;
		else
			this.isIncoming = true;

		if (this.isPlan == null)
			this.isPlan = false;

		if (isDecline != null && isDecline.equals("Y")) {
			this.isDecline = true;
		}

		if (keypage.equals("0")) {
			gbSearch.setVisible(true);
			captIncoming.setVisible(false);
		} else if (keypage.equals("1")) {
			captIncoming.setVisible(true);
		} else if (keypage.equals("2")) {
			captIncoming.setVisible(true);
			gbSearch.setVisible(false);
		}

		if (isPFA != null && isPFA.equals("Y")) {
			this.isPFA = true;
			rowStatusPFA.setVisible(true);
		} else {
			rowStatus.setVisible(true);
		}

		System.out.println(arg);
		System.out.println(AppUtils.PRODUCTGROUP_DOCUMENT);
		if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			colJumlah.setLabel("Total Jumlah(Buku/Set/Lembar)");
			colTotal.setLabel("Total Proses(Buku/Set/Lembar)");
		} else {
			colJumlah.setLabel("Total Unit");
			colTotal.setLabel("Total Proses");
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tplan>() {

			@Override
			public void render(Row row, Tplan data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				row.getChildren().add(new Label(data.getPlanno() != null ? data.getPlanno() : "-"));
				row.getChildren()
						.add(new Label(data.getInputtime() != null
								? new SimpleDateFormat("dd-MM-YYYY").format(data.getInputtime())
								: "-"));
				row.getChildren().add(new Label(
						data.getProductgroup() != null ? AppData.getProductgroupLabel(data.getProductgroup()) : "-"));
				row.getChildren().add(new Label(data.getMbranch() != null ? data.getMbranch().getBranchname() : "-"));
				row.getChildren()
						.add(new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
				row.getChildren()
						.add(new Label(data.getAnggaran() != null
								? "Rp " + NumberFormat.getInstance().format(data.getAnggaran())
								: "-"));
				row.getChildren().add(new Label(
						data.getTotalqty() != null ? NumberFormat.getInstance().format(data.getTotalqty()) : "-"));
				row.getChildren()
						.add(new Label(data.getTotalprocess() != null
								? NumberFormat.getInstance().format(data.getTotalprocess())
								: "-"));
				row.getChildren().add(new Label(data.getMemono() != null ? data.getMemono() : "-"));
				row.getChildren()
						.add(new Label(data.getMemodate() != null
								? new SimpleDateFormat("dd-MM-YYYY").format(data.getMemodate())
								: "-"));
				row.getChildren().add(new Label(data.getDecisionby() != null ? data.getDecisionby() : "-"));
				row.getChildren()
						.add(new Label(data.getDecisiontime() != null
								? new SimpleDateFormat("dd-MM-YYYY").format(data.getDecisiontime())
								: "-"));

				Button btnDetail = new Button();
				btnDetail.setLabel("Detail");
				btnDetail.setAutodisable("self");
				btnDetail.setSclass("btn-light");
				btnDetail.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btnDetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);
						map.put("isDetail", "Y");
						map.put("arg", arg);
						Window win = (Window) Executions.createComponents("/view/planning/planningdata.zul", null, map);
						win.setWidth("70%");
						win.setClosable(true);
						win.doModal();
					}
				});

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
						Window win = new Window();
						if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
							win = (Window) Executions.createComponents("/view/planning/planningentrypinpad.zul", null,
									map);
						} else {
							win = (Window) Executions.createComponents("/view/planning/planningentry.zul", null, map);
						}
						win.setWidth("70%");
						win.setClosable(true);
						win.doModal();
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								doReset();
								BindUtils.postNotifyChange(null, null, PlanningListVm.this, "*");
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

												planDao.delete(session, data);
												transaction.commit();
												session.close();

												Mmenu mmenu = new MmenuDAO().findByFilter(
														"menupath = '/view/planning/planning.zul' and menuparamvalue = 'approval'");
												NotifHandler.delete(mmenu, oUser.getMbranch(), arg,
														oUser.getMbranch().getBranchlevel());

												Clients.showNotification(Labels.getLabel("common.delete.success"),
														"info", null, "middle_center", 3000);

												BindUtils.postNotifyChange(null, null, PlanningListVm.this,
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

				Button btnPlan = new Button();
				btnPlan.setLabel("Pilih");
				btnPlan.setAutodisable("self");
				btnPlan.setSclass("btn-light");
				btnPlan.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btnPlan.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Event closeEvent = new Event("onClose", winPlan, data);
						Events.postEvent(closeEvent);
					}
				});

				Button btnIncoming = new Button();
				btnIncoming.setLabel("Data Persediaan");
				btnIncoming.setAutodisable("self");
				btnIncoming.setSclass("btn-light");
				btnIncoming.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btnIncoming.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("planfk", data.getTplanpk());
						map.put("arg", arg);
						Window win = (Window) Executions.createComponents("/view/inventory/incominglist.zul", null,
								map);
						win.setWidth("70%");
						win.setClosable(true);
						win.doModal();
					}
				});

				Button btnOK = new Button();
				btnOK.setLabel("OK");
				btnOK.setAutodisable("self");
				btnOK.setSclass("btn-light");
				btnOK.setStyle("border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btnOK.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						session = StoreHibernateUtil.openSession();
						transaction = session.beginTransaction();

						data.setIsdecline("Y");
						planDao.save(session, data);

						transaction.commit();
						session.close();

						Mmenu mmenu = new MmenuDAO().findByFilter(
								"menupath = '/view/planning/planning.zul' and menuparamvalue = 'listfail'");
						NotifHandler.delete(mmenu, data.getMbranch(), arg,
								data.getMbranch().getBranchlevel());
						
						refreshModel(pageStartNumber);
					}
				});

				if (keypage.equals("1")) {
					if (data.getTotalqty() != 0) {
						Div div = new Div();
						div.appendChild(btnPlan);
						row.appendChild(div);
					} else {
						Div div = new Div();
						Label planused = new Label("Pengadaan telah dilakukan");
						planused.setStyle("font-weight:bold;");
						div.appendChild(planused);
						row.appendChild(div);
					}
				} else if (keypage.equals("2")) {
					Div div = new Div();
					div.appendChild(btnDetail);
					row.appendChild(div);
				} else {
					Div div = new Div();
					if (!arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
						div.appendChild(btnDetail);

						if ((data.getStatus().equals(AppUtils.STATUS_PLANNING_WAITAPPROVAL)
								|| (data.getStatus().equals(AppUtils.STATUS_PLANNING_WAITAPPROVALOPR)))
								&& oUser.getUserid().equals(data.getInputer())) {
							div.appendChild(btnEdit);
							div.appendChild(btnDelete);
						}
					} else {
						if (data.getStatus().equals(AppUtils.STATUS_PLANNING_WAITAPPROVAL)
								&& oUser.getUserid().equals(data.getInputer())) {
							div.appendChild(btnEdit);
							div.appendChild(btnDelete);
						}
					}
					if (data.getIncomingused() != null)
						div.appendChild(btnIncoming);

					if (isDecline != null && data.getIsdecline().equals("N")) {
						div.appendChild(btnOK);
					}

					row.appendChild(div);
				}
			}
		});

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
		if (planfk != null) {
			filter = " tplanpk = " + planfk;

			if (planno != null && planno.length() > 0)
				filter += " and planno like '%" + planno.trim().toUpperCase() + "%'";

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} else {
			if (year != null && month != null) {
				filter = "extract(year from inputtime) = " + year + " and " + "extract(month from inputtime) = " + month
						+ " and productgroup = '" + arg + "' ";

				if (status.length() > 0)
					filter += " and status = '" + status + "'";

				if (producttype != null && producttype.trim().length() > 0) {
					if (filter.length() > 0)
						filter += " and mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
				}

				if (selincused.equals("1") || "1".equals(selincused))
					filter += " and incomingused = 1";
				else if (selincused.equals("2") || "2".equals(selincused))
					filter += " and incomingused is null";

				if (planno != null && planno.length() > 0)
					filter += " and planno like '%" + planno.trim().toUpperCase() + "%'";

				if (isPFA) {
					filter += " and status in ('" + AppUtils.STATUS_PLANNING_WAITAPPROVALPFA + "', '"
							+ AppUtils.STATUS_PLANNING_DECLINEBYPFA + "', '" + AppUtils.STATUS_PLANNING_APPROVED
							+ "', '" + AppUtils.STATUS_PLANNING_DONE + "')";
				} else if (!isPFA && !isIncoming) {
					if (oUser.getMbranch().getBranchid().equals("760") && arg.equals(AppUtils.PRODUCTGROUP_PINPAD))
						filter += " and status in ('" + AppUtils.STATUS_PLANNING_WAITAPPROVALOPR + "', '"
								+ AppUtils.STATUS_PLANNING_DECLINEBYOPR + "', '"
								+ AppUtils.STATUS_PLANNING_WAITAPPROVALPFA + "', '"
								+ AppUtils.STATUS_PLANNING_DECLINEBYPFA + "', '" + AppUtils.STATUS_PLANNING_APPROVED
								+ "')";
					else
						filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();
				}

				if (isIncoming)
					filter += " and status = '" + AppUtils.STATUS_PLANNING_APPROVED + "'";

				if (isDecline)
					filter += " and mbranchfk = '" + oUser.getMbranch().getMbranchpk() + "' and isdecline = 'N'";

				if (isPlan)
					filter += " and tplanpk = '" + planfk + "'";

				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		productgroup = AppData.getProductgroupLabel(arg);
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		status = "";
		selincused = "";
		planno = "";
		grid.setModel(model);
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tplanpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TplanListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
				List<Tplan> listData = planDao.listByFilter(filter, orderby);
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
					BigDecimal totalanggaran = new BigDecimal(0);
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("DAFTAR INISIASI PENGADAAN " + AppData.getProductgroupLabel(productgroup));
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
					datamap.put(1,
							new Object[] { "No", "No Pengadaan", "Tanggal Input", "Produk Grup", "Status", "Anggaran",
									"Total Unit", "Total Proses", "No Memo", "Tanggal Memo", "Status", "Inputer",
									"Pemutus", "Tanggal Keputusan" });
					no = 2;
					for (Tplan data : listData) {
						datamap.put(no, new Object[] { no - 1, data.getPlanno(),
								datetimeLocalFormatter.format(data.getInputtime()),
								AppData.getProductgroupLabel(data.getProductgroup()),
								AppData.getStatusLabel(data.getStatus()),
								"Rp." + NumberFormat.getInstance().format(data.getAnggaran()), data.getTotalqty(),
								data.getTotalprocess(), data.getMemono(), dateLocalFormatter.format(data.getMemodate()),
								AppData.getStatusLabel(data.getStatus()), data.getInputer(),
								data.getDecisionby() != null ? data.getDecisionby() : "-",
								data.getDecisiontime() != null ? datetimeLocalFormatter.format(data.getDecisiontime())
										: "-" });
						no++;
						total += data.getTotalqty();
						totalanggaran = totalanggaran.add(data.getAnggaran());

					}

					datamap.put(no, new Object[] { "TOTAL", "", "", "", "", "", total });
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
					String filename = "CAPTION_DAFTAR_INISIASI_PENGADAAN_"
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

	@Command
	@NotifyChange("*")
	public void doAdd(@BindingParam("item") String item) {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("arg", arg);
			map.put("isDetail", "Y");
			Window win = (Window) Executions.createComponents("/view/planning/planningentry.zul", null, map);
			win.setWidth("70%");
			win.setClosable(true);
			win.doModal();
			win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					doReset();
					BindUtils.postNotifyChange(null, null, PlanningListVm.this, "*");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getPageStartNumber() {
		return pageStartNumber;
	}

	public void setPageStartNumber(int pageStartNumber) {
		this.pageStartNumber = pageStartNumber;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public boolean isNeedsPageUpdate() {
		return needsPageUpdate;
	}

	public void setNeedsPageUpdate(boolean needsPageUpdate) {
		this.needsPageUpdate = needsPageUpdate;
	}

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
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

	public String getSelincused() {
		return selincused;
	}

	public void setSelincused(String selincused) {
		this.selincused = selincused;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public Muser getoUser() {
		return oUser;
	}

	public void setoUser(Muser oUser) {
		this.oUser = oUser;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public String getPlanno() {
		return planno;
	}

	public void setPlanno(String planno) {
		this.planno = planno;
	}
}
