package com.sdd.caption.viewmodel;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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

import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Tsecuritiesitem;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.model.ToutgoingListModel;
import com.sdd.caption.pojo.InquiryDetailBean;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;

public class OutgoingDataVm {
	
	private Session zkSession = Sessions.getCurrent();

	private ToutgoingListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	
	private Toutgoing obj;

	private Boolean isSaved;
	private String productgroup;
	private String arg;
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd MMMMM yyyy HH:mm");

	@Wire
	private Window winOutgoingData;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Label lbTitle;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Toutgoing toutgoing)
			throws ParseException {
		
		Selectors.wireComponents(view, this, false);
		obj = toutgoing;
		
//		paging.addEventListener("onPaging", new EventListener<Event>() {
//			@Override
//			public void onEvent(Event event) throws Exception {
//				PagingEvent pe = (PagingEvent) event;
//				pageStartNumber = pe.getActivePage();
//				refreshModel(pageStartNumber);
//			}
//		});
		
//		if (grid != null) {
//			grid.setRowRenderer(new RowRenderer<Tsecuritiesitem>() {
//
//				@Override
//				public void render(Row row, Tsecuritiesitem data, int index) throws Exception {
//					row.getChildren().add(new Label(
//							String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
//									+ index + 1)));
//					row.getChildren().add(new Label(data.getItemno() != null ? data.getItemno() : "-"));
//					row.getChildren().add(new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
//				}
//			});
//		}
		
		doReset();
	}
	
	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		
		filter = "toutgoingfk = " + obj.getToutgoingpk();
//			needsPageUpdate = true;
//			paging.setActivePage(0);
//			pageStartNumber = 0;
//			refreshModel(pageStartNumber);
	}
	
	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "toutgoingfk";
//		paging.setPageSize(SysUtils.PAGESIZE);
//		model = new ToutgoingListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
//		if (needsPageUpdate) {
//			pageTotalSize = model.getTotalSize(filter);
//			needsPageUpdate = false;
//		}
//		paging.setTotalSize(pageTotalSize);
//		grid.setModel(model);
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winOutgoingData, isSaved);
		Events.postEvent(closeEvent);
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException { 
		productgroup = AppData.getProductgroupLabel(obj.getProductgroup());
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

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public Toutgoing getObj() {
		return obj;
	}

	public void setObj(Toutgoing obj) {
		this.obj = obj;
	}

	public Boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public Paging getPaging() {
		return paging;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
	}


}
