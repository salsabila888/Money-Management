package com.sdd.caption.viewmodel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Footer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.dao.TderivatifdataDAO;
import com.sdd.caption.dao.TderivatifproductDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.domain.Tderivatifdata;
import com.sdd.caption.domain.Tderivatifproduct;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TembossdataListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DerivatifGetDataVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private Session session;
	private Transaction transaction;

	private SimpleDateFormat periodLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	private Tderivatif obj;
	private TembossdataListModel model;

	private TderivatifDAO oDao = new TderivatifDAO();
	private TderivatifproductDAO derivatifproductDao = new TderivatifproductDAO();
	private TderivatifdataDAO derivatifdataDao = new TderivatifdataDAO();
	private TembossdataDAO tedDao = new TembossdataDAO();

	private List<String> cardnoList = new ArrayList<>();

	private List<Tderivatifdata> listTdata = new ArrayList<>();
	private List<Tembossdata> getdataList = new ArrayList<>();
	private List<Tembossdata> datafailList = new ArrayList<>();
	private List<Tembossdata> dataInsertList = new ArrayList<>();
	private List<Tembossdata> dataDelList = new ArrayList<>();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Date orderdate;
	private Integer totaldata;
	private Integer pageno;
	private String cardno;
	private String nama;
	private Integer totalselected;
	private String filename;
	private Media media;
	private String type;
	private Integer totalupload;
	private Integer inserted;
	private Integer failed;

	private Map<Integer, Tembossdata> mapData = new HashMap<>();

	@Wire
	private Paging paging;
	@Wire
	private Grid grid, gridGetdata;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Window winBranchGetData;
	@Wire
	private Row rowUpload, rowOrderdate;
	@Wire
	private Footer footUpload, footManual;
	@Wire
	private Div divManual;
	@Wire
	private Groupbox gbHasilUpload;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tderivatif obj,
			@ExecutionArgParam("pageno") Integer pageno) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		
		this.obj = obj;
		this.pageno = pageno;

		doReset();

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		grid.setRowRenderer(new RowRenderer<Tembossdata>() {

			@Override
			public void render(final Row row, final Tembossdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tembossdata ted = (Tembossdata) checked.getAttribute("obj");
						if (checked.isChecked()) {
							if (ted.getMproduct() == null) {
								checked.setChecked(false);
								Messagebox.show("Data belum bisa dipilih karna jenis produk belum terdaftar", "Info",
										Messagebox.OK, Messagebox.INFORMATION);
							} else if (cardnoList.contains(data.getCardno())) {
								checked.setChecked(false);
								Messagebox.show("Data sudah terdaftar GET DATA", "Info", Messagebox.OK,
										Messagebox.INFORMATION);
							} else if (cardnoList.size() >= obj.getTotaldata()) {
								checked.setChecked(false);
								Messagebox.show("Jumlah data yang dipilih sudah melebihi jumlah order", "Info",
										Messagebox.OK, Messagebox.INFORMATION);
							} else {
								mapData.put(data.getTembossdatapk(), data);
								cardnoList.add(data.getCardno());
								System.out.println("total : " + cardnoList.size() + ", order : " + obj.getTotaldata());
							}
						} else {
							mapData.remove(data.getTembossdatapk());
							cardnoList.remove(data.getCardno());
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, DerivatifGetDataVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, DerivatifGetDataVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTembossdatapk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getCardno()));
				row.getChildren().add(new Label(data.getNameoncard()));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(new Label(periodLocalFormatter.format(data.getOrderdate())));
			}
		});

		gridGetdata.setRowRenderer(new RowRenderer<Tembossdata>() {

			@Override
			public void render(final Row row, final Tembossdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getCardno()));
				row.getChildren().add(new Label(data.getNameoncard()));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(new Label(periodLocalFormatter.format(data.getOrderdate())));

				Button btnCancel = new Button("Cancel");
				btnCancel.setSclass("btn btn-danger btn-sm");
				btnCancel.setAutodisable("self");
				btnCancel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
								Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

									@Override
									public void onEvent(Event event) throws Exception {
										if (event.getName().equals("onOK")) {
											getdataList.remove(data);
											if (data.getTembossdatapk() != null) {
												dataDelList.add(data);
											}
											if (dataInsertList.contains(data)) {
												dataInsertList.remove(data);
											}

											cardnoList.remove(data.getCardno());
											gridGetdata.setModel(new ListModelList<>(getdataList));
											totaldata = getdataList.size();
											BindUtils.postNotifyChange(null, null, DerivatifGetDataVm.this,
													"totaldata");
										}
									}
								});
					}
				});

				row.getChildren().add(btnCancel);

				gridGetdata.getRows().insertBefore(row, gridGetdata.getRows().getFirstChild());
			}
		});
	}

	@Command
	@NotifyChange("*")
	public void doView() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("obj", obj);
		map.put("listFail", datafailList);
		map.put("isFail", "Y");
		Window win = (Window) Executions.createComponents("/view/derivatif/derivatifdatafail.zul", null, map);
		win.setWidth("80%");
		win.setClosable(true);
		win.doModal();
	}

	@Command
	public void doCheckedtype() {
		if (type.equals("M")) {
			rowOrderdate.setVisible(true);
			footManual.setVisible(true);
			grid.setVisible(true);
			divManual.setVisible(true);

			rowUpload.setVisible(false);
			footUpload.setVisible(false);
			gbHasilUpload.setVisible(false);
		} else {
			rowOrderdate.setVisible(false);
			footManual.setVisible(false);
			grid.setVisible(false);
			divManual.setVisible(false);

			rowUpload.setVisible(true);
			footUpload.setVisible(true);
			gbHasilUpload.setVisible(true);
		}
	}

	@Command
	@NotifyChange("filename")
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			filename = media.getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doUpload() {
		if (media == null) {
			Messagebox.show("Silahkan upload file untuk proses get data", WebApps.getCurrent().getAppName(),
					Messagebox.OK, Messagebox.INFORMATION);
		} else {
			datafailList = new ArrayList<>();
			BufferedReader reader = null;
			try {
				if (media.isBinary()) {
					reader = new BufferedReader(new InputStreamReader(media.getStreamData()));
				} else {
					reader = new BufferedReader(media.getReaderData());
				}

				String line = "";
				String cardno = "";
				String keterangan = "";
				while ((line = reader.readLine()) != null) {
					cardno = line.substring(1, 20).trim();
					Tembossdata tod = null;
					List<Tembossdata> listCardno = tedDao.listByFilter("cardno = '" + cardno + "'",
							"tembossdatapk desc");
					if (listCardno.size() > 0) {
						tod = listCardno.get(0);
					}
					if (tod != null) {
						System.out.println("Emboss : " + tod.getCardno());
						if (cardnoList.size() <= obj.getTotaldata()) {
							if (tod.getMbranch() != null) {
								if (tod.getMbranch().getMbranchpk().equals(obj.getMbranch().getMbranchpk())) {
									if (tod.getMproduct() != null) {
										if (tod.getMproduct().getMproductpk()
												.equals(obj.getMproduct().getMproductpk())) {
											if (tod.getTembossproduct().getIsneeddoc().equals("Y")) {
												if (cardnoList.contains(tod.getCardno())) {
													keterangan = "Duplicate data";
													tod.setNameoncard(keterangan);
													datafailList.add(tod);
													failed++;
												} else {
													System.out.println("Cardno Success : " + cardno);
													getdataList.add(tod);
													dataInsertList.add(tod);
													cardnoList.add(tod.getCardno());
													inserted++;
												}
											} else {
												keterangan = "Data bukan tipe kartu derivatif.";
												tod.setNameoncard(keterangan);
												datafailList.add(tod);
												failed++;
											}
										} else {
											keterangan = "Jenis produk pada data tidak sama dengan jenis produk order.";
											tod.setNameoncard(keterangan);
											datafailList.add(tod);
											failed++;
										}
									} else {
										keterangan = "Parameter produk belum terdaftar.";
										tod.setNameoncard(keterangan);
										datafailList.add(tod);
										failed++;
									}
								} else {
									keterangan = "Cabang pada data tidak sama dengan cabang order.";
									tod.setNameoncard(keterangan);
									datafailList.add(tod);
									failed++;
								}
							} else {
								keterangan = "Parameter Cabang belum terdaftar.";
								tod.setNameoncard(keterangan);
								datafailList.add(tod);
								failed++;
							}
						} else {
							keterangan = "Data sudah melebihi jumlah order.";
							tod.setNameoncard(keterangan);
							datafailList.add(tod);
							failed++;
						}
					} else {
						keterangan = "Data tidak ditemukan.";
						tod = new Tembossdata();
						tod.setCardno(cardno);
						tod.setNameoncard(keterangan);
						datafailList.add(tod);
						failed++;
					}
				}
				System.out.println("Jumlah Cardno : " + cardnoList.size());

				totalupload = inserted + failed;
				gridGetdata.setModel(new ListModelList<>(getdataList));
				totaldata = getdataList.size();
				gridGetdata.setFocus(true);

				media = null;
				BindUtils.postNotifyChange(null, null, DerivatifGetDataVm.this, "totaldata");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			int statusfail = 0;
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tembossdata ted = (Tembossdata) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						if (ted.getMproduct() == null || cardnoList.contains(ted.getCardno())
								|| cardnoList.size() >= obj.getTotaldata()) {
							chk.setChecked(false);
							statusfail++;
						} else {
							chk.setChecked(true);
							mapData.put(ted.getTembossdatapk(), ted);
							cardnoList.add(ted.getCardno());
						}
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getTotaldata());
						cardnoList.remove(ted.getCardno());
					}
				}
			}
			if (statusfail > 0) {
				Messagebox.show("Ada beberapa data belum bisa dipilih mohon diperiksa kembali", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
			totalselected = mapData.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("totaldata")
	public void doSave() {
		try {
			if (mapData.size() > 0) {
				if (mapData.size() <= obj.getTotaldata()) {
					for (Entry<Integer, Tembossdata> entry : mapData.entrySet()) {
						Tembossdata data = entry.getValue();
						getdataList.add(data);
						dataInsertList.add(data);
					}
					gridGetdata.setModel(new ListModelList<>(getdataList));
					totaldata = getdataList.size();
					gridGetdata.setFocus(true);
				} else {
					Messagebox.show("Jumlah data yang dipilih melebihi jumlah order", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Tidak ada data yang dipilih", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
			mapData = new HashMap<>();
			doSearch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSubmit() {
		if (totaldata.equals(0)) {
			Messagebox.show("Tidak ada data untuk di Submit", WebApps.getCurrent().getAppName(), Messagebox.OK,
					Messagebox.EXCLAMATION);
		} else {
			String message = "";
			if (cardnoList.size() < obj.getTotaldata())
				message = "Jumlah data yang dipilih kurang dari jumlah order. Apakah anda ingin tetap melanjutkan ke proses selanjutnya?.";
			else
				message = "Apakah anda ingin menyelesaikan proses get data dan melanjutkan ke proses selanjutnya? Pastikan proses get data untuk cabang "
						+ obj.getMbranch().getBranchname() + " dengan nomor surat " + obj.getOrderno()
						+ " sudah selesai semua.";
			Messagebox.show(message, "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
					new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								session = StoreHibernateUtil.openSession();
								transaction = session.beginTransaction();
								try {
									for (Tembossdata data : dataDelList) {
										Tderivatifdata tdd = derivatifdataDao
												.findByFilter("tembossdata.tembossdatapk = " + data.getTembossdatapk()
														+ " and tderivatif.tderivatifpk = " + obj.getTderivatifpk()
														+ " and status = '" + AppUtils.STATUS_PROSES + "'");
										if (tdd != null) {
											derivatifdataDao.delete(session, tdd);
										}
									}

									for (Tembossdata data : dataInsertList) {
										Tderivatifdata tdd = new Tderivatifdata();
										tdd.setCardno(data.getCardno());
										tdd.setOrderdate(data.getOrderdate());
										tdd.setTderivatif(obj);
										tdd.setTembossdata(data);
										tdd.setStatus(AppUtils.STATUS_PROSES);
										listTdata.add(tdd);
									}

									Tderivatifproduct tderivatifproduct = null;
									List<Tderivatifproduct> listTproduct = new ArrayList<>();

									Date orderdate = null;
									Integer totalproduct = 0;
									Collections.sort(listTdata, Tderivatifdata.dateComparator);
									for (Tderivatifdata tod : listTdata) {
										if (!tod.getTembossdata().getOrderdate().equals(orderdate)) {
											if (tderivatifproduct != null) {
												tderivatifproduct.setTotaldata(totalproduct);
												listTproduct.add(tderivatifproduct);
											}

											tderivatifproduct = new Tderivatifproduct();
											tderivatifproduct.setTderivatif(obj);
											tderivatifproduct.setOrderdate(tod.getOrderdate());
											tderivatifproduct.setEntrytime(new Date());
											tderivatifproduct.setMproduct(tod.getTembossdata().getMproduct());
											tderivatifproduct.setTembossbranch(tod.getTembossdata().getTembossbranch());

											totalproduct = 0;
										}

										orderdate = tod.getOrderdate();
										totalproduct++;
										tod.setTderivatifproduct(tderivatifproduct);
									}
									if (tderivatifproduct != null) {
										tderivatifproduct.setTotaldata(totalproduct);
										listTproduct.add(tderivatifproduct);
									}

									for (Tderivatifproduct tdp : listTproduct) {
										derivatifproductDao.save(session, tdp);
									}

									for (Tderivatifdata ted : listTdata) {
										derivatifdataDao.save(session, ted);
									}

									if (getdataList.size() < obj.getTotaldata()) {
										obj.setTotaladj(getdataList.size());
										obj.setStatus(16);
									} else
										obj.setStatus(obj.getStatus() + 1);
									oDao.save(session, obj);
									
									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/derivatif/derivatiflist.zul' and menuparamvalue = 'scan'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO, oUser.getMbranch().getBranchlevel());
									
									mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/derivatif/derivatiflist.zul' and menuparamvalue = 'getdata'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO,
											oUser.getMbranch().getBranchlevel());
									
									transaction.commit();

									Map<String, Object> map = new HashMap<>();
									map.put("pageno", pageno);
									Event closeEvent = new Event("onClose", winBranchGetData, map);
									Events.postEvent(closeEvent);

									Clients.showNotification("Proses get data selesai", "info", null, "middle_center",
											3000);
								} catch (Exception e) {
									transaction.rollback();
									e.printStackTrace();
								} finally {
									session.close();
								}
								
								try {
									if (listTdata.size() < obj.getTotaldata()) {
										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/derivatif/derivatifapprovaladj.zul'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO,
												oUser.getMbranch().getBranchlevel());
									} else {
										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'scan'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO,
												oUser.getMbranch().getBranchlevel());
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (orderdate != null) {
			filter = "mbranchfk = " + obj.getMbranch().getMbranchpk() + " and tembossdata.mproductfk = "
					+ obj.getMproduct().getMproductpk() + " and isneeddoc = 'Y'";
			if (cardno != null && cardno.length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "tembossdata.cardno like '%" + cardno.trim().toUpperCase() + "%'";
			}
			if (nama != null && nama.length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "tembossdata.nameoncard like '%" + nama.trim().toUpperCase() + "%'";
			}
			if (orderdate != null) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "Tembossdata.orderdate = '" + dateFormatter.format(orderdate) + "'";
			}

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}

	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "Tembossdata.productcode, tembossdata.cardno";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TembossdataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		try {
			cardno = null;
			orderdate = null;
			rowUpload.setVisible(false);
			footUpload.setVisible(false);
			gbHasilUpload.setVisible(false);
			type = "M";
			totalselected = 0;
			totalupload = 0;
			inserted = 0;
			failed = 0;
			mapData = new HashMap<>();
			listTdata = new ArrayList<>();
			doSearch();

			List<Tderivatifdata> dataList = derivatifdataDao.listByFilter("tderivatiffk = " + obj.getTderivatifpk(),
					"cardno");
			if (dataList.size() > 0) {
				for (Tderivatifdata data : dataList) {
					cardnoList.add(data.getCardno());
					getdataList.add(data.getTembossdata());
				}
			} else {
				getdataList = new ArrayList<>();
				cardnoList = new ArrayList<>();
			}
			gridGetdata.setModel(new ListModelList<>(getdataList));
			totaldata = getdataList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public String getNama() {
		return nama;
	}

	public void setNama(String nama) {
		this.nama = nama;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public Tderivatif getObj() {
		return obj;
	}

	public void setObj(Tderivatif obj) {
		this.obj = obj;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getTotalupload() {
		return totalupload;
	}

	public void setTotalupload(Integer totalupload) {
		this.totalupload = totalupload;
	}

	public Integer getInserted() {
		return inserted;
	}

	public void setInserted(Integer inserted) {
		this.inserted = inserted;
	}

	public Integer getFailed() {
		return failed;
	}

	public void setFailed(Integer failed) {
		this.failed = failed;
	}

}
