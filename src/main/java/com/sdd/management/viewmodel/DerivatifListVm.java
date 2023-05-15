package com.sdd.caption.viewmodel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.bind.annotation.ContextParam;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Foot;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.dao.TderivatifdataDAO;
import com.sdd.caption.dao.TderivatifproductDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.domain.Tderivatifproduct;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.domain.Vsumdate;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.handler.PaketManifestHandler;
import com.sdd.caption.handler.PersoPrintHandler;
import com.sdd.caption.model.TderivatifListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.caption.utils.LetterDerivatifGenerator;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DerivatifListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TderivatifListModel model;
	private TderivatifDAO oDao = new TderivatifDAO();
	private TderivatifproductDAO tderivatifproductDao = new TderivatifproductDAO();
	private TpersoDAO tpersoDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private ToutgoingDAO toutgoingDao = new ToutgoingDAO();
	private TpaketDAO tpaketDao = new TpaketDAO();
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String filename;
	private Date processdate;
	private Integer year;
	private Integer month;
	private Integer totaldata;
	private Integer totalreject;
	private String arg;
	private String action;
	private String decisionmemo;
	private String memo;
	private String title;
	private String orderno;
	private String branchname;
	private String productcode;
	private Date dateverify;

	private List<Tderivatif> objList = new ArrayList<>();
	private Map<Integer, Tderivatif> mapData = new HashMap<>();
	private Map<Integer, Mproducttype> mapProducttype = new HashMap<>();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Row rowMemo;
	@Wire
	private Column colCheck;
	@Wire
	private Column colAction;
	@Wire
	private Foot footOutApr;

	@Wire
	private Div divVerify;
	@Wire
	private Div divPersoOrder;
	@Wire
	private Div divApproval;
	@Wire
	private Div divPersoList;
	@Wire
	private Div divPaketOrder;
	@Wire
	private Div divPaketList;
	@Wire
	private Div divDlvOrder;
	@Wire
	private Div divDlvList;
	@Wire
	private Div divExport;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String argparam)
			throws ParseException {
		Selectors.wireComponents(view, this, false);

		/*
		 * G : Get Data V : Verify OPR : Order Perso PR : Perso OPK : Order Paket PK :
		 * Paket ODLV = Order Delivery DLV : Delivery
		 */

		oUser = (Muser) zkSession.getAttribute("oUser");

		if (argparam != null) {
			arg = argparam;

			if (arg.equals("getdata")) {
				colAction.setVisible(true);
				colCheck.setVisible(false);
				title = "Daftar Order Kartu Berfoto";
			} else if (arg.equals("scan") || arg.equals("crop") || arg.equals("merge")) {
				divVerify.setVisible(true);
				if (arg.equals("crop"))
					rowMemo.setVisible(true);
				title = "Daftar Order Kartu Berfoto";
			} else if (arg.equals("persoorder")) {
				divPersoOrder.setVisible(true);
				title = "Order Perso Kartu Berfoto";
			} else if (arg.equals("persoapproval")) {
				divApproval.setVisible(true);
				title = "Approval Order Perso Kartu Berfoto";
			} else if (arg.equals("persolist")) {
				divPersoList.setVisible(true);
				title = "Daftar Order Perso Kartu Berfoto";
			} else if (arg.equals("invoutapproval")) {
				divApproval.setVisible(true);
				title = "Approval Outgoing Kartu Berfoto";
				footOutApr.setVisible(true);
			} else if (arg.equals("paketorder")) {
				divPaketOrder.setVisible(true);
				title = "Order Paket Kartu Berfoto";
			} else if (arg.equals("paketlist")) {
				divPaketList.setVisible(true);
				title = "Daftar Paket Kartu Berfoto";
			} else if (arg.equals("dlvorder")) {
				divDlvOrder.setVisible(true);
				title = "Order Delivery Kartu Berfoto";
			} else if (arg.equals("dlvlist")) {
				divDlvList.setVisible(true);
				title = "Daftar delivery Kartu Berfoto";
			}

		} else {
			arg = "";
			colCheck.setVisible(false);
			divExport.setVisible(true);
			colAction.setVisible(true);
			title = "Daftar Order Kartu Berfoto";
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		grid.setRowRenderer(new RowRenderer<Tderivatif>() {

			@Override
			public void render(Row row, final Tderivatif data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tderivatif obj = (Tderivatif) checked.getAttribute("obj");
						if (checked.isChecked()) {
							mapData.put(obj.getTderivatifpk(), obj);
						} else {
							mapData.remove(obj.getTderivatifpk());
						}
					}
				});
				if (mapData.get(data.getTderivatifpk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				A a = new A(data.getOrderno());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						String path = "/view/derivatif/derivatifdata.zul";
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);

						Window win = (Window) Executions.createComponents(path, null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.doModal();
					}
				});
				row.getChildren().add(a);
				if (data.getFilename() != null) {
					A file = new A(data.getFilename());
					file.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {

							zkSession.setAttribute("reportPath",
									AppUtils.FILES_ROOT_PATH + AppUtils.PATH_DERIVATIFFILE + "/" + data.getFilename());
							Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
						}
					});

					row.getChildren().add(file);
				} else
					row.getChildren().add(new Label(""));
				row.getChildren().add(new Label(data.getMproduct() != null ? data.getMproduct().getProductcode() : ""));
				row.getChildren().add(new Label(data.getMproduct() != null ? data.getMproduct().getProductname() : ""));
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalreject())));
				row.getChildren().add(new Label(AppData.getStatusDerivatifLabel(data.getStatus())));
				row.getChildren().add(new Label(data.getMemo()));
				Div div = new Div();
				div.setClass("btn-group btn-sm");

				if (arg.equals("getdata")) {
					if (data.getStatus() == AppUtils.STATUS_DERIVATIF_GETDATA) {
						Button btGetData = new Button("Get Data");
						btGetData.setSclass("btn btn-success btn-sm");
						btGetData.setAutodisable("self");
						btGetData.setWidth("130px");
						btGetData.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								final Map<String, Object> map = new HashMap<String, Object>();
								map.put("pageno", pageStartNumber);
								map.put("obj", data);
								Window win = (Window) Executions
										.createComponents("/view/derivatif/derivatifgetdata.zul", null, map);
								win.setWidth("80%");
								win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
									@Override
									public void onEvent(Event event) throws Exception {
										if (event.getData() != null) {
											pageStartNumber = (Integer) map.get("pageno");
											needsPageUpdate = true;
											refreshModel(pageStartNumber);
										}
									}

								});
								win.setClosable(true);
								win.doModal();

							}

						});
						div.appendChild(btGetData);
					} else if (data.getStatus() > AppUtils.STATUS_DERIVATIF_GETDATA) {
						Button btDownload = new Button("File Emboss");
						btDownload.setSclass("btn btn-warning btn-sm");
						btDownload.setAutodisable("self");
						btDownload.setWidth("130px");
						btDownload.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								doGenerateEmboss(data);
							}

						});
						div.appendChild(btDownload);
					}
				} else if (arg.equals("verify")) {
					Button btVerify = new Button("Verify");
					btVerify.setSclass("btn btn-warning btn-sm");
					btVerify.setAutodisable("self");
					btVerify.setWidth("130px");
					btVerify.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							final Map<String, Object> map = new HashMap<String, Object>();
							map.put("pageno", pageStartNumber);
							map.put("obj", data);
							Window win = (Window) Executions.createComponents("/view/derivatif/derivatifverify.zul",
									null, map);
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
								@Override
								public void onEvent(Event event) throws Exception {
									if (event.getData() != null) {
										pageStartNumber = (Integer) map.get("pageno");
										needsPageUpdate = true;
										refreshModel(pageStartNumber);
									}
								}

							});
							win.setClosable(true);
							win.doModal();
						}

					});
					div.appendChild(btVerify);
				} else if (arg.equals("persoorder")) {
					Button btAction = new Button("Manifest Perso");
					btAction.setSclass("btn btn-default btn-sm");
					btAction.setAutodisable("self");

					btAction.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							doOrderPerso();
						}
					});
					div.appendChild(btAction);
				} else if (arg.equals("dlvorder")) {
					Button btAction = new Button("Manifest Pengiriman");
					btAction.setSclass("btn btn-default btn-sm");
					btAction.setAutodisable("self");
					div.appendChild(btAction);
				} else {
					if (data.getStatus() <= AppUtils.STATUS_DERIVATIF_GETDATA) {
						Button btEdit = new Button("Edit");
						btEdit.setSclass("btn btn-default btn-sm");
						btEdit.setAutodisable("self");
						btEdit.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								final Map<String, Object> map = new HashMap<String, Object>();
								map.put("pageno", pageStartNumber);
								map.put("obj", data);
								map.put("isEdit", "Y");
								Window win = (Window) Executions.createComponents("/view/derivatif/derivatiforder.zul",
										null, map);
								win.setWidth("50%");
								win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
									@Override
									public void onEvent(Event event) throws Exception {
										if (event.getData() != null) {
											pageStartNumber = (Integer) map.get("pageno");
											needsPageUpdate = true;
											refreshModel(pageStartNumber);
										}
									}

								});
								win.setClosable(true);
								win.doModal();
							}

						});
						div.appendChild(btEdit);
					}

					if (data.getStatus() <= AppUtils.STATUS_DERIVATIF_ORDERPERSO
							|| data.getStatus() == AppUtils.STATUS_DERIVATIF_APPROVEADJ) {
						Button btDelete = new Button("Delete");
						btDelete.setSclass("btn btn-danger btn-sm");
						btDelete.setAutodisable("self");
						btDelete.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								doDelete(data);
							}

						});
						div.appendChild(btDelete);
					}
				}
				row.getChildren().add(div);
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
	@NotifyChange("*")
	public void doDelete(Tderivatif oForm) {
		Messagebox.show("Anda ingin menghapus data ini?", "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL,
				Messagebox.QUESTION, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getName().equals("onOK")) {
							try {
								Session session = StoreHibernateUtil.openSession();
								Transaction transaction = session.beginTransaction();
								oDao.delete(session, oForm);
								transaction.commit();
								session.close();

								Messagebox.show("Data sudah berhasil dihapus.", WebApps.getCurrent().getAppName(),
										Messagebox.OK, Messagebox.INFORMATION);
								doSearch();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tderivatif obj = (Tderivatif) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						chk.setChecked(true);
						mapData.put(obj.getTderivatifpk(), obj);
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getTderivatifpk());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doGenerateEmboss(Tderivatif data) throws Exception {
		try {
			String pathCreate = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.GENERATE_EMBOSS_PATH + "/"
							+ data.getMbranch().getBranchname()
							+ new SimpleDateFormat("YYMMDD").format(data.getOrderdate()));
			String pathEmboss = "";

			FileWriter file = new FileWriter(pathCreate);

			BufferedWriter bufferedWriter = new BufferedWriter(file);

			List<String> listCardno = new TderivatifdataDAO()
					.listCardno("tderivatif.tderivatifpk = " + data.getTderivatifpk());

			List<Vsumdate> listOrderdate = new TderivatifdataDAO()
					.getGroupOrderdate("tderivatiffk = " + data.getTderivatifpk());
			for (Vsumdate vsumdate : listOrderdate) {
				int count = 0;
				pathEmboss = Executions.getCurrent().getDesktop().getWebApp().getRealPath(AppUtils.FILES_ROOT_PATH
						+ AppUtils.PATH_EMBOSSFILE + "/" + dateFormatter.format(vsumdate.getDate()));
				File[] files = new File(pathEmboss + "/").listFiles();
				if (files != null) {
					for (File emboss : files) {
						BufferedReader reader = new BufferedReader(new FileReader(emboss));
						String line = "";
						while ((line = reader.readLine()) != null) {
							try {
								if (listCardno.contains(line.substring(1, 20).trim())) {
									bufferedWriter.write(line);
									bufferedWriter.newLine();
									count++;
								}

								if (count == vsumdate.getTotal())
									break;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						reader.close();
					}
				}

			}

			bufferedWriter.close();
			Filedownload.save(new File(pathCreate), "text/plain");
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
		}
	}

	@Command
	public void doVerify() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else if (dateverify == null) {
			Messagebox.show("Silahkan lengkapi isian tanggal proses", WebApps.getCurrent().getAppName(), Messagebox.OK,
					Messagebox.INFORMATION);
		} else {
			Messagebox.show("Anda ingin melakukan update status?", "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL,
					Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								String strError = "";
								Session session = null;
								Transaction transaction = null;
								for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									Tderivatif data = entry.getValue();
									try {
										if (data.getStatus().equals(AppUtils.STATUS_DERIVATIF_SCAN)) {
											data.setScandate(dateverify);

											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/derivatif/derivatiflist.zul' and menuparamvalue = 'crop'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());

											mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/derivatif/derivatiflist.zul' and menuparamvalue = 'scan'");
											NotifHandler.delete(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());

										} else if (data.getStatus().equals(AppUtils.STATUS_DERIVATIF_CROP)) {
											data.setCropdate(dateverify);
											data.setMemo(memo);

											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/derivatif/derivatiflist.zul' and menuparamvalue = 'merge'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());

											mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/derivatif/derivatiflist.zul' and menuparamvalue = 'crop'");
											NotifHandler.delete(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());
										} else if (data.getStatus().equals(AppUtils.STATUS_DERIVATIF_MERGE)) {
											data.setMergedate(dateverify);

											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persoorder'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());

											mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/derivatif/derivatiflist.zul' and menuparamvalue = 'merge'");
											NotifHandler.delete(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());
										}
										data.setStatus(data.getStatus() + 1);
										oDao.save(session, data);
										transaction.commit();
									} catch (HibernateException e) {
										e.printStackTrace();
										if (strError.length() > 0)
											strError += ". \n";
										strError += e.getMessage();
									} catch (Exception e) {
										e.printStackTrace();
										if (strError.length() > 0)
											strError += ". \n";
										strError += e.getMessage();
									} finally {
										session.close();
									}

								}
								if (strError.length() > 0)
									Messagebox.show("Terdapat kegagalan pada proses submit data. \n" + strError,
											WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
								else {
									Clients.showNotification("Proses update status berhasil", "info", null,
											"middle_center", 3000);
								}
								doReset();
								BindUtils.postNotifyChange(null, null, DerivatifListVm.this, "*");
							}
						}
					});
		}
	}

	@Command
	public void doOrderPerso() throws Exception {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			Messagebox.show("Anda ingin membuat manifest perso untuk data order kartu berfoto?", "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								Session session = null;
								Transaction transaction = null;
								TpersoDAO tpersoDao = new TpersoDAO();
								TpersodataDAO tpersodataDao = new TpersodataDAO();

								int productRejectCount = 0;
								String productRejected = "";
								boolean isValidStock = true;

								try {
									mapProducttype = new HashMap<Integer, Mproducttype>();
									for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
										Tderivatif data = entry.getValue();
										if (mapProducttype.get(
												data.getMproduct().getMproducttype().getMproducttypepk()) == null) {
											mapProducttype.put(data.getMproduct().getMproducttype().getMproducttypepk(),
													data.getMproduct().getMproducttype());
										}

										Mproducttype objStock = mapProducttype
												.get(data.getMproduct().getMproducttype().getMproducttypepk());
										if (objStock.getLaststock() - objStock.getStockreserved() < data
												.getTotaldata()) {
											productRejectCount++;
											isValidStock = false;
											if (productRejected.length() > 0)
												productRejected += "\n";
											productRejected += objStock.getProducttype();
											// break;
										} else {
											objStock.setStockreserved(
													objStock.getStockreserved() + data.getTotaldata());
											mapProducttype.put(objStock.getMproducttypepk(), objStock);
										}

									}
								} catch (Exception e) {
									e.printStackTrace();
								}

								if (!isValidStock) {
									Messagebox.show(
											"Proses pembuatan grup manifest tidak bisa dilakukan karena terdapat "
													+ productRejectCount
													+ " tipe produk yang melebihi jumlah ketersediaan stock.\nTipe Produk : \n"
													+ productRejected,
											WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
								} else {
									for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										Tderivatif data = entry.getValue();
										try {
											List<Tderivatifproduct> objList = new TderivatifproductDAO().listByFilter(
													"tderivatiffk = " + data.getTderivatifpk(), "tderivatifproductpk");
											for (Tderivatifproduct obj : objList) {
												Tperso objForm = new Tperso();
												objForm.setTderivatifproduct(obj);
												objForm.setMproduct(obj.getMproduct());
												objForm.setPersoid(
														new TcounterengineDAO().generateCounter(AppUtils.CE_PERSO));
												objForm.setOrderdate(obj.getOrderdate());
												objForm.setTotaldata(obj.getTotaldata());
												objForm.setTotalpaket(0);
												objForm.setPersostartby(oUser.getUserid());
												objForm.setPersostarttime(new Date());
												objForm.setStatus(AppUtils.STATUS_PERSO_PERSOWAITAPPROVAL);
												objForm.setIsgetallpaket("");
												tpersoDao.save(session, objForm);

												Tpersodata tpersodata = new Tpersodata();
												tpersodata.setMbranch(obj.getTderivatif().getMbranch());
												tpersodata.setTperso(objForm);
												tpersodata.setOrderdate(obj.getOrderdate());
												tpersodata.setQuantity(obj.getTotaldata());
												tpersodata.setStatus("");
												tpersodata.setIsgetpaket("");
												tpersodataDao.save(session, tpersodata);

												obj.getTembossbranch().setTotalproses(
														obj.getTembossbranch().getTotalproses() + obj.getTotaldata());
												obj.getTembossbranch().setTotalos(
														obj.getTembossbranch().getTotalos() - obj.getTotaldata());
												if (obj.getTembossbranch().getTotalos().equals(0))
													obj.getTembossbranch().setStatus(AppUtils.STATUS_PROSES);
												new TembossbranchDAO().save(session, obj.getTembossbranch());
											}

											data.setStatus(AppUtils.STATUS_DERIVATIF_ORDERPERSOAPPROVAL);
											oDao.save(session, data);
											transaction.commit();

											needsPageUpdate = true;
											refreshModel(pageStartNumber);
											BindUtils.postNotifyChange(null, null, DerivatifListVm.this,
													"pageTotalSize");
											Messagebox.show("Proses pembuatan manifest perso berhasil",
													WebApps.getCurrent().getAppName(), Messagebox.OK,
													Messagebox.INFORMATION);
										} catch (HibernateException e) {
											e.printStackTrace();
										} catch (Exception e) {
											e.printStackTrace();
										} finally {
											session.close();
										}

										try {

											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persoapproval'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());

											mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persoorder'");
											NotifHandler.delete(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									if (mapProducttype.size() > 0) {
										try {
											session = StoreHibernateUtil.openSession();
											transaction = session.beginTransaction();
											for (Entry<Integer, Mproducttype> entry : mapProducttype.entrySet()) {
												Mproducttype mproducttype = entry.getValue();
												mproducttypeDao.save(session, mproducttype);
											}
											transaction.commit();
										} catch (Exception e) {
											e.printStackTrace();
										} finally {
											session.close();
										}
									}
								}
							}
						}
					});
		}
	}

	@Command
	@NotifyChange("*")
	public void doApproval() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			if (action != null && action.length() > 0) {
				if (action.length() > 0 || action.equals(AppUtils.STATUS_DECLINE) && decisionmemo != null
						&& decisionmemo.trim().length() > 0) {
					if (arg.equals("persoapproval"))
						doApprovalPerso();
					else if (arg.equals("invoutapproval"))
						doApprovalOutgoing();
				} else
					Messagebox.show("Anda harus mengisi alasan decline pada field Memo", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
			} else
				Messagebox.show("Silahkan pilih action", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doApprovalPerso() {
		Session session = null;
		Transaction transaction = null;
		for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			Tderivatif data = entry.getValue();
			List<Tperso> objSelected = new ArrayList<Tperso>();
			try {
				objSelected = tpersoDao.listByFilter(
						"tderivatifproduct.tderivatif.tderivatifpk = " + data.getTderivatifpk(), "tderivatifproductpk");
				for (Tperso obj : objSelected) {
					obj.setStatus(action.equals(AppUtils.STATUS_APPROVED) ? AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL
							: AppUtils.STATUS_PERSO_PERSODECLINE);
					obj.setDecisionby(oUser.getUserid());
					obj.setDecisiontime(new Date());
					tpersoDao.save(session, obj);

					if (action.equals(AppUtils.STATUS_APPROVED)) {
						Toutgoing toutgoing = new Toutgoing();
						toutgoing
								.setOutgoingid(new TcounterengineDAO().generateCounter(AppUtils.CE_INVENTORY_OUTGOING));
						toutgoing.setTperso(obj);
						toutgoing.setMproduct(obj.getMproduct());
						toutgoing.setEntryby(obj.getPersostartby());
						toutgoing.setEntrytime(new Date());
						toutgoing.setItemqty(obj.getTotaldata());
						toutgoing.setLastupdated(new Date());
						toutgoing.setMemo(obj.getMemo());
						toutgoing.setProductgroup(AppUtils.PRODUCTGROUP_CARDPHOTO);
						toutgoing.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL);
						toutgoing.setUpdatedby(oUser.getUserid());
						toutgoingDao.save(session, toutgoing);

						Mmenu mmenu = new MmenuDAO().findByFilter(
								"menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'approval'");
						NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO,
								oUser.getMbranch().getBranchlevel());
					}

					Mmenu mmenu = new MmenuDAO()
							.findByFilter("menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persoapproval'");
					NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO,
							oUser.getMbranch().getBranchlevel());
				}

				data.setStatus(
						action.equals(AppUtils.STATUS_APPROVED) ? AppUtils.STATUS_DERIVATIF_ORDERPERSOINVENTORYAPPROVAL
								: AppUtils.STATUS_DERIVATIF_ORDERPERSODECLINE);
				oDao.save(session, data);

				if (!action.equals(AppUtils.STATUS_APPROVED)) {
					if (data.getMproduct() != null) {
						Mproducttype mproducttype = data.getMproduct().getMproducttype();
						mproducttype.setStockreserved(mproducttype.getStockreserved() - data.getTotaldata());
						mproducttypeDao.save(session, mproducttype);
					}
				}

				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				e.printStackTrace();
			} finally {
				session.close();
			}
		}

		if (action.equals(AppUtils.STATUS_APPROVED))
			Clients.showNotification("Proses approval order berhasil", "info", null, "middle_center", 3000);
		else
			Clients.showNotification("Proses decline data order berhasil", "info", null, "middle_center", 3000);
		doReset();
	}

	@Command
	@NotifyChange("*")
	public void doApprovalOutgoing() {
		Session session = null;
		Transaction transaction = null;

		for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			Tderivatif data = entry.getValue();
			List<Toutgoing> outList = new ArrayList<Toutgoing>();
			try {
				List<Tperso> objSelected = tpersoDao.listByFilter(
						"tderivatifproduct.tderivatif.tderivatifpk = " + data.getTderivatifpk(), "tderivatifproductpk");
				for (Tperso obj : objSelected) {
					outList = toutgoingDao.listByFilter("tpersofk = " + obj.getTpersopk(), "toutgoingpk");
					for (Toutgoing toutgoing : outList) {
						toutgoing.setStatus(
								action.equals(AppUtils.STATUS_APPROVED) ? AppUtils.STATUS_INVENTORY_OUTGOINGAPPROVED
										: AppUtils.STATUS_INVENTORY_OUTGOINGDECLINE);
						toutgoing.setDecisionby(oUser.getUserid());
						toutgoing.setDecisiontime(new Date());
						toutgoingDao.save(session, toutgoing);
					}

					obj.setStatus(action.equals(AppUtils.STATUS_APPROVED) ? AppUtils.STATUS_PERSO_PRODUKSI
							: AppUtils.STATUS_PERSO_OUTGOINGDECLINE);
					tpersoDao.save(session, obj);
				}

				data.setStatus(action.equals(AppUtils.STATUS_APPROVED) ? AppUtils.STATUS_DERIVATIF_PRODUKSI
						: AppUtils.STATUS_DERIVATIF_ORDERPERSO);
				oDao.save(session, data);

				Mproducttype mproducttype = data.getMproduct().getMproducttype();
				mproducttype.setStockreserved(mproducttype.getStockreserved() - data.getTotaldata());
				if (action.equals(AppUtils.STATUS_APPROVED)) {
					mproducttype
							.setLaststock(data.getMproduct().getMproducttype().getLaststock() - data.getTotaldata());
				}
				mproducttypeDao.save(session, mproducttype);

				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				e.printStackTrace();
			} finally {
				session.close();
			}

			try {
				if (action.equals(AppUtils.STATUS_APPROVED)) {
					Mmenu mmenu = new MmenuDAO()
							.findByFilter("menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persolist'");
					NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO,
							oUser.getMbranch().getBranchlevel());
				}
				Mmenu mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'approval'");
				NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO,
						oUser.getMbranch().getBranchlevel());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (action.equals(AppUtils.STATUS_APPROVED))
			Clients.showNotification("Proses approval order berhasil", "info", null, "middle_center", 3000);
		else
			Clients.showNotification("Proses decline data order berhasil", "info", null, "middle_center", 3000);
		doReset();
	}

	@Command
	public void doBon() {
		try {
			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				Map<String, Object> map = new HashMap<>();
				map.put("process", "Cetak Bon Kartu");
				Window win = (Window) Executions.createComponents("/view/export/exportformat.zul", null, map);
				win.setClosable(true);
				win.doModal();
				win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getData() != null) {
							@SuppressWarnings("unchecked")
							Map<String, Object> map = (Map<String, Object>) event.getData();
							String format = (String) map.get("format");
							List<Tperso> objList = new ArrayList<>();
							for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
								Tderivatif obj = entry.getValue();
								objList.addAll(tpersoDao.listByFilter(
										"tderivatifproduct.tderivatif.tderivatifpk = " + obj.getTderivatifpk(),
										"tderivatifproductpk"));
							}
							PersoPrintHandler.doBonPrint(objList, format);
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doPersoManifest() {
		try {
			if (mapData.size() == 0) {
				Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				List<Tperso> objList = new ArrayList<>();
				for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
					Tderivatif obj = entry.getValue();
					objList.addAll(tpersoDao.listByFilter(
							"tderivatifproduct.tderivatif.tderivatifpk = " + obj.getTderivatifpk(),
							"tderivatifproductpk"));
				}

				Window win = (Window) Executions.createComponents("/view/perso/persomanifestprint.zul", null, null);
				win.setClosable(true);
				win.doModal();
				win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@SuppressWarnings("unchecked")
					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getData() != null) {
							try {
								Map<String, Object> map = (Map<String, Object>) event.getData();
								String format = (String) map.get("format");
								String operators = (String) map.get("operators");

								PersoPrintHandler.doManifestPrint(objList, format, operators);

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doPersoDone() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			boolean isValid = true;
			for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
				Tderivatif obj = entry.getValue();
				if (!obj.getStatus().equals(AppUtils.STATUS_DERIVATIF_PRODUKSI)) {
					isValid = false;
					Messagebox.show(
							"Proses update status tidak bisa dilakukan karena terdapat data dengan status bukan produksi. \nSilahkan periksa kembali data-data yang anda pilih",
							WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
					break;
				}
			}

			if (isValid) {
				Messagebox.show("Anda ingin update status berhasil perso?", "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									Session session = null;
									Transaction transaction = null;
									try {
										boolean isError = false;
										String strError = "";
										for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
											Tderivatif obj = entry.getValue();
											session = StoreHibernateUtil.openSession();
											transaction = session.beginTransaction();
											try {
												for (Tperso objForm : tpersoDao.listNativeDrvByFilter(
														"tderivatiffk = " + obj.getTderivatifpk(),
														"tderivatifproductpk")) {
													objForm.setPersofinishby(oUser.getUserid());
													objForm.setPersofinishtime(new Date());
													objForm.setStatus(AppUtils.STATUS_PERSO_DONE);
													objForm.setIsgetallpaket("N");
													tpersoDao.save(session, objForm);

													for (Tpersodata data : tpersodataDao.listByFilter(
															"tpersofk = " + objForm.getTpersopk(), "tpersodatapk")) {
														data.setStatus(AppUtils.STATUS_PERSO_DONE);
														data.setIsgetpaket("N");
														tpersodataDao.save(session, data);
													}
												}
												obj.setStatus(AppUtils.STATUS_DERIVATIF_ORDERPAKET);
												oDao.save(session, obj);

												transaction.commit();
											} catch (HibernateException e) {
												transaction.rollback();
												isError = true;
												if (strError.length() > 0)
													strError += ". \n";
												strError += e.getMessage();
												e.printStackTrace();
											} catch (Exception e) {
												transaction.rollback();
												isError = true;
												if (strError.length() > 0)
													strError += ". \n";
												strError += e.getMessage();
												e.printStackTrace();
											} finally {
												session.close();
											}

											try {
												Mmenu mmenu = new MmenuDAO().findByFilter(
														"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketorder'");
												NotifHandler.doNotif(mmenu, oUser.getMbranch(),
														AppUtils.PRODUCTGROUP_CARDPHOTO,
														oUser.getMbranch().getBranchlevel());

												mmenu = new MmenuDAO().findByFilter(
														"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persolist'");
												NotifHandler.delete(mmenu, oUser.getMbranch(),
														AppUtils.PRODUCTGROUP_CARDPHOTO,
														oUser.getMbranch().getBranchlevel());
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										if (isError)
											Messagebox.show(strError, WebApps.getCurrent().getAppName(), Messagebox.OK,
													Messagebox.ERROR);
										else
											Messagebox.show("Proses update status perso selesai",
													WebApps.getCurrent().getAppName(), Messagebox.OK,
													Messagebox.INFORMATION);
										needsPageUpdate = false;
										refreshModel(pageStartNumber);
										doReset();
										BindUtils.postNotifyChange(null, null, DerivatifListVm.this, "*");
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}

						});
			}
		}
	}

	@Command
	public void doOrderPaket() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			try {
				Messagebox.show("Anda ingin membuat manifest paket?", "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

							@SuppressWarnings("unused")
							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									Session session = null;
									Transaction transaction = null;
									boolean isError = false;
									String strError = "";
									for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
										Tderivatif obj = entry.getValue();
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										try {
											for (Tperso tperso : tpersoDao.listByFilter(
													"tderivatifproduct.tderivatif.tderivatifpk = "
															+ obj.getTderivatifpk(),
													"tderivatifproduct.tderivatifproductpk")) {
												Tpaket paket = new Tpaket();
												paket.setTperso(tperso);
												paket.setMproduct(tperso.getMproduct());
												paket.setTderivatifproduct(tperso.getTderivatifproduct());
												paket.setPaketid(new TcounterengineDAO()
														.generateYearMonthCounter(AppUtils.CE_PAKET));
												paket.setProductgroup(AppUtils.PRODUCTGROUP_CARDPHOTO);
												paket.setTotaldata(tperso.getTotaldata());
												paket.setTotaldone(0);
												paket.setOrderdate(tperso.getOrderdate());
												paket.setBranchpool(oUser.getMbranch().getBranchid());
												paket.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
												paket.setProcessedby(oUser.getUserid());
												paket.setProcesstime(new Date());
												tpaketDao.save(session, paket);

												String nopaket = "";
												for (Tpersodata data : tpersodataDao.listByFilter("tperso.tpersopk = "
														+ tperso.getTpersopk() + " and isgetpaket = 'N'",
														"tpersodatapk")) {
													Tpaketdata paketdata = new Tpaketdata();
													paketdata.setTpaket(paket);
													if (data.getTembossbranch() != null)
														paketdata.setTembossbranch(data.getTembossbranch());
													paketdata.setNopaket(new TcounterengineDAO().generateNopaket());
													paketdata.setProductgroup(paket.getProductgroup());
													paketdata.setMbranch(data.getMbranch());
													paketdata.setOrderdate(data.getTperso().getOrderdate());
													paketdata.setQuantity(data.getQuantity());
													paketdata.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
													paketdata.setIsdlv("");
													paketdata.setPaketstartby(oUser.getUserid());
													paketdata.setPaketstarttime(new Date());
													tpaketdataDao.save(session, paketdata);

													data.setIsgetpaket("Y");
													tpersodataDao.save(session, data);

													nopaket = paketdata.getNopaket();
												}

												tperso.setTotalpaket(tperso.getTotaldata());
												tperso.setIsgetallpaket("Y");
												tpersoDao.save(session, tperso);

												tperso.getTderivatifproduct().setNopaket(nopaket);
												tperso.getTderivatifproduct().setPakettime(new Date());
												tderivatifproductDao.save(session, tperso.getTderivatifproduct());
											}

											obj.setStatus(AppUtils.STATUS_DERIVATIF_PAKET);
											obj.setDlvstarttime(new Date());
											oDao.save(session, obj);
											transaction.commit();
										} catch (HibernateException e) {
											transaction.rollback();
											isError = true;
											if (strError.length() > 0)
												strError += ", ";
											strError += e.getMessage();
											e.printStackTrace();
										} catch (Exception e) {
											transaction.rollback();
											isError = true;
											if (strError.length() > 0)
												strError += ", ";
											strError += e.getMessage();
											e.printStackTrace();
										} finally {
											session.close();
										}

										try {
											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketlist'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());

											mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketorder'");
											NotifHandler.delete(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									if (isError)
										Messagebox.show("Proses pembuatan manifest paket gagal. \n" + strError,
												WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
									else {
										Messagebox.show("Proses pembuatan manifest paket berhasil",
												WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.INFORMATION);
										doReset();
										BindUtils.postNotifyChange(null, null, DerivatifListVm.this, "*");
									}
								}
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	public void doPaketLabel() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			try {
				List<Tpaketdata> objList = new ArrayList<>();
				for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
					Tderivatif data = entry.getValue();
					objList.addAll(tpaketdataDao.listByFilter(
							"tpaket.tderivatifproduct.tderivatif.tderivatifpk = " + data.getTderivatifpk(),
							"tpaket.tderivatifproduct.tderivatifproductpk"));
				}

				PaketManifestHandler.doLabelPrint(objList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	public void doPaketDone() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			boolean isValid = true;
			for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
				Tderivatif obj = entry.getValue();
				if (!obj.getStatus().equals(AppUtils.STATUS_DERIVATIF_PAKET)) {
					isValid = false;
					Messagebox.show(
							"Proses update status tidak bisa \ndilakukan karena terdapat data \ndengan status bukan proses paket. \nSilahkan periksa kembali \ndata-data yang anda pilih",
							"Info", Messagebox.OK, Messagebox.INFORMATION);
					break;
				}
			}
			if (isValid) {
				Messagebox.show("Anda ingin update status done?", "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL,
						Messagebox.QUESTION, new EventListener<Event>() {

							@SuppressWarnings("unused")
							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									Session session = null;
									Transaction transaction = null;
									int totaldone;
									for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
										Tderivatif obj = entry.getValue();
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										try {
											for (Tpaket objPaket : tpaketDao.listByFilter(
													"tderivatifproduct.tderivatif.tderivatifpk = "
															+ obj.getTderivatifpk() + " and status = '"
															+ AppUtils.STATUS_DELIVERY_PAKETPROSES + "'",
													"tderivatifproduct.tderivatifproductpk")) {
												objPaket.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);

												totaldone = 0;
												List<Tpaketdata> listPaketdata = tpaketdataDao.listByFilter(
														"tpaket.tpaketpk = " + objPaket.getTpaketpk()
																+ " and status = '"
																+ AppUtils.STATUS_DELIVERY_PAKETPROSES + "'",
														"tpaketdatapk");
												for (Tpaketdata data : listPaketdata) {
													data.setPaketfinishby(oUser.getUserid());
													data.setPaketfinishtime(new Date());
													data.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
													data.setIsdlv("N");
													tpaketdataDao.save(session, data);

													totaldone = totaldone + data.getQuantity();

													Mmenu mmenu = new MmenuDAO().findByFilter(
															"menupath = '/view/delivery/deliveryjob.zul'");
													NotifHandler.doNotif(mmenu, oUser.getMbranch(),
															AppUtils.PRODUCTGROUP_CARDPHOTO,
															oUser.getMbranch().getBranchlevel());
												}

												objPaket.setTotaldone(objPaket.getTotaldone() + totaldone);
												tpaketDao.save(session, objPaket);
											}

											obj.setDlvstarttime(new Date());
											obj.setStatus(AppUtils.STATUS_DERIVATIF_ORDERDELIVERY);
											oDao.save(session, obj);

											transaction.commit();
										} catch (Exception e) {
											transaction.rollback();
											e.printStackTrace();
										} finally {
											session.close();
										}

										try {
											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketlist'");
											NotifHandler.delete(mmenu, oUser.getMbranch(),
													AppUtils.PRODUCTGROUP_CARDPHOTO,
													oUser.getMbranch().getBranchlevel());
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									Messagebox.show("Proses update status done paket \nselesai",
											WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
									doReset();
									BindUtils.postNotifyChange(null, null, DerivatifListVm.this, "*");
								}
							}
						});
			}
		}
	}

	@Command
	public void doPrintLetter() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			try {
				List<Tderivatif> objList = new ArrayList<>();
				for (Entry<Integer, Tderivatif> entry : mapData.entrySet()) {
					Tderivatif obj = entry.getValue();
					objList.add(obj);
				}
				doLetterGenerator(objList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void doLetterGenerator(List<Tderivatif> objList) throws Exception {
		try {
			String filename = "LETTER" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + ".pdf";
			Font font = new Font(Font.FontFamily.HELVETICA, 10);
			Font fonttable = new Font(Font.FontFamily.HELVETICA, 9);
			String output = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + filename);
			Document document = new Document(new Rectangle(PageSize.A4));
			PdfWriter.getInstance(document, new FileOutputStream(output));
			document.open();

			for (Tderivatif obj : objList) {
				if (obj.getTdelivery() != null) {
					Tdelivery delivery = new TdeliveryDAO()
							.findByFilter("tdeliverypk = " + obj.getTdelivery().getTdeliverypk());
					LetterDerivatifGenerator.doGenerate(document, font, fonttable, delivery);
				}
			}
			document.close();

			Filedownload.save(
					new File(Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + filename)),
					"application/pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		try {
			if (year != null && month != null) {
				filter = "extract(year from orderdate) = " + year + " and " + "extract(month from orderdate) = "
						+ month;

				if (arg.equals("getdata")) {
					filter += " and status >= " + AppUtils.STATUS_DERIVATIF_GETDATA;
				} else if (arg.equals("scan")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_SCAN;
				} else if (arg.equals("crop")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_CROP;
				} else if (arg.equals("merge")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_MERGE;
				} else if (arg.equals("persoorder")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_ORDERPERSO;
				} else if (arg.equals("persoapproval")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_ORDERPERSOAPPROVAL;
				} else if (arg.equals("invoutapproval")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_ORDERPERSOINVENTORYAPPROVAL;
				} else if (arg.equals("persolist")) {
					filter += " and status > " + AppUtils.STATUS_DERIVATIF_ORDERPERSOAPPROVAL;
				} else if (arg.equals("paketorder")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_ORDERPAKET;
				} else if (arg.equals("paketlist")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_PAKET;
				} else if (arg.equals("dlvorder")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_ORDERDELIVERY;
				} else if (arg.equals("dlvlist")) {
					filter += " and status = " + AppUtils.STATUS_DERIVATIF_DELIVERY;
				}

				if (orderno != null && orderno.length() > 0) {
					filter += " and orderno like '%" + orderno.trim().toUpperCase() + "%'";
				}
				if (productcode != null && productcode.length() > 0) {
					filter += " and mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
				}
				if (orderno != null && orderno.length() > 0) {
					filter += " and orderno like '%" + orderno.trim().toUpperCase() + "%'";
				}
				if (branchname != null && branchname.length() > 0) {
					filter += "and mbranch.branchname like '%" + branchname.trim().toUpperCase() + "%'";
				}
				if (filename != null && filename.length() > 0) {
					filter += "and filename like '%" + filename.trim().toUpperCase() + "%'";
				}

				objList = oDao.listByFilter(filter, "orderdate");
				for (Tderivatif objDer : objList) {
					totaldata = totaldata + objDer.getTotaldata();
					totalreject = totalreject + objDer.getTotalreject();
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
		totaldata = 0;
		totalreject = 0;
		dateverify = null;
		memo = "";
		orderno = "";
		branchname = "";
		objList = new ArrayList<>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tderivatifpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TderivatifListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
				cell.setCellValue("Daftar Order Kartu Berfoto");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "No Order", "Kode Produk", "Jenis Produk", "Cabang", "Tgl Order",
						"Total Data", "Status", "Memo" });
				no = 2;
				int totaldata = 0;
				for (Tderivatif data : objList) {
					datamap.put(no, new Object[] { no - 1, data.getOrderno(),
							data.getMproduct() != null ? data.getMproduct().getProductcode() : "",
							data.getMproduct() != null ? data.getMproduct().getProductname() : "",
							data.getMbranch().getBranchname(), dateLocalFormatter.format(data.getOrderdate()),
							data.getTotaldata(), AppData.getStatusDerivatifLabel(data.getStatus()), data.getMemo() });
					no++;
					totaldata = totaldata + data.getTotaldata();
				}
				datamap.put(no, new Object[] { "", "TOTAL", "", "", "", "", totaldata, "", "" });
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 3) {
						XSSFCellStyle styleHeader = workbook.createCellStyle();
						styleHeader.setBorderTop(BorderStyle.MEDIUM);
						styleHeader.setBorderBottom(BorderStyle.MEDIUM);
						styleHeader.setBorderLeft(BorderStyle.MEDIUM);
						styleHeader.setBorderRight(BorderStyle.MEDIUM);
						styleHeader.setFillForegroundColor(IndexedColors.CORAL.getIndex());
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
				String filename = "CAPTION_KARTU_BERFOTO" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Date getProcessdate() {
		return processdate;
	}

	public void setProcessdate(Date processdate) {
		this.processdate = processdate;
	}

	public String getDecisionmemo() {
		return decisionmemo;
	}

	public void setDecisionmemo(String decisionmemo) {
		this.decisionmemo = decisionmemo;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Date getDateverify() {
		return dateverify;
	}

	public void setDateverify(Date dateverify) {
		this.dateverify = dateverify;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Integer getTotalreject() {
		return totalreject;
	}

	public void setTotalreject(Integer totalreject) {
		this.totalreject = totalreject;
	}

}
