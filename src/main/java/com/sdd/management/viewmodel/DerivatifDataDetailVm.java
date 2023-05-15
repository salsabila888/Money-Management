package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Tderivatifdata;
import com.sdd.caption.domain.Tderivatifproduct;
import com.sdd.caption.model.TderivatifdataListModel;
import com.sdd.utils.SysUtils;

public class DerivatifDataDetailVm {

	private TderivatifdataListModel model;
	private Tderivatifproduct obj;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;

	private SimpleDateFormat periodLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	private String orderby;
	private String filter;
	private String cardno;

	@Wire
	private Grid grid;
	@Wire
	private Paging paging;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Tderivatifproduct obj) {
		Selectors.wireComponents(view, this, false);

		this.obj = obj;

		doReset();
		paging.addEventListener("onPaging", new EventListener() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tderivatifdata>() {

			@Override
			public void render(Row row, Tderivatifdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				row.getChildren().add(new Label(data.getCardno()));
				row.getChildren().add(new Label(data.getTembossdata().getNameoncard()));
				row.getChildren().add(new Label(data.getTembossdata().getProductcode()));
				row.getChildren().add(new Label(data.getTembossdata().getMproduct().getProductname()));
				row.getChildren().add(new Label(periodLocalFormatter.format(new SimpleDateFormat("yyyy-MM-dd")
						.parse(String.valueOf(data.getTembossdata().getOrderdate())))));
				row.getChildren().add(new Label(data.getTembossdata().getMbranch().getBranchcode()));
				row.getChildren().add(new Label(data.getTembossdata().getBranchname()));

			}
		});

	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "tderivatifproductfk = " + obj.getTderivatifproductpk();
		if (cardno != null && cardno.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += " cardno like '%" + cardno.trim().toUpperCase() + "%'";
		}

		needsPageUpdate = true;
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	public void refreshModel(int activePage) {
		orderby = "tderivatifdatapk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TderivatifdataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	private void doReset() {
		doSearch();
	}

	public Tderivatifproduct getObj() {
		return obj;
	}

	public void setObj(Tderivatifproduct obj) {
		this.obj = obj;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

}
