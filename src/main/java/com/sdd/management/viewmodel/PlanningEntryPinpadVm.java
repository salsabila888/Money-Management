package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
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
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.dao.TplandocDAO;
import com.sdd.caption.dao.TplanproductDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.domain.Tplandoc;
import com.sdd.caption.domain.Tplanproduct;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TorderdocListModel;
import com.sdd.caption.model.TplandocListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PlanningEntryPinpadVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private TplandocListModel model;

	private Tplan objForm;
	private Tplandoc objDoc;
	private TplanDAO oDao = new TplanDAO();

	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	private String productgroup;
	private String arg;
	private Date memodate;
	private String filename;
	private String producttype;
	private String cabang;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String filterdoc;
	private String orderby;
	private String orderbydoc;
	private boolean isEdit = false;
	private Media media;

	private String docfileori, docfileid;
	private Integer docfilesize;
	private Date doctime;

	private List<Tplandoc> docList;
	private List<Tplandoc> docDelList = new ArrayList<Tplandoc>();
	private Map<String, Media> mapMedia = new HashMap<String, Media>();

	@Wire
	private Grid gridDoc;
	@Wire
	private Caption caption;
//	@Wire
//	private Window winPlanEntry;
	@Wire
	private Div divRoot;
	@Wire
	private Row rowUpload, rowDoc;
//	@Wire
//	private Paging paging;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("isDetail") String isDetail, @ExecutionArgParam("obj") Tplan obj) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		this.arg = arg;
		doReset();

		if (isDetail != null && isDetail.equals("Y")) {
			caption.setVisible(true);
		}

		if (obj != null) {
			objForm = obj;

			docList = new TplandocDAO().listByFilter("tplanfk = " + obj.getTplanpk(), "tplanfk");
			gridDoc.setModel(new ListModelList<>(docList));
			memodate = obj.getMemodate();
			cabang = obj.getMbranch().getBranchname();

			if (obj.getMemofileori() != null && obj.getMemofileori().trim().length() > 0) {
				filename = obj.getMemofileori();
			}

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
			Tplandoc doc = new Tplandoc();
			doc.setDocfileori(media.getName());
			doc.setDocfileid(
					"PLANDOC" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + "." + media.getFormat());
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
			gridDoc.setRowRenderer(new RowRenderer<Tplandoc>() {
				public void render(Row row, Tplandoc data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getDocfileori() != null ? data.getDocfileori() : ""));
					Button btnDel = new Button("Delete");
					btnDel.setAutodisable("self");
					btnDel.setClass("btn-danger");
					btnDel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							if (data.getTplandocpk() != null) {
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
			String prefix = "";
			if (arg.equals("01"))
				prefix = "PC";
			else if (arg.equals("02"))
				prefix = "PT";
			else if (arg.equals("03"))
				prefix = "PP";
			else if (arg.equals("04"))
				prefix = "PD";

			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			if (!isEdit) {
				objForm.setPlanno(new TcounterengineDAO().generateCounter(prefix));
				objForm.setProductgroup(arg);
			}
			objForm.setMbranch(oUser.getMbranch());
			objForm.setMemodate(memodate);
			objForm.setTotalprocess(0);
			objForm.setStatus(AppUtils.STATUS_PLANNING_WAITAPPROVALPFA);
			oDao.save(session, objForm);

			String path = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.MEMO_PATH);
			for (Tplandoc plandoc : docList) {
				plandoc.setTplan(objForm);
				new TplandocDAO().save(session, plandoc);

				Media mediaDoc = mapMedia.get(plandoc.getDocfileid());
				if (mediaDoc != null) {
					if (mediaDoc.isBinary()) {
						Files.copy(new File(path + plandoc.getDocfileid()), mediaDoc.getStreamData());
					} else {
						BufferedWriter writer = new BufferedWriter(new FileWriter(path + plandoc.getDocfileid()));
						Files.copy(writer, mediaDoc.getReaderData());
						writer.close();
					}
				}
			}

			for (Tplandoc plandoc : docDelList) {
				new TplandocDAO().delete(session, plandoc);

				File file = new File(path + plandoc.getDocfileid());
				if (file.exists()) {
					System.out.println("FILE : " + plandoc.getDocfileid());
					file.delete();
				}
			}

			if (!isEdit) {
				Mmenu mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/planning/planingapprovalbydiv.zul ' and menuparamvalue = 'pfa'");
				NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
			}
			doDone();
			doReset();

//				if (isEdit) {
//					Event closeEvent = new Event("onClose", winPlanEntry, null);
//					Events.postEvent(closeEvent);
//				}

			transaction.commit();
			session.close();
			Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 5000);

			doReset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doDone() {
		try {
			divRoot.getChildren().clear();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("obj", objForm);
			if (isEdit)
				map.put("isEdit", "Y");
			map.put("arg", arg);
			Executions.createComponents("/view/planning/planningentryresume.zul", divRoot, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			orderby = "producttype";
			filter = "productgroupcode = '" + arg + "' and mproductowner.mbranchfk = "
					+ oUser.getMbranch().getMbranchpk();

			if (producttype != null && producttype.trim().length() > 0)
				filter += " and producttype like '%" + producttype.toUpperCase() + "%'";

			filterdoc = "tplanfk = " + objForm.getTplanpk();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		productgroup = AppData.getProductgroupLabel(arg);
		cabang = oUser.getMbranch().getBranchname();
		filename = "";
		docfileori = "";
		memodate = null;
		doctime = null;
		docfilesize = null;

		objForm = new Tplan();
		objForm.setInputtime(new Date());
		objForm.setInputer(oUser.getUserid());

		docList = new ArrayList<Tplandoc>();
		mapMedia = new HashMap<String, Media>();
		gridDoc.setModel(new ListModelList<>(docList));
		doRenderDoc();
		doSearch();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				BigDecimal anggaran = (BigDecimal) ctx.getProperties("anggaran")[0].getValue();
				String memono = (String) ctx.getProperties("memono")[0].getValue();

				if (anggaran == null)
					this.addInvalidMessage(ctx, "anggaran", Labels.getLabel("common.validator.empty"));
				if (memono == null || "".equals(memono.trim()))
					this.addInvalidMessage(ctx, "memono", Labels.getLabel("common.validator.empty"));
				if (memodate == null)
					this.addInvalidMessage(ctx, "memodate", Labels.getLabel("common.validator.empty"));

			}
		};
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public Tplan getObjForm() {
		return objForm;
	}

	public void setObjForm(Tplan objForm) {
		this.objForm = objForm;
	}

	public Date getMemodate() {
		return memodate;
	}

	public void setMemodate(Date memodate) {
		this.memodate = memodate;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public String getCabang() {
		return cabang;
	}

	public void setCabang(String cabang) {
		this.cabang = cabang;
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

	public Tplandoc getObjDoc() {
		return objDoc;
	}

	public void setObjDoc(Tplandoc objDoc) {
		this.objDoc = objDoc;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}
}