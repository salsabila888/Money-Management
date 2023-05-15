package com.sdd.caption.viewmodel;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
import org.zkoss.lang.Library;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.dao.TpinpaditemDAO;
import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.dao.TplanproductDAO;
import com.sdd.caption.dao.TregisterstockDAO;
import com.sdd.caption.dao.TsecuritiesitemDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.domain.Tpinpaditem;
import com.sdd.caption.domain.Tplanproduct;
import com.sdd.caption.domain.Tregisterstock;
import com.sdd.caption.domain.Tsecuritiesitem;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TincomingListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class IncomingApprovalVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private TincomingListModel model;

	private TincomingDAO oDao = new TincomingDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String action;
	private String memo;
	private String productgroup;
	private String producttype;
	private int maxFlush;
	private int maxBatchCommit;
	private int flushCounter;
	private Integer total;

	BigDecimal x = new BigDecimal(0);
	BigDecimal totalitem = new BigDecimal(0);
	BigDecimal percent = new BigDecimal(0);

	private Muser oUser;
	private List<Tincoming> objSelected = new ArrayList<Tincoming>();

	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Radiogroup rgapproval;
	@Wire
	private Column colStartno, colEndno;
	@Wire
	private Progressmeter progressmeter;
	@Wire
	private Label progress_label;
	@Wire
	private Div divProgress;
	@Wire
	private Timer timer;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		productgroup = arg.trim();
		maxFlush = Integer.parseInt(Library.getProperty("maxFlush"));
		maxBatchCommit = Integer.parseInt(Library.getProperty("maxBatchCommit"));
		oUser = (Muser) zkSession.getAttribute("oUser");

		if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			colStartno.setVisible(true);
			colEndno.setVisible(true);
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
			grid.setRowRenderer(new RowRenderer<Tincoming>() {
				@Override
				public void render(Row row, final Tincoming data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked()) {
								objSelected.add((Tincoming) checked.getAttribute("obj"));
								totalitem = totalitem.add(new BigDecimal(data.getItemqty()));
								System.out.println("TOTAL ITEM : " + totalitem);
							} else {
								objSelected.remove((Tincoming) checked.getAttribute("obj"));
								totalitem = totalitem.subtract(new BigDecimal(data.getItemqty()));
								System.out.println("TOTAL ITEM : " + totalitem);
							}
						}
					});
					row.getChildren().add(check);
					if (data.getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
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
					row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
					row.getChildren().add(new Label(data.getMproducttype().getProducttype()));
					if (data.getPrefix() != null && !"".equals(data.getPrefix().trim())) {
						row.getChildren().add(
								new Label(data.getPrefix() != null ? data.getPrefix() + data.getItemstartno() : "-"));
						Integer x = data.getItemstartno() + data.getItemqty() - 1;
						row.getChildren().add(new Label(data.getPrefix() != null ? data.getPrefix() + x : "-"));
					} else {
						row.getChildren().add(new Label("-"));
						row.getChildren().add(new Label("-"));
					}
					row.getChildren().add(new Label(
							data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : "0"));
					row.getChildren().add(new Label(
							data.getEntrytime() != null ? datetimeLocalFormatter.format(data.getEntrytime()) : "-"));
					row.getChildren().add(new Label(
							data.getEntryby() != null && !data.getEntryby().equals("") ? data.getEntryby() : "-"));
					row.getChildren().add(
							new Label(data.getMemo() != null && !data.getMemo().equals("") ? data.getMemo() : "-"));
				}
			});
		}
		doReset();
	}

	@Command
	@NotifyChange({ "pageTotalSize", "total" })
	public void doSearch() {
		try {
			filter = "tincoming.productgroup = '" + productgroup + "' and tincoming.status = '"
					+ AppUtils.STATUS_INVENTORY_INCOMINGWAITAPPROVAL + "'";

			if (producttype != null && producttype.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
			}

			for (Tincoming data : oDao.listNativeByFilter(filter, "tincomingpk")) {
				total = total + data.getItemqty();
			}
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			objSelected = new ArrayList<Tincoming>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tincoming incoming = (Tincoming) chk.getAttribute("obj");
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Tincoming) chk.getAttribute("obj"));
					totalitem = totalitem.add(new BigDecimal(incoming.getItemqty()));
					System.out.println("TOTAL ITEM : " + totalitem);
				} else {
					chk.setChecked(false);
					objSelected.remove((Tincoming) chk.getAttribute("obj"));
					totalitem = totalitem.subtract(new BigDecimal(incoming.getItemqty()));
					System.out.println("TOTAL ITEM : " + totalitem);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		x = new BigDecimal(0);
		totalitem = new BigDecimal(0);
		percent = new BigDecimal(0);
		memo = null;
		objSelected = new ArrayList<Tincoming>();
		total = 0;
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
	@NotifyChange("*")
	public void doSave() {
		if (objSelected.size() > 0) {
			if (action != null && action.length() > 0) {
				if (action.equals(AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED)
						|| (action.equals(AppUtils.STATUS_INVENTORY_INCOMINGDECLINE) && memo != null
								&& memo.trim().length() > 0)) {

					Session session = null;
					Transaction transaction = null;
					try {
						boolean isValid = true;
						String incomingid = "";
						for (Tincoming obj : objSelected) {
							if (action.equals(AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED)) {
								flushCounter = 0;
//								divProgress.setVisible(true);
								Mproducttype mproducttype = obj.getMproducttype();

								if (obj.getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
									if (obj.getFilename() != null) {
										System.out.println(obj.getFilename());
										String path = Executions.getCurrent().getDesktop().getWebApp()
												.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_TOKEN + "/"
														+ obj.getFilename());
										FileInputStream file = new FileInputStream(path);

										Workbook wb = null;
										if (obj.getFilename().trim().toLowerCase().endsWith("xlsx")) {
											wb = new XSSFWorkbook(file);
										} else if (obj.getFilename().trim().toLowerCase().endsWith("xls")) {
											wb = new HSSFWorkbook(file);
										}
										Sheet sheet = wb.getSheetAt(0);
										for (org.apache.poi.ss.usermodel.Row row : sheet) {
											try {
												if (row.getRowNum() < 2) {
													continue;
												}
												String startno = null;
												String endno = null;

												for (int count = 0; count <= row.getLastCellNum(); count++) {
													Cell cell = row.getCell(count,
															org.apache.poi.ss.usermodel.Row.RETURN_BLANK_AS_NULL);
													if (cell == null) {
														continue;
													}

													switch (count) {
													case 1:
														if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
															cell.setCellType(Cell.CELL_TYPE_STRING);
															startno = cell.getStringCellValue();
														} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
															startno = cell.getStringCellValue();
														} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
															cell.setCellType(Cell.CELL_TYPE_STRING);
															startno = cell.getStringCellValue();
														}
														break;
													case 3:
														if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
															cell.setCellType(Cell.CELL_TYPE_STRING);
															endno = cell.getStringCellValue();
														} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
															endno = cell.getStringCellValue();
														} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
															cell.setCellType(Cell.CELL_TYPE_STRING);
															endno = cell.getStringCellValue();
														}
														break;
													}
												}

												session = StoreHibernateUtil.openSession();
												transaction = session.beginTransaction();
												try {
													Integer itemno = Integer.parseInt(startno);
													for (Integer i = itemno; i <= Integer.parseInt(endno); i++) {
														Ttokenitem data = new Ttokenitem();
														data.setTincoming(obj);
														data.setItemno(i.toString());
														data.setItemnoinject(null);
														data.setStatus(AppUtils.STATUS_SERIALNO_ENTRY);
														new TtokenitemDAO().save(session, data);

														if (flushCounter % maxFlush == 0) {
															session.flush();
															session.clear();
														}

														if (flushCounter % maxBatchCommit == 0) {
															transaction.commit();
															session.close();

															session = StoreHibernateUtil.openSession();
															transaction = session.beginTransaction();
														}
														flushCounter++;
														System.out.println("TOTAL PROSES : " + flushCounter);
													}
												} catch (Exception e) {
													e.printStackTrace();
												} finally {
													if (session.isOpen()) {
														transaction.commit();
														session.close();
													}
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
										}

										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										obj.setDecisionby(oUser.getUsername());
										obj.setDecisiontime(new Date());
										obj.setDecisionmemo(memo);
										obj.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED);
										oDao.save(session, obj);
										transaction.commit();
										session.close();
									} else {
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										obj.setDecisionby(oUser.getUsername());
										obj.setDecisiontime(new Date());
										obj.setDecisionmemo(memo);
										obj.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED);
										obj.getTplanfk().setIncomingused(1);
										oDao.save(session, obj);
										transaction.commit();
										session.close();

										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										try {
											Integer itemno = obj.getItemstartno();
											for (Integer i = 1; i <= obj.getItemqty(); i++) {
												Ttokenitem data = new Ttokenitem();
												data.setTincoming(obj);
												data.setItemno(itemno.toString());
												data.setItemnoinject(null);
												data.setStatus(AppUtils.STATUS_SERIALNO_ENTRY);
												new TtokenitemDAO().save(session, data);
												itemno++;
												if (flushCounter % maxFlush == 0) {
													session.flush();
													session.clear();
												}

												if (flushCounter % maxBatchCommit == 0) {
													transaction.commit();
													session.close();

													session = StoreHibernateUtil.openSession();
													transaction = session.beginTransaction();
												}

												flushCounter++;
											}
										} catch (Exception e) {
											e.printStackTrace();
										} finally {
											if (session.isOpen()) {
												transaction.commit();
												session.close();
											}
										}
									}
								} else if (obj.getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();

									String path = Executions.getCurrent().getDesktop().getWebApp().getRealPath(
											AppUtils.FILES_ROOT_PATH + AppUtils.PATH_PINPAD + "/" + obj.getFilename());
									System.out.println(path);
									FileInputStream file = new FileInputStream(path);

									Workbook wb = null;
									if (obj.getFilename().toLowerCase().endsWith("xlsx")) {
										wb = new XSSFWorkbook(file);
									} else if (obj.getFilename().trim().toLowerCase().endsWith("xls")) {
										wb = new HSSFWorkbook(file);
									}
									Sheet sheet = wb.getSheetAt(0);
									for (org.apache.poi.ss.usermodel.Row row : sheet) {
										try {
											if (row.getRowNum() < 1) {
												continue;
											}
											Tpinpaditem data = new Tpinpaditem();
											for (Integer count = 0; count <= row.getLastCellNum(); count++) {
												data.setTincoming(obj);
												Cell cell = row.getCell(count,
														org.apache.poi.ss.usermodel.Row.RETURN_BLANK_AS_NULL);
												if (cell == null) {
													continue;
												}

												switch (count) {
												case 1:
													if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
														cell.setCellType(Cell.CELL_TYPE_STRING);
														data.setItemno(cell.getStringCellValue());
													} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
														data.setItemno(cell.getStringCellValue());
													}
													break;
												}
												data.setStatus(AppUtils.STATUS_SERIALNO_ENTRY);
												new TpinpaditemDAO().save(session, data);
											}

											if (flushCounter % maxFlush == 0) {
												session.flush();
												session.clear();
											}

											if (flushCounter % maxBatchCommit == 0) {
												transaction.commit();
												session.close();

												session = StoreHibernateUtil.openSession();
												transaction = session.beginTransaction();
											}
											flushCounter++;
										} catch (Exception e) {
											e.printStackTrace();
										}
									}

									obj.setDecisionby(oUser.getUsername());
									obj.setDecisiontime(new Date());
									obj.setDecisionmemo(memo);
									obj.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED);
									oDao.save(session, obj);

									if (session.isOpen()) {
										transaction.commit();
										session.close();
									}

								} else if (obj.getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
									Tincoming duplicate = oDao.findByFilter("tincomingpk != " + obj.getTincomingpk()
											+ " and mproducttypefk = " + obj.getMproducttype().getMproducttypepk()
											+ " and prefix = '" + obj.getPrefix() + "' and " + obj.getItemstartno()
											+ " between  itemstartno and (itemstartno + itemqty - 1) and status != '"
											+ AppUtils.STATUS_INVENTORY_INCOMINGDECLINE + "'");
									if (duplicate != null) {
										isValid = false;
										if (incomingid.trim().length() > 0)
											incomingid += "\n";
										incomingid += obj.getIncomingid();
									}

									if (isValid) {
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										obj.setDecisionby(oUser.getUsername());
										obj.setDecisiontime(new Date());
										obj.setDecisionmemo(memo);
										obj.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED);
										obj.setLastupdated(new Date());
										obj.setUpdatedby(oUser.getUserid());
										obj.getTplanfk().setIncomingused(1);
										oDao.save(session, obj);
										new TplanDAO().save(session, obj.getTplanfk());

										Tregisterstock objregstock = new Tregisterstock();
										objregstock.setBranch(oUser.getMbranch().getBranchname());
										objregstock.setProductgroup(productgroup);
										objregstock.setMproduct(obj.getMproducttype().getProducttype());
										objregstock.setTglincoming(new Date());
										objregstock.setPrefix(obj.getPrefix());
										objregstock.setNumerawalinc(obj.getPrefix().trim() + obj.getItemstartno());
										Integer numerakhirinc = obj.getItemstartno() + obj.getItemqty() - 1;
										objregstock.setNumerakhirinc(obj.getPrefix().trim() + numerakhirinc);
										objregstock.setJumlahinc(obj.getItemqty());
										objregstock.setTgloutgoing(null);
										objregstock.setNumerawaloutg(null);
										objregstock.setNumerakhiroutg(null);
										objregstock.setJumlahoutg(null);
										objregstock.setNumerawalouts(obj.getPrefix().trim() + obj.getItemstartno());
										objregstock.setNumerakhirouts(obj.getPrefix().trim() + numerakhirinc);
										objregstock.setJumlahouts(obj.getItemqty());
										objregstock.setTincomingfk(obj);
										objregstock.setBranchlevel(oUser.getMbranch().getBranchlevel());
										new TregisterstockDAO().save(session, objregstock);
										transaction.commit();
										session.close();

										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										try {
											Integer itemno = obj.getItemstartno();
											Integer itemprocess = 0;
											for (Integer i = 1; i <= obj.getItemqty(); i++) {
												Tsecuritiesitem data = new Tsecuritiesitem();
												data.setTincoming(obj);
												data.setItemno(obj.getPrefix() + itemno.toString());
												data.setNumerator(itemno);
												data.setStatus(AppUtils.STATUS_SERIALNO_ENTRY);
												new TsecuritiesitemDAO().save(session, data);

												itemno++;
												itemprocess++;

//												x = new BigDecimal(itemprocess);
//												percent = x.divide(totalitem).multiply(new BigDecimal(100));
//												percent = percent.setScale(0, RoundingMode.HALF_UP);
//
//												progress_label.setValue(percent + "%");
//												progressmeter.setValue(percent.intValue());

												if (flushCounter % maxFlush == 0) {
													session.flush();
													session.clear();
												}

												if (flushCounter % maxBatchCommit == 0) {
													transaction.commit();
													session.close();

													session = StoreHibernateUtil.openSession();
													transaction = session.beginTransaction();
												}
												flushCounter++;
											}
										} catch (Exception e) {
											e.printStackTrace();
										} finally {
											if (session.isOpen()) {
												transaction.commit();
												session.close();
											}
										}
									}
								} else {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									obj.setStatus(action);
									obj.setDecisionmemo(memo);
									obj.setDecisionby(oUser.getUserid());
									obj.setDecisiontime(new Date());
									oDao.save(session, obj);

									isValid = true;
									transaction.commit();
									session.close();
								}

								if (isValid) {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									mproducttype.setLaststock(
											(mproducttype.getLaststock() == null ? 0 : mproducttype.getLaststock())
													+ obj.getItemqty());
									if (mproducttype.getLaststock() >= mproducttype.getStockmin()
											&& mproducttype.getIsalertstockpagu().equals("Y")) {
										mproducttype.setIsalertstockpagu("N");
										mproducttype.setIsblockpagu("N");
										mproducttype.setAlertstockpagurelease(new Date());
									} else {
										mproducttype.setIsalertstockpagu("N");
										mproducttype.setAlertstockpagurelease(new Date());
									}
									mproducttypeDao.save(session, mproducttype);
									transaction.commit();
									session.close();

									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/inventory/incoming.zul' and menuparamvalue = 'approval'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());
								}
							} else {
								session = StoreHibernateUtil.openSession();
								transaction = session.beginTransaction();
								obj.setStatus(action);
								obj.setDecisionmemo(memo);
								obj.setDecisionby(oUser.getUsername());
								obj.setDecisiontime(new Date());
								oDao.save(session, obj);

								if (obj.getTplanfk() != null) {
									obj.getTplanfk()
											.setTotalprocess(obj.getTplanfk().getTotalprocess() - obj.getItemqty());
									obj.getTplanfk().setStatus(AppUtils.STATUS_PLANNING_APPROVED);
									new TplanDAO().save(session, obj.getTplanfk());

									Tplanproduct tpp = new TplanproductDAO().findByFilter(
											"tplanfk = " + obj.getTplanfk().getTplanpk() + " and mproducttypefk = "
													+ obj.getMproducttype().getMproducttypepk());
									if (tpp != null) {
										tpp.setTotalprocess(tpp.getTotalprocess() - obj.getItemqty());
										new TplanproductDAO().save(session, tpp);
									}
								}
								Mmenu mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/inventory/incoming.zul' and menuparamvalue = 'approval'");
								NotifHandler.delete(mmenu, oUser.getMbranch(), productgroup,
										oUser.getMbranch().getBranchlevel());

								transaction.commit();
								session.close();
							}
						}

						if (isValid) {
							if (action.equals(AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED)) {
								Clients.showNotification("Proses persetujuan data berhasil disetujui", "info", null,
										"middle_center", 3000);
							} else {
								Clients.showNotification("Proses persetujuan data berhasil ditolak", "info", null,
										"middle_center", 3000);
							}
						} else {
							Messagebox.show(
									"Terdapat data yang mempunyai nomer serial yang sudah terdaftar.\nTipe Produk : \n"
											+ incomingid,
									"Info", Messagebox.OK, Messagebox.INFORMATION);
						}
						doReset();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Messagebox.show("Anda harus mengisi alasan decline pada kolom Catatan Approval", "Info",
							Messagebox.OK, Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Silahkan pilih action", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} else {
			Messagebox.show("Silahkan pilih data untuk proses submit", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

//	public Label getProgress_label() {
//		return progress_label;
//	}
//
//	public void setProgress_label(Label progress_label) {
//		this.progress_label = progress_label;
//	}
}
