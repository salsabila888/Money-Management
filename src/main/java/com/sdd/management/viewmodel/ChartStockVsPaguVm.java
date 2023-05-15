package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
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
import org.zkoss.chart.Color;
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
import com.sdd.caption.dao.MorgDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Vinventory;
import com.sdd.caption.utils.AppUtils;

public class ChartStockVsPaguVm {

	private String orderdate;
	private String persotype;
	private Date date;
	private Vinventory objForm;
	private String org;
	private Integer gt50;
	private Integer gt25;
	private Integer lt25;
	private Integer totalgt50;
	private Integer totalgt25;
	private Integer totallt25;

	@Wire
	private Charts chart;
	@Wire
	private Window winStockvspagu;
	@Wire
	private Grid grid;
	@Wire
	private Div divChart;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("date") Date dateback)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		
		if (dateback != null)
			this.date = dateback;
		doReset();
		
		/* setMonthList(); */
		grid.setRowRenderer(new RowRenderer<Vinventory>() {

			@Override
			public void render(Row row, final Vinventory data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getDescription()));
				if(data.getA() != 0) {
					
					A agt6 = new A(data.getA() != null ? NumberFormat.getInstance().format(data.getA()) : "0");
					agt6.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);
							map.put("filterpredict", "persen >= 50");
							map.put("title",
									"Jumlah Stock VS Pagu Diatas 50% untuk Org " + data.getDescription());

							Window win = (Window) Executions.createComponents("/view/dashboard/chartstockvspagudetail.zul",
									null, map);
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(agt6);
				} else {					
					row.getChildren().add(new Label(data.getA() != null ? NumberFormat.getInstance().format(data.getA()) : "0"));
				}
				if(data.getB() != 0) {					
					A agt3 = new A(data.getB() != null ? NumberFormat.getInstance().format(data.getB()) : "0");
					agt3.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);
							map.put("filterpredict", "persen between 25 and 50");
							map.put("title",
									"Jumlah Stock VS Pagu antara 25% s/d 50% untuk Org " + data.getDescription());

							Window win = (Window) Executions.createComponents("/view/dashboard/chartstockvspagudetail.zul",
									null, map);
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(agt3);
					
				} else {					
					row.getChildren().add(new Label(data.getB() != null ? NumberFormat.getInstance().format(data.getB()) : "0"));
				}
				if(data.getC() != 0) {
					A alt3 = new A(data.getC() != null ? NumberFormat.getInstance().format(data.getC()) : "0");
					alt3.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);
							map.put("filterpredict", "persen < 25");
							map.put("title",
									"Jumlah Stock VS Pagu Dibawah 25% untuk Org " + data.getDescription());

							Window win = (Window) Executions.createComponents("/view/dashboard/chartstockvspagudetail.zul",
									null, map);
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(alt3);
				} else {
					row.getChildren().add(new Label(data.getC() != null ? NumberFormat.getInstance().format(data.getC()) : "0"));
				}
			}
		});
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		if (date != null) {
			try {
				
				doChartRefresh();
				/*List<Vpersodelivdata> objList = new ArrayList<>();
				
				for (int i = 0; i < catorg.length; i++) {
					Vpersodelivdata obj = new Vpersodelivdata();
					obj.setOrg(catorg[i]);
					obj.setDescription(catdescription[i]);
					obj.setTotalperso(catperso[i].intValue());
					obj.setTotaldeliv(catorder[i].intValue());
					objList.add(obj);
				}
				listbox.setModel(new ListModelList<Vpersodelivdata>(objList));*/
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doChartRefresh() {
			try {
				divChart.getChildren().clear();
				Charts chart = new Charts();
				chart.setTitle("Dashboard Jumlah Stock VS Pagu Stock Blankcard Periode Akumulatif");
				chart.setType("bar");				
				
				Integer gt50_200 = 0;
				Integer gt25_200 = 0;
				Integer lt25_200 = 0;
				List<Vinventory> obj = new DashboardDAO().listStockVsPagu();
				Map<String, Integer> map = new HashMap<>();
				for (Vinventory data: obj) {
					if (data.getOrg().startsWith("2")) {
						gt50_200 += data.getA();
						gt25_200 += data.getB();
						lt25_200 += data.getC();
					} else {
						map.put(data.getOrg() + "gt50", data.getA());
						map.put(data.getOrg() + "gt25", data.getB());
						map.put(data.getOrg() + "lt25", data.getC());
					}					
				}
				map.put("200" + "gt50", gt50_200);
				map.put("200" + "gt25", gt25_200);
				map.put("200" + "lt25", lt25_200);
				
				CategoryModel model = new DefaultCategoryModel();
				List<Vinventory> objList = new ArrayList<>();
				List<Morg> listOrg = new MorgDAO().listByFilter("0=0", "org");
				totalgt50 = 0;
				totalgt25 = 0;
				totallt25 = 0;
				
				for (Morg morg : listOrg) {	
					if (morg.getOrg().startsWith("2") && !morg.getOrg().equals("200"))
						continue;
					String org = morg.getOrg().startsWith("2") ? "200" :  morg.getOrg();
					String description = morg.getOrg().startsWith("2") ? "DERIVATIF" :  morg.getDescription();
					model.setValue("> 50%", description, map.get(org + "gt50"));
					model.setValue("25% - 50%", description, map.get(org + "gt25"));
					model.setValue("< 25%", description, map.get(org + "lt25"));
					Vinventory o = new Vinventory();
					o.setOrg(org);
					o.setDescription(description);
					o.setA(map.get(org + "gt50"));
					o.setB(map.get(org + "gt25"));
					o.setC(map.get(org + "lt25"));
					objList.add(o);
					totalgt50 += o.getA();
					totalgt25 += o.getB();
					totallt25 += o.getC();
				}
				grid.setModel(new ListModelList<Vinventory>(objList));				
				
				chart.setModel(model);
				chart.getXAxis().setMin(0);
				chart.getXAxis().getTitle().setText("Jenis");
				chart.getYAxis().getTitle().setText("Jumlah Data");
				chart.getXAxis().setCrosshair(true);
								
				Tooltip tooltip = chart.getTooltip();
				tooltip.setHeaderFormat("<span style=\"font-size:10px\">{point.key}</span><table>");
				tooltip.setPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td>"
					+ "<td style=\"padding:0\">{point.y} Kartu</td></tr>");
				tooltip.setFooterFormat("</table>");
				tooltip.setShared(true);
				tooltip.setUseHTML(true);
				
				chart.getPlotOptions().getColumn().setPointPadding(0.2);
				chart.getPlotOptions().getColumn().setBorderWidth(0);				
				
				chart.getSeries(0).setColor(new Color(AppUtils.COLOR[0]));
				chart.getSeries(1).setColor(new Color(AppUtils.COLOR[1]));
				chart.getSeries(2).setColor(new Color(AppUtils.COLOR[2]));
				
				chart.setTheme(Theme.DEFAULT);
				divChart.appendChild(chart);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	/*private void initSeries(Charts chart) throws Exception {
		try {
			List<Vpersodeliv> objList = oDao.listOrdOnOrdVsProd(dateFormatter.format(date));
			catperso = new Double[objList.size()];
			int i = 0;
			totalperso = 0;
			for (Vpersodeliv obj : objList) {
				catperso[i++] = new Double(obj.getTotaldata());
				totalperso += obj.getTotaldata();
			}
			Series ord = new Series("Order");
			ord.setName("Order");
			ord.setType("column");
			ord.setYAxis(1);
			ord.setData(catperso);
			chart.addSeries(ord);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			List<Vpersodeliv> objList = oDao.listProdOnOrdVsProd(dateFormatter.format(date));
			catorder = new Double[objList.size()];
			int i = 0;
			totaldeliv = 0;
			for (Vpersodeliv obj : objList) {
				catorder[i++] = new Double(obj.getTotaldata());
				totaldeliv += obj.getTotaldata();
			}
			Series prod = new Series("Produksi");
			prod.setName("Produksi");
			prod.setType("column");
			prod.setYAxis(2);
			prod.setData(catorder);
			chart.addSeries(prod);
		} catch (Exception e) {
			e.printStackTrace();
		}

		 chart.getPlotOptions().getSpline().getTooltip().setValueSuffix("°C"); 

	}*/

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

	public Vinventory getObjForm() {
		return objForm;
	}

	public void setObjForm(Vinventory objForm) {
		this.objForm = objForm;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public Integer getGt50() {
		return gt50;
	}

	public void setGt50(Integer gt50) {
		this.gt50 = gt50;
	}

	public Integer getGt25() {
		return gt25;
	}

	public void setGt25(Integer gt25) {
		this.gt25 = gt25;
	}

	public Integer getLt25() {
		return lt25;
	}

	public void setLt25(Integer lt25) {
		this.lt25 = lt25;
	}

	public Integer getTotalgt50() {
		return totalgt50;
	}

	public void setTotalgt50(Integer totalgt50) {
		this.totalgt50 = totalgt50;
	}

	public Integer getTotalgt25() {
		return totalgt25;
	}

	public void setTotalgt25(Integer totalgt25) {
		this.totalgt25 = totalgt25;
	}

	public Integer getTotallt25() {
		return totallt25;
	}

	public void setTotallt25(Integer totallt25) {
		this.totallt25 = totallt25;
	}

	public Window getWinStockvspagu() {
		return winStockvspagu;
	}

	public void setWinStockvspagu(Window winStockvspagu) {
		this.winStockvspagu = winStockvspagu;
	}

}
