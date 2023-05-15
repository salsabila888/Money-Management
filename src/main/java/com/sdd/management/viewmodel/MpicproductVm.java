/**
 * 
 */
package com.sdd.caption.viewmodel;

import java.util.Date;

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

import com.sdd.caption.dao.MpicproductDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mpicproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.MpicproductListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MpicproductVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private MpicproductListModel model;
	private MpicproductDAO oDao = new MpicproductDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mpicproduct objForm;
	private String picname;

	@Wire
	private Button btnSave, btnCancel, btnDelete;
	@Wire
	private Combobox cbCabang;
	@Wire
	private Paging paging;
	@Wire
	private Listbox listbox;

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
			listbox.setItemRenderer(new ListitemRenderer<Mpicproduct>() {

				@Override
				public void render(Listitem item, final Mpicproduct data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(AppData.getProductgroupLabel(data.getProductgroup()));
					item.appendChild(cell);
					cell = new Listcell(data.getPicname());
					item.appendChild(cell);
					cell = new Listcell(data.getPichp());
					item.appendChild(cell);
					cell = new Listcell(data.getPicemail());
					item.appendChild(cell);
					cell = new Listcell(data.getMbranch() != null ? data.getMbranch().getBranchname() : "-");
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
					cbCabang.setValue(objForm.getMbranch() != null ? objForm.getMbranch().getBranchname() : "");
				}
			}
		});
	}

	public void refreshModel(int activePage) {
		orderby = "productgroup,picname";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MpicproductListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (picname != null && picname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "picname like '%" + picname.trim().toUpperCase() + "%'";
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
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			objForm.setUpdatedby(oUser.getUserid());
			objForm.setLastupdated(new Date());
			oDao.save(session, objForm);
			transaction.commit();
			session.close();
			if (isInsert) {
				needsPageUpdate = true;
				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 3000);
			} else
				Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
			doReset();
		} catch (HibernateException e) {
			transaction.rollback();
			if (isInsert)
				objForm.setMpicproductpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			if (isInsert)
				objForm.setMpicproductpk(null);
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
									BindUtils.postNotifyChange(null, null, MpicproductVm.this, "objForm");
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
		objForm = new Mpicproduct();
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
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

	public Validator getValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(ValidationContext ctx) {
				try {
					String productgroup = (String) ctx.getProperties("productgroup")[0].getValue();
					String picname = (String) ctx.getProperties("picname")[0].getValue();
					Integer pichp = (Integer) ctx.getProperties("pichp")[0].getValue();
					String picemail = (String) ctx.getProperties("picemail")[0].getValue();
					Mbranch mbranch = (Mbranch) ctx.getProperties("mbranch")[0].getValue();
					
					if (productgroup == null || "".equals(productgroup.trim()))
						this.addInvalidMessage(ctx, "productgroup", Labels.getLabel("common.validator.empty"));
					if (picname == null || "".equals(picname.trim()))
						this.addInvalidMessage(ctx, "picname", Labels.getLabel("common.validator.empty"));
					if (pichp == null)
						this.addInvalidMessage(ctx, "pichp", Labels.getLabel("common.validator.empty"));

					if (picemail.trim() == null || "".equals(picemail.trim()))
						this.addInvalidMessage(ctx, "picemail", Labels.getLabel("common.validator.empty"));
					if (!StringUtils.emailValidator(picemail))
						this.addInvalidMessage(ctx, "picemail", "Invalid e-mail format");
					if (mbranch == null)
						this.addInvalidMessage(ctx, "mbranch", Labels.getLabel("common.validator.empty"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public Mpicproduct getObjForm() {
		return objForm;
	}

	public void setObjForm(Mpicproduct objForm) {
		this.objForm = objForm;
	}

	public String getPicname() {
		return picname;
	}

	public void setPicname(String picname) {
		this.picname = picname;
	}

}
