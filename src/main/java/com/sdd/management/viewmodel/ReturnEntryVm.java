package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
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
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.domain.Mpersovendor;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mreturnreason;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ReturnEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private TreturnDAO oDao = new TreturnDAO();
	
	private Session session;
	private Transaction transaction;
		
	private Treturn objForm;	
	private Muser muser;
	private Mreturnreason mreturnreason;	
	private String productgroupcode;
	private Integer total;
	private int gridno;
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");	
	
	@Wire
	private Row rowVendor;
	@Wire
	private Combobox cbProduct;
	@Wire
	private Combobox cbPersovendor;
	@Wire
	private Combobox cbReturnreason;
	@Wire
	private Textbox tbName;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;
	
	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroupcode = AppUtils.PRODUCTGROUP_CARD;		
		doReset();
	}

	public void addGriddata(final Treturn data) {
		final Row row = new Row();
		row.appendChild(new Label(String.valueOf(++gridno)));
		row.appendChild(new Label(datelocalFormatter.format(data.getReturndate())));
		row.appendChild(new Label(data.getMproduct().getProductname()));
		row.appendChild(new Label(data.getCardno()));				
		row.appendChild(new Label(data.getName()));
		row.appendChild(new Label(data.getMpersovendor() != null ? data.getMpersovendor().getVendorcode() : "OPR"));
		row.appendChild(new Label(data.getDescription()));
		row.appendChild(new Label(data.getProdoprname()));
		Button btndel = new Button("Hapus");
		btndel.setAutodisable("self");
		btndel.setClass("btn btn-danger btn-sm");
		btndel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
					Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event)
								throws Exception {
							if (event.getName().equals("onOK")) {
								doDelete(data);
								grid.getRows().removeChild(row);																												
							} 									
						}				
					});				
			}
			
		});
		row.appendChild(btndel);
		grid.getRows().insertBefore(row, grid.getRows().getFirstChild());
	}
	
	@Command
	public void doProductSelected(@BindingParam("item") Mproduct item) {
		if (item != null) {
			if (item.getIsinstant().equals("Y")) {
				tbName.setDisabled(true);
			} else {
				tbName.setDisabled(false);
			}
		}		
	}
	
	@Command
	public void doPersotypeSelected(@BindingParam("item") String item) {
		if (item.equals("I")) {
			rowVendor.setVisible(false);
		} else if (item.equals("E")) {
			rowVendor.setVisible(true);
		} 
	}
	
	@Command
	@NotifyChange("*")
	public void doDelete(Treturn obj) {
		session = StoreHibernateUtil.openSession();
		transaction = session.beginTransaction();
		try {						
			oDao.delete(session, obj);			
			transaction.commit();			
			Clients.showNotification(
					"Hapus data entri stock berhasil.",
					"info", null, "middle_center", 3000);
		} catch (HibernateException e){
			transaction.rollback();			
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doSave() {
		session = StoreHibernateUtil.openSession();
		transaction = session.beginTransaction();
		try {						
			if (objForm.getMproduct().getIsinstant().equals("Y"))
				objForm.setName("INSTAN");
			objForm.setProductgroup(productgroupcode);
			objForm.setDescription(mreturnreason.getReturnreason());
			objForm.setProdoprid(muser.getUserid());
			objForm.setProdoprname(muser.getUsername());
			objForm.setEntryby(oUser.getUserid());
			objForm.setEntrytime(new Date());
			oDao.save(session, objForm);			
			transaction.commit();			
			Clients.showNotification(
					"Entri data return berhasil",
					"info", null, "middle_center", 3000);
			addGriddata(objForm);
			doReset();
		} catch (HibernateException e){
			transaction.rollback();			
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
		
	@NotifyChange("*")
	public void doReset() {
		objForm = new Treturn();
		objForm.setEntrytime(new Date());		
		objForm.setProdtype("I");
		cbProduct.setValue(null);
		cbReturnreason.setValue(null);
		cbPersovendor.setValue(null);
		rowVendor.setVisible(false);
		muser = null;
		mreturnreason = null;
	}
	
	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mproduct mproduct = (Mproduct) ctx
						.getProperties("mproduct")[0].getValue();
				Date returndate = (Date) ctx.getProperties("returndate")[0].getValue();
				String cardno = (String) ctx.getProperties("cardno")[0].getValue();
				String name = (String) ctx.getProperties("name")[0].getValue();
				String prodtype = (String) ctx
						.getProperties("prodtype")[0].getValue();
				Mpersovendor mpersovendor = (Mpersovendor) ctx
						.getProperties("mpersovendor")[0].getValue();
				
				if (prodtype.equals("E") && (mpersovendor == null))
					this.addInvalidMessage(ctx, "mpersovendor",
							Labels.getLabel("common.validator.empty"));
				
				if (mproduct == null)
					this.addInvalidMessage(ctx, "mproduct",
							Labels.getLabel("common.validator.empty"));
				else if (mproduct.getIsinstant().equals("N") && (name == null || name.trim().length() == 0))
					this.addInvalidMessage(ctx, "name",
							Labels.getLabel("common.validator.empty"));	
				if (returndate == null)
					this.addInvalidMessage(ctx, "returndate",
							Labels.getLabel("common.validator.empty"));
				if (cardno == null || cardno.trim().length() == 0)
					this.addInvalidMessage(ctx, "cardno",
							Labels.getLabel("common.validator.empty"));				
				if (mreturnreason == null)
					this.addInvalidMessage(ctx, "mreturnreason",
							Labels.getLabel("common.validator.empty"));
				if (muser == null)
					this.addInvalidMessage(ctx, "muser",
							Labels.getLabel("common.validator.empty"));
			}
		};
	}
	
	public ListModelList<Mproduct> getMproduct() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct("productgroupcode = '" + productgroupcode + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public ListModelList<Mpersovendor> getMpersovendormodel() {
		ListModelList<Mpersovendor> lm = null;
		try {
			lm = new ListModelList<Mpersovendor>(AppData.getMpersovendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public ListModelList<Mreturnreason> getMreturnreasonmodel() {
		ListModelList<Mreturnreason> lm = null;
		try {
			lm = new ListModelList<Mreturnreason>(AppData.getMreturnreason());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public ListModelList<Muser> getMusermodel() {
		ListModelList<Muser> lm = null;
		try {
			lm = new ListModelList<Muser>(AppData.getMuser());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}


	public Treturn getObjForm() {
		return objForm;
	}

	public void setObjForm(Treturn objForm) {
		this.objForm = objForm;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getProductgroupcode() {
		return productgroupcode;
	}

	public void setProductgroupcode(String productgroupcode) {
		this.productgroupcode = productgroupcode;
	}

	public Mreturnreason getMreturnreason() {
		return mreturnreason;
	}

	public void setMreturnreason(Mreturnreason mreturnreason) {
		this.mreturnreason = mreturnreason;
	}

	public Muser getMuser() {
		return muser;
	}

	public void setMuser(Muser muser) {
		this.muser = muser;
	}
}
