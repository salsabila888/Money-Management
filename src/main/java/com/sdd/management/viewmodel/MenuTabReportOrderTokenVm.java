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

import com.sdd.caption.utils.AppUtils;

public class MenuTabReportOrderTokenVm {

	@Wire
	private Div tabPageReportorder;
	
	@AfterCompose
	public void init(@ContextParam(ContextType.VIEW) Component view) {		
		Selectors.wireComponents(view, this, false);
		doTab("produksi");
	}
	
	@Command
	@NotifyChange("title")
    public void doTab(@BindingParam("tab") String tab) {
		tabPageReportorder.getChildren().clear();
    	Map<String, Object> map = new HashMap<>();
    	if (tab.equals("produksi")) { 
    		map.put("arg", "02");
    		map.put("type", AppUtils.ENTRYTYPE_MANUAL);
    		Executions.createComponents("/view/report/reportordermanual.zul", tabPageReportorder,map);
    	} else if (tab.equals("cabang")) {
    		map.put("arg", "02");
    		map.put("type", AppUtils.ENTRYTYPE_MANUAL_BRANCH);
    		Executions.createComponents("/view/report/reportordermanual.zul", tabPageReportorder,map);
    	}
	}
}
