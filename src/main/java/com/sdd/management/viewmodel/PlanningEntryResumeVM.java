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
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Tplan;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class PlanningEntryResumeVM {
	
	private Tplan obj;
	
	private String arg;
	private String productgroupname;
	
	@Wire
	private Window winPlanResume;
	@Wire
	private Button btnCreate, btnCancel;

	@AfterCompose
		public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("content") Div divContent, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("obj") Tplan obj, @ExecutionArgParam("isEdit") String isEdit) {
		Selectors.wireComponents(view, this, false);
		
		this.obj = obj;
		this.arg = arg;
		
		productgroupname = AppData.getProductgroupLabel(obj.getProductgroup());
		
		if (isEdit != null) {
			btnCreate.setVisible(false);
			btnCancel.setVisible(false);
		} 
	}

	@Command
		public void doCreate() {
		Div divRoot = (Div) winPlanResume.getParent();
		divRoot.getChildren().clear();
		Map<String, String> map = new HashMap<String, String>();
		map.put("arg", arg);
		if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
			Executions.createComponents("/view/planning/planningentrypinpad.zul", divRoot, map);
		} else 
			Executions.createComponents("/view/planning/planningentry.zul", divRoot, map);
	}
	
	@Command
		public void doList() {
		Div divRoot = (Div) winPlanResume.getParent();
		divRoot.getChildren().clear();
		Map<String, String> map = new HashMap<String, String>();
		map.put("arg", arg);
		Executions.createComponents("/view/planning/planninglist.zul", divRoot, map);
	}

	public Tplan getObj() {
		return obj;
	}

	public void setObj(Tplan obj) {
		this.obj = obj;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}


}
