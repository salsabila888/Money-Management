package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
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
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TpersoListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PersoApprovalVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private TpersoListModel model;
	private TpersoDAO oDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private TembossproductDAO tepDao = new TembossproductDAO();
	private TembossbranchDAO tebDao = new TembossbranchDAO();
	private ToutgoingDAO toutgoingDao = new ToutgoingDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String action;
	private String decisionmemo;
	private String arg;

	private List<Tperso> objSelected = new ArrayList<Tperso>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Textbox tbMemo;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.arg = arg;

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tperso>() {

				@Override
				public void render(Row row, final Tperso data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked())
								objSelected.add((Tperso) checked.getAttribute("obj"));
							else
								objSelected.remove((Tperso) checked.getAttribute("obj"));
						}
					});
					row.getChildren().add(check);
					A a = new A(data.getPersoid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);

							Window win = (Window) Executions.createComponents("/view/perso/persodata.zul", null, map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getPersostarttime())));
					row.getChildren().add(new Label(data.getMproduct().getProductcode()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
					row.getChildren().add(new Label(data.getPersostartby()));
				}

			});
		}
		doReset();

	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				objSelected = new ArrayList<Tperso>();
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					if (checked) {
						chk.setChecked(true);
						objSelected.add((Tperso) chk.getAttribute("obj"));
					} else {
						chk.setChecked(false);
						objSelected.remove((Tperso) chk.getAttribute("obj"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("decisionmemo")
	public void doActionSelected(@BindingParam("item") String item) {
		if (item != null && item.equals(AppUtils.STATUS_PERSO_PERSODECLINE)) {
			tbMemo.setDisabled(false);
		} else {
			decisionmemo = "";
			tbMemo.setDisabled(true);
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (arg.equals("P")) {
			filter = "tperso.status = '" + AppUtils.STATUS_PERSO_PERSOWAITAPPROVAL
					+ "' and tderivatifproductfk is null";

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		decisionmemo = null;
		objSelected = new ArrayList<Tperso>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpersopk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpersoListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (arg.equals("P"))
			doSaveOpr();
		else if (arg.equals("V"))
			doSaveVendor();
	}

	@Command
	@NotifyChange("*")
	public void doSaveOpr() {
		if (objSelected.size() > 0) {
			if (action != null && action.length() > 0) {
				if (action.equals(AppUtils.STATUS_DECLINE)
						&& (decisionmemo == null || decisionmemo.trim().length() == 0)) {
					Messagebox.show("Anda harus mengisi alasan decline pada field Memo", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				} else {
					for (Tperso obj : objSelected) {
						Session session = StoreHibernateUtil.openSession();
						Transaction transaction = session.beginTransaction();
						try {
							if (action.equals(AppUtils.STATUS_APPROVED)) {
								obj.setStatus(AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL);

								/*
								 * if(obj.getTpersoupload() == null) {
								 * obj.getTembossproduct().setStatus(AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL)
								 * ; tepDao.save(session, obj.getTembossproduct()); }
								 */

								Toutgoing toutgoing = new Toutgoing();
								toutgoing.setOutgoingid(
										new TcounterengineDAO().generateCounter(AppUtils.CE_INVENTORY_OUTGOING));
								toutgoing.setTperso(obj);
								toutgoing.setMproduct(obj.getMproduct());
								toutgoing.setEntryby(obj.getPersostartby());
								toutgoing.setEntrytime(new Date());
								toutgoing.setItemqty(obj.getTotaldata());
								toutgoing.setLastupdated(new Date());
								toutgoing.setMemo(obj.getMemo());
								toutgoing.setProductgroup(AppUtils.PRODUCTGROUP_CARD);
								toutgoing.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL);
								toutgoing.setUpdatedby(oUser.getUserid());
								toutgoingDao.save(session, toutgoing);

								for (Tpersodata persodata : tpersodataDao
										.listByFilter("tperso.tpersopk = " + obj.getTpersopk(), "tpersodatapk")) {
									Tembossbranch objBranch = persodata.getTembossbranch();
									objBranch.setStatus(AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL);
									tebDao.save(session, objBranch);
								}

//								Mmenu mmenu = new MmenuDAO().findByFilter(
//										"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persoapproval'");
//								NotifHandler.delete(session, mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD,
//										oUser.getMbranch().getBranchlevel());
							} else {
								obj.setStatus(AppUtils.STATUS_PERSO_PERSODECLINE);

								obj.getTembossproduct().setStatus(AppUtils.STATUS_ORDER);
								obj.getTembossproduct()
										.setTotalproses(obj.getTembossproduct().getTotalproses() - obj.getTotaldata());
								obj.getTembossproduct().setOrderos(obj.getTembossproduct().getTotaldata()
										- obj.getTembossproduct().getTotalproses());
								tepDao.save(session, obj.getTembossproduct());

								for (Tpersodata persodata : tpersodataDao
										.listByFilter("tperso.tpersopk = " + obj.getTpersopk(), "tpersodatapk")) {
									Tembossbranch objBranch = persodata.getTembossbranch();
									objBranch.setTotalproses(0);
									objBranch.setTotalos(objBranch.getTotaldata());
									if (obj.getTpersoupload() == null) {
										objBranch.setStatus(AppUtils.STATUSBRANCH_PENDINGPRODUKSI);
									}
									tebDao.save(session, objBranch);

								}

								Mproducttype mproducttype = obj.getMproduct().getMproducttype();
								mproducttype.setStockreserved(mproducttype.getStockreserved() - obj.getTotaldata());
								mproducttypeDao.save(session, mproducttype);

							}

							obj.setDecisionby(oUser.getUserid());
							obj.setDecisiontime(new Date());
							obj.setDecisionmemo(decisionmemo);
							oDao.save(session, obj);
							
							Mmenu mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persoapproval'");
							NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD,
									oUser.getMbranch().getBranchlevel());

							transaction.commit();

						} catch (Exception e) {
							transaction.rollback();
							e.printStackTrace();
						} finally {
							session.close();
						}

						try {
							if (action.equals(AppUtils.STATUS_APPROVED)) {
								Mmenu mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'approval'");
								NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD,
										oUser.getMbranch().getBranchlevel());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					Clients.showNotification("Proses approval manifest perso berhasil", "info", null, "middle_center",
							3000);
					doReset();

				}
			} else {
				Messagebox.show("Silahkan pilih keputusan", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} else {
			Messagebox.show("Silahkan pilih data untuk proses submit", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doSaveVendor() {
		if (objSelected.size() > 0) {
			if (action != null && action.length() > 0) {
				if (action.equals(AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL)
						|| (action.equals(AppUtils.STATUS_PERSO_PERSODECLINE) && decisionmemo != null
								&& decisionmemo.trim().length() > 0)) {
					/*
					 * Session session = StoreHibernateUtil.openSession(); Transaction transaction =
					 * session.beginTransaction(); try { String status =
					 * action.equals(AppUtils.STATUS_APPROVED) ? AppUtils.STATUS_VENDOR_ENTRY :
					 * AppUtils.STATUS_VENDOR_DECLINE; for (Tperso obj : objSelected) {
					 * oDao.updateSql(session, obj.getTpersopk(), "isapprovalvendor = 'Y'"); if
					 * (action.equals(AppUtils.STATUS_DECLINE)) { oDao.updatePersotypeSql(session,
					 * "tpersopk = " + obj.getTpersopk()); }
					 * 
					 * List<Torderdata> objList = torderdataDao.listByFilter("tpersofk = " +
					 * obj.getTpersopk(), "torderdatapk"); for (Torderdata data : objList) {
					 * Tordervendor tordervendor = new Tordervendor();
					 * tordervendor.setTorderdata(data); tordervendor.setOrdertime(new Date());
					 * tordervendor.setStatus(status);
					 * tordervendor.setOrderdate(data.getOrderdate());
					 * tordervendor.setMproduct(data.getMproduct());
					 * tordervendor.setMbranch(data.getMbranch());
					 * tordervendor.setMpersovendor(oUser.getMpersovendor());
					 * tordervendor.setTperso(obj); tordervendor.setIsslastart("Y");
					 * tordervendor.setSlavendor(0); tordervendorDao.save(session, tordervendor); }
					 * }
					 * 
					 * transaction.commit();
					 * 
					 * if (action.equals(AppUtils.STATUS_APPROVED))
					 * Clients.showNotification("Approval data order perso berhasil", "info", null,
					 * "middle_center", 3000); else
					 * Clients.showNotification("Decline data order perso berhasil", "info", null,
					 * "middle_center", 3000); doReset(); } catch (Exception e) {
					 * transaction.rollback(); e.printStackTrace(); } finally { session.close(); }
					 */
				} else {
					Messagebox.show("Anda harus mengisi alasan decline pada field Memo", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
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

	public String getDecisionmemo() {
		return decisionmemo;
	}

	public void setDecisionmemo(String decisionmemo) {
		this.decisionmemo = decisionmemo;
	}
}
