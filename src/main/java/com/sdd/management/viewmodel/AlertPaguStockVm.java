package com.sdd.caption.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Window;

public class AlertPaguStockVm {
	
	private Integer countalert;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("countalert") Integer countalert) {
		Selectors.wireComponents(view, this, false);
		this.countalert = countalert;
	}
	
	@Command
	public void doView() {
		Map<String, Object> map = new HashMap<>();
		map.put("filteralert", "laststock < stockmin"); 
		Window win = (Window) Executions
				.createComponents(
						"/view/parameter/producttypenoncard.zul",
						null, map);
		win.setWidth("90%");
		win.setClosable(true);
		win.doModal();	
	}

	public Integer getCountalert() {
		return countalert;
	}

	public void setCountalert(Integer countalert) {
		this.countalert = countalert;
	}
	
}
