package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
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
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MproductreqDAO;
import com.sdd.caption.domain.Mproductreq;

public class MproductreqVm {

	private MproductreqDAO oDao = new MproductreqDAO();
	
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	private int totalrecord;
	private String status;
	private String productcode;
	private String filter;
	private List<Mproductreq> objList;	
	
	@Wire
	private Grid grid;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		
		doReset();
		grid.setRowRenderer(new RowRenderer<Mproductreq>() {

			@Override
			public void render(Row row, Mproductreq data, int index) throws Exception {
				row.getChildren().add(new Label(
						String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getProductname()));
				row.getChildren().add(new Label(data.getIsinstant().equals("Y") ? "YA" : "TIDAK"));
				row.getChildren().add(new Label(data.getIsderivatif().equals("Y") ? "YA" : "TIDAK"));
				row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getEntrytime())));
				
				Label label = new Label("NEW");
				label.setStyle("color: red");
				if(data.getStatus().equals("E"))
					row.getChildren().add(label);
				else row.getChildren().add(new Label("VERIFIED"));
				
				Button btnVerify = new Button("Verify");
				btnVerify.setAutodisable("self");
				btnVerify.setClass("btn btn-success btn-sm");
				btnVerify.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);
						Window win = (Window) Executions.createComponents("/view/parameter/productreqverify.zul", null, map);
						win.setWidth("40%");
						win.setClosable(true);
						win.doModal();
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								doReset();
								BindUtils.postNotifyChange(null, null, MproductreqVm.this, "totalrecord");
							}
						});
					}
				});
				if(data.getStatus().equals("E"))
					row.getChildren().add(btnVerify);
				else row.getChildren().add(new Label());
			}
		});

	}
	
	@Command
	@NotifyChange("*")
	public void doSearch() {
		filter = "status = '" + status + "'";
		if(productcode != null && productcode.length() > 0)
			filter += " and productcode like '%" + productcode.trim().toUpperCase() + "%'"; 
		
		doRefresh();
	}
	
	@NotifyChange("*")
	public void doRefresh() {
		try {
			objList = oDao.listByFilter(filter, "entrytime");
			grid.setModel(new ListModelList<>(objList));
			totalrecord = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		status = "E";
		productcode = "";
		doSearch();
	}
	

	public int getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(int totalrecord) {
		this.totalrecord = totalrecord;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}
}
