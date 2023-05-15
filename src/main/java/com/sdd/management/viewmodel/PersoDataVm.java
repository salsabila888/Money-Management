package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.model.TpersodataListModel;
import com.sdd.utils.SysUtils;

public class PersoDataVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private TpersodataListModel model;
		
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;

	private Tperso obj;
	private String branchid;
	private String branchname;
	private String producttype;
	private String productcode;
	private String productname;
	private Boolean isSaved;
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd MMMMM yyyy HH:mm");

	@Wire
	private Window winPersodata;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	
	/*@Wire
	private Foot footButton;
	@Wire
	private Button btnFindDetail;
	@Wire
	private Button btnFindGroup;*/

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tperso tperso)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		obj = tperso;
		
		doSearch();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tpersodata>() {

				@Override
				public void render(Row row, final Tpersodata data, int index)
						throws Exception {
					row.getChildren()
							.add(new Label(
									String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
											+ index + 1)));			
					row.getChildren().add(new Label(data.getTperso().getMproduct().getMproducttype().getProducttype()));
					row.getChildren().add(new Label(data.getTperso().getMproduct().getProductcode()));
					row.getChildren().add(new Label(data.getTperso().getMproduct().getProductname()));
					row.getChildren().add(new Label(data.getMbranch().getBranchname()));
					row.getChildren().add(new Label(data.getTperso().getOrderdate() != null ? datelocalFormatter.format(data.getTperso().getOrderdate()) : ""));
					row.getChildren().add(new Label(data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "0"));	
					Button btnDetail = new Button("Detail");
					btnDetail.setAutodisable("self");
					btnDetail.setClass("btn btn-default btn-sm");
					btnDetail.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnDetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						
						@Override
						public void onEvent(Event event) throws Exception {
							String page = "";
							Map<String, Object> map = new HashMap<>();
							
							if (data.getTperso().getTderivatifproduct() == null) {
								map.put("tembossbranch", data.getTembossbranch());
								page = "/view/emboss/embossdata.zul";
							} else {
								map.put("obj", data.getTperso().getTderivatifproduct());
								page = "/view/derivatif/derivatifdata.zul";
							}
							Window win = (Window) Executions
									.createComponents(page,
											null, map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();															
						}
					});
					row.getChildren().add(btnDetail);
				}
			});
		}				
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpersodata.orderdate, mbranch.branchid";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpersodataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}
	
	@Command
	public void doPrint() {
		try {
			List<Tpersodata> objList = new TpersodataDAO().listByFilter("tperso.tpersopk = " + obj.getTpersopk(), "tpersodatapk");
			Map<String, String> parameters = new HashMap<>();
			parameters.put("PERSOID", obj.getPersoid());
			parameters.put("PERSODATE", datetimelocalFormatter.format(obj.getPersostarttime()));
			//parameters.put("PROCESSTYPE", obj.getProcesstype().equals(AppUtils.PROCESSTYPE_URGENT) ? "URGENT" : "REGULAR");
			zkSession.setAttribute("objList", objList);
			zkSession.setAttribute("parameters", parameters);	
			zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp().getRealPath(SysUtils.JASPER_PATH + "/persomanifest.jasper"));
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

			Window win = (Window) Executions
					.createComponents(
							path,
							null, map);
			win.setWidth("90%");
			win.setClosable(true);
			win.doModal();	
			win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getData() != null) {
						Map<String, Object> map = (Map<String, Object>) event.getData();
						obj = (Tperso) map.get("obj");
						isSaved = (Boolean) map.get("isSaved");
						BindUtils.postNotifyChange(null, null, PersoDataVm.this, "obj");
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
		filter = "tpersofk = " + obj.getTpersopk();
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
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event( "onClose", winPersodata, isSaved);
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
	
	public Tperso getObj() {
		return obj;
	}

	public void setObj(Tperso obj) {
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

}
