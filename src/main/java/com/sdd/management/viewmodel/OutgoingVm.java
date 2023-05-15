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

import com.sdd.caption.dao.MbranchproductgroupDAO;
import com.sdd.caption.dao.MproductgroupDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vsumbyproductgroup;
import com.sdd.caption.utils.AppUtils;

public class OutgoingVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private String arg;
	private String filter;

	private Div divContent;

	@Wire
	private Div divList, divApproval, divEntry;
	@Wire
	private Div divProduct;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("content") Div divContent) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.arg = arg;
		this.divContent = divContent;
		if (arg.equals("list")) {
			divList.setVisible(true);
		} else if (arg.equals("approval")) {
			divApproval.setVisible(true);
		} else if (arg.equals("scan")) {
			divEntry.setVisible(true);
		}
		doRender();
	}

	@NotifyChange("*")
	public void doRender() {
		try {
			if (oUser.getMbranch().getBranchlevel() > 1) {
				List<Mproductgroup> objList = new MproductgroupDAO()
						.listByFilter("productgroupcode not in ('01', '09')", "productgroupcode");
				for (Mproductgroup obj : objList) {
					if (arg.equals("approval")) {
						if (oUser.getMbranch().getBranchlevel() == 2)
							filter = "orderlevel = " + (oUser.getMbranch().getBranchlevel() + 1) + " and mregionfk = "
									+ oUser.getMbranch().getMregion().getMregionpk() + " and toutgoing.status = '"
									+ AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL + "' and orderoutlet = '00'";
						else if (oUser.getMbranch().getBranchlevel() == 3)
							filter = "orderoutlet != '00' and mbranchpk = " + oUser.getMbranch().getMbranchpk()
									+ " and toutgoing.status = '" + AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL
									+ "'";
					} else {
						if (oUser.getMbranch().getBranchlevel() == 2) {
							filter = "orderoutlet = '00' and orderlevel = " + (oUser.getMbranch().getBranchlevel() + 1)
									+ " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
						} else if (oUser.getMbranch().getBranchlevel() == 3) {
							filter = "orderoutlet != '00' and mbranchpk = " + oUser.getMbranch().getMbranchpk();
						}

						if (arg.equals("scan"))
							filter += " and toutgoing.status = '" + AppUtils.STATUS_INVENTORY_OUTGOINGAPPROVED + "'";
					}

					List<Vsumbyproductgroup> productList = new ToutgoingDAO().getSumdataByProductgroup(
							filter + " and mproduct.productgroup = '" + obj.getProductgroupcode() + "'");

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

					Div divBody = new Div();
					divBody.setClass("card-body");
					divBody.setStyle("text-align:right");
					lbl = new Label("Jumlah : ");
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
					if (arg.equals("entry")) {
						btn.setLabel("Buat Data Persediaan Baru");
					} else {
						btn.setLabel("Tampilkan Data");
					}
					btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							divContent.getChildren().clear();
							Map<String, Object> map = new HashMap<>();
							map.put("arg", obj.getProductgroupcode());
							map.put("content", divContent);
							if (arg.equals("entry")) {
								map.put("arg", obj.getProductgroupcode());
								Executions.createComponents("/view/inventory/incomingentry.zul", divContent, map);
							} else if (arg.equals("list")) {
								map.put("arg", obj.getProductgroupcode());
								Executions.createComponents("/view/inventory/outgoinglist.zul", divContent, map);
							} else if (arg.equals("approval")) {
								if (obj.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_CARD)
										|| obj.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_CARDPHOTO)) {
									map.put("arg", "invoutapproval");
									Executions.createComponents("/view/order/ordertab.zul", divContent, map);
								} else {
									map.put("arg", obj.getProductgroupcode());
									Executions.createComponents("/view/inventory/outgoingapproval.zul", divContent,
											map);
								}
							} else if (arg.equals("scan")) {
								map.put("arg", obj.getProductgroupcode());
								Executions.createComponents("/view/inventory/outgoingscanentry.zul", divContent, map);
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
			} else {
				String filterproduct = "mbranchfk = " + oUser.getMbranch().getMbranchpk();
				if (!oUser.getMbranch().getBranchid().equals("760") || arg.equals("scan"))
					filterproduct += " and mproductgroup.productgroupcode not in ('01','09', '06')";

				List<Mbranchproductgroup> objList = new MbranchproductgroupDAO().listNativeByFilter(filterproduct,
						"mproductgroupfk");
				for (Mbranchproductgroup obj : objList) {
					if (arg.equals("scan")) {
						filter = "toutgoing.status = '" + AppUtils.STATUS_INVENTORY_OUTGOINGAPPROVED + "'";
						if (obj.getMproductgroup().getProductgroupcode().equals(AppUtils.PRODUCTGROUP_TOKEN)
								|| obj.getMproductgroup().getProductgroupcode().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
							filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();
						}
					} else if (arg.equals("approval")) {
						if (obj.getMproductgroup().getProductgroupcode().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
							filter = "orderlevel = " + (oUser.getMbranch().getBranchlevel() + 1)
									+ " and toutgoing.status = '" + AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL
									+ "'";
						} else if (obj.getMproductgroup().getProductgroupcode().equals(AppUtils.PRODUCTGROUP_CARD)) {
							filter = "toutgoing.status = '" + AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL
									+ "' and tderivatifproductfk is null";
						} else if (obj.getMproductgroup().getProductgroupcode()
								.equals(AppUtils.PRODUCTGROUP_CARDPHOTO)) {
							filter = "toutgoing.status = '" + AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL
									+ "'  and tderivatifproductfk is not null";
						} else {
							filter = "orderlevel = 1 and toutgoing.status = '"
									+ AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL + "'";
						}
					} else {
						if (obj.getMproductgroup().getProductgroupcode().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
							filter = "orderlevel = " + (oUser.getMbranch().getBranchlevel() + 1);
						} else if (obj.getMproductgroup().getProductgroupcode().equals(AppUtils.PRODUCTGROUP_CARD)
								|| obj.getMproductgroup().getProductgroupcode().equals(AppUtils.PRODUCTGROUP_CARDPHOTO)
								|| obj.getMproductgroup().getProductgroupcode()
										.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
							filter = "0=0";
						} else {
							filter = "orderlevel = 1 and mbranchpk = " + oUser.getMbranch().getMbranchpk();
						}
					}

					List<Vsumbyproductgroup> productList = new ToutgoingDAO().getSumdataByProductgroup(filter
							+ " and toutgoing.productgroup = '" + obj.getMproductgroup().getProductgroupcode() + "'");

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
					lbl = new Label("Jumlah : ");
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
							if (arg.equals("entry")) {
								Executions.createComponents("/view/inventory/incomingentry.zul", divContent, map);
							} else if (arg.equals("list")) {
								Executions.createComponents("/view/inventory/outgoinglist.zul", divContent, map);
							} else if (arg.equals("approval")) {
								if (obj.getMproductgroup().getProductgroupcode()
										.equals(AppUtils.PRODUCTGROUP_CARDPHOTO)) {
									map.put("arg", "invoutapproval");
									Executions.createComponents("/view/derivatif/derivatiflist.zul", divContent, map);
								} else {
									Executions.createComponents("/view/inventory/outgoingapproval.zul", divContent,
											map);
								}
							} else if (arg.equals("scan")) {
								map.put("arg", obj.getMproductgroup().getProductgroupcode());
								Executions.createComponents("/view/inventory/outgoingscanentry.zul", divContent, map);
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
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
