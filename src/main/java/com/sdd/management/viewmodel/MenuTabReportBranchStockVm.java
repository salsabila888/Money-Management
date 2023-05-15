package com.sdd.caption.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;

public class MenuTabReportBranchStockVm {

	@Wire
	private Div tabPageBranchStock;
	
	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {		
		Selectors.wireComponents(view, this, false);
		doTab("branch");
	}
	
	@Command
	@NotifyChange("title")
    public void doTab(@BindingParam("tab") String tab) {
		tabPageBranchStock.getChildren().clear();
    	Map<String, Object> map = new HashMap<>();
    	if (tab.equals("branch")) { 
    		Executions.createComponents("/view/report/reportbranchstock.zul", tabPageBranchStock,map);
    	} else if (tab.equals("product")) {
    		Executions.createComponents("/view/report/reportbranchstockbyproduct.zul", tabPageBranchStock,map);
    	}
	}
}
