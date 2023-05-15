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
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
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
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.DashboardDAO;
import com.sdd.caption.dao.MorgDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vinventory;
import com.sdd.caption.utils.AppUtils;

public class ChartStockEstimateVm {
	
	private Session zkSession = Sessions.getCurrent();
    private Muser oUser;
    private DashboardDAO oDao = new DashboardDAO();

	private String orderdate;
	private String persotype;
	private Date date;
	private Vinventory objForm;
	private String org;
	private Integer gt6m;
	private Integer gt3m;
	private Integer lt3m;
	private Integer totalgt6m;
	private Integer totalgt3m;
	private Integer totallt3m;
	private Boolean isIndex;
	
	@Wire
	private Charts chart;
	@Wire
	private Window winPredikstock;
	@Wire
	private Window winIndex;
	@Wire
	private Grid grid;
	@Wire
	private Div divChart;
	@Wire
	private Div divTable;
	@Wire
	private A aView;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("isIndex") Boolean isIndex, @ExecutionArgParam("date") Date dateback)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		
		oUser = (Muser) zkSession.getAttribute("oUser");
		
		if (isIndex != null)
			this.isIndex = isIndex;
		else this.isIndex = false; 
		
		/*if (Executions.getCurrent().getParameter("id") != null) {
			isIndex = true;			
		} else isIndex = false;*/
		
		if (dateback != null)
			this.date = dateback;
		doReset();
		
		if (this.isIndex) {
			divTable.setVisible(false);
		} else {
			aView.setVisible(false);
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
								map.put("filterpredict", "estdays >= 180");
								map.put("title",
										"Estimasi Ketersediaan Stock Diatas 6 Bulan untuk Jenis " + data.getDescription());

								Window win = (Window) Executions.createComponents("/view/dashboard/chartstockestimatedetail.zul",
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
								map.put("filterpredict", "estdays between 90 and 180");
								map.put("title",
										"Estimasi Ketersediaan Stock antara 3 s/d 6 Bulan untuk Org " + data.getDescription());

								Window win = (Window) Executions.createComponents("/view/dashboard/chartstockestimatedetail.zul",
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
								map.put("filterpredict", "estdays <= 90");
								map.put("title",
										"Estimasi Ketersediaan Stock Dibawah 3 Bulan untuk Org " + data.getDescription());

								Window win = (Window) Executions.createComponents("/view/dashboard/chartstockestimatedetail.zul",
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
	public void doChartRefresh() {
			try {
				divChart.getChildren().clear();
				Charts chart = new Charts();
				chart.setTitle("Dashboard Estimasi Ketersediaan Stock Blankcard");
				chart.setType("bar");
				
				Integer gt6m_200 = 0;
				Integer gt3m_200 = 0;
				Integer lt3m_200 = 0;
				List<Vinventory> obj = new DashboardDAO().getStockEstimation();
				Map<String, Integer> map = new HashMap<>();
				for (Vinventory data: obj) {
					if (data.getOrg().startsWith("2")) {
						gt6m_200 += data.getA();
						gt3m_200 += data.getB();
						lt3m_200 += data.getC();
					} else {
						map.put(data.getOrg() + "gt6m", data.getA());
						map.put(data.getOrg() + "gt3m", data.getB());
						map.put(data.getOrg() + "lt3m", data.getC());
					}	
				}
				
				map.put("200" + "gt6m", gt6m_200);
				map.put("200" + "gt3m", gt3m_200);
				map.put("200" + "lt3m", lt3m_200);
				
				CategoryModel model = new DefaultCategoryModel();
				List<Vinventory> objList = new ArrayList<>();
				List<Morg> listOrg = new MorgDAO().listByFilter("0=0", "org");
				totalgt6m = 0;
				totalgt3m = 0;
				totallt3m = 0;
				
				for (Morg morg : listOrg) {	
					if (morg.getOrg().startsWith("2") && !morg.getOrg().equals("200"))
						continue;
					String org = morg.getOrg().startsWith("2") ? "200" :  morg.getOrg();
					String description = morg.getOrg().startsWith("2") ? "DERIVATIF" :  morg.getDescription();
					model.setValue("> 6 bulan", description, map.get(org + "gt6m"));
					model.setValue("3 s/d 6 bulan", description, map.get(org + "gt3m"));
					model.setValue("< 3 bulan", description, map.get(org + "lt3m"));
					Vinventory o = new Vinventory();
					o.setOrg(org);
					o.setDescription(description);
					o.setA(map.get(org + "gt6m"));
					o.setB(map.get(org + "gt3m"));
					o.setC(map.get(org + "lt3m"));
					objList.add(o);
					totalgt6m += o.getA();
					totalgt3m += o.getB();
					totallt3m += o.getC();
				}		
				
				if (!isIndex)
					grid.setModel(new ListModelList<Vinventory>(objList));
				
				chart.setModel(model);
				chart.getXAxis().setMin(0);
				chart.getXAxis().getTitle().setText("");
				chart.getYAxis().getTitle().setText("Jumlah Data");
				chart.getXAxis().setCrosshair(true);
				
				Tooltip tooltip = chart.getTooltip();
				tooltip.setHeaderFormat("<span style=\"font-size:10px\">{point.key}</span><table>");
				tooltip.setPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name} : </td>"
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
	
	@Command
	@NotifyChange("*")
	public void doView() {
		if (oUser != null) {
            try {
                if (oDao.usermenuChecker("muserpk = " + oUser.getMuserpk() + " and menupath = '/view/dashboard/chartstockestimate.zul'") > 0) {
                	Div divContent = (Div) winPredikstock.getParent().getParent().getParent().getParent().getParent();
            		divContent.getChildren().clear();		
            		Executions.createComponents("/view/dashboard/chartstockestimate.zul", divContent, null);
                } else {
                    Messagebox.show("Anda tidak punya kewenangan untuk mengakses modul ini", "Info", 1, "z-messagebox-icon z-messagebox-information");
                }
            }
            catch (Exception e) {
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

	public Integer getGt6m() {
		return gt6m;
	}

	public void setGt6m(Integer gt6m) {
		this.gt6m = gt6m;
	}

	public Integer getGt3m() {
		return gt3m;
	}

	public void setGt3m(Integer gt3m) {
		this.gt3m = gt3m;
	}

	public Integer getLt3m() {
		return lt3m;
	}

	public void setLt3m(Integer lt3m) {
		this.lt3m = lt3m;
	}

	public Integer getTotalgt6m() {
		return totalgt6m;
	}

	public void setTotalgt6m(Integer totalgt6m) {
		this.totalgt6m = totalgt6m;
	}

	public Integer getTotalgt3m() {
		return totalgt3m;
	}

	public void setTotalgt3m(Integer totalgt3m) {
		this.totalgt3m = totalgt3m;
	}

	public Integer getTotallt3m() {
		return totallt3m;
	}

	public void setTotallt3m(Integer totallt3m) {
		this.totallt3m = totallt3m;
	}

}
