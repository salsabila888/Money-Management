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
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.dao.TorderdataDAO;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Torder;

public class OrderProductVm {

	private TembossproductDAO oDao = new TembossproductDAO();
	private TorderdataDAO torderdataDao = new TorderdataDAO();
			
	private Torder obj;
	private String filter;
	private String producttype;
	private String productcode;
	private String productname;
	private Integer totaldata;
	private String productgroup;
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Window winOrderproduct;
	@Wire
	private Groupbox gbHeader;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Groupbox gbUnmapped;
	@Wire
	private Grid gridUnmapped;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder torder)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		obj = torder;		
		doReset();

		grid.setRowRenderer(new RowRenderer<Tembossproduct>() {

			@Override
			public void render(Row row, final Tembossproduct data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getMproduct().getMproducttype().getProducttype()));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));	
				row.getChildren().add(new Label(data.getOrderdate() != null ? datelocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				Button btndetail = new Button("Detail");
				btndetail.setAutodisable("self");
				btndetail.setClass("btn btn-default btn-sm");
				btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", obj);
							map.put("tembossproduct", data);

							Window win = (Window) Executions
									.createComponents(
											"/view/order/orderbranch.zul",
											null, map);
							win.setWidth("60%");
							win.setClosable(true);
							win.doModal();							
					}
				});
				row.getChildren().add(btndetail);				
			}
		});	
		
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			totaldata = 0;
			grid.setModel(new ListModelList<>(oDao.listByFilterType(filter, "tembossproduct")));
			totaldata = obj.getItemqty();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Command
	public void doSearch() {
		filter = "torderfk = " + obj.getTorderpk();
		if (producttype != null && producttype.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
		}
		if (productcode != null && productcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
		}
		if (productname != null && productname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
		}
		refreshModel();
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event( "onClose", winOrderproduct, null);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totaldata = 0;
		producttype = null;
		productcode = null;
		productname = null;
		doSearch();
	}

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
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

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}		

}
