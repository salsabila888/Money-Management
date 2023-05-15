package com.sdd.caption.viewmodel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.model.TorderMemoListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;

public class OrderMemoVm {
	
	private Session zkSession = Sessions.getCurrent();

	private TorderMemoListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	
	private Torder obj;
	private String productname;
	private String orderid;
	private Date orderdate;
	private Integer orderlevel;
	private String orderoutlet;
	private Integer itemqty;

	private Boolean isSaved;
	private String productgroup;
	private String arg;
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd MMMMM yyyy HH:mm");

	@Wire
	private Window winOrderMemo;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Label lbTitle;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder torder)
			throws ParseException {
		
		Selectors.wireComponents(view, this, false);
		obj = torder;
		
		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});
		
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tordermemo>() {

				@Override
				public void render(Row row, Tordermemo data, int index) throws Exception {
					row.getChildren().add(new Label(
							String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
									+ index + 1)));
					row.getChildren().add(new Label(data.getMemo()));
					row.getChildren().add(new Label(data.getMemoby()));
					row.getChildren().add(new Label(datelocalFormatter.format(data.getMemotime())));
				}
			});
		}
		
		doReset();
	}
	
	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		
		filter = "torderfk = " + obj.getTorderpk();
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
	}
	
	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tordermemopk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TorderMemoListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winOrderMemo, isSaved);
		Events.postEvent(closeEvent);
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException { 
		productgroup = AppData.getProductgroupLabel(obj.getProductgroup());
		doSearch();
	}
	
	public TorderMemoListModel getModel() {
		return model;
	}
	public void setModel(TorderMemoListModel model) {
		this.model = model;
	}
	public int getPageStartNumber() {
		return pageStartNumber;
	}
	public void setPageStartNumber(int pageStartNumber) {
		this.pageStartNumber = pageStartNumber;
	}
	public int getPageTotalSize() {
		return pageTotalSize;
	}
	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}
	public boolean isNeedsPageUpdate() {
		return needsPageUpdate;
	}
	public void setNeedsPageUpdate(boolean needsPageUpdate) {
		this.needsPageUpdate = needsPageUpdate;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public String getOrderby() {
		return orderby;
	}
	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}
	public Paging getPaging() {
		return paging;
	}
	public void setPaging(Paging paging) {
		this.paging = paging;
	}
	public Grid getGrid() {
		return grid;
	}
	public void setGrid(Grid grid) {
		this.grid = grid;
	}
	public Label getLbTitle() {
		return lbTitle;
	}
	public void setLbTitle(Label lbTitle) {
		this.lbTitle = lbTitle;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public Integer getOrderlevel() {
		return orderlevel;
	}

	public void setOrderlevel(Integer orderlevel) {
		this.orderlevel = orderlevel;
	}

	public String getOrderoutlet() {
		return orderoutlet;
	}

	public void setOrderoutlet(String orderoutlet) {
		this.orderoutlet = orderoutlet;
	}

	public Integer getItemqty() {
		return itemqty;
	}

	public void setItemqty(Integer itemqty) {
		this.itemqty = itemqty;
	}
	
	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}


}
