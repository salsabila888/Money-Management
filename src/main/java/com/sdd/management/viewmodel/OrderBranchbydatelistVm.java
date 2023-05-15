package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormatSymbols;
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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.TorderReport2DW;
import com.sdd.caption.domain.TorderReport2O;
import com.sdd.caption.utils.AppUtils;

public class OrderBranchbydatelistVm {
	private Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TorderDAO oDao = new TorderDAO();

	private String productgroup, filter, filter2, orderby, orderby2, branchfk, branchname, sentregion, sentordoutlet;
	private Integer month, year;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("MMM YYYY");
	private SimpleDateFormat datenormalFormatter = new SimpleDateFormat("YYYY-MM-dd");

	@Wire
	private Combobox cbMonth;
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
			@ExecutionArgParam("sentordoutlet") String sentordoutlet, @ExecutionArgParam("month") Integer month,
			@ExecutionArgParam("year") Integer year) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		this.productgroup = prodgrp;
		this.branchfk = sentid;
		this.branchname = sentname;
		this.sentregion = sentregion;
		this.sentordoutlet = sentordoutlet;
		this.month = month;
		this.year = year;
		doReset();

		if (grid != null) {
			if (oUser.getMbranch().getBranchlevel() == 3) {
				grid.setRowRenderer(new RowRenderer<TorderReport2O>() {
					@Override
					public void render(Row row, final TorderReport2O data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						A aOrderdate = new A(data.getOrderdate().toString());
						aOrderdate.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("prodgrp", productgroup);
								map.put("sentid", data.getMbranchfk());
								map.put("sentname", data.getBranchname());
								map.put("sentregion", sentregion);
								if (month != null && year != null) {
									map.put("month", month);
									map.put("year", year);
								}
								map.put("sentordate", data.getOrderdate().toString());
								map.put("keypage", 2);
								map.put("sentordoutlet", data.getOrderoutlet());
								Window win = (Window) Executions
										.createComponents("/view/report/orderbranchlistbyorderid.zul", null, map);
								win.setWidth("60%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(aOrderdate);
						row.getChildren().add(new Label(data.getOrderoutlet()));
						row.getChildren().add(new Label(
								data.getJumlahorder() != null ? data.getJumlahorder().toString().trim() : "0"));
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
				grid.setRowRenderer(new RowRenderer<TorderReport2DW>() {
					@Override
					public void render(Row row, final TorderReport2DW data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						A aOrderdate = new A(data.getOrderdate().toString());
						aOrderdate.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("prodgrp", productgroup);
								map.put("sentid", data.getMbranchfk());
								map.put("sentname", data.getBranchname());
								map.put("sentregion", sentregion);
								if (month != null && year != null) {
									map.put("month", month);
									map.put("year", year);
								}
								map.put("sentordate", data.getOrderdate().toString());
								map.put("keypage", 2);
								map.put("sentordoutlet", null);
								Window win = (Window) Executions
										.createComponents("/view/report/orderbranchlistbyorderid.zul", null, map);
								win.setWidth("60%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(aOrderdate);
						row.getChildren().add(new Label(""));
						row.getChildren().add(new Label(
								data.getJumlahorder() != null ? data.getJumlahorder().toString().trim() : "0"));
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

		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item = new Comboitem();
			item.setLabel(months[i]);
			item.setValue(i + 1);
			cbMonth.appendChild(item);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
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
						// div
						if (oUser.getMbranch().getBranchlevel() == 1) {
							if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
								filter = "branchlevel = 2 and productgroup = '" + productgroup + "' and orderdate between '"
										+ sldate + "' and '" + eldate + "'";
								filter2 = "branchlevel = 2 and productgroup = '" + productgroup
										+ "' and status = 'D29' and orderdate between '" + sldate + "' and '" + eldate
										+ "'";
								orderby = "orderdate desc";
								orderby2 = "orderdate desc";
							} else { 
								filter = "branchlevel = 3 and productgroup = '" + productgroup + "' and orderdate between '"
										+ sldate + "' and '" + eldate + "'";
								filter2 = "branchlevel = 3 and productgroup = '" + productgroup
										+ "' and status = 'D29' and orderdate between '" + sldate + "' and '" + eldate
										+ "'";
								orderby = "orderdate desc";
								orderby2 = "orderdate desc";
							}
						}

						// wil
						else if (oUser.getMbranch().getBranchlevel() == 2) {
							filter = "branchlevel > 1 and productgroup = '" + productgroup + "' and mregionfk = "
									+ oUser.getMbranch().getMregion().getMregionpk() + " and mbranchfk = " + branchfk
									+ " and orderdate between '" + sldate + "' and '" + eldate + "'";
							filter2 = "branchlevel > 1 and productgroup = '" + productgroup
									+ "' and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk()
									+ " and mbranchfk = " + branchfk + " and status = 'D29' and orderdate between '"
									+ sldate + "' and '" + eldate + "'";
							orderby = "orderdate desc";
							orderby2 = "orderdate desc";
						}

						// cab
						else if (oUser.getMbranch().getBranchlevel() == 3) {
							filter = "orderoutlet = '" + sentordoutlet + "' and branchlevel = 3 and productgroup = '"
									+ productgroup + "' and mbranchfk = " + oUser.getMbranch().getMbranchpk()
									+ " and orderdate between '" + sldate + "' and '" + eldate + "'";
							filter2 = "orderoutlet = '" + sentordoutlet + "' and mbranchfk = "
									+ oUser.getMbranch().getMbranchpk() + " and status = 'D29' and orderdate between '"
									+ sldate + "' and '" + eldate + "'";
							orderby = "orderdate desc";
							orderby2 = "orderdate desc";
						}
					}

					if (oUser.getMbranch().getBranchlevel() == 1) {
						cardTitle.setValue("Daftar Order " + branchname + " / " + datelocalFormatter.format(edt));
					} else if (oUser.getMbranch().getBranchlevel() == 2) {
						cardTitle.setValue("Daftar Order " + branchname + " / " + datelocalFormatter.format(edt));
					} else if (oUser.getMbranch().getBranchlevel() == 3) {
						clOutlet.setVisible(true);
						clOutlet.setLabel("Outlet");
						cardTitle.setValue("Daftar Order " + branchname + " / " + datelocalFormatter.format(edt));
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
				List<TorderReport2O> objList = new ArrayList<>();
				objList = oDao.listPagingreport2O(filter, filter2, orderby, orderby2);
				grid.setModel(new ListModelList<>(objList));
			} else {
				List<TorderReport2DW> objList = new ArrayList<>();
				objList = oDao.listPagingreport2DW(filter, filter2, orderby, orderby2);
				grid.setModel(new ListModelList<>(objList));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
}
