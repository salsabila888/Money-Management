package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.model.TpaketdataListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PaketToDeliveryVm {
	
	private TpaketdataListModel model;
		
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TdeliverydataDAO tdeliverydataDao = new TdeliverydataDAO();
	private TdeliveryDAO tdeliveryDao = new TdeliveryDAO();
	private TpaketDAO oDao = new TpaketDAO();

	private Tdelivery obj;
	private String branchname;
	private String producttype;
	private String productcode;
	private String productname;
	private String productgroup;
	private Integer totalselected;
	private Integer totaldataselected;	
	private Boolean isSaved;
	private Map<Integer, Tpaketdata> mapData;
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	
	@Wire
	private Window winPakettodelivery;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tdelivery tdelivery)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		obj = tdelivery;		
		doSearch();	
		mapData = new HashMap<>();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tpaketdata>() {

			@Override
			public void render(Row row, final Tpaketdata data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
										+ index + 1)));			
				Checkbox check = new Checkbox();
				if (!data.getStatus().equals(AppUtils.STATUS_DELIVERY_DELIVERYORDER))
					check.setDisabled(true);
				check.setChecked(false);
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK,
						new EventListener<Event>() {

							@Override
							public void onEvent(Event event)
									throws Exception {
								Checkbox checked = (Checkbox) event
										.getTarget();
								Tpaketdata obj = (Tpaketdata) checked.getAttribute("obj");
								if (checked.isChecked()) {									
									mapData.put(obj.getTpaketdatapk(), obj);
								} else {
									mapData.remove(obj.getTpaketdatapk(), obj);
								}
								totalselected = mapData.size();
							}
						});
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getNopaket()));					
				row.getChildren().add(new Label(data.getTpaket().getTembossproduct().getMproduct().getMproducttype().getProducttype()));
				row.getChildren().add(new Label(data.getTpaket().getTembossproduct().getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getTpaket().getTembossproduct().getMproduct().getProductname()));	
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(data.getOrderdate() != null ? datelocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "0"));		
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
			}
		});				
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tpaketdatapk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TpaketdataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}
	
	@Command
	@NotifyChange({"totalselected", "totaldataselected"})
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Tpaketdata objList = (Tpaketdata) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(objList.getTpaketdatapk(), objList);
						}								
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(objList.getTpaketdatapk(), objList);
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
	public void doAddPaket() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info",
					Messagebox.OK, Messagebox.INFORMATION);
		} else {
			Messagebox.show("Anda ingin menambahkan paket ke delivery manifest?", "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getData() != null) {		
						boolean isError = false;
						String strError = "";
						Session session = null;
						Transaction transaction = null;
						for (Entry<Integer, Tpaketdata> entry: mapData.entrySet()) {
							try {
								Tpaketdata paketdata = entry.getValue();
								session = StoreHibernateUtil.openSession();
								transaction = session.beginTransaction();
								obj.setTotaldata(obj.getTotaldata() + totalselected);
								tdeliveryDao.save(session, obj);								
								
								Tdeliverydata tdeliverydata = new Tdeliverydata();
								tdeliverydata.setTdelivery(obj);	
								tdeliverydata.setTpaketdata(paketdata);
								tdeliverydata.setMproduct(paketdata.getTpaket().getMproduct());
								tdeliverydata.setOrderdate(paketdata.getOrderdate());
								tdeliverydata.setQuantity(paketdata.getQuantity());
								tdeliverydataDao.save(session, tdeliverydata);
								
								paketdata.setIsdlv("Y");
								tpaketdataDao.save(session, paketdata);
								
								oDao.save(session, paketdata.getTpaket());								
								transaction.commit();
							} catch (HibernateException e) {
								transaction.rollback();
								isError = true;
								if (strError.length() > 0)
									strError += ". \n";
								strError += e.getMessage();
								e.printStackTrace();
							} catch (Exception e) {
								transaction.rollback();
								isError = true;
								if (strError.length() > 0)
									strError += ". \n";
								strError += e.getMessage();
								e.printStackTrace();
							} finally {
								session.close();
							}
						}
						try {
														
						} catch (Exception e) {
							e.printStackTrace();
							
						}
						
						if (isError) 
							Messagebox.show(strError, WebApps.getCurrent().getAppName(), Messagebox.OK,
									Messagebox.ERROR);
						else {
							Clients.showNotification("Submit data delivery berhasil", "info",
									null, "middle_center", 2000);
							isSaved = new Boolean(true);
							doClose();
						}						
					}
				}
				
			});
						
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "mbranchfk = " + obj.getMbranch().getMbranchpk() + " and isdlv = 'N'";
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event( "onClose", winPakettodelivery, isSaved);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException {
		doSearch();
	}
	
	public Tdelivery getObj() {
		return obj;
	}

	public void setObj(Tdelivery obj) {
		this.obj = obj;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
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

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}		

}
