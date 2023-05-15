package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
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

import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Torder;

public class OrderBranchVm {

	private TembossbranchDAO oDao = new TembossbranchDAO();
		
	private Torder obj;
	private Tembossproduct tembossproduct;
	private Integer totaldata;
	private String filter;
	private String branchname;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	
	@Wire
	private Window winOrderbranch;
	@Wire
	private Groupbox gbHeader;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder torder, 
			@ExecutionArgParam("tembossproduct") final Tembossproduct tembossproduct)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		obj = torder;
		this.tembossproduct = tembossproduct;
		doSearch();
		grid.setRowRenderer(new RowRenderer<Tembossbranch>() {

			@Override
			public void render(Row row, final Tembossbranch data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf(index + 1)));			
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));	
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
							map.put("tembossbranch", data);

							Window win = (Window) Executions
									.createComponents(
											"/view/order/orderdata.zul",
											null, map);
							win.setWidth("95%");
							win.setClosable(true);
							win.doModal();								
					}
				});
				row.getChildren().add(btndetail);
				totaldata += data.getTotaldata() != null ? data.getTotaldata() : 0;
				BindUtils.postNotifyChange(null, null, OrderBranchVm.this, "totaldata");
			}
		});			
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			totaldata = 0;
			grid.setModel(new ListModelList<>(oDao.listByFilter(filter, "tembossbranchpk")));
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Command
	public void doSearch() {
		filter = "torderfk = " + obj.getTorderpk() + " and tembossproductfk = " + tembossproduct.getTembossproductpk();
		if (branchname != null && branchname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mbranch.branchname like '" + branchname.trim().toUpperCase() + "%'";
		}				
		refreshModel();
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event( "onClose", winOrderbranch, null);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		branchname = null;
		doSearch();
	}

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}
	

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Tembossproduct getTembossproduct() {
		return tembossproduct;
	}

	public void setTembossproduct(Tembossproduct tembossproduct) {
		this.tembossproduct = tembossproduct;
	}

}
