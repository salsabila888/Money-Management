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
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MusergrouplevelDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergrouplevel;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TdeliveryListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DeliveryApprovalVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private TdeliveryListModel model;

	private TdeliveryDAO oDao = new TdeliveryDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String productgroup;
	private Integer total;

	private Muser oUser;
	private List<Tdelivery> objSelected = new ArrayList<Tdelivery>();

	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Radiogroup rgapproval;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		productgroup = arg.trim();
		oUser = (Muser) zkSession.getAttribute("oUser");
		System.out.println("GROUPFK : " + oUser.getMusergroup().getMusergrouppk());
		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});
		if (grid != null) {
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
							if (checked.isChecked()) {
								objSelected.add((Tdelivery) checked.getAttribute("obj"));
							} else {
								objSelected.remove((Tdelivery) checked.getAttribute("obj"));
							}
						}
					});
					row.getChildren().add(check);
					row.getChildren().add(new Label(data.getDlvid()));
					row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
//					row.getChildren().add(new Label(data.getMproducttype().getProducttype()));

					row.getChildren()
							.add(new Label(
									data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata())
											: "0"));
					row.getChildren()
							.add(new Label(data.getTotalamount() != null
									? NumberFormat.getInstance().format(data.getTotalamount())
									: "0"));
					row.getChildren()
							.add(new Label(
									data.getProcesstime() != null ? datetimeLocalFormatter.format(data.getProcesstime())
											: "-"));
					row.getChildren()
							.add(new Label(data.getProcessedby() != null && !data.getProcessedby().equals("")
									? data.getProcessedby()
									: "-"));
					row.getChildren().add(
							new Label(data.getMemo() != null && !data.getMemo().equals("") ? data.getMemo() : "-"));
				}
			});
		}
		doReset();
	}

	@Command
	@NotifyChange({ "pageTotalSize", "total" })
	public void doSearch() {
		try {
			filter = "Tdelivery.productgroup = '" + productgroup + "' and tdelivery.status = '"
					+ AppUtils.STATUS_DELIVERY_WAITAPPROVAL + "' and branchpool = '" + oUser.getMbranch().getBranchid()
					+ "'";

			if (oUser.getMbranch().getBranchlevel() < 3) {
				Musergrouplevel grouplevel = new MusergrouplevelDAO()
						.findByFilter("musergroupfk = " + oUser.getMusergroup().getMusergrouppk());
				if (grouplevel != null) {
					filter += " and (tdelivery.totalamount between " + grouplevel.getAmountstart() + " and "
							+ grouplevel.getAmountend() + ")";
				}
			}

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
			objSelected = new ArrayList<Tdelivery>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				if (checked) {
					chk.setChecked(true);
					objSelected.add((Tdelivery) chk.getAttribute("obj"));
				} else {
					chk.setChecked(false);
					objSelected.remove((Tdelivery) chk.getAttribute("obj"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		objSelected = new ArrayList<Tdelivery>();
		total = 0;
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tdeliverypk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TdeliveryListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
			Session session = StoreHibernateUtil.openSession();
			Transaction transaction = session.beginTransaction();
			try {
				for (Tdelivery obj : objSelected) {
					obj.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
					oDao.save(session, obj);

					List<Tdeliverydata> tddList = new TdeliverydataDAO()
							.listByFilter("tdeliveryfk = " + obj.getTdeliverypk(), "tdeliveryfk");
					for (Tdeliverydata tdd : tddList) {
						if (tdd.getTpaketdata().getTpaket().getTorder() != null) {
							FlowHandler.doFlow(session, null, tdd.getTpaketdata().getTpaket().getTorder(),
									AppData.getStatusLabel(tdd.getTpaketdata().getTpaket().getTorder().getStatus()),
									oUser.getUserid());
						}
					}
				}
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				e.printStackTrace();
			} finally {
				session.close();
			}

			try {
				Musergrouplevel grouplevel = new MusergrouplevelDAO()
						.findByFilter("musergroupfk = " + oUser.getMusergroup().getMusergrouppk());
				if (grouplevel != null) {
					for (Tdelivery obj : objSelected) {
						if (grouplevel.getGrouplevel() == 3) {
							System.out.println("HAPUS");
							Mmenu mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/delivery/deliverymanifestcourier.zul'");
							NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
									oUser.getMbranch().getBranchlevel());
							mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/delivery/cardapprovaldelivery.zul' and menuparamvalue = 'kelompok'");
							NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(),
									oUser.getMbranch().getBranchlevel());
						} else if (grouplevel.getGrouplevel() == 2) {
							Mmenu mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/delivery/deliverymanifestcourier.zul'");
							NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
									oUser.getMbranch().getBranchlevel());
							mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/delivery/cardapprovaldelivery.zul' and menuparamvalue = 'wakil'");
							NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(),
									oUser.getMbranch().getBranchlevel());
						} else if (grouplevel.getGrouplevel() == 1) {
							Mmenu mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/delivery/deliverymanifestcourier.zul'");
							NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
									oUser.getMbranch().getBranchlevel());
							mmenu = new MmenuDAO().findByFilter(
									"menupath = '/view/delivery/cardapprovaldelivery.zul' and menuparamvalue = 'pimpinan'");
							NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(),
									oUser.getMbranch().getBranchlevel());
						}
					}
				}
				Clients.showNotification("Submit data approval berhasil", "info", null, "middle_center", 3000);
				doReset();
			} catch (Exception e) {
				e.printStackTrace();
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

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
}