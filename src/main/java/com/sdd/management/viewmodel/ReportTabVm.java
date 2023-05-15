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

public class ReportTabVm {
	private Div divContent;

	@Wire
	private Div tabPage;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("content") Div divContent) {
		Selectors.wireComponents(view, this, false);
		this.divContent = divContent;
		doTab("1");
	}

	@Command
	@NotifyChange("title")
	public void doTab(@BindingParam("tab") String tab) {
		String page = "";
		tabPage.getChildren().clear();
		Map<String, Object> mapu = new HashMap<>();
//		map.put("isAdmin", new Boolean(true));
		if (tab.equals("1")) {
			mapu.put("content", divContent);
			mapu.put("menumodel", "report");
			page = "/view/report/reportplan.zul";

//			if (arg.equals("persoorder"))
//				page = "/view/perso/persoproduct.zul";
//			else if (arg.equals("persoapproval")) {
//				map.put("arg", "P");
//				page = "/view/perso/persoapproval.zul";
//			} else if (arg.equals("invoutapproval")) {
//				map.put("arg", "01");
//				page = "/view/inventory/outgoingapproval.zul";
//			} else if (arg.equals("persolist"))
//				page = "/view/perso/persolist.zul";
//			else if (arg.equals("paketorder"))
//				page = "/view/delivery/paketproduct.zul";
//			else if (arg.equals("paketlist"))
//				page = "/view/delivery/paketlist.zul";
//			else if (arg.equals("dlvorder"))
//				page = "/view/delivery/deliveryjob.zul";
//			else if (arg.equals("dlvlist"))
//				page = "/view/delivery/deliverylist.zul";
		} else if (tab.equals("2")) {
			page = "/view/derivatif/derivatiflist.zul";
		} else if (tab.equals("3")) {
			page = "/view/derivatif/derivatiflist.zul";
		} else if (tab.equals("4")) {
			page = "/view/derivatif/derivatiflist.zul";
		} else if (tab.equals("5")) {
			page = "/view/derivatif/derivatiflist.zul";
		} else if (tab.equals("6")) {
			page = "/view/derivatif/derivatiflist.zul";
		} else if (tab.equals("7")) {
			page = "/view/derivatif/derivatiflist.zul";
		}
		Executions.createComponents(page, tabPage, mapu);
	}
}
