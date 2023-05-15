package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.model.TpersoListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class DerivatifPersoListVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpersoListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String productcode;
	private String productname;
	private String persoid;
	private Date orderdate;
	private Integer totalselected;
	private Integer totaldataselected;

	private Map<Integer, Tperso> mapData = new HashMap<>();

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {

		Selectors.wireComponents(view, this, false);

		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
				chkAll.setChecked(false);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tperso>() {
			@Override
			public void render(Row row, final Tperso data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tperso obj = (Tperso) checked.getAttribute("obj");
						if (checked.isChecked()) {
							if (obj.getStatus().equals(AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL)
									|| obj.getStatus().equals(AppUtils.STATUS_PERSO_PRODUKSI)
									|| obj.getStatus().equals(AppUtils.STATUS_PERSO_DONE)) {
								mapData.put(data.getTpersopk(), data);
								totaldataselected += obj.getTotaldata();
							} else {
								checked.setChecked(false);
								Messagebox.show(
										"Manifest belum bisa diproses karena dalam status "
												+ AppData.getStatusLabel(obj.getStatus()),
										"Info", Messagebox.OK, Messagebox.INFORMATION);
							}

						} else {
							mapData.remove(data.getTpersopk());
							totaldataselected -= obj.getTotaldata();
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, DerivatifPersoListVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, DerivatifPersoListVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTpersopk()) != null)
					check.setChecked(true);
				if (!data.getStatus().equals(AppUtils.STATUS_PERSO_DONE))
					row.getChildren().add(check);
				else
					row.getChildren().add(new Label());
				row.getChildren().add(new Label(data.getPersoid()));
				row.getChildren().add(new Label(data.getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(
						new Label(data.getOrderdate() != null ? datelocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
			}
		});

	}

	@Command
	@NotifyChange("*")
	public void doDone() {

	}

	@Command
	public void doPrint(@BindingParam("type") final String type) {

	}

	@Command
	public void doPrintManifest(final String printtype) {

	}

	@Command
	public void doBon(final String prinString) {

	}

	@NotifyChange({ "totalselected", "totaldataselected" })
	private void doResetListSelected() {
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpersopk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpersoListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {

		filter = "tderivatifproductfk is not null";
		if (persoid != null && persoid.length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "persoid like '%" + persoid.trim().toUpperCase() + "%'";
		}
		if (productcode != null && productcode.length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "productcode like '%" + productcode.trim().toUpperCase() + "%'";
		}
		if (productname != null && productname.length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "productname like '%" + productname.trim().toUpperCase() + "%'";
		}
		if (orderdate != null) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "orderdate like '%" + orderdate + "%'";
		}

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);

	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tperso obj = (Tperso) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						if (obj.getStatus().equals(AppUtils.STATUS_PERSO_PRODUKSI)
								|| obj.getStatus().equals(AppUtils.STATUS_PERSO_DONE)) {
							chk.setChecked(true);
							mapData.put(obj.getTpersopk(), obj);
							totaldataselected += obj.getTotaldata();
						}
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getTpersopk());
						totaldataselected -= obj.getTotaldata();
					}
				}
			}
			totalselected = mapData.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	@Command
	public void doResetSelected() {
		if (mapData.size() > 0) {
			Messagebox.show("Anda ingin mereset data-data yang sudah anda pilih?", "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								doResetListSelected();
								refreshModel(pageStartNumber);
								BindUtils.postNotifyChange(null, null, DerivatifPersoListVm.this, "*");
							}
						}
					});
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		persoid = "";
		productcode = "";
		productname = "";
		doSearch();
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
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

	public String getPersoid() {
		return persoid;
	}

	public void setPersoid(String persoid) {
		this.persoid = persoid;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

}
