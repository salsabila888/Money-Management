package com.sdd.caption.viewmodel;

import java.text.ParseException;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.domain.Tpinpaditem;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.model.TpinpaditemListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class IncomingPinpadDataVm {

	private TpinpaditemListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;

	private Tincoming obj;
	private Ttokenitem objProduct;
	private String productname;
	private Integer itemqty;

	private Boolean isSaved;
	private String productgroup;
	private String arg;
	private Mproducttype mproducttype;
	private String type;
	private String itemno;
	private String status;

	@Wire
	private Window winIncomingPinpadItem;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid, gridDetail;
	@Wire
	private Label lbTitle;
	@Wire
	private Div divRecord;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Tincoming tincoming, @ExecutionArgParam("isInqPinpad") final String isInqPinpad,
			@ExecutionArgParam("producttype") Mproducttype mproducttype, @ExecutionArgParam("type") final String type)
			throws ParseException {
		Selectors.wireComponents(view, this, false);

		if (mproducttype != null) {
			this.mproducttype = mproducttype;
			gridDetail.setVisible(false);
			productgroup = AppData.getProductgroupLabel(mproducttype.getProductgroupcode());
		} else {
			obj = tincoming;
			productgroup = AppData.getProductgroupLabel(obj.getProductgroup());
		}

		if (type != null) {
			this.type = type;
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (isInqPinpad != null && isInqPinpad.equals("Y")) {
			grid.setVisible(false);
			divRecord.setVisible(false);
		}

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tpinpaditem>() {

				@Override
				public void render(Row row, Tpinpaditem data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren().add(new Label(data.getItemno() != null ? data.getItemno() : "-"));
					status = "";
					if(data.getStatus().equals(AppUtils.STATUS_SERIALNO_OUTINVENTORY))
						status = "SIAP PRODUKSI";
					else
						status = AppData.getStatusLabel(data.getStatus());
					row.getChildren()
							.add(new Label(data.getStatus() != null ? status : "-"));
				}
			});
		}

		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (mproducttype != null) {
			filter = "mproducttypefk = " + mproducttype.getMproducttypepk();

			if (type != null) {
				if (type.equals("unused")) {
					filter += " and tpinpaditem.status = '" + AppUtils.STATUS_REPAIR_FAILED + "'";
				} else if (type.equals("outstanding")) {
					filter += " and tpinpaditem.status = '" + AppUtils.STATUS_SERIALNO_OUTINVENTORY + "'";
				}
			} else {
				filter += " and tpinpaditem.status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "'";
			}
		} else {
			filter = "tincomingfk = " + obj.getTincomingpk();
		}

		if (itemno != null && itemno.trim().length() > 0)
			filter += " and itemno = '" + itemno.trim().toUpperCase() + "'";

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpinpaditempk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpinpaditemListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winIncomingPinpadItem, isSaved);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException {
		doSearch();
	}

	public int getPageStartNumber() {
		return pageStartNumber;
	}

	public void setPageStartNumber(int pageStartNumber) {
		this.pageStartNumber = pageStartNumber;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public Tincoming getObj() {
		return obj;
	}

	public void setObj(Tincoming obj) {
		this.obj = obj;
	}

	public Ttokenitem getObjProduct() {
		return objProduct;
	}

	public void setObjProduct(Ttokenitem objProduct) {
		this.objProduct = objProduct;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public Integer getItemqty() {
		return itemqty;
	}

	public void setItemqty(Integer itemqty) {
		this.itemqty = itemqty;
	}

	public Boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public Paging getPaging() {
		return paging;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	public String getItemno() {
		return itemno;
	}

	public void setItemno(String itemno) {
		this.itemno = itemno;
	}

}
