package com.sdd.caption.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Trepair;
import com.sdd.caption.utils.AppData;

public class RepairEntryResume {
	
	private Trepair obj;
	
	private String arg;
	private String productgroupname;
	
	@Wire
	private Window winRepairResume;

	@AfterCompose
		public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("content") Div divContent,
			@ExecutionArgParam("obj") Trepair obj) {
		Selectors.wireComponents(view, this, false);
		
		this.obj = obj;
		this.arg = arg;
		
		productgroupname = AppData.getProductgroupLabel(obj.getMproduct().getProductgroup());
	}

	@Command
		public void doCreate() {
		Div divRoot = (Div) winRepairResume.getParent();
		divRoot.getChildren().clear();
		Executions.createComponents("/view/repair/repairentry.zul", divRoot, null);
	}
	
	@Command
		public void doList() {
		Div divRoot = (Div) winRepairResume.getParent();
		divRoot.getChildren().clear();
		Executions.createComponents("/view/repair/repairlist.zul", divRoot, null);
	}

	public Trepair getObj() {
		return obj;
	}

	public void setObj(Trepair obj) {
		this.obj = obj;
	}
	
	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}
	

}
