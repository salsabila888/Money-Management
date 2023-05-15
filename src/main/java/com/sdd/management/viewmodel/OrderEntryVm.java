package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
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
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MoutletDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderdocDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Moutlet;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mregion;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Torderdoc;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OrderEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Torder objForm;
	private Tordermemo objMemo;
	private TorderDAO oDao = new TorderDAO();
	private TordermemoDAO oDaoMemo = new TordermemoDAO();

	private String cabang;
	private String wilayah;
	private String productgroup;
	private String unit;
	private String memo;
	private String arg;
	private String memono;
	private Date memodate;
	private int branchlevel;
	private Media media;
	private boolean isEdit = false;
	private Integer totalqty;
	private Integer itemqty;
	private Mproduct mproduct;
	private Moutlet moutlet;
	private Mbranch mbranch;
	private String type;

	private String status, orderid, ordertype, orderlevel;

	private String docfileori, docfileid;
	private Integer docfilesize;
	private Date doctime;

	private List<Torderdoc> docList;
	private List<Torderdoc> docDelList = new ArrayList<Torderdoc>();
	private Map<String, Media> mapMedia = new HashMap<String, Media>();
	private ListModelList<Moutlet> moutletmodel;

	@Wire
	private Div divRoot;
	@Wire
	private Row rowUpload, rowDoc, rowTotal, rowOutlet;
	@Wire
	private Combobox cbProduct, cbOutlet, cbBranch;
	@Wire
	private Textbox tbBranch;
	@Wire
	private Grid gridDoc;
	@Wire
	private Caption caption;
