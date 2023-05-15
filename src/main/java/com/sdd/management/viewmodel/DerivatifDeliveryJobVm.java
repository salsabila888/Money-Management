package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Mletter;
import com.sdd.caption.domain.Mlettertype;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Vbranchdelivery;
import com.sdd.caption.domain.Vproductgroupsumdata;
import com.sdd.caption.model.TpaketListModel;
import com.sdd.caption.model.TpaketdataListModel;
import com.sdd.caption.model.VbranchdeliveryListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DerivatifDeliveryJobVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private VbranchdeliveryListModel model;

	private TdeliveryDAO tdeliveryDao = new TdeliveryDAO();
	private TdeliverydataDAO tdeliverydataDao = new TdeliverydataDAO();
	private TembossdataDAO torderdataDao = new TembossdataDAO();
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TpaketDAO tpaketDao = new TpaketDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;

	private Date orderdate;
	private int totaldata;
	private Integer totalselected;
	private Integer totaldataselected;
	private int totalcard;
	private int totaltoken;
	private int totalpinpad;
	private int totaldoc;
	private int totalsupplies;
	private int totalpinmailer;
	private String branchid;
	private String branchname;
	private String productgroup;
	private Tpaketdata obj;
	private Boolean isSaved;

	private Map<String, Vbranchdelivery> mapData;

	private Map<String, Mbranch> mapBranch = new HashMap<String, Mbranch>();
	private Map<Integer, Mproduct> mapProduct = new HashMap<Integer, Mproduct>();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbBranch;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroup = AppUtils.PRODUCTGROUP_CARD;
		try {
			mapBranch = new HashMap<String, Mbranch>();
			for (Mbranch obj : AppData.getMbranch()) {
				mapBranch.put(obj.getBranchid(), obj);
			}
			mapProduct = new HashMap<Integer, Mproduct>();
			for (Mproduct obj : AppData.getMproduct()) {
				mapProduct.put(obj.getMproductpk(), obj);
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

			grid.setRowRenderer(new RowRenderer<Vbranchdelivery>() {

				@Override
				public void render(final Row row, final Vbranchdelivery data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							Vbranchdelivery obj = (Vbranchdelivery) checked.getAttribute("obj");
							if (checked.isChecked()) {
								mapData.put(data.getBranchid(), obj);
								totaldataselected += obj.getTotal();
							} else {
								mapData.remove(obj.getBranchid());
								totaldataselected -= obj.getTotal();
							}
							totalselected = mapData.size();
							BindUtils.postNotifyChange(null, null, DerivatifDeliveryJobVm.this, "totalselected");
							BindUtils.postNotifyChange(null, null, DerivatifDeliveryJobVm.this, "totaldataselected");
						}
					});
					if (mapData.get(data.getBranchid()) != null)
						check.setChecked(true);
					row.getChildren().add(check);
					row.getChildren().add(new Label(data.getBranchid()));
					row.getChildren().add(new Label(data.getBranchname()));
					row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(
							data.getTotal() != null ? NumberFormat.getInstance().format(data.getTotal()) : "0"));
					
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		doReset();
	}

	@NotifyChange({ "pageTotalSize", "totaldata" })
	public void refreshModel(int activePage) {
		orderby = "branchid";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new VbranchdeliveryListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				if (comp.getChildren() != null && comp.getChildren().size() > 0) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Vbranchdelivery obj = (Vbranchdelivery) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getBranchid(), obj);
							totaldataselected += obj.getTotal();
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getBranchid());
							totaldataselected -= obj.getTotal();
						}
					}
				}
			}
			totalselected = mapData.size();
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

			Window win = (Window) Executions.createComponents("/view/delivery/deliverybranchselected.zul", null, map);
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
								BindUtils.postNotifyChange(null, null, DerivatifDeliveryJobVm.this, "*");
							}
						}
					});
		}
	}

	@Command
	public void doDeliveryGroup() {
		
	}

	@Command
	public void doSearch() {
		totaldata = 0;
		totalselected = 0;
		filter = "isdlv ='N'";
		if (branchid != null && branchid.trim().length() > 0)
			filter += " and mbranch.branchid like '%" + branchid.trim().toUpperCase() + "%'";
		if (branchname != null && branchname.trim().length() > 0)
			filter += " and mbranch.branchname like '" + branchname.trim().toUpperCase() + "%'";
		if (orderdate != null)
			filter += " and orderdate = '" + dateFormatter.format(orderdate) + "'";

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		obj = new Tpaketdata();
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
		doSearch();
	}

	public ListModelList<Mbranch> getMbranch() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(AppData.getMbranch());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Tpaketdata getObj() {
		return obj;
	}

	public void setObj(Tpaketdata obj) {
		this.obj = obj;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public String getBranchid() {
		return branchid;
	}

	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public int getTotalcard() {
		return totalcard;
	}

	public int getTotaltoken() {
		return totaltoken;
	}

	public int getTotalpinpad() {
		return totalpinpad;
	}

	public int getTotaldoc() {
		return totaldoc;
	}

	public int getTotalsupplies() {
		return totalsupplies;
	}

	public void setTotalcard(int totalcard) {
		this.totalcard = totalcard;
	}

	public void setTotaltoken(int totaltoken) {
		this.totaltoken = totaltoken;
	}

	public void setTotalpinpad(int totalpinpad) {
		this.totalpinpad = totalpinpad;
	}

	public void setTotaldoc(int totaldoc) {
		this.totaldoc = totaldoc;
	}

	public void setTotalsupplies(int totalsupplies) {
		this.totalsupplies = totalsupplies;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}

	public int getTotalpinmailer() {
		return totalpinmailer;
	}

	public void setTotalpinmailer(int totalpinmailer) {
		this.totalpinmailer = totalpinmailer;
	}

}
