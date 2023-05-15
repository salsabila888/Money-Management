package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderflowDAO;
import com.sdd.caption.domain.Torderflow;
import com.sdd.caption.model.TorderflowListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.utils.SysUtils;

public class InquiryFlowVm {

	private TorderflowDAO oDao = new TorderflowDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String id;
	private int totaldata;
	private String cabang;

	private TorderflowListModel model;
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	private List<Torderflow> objList = new ArrayList<>();

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);

		doReset();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Torderflow>() {

				@Override
				public void render(Row row, final Torderflow data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));

					if (data.getTembossbranch() != null) {
						cabang = data.getTembossbranch().getMbranch().getBranchname();
					} else {
						if (data.getTorder().getMbranch() != null)
							cabang = data.getTorder().getMbranch().getBranchname();
						else
							cabang = "OPR";
					}
					
					row.getChildren()
							.add(new Label(data.getTembossbranch() != null
									? data.getTembossbranch().getTembossfile().getEmbossid()
									: data.getTorder().getOrderid()));
					row.getChildren().add(new Label(cabang));
					row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
					row.getChildren()
							.add(new Label(
									data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata())
											: "0"));
					row.getChildren().add(new Label(AppData.getFlowgroup(data.getFlowgroup())));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getFlowname())));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getFlowtime())));
					
					totaldata = totaldata + data.getTotaldata();
				}
			});
		}

	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			totaldata = 0;
			filter = "";

			if (id != null && id.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "embossid = '" + id + "' or orderid = '" + id + "'";
			}

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
			
			objList = oDao.listByFilter(filter, "torderflowpk");
			System.out.println(objList.get(1).getFlowname());
			for(Torderflow obj : objList) {
				totaldata = totaldata + obj.getTotaldata();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		id = "";
		cabang = "";
		objList = new ArrayList<>();
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "Torderflowpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TorderflowListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
	}
}
