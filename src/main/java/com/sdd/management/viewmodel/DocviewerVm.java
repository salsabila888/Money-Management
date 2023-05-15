package com.sdd.caption.viewmodel;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Iframe;

public class DocviewerVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private String reportPath;

	@Wire
	private Iframe iframe;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		this.reportPath = (String) zkSession.getAttribute("reportPath");
		
//		this.reportPath = "../../ims-new-caption-ui/src/main/webapp/";
		try {
			System.out.println(reportPath);
			iframe.setSrc(reportPath);

			if (zkSession.getAttribute("reportPath") != null)
				zkSession.removeAttribute("reportPath");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
