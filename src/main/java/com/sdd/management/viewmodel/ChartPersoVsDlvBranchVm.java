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
import com.sdd.caption.domain.Vgroupbybranch;
import com.sdd.caption.domain.Vgroupbybranchdata;
import com.sdd.caption.domain.Vgroupbyregiondata;
import com.sdd.caption.domain.Vpersodelivdata;

public class ChartPersoVsDlvBranchVm {

	private DashboardDAO oDao = new DashboardDAO();

	private String orderdate;
	private String persotype;
	private Date date;
	private Integer totalperso;
	private Integer totaldeliv;
	private Vpersodelivdata obj;
	private Vpersodelivdata objproduct;
	private Vgroupbyregiondata objregion;
	private List<Vgroupbybranch> listProd;
	private List<Vgroupbybranch> listDlv;
	private Integer[] catpk;
	private String[] catmodel;
	private Double[] catperso;
	private Double[] catdeliv;
	
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
			@ExecutionArgParam("obj") Vpersodelivdata objarg, @ExecutionArgParam("objproduct") Vpersodelivdata objproductarg, @ExecutionArgParam("objregion") Vgroupbyregiondata objregionarg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		
		if (dateback != null)
			this.date = dateback;
		this.obj = objarg;
		this.objproduct = objproductarg;
		this.objregion = objregionarg;
		doReset();
		
		grid.setRowRenderer(new RowRenderer<Vgroupbybranchdata>() {

			@Override
			public void render(Row row, final Vgroupbybranchdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				A a = new A(data.getBranchname());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("date", date);
						map.put("obj", obj);
						map.put("objproduct", objproduct);
						map.put("objregion", objregion);
						map.put("objbranch", data);

						Div divContent = (Div) winPersodelivproductcode.getParent();
						divContent.getChildren().clear();
						Executions.createComponents("/view/dashboard/chartpersovsdlvkln.zul", divContent, map);
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
				List<Vgroupbybranchdata> objList = new ArrayList<>();				
				for (int i = 0; i < catmodel.length; i++) {
					Vgroupbybranchdata obj = new Vgroupbybranchdata();
					obj.setMbranchpk(catpk[i]);
					obj.setBranchname(catmodel[i]);
					obj.setTotalperso(catperso[i].intValue());
					obj.setTotaldeliv(catdeliv[i].intValue());
					objList.add(obj);
				}
				grid.setModel(new ListModelList<Vgroupbybranchdata>(objList));
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
				listProd = oDao.listProdOnProdVsDlvBranch(dateFormatter.format(date), objproduct.getOrg(), objregion.getMregionpk());
				catpk = new Integer[listProd.size()];				
				catmodel = new String[listProd.size()];		
				int i = 0;				
				for (Vgroupbybranch obj: listProd) {
					catpk[i] = obj.getMbranchpk();
					catmodel[i] = obj.getBranchname();
					i++;
				}
				
				divChart.getChildren().clear();
				Charts chart = new Charts();
				chart.setTitle("Dashboard Perso VS Delivery Periode Harian Berdasarkan KLN");
				chart.setSubtitle("Periode " + dateLocalFormatter.format(date) + "\n Jenis Produk " + objproduct.getOrg() + " Cabang " + objregion.getRegionname());
				
				chart.getXAxis().setCategories(catmodel);
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
			catperso = new Double[listProd.size()];
			int i = 0;
			totalperso = 0;
			for (Vgroupbybranch obj : listProd) {
				catperso[i++] = new Double(obj.getTotal());
				totalperso += obj.getTotal();
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
			Map<Integer, Vgroupbybranch> mapDlv = new HashMap<>();
			listDlv = oDao.listDlvOnProdVsDlvBranch(dateFormatter.format(date), objproduct.getOrg(), objregion.getMregionpk());
			catdeliv = new Double[listProd.size()];
			int i = 0;
			totaldeliv = 0;
			for (Vgroupbybranch obj : listDlv) {
				mapDlv.put(obj.getMbranchpk(), obj);							
			}
			
			for (Vgroupbybranch obj : listProd) {
				Vgroupbybranch datadlv = mapDlv.get(obj.getMbranchpk());
				if (datadlv != null) {
					catdeliv[i++] = new Double(datadlv.getTotal());
					totaldeliv += obj.getTotal();
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
		map.put("obj", obj);
		map.put("objproduct", objproduct);
		
		Div divContent = (Div) winPersodelivproductcode.getParent();
		divContent.getChildren().clear();
		Executions.createComponents("/view/dashboard/chartpersovsdlvregion.zul", divContent, map);
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

	public Vpersodelivdata getObjproduct() {
		return objproduct;
	}

	public void setObjproduct(Vpersodelivdata objproduct) {
		this.objproduct = objproduct;
	}	
	
}
