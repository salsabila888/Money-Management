package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
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

import com.sdd.caption.dao.TrepairDAO;
import com.sdd.caption.dao.TrepairitemDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Trepair;
import com.sdd.caption.domain.Trepairitem;
import com.sdd.caption.model.TrepairListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;

public class RepairListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private TrepairDAO oDao = new TrepairDAO();

	private TrepairListModel model;

	private Trepairitem objForm;
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer year;
	private Integer month;
	private int branchlevel;

	Map<Integer, Trepair> mapData = new HashMap<Integer, Trepair>();
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Column colCheck;
	@Wire
	private Div divFoot;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		branchlevel = oUser.getMbranch().getBranchlevel();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		if (branchlevel > 1) {
			colCheck.setVisible(false);
			divFoot.setVisible(false);
		}

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Trepair>() {

				@Override
				public void render(Row row, final Trepair data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setDisabled(true);
					if (data.getStatus().equals(AppUtils.STATUS_REPAIR_PENDINGPROCESS)) {
						check.setDisabled(false);
					}
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							Trepair obj = (Trepair) checked.getAttribute("obj");
							if (checked.isChecked()) {
								mapData.put(obj.getTrepairpk(), obj);
							} else {
								mapData.remove(obj.getTrepairpk());
							}
						}
					});
					row.getChildren().add(check);

					A a = new A(data.getRegid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("obj", data);
							win = (Window) Executions.createComponents("/view/repair/repairitem.zul", null, map);
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);

					A doc = new A(data.getFilename());
					doc.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Sessions.getCurrent().setAttribute("reportPath",
									AppUtils.FILES_ROOT_PATH + AppUtils.PATH_PINPAD + "/" + data.getFilename().trim());
							Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
						}
					});
					if (data.getFilename() != null)
						row.getChildren().add(doc);
					else
						row.getChildren().add(new Label("-"));

					row.appendChild(new Label(
							data.getMbranch().getBranchname() != null ? data.getMbranch().getBranchname() : "-"));
					row.appendChild(new Label(data.getMbranch().getMregion().getRegionname() != null
							? data.getMbranch().getMregion().getRegionname()
							: "-"));
					row.appendChild(new Label(data.getMproduct().getMproducttype().getProducttype() != null
							? data.getMproduct().getMproducttype().getProducttype()
							: "-"));
					row.appendChild(new Label(
							data.getMproduct().getProductcode() != null ? data.getMproduct().getProductcode() : "-"));
					row.appendChild(new Label(
							data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : "-"));
					row.appendChild(new Label(
							data.getTotalproses() != null ? NumberFormat.getInstance().format(data.getTotalproses())
									: "-"));
					row.appendChild(new Label(
							data.getTglpemenuhan() != null ? dateLocalFormatter.format(data.getTglpemenuhan()) : "-"));
					row.appendChild(
							new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
					row.appendChild(new Label(data.getInsertedby() != null ? data.getInsertedby() : "-"));
					row.appendChild(new Label(
							data.getInserttime() != null ? dateLocalFormatter.format(data.getInserttime()) : "-"));
					row.appendChild(new Label(data.getDecisionby() != null ? data.getDecisionby() : "-"));
					row.appendChild(new Label(
							data.getDecisiontime() != null ? dateLocalFormatter.format(data.getDecisiontime()) : "-"));

					a = new A(data.getDocfile());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Sessions.getCurrent().setAttribute("reportPath",
									AppUtils.FILES_ROOT_PATH + AppUtils.PATH_PINPAD + "/" + data.getDocfile().trim());
							Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
						}
					});
					if (data.getDocfile() != null)
						row.getChildren().add(a);
					else
						row.getChildren().add(new Label("-"));

					Button btnCek = new Button("Cek Histori");
					btnCek.setAutodisable("self");
					btnCek.setClass("btn-default");
					btnCek.setStyle("border-radius: 8px;");
					btnCek.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("obj", data);
							Window win = (Window) Executions.createComponents("/view/repair/repairhistory.zul", null,
									map);
							win.setWidth("60%");
							win.setClosable(true);
							win.doModal();
						}
					});

					Button btnScan = new Button("Proses Checking");
					btnScan.setAutodisable("self");
					btnScan.setClass("btn-default");
					btnScan.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnScan.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("obj", data);
							Window win = (Window) Executions.createComponents("/view/repair/repairchecking.zul", null,
									map);
							win.setWidth("80%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									doSearch();
								}
							});
						}
					});

					Div div = new Div();
					div.setClass("btn-group");

					if (data.getStatus().trim().equals(AppUtils.STATUS_REPAIR_PROCESSCHECKING) && branchlevel == 1) {
						if (data.getTotalproses() > 0) {
							div.appendChild(btnCek);
						}
						div.appendChild(btnScan);
						row.appendChild(div);
					} else {
						if (data.getTotalproses() > 0) {
							div.appendChild(btnCek);
							row.appendChild(div);
						} else {
							row.getChildren().add(new Label());
						}
					}
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
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Trepair obj = (Trepair) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						chk.setChecked(true);
						mapData.put(obj.getTrepairpk(), obj);
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getTrepairpk());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doPrintLetter() {
		try {
			for (Entry<Integer, Trepair> entry : mapData.entrySet()) {
				List<Trepairitem> objList = new TrepairitemDAO().listNativeByFilter("itemstatus = '"
						+ AppUtils.STATUS_REPAIR_PROCESSVENDOR + "' and trepairfk = " + entry.getValue().getTrepairpk(),
						"trepairitempk");
				if (objList.size() > 0) {
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
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum);
					Cell cell = row.createCell(0);
					cell.setCellValue("Berita Acara Serah Terima Perbaikan Pinpad");
					rownum++;
					row = sheet.createRow(rownum++);
					
					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1, new Object[] { "No", "Jenis Barang", "S/N", "Keterangan" });
					no = 2;
					for (Trepairitem data : objList) {
						datamap.put(no, new Object[] { no - 1, data.getTrepair().getMproduct().getProductname(),
								data.getItemno(), data.getTrepair().getMrepairreason().getRepairreason() });
						no++;
					}
					Set<Integer> keyset = datamap.keySet();
					for (Integer key : keyset) {
						row = sheet.createRow(rownum++);
						Object[] objArr = datamap.get(key);
						cellnum = 0;
						if (rownum == 1) {
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
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPAIR_PATH);
					String filename = "LAMPIRAN_PERBAIKAN_PINPAD"
							+ new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
					FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
					workbook.write(out);
					out.close();

					Filedownload.save(new File(path + "/" + filename),
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "trepairpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TrepairListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (year != null && month != null) {
			filter = "extract(year from inserttime) = " + year + " and " + "extract(month from inserttime) = " + month;

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);

		}
	}

	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Trepair> listData = oDao.listNativeByFilter(filter, orderby);
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
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("DAFTAR REPAIR PINPAD");
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
					datamap.put(1, new Object[] { "No", "Repair ID", "Jumlah", "Status", "Direquest oleh",
							"Tanggal Request", "Pemutus", "Tanggal Keputusan" });
					no = 2;
					for (Trepair data : listData) {
						datamap.put(no,
								new Object[] { no - 1, data.getRegid(),
										NumberFormat.getInstance().format(data.getItemqty()),
										AppData.getStatusLabel(data.getStatus()), data.getInsertedby(),
										dateLocalFormatter.format(data.getInserttime()),
										data.getDecisionby() != null ? data.getDecisionby() : "-",
										data.getDecisiontime() != null
												? dateLocalFormatter.format(data.getDecisiontime())
												: "-" });
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
					String filename = "CAPTION_DAFTAR_REPAIR_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
							+ ".xlsx";
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
	public void doReset() {
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		doSearch();
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

	public Muser getoUser() {
		return oUser;
	}

	public void setoUser(Muser oUser) {
		this.oUser = oUser;
	}

	public Trepairitem getObjForm() {
		return objForm;
	}

	public void setObjForm(Trepairitem objForm) {
		this.objForm = objForm;
	}

}
