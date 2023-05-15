package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.model.TtokenitemListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;

public class TokenInquiryVm {
	private int pageStartNumber, pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby, filter, serialnoaw, serialnoak, status;
	private Date orderdate;

	private TtokenitemListModel model;
//	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);

		paging.addEventListener("onPaging", new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Ttokenitem>() {
				@Override
				public void render(Row row, final Ttokenitem data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren().add(new Label(data.getItemno()));
					row.getChildren().add(new Label(
							data.getTincoming() == null ? "-" : data.getTincoming().getMbranch().getBranchname()));
					row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getTincoming().getEntrytime())));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
				}
			});
		}
		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "";
		if ((serialnoak != null && serialnoak.length() > 0) && (serialnoaw != null && serialnoaw.length() > 0)) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "itemno between '" + serialnoaw + "' and '" + serialnoak + "'";
		}
//		if (orderdate != null) {
//			if (filter.length() > 0)
//				filter += " and ";
//			filter += "date(tincoming.entrytime) = '" + dateFormatter.format(orderdate) + "'";
//		}
//		if (status != null && status.length() > 0) {
//			if (filter.length() > 0)
//				filter += " and ";
//			filter += "ttokenserial.status = '" + status + "'";
//		}

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		serialnoaw = null;
		serialnoak = null;
		orderdate = new Date();
		status = "";
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "ttokenitempk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TtokenitemListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public String getSerialnoaw() {
		return serialnoaw;
	}

	public void setSerialnoaw(String serialnoaw) {
		this.serialnoaw = serialnoaw;
	}

	public String getSerialnoak() {
		return serialnoak;
	}

	public void setSerialnoak(String serialnoak) {
		this.serialnoak = serialnoak;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
