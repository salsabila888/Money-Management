package com.sdd.management.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Separator;

import com.sdd.management.dao.MproductgroupDAO;
import com.sdd.management.domain.Mproductgroup;
import com.sdd.management.domain.Muser;


public class GoalVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private String filter;

	private Div divContent;

	@Wire
	private Div divGoal, divProduct;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("content") Div divContent) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.divContent = divContent;
		divGoal.setVisible(true);
		doRender();
	}

	@NotifyChange("*")
	public void doRender() {
		try {
			filter = "0=0";

			String filterProduct = "";
			
			filterProduct = "productgroupcode in ('01', '03', '04', '02')";
				
			List<Mproductgroup> objList = new MproductgroupDAO().listByFilter(filterProduct, "productgroupcode");
			for (Mproductgroup obj : objList) {

				Div divRow = new Div();
				divRow.setClass("col-md-3");

				Div divCard = new Div();
				divCard.setClass("card");

				Div divCardHeader = new Div();
				divCardHeader.setClass("card-header");

				Image image = new Image();
				image.setWidth("100%");
				image.setHeight("100%");
				if (obj.getProductgroupcode().equals("01")) {
					image.setSrc("/files/img/home.png");
					divCard.setClass("card text-white bg-primary mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getProductgroupcode().equals("02")) {
					image.setSrc("/files/img/motorcycle.png");
					divCard.setClass("card text-white bg-success mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getProductgroupcode().equals("03")) {
					image.setSrc("/files/img/laptop.png");
					divCard.setClass("card text-white bg-danger mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getProductgroupcode().equals("04")) {
					image.setSrc("/files/img/online-course.png");
					divCard.setClass("card text-white bg-secondary mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} 
				Label lbl = new Label();
				lbl.setValue(" " + obj.getProductgroup());
				lbl.setStyle("font-size: 14px; font-weight: bold");

				divCardHeader.appendChild(image);

				Separator s = new Separator();
				Div div = new Div();
				div.setStyle("text-align:center");
				div.appendChild(s);
				div.appendChild(lbl);
				divCardHeader.appendChild(div);

				Div divBody = new Div();
				divBody.setClass("card-body");
				divBody.setStyle("text-align:right");
				lbl = new Label("Jumlah Data Retur : ");
				lbl.setStyle("font-size: 14px");
				divBody.appendChild(lbl);
				lbl = new Label();
				lbl.setValue(String.valueOf(0));
				lbl.setStyle("font-size: 14px");
				divBody.appendChild(lbl);

				Div divLabel = new Div();
				divLabel.setClass("card-footer");
				Button btn = new Button();
				btn.setSclass("btn btn-light btn-sm");
				btn.setStyle(
						"border-radius: 8px; background-color: #845EC2 !important; color: #ffffff !important; float:right !important;");
				btn.setLabel("Tampilkan Data");
				btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						divContent.getChildren().clear();
//						Map<String, Object> map = new HashMap<>();
//						map.put("arg", obj.getProductgroupcode());
//						map.put("content", divContent);
//						if (arg.equals("entry")) {
//							if (obj.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_TOKEN))
//								Executions.createComponents("/view/return/returentrytoken.zul", divContent, map);
//							else if (obj.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
//								System.out.println("ENTRY SURAT BERHARGA");
//								Executions.createComponents("/view/return/returentrydocument.zul", divContent, map);
//							}
//						} else if (arg.equals("list")) {
//							Executions.createComponents("/view/return/returproductlist.zul", divContent, map);
//						} else if (arg.equals("approval") || arg.equals("approvalwil") || arg.equals("approvalpfa")
//								|| arg.equals("approvalopr")) {
//							if (arg.equals("approval")) {
//								map.put("stats", AppUtils.STATUS_RETUR_WAITAPPROVAL);
//							} else if (arg.equals("approvalwil")) {
//								map.put("stats", AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH);
//							} else if (arg.equals("approvalpfa")) {
//								map.put("stats", AppUtils.STATUS_RETUR_WAITAPPROVALPFA);
//							} else if (arg.equals("approvalopr")) {
//								map.put("stats", AppUtils.STATUS_RETUR_WAITAPPROVALOPR);
//							}
//							System.out.println(obj.getProductgroupcode());
//							System.out.println(AppUtils.PRODUCTGROUP_DOCUMENT);
//							if (obj.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
//								Executions.createComponents("/view/return/returapprovaldocument.zul", divContent, map);
//							} else {
//								Executions.createComponents("/view/return/returproductapproval.zul", divContent, map);
//							}
//						} else if (arg.equals("destroy")) {
//							Executions.createComponents("/view/return/destroylist.zul", divContent, map);
//						} else if (arg.equals("returpaket")) {
//							Executions.createComponents("/view/return/returpaketlist.zul", divContent, map);
//						} else if (arg.equals("entrydes")) {
//							map.put("isDestroy", "Y");
//							Executions.createComponents("/view/return/destroyentry.zul", divContent, map);
//						}
					}
				});
				divLabel.appendChild(btn);

				Separator separator = new Separator();
				Separator separator2 = new Separator();

				divCard.appendChild(divCardHeader);
				divCard.appendChild(divBody);
				divCard.appendChild(divLabel);

				divRow.appendChild(divCard);
				divRow.appendChild(separator);
				divRow.appendChild(separator2);
				divProduct.appendChild(divRow);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
