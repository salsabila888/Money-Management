package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zkoss.chart.Theme;
import org.zkoss.chart.Tooltip;
import org.zkoss.chart.model.CategoryModel;
import org.zkoss.chart.model.DefaultCategoryModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.DashboardDAO;
import com.sdd.caption.domain.Vpersodeliv;
import com.sdd.caption.domain.Vpersodelivdata;

public class ChartOrderVsPersoProductVm {

	private String orderdate;
	private String persotype;
	private Date date;
	private Integer totalorder;
	private Integer totalprod;
	private Vpersodeliv objForm;
	private Vpersodelivdata obj;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd MMM yyyy");

	@Wire
	private Charts chart;
	@Wire
	private Window winOrderPersoProdcod;
	@Wire
	private Grid grid;
	@Wire
	private Div divChart;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("date") Date dateback, @ExecutionArgParam("obj") Vpersodelivdata objarg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		
		if (dateback != null)
			this.date = dateback;
		this.obj = objarg;
		doReset();
		
		grid.setRowRenderer(new RowRenderer<Vpersodelivdata>() {

			@Override
			public void render(Row row, final Vpersodelivdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				A a = new A(data.getOrg());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("date", date);
						map.put("obj", obj);
						map.put("objproduct", data);

						Div divContent = (Div) winOrderPersoProdcod.getParent();
						divContent.getChildren().clear();
						Executions.createComponents("/view/dashboard/chartordervspersoregion.zul", divContent, map);
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(data.getTotalperso() != null ? NumberFormat.getInstance().format(data.getTotalperso()) : "0"));
				row.getChildren().add(new Label(data.getTotaldeliv() != null ? NumberFormat.getInstance().format(data.getTotaldeliv()) : "0"));
			}
		});
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		if (date != null) {
			try {
				doChartRefresh();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doBack() {									
		Map<String, Object> map = new HashMap<>();
		map.put("date", date);
		
		Div divContent = (Div) winOrderPersoProdcod.getParent();
		divContent.getChildren().clear();
		Executions.createComponents("/view/dashboard/chartordervsperso.zul", divContent, map);
	}

	@Command
	@NotifyChange("*")
	public void doChartRefresh() {
		if (date != null) {
			try {
				Map<String, Integer> map = new HashMap<>();
				chart.setTitle("Dashboard Order VS Produksi Org " + obj.getDescription());
				chart.setSubtitle("Periode " + datelocalFormatter.format(date));
				
				List<String> listProduct = new ArrayList<>();
				List<Vpersodeliv> oList = new DashboardDAO().listOrdOnOrdVsProdProcCod(dateFormatter.format(date), obj.getOrg());
				for (Vpersodeliv data: oList) {
					map.put(data.getOrg() + "ord", data.getTotaldata());
					if (!listProduct.contains(data.getOrg()))
						listProduct.add(data.getOrg());
				}
				List<Vpersodeliv> listProd = new DashboardDAO().listProdOnOrdVsProdProcCod(dateFormatter.format(date), obj.getOrg());
				for (Vpersodeliv data: listProd) {
					map.put(data.getOrg() + "prod", data.getTotaldata());
				}
				
				CategoryModel model = new DefaultCategoryModel();
				List<Vpersodelivdata> objList = new ArrayList<>();
				//List<String> listProduct = new MproductDAO().getProductOnOrder("orderdate = '" + dateFormatter.format(date) + "' and mproduct.mproducttype.productorg = '" + obj.getOrg() + "'");
				totalorder = 0;
				totalprod = 0;
				for (String mproduct : listProduct) {
					model.setValue("Order", mproduct, map.get(mproduct + "ord"));
					model.setValue("Produksi", mproduct, map.get(mproduct + "prod"));	
					Vpersodelivdata o = new Vpersodelivdata();
					o.setOrg(mproduct);
					o.getDescription();
					o.setTotalperso(map.get(mproduct + "ord"));
					o.setTotaldeliv(map.get(mproduct + "prod"));
					objList.add(o);
					totalorder += (o.getTotalperso() != null ? o.getTotalperso() : 0);
					totalprod += (o.getTotaldeliv() != null ? o.getTotalperso() : 0);
				}
				grid.setModel(new ListModelList<Vpersodelivdata>(objList));
				
				chart.setModel(model);
				chart.getXAxis().setMin(0);
				chart.getXAxis().getTitle().setText("Jenis Kartu");
				chart.getYAxis().getTitle().setText("Jumlah Data");
				chart.getXAxis().setCrosshair(true);
				
				Tooltip tooltip = chart.getTooltip();
				tooltip.setHeaderFormat("<span style=\"font-size:10px\">{point.key}</span><table>");
				tooltip.setPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td>"
					+ "<td style=\"padding:0\"><b>{point.y}</b></td></tr>");
				tooltip.setFooterFormat("</table>");
				tooltip.setShared(true);
				tooltip.setUseHTML(true);
				
				chart.getPlotOptions().getColumn().setPointPadding(0.2);
				chart.getPlotOptions().getColumn().setBorderWidth(0);
				
				chart.setTheme(Theme.DEFAULT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		if (date == null)
			date = new Date();
		doSearch();
	}

	public String getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(String orderdate) {
		this.orderdate = orderdate;
	}

	public String getPersotype() {
		return persotype;
	}

	public void setPersotype(String persotype) {
		this.persotype = persotype;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Vpersodeliv getObjForm() {
		return objForm;
	}

	public void setObjForm(Vpersodeliv objForm) {
		this.objForm = objForm;
	}

	public Integer getTotalorder() {
		return totalorder;
	}

	public void setTotalorder(Integer totalorder) {
		this.totalorder = totalorder;
	}

	public Integer getTotalprod() {
		return totalprod;
	}

	public void setTotalprod(Integer totalprod) {
		this.totalprod = totalprod;
	}

	public Vpersodelivdata getObj() {
		return obj;
	}

	public void setObj(Vpersodelivdata obj) {
		this.obj = obj;
	}

	public Window getWinOrderPersoProdcod() {
		return winOrderPersoProdcod;
	}

	public void setWinOrderPersoProdcod(Window winOrderPersoProdcod) {
		this.winOrderPersoProdcod = winOrderPersoProdcod;
	}
	

}
