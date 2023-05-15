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
import org.zkoss.zhtml.Tr;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.MproductreqDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproductreq;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ProductreqVerifyVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private MproductDAO productDao = new MproductDAO();
	private MproducttypeDAO producttypeDao = new MproducttypeDAO();
	private MproductreqDAO oDao = new MproductreqDAO();

	private Mproduct obj;
	private Mproductreq objForm;
	private Mproducttype objType;
	private Morg morg;
	private Mproducttype mproducttype;

	private String instant;
	private String berfoto;
	private String producttype;
	private Integer laststock;
	private Integer stockreserved;
	private Integer stockmin;

	@Wire
	private Radio rbYes, rbNo;
	@Wire
	private Combobox cbOrg, cbProducttype;
	@Wire
	private Textbox tbProducttype;
	@Wire
	private Label label, label2;
	@Wire
	private Tr stocklst, stockres, stockpagu, org;
	@Wire
	private Window winProductreqverify;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Mproductreq obj) {
		Selectors.wireComponents(view, this, false);

		objForm = obj;
		doReset();
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			obj = productDao.findByFilter("productcode = '" + objForm.getProductcode() + "' and isinstant = '"
					+ objForm.getIsinstant() + "'");
			if (obj == null) {
				Muser oUser = (Muser) zkSession.getAttribute("oUser");
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();

				objForm.setStatus("V");
				objForm.setLastupdated(new Date());
				objForm.setUpdatedby(oUser.getUserid());
				oDao.save(session, objForm);

				if (rbYes.isChecked()) {
					objType = new Mproducttype();
					objType.setLaststock(laststock);
					objType.setStockreserved(stockreserved);
					objType.setStockinjected(0);
					objType.setStockmin(stockmin);
					objType.setIsalertstockpagu("N");
					objType.setProductorg(morg.getOrg());
					objType.setProductgroupcode(AppUtils.PRODUCTGROUP_CARD);
					objType.setProductgroupname(AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_CARD));
					objType.setProducttype(producttype);
					objType.setLastupdated(new Date());
					objType.setUpdatedby(oUser.getUserid());
					objType.setSlacountertype(AppUtils.SLACOUNTERTYPE_DATEORDER);
					objType.setIsestcount(AppUtils.ESTIMATESTOCK_COUNTING);
					producttypeDao.save(session, objType);
				}

				obj = new Mproduct();
				if (rbYes.isChecked())
					obj.setMproducttype(objType);
				else
					obj.setMproducttype(mproducttype);
				obj.setProductcode(objForm.getProductcode());
				obj.setProductgroup(AppUtils.PRODUCTGROUP_CARD);
				obj.setIsmm("Y");
				obj.setLastupdated(new Date());
				obj.setUpdatedby(oUser.getUserid());

				if (objForm.getIsinstant().equals("Y") && objForm.getIsderivatif().equals("Y")) {
					obj.setProductname(objForm.getProductname());
					obj.setIsinstant("N");
					productDao.save(session, obj);

					obj = new Mproduct();
					if (rbYes.isChecked())
						obj.setMproducttype(objType);
					else
						obj.setMproducttype(mproducttype);
					obj.setProductcode(objForm.getProductcode());
					obj.setProductname(objForm.getProductname() + " INSTAN");
					obj.setProductgroup(AppUtils.PRODUCTGROUP_CARD);
					obj.setIsinstant(objForm.getIsinstant());
					obj.setIsmm("Y");
					obj.setLastupdated(new Date());
					obj.setUpdatedby(oUser.getUserid());
					productDao.save(session, obj);
				} else {
					obj.setIsinstant(objForm.getIsinstant());
					if (objForm.getIsinstant().equals("Y"))
						obj.setProductname(objForm.getProductname() + " INSTAN");
					else
						obj.setProductname(objForm.getProductname());
					productDao.save(session, obj);
				}

				transaction.commit();
				session.close();
				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 3000);
				Event closeEvent = new Event("onClose", winProductreqverify, null);
				Events.postEvent(closeEvent);
			} else {
				Messagebox.show("Kode produk sudah ada dalam daftar", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (HibernateException e) {
			transaction.rollback();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doChecked() {
		if (rbYes.isChecked()) {
			stocklst.setVisible(true);
			stockres.setVisible(true);
			stockpagu.setVisible(true);
			label2.setVisible(false);
			cbProducttype.setVisible(false);
			org.setVisible(true);
			label.setVisible(true);
			tbProducttype.setVisible(true);
			cbOrg.setValue(null);
			cbProducttype.setValue(null);
		} else {
			stocklst.setVisible(false);
			stockres.setVisible(false);
			stockpagu.setVisible(false);
			label.setVisible(false);
			tbProducttype.setVisible(false);
			label2.setVisible(true);
			cbProducttype.setVisible(true);
			org.setVisible(false);
			cbOrg.setValue(null);
			cbProducttype.setValue(null);
		}
	}

	public void doReset() {
		stocklst.setVisible(true);
		stockres.setVisible(true);
		stockpagu.setVisible(true);
		label2.setVisible(false);
		cbProducttype.setVisible(false);
		org.setVisible(true);
		laststock = 0;
		stockreserved = 0;
		stockmin = null;
		morg = null;
		cbOrg.setValue(null);
		cbProducttype.setValue(null);

		if (objForm.getIsinstant().equals("Y"))
			instant = "YA";
		else
			instant = "TIDAK";

		if (objForm.getIsderivatif().equals("Y"))
			berfoto = "YA";
		else
			berfoto = "TIDAK";

	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				if (rbYes.isChecked()) {
					if (producttype == null || "".equals(producttype.trim()))
						this.addInvalidMessage(ctx, "producttype", Labels.getLabel("common.validator.empty"));
					if (morg == null)
						this.addInvalidMessage(ctx, "morg", Labels.getLabel("common.validator.empty"));
					if (stockmin == null)
						this.addInvalidMessage(ctx, "stockmin", Labels.getLabel("common.validator.empty"));
				} else {
					if (mproducttype == null)
						this.addInvalidMessage(ctx, "mproducttype", Labels.getLabel("common.validator.empty"));
				}
			}
		};
	}

	public ListModelList<Morg> getMorgmodel() {
		ListModelList<Morg> lm = null;
		try {
			lm = new ListModelList<Morg>(AppData.getMorg());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mproducttype> getMproducttypemodel() {
		ListModelList<Mproducttype> lm = null;
		try {
			lm = new ListModelList<Mproducttype>(AppData.getMproducttype("productgroupcode = '01'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Mproduct getObj() {
		return obj;
	}

	public void setObj(Mproduct obj) {
		this.obj = obj;
	}

	public Mproductreq getObjForm() {
		return objForm;
	}

	public void setObjForm(Mproductreq objForm) {
		this.objForm = objForm;
	}

	public Morg getMorg() {
		return morg;
	}

	public void setMorg(Morg morg) {
		this.morg = morg;
	}

	public Mproducttype getMproducttype() {
		return mproducttype;
	}

	public void setMproducttype(Mproducttype mproducttype) {
		this.mproducttype = mproducttype;
	}

	public String getInstant() {
		return instant;
	}

	public void setInstant(String instant) {
		this.instant = instant;
	}

	public String getBerfoto() {
		return berfoto;
	}

	public void setBerfoto(String berfoto) {
		this.berfoto = berfoto;
	}

	public Integer getLaststock() {
		return laststock;
	}

	public void setLaststock(Integer laststock) {
		this.laststock = laststock;
	}

	public Integer getStockreserved() {
		return stockreserved;
	}

	public void setStockreserved(Integer stockreserved) {
		this.stockreserved = stockreserved;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public Integer getStockmin() {
		return stockmin;
	}

	public void setStockmin(Integer stockmin) {
		this.stockmin = stockmin;
	}

}
