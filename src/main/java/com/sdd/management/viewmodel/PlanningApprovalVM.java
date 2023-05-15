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
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.bind.annotation.ContextParam;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TplanListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PlanningApprovalVM {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TplanDAO oDao = new TplanDAO();

	private TplanListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String productgroup;
	private String action;
	private boolean isPFA = false;
	private boolean isOPR = false;
	private String decisiondesc;
	private List<Tplan> objSelected = new ArrayList<Tplan>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Radiogroup rgapproval;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("isOPR") String opr, @ExecutionArgParam("isPFA") String pfa) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroup = arg;

		if (pfa != null && pfa.equals("Y"))
			isPFA = true;

		if (opr != null)
			isOPR = true;

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tplan>() {
			@Override
			public void render(Row row, final Tplan data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						if (checked.isChecked())
							objSelected.add((Tplan) checked.getAttribute("obj"));
						else
							objSelected.remove((Tplan) checked.getAttribute("obj"));
					}
				});
				row.getChildren().add(check);
				if (!arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
					A a = new A(data.getPlanno());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);
							map.put("arg", arg);
							Window win = (Window) Executions.createComponents("/view/planning/planningdata.zul", null,
									map);
							win.setWidth("60%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
				} else {
					row.getChildren().add(new Label(data.getPlanno()));
				}
				row.getChildren().add(new Label(
						data.getTotalqty() != null ? NumberFormat.getInstance().format(data.getTotalqty()) : "-"));
				row.getChildren()
						.add(new Label(data.getAnggaran() != null
								? "Rp " + NumberFormat.getInstance().format(data.getAnggaran())
								: "-"));
				row.getChildren().add(
						new Label(data.getMemono() != null && !data.getMemono().equals("") ? data.getMemono() : "-"));
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getInputtime())));
				row.getChildren()
						.add(new Label(data.getStatus() != null && !data.getStatus().equals("")
								? AppData.getStatusLabel(data.getStatus())
								: "-"));
			}
		});
		doReset();
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			objSelected = new ArrayList<Tplan>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Tplan) chk.getAttribute("obj"));
				} else {
					chk.setChecked(false);
					objSelected.remove((Tplan) chk.getAttribute("obj"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (isPFA)
			filter = "status = '" + AppUtils.STATUS_PLANNING_WAITAPPROVALPFA + "' and productgroup = '" + productgroup
					+ "'";
		else if (isOPR)
			filter = "status = '" + AppUtils.STATUS_PLANNING_WAITAPPROVALOPR + "' and productgroup = '" + productgroup
					+ "'";
		else
			filter = "status = '" + AppUtils.STATUS_PLANNING_WAITAPPROVAL + "' and productgroup = '" + productgroup
					+ "' and mbranchfk = " + oUser.getMbranch().getMbranchpk();

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		decisiondesc = null;
		objSelected = new ArrayList<Tplan>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tplanpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TplanListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
				if (action.equals(AppUtils.STATUS_APPROVED) || (action.equals(AppUtils.STATUS_DECLINE)
						&& decisiondesc != null && decisiondesc.trim().length() > 0)) {
					try {
						for (Tplan data : objSelected) {
							Session session = StoreHibernateUtil.openSession();
							Transaction transaction = session.beginTransaction();
							data.setDecisionby(oUser.getUsername());
							data.setDecisiontime(new Date());
							data.setDecisiondesc(decisiondesc);

							if (action.equals(AppUtils.STATUS_APPROVED)) {
								if (isPFA) {
									data.setStatus(AppUtils.STATUS_PLANNING_APPROVED);
									data.setDecisionbypfa(oUser.getUsername());
									data.setDecisiontimepfa(new Date());
								} else if (isOPR) {
									data.setStatus(AppUtils.STATUS_PLANNING_WAITAPPROVALPFA);
									data.setDecisionbypfa(oUser.getUsername());
									data.setDecisiontimepfa(new Date());
								} else {
									if (data.getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)
											|| data.getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN))
										data.setStatus(AppUtils.STATUS_PLANNING_WAITAPPROVALPFA);
								}
							} else {
								if (decisiondesc != null || !decisiondesc.equals("")
										|| decisiondesc.trim().length() > 0) {
									if (isPFA) {
										data.setStatus(AppUtils.STATUS_PLANNING_DECLINEBYPFA);
										data.setIsdecline("N");
										data.setDecisionbypfa(oUser.getUsername());
										data.setDecisiontimepfa(new Date());
									} else if (isOPR) {
										data.setIsdecline("N");
										data.setStatus(AppUtils.STATUS_PLANNING_DECLINEBYOPR);
										data.setDecisionbypfa(oUser.getUsername());
										data.setDecisiontimepfa(new Date());
									} else {
										data.setStatus(AppUtils.STATUS_PLANNING_DECLINEBYLEAD);
									}
								}
							}
							oDao.save(session, data);
							transaction.commit();
							session.close();

							if (action.equals(AppUtils.STATUS_APPROVED)) {
								if (!isPFA) {
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/planning/planingapprovalbydiv.zul' and menuparamvalue = 'pfa'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());

									mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/planning/planingapprovalbydiv.zul' and menuparamvalue = 'opr'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());

									mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/planning/planning.zul' and menuparamvalue = 'approval'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());
								} else {
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/planning/planingapprovalbydiv.zul' and menuparamvalue = 'pfa'");
									NotifHandler.delete(mmenu, data.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());
								}
							} else {
								if (!isPFA) {
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/planning/planning.zul' and menuparamvalue = 'approval'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());
									
									mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/planning/planingapprovalbydiv.zul' and menuparamvalue = 'opr'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());

								} else {
									Mmenu mmenu = new MmenuDAO().findByFilter(
											"menupath = '/view/planning/planingapprovalbydiv.zul' and menuparamvalue = 'pfa'");
									NotifHandler.delete(mmenu, data.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());
									
									if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/planning/planning.zul' and menuparamvalue = 'listfail'");
										NotifHandler.doNotif(mmenu, data.getMbranch(), productgroup,
												data.getMbranch().getBranchlevel());
									}		
								} 
							}
						}
						
						if (action.equals(AppUtils.STATUS_APPROVED)) {
							Clients.showNotification("Proses persetujuan data berhasil disetujui", "info", null, "middle_center",
									3000);
						} else {
							Clients.showNotification("Proses persetujuan data berhasil ditolak", "info", null, "middle_center",
									3000);
						}
						
						doReset();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Messagebox.show("Anda harus mengisi kolom memo", "Info", Messagebox.OK, Messagebox.INFORMATION);
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

	public String getDecisiondesc() {
		return decisiondesc;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setDecisiondesc(String decisiondesc) {
		this.decisiondesc = decisiondesc;
	}

}