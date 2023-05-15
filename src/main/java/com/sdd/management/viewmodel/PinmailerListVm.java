package com.sdd.caption.viewmodel;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.bind.annotation.ContextParam;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.dao.TpinmailerfileDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.domain.Tpinmailerfile;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TpinmailerfileListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PinmailerListVm {
	
	private Muser oUser;
	private TpinmailerfileDAO oDao = new TpinmailerfileDAO();
	private TpinmailerfileListModel model;
	
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String filename;
	private Date processdate;
	private Integer year;
	private Integer month;
	
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) 
			throws Exception {
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
		
		grid.setRowRenderer(new RowRenderer<Tpinmailerfile>() {

			@Override
			public void render(Row row, final Tpinmailerfile data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
										+ index + 1)));
				A a = new A(data.getBatchid());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {		
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);
						
						Window win = (Window) Executions
								.createComponents(
										"/view/pinmailer/pinmailerbranch.zul",
										null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.doModal();	
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getUploadtime())));
				row.getChildren().add(new Label(data.getFilename()));
				row.getChildren().add(new Label(data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				row.getChildren().add(new Label(data.getMemo()));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
				if (data.getStatus().equals(AppUtils.STATUS_ORDER)) {
					Button btn = new Button("Order Proses");
					btn.setAutodisable("self");
					btn.setClass("btn btn-default btn-sm");
					btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Messagebox.show("Anda ingin melakukan order data pin mailer?", "Confirm Dialog",
									Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

										@Override
										public void onEvent(Event event) throws Exception {
											if (event.getName().equals("onOK")) {
												doOrder(data);
											}
										}
							});
						}
					});
					row.getChildren().add(btn);
				} else row.getChildren().add(new Label());
			}

		});
		
		String[] months = new DateFormatSymbols().getMonths();
	    for (int i = 0; i < months.length; i++) {
	      Comboitem item = new Comboitem();
	      item.setLabel(months[i]);
	      item.setValue(i+1);
	      cbMonth.appendChild(item);
	    }
	    
		doReset();
	}
	
	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpinmailerfilepk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpinmailerfileListModel(activePage, SysUtils.PAGESIZE, filter,
				orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}
	
	public void doOrder(Tpinmailerfile data) throws Exception {
		Session session = StoreHibernateUtil.openSession();
		Transaction transaction = session.beginTransaction();
		TorderDAO torderDao = new TorderDAO();
		try {
			Torder obj = new Torder();
			obj.setTpinmailerfile(data);
			obj.setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
			obj.setInsertedby(oUser.getUserid());
			obj.setInserttime(new Date());
			obj.setOrderdate(new Date());
			obj.setMproduct(data.getMproduct());
			obj.setOrderid(new TcounterengineDAO().generateCounter(AppUtils.CE_ORDER));
			obj.setProductgroup(AppUtils.PRODUCTGROUP_PINMAILER);
			obj.setStatus(AppUtils.STATUS_ORDER_WAITAPPROVAL);
			obj.setItemqty(data.getTotaldata());
			obj.setTotalqty(data.getTotaldata());
			obj.setMemo(data.getMemo());
			obj.setOrderlevel(oUser.getMbranch().getBranchlevel());
			obj.setTotalproses(0);
			torderDao.save(session, obj);
			
			data.setStatus(AppUtils.STATUS_PROSES);
			oDao.save(session, data);
			
			Tordermemo objMemo = new Tordermemo();
			objMemo.setTorder(obj);
			objMemo.setMemo(data.getMemo());
			objMemo.setMemoby(oUser.getUserid());
			objMemo.setMemotime(new Date());
			new TordermemoDAO().save(session, objMemo);
			
			FlowHandler.doFlow(session, null, obj, data.getMemo(), oUser.getUserid());
			
			transaction.commit();
			
			Mmenu mmenu = new MmenuDAO()
					.findByFilter("menupath = '/view/order/orderapproval.zul' and menuparamvalue = '06'");
			NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_PINMAILER, oUser.getMbranch().getBranchlevel());
			
			Clients.showNotification("Proses order pin mailer berhasil", "info", null, "middle_center", 3000);
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
	
	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (year != null && month != null) {
			filter = "extract(year from uploadtime) = " + year + " and "
					+ "extract(month from uploadtime) = " + month;
			if (filename != null && filename.trim().length() > 0)
				filter += " and filename like '%" + filename.trim().toUpperCase() + "%'";
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
		doSearch();
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Date getProcessdate() {
		return processdate;
	}

	public void setProcessdate(Date processdate) {
		this.processdate = processdate;
	}

}
