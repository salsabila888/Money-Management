
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
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.HibernateException;
import org.hibernate.Session;
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

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.dao.TreturnitemDAO;
import com.sdd.caption.dao.TreturntrackDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.domain.Treturntrack;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.domain.Vsumbyproductgroup;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TreturnListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ReturProductListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Session session;
	private Transaction transaction;

	private Muser oUser;
	private TreturnDAO oDao = new TreturnDAO();

	private TreturnListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer year;
	private Integer month;
	private String status;
	private String arg;
	private String productgroup;
	private Integer branchlevel;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private Map<Integer, Tbranchstock> mapStock = new HashMap<Integer, Tbranchstock>();

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();
		this.arg = arg;
		productgroup = arg;
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Treturn>() {

				@Override
				public void render(Row row, final Treturn data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					A a = new A(data.getRegid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("obj", data);
							win = (Window) Executions.createComponents("/view/return/returproductitem.zul", null, map);
							win.setWidth("50%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);

					A doc = new A(data.getFilename());
					doc.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Sessions.getCurrent().setAttribute("reportPath", AppUtils.FILES_ROOT_PATH
									+ AppUtils.PATH_TOKENDOC + "/" + data.getFilename().trim());
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
					row.appendChild(
							new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
					row.appendChild(new Label(
							data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : "-"));
					row.appendChild(
							new Label(data.getInsertedby() != null ? data.getMreturnreason().getReturnreason() : "-"));
					row.appendChild(new Label(data.getInsertedby() != null ? data.getInsertedby() : "-"));
					row.appendChild(new Label(
							data.getInserttime() != null ? dateLocalFormatter.format(data.getInserttime()) : "-"));
					row.appendChild(new Label(data.getDecisionby() != null ? data.getDecisionby() : "-"));
					row.appendChild(new Label(
							data.getDecisiontime() != null ? dateLocalFormatter.format(data.getDecisiontime()) : "-"));
					Button btnRetur = new Button();
					btnRetur.setLabel("Retur");
					btnRetur.setAutodisable("self");
					btnRetur.setSclass("btn-light");
					btnRetur.setStyle(
							"border-radius: 8px; background-color: #eeba0b !important; color: #ffffff !important;");
					btnRetur.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							session = StoreHibernateUtil.openSession();
							transaction = session.beginTransaction();
							if (data.getStatus().equals(AppUtils.STATUS_RETUR_PROCESSPFA)) {
								data.setStatus(AppUtils.STATUS_RETUR_RETURNPFA);
								
								Mmenu mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/return/return.zul' and menuparamvalue = 'returpaket'");
								NotifHandler.doNotif(mmenu, data.getMbranch(), data.getMproduct().getProductgroup(),
										oUser.getMbranch().getBranchlevel());
							} else if (data.getStatus().equals(AppUtils.STATUS_RETUR_PROCESSWIL)) {
								data.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVALPFA);

								Mmenu mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/return/return.zul' and menuparamvalue = 'approvalpfa'");
								NotifHandler.doNotif(mmenu, data.getMbranch(), data.getMproduct().getProductgroup(),
										oUser.getMbranch().getBranchlevel() - 1);
							}

							List<Treturnitem> objList = new TreturnitemDAO()
									.listByFilter("treturnfk = " + data.getTreturnpk(), "treturnitempk");
							for (Treturnitem dataItem : objList) {
								Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
										+ dataItem.getItemno() + "' and status = '" + dataItem.getItemstatus() + "'");
								if (objStock != null) {

									objStock.setStatus(data.getStatus());
									new TbranchstockitemDAO().save(session, objStock);

									Tbranchitemtrack objTrack = new Tbranchitemtrack();
									objTrack.setItemno(dataItem.getItemno());
									objTrack.setTracktime(new Date());
									objTrack.setTrackdesc(AppData.getStatusLabel(dataItem.getTreturn().getStatus()));
									objTrack.setProductgroup(dataItem.getTreturn().getMproduct().getProductgroup());
									objTrack.setMproduct(dataItem.getTreturn().getMproduct());
									objTrack.setTrackstatus(dataItem.getTreturn().getStatus());
									new TbranchitemtrackDAO().save(session, objTrack);

									mapStock.put(objStock.getTbranchstock().getTbranchstockpk(),
											objStock.getTbranchstock());
								}
								dataItem.setItemstatus(data.getStatus());
								new TreturnitemDAO().save(session, dataItem);
							}
							for (Entry<Integer, Tbranchstock> entry : mapStock.entrySet()) {
								Tbranchstock stock = entry.getValue();
								if (data.getMreturnreason().getIsDestroy().equals("Y")) {
									stock.setStockdestroyed(stock.getStockdestroyed() + data.getItemqty());
									stock.setStockactivated(stock.getStockactivated() - data.getItemqty());
								}
								new TbranchstockDAO().save(session, stock);
							}

							oDao.save(session, data);

							Treturntrack objrt = new Treturntrack();
							objrt.setTreturn(data);
							objrt.setTracktime(new Date());
							objrt.setTrackstatus(data.getStatus());
							objrt.setTrackdesc(AppData.getStatusLabel(objrt.getTrackstatus()));
							new TreturntrackDAO().save(session, objrt);

							Mmenu mmenu = new MmenuDAO()
									.findByFilter("menupath = '/view/return/return.zul' and menuparamvalue = 'list'");
							NotifHandler.delete(mmenu, data.getMbranch(), data.getMproduct().getProductgroup(),
									oUser.getMbranch().getBranchlevel());

							transaction.commit();
							session.close();
							refreshModel(pageStartNumber);
						}
					});

					Button btnDestroy = new Button();
					btnDestroy.setLabel("Destroy");
					btnDestroy.setAutodisable("self");
					btnDestroy.setSclass("btn-light");
					btnDestroy.setStyle(
							"border-radius: 8px; background-color: #ce0012 !important; color: #ffffff !important;");
					btnDestroy.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							session = StoreHibernateUtil.openSession();
							transaction = session.beginTransaction();

							data.setStatus(AppUtils.STATUS_RETUR_DESTROYED);
							data.setIsdestroy("Y");
							data.setLettertype(data.getMproduct().getMproducttype().getDoctype());
							List<Treturnitem> objList = new TreturnitemDAO()
									.listByFilter("treturnfk = " + data.getTreturnpk(), "treturnitempk");
							for (Treturnitem dataItem : objList) {
								Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
										+ dataItem.getItemno() + "' and status = '" + dataItem.getItemstatus() + "'");
								if (objStock != null) {

									objStock.setStatus(data.getStatus());
									new TbranchstockitemDAO().save(session, objStock);

									Tbranchitemtrack objTrack = new Tbranchitemtrack();
									objTrack.setItemno(dataItem.getItemno());
									objTrack.setTracktime(new Date());
									objTrack.setTrackdesc(AppData.getStatusLabel(dataItem.getTreturn().getStatus()));
									objTrack.setProductgroup(dataItem.getTreturn().getMproduct().getProductgroup());
									objTrack.setMproduct(dataItem.getTreturn().getMproduct());
									objTrack.setTrackstatus(dataItem.getTreturn().getStatus());
									new TbranchitemtrackDAO().save(session, objTrack);

								}
								dataItem.setItemstatus(data.getStatus());
								new TreturnitemDAO().save(session, dataItem);
							}

							for (Entry<Integer, Tbranchstock> entry : mapStock.entrySet()) {
								Tbranchstock stock = entry.getValue();
								stock.setStockcabang(stock.getStockcabang() + data.getItemqty());
								stock.setStockactivated(stock.getStockactivated() - data.getItemqty());
								new TbranchstockDAO().save(session, stock);
							}
							oDao.save(session, data);

							Treturntrack objrt = new Treturntrack();
							objrt.setTreturn(data);
							objrt.setTracktime(new Date());
							objrt.setTrackstatus(data.getStatus());
							objrt.setTrackdesc(AppData.getStatusLabel(objrt.getTrackstatus()));
							new TreturntrackDAO().save(session, objrt);

							transaction.commit();
							session.close();
							
							Mmenu mmenu = new MmenuDAO()
									.findByFilter("menupath = '/view/return/return.zul' and menuparamvalue = 'list'");
							NotifHandler.delete(mmenu, data.getMbranch(), data.getMproduct().getProductgroup(),
									oUser.getMbranch().getBranchlevel());
							refreshModel(pageStartNumber);
						}
					});

					Div div = new Div();
					if (data.getStatus().equals(AppUtils.STATUS_RETUR_PROCESSWIL)
							&& oUser.getMbranch().getBranchlevel() == 2
							|| data.getStatus().equals(AppUtils.STATUS_RETUR_PROCESSPFA)
									&& oUser.getMbranch().getBranchlevel() == 1) {
						div.appendChild(btnRetur);
						div.appendChild(btnDestroy);
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

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "treturnpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TreturnListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
			filter = "extract(year from inserttime) = " + year + " and " + "extract(month from inserttime) = " + month
					+ " and productgroup = '" + arg + "' and isdestroy = 'N'";
			
			if (branchlevel == 3) {
				filter += "and returnlevel = '3'";
			} else if (branchlevel == 2) { 
				filter += " and status not in ('" + AppUtils.STATUS_RETUR_DECLINE + "', '" 
						+ AppUtils.STATUS_RETUR_WAITAPPROVAL + "')";
			} else if (branchlevel == 1) {
				if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
					filter += "and status in ('" + AppUtils.STATUS_RETUR_WAITAPPROVALPFA + "', '" 
							+ AppUtils.STATUS_RETUR_APPROVALPFA + "', '" 
							+ AppUtils.STATUS_RETUR_DECLINEAPPROVALPFA + "', '" 
							+ AppUtils.STATUS_RETUR_RETURNPFA + "', '" 
							+ AppUtils.STATUS_RETUR_RETURNEDPFA + "', '"  
							+ AppUtils.STATUS_RETUR_RECEIVED + "', '"  
							+ AppUtils.STATUS_RETUR_PROCESSPFA + "')";
				} else if (arg.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
					filter += " and status in ('" + AppUtils.STATUS_RETUR_WAITAPPROVALOPR + "', '" 
							+ AppUtils.STATUS_RETUR_APPROVALOPR + "', '" 
							+ AppUtils.STATUS_RETUR_DECLINEAPPROVALOPR + "', '" 
							+ AppUtils.STATUS_RETUR_RETURNOPR + "')";
				}
			} 

			if (status.length() > 0)
				filter += " and status = '" + status + "'";

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
				List<Treturn> listData = oDao.listNativeByFilter(filter, orderby);
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
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("DAFTAR RETUR " + AppData.getProductgroupLabel(productgroup));
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
							new Object[] { "No", "Retur ID", "Nama Cabang", "Nama Wilayah", "Nama Barang",
									"Kode Barang", "Jumlah Unit", "Alasan Pengembalian", "Status", "Direquest oleh",
									"Tanggal Request", "Pemutus", "Tanggal Keputusan" });
					no = 2;
					for (Treturn data : listData) {
						datamap.put(no, new Object[] { no - 1, data.getRegid(), data.getMbranch().getBranchname(),
								data.getMbranch().getMregion().getRegionname(), data.getMproduct().getProductname(),
								data.getMproduct().getProductcode(),
								NumberFormat.getInstance().format(data.getItemqty()),
								data.getMreturnreason().getReturnreason(), AppData.getStatusLabel(data.getStatus()),
								data.getInsertedby(), dateLocalFormatter.format(data.getInserttime()),
								data.getDecisionby(), dateLocalFormatter.format(data.getDecisiontime()) });
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
					String filename = "CAPTION_DAFTAR_RETUR_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
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
		status = "";
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Muser getoUser() {
		return oUser;
	}

	public void setoUser(Muser oUser) {
		this.oUser = oUser;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}


}
