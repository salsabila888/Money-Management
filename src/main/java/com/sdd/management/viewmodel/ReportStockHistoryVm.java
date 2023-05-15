package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
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
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Vproducttypestock;
import com.sdd.caption.domain.Vstockproducthistory;
import com.sdd.caption.model.VstockproducthistoryListModel;
import com.sdd.utils.SysUtils;

public class ReportStockHistoryVm {

	private VstockproducthistoryListModel model;
	private MproducttypeDAO oDao = new MproducttypeDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private String productgroup;

	private Integer total;
	private String datereport;
	private Vproducttypestock obj;
	private ListModelList<Mproducttype> mproducttypemodel;
	
	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Window winReportstockhistory;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Vproducttypestock obj) {
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

		needsPageUpdate = true;
		doReset();
		
		grid.setRowRenderer(new RowRenderer<Vstockproducthistory>() {

			@Override
			public void render(Row row, Vstockproducthistory data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				row.getChildren().add(new Label(data.getProducttype()));
				row.getChildren().add(new Label(data.getTrxtype()));
				row.getChildren().add(new Label(data.getTrxtime() != null ? datetimelocalFormatter.format(data.getTrxtime()) : ""));
				row.getChildren().add(new Label(data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : "0"));
				row.getChildren().add(new Label(data.getMemo()));				
			}
		});		
	}
	
	@NotifyChange({"pageTotalSize", "total"})
	public void refreshModel(int activePage) {
		try {
			orderby = "trxtime";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new VstockproducthistoryListModel(activePage, SysUtils.PAGESIZE, filter,
					orderby);
			if (needsPageUpdate) {
				pageTotalSize = model.getTotalSize(filter);
				needsPageUpdate = false;
			}
			paging.setTotalSize(pageTotalSize);
			grid.setModel(model);
			total = oDao.sumStockHistory(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Command
	@NotifyChange({"pageTotalSize", "total"})
	public void doSearch() {
		filter = "mproducttypepk = " + obj.getMproducttypepk();		
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event( "onClose", winReportstockhistory, null);
		Events.postEvent(closeEvent);
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		doSearch();
	}	
	
	public ListModelList<Mproducttype> getMproducttypemodel() {
		return mproducttypemodel;
	}

	public void setMproducttypemodel(ListModelList<Mproducttype> mproducttypemodel) {
		this.mproducttypemodel = mproducttypemodel;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getDatereport() {
		return datereport;
	}

	public void setDatereport(String datereport) {
		this.datereport = datereport;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}	

}
