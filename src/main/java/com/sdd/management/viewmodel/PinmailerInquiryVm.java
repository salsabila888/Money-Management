package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.A;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tpinmailerdata;
import com.sdd.caption.model.TpinmailerdataListModel;
import com.sdd.utils.SysUtils;

public class PinmailerInquiryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String cardno;
	private Date orderdate;

	private TpinmailerdataListModel model;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		
		paging.addEventListener("onPaging", new EventListener() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tpinmailerdata>() {

				@Override
				public void render(Row row, final Tpinmailerdata data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					A a = new A(data.getCardno());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("pm", data);
							map.put("isPM", "Y");
							Window win = (Window) Executions.createComponents("/view/opr/inquirydetail.zul", null, map);
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(data.getName()));
					row.getChildren().add(new Label(data.getProductcode()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
					row.getChildren()
							.add(new Label(data.getTpinmailerbranch().getMbranch() != null
									? data.getTpinmailerbranch().getMbranch().getBranchname()
									: ""));

				}
			});
		}
		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (cardno != null || orderdate != null) {
			if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 700)
				filter = "";
			else if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) < 700
					&& Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600)
				filter = "mbranch.mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
			else
				filter = "tpinmailerbranch.branchid = '" + oUser.getMbranch().getBranchid() + "'";
			
			if (cardno != null && cardno.trim().length() > 0) {
				if (filter != null && filter.length() > 0)
					filter += " and ";
				filter += "cardno = '" + cardno + "'";
			}

			if (orderdate != null) {
				if (filter != null  && filter.length() > 0)
					filter += " and ";
				filter += "date(orderdate) = '" + dateFormatter.format(orderdate) + "'";
			}

			if (filter != null && filter.length() > 0) {
				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		cardno = null;
		orderdate = null;
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpinmailerdatapk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpinmailerdataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

}
