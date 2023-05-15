package com.sdd.caption.viewmodel;

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
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Separator;

import com.sdd.caption.dao.MbranchproductgroupDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vsumbyproductgroup;
import com.sdd.caption.utils.AppUtils;

public class OrderApprovalDivVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private String filter;
	private String arg;

	private Div divContent;

	@Wire
	private Div divProduct, divOpr, divPfa;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg, @ExecutionArgParam("content") Div divContent) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.divContent = divContent;
		this.arg = arg;
		if(arg.equals("opr"))
			divOpr.setVisible(true);
		else if (arg.equals("jal"))
			divPfa.setVisible(true);
		doRender();
	}

	@NotifyChange("*")
	public void doRender() {
		try {
			if(arg.equals("opr"))
				filter = "status = '" + AppUtils.STATUS_ORDER_WAITAPPROVALOPR + "'";
			else if (arg.equals("jal"))
				filter = "status = '" + AppUtils.STATUS_ORDER_WAITAPPROVALJAL + "'";

			List<Mbranchproductgroup> objList = new MbranchproductgroupDAO()
					.listByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk(), "mproductgroupfk");
			for (Mbranchproductgroup obj : objList) {
				List<Vsumbyproductgroup> productList = new TorderDAO().getSumdataByProductgroup(
						filter + " and productgroup = '" + obj.getMproductgroup().getProductgroupcode() + "'");

				Div divRow = new Div();
				divRow.setClass("col-md-3");

				Div divCard = new Div();
				divCard.setClass("card");

				Div divCardHeader = new Div();
				divCardHeader.setClass("card-header");

				Image image = new Image();
				image.setWidth("100%");
				image.setHeight("100%");
				if (obj.getMproductgroup().getProductgroupcode().equals("01")) {
					image.setSrc("/files/img/cardkarturegular.png");
					divCard.setClass("card text-white bg-primary mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("02")) {
					image.setSrc("/files/img/cardtoken.png");
					divCard.setClass("card text-white bg-success mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("03")) {
					image.setSrc("/files/img/cardpinpad.png");
					divCard.setClass("card text-white bg-danger mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("04")) {
					image.setSrc("/files/img/cardsurat.png");
					divCard.setClass("card text-white bg-secondary mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("06")) {
					image.setSrc("/files/img/cardpinmailer.png");
					divCard.setClass("card text-white bg-info mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("07")) {
					image.setSrc("/files/img/cardsurat.png");
					divCard.setClass("card text-white bg-light mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("08")) {
					image.setSrc("/files/img/cardsurat.png");
					divCard.setClass("card text-white bg-light mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("09")) {
					image.setSrc("/files/img/cardderivatif.png");
					divCard.setClass("card text-white bg-light mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("10")) {
					image.setSrc("/files/img/cardsurat.png");
					divCard.setClass("card text-white bg-light mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("11")) {
					image.setSrc("/files/img/cardsurat.png");
					divCard.setClass("card text-white bg-light mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getMproductgroup().getProductgroupcode().equals("12")) {
					image.setSrc("/files/img/cardsurat.png");
					divCard.setClass("card text-white bg-light mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				}

				Label lbl = new Label();
				lbl.setValue(" " + obj.getMproductgroup().getProductgroup());
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
				lbl = new Label("Jumlah Order : ");
				lbl.setStyle("font-size: 14px");
				divBody.appendChild(lbl);
				lbl = new Label();
				if (productList.size() > 0)
					lbl.setValue(String.valueOf(productList.get(0).getTotal()));
				else
					lbl.setValue(String.valueOf(0));
				lbl.setStyle("font-size: 14px");
				divBody.appendChild(lbl);
				
				Div divLabel = new Div();
				divLabel.setClass("card-footer");
				Button btn = new Button();
				btn.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important; float: right !important;");
				btn.setSclass("btn btn-light btn-sm");
				btn.setLabel("Tampilkan Data");
				btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						divContent.getChildren().clear();
						Map<String, Object> map = new HashMap<>();
						map.put("arg", obj.getMproductgroup().getProductgroupcode());
						map.put("content", divContent);
						if(arg.equals("opr")) {
							map.put("isOPR", "Y");
							if(obj.getMproductgroup().getProductgroupcode().equals("03"))
								Executions.createComponents("/view/order/orderapprovalpinpad.zul", divContent, map);
							else
								Executions.createComponents("/view/order/orderapproval.zul", divContent, map);
						}else if (arg.equals("jal")) {
							map.put("isJAL", "Y");
							Executions.createComponents("/view/order/orderapproval.zul", divContent, map);
						}
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
