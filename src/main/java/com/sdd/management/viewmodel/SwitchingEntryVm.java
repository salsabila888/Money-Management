package com.sdd.caption.viewmodel;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MoutletDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.dao.TswitchmemoDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Moutlet;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.domain.Tswitchmemo;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class SwitchingEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Torder obj;
	private Tswitch objForm;
	private Mbranch mbranch;

	private TorderDAO oDao = new TorderDAO();
	private TswitchDAO switchDao = new TswitchDAO();
	private TswitchmemoDAO switchMemoDao = new TswitchmemoDAO();

	private String outletreq;
	private String stockbranchpool;
	private String cabang;
	private String wilayah;
	private String productgroup;
	private String unit;
	private String memo;
	private String arg;
	private boolean isEdit;
	private Integer branchlevel;
	private Moutlet moutlet;
	
	private ListModelList<Moutlet> moutletmodel;

	@Wire
	private Combobox cbBranch, cbOutlet;
	@Wire
	private Row rowCabang, rowOutlet1;
	@Wire
	private Window winSwitchentry;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder obj,
			@ExecutionArgParam("objSwitch") Tswitch objSwitch, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("isEdit") String isEdit) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		branchlevel = oUser.getMbranch().getBranchlevel();

		this.arg = arg;
		doReset();

		if (obj != null) {
			this.obj = obj;
			objForm = new Tswitch();
			objForm.setMproduct(obj.getMproduct());
			objForm.setItemqty(obj.getTotalqty());
			objForm.setInserttime(new Date());
			wilayah = obj.getMbranch().getMregion().getRegionname();
			cabang = obj.getMbranch().getBranchname();
			outletreq = obj.getOrderoutlet();
			mbranch = obj.getMbranch();
			cbBranch.setValue(obj.getMbranch().getBranchname());
		}
		
		System.out.println(cabang);
		if (cabang != null) {
			try {
				moutletmodel = new ListModelList<>(AppData.getMoutlet("mbranch.mbranchpk = " + oUser.getMbranch().getMbranchpk() + "and outletcode != '00'"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (isEdit != null && isEdit.equals("Y"))
			this.isEdit = true;

		if (branchlevel == 3) {
			cbBranch.setReadonly(true);
			cbBranch.setButtonVisible(false);
		}

		if (objSwitch != null) {
			try {
				objForm = objSwitch;
				this.obj = objForm.getTorder();
				wilayah = objForm.getTorder().getMbranch().getMregion().getRegionname();
				cabang = objForm.getTorder().getMbranch().getBranchname();
				outletreq = objForm.getTorder().getOrderoutlet();

				if (!objForm.getOutletreq().equals("00")) {
					mbranch = oUser.getMbranch();
					cbBranch.setDisabled(true);
					cbBranch.setValue(mbranch.getBranchname());
				}

				if (objForm.getBranchidpool() != null) {
					mbranch = new MbranchDAO().findByFilter("branchid = '" + objForm.getBranchidpool() + "'");
					if (mbranch != null) {
						cbBranch.setValue(mbranch.getBranchname());
						if (objSwitch.getOutletpool() != null) {
							moutlet = new MoutletDAO().findByFilter("outletcode = '" + objSwitch.getOutletpool() + "' and mbranchfk = " + mbranch.getMbranchpk());
							cbOutlet.setValue(moutlet.getOutletcode());
						}
					}
				}

				if (mbranch != null) {
					String filter = "";
					Tbranchstock objStock = null;
					filter = "mbranchfk = " + mbranch.getMbranchpk() + " and mproductfk = "
							+ objForm.getTorder().getMproduct().getMproductpk() + " and outlet = '" + moutlet.getOutletcode().trim()
							+ "'";

					objStock = new TbranchstockDAO().findByFilter(filter);

					if (objStock != null) {
						stockbranchpool = String.valueOf(objStock.getStockcabang());
					} else {
						stockbranchpool = String.valueOf(0);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (branchlevel == 2) {
			rowOutlet1.setVisible(false);
		}
	}

	@Command
	@NotifyChange("stockbranchpool")
	public void doSelect() {
		try {
			String filter = "";
			Tbranchstock objStock = null;
			
			if (moutlet != null)
				filter = "mbranchfk = " + mbranch.getMbranchpk() + " and mproductfk = " + obj.getMproduct().getMproductpk()
				+ " and outlet = '" + moutlet.getOutletcode().trim() + "'";
			else
				filter = "mbranchfk = " + mbranch.getMbranchpk() + " and mproductfk = " + obj.getMproduct().getMproductpk()
				+ " and outlet = '00'";

			objStock = new TbranchstockDAO().findByFilter(filter);

			if (objStock != null) {
				System.out.println("VALID");
				stockbranchpool = String.valueOf(objStock.getStockcabang());
				System.out.println(stockbranchpool);
			} else {
				System.out.println("INVALID");
				stockbranchpool = String.valueOf(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (Integer.valueOf(stockbranchpool) > objForm.getItemqty()) {
			try {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();

				if (!isEdit) {
					objForm.setRegid(new TcounterengineDAO().generateCounter("SW"));
					objForm.setMbranch(obj.getMbranch());
					objForm.setBranchidreq(oUser.getMbranch().getBranchid());
					objForm.setBranchidpool(mbranch.getBranchid());
					objForm.setOutletreq(outletreq);
					if (moutlet != null)
						objForm.setOutletpool(moutlet.getOutletcode());
					else
						objForm.setOutletpool("00");
					objForm.setInsertedby(oUser.getUsername());
					objForm.setStatus(AppUtils.STATUS_SWITCH_WAITAPPROVAL);
					objForm.setTorder(obj);
					switchDao.save(session, objForm);

					obj.setTotalproses(objForm.getItemqty());
					obj.setStatus(AppUtils.STATUS_SWITCH_WAITAPPROVAL);
					oDao.save(session, obj);

				} else {
					objForm.setBranchidreq(oUser.getMbranch().getBranchid());
					objForm.setBranchidpool(mbranch.getBranchid());
					objForm.setOutletreq(outletreq);
					if (moutlet != null)
						objForm.setOutletpool(moutlet.getOutletcode());
					else
						objForm.setOutletpool("00");
					objForm.setInsertedby(oUser.getUsername());
					objForm.setStatus(AppUtils.STATUS_SWITCH_WAITAPPROVAL);
					objForm.setTorder(obj);
					switchDao.save(session, objForm);

					obj.setTotalproses(objForm.getItemqty());
					obj.setStatus(AppUtils.STATUS_SWITCH_WAITAPPROVAL);
					oDao.save(session, obj);
				}

				Tswitchmemo objMemo = new Tswitchmemo();
				objMemo.setMemo(memo);
				objMemo.setMemoby(oUser.getUsername());
				objMemo.setMemotime(new Date());
				objMemo.setTswitch(objForm);
				switchMemoDao.save(session, objMemo);

				Tordermemo orderMemo = new Tordermemo();
				orderMemo.setMemo(memo);
				orderMemo.setMemoby(oUser.getUsername());
				orderMemo.setMemotime(new Date());
				orderMemo.setTorder(objForm.getTorder());
				new TordermemoDAO().save(session, orderMemo);

				FlowHandler.doFlow(session, null, obj, memo, oUser.getUserid());

				Event closeEvent = new Event("onClose", winSwitchentry, null);
				Events.postEvent(closeEvent);

				transaction.commit();
				session.close();

				Mmenu mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/switching/switching.zul' and menuparamvalue = 'approvereq'");
				NotifHandler.doNotif(mmenu, oUser.getMbranch(), objForm.getMproduct().getProductgroup(),
						oUser.getMbranch().getBranchlevel());

				mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'req'");
				NotifHandler.delete(mmenu, obj.getMbranch(), obj.getProductgroup(), branchlevel);

				mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/switching/switchinglist.zul' and menuparamvalue = 'req'");
				NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
						oUser.getMbranch().getBranchlevel());

				Clients.showNotification("Entri data switching berhasil", "info", null, "middle_center", 5000);
				doReset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Stok dari cabang " + mbranch.getBranchname() + " tidak mencukupi.", "Info", Messagebox.OK,
					Messagebox.INFORMATION);
		}
	}

	public void doReset() {
		memo = "";
		productgroup = AppData.getProductgroupLabel(arg);
		stockbranchpool = "0";
		mbranch = null;

		if (arg.equals("04"))
			unit = arg;
		else
			unit = "02";
	}

	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct("productgroup = '" + arg + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mbranch> getMbranchmodel() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(
					AppData.getMbranch("mregionfk = " + oUser.getMbranch().getMregion().getMregionpk()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}

	public String getCabang() {
		return cabang;
	}

	public void setCabang(String cabang) {
		this.cabang = cabang;
	}

	public String getWilayah() {
		return wilayah;
	}

	public void setWilayah(String wilayah) {
		this.wilayah = wilayah;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Tswitch getObjForm() {
		return objForm;
	}

	public void setObjForm(Tswitch objForm) {
		this.objForm = objForm;
	}

	public String getOutletreq() {
		return outletreq;
	}

	public void setOutletreq(String outletreq) {
		this.outletreq = outletreq;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public String getStockbranchpool() {
		return stockbranchpool;
	}

	public void setStockbranchpool(String stockbranchpool) {
		this.stockbranchpool = stockbranchpool;
	}

	public Moutlet getMoutlet() {
		return moutlet;
	}

	public void setMoutlet(Moutlet moutlet) {
		this.moutlet = moutlet;
	}

	public ListModelList<Moutlet> getMoutletmodel() {
		return moutletmodel;
	}

	public void setMoutletmodel(ListModelList<Moutlet> moutletmodel) {
		this.moutletmodel = moutletmodel;
	}
}