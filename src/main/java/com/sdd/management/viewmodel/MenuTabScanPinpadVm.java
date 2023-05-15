package com.sdd.caption.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
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
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Torder;

public class MenuTabScanPinpadVm {
	private Torder obj;

	@Wire
	private Div tabPagePinpad;
	@Wire
	private Window winTabPinpad;
	
	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder obj, @ExecutionArgParam("isClose") String isClose) {		
		Selectors.wireComponents(view, this, false);
		this.obj = obj;
		doTab("manual");
	}
	
	@Command
	@NotifyChange("title")
    public void doTab(@BindingParam("tab") String tab) {
		tabPagePinpad.getChildren().clear();
    	Map<String, Object> map = new HashMap<>();
    	map.put("winid", winTabPinpad);
    	map.put("obj", obj);
    	if (tab.equals("manual")) { 
    		Executions.createComponents("/view/pinpad/pinpadscanentry.zul", tabPagePinpad,map);
    	} else if (tab.equals("upload")) {
    		Executions.createComponents("/view/pinpad/pinpadscanupload.zul", tabPagePinpad,map);
    	}
	}
}
