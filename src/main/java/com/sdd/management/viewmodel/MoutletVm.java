package com.sdd.caption.viewmodel;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MoutletDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Moutlet;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.MoutletListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MoutletVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private MoutletListModel model;
	private MoutletDAO oDao = new MoutletDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Moutlet objForm;
	private String outletcode;
	private String outletname;
	private String zipcode;

	@Wire
	private Paging paging;
	@Wire
	private Listbox listbox;
	@Wire
	private Combobox cbCabang;
	@Wire
	private Button btnSave;
	@Wire
	private Button btnCancel;
	@Wire
	private Button btnDelete;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);

		paging.addEventListener("onPaging", new EventListener() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (listbox != null) {
			listbox.setItemRenderer(new ListitemRenderer<Moutlet>() {
				@Override
				public void render(Listitem item, final Moutlet data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getOutletcode());
					item.appendChild(cell);
					cell = new Listcell(data.getOutletname());
					item.appendChild(cell);
					cell = new Listcell(data.getAddress());
					item.appendChild(cell);
					cell = new Listcell(data.getOutletcity());
					item.appendChild(cell);
					cell = new Listcell(data.getMbranch() != null ? data.getMbranch().getBranchname() : "");
					item.appendChild(cell);
					cell = new Listcell(data.getZipcode());
					item.appendChild(cell);

				}
			});
		}

		listbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if (listbox.getSelectedIndex() != -1) {
					isInsert = false;
					btnSave.setLabel(Labels.getLabel("common.update"));
					btnCancel.setDisabled(false);
					btnDelete.setDisabled(false);

					cbCabang.setValue(objForm.getMbranch().getBranchname());
				}
			}
		});
		needsPageUpdate = true;
		doReset();
	}

	public void refreshModel(int activePage) {
		orderby = "branchname, outletcode";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MoutletListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		listbox.setModel(model);
	}

	@Command
	public void doSearch() {
		filter = "";
		if (outletcode != null && outletcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "outletcode like '%" + outletcode.trim().toUpperCase() + "%'";
		}
		if (outletname != null && outletname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "outletname like '%" + outletname.trim().toUpperCase() + "%'";
		}
		if (zipcode != null && zipcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "moutlet.zipcode like '%" + zipcode.trim().toUpperCase() + "%'";
		}

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void save() {
		try {
			Muser oUser = (Muser) zkSession.getAttribute("oUser");
			if (oUser == null)
				oUser = new Muser();

			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			oDao.save(session, objForm);
			transaction.commit();
			session.close();
			if (isInsert) {
				needsPageUpdate = true;
				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 3000);
			} else
				Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
			doReset();

		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("*")
	public void delete() {
		try {
			Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								try {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									oDao.delete(session, objForm);
									transaction.commit();
									session.close();

									Clients.showNotification(Labels.getLabel("common.delete.success"), "info", null,
											"middle_center", 3000);

									needsPageUpdate = true;
									doReset();
									BindUtils.postNotifyChange(null, null, MoutletVm.this, "objForm");
								} catch (HibernateException e) {
									transaction.rollback();
									Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								} catch (Exception e) {
									Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								}
							}
						}

					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		isInsert = true;
		objForm = new Moutlet();
		cbCabang.setValue(null);
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
	}

	@Command
	@NotifyChange("*")
	public void cancel() {
		doReset();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {
					String outletcode = (String) ctx.getProperties("outletcode")[0].getValue();
					String outletname = (String) ctx.getProperties("outletname")[0].getValue();
					String address = (String) ctx.getProperties("address")[0].getValue();
					String outletcity = (String) ctx.getProperties("outletcity")[0].getValue();
					String zipcode = (String) ctx.getProperties("zipcode")[0].getValue();
					Mbranch mbranch = (Mbranch) ctx.getProperties("mbranch")[0].getValue();

					if (mbranch == null)
						this.addInvalidMessage(ctx, "mbranch", Labels.getLabel("common.validator.empty"));
					if (outletcode == null || "".equals(outletcode.trim()))
						this.addInvalidMessage(ctx, "outletcode", Labels.getLabel("common.validator.empty"));
					if (outletname == null || "".equals(outletname.trim()))
						this.addInvalidMessage(ctx, "outletname", Labels.getLabel("common.validator.empty"));
					if (address == null || "".equals(address.trim()))
						this.addInvalidMessage(ctx, "address", Labels.getLabel("common.validator.empty"));
					if (outletcity == null || "".equals(outletcity.trim()))
						this.addInvalidMessage(ctx, "outletcity", Labels.getLabel("common.validator.empty"));
					if (zipcode == null || "".equals(zipcode.trim()))
						this.addInvalidMessage(ctx, "zipcode", Labels.getLabel("common.validator.empty"));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
	}

	public ListModelList<Mbranch> getMbranch() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(AppData.getMbranch());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Moutlet getObjForm() {
		return objForm;
	}

	public void setObjForm(Moutlet objForm) {
		this.objForm = objForm;
	}

	public String getOutletcode() {
		return outletcode;
	}

	public void setOutletcode(String outletcode) {
		this.outletcode = outletcode;
	}

	public String getOutletname() {
		return outletname;
	}

	public void setOutletname(String outletname) {
		this.outletname = outletname;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
}
