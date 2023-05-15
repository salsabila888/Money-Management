package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
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
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TderivatifListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DerivatifApprovalVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TderivatifListModel model;
	private Tderivatif obj;
	private TderivatifDAO oDao = new TderivatifDAO();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String action;
	private String memo;

	private List<Tderivatif> objSelected = new ArrayList<Tderivatif>();

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tderivatif>() {

				@Override
				public void render(Row row, final Tderivatif data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked())
								objSelected.add((Tderivatif) checked.getAttribute("obj"));
							else
								objSelected.remove((Tderivatif) checked.getAttribute("obj"));
						}
					});
					row.getChildren().add(check);

					row.getChildren().add(new Label(data.getOrderno()));
					if (data.getFilename() != null) {
						A file = new A(data.getFilename());
						file.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {

								zkSession.setAttribute("reportPath",
										AppUtils.FILES_ROOT_PATH + AppUtils.PATH_DERIVATIFFILE + "/" + data.getFilename());
								Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
							}
						});

						row.getChildren().add(file);
					} else
						row.getChildren().add(new Label(""));
					row.getChildren()
							.add(new Label(data.getMproduct() != null ? data.getMproduct().getProductcode() : ""));
					row.getChildren()
							.add(new Label(data.getMproduct() != null ? data.getMproduct().getProductname() : ""));
					row.getChildren().add(new Label(data.getMbranch().getBranchname()));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
					row.getChildren().add(new Label(data.getMemo()));
				}
			});
		}
		doReset();
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				objSelected = new ArrayList<Tderivatif>();
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					if (checked) {
						chk.setChecked(true);
						objSelected.add((Tderivatif) chk.getAttribute("obj"));
					} else {
						chk.setChecked(false);
						objSelected.remove((Tderivatif) chk.getAttribute("obj"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (objSelected.size() > 0) {
			if (action.length() > 0) {
				if (action.equals(AppUtils.STATUS_APPROVED)
						|| (action.equals(AppUtils.STATUS_DECLINE) && memo != null && memo.trim().length() > 0)) {
					Session session = StoreHibernateUtil.openSession();
					Transaction transaction = session.beginTransaction();
					try {

						if (action.equals(AppUtils.STATUS_APPROVED)) {
							for (Tderivatif obj : objSelected) {
								obj.setStatus(AppUtils.STATUS_DERIVATIF_GETDATA);
								oDao.save(session, obj);
								
								Mmenu mmenu = new MmenuDAO()
										.findByFilter("menupath = '/view/derivatif/derivatiflist.zul' and menuparamvalue = 'getdata'");
								NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO, oUser.getMbranch().getBranchlevel());
								
								mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/derivatif/derivatifapproval.zul'");
								NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO,
										oUser.getMbranch().getBranchlevel());
							}
							transaction.commit();
							Clients.showNotification("Approval data surat order cabang berhasil", "info", null,
									"middle_center", 3000);
						} else {
							for (Tderivatif obj : objSelected) {
								obj.setStatus(AppUtils.STATUS_DERIVATIF_ORDERDECLINE);
								obj.setMemo(memo);
								oDao.save(session, obj);
								
								Mmenu mmenu = new MmenuDAO().findByFilter(
										"menupath = '/view/derivatif/deriavtifapproval.zul'");
								NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO,
										oUser.getMbranch().getBranchlevel());
							}
							transaction.commit();
							Clients.showNotification("Decline data surat order cabang berhasil", "info", null,
									"middle_center", 3000);
						}
						doReset();
						
						
					} catch (Exception e) {
						transaction.rollback();
						e.printStackTrace();
					} finally {
						session.close();
					}
					
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

	@NotifyChange("pageTotalSize")
	public void doSearch() {
		try {
			filter = "status = " + AppUtils.STATUS_DERIVATIF_WAITAPPROVAL;
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		memo = null;
		objSelected = new ArrayList<Tderivatif>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tderivatifpk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TderivatifListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	public Tderivatif getObj() {
		return obj;
	}

	public void setObj(Tderivatif obj) {
		this.obj = obj;
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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
}
