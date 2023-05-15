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
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderdocDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Torderdoc;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OrderEntryPinpadVm {

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
	private Integer totalcs;
	private Integer totalteller;
	private Integer totalqty;
	private int branchlevel;
	private Media media;
	private Mbranch mbranch;
	private boolean isEdit = false;

	private String status, orderid, ordertype, orderlevel;

	private String docfileori, docfileid;
	private Integer docfilesize;
	private Date doctime;

	private List<Torderdoc> docList;
	private List<Torderdoc> docDelList = new ArrayList<Torderdoc>();
	private Map<String, Media> mapMedia = new HashMap<String, Media>();

	@Wire
	private Div divRoot;
	@Wire
	private Row outlet, rowUpload, rowDoc, rowCS, rowTeller;
	@Wire
	private Grid gridDoc;
	@Wire
	private Caption caption;
	@Wire
	private Checkbox tipeC, tipeT;
	@Wire
	private Intbox intcs, inttel, inttotal;
	@Wire
	private Textbox tbBranch;
	@Wire
	private Combobox cbBranch;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("obj") Torder obj, @ExecutionArgParam("objmemo") Tordermemo objMemo) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();

		this.arg = arg;
		this.objMemo = objMemo;
		doReset();

		if (oUser.getMbranch().getBranchlevel() == 3) {
			outlet.setVisible(true);
			tbBranch.setVisible(true);
			cbBranch.setVisible(false);
		} else if (oUser.getMbranch().getBranchlevel() == 2) {
			outlet.setVisible(false);
			tbBranch.setVisible(true);
			cbBranch.setVisible(false);
		} else {
			outlet.setVisible(false);
			tbBranch.setVisible(false);
			cbBranch.setVisible(true);
		}

		if (obj != null) {
			objForm = obj;
			memono = obj.getMemono();
			memodate = obj.getMemodate();
			memo = obj.getMemo();
			if (obj.getTotalcs() != 0) {
				tipeC.setChecked(true);
				intcs.setDisabled(false);
				totalcs = obj.getTotalcs();
			} else {
				totalcs = 0;
				tipeC.setChecked(false);
			}
			if (obj.getTotalteller() != 0) {
				tipeT.setChecked(true);
				inttel.setDisabled(false);
				totalteller = obj.getTotalteller();
			} else {
				totalteller = 0;
				tipeT.setChecked(false);
			} 
			
			totalqty = obj.getTotalqty();

			docList = new TorderdocDAO().listByFilter("torderfk = " + obj.getTorderpk(), "torderfk");
			gridDoc.setModel(new ListModelList<>(docList));

			isEdit = true;
			caption.setVisible(true);
		}
	}

	public void doChecked() {
		if (tipeC.isChecked()) {
			intcs.setDisabled(false);
		} else {
			intcs.setDisabled(true);
		}
		
		if (tipeT.isChecked()) {
			inttel.setDisabled(false);
		} else {
			inttel.setDisabled(true);
		}
	}
	
	@Command
	@NotifyChange("totalqty")
	public void doChange() {
		totalqty = totalcs + totalteller;
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
	@NotifyChange("*")
	public void doSave() {
		try {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			if (!isEdit)
				objForm.setOrderid(new TcounterengineDAO().generateCounter("ORD"));
			ordertype = "";
			orderlevel = "";
			objForm.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
			ordertype = objForm.getOrdertype();
			objForm.setOrderlevel(oUser.getMbranch().getBranchlevel());
			orderlevel = objForm.getOrderlevel().toString();

			status = "";
			status = objForm.getStatus();
			if (branchlevel == 3) {
				objForm.setStatus(AppUtils.STATUS_ORDER_WAITAPPROVALCAB);
			} else {
				objForm.setStatus(AppUtils.STATUS_ORDER_WAITAPPROVAL);
			}

			// wathc memo or memono here
			if (mbranch != null) {
				objForm.setMbranch(mbranch);
			} else 
				objForm.setMbranch(oUser.getMbranch());
			objForm.setProductgroup(arg);
			objForm.setOrderdate(new Date());
			objForm.setInsertedby(oUser.getUsername());
			objForm.setMemono(memono);
			objForm.setMemodate(memodate);
			objForm.setMemo(memo);
			if (totalcs != 0 && totalteller != 0) {
				objForm.setOrderpinpadtype(AppUtils.PINPADTYPE_BOTH);
			} else {
				if (totalcs != 0) {
					objForm.setOrderpinpadtype(AppUtils.PINPADTYPE_CS);
				} else if (totalteller != 0) {
					objForm.setOrderpinpadtype(AppUtils.PINPADTYPE_TELLER);
				}
			}
			objForm.setTotalcs(totalcs);
			objForm.setTotalteller(totalteller);
			objForm.setTotalqty(totalqty);
			objForm.setItemqty(totalqty);
			objForm.setTotalproses(0);

			oDao.save(session, objForm);

			objMemo = new Tordermemo();
			objMemo.setMemo(memo);
			objMemo.setMemoby(oUser.getUsername());
			objMemo.setMemotime(new Date());
			objMemo.setTorder(objForm);
			oDaoMemo.save(session, objMemo);

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
				Mmenu mmenu = new MmenuDAO().findByFilter(
						"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'approval'");
				NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());

			} else {
				Mmenu mmenu = new MmenuDAO().findByFilter(
						"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'approval'");
				NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
				
				mmenu = new MmenuDAO().findByFilter(
						"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'list'");
				NotifHandler.delete(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
			}
			doDone();
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
		totalcs = 0;
		totalteller = 0;
		totalqty = 0;
		productgroup = AppData.getProductgroupLabel(arg);
		cabang = oUser.getMbranch().getBranchname();
		wilayah = oUser.getMbranch().getMregion().getRegionname();
		docList = new ArrayList<Torderdoc>();
		mapMedia = new HashMap<String, Media>();
		gridDoc.setModel(new ListModelList<>(docList));
		doRenderDoc();
		if (arg.equals("04"))
			unit = "04";
		else
			unit = "02";
		objForm.setOrderoutlet("00");
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

	public Integer getTotalcs() {
		return totalcs;
	}

	public void setTotalcs(Integer totalcs) {
		this.totalcs = totalcs;
	}

	public Integer getTotalteller() {
		return totalteller;
	}

	public void setTotalteller(Integer totalteller) {
		this.totalteller = totalteller;
	}

	public Integer getTotalqty() {
		return totalqty;
	}

	public void setTotalqty(Integer totalqty) {
		this.totalqty = totalqty;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

}
