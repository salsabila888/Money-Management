package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.sdd.caption.dao.McouriervendorDAO;
import com.sdd.caption.dao.McourierzipcodeDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mcourierzipcode;
import com.sdd.caption.domain.Mletter;
import com.sdd.caption.domain.Mlettertype;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Vbranchdelivery;
import com.sdd.caption.handler.ItemTrackHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.VbranchdeliveryListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DeliveryJobCustomerVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private VbranchdeliveryListModel model;

	private TdeliveryDAO tdeliveryDao = new TdeliveryDAO();
	private TdeliverydataDAO tdeliverydataDao = new TdeliverydataDAO();
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TembossbranchDAO tembossbranchDao = new TembossbranchDAO();

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
	private int totalcardphoto;
	private int totaltoken;
	private int totalpinpad;
	private int totalpinmailer;
	private int totaldocument;
	private String branchid;
	private String branchname;
	private String productgroup;
	private Tpaketdata obj;
	private Boolean isSaved;

	private Map<String, Vbranchdelivery> mapData;

	private Map<String, Mbranch> mapBranch = new HashMap<String, Mbranch>();
	Map<String, Tbranchstock> mapTbranchstock = new HashMap<>();

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
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroup = AppUtils.PRODUCTGROUP_CARD;
		try {
			mapBranch = new HashMap<String, Mbranch>();
			for (Mbranch obj : AppData.getMbranch()) {
				mapBranch.put(obj.getBranchid(), obj);
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
							BindUtils.postNotifyChange(null, null, DeliveryJobCustomerVm.this, "totalselected");
							BindUtils.postNotifyChange(null, null, DeliveryJobCustomerVm.this, "totaldataselected");
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
					Button btnManifest = new Button("Paket");
					btnManifest.setAutodisable("self");
					btnManifest.setClass("btn btn-default btn-sm");
					btnManifest.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnManifest.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("mbranch", mapBranch.get(data.getBranchid()));
							map.put("productgroup", productgroup);

							Window win = (Window) Executions
									.createComponents("/view/delivery/deliverymanifestcustomer.zul", null, map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									Boolean isSaved = (Boolean) event.getData();
									if (isSaved != null && isSaved) {
										doReset();
										BindUtils.postNotifyChange(null, null, DeliveryJobCustomerVm.this, "*");
									}
								}
							});
						}
					});
					row.getChildren().add(btnManifest);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		doReset();
	}

	@NotifyChange({ "pageTotalSize", "totaldata" })
	public void refreshModel(int activePage) {
		try {
			orderby = "branchid";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new VbranchdeliveryListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
			if (needsPageUpdate) {
				pageTotalSize = model.getTotalSize(filter);
				needsPageUpdate = false;
			}
			paging.setTotalSize(pageTotalSize);
			grid.setModel(model);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
								BindUtils.postNotifyChange(null, null, DeliveryJobCustomerVm.this, "*");
							}
						}
					});
		}
	}

	@Command
	public void doDeliveryGroup() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			List<Vbranchdelivery> objList = new ArrayList<>();
			for (Entry<String, Vbranchdelivery> entry : mapData.entrySet()) {
				Vbranchdelivery data = entry.getValue();
				objList.add(data);
			}
			Map<String, Object> map = new HashMap<>();
			map.put("objSelected", objList);
			map.put("productgroup", productgroup);
			map.put("isDlvHome", "Y");
			Window win = (Window) Executions.createComponents("/view/delivery/deliverymanifestgenerate.zul", null, map);
			win.setClosable(true);
			win.doModal();
			win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getData() != null) {
						boolean isError = false;
						String strError = "";
						Session session = null;
						Transaction transaction = null;

						@SuppressWarnings("unchecked")
						Map<String, Object> map = (Map<String, Object>) event.getData();
						Tdelivery tdelivery = (Tdelivery) map.get("obj");
						Mletter mletter = (Mletter) map.get("prefix");
						Mlettertype mlettertype = (Mlettertype) map.get("lettertype");
						String filterpaket = "";

						filterpaket = "isdlv = 'N' and tpaket.tderivatifproductfk is null and tpaket.productgroup = '"
								+ productgroup + "' and branchpool = '" + oUser.getMbranch().getBranchid()
								+ "' and tpaketdata.zipcode is not null";

						for (Entry<String, Vbranchdelivery> entry : mapData.entrySet()) {
							Vbranchdelivery objDelivery = entry.getValue();
							Map<Integer, Tembossproduct> mapTembossproduct = new HashMap<>();
							try {
								List<Tpaketdata> objList = tpaketdataDao.listDelivery(
										"mbranch.mbranchpk = " + mapBranch.get(objDelivery.getBranchid()).getMbranchpk()
												+ " and " + filterpaket);
								for (Tpaketdata data : objList) {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									Tdelivery objForm = new Tdelivery();
									List<Mcourierzipcode> courierList = new ArrayList<Mcourierzipcode>();
									Mcourierzipcode courier = null;
									if (data.getZipcode() != null) {
										courierList = new McourierzipcodeDAO().listByFilter(
												data.getZipcode() + " BETWEEN ZIPCODESTART AND ZIPCODEEND",
												"mcourierzipcodepk");
										if (courierList.size() > 0)
											courier = courierList.get(0);
									}

									objForm.setMbranch(mapBranch.get(objDelivery.getBranchid()));
									objForm.setIsproductphoto("N");
									objForm.setProductgroup(productgroup);
									if (courier == null)
										objForm.setMcouriervendor(
												new McouriervendorDAO().findByFilter("vendorcode = 'SAP'"));
									else
										objForm.setMcouriervendor(courier.getMcouriervendor());
									objForm.setDlvid(
											new TcounterengineDAO().generateLetterNo(mletter.getLetterprefix()));
									objForm.setLettertype(mlettertype.getLettertype());
									objForm.setTotaldata(1);
									objForm.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
									objForm.setMemo(tdelivery.getMemo());
									objForm.setProcesstime(new Date());
									objForm.setProcessedby(oUser.getUserid());
									objForm.setBranchpool(oUser.getMbranch().getBranchid());
									objForm.setIsdlvcust("Y");
									objForm.setAccno(data.getAccno());
									objForm.setAddress1(data.getAddress1());
									objForm.setAddress2(data.getAddress2());
									objForm.setAddress3(data.getAddress3());
									objForm.setCity(data.getCity());
									objForm.setZipcode(data.getZipcode());
									objForm.setHpno(data.getHpno());
									objForm.setCardno(data.getCardno());
									objForm.setCustname(data.getCustname());
									objForm.setTotalamount(new BigDecimal(0));

									tdeliveryDao.save(session, objForm);

									Tdeliverydata deliverydata = new Tdeliverydata();
									deliverydata.setTdelivery(objForm);
									deliverydata.setTpaketdata(data);
									deliverydata.setMproduct(data.getTpaket().getMproduct());
									deliverydata.setProductgroup(data.getTpaket().getProductgroup());
									deliverydata.setOrderdate(data.getOrderdate());
									deliverydata.setQuantity(data.getQuantity());
									tdeliverydataDao.save(session, deliverydata);

									data.setIsdlv("Y");
									tpaketdataDao.save(session, data);

									if (data.getTembossbranch() != null) {
										data.getTembossbranch().setStatus(AppUtils.STATUSBRANCH_PROSESDELIVERY);
										data.getTembossbranch().setDlvfinishtime(new Date());
										tembossbranchDao.save(session, data.getTembossbranch());
										mapTembossproduct.put(
												data.getTpaket().getTembossproduct().getTembossproductpk(),
												data.getTpaket().getTembossproduct());

									}

									Tbranchstock tbranchstock = new TbranchstockDAO().findByFilter("mbranchfk = "
											+ mapBranch.get(objDelivery.getBranchid()).getMbranchpk()
											+ " and mproductfk = " + data.getTpaket().getMproduct().getMproductpk()
											+ " and outlet = '00'");

									if (tbranchstock == null) {
										tbranchstock = new Tbranchstock();
										tbranchstock.setMbranch(mapBranch.get(objDelivery.getBranchid()));
										tbranchstock.setMproduct(data.getTpaket().getMproduct());
										tbranchstock.setStockdelivered(1);
										tbranchstock.setStockactivated(1);
										tbranchstock.setStockreserved(0);
										tbranchstock.setStockdestroyed(0);
										tbranchstock.setProductgroup(data.getTpaket().getMproduct().getProductgroup());
										tbranchstock.setStockcabang(0);
										tbranchstock.setOutlet("00");
									} else {
										tbranchstock.setStockactivated(tbranchstock.getStockactivated() + 1);
										tbranchstock.setStockdelivered(tbranchstock.getStockdelivered() + 1);
									}
									new TbranchstockDAO().save(session, tbranchstock);

									Tbranchstockitem item = new Tbranchstockitem();
									item.setItemno(data.getCardno());
									item.setProductgroup(data.getTpaket().getMproduct().getProductgroup());
									item.setStatus(AppUtils.STATUS_SERIALNO_OUTINVENTORY);
									item.setTbranchstock(tbranchstock);
									new TbranchstockitemDAO().save(session, item);

									ItemTrackHandler.Run(item, session);
									
									transaction.commit();
									session.close();
								}
								
								Mmenu mmenu = new MmenuDAO()
										.findByFilter("menupath = '/view/delivery/deliverymanifestcourier.zul'");
								NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD,
										oUser.getMbranch().getBranchlevel());
//
								mmenu = new MmenuDAO()
										.findByFilter("menupath = '/view/delivery/deliveryjobcustomer.zul'");
								NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD,
										oUser.getMbranch().getBranchlevel());

							} catch (HibernateException e) {
								isError = true;
								if (strError.length() > 0)
									strError += ". ";
								strError += e.getMessage();
								e.printStackTrace();
							} catch (Exception e) {
								isError = true;
								if (strError.length() > 0)
									strError += ". ";
								strError += e.getMessage();
								e.printStackTrace();
							} finally {
								if (session.isOpen())
									session.close();
							}

						}

						if (isError) {
							Messagebox.show("Terdapat masalah pada proses pembuatan manifest pengiriman. \n" + strError,
									WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
						} else {
							Messagebox.show("Proses pembuatan manifest pengiriman berhasil" + strError,
									WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
							doReset();
							BindUtils.postNotifyChange(null, null, DeliveryJobCustomerVm.this, "*");
						}
					}
				}

			});

		}
	}

	@Command
	public void doSearch() {
		try {
			totaldata = 0;
			totalselected = 0;
			filter = "tpaket.productgroup = '" + productgroup
					+ "' and isdlv ='N' and tderivatifproductfk is null and branchpool = '"
					+ oUser.getMbranch().getBranchid() + "' and tpaketdata.zipcode is not null";

			if (branchid != null && branchid.trim().length() > 0)
				filter += " and mbranch.branchid like '%" + branchid.trim().toUpperCase() + "%'";
			if (branchname != null && branchname.trim().length() > 0)
				filter += " and mbranch.branchname like '%" + branchname.trim().toUpperCase() + "%'";
			if (orderdate != null)
				filter += " and orderdate = '" + dateFormatter.format(orderdate) + "'";

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		try {
			obj = new Tpaketdata();
			totaldata = 0;
			totalselected = 0;
			totaldataselected = 0;
			mapData = new HashMap<>();
			if (grid.getRows() != null)
				grid.getRows().getChildren().clear();
			doSearch();
		} catch (Exception e) {
			e.printStackTrace();
		}

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

	public void setTotalcard(int totalcard) {
		this.totalcard = totalcard;
	}

	public void setTotaltoken(int totaltoken) {
		this.totaltoken = totaltoken;
	}

	public void setTotalpinpad(int totalpinpad) {
		this.totalpinpad = totalpinpad;
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

	public int getTotalcardphoto() {
		return totalcardphoto;
	}

	public void setTotalcardphoto(int totalcardphoto) {
		this.totalcardphoto = totalcardphoto;
	}

	public int getTotaldocument() {
		return totaldocument;
	}

	public void setTotaldocument(int totaldocument) {
		this.totaldocument = totaldocument;
	}

}
