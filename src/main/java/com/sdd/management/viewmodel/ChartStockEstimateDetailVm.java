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
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Vinventory;
import com.sdd.caption.model.MproducttypeListModel;
import com.sdd.caption.utils.ProductgroupBean;
import com.sdd.utils.SysUtils;

public class ChartStockEstimateDetailVm {

	private MproducttypeListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;

	private Mproducttype objForm;
	private ProductgroupBean productgroup;
	private String productgroupcode;	
	private String productgroupname;
	private String producttype;
	private String filterpredict;
	private Vinventory obj;
	private String title;
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MMM-yy");
	
	@Wire
	private Button btnSave;
	@Wire
	private Button btnCancel;
	@Wire
	private Button btnDelete;
	@Wire
	private Combobox cbProductgroup;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Combobox cbProductunit;
	@Wire
	private Intbox ibProductunitqty;
	@Wire
	private Div divForm;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Vinventory obj, 
			@ExecutionArgParam("filterpredict") String filterpredict, @ExecutionArgParam("title") String title) {
		Selectors.wireComponents(view, this, false);
		this.obj = obj;
		this.filterpredict = filterpredict;		
		this.title = title;
		
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
		
		grid.setRowRenderer(new RowRenderer<Mproducttype>() {

			@Override
			public void render(Row row, final Mproducttype data, int index)throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber)+ index + 1)));
				row.getChildren().add(new Label(data.getProducttype()));
				row.getChildren().add(new Label(data.getLaststock() != null ? NumberFormat.getInstance().format(data.getLaststock()) : "0"));
				row.getChildren().add(new Label(data.getEstdays() != null ? data.getEstdays() + " Hari" : "0 Hari"));
				row.getChildren().add(new Label(data.getEstdate() != null ? datelocalFormatter.format(data.getEstdate()) : ""));
			}

		});
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "producttype";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MproducttypeListModel(activePage, SysUtils.PAGESIZE, filter,
				orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (obj.getOrg().equals("200")) {
			filter = "productorg like '2%' and isestcount = 'Y' and " + filterpredict;
		} else {
			filter = "productorg = '" + obj.getOrg() + "' and isestcount = 'Y' and " + filterpredict;
		}
		
		
		
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@NotifyChange("*")
	public void doReset() {
		doSearch();
	}

	public Mproducttype getObjForm() {
		return objForm;
	}

	public void setObjForm(Mproducttype objForm) {
		this.objForm = objForm;
	}


	public String getProductgroupcode() {
		return productgroupcode;
	}

	public void setProductgroupcode(String productgroupcode) {
		this.productgroupcode = productgroupcode;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public ProductgroupBean getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(ProductgroupBean productgroup) {
		this.productgroup = productgroup;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Vinventory getObj() {
		return obj;
	}

	public void setObj(Vinventory obj) {
		this.obj = obj;
	}

}