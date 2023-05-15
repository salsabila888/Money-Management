package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.model.TdeliverydataListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class DeliveryDataVm {

	private TdeliverydataListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;

	private Tdelivery obj;
	private String nopaket;
	private String branchname;
	private String producttype;
	private String productcode;
	private String productname;
	private Boolean isSaved;
	private Map<Integer, Tdeliverydata> mapData;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Window winDeliverydata;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Button btnPaket;
	@Wire
	private Div divRecord, divButton;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tdelivery tdelivery, 
			@ExecutionArgParam("isInquiry") final String isInquiry) throws ParseException {
		Selectors.wireComponents(view, this, false);
		if (tdelivery.getProductgroup().equals(AppUtils.PRODUCTGROUP_CARD))
			btnPaket.setVisible(true);
		obj = tdelivery;
		doSearch();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
		
		if (isInquiry != null && isInquiry.equals("Y")) {
			grid.setVisible(false);
			divRecord.setVisible(false);
			divButton.setVisible(false);
		}

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tdeliverydata>() {

				@Override
				public void render(Row row, final Tdeliverydata data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren().add(new Label(data.getTpaketdata().getNopaket()));
					row.getChildren().add(new Label(data.getMproduct().getMproducttype().getProducttype()));
					row.getChildren().add(new Label(data.getMproduct().getProductcode()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
					row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(
							data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "0"));
					Button btnDetail = new Button("Detail");
					btnDetail.setAutodisable("self");
					btnDetail.setClass("btn btn-default btn-sm");
					btnDetail.setStyle("border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnDetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("tembossbranch", data.getTpaketdata().getTembossbranch());
							Window win = new Window();
							if (tdelivery.getIsproductphoto().equals("Y")) {
								map.put("obj", data.getTpaketdata().getTpaket().getTderivatifproduct());
								win = (Window) Executions.createComponents("/view/derivatif/derivatifdata.zul", null,
										map);
							} else if (tdelivery.getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
								map.put("obj", data.getTpaketdata().getTpaket().getTorder());
								win = (Window) Executions.createComponents("/view/order/tokendata.zul", null,
										map);
							} else if (tdelivery.getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
								if (data.getTpaketdata().getTpaket().getTrepairdlv() != null) {
									map.put("objDlv", data.getTpaketdata().getTpaket().getTrepairdlv());
									win = (Window) Executions.createComponents("/view/repair/repairitem.zul", null,
											map);
								} else {
									map.put("obj", data.getTpaketdata().getTpaket().getTpinpadorderproduct().getTorder());
									win = (Window) Executions.createComponents("/view/order/pinpaddata.zul", null,
											map);
								}
							} else if (tdelivery.getProductgroup().equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
								map.put("obj", data.getTpaketdata().getTpinmailerbranch());
								win = (Window) Executions.createComponents("/view/pinmailer/pinmailerdata.zul", null,
										map);
							} else if (tdelivery.getProductgroup().equals(AppUtils.PRODUCTGROUP_CARD)) {
								map.put("obj", data.getTpaketdata().getTembossbranch());
								if(data.getTdelivery().getCardno() != null)
									map.put("cardno", data.getTdelivery().getCardno());
								win = (Window) Executions.createComponents("/view/emboss/embossdata.zul", null,
										map);
							} else if (tdelivery.getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
								if (data.getTpaketdata().getTpaket().getTreturn() != null) {
									map.put("obj", data.getTpaketdata().getTpaket().getTreturn());
									win = (Window) Executions.createComponents("/view/return/returproductitem.zul", null,
											map);
								} else {
									map.put("obj", data.getTpaketdata().getTpaket().getTorder());
									win = (Window) Executions.createComponents("/view/order/orderitem.zul", null,
											map);
								}
							}
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(btnDetail);
				}
			});
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tdeliverydata.orderdate";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TdeliverydataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "tdeliveryfk = " + obj.getTdeliverypk();
		if (nopaket != null && nopaket.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "nopaket like '" + nopaket.trim().toUpperCase() + "%'";
		}
		if (branchname != null && branchname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "branchname like '" + branchname.trim().toUpperCase() + "%'";
		}
		if (producttype != null && producttype.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "producttype like '" + producttype.trim().toUpperCase() + "%'";
		}
		if (productcode != null && productcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "productcode like '" + productcode.trim().toUpperCase() + "%'";
		}
		if (productname != null && productname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "productname like '" + productname.trim().toUpperCase() + "%'";
		}
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	public void doClose() {
		isSaved = new Boolean(true);
		Event closeEvent = new Event("onClose", winDeliverydata, isSaved);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException {
		branchname = null;
		producttype = null;
		productcode = null;
		productname = null;
		doSearch();
	}

	@Command
	public void doAddPaket() {
		Map<String, Object> map = new HashMap<>();
		map.put("obj", obj);
		map.put("mapData", mapData);

		Window win = (Window) Executions.createComponents("/view/delivery/pakettodelivery.zul", null, map);
		win.setClosable(true);
		win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				Boolean isSaved = (Boolean) event.getData();
				if (isSaved != null && isSaved) {
					needsPageUpdate = true;
					refreshModel(pageStartNumber);
					BindUtils.postNotifyChange(null, null, DeliveryDataVm.this, "pageTotalSize");
				}
			}
		});
		win.doModal();
	}

	public Tdelivery getObj() {
		return obj;
	}

	public void setObj(Tdelivery obj) {
		this.obj = obj;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public String getNopaket() {
		return nopaket;
	}

	public void setNopaket(String nopaket) {
		this.nopaket = nopaket;
	}

	public Boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}

}
