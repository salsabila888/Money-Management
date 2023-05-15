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

public class ChartOrderVsPersoVm {
	
	private Session zkSession = Sessions.getCurrent();
    private Muser oUser;
	private DashboardDAO oDao = new DashboardDAO();

	private String orderdate;
	private String persotype;
	private Date date;
	private Integer totalorder;
	private Integer totalprod;
	private Vpersodeliv objForm;
	private Boolean isIndex;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd MMM yyyy");

	@Wire
	private Charts chart;
	@Wire
	private Window winOrderperso;
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
		
		/*if (Executions.getCurrent().getParameter("id") != null)
			isIndex = true;
		else isIndex = false;*/
		
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

							Div divContent = (Div) winOrderperso.getParent();
							divContent.getChildren().clear();
							Executions.createComponents("/view/dashboard/chartordervspersoproduct.zul", divContent, map);
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
		/*Div divContent = (Div) winOrderperso.getParent().getParent().getParent().getParent().getParent();
		divContent.getChildren().clear();	
		Map<String, Object> map = new HashMap<>();
		map.put("date", date);
		Executions.createComponents("/view/dashboard/chartordervsperso.zul", divContent, map);	*/
		
		if (oUser != null) {
            try {
                if (oDao.usermenuChecker("muserpk = " + oUser.getMuserpk() + " and menupath = '/view/dashboard/chartordervsperso.zul'") > 0) {
                	Div divContent = (Div) winOrderperso.getParent().getParent().getParent().getParent().getParent();
            		divContent.getChildren().clear();	
            		Map<String, Object> map = new HashMap<>();
            		map.put("date", date);
            		Executions.createComponents("/view/dashboard/chartordervsperso.zul", divContent, map);	
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
				Map<String, Integer> map = new HashMap<>();
				chart.setTitle("Dashboard Order VS Produksi");
				chart.setSubtitle("Periode " + datelocalFormatter.format(date));
				
				Integer order200 = 0;
				List<Vpersodeliv> oList = new DashboardDAO().listOrdOnOrdVsProd(dateFormatter.format(date));
				for (Vpersodeliv data: oList) {
					if (data.getOrg().startsWith("2")) {
						order200 += data.getTotaldata();
					} else {
						map.put(data.getOrg() + "ord", data.getTotaldata());
					} 
				}
				map.put("200" + "ord", order200);
				
				Integer perso200 = 0;
				List<Vpersodeliv> obj = new DashboardDAO().listProdOnOrdVsProd(dateFormatter.format(date));
				for (Vpersodeliv data: obj) {
					if (data.getOrg().startsWith("2")) {
						perso200 += data.getTotaldata();
					} else {
						map.put(data.getOrg() + "prod", data.getTotaldata());
					} 
				}
				map.put("200" + "prod", perso200);
				
				CategoryModel model = new DefaultCategoryModel();
				List<Vpersodelivdata> objList = new ArrayList<>();
				List<Morg> listOrg = new MorgDAO().listByFilter("0=0", "org");
				totalorder = 0;
				totalprod = 0;
				for (Morg morg : listOrg) {
					if (morg.getOrg().startsWith("2") && !morg.getOrg().equals("200"))
						continue;
					String org = morg.getOrg().startsWith("2") ? "200" :  morg.getOrg();
					String description = morg.getOrg().startsWith("2") ? "DERIVATIF" :  morg.getDescription();
					model.setValue("Order", description, map.get(org + "ord"));
					model.setValue("Produksi", description, map.get(org + "prod"));	
					Vpersodelivdata o = new Vpersodelivdata();
					o.setOrg(org);
					o.setDescription(description);
					o.setTotalperso(map.get(org + "ord"));
					o.setTotaldeliv(map.get(org + "prod"));
					objList.add(o);
					totalorder = totalorder + o.getTotalperso();
					totalprod += o.getTotaldeliv();
				}
				if (!isIndex)
					grid.setModel(new ListModelList<Vpersodelivdata>(objList));
								
				chart.setModel(model);
				chart.getXAxis().setMin(0);
				chart.getXAxis().getTitle().setText("Jenis");
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

}
