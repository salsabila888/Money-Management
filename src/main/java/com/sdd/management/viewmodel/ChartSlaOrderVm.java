package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
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
import org.zkoss.chart.Charts;
import org.zkoss.chart.Color;
import org.zkoss.chart.Point;
import org.zkoss.chart.Series;
import org.zkoss.chart.plotOptions.PieDataLabels;
import org.zkoss.chart.plotOptions.PiePlotOptions;
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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.DashboardDAO;
import com.sdd.caption.dao.TorderdataDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.utils.AppUtils;

public class ChartSlaOrderVm {

	private Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private DashboardDAO oDao = new DashboardDAO();

	private String filter;
	private Date startdate;
	private Date enddate;
	private Integer year;
	private Integer month1;
	private Integer totalperso;
	private Integer totaldelivery;
	private Integer sla0;
	private Integer sla1;
	private Integer sla2;
	private Integer sla3;
	private Integer sla4;
	private Integer sla5;
	private Boolean isIndex;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Div divChartProd;
	@Wire
	private Div divChartDlv;
	@Wire
	private Window winSlaPersoDelivery;
	@Wire
	private Grid grid;
	@Wire
	private Combobox cbMonth1;
	@Wire
	private Div divTable;
	@Wire
	private A aView;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("isIndex") Boolean isIndex, @ExecutionArgParam("startdate") Date backstartdate,
			@ExecutionArgParam("enddate") Date backenddate) throws ParseException {
		Selectors.wireComponents(view, this, false);

		oUser = (Muser) zkSession.getAttribute("oUser");

		if (isIndex != null)
			this.isIndex = isIndex;
		else
			this.isIndex = false;

		if (backstartdate != null)
			this.startdate = backstartdate;
		if (backenddate != null)
			this.enddate = backenddate;

		doReset();
		// setMonthList();

		if (this.isIndex) {
			divTable.setVisible(false);
		} else {
			aView.setVisible(false);
			
		}
	}

	@Command
	@NotifyChange("*")
	public void doView() {
		if (oUser != null) {
			try {
				if (oDao.usermenuChecker("muserpk = " + oUser.getMuserpk()
						+ " and menupath = '/view/dashboard/chartslaorder.zul'") > 0) {
					Div divContent = (Div) winSlaPersoDelivery.getParent().getParent().getParent().getParent()
							.getParent();
					divContent.getChildren().clear();
					Map<String, Object> map = new HashMap<>();
					map.put("startdate", startdate);
					map.put("enddate", enddate);
					Executions.createComponents("/view/dashboard/chartslaorder.zul", divContent, map);
				} else {
					Messagebox.show("Anda tidak punya kewenangan untuk mengakses modul ini", "Info", 1,
							"z-messagebox-icon z-messagebox-information");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		if (startdate != null && enddate != null) {
			try {
				filter = "orderdate between '" + dateFormatter.format(startdate) + "' and " + "'"
						+ dateFormatter.format(enddate) + "'";

				doChartRefresh();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doChartRefresh() {
		if (startdate != null && enddate != null) {
			try {
				divChartProd.getChildren().clear();
				divChartDlv.getChildren().clear();

				Charts chart = new Charts();
				chart.setType("pie");
				Charts chart2 = new Charts();
				chart2.setType("pie");

				chart.setTitle("Dashboard SLA Perso");
				chart.setSubtitle("Periode " + datelocalFormatter.format(startdate) + " s/d "
						+ datelocalFormatter.format(enddate));
				chart2.setTitle("Dashboard SLA Delivery");
				chart2.setSubtitle("Periode " + datelocalFormatter.format(startdate) + " s/d "
						+ datelocalFormatter.format(enddate));
				Series series = chart.getSeries();
				Series series2 = chart2.getSeries();
				totalperso = 0;
				totaldelivery = 0;

				sla0 = 0;
				sla1 = 0;
				sla2 = 0;
				sla3 = 0;
				sla4 = 0;
				sla5 = 0;

				
				series.setName("Total Data");
				series.addPoint(new Point("0 s/d 2 Hari", sla0));
				series.addPoint(new Point("3 Hari", sla3));
				series.addPoint(new Point(">= 4 Hari", sla4));

				series2.setName("Total Data");
				series2.addPoint(new Point("0 s/d 2 Hari", sla1));
				series2.addPoint(new Point("3 Hari", sla2));
				series2.addPoint(new Point(">= 4 Hari", sla5));

				PiePlotOptions plotOptions = chart.getPlotOptions().getPie();
				plotOptions.setAllowPointSelect(true);
				plotOptions.setCursor("pointer");
				PieDataLabels dataLabels = (PieDataLabels) plotOptions.getDataLabels();
				dataLabels.setEnabled(true);
				dataLabels.setFormat("<b>{point.name}</b>: {point.percentage:.1f} %");
				plotOptions.setShowInLegend(true);

				PiePlotOptions plotOptions2 = chart2.getPlotOptions().getPie();
				plotOptions2.setAllowPointSelect(true);
				plotOptions2.setCursor("pointer");
				PieDataLabels dataLabels2 = (PieDataLabels) plotOptions2.getDataLabels();
				dataLabels2.setEnabled(true);
				dataLabels2.setFormat("<b>{point.name}</b>: {point.percentage:.1f} %");
				plotOptions2.setShowInLegend(true);

				chart.getSeries().getPoint(0).setColor(new Color(AppUtils.COLOR[0]));
				chart.getSeries().getPoint(1).setColor(new Color(AppUtils.COLOR[1]));
				chart.getSeries().getPoint(2).setColor(new Color(AppUtils.COLOR[2]));

				chart2.getSeries().getPoint(0).setColor(new Color(AppUtils.COLOR[0]));
				chart2.getSeries().getPoint(1).setColor(new Color(AppUtils.COLOR[1]));
				chart2.getSeries().getPoint(2).setColor(new Color(AppUtils.COLOR[2]));

				divChartProd.appendChild(chart);
				divChartDlv.appendChild(chart2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		if (startdate == null && enddate == null) {
			Calendar cal = Calendar.getInstance();
			enddate = new Date();
			cal.setTime(enddate);
			cal.add(Calendar.DAY_OF_MONTH, -7);
			startdate = cal.getTime();
		}
		doSearch();
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth1() {
		return month1;
	}

	public void setMonth1(Integer month1) {
		this.month1 = month1;
	}

	public Integer getTotalperso() {
		return totalperso;
	}

	public void setTotalperso(Integer totalperso) {
		this.totalperso = totalperso;
	}

	public Integer getTotaldelivery() {
		return totaldelivery;
	}

	public void setTotaldelivery(Integer totaldelivery) {
		this.totaldelivery = totaldelivery;
	}

	public Window getWinSlaPersoDelivery() {
		return winSlaPersoDelivery;
	}

	public void setWinSlaPersoDelivery(Window winSlaPersoDelivery) {
		this.winSlaPersoDelivery = winSlaPersoDelivery;
	}

	public Date getStartdate() {
		return startdate;
	}

	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}

	public Date getEnddate() {
		return enddate;
	}

	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}

}
