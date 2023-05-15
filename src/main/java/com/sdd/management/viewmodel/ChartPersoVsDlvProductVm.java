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
import org.zkoss.chart.Series;
import org.zkoss.chart.YAxis;
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
import com.sdd.caption.domain.Vdeliverytotal;
import com.sdd.caption.domain.Vpersodeliv;
import com.sdd.caption.domain.Vpersodelivdata;

public class ChartPersoVsDlvProductVm {

	private DashboardDAO oDao = new DashboardDAO();

	private String orderdate;
	private String persotype;
	private Date date;
	private Vpersodelivdata obj;
	private Integer totalperso;
	private Integer totaldeliv;
	private Vpersodeliv objForm;
	private List<Vdeliverytotal> listData;
	private String[] catorg;
	private String[] catdescription;
	private Double[] catperso;
	private Double[] catdeliv;
	private List<String> listProduct = new ArrayList<>();
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd MMM yyyy");

	@Wire
	private Window winPersodelivproductcode;
	@Wire
	private Grid grid;
	@Wire
	private Div divChart;

	protected Object vperso;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("date") Date dateback, 
			@ExecutionArgParam("obj") Vpersodelivdata objarg)
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

						Div divContent = (Div) winPersodelivproductcode.getParent();
						divContent.getChildren().clear();
						Executions.createComponents("/view/dashboard/chartpersovsdlvregion.zul", divContent, map);
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
				List<Vpersodelivdata> objList = new ArrayList<>();
				
				for (int i = 0; i < catorg.length; i++) {
					Vpersodelivdata obj = new Vpersodelivdata();
					obj.setOrg(catorg[i]);
					obj.setDescription(catdescription[i]);
					obj.setTotalperso(catperso[i].intValue());
					obj.setTotaldeliv(catdeliv[i].intValue());
					objList.add(obj);
				}
				grid.setModel(new ListModelList<Vpersodelivdata>(objList));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doChartRefresh() {
		if (date != null) {
			try {
				listData = oDao.listProdOnProdVsDlvProcod(dateFormatter.format(date), obj.getOrg());
				catorg = new String[listData.size()];
				catdescription = new String[listData.size()];
				int i = 0;				
				for (Vdeliverytotal obj: listData) {
					listProduct.add(obj.getId());
					catorg[i] = obj.getId();
					i++;
				}
				
				divChart.getChildren().clear();
				Charts chart = new Charts();
				chart.setTitle("Dashboard Perso VS Delivery Periode Harian Berdasarkan Jenis Kartu");
				chart.setSubtitle("Periode " + dateLocalFormatter.format(date) + " Org " + obj.getDescription());
				
				chart.getXAxis().setCategories(catorg);
				chart.getXAxis().setCrosshair(true);

				YAxis yAxis1 = chart.getYAxis();
				yAxis1.getLabels().setStyle("color: " + chart.getColors().get(4).stringValue());
				yAxis1.setTitle("Jumlah Data");

				chart.getTooltip().setShared(true);
				
				initSeries(chart);
				
				divChart.appendChild(chart);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initSeries(Charts chart) throws Exception {
		try {
			/*List<Vdeliverytotal> objList = oDao.listProdOnProdVsDlvProcod(dateFormatter.format(date), org);*/
			catperso = new Double[listData.size()];
			int i = 0;
			totalperso = 0;
			for (Vdeliverytotal obj : listData) {
				catperso[i++] = new Double(obj.getTotaldata());
				totalperso += obj.getTotaldata();
			}
			Series prod = new Series("Produksi");
			prod.setName("Produksi");
			prod.setType("column");
			prod.setData(catperso);
			chart.addSeries(prod);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Map<String, Vdeliverytotal> mapDlv = new HashMap<>();
			List<Vdeliverytotal> objList = oDao.listDlvOnProdVsDlvProcod(dateFormatter.format(date), obj.getOrg());
			catdeliv = new Double[listData.size()];
			int i = 0;
			totaldeliv = 0;
			for (Vdeliverytotal obj : objList) {
				mapDlv.put(obj.getId(), obj);							
			}
			
			for (Vdeliverytotal obj : listData) {
				Vdeliverytotal datadlv = mapDlv.get(obj.getId());
				if (datadlv != null) {
					catdeliv[i++] = new Double(datadlv.getTotaldata());
					totaldeliv += obj.getTotaldata();
				} else {
					catdeliv[i++] = new Double(0);
				}
			}
			
			Series dlv = new Series("Delivery");
			dlv.setName("Delivery");
			dlv.setType("spline");
			dlv.setData(catdeliv);
			chart.addSeries(dlv);
			//dlv.setColor(new Color("#FFFFFF")); //Warna Gue :p
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doBack() {									
		Map<String, Object> map = new HashMap<>();
		map.put("date", date);
		
		Div divContent = (Div) winPersodelivproductcode.getParent();
		divContent.getChildren().clear();
		Executions.createComponents("/view/dashboard/chartpersovsdlv.zul", divContent, map);
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

	public Integer getTotalperso() {
		return totalperso;
	}

	public void setTotalperso(Integer totalperso) {
		this.totalperso = totalperso;
	}

	public Integer getTotaldeliv() {
		return totaldeliv;
	}

	public void setTotaldeliv(Integer totaldeliv) {
		this.totaldeliv = totaldeliv;
	}

	public Vpersodelivdata getObj() {
		return obj;
	}

	public void setObj(Vpersodelivdata obj) {
		this.obj = obj;
	}	

}
