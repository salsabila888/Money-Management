package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.model.TorderListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OrderManualApprovalCardVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TorderDAO oDao = new TorderDAO();
	private ToutgoingDAO toutgoingDao = new ToutgoingDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private TorderListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String productgroup;
	private String action;
	private String decisionmemo;

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
						if (checked.isChecked()) {
							objSelected.add((Torder) checked.getAttribute("obj"));
						} else
							objSelected.remove((Torder) checked.getAttribute("obj"));
					}
				});
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getOrderid()));
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getMproduct().getProductgroup())));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getInserttime())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalqty())));
				row.getChildren().add(new Label(data.getMemo()));
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
				Torder obj = (Torder) chk.getAttribute("obj");
				if (checked) {
					chk.setChecked(true);
					objSelected.add(obj);
				} else {
					chk.setChecked(false);
					objSelected.remove(obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "status in ('" + AppUtils.STATUS_ORDER_WAITAPPROVAL + "') and productgroup = '" + productgroup + "'";
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

							obj.setMemo(decisionmemo);
							obj.setDecisionby(oUser.getUserid());
							obj.setDecisiontime(new Date());

							if (obj.getOrdertype().equals(AppUtils.ENTRYTYPE_MANUAL_BRANCH)) {
								if (action.equals(AppUtils.STATUS_APPROVED)) {
									obj.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL);

									Toutgoing toutgoing = new Toutgoing();
									toutgoing.setMproduct(obj.getMproduct());
									toutgoing.setTorder(obj);
									toutgoing.setEntryby(obj.getDecisionby());
									toutgoing.setEntrytime(new Date());
									toutgoing.setItemqty(obj.getTotalqty());
									toutgoing.setLastupdated(new Date());
									toutgoing.setMemo(obj.getMemo());
									toutgoing.setOutgoingid(obj.getOrderid());
									toutgoing.setProductgroup(obj.getMproduct().getProductgroup());
									toutgoing.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL);
									toutgoing.setUpdatedby(oUser.getUserid());
									toutgoingDao.save(session, toutgoing);

									obj.getMproduct().getMproducttype()
											.setStockinjected(obj.getMproduct().getMproducttype().getStockinjected()
													- obj.getTotalqty());
									mproducttypeDao.save(session, obj.getMproduct().getMproducttype());

								} else if (action.equals(AppUtils.STATUS_DECLINE)) {
									obj.setStatus(AppUtils.STATUS_PRODUCTION_DECLINEPRODUKSI);

									Mproducttype mproducttype = obj.getMproduct().getMproducttype();
									mproducttype.setStockreserved(mproducttype.getStockreserved() - obj.getTotalqty());
									mproducttypeDao.save(session, mproducttype);
								}

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
