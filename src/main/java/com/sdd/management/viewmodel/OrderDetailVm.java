package com.sdd.caption.viewmodel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
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
import org.zkoss.zul.A;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TorderflowDAO;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Torderdoc;
import com.sdd.caption.domain.Torderflow;
import com.sdd.caption.model.TorderdocListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class OrderDetailVm {

	private TorderdocListModel modelDocument;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private Integer totaldata;

	private Torder obj;
	private Torderdoc objDoc;
	private String branchid;
	private String branchname;
	private String producttype;
	private String productcode;
	private String productname;
	private String productgroup;
	private Boolean isSaved;
	private String decisiontime;
	
	List<Torderflow> flowList = new ArrayList<Torderflow>();

	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd MMMMM yyyy HH:mm");

	@Wire
	private Window winOrderDetail;
	@Wire
	private Grid grid, gridDocument;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder torder,
			@ExecutionArgParam("isDetail") final String isDetail) throws ParseException {
		Selectors.wireComponents(view, this, false);
		obj = torder;

		if (obj != null) {
			if (obj.getDecisionby() == null)
				obj.setDecisionby("-");

			if (obj.getDecisiontime() == null)
				decisiontime = "-";
			else
				decisiontime = datetimelocalFormatter.format(obj.getDecisiontime());
		}

		doReset();
		
		if (gridDocument != null) {
			gridDocument.setRowRenderer(new RowRenderer<Torderdoc>() {
				@Override
				public void render(Row row, Torderdoc data, int index) throws Exception {
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
			grid.setRowRenderer(new RowRenderer<Torderflow>() {
				@Override
				public void render(Row row, Torderflow data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getFlowname()));
					row.getChildren().add(new Label(data.getFlowuser()));
					row.getChildren().add(new Label(datetimelocalFormatter.format(data.getFlowtime())));
				}
			});
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "torderdocpk";
		modelDocument = new TorderdocListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = modelDocument.getTotalSize(filter);
			needsPageUpdate = false;
		}
		gridDocument.setModel(modelDocument);
	}


	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "torderfk = " + obj.getTorderpk();
		needsPageUpdate = true;
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}
	
	public void refreshHistory() {
		try {
			flowList = new TorderflowDAO().listByFilter("torderfk = " + obj.getTorderpk(), "flowtime");
			totaldata = flowList.size();
			grid.setModel(new ListModelList<>(flowList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winOrderDetail, isSaved);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException {
		productgroup = AppData.getProductgroupLabel(obj.getProductgroup());
		branchname = null;
		producttype = null;
		productcode = null;
		productname = null;
		totaldata = 0;
		doSearch();
		refreshHistory();
	}

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
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

	public Torderdoc getObjDoc() {
		return objDoc;
	}

	public void setObjDoc(Torderdoc objDoc) {
		this.objDoc = objDoc;
	}

	public String getDecisiontime() {
		return decisiontime;
	}

	public void setDecisiontime(String decisiontime) {
		this.decisiontime = decisiontime;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}


}
