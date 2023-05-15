package com.sdd.caption.viewmodel;

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

public class MenuTabDeliveryRegulerVm {
	
	@Wire
	private Div tabPage;
	
	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {		
		Selectors.wireComponents(view, this, false);
		doTab("orderpaket");
	}
	
	@Command
	@NotifyChange("title")
    public void doTab(@BindingParam("tab") String tab) {
    	tabPage.getChildren().clear();
    	if (tab.equals("orderpaket")) { 
    		Executions.createComponents("/view/delivery/paketproduct.zul", tabPage,null);
    	} else if (tab.equals("paketlist")) {
    		Executions.createComponents("/view/delivery/paketlist.zul", tabPage,null);
    	} else if (tab.equals("orderdelivery")) {
    		Executions.createComponents("/view/delivery/deliveryjob.zul", tabPage,null);
    	} else if (tab.equals("deliverylist")) {
    		Executions.createComponents("/view/delivery/deliverylist.zul", tabPage,null);
    	}
    }

}
