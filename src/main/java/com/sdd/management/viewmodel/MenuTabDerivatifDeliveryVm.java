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

public class MenuTabDerivatifDeliveryVm {
	
	@Wire
	private Div tabPageDlv;
	
	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {		
		Selectors.wireComponents(view, this, false);
		doTab("order");
	}
	
	@Command
	@NotifyChange("title")
    public void doTab(@BindingParam("tab") String tab) {
		tabPageDlv.getChildren().clear();
    	Map<String, Object> map = new HashMap<>();
    	if (tab.equals("order")) { 
    		Executions.createComponents("/view/derivatif/derivatifdeliveryjob.zul", tabPageDlv,map);
    	} else if (tab.equals("list")) {
    		Executions.createComponents("/view/derivatif/derivatifdeliverylist.zul", tabPageDlv,map);
    	}
	}

}
