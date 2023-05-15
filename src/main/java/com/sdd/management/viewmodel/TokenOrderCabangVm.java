package com.sdd.caption.viewmodel;

import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class TokenOrderCabangVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private Session session;
	private Transaction transaction;
		
	private Torder objForm;
	private TorderDAO oDao = new TorderDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();
	private Integer total;
	
	private ListModelList<Mproduct> mproductmodel;
	
	@Wire
	private Combobox cbBranch;
	@Wire
	private Combobox cbTypeProduct;
	@Wire
	private Button btnCreate;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;
	@Wire
	private Groupbox gbData;
	@Wire
	private Row outlet;
	
	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();
		if (oUser.getMbranch().getBranchlevel() == 3) {
			outlet.setVisible(true);
			objForm.setOrderoutlet("00");
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doSave() {
		Integer stockinjected = objForm.getMproduct().getMproducttype().getStockinjected();
		if (stockinjected == null)
			stockinjected = 0;
		if (objForm.getMproduct().getMproducttype().getStockinjected() != null
				&& objForm.getItemqty()  <= objForm.getMproduct().getMproducttype().getStockinjected()) {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			try {
				objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
				objForm.setOrderdate(new Date());
				objForm.setOrderid(new TcounterengineDAO().generateCounter(AppUtils.ID_TOKEN_BRANCH));
				objForm.setStatus(AppUtils.STATUS_PRODUCTION_PRODUKSIWAITAPPROVAL);
				/*if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN))*/
					/*objForm.setMemo(objForm.getMemo() == null ? "(ORDER INVENTORY)"
							: "(ORDER INVENTORY) " + objForm.getMemo().trim().substring(0, 82));*/
				objForm.setInsertedby(oUser.getUserid());
				objForm.setInserttime(new Date());
				objForm.setProductgroup(AppUtils.PRODUCTGROUP_TOKEN);
				objForm.setOrderlevel(oUser.getMbranch().getBranchlevel());
				oDao.save(session, objForm);

				objForm.getMproduct().getMproducttype()
						.setStockreserved(objForm.getMproduct().getMproducttype().getStockreserved() != null
								? objForm.getMproduct().getMproducttype().getStockinjected() - objForm.getItemqty()
								: objForm.getItemqty());
				mproducttypeDao.save(session, objForm.getMproduct().getMproducttype());
				
				transaction.commit();
				Clients.showNotification("Submit data order berhasil", "info", null, "middle_center", 3000);
				doReset();
			} catch (HibernateException e) {
				transaction.rollback();
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				session.close();
			}
		} else {
			Messagebox.show("Stok tidak cukup untuk memenuhi jumlah order", "Info", Messagebox.OK,
					Messagebox.INFORMATION);
		}
	}
		
	@NotifyChange("*")
	public void doReset() {
		total = 0;
		objForm = new Torder();
		objForm.setInserttime(new Date());
		
		cbBranch.setValue(null);		
		cbTypeProduct.setValue(null);
	}
	
	public Validator getValidatorData() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mbranch mbranch = (Mbranch) ctx
						.getProperties("mbranch")[0].getValue();
				Mproduct mproduct = (Mproduct) ctx
						.getProperties("mproduct")[0].getValue();
				Integer itemqty = (Integer) ctx.getProperties("itemqty")[0].getValue();
				
				if (mbranch == null)
					this.addInvalidMessage(ctx, "mbranch",
							Labels.getLabel("common.validator.empty"));
				if (mproduct == null)
					this.addInvalidMessage(ctx, "mproduct",
							Labels.getLabel("common.validator.empty"));
				if (itemqty == null)
					this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));
				if (oUser.getMbranch().getBranchlevel() == 3) {
					String orderoutlet = (String) ctx.getProperties("orderoutlet")[0].getValue();
					if (orderoutlet == null || "".trim().equals(orderoutlet))
						this.addInvalidMessage(ctx, "orderoutlet", Labels.getLabel("common.validator.empty"));
				}
				
			}
		};
	}
	
	public ListModelList<Mbranch> getMbranchmodel() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(AppData.getMbranch());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}	
	
	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct("productgroup = '02'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}	
	
	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public void setMproductmodel(ListModelList<Mproduct> mproductmodel) {
		this.mproductmodel = mproductmodel;
	}

	public Torder getObjForm() {
		return objForm;
	}

	public void setObjForm(Torder objForm) {
		this.objForm = objForm;
	}
	
	
}
