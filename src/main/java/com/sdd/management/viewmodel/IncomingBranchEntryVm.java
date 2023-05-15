package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MoutletDAO;
import com.sdd.caption.dao.TbranchitembucketDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TpilotingDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Moutlet;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitembucket;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tpiloting;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class IncomingBranchEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private TbranchstockDAO tbsDao = new TbranchstockDAO();
	private TbranchitembucketDAO tbibDao = new TbranchitembucketDAO();

	private Tpiloting obj;
	private Tbranchstock tbs;
	private Tbranchitembucket tbib;
	private Mproduct mproduct;
	private Moutlet moutlet;
	private Mbranch mbranch;
	private String cabang;
	private BigDecimal harga;
	private String prefix;
	private Integer startno;
	private Integer endno;
	private Integer totaldata;
	private int branchlevel;
	private Integer totalqty;
	private Date entrytime;
	private String outletcode;
	private boolean isEdit;

	@Wire
	private Combobox cbOutlet, cbProduct;
	@Wire
	private Row rowOutlet;
	@Wire
	private Caption caption;
	@Wire
	private Window winBranchentri;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tpiloting obj) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		branchlevel = oUser.getMbranch().getBranchlevel();
		doReset();
		
		if (obj != null) {
			try {
				isEdit = true;
				caption.setVisible(true);
				this.obj = obj;
				tbib = obj.getTbranchitembucket();
				mbranch = obj.getTbranchitembucket().getTbranchstock().getMbranch();
				mproduct = obj.getTbranchitembucket().getTbranchstock().getMproduct();
				List<Moutlet> outletList = new MoutletDAO().listByFilter(
						"mbranchfk = " + mbranch.getMbranchpk() + " and outletcode = '" + obj.getOutlet() + "'",
						"outletcode");
				if(outletList.size() > 0)
					moutlet = outletList.get(0);
				cabang = mbranch.getBranchname();
				outletcode = obj.getOutlet();
				cbOutlet.setValue(outletcode);
				cbProduct.setValue(mproduct.getProductname());
				harga = obj.getItemprice();
				prefix = obj.getPrefix();
				startno = obj.getStartno();
				endno = obj.getEndno();
				totaldata = obj.getTotalitem();
				totalqty = obj.getTotalqty();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (branchlevel == 3)
			rowOutlet.setVisible(true);
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			boolean isValid = true;
			if (branchlevel > 1) {
				if (branchlevel == 3)
					outletcode = moutlet.getOutletcode();
				else
					outletcode = "00";

				tbs = tbsDao.findByFilter("mbranchfk = " + mbranch.getMbranchpk() + " and outlet = '" + outletcode
						+ "' and mproductfk = " + mproduct.getMproductpk());
				if (tbs != null) {
					String filterDuplicate = "prefix = '" + prefix.trim().toUpperCase() + "' and (" + startno
							+ " between itemstartno and itemendno or " + endno + " between itemstartno and itemendno)";

					if (mproduct.getMproducttype().getGrouptype().equals(AppUtils.GROUPTYPE_DEPOSITO)
							|| mproduct.getMproducttype().getGrouptype().equals(AppUtils.GROUPTYPE_BUKUTABUNGAN)) {
						filterDuplicate += " and mproducttypefk = " + mproduct.getMproducttype().getMproducttypepk();
						List<Tbranchitembucket> duplicate = tbibDao.listNativeByFilter(filterDuplicate,
								"tbranchitembucketpk");
						if (duplicate.size() > 0) {
							if (isEdit) {
								if (duplicate.get(0).getTbranchitembucketpk() != tbib.getTbranchitembucketpk())
									isValid = false;
							} else {
								isValid = false;
							}
						}
					} else {
						filterDuplicate += " and grouptype = '" + mproduct.getMproducttype().getGrouptype() + "'";
						System.out.println(filterDuplicate);
						List<Tbranchitembucket> duplicate = tbibDao.listNativeByFilter(filterDuplicate,
								"tbranchitembucketpk");
						if (duplicate.size() > 0) {
							if (isEdit) {
								if (duplicate.get(0).getTbranchitembucketpk() != tbib.getTbranchitembucketpk())
									isValid = false;
							} else {
								isValid = false;
							}
						}
					}
				} else {
					tbs = new Tbranchstock();
					tbs.setMbranch(mbranch);
					tbs.setMproduct(mproduct);
					tbs.setOutlet(outletcode);
					tbs.setProductgroup(AppUtils.PRODUCTGROUP_DOCUMENT);
					tbs.setStockactivated(0);
					tbs.setStockcabang(0);
					tbs.setStockdelivered(0);
					tbs.setStockdestroyed(0);
					tbs.setStockreserved(0);
				}

				if (isValid) {
					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					try {
						tbsDao.save(session, tbs);

						tbib.setCurrentno(startno);
						tbib.setIsgenerate("N");
						tbib.setIspod("N");
						tbib.setIsrunout("N");
						tbib.setItemendno(endno);
						tbib.setItemprice(harga);
						tbib.setItemstartno(startno);
						tbib.setOutbound(0);
						tbib.setOutlet(outletcode);
						tbib.setPrefix(prefix);
						tbib.setTbranchstock(tbs);
						tbib.setTotalitem(totalqty);
						tbib.setInserttime(new Date());
						tbibDao.save(session, tbib);

						obj.setTbranchitembucket(tbib);
						obj.setBranchid(mbranch.getBranchid());
						obj.setBranchname(mbranch.getBranchname());
						obj.setProducttype(mproduct.getProductname());
						obj.setOutlet(outletcode);
						obj.setPrefix(prefix);
						obj.setStartno(startno);
						obj.setEndno(endno);
						obj.setTotalitem(totaldata);
						obj.setTotalqty(totalqty);
						obj.setItemprice(harga);
						obj.setStatus("WA");
						obj.setInsertedby(oUser.getUserid());
						obj.setInserttime(new Date());
						new TpilotingDAO().save(session, obj);

						transaction.commit();
						Clients.showNotification("Input data persediaan berhasil", "info", null, "middle_center", 5000);
						
						if (isEdit) {
							Event closeEvent = new Event("onClose", winBranchentri, new Boolean(true));
							Events.postEvent(closeEvent);
						}
						
						doReset();
					} catch (Exception e) {
						transaction.rollback();
						e.printStackTrace();
					} finally {
						session.close();
					}
				} else {
					Messagebox.show("Nomer seri sudah pernah terdaftar.", "Info", Messagebox.OK,
							Messagebox.EXCLAMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doGetEndno() {
		if (startno != null && startno > 0) {
			if (totaldata != null && totaldata > 0) {
				if (mproduct != null) {
					totalqty = totaldata * mproduct.getMproducttype().getProductunitqty();
					if (prefix != null && !"".equals(prefix)) {
						endno = startno + totalqty - 1;
						if (endno > 9999999) {
							endno = 9999999;
							if (totalqty > 9999999)
								totalqty = 9999999;
							Messagebox.show("Nomer seri melebihi 9,999,999 digit.", "Info", Messagebox.OK,
									Messagebox.INFORMATION);
						}
					}
				}
			}
		}
	}

	@NotifyChange("*")
	public void doReset() {
		isEdit = false;
		obj = new Tpiloting();
		tbs = new Tbranchstock();
		tbib = new Tbranchitembucket();
		mproduct = null;
		moutlet = null;
		mbranch = oUser.getMbranch();
		cabang = oUser.getMbranch().getBranchname();
		harga = new BigDecimal(0);
		prefix = null;
		startno = null;
		endno = null;
		totaldata = 0;
		totalqty = 0;
		entrytime = new Date();
		outletcode = "";
		cbOutlet.setValue(null);
		cbProduct.setValue(null);

	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {
					if (mproduct == null)
						this.addInvalidMessage(ctx, "mproduct", Labels.getLabel("common.validator.empty"));

					if (branchlevel == 3) {
						if (moutlet == null)
							this.addInvalidMessage(ctx, "moutlet", Labels.getLabel("common.validator.empty"));
					}

					if (startno == null || startno == 0 || prefix == null || "".equals(prefix.trim()))
						this.addInvalidMessage(ctx, "startno", Labels.getLabel("common.validator.empty"));

					if (totaldata == null || totaldata == 0)
						this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(
					AppData.getMproduct("productgroup = '" + AppUtils.PRODUCTGROUP_DOCUMENT + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Moutlet> getMoutletmodel() {
		ListModelList<Moutlet> lm = null;
		try {
			lm = new ListModelList<>(AppData.getMoutlet("mbranchfk = " + oUser.getMbranch().getMbranchpk()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public Moutlet getMoutlet() {
		return moutlet;
	}

	public void setMoutlet(Moutlet moutlet) {
		this.moutlet = moutlet;
	}

	public String getCabang() {
		return cabang;
	}

	public void setCabang(String cabang) {
		this.cabang = cabang;
	}

	public BigDecimal getHarga() {
		return harga;
	}

	public void setHarga(BigDecimal harga) {
		this.harga = harga;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Integer getStartno() {
		return startno;
	}

	public void setStartno(Integer startno) {
		this.startno = startno;
	}

	public Integer getEndno() {
		return endno;
	}

	public void setEndno(Integer endno) {
		this.endno = endno;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Tbranchstock getTbs() {
		return tbs;
	}

	public void setTbs(Tbranchstock tbs) {
		this.tbs = tbs;
	}

	public Date getEntrytime() {
		return entrytime;
	}

	public void setEntrytime(Date entrytime) {
		this.entrytime = entrytime;
	}

	public Integer getTotalqty() {
		return totalqty;
	}

	public void setTotalqty(Integer totalqty) {
		this.totalqty = totalqty;
	}
}