//	@Wire
//	private Textbox tbOutlet;
	@Wire 
	private Label lbJumlah, lbTotal;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("obj") Torder obj, @ExecutionArgParam("objmemo") Tordermemo objMemo, 
			@ExecutionArgParam("type") String type) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();

		this.arg = arg;
		this.objMemo = objMemo;
		doReset();
		
		if (cabang != null) {
			try {
				moutletmodel = new ListModelList<>(AppData.getMoutlet("mbranch.mbranchpk = " + oUser.getMbranch().getMbranchpk()));
				System.out.println(moutletmodel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
//			tbOutlet.setReadonly(false);
			lbJumlah.setValue("Jumlah Buku");
			lbTotal.setValue("Total Buku/Lembar");
			rowTotal.setVisible(true);
		} else {
			lbJumlah.setValue("Jumlah Unit");
			lbTotal.setValue("Total Unit");
		}

		if (oUser.getMbranch().getBranchlevel() == 3) {
			rowOutlet.setVisible(true);
			tbBranch.setVisible(true);
			cbBranch.setVisible(false);
		} else if (oUser.getMbranch().getBranchlevel() == 2) {
			rowOutlet.setVisible(false);
			tbBranch.setVisible(true);
			cbBranch.setVisible(false);
		} else {
			rowOutlet.setVisible(false);
			this.type = type;
			if (type != null && type.equals("P")) {
				tbBranch.setVisible(true);
				cbBranch.setVisible(false);
			} else if (type != null && type.equals("C")) {
				tbBranch.setVisible(false);
				cbBranch.setVisible(true);
			}
		}
		
		cbProduct.setVisible(true);

		if (obj != null) {
			objForm = obj;
			cbProduct.setValue(obj.getMproduct().getProductname());
			moutlet = new MoutletDAO().findByFilter("outletcode = '" + objForm.getOrderoutlet() + "' and mbranchfk = " + oUser.getMbranch().getMbranchpk());
			cbOutlet.setValue(obj.getOrderoutlet());
			mproduct = objForm.getMproduct();
			itemqty = objForm.getItemqty();
			totalqty = objForm.getTotalqty();
			memono = obj.getMemono();
			memodate = obj.getMemodate();
			memo = obj.getMemo();

			docList = new TorderdocDAO().listByFilter("torderfk = " + obj.getTorderpk(), "torderfk");
			gridDoc.setModel(new ListModelList<>(docList));

			isEdit = true;
			caption.setVisible(true);
		}
	}

	@Command
	@NotifyChange("docfileori")
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			Torderdoc doc = new Torderdoc();
			doc.setDocfileori(media.getName());
			doc.setDocfileid(
					"ORDERDOC" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + "." + media.getFormat());
			doc.setDocfilesize(docfilesize);
			doc.setDoctime(new Date());
			docList.add(doc);
			mapMedia.put(doc.getDocfileid(), media);
			gridDoc.setModel(new ListModelList<>(docList));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doRenderDoc() {
		if (gridDoc != null) {
			gridDoc.setRowRenderer(new RowRenderer<Torderdoc>() {
				public void render(Row row, Torderdoc data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getDocfileori() != null ? data.getDocfileori() : ""));
					Button btnDel = new Button("Delete");
					btnDel.setAutodisable("self");
					btnDel.setClass("btn-danger");
					btnDel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							if (data.getTorderdocpk() != null) {
								docDelList.add(data);
							}
							mapMedia.remove(data.getDocfileid());
							docList.remove(data);
							gridDoc.setModel(new ListModelList<>(docList));
						}
					});
					row.getChildren().add(btnDel);
				}
			});
		}
	}
	
	@Command
	@NotifyChange("totalqty")
	public void doChange() {
		totalqty = itemqty * mproduct.getMproducttype().getProductunitqty();
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			if(!isEdit)
				objForm.setOrderid(new TcounterengineDAO().generateCounter("ORD"));

			ordertype = "";
			orderlevel = "";
			if (arg.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
				if (branchlevel > 1) {
					objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
					ordertype = objForm.getOrdertype();
					objForm.setOrderlevel(oUser.getMbranch().getBranchlevel());
					orderlevel = objForm.getOrderlevel().toString();
				} else {
					if (type != null && type.equals("P")) {
						objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL);
					} else if (type != null && type.equals("C")) {
						objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
					}
					ordertype = objForm.getOrdertype();
					objForm.setOrderlevel(oUser.getMbranch().getBranchlevel());
					orderlevel = objForm.getOrderlevel().toString();
				}
			} else if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
				if (branchlevel > 1) {
					objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
					ordertype = objForm.getOrdertype();
					objForm.setOrderlevel(oUser.getMbranch().getBranchlevel());
					orderlevel = objForm.getOrderlevel().toString();
				} else {
					if (type != null && type.equals("P")) {
						objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL);
					} else if (type != null && type.equals("C")) {
						objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
					}
					ordertype = objForm.getOrdertype();
					objForm.setOrderlevel(oUser.getMbranch().getBranchlevel());
					orderlevel = objForm.getOrderlevel().toString();
				}
			} else {
				objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
				ordertype = objForm.getOrdertype();
				objForm.setOrderlevel(oUser.getMbranch().getBranchlevel());
				orderlevel = objForm.getOrderlevel().toString();
			}

			status = "";
			if (branchlevel == 2) {
				objForm.setStatus(AppUtils.STATUS_ORDER_WAITAPPROVALWIL);
				status = objForm.getStatus();
			} else if (branchlevel == 3) {
				objForm.setStatus(AppUtils.STATUS_ORDER_WAITAPPROVALCAB);
				status = objForm.getStatus();
			} else {
				objForm.setStatus(AppUtils.STATUS_ORDER_WAITAPPROVAL);
				status = objForm.getStatus();
			}

			// wathc memo or memono here
			if (branchlevel == 3) {
				objForm.setOrderoutlet(moutlet.getOutletcode());
			} else {
				objForm.setOrderoutlet("00");
			}
			if (branchlevel == 1) {
				if (type != null && type.equals("P")) {
					objForm.setMbranch(oUser.getMbranch());
				} else if (type != null && type.equals("C")) {
					objForm.setMbranch(mbranch);
				}
			} else 
				objForm.setMbranch(oUser.getMbranch());
			objForm.setMproduct(mproduct);
			objForm.setItemqty(itemqty);
			objForm.setTotalqty(totalqty);
			objForm.setTotalproses(0);
			objForm.setProductgroup(arg);
			objForm.setOrderdate(new Date());
			objForm.setInsertedby(oUser.getUsername());
			objForm.setMemono(memono);
			objForm.setMemodate(memodate);
			objForm.setMemo(memo);

			oDao.save(session, objForm);

			objMemo = new Tordermemo();
			objMemo.setMemo(memo);
			objMemo.setMemoby(oUser.getUsername());
			objMemo.setMemotime(new Date());
			objMemo.setTorder(objForm);
			oDaoMemo.save(session, objMemo);
			
			FlowHandler.doFlow(session, null, objForm, memo, oUser.getUserid());

			String path = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.MEMO_PATH);
			for (Torderdoc orderdoc : docList) {
				orderdoc.setTorder(objForm);
				new TorderdocDAO().save(session, orderdoc);

				Media mediaDoc = mapMedia.get(orderdoc.getDocfileid());
				if (mediaDoc != null) {
					if (mediaDoc.isBinary()) {
						Files.copy(new File(path + orderdoc.getDocfileid()), mediaDoc.getStreamData());
					} else {
						BufferedWriter writer = new BufferedWriter(new FileWriter(path + orderdoc.getDocfileid()));
						Files.copy(writer, mediaDoc.getReaderData());
						writer.close();
					}
				}
			}

			for (Torderdoc orderdoc : docDelList) {
				new TorderdocDAO().delete(session, orderdoc);

				File file = new File(path + orderdoc.getDocfileid());
				if (file.exists()) {
					System.out.println("FILE : " + orderdoc.getDocfileid());
					file.delete();
				}
			}

			transaction.commit();
			session.close();

			if (!isEdit) {
				if (branchlevel > 1) {
					Mmenu mmenu = new MmenuDAO().findByFilter(
							"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'approval'");
					NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
				} else {
					if (objForm.getMbranch().getMbranchpk().equals(oUser.getMbranch().getMbranchpk())) {
						Mmenu mmenu = new MmenuDAO().findByFilter(
								"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'approvalopr'");
						NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
					} else {
						Mmenu mmenu = new MmenuDAO().findByFilter(
								"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'approval'");
						NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
					}
				}
			}
			doDone();
			Clients.showNotification("Input data pemesanan berhasil", "info", null, "middle_center", 5000);
			doReset();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doDone() {
		try {
			divRoot.getChildren().clear();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objorder", objForm);
			map.put("objmemo", objMemo);
			if (isEdit)
				map.put("isEdit", "Y");
			map.put("arg", arg);
			Executions.createComponents("/view/order/orderentryresume.zul", divRoot, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		objForm = new Torder();
		objForm.setInserttime(new Date());
		memo = "";
		memono = "";
		totalqty = 0;
		itemqty = 0;
		productgroup = AppData.getProductgroupLabel(arg);
		cabang = oUser.getMbranch().getBranchname();
		wilayah = oUser.getMbranch().getMregion().getRegionname();
		cbProduct.setValue(null);
		docList = new ArrayList<Torderdoc>();
		mapMedia = new HashMap<String, Media>();
		gridDoc.setModel(new ListModelList<>(docList));
		doRenderDoc();
		if (arg.equals("04"))
			unit = "04";
		else
			unit = "02";
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {
					if (oUser.getMbranch().getBranchlevel() == 3) {
						String orderoutlet = (String) ctx.getProperties("orderoutlet")[0].getValue();
						if (orderoutlet == null || "".trim().equals(orderoutlet))
							this.addInvalidMessage(ctx, "orderoutlet", Labels.getLabel("common.validator.empty"));
					}

					if (mproduct == null)
						this.addInvalidMessage(ctx, "mproduct", Labels.getLabel("common.validator.empty"));

					if (itemqty == null)
						this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));

					if (memo == null || "".trim().equals(memo))
						this.addInvalidMessage(ctx, "memo", Labels.getLabel("common.validator.empty"));

//					if (memono == null || "".trim().equals(memono))
//						this.addInvalidMessage(ctx, "memono", Labels.getLabel("common.validator.empty"));

					if (memodate == null)
						this.addInvalidMessage(ctx, "memodate", Labels.getLabel("common.validator.empty"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct("productgroup = '" + arg + "' and isdlvhome != 'Y'"));
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

	public String getDocfileori() {
		return docfileori;
	}

	public void setDocfileori(String docfileori) {
		this.docfileori = docfileori;
	}

	public String getDocfileid() {
		return docfileid;
	}

	public void setDocfileid(String docfileid) {
		this.docfileid = docfileid;
	}

	public Integer getDocfilesize() {
		return docfilesize;
	}

	public void setDocfilesize(Integer docfilesize) {
		this.docfilesize = docfilesize;
	}

	public Date getDoctime() {
		return doctime;
	}

	public void setDoctime(Date doctime) {
		this.doctime = doctime;
	}

	public String getMemono() {
		return memono;
	}

	public void setMemono(String memono) {
		this.memono = memono;
	}

	public Date getMemodate() {
		return memodate;
	}

	public void setMemodate(Date memodate) {
		this.memodate = memodate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getOrdertype() {
		return ordertype;
	}

	public void setOrdertype(String ordertype) {
		this.ordertype = ordertype;
	}

	public String getOrderlevel() {
		return orderlevel;
	}

	public void setOrderlevel(String orderlevel) {
		this.orderlevel = orderlevel;
	}

	public Integer getTotalqty() {
		return totalqty;
	}

	public void setTotalqty(Integer totalqty) {
		this.totalqty = totalqty;
	}

	public Integer getItemqty() {
		return itemqty;
	}

	public void setItemqty(Integer itemqty) {
		this.itemqty = itemqty;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public ListModelList<Moutlet> getMoutletmodel() {
		return moutletmodel;
	}

	public void setMoutletmodel(ListModelList<Moutlet> moutletmodel) {
		this.moutletmodel = moutletmodel;
	}

	public Moutlet getMoutlet() {
		return moutlet;
	}

	public void setMoutlet(Moutlet moutlet) {
		this.moutlet = moutlet;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}
}
