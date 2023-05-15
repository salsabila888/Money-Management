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
import org.zkoss.zhtml.Tr;
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
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MreturnreasonDAO;
import com.sdd.caption.domain.Mreturnreason;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.MreturnreasonListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MreturnreasonVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private MreturnreasonListModel model;
	private MreturnreasonDAO oDao = new MreturnreasonDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mreturnreason objForm;
	private String returnreason;
	private String productgroup;

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
	private Combobox cbProduk;

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
		needsPageUpdate = true;
		doReset();

		if (listbox != null) {
			listbox.setItemRenderer(new ListitemRenderer<Mreturnreason>() {
				@Override
				public void render(Listitem item, final Mreturnreason data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getReturnreason()));
					item.appendChild(cell);
					cell = new Listcell(AppData.getProductgroupLabel(data.getProductgroup()));
					item.appendChild(cell);
					cell = new Listcell(AppData.getProductgroupLabel(data.getIsDestroy()));
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

//					if (objForm.getProductgroup().equals("02")) {
//						rowDestroy.setVisible(false);
//					} else {
//						rowDestroy.setVisible(true);
//					}
					
					productgroup = objForm.getProductgroup();
					cbProduk.setValue(productgroup);
				}
			}
		});
	}

	public void refreshModel(int activePage) {
		orderby = "returnreason";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MreturnreasonListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (returnreason != null && returnreason.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "returnreason like '%" + returnreason.trim().toUpperCase() + "%'";
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
			try {
				Muser oUser = (Muser) zkSession.getAttribute("oUser");
				if (oUser == null)
					oUser = new Muser();

				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				if (isInsert) {
					objForm.setUpdatedby(oUser.getUserid());
					objForm.setLastupdated(new Date());
				} else {
					objForm.setUpdatedby(oUser.getUserid());
					objForm.setLastupdated(new Date());
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
			} catch (HibernateException e) {
				transaction.rollback();
				if (isInsert)
					objForm.setMreturnreasonpk(null);
				Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
			} catch (Exception e) {
				if (isInsert)
					objForm.setMreturnreasonpk(null);
				Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
			}
		} catch (Exception e) {
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
									BindUtils.postNotifyChange(null, null, MreturnreasonVm.this, "objForm");
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

	@Command
	public void doSelect() {
//		if (productgroup.equals("02")) {
//			rowDestroy.setVisible(false);
//			System.out.println("TOKEN");
//		} else {
//			rowDestroy.setVisible(true);
//			System.out.println("SURAT BERHARGA");
//		}
		
		objForm.setProductgroup(productgroup);
	}

	@NotifyChange("objForm")
	public void doReset() {
		isInsert = true;
		productgroup = null;
		cbProduk.setValue(null);
		objForm = new Mreturnreason();
		objForm.setIsDestroy("N");
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				String returnreason = (String) ctx.getProperties("returnreason")[0].getValue();

				if (returnreason == null || "".equals(returnreason.trim()))
					this.addInvalidMessage(ctx, "returnreason", Labels.getLabel("common.validator.empty"));

				if (productgroup == null)
					this.addInvalidMessage(ctx, "productgroup", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public Mreturnreason getObjForm() {
		return objForm;
	}

	public void setObjForm(Mreturnreason objForm) {
		this.objForm = objForm;
	}

	public String getReturnreason() {
		return returnreason;
	}

	public void setReturnreason(String returnreason) {
		this.returnreason = returnreason;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}
}