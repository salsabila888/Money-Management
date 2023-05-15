package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
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
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PaketBranchVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpaketDAO oDao = new TpaketDAO();
	private TpaketdataDAO dataDao = new TpaketdataDAO();
	private TpersoDAO tpersoDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private TembossbranchDAO tembossbranchDao = new TembossbranchDAO();
	private List<Tpersodata> objList = new ArrayList<Tpersodata>();
	private List<Tpersodata> objSelected = new ArrayList<Tpersodata>();

	private String filter;

	private Tperso obj;
	private Tpaket objForm;
	private Mbranch mbranch;
	private Mbranch mbranch2;
	private Mproduct mproduct;
	private int totaldata;
	private String productgroupname;
	private Boolean isSaved;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Window winDeliverybranch;
	@Wire
	private Combobox cbBranch;
	@Wire
	private Combobox cbBranch2;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") final Tperso obj)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.obj = obj;
		grid.setRowRenderer(new RowRenderer<Tpersodata>() {

			@Override
			public void render(Row row, final Tpersodata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				Checkbox chkbox = new Checkbox();
				chkbox.setChecked(true);
				chkbox.setAttribute("obj", data);
				chkbox.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tpersodata obj = (Tpersodata) checked.getAttribute("obj");
						if (checked.isChecked()) {
							objSelected.add(obj);
							totaldata += obj.getQuantity();
						} else {
							objSelected.remove(obj);
							totaldata -= obj.getQuantity();
						}
						BindUtils.postNotifyChange(null, null, PaketBranchVm.this, "totaldata");
					}
				});
				row.getChildren().add(chkbox);
				row.getChildren()
						.add(new Label(data.getMbranch().getBranchid() + "-" + data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(datelocalFormatter.format(data.getTperso().getPersofinishtime())));
				row.getChildren().add(new Label(
						data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "0"));
				Button btndetail = new Button("Detail");
				btndetail.setAutodisable("self");
				btndetail.setClass("btn btn-default btn-sm");
				btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("tembossbranch", data.getTembossbranch());

						Window win = (Window) Executions.createComponents("/view/order/orderdata.zul", null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.doModal();
					}
				});
				row.getChildren().add(btndetail);
			}
		});

		doReset();
	}

	@Command
	@NotifyChange("totaldata")
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tpersodata obj = (Tpersodata) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						chk.setChecked(true);
						objSelected.add(obj);
						totaldata += obj.getQuantity();
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						objSelected.remove(obj);
						totaldata -= obj.getQuantity();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("totaldata")
	public void doSearch() {
		try {
			filter = "tperso.tpersopk = " + obj.getTpersopk() + " and isgetpaket = 'N'";
			if (mbranch != null) {
				if (filter.length() > 0)
					filter += " and ";
				if (mbranch2 != null)
					filter += "mbranch.branchid between '" + mbranch.getBranchid() + "' and '" + mbranch2.getBranchid()
							+ "'";
				else
					filter += "mbranch.branchid = '" + mbranch.getBranchid() + "'";
			}
			objSelected = new ArrayList<Tpersodata>();
			objList = tpersodataDao.listByFilter(filter, "orderdate");
			objSelected.addAll(objList);
			grid.setModel(new ListModelList<Tpersodata>(objList));

			totaldata = tpersodataDao.getSumOrder(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winDeliverybranch, isSaved);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totaldata = 0;
		objForm = new Tpaket();
		objForm.setProcesstime(new Date());
		mbranch = null;
		mbranch2 = null;
		cbBranch.setValue(null);
		cbBranch2.setValue(null);
		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (objSelected.size() > 0) {
			try {
				Messagebox.show("Anda ingin membuat \nmanifest paket?", "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									boolean isError = false;
									String strError = "";
									Session session = StoreHibernateUtil.openSession();
									Transaction transaction = null;
									try {
										transaction = session.beginTransaction();

										objForm.setTperso(obj);
										objForm.setMproduct(obj.getMproduct());
										objForm.setTembossproduct(obj.getTembossproduct());
										objForm.setPaketid(
												new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_PAKET));
										objForm.setProductgroup(
												obj.getTembossproduct().getMproduct().getProductgroup());
										objForm.setTotaldata(totaldata);
										objForm.setTotaldone(0);
										objForm.setOrderdate(obj.getOrderdate());
										objForm.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
										objForm.setBranchpool(oUser.getMbranch().getBranchid());
										objForm.setProcessedby(oUser.getUserid());
										objForm.setProcesstime(new Date());
										oDao.save(session, objForm);

										for (Tpersodata tpersodata : objSelected) {
											Tpaketdata data = new Tpaketdata();
											data.setTpaket(objForm);
											data.setTembossbranch(tpersodata.getTembossbranch());
											data.setNopaket(new TcounterengineDAO().generateNopaket());
											data.setProductgroup(tpersodata.getTembossbranch().getTembossproduct()
													.getMproduct().getProductgroup());
											data.setMbranch(tpersodata.getTembossbranch().getMbranch());
											data.setOrderdate(tpersodata.getTperso().getOrderdate());
											data.setQuantity(tpersodata.getQuantity());
											data.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
											data.setIsdlv("");
											dataDao.save(session, data);

											tpersodata.setIsgetpaket("Y");
											tpersodataDao.save(session, tpersodata);

											if (obj.getTpersoupload() == null) {
												tpersodata.getTembossbranch()
														.setStatus(AppUtils.STATUSBRANCH_PROSESPAKET);
											}
											tpersodata.getTembossbranch().setDlvstarttime(new Date());
											tembossbranchDao.save(session, tpersodata.getTembossbranch());

										}

										obj.setTotalpaket(obj.getTotalpaket() + totaldata);
										if (obj.getTotaldata().equals(obj.getTotalpaket()))
											obj.setIsgetallpaket("Y");

										tpersoDao.save(session, obj);
										transaction.commit();
									} catch (HibernateException e) {
										isError = true;
										if (strError.length() > 0)
											strError += ". \n";
										strError = e.getMessage();
										transaction.rollback();
										e.printStackTrace();
									} catch (Exception e) {
										isError = true;
										if (strError.length() > 0)
											strError += ". \n";
										strError = e.getMessage();
										transaction.rollback();
										e.printStackTrace();
									} finally {
										session.close();
									}
									
									try {
										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketlist'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(), objForm.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
										
										mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketorder'");
										NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD,
												oUser.getMbranch().getBranchlevel());
									} catch (Exception e) {
										e.printStackTrace();
									}

									if (isError) {
										Messagebox.show(strError, WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.ERROR);
										doReset();
										BindUtils.postNotifyChange(null, null, PaketBranchVm.this, "*");
									} else {
										isSaved = new Boolean(true);
										if (objList.size() == objSelected.size()) {
											Clients.showNotification("Submit data paket berhasil", "info", null,
													"middle_center", 2000);
											doClose();
										} else {
											Messagebox.show(
													"Pembuatan manifest paket berhasil. No Manifest : "
															+ objForm.getPaketid(),
													WebApps.getCurrent().getAppName(), Messagebox.OK,
													Messagebox.INFORMATION);
											doReset();
											BindUtils.postNotifyChange(null, null, PaketBranchVm.this, "*");
										}
									}
								}
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {

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

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

	public Mbranch getMbranch2() {
		return mbranch2;
	}

	public void setMbranch2(Mbranch mbranch2) {
		this.mbranch2 = mbranch2;
	}

	public Tperso getObj() {
		return obj;
	}

	public void setObj(Tperso obj) {
		this.obj = obj;
	}

	public Tpaket getObjForm() {
		return objForm;
	}

	public void setObjForm(Tpaket objForm) {
		this.objForm = objForm;
	}

}
