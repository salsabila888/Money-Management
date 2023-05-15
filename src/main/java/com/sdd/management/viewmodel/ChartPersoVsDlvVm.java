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
import org.zkoss.chart.ChartsEvent;
import org.zkoss.chart.Point;
import org.zkoss.chart.Series;
import org.zkoss.chart.YAxis;
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
import com.sdd.caption.domain.Vpersodeliv;
import com.sdd.caption.domain.Vpersodelivdata;

public class ChartPersoVsDlvVm {

	private Session zkSession = Sessions.getCurrent();
    private Muser oUser;
	private DashboardDAO oDao = new DashboardDAO();

	private String orderdate;
	private String persotype;
	private Date date;
	private Integer totalperso;
	private Integer totaldeliv;
	private Vpersodeliv objForm;
	private String[] catorg;
	private String[] catdescription;
	private Double[] catperso;
	private Double[] catdeliv;
	private Boolean isIndex;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd MMM yyyy");

	@Wire
	private Window winPersodeliv;
	@Wire
	private Grid grid;
	@Wire
	private Div divChart;
	@Wire
	private Div divTable;
	@Wire
	private A aView;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("isIndex") Boolean isIndex, 
			@ExecutionArgParam("date") Date dateback)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		
		oUser = (Muser) zkSession.getAttribute("oUser");
       if (isIndex != null)
			this.isIndex = isIndex;
		else this.isIndex = false;
		
		if (dateback != null)
			this.date = dateback;
		doReset();
		
		if (this.isIndex) {
			divTable.setVisible(false);			
		} else {
			aView.setVisible(false);
			grid.setRowRenderer(new RowRenderer<Vpersodelivdata>() {

				@Override
				public void render(Row row, final Vpersodelivdata data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					A a = new A(data.getDescription());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("date", date);
							map.put("obj", data);						

							Div divContent = (Div) winPersodeliv.getParent();
							divContent.getChildren().clear();
							Executions.createComponents("/view/dashboard/chartpersovsdlvproduct.zul", divContent, map);
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(data.getTotalperso() != null ? NumberFormat.getInstance().format(data.getTotalperso()) : "0"));
					row.getChildren().add(new Label(data.getTotaldeliv() != null ? NumberFormat.getInstance().format(data.getTotaldeliv()) : "0"));
				}
			});
		}		
	}
	
	@Command
	@NotifyChange("*")
	public void doView() {
		/*Div divContent = (Div) winPersodeliv.getParent().getParent().getParent().getParent().getParent();
		divContent.getChildren().clear();		
		Map<String, Object> map = new HashMap<>();
		map.put("date", date);
		Executions.createComponents("/view/dashboard/chartpersovsdlv.zul", divContent, map);*/	
		
		if (oUser != null) {
            try {
                if (oDao.usermenuChecker("muserpk = " + oUser.getMuserpk() + " and menupath = '/view/dashboard/chartpersovsdlv.zul'") > 0) {
                    Div divContent = (Div) winPersodeliv.getParent().getParent().getParent().getParent().getParent();
                    divContent.getChildren().clear();
                    HashMap<String, Date> map = new HashMap<String, Date>();
                    map.put("date", date);
                    Executions.createComponents("/view/dashboard/chartpersovsdlv.zul", divContent, map);
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
	public void doSearch() {
		if (date != null) {
			try {
				doChartRefresh();
				List<Vpersodelivdata> objList = new ArrayList<>();
				
				for (int i = 0; i < catorg.length; i++) {
					/*System.out.println(catorg[i]);*/
					Vpersodelivdata obj = new Vpersodelivdata();
					obj.setOrg(catorg[i]);
					obj.setDescription(catdescription[i]);
					obj.setTotalperso(catperso[i].intValue());
					obj.setTotaldeliv(catdeliv[i].intValue());
					objList.add(obj);
				}
				
				if (!isIndex)
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
				//List<Morg> listOrg = new MorgDAO().listByFilter("0=0", "org");
				List<Morg> listOrg = new MorgDAO().listByFilter("(org not like '2%' or org = '200')", "org");
				catorg = new String[listOrg.size()];
				catdescription = new String[listOrg.size()];
				int i = 0;
				for (Morg morg : listOrg) {
					catorg[i] = morg.getOrg();
					catdescription[i] = morg.getOrg().equals("200") ? "DERIVATIF" : morg.getDescription();
					i++;
				}
				
				divChart.getChildren().clear();
				Charts chart = new Charts();
				chart.setTitle("Dashboard Perso VS Delivery");
				chart.setSubtitle("Periode " + dateLocalFormatter.format(date));
				
				chart.getXAxis().setCategories(catdescription);
				chart.getXAxis().setCrosshair(true);

				YAxis yAxis1 = chart.getYAxis();
				yAxis1.getLabels().setStyle("color: " + chart.getColors().get(4).stringValue());
				yAxis1.setTitle("Jumlah Data");

				chart.getTooltip().setShared(true);

				initSeries(chart);
				
				chart.addEventListener("onPlotClick", new EventListener<ChartsEvent>() {

					@Override
					public void onEvent(ChartsEvent event) throws Exception {
						System.out.println("Test");
						Point point = event.getPoint();
						System.out.println(point.getY());
						System.out.println(event.getPointIndex());						
					}
				});
				
				divChart.appendChild(chart);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initSeries(Charts chart) throws Exception {
		try {
			List<Vpersodeliv> objList = oDao.listProdOnProdVsDlv(dateFormatter.format(date));
			//catperso = new Double[objList.size()];
			catperso = new Double[catorg.length];
			int i = 0;
			int i200 = -1;
			totalperso = 0;
			for (Vpersodeliv obj : objList) {				
				if (obj.getOrg().startsWith("2")) {
					if (i200 < 0)
						i200 = i;
					catperso[i200] = (catperso[i200] == null ? 0 : catperso[i200]) + new Double(obj.getTotaldata());
					totalperso += obj.getTotaldata();	
					if (obj.getOrg().equals("200"))
						i++;
				} else {
					catperso[i] = new Double(obj.getTotaldata());
					totalperso += obj.getTotaldata();
					i++;
				}				
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
			List<Vpersodeliv> objList = oDao.listDlvOnProdVsDlv(dateFormatter.format(date));
			//catdeliv = new Double[objList.size()];
			catdeliv = new Double[catorg.length];
			int i = 0;
			int i200 = -1;
			totaldeliv = 0;
			for (Vpersodeliv obj : objList) {
				/*catdeliv[i++] = new Double(obj.getTotaldata());
				totaldeliv += obj.getTotaldata();*/				
				if (obj.getOrg().startsWith("2")) {
					if (i200 < 0)
						i200 = i;
					catdeliv[i200] = (catdeliv[i200] == null ? 0 : catdeliv[i200]) + new Double(obj.getTotaldata());
					totaldeliv += obj.getTotaldata();
					if (obj.getOrg().equals("200"))
						i++;
				} else {
					catdeliv[i] = new Double(obj.getTotaldata());
					totaldeliv += obj.getTotaldata();
					i++;
				}				
			}
			Series dlv = new Series("Delivery");
			dlv.setName("Delivery");
			dlv.setType("spline");
			dlv.setData(catdeliv);
			//dlv.setColor(new Color("#FFFFFF")); //Warna Gue :p
			chart.addSeries(dlv);
		} catch (Exception e) {
			e.printStackTrace();
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

}
