package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
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
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
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

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TpersoListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PaketProductVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpersoListModel model;

	private TpaketDAO oDao = new TpaketDAO();
	private TpaketdataDAO dataDao = new TpaketdataDAO();
	private TpersoDAO tpersoDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private TembossbranchDAO tembossbranchDao = new TembossbranchDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;

	private String producttype;
	private String productcode;
	private String productname;
	private Date orderdate;
	private Integer totaldata;
	private Integer totalselected;
	private Integer totaldataselected;
	private Date prodfinishtime;

	private int totalcard;
	private int totaltoken;
	private int totalpinpad;
	private int totaldoc;
	private int totalsupplies;
	private int totalpinmailer;
	private String productgroup;
	private String productgroupname;

	private Map<Integer, Tperso> mapData = new HashMap<>();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	@Wire
	private Groupbox gbHeader;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
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
							mapData.put(data.getTpersopk(), data);
							totaldataselected += obj.getTotaldata();
						} else {
							mapData.remove(data.getTpersopk());
							totaldataselected -= obj.getTotaldata();
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, PaketProductVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, PaketProductVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTpersopk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));
				row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(datetimelocalFormatter.format(data.getPersofinishtime())));
				row.getChildren().add(new Label(
						data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				Button btndetail = new Button("Cabang");
				btndetail.setAutodisable("self");
				btndetail.setClass("btn btn-default btn-sm");
				btndetail.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);

						Window win = (Window) Executions.createComponents("/view/delivery/paketbranch.zul", null, map);
						win.setClosable(true);
						win.doModal();
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								Boolean isSaved = (Boolean) event.getData();
								if (isSaved != null && isSaved) {
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
									BindUtils.postNotifyChange(null, null, PaketProductVm.this, "*");
								}
							}
						});
					}
				});
				row.getChildren().add(btndetail);
			}
		});
	}

	@NotifyChange({ "pageTotalSize", "totaldata", "totaldataselected" })
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
		try {
			totaldata = tpersoDao.getSum(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Tperso obj = (Tperso) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getTpersopk(), obj);
							totaldataselected += obj.getTotaldata();
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
								BindUtils.postNotifyChange(null, null, PaketProductVm.this, "*");
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
									boolean isError = false;
									String strError = "";
									for (Entry<Integer, Tperso> entry : mapData.entrySet()) {
										Tperso tperso = entry.getValue();
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										try {
											Tpaket paket = new Tpaket();
											paket.setTperso(tperso);
											paket.setMproduct(tperso.getMproduct());
											paket.setTembossproduct(tperso.getTembossproduct());
											paket.setPaketid(new TcounterengineDAO()
													.generateYearMonthCounter(AppUtils.CE_PAKET));
											paket.setProductgroup(tperso.getMproduct().getProductgroup());
											paket.setTotaldata(tperso.getTotaldata());
											paket.setTotaldone(0);
											paket.setOrderdate(tperso.getOrderdate());
											paket.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
											paket.setBranchpool(oUser.getMbranch().getBranchid());
											paket.setProcessedby(oUser.getUserid());
											paket.setProcesstime(new Date());
											oDao.save(session, paket);
											for (Tpersodata data : tpersodataDao.listNativeByFilter("tpersofk = "
													+ tperso.getTpersopk() + " and isgetpaket = 'N' and status = '"
													+ AppUtils.STATUS_PERSO_DONE + "'", "tpersodatapk")) {
												Tpaketdata paketdata = new Tpaketdata();
												paketdata.setTpaket(paket);
												paketdata.setTembossbranch(data.getTembossbranch());
												paketdata.setNopaket(new TcounterengineDAO().generateNopaket());
												paketdata.setProductgroup(
														data.getTperso().getMproduct().getProductgroup());
												paketdata.setMbranch(data.getMbranch());
												paketdata.setOrderdate(data.getTperso().getOrderdate());
												paketdata.setQuantity(data.getQuantity());
												paketdata.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
												paketdata.setIsdlv("");
												paketdata.setZipcode(null);
												paketdata.setPaketstartby(oUser.getUserid());
												paketdata.setPaketstarttime(new Date());
												dataDao.save(session, paketdata);

												data.setIsgetpaket("Y");
												tpersodataDao.save(session, data);

												if (tperso.getTpersoupload() == null) {
													if (data.getTembossbranch() != null) {
														data.getTembossbranch()
																.setStatus(AppUtils.STATUSBRANCH_PROSESPAKET);
													}
												}
												data.getTembossbranch().setDlvstarttime(new Date());
												tembossbranchDao.save(session, data.getTembossbranch());
											}

											tperso.setTotalpaket(tperso.getTotaldata());
											tperso.setIsgetallpaket("Y");
											tperso.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
											tpersoDao.save(session, tperso);

											transaction.commit();
										} catch (HibernateException e) {
											transaction.rollback();
											isError = true;
											if (strError.length() > 0)
												strError += ", ";
											strError += e.getMessage();
											e.printStackTrace();
										} catch (Exception e) {
											transaction.rollback();
											isError = true;
											if (strError.length() > 0)
												strError += ", ";
											strError += e.getMessage();
											e.printStackTrace();
										} finally {
											session.close();
										}

										try {
											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketlist'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(),
													tperso.getMproduct().getProductgroup(),
													oUser.getMbranch().getBranchlevel());

											mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/order/ordertab.zul' and menuparamvalue = 'paketorder'");
											NotifHandler.delete(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD,
													oUser.getMbranch().getBranchlevel());
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									if (isError)
										Messagebox.show("Proses pembuatan manifest paket gagal. \n" + strError,
												WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
									else {
										Messagebox.show("Proses pembuatan manifest paket berhasil",
												WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.INFORMATION);
										doReset();
										BindUtils.postNotifyChange(null, null, PaketProductVm.this, "*");
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
	public void doSearchGroup(@BindingParam("item") String item) {
		try {
			productgroup = item;
			productgroupname = AppData.getProductgroupLabel(productgroup);
			doSearch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		totaldata = 0;
		filter = "isgetallpaket = 'N' and tderivatifproductfk is null";

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
		totaldata = 0;
		totalselected = 0;
		totaldataselected = 0;
		producttype = null;
		productcode = null;
		productname = null;
		mapData = new HashMap<>();
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		productgroup = AppUtils.PRODUCTGROUP_CARD;
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

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public int getTotalcard() {
		return totalcard;
	}

	public void setTotalcard(int totalcard) {
		this.totalcard = totalcard;
	}

	public int getTotaltoken() {
		return totaltoken;
	}

	public void setTotaltoken(int totaltoken) {
		this.totaltoken = totaltoken;
	}

	public int getTotalpinpad() {
		return totalpinpad;
	}

	public void setTotalpinpad(int totalpinpad) {
		this.totalpinpad = totalpinpad;
	}

	public int getTotaldoc() {
		return totaldoc;
	}

	public void setTotaldoc(int totaldoc) {
		this.totaldoc = totaldoc;
	}

	public int getTotalsupplies() {
		return totalsupplies;
	}

	public void setTotalsupplies(int totalsupplies) {
		this.totalsupplies = totalsupplies;
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

	public int getTotalpinmailer() {
		return totalpinmailer;
	}

	public void setTotalpinmailer(int totalpinmailer) {
		this.totalpinmailer = totalpinmailer;
	}

}
