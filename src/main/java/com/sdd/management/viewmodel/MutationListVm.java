package com.sdd.management.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zel.impl.parser.ParseException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.management.dao.TmutationDAO;
import com.sdd.management.domain.Muser;
import com.sdd.management.domain.Tmutation;
import com.sdd.management.model.TmutationListModel;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MutationListVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private Muser oUser;
	private TmutationListModel model;
	private TmutationDAO mutationDao = new TmutationDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby, filter;
	private Integer year, month;

	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Window winPlan;
	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Caption captIncoming;
	@Wire
	private Groupbox gbSearch;
	@Wire
	private Column colTotal, colJumlah;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) Sessions.getCurrent().getAttribute("oUser");

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tmutation>() {

			@Override
			public void render(Row row, Tmutation data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				row.getChildren().add(new Label(data.getMutationno() != null ? data.getMutationno() : "-"));
				row.getChildren().add(new Label(data.getMutationdate() != null ? SimpleDateFormat.getInstance().format(data.getMutationdate()) : "-"));
				row.getChildren().add(new Label(data.getMincome() != null ? data.getMincome().getIncomesource() : "-"));
				row.getChildren().add(new Label(data.getMexpenses() != null ? data.getMexpenses().getExpenses() : "-"));
				row.getChildren().add(new Label(
						data.getMaim() != null ? data.getMaim().getAim() : "-"));
				row.getChildren().add(new Label(data.getMpayment() != null ? data.getMpayment().getPaymenttype() : "-"));
				row.getChildren().add(new Label(data.getMbank() != null ? data.getMbank().getBankname() : "-"));
				row.getChildren()
						.add(new Label(data.getMutationamount() != null ? "Rp " + NumberFormat.getInstance().format(data.getMutationamount()) : "-"));

				Button btnDetail = new Button();
				btnDetail.setLabel("Detail");
				btnDetail.setAutodisable("self");
				btnDetail.setSclass("btn-light");
				btnDetail.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btnDetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);
						map.put("isDetail", "Y");
						Window win = (Window) Executions.createComponents("/view/planning/planningdata.zul", null, map);
						win.setWidth("70%");
						win.setClosable(true);
						win.doModal();
					}
				});

				Button btnEdit = new Button();
				btnEdit.setLabel("Edit");
				btnEdit.setAutodisable("self");
				btnEdit.setSclass("btn-light");
				btnEdit.setStyle(
						"border-radius: 8px; background-color: #eeba0b !important; color: #ffffff !important;");
				btnEdit.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);
						map.put("isEdit", "Y");
						Window win = new Window();
						win.setWidth("70%");
						win.setClosable(true);
						win.doModal();
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								doReset();
								BindUtils.postNotifyChange(null, null, MutationListVm.this, "*");
							}
						});
					}
				});

				Button btnDelete = new Button();
				btnDelete.setLabel("Delete");
				btnDelete.setAutodisable("self");
				btnDelete.setSclass("btn-light");
				btnDelete.setStyle(
						"border-radius: 8px; background-color: #ce0012 !important; color: #ffffff !important;");
				btnDelete.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@SuppressWarnings({ "unchecked", "rawtypes" })
					@Override
					public void onEvent(Event event) throws Exception {
						Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
								Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

									@Override
									public void onEvent(Event event) throws Exception {
										if (event.getName().equals("onOK")) {
											try {
												session = StoreHibernateUtil.openSession();
												transaction = session.beginTransaction();

												mutationDao.delete(session, data);
												transaction.commit();
												session.close();

												Clients.showNotification(Labels.getLabel("common.delete.success"),
														"info", null, "middle_center", 3000);

												BindUtils.postNotifyChange(null, null, MutationListVm.this,
														"pageTotalSize");
											} catch (Exception e) {
												Messagebox.show("Error : " + e.getMessage(),
														WebApps.getCurrent().getAppName(), Messagebox.OK,
														Messagebox.ERROR);
												e.printStackTrace();
											}

										}
										needsPageUpdate = true;
										doReset();
									}
								});
					}
				});
			}
		});

		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item = new Comboitem();
			item.setLabel(months[i]);
			item.setValue(i + 1);
			cbMonth.appendChild(item);
		}

		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (year != null && month != null) {
			filter = "extract(year from mutationdate) = " + year + " and " + "extract(month from mutationdate) = " + month;

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		grid.setModel(model);
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tmutationpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TmutationListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}
	
	@Command
	public void doAdd() {
		Window win = (Window) Executions.createComponents("/view/moneymng/mutationform.zul", null, null);
		win.setWidth("80%");
		win.setClosable(true);
		win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				doReset();
				BindUtils.postNotifyChange(null, null, MutationListVm.this, "*");
			}
		});
		win.doModal();

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

	public boolean isNeedsPageUpdate() {
		return needsPageUpdate;
	}

	public void setNeedsPageUpdate(boolean needsPageUpdate) {
		this.needsPageUpdate = needsPageUpdate;
	}

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
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

	public Muser getoUser() {
		return oUser;
	}

	public void setoUser(Muser oUser) {
		this.oUser = oUser;
	}
}
