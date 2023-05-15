package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Div;
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
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.dao.TpersodataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpersodata;
import com.sdd.caption.domain.Vorderperso;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TembossproductListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PersoProductVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TembossproductListModel model;

	private TpersoDAO tpersoDao = new TpersoDAO();
	private TpersodataDAO tpersodataDao = new TpersodataDAO();
	private TembossproductDAO tembossproductDao = new TembossproductDAO();
	private TembossbranchDAO tembossbranchDao = new TembossbranchDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String filename;
	private String producttype;
	private String productcode;
	private String productname;
	private Date orderdate;
	private Integer totaldata;
	private Integer totalselected;
	private Integer totaldataselected;
	private Integer branchInvalid;

	private Map<Integer, Tembossproduct> mapData = new HashMap<>();
	private Map<Integer, Mproducttype> mapProducttype = new HashMap<>();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	private List<Vorderperso> listPersoStatus = new ArrayList<Vorderperso>();
	private List<Morg> listOrg = new ArrayList<Morg>();
	private String productorg;
	private String filterperiod;
	private String orgdesc;
	private String periode;

	@Wire
	private Window winOrderproduct;
	@Wire
	private Groupbox gbHeader;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Grid gridStatus;
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

		grid.setRowRenderer(new RowRenderer<Tembossproduct>() {
			@Override
			public void render(Row row, final Tembossproduct data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tembossproduct obj = (Tembossproduct) checked.getAttribute("obj");
						if (checked.isChecked()) {
							List<Tembossbranch> tebList = tembossbranchDao.listByFilter(
									"tembossproductfk = " + data.getTembossproductpk() + " and mbranchfk is null",
									"branchid");
							String branchRejected = "";
							if (tebList.size() > 0) {
								for (Tembossbranch teb : tebList) {
									if (branchRejected.length() > 0)
										branchRejected += "\n";
									branchRejected += teb.getBranchid();
								}
								checked.setChecked(false);
								Messagebox.show(
										"Dalam produk ini terdapat cabang yang belum terdaftar diparameter. Branch ID : "
												+ branchRejected,
										"Info", Messagebox.OK, Messagebox.INFORMATION);
							} else {
								mapData.put(data.getTembossproductpk(), data);
								totaldataselected += obj.getOrderos();
							}
						} else {
							mapData.remove(data.getTembossproductpk());
							totaldataselected -= obj.getOrderos();
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, PersoProductVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, PersoProductVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTembossproductpk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getTembossfile().getFilename()));

				if (data.getMproduct() != null) {
					row.getChildren().add(new Label(data.getMproduct().getMproducttype().getProducttype()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
				} else {
					row.getChildren().add(new Label(""));
					row.getChildren().add(new Label(""));
				}

				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getIsinstant().equals("Y") ? "YA" : "TIDAK"));
				row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getOrderos())));

				Button btnManifest = new Button("Data Cabang");
				btnManifest.setAutodisable("self");
				btnManifest.setClass("btn btn-default btn-sm");
				btnManifest.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important; float: right !important;");
				btnManifest.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						if (data.getMproduct() == null) {
							Messagebox.show("Data tidak dapat diproses karena kode produk tidak terdaftar diparameter",
									WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.EXCLAMATION);
						} else {
							Map<String, Object> map = new HashMap<>();
							map.put("tembossproduct", data);

							Window win = (Window) Executions.createComponents("/view/perso/persobranch.zul", null, map);
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									Boolean isSaved = (Boolean) event.getData();
									if (isSaved != null && isSaved) {
										needsPageUpdate = true;
										refreshModel(pageStartNumber);
										doSumStatus();
										BindUtils.postNotifyChange(null, null, PersoProductVm.this, "*");
									}
								}
							});
						}
					}
				});

				Div div = new Div();
				div.setClass("btn-group btn-group-sm");
				div.appendChild(btnManifest);
				row.getChildren().add(div);
			}
		});
	}

	@NotifyChange({ "pageTotalSize", "totaldata" })
	public void refreshModel(int activePage) {
		try {
			orderby = "tembossproduct.productcode";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new TembossproductListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
			if (needsPageUpdate) {
				pageTotalSize = model.getTotalSize(filter);
				needsPageUpdate = false;
			}
			paging.setTotalSize(pageTotalSize);
			grid.setModel(model);

			totaldata = tembossproductDao.sumOrderData(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doSumStatus() {
		try {
			List<String> listPeriod = new ArrayList<>();
			Map<String, Integer> map = new HashMap<>();
			listPersoStatus = tembossproductDao.listSumRegularOrder();
			for (Vorderperso obj : listPersoStatus) {
				if (!listPeriod.contains(String.valueOf(obj.getYear()) + String.valueOf(obj.getMonth())))
					listPeriod.add(String.valueOf(obj.getYear()) + String.valueOf(obj.getMonth()));

				if (obj.getProductorg() == null || obj.getProductorg().trim().equals(""))
					map.put(String.valueOf(obj.getYear()) + String.valueOf(obj.getMonth()) + "undefined",
							obj.getTotal());
				else
					map.put(String.valueOf(obj.getYear()) + String.valueOf(obj.getMonth()) + obj.getProductorg(),
							obj.getTotal());
			}

			gridStatus.getColumns().getChildren().clear();
			Column colPeriod = new Column("Periode");
			colPeriod.setAlign("center");
			colPeriod.setStyle("background-color: #81bf3d !important; border-color: #f1f2f2 !important;");
			gridStatus.getColumns().appendChild(colPeriod);

			listOrg = AppData.getMorgByFilter("isneeddoc = 'N'");
			for (Morg morg : listOrg) {
				Column colOrg = new Column(morg.getOrg());
				colOrg.setTooltiptext(morg.getDescription());
				colOrg.setAlign("right");
				colOrg.setStyle("background-color: #81bf3d !important; border-color: #f1f2f2 !important;");
				gridStatus.getColumns().appendChild(colOrg);
			}

			Column colUndefined = new Column("Missing Product");
			colUndefined.setAlign("right");
			colUndefined.setStyle("background-color: #81bf3d !important; border-color: #f1f2f2 !important;");
			gridStatus.getColumns().appendChild(colUndefined);

			Column colTotal = new Column("Total Data");
			colTotal.setAlign("right");
			colTotal.setStyle("background-color: #81bf3d !important; border-color: #f1f2f2 !important;");
			gridStatus.getColumns().appendChild(colTotal);

			Integer total = 0;
			gridStatus.getRows().getChildren().clear();
			for (final String period : listPeriod) {
				Row row = new Row();
				row.appendChild(new Label(StringUtils.getMonthshortLabel(Integer.parseInt(period.substring(4))) + " "
						+ period.substring(0, 4)));
				for (final Morg morg : listOrg) {
					Integer qty = map.get(period + morg.getOrg());
					if (qty == null)
						qty = 0;
					if (qty.intValue() != 0) {
						A a = new A(NumberFormat.getInstance().format(qty));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								productorg = morg.getOrg();
								orgdesc = morg.getDescription();
								periode = StringUtils.getMonthshortLabel(Integer.parseInt(period.substring(4))) + " "
										+ period.substring(0, 4);
								String year = period.substring(0, 4);
								String month = period.substring(4);
								month = "0" + month;
								month = month.substring(month.length() - 2, month.length());
								Calendar cal = Calendar.getInstance();
								cal.set(Integer.parseInt(year), Integer.parseInt(month) - 1, 1);
								String startdate = dateFormatter.format(cal.getTime());
								cal.add(Calendar.MONTH, 1);
								cal.add(Calendar.DAY_OF_MONTH, -1);
								String enddate = dateFormatter.format(cal.getTime());
								filterperiod = "orderdate between '" + startdate + "' and '" + enddate + "'";
								doSearch();
								BindUtils.postNotifyChange(null, null, PersoProductVm.this, "*");
							}
						});
						row.appendChild(a);
					} else {
						row.appendChild(new Label(NumberFormat.getInstance().format(qty)));
					}
					total = total + qty;
				}

				Integer undefined = map.get(period + "undefined");
				if (undefined == null) {
					undefined = Integer.valueOf(0);
				}
				row.appendChild(new Label(NumberFormat.getInstance().format(undefined)));
				total = total + undefined;

				row.appendChild(new Label(NumberFormat.getInstance().format(total)));
				gridStatus.getRows().appendChild(row);
			}
			gridStatus.appendChild(gridStatus.getRows());
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
				branchInvalid = 0;
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Tembossproduct obj = (Tembossproduct) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							List<Tembossbranch> tebList = tembossbranchDao.listByFilter(
									"tembossproductfk = " + obj.getTembossproductpk() + " and mbranchfk is null",
									"branchid");
							if (tebList.size() > 0) {
								branchInvalid++;
								chk.setChecked(false);
							} else {
								chk.setChecked(true);
								mapData.put(obj.getTembossproductpk(), obj);
								totaldataselected += obj.getOrderos();
							}
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getTembossproductpk());
							totaldataselected = 0;
						}
					}
				}
				totalselected = mapData.size();
				
				if(branchInvalid > 0) {
					Messagebox.show("Ada beberapa produk yang belum bisa diproses manifest perso.",
							WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
				}
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

			Window win = (Window) Executions.createComponents("/view/perso/persoproductselected.zul", null, map);
			win.setClosable(true);
			win.doModal();
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
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
								doReset();
								BindUtils.postNotifyChange(null, null, PersoProductVm.this, "*");
							}
						}
					});
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		mapData = new HashMap<>();
		// filter = "status = '" + AppUtils.STATUS_ORDER + "' and org = '" + productorg
		// + "'and " + filterperiod;
		filter = "orderos > 0 and org = '" + productorg + "'and " + filterperiod;
		if (orderdate != null) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "orderdate = '" + dateFormatter.format(orderdate) + "'";
		}
		if (producttype != null && producttype.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
		}
		if (productcode != null && productcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "tembossproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
		}
		if (productname != null && productname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
		}

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	public synchronized void doManifests() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			synchronized (this) {
				int productRejectCount = 0;
				String productRejected = "";
				boolean isValidStock = true;

				try {
					mapProducttype = new HashMap<Integer, Mproducttype>();
					for (Entry<Integer, Tembossproduct> entry : mapData.entrySet()) {
						Tembossproduct data = entry.getValue();

						if (mapProducttype.get(data.getMproduct().getMproducttype().getMproducttypepk()) == null) {
							mapProducttype.put(data.getMproduct().getMproducttype().getMproducttypepk(),
									data.getMproduct().getMproducttype());
						}

						Mproducttype objStock = mapProducttype
								.get(data.getMproduct().getMproducttype().getMproducttypepk());
						if (objStock.getLaststock() - objStock.getStockreserved() < data.getTotaldata()) {
							productRejectCount++;
							isValidStock = false;
							if (productRejected.length() > 0)
								productRejected += "\n";
							productRejected += objStock.getProducttype();
							// break;
						} else {
							objStock.setStockreserved(objStock.getStockreserved() + data.getTotaldata());
							mapProducttype.put(objStock.getMproducttypepk(), objStock);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (!isValidStock) {
					Messagebox.show(
							"Proses pembuatan grup manifest tidak bisa dilakukan karena terdapat " + productRejectCount
									+ " tipe produk yang melebihi jumlah ketersediaan stock.\nTipe Produk : \n"
									+ productRejected,
							WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
				} else {
					Map<String, Object> map = new HashMap<>();
					map.put("mapData", mapData);
					Window win = (Window) Executions.createComponents("/view/perso/persomanifestgenerate.zul", null,
							map);
					win.setClosable(true);
					win.doModal();
					win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getData() != null) {
								@SuppressWarnings("unchecked")
								Map<String, Object> mapPerso = (Map<String, Object>) event.getData();
								Tperso tperso = (Tperso) mapPerso.get("objForm");

								boolean isError = false;
								String strError = "";
								String filterdata = "";
								List<Tembossbranch> objList = null;
								Session session = null;
								Transaction transaction = null;
								for (Entry<Integer, Tembossproduct> entry : mapData.entrySet()) {
									Tembossproduct embossproduct = entry.getValue();
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									try {
										Tperso objForm = new Tperso();
										objForm.setTembossproduct(embossproduct);
										objForm.setMproduct(embossproduct.getMproduct());
										if (tperso.getMpersovendor() != null)
											objForm.setMpersovendor(tperso.getMpersovendor());
										objForm.setPersoid(new TcounterengineDAO().generateCounter(AppUtils.CE_PERSO));
										objForm.setOrderdate(embossproduct.getOrderdate());
										objForm.setTotaldata(embossproduct.getOrderos());
										objForm.setTotalpaket(0);
										objForm.setPersostartby(oUser.getUserid());
										objForm.setPersostarttime(tperso.getPersostarttime());
										objForm.setMemo(tperso.getMemo());
										objForm.setStatus(AppUtils.STATUS_PERSO_PERSOWAITAPPROVAL);
										objForm.setIsgetallpaket("");
										tpersoDao.save(session, objForm);

										embossproduct.setTotalproses(embossproduct.getTotaldata());
										embossproduct.setOrderos(0);
										embossproduct.setStatus(AppUtils.STATUS_PROSES);
										tembossproductDao.save(session, embossproduct);

										filterdata = "tembossproduct.tembossproductpk = "
												+ embossproduct.getTembossproductpk()
												+ " and totalos > 0 and mbranchfk is not null";
										objList = new TembossbranchDAO().listByFilter(filterdata, "tembossbranchpk");
										for (Tembossbranch obj : objList) {
											Tpersodata tpersodata = new Tpersodata();
											tpersodata.setTembossbranch(obj);
											tpersodata.setMbranch(obj.getMbranch());
											tpersodata.setTperso(objForm);
											tpersodata.setOrderdate(obj.getOrderdate());
											tpersodata.setQuantity(obj.getTotaldata());
											tpersodata.setStatus("");
											tpersodata.setIsgetpaket("");
											tpersodataDao.save(session, tpersodata);

											obj.setStatus(AppUtils.STATUSBRANCH_PROSESPRODUKSI);
											obj.setTotalproses(obj.getTotaldata());
											obj.setTotalos(0);
											tembossbranchDao.save(session, obj);

										}
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
									
									try {
										Mmenu mmenu = new MmenuDAO()
												.findByFilter("menupath = '/view/order/ordertab.zul' and menuparamvalue = 'persoapproval'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARD, oUser.getMbranch().getBranchlevel());
									} catch (Exception e) {
										e.printStackTrace();
									}

								}

								if (isError)
									Messagebox.show(strError, WebApps.getCurrent().getAppName(), Messagebox.OK,
											Messagebox.ERROR);
								else {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									try {
										for (Entry<Integer, Mproducttype> entry : mapProducttype.entrySet()) {
											Mproducttype mproducttype = entry.getValue();
											mproducttypeDao.save(session, mproducttype);
										}
										transaction.commit();
									} catch (Exception e) {
										e.printStackTrace();
									} finally {
										session.close();
									}
									Messagebox.show("Proses pembuatan manifest perso berhasil",
											WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
									doReset();
									doSearch();
									BindUtils.postNotifyChange(null, null, PersoProductVm.this, "*");
								}
							}
						}
					});
				}
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totaldata = 0;
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
		producttype = null;
		productcode = null;
		productname = null;
		orderdate = null;
		if (gridStatus.getRows() != null)
			gridStatus.getRows().getChildren().clear();
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		chkAll.setChecked(false);
		doSumStatus();
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
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

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
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

	public String getProductorg() {
		return productorg;
	}

	public void setProductorg(String productorg) {
		this.productorg = productorg;
	}

	public String getOrgdesc() {
		return orgdesc;
	}

	public void setOrgdesc(String orgdesc) {
		this.orgdesc = orgdesc;
	}

	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

}
