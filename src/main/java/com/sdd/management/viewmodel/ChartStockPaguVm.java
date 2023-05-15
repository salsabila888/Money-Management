package com.sdd.caption.viewmodel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlNativeComponent;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MbranchproductgroupDAO;
import com.sdd.caption.dao.MproductownerDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Mproductowner;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vinventory;
import com.sdd.caption.utils.AppData;

public class ChartStockPaguVm {

	private Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Date date;
	private Vinventory objForm;

	private Integer branchlevel, totalgt50, totalgt25, totallt25, gt50, gt25, lt25;

	private String filter, orderby, productgroup, orderdate, persotype, org;
	private Mproductgroup mproductgroup;
	private Mbranchproductgroup mbranchproductgroup;

	private List<Mproducttype> objList = new ArrayList<Mproducttype>();

	@Wire
	private Combobox cbProductgroup;
	@Wire
	private Div divChart, divChartcard, divTableDiv, divpagucard, divLegend, divhr;
	@Wire
	private Window winStockpagu;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();
		doReset();

	}

	@NotifyChange("*")
	public void getProduct() {
		try {
			List<Mbranchproductgroup> productgroupList = new MbranchproductgroupDAO().listNativeByFilter(
					"mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and productgroupcode != '09'",
					"mproductgroupfk desc");
			for (Mbranchproductgroup data : productgroupList) {
				productgroup = data.getMproductgroup().getProductgroupcode();
				Comboitem item = new Comboitem();
				item.setLabel(data.getMproductgroup().getProductgroup());
				if (data.getMproductgroup().getProductgroupcode().equals("01")) {
					item.setLabel("KARTU");
				}
				item.setValue(data.getMproductgroup().getProductgroupcode());
				cbProductgroup.appendChild(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			orderby = "laststock, producttype";
			filter = "productgroupcode = '" + productgroup.trim() + "' and mbranchfk = "
					+ oUser.getMbranch().getMbranchpk();
			objList = new MproducttypeDAO().listNativeByFilter(filter, orderby);
			if (objList.size() < 1) {
				filter = "productgroupcode = '" + productgroup.trim() + "'";
				objList = new MproducttypeDAO().listNativeByFilter(filter, orderby);
			}

			if (productgroup == "01" || "01".equals(productgroup) || productgroup == "09"
					|| "09".equals(productgroup)) {
				divChart.getChildren().clear();
				divhr.setVisible(false);
				divTableDiv.setVisible(true);
				Map<String, Object> map = new HashMap<>();
				map.put("isIndex", new Boolean(true));
				Executions.createComponents("/view/dashboard/stockpagucard.zul", divpagucard, map);
			} else {
				divChart.getChildren().clear();
				divTableDiv.setVisible(false);
				generateLegend();
				divhr.setVisible(true);
				generateChart();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateLegend() {
		try {
			divLegend.getChildren().clear();
			Div divRow = null;
			Div divCol = null;

			divRow = new Div();
			divRow.setClass("row-inline");
			divRow.setParent(winStockpagu);

			divCol = new Div();
			divCol.setClass("col-md-2");
			divCol.setParent(divRow);
			List<String> a = new ArrayList<>();
			for (Mproducttype data1 : objList) {
				a.add(data1.getProductgroupname().trim());
			}

			List<String> b = new ArrayList<>();
			for (String data2 : a) {
				if (!b.contains(data2))
					b.add(data2);
			}

			if (objList.size() > 0) {
				for (String legend : b) {
					divRow = new Div();
					divRow.setClass("row");
					divRow.setParent(winStockpagu);
					Div divGrid = new Div();
					divGrid.setParent(divCol);

					Div divColor = new Div();
					if (legend.equals("KARTU"))
						divColor.setClass("color p1");
					else if (legend.equals("TOKEN"))
						divColor.setClass("color p2");
					else if (legend.equals("PINPAD"))
						divColor.setClass("color p3");
					else if (legend.equals("SUPPLIES"))
						divColor.setClass("color p4");
					else if (legend.equals("PIN MAILER"))
						divColor.setClass("color p5");
					else if (legend.equals("KARTU NON REGULAR"))
						divColor.setClass("color p6");
					else if (legend.contains("WARKAT"))
						divColor.setClass("color p7");
//					else if (legend.contains("DEPOSITO"))
//						divColor.setClass("color p8");
					else if (legend.contains("TABUNGAN"))
						divColor.setClass("color p9");
					else if (legend.contains("CEK"))
						divColor.setClass("color p10");
					else if (legend.contains("BILYET GIRO"))
						divColor.setClass("color p11");
					else if (legend.contains("DEPOSITO"))
						divColor.setClass("color p12");
					else
						divColor.setClass("color p0");

					HtmlNativeComponent productname = new HtmlNativeComponent("Label");
					productname.setPrologContent(legend);
					productname.setClientAttribute("style",
							"margin: 0px; font-size:13.5px; font-weight: bold; cursor: default !important;");
					Separator separator1 = new Separator();
					productname.appendChild(separator1);
					Separator separator2 = new Separator();
					productname.appendChild(separator2);

					divGrid.appendChild(divColor);
					divGrid.appendChild(productname);
					divLegend.appendChild(divGrid);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generateChart() {
		try {
			divChart.getChildren().clear();
			String color = "";
			double percent = 0;
			String emptymsg = "No items match your search";

			Div divRow = null;
			Div divCol = null;

			divRow = new Div();
			divRow.setClass("row");
			divRow.setParent(winStockpagu);

			divCol = new Div();
			divCol.setClass("col-md-12");
			divCol.setParent(divRow);

			if (objList.size() > 0) {
				for (Mproducttype data : objList) {
					divRow = new Div();
					divRow.setClass("row");
					divRow.setParent(winStockpagu);
					Div divGrid = new Div();
					divGrid.setParent(divRow);

					HtmlNativeComponent nproducttype = new HtmlNativeComponent("p");
					nproducttype.setPrologContent(data.getProducttype().trim().toString());
					nproducttype.setClientAttribute("style",
							"text-align: center !important; font-size: 13px; font-weight: bold; overflow: hidden; width: 220px; margin: auto; padding: 13px 20px 7px 20px; white-space: nowrap; text-overflow: ellipsis;");
					nproducttype.setClientAttribute("title", data.getProducttype().toString());
					divGrid.appendChild(nproducttype);

					float x = (float) data.getLaststock();
					float y = (float) data.getStockmin();
					if (y != 0) {
						double z = (x / y) * 100;
						percent = Double.parseDouble(new DecimalFormat("#.##").format(z));
					} else {
						percent = Double.parseDouble(new DecimalFormat("#.##").format(0));
					}

					Div divCircle = new Div();
					if (data.getProductgroupname().trim().equals("KARTU"))
						divCircle.setClass("circlep1");
					else if (data.getProductgroupname().trim().equals("TOKEN"))
						divCircle.setClass("circlep2");
					else if (data.getProductgroupname().trim().equals("PINPAD"))
						divCircle.setClass("circlep3");
					else if (data.getProductgroupname().trim().equals("SURAT BERHARGA"))
						divCircle.setClass("circlep4");
					else if (data.getProductgroupname().trim().equals("SUPPLIES"))
						divCircle.setClass("circlep5");
					else if (data.getProductgroupname().trim().equals("PIN MAILER"))
						divCircle.setClass("circlep6");
					else if (data.getProductgroupname().trim().equals("KARTU NON REGULAR"))
						divCircle.setClass("circlep7");
					else if (data.getProductgroupname().trim().contains("WARKAT"))
						divCircle.setClass("circlep8");
					else if (data.getProductgroupname().trim().contains("TABUNGAN"))
						divCircle.setClass("circlep9");
					else if (data.getProductgroupname().trim().contains("BILYET GIRO"))
						divCircle.setClass("circlep10");
					else if (data.getProductgroupname().trim().contains("CEK"))
						divCircle.setClass("circlep11");
					else if (data.getProductgroupname().trim().contains("DEPOSITO"))
						divCircle.setClass("circlep12");
					else
						divCircle.setClass("circlep0");

					if (y != 0) {
						if (percent < 70.0)
							color = "background-color: #ff595e;";
						else if (percent >= 70.0 && percent < 100.0)
							color = "background-color: #ffca3a;";
						else if (percent >= 100) {
							color = "background-color: #8ac926;";
						}
					} else {
						color = "background-color: #f4f4f4;";
					}

					Div divColor = new Div();
					divColor.setStyle("border-top-left-radius: 75px; border-top-right-radius: 75px; " + color
							+ " width: 100%; height: 50%; ");
					HtmlNativeComponent npercentase = new HtmlNativeComponent("p");

					String a = String.valueOf(percent);
					int b = a.length();
					if (b > 6 && y != 0) {
						npercentase.setClientAttribute("style", "padding-top: 40px; font-size: 17px;");
					} else {
						npercentase.setClientAttribute("style", "padding-top: 35px; font-size: 27px;");
					}

					if (y != 0) {
						npercentase.setPrologContent(String.valueOf(percent) + "%");
					} else {
						npercentase.setPrologContent("0");
						npercentase.setClientAttribute("title", "Fill pagu stock to get the stock percentage!");
					}
					divColor.appendChild(npercentase);

					HtmlNativeComponent nstatistic = new HtmlNativeComponent("p");
					nstatistic.setClientAttribute("style", "padding-top: 7px; font-size: 15px;");
					nstatistic.setPrologContent(data.getLaststock().toString() + " / " + data.getStockmin());
					divColor.appendChild(nstatistic);

					Separator separator1 = new Separator();
					Separator separator2 = new Separator();
					Separator separator3 = new Separator();
					Separator separator4 = new Separator();

					divCircle.appendChild(divColor);
					divGrid.appendChild(divCircle);
					divGrid.appendChild(separator1);
					divGrid.appendChild(separator2);
					divGrid.appendChild(separator3);
					divGrid.appendChild(separator4);
					divChart.appendChild(divGrid);
				}
			} else {
				divRow = new Div();
				divRow.setClass("row");
				divRow.setParent(winStockpagu);

				Div divGrid = new Div();
				divGrid.setParent(divRow);

				HtmlNativeComponent emptydatap = new HtmlNativeComponent("p");
				emptydatap.setPrologContent(emptymsg);
				emptydatap.setClientAttribute("style",
						"text-align: center !important; font-size: 13px; font-weight: bold; overflow: hidden; width: 220px; margin: auto; padding: 0px 20px 5px 20px; white-space: nowrap; text-overflow: ellipsis;");
				divGrid.appendChild(emptydatap);
				divChart.appendChild(divGrid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		productgroup = "01";
		getProduct();
		cbProductgroup.setValue(AppData.getProductgroupLabel(productgroup.trim()));
		doSearch();
	}

	public Mproductgroup getMproductgroup() {
		return mproductgroup;
	}

	public void setMproductgroup(Mproductgroup mproductgroup) {
		this.mproductgroup = mproductgroup;
	}

	public Mbranchproductgroup getMbranchproductgroup() {
		return mbranchproductgroup;
	}

	public void setMbranchproductgroup(Mbranchproductgroup mbranchproductgroup) {
		this.mbranchproductgroup = mbranchproductgroup;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
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
		return winStockpagu;
	}

	public void setWinStockvspagu(Window winStockvspagu) {
		this.winStockpagu = winStockvspagu;
	}

}
