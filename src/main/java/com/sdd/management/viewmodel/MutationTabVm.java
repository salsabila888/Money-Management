package com.sdd.management.viewmodel;

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

public class MutationTabVm {
	
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
    		map.put("type", "I");
    		page = "/view/moneymng/mutationform.zul";
    	} else if (tab.equals("2")) {
    		map.put("type", "E");		
    		page = "/view/moneymng/mutationform.zul";
    	} 
    	Executions.createComponents(page, tabPage, map);
    }
	
}
