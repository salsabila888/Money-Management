package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TderivatifproductDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tderivatifproduct;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Vembossbranch;
import com.sdd.caption.model.TembossbranchListModel;
import com.sdd.caption.model.VembossbranchListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;

public class InquiryCabangListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TembossbranchListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Date orderdate;
	private Mbranch mbranch;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbBranch;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Row rowBranch;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);

		oUser = (Muser) zkSession.getAttribute("oUser");

		if (oUser != null) {
			if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600) {
				rowBranch.setVisible(true);
			} else {
				rowBranch.setVisible(false);
			}
		}

		paging.addEventListener("onPaging", new EventListener() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tembossbranch>() {

				@Override
				public void render(Row row, final Tembossbranch data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren()
							.add(new Label(data.getMproduct() != null
									? data.getTembossproduct().getMproduct().getMproducttype().getProducttype()
									: ""));
					row.getChildren().add(new Label(data.getTembossproduct().getProductcode()));
					row.getChildren().add(new Label(
							data.getMproduct() != null ? data.getTembossproduct().getMproduct().getProductname() : ""));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));

					Button btnDetail = new Button("Data");
					btnDetail.setAutodisable("self");
					btnDetail.setClass("btn btn-default btn-sm");
					btnDetail.addEventListener(Events.ON_CLICK, new EventListener() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("tembossbranch", data);

							Window win = (Window) Executions.createComponents("/view/emboss/embossdata.zul", null, map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(btnDetail);
				}
			});
		}
		doReset();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "orderdate desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TembossbranchListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (oUser != null) {
			if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600) {
				if (mbranch != null) {
					filter = "mbranchfk = " + mbranch.getMbranchpk() + " and mproductfk is not null";
					if (orderdate != null) {
						if (filter.length() > 0)
							filter += " and ";
						filter += "orderdate = '" + dateFormatter.format(orderdate) + "'";
					}

					if (filter.length() > 0) {
						needsPageUpdate = true;
						paging.setActivePage(0);
						pageStartNumber = 0;
						refreshModel(pageStartNumber);
					}
				}
			} else {
				filter = "mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and mproductfk is not null";
				if (orderdate != null) {
					if (filter.length() > 0)
						filter += " and ";
					filter += "orderdate = '" + dateFormatter.format(orderdate) + "'";
				}

				if (filter.length() > 0) {
					needsPageUpdate = true;
					paging.setActivePage(0);
					pageStartNumber = 0;
					refreshModel(pageStartNumber);
				}
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		mbranch = null;
		orderdate = null;
		cbBranch.setValue(null);
		// doSearch();
	}

	public ListModelList<Mbranch> getMbranchmodel() {
		ListModelList<Mbranch> lm = null;
		try {
			if (oUser != null) {
				if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 700) {
					lm = new ListModelList<Mbranch>(AppData.getMbranch());
				} else if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) < 700
						&& Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600) {
					lm = new ListModelList<Mbranch>(
							AppData.getMbranch("mregionfk = " + oUser.getMbranch().getMregion().getMregionpk()));
				}
			}
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

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

}
