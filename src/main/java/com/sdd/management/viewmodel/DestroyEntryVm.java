package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.dao.TreturnitemDAO;
import com.sdd.caption.dao.TreturnmemoDAO;
import com.sdd.caption.dao.TreturntrackDAO;
import com.sdd.caption.domain.Mlettertype;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mreturnreason;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.domain.Treturnmemo;
import com.sdd.caption.domain.Treturntrack;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DestroyEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Treturn objForm;
	private Mproduct mproduct;
	private Mreturnreason mreturnreason;
	private Tbranchstock objStock;
	private Integer branchlevel;
	private String productcode;
	private String arg;
	private String productgroup;

	private TreturnDAO oDao = new TreturnDAO();
	private TbranchstockDAO tbranchstockDao = new TbranchstockDAO();

	private List<Tbranchstockitem> inList = new ArrayList<>();
	private List<Tbranchstockitem> objList = new ArrayList<>();
	private List<String> listData = new ArrayList<>();

	private String itemno;
	private String itemnoend;
	private String itemnomanual;
	private String prefixmanual;
	private String memo;
	private Integer totaldata;
	private String prefix;
	private boolean isValid;

	@Wire
	private Row outlet, rowManual, rowRange;
	@Wire
	private Combobox cbProduct, cbReason;
	@Wire
	private Grid grid;
	@Wire
	private Div divRoot;
	@Wire
	private Caption caption;
	@Wire
	private Textbox tbItem, tbItemend, tbPrefix;
	@Wire
	private Checkbox chkbox;
	@Wire
	private Label lbS;
	@Wire
	private Vlayout vlayout;
	@Wire
	private Button btnRegister;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("obj") Treturn obj) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		branchlevel = oUser.getMbranch().getBranchlevel();
		this.arg = arg;

		doReset();

		if (obj != null) {
			objForm = obj;

			System.out.println(productcode);
			cbProduct.setValue(obj.getMproduct().getProductname());
			productcode = obj.getMproduct().getProductcode();
			totaldata = obj.getItemqty();
			cbReason.setValue(obj.getMreturnreason().getReturnreason());

			caption.setVisible(true);
		}

		grid.setRowRenderer(new RowRenderer<Tbranchstockitem>() {

			@Override
			public void render(final Row row, final Tbranchstockitem data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getItemno()));
			}
		});

	}

	@NotifyChange("*")
	@Command
	public void doRegister() {
		try {
			if (mproduct != null) {
				if (chkbox.isChecked()) {
					System.out.println("RANGE");
					getNumerator(prefix, itemno, itemnoend);
				} else {
					System.out.println("MANUAL");
					getNumerator(prefixmanual, itemnomanual, itemnomanual);
				}

				if (inList.size() > 0) {
					if (chkbox.isChecked()) {
						tbPrefix.setDisabled(true);
						tbItem.setDisabled(true);
						tbItemend.setDisabled(true);
						doAdd();
					}
					refresh();
				} else {
					Messagebox.show("Data tidak ditemukan atau sedang dalam proses pengembalian.", "Info",
							Messagebox.OK, Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Nama barang belum dipilih.", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
			prefixmanual = "";
			itemnomanual = "";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doAdd() {
		btnRegister.setDisabled(true);
		Hlayout hlayout = new Hlayout();

		Textbox txtPrefix = new Textbox();
		txtPrefix.setPlaceholder("Prefix");
		txtPrefix.setCols(5);
		txtPrefix.setMaxlength(5);
		Label label1 = new Label(" ");
		Textbox txtAwal = new Textbox();
		txtAwal.setPlaceholder("Entri Nomor Seri Awal");
		Label label2 = new Label("s/d");
		Textbox txtAkhir = new Textbox();
		txtAkhir.setPlaceholder("Entri Nomor Seri Akhir");
		txtAkhir.setCols(20);
		txtAkhir.setMaxlength(40);
		Label label3 = new Label(" ");
		Button btn = new Button("Check Data");
		btn.setAutodisable("self");
		btn.setSclass("btn btn-info btn-sm");
		btn.setStyle("border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
		btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				if (txtPrefix == null || txtPrefix.getValue().trim().length() == 0)
					Messagebox.show("Silahkan isi Prefix data.", "Info", Messagebox.OK, Messagebox.INFORMATION);
				else if (txtAwal == null || txtAwal.getValue().trim().length() == 0)
					Messagebox.show("Silahkan isi data nomor item awal dengan data numerik.", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				else if (txtAkhir == null || txtAkhir.getValue().trim().length() == 0)
					Messagebox.show("Silahkan isi data nomor item akhir dengan data numerik.", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				else {
					if (mproduct != null) {
						getNumerator(txtPrefix.getValue(), txtAwal.getValue(), txtAwal.getValue());

						if (isValid) {
							txtPrefix.setReadonly(true);
							txtAwal.setReadonly(true);
							txtAkhir.setReadonly(true);
							btn.setDisabled(true);
							doAdd();
							refresh();
							BindUtils.postNotifyChange(null, null, DestroyEntryVm.this, "outstanding");
						} else {
							Messagebox.show("Data yang dicari tidak ditemukan atau sudah masuk kedalam daftar.", "Info",
									Messagebox.OK, Messagebox.INFORMATION);
						}
					} else {
						Messagebox.show("Nama barang belum dipilih.", "Info", Messagebox.OK, Messagebox.INFORMATION);
					}
				}
			}
		});

		hlayout.appendChild(txtPrefix);
		hlayout.appendChild(label1);
		hlayout.appendChild(txtAwal);
		hlayout.appendChild(label2);
		hlayout.appendChild(txtAkhir);
		hlayout.appendChild(label3);
		hlayout.appendChild(btn);

		vlayout.appendChild(hlayout);

	}

	public void getNumerator(String prefix, String startno, String endno) {
		try {
			isValid = false;
			if (branchlevel == 2) {
				objStock = tbranchstockDao.findByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk()
						+ " and mproductfk = " + mproduct.getMproductpk());
			} else {
				objStock = tbranchstockDao.findByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk()
						+ " and mproductfk = " + mproduct.getMproductpk() + " and outlet = '00'");
			}

			if (objStock != null) {
				objList = new TbranchstockitemDAO().listNativeByFilter(
						"tbranchstockfk = " + objStock.getTbranchstockpk() + " and numerator between " + startno.trim()
								+ " and " + endno.trim() + " and status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "'",
						"itemno");
				System.out.println(objList.size());
				if (objList.size() > 0) {
					for (Tbranchstockitem data : objList) {
						if (!listData.contains(data.getItemno().trim())) {
							if (data.getItemno().equals(prefix.trim().toUpperCase() + data.getNumerator().toString())) {
								inList.add(data);
								listData.add(data.getItemno().trim());
								totaldata++;
								isValid = true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doChecked() {
		if (chkbox.isChecked()) {
			rowRange.setVisible(true);
			rowManual.setVisible(false);
		} else {
			rowRange.setVisible(false);
			rowManual.setVisible(true);
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (inList.size() > 0) {
			try {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();

				objForm.setRegid(new TcounterengineDAO().generateCounter(AppUtils.ID_DOCUMENT_PRODUCTION));
				objForm.setReturnlevel(oUser.getMbranch().getBranchlevel());
				objForm.setItemqty(totaldata);
				objForm.setMproduct(mproduct);
				objForm.setMreturnreason(mreturnreason);
				objForm.setIsdestroy("Y");
				objForm.setInsertedby(oUser.getUsername());
				if (oUser.getMbranch().getBranchlevel() == 3) {
					objForm.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
				} else if (oUser.getMbranch().getBranchlevel() == 2) {
					objForm.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH);
				}
				if (objForm.getMreturnreason().getIsDestroy().equals("Y")) {
					objForm.setLettertype(mproduct.getMproducttype().getDoctype());
				} else {
					objForm.setLettertype(null);
				}

				oDao.save(session, objForm);

				for (Tbranchstockitem data : inList) {
					Treturnitem objData = new Treturnitem();
					objData.setItemno(data.getItemno());
					objData.setItemstatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
					objData.setItemprice(data.getItemprice());
					objData.setTreturn(objForm);
					new TreturnitemDAO().save(session, objData);

					data.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
					new TbranchstockitemDAO().save(session, data);

					Tbranchitemtrack objTrack = new Tbranchitemtrack();
					objTrack.setItemno(data.getItemno());
					objTrack.setTracktime(new Date());
					objTrack.setTrackdesc(AppData.getStatusLabel(data.getStatus()));
					objTrack.setProductgroup(data.getProductgroup());
					objTrack.setMproduct(objForm.getMproduct());
					objTrack.setTrackstatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
					new TbranchitemtrackDAO().save(session, objTrack);

				}

				objStock.setStockactivated(objStock.getStockactivated() + inList.size());
				objStock.setStockcabang(objStock.getStockcabang() - inList.size());
				new TbranchstockDAO().save(session, objStock);

				Treturnmemo objMemo = new Treturnmemo();
				objMemo.setMemo(memo);
				objMemo.setMemoby(oUser.getUsername());
				objMemo.setMemotime(new Date());
				objMemo.setTreturn(objForm);
				new TreturnmemoDAO().save(session, objMemo);

				Treturntrack objTrack = new Treturntrack();
				objTrack.setTreturn(objForm);
				objTrack.setTrackstatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
				objTrack.setTracktime(new Date());
				objTrack.setTrackdesc(AppData.getStatusLabel(objTrack.getTrackstatus()));
				new TreturntrackDAO().save(session, objTrack);

				transaction.commit();
				session.close();

				Mmenu mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/return/return.zul' and menuparamvalue = 'approval'");
				NotifHandler.doNotif(mmenu, oUser.getMbranch(), objForm.getMproduct().getProductgroup(),
						oUser.getMbranch().getBranchlevel());

				doDone();

				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 5000);

				doReset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Tidak ada data.", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	private void doDone() {
		try {
			divRoot.getChildren().clear();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("obj", objForm);
			map.put("arg", arg);
			Executions.createComponents("/view/return/returentryresume.zul", divRoot, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void refresh() {
		grid.setModel(new ListModelList<Tbranchstockitem>(inList));
	}

	@NotifyChange("*")
	public void doReset() {
		objForm = new Treturn();
		objForm.setInserttime(new Date());
		objForm.setMbranch(oUser.getMbranch());
		memo = "";
		totaldata = 0;
		itemno = "";
		itemnoend = "";
		inList = new ArrayList<Tbranchstockitem>();
		objList = new ArrayList<Tbranchstockitem>();
		listData = new ArrayList<String>();
		cbProduct.setValue(null);
		cbReason.setValue(null);
		mproduct = null;
		mreturnreason = null;
		productgroup = AppData.getProductgroupLabel(arg);
		refresh();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {
					Mproduct mproduct = (Mproduct) ctx.getProperties("mproduct")[0].getValue();
					if (mproduct == null)
						this.addInvalidMessage(ctx, "mproduct", Labels.getLabel("common.validator.empty"));

					Mreturnreason mreturnreason = (Mreturnreason) ctx.getProperties("mreturnreason")[0].getValue();
					if (mreturnreason == null)
						this.addInvalidMessage(ctx, "mreturnreason", Labels.getLabel("common.validator.empty"));

					if (memo == null || "".trim().equals(memo))
						this.addInvalidMessage(ctx, "memo", Labels.getLabel("common.validator.empty"));

					if (objForm.getMreturnreason().getIsDestroy().equals("Y")) {
						Mlettertype mlettertype = (Mlettertype) ctx.getProperties("mlettertype")[0].getValue();
						if (mlettertype == null)
							this.addInvalidMessage(ctx, "mlettertype", Labels.getLabel("common.validator.empty"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
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

	public ListModelList<Mreturnreason> getMreturnreasonmodel() {
		ListModelList<Mreturnreason> lm = null;
		try {
			lm = new ListModelList<Mreturnreason>(AppData.getMreturnreason("productgroup = '" + arg + "' and isdestroy = 'Y'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getItemno() {
		return itemno;
	}

	public void setItemno(String itemno) {
		this.itemno = itemno;
	}

	public String getItemnoend() {
		return itemnoend;
	}

	public void setItemnoend(String itemnoend) {
		this.itemnoend = itemnoend;
	}

	public Treturn getObjForm() {
		return objForm;
	}

	public void setObjForm(Treturn objForm) {
		this.objForm = objForm;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public Mreturnreason getMreturnreason() {
		return mreturnreason;
	}

	public void setMreturnreason(Mreturnreason mreturnreason) {
		this.mreturnreason = mreturnreason;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public Tbranchstock getObjStock() {
		return objStock;
	}

	public void setObjStock(Tbranchstock objStock) {
		this.objStock = objStock;
	}

	public String getItemnomanual() {
		return itemnomanual;
	}

	public void setItemnomanual(String itemnomanual) {
		this.itemnomanual = itemnomanual;
	}

	public String getPrefixmanual() {
		return prefixmanual;
	}

	public void setPrefixmanual(String prefixmanual) {
		this.prefixmanual = prefixmanual;
	}

}