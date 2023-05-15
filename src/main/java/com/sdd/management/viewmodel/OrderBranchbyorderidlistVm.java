package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.TorderReport3DW;
import com.sdd.caption.domain.TorderReport3O;
import com.sdd.caption.utils.AppUtils;

public class OrderBranchbyorderidlistVm {
	private Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TorderDAO oDao = new TorderDAO();

	private String productgroup, filter, filter2, orderby, orderby2, branchfk, branchname, sentregion, sentordoutlet,
			sentordate, orderid;
	private Integer month, year;

//	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd - MMM - YYYY");
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
			@ExecutionArgParam("sentordate") String sentordate, @ExecutionArgParam("month") Integer month,
			@ExecutionArgParam("year") Integer year) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.productgroup = prodgrp;
		this.branchfk = sentid;
		this.branchname = sentname;
		this.sentregion = sentregion;
		this.sentordate = sentordate;
		this.sentordoutlet = sentordoutlet;
		this.month = month;
		this.year = year;
		doReset();

		if (grid != null) {
			if (oUser.getMbranch().getBranchlevel() == 3) {
				grid.setRowRenderer(new RowRenderer<TorderReport3O>() {
					@Override
					public void render(Row row, final TorderReport3O data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						A aOrderid = new A(data.getOrderid().toString());
						aOrderid.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("prodgrp", productgroup);
								map.put("sentid", data.getMbranchfk());
								map.put("sentname", data.getBranchname());
								map.put("sentordate", data.getOrderdate().toString());
								map.put("keypage", 3);
								map.put("sentordoutlet", data.getOrderoutlet());
								map.put("sentordid", data.getOrderid().toString().trim());
								map.put("sentregion", sentregion);
								if (month != null && year != null) {
									map.put("month", month);
									map.put("year", year);
								}
								Window win = (Window) Executions
										.createComponents("/view/report/orderbranchlistbyorderitem.zul", null, map);
								win.setWidth("60%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(aOrderid);
						row.getChildren().add(new Label(data.getOrderoutlet()));
						row.getChildren().add(
								new Label(data.getJumlahitem() != null ? data.getJumlahitem().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getJumlahdiproses() != null ? data.getJumlahdiproses().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getJumlahdikirim() != null ? data.getJumlahdikirim().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getOutstanding() != null ? data.getOutstanding().toString().trim() : "0"));

						if (data.getJumlahdiproses() == null)
							data.setJumlahdiproses(0);
						if (data.getJumlahdikirim() == null)
							data.setJumlahdikirim(0);

						if (data.getJumlahdikirim() == 0 || data.getJumlahdikirim() == null) {
							row.getChildren().add(new Label("0%"));
						} else {						
							BigDecimal percent = new BigDecimal(0);
							BigDecimal x = BigDecimal.valueOf(data.getJumlahdikirim());
							BigDecimal y = BigDecimal.valueOf(data.getJumlahitem());
							percent = (x.divide(y, 4, RoundingMode.HALF_UP));
							percent = percent.multiply(new BigDecimal(100));
							row.getChildren().add(new Label(new DecimalFormat("0.00").format(percent) + "%"));
						}
					}
				});
			} else {
				grid.setRowRenderer(new RowRenderer<TorderReport3DW>() {
					@Override
					public void render(Row row, final TorderReport3DW data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						A aOrderid = new A(data.getOrderid().toString());
						aOrderid.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("prodgrp", productgroup);
								map.put("sentid", data.getMbranchfk());
								map.put("sentname", data.getBranchname());
								map.put("sentordate", data.getOrderdate().toString());
								map.put("keypage", 3);
								map.put("sentordoutlet", null);
								map.put("sentordid", data.getOrderid().toString().trim());
								map.put("sentregion", sentregion);
								if (month != null && year != null) {
									map.put("month", month);
									map.put("year", year);
								}
								Window win = (Window) Executions
										.createComponents("/view/report/orderbranchlistbyorderitem.zul", null, map);
								win.setWidth("60%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(aOrderid);
						row.getChildren().add(new Label(""));
						row.getChildren().add(
								new Label(data.getJumlahitem() != null ? data.getJumlahitem().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getJumlahdiproses() != null ? data.getJumlahdiproses().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getJumlahdikirim() != null ? data.getJumlahdikirim().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getOutstanding() != null ? data.getOutstanding().toString().trim() : "0"));

						if (data.getJumlahdiproses() == null)
							data.setJumlahdiproses(0);
						if (data.getJumlahdikirim() == null)
							data.setJumlahdikirim(0);

						if (data.getJumlahdikirim() == 0 || data.getJumlahdikirim() == null) {
							row.getChildren().add(new Label("0%"));
						} else {
							BigDecimal percent = new BigDecimal(0);
							BigDecimal x = BigDecimal.valueOf(data.getJumlahdikirim());
							BigDecimal y = BigDecimal.valueOf(data.getJumlahitem());
							percent = (x.divide(y, 4, RoundingMode.HALF_UP));
							percent = percent.multiply(new BigDecimal(100));
							row.getChildren().add(new Label(new DecimalFormat("0.00").format(percent) + "%"));
						}
					}
				});
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		orderid = "";
		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (oUser != null) {
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
						if (orderid == "" || "".equals(orderid)) {
							// div
							if (oUser.getMbranch().getBranchlevel() == 1) {
								if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
									filter = "mregionfk = " + sentregion + " and branchlevel = 2 and productgroup = '"
											+ productgroup + "' and orderdate = '" + sentordate
											+ "' and orderdate between '" + sldate + "' and '" + eldate + "'";
									filter2 = "mregionfk = " + sentregion + " and branchlevel = 2 and productgroup = '"
											+ productgroup + "' and orderdate = '" + sentordate
											+ "' and status = 'D29' and orderdate between '" + sldate + "' and '" + eldate
											+ "'";
									orderby = "orderid";
									orderby2 = "orderid";
								} else {
									filter = "mregionfk = " + sentregion + " and branchlevel = 3 and productgroup = '"
											+ productgroup + "' and orderdate = '" + sentordate
											+ "' and orderdate between '" + sldate + "' and '" + eldate + "'";
									filter2 = "mregionfk = " + sentregion + " and branchlevel = 3 and productgroup = '"
											+ productgroup + "' and orderdate = '" + sentordate
											+ "' and status = 'D29' and orderdate between '" + sldate + "' and '" + eldate
											+ "'";
									orderby = "orderid";
									orderby2 = "orderid";
								}
							}

							// wil
							else if (oUser.getMbranch().getBranchlevel() == 2) {
								filter = "mbranchfk = " + branchfk + " and productgroup = '"
										+ productgroup + "' and mregionfk = "
										+ oUser.getMbranch().getMregion().getMregionpk() + " and orderdate = '"
										+ sentordate + "' and orderdate between '" + sldate + "' and '" + eldate + "'";
								filter2 = "mbranchfk = " + branchfk + " and productgroup = '"
										+ productgroup + "' and mregionfk = "
										+ oUser.getMbranch().getMregion().getMregionpk() + " and orderdate = '"
										+ sentordate + "' and  status = 'D29' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
								orderby = "orderid";
								orderby2 = "orderid";
							}

							// cab
							else if (oUser.getMbranch().getBranchlevel() == 3) {
								filter = "orderoutlet = '" + sentordoutlet
										+ "' and branchlevel = 3 and productgroup = '" + productgroup
										+ "' and mbranchfk = " + oUser.getMbranch().getMbranchpk()
										+ " and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
								filter2 = "orderoutlet = '" + sentordoutlet + "' and mbranchfk = "
										+ oUser.getMbranch().getMbranchpk() + " and orderdate = '" + sentordate
										+ "' and status = 'D29' and orderdate between '" + sldate + "' and '" + eldate
										+ "'";
								orderby = "orderid";
								orderby2 = "orderid";
							}
						} else {
							// div
							if (oUser.getMbranch().getBranchlevel() == 1) {
								filter = "orderid = '" + orderid + "' and mregionfk = " + sentregion
										+ " and branchlevel = 2 and productgroup = '" + productgroup
										+ "' and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
								filter2 = "orderid = '" + orderid + "' and mregionfk = " + sentregion
										+ " and branchlevel = 2 and productgroup = '" + productgroup
										+ "' and orderdate = '" + sentordate
										+ "' and status = 'D29' and orderdate between '" + sldate + "' and '" + eldate
										+ "'";
								orderby = "orderid";
								orderby2 = "orderid";
							}

							// wil
							else if (oUser.getMbranch().getBranchlevel() == 2) {
								filter = "orderid = '" + orderid + "' and mbranchfk = " + branchfk
										+ " and branchlevel = 3 and productgroup = '" + productgroup
										+ "' and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk()
										+ " and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
								filter2 = "orderid = '" + orderid + "' and mbranchfk = " + branchfk
										+ " and branchlevel = 3 and productgroup = '" + productgroup
										+ "' and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk()
										+ " and orderdate = '" + sentordate
										+ "' and  status = 'D29' and orderdate between '" + sldate + "' and '" + eldate
										+ "'";
								orderby = "orderid";
								orderby2 = "orderid";
							}

							// cab
							else if (oUser.getMbranch().getBranchlevel() == 3) {
								filter = "orderid = '" + orderid + "' and orderoutlet = '" + sentordoutlet
										+ "' and branchlevel = 3 and productgroup = '" + productgroup
										+ "' and mbranchfk = " + oUser.getMbranch().getMbranchpk()
										+ " and orderdate = '" + sentordate + "' and orderdate between '" + sldate
										+ "' and '" + eldate + "'";
								filter2 = "orderid = '" + orderid + "' and orderoutlet = '" + sentordoutlet
										+ "' and mbranchfk = " + oUser.getMbranch().getMbranchpk()
										+ " and orderdate = '" + sentordate
										+ "' and status = 'D29' and orderdate between '" + sldate + "' and '" + eldate
										+ "'";
								orderby = "orderid";
								orderby2 = "orderid";
							}
						}

					}

					if (oUser.getMbranch().getBranchlevel() == 1) {
						cardTitle.setValue("Daftar Order " + branchname + " / " + sentordate);
					} else if (oUser.getMbranch().getBranchlevel() == 2) {
						cardTitle.setValue("Daftar Order " + branchname + " / " + sentordate);
					} else if (oUser.getMbranch().getBranchlevel() == 3) {
						clOutlet.setVisible(true);
						clOutlet.setLabel("Outlet");
						cardTitle.setValue("Daftar Order " + branchname + " / " + sentordate);
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
			if (oUser.getMbranch().getBranchlevel() == 3) {
				List<TorderReport3O> objList = new ArrayList<>();
				objList = oDao.listPagingreport3O(filter, filter2, orderby, orderby2);
				grid.setModel(new ListModelList<>(objList));
			} else {
				List<TorderReport3DW> objList = new ArrayList<>();
				objList = oDao.listPagingreport3DW(filter, filter2, orderby, orderby2);
				grid.setModel(new ListModelList<>(objList));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}
}