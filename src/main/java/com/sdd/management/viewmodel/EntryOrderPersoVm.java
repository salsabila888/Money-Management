package com.sdd.caption.viewmodel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.dao.TderivatifdataDAO;
import com.sdd.caption.dao.TderivatifproductDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mpersovendor;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.domain.Tderivatifdata;
import com.sdd.caption.domain.Tderivatifproduct;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TembossdataListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class EntryOrderPersoVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TembossdataListModel model;

	private TpersoDAO tpersoDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private TembossdataDAO tembossdataDao = new TembossdataDAO();
	private TderivatifDAO derivatifDao = new TderivatifDAO();
	private TderivatifproductDAO derproductDao = new TderivatifproductDAO();
	private TderivatifdataDAO derdataDao = new TderivatifdataDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private Media media;
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String filename;
	private Date persotime;
	private String memo;

	private Tperso objForm;
	private Tderivatif obj;

	private int totaldata;
	private int gridno;
	private int gridnounmatch;
	private Mproduct mproduct;
	private Mpersovendor mpersovendor;
	private String keterangan;

	private List<Tderivatifdata> listTdata = new ArrayList<>();
	private Map<String, Tembossdata> mapData = new HashMap<>();
	private Map<Integer, Tembossbranch> mapBranch = new HashMap<>();
	private Map<Integer, Tderivatif> mapDerivatif = new HashMap<>();
	private Map<String, Tderivatifproduct> mapDerProduct = new HashMap<>();
	private Map<String, Tperso> mapPerso = new HashMap<>();

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Window winPersobranchdetail;
	@Wire
	private Row rowVendor;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Grid gridSelected;
	@Wire
	private Grid gridUnmatch;
	@Wire
	private Combobox cbTypeProduct, cbPersovendor;
	@Wire
	private Radio rbInternal, rbExternal;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "torderdatapk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TembossdataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	private void addGridSelected(final Tembossdata data) {
		Row row = new Row();
		row.appendChild(new Label(String.valueOf(++gridno)));
		row.appendChild(new Label(data.getCardno()));
		row.appendChild(new Label(data.getNameonid()));
		row.appendChild(new Label(data.getMproduct().getProductcode()));
		row.appendChild(new Label(data.getMproduct().getProductname()));
		row.appendChild(new Label(datelocalFormatter.format(data.getOrderdate())));
		row.appendChild(new Label(data.getMbranch().getBranchid()));
		row.appendChild(new Label(data.getMbranch().getBranchname()));
		// gridSelected.getRows().appendChild(row);
		gridSelected.getRows().insertBefore(row, gridSelected.getRows().getFirstChild());
	}

	private void addGridUnmatch(final Tembossdata data) {
		Row row = new Row();
		row.appendChild(new Label(String.valueOf(++gridnounmatch)));
		row.appendChild(new Label(data.getCardno()));
		row.appendChild(new Label(keterangan));
		// gridUnmatch.getRows().appendChild(row);
		gridUnmatch.getRows().insertBefore(row, gridUnmatch.getRows().getFirstChild());
	}

	@NotifyChange("filename")
	@Command
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
		media = event.getMedia();
		filename = media.getName();
		/*
		 * if (media != null) btnSave.setDisabled(false);
		 */
	}

	@Command
	@NotifyChange({ "pageTotalSize", "filename" })
	public void doUpload() {
		if (media != null) {
			gridno = 0;
			gridnounmatch = 0;
			mapData = new HashMap<>();
			if (gridSelected.getRows() != null)
				gridSelected.getRows().getChildren().clear();
			if (gridUnmatch.getRows() != null)
				gridUnmatch.getRows().getChildren().clear();
			BufferedReader reader = null;
			try {
				if (media.isBinary()) {
					reader = new BufferedReader(new InputStreamReader(media.getStreamData()));
				} else {
					reader = new BufferedReader(media.getReaderData());
				}

				String line = "";
				while ((line = reader.readLine()) != null) {
					if (line.length() >= 20) {
						Tembossdata data = null;
						List<Tembossdata> listCardno = tembossdataDao
								.listByFilter("cardno = '" + line.substring(1, 20).trim() + "'", "tembossdatapk");
						if (listCardno.size() > 0) {
							data = listCardno.get(0);
						}
						System.out.println("CARDNO : " + line.substring(1, 20));
						if (data != null) {
							if (data.getMbranch() != null) {
								if (data.getMproduct() != null) {
									if (data.getMproduct().getMproductpk().equals(mproduct.getMproductpk())) {
										if (mapData.get(data.getCardno()) == null) {
											addGridSelected(data);
											mapData.put(data.getCardno(), data);
											totaldata++;
											BindUtils.postNotifyChange(null, null, EntryOrderPersoVm.this, "totaldata");
										} else {
											keterangan = "Duplicate data";
											addGridUnmatch(data);
										}
									} else {
										keterangan = "Kode produk tidak sesuai";
										addGridUnmatch(data);
									}
								} else {
									keterangan = "Parameter produk belum terdaftar";
									addGridUnmatch(data);
								}
							} else {
								keterangan = "Parameter Cabang belum terdaftar";
								addGridUnmatch(data);
							}
						} else {
							keterangan = "Data tidak ditemukan";
							data = new Tembossdata();
							data.setCardno(line.substring(1, 20).trim());
							addGridUnmatch(data);
						}
					}
				}

				media = null;
				filename = null;

				if (totaldata == 0)
					Messagebox.show("Tidak ada data yang dapat diproses.", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Silahkan browse file untuk proses upload", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}

	}

	@Command
	public void doPersotypeSelected() {
		if (rbInternal.isChecked()) {
			rowVendor.setVisible(false);
			cbPersovendor.setValue(null);
			objForm.setMpersovendor(null);
		} else {
			rowVendor.setVisible(true);
		}
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winPersobranchdetail, null);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		gridno = 0;
		gridnounmatch = 0;
		totaldata = 0;
		persotime = new Date();
		memo = "";
		rowVendor.setVisible(false);
		mapData = new HashMap<>();
		mapDerivatif = new HashMap<>();
		mapDerProduct = new HashMap<>();
		mapPerso = new HashMap<>();
		if (gridSelected.getRows() != null)
			gridSelected.getRows().getChildren().clear();
		if (gridUnmatch.getRows() != null)
			gridUnmatch.getRows().getChildren().clear();
		cbTypeProduct.setValue(null);
		media = null;
		filename = null;
	}

	@Command
	public void doSave() {
		if (totaldata > 0) {
			try {
				Messagebox.show("Anda ingin membuat \nmanifest perso?", "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

							@SuppressWarnings("unused")
							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									synchronized (this) {
										Mproducttype mproducttype = null;
										Session session = null;
										Transaction transaction = null;

										mproducttype = mproducttypeDao
												.findByPk(mproduct.getMproducttype().getMproducttypepk());
										if (mproducttype.getLaststock()
												- mproducttype.getStockreserved() >= totaldata) {

											try {
												session = StoreHibernateUtil.openSession();
												transaction = session.beginTransaction();

												for (Entry<String, Tembossdata> entry : mapData.entrySet()) {
													Tembossdata data = entry.getValue();

													obj = mapDerivatif.get(data.getMbranch().getMbranchpk());
													if (obj == null) {
														obj = new Tderivatif();
														obj.setMbranch(data.getMbranch());
														obj.setMproduct(data.getMproduct());
														obj.setOrderdate(new Date());
														obj.setOrderno(new TcounterengineDAO()
																.generateCounter(AppUtils.CE_DERIVATIF));
														obj.setProdsla(0);
														obj.setDlvsla(0);
														obj.setEntryby(oUser.getUserid());
														obj.setEntrytime(new Date());
														obj.setSlatotal(0);
														obj.setStatus(AppUtils.STATUS_DERIVATIF_ORDERPERSOAPPROVAL);
														obj.setTotaladj(0);
														obj.setTotaldata(1);
														obj.setTotalreject(0);
														obj.setMemo(memo);

														mapDerivatif.put(data.getMbranch().getMbranchpk(), obj);
													} else {
														obj.setTotaldata(obj.getTotaldata() + 1);
													}

													Tderivatifproduct derProduct = mapDerProduct
															.get(data.getBranchid() + data.getOrderdate());
													if (derProduct == null) {
														derProduct = new Tderivatifproduct();
														derProduct.setTderivatif(obj);
														derProduct.setEntrytime(new Date());
														derProduct.setMproduct(data.getMproduct());
														derProduct.setOrderdate(data.getOrderdate());
														derProduct.setTembossbranch(data.getTembossbranch());
														derProduct.setTotaldata(1);

														mapDerProduct.put(data.getBranchid() + data.getOrderdate(),
																derProduct);

														objForm = new Tperso();
														objForm.setTderivatifproduct(derProduct);
														objForm.setMproduct(derProduct.getMproduct());
														objForm.setPersoid(new TcounterengineDAO()
																.generateCounter(AppUtils.CE_PERSO));
														objForm.setOrderdate(derProduct.getOrderdate());
														objForm.setTotaldata(derProduct.getTotaldata());
														objForm.setTotalpaket(0);
														objForm.setPersostartby(oUser.getUserid());
														objForm.setPersostarttime(new Date());
														objForm.setStatus(AppUtils.STATUS_PERSO_PERSOWAITAPPROVAL);
														objForm.setIsgetallpaket("");
														tpersoDao.save(session, objForm);

														mapPerso.put(data.getBranchid() + data.getOrderdate(), objForm);

													} else {
														derProduct.setTotaldata(derProduct.getTotaldata() + 1);
														objForm = mapPerso
																.get(data.getBranchid() + data.getOrderdate());
														if (objForm != null) {
															objForm.setTotaldata(objForm.getTotaldata() + 1);
														}
													}

													Tderivatifdata tdd = new Tderivatifdata();
													tdd.setTderivatif(obj);
													tdd.setTderivatifproduct(derProduct);
													tdd.setCardno(data.getCardno());
													tdd.setOrderdate(data.getOrderdate());
													tdd.setTembossdata(data);
													tdd.setStatus(AppUtils.STATUS_PROSES);
													listTdata.add(tdd);
												}

												for (Entry<Integer, Tderivatif> entry : mapDerivatif.entrySet()) {
													obj = entry.getValue();
													derivatifDao.save(session, obj);
												}

												for (Entry<String, Tderivatifproduct> entry : mapDerProduct
														.entrySet()) {
													Tderivatifproduct derProduct = entry.getValue();
													derproductDao.save(session, derProduct);

													if (mapBranch.get(derProduct.getTembossbranch()
															.getTembossbranchpk()) == null) {
														derProduct.getTembossbranch().setTotalproses(
																derProduct.getTembossbranch().getTotalproses()
																		+ derProduct.getTotaldata());
														derProduct.getTembossbranch()
																.setTotalos(derProduct.getTembossbranch().getTotalos()
																		- derProduct.getTotaldata());
														if (derProduct.getTembossbranch().getTotalos().equals(0))
															derProduct.getTembossbranch()
																	.setStatus(AppUtils.STATUS_PROSES);

														mapBranch.put(
																derProduct.getTembossbranch().getTembossbranchpk(),
																derProduct.getTembossbranch());
													}

												}

												for (Entry<Integer, Tembossbranch> entry : mapBranch.entrySet()) {
													Tembossbranch branch = entry.getValue();
													new TembossbranchDAO().save(session, branch);
												}

												for (Entry<Integer, Tembossbranch> entry : mapBranch.entrySet()) {
													Tembossbranch branch = entry.getValue();
													new TembossbranchDAO().save(session, branch);
												}

												for (Tderivatifdata data : listTdata) {
													derdataDao.save(session, data);
												}

												for (Entry<String, Tperso> entry : mapPerso.entrySet()) {
													Tperso perso = entry.getValue();
													tpersoDao.save(session, perso);

													Tpersodata tpersodata = new Tpersodata();
													tpersodata.setMbranch(perso.getTderivatifproduct()
															.getTembossbranch().getMbranch());
													tpersodata.setTperso(perso);
													tpersodata.setOrderdate(perso.getOrderdate());
													tpersodata.setQuantity(perso.getTotaldata());
													tpersodata.setStatus("");
													tpersodata.setIsgetpaket("");
													tpersodataDao.save(session, tpersodata);
												}

												transaction.commit();
											} catch (Exception e) {
												transaction.rollback();
												e.printStackTrace();
											} finally {
												session.close();
											}

											try {
												for (Entry<Integer, Tderivatif> entry : mapDerivatif.entrySet()) {
													Mmenu mmenu = new MmenuDAO()
															.findByFilter("menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persoapproval'");
													NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD, oUser.getMbranch().getBranchlevel());
												}
												
												session = StoreHibernateUtil.openSession();
												transaction = session.beginTransaction();

												mproducttype
														.setStockreserved(mproducttype.getStockreserved() + totaldata);

												mproducttypeDao.save(session, mproducttype);
												transaction.commit();
												session.close();
											} catch (Exception e) {
												e.printStackTrace();
											}

											Messagebox.show("Pembuatan manifest perso berhasil", "Info", Messagebox.OK,
													Messagebox.INFORMATION);
											doReset();
											BindUtils.postNotifyChange(null, null, EntryOrderPersoVm.this, "objForm");
											BindUtils.postNotifyChange(null, null, EntryOrderPersoVm.this, "totaldata");

										} else {
											Messagebox.show("Stock tidak mencukupi", "Info", Messagebox.OK,
													Messagebox.INFORMATION);
										}

									}
								}
							}

						});
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	/*
	 * public Validator getValidator() { return new AbstractValidator() {
	 * 
	 * @Override public void validate(ValidationContext ctx) { String persotype =
	 * (String) ctx.getProperties("persotype")[0].getValue(); Mpersovendor
	 * mpersovendor = (Mpersovendor)
	 * ctx.getProperties("mpersovendor")[0].getValue();
	 * 
	 * Mproduct mproduct = (Mproduct) ctx.getProperties("mproduct")[0].getValue();
	 * 
	 * 
	 * if (persotype.equals("E") && (mpersovendor == null))
	 * this.addInvalidMessage(ctx, "mpersovendor",
	 * Labels.getLabel("common.validator.empty"));
	 * 
	 * if (mproduct == null) this.addInvalidMessage(ctx, "mproduct",
	 * Labels.getLabel("common.validator.empty"));
	 * 
	 * } }; }
	 */

	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(new MproductDAO()
					.listByFilter("productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'", "productcode, productname"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mpersovendor> getMpersovendormodel() {
		ListModelList<Mpersovendor> lm = null;
		try {
			lm = new ListModelList<Mpersovendor>(AppData.getMpersovendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Tperso getObjForm() {
		return objForm;
	}

	public void setObjForm(Tperso objForm) {
		this.objForm = objForm;
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public Date getPersotime() {
		return persotime;
	}

	public void setPersotime(Date persotime) {
		this.persotime = persotime;
	}

	public Mpersovendor getMpersovendor() {
		return mpersovendor;
	}

	public void setMpersovendor(Mpersovendor mpersovendor) {
		this.mpersovendor = mpersovendor;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

}
