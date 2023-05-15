package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.model.TpaketListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DerivatifPaketOrderVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpaketListModel model;

	private TpaketDAO tpaketDao = new TpaketDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;

	private String producttype;
	private String productcode;
	private String productname;
	private Date orderdate;
	private Integer totalselected;
	private Integer totaldataselected;
	private Date prodfinishtime;

	private String productgroup;
	private String productgroupname;

	private Map<Integer, Tpaket> mapData = new HashMap<>();
	private Map<Integer, Mproduct> mapProduct = new HashMap<Integer, Mproduct>();
	private Map<Integer, Mbranch> mapBranch = new HashMap<Integer, Mbranch>();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Groupbox gbHeader;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();

		try {
			mapProduct = new HashMap<Integer, Mproduct>();
			// for (Mproduct obj: AppData.getMproduct("productgroup = '" +
			// AppUtils.PRODUCTGROUP_CARD + "'")) {
			for (Mproduct obj : AppData.getMproduct("0=0")) {
				mapProduct.put(obj.getMproductpk(), obj);
			}
			mapBranch = new HashMap<Integer, Mbranch>();
			for (Mbranch obj : AppData.getMbranch()) {
				mapBranch.put(obj.getMbranchpk(), obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
				chkAll.setChecked(false);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tpaket>() {

			@Override
			public void render(Row row, final Tpaket data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tpaket obj = (Tpaket) checked.getAttribute("obj");
						if (checked.isChecked()) {
							mapData.put(data.getTpaketpk(), data);
							totaldataselected += obj.getTotaldata();
						} else {
							mapData.remove(data.getTpaketpk());
							totaldataselected -= obj.getTotaldata();
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, DerivatifPaketOrderVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, DerivatifPaketOrderVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTpaketpk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
				row.getChildren().add(new Label(data.getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(
						new Label(data.getOrderdate() != null ? datelocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(
						data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
			}

		});
	}

	@NotifyChange({ "pageTotalSize", "totaldata", "totaldataselected" })
	public void refreshModel(int activePage) {
		orderby = "tpaket desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpaketListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Tpaket obj = (Tpaket) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getTpaketpk(), obj);
							totaldataselected += obj.getTotaldata();
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getTpaketpk());
							totaldataselected -= obj.getTotaldata();
						}
					}
				}
				totalselected = mapData.size();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doViewSelected() {
		if (mapData.size() > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("mapData", mapData);
			map.put("totalselected", totalselected);
			map.put("totaldataselected", totaldataselected);

			Window win = (Window) Executions.createComponents("/view/delivery/paketproductselected.zul", null, map);
			win.setClosable(true);
			win.doModal();
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
								totalselected = 0;
								totaldataselected = 0;
								mapData = new HashMap<>();
								refreshModel(pageStartNumber);
								BindUtils.postNotifyChange(null, null, DerivatifPaketOrderVm.this, "*");
							}
						}
					});
		}
	}

	@Command
	public void doPaketGroup() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			try {
				Messagebox.show("Anda ingin membuat manifest paket?", "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									Session session = null;
									Transaction transaction = null;
									for (Entry<Integer, Tpaket> entry : mapData.entrySet()) {
										Tpaket objPaket = entry.getValue();
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										try {
											objPaket.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
											objPaket.setProcesstime(new Date());
											objPaket.setProcessedby(oUser.getUserid());
											tpaketDao.save(session, objPaket);
											transaction.commit();

											Clients.showNotification("Submit data paket berhasil", "info", null,
													"middle_center", 2000);
											doReset();
											BindUtils.postNotifyChange(null, null, DerivatifPaketOrderVm.this, "*");
										} catch (HibernateException e) {
											transaction.rollback();
											e.printStackTrace();
											Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
										} catch (Exception e) {
											transaction.rollback();
											e.printStackTrace();
											Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
										} finally {
											session.close();
										}
									}
								}
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		filter = "tderivatifproductfk is not null and status = '" + AppUtils.STATUS_DELIVERY_PAKETORDER + "'";
		System.out.println(filter);

		if (productcode != null && productcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
		}
		if (productname != null && productname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
		}
		if (orderdate != null) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "orderdate = '" + dateFormatter.format(orderdate) + "'";
		}

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totalselected = 0;
		totaldataselected = 0;
		producttype = null;
		productcode = null;
		productname = null;
		mapData = new HashMap<>();
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		// doSearchGroup(productgroup);
		doSearch();
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

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
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

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public Date getProdfinishtime() {
		return prodfinishtime;
	}

	public void setProdfinishtime(Date prodfinishtime) {
		this.prodfinishtime = prodfinishtime;
	}

}
