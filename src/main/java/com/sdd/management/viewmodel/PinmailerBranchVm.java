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

import com.sdd.caption.dao.TpinmailerbranchDAO;
import com.sdd.caption.domain.Tpinmailerbranch;
import com.sdd.caption.domain.Tpinmailerfile;

public class PinmailerBranchVm {

	private TpinmailerbranchDAO oDao = new TpinmailerbranchDAO();
		
	private Tpinmailerfile obj;
	private Integer totaldata;
	private String filter;
	private String branchname;
	
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
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tpinmailerfile obj)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		this.obj = obj;
		doSearch();
		grid.setRowRenderer(new RowRenderer<Tpinmailerbranch>() {

			@Override
			public void render(Row row, final Tpinmailerbranch data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf(index + 1)));			
				row.getChildren().add(new Label(data.getBranchid()));
				row.getChildren().add(new Label(data.getMbranch() != null ? data.getMbranch().getBranchname() : ""));	
				row.getChildren().add(new Label(data.getOrderdate() != null ? datelocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				Button btndetail = new Button("Data");
				btndetail.setAutodisable("self");
				btndetail.setClass("btn btn-default btn-sm");
				btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);

							Window win = (Window) Executions
									.createComponents(
											"/view/pinmailer/pinmailerdata.zul",
											null, map);
							win.setWidth("95%");
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
			grid.setModel(new ListModelList<>(oDao.listByFilter(filter, "branchid")));
			totaldata = 0;
			totaldata = oDao.sum(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Command
	@NotifyChange("totaldata")
	public void doSearch() {
		filter = "tpinmailerfilefk = " + obj.getTpinmailerfilepk();
		if (branchname != null && branchname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mbranch.branchname like '%" + branchname.trim().toUpperCase() + "%'";
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

	public Tpinmailerfile getObj() {
		return obj;
	}

	public void setObj(Tpinmailerfile obj) {
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

}
