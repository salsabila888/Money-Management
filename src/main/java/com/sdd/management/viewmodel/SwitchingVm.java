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
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Separator;

import com.sdd.caption.dao.MproductgroupDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vsumbyproductgroup;
import com.sdd.caption.utils.AppUtils;

public class SwitchingVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private String arg;
	private String filter;
	private int branchlevel;

	private Div divContent;

	@Wire
	private Div divListreq, divListpool, divApprovalreq, divApprovalpool;
	@Wire
	private Div divProduct;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("content") Div divContent) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		branchlevel = oUser.getMbranch().getBranchlevel();
		this.arg = arg;
		this.divContent = divContent;
		
		if (arg.equals("listreq")) {
			divListreq.setVisible(true);
		} else if (arg.equals("listpool")) {
			divListpool.setVisible(true);
		} else if (arg.equals("approvereq")) {
			divApprovalreq.setVisible(true);
		} else if (arg.equals("approvepool")) {
			divApprovalpool.setVisible(true);
		}
		
		doRender();
	}

	@NotifyChange("*")
	public void doRender() {
		try {
			if (arg.equals("approvereq")) {
				filter = "status = '" + AppUtils.STATUS_SWITCH_WAITAPPROVAL + "' and branchidreq = '"
						+ oUser.getMbranch().getBranchid() + "'";
			} else if (arg.equals("approvepool")) {
				filter = "status = '" + AppUtils.STATUS_SWITCH_WAITAPPROVALPOOL + "' and branchidpool = '"
						+ oUser.getMbranch().getBranchid() + "'";
			} else if (arg.equals("listreq")) {
				if (branchlevel == 2)
					filter = "mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
				else if (branchlevel == 3)
					filter = "mbranchfk = " + oUser.getMbranch().getMbranchpk();
			} else if (arg.equals("listpool")) {
				filter = "branchidpool = '" + oUser.getMbranch().getBranchid() + "'";
			}

			List<Mproductgroup> objList = new MproductgroupDAO().listByFilter("productgroupcode in ('01', '04')",
					"mproductgrouppk");
			for (Mproductgroup obj : objList) {
				List<Vsumbyproductgroup> productList = new TswitchDAO()
						.getSumdataByProductgroup(filter + " and productgroup = '" + obj.getProductgroupcode() + "'");

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
					image.setSrc("/files/img/cardkarturegular.png");
					divCard.setClass("card text-white bg-primary mb-3");
					divCard.setStyle(
							"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
				} else if (obj.getProductgroupcode().equals("04")) {
					image.setSrc("/files/img/cardsurat.png");
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
				lbl = new Label("Jumlah Usulan : ");
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
				btn.setSclass("btn btn-light btn-sm");
				btn.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important; float: right !important;");

				btn.setLabel("Tampilkan Data");
				btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						divContent.getChildren().clear();
						Map<String, Object> map = new HashMap<>();
						map.put("productgroup", obj.getProductgroupcode());
						map.put("content", divContent);
						if (arg.equals("approvereq")) {
							map.put("arg", "req");
							Executions.createComponents("/view/switching/switchingapproval.zul", divContent, map);
						} else if (arg.equals("approvepool")) {
							map.put("arg", "pool");
							Executions.createComponents("/view/switching/switchingapproval.zul", divContent, map);
						} else if (arg.equals("listreq")) {
							map.put("arg", "req");
							Executions.createComponents("/view/switching/switchinglist.zul", divContent, map);
						} else if (arg.equals("listpool")) {
							map.put("arg", "pool");
							Executions.createComponents("/view/switching/switchinglist.zul", divContent, map);
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
