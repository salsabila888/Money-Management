package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.dao.TpilotingDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Moutlet;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Msupplier;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.domain.Tpiloting;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class IncomingDivisionEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Tincoming objForm;
	private Tpiloting oForm;
	private Mproduct mproduct;
	private Moutlet moutlet;
	private Mbranch mbranch;
	private Msupplier msupplier;
	private String cabang;
	private BigDecimal harga;
	private String prefix;
	private Integer startno;
	private Integer endno;
	private Integer totaldata;
	private int branchlevel;
	private Integer totalqty;
	private Date entrytime;
	private String filename;
	private Media media;
	private boolean isEdit;

	@Wire
	private Combobox cbProduct, cbVendor;

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
				this.oForm = obj;
				objForm = obj.getTincoming();
				mbranch = obj.getTincoming().getMbranch();
				mproduct = new MproductDAO().findByFilter("productname  = '" + obj.getProducttype() + "'");
				msupplier = obj.getMsupplier();
				cabang = mbranch.getBranchname();
				cbProduct.setValue(mproduct.getProductname());
				cbVendor.setValue(msupplier.getSuppliername());
				harga = obj.getItemprice();
				prefix = obj.getPrefix();
				startno = obj.getStartno();
				endno = obj.getEndno();
				totaldata = obj.getTotalitem();
				totalqty = obj.getTotalqty();
				filename = obj.getFilename();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Command
	@NotifyChange("filename")
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			filename = media.getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			boolean isValid = true;

			String filterDuplicate = "prefix = '" + prefix.trim().toUpperCase() + "' and (" + startno
					+ " between itemstartno and (itemstartno + itemqty - 1) or " + endno
					+ "  between itemstartno and (itemstartno + itemqty - 1)) and tincoming.status != '"
					+ AppUtils.STATUS_INVENTORY_INCOMINGDECLINE + "'";

			if (mproduct.getMproducttype().getGrouptype().equals(AppUtils.GROUPTYPE_DEPOSITO)
					|| mproduct.getMproducttype().getGrouptype().equals(AppUtils.GROUPTYPE_BUKUTABUNGAN)) {
				filterDuplicate += " and mproducttypefk = " + mproduct.getMproducttype().getMproducttypepk();
				List<Tincoming> duplicate = new TincomingDAO().listNativeByFilter(filterDuplicate, "tincomingpk");
				if (duplicate.size() > 0) {
					System.out.println(duplicate.get(0).getTincomingpk() + ", " + objForm.getTincomingpk());
					if (isEdit) {
						System.out.println("EDIT");
						if (!duplicate.get(0).getTincomingpk().equals(objForm.getTincomingpk()))
							isValid = false;
					} else {
						isValid = false;
					}
				}
			} else {
				filterDuplicate += " and grouptype = '" + mproduct.getMproducttype().getGrouptype() + "'";
				System.out.println(filterDuplicate);
				List<Tincoming> duplicate = new TincomingDAO().listNativeByFilter(filterDuplicate, "tincomingpk");
				if (duplicate.size() > 0) {
					if (isEdit) {
						if (duplicate.get(0).getTincomingpk() != objForm.getTincomingpk())
							isValid = false;
					} else {
						isValid = false;
					}
				}
			}

			if (isValid) {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				try {
					if (!isEdit) {
						objForm.setIncomingid(
								new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_INVENTORY_INCOMING));
					}
					objForm.setEntryby(oUser.getUsername());
					objForm.setEntrytime(new Date());
					objForm.setHarga(harga);
					objForm.setItemqty(totalqty);
					objForm.setItemstartno(startno);
					objForm.setMbranch(mbranch);
					objForm.setMproducttype(mproduct.getMproducttype());
					objForm.setPrefix(prefix);
					objForm.setMsupplier(msupplier);
					objForm.setPksdate(oForm.getPksdate());
					objForm.setFilename(filename);
					objForm.setManufacturedate(oForm.getManufacturedate());
					objForm.setMemo(oForm.getMemo());
					objForm.setPksdate(oForm.getPksdate());
					objForm.setPksno(oForm.getPksno());
					objForm.setSpkdate(oForm.getSpkdate());
					objForm.setSpkno(oForm.getSpkno());
					objForm.setVendorletterdate(oForm.getVendorletterdate());
					objForm.setVendorletterno(oForm.getVendorletterno());
					objForm.setProductgroup(AppUtils.PRODUCTGROUP_DOCUMENT);
					objForm.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGWAITAPPROVAL);
					new TincomingDAO().save(session, objForm);

					oForm.setTincoming(objForm);
					oForm.setBranchid(mbranch.getBranchid());
					oForm.setBranchname(mbranch.getBranchname());
					oForm.setProducttype(mproduct.getProductname());
					oForm.setOutlet("00");
					oForm.setPrefix(prefix);
					oForm.setStartno(startno);
					oForm.setEndno(endno);
					oForm.setTotalitem(totaldata);
					oForm.setTotalqty(totalqty);
					oForm.setItemprice(harga);
					oForm.setStatus("WA");
					oForm.setMsupplier(msupplier);
					oForm.setInsertedby(oUser.getUserid());
					oForm.setInserttime(new Date());
					oForm.setFilename(filename);
					new TpilotingDAO().save(session, oForm);

					if (media != null) {
						String path = Executions.getCurrent().getDesktop().getWebApp()
								.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
						if (media.isBinary()) {
							Files.copy(new File(path + media.getName()), media.getStreamData());
						} else {
							BufferedWriter writer = new BufferedWriter(new FileWriter(path + media.getName()));
							Files.copy(writer, media.getReaderData());
							writer.close();
						}

					}

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
				Messagebox.show("Nomer seri sudah pernah terdaftar.", "Info", Messagebox.OK, Messagebox.EXCLAMATION);
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
		objForm = new Tincoming();
		oForm = new Tpiloting();
		mproduct = null;
		moutlet = null;
		msupplier = null;
		mbranch = oUser.getMbranch();
		cabang = oUser.getMbranch().getBranchname();
		harga = new BigDecimal(0);
		prefix = null;
		startno = null;
		endno = null;
		totaldata = 0;
		totalqty = 0;
		entrytime = new Date();
		filename = "";
		cbProduct.setValue(null);
		cbVendor.setValue(null);

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

					String spkno = (String) ctx.getProperties("spkno")[0].getValue();
					if (spkno == null || "".equals(spkno.trim()))
						this.addInvalidMessage(ctx, "spkno", Labels.getLabel("common.validator.empty"));

					Date spkdate = (Date) ctx.getProperties("spkdate")[0].getValue();
					if (spkdate == null)
						this.addInvalidMessage(ctx, "spkdate", Labels.getLabel("common.validator.empty"));

					String pksno = (String) ctx.getProperties("pksno")[0].getValue();
					if (pksno == null || "".equals(pksno.trim()))
						this.addInvalidMessage(ctx, "pksno", Labels.getLabel("common.validator.empty"));

					Date pksdate = (Date) ctx.getProperties("pksdate")[0].getValue();
					if (pksdate == null)
						this.addInvalidMessage(ctx, "pksdate", Labels.getLabel("common.validator.empty"));

					String vendorletterno = (String) ctx.getProperties("vendorletterno")[0].getValue();
					if (vendorletterno == null || "".equals(vendorletterno.trim()))
						this.addInvalidMessage(ctx, "vendorletterno", Labels.getLabel("common.validator.empty"));

					Date vendorletterdate = (Date) ctx.getProperties("vendorletterdate")[0].getValue();
					if (vendorletterdate == null)
						this.addInvalidMessage(ctx, "vendorletterdate", Labels.getLabel("common.validator.empty"));

					Integer manufacturedate = (Integer) ctx.getProperties("manufacturedate")[0].getValue();
					if (manufacturedate == null)
						this.addInvalidMessage(ctx, "manufacturedate", Labels.getLabel("common.validator.empty"));

					String memo = (String) ctx.getProperties("memo")[0].getValue();
					if (memo == null)
						this.addInvalidMessage(ctx, "memo", Labels.getLabel("common.validator.empty"));

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

	public ListModelList<Msupplier> getMsuppliermodel() {
		ListModelList<Msupplier> lm = null;
		try {
			lm = new ListModelList<Msupplier>(AppData.getMsupplier());
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Tpiloting getoForm() {
		return oForm;
	}

	public void setoForm(Tpiloting oForm) {
		this.oForm = oForm;
	}

	public Msupplier getMsupplier() {
		return msupplier;
	}

	public void setMsupplier(Msupplier msupplier) {
		this.msupplier = msupplier;
	}
}
