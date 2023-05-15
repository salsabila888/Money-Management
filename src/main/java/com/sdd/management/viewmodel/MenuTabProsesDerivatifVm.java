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

public class MenuTabProsesDerivatifVm {
	
	@Wire
	private Div tabPage;
	
	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {		
		Selectors.wireComponents(view, this, false);
		doTab("scan");
	}
	
	@Command
	@NotifyChange("title")
    public void doTab(@BindingParam("tab") String tab) {
    	tabPage.getChildren().clear();
    	Map<String, Object> map = new HashMap<>();
    	if (tab.equals("scan")) { 
    		map.put("arg", "S");
    		Executions.createComponents("/view/derivatif/derivatifupdatestatus.zul", tabPage,map);
    	} else if (tab.equals("crop")) {
    		map.put("arg", "C");
    		Executions.createComponents("/view/derivatif/derivatifupdatestatus.zul", tabPage,map);
    	} else if (tab.equals("merge")) {
    		map.put("arg", "M");
    		Executions.createComponents("/view/derivatif/derivatifupdatestatus.zul", tabPage,map);
    	} else if (tab.equals("perso")) {
    		Executions.createComponents("/view/menutab/menutabderivatifperso.zul", tabPage,null);
    	} else if (tab.equals("paket")) {
    		Executions.createComponents("/view/menutab/menutabderivatifpaket.zul", tabPage,null);
    	} else if (tab.equals("delivery")) {
    		Executions.createComponents("/view/menutab/menutabderivatifdelivery.zul", tabPage,null);
    	}
    }

}
