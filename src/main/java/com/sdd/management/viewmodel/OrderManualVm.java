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
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
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

public class OrderManualVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private TorderDAO oDao = new TorderDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private Torder objForm;

	private String productgroup;
	private String title;
	private String memo;

	@Wire
	private Combobox cbBranch;
	@Wire
	private Combobox cbProductgroup;
	@Wire
	private Combobox cbTypeProduct;
	@Wire
	private Row row, rowBranch, rowPinpad;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if(zkSession.getAttribute("arg") != null)
			productgroup = (String) zkSession.getAttribute("arg");
		else productgroup = arg;
		title = AppData.getProductgroupLabel(productgroup);

		if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
			rowBranch.setVisible(false);
			rowPinpad.setVisible(false);
		} else if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD))
			rowPinpad.setVisible(false);
		else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD))
			row.setVisible(false);

		doReset();
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (objForm.getMproduct().getMproducttype().getLaststock() != null
				&& objForm.getItemqty() <= objForm.getMproduct().getMproducttype().getLaststock()) {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			try {

				if (productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
					objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
				} else if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
					objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL);
					objForm.setOrderid(new TcounterengineDAO().generateCounter(AppUtils.ID_TOKEN_BRANCH));
				} else {
					objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
					objForm.setOrderid(new TcounterengineDAO().generateCounter(AppUtils.ID_CARD_PRODUCTION));
				}
				objForm.setOrderlevel(oUser.getMbranch().getBranchlevel());
				objForm.setInsertedby(oUser.getUserid());
				objForm.setStatus(AppUtils.STATUS_PRODUCTION_PRODUKSIWAITAPPROVAL);
				objForm.setInserttime(new Date());
				objForm.setProductgroup(productgroup);
				objForm.setMemo(memo);
//				objForm.setProdsla(0);
//				objForm.setDlvsla(0);
//				objForm.setSlatotal(0);

				objForm.getMproduct().getMproducttype()
						.setStockreserved(objForm.getMproduct().getMproducttype().getStockreserved() != null
								? objForm.getMproduct().getMproducttype().getStockreserved() + objForm.getItemqty()
								: objForm.getItemqty());
				mproducttypeDao.save(session, objForm.getMproduct().getMproducttype());
				oDao.save(session, objForm);

//				FlowHandler.doFlow(session, null, objForm, AppUtils.PROSES_ORDER, objForm.getMemo(), oUser.getUserid());
				
				Mmenu mmenu = new MmenuDAO().findByFilter(
						"menupath = '/view/order/orderapproval.zul' and menuparamvalue = '" + productgroup +"'");
				NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
						oUser.getMbranch().getBranchlevel());
				
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
		objForm = new Torder();
		objForm.setInserttime(new Date());
		cbTypeProduct.setValue(null);
		cbBranch.setValue(null);
		memo = "";
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mproduct mproduct = (Mproduct) ctx.getProperties("mproduct")[0].getValue();
				Integer itemqty = (Integer) ctx.getProperties("itemqty")[0].getValue();

				if (mproduct == null)
					this.addInvalidMessage(ctx, "mproduct", Labels.getLabel("common.validator.empty"));
				if (itemqty == null)
					this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct("productgroup = '" + productgroup + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
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

	public Torder getObjForm() {
		return objForm;
	}

	public void setObjForm(Torder objForm) {
		this.objForm = objForm;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
}
