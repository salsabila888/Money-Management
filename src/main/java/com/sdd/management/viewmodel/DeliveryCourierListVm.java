package com.sdd.caption.viewmodel;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
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
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverycourier;
import com.sdd.caption.model.TdeliverycourierListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class DeliveryCourierListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer year;
	private Integer month;
	private String status;
	private Mcouriervendor mcouriervendor;
	private Date processtime;
	private String vendorcode;
	private String vendorname;
	private String dlvcourierid;
	private String isurgent;

	private TdeliverycourierListModel model;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Combobox cbMonth, cbProducttype;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tdeliverycourier>() {

				@Override
				public void render(Row row, final Tdeliverycourier data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					A a = new A(data.getDlvcourierid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);
							map.put("isCourierlist", "Y");
							Window win = (Window) Executions.createComponents("/view/delivery/deliverylist.zul", null,
									map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getProcesstime())));
					row.getChildren().add(new Label(String.valueOf(data.getTotaldata())));
					row.getChildren().add(new Label(data.getMcouriervendor().getVendorcode()));
					row.getChildren()
							.add(new Label(data.getMcourier() != null ? data.getMcourier().getCouriername() : ""));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
					row.getChildren().add(new Label(data.getIsurgent().equals("Y") ? "Urgent" : "Regular"));

					Button btnPrint = new Button();
					btnPrint.setImage("/images/printer.png");
					btnPrint.setTooltiptext("Cetak Manifest");
					btnPrint.setAutodisable("self");
					btnPrint.setClass("btn btn-default btn-sm");
					btnPrint.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnPrint.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							List<Tdelivery> objlist = new TdeliveryDAO().listByFilter(
									"tdeliverycourier.tdeliverycourierpk = " + data.getTdeliverycourierpk(),
									"mbranch.branchname");
							Map<String, String> parameters = new HashMap<>();
							parameters.put("DLVID", data.getDlvcourierid());
							parameters.put("EXPEDISI", data.getMcouriervendor().getVendorname());
							parameters.put("PRODUCTGROUP", AppData.getProductgroupLabel(data.getProductgroup()));
							parameters.put("DELIVERYDATE", datetimeLocalFormatter.format(data.getProcesstime()));
							parameters.put("DLVTYPE", data.getIsurgent().equals("Y") ? "URGENT" : "REGULAR");
							System.out.println(oUser.getMbranch().getBranchcode());
							parameters.put("BRANCHCODE", oUser.getMbranch().getBranchcode());
							parameters
									.put("URGENTIMG",
											data.getIsurgent().equals("Y")
													? Executions.getCurrent().getDesktop().getWebApp()
															.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH
																	+ "/urgent_logo.png")
													: null);
							zkSession.setAttribute("objList", objlist);
							zkSession.setAttribute("parameters", parameters);
							zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
									.getRealPath(SysUtils.JASPER_PATH + "/dlvcouriermanifest.jasper"));
							Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");

						}
					});
					row.getChildren().add(btnPrint);
				}

			});
		}

		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item = new Comboitem();
			item.setLabel(months[i]);
			item.setValue(i + 1);
		}

		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (processtime != null) {
			filter = "Date(tdeliverycourier.processtime) = '" + dateFormatter.format(processtime)
					+ "' and courierbranchpool = '" + oUser.getMbranch().getBranchid() + "'";
			if (dlvcourierid != null && dlvcourierid.length() > 0)
				filter += " and tdeliverycourier.dlvcourierid like '%" + dlvcourierid.trim().toUpperCase() + "%'";
			if (mcouriervendor != null)
				filter += " and tdeliverycourier.mcouriervendorfk = " + mcouriervendor.getMcouriervendorpk();
			if (isurgent != null && isurgent.length() > 0)
				filter += " and tdeliverycourier.isurgent like '%" + isurgent.trim().toUpperCase() + "%'";

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		processtime = new Date();
		cbProducttype.setValue(null);
		mcouriervendor = null;
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tdeliverycourierpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TdeliverycourierListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	public ListModelList<Mcouriervendor> getMcouriervendormodel() {
		ListModelList<Mcouriervendor> lm = null;
		try {
			lm = new ListModelList<Mcouriervendor>(AppData.getMcouriervendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Mcouriervendor getMcouriervendor() {
		return mcouriervendor;
	}

	public void setMcouriervendor(Mcouriervendor mcouriervendor) {
		this.mcouriervendor = mcouriervendor;
	}

	public Date getProcesstime() {
		return processtime;
	}

	public void setProcesstime(Date processtime) {
		this.processtime = processtime;
	}

	public String getVendorcode() {
		return vendorcode;
	}

	public void setVendorcode(String vendorcode) {
		this.vendorcode = vendorcode;
	}

	public String getVendorname() {
		return vendorname;
	}

	public void setVendorname(String vendorname) {
		this.vendorname = vendorname;
	}

	public String getDlvcourierid() {
		return dlvcourierid;
	}

	public void setDlvcourierid(String dlvcourierid) {
		this.dlvcourierid = dlvcourierid;
	}

	public String getIsurgent() {
		return isurgent;
	}

	public void setIsurgent(String isurgent) {
		this.isurgent = isurgent;
	}

}
