package com.sdd.caption.viewmodel;

import java.text.NumberFormat;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.sdd.caption.dao.DashboardDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Vinventory;
import com.sdd.caption.utils.ProductgroupBean;

public class ChartStockvsPaguDetailVm {


	private int pageTotalSize;
	private boolean needsPageUpdate;

	private Mproducttype objForm;
	private ProductgroupBean productgroup;
	private String productgroupcode;	
	private String productgroupname;
	private String producttype;
	private String filterpredict;
	private Vinventory obj;
	private String title;
	
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

		needsPageUpdate = true;
		doReset();
		
		grid.setRowRenderer(new RowRenderer<Mproducttype>() {

			@Override
			public void render(Row row, final Mproducttype data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getProducttype()));
				row.getChildren().add(new Label(data.getLaststock() != null ? NumberFormat.getInstance().format(data.getLaststock()) : "0"));
				row.getChildren().add(new Label(data.getStockmin() != null ? NumberFormat.getInstance().format(data.getStockmin()) : "0"));
			}

		});
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel() {	
		try {
			grid.setModel(new ListModelList<>(new DashboardDAO().listProducttypeestimate(obj.getOrg(), filterpredict)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		refreshModel();
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