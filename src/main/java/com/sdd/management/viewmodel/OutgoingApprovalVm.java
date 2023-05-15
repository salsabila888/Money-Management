package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
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
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.MregionDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.dao.TpinmailerfileDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Mregion;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.domain.Tpinmailerfile;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.ToutgoingListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OutgoingApprovalVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private ToutgoingListModel model;

	private ToutgoingDAO oDao = new ToutgoingDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();
	private TbranchstockDAO tbranchstockDao = new TbranchstockDAO();
	private TorderDAO torderDao = new TorderDAO();
	private TpersoDAO tpersoDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private TembossbranchDAO tebDao = new TembossbranchDAO();
	private TembossproductDAO tepDao = new TembossproductDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String action;
	private String decisionmemo;
	private String productgroup;
	private String productcode;
	private String producttype;
	private String productname;
	private String outgoingid;
	private Integer total;

	private Muser oUser;
	private List<Toutgoing> objSelected = new ArrayList<Toutgoing>();

	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Radiogroup rgapproval;
	@Wire
	private Column colBranch, colOutlet;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		productgroup = arg;
		oUser = (Muser) zkSession.getAttribute("oUser");

		if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN) || productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
			colBranch.setVisible(true);
			colOutlet.setVisible(true);
		} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
			colBranch.setVisible(false);
			colOutlet.setVisible(false);
		} else {
			colBranch.setVisible(false);
			colOutlet.setVisible(false);
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
			grid.setRowRenderer(new RowRenderer<Toutgoing>() {

				@Override
				public void render(Row row, final Toutgoing data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked()) {
								objSelected.add((Toutgoing) checked.getAttribute("obj"));
							} else
								objSelected.remove((Toutgoing) checked.getAttribute("obj"));
						}
					});
					row.getChildren().add(check);
					A a = new A(data.getOutgoingid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							String page = "";
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("arg", arg);
							if (data.getTperso() != null) {
								page = "/view/perso/persodata.zul";
								map.put("obj", data.getTperso());
							} else if (data.getProductgroup().equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
								page = "/view/pinmailer/pinmailerbranch.zul";
								map.put("obj", data.getTorder().getTpinmailerfile());
							} else {
								page = "/view/inventory/outgoingdata.zul";
								map.put("obj", data);
							}
							win = (Window) Executions.createComponents(page, null, map);
							win.setWidth("60%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(data.getMproduct().getMproducttype().getProducttype()));
					row.getChildren().add(new Label(data.getMproduct().getProductcode()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
					row.getChildren().add(new Label(data.getTorder() != null
							? data.getTorder().getMbranch() != null ? data.getTorder().getMbranch().getBranchname() : ""
							: ""));
					row.getChildren().add(new Label(data.getTorder() != null ? data.getTorder().getOrderoutlet() : ""));
					row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getEntrytime())));
					row.getChildren().add(new Label(data.getEntryby()));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getItemqty())));
					row.getChildren().add(new Label(data.getMemo() != null ? data.getMemo() : "-"));
					if (data.getTorder() != null) {
						System.out.println(data.getTorder());
						Button btnOrder = new Button("Lihat Data Pemesanan");
						btnOrder.setAutodisable("self");
						btnOrder.setClass("btn-default");
						btnOrder.setStyle(
								"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
						btnOrder.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("obj", data.getTorder());
								map.put("arg", arg);
								Window win = (Window) Executions.createComponents("/view/order/orderdetail.zul", null, map);
								win.setWidth("60%");
								win.setClosable(true);
								win.doModal();
							}
						});

						Div div = new Div();
						div.appendChild(btnOrder);
						row.appendChild(div);
					} else {
						row.getChildren().add(new Label("-"));
					}
				}

			});
		}
		doReset();
	}

	@Command
	@NotifyChange({"pageTotalSize", "total"})
	public void doSearch() {
		try {
			filter = "toutgoing.productgroup = '" + productgroup + "' and toutgoing.status = '"
					+ AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL + "'";
			if (oUser.getMbranch().getBranchlevel() == 1) {
				if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
					filter += " and orderlevel = " + (oUser.getMbranch().getBranchlevel() + 1);
				} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)
						|| productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
					filter += " and orderlevel = 1";
				}
			} else if (oUser.getMbranch().getBranchlevel() == 2) {
				filter += " and orderoutlet = '00' and orderlevel = " + (oUser.getMbranch().getBranchlevel() + 1)
						+ " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
			} else if (oUser.getMbranch().getBranchlevel() == 3) {
				filter += " and orderoutlet != '00' and mbranchpk = " + oUser.getMbranch().getMbranchpk();
			}

			if (outgoingid != null && outgoingid.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "cardno like '%" + outgoingid.trim().toUpperCase() + "%'";
			}

			if (producttype != null && producttype.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
			}
			if (productname != null && productname.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
			}
			if (productcode != null && productcode.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
			}

			if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD)) {
				filter += " and tderivatifproductfk is null";
			}

