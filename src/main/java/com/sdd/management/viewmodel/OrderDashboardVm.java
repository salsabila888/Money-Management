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
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vsumbyproductgroup;
import com.sdd.caption.utils.AppUtils;

public class OrderDashboardVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private String arg;
	private String filter;

	private Div divContent;

	@Wire
	private Div divEntry, divList, divApproval, divRequest, divPod;
	@Wire
	private Div divProduct;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("content") Div divContent) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.arg = arg;
		this.divContent = divContent;
		if (arg.equals("entry") || arg.equals("entryopr")) {
			divEntry.setVisible(true);
		} else if (arg.equals("list") || arg.equals("listopr")) {
			divList.setVisible(true);
		} else if (arg.equals("approval") || arg.equals("approvalopr")) {
			divApproval.setVisible(true);
		} else if (arg.equals("req")) {
			divRequest.setVisible(true);
		} else if (arg.equals("pod")) {
			divPod.setVisible(true);
		}
		doRender();
	}

	@NotifyChange("*")
	public void doRender() {
		try {
			filter = "0=0";

			if (arg.equals("approval") || arg.equals("approvalopr")) {
				if (oUser.getMbranch().getBranchlevel() == 2) {
					filter += " and status = '" + AppUtils.STATUS_ORDER_WAITAPPROVALWIL + "' and mbranchpk = "
							+ oUser.getMbranch().getMbranchpk();
				} else if (oUser.getMbranch().getBranchlevel() == 3) {
					filter += " and status = '" + AppUtils.STATUS_ORDER_WAITAPPROVALCAB + "' and mbranchpk = "
							+ oUser.getMbranch().getMbranchpk();
				} else {
					if (oUser.getMbranch().getBranchid().equals("423")) {
						filter += " and status = '" + AppUtils.STATUS_ORDER_WAITAPPROVAL + "' and mbranchpk = "
								+ oUser.getMbranch().getMbranchpk();
					} else {
						if (arg.equals("approval")) {
							filter += " and status = '" + AppUtils.STATUS_ORDER_WAITAPPROVAL + "' and ordertype = 'C'";
						} else if (arg.equals("approvalopr")) {
							filter += " and status = '" + AppUtils.STATUS_ORDER_WAITAPPROVAL + "' and ordertype = 'P' and mbranchpk = "
									+ oUser.getMbranch().getMbranchpk();
						}
					}
				}
			} else if (arg.equals("list")) {
				if (oUser.getMbranch().getBranchlevel() == 2)
					filter += " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
				else if (oUser.getMbranch().getBranchlevel() == 3)
					filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();
				else
					filter += " and ordertype = '" + AppUtils.ENTRYTYPE_MANUAL_BRANCH
							+ "' and status not in ('" + AppUtils.STATUS_ORDER_WAITAPPROVALWIL
							+ "' , '" + AppUtils.STATUS_ORDER_DECLINEWIL + "', '" + AppUtils.STATUS_ORDER_REJECTED
							+ "', '" + AppUtils.STATUS_ORDER_DECLINECAB + "', '" + AppUtils.STATUS_ORDER_WAITAPPROVALCAB + "')";
			} else if (arg.equals("req")) {
				if (oUser.getMbranch().getBranchlevel() == 2)
					filter += " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk() + " and status = '"
							+ AppUtils.STATUS_ORDER_REQUESTORDER + "' and ordertype = '"
							+ AppUtils.ENTRYTYPE_MANUAL_BRANCH + "' and orderoutlet = '00'";
				else if (oUser.getMbranch().getBranchlevel() == 3)
					filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and ordertype = '"
							+ AppUtils.ENTRYTYPE_MANUAL_BRANCH + "' and status = '" + AppUtils.STATUS_ORDER_REQUESTORDER
							+ "' and orderoutlet != '00'";
				else
					filter += " and ordertype = '" + AppUtils.ENTRYTYPE_MANUAL_BRANCH
							+ "' and status = '" + AppUtils.STATUS_ORDER_WAITSCANPRODUKSIOPR + "'";
			} else if (arg.equals("pod")) {
				filter = "tglterima is null and penerima is null and mbranchfk = " + oUser.getMbranch().getMbranchpk()
						+ " and torder.status = '" + AppUtils.STATUS_DELIVERY_DELIVERY + "' and orderoutlet != '00'";
			} else {
				filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();
			}

			if (oUser.getMbranch().getBranchlevel() > 1) {
				String filterbranch = "";
				if (oUser.getMbranch().getBranchlevel() == 2)
					filterbranch = "productgroupcode = '04'";
				else
					if (arg.equals("pod")) {
						filterbranch = "productgroupcode = '04'";
					} else {
						filterbranch = "productgroupcode in ('02', '03', '04')";
					}
				List<Mproductgroup> objList = new MproductgroupDAO().listByFilter(filterbranch, "productgroupcode");
				for (Mproductgroup obj : objList) {
					List<Vsumbyproductgroup> productList = new TorderDAO().getSumdataByProductgroup(
							filter + " and productgroup = '" + obj.getProductgroupcode() + "'");

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
					lbl = new Label("Jumlah Pemesanan : ");
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
						btn.setLabel("Buat Pemesanan Baru");
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
							if (arg.equals("entry") || arg.equals("entryopr")) {
								if (obj.getProductgroupcode().trim().equals(AppUtils.PRODUCTGROUP_PINPAD))
									Executions.createComponents("/view/order/orderentrypinpad.zul", divContent, map);
								else
									Executions.createComponents("/view/order/orderentry.zul", divContent, map);
							} else if (arg.equals("list") || arg.equals("listopr")) {
								Executions.createComponents("/view/order/orderlist.zul", divContent, map);
							} else if (arg.equals("approval") || arg.equals("approvalopr")) {
								Executions.createComponents("/view/order/orderapproval.zul", divContent, map);
							} else if (arg.equals("req")) {
								Executions.createComponents("/view/order/orderrequestlist.zul", divContent, map);
							} else if (arg.equals("pod")) {
								Executions.createComponents("/view/order/orderaccepted.zul", divContent, map);
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
				List<Mbranchproductgroup> objList = new MbranchproductgroupDAO().listNativeByFilter("mbranchfk = "
						+ oUser.getMbranch().getMbranchpk() + " and productgroupcode not in ('01', '09')",
						"mproductgroupfk");
				for (Mbranchproductgroup obj : objList) {
					if (obj.getMproductgroup().getProductgroupcode().equals(AppUtils.PRODUCTGROUP_DOCUMENT))
						filter = filter.replace("orderlevel = 1", "orderlevel = 2");
					else
						filter = filter.replace("orderlevel = 2", "orderlevel = 3");
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
					lbl = new Label("Jumlah Data Pemesanan : ");
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
							if (arg.equals("entry") || arg.equals("entryopr")) {
								if (arg.equals("entry")) {
									map.put("type", "C");
								} else if (arg.equals("entryopr")) {
									map.put("type", "P");
								}
								if (oUser.getMbranch().getBranchlevel() == 1) {
									if (obj.getMproductgroup().getProductgroupcode().trim().equals(AppUtils.PRODUCTGROUP_PINPAD) && arg.equals("entry"))
										Executions.createComponents("/view/order/orderentrypinpad.zul", divContent, map);
									else
										Executions.createComponents("/view/order/orderentry.zul", divContent, map);
								} else 
									Executions.createComponents("/view/order/orderentry.zul", divContent, map);
							} else if (arg.equals("list") || arg.equals("listopr")) {
								if (arg.equals("listopr"))
									map.put("isOPR", "Y");
								Executions.createComponents("/view/order/orderlist.zul", divContent, map);
							} else if (arg.equals("approval") || arg.equals("approvalopr")) {
								if (arg.equals("approvalopr")) {
									map.put("type", "P");
								} else if (arg.equals("approval")) {
									map.put("type", "C");
								}
								Executions.createComponents("/view/order/orderapproval.zul", divContent, map);
							} else if (arg.equals("req")) {
								Executions.createComponents("/view/order/orderrequestlist.zul", divContent, map);
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
