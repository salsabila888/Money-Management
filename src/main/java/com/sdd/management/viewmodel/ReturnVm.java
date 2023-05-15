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
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vsumbyproductgroup;
import com.sdd.caption.utils.AppUtils;

public class ReturnVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private String arg;
	private String filter;

	private Div divContent;

	@Wire
	private Div divEntry, divList, divApproval, divDestroy, divPaket, divEntryDes;
	@Wire
	private Div divProduct;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("content") Div divContent) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.arg = arg;
		this.divContent = divContent;
		if (arg.equals("entry")) {
			divEntry.setVisible(true);
		} else if (arg.equals("list")) {
			divList.setVisible(true);
		} else if (arg.equals("approval") || arg.equals("approvalwil") || arg.equals("approvalpfa")
				|| arg.equals("approvalopr")) {
			divApproval.setVisible(true);
		} else if (arg.equals("destroy")) {
			divDestroy.setVisible(true);
		} else if (arg.equals("returpaket")) {
			divPaket.setVisible(true);
		} else if (arg.equals("entrydes")) {
			divEntryDes.setVisible(true);
		}
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

			if (arg.equals("approval")) {
				filter += " and status = '" + AppUtils.STATUS_RETUR_WAITAPPROVAL + "'";
			} else if (arg.equals("approvalwil")) {
				filter += " and status = '" + AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH + "'";
			} else if (arg.equals("approvalpfa")) {
				filter += " and status = '" + AppUtils.STATUS_RETUR_WAITAPPROVALPFA + "'";
			} else if (arg.equals("approvalopr")) {
				filter += " and status = '" + AppUtils.STATUS_RETUR_WAITAPPROVALOPR + "'";
			} else if (arg.equals("destroy")) {
				if (oUser.getMbranch().getBranchlevel() == 3) {
					filter += "and isdestroy = 'Y' and returnlevel = '3'";
				} else if (oUser.getMbranch().getBranchlevel() == 2) {
					filter += " and isdestroy = 'Y' and status not in ('" + AppUtils.STATUS_RETUR_DECLINE + "')";
				} else if (oUser.getMbranch().getBranchlevel() == 1) {
					filter += "and isdestroy = 'Y' and returnlevel = '2' and status not in ('"
							+ AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH + "')";
				}
			} else if (arg.equals("returpaket")) {
				filter += " and status in ('" + AppUtils.STATUS_RETUR_RETURNPFA + "', '"
						+ AppUtils.STATUS_RETUR_RETURNOPR + "')";
			} else if (arg.equals("list")) {
				if (oUser.getMbranch().getBranchlevel() == 3) {
					filter += "and isdestroy = 'N' and returnlevel = '3'";
				} else if (oUser.getMbranch().getBranchlevel() == 2) {
					filter += "and isdestroy = 'N' and status not in ('" + AppUtils.STATUS_RETUR_DECLINE + "', '"
							+ AppUtils.STATUS_RETUR_WAITAPPROVAL + "')";
				} else if (oUser.getMbranch().getBranchlevel() == 1) {
					if (oUser.getMbranch().getBranchid().equals("723")) {
						filter += "and isdestroy = 'N' and status in ('" + AppUtils.STATUS_RETUR_WAITAPPROVALPFA
								+ "', '" + AppUtils.STATUS_RETUR_APPROVALPFA + "', '"
								+ AppUtils.STATUS_RETUR_DECLINEAPPROVALPFA + "', '" + AppUtils.STATUS_RETUR_RETURNPFA
								+ "', '" + AppUtils.STATUS_RETUR_RETURNEDPFA + "', '" + AppUtils.STATUS_RETUR_RECEIVED
								+ "', '" + AppUtils.STATUS_RETUR_PROCESSPFA + "')";
					} else {
						filter += "and isdestroy = 'N' and status in ('" + AppUtils.STATUS_RETUR_WAITAPPROVALOPR
								+ "', '" + AppUtils.STATUS_RETUR_APPROVALOPR + "', '"
								+ AppUtils.STATUS_RETUR_DECLINEAPPROVALOPR + "', '" + AppUtils.STATUS_RETUR_RETURNOPR
								+ "')";
					}
				}
			} else if (arg.equals("entrydes")) {
				if (oUser.getMbranch().getBranchlevel() == 3) {
					filter += "and isdestroy = 'Y' and returnlevel = '3'";
				} else if (oUser.getMbranch().getBranchlevel() == 2) {
					filter += " and status not in ('" + AppUtils.STATUS_RETUR_DECLINE + "')";
				} else if (oUser.getMbranch().getBranchlevel() == 1) {
					filter += "and isdestroy = 'Y' and returnlevel = '2' and status not in ('"
							+ AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH + "')";
				}
			} else if (arg.equals("entry")) {
				filter += "and isdestroy = 'N'";
			}

			String filterProduct = "";
			if (oUser.getMbranch().getBranchlevel() > 1) {
				if (arg.equals("entrydes")) {
					filterProduct = "productgroupcode = '04'";
				} else {
					filterProduct = "productgroupcode in ('04', '02')";
				}

			} else {
				if (oUser.getMbranch().getBranchid().equals("723")) {
					filterProduct = "productgroupcode in ('04')";
				} else {
					filterProduct = "productgroupcode in ('02')";
				}
			}

			List<Mproductgroup> objList = new MproductgroupDAO().listByFilter(filterProduct, "productgroupcode");
			for (Mproductgroup obj : objList) {
				List<Vsumbyproductgroup> productList = new TreturnDAO()
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
				if (arg.equals("entrydes") || arg.equals("destroy")) {
					lbl = new Label("Jumlah Data Destroy : ");
				} else {
					lbl = new Label("Jumlah Data Retur : ");
				}
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
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important; float:right !important;");
				if (arg.equals("entry")) {
					btn.setLabel("Buat Retur Baru");
				} else if (arg.equals("entrydes")) {
					btn.setLabel("Buat Destroy Baru");
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
							if (obj.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_TOKEN))
								Executions.createComponents("/view/return/returentrytoken.zul", divContent, map);
							else if (obj.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
								System.out.println("ENTRY SURAT BERHARGA");
								Executions.createComponents("/view/return/returentrydocument.zul", divContent, map);
							}
						} else if (arg.equals("list")) {
							Executions.createComponents("/view/return/returproductlist.zul", divContent, map);
						} else if (arg.equals("approval") || arg.equals("approvalwil") || arg.equals("approvalpfa")
								|| arg.equals("approvalopr")) {
							if (arg.equals("approval")) {
								map.put("stats", AppUtils.STATUS_RETUR_WAITAPPROVAL);
							} else if (arg.equals("approvalwil")) {
								map.put("stats", AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH);
							} else if (arg.equals("approvalpfa")) {
								map.put("stats", AppUtils.STATUS_RETUR_WAITAPPROVALPFA);
							} else if (arg.equals("approvalopr")) {
								map.put("stats", AppUtils.STATUS_RETUR_WAITAPPROVALOPR);
							}
							System.out.println(obj.getProductgroupcode());
							System.out.println(AppUtils.PRODUCTGROUP_DOCUMENT);
							if (obj.getProductgroupcode().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
								Executions.createComponents("/view/return/returapprovaldocument.zul", divContent, map);
							} else {
								Executions.createComponents("/view/return/returproductapproval.zul", divContent, map);
							}
						} else if (arg.equals("destroy")) {
							Executions.createComponents("/view/return/destroylist.zul", divContent, map);
						} else if (arg.equals("returpaket")) {
							Executions.createComponents("/view/return/returpaketlist.zul", divContent, map);
						} else if (arg.equals("entrydes")) {
							map.put("isDestroy", "Y");
							Executions.createComponents("/view/return/destroyentry.zul", divContent, map);
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
