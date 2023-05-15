package com.sdd.caption.viewmodel;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;


public class AlertUneodVm {
	
	private Integer countalert;
	
	@Wire
	private Window winClosingeod;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("countalert") Integer countalert) {
		Selectors.wireComponents(view, this, false);
		this.countalert = countalert;
	}
	
	@Command
	public void doView() {
		Div divContent = (Div) winClosingeod.getParent();
		divContent.getChildren().clear();
		Executions.createComponents("/view/inventory/closing.zul", divContent, null);
	}

	public Integer getCountalert() {
		return countalert;
	}

	public void setCountalert(Integer countalert) {
		this.countalert = countalert;
	}
	
}
