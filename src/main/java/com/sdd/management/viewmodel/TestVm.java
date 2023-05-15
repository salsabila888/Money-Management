package com.sdd.caption.viewmodel;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;

import net.sourceforge.barbecue.Main;

public class TestVm {
	private String value;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);

	}

	@NotifyChange("*")
	@Command
	public void doCount() {
		Thread thread = new Thread();
		thread.start();
		for (int i = 0; i <= 200; i++) {
			if (i % 10 == 1) {
				value = String.valueOf(i);
				thread.interrupt();
				BindUtils.postNotifyChange(TestVm.this, "*");
			}

		}
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