//			total = 0;
//			for (Toutgoing data : oDao.listFilter(filter, orderby)) {
//				total = total + data.getItemqty();
//			}

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
			objSelected = new ArrayList<Toutgoing>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Toutgoing) chk.getAttribute("obj"));
				} else {
					chk.setChecked(false);
					objSelected.remove((Toutgoing) chk.getAttribute("obj"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		decisionmemo = null;
		action = null;
		total = 0;
		objSelected = new ArrayList<Toutgoing>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		try {
			orderby = "toutgoingpk";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new ToutgoingListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (objSelected.size() > 0) {
			boolean isError = false;
			String strError = "";
			if (action != null && action.length() > 0) {
				if (action.equals(AppUtils.STATUS_DECLINE)
						&& (decisionmemo == null || decisionmemo.trim().length() == 0)) {
					Messagebox.show("Anda harus mengisi Catatan Keputusan", "Info",
							Messagebox.OK, Messagebox.INFORMATION);
				} else {
					if (action.equals(AppUtils.STATUS_APPROVED)) {
						for (Toutgoing obj : objSelected) {
							Session session = StoreHibernateUtil.openSession();
							Transaction transaction = session.beginTransaction();
							try {
								obj.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGAPPROVED);
								obj.setDecisionby(oUser.getUsername());
								obj.setDecisiontime(new Date());
								obj.setDecisionmemo(decisionmemo);
								oDao.save(session, obj);

								if (obj.getTperso() != null) {
									obj.getTperso().setStatus(AppUtils.STATUS_PERSO_PRODUKSI);
									tpersoDao.save(session, obj.getTperso());

									List<Tpersodata> listPersodata = tpersodataDao.listByFilter(
											"tperso.tpersopk = " + obj.getTperso().getTpersopk(), "tpersodatapk");
									for (Tpersodata persodata : listPersodata) {
										persodata.getTembossbranch().setStatus(AppUtils.STATUS_PERSO_PRODUKSI);
										new TembossbranchDAO().save(session, persodata.getTembossbranch());
									}

									Mproducttype objStock = obj.getMproduct().getMproducttype();
									objStock.setStockreserved(objStock.getStockreserved() - obj.getItemqty());
									if(objStock.getStockreserved() < 0)
										objStock.setStockreserved(0);
									objStock.setLaststock(objStock.getLaststock() - obj.getItemqty());
									if(objStock.getLaststock() < 0)
										objStock.setLaststock(0);
									mproducttypeDao.save(session, objStock);

								} else if (obj.getTorder() != null) {
									if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)
											|| productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)
											|| productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
										obj.getTorder().setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGWAITSCAN);

									} else {
										obj.getTorder().setStatus(AppUtils.STATUS_ORDER_PRODUKSI);

										if (obj.getTorder().getMbranch() != null) {
											if (obj.getTorder().getMbranch().getBranchlevel() == 2) {
												Mproducttype objStock = mproducttypeDao.findByPk(
														obj.getMproduct().getMproducttype().getMproducttypepk());
												if (objStock != null) {
													objStock.setStockreserved(
															objStock.getStockreserved() - obj.getItemqty());
													mproducttypeDao.save(session, objStock);
												}
											} else if (obj.getTorder().getMbranch().getBranchlevel() == 3) {
												Mregion region = new MregionDAO().findByPk(
														obj.getTorder().getMbranch().getMregion().getMregionpk());
												Mbranch branch = new MbranchDAO()
														.findByFilter("branchid = '" + region.getRegionid() + "'");

												Tbranchstock objStock = tbranchstockDao.findByFilter(
														"mbranchfk = " + branch.getMbranchpk() + " and mproductfk = "
																+ obj.getTorder().getMproduct().getMproductpk());
												if (objStock != null) {
													objStock.setStockreserved(
															objStock.getStockreserved() - obj.getItemqty());
													tbranchstockDao.save(session, objStock);
												}
											} else if (obj.getTorder().getMbranch().getBranchlevel() == 1) {
												Mproducttype objStock = obj.getMproduct().getMproducttype();
												objStock.setStockreserved(objStock.getStockreserved() - obj.getItemqty());
												if(objStock.getStockreserved() < 0)
													objStock.setStockreserved(0);
												objStock.setLaststock(objStock.getLaststock() - obj.getItemqty());
												if(objStock.getLaststock() < 0)
													objStock.setLaststock(0);
												mproducttypeDao.save(session, objStock);
											} 
										} else {
											Mproducttype objStock = mproducttypeDao
													.findByPk(obj.getMproduct().getMproducttype().getMproducttypepk());
											if (objStock != null) {
												objStock.setStockreserved(
														objStock.getStockreserved() - obj.getItemqty());
												mproducttypeDao.save(session, objStock);
											}
										}
									}
									torderDao.save(session, obj.getTorder());

									FlowHandler.doFlow(session, null, obj.getTorder(), decisionmemo, oUser.getUserid());
								}

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
								if (obj.getProductgroup().trim().equals(AppUtils.PRODUCTGROUP_CARD)
										|| obj.getProductgroup().trim().equals(AppUtils.PRODUCTGROUP_CARDPHOTO)) {
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persolist'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} else if (obj.getProductgroup().trim().equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/order/orderlist.zul' and menuparamvalue = '06'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} else {
									if (obj.getTorder().getMbranch() != null) {
										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'scan'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					} else if (action.equals(AppUtils.STATUS_DECLINE)) {
						for (Toutgoing obj : objSelected) {
							Session session = StoreHibernateUtil.openSession();
							Transaction transaction = session.beginTransaction();
							try {
								obj.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGDECLINE);
								obj.setDecisionby(oUser.getUsername());
								obj.setDecisiontime(new Date());
								obj.setDecisionmemo(decisionmemo);
								oDao.save(session, obj);

								obj.getMproduct().getMproducttype().setStockreserved(
										obj.getMproduct().getMproducttype().getStockreserved() - obj.getItemqty());

								if (obj.getMproduct().getMproducttype().getStockreserved() < 0) {
									obj.getMproduct().getMproducttype().setStockreserved(0);
								}

								mproducttypeDao.save(session, obj.getMproduct().getMproducttype());

								if (obj.getTperso() != null) {
									obj.getTperso().setStatus(AppUtils.STATUS_PERSO_OUTGOINGDECLINE);
									tpersoDao.save(session, obj.getTperso());

									obj.getTperso().getTembossproduct().setStatus(AppUtils.STATUS_ORDER);
									obj.getTperso().getTembossproduct()
											.setTotalproses(obj.getTperso().getTembossproduct().getTotalproses()
													- obj.getTperso().getTotaldata());
									obj.getTperso().getTembossproduct()
											.setOrderos(obj.getTperso().getTembossproduct().getTotaldata()
													- obj.getTperso().getTembossproduct().getTotalproses());
									tepDao.save(session, obj.getTperso().getTembossproduct());

									List<Tpersodata> listPersodata = tpersodataDao.listByFilter(
											"tperso.tpersopk = " + obj.getTperso().getTpersopk(), "tpersodatapk");
									for (Tpersodata persodata : listPersodata) {
										persodata.getTembossbranch().setTotalproses(0);
										persodata.getTembossbranch()
												.setTotalos(persodata.getTembossbranch().getTotaldata());
										persodata.getTembossbranch().setStatus(AppUtils.STATUSBRANCH_PENDINGPRODUKSI);
										tebDao.save(session, persodata.getTembossbranch());

									}
								} else if (obj.getTorder() != null) {
									obj.getTorder().setStatus(AppUtils.STATUS_ORDER_OUTGOINGDECLINE);
									torderDao.save(session, obj.getTorder());

									if (obj.getTorder().getTpinmailerfile() != null) {
										obj.getTorder().getTpinmailerfile().setStatus(AppUtils.STATUS_ORDER);
										new TpinmailerfileDAO().save(session, obj.getTorder().getTpinmailerfile());
									}

									FlowHandler.doFlow(session, null, obj.getTorder(), decisionmemo, oUser.getUserid());

								}
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
						}
					}

					try {
						for (Toutgoing obj : objSelected) {
							Mbranch mbranch = null;
							if (obj.getTorder() != null)
								if(obj.getTorder().getMbranch() != null)
									mbranch = obj.getTorder().getMbranch();
								else
									mbranch = oUser.getMbranch();
							else if (obj.getTrepair() != null)
								mbranch = obj.getTrepair().getMbranch();
							else 
								mbranch = oUser.getMbranch();
							
							if (mbranch != null) {
								Mmenu mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'approval'");
								NotifHandler.delete(mmenu, mbranch, obj.getProductgroup(),
										oUser.getMbranch().getBranchlevel());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (isError)
						Messagebox.show(strError, WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
					else {
						if (action.equals(AppUtils.STATUS_APPROVED)) {
							Clients.showNotification("Proses persetujuan data berhasil disetujui", "info", null, "middle_center",
									3000);
						} else {
							Clients.showNotification("Proses persetujuan data berhasil ditolak", "info", null, "middle_center",
									3000);
						}

						doReset();
					}
				}

			} else {
				Messagebox.show("Silahkan pilih keputusan", "Info", Messagebox.OK, Messagebox.INFORMATION);
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

	public String getDecisionmemo() {
		return decisionmemo;
	}

	public void setDecisionmemo(String decisionmemo) {
		this.decisionmemo = decisionmemo;
	}

	public String getOutgoingid() {
		return outgoingid;
	}

	public void setOutgoingid(String outgoingid) {
		this.outgoingid = outgoingid;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
}
