package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Trepair;
import com.sdd.caption.domain.Trepairdlv;
import com.sdd.caption.model.TrepairdlvListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;

public class RepairHistoryVm {

	private TrepairdlvListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;

	private Trepair obj;

	@Wire
	private Window winReturnItem;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Trepair obj)
			throws ParseException {

		Selectors.wireComponents(view, this, false);
		this.obj = obj;

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Trepairdlv>() {

				@Override
				public void render(Row row, Trepairdlv data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					A a = new A(data.getDlvno());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("objDlv", data);
							win = (Window) Executions.createComponents("/view/repair/repairitem.zul", null, map);
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getQuantity())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalfail())));
					row.getChildren()
							.add(new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
					row.getChildren().add(new Label(data.getProcessby()));
					row.getChildren().add(new Label(new SimpleDateFormat("dd-MM-yyyy").format(data.getProcesstime())));
				}
			});
		}
		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "trepairfk = " + obj.getTrepairpk();

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "trepairdlvpk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TrepairdlvListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
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

	public Paging getPaging() {
		return paging;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	public Trepair getObj() {
		return obj;
	}

	public void setObj(Trepair obj) {
		this.obj = obj;
	}

}
