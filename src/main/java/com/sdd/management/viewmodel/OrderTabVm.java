package com.sdd.caption.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;

public class OrderTabVm {
	
	private String arg;
	
	@Wire
	private Div tabPage;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg) {
		Selectors.wireComponents(view, this, false);
		this.arg = arg;
		doTab("1");
	}
	
	@Command
	@NotifyChange("title")
    public void doTab(@BindingParam("tab") String tab) {
		String page = "";
    	tabPage.getChildren().clear();
		Map<String, Object> map = new HashMap<>();	
		map.put("isAdmin", new Boolean(true));
    	if (tab.equals("1")) {
    		if (arg.equals("persoorder"))
    			page = "/view/perso/persoproduct.zul";
    		else if (arg.equals("persoapproval")) {
    			map.put("arg", "P");
    			page = "/view/perso/persoapproval.zul";
    		} else if (arg.equals("invoutapproval")) {
    			map.put("arg", "01");
    			page = "/view/inventory/outgoingapproval.zul";
    		} else if (arg.equals("persolist"))
    			page = "/view/perso/persolist.zul";
    		else if (arg.equals("paketorder"))
    			page = "/view/delivery/paketproduct.zul";
    		else if (arg.equals("paketlist"))
    			page = "/view/delivery/paketlist.zul";
    		else if (arg.equals("dlvorder"))
    			page = "/view/delivery/deliveryjob.zul";
    		else if (arg.equals("dlvlist"))
    			page = "/view/delivery/deliverylist.zul";
    	} else if (tab.equals("2")) {
    		map.put("arg", arg);    		
    		page = "/view/derivatif/derivatiflist.zul";
    	} 
    	Executions.createComponents(page, tabPage, map);
    }
	
}
