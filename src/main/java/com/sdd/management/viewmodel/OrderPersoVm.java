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

public class OrderPersoVm {
	
	private String title;
	
	@Wire
	private Div tabPage;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
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
    		page = "/view/perso/persoproduct.zul";
    		title = "Order Perso Kartu Regular (Non Foto)";
    	} else if (tab.equals("2")) {
    		map.put("arg", "OPR");
    		page = "/view/derivatif/derivatiflist.zul";
    		title = "Order Perso Kartu Berfoto";
    	} 
    	Executions.createComponents(page, tabPage, map);
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}
