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

import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class IncomingEntryResumeVM {

	private Tincoming obj;

	private String arg, memo;
	private String productgroupname;

	@Wire
	private Window winIncomingResume;
	@Wire 
	private Button btnCreate, btnCancel;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("content") Div divContent, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("obj") Tincoming obj, @ExecutionArgParam("isEdit") String isEdit) {
		Selectors.wireComponents(view, this, false);

		this.obj = obj;
		this.arg = arg;

		if (obj != null)
			memo = obj.getMemo();
		else
			memo = "-";

		productgroupname = AppData.getProductgroupLabel(obj.getProductgroup());
		
		if (isEdit != null) {
			btnCreate.setVisible(false);
			btnCancel.setVisible(false);
		} 
	}

	@Command
	public void doCreate() {
		Div divRoot = (Div) winIncomingResume.getParent();
		divRoot.getChildren().clear();
		Map<String, String> map = new HashMap<String, String>();
		map.put("arg", arg);
		if(arg.equals(AppUtils.PRODUCTGROUP_PINPAD))
			Executions.createComponents("/view/inventory/incomingentrypinpad.zul", divRoot, map);
		else
			Executions.createComponents("/view/inventory/incomingentry.zul", divRoot, map);
	}

	@Command
	public void doList() {
		Div divRoot = (Div) winIncomingResume.getParent();
		divRoot.getChildren().clear();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("arg", arg);
		map.put("content", divRoot);
		map.put("list", "1");
		Executions.createComponents("/view/inventory/incominglist.zul", divRoot, map);
	}

	public Tincoming getObj() {
		return obj;
	}

	public void setObj(Tincoming obj) {
		this.obj = obj;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
}
