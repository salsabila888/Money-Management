package com.sdd.caption.viewmodel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Tdelivery;


public class JasperViewerVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	
	private String reportPath;
	@SuppressWarnings("rawtypes")
	private Map parameters = new HashMap();
	private List<Tdelivery> objList = new ArrayList<Tdelivery>();
	
	@Wire
	private Div div;
	@Wire
	private Window window;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		
		try {			
			this.objList = (List<Tdelivery>) zkSession.getAttribute("objList");
			this.parameters = (Map) zkSession.getAttribute("parameters");
			this.reportPath = (String) zkSession.getAttribute("reportPath");
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			
			System.out.println(reportPath);
			JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, parameters, new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(
					(java.util.Collection) objList));
			
			JasperExportManager.exportReportToPdfStream(jasperPrint, output);	
			
			AMedia amedia = new AMedia("report", "pdf", "application/pdf", output.toByteArray());
			Iframe iframe = new Iframe();
			iframe.setHeight("100%");
			iframe.setWidth("100%");			
			iframe.setContent(amedia);
			div.appendChild(iframe);
			
	        if (zkSession.getAttribute("objList") != null)
	        	zkSession.removeAttribute("objList");
	        if (zkSession.getAttribute("parameters") != null)
	        	zkSession.removeAttribute("parameters");
	        if (zkSession.getAttribute("reportPath") != null)
	        	zkSession.removeAttribute("reportPath");	       
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}
	
}