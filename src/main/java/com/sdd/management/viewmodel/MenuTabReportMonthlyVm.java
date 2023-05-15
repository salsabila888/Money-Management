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

public class MenuTabReportMonthlyVm {

	@Wire
    private Div tabPageReportmonthly;
	
	@AfterCompose
    public void init(@ContextParam(ContextType.VIEW) final Component view) {
        Selectors.wireComponents(view, this, false);
        doTab("R");
    }
	
	@Command
    @NotifyChange({ "title" })
    public void doTab(@BindingParam("tab") final String tab) {
		tabPageReportmonthly.getChildren().clear();
		Map<String, Object> map = new HashMap<String, Object>();
		
		 if (tab.equals("R")) {
	            Executions.createComponents("/view/report/reportmonthlyregular.zul", tabPageReportmonthly, map);
	        }
	        else if (tab.equals("D")) {
	            Executions.createComponents("/view/report/reportmonthlyderivatif.zul", tabPageReportmonthly, map);
	        }
	}
}
