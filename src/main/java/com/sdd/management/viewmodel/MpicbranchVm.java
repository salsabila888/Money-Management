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
import org.zkoss.bind.annotation.ExecutionArgParam;
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
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MpicbranchDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mpicbranch;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.MpicbranchListModel;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MpicbranchVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private Session session;
	private Transaction transaction;
		
	private MpicbranchListModel model;
	private MpicbranchDAO oDao = new MpicbranchDAO();
	
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;	
	private String filter;
	private String orderby;
	private boolean isInsert;
	private Mbranch obj;
	private Boolean isSaved;
	
	private Mpicbranch objForm;
	private String picname;	
	
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
	private Window winPicbranch;
	
	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("data") Mbranch mbranch) {
		Selectors.wireComponents(view, this, false);	
		oUser = (Muser) zkSession.getAttribute("oUser");
		obj = mbranch;
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
			listbox.setItemRenderer(new ListitemRenderer<Mpicbranch>() {

				@Override
				public void render(Listitem item, final Mpicbranch data, int index)
						throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(obj.getBranchname());
					item.appendChild(cell);
					cell = new Listcell(data.getPicname());
					item.appendChild(cell);
					cell = new Listcell(data.getPicphone());
					item.appendChild(cell);
					cell = new Listcell(data.getPicemail());
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
		orderby = "picname";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MpicbranchListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {					
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);	
		listbox.setModel(model);
	}
	
	@Command
	public void doSearch() {
		filter = "mbranchfk = " + obj.getMbranchpk();
		if (picname != null && picname.trim().length() > 0) {
			if (filter.length() > 0) filter += " and ";
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
	public void doClose() {
		isSaved = new Boolean(true);
		Event closeEvent = new Event( "onClose", winPicbranch, isSaved);
		Events.postEvent(closeEvent);
	}
	
	@Command
	@NotifyChange("objForm")
	public void save() {
		try {			
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			objForm.setUpdatedby(oUser.getUserid());
			objForm.setLastupdated(new Date());	
			objForm.setMbranch(obj);
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
				objForm.setMpicbranchpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			if (isInsert)
				objForm.setMpicbranchpk(null);
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
							BindUtils.postNotifyChange(null, null, MpicbranchVm.this, "objForm");
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
		objForm = new Mpicbranch();
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));	
	}
	
	public Validator getValidator() {
		return new AbstractValidator() {
			
			@Override
			public void validate(ValidationContext ctx) {					
				/*String branchname = (String) ctx.getProperties("branchname")[0]
						.getValue();	*/
				String picname = (String) ctx.getProperties("picname")[0]
						.getValue();	
				String picphone = (String) ctx.getProperties("picphone")[0]
						.getValue();	
				String picemail = (String) ctx.getProperties("picemail")[0]
						.getValue();
					
				/*if (branchname == null || "".equals(branchname.trim())) 
					this.addInvalidMessage(ctx, "branchname",
							Labels.getLabel("common.validator.empty"));*/
				if (picname == null || "".equals(picname.trim())) 
					this.addInvalidMessage(ctx, "picname",
							Labels.getLabel("common.validator.empty"));
				if (picphone == null || "".equals(picphone.trim())) 
					this.addInvalidMessage(ctx, "picphone",
							Labels.getLabel("common.validator.empty"));
				if (picemail == null || "".equals(picemail.trim()))
					this.addInvalidMessage(ctx, "picemail",
							Labels.getLabel("common.validator.empty"));	
				else if (!StringUtils.emailValidator(picemail)) 
					this.addInvalidMessage(ctx, "picemail",
							"Invalid e-mail format");
			}
		};
	}
	
	public Mpicbranch getObjForm() {
		return objForm;
	}

	public void setObjForm(Mpicbranch objForm) {
		this.objForm = objForm;
	}

	public String getPicname() {
		return picname;
	}

	public void setPicname(String picname) {
		this.picname = picname;
	}

	public Mbranch getObj() {
		return obj;
	}

	public void setObj(Mbranch obj) {
		this.obj = obj;
	}

	public Boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}	
		
}
