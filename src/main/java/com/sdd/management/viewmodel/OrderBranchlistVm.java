package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
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
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.TorderReport1DW;
import com.sdd.caption.domain.TorderReport1O;
import com.sdd.caption.utils.AppUtils;

public class OrderBranchlistVm {
	private Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TorderDAO oDao = new TorderDAO();

	private String productgroup, filter, filter2, orderby, orderby2;
	private Integer month, year;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("MMM YYYY");
	private SimpleDateFormat datenormalFormatter = new SimpleDateFormat("YYYY-MM-dd");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Grid grid;
	@Wire
	private Combobox cbBranch;
	@Wire
	private Label cardTitle;
	@Wire
	private Column clName;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("argid") String argid)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.productgroup = argid;
		doReset();

		if (grid != null) {
			if (oUser.getMbranch().getBranchlevel() == 3) {
				grid.setRowRenderer(new RowRenderer<TorderReport1O>() {
					@Override
					public void render(Row row, final TorderReport1O data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						A aBranch = new A(data.getBranchname());
						aBranch.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("prodgrp", productgroup);
								map.put("sentid", data.getMbranchfk());
								map.put("sentname", data.getBranchname());
								map.put("sentregion", data.getMregionfk());
								if (month != null && year != null) {
									map.put("month", month);
									map.put("year", year);
								}
								map.put("sentordate", month);
								map.put("keypage", 1);
								map.put("sentordoutlet", data.getOrderoutlet());
								Window win = (Window) Executions
										.createComponents("/view/report/orderbranchlistbydate.zul", null, map);
								win.setWidth("60%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(aBranch);
						row.getChildren().add(new Label(data.getOrderoutlet()));
						row.getChildren().add(new Label(
								data.getJumlahorder() != null ? data.getJumlahorder().toString().trim() : "0"));
						row.getChildren().add(
								new Label(data.getJumlahitem() != null ? data.getJumlahitem().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getJumlahdiproses() != null ? data.getJumlahdiproses().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getJumlahdikirim() != null ? data.getJumlahdikirim().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getOutstanding() != null ? data.getOutstanding().toString().trim() : "0"));

						if (data.getJumlahdiproses() == null)
							data.setJumlahdiproses(0);
						if (data.getJumlahdikirim() == null)
							data.setJumlahdikirim(0);

						if (data.getJumlahdikirim() == 0 || data.getJumlahdikirim() == null) {
							row.getChildren().add(new Label("0%"));
						} else {
							BigDecimal percent = new BigDecimal(0);
							BigDecimal x = BigDecimal.valueOf(data.getJumlahdikirim());
							BigDecimal y = BigDecimal.valueOf(data.getJumlahitem());
							percent = (x.divide(y, 4, RoundingMode.HALF_UP));
							percent = percent.multiply(new BigDecimal(100));
							row.getChildren().add(new Label(new DecimalFormat("0.00").format(percent) + "%"));
						}
					}
				});
			} else {
				grid.setRowRenderer(new RowRenderer<TorderReport1DW>() {
					@Override
					public void render(Row row, final TorderReport1DW data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						A aBranch = new A(data.getBranchname());
						aBranch.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("prodgrp", productgroup);
								map.put("sentid", data.getMbranchfk());
								map.put("sentname", data.getBranchname());
								map.put("sentregion", data.getMregionfk());
								if (month != null && year != null) {
									map.put("month", month);
									map.put("year", year);
								}
								map.put("keypage", 1);
								map.put("sentordoutlet", null);
								Window win = (Window) Executions
										.createComponents("/view/report/orderbranchlistbydate.zul", null, map);
								win.setWidth("60%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(aBranch);
						row.getChildren().add(new Label(""));
						row.getChildren().add(new Label(
								data.getJumlahorder() != null ? data.getJumlahorder().toString().trim() : "0"));
						row.getChildren().add(
								new Label(data.getJumlahitem() != null ? data.getJumlahitem().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getJumlahdiproses() != null ? data.getJumlahdiproses().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getJumlahdikirim() != null ? data.getJumlahdikirim().toString().trim() : "0"));
						row.getChildren().add(new Label(
								data.getOutstanding() != null ? data.getOutstanding().toString().trim() : "0"));

						if (data.getJumlahdiproses() == null)
							data.setJumlahdiproses(0);
						if (data.getJumlahdikirim() == null)
							data.setJumlahdikirim(0);

						if (data.getJumlahdikirim() == 0 || data.getJumlahdikirim() == null) {
							row.getChildren().add(new Label("0%"));
						} else {
							BigDecimal percent = new BigDecimal(0);
							BigDecimal x = BigDecimal.valueOf(data.getJumlahdikirim());
							BigDecimal y = BigDecimal.valueOf(data.getJumlahitem());
							percent = (x.divide(y, 4, RoundingMode.HALF_UP));
							percent = percent.multiply(new BigDecimal(100));
							row.getChildren().add(new Label(new DecimalFormat("0.00").format(percent) + "%"));
						}
					}
				});
			}
		}

		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item = new Comboitem();
			item.setLabel(months[i]);
			item.setValue(i + 1);
			cbMonth.appendChild(item);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		if (oUser.getMbranch().getBranchlevel() > 1) {
			month = Calendar.getInstance().get(Calendar.MONTH) + 1;
			year = Calendar.getInstance().get(Calendar.YEAR);
		} else {
			month = Calendar.getInstance().get(Calendar.MONTH) + 1;
			year = Calendar.getInstance().get(Calendar.YEAR);
		}
		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (oUser != null) {
				if (month != null && year != null) {
					Integer fday = 1;
					String dd = "";
					String md = "";

					if (fday.toString().length() < 2) {
						dd = "0" + fday.toString();
					}

					if (month.toString().length() < 2) {
						md = "0" + month.toString();
					} else {
						md = month.toString();
					}
					String sldate = year + "-" + md + "-" + dd;

					Calendar cal = Calendar.getInstance();
					cal.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(dd + "/" + md + "/" + year));
					cal.add(Calendar.MONTH, 1);
					cal.add(Calendar.DATE, -1);
					Date edt = cal.getTime();
					String eldate = datenormalFormatter.format(edt);

					if (oUser.getMbranch() != null) {
						// div
						if (oUser.getMbranch().getBranchlevel() == 1) {
							filter = "productgroup = '" + productgroup + "' and orderdate between '" + sldate
									+ "' and '" + eldate + "'";
							filter2 = "productgroup = '" + productgroup + "' and status = 'D29' and orderdate between '"
									+ sldate + "' and '" + eldate + "'";
							orderby = "branchname";
							orderby2 = "branchname";
						}

						// wil
						else if (oUser.getMbranch().getBranchlevel() == 2) {
							filter = "branchlevel > 1 and productgroup = '" + productgroup + "' and mregionfk = "
									+ oUser.getMbranch().getMregion().getMregionpk() + " and orderdate between '"
									+ sldate + "' and '" + eldate + "'";
							filter2 = "branchlevel > 1 and productgroup = '" + productgroup + "' and mregionfk = "
									+ oUser.getMbranch().getMregion().getMregionpk()
									+ " and status = 'D29' and orderdate between '" + sldate + "' and '" + eldate + "'";
							orderby = "branchname";
							orderby2 = "branchname";
						}

						// cab
						else if (oUser.getMbranch().getBranchlevel() == 3) {
							filter = "branchlevel = 3 and productgroup = '" + productgroup + "' and mbranchfk = "
									+ oUser.getMbranch().getMbranchpk() + " and orderdate between '" + sldate
									+ "' and '" + eldate + "'";
							filter2 = "branchlevel = 3 and productgroup = '" + productgroup + "' and mbranchfk = "
									+ oUser.getMbranch().getMbranchpk() + " and status = 'D29' and orderdate between '"
									+ sldate + "' and '" + eldate + "'";
							orderby = "orderoutlet";
							orderby2 = "orderoutlet";
						}
					}

					if (oUser.getMbranch().getBranchlevel() == 1) {
						cardTitle.setValue("Daftar Order Wilayah / " + datelocalFormatter.format(edt));
						clName.setLabel("Nama Wilayah");
					} else if (oUser.getMbranch().getBranchlevel() == 2) {
						cardTitle.setValue("Daftar Order Cabang / " + datelocalFormatter.format(edt));
						clName.setLabel("Nama Cabang");
					} else if (oUser.getMbranch().getBranchlevel() == 3) {
						cardTitle.setValue("Daftar Order Cabang / " + datelocalFormatter.format(edt));
						clName.setLabel("Nama Cabang");
					}
				}
				refreshModel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			if (oUser.getMbranch().getBranchlevel() == 3) {
				List<TorderReport1O> objList = new ArrayList<>();
				objList = oDao.listPagingreport1O(filter, filter2, orderby, orderby2);
				grid.setModel(new ListModelList<>(objList));
			} else {
				List<TorderReport1DW> objList = new ArrayList<>();
				objList = oDao.listPagingreport1DW(filter, filter2, orderby, orderby2);
				grid.setModel(new ListModelList<>(objList));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

//	@Command
//	@NotifyChange("*")
//	public void doBack() {									
//		Map<String, Object> map = new HashMap<>();
//		map.put("date", date);
//		map.put("obj", obj);
//		map.put("objproduct", objproduct);
//		
//		Div divContent = (Div) winOrderPersoBranch.getParent();
//		divContent.getChildren().clear();
//		Executions.createComponents("/view/dashboard/chartordervspersoregion.zul", divContent, map);
//	}
//
//	@Command
//	@NotifyChange("*")
//	public void doSearch() {
//		try {
//			doChartRefresh();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

//	@Command
//	@NotifyChange("*")
//	public void doChartRefresh() {
//		if (date != null) {
//			try {
//				Map<String, Integer> map = new HashMap<>();
//				chart.setTitle("Dashboard Order VS Produksi Jenis Kartu " + objproduct.getOrg() + " " + objregion.getRegionname());
//				chart.setSubtitle("Periode " + datelocalFormatter.format(date));
//				
//				List<Vgroupbybranch> listOrder = new DashboardDAO().listOrdOnOrdVsProdBranch(dateFormatter.format(date), objproduct.getOrg(), objregion.getMregionpk());
//				for (Vgroupbybranch data: listOrder) {
//					map.put(data.getMbranchpk() + "ord", data.getTotal());	
//				}
//				List<Vgroupbybranch> listProd = new DashboardDAO().listProdOnOrdVsProdBranch(dateFormatter.format(date), objproduct.getOrg(), objregion.getMregionpk());
//				for (Vgroupbybranch data: listProd) {
//					map.put(data.getMbranchpk() + "prod", data.getTotal());	
//				}
//				
//				CategoryModel model = new DefaultCategoryModel();
//				List<Vgroupbybranchdata> objList = new ArrayList<>();
//				totalorder = 0;
//				totalprod = 0;
//				for (Vgroupbybranch datamodel: listOrder) {
//					model.setValue("Order", datamodel.getBranchname(), map.get(datamodel.getMbranchpk() + "ord"));
//					model.setValue("Produksi", datamodel.getBranchname(), map.get(datamodel.getMbranchpk() + "prod"));	
//					Vgroupbybranchdata o = new Vgroupbybranchdata();
//					o.setMbranchpk(datamodel.getMbranchpk());
//					o.setBranchname(datamodel.getBranchname());
//					o.setTotalperso(map.get(datamodel.getMbranchpk() + "ord"));
//					o.setTotaldeliv(map.get(datamodel.getMbranchpk() + "prod"));
//					objList.add(o);
//					totalorder += (o.getTotalperso() != null ? o.getTotalperso() : 0);
//					totalprod += (o.getTotaldeliv() != null ? o.getTotaldeliv() : 0);
//				}
//				grid.setModel(new ListModelList<Vgroupbybranchdata>(objList));
//				
//				chart.setModel(model);
//				chart.getXAxis().setMin(0);
//				chart.getXAxis().getTitle().setText("Cabang");
//				chart.getYAxis().getTitle().setText("Jumlah Data");
//				chart.getXAxis().setCrosshair(true);
//				
//				Tooltip tooltip = chart.getTooltip();
//				tooltip.setHeaderFormat("<span style=\"font-size:10px\">{point.key}</span><table>");
//				tooltip.setPointFormat("<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td>"
//					+ "<td style=\"padding:0\"><b>{point.y}</b></td></tr>");
//				tooltip.setFooterFormat("</table>");
//				tooltip.setShared(true);
//				tooltip.setUseHTML(true);
//				
//				chart.getPlotOptions().getColumn().setPointPadding(0.2);
//				chart.getPlotOptions().getColumn().setBorderWidth(0);
//				
//				chart.setTheme(Theme.DEFAULT);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}

//	@Command
//	@NotifyChange("*")
//	public void doReset() {
////		if (date == null)
////			date = new Date();
//		doSearch();
//	}

}