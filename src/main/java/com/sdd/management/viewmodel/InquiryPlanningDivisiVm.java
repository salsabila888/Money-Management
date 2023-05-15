package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.domain.Msupplier;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincomingvendor;
import com.sdd.caption.domain.Vinquiryplan;
import com.sdd.caption.domain.Vreportplan;
import com.sdd.caption.model.TincomingVendorListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class InquiryPlanningDivisiVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	List<Vinquiryplan> objList = new ArrayList<>();
	private TplanDAO oDao = new TplanDAO();

	private int pageStartNumber, pageTotalSize;
	private boolean needsPageUpdate;
	private String planno, memono, orderby, filter, status, incid;
	private Msupplier msupplier;
	private String productgroup;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbVendor;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Row rowVendor;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroup = arg;
		System.out.println(productgroup);

		paging.addEventListener("onPaging", new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Vinquiryplan>() {
				@Override
				public void render(Row row, final Vinquiryplan data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren().add(new Label(data.getPlanno() != null ? data.getPlanno() : "-"));
					row.getChildren().add(new Label(data.getIncomingid() != null ? data.getIncomingid() : "-"));
					row.getChildren().add(new Label(data.getBranchname() != null ? data.getBranchname() : "-"));
					row.getChildren().add(new Label(data.getMproducttypefk() != null ? data.getProducttype() : "-"));
					row.getChildren().add(new Label(
							data.getInputtime() != null ? dateLocalFormatter.format(data.getInputtime()) : "-"));
					row.getChildren().add(new Label(
							data.getEntrytime() != null ? dateLocalFormatter.format(data.getEntrytime()) : "-"));
					row.getChildren()
							.add(new Label(data.getAnggaran() != null
									? "Rp " + NumberFormat.getInstance().format(data.getAnggaran())
									: "-"));
					row.getChildren().add(new Label(
							data.getTotalqty() != null ? NumberFormat.getInstance().format(data.getTotalqty()) : "0"));
					row.getChildren()
							.add(new Label(data.getTotalprocess() != null
									? NumberFormat.getInstance().format(data.getTotalprocess())
									: "0"));
					row.getChildren()
							.add(new Label(data.getHarga() != null
									? "Rp " + NumberFormat.getInstance()
											.format(data.getHarga().multiply(new BigDecimal(data.getItemqty())))
									: "-"));
					row.getChildren().add(new Label(data.getSuppliername() != null ? data.getSuppliername() : "-"));
					row.getChildren().add(new Label(data.getMemono() != null ? data.getMemono() : "-"));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getMemodate())));
					row.getChildren().add(new Label(data.getInputer() != null ? data.getInputer() : "-"));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
					A a = new A(data.getDocfileori());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Sessions.getCurrent().setAttribute("reportPath",
									AppUtils.FILES_ROOT_PATH + AppUtils.MEMO_PATH + data.getDocfileid());
							Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
						}
					});
					if (data.getDocfileori() != null) {
						row.getChildren().add(a);
					} else
						row.getChildren().add(new Label("-"));
				}
			});
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		try {
			objList = oDao.listInquiryplan(filter, orderby);
			grid.setModel(new ListModelList<>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		try {
			if ((oUser.getMbranch().getBranchlevel()) == 1) {
				filter = "tplan.productgroup = '" + productgroup + "'";
				if (planno != null)
					filter += " and tplan.planno = '" + planno.trim() + "'";

				if (incid != null)
					filter += " and incomingid = '" + incid.trim() + "'";

				if (memono != null)
					filter += " and tplan.memono = '" + memono.trim() + "'";

				if (msupplier != null)
					filter += " and tincomingvendor.msupplierfk = '" + msupplier.getMsupplierpk() + "'";

				if (status != null)
					filter += " and tplan.status = '" + status + "'";

				if (filter.length() > 0) {
					needsPageUpdate = true;
					paging.setActivePage(0);
					pageStartNumber = 0;
					refreshModel(pageStartNumber);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		msupplier = null;
		status = null;
		planno = null;
		incid = null;
		memono = null;
		cbVendor.setValue(null);
		objList = new ArrayList<>();
		grid.setModel(new ListModelList<>(objList));
	}

	public ListModelList<Msupplier> getMsuppliermodel() {
		ListModelList<Msupplier> lm = null;
		try {
			if (oUser != null) {
				lm = new ListModelList<Msupplier>(AppData.getMsupplier());
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

	public String getPlanno() {
		return planno;
	}

	public void setPlanno(String planno) {
		this.planno = planno;
	}

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public String getMemono() {
		return memono;
	}

	public void setMemono(String memono) {
		this.memono = memono;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIncid() {
		return incid;
	}

	public void setIncid(String incid) {
		this.incid = incid;
	}

	public Msupplier getMsupplier() {
		return msupplier;
	}

	public void setMsupplier(Msupplier msupplier) {
		this.msupplier = msupplier;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

}