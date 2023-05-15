package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
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
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.domain.Tbookdata;
import com.sdd.caption.domain.Tbookfile;
import com.sdd.caption.model.TbookdataListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;

public class BookdataVm {
	
	private Session zkSession = Sessions.getCurrent();

	private TbookdataListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	
	private Tbookfile obj;
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
	private Window winBookData;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Label lbTitle;
	@Wire
	private Div divRecord;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tbookfile tbookfile)
			throws Exception {
		
		Selectors.wireComponents(view, this, false);
		obj = tbookfile;
		
		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});
		
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tbookdata>() {

				@Override
				public void render(Row row, Tbookdata data, int index) throws Exception {
					row.getChildren().add(new Label(
							String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
									+ index + 1)));
					A a = new A(data.getSeqnum());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<String, Object>();
							Window win = new Window();
							map.put("arg", arg);
							if (data.getTdeliverydata().getTpaketdata().getTpaket().getTreturn() != null) {
								map.put("obj", data.getTdeliverydata().getTpaketdata().getTpaket().getTreturn());
								win = (Window) Executions.createComponents("/view/return/returproductitem.zul", null,
										map);
							} else {
								map.put("obj", data.getTdeliverydata().getTpaketdata().getTpaket().getTorder());
								map.put("itemprice", data.getItemprice());
								win = (Window) Executions.createComponents("/view/order/orderitem.zul", null, map);
							}
							win.setWidth("60%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					
					String startno = new TorderitemDAO().listNativeByFilter("torderfk = " + data.getTdeliverydata().getTpaketdata().getTpaket().getTorder().getTorderpk(), "torderitempk asc").get(0).getItemno();
					String endno = new TorderitemDAO().listNativeByFilter("torderfk = " + data.getTdeliverydata().getTpaketdata().getTpaket().getTorder().getTorderpk(), "torderitempk desc").get(0).getItemno();
					
					row.getChildren().add(new Label(startno != null && endno != null ? startno + " - " + endno : "-"));
					row.getChildren().add(new Label(data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "-"));
					row.getChildren().add(new Label(data.getTotalamount() != null ? "Rp " + NumberFormat.getInstance().format(data.getTotalamount()) : "-"));
					row.getChildren().add(new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus())  : "-"));
					row.getChildren().add(new Label(data.getErrcode() != null ? data.getErrcode() : "-"));
					row.getChildren().add(new Label(data.getErrdesc() != null ? data.getErrdesc() : "-"));
				}
			});
		}
		
		doReset();
	}
	
	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "tbookfilefk = " + obj.getTbookfilepk();
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
	}
	
	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tbookdatapk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TbookdataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winBookData, isSaved);
		Events.postEvent(closeEvent);
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException { 
		doSearch();
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

	public Tbookfile getObj() {
		return obj;
	}

	public void setObj(Tbookfile obj) {
		this.obj = obj;
	}	

}
