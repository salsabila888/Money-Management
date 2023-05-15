package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TplanproductDAO;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.domain.Tplandoc;
import com.sdd.caption.domain.Tplanproduct;
import com.sdd.caption.model.TplandocListModel;
import com.sdd.caption.model.TplanproductListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class PlanningDataVm {

	private Session zkSession = Sessions.getCurrent();

	private TplanproductListModel model;
	private TplandocListModel modelDocument;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private String orderbydoc;

	private Tplan obj;
	private Tplandoc objDoc;
	private String branchid;
	private String branchname;
	private String producttype;
	private String productcode;
	private String productname;
	private Boolean isSaved;
	private String isDetail;
	private String decisiontime;
	private Integer totalitem;
	private Integer totalkonversi;
	private Integer totalprocess;
	private Integer totaloutstanding;
	private String arg;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd MMMMM yyyy HH:mm");

	private List<Tplanproduct> objList = new ArrayList<Tplanproduct>();

	@Wire
	private Window winPlanningdata;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid, gridDocument;
	@Wire
	private Label lbTitle;
	@Wire
	private Column colKonversi, colJumlah, colSisa, colTotal;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tplan tplan,
			@ExecutionArgParam("isDetail") final String isDetail, @ExecutionArgParam("arg") String arg) throws ParseException {
		Selectors.wireComponents(view, this, false);
		obj = tplan;

		if (obj != null) {
			if (obj.getDecisionby() == null)
				obj.setDecisionby("-");

			if (obj.getDecisiontime() == null)
				decisiontime = "-";
			else
				decisiontime = datetimelocalFormatter.format(obj.getDecisiontime());

			if (obj.getDecisiondesc() == null)
				obj.setDecisiondesc("-");
		}

		if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			colKonversi.setVisible(true);
			colTotal.setLabel("Total Proses(Buku/Set/Lembar)");
			colSisa.setLabel("Jumlah Sisa(Buku/Set/Lembar)");
			colJumlah.setLabel("Total Buku");
		} else {
			colTotal.setLabel("Total Proses");
			colSisa.setLabel("Jumlah Sisa");
			colJumlah.setLabel("Total Unit");
		}
		
		doReset();
		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (isDetail != null && isDetail.equals("Y")) {
			this.isDetail = isDetail;
			lbTitle.setValue("Detail Daftar Inisiasi Pengadaan");
			lbTitle.setStyle("font-size: 20px");
		} else {
			lbTitle.setValue("Detail Approval Inisiasi Pengadaan");
			lbTitle.setStyle("font-size: 20px");
		}

		if (gridDocument != null) {
			gridDocument.setRowRenderer(new RowRenderer<Tplandoc>() {
				@Override
				public void render(Row row, Tplandoc data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					A a = new A(data.getDocfileori());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Sessions.getCurrent().setAttribute("reportPath",
									AppUtils.FILES_ROOT_PATH + AppUtils.MEMO_PATH + data.getDocfileid());
							Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
						}
					});
					row.getChildren().add(a);
				}
			});
		}

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tplanproduct>() {
				@Override
				public void render(Row row, final Tplanproduct data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren().add(new Label(data.getMproducttype().getProductgroupname()));
					row.getChildren().add(new Label(data.getMproducttype().getProducttype()));
					row.getChildren()
							.add(new Label(data.getTplan().getInputtime() != null
									? datelocalFormatter.format(data.getTplan().getInputtime())
									: ""));
					row.getChildren().add(new Label(
							data.getUnitqty() != null ? NumberFormat.getInstance().format(data.getUnitqty()) : "0"));
					row.getChildren()
							.add(new Label(data.getQtyperunit() != null
									? NumberFormat.getInstance().format(data.getQtyperunit())
									: "0"));
					row.getChildren()
							.add(new Label(data.getQtyperunit() != null
									? NumberFormat.getInstance().format(data.getTotalprocess())
									: "0"));
					row.getChildren()
							.add(new Label(data.getQtyperunit() != null
									? NumberFormat.getInstance().format(data.getQtyperunit() - data.getTotalprocess())
									: "0"));
				}
			});
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tplanproduct.tplanproductpk";
		orderbydoc = "tplandocpk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TplanproductListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
		modelDocument = new TplandocListModel(activePage, SysUtils.PAGESIZE, filter, orderbydoc);
		gridDocument.setModel(modelDocument);
	}

	@Command
	public void doPrint() {
		try {
			List<Tplanproduct> objList = new TplanproductDAO().listByFilter("tplan.tplanpk = " + obj.getTplanpk(),
					"tplanproductpk");
			Map<String, String> parameters = new HashMap<>();
			parameters.put("PLANNO", obj.getPlanno());
			parameters.put("INPUTTIME", datetimelocalFormatter.format(obj.getInputtime()));
			zkSession.setAttribute("objList", objList);
			zkSession.setAttribute("parameters", parameters);
			zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(SysUtils.JASPER_PATH + "/persomanifest.jasper"));
			Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doFind(@BindingParam("item") String item) {
		try {
			String path = "";
			if (item.equals("single"))
				path = "/view/perso/persopendingfinddetail.zul";
			else if (item.equals("group"))
				path = "/view/perso/persopendingfindgroup.zul";
			Map<String, Object> map = new HashMap<>();
			map.put("obj", obj);

			Window win = (Window) Executions.createComponents(path, null, map);
			win.setWidth("90%");
			win.setClosable(true);
			win.doModal();
			win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getData() != null) {
						obj = (Tplan) map.get("obj");
						isSaved = (Boolean) map.get("isSaved");
						BindUtils.postNotifyChange(null, null, PlanningDataVm.this, "obj");
					}
					needsPageUpdate = false;
					refreshModel(pageStartNumber);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		try {
			filter = "tplanfk = " + obj.getTplanpk();
			if (producttype != null && producttype.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
			}
			if (productcode != null && productcode.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
			}
			if (productname != null && productname.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
			}
			if (branchid != null && branchid.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mbranch.branchid like '%" + branchid.trim().toUpperCase() + "%'";
			}
			if (branchname != null && branchname.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mbranch.branchname like '%" + branchname.trim().toUpperCase() + "%'";
			}

			objList = new TplanproductDAO().listNativeByFilter(filter, "tplanproductpk");
			totalitem = 0;
			totalkonversi = 0;
			totaloutstanding = 0;
			totalprocess = 0;
			for (Tplanproduct data : objList) {
				totalitem = totalitem + data.getUnitqty();
				totalkonversi = totalkonversi + data.getQtyperunit();
				totalprocess = totalprocess + data.getTotalprocess();
				totaloutstanding = totaloutstanding + (data.getQtyperunit() - data.getTotalprocess());
			}
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winPlanningdata, isSaved);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException {
		branchname = null;
		producttype = null;
		productcode = null;
		productname = null;
		doSearch();
	}

	public Tplan getObj() {
		return obj;
	}

	public void setObj(Tplan obj) {
		this.obj = obj;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public String getBranchid() {
		return branchid;
	}

	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}

	public Tplandoc getObjDoc() {
		return objDoc;
	}

	public void setObjDoc(Tplandoc objDoc) {
		this.objDoc = objDoc;
	}

	public String getDecisiontime() {
		return decisiontime;
	}

	public void setDecisiontime(String decisiontime) {
		this.decisiontime = decisiontime;
	}

	public Integer getTotalitem() {
		return totalitem;
	}

	public void setTotalitem(Integer totalitem) {
		this.totalitem = totalitem;
	}

	public Integer getTotalkonversi() {
		return totalkonversi;
	}

	public void setTotalkonversi(Integer totalkonversi) {
		this.totalkonversi = totalkonversi;
	}

	public Integer getTotalprocess() {
		return totalprocess;
	}

	public void setTotalprocess(Integer totalprocess) {
		this.totalprocess = totalprocess;
	}

	public Integer getTotaloutstanding() {
		return totaloutstanding;
	}

	public void setTotaloutstanding(Integer totaloutstanding) {
		this.totaloutstanding = totaloutstanding;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

}
