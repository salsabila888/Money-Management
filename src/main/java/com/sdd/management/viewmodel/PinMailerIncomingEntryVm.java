package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.text.NumberFormat;
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

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PinMailerIncomingEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private TincomingDAO oDao = new TincomingDAO();
	
	private Session session;
	private Transaction transaction;
		
	private Tincoming objForm;	
	private String productgroupcode;
	private String productgroupname;
	private Integer total;
	private int gridno;
	private int totalpcs;
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");	
	
	@Wire
	private Combobox cbTypeProduct;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;
	
	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroupcode = AppUtils.PRODUCTGROUP_PINMAILER;
		productgroupname = AppData.getProductgroupLabel(productgroupcode);
		doReset();
	}

	@Command
	@NotifyChange("totalpcs")
	public void doCaltotalpcs(@BindingParam("qty") Integer qty, @BindingParam("qtyunit") Integer qtyunit) {
		if (qty != null && qtyunit != null) {
			totalpcs = qty * qtyunit;
		}
	}
	
	public void addGriddata(final Tincoming data) {
		final Row row = new Row();
		row.appendChild(new Label(String.valueOf(++gridno)));
		row.appendChild(new Label(data.getIncomingid()));
		row.appendChild(new Label(data.getMproducttype().getProducttype()));
		row.appendChild(new Label(datelocalFormatter.format(data.getEntrytime())));
		row.appendChild(new Label(NumberFormat.getInstance().format(data.getItemqty())));
		row.appendChild(new Label(data.getMemo()));
		row.appendChild(new Label(AppData.getStatusLabel(data.getStatus())));
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
	@NotifyChange("*")
	public void doDelete(Tincoming obj) {
		if (obj.getStatus().equals(AppUtils.STATUS_APPROVED)) {
			Messagebox.show("Data tidak bisa dihapus karena sudah di approve ", "Info", Messagebox.OK, Messagebox.ERROR);			
		} else {
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
	}
	
	@Command
	@NotifyChange("*")
	public void doSave() {
		session = StoreHibernateUtil.openSession();
		transaction = session.beginTransaction();
		try {						
			objForm.setIncomingid(new TcounterengineDAO()
					.generateCounter(AppUtils.CE_INVENTORY_INCOMING));
			objForm.setProductgroup(productgroupcode);
			objForm.setEntryby(oUser.getUserid());
			objForm.setEntrytime(new Date());
			objForm.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGWAITAPPROVAL);
			objForm.setLastupdated(new Date());
			objForm.setUpdatedby(oUser.getUserid());
			objForm.setHarga(new BigDecimal(0));
			objForm.setMbranch(oUser.getMbranch());
			oDao.save(session, objForm);
			
//			Mmenu menu = new MmenuDAO().findByFilter(
//					"menupath = '/view/inventory/incomingapproval.zul' and menuparamvalue = '06'");
//			if (menu != null) {
//				NotifHandler.doNotif(session, menu, "Data PIN MAILER baru yang harus diApproval Incoming");
//			}
			transaction.commit();	
			Clients.showNotification(
					"Entri data incoming berhasil",
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
		objForm = new Tincoming();
		objForm.setEntrytime(new Date());
		cbTypeProduct.setValue(null);
	}
	
	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mproducttype mproducttype = (Mproducttype) ctx
						.getProperties("mproducttype")[0].getValue();
				Date entrytime = (Date) ctx.getProperties("entrytime")[0].getValue();
				Integer itemqty = (Integer) ctx.getProperties("itemqty")[0].getValue();
				
				if (mproducttype == null)
					this.addInvalidMessage(ctx, "mproducttype",
							Labels.getLabel("common.validator.empty"));
				if (entrytime == null)
					this.addInvalidMessage(ctx, "entrytime",
							Labels.getLabel("common.validator.empty"));
				if (itemqty == null)
					this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));
			}
		};
	}
	
	public ListModelList<Mproducttype> getMproducttype() {
		ListModelList<Mproducttype> lm = null;
		try {
			lm = new ListModelList<Mproducttype>(AppData.getMproducttype("productgroupcode = '" + productgroupcode + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}


	public Tincoming getObjForm() {
		return objForm;
	}

	public void setObjForm(Tincoming objForm) {
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

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

	public int getTotalpcs() {
		return totalpcs;
	}

	public void setTotalpcs(int totalpcs) {
		this.totalpcs = totalpcs;
	}
	

}
