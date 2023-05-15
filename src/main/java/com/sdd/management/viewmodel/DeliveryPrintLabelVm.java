package com.sdd.caption.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Window;

public class DeliveryPrintLabelVm {
	
	private String isauto;
	
	@Wire
	private Window winPrintlabel;
	@Wire
	private Radio rdAuto;
	@Wire
	private Radio rdGenerate;

	@AfterCompose
	@NotifyChange("*")	
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		isauto = "Y";
	}
	
	
	@Command
	public void doSave() {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("isauto", isauto);
			Event closeEvent = new Event( "onClose", winPrintlabel, map);
			Events.postEvent(closeEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getisauto() {
		return isauto;
	}

	public void setisauto(String isauto) {
		this.isauto = isauto;
	}
}
