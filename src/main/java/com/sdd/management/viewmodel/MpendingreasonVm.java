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
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MpendingreasonDAO;
import com.sdd.caption.domain.Mpendingreason;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.MpendingreasonListModel;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MpendingreasonVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	
	private Session session;
	private Transaction transaction;
		
	private MpendingreasonListModel model;
	private MpendingreasonDAO oDao = new MpendingreasonDAO();
	
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;	
	private String filter;
	private String orderby;
	private boolean isInsert;
	
	private Mpendingreason objForm;
	private String pendingreason;
	
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
			listbox.setItemRenderer(new ListitemRenderer<Mpendingreason>() {

				@Override
				public void render(Listitem item, final Mpendingreason data, int index)
						throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getPendingreason()));
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
					
				}							
			}
		});
	}
	
	public void refreshModel(int activePage) {
		orderby = "pendingreason";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MpendingreasonListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (pendingreason != null && pendingreason.trim().length() > 0) {
			if (filter.length() > 0) filter += " and ";
			filter += "pendingreason like '%" + pendingreason.trim().toUpperCase() + "%'";
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
			Muser oUser = (Muser) zkSession.getAttribute("oUser");
			if (oUser == null) oUser = new Muser();
			
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
				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 3000);
			} else Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
			doReset();
		} catch (HibernateException e) {
			transaction.rollback();
			if (isInsert)
				objForm.setMpendingreasonpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			if (isInsert)
				objForm.setMpendingreasonpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("objForm")
	public void delete() {
		try {
			Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

				@Override
				public void onEvent(Event event)
						throws Exception {
					if (event.getName().equals("onOK")) {
						try {
							session = StoreHibernateUtil.openSession();
							transaction = session.beginTransaction();
							oDao.delete(session, objForm);
							transaction.commit();
							session.close();	
							
							Clients.showNotification(Labels.getLabel("common.delete.success"), "info", null, "middle_center", 3000);
							
							needsPageUpdate = true;
							doReset();
							BindUtils.postNotifyChange(null, null, MpendingreasonVm.this, "objForm");
						} catch (HibernateException e) {	
							transaction.rollback();
							Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
							e.printStackTrace();
						} catch (Exception e) {	
							Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
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
		objForm = new Mpendingreason();
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));	
	}
	
	public Validator getValidator() {
		return new AbstractValidator() {
			
			@Override
			public void validate(ValidationContext ctx) {												
				String pendingreason = (String) ctx.getProperties("pendingreason")[0]
						.getValue();	
					
				if (pendingreason == null || "".equals(pendingreason.trim())) 
					this.addInvalidMessage(ctx, "pendingreason",
							Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public Mpendingreason getObjForm() {
		return objForm;
	}

	public void setObjForm(Mpendingreason objForm) {
		this.objForm = objForm;
	}

	public String getPendingreason() {
		return pendingreason;
	}

	public void setPendingreason(String pendingreason) {
		this.pendingreason = pendingreason;
	}
	
}
