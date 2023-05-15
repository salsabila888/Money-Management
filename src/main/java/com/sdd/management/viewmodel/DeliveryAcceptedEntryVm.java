package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MusergrouplevelDAO;
import com.sdd.caption.dao.TbookdataDAO;
import com.sdd.caption.dao.TbookfileDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverycourierDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.TpinpadorderproductDAO;
import com.sdd.caption.dao.TrepairdlvDAO;
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.dao.TreturntrackDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergrouplevel;
import com.sdd.caption.domain.Tbookdata;
import com.sdd.caption.domain.Tbookfile;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverycourier;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tpinpadorderproduct;
import com.sdd.caption.domain.Trepairdlv;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturntrack;
import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.domain.Vitemprice;
import com.sdd.caption.handler.BranchStockManager;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TdeliveryListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DeliveryAcceptedEntryVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private TdeliveryListModel model;
	private Mbranch mbranch;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String arg;

	private String vendorcode;
	private String dlvid;
	private String produk;
	private Date processtime;
	private Integer totaldata;
	private Integer totalselected;
	private Integer totaldataselected;
	private Date tglterima;
	private String penerima;
	private String filename;
	private Media media;

	private Tdelivery obj;
	private TdeliveryDAO oDao = new TdeliveryDAO();
	private Map<Integer, Tdelivery> mapData = new HashMap<>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Combobox cbCabang, cbProduk;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Textbox tbPenerima;
	@Wire 
	private Label lbFileBrowse;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String argp)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.arg = argp;
		this.mbranch = oUser.getMbranch();
		
		System.out.println(mbranch.getBranchid());

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
				chkAll.setChecked(false);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tdelivery>() {
			@Override
			public void render(Row row, final Tdelivery data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tdelivery obj = (Tdelivery) checked.getAttribute("obj");
						if (checked.isChecked()) {
							mapData.put(data.getTdeliverypk(), data);
							totaldataselected += obj.getTotaldata();
						} else {
							mapData.remove(data.getTdeliverypk());
							totaldataselected -= obj.getTotaldata();
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, DeliveryAcceptedEntryVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, DeliveryAcceptedEntryVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTdeliverypk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);

				A a = new A(data.getDlvid());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);

						Window win = (Window) Executions.createComponents("/view/delivery/deliverydata.zul", null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Boolean isSaved = (Boolean) event.getData();
								if (isSaved != null && isSaved) {
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
									BindUtils.postNotifyChange(null, null, DeliveryAcceptedEntryVm.this,
											"pageTotalSize");
								}
							}
						});
						win.doModal();
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getProcesstime())));
				row.getChildren().add(new Label(data.getMbranch().getBranchid()));
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(String.valueOf(data.getTotaldata())));
				row.getChildren().add(new Label(data.getMcouriervendor().getVendorcode()));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
			}
		});
		doReset();
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Tdelivery obj = (Tdelivery) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getTdeliverypk(), obj);
							totaldataselected += obj.getTotaldata();
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getTdeliverypk());
							totaldataselected = 0;
						}
					}
				}
				totalselected = mapData.size();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	public void doSearch() {
		try {
			if (mbranch != null) {
				if (oUser.getMbranch().getBranchlevel() == 1) {
					filter = "tglterima is null and penerima is null " + " and tdelivery.status = '" 
							+ AppUtils.STATUS_DELIVERY_DELIVERY + "' and tdelivery.productgroup = '" + produk + "'";
				} else {
					filter = "tglterima is null and penerima is null and tdelivery.mbranchfk = " + mbranch.getMbranchpk()
							+ " and tdelivery.status = '" + AppUtils.STATUS_DELIVERY_DELIVERY + "' and tdelivery.productgroup = '" + produk + "'";
				}

				if (dlvid != null && dlvid.length() > 0)
					filter += " and dlvid like '%" + dlvid.trim().toUpperCase() + "%'";
				if (vendorcode != null && vendorcode.length() > 0)
					filter += " and vendorcode like '%" + vendorcode.trim().toUpperCase() + "%'";
				if (processtime != null)
					filter += " and DATE(tdelivery.processtime) = '" + dateFormatter.format(processtime) + "'";

				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	@Command
	public void doResetSelected() {
		if (mapData.size() > 0) {
			Messagebox.show("Anda ingin mereset data-data yang sudah anda pilih?", "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								doReset();
								BindUtils.postNotifyChange(null, null, DeliveryAcceptedEntryVm.this, "*");
							}
						}
					});
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totaldata = 0;
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
		chkAll.setChecked(false);
		processtime = new Date();
		dlvid = "";
		vendorcode = "";
		processtime = null;

		produk = arg;
		tglterima = new Date();
		filename = null;
		penerima = null;
		tbPenerima.setValue(null);
		lbFileBrowse.setValue(null);
		
		obj = new Tdelivery();

		pageTotalSize = 0;
		paging.setTotalSize(pageTotalSize);
		if (grid.getRows() != null) {
			grid.getRows().getChildren().clear();
		}

		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void refreshModel(int activePage) {
		try {
			orderby = "tdeliverypk asc";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new TdeliveryListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	@NotifyChange("*")
	public void doManifest() {
		Session session = StoreHibernateUtil.openSession();
		Transaction transaction = session.beginTransaction();
		if (mapData.size() > 0) {
			if (filename == null || tglterima == null || penerima == null || penerima.trim().length() == 0) {
				Messagebox.show("Silahkan isi Tanggal Terima, Nama Penerima dan Tanda Terima", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				String msg = "";
				if (produk.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
					msg = "Apakah anda yakin ingin menyelesaikan pengiriman dan melanjutkan proses pembukuan?";
				else
					msg = "Apakah anda yakin ingin menyelesaikan pengiriman?";
				Messagebox.show(msg, "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
						new EventListener() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									try {
										Map<Integer, Torder> mapOrder = new HashMap<Integer, Torder>();
										Map<Integer, Tpinpadorderproduct> mapPinpad = new HashMap<Integer, Tpinpadorderproduct>();
										Map<Integer, Treturn> mapReturn = new HashMap<Integer, Treturn>();
										Map<Integer, Trepairdlv> mapRepair = new HashMap<Integer, Trepairdlv>();
										Map<Integer, Tswitch> mapSwitch = new HashMap<Integer, Tswitch>();
										Map<String, Mproductgroup> mapProductgroup = AppData.getMproductgroup();

										Tdeliverycourier objForm = new Tdeliverycourier();
										objForm.setMcouriervendor(mbranch.getMcouriervendor());
										objForm.setProductgroup(produk);
										objForm.setDlvcourierid(
												new TcounterengineDAO().generateCounter(AppUtils.CE_EXPEDITION));
										objForm.setTotaldata(mapData.size());
										objForm.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
										objForm.setProcessedby(oUser.getUserid());
										objForm.setProcesstime(new Date());
										objForm.setIsurgent("N");
										
										boolean isValid = false;
										boolean isCemtext = true;
										for (Entry<Integer, Tdelivery> entry : mapData.entrySet()) {
											Tdelivery obj = entry.getValue();
											if (obj.getTdeliverycourier() == null) {
												objForm.setCourierbranchpool(obj.getBranchpool());
												obj.setTdeliverycourier(objForm);
												isValid = true;
											} else {
												obj.getTdeliverycourier().setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
											}
											obj.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
											obj.setIsurgent("N");
											obj.setTglterima(tglterima);
											obj.setPenerima(penerima);
											obj.setFilename(filename);
											
											oDao.save(session, obj);

											List<Tdeliverydata> tddList = new TdeliverydataDAO().listByFilter(
													"tdeliveryfk = " + obj.getTdeliverypk(), "tdeliveryfk");
											for (Tdeliverydata tdd : tddList) {
												if (tdd.getTpaketdata().getTpaket().getTorder() != null) {
													mapOrder.put(
															tdd.getTpaketdata().getTpaket().getTorder().getTorderpk(),
															tdd.getTpaketdata().getTpaket().getTorder());
												} else if (tdd.getTpaketdata().getTpaket().getTreturn() != null) {
													mapReturn.put(
															tdd.getTpaketdata().getTpaket().getTreturn().getTreturnpk(),
															tdd.getTpaketdata().getTpaket().getTreturn());

													isCemtext = false;
												} else if (tdd.getTpaketdata().getTpaket().getTrepairdlv() != null) {
													mapRepair.put(
															tdd.getTpaketdata().getTpaket().getTrepairdlv()
																	.getTrepairdlvpk(),
															tdd.getTpaketdata().getTpaket().getTrepairdlv());

													isCemtext = false;
												} else if (tdd.getTpaketdata().getTpaket()
														.getTpinpadorderproduct() != null) {
													mapPinpad.put(
															tdd.getTpaketdata().getTpaket().getTpinpadorderproduct()
																	.getTpinpadorderproductpk(),
															tdd.getTpaketdata().getTpaket().getTpinpadorderproduct());
													
													isCemtext = false;
												}
												if (tdd.getTpaketdata().getTpaket().getTswitch() != null) {
													mapSwitch.put(
															tdd.getTpaketdata().getTpaket().getTswitch().getTswitchpk(),
															tdd.getTpaketdata().getTpaket().getTswitch());
												}

											}

											if (isCemtext) {
												Mproductgroup mproductgroup = mapProductgroup
														.get(objForm.getProductgroup());
												if (mproductgroup.getIscoa().equals("Y")) {
													String branch = StringUtils
															.leftPad(oUser.getMbranch().getBranchid(), 4, "0")
															.substring(0, 4);
													String fileid = "XTRDBINV" + branch
															+ new SimpleDateFormat("ddMMYYYYHHmmss").format(new Date())
															+ ".CTX";

													int trxcounter = 0;
													BigDecimal amount = new BigDecimal(0);
													BigDecimal totalamount = new BigDecimal(0);
													String seqnum = "";
													List<Tbookdata> bookdataList = new ArrayList<Tbookdata>();

													for (Tdeliverydata data : new TdeliverydataDAO().listByFilter(
															"tdeliveryfk = " + obj.getTdeliverypk(),
															"tdeliverydatapk")) {
														for (Vitemprice vprice : new TorderitemDAO()
																.sumItemPriceByDlv(data.getTdeliverydatapk())) {
															trxcounter++;
															amount = vprice.getTotalprice();
															totalamount = totalamount.add(amount);
															seqnum = new TcounterengineDAO().generateSeqnum();

															Tbookdata tbd = new Tbookdata();
															tbd.setTdeliverydata(data);
															tbd.setSeqnum(seqnum);
															tbd.setQuantity(vprice.getItemqty());
															tbd.setItemprice(vprice.getItemprice());
															tbd.setTotalamount(amount);
															tbd.setStatus(AppUtils.STATUS_CEMTEXT_WAITAPPROVAL);
															bookdataList.add(tbd);
														}
													}
													Tbookfile tbookfile = new Tbookfile();
													tbookfile.setTdelivery(obj);
													tbookfile.setBookid(fileid);
													tbookfile.setTotaldata(trxcounter);
													tbookfile.setTotalamount(totalamount);
													tbookfile.setTotalerror(0);
													tbookfile.setTotalsuccess(0);
													tbookfile.setBooktime(new Date());
													tbookfile.setBookedby(oUser.getUserid());
													tbookfile.setStatus(AppUtils.STATUS_CEMTEXT_WAITAPPROVAL);
													tbookfile.setStatusdesc(
															AppData.getStatusLabel(tbookfile.getStatus()));
													new TbookfileDAO().save(session, tbookfile);

													for (Tbookdata bookdata : bookdataList) {
														bookdata.setTbookfile(tbookfile);
														new TbookdataDAO().save(session, bookdata);
													}

													if (oUser.getMbranch().getBranchlevel() < 3) {
														Musergrouplevel grouplevel = new MusergrouplevelDAO()
																.findByFilter("branchlevel = "
																		+ oUser.getMbranch().getBranchlevel() + " and "
																		+ obj.getTotalamount()
																		+ " between amountstart and amountend");
														if (grouplevel != null) {
															if (grouplevel.getGrouplevel() == 3) {
																Mmenu mmenu = new MmenuDAO().findByFilter(
																		"menupath = '/view/pembukuan/booklist.zul' and menuparamvalue = 'kelompok'");
																NotifHandler.doNotif(mmenu, oUser.getMbranch(), "04",
																		oUser.getMbranch().getBranchlevel());
															} else if (grouplevel.getGrouplevel() == 2) {
																Mmenu mmenu = new MmenuDAO().findByFilter(
																		"menupath = '/view/pembukuan/booklist.zul' and menuparamvalue = 'wakil'");
																NotifHandler.doNotif(mmenu, oUser.getMbranch(), "04",
																		oUser.getMbranch().getBranchlevel());
															} else if (grouplevel.getGrouplevel() == 1) {
																Mmenu mmenu = new MmenuDAO().findByFilter(
																		"menupath = '/view/pembukuan/booklist.zul' and menuparamvalue = 'pimpinan'");
																NotifHandler.doNotif(mmenu, oUser.getMbranch(), "04",
																		oUser.getMbranch().getBranchlevel());
															}
														}
													}
												}
											}
										}

										if (isValid) {
											new TdeliverycourierDAO().save(session, objForm);
										}
										
										if (media != null) {
											String path = Executions.getCurrent().getDesktop().getWebApp()
													.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.POD_PATH);
											if (media.isBinary()) {
												Files.copy(new File(path + "/" + media.getName()), media.getStreamData());
											} else {
												BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + media.getName()));
												Files.copy(writer, media.getReaderData());
												writer.close();
											}
											System.out.println(path);
										}

										for (Entry<Integer, Torder> entry : mapOrder.entrySet()) {
											Torder order = entry.getValue();
											order.setFileterima(filename);
											order.setPenerima(penerima);
											order.setTglterima(tglterima);
											order.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
											new TorderDAO().save(session, order);

											BranchStockManager.manageNonCard(order, order.getProductgroup());
											
											FlowHandler.doFlow(session, null, order, AppData.getStatusLabel(order.getStatus()), oUser.getUserid());
										}

										for (Entry<Integer, Treturn> entry : mapReturn.entrySet()) {
											Treturn retur = entry.getValue();
											retur.setStatus(AppUtils.STATUS_RETUR_RECEIVED);
											new TreturnDAO().save(session, retur);

											Treturntrack objrt = new Treturntrack();
											objrt.setTreturn(retur);
											objrt.setTracktime(new Date());
											objrt.setTrackstatus(AppUtils.STATUS_RETUR_RECEIVED);
											objrt.setTrackdesc(AppData.getStatusLabel(objrt.getTrackstatus()));
											new TreturntrackDAO().save(session, objrt);

											BranchStockManager.manageReturStock(retur);
										}

										for (Entry<Integer, Trepairdlv> entry : mapRepair.entrySet()) {
											Trepairdlv repair = entry.getValue();
											repair.setStatus(AppUtils.STATUS_REPAIR_RECEIVED);
											new TrepairdlvDAO().save(session, repair);

											BranchStockManager.manageRepairStock(repair);
										}
										for (Entry<Integer, Tswitch> entry : mapSwitch.entrySet()) {
											Tswitch sw = entry.getValue();
											sw.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
											new TswitchDAO().save(session, sw);
										}
										
										for(Entry<Integer, Tpinpadorderproduct> entry : mapPinpad.entrySet()) {
											Tpinpadorderproduct tpp = entry.getValue();
											tpp.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
											new TpinpadorderproductDAO().save(session, tpp);
											
											tpp.getTorder().setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
											new TorderDAO().save(session, tpp.getTorder()); 
											
											BranchStockManager.managePinpadStock(tpp, tpp.getMproduct().getProductgroup());
										}

										transaction.commit();
										Clients.showNotification(Labels.getLabel("common.add.success"), "info", null,
												"middle_center", 3000);
										doReset();
									} catch (Exception e) {
										transaction.rollback();
										Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(),
												Messagebox.OK, Messagebox.ERROR);
										e.printStackTrace();
									} finally {
										session.close();
									}

								}
							}

						});
			}
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}


	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Tdelivery getObj() {
		return obj;
	}

	public void setObj(Tdelivery obj) {
		this.obj = obj;
	}

	public String getVendorcode() {
		return vendorcode;
	}

	public void setVendorcode(String vendorcode) {
		this.vendorcode = vendorcode;
	}

	public String getDlvid() {
		return dlvid;
	}

	public void setDlvid(String dlvid) {
		this.dlvid = dlvid;
	}

	public String getProduk() {
		return produk;
	}

	public void setProduk(String produk) {
		this.produk = produk;
	}

	public Date getProcesstime() {
		return processtime;
	}

	public void setProcesstime(Date processtime) {
		this.processtime = processtime;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public Date getTglterima() {
		return tglterima;
	}

	public void setTglterima(Date tglterima) {
		this.tglterima = tglterima;
	}

	public String getPenerima() {
		return penerima;
	}

	public void setPenerima(String penerima) {
		this.penerima = penerima;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}