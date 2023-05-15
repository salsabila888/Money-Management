/**
 * 
 */
package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

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
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MorgDAO;
import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.model.MorgListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MorgVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private MorgListModel model;
	private MorgDAO oDao = new MorgDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Morg objForm;
	private List<Tembossproduct> tepList;
	private String org;
	private String description;

	@Wire
	private Button btnSave;
	@Wire
	private Button btnCancel;
	@Wire
	private Button btnDelete;
	@Wire
	private Paging paging;
	@Wire
	private Listbox listbox;
	@Wire
	private Textbox tbOrg;

	@NotifyChange("*")
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

		needsPageUpdate = true;
		doReset();

		if (listbox != null) {
			listbox.setItemRenderer(new ListitemRenderer<Morg>() {

				@Override
				public void render(Listitem item, final Morg data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(AppData.getProductgroupLabel(data.getOrg()));
					item.appendChild(cell);
					cell = new Listcell(data.getDescription());
					item.appendChild(cell);
					cell = new Listcell(NumberFormat.getInstance().format(data.getOprcapacity()));
					item.appendChild(cell);
					cell = new Listcell(data.getIsneeddoc());
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

					tbOrg.setDisabled(true);
				}
			}
		});
	}

	public void refreshModel(int activePage) {
		orderby = "org";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MorgListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (org != null && org.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "org like '%" + org.trim().toUpperCase() + "%'";
		}
		if (description != null && description.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "description like '%" + description.trim().toUpperCase() + "%'";
		}

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("objForm")
	public void cancel() {
		doReset();
	}

	@Command
	@NotifyChange("objForm")
	public void save() {
		try {
			boolean isValid = true;
			Morg dataorg = new MorgDAO().findByFilter("org = '" + objForm.getOrg() + "'");
			if (isInsert && dataorg != null) {
				isValid = false;
			}
			if (isValid) {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				if (isInsert) {
					objForm.setUpdatedby(oUser.getUserid());
					objForm.setLastupdated(new Date());
				} else {
					objForm.setUpdatedby(oUser.getUserid());
					objForm.setLastupdated(new Date());
				}

				tepList = new TembossproductDAO().listByFilter("org = '" + objForm.getOrg() + "'", "org");
				if (tepList.size() > 0) {
					for (Tembossproduct data : tepList) {
						data.setIsneeddoc(objForm.getIsneeddoc());
						new TembossproductDAO().save(session, data);
					}
				}

				oDao.save(session, objForm);
				transaction.commit();
				session.close();
				if (isInsert) {
					needsPageUpdate = true;
					Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center",
							3000);
				} else
					Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center",
							3000);
				doReset();
			} else {
				Messagebox.show("Gagal menambah org kartu, org '" + objForm.getOrg().trim() + "' sudah terdaftar.",
						"Peringatan", Messagebox.OK, Messagebox.EXCLAMATION);
			}
		} catch (HibernateException e) {
			transaction.rollback();
			if (isInsert)
				objForm.setMorgpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			if (isInsert)
				objForm.setMorgpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("objForm")
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
									BindUtils.postNotifyChange(null, null, MorgVm.this, "objForm");
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

	@NotifyChange("objForm")
	public void doReset() {
		isInsert = true;
		tbOrg.setDisabled(false);
		objForm = new Morg();
		objForm.setIsneeddoc("N");
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				String org = (String) ctx.getProperties("org")[0].getValue();
				String description = (String) ctx.getProperties("description")[0].getValue();
				if (org == null || org.trim().length() == 0)
					this.addInvalidMessage(ctx, "org", Labels.getLabel("common.validator.empty"));
				if (description == null || description.trim().length() == 0)
					this.addInvalidMessage(ctx, "description", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public Morg getObjForm() {
		return objForm;
	}

	public void setObjForm(Morg objForm) {
		this.objForm = objForm;
	}

	public String getOrg() {
		return org;
	}

	public String getDescription() {
		return description;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
