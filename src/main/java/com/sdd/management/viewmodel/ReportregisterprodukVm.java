package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

import com.sdd.caption.dao.MbranchproductgroupDAO;
import com.sdd.caption.dao.MproductgroupDAO;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;

public class ReportregisterprodukVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private String filter;
	private Div divContent;

	private SimpleDateFormat datenormalFormatter = new SimpleDateFormat("YYYY-MM-dd");

	@Wire
	private Div divProduct;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("content") Div divContent) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.divContent = divContent;
		doRender();
	}

	@NotifyChange("*")
	public void doRender() {
		try {
			filter = "0=0";
			if (oUser.getMbranch().getBranchlevel() == 2)
				filter += " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
			else if (oUser.getMbranch().getBranchlevel() == 3)
				filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();

			String month = new SimpleDateFormat("MM").format(new Date());
			String year = new SimpleDateFormat("yyyy").format(new Date());
			Integer fday = 1;
			String dd = "";
			String md = "";

			if (fday.toString().length() < 2)
				dd = "0" + fday.toString();

			if (month.toString().length() < 2)
				md = "0" + month.toString();
			else
				md = month.toString();


			Calendar cal = Calendar.getInstance();
			cal.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(dd + "/" + md + "/" + year));
			cal.add(Calendar.MONTH, 1);
			cal.add(Calendar.DATE, -1);
			Date edt = cal.getTime();
//			String sldate = year + "-" + md + "-" + dd;
//			String eldate = datenormalFormatter.format(edt);

			if (oUser.getMbranch().getBranchlevel() > 1) {
				List<Mproductgroup> objList = new MproductgroupDAO().listByFilter("0=0", "productgroupcode");
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
						image.setSrc("/files/img/cardkarturegular.png");
						divCard.setClass("card text-white bg-primary mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("02")) {
						image.setSrc("/files/img/cardtoken.png");
						divCard.setClass("card text-white bg-success mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("03")) {
						image.setSrc("/files/img/cardpinpad.png");
						divCard.setClass("card text-white bg-danger mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("04")) {
						image.setSrc("/files/img/cardsurat.png");
						divCard.setClass("card text-white bg-secondary mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("06")) {
						image.setSrc("/files/img/cardpinmailer.png");
						divCard.setClass("card text-white bg-info mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("07")) {
						image.setSrc("/files/img/cardsurat.png");
						divCard.setClass("card text-white bg-light mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("08")) {
						image.setSrc("/files/img/cardsurat.png");
						divCard.setClass("card text-white bg-light mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("09")) {
						image.setSrc("/files/img/cardderivatif.png");
						divCard.setClass("card text-white bg-light mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("10")) {
						image.setSrc("/files/img/cardsurat.png");
						divCard.setClass("card text-white bg-light mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("11")) {
						image.setSrc("/files/img/cardsurat.png");
						divCard.setClass("card text-white bg-light mb-3");
						divCard.setStyle(
								"border-radius: 8px; background-color: #f1f2f2 !important; color: #000000 !important;");
					} else if (obj.getProductgroupcode().equals("12")) {
						image.setSrc("/files/img/cardsurat.png");
						divCard.setClass("card text-white bg-light mb-3");
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

					Div divLabel = new Div();
					divLabel.setClass("card-footer");
					Button btn = new Button();
					btn.setSclass("btn btn-light btn-sm");
					btn.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important; float:right");
					btn.setLabel("Tampilkan Data");

					btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							divContent.getChildren().clear();
							Map<String, Object> map = new HashMap<>();
							map.put("argid", obj.getProductgroupcode());
							map.put("content", divContent);
							Executions.createComponents("/view/report/Reportregisterstock.zul", divContent, map);
						}
					});
					divLabel.appendChild(btn);

					Separator separator = new Separator();
					Separator separator2 = new Separator();

					divCard.appendChild(divCardHeader);
					divCard.appendChild(divLabel);

					divRow.appendChild(divCard);
					divRow.appendChild(separator);
					divRow.appendChild(separator2);
					divProduct.appendChild(divRow);
				}
			} else {
				List<Mbranchproductgroup> objList = new MbranchproductgroupDAO()
						.listByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk(), "mproductgroupfk");
				for (Mbranchproductgroup obj : objList) {
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

					Div divLabel = new Div();
					divLabel.setClass("card-footer");
					Button btn = new Button();
					btn.setSclass("btn btn-light btn-sm");
					btn.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important; float:right");
					btn.setLabel("Tampilkan Data");

					btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							divContent.getChildren().clear();
							Map<String, Object> map = new HashMap<>();
							map.put("argid", obj.getMproductgroup().getProductgroupcode());
							map.put("content", divContent);
							Executions.createComponents("/view/report/Reportregisterstock.zul", divContent, map);
						}
					});
					divLabel.appendChild(btn);

					Separator separator = new Separator();
					Separator separator2 = new Separator();

					divCard.appendChild(divCardHeader);
					divCard.appendChild(divLabel);

					divRow.appendChild(divCard);
					divRow.appendChild(separator);
					divRow.appendChild(separator2);
					divProduct.appendChild(divRow);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}