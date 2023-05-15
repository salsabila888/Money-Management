package com.sdd.caption.viewmodel;

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
import org.zkoss.chart.Legend;
import org.zkoss.chart.YAxis;
import org.zkoss.chart.model.CategoryModel;
import org.zkoss.chart.model.DefaultCategoryModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vsumbranchstockbyorg;
import com.sdd.caption.domain.Vsumoutletstockbyorg;
import com.sdd.caption.utils.AppData;

public class ChartBranchStockCardVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TbranchstockDAO oDao = new TbranchstockDAO();
	private String filter;
	private String arg;
	private int branchlevel;
	private String productgroup;
	
	private Map<String, String> mapOrg = new HashMap<String, String>();

	@Wire
	private Charts chart;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();
		this.arg = arg;
		productgroup = AppData.getProductgroupLabel(arg);
		
		for(Morg org : AppData.getMorg()) {
			mapOrg.put(org.getOrg(), org.getDescription());
		}

		generateChart();
	}

	@SuppressWarnings("deprecation")
	@NotifyChange("*")
	public void generateChart() {
		try {

			CategoryModel model = new DefaultCategoryModel();
			if (branchlevel == 2) {
				filter = "TBRANCHSTOCK.PRODUCTGROUP in ('01', '09') AND MREGIONFK = "
						+ oUser.getMbranch().getMregion().getMregionpk() + " and productorg is not null";
			} else if (branchlevel == 3) {
				filter = "TBRANCHSTOCK.PRODUCTGROUP in ('01', '09') AND MBRANCHFK = "
						+ oUser.getMbranch().getMbranchpk() + " and productorg is not null";
			}
			
			if (branchlevel == 2) {
				List<Vsumbranchstockbyorg> objList = oDao.getSumBranchStockByOrg(filter);
				for (Vsumbranchstockbyorg data : objList) {
					model.setValue(data.getBranchname(), mapOrg.get(data.getProductorg().trim()), data.getStockcabang());
				}
			} else if (branchlevel == 3) {
				List<Vsumoutletstockbyorg> objList = oDao.getSumOutletStockByOrg(filter);
				for (Vsumoutletstockbyorg data : objList) {
					model.setValue(data.getOutlet(), mapOrg.get(data.getProductorg().trim()), data.getStockcabang());
				}
			}

			chart.setModel(model);
			chart.getXAxis().setTitle("");

			YAxis yAxis = chart.getYAxis();
			yAxis.setMin(0);
			yAxis.setTitle("Jumlah Stock");
			yAxis.getTitle().setAlign("high");
			yAxis.getLabels().setOverflow("justify");

			chart.getPlotOptions().getBar().getDataLabels().setEnabled(true);

			Legend legend = chart.getLegend();
			legend.setLayout("vertical");
			legend.setAlign("right");
			legend.setVerticalAlign("top");
			legend.setX(-40);
			legend.setY(100);
			legend.setFloating(true);
			legend.setBorderWidth(1);
			legend.setShadow(true);

			chart.getCredits().setEnabled(false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doView() {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("arg", arg);
			map.put("isIndex", "Y");
			Window win = new Window();
			win = (Window) Executions.createComponents("/view/report/reportproductstock.zul", null, map);
			win.setWidth("90%");
			win.setClosable(true);
			win.doModal();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

}
