package com.sdd.management.viewmodel;

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
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.sdd.management.dao.TcounterengineDAO;
import com.sdd.management.dao.TmutationDAO;
import com.sdd.management.dao.TmutationdocDAO;
import com.sdd.management.domain.Maim;
import com.sdd.management.domain.Mbank;
import com.sdd.management.domain.Mexpenses;
import com.sdd.management.domain.Mincome;
import com.sdd.management.domain.Mpayment;
import com.sdd.management.domain.Mproduct;
import com.sdd.management.domain.Muser;
import com.sdd.management.domain.Tmutation;
import com.sdd.management.domain.Tmutationdoc;
import com.sdd.management.utils.AppData;
import com.sdd.management.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MutationFormVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Tmutation objForm;
	private TmutationDAO oDao = new TmutationDAO();

	private String unit;
	private Media media;
	private boolean isEdit = false;
	private Mproduct mproduct;
	private Mincome mincome;
	private Mexpenses mexpenses;
	private Mpayment mpayment;
	private Mbank mbank;
	private Maim maim;
	
	private String docfileori, docfileid;
	private Integer docfilesize;
	private Date doctime;

	private List<Tmutationdoc> docList;
	private List<Tmutationdoc> docDelList = new ArrayList<Tmutationdoc>();
	private Map<String, Media> mapMedia = new HashMap<String, Media>();

	@Wire
	private Div divRoot;
	@Wire
	private Row rowUpload, rowDoc, rowTotal, rowOutlet;
	@Wire
	private Grid gridDoc;
	@Wire
	private Caption caption;
	@Wire
	private Div tabPage;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doTab("1");
		doReset();
	}
	
	@Command
	@NotifyChange("title")
    public void doTab(@BindingParam("tab") String tab) {
		String page = "";
    	tabPage.getChildren().clear();
		Map<String, Object> map = new HashMap<>();	
		map.put("isAdmin", new Boolean(true));
    	if (tab.equals("1")) {
    		map.put("type", "I");
    		page = "/view/moneymng/mutationform.zul";
    	} else if (tab.equals("2")) {
    		map.put("type", "E");		
    		page = "/view/moneymng/mutationform.zul";
    	} 
    	Executions.createComponents(page, tabPage, map);
    }
	
	@Command
	@NotifyChange("docfileori")
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			Tmutationdoc doc = new Tmutationdoc();
			doc.setDocfileori(media.getName());
			doc.setDocfileid(
					"TRXDOC" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + "." + media.getFormat());
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
			gridDoc.setRowRenderer(new RowRenderer<Tmutationdoc>() {
				public void render(Row row, Tmutationdoc data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getDocfileori() != null ? data.getDocfileori() : ""));
					Button btnDel = new Button("Delete");
					btnDel.setAutodisable("self");
					btnDel.setClass("btn-danger");
					btnDel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							if (data.getTmutationdocpk() != null) {
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
			if(!isEdit)
				objForm.setMutationno(new TcounterengineDAO().generateCounter("TRX"));

			objForm.setMutationdate(new Date());
			objForm.setMincome(mincome);
			objForm.setCreatedby(oUser.getUsername());
			oDao.save(session, objForm);

			String path = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.MEMO_PATH);
			for (Tmutationdoc trxdoc : docList) {
				trxdoc.setTmutation(objForm);
				new TmutationdocDAO().save(session, trxdoc);

				Media mediaDoc = mapMedia.get(trxdoc.getDocfileid());
				if (mediaDoc != null) {
					if (mediaDoc.isBinary()) {
						Files.copy(new File(path + trxdoc.getDocfileid()), mediaDoc.getStreamData());
					} else {
						BufferedWriter writer = new BufferedWriter(new FileWriter(path + trxdoc.getDocfileid()));
						Files.copy(writer, mediaDoc.getReaderData());
						writer.close();
					}
				}
			}

			for (Tmutationdoc trxdoc : docDelList) {
				new TmutationdocDAO().delete(session, trxdoc);

				File file = new File(path + trxdoc.getDocfileid());
				if (file.exists()) {
					System.out.println("FILE : " + trxdoc.getDocfileid());
					file.delete();
				}
			}
			
			transaction.commit();
			session.close();

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
			if (isEdit)
				map.put("isEdit", "Y");
			Executions.createComponents("/view/order/orderentryresume.zul", divRoot, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		objForm = new Tmutation();
		objForm.setMutationdate(new Date());
		mapMedia = new HashMap<String, Media>();
		unit = "02";
		docList = new ArrayList<Tmutationdoc>();
		mapMedia = new HashMap<String, Media>();
		gridDoc.setModel(new ListModelList<>(docList));
		doRenderDoc();
	}

	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct(" isdlvhome != 'Y'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public ListModelList<Mincome> getMincomemodel() {
		ListModelList<Mincome> lm = null;
		try {
			lm = new ListModelList<Mincome>(AppData.getMincome());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public ListModelList<Mexpenses> getMexpensesmodel() {
		ListModelList<Mexpenses> lm = null;
		try {
			lm = new ListModelList<Mexpenses>(AppData.getMexpenses());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public ListModelList<Mpayment> getMpaymentmodel() {
		ListModelList<Mpayment> lm = null;
		try {
			lm = new ListModelList<Mpayment>(AppData.getMpayment());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mbank> getMbankmodel() {
		ListModelList<Mbank> lm = null;
		try {
			lm = new ListModelList<Mbank>(AppData.getMbank());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public ListModelList<Maim> getMaimmodel() {
		ListModelList<Maim> lm = null;
		try {
			lm = new ListModelList<Maim>(AppData.getMaim());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public Tmutation getObjForm() {
		return objForm;
	}

	public void setObjForm(Tmutation objForm) {
		this.objForm = objForm;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
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

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}
	
	public Mincome getMincome() {
		return mincome;
	}

	public void setMincome(Mincome mincome) {
		this.mincome = mincome;
	}

	public Mexpenses getMexpenses() {
		return mexpenses;
	}

	public void setMexpenses(Mexpenses mexpenses) {
		this.mexpenses = mexpenses;
	}

	public Mpayment getMpayment() {
		return mpayment;
	}

	public void setMpayment(Mpayment mpayment) {
		this.mpayment = mpayment;
	}

	public Mbank getMbank() {
		return mbank;
	}

	public void setMbank(Mbank mbank) {
		this.mbank = mbank;
	}

	public Maim getMaim() {
		return maim;
	}

	public void setMaim(Maim maim) {
		this.maim = maim;
	}

}
