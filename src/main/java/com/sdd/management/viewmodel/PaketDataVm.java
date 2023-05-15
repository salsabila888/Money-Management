package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
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
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TpaketdataListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PaketDataVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpaketdataListModel model;
		
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	
	private TpaketDAO oDao = new TpaketDAO();
	private TpaketdataDAO dataDao = new TpaketdataDAO();	

	private Tpaket obj;
	private String branchname;
	private String producttype;
	private String productcode;
	private String productname;
	private Integer totalselected;
	private Integer totaldataselected;	
	private Boolean isSaved;
	private Map<Integer, Tpaketdata> mapData;
	
	@Wire
	private Window winPaketdata;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Div divRecord, divGridRecord, divButton;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tpaket tpaket, 
			@ExecutionArgParam("isInquiry") final String isInquiry) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		obj = tpaket;
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
		
		if (isInquiry != null && isInquiry.equals("Y")) {
			grid.setVisible(false);
			divRecord.setVisible(false);
			divGridRecord.setVisible(false);
			divButton.setVisible(false);
		}

		grid.setRowRenderer(new RowRenderer<Tpaketdata>() {

			@Override
			public void render(Row row, final Tpaketdata data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
										+ index + 1)));		
				Checkbox check = new Checkbox();
				if (data.getIsdlv().equals("Y")) 
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
									totaldataselected += obj.getQuantity();
								} else {
									mapData.remove(obj.getTpaketdatapk(), obj);
									totaldataselected -= obj.getQuantity();
								}
								totalselected = mapData.size();
								BindUtils.postNotifyChange(null, null, PaketDataVm.this, "totalselected");
								BindUtils.postNotifyChange(null, null, PaketDataVm.this, "totaldataselected");
							}
						});
				if (mapData.get(data.getTpaketdatapk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getNopaket()));					
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getQuantity())));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
				Button btnDetail = new Button("Detail");
				btnDetail.setAutodisable("self");
				btnDetail.setClass("btn btn-default btn-sm");
				btnDetail.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btnDetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("tembossbranch", data.getTembossbranch());
							Window win = (Window) Executions
									.createComponents(
											"/view/order/orderdata.zul",
											null, map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();								
					}
				});
				row.getChildren().add(btnDetail);
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
					Tpaketdata obj = (Tpaketdata) chk.getAttribute("obj");
					if (obj.getIsdlv().equals("Y")) {
						chk.setChecked(false);
					} else {
						if (checked) {
							if (!chk.isChecked()) {
								chk.setChecked(true);
								mapData.put(obj.getTpaketdatapk(), obj);
								totaldataselected += obj.getQuantity();
							}								
						} else {
							if (chk.isChecked()) {
								chk.setChecked(false);
								mapData.remove(obj.getTpaketdatapk(), obj);
								totaldataselected -= obj.getQuantity();
							}					
						}
					}
				}
				totalselected = mapData.size();
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@NotifyChange({"totalselected", "totaldataselected"})
	private void doResetListSelected() {
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
	}
	
	@NotifyChange("*")
	@Command
	public void doResetSelected() {
		if (mapData.size() > 0) {
			Messagebox.show("Anda ingin mereset data-data yang sudah anda pilih?", "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {
				
				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getName().equals("onOK")) {		
						totalselected = 0;
						totaldataselected = 0;
						mapData = new HashMap<>();
						refreshModel(pageStartNumber);
						BindUtils.postNotifyChange(null, null, PaketDataVm.this, "*");
					}
				}
			});
		}		
	}
	
	@Command
	public void doPrintLabel() {
		if (mapData.size() > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("obj", obj);
			map.put("mapData", mapData);

			Window win = (Window) Executions
					.createComponents(
							"/view/delivery/paketlabel.zul",
							null, map);							
			win.setClosable(true);
			win.doModal();	
		}	
	}
	
	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "tpaketfk = " + obj.getTpaketpk();
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}
	
	@Command
	public void doDoneSorting() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(),
					Messagebox.OK, Messagebox.INFORMATION);
		} else {
			boolean isValid = true;
			for (Entry<Integer, Tpaketdata> entry: mapData.entrySet()) {
				Tpaketdata data = entry.getValue();
				if (data.getIsdlv().equals("Y")) {
					isValid = false;
					Messagebox.show("Proses update status tidak bisa \ndilakukan karena terdapat data \ndengan status bukan proses paket. "
							+ "\nSilahkan periksa kembali \ndata-data yang anda pilih", WebApps.getCurrent().getAppName(),
							Messagebox.OK, Messagebox.INFORMATION);					
					break;
				}
			}
			if (isValid) {
				Messagebox.show("Anda ingin update status done?", "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getName().equals("onOK")) {
							boolean isError = false;
							String strError = "";
							Session session = StoreHibernateUtil.openSession();
							Transaction transaction = session.beginTransaction();
							try {
								for (Entry<Integer, Tpaketdata> entry: mapData.entrySet()) {
									Tpaketdata data = entry.getValue();							
									data.setPaketfinishby(oUser.getUserid());
									data.setPaketfinishtime(new Date());
									data.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
									data.setIsdlv("N");
									dataDao.save(session, data);				
									
//									FlowHandler.doFlow(session, data.getTembossbranch(), null, AppUtils.PROSES_PAKET,
//											data.getTembossbranch().getTembossfile().getMemo(),
//											oUser.getUserid());
									
									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), "01",
											oUser.getMbranch().getBranchlevel());
								}
								
								obj.setTotaldone(obj.getTotaldone() + totaldataselected);
								if (obj.getTotaldata().equals(obj.getTotaldone())) {										
									obj.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
								}
								oDao.save(session, obj);
								
								Mmenu mmenu = new MmenuDAO().findByFilter("menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketlist'");
								NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD,
										oUser.getMbranch().getBranchlevel());
																
								transaction.commit();									
							} catch (HibernateException e) {
								transaction.rollback();
								isError = true;
								strError = e.getMessage();
								e.printStackTrace();
							} catch (Exception e) {
								transaction.rollback();
								isError = true;
								strError = e.getMessage();
								e.printStackTrace();
							} finally {
								session.close();
							}
							
							if (isError) {
								Messagebox.show(strError, WebApps.getCurrent().getAppName(),
										Messagebox.OK, Messagebox.INFORMATION);	
								doReset();
							} else {
								Clients.showNotification("Proses update status done paket \nselesai", "info",
										null, "middle_center", 2000);								
								isSaved = new Boolean(true);
								doClose();	
							}							
						}
					}
				});
			}
		}				
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event( "onClose", winPaketdata, isSaved);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException {
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
		doSearch();	
	}
	
	public Tpaket getObj() {
		return obj;
	}

	public void setObj(Tpaket obj) {
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

}
