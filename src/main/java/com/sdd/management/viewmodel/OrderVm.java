package com.sdd.caption.viewmodel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.io.Files;
import org.zkoss.lang.Library;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.dao.TmissingbranchDAO;
import com.sdd.caption.dao.TmissingproductDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TembossfileDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TpinmailerbranchDAO;
import com.sdd.caption.dao.TpinmailerdataDAO;
import com.sdd.caption.dao.TpinmailerfileDAO;
import com.sdd.caption.dao.TproductmmDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Tmissingbranch;
import com.sdd.caption.domain.Tmissingproduct;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.domain.Tpinmailerbranch;
import com.sdd.caption.domain.Tpinmailerdata;
import com.sdd.caption.domain.Tpinmailerorder;
import com.sdd.caption.domain.Tproductmm;
import com.sdd.caption.domain.Vmissingbranch;
import com.sdd.caption.domain.Vmissingproduct;
import com.sdd.caption.handler.PinmailerHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OrderVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Torder objForm;

	private TorderDAO oDao = new TorderDAO();
	private TpinmailerbranchDAO branchDao = new TpinmailerbranchDAO();
	private TpinmailerdataDAO dataDao = new TpinmailerdataDAO();
	private TmissingproductDAO tmpDao = new TmissingproductDAO();
	private TmissingbranchDAO tmbDao = new TmissingbranchDAO();

	private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private DateFormat datedbFormatter = new SimpleDateFormat("yyMMdd");

	private int maxFlush;
	private int maxBatchCommit;
	private int flushCounter;

	private Integer totaldata;
	private Integer totalinserted;
	private Integer totalfailed;
	private int totalmissingproduct;
	private int totalmissingbranch;	
	private String title;
	private String filename;
	private Media media;

	private Map<String, Mbranch> mapBranch = new HashMap<String, Mbranch>();
	private Map<String, Mproduct> mapProduct = new HashMap<String, Mproduct>();
	private Map<String, Morg> mapOrg = new HashMap<String, Morg>();

	private Map<String, Tmissingproduct> mapMissingProduct;
	private Map<String, Tmissingbranch> mapMissingBranch;
	
	private List<Tmissingproduct> missingproductList;
	private List<Tmissingbranch> missingbranchList;
	private List<Vmissingbranch> vmissingBranchList;
	private List<Vmissingproduct> vmissingProductList;
	
	private Map<String, Tpinmailerbranch> mapTbranch;
	private Map<String, List<Tpinmailerdata>> mapTdata;
	private List<Tpinmailerdata> listTod;
	
	private String productgroup;
	
	@Wire
	private Button btnBrowse;
	@Wire
	private Button btnSave;
	@Wire
	private Label fileBrowse;
	@Wire
	private Groupbox gbResult;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroup = arg;
		title = "Order Produk " + AppData.getProductgroupLabel(productgroup);
		doReset();
		try {
			maxFlush = Integer.parseInt(Library.getProperty("maxFlush"));
			maxBatchCommit = Integer.parseInt(Library.getProperty("maxBatchCommit"));
			mapBranch = new HashMap<String, Mbranch>();
			for (Mbranch obj : AppData.getMbranch()) {
				mapBranch.put(obj.getBranchid(), obj);
			}
			mapProduct = new HashMap<String, Mproduct>();
			for (Mproduct obj : AppData.getMproduct("productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'")) {
				mapProduct.put(obj.getProductcode() + obj.getIsinstant(), obj);
			}
			mapOrg = new HashMap<String, Morg>();
			for (Morg obj : AppData.getMorg()) {
				mapOrg.put(obj.getOrg(), obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("filename")
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			fileBrowse.setVisible(true);
			filename = media.getName();
			if (media != null) {
				Torder obj = oDao.findByFilter("filename = '" + media.getName().toUpperCase() + "' and productgroup = '" + productgroup + "'");
				if (obj == null) {
					btnSave.setDisabled(false);
				} else {
					Messagebox.show("File sudah pernah diupload", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("objForm")
	public void doDelete() {
		try {
			Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								try {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									oDao.delete(session, objForm);
									transaction.commit();
									session.close();

									Clients.showNotification(Labels.getLabel("common.delete.success"), "info", null,
											"middle_center", 3000);

									doReset();
									BindUtils.postNotifyChange(null, null, OrderVm.this, "objForm");
								} catch (HibernateException e) {
									transaction.rollback();
									Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								} catch (Exception e) {
									Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								}
							}
						}

					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doView(@BindingParam("arg") String arg) {
		if (arg.equals("missingbranch")) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objList", vmissingBranchList);
			Window win = (Window) Executions.createComponents("/view/emboss/missingbranch.zul", null, map);
			win.setWidth("60%");
			win.setClosable(true);
			win.doModal();
		} else if (arg.equals("missingproduct")) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objList", vmissingProductList);
			Window win = (Window) Executions.createComponents("/view/emboss/missingproduct.zul", null, map);
			win.setWidth("60%");
			win.setClosable(true);
			win.doModal();
		} else if (arg.equals("alldata")) {
			String path = "";
			if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER))
				path = "/view/pinmailer/pinmailerbranch.zul";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("obj", objForm);
			Window win = (Window) Executions.createComponents(path, null, map);
			win.setWidth("90%");
			win.setClosable(true);
			win.doModal();
		}

	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		objForm = new Torder();
		objForm.setInserttime(new Date());
		objForm.setProductgroup(productgroup);
		totaldata = 0;
		totalinserted = 0;
		totalmissingproduct = 0;
		totalmissingbranch = 0;
		totalfailed = 0;
		gbResult.setVisible(false);
		btnBrowse.setDisabled(false);
		btnSave.setDisabled(true);
		fileBrowse.setVisible(false);
		filename = null;
		media = null;

		mapMissingProduct = new HashMap<String, Tmissingproduct>();
		mapMissingBranch = new HashMap<String, Tmissingbranch>();
		listTod = new ArrayList<>();
		missingproductList = new ArrayList<>();
		missingbranchList = new ArrayList<>();
		
		vmissingBranchList = new ArrayList<>();
		vmissingProductList = new ArrayList<>();
	}
	
	public ListModelList<Mproduct> getMproduct() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct("productgroup = '" + AppUtils.PRODUCTGROUP_PINMAILER + "'"));
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

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Integer getTotalinserted() {
		return totalinserted;
	}

	public void setTotalinserted(Integer totalinserted) {
		this.totalinserted = totalinserted;
	}

	public Integer getTotalfailed() {
		return totalfailed;
	}

	public void setTotalfailed(Integer totalfailed) {
		this.totalfailed = totalfailed;
	}

	public int getTotalmissingproduct() {
		return totalmissingproduct;
	}

	public void setTotalmissingproduct(int totalmissingproduct) {
		this.totalmissingproduct = totalmissingproduct;
	}

	public int getTotalmissingbranch() {
		return totalmissingbranch;
	}

	public void setTotalmissingbranch(int totalmissingbranch) {
		this.totalmissingbranch = totalmissingbranch;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
