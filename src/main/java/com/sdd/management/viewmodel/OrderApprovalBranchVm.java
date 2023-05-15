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
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.bind.annotation.ContextParam;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TorderListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OrderApprovalBranchVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private TorderListModel model;

	private TorderDAO oDao = new TorderDAO();
	private ToutgoingDAO toutgoingDao = new ToutgoingDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String productgroup;
	private String action;
	private String decisionmemo;

	private Muser oUser;
	private List<Torder> objSelected = new ArrayList<Torder>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroup = arg;
		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Torder>() {
			@Override
			public void render(Row row, final Torder data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						if (checked.isChecked())
							objSelected.add((Torder) checked.getAttribute("obj"));
						else
							objSelected.remove((Torder) checked.getAttribute("obj"));
					}
				});
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getOrderid()));
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getMproduct().getProductgroup())));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getInserttime())));
				row.getChildren().add(new Label(
						data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : ""));
			}
		});
		doReset();
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			objSelected = new ArrayList<Torder>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Torder) chk.getAttribute("obj"));
				} else {
					chk.setChecked(false);
					objSelected.remove((Torder) chk.getAttribute("obj"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "status = '" + AppUtils.STATUS_PRODUCTION_PRODUKSIWAITAPPROVAL + "' and productgroup = '" + productgroup + "'";
		
		if (oUser.getMbranch().getBranchlevel() == 2) {
			filter += " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
		} else if (oUser.getMbranch().getBranchlevel() == 3) {
			filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();
		}
		
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		decisionmemo = null;
		objSelected = new ArrayList<Torder>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "torderpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TorderListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
						&& decisionmemo != null && decisionmemo.trim().length() > 0)) {
					Session session = StoreHibernateUtil.openSession();
					Transaction transaction = session.beginTransaction();
					try {
						for (Torder obj : objSelected) {
							if (action.equals(AppUtils.STATUS_APPROVED)) {
								obj.setDecisionby(oUser.getUserid());
								obj.setDecisiontime(new Date());
								obj.setStatus(AppUtils.STATUS_ORDER_PRODUKSI);
								
							} else if (action.equals(AppUtils.STATUS_DECLINE)) {
								obj.setStatus(AppUtils.STATUS_ORDER_DECLINEDIV);
							}
							oDao.save(session, obj);

						}
						transaction.commit();
						Clients.showNotification("Submit data approval berhasil", "info", null, "middle_center", 3000);
						doReset();
					} catch (Exception e) {
						transaction.rollback();
						e.printStackTrace();
					} finally {
						session.close();
					}
				} else {
					Messagebox.show("Anda harus mengisi field Decision Memo", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Silahkan pilih action", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} else

		{
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
