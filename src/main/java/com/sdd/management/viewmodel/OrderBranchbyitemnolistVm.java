package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.A;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.handler.InquiryHandler;
import com.sdd.caption.pojo.InquiryDetailBean;
import com.sdd.caption.pojo.InquiryOrder;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class OrderBranchbyitemnolistVm {
	private Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TorderitemDAO oDao = new TorderitemDAO();

	private String productgroup, filter, orderby, branchfk, branchname, sentregion, sentordoutlet, sentordate,
			sentordid, itemno;
	private Integer month, year;

	private SimpleDateFormat datenormalFormatter = new SimpleDateFormat("YYYY-MM-dd");

	@Wire
	private Grid grid;
	@Wire
	private Label cardTitle;
	@Wire
	private Column clOutlet;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("prodgrp") String prodgrp, @ExecutionArgParam("sentid") String sentid,
			@ExecutionArgParam("sentname") String sentname, @ExecutionArgParam("sentregion") String sentregion,
			@ExecutionArgParam("sentordoutlet") String sentordoutlet,
			@ExecutionArgParam("sentordate") String sentordate, @ExecutionArgParam("sentordid") String sentordid,
			@ExecutionArgParam("month") Integer month, @ExecutionArgParam("year") Integer year) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.productgroup = prodgrp;
		this.branchfk = sentid;
		this.branchname = sentname;
		this.sentregion = sentregion;
		this.sentordate = sentordate;
		this.sentordoutlet = sentordoutlet;
		this.sentordid = sentordid;
		this.month = month;
		this.year = year;

		if (oUser.getMbranch().getBranchlevel() == 1) {
			cardTitle.setValue("Daftar Order " + branchname + " / ORDER ID: " + sentordid + " / " + sentordate);
		} else if (oUser.getMbranch().getBranchlevel() == 2) {
			cardTitle.setValue("Daftar Order " + branchname + " / ORDER ID: " + sentordid + " / " + sentordate);
		} else if (oUser.getMbranch().getBranchlevel() == 3) {
			if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
				clOutlet.setVisible(true);
			} else { 
				clOutlet.setVisible(false);
			}
			
			clOutlet.setLabel("Outlet");
			cardTitle.setValue("Daftar Order " + branchname + " / ORDER ID: " + sentordid + " / " + sentordate);
		}
		doReset();

		if (grid != null) {
			if (oUser.getMbranch().getBranchlevel() == 3) {
				grid.setRowRenderer(new RowRenderer<Torderitem>() {
					@Override
					public void render(Row row, final Torderitem data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						if (oUser.getMbranch().getBranchlevel() == 3) {
							row.getChildren().add(new Label(data.getTorder().getOrderoutlet()));
						} else {
							row.getChildren().add(new Label(""));
						}

						A aItemnoparse = new A(data.getItemno());
						aItemnoparse.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								InquiryOrder obj = new InquiryOrder();
								obj.setItemno(data.getItemno());
								obj.setMbranch(data.getTorder().getMbranch());
								obj.setMproducttype(data.getTorder().getMproduct().getMproducttype());

								Map<String, Object> map = new HashMap<String, Object>();
								map.put("obj", obj);
								map.put("arg", data.getTorder().getProductgroup());
								Window win = (Window) Executions.createComponents("/view/inquiry/inquirydatadetail.zul",
										null, map);
								win.setWidth("70%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(aItemnoparse);
						row.getChildren().add(new Label(AppData.getStatusLabel(data.getTorder().getStatus())));
					}
				});
			} else {
				grid.setRowRenderer(new RowRenderer<Torderitem>() {
					@Override
					public void render(Row row, final Torderitem data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						row.getChildren().add(new Label(""));
						A aItemnoparse = new A(data.getItemno());
						aItemnoparse.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
//								
								InquiryOrder obj = new InquiryOrder();
								obj.setItemno(data.getItemno());
								obj.setMbranch(data.getTorder().getMbranch());
								if (productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
									obj.setMproducttype(data.getTpinpadorderproduct().getMproduct().getMproducttype());
								} else {
									obj.setMproducttype(data.getTorder().getMproduct().getMproducttype());
								}

								Map<String, Object> map = new HashMap<String, Object>();
								map.put("obj", obj);
								map.put("arg", data.getTorder().getProductgroup());
								Window win = (Window) Executions.createComponents("/view/inquiry/inquirydatadetail.zul",
										null, map);
								win.setWidth("70%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(aItemnoparse);
						row.getChildren().add(new Label(AppData.getStatusLabel(data.getTorder().getStatus())));
					}
				});
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		itemno = "";
		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (oUser != null) {
				orderby = "numerator";
				if (month != null && year != null) {
					Integer fday = 1;
					String dd = "";
					String md = "";

					if (fday.toString().length() < 2) {
						dd = "0" + fday.toString();
					}

					if (month.toString().length() < 2) {
						md = "0" + month.toString();
					} else {
						md = month.toString();
					}
					String sldate = year + "-" + md + "-" + dd;

					Calendar cal = Calendar.getInstance();
					cal.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(dd + "/" + md + "/" + year));
					cal.add(Calendar.MONTH, 1);
					cal.add(Calendar.DATE, -1);
					Date edt = cal.getTime();
					String eldate = datenormalFormatter.format(edt);

					if (oUser.getMbranch() != null) {
						if (itemno == "" || "".equals(itemno)) {
							// div
							if (oUser.getMbranch().getBranchlevel() == 1) {
								filter = "orderid = '" + sentordid + "' and mregionfk = " + sentregion
										+ " and branchlevel > 1 and torder.productgroup = '" + productgroup
										+ "' and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
							}

							// wil
							else if (oUser.getMbranch().getBranchlevel() == 2) {
								filter = "orderid = '" + sentordid + "' and mbranchfk = " + branchfk
										+ " and branchlevel > 1 and torder.productgroup = '" + productgroup
										+ "' and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk()
										+ " and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
							}

							// cab
							else if (oUser.getMbranch().getBranchlevel() == 3) {
								filter = "orderid = '" + sentordid + "' and orderoutlet = '" + sentordoutlet
										+ "' and branchlevel = 3 and torder.productgroup = '" + productgroup
										+ "' and mbranchfk = " + oUser.getMbranch().getMbranchpk()
										+ " and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
							}
						} else {
							// div
							if (oUser.getMbranch().getBranchlevel() == 1) {
								filter = "itemno = '" + itemno + "' and orderid = '" + sentordid + "' and mregionfk = "
										+ sentregion + " and branchlevel > 1 and torder.productgroup = '" + productgroup
										+ "' and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
							}

							// wil
							else if (oUser.getMbranch().getBranchlevel() == 2) {
								filter = "itemno = '" + itemno + "' and orderid = '" + sentordid + "' and mbranchfk = "
										+ branchfk + " and branchlevel > 1 and torder.productgroup = '" + productgroup
										+ "' and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk()
										+ " and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
							}

							// cab
							else if (oUser.getMbranch().getBranchlevel() == 3) {
								filter = "itemno = '" + itemno + "' and orderid = '" + sentordid
										+ "' and orderoutlet = '" + sentordoutlet
										+ "' and branchlevel = 3 and torder.productgroup = '" + productgroup
										+ "' and mbranchfk = " + oUser.getMbranch().getMbranchpk()
										+ " and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
							}
						}
					}
				}
				refreshModel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			List<Torderitem> objList = new ArrayList<>();
			objList = oDao.listNativeByFilter2(filter, orderby);
			grid.setModel(new ListModelList<>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getItemno() {
		return itemno;
	}

	public void setItemno(String itemno) {
		this.itemno = itemno;
	}

}
