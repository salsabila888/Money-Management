package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mpersovendor;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PersoBranchVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TembossbranchDAO oDao = new TembossbranchDAO();
	private TembossproductDAO productDao = new TembossproductDAO();
	private TpersoDAO tpersoDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private List<Tembossbranch> objList = new ArrayList<Tembossbranch>();
	private List<Tembossbranch> listSelected = new ArrayList<Tembossbranch>();
	private Map<Integer, Tembossbranch> mapData = new HashMap<>();
	private String filter;

	private Tembossproduct tembossproduct;
	private Tperso objForm;
	private Mbranch mbranch;
	private Mbranch mbranch2;
	private String persotype;
	private int totaldata;
	private int stockreserved;
	private int stockavailable;
	private Boolean isSaved;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Window winPersobranch;
	@Wire
	private Combobox cbBranch;
	@Wire
	private Combobox cbBranch2;
	@Wire
	private Row rowVendor;

	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("tembossproduct") Tembossproduct tembossproduct) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.tembossproduct = tembossproduct;
		doReset();
		grid.setRowRenderer(new RowRenderer<Tembossbranch>() {

			@Override
			public void render(Row row, final Tembossbranch data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				Checkbox chkbox = new Checkbox();
				if (data.getMbranch() == null) {
					chkbox.setChecked(false);
					chkbox.setDisabled(true);
				} else {
					chkbox.setChecked(true);
				}
				chkbox.setAttribute("obj", data);
				chkbox.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tembossbranch obj = (Tembossbranch) checked.getAttribute("obj");
						if (checked.isChecked()) {
							if (data.getMbranch() != null) {
								if (stockavailable >= data.getTotaldata()) {
									listSelected.remove(obj);
									listSelected.add(obj);
									mapData.put(data.getMbranch().getMbranchpk(), obj);
									totaldata += obj.getTotaldata();
									/*
									 * stockavailable -= obj.getTotaldata(); stockreserved += obj.getTotaldata();
									 */
								} else {
									// Stock Not Available
									checked.setChecked(false);
									Messagebox.show("Stock Not Available", "Info", Messagebox.OK,
											Messagebox.INFORMATION);
								}
							} else {
								Messagebox.show("Cabang belum terdaftar diparameter", "Info", Messagebox.OK,
										Messagebox.INFORMATION);
							}
						} else {
							listSelected.remove(obj);
							mapData.remove(data.getMbranch().getMbranchpk());
							totaldata -= obj.getTotaldata();
							/*
							 * stockavailable += obj.getTotaldata(); stockreserved -= obj.getTotaldata();
							 */
						}
						BindUtils.postNotifyChange(null, null, PersoBranchVm.this, "totaldata");
						BindUtils.postNotifyChange(null, null, PersoBranchVm.this, "stockavailable");
						BindUtils.postNotifyChange(null, null, PersoBranchVm.this, "stockreserved");
					}
				});
				row.getChildren().add(chkbox);

				row.getChildren().add(new Label(data.getBranchid()));

				if (data.getMbranch() != null)
					row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				else {
					row.getChildren().add(new Label(""));
					chkbox.setChecked(false);
				}

				row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(
						data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));

				Button btndetail = new Button("Detail");
				btndetail.setAutodisable("self");
				btndetail.setClass("btn btn-default btn-sm");
				btndetail.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important; float: right !important;");
				btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("tembossbranch", data);

						Window win = (Window) Executions.createComponents("/view/order/orderdata.zul", null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.doModal();
					}
				});
				row.getChildren().add(btndetail);
			}
		});
	}

	@Command
	@NotifyChange({ "totaldata", "stockavailable" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Tembossbranch obj = (Tembossbranch) chk.getAttribute("obj");
					if (obj.getMbranch() != null) {
						if (checked) {
							if (!chk.isChecked()) {
								if (stockavailable >= obj.getTotaldata()) {
									listSelected.add(obj);
									mapData.put(obj.getMbranch().getMbranchpk(), obj);
									totaldata += obj.getTotaldata();
									/*
									 * stockavailable -= obj.getTotaldata(); stockreserved += obj.getTotaldata();
									 */
									chk.setChecked(true);
								} else {
									// Stock Not Available
									chk.setChecked(false);
								}
							}
						} else {
							if (chk.isChecked()) {
								listSelected.remove(obj);
								mapData.remove(obj.getMbranch().getMbranchpk());
								totaldata -= obj.getTotaldata();
								/*
								 * stockavailable += obj.getTotaldata(); stockreserved -= obj.getTotaldata();
								 */
								chk.setChecked(false);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange({ "stockavailable", "stockreserved" })
	private void doResetStockCal() {
		stockreserved = tembossproduct.getMproduct().getMproducttype().getStockreserved();
		stockavailable = tembossproduct.getMproduct().getMproducttype().getLaststock() - stockreserved;
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			// filter = "tembossproduct.tembossproductpk = " +
			// tembossproduct.getTembossproductpk() + " and status = '" +
			// AppUtils.STATUS_ORDER + "'";
			filter = "tembossproduct.tembossproductpk = " + tembossproduct.getTembossproductpk() + " and totalos > 0";
			if (mbranch != null) {
				if (filter.length() > 0)
					filter += " and ";
				if (mbranch2 != null)
					filter += "mbranch.branchid between '" + mbranch.getBranchid() + "' and '" + mbranch2.getBranchid()
							+ "'";
				else
					filter += "mbranch.branchid = '" + mbranch.getBranchid() + "'";
			}

			listSelected = new ArrayList<Tembossbranch>();
			mapData = new HashMap<>();

			objList = oDao.listByFilter(filter, "branchid");

			totaldata = 0;
			doResetStockCal();
			for (Tembossbranch data : objList) {
				if (data.getMbranch() != null) {
					/*
					 * if (stockavailable >= data.getTotaldata()) { totaldata +=
					 * data.getTotaldata(); listSelected.add(data);
					 * mapData.put(data.getMbranch().getMbranchpk(), data); stockavailable -=
					 * data.getTotaldata(); stockreserved += data.getTotaldata();
					 * System.out.println("TEST : " + totaldata); }
					 */
					totaldata += data.getTotaldata();
					listSelected.add(data);
					mapData.put(data.getMbranch().getMbranchpk(), data);
				}
			}

			grid.setModel(new ListModelList<Tembossbranch>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doPersotypeSelected(@BindingParam("item") String item) {
		if (item.equals("I")) {
			rowVendor.setVisible(false);
		} else if (item.equals("E")) {
			rowVendor.setVisible(true);
		}
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winPersobranch, isSaved);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		System.out.println("AVAILABLE : " + tembossproduct.getOrderos());
		totaldata = 0;
		objForm = new Tperso();
		objForm.setPersostarttime(new Date());
		mbranch = null;
		mbranch2 = null;
		cbBranch.setValue(null);
		cbBranch2.setValue(null);
		persotype = "I";
		rowVendor.setVisible(false);
		stockavailable = tembossproduct.getOrderos();
		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (totaldata > 0) {
			if (stockavailable >= totaldata) {
				try {
					Messagebox.show("Anda ingin membuat \nmanifest perso?", "Confirm Dialog",
							Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									if (event.getName().equals("onOK")) {
										synchronized (this) {
											Session session = StoreHibernateUtil.openSession();
											Transaction transaction = null;
											try {
												transaction = session.beginTransaction();
												if (persotype.equals("I"))
													objForm.setMpersovendor(null);

												objForm.setTembossproduct(tembossproduct);
												objForm.setMproduct(tembossproduct.getMproduct());
												objForm.setPersoid(
														new TcounterengineDAO().generateCounter(AppUtils.CE_PERSO));
												objForm.setOrderdate(tembossproduct.getOrderdate());
												objForm.setTotaldata(totaldata);
												objForm.setTotalpaket(0);
												objForm.setPersostartby(oUser.getUserid());
												objForm.setPersostarttime(new Date());
												objForm.setStatus(AppUtils.STATUS_PERSO_PERSOWAITAPPROVAL);
												objForm.setIsgetallpaket("");
												tpersoDao.save(session, objForm);

												for (Tembossbranch obj : listSelected) {
													obj.setStatus(AppUtils.STATUSBRANCH_PROSESPRODUKSI);
													obj.setTotalproses(obj.getTotaldata());
													obj.setTotalos(0);
													oDao.save(session, obj);

													Tpersodata tpersodata = new Tpersodata();
													tpersodata.setTembossbranch(obj);
													tpersodata.setMbranch(obj.getMbranch());
													tpersodata.setTperso(objForm);
													tpersodata.setOrderdate(objForm.getOrderdate());
													tpersodata.setQuantity(obj.getTotaldata());
													tpersodata.setStatus("");
													tpersodata.setIsgetpaket("");
													tpersodataDao.save(session, tpersodata);
												}

												tembossproduct
														.setTotalproses(tembossproduct.getTotalproses() + totaldata);
												tembossproduct.setOrderos(tembossproduct.getTotaldata()
														- tembossproduct.getTotalproses());
												if (tembossproduct.getOrderos().equals(0))
													tembossproduct.setStatus(AppUtils.STATUS_PROSES);
												productDao.save(session, tembossproduct);
												transaction.commit();

												transaction = session.beginTransaction();

												tembossproduct.getMproduct().getMproducttype()
														.setStockreserved(tembossproduct.getMproduct().getMproducttype()
																.getStockreserved() + objForm.getTotaldata());
												mproducttypeDao.save(session,
														tembossproduct.getMproduct().getMproducttype());

												transaction.commit();

												isSaved = new Boolean(true);
												Messagebox.show(
														"Pembuatan manifest perso berhasil. No Manifest : "
																+ objForm.getPersoid(),
														WebApps.getCurrent().getAppName(), Messagebox.OK,
														Messagebox.INFORMATION);
												doReset();
												BindUtils.postNotifyChange(null, null, PersoBranchVm.this, "*");
											} catch (Exception e) {
												transaction.rollback();
												e.printStackTrace();
												Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(),
														Messagebox.OK, Messagebox.ERROR);
											} finally {
												session.close();
											}
											
											try {
												Mmenu mmenu = new MmenuDAO()
														.findByFilter("menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persoapproval'");
												NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD, oUser.getMbranch().getBranchlevel());
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
								}

							});
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Messagebox.show(
						"Proses pembuatan manifest tidak bisa dilakukan karena total data melebihi stock available.",
						WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
			}
		} else {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mpersovendor mpersovendor = (Mpersovendor) ctx.getProperties("mpersovendor")[0].getValue();

				if (persotype.equals("E") && (mpersovendor == null))
					this.addInvalidMessage(ctx, "mpersovendor", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public ListModelList<Mbranch> getMbranchmodel() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(AppData.getMbranch());
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

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public String getPersotype() {
		return persotype;
	}

	public void setPersotype(String persotype) {
		this.persotype = persotype;
	}

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
	}

	public Mbranch getMbranch2() {
		return mbranch2;
	}

	public void setMbranch2(Mbranch mbranch2) {
		this.mbranch2 = mbranch2;
	}

	public Tembossproduct getTembossproduct() {
		return tembossproduct;
	}

	public void setTembossproduct(Tembossproduct tembossproduct) {
		this.tembossproduct = tembossproduct;
	}

	public int getStockavailable() {
		return stockavailable;
	}

	public void setStockavailable(int stockavailable) {
		this.stockavailable = stockavailable;
	}

	public int getStockreserved() {
		return stockreserved;
	}

	public void setStockreserved(int stockreserved) {
		this.stockreserved = stockreserved;
	}

	/*
	 * public int getStockreserved() { return stockreserved; }
	 * 
	 * public void setStockreserved(int stockreserved) { this.stockreserved =
	 * stockreserved; }
	 */

}
