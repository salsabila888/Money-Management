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

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OrderManualCardVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private TorderDAO oDao = new TorderDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private Torder objForm;

	private String productgroup;
	private String title;
	private Integer totalqty;

	@Wire
	private Combobox cbBranch;
	@Wire
	private Combobox cbProductgroup;
	@Wire
	private Combobox cbTypeProduct;
	@Wire
	private Row row, rowBranch;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (zkSession.getAttribute("arg") != null)
			productgroup = (String) zkSession.getAttribute("arg");
		else
			productgroup = arg;
		title = AppData.getProductgroupLabel(productgroup);

		doReset();
	}

	@Command
	@NotifyChange("*")
	public void doSave() {

		Integer stockreserved = objForm.getMproduct().getMproducttype().getStockreserved();
		if (stockreserved == null)
			stockreserved = 0;
		
		if (objForm.getMproduct().getMproducttype().getLaststock() != null
				&& totalqty <= objForm.getMproduct().getMproducttype().getLaststock() - stockreserved) {
			
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			try {
				objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
				objForm.setOrderid(new TcounterengineDAO().generateCounter(AppUtils.ID_CARD_PRODUCTION));
				objForm.setInsertedby(oUser.getUserid());
				objForm.setStatus(AppUtils.STATUS_ORDER_WAITAPPROVAL);
				objForm.setInserttime(new Date());
				objForm.setTotalqty(totalqty);
				objForm.setItemqty(totalqty);
				objForm.setTotalproses(0);
				objForm.setOrderdate(new Date());
				objForm.setProductgroup(productgroup);

				objForm.getMproduct().getMproducttype()
						.setStockreserved(objForm.getMproduct().getMproducttype().getStockreserved() != null
								? objForm.getMproduct().getMproducttype().getStockreserved() + objForm.getTotalqty()
								: objForm.getTotalqty());
				mproducttypeDao.save(session, objForm.getMproduct().getMproducttype());
				oDao.save(session, objForm);

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
		totalqty = 0;

	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {

					Mproduct mproduct = (Mproduct) ctx.getProperties("mproduct")[0].getValue();

					if (mproduct == null)
						this.addInvalidMessage(ctx, "mproduct", Labels.getLabel("common.validator.empty"));
				} catch (Exception e) {
					e.printStackTrace();
				}
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

	public Integer getTotalqty() {
		return totalqty;
	}

	public void setTotalqty(Integer totalqty) {
		this.totalqty = totalqty;
	}
}
