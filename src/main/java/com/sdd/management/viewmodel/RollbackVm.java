package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
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
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TrollbackDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Trollback;
import com.sdd.caption.model.TdeliverydataListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.ListModelFlyweight;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class RollbackVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private MproductDAO mproductDao = new MproductDAO();
	private TpaketDAO tpaketDao = new TpaketDAO();
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TdeliveryDAO tdeliveryDao = new TdeliveryDAO();
	private TbranchstockDAO tbranchstockDao = new TbranchstockDAO();
	private TrollbackDAO trollbackDao = new TrollbackDAO();

	private ListModelList<Mproducttype> mproducttypemodel;
	private TdeliverydataListModel model;
	private Map<Integer, Tdeliverydata> mapData = new HashMap<>();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	private Mproduct mproduct;
	private Mproduct mproduct1;
	private Mbranch mbranch;
	private Integer total;
	private Integer totaldataselected;
	private String filter;
	private String action;
	private Date orderdate;
	private Tdeliverydata tdeliverydata;
	private Tdeliverydata tdlv;
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private Tdeliverydata obj;
	private Integer totalselected;

	@Wire
	private Combobox cbProduct;
	@Wire
	private Combobox cbBranch;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;
	@Wire
	private Button btnDelete;
	@Wire
	private Paging paging;
	@Wire
	private Datebox datebox;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		action = AppUtils.STATUS_DELIVERY_PAKETPROSES;
		doReset();

		try {
			setProductAutocomplete();
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

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tdeliverydata>() {

				@Override
				public void render(Row row, final Tdeliverydata data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							Tdeliverydata obj = (Tdeliverydata) checked.getAttribute("obj");
							if (checked.isChecked()) {
								mapData.put(obj.getTdeliverydatapk(), obj);
								totaldataselected += obj.getQuantity();
							} else {
								mapData.remove(obj.getTdeliverydatapk());
								totaldataselected -= obj.getQuantity();
							}
							totalselected = mapData.size();
							BindUtils.postNotifyChange(null, null, RollbackVm.this, "totalselected");
							BindUtils.postNotifyChange(null, null, RollbackVm.this, "totaldataselected");
						}
					});
					if (mapData.get(data.getTdeliverydatapk()) != null)
						check.setChecked(true);
					row.getChildren().add(check);
					row.getChildren().add(new Label(data.getMproduct().getProductcode()));
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(data.getTdelivery().getMbranch().getBranchid()));
					row.getChildren().add(new Label(data.getTdelivery().getMbranch().getBranchname()));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getTdelivery().getStatus())));
					row.getChildren().add(new Label(
							data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "0"));
					Button btnManifest = new Button("Detail");
					btnManifest.setAutodisable("self");
					btnManifest.setClass("btn btn-default btn-sm");
					btnManifest.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("tembossbranch", data.getTpaketdata().getTembossbranch());
							Window win = (Window) Executions.createComponents("/view/emboss/embossdata.zul", null, map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(btnManifest);
				}
			});
		}
	}

	@Command
	@NotifyChange("mproducttypemodel")
	public void doProductLoad(@BindingParam("item") String item) {
		if (item != null) {
			try {
				mproducttypemodel = new ListModelList<>(AppData.getMproducttype("productorg = " + item + "'"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public void setProductAutocomplete() {
		try {
			List<Mproduct> oList = AppData.getMproduct("productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'");
			cbProduct.setModel(new SimpleListModel(oList) {
				public ListModel getSubModel(Object value, int nRows) {
					if (value != null && value.toString().trim().length() > AppUtils.AUTOCOMPLETE_MINLENGTH) {
						String nameStartsWith = value.toString().trim().toUpperCase();
						List data = mproductDao.startsWith(AppUtils.AUTOCOMPLETE_MAXROWS, nameStartsWith,
								"productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'");
						return ListModelFlyweight.create(data, nameStartsWith, "mproduct");
					}
					return ListModelFlyweight.create(Collections.emptyList(), "", "mproduct");
				}
			});

			cbProduct.setItemRenderer(new ComboitemRenderer<Mproduct>() {

				@Override
				public void render(Comboitem item, Mproduct data, int index) throws Exception {
					item.setLabel(data.getProductcode());
					item.setDescription(data.getProductname());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doRollback() {
		if (action != null && action.length() > 0) {
			if (action.equals(AppUtils.STATUS_DELIVERY_PAKETPROSES)) {
				if (mapData.size() > 0) {
					Messagebox.show("Are you sure to rollback this data?", "Confirm Dialog",
							Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									if (event.getName().equals("onOK")) {

										Session session = null;
										Transaction transaction = null;
										for (Entry<Integer, Tdeliverydata> entry : mapData.entrySet()) {
											Tdeliverydata obj = entry.getValue();

											try {
												session = StoreHibernateUtil.openSession();
												transaction = session.beginTransaction();

												new TderivatifDAO().updateRollbackSql(session,
														AppUtils.STATUS_DERIVATIF_PAKET,
														"mbranchfk = " + obj.getTdelivery().getMbranch().getMbranchpk()
																+ " and tdeliveryfk = "
																+ obj.getTdelivery().getTdeliverypk());

												obj.getTpaketdata().setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
												obj.getTpaketdata().setIsdlv("");
												tpaketdataDao.save(session, obj.getTpaketdata());

												if (obj.getTpaketdata().getTembossbranch() != null) {
													obj.getTpaketdata().getTembossbranch()
															.setStatus(AppUtils.STATUSBRANCH_PROSESPAKET);
													new TembossbranchDAO().save(session,
															obj.getTpaketdata().getTembossbranch());

//													FlowHandler.doFlow(session, obj.getTpaketdata().getTembossbranch(),
//															null, AppUtils.PROSES_PAKET, null, oUser.getUserid());
												}

												if (obj.getTpaketdata().getTpaket().getTotaldone() > 0) {
													obj.getTpaketdata().getTpaket()
															.setTotaldone(obj.getTpaketdata().getTpaket().getTotaldone()
																	- obj.getQuantity());

													obj.getTpaketdata().getTpaket()
															.setStatus(AppUtils.STATUS_DELIVERY_PAKETPROSES);
													tpaketDao.save(session, obj.getTpaketdata().getTpaket());
												}

												Trollback trollback = new Trollback();
												trollback.setTpaketdata(obj.getTpaketdata());
												trollback.setMproduct(obj.getMproduct());
												trollback.setMbranch(obj.getTpaketdata().getMbranch());
												trollback.setOrderdate(obj.getTpaketdata().getOrderdate());
												trollback.setStatus(obj.getTpaketdata().getStatus());
												trollback.setTotaldata(obj.getQuantity());
												trollback.setRollbacktime(new Date());
												trollback.setRollbackby(oUser.getUserid());
												trollbackDao.save(session, trollback);

												transaction.commit();
												session.close();

												session = StoreHibernateUtil.openSession();
												transaction = session.beginTransaction();

												Integer total = obj.getTdelivery().getTotaldata() - 1;
												if (total == 0) {
													tdeliveryDao.delete(session, obj.getTdelivery());
												} else {
													obj.getTdelivery().setTotaldata(total);
													tdeliveryDao.save(session, obj.getTdelivery());
													new TdeliverydataDAO().delete(session, obj);
												}

												String filterstock = "mproductfk = " + obj.getMproduct().getMproductpk()
														+ " and mbranchfk = "
														+ obj.getTpaketdata().getMbranch().getMbranchpk()
														+ " and outlet = '00' and productgroup = '01'";
												Tbranchstock tbranchstock = tbranchstockDao.findByFilter(filterstock);
												if (tbranchstock != null) {
													tbranchstock.setStockdelivered(
															tbranchstock.getStockdelivered() - obj.getQuantity());
													tbranchstock.setStockcabang(tbranchstock.getStockcabang() - obj.getQuantity());
													if(tbranchstock.getStockcabang() < 0)
														tbranchstock.setStockcabang(0);
													tbranchstockDao.save(session, tbranchstock);
												}

												transaction.commit();
												session.close();

											} catch (HibernateException e) {
												transaction.rollback();
												e.printStackTrace();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										Clients.showNotification("Proses rollback data berhasil", "info", null,
												"middle_center", 3000);

										doReset();
										BindUtils.postNotifyChange(null, null, RollbackVm.this, "*");

									}
								}
							});
				} else if (mapData.size() == 0) {
					Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			}
		} else {
			Messagebox.show("Silahkan pilih status", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tdeliverydatapk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TdeliverydataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (mproduct != null && orderdate != null) {
			filter = "Tdeliverydata.orderdate = '" + dateFormatter.format(orderdate) + "' and mproductfk = "
					+ mproduct.getMproductpk();
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				if (comp.getChildren() != null && comp.getChildren().size() > 0) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					obj = (Tdeliverydata) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getTdeliverydatapk(), obj);
							totaldataselected += obj.getQuantity();
							tdlv = obj;
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getTdeliverydatapk());
							totaldataselected -= obj.getQuantity();
						}
					}
				}
			}
			totalselected = mapData.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		filter = "";
		obj = new Tdeliverydata();
		totaldataselected = 0;
		totalselected = 0;
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		mapData = new HashMap<>();

		needsPageUpdate = true;
		pageTotalSize = 0;
		paging.setTotalSize(pageTotalSize);

		doSearch();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mproducttype mproduct = (Mproducttype) ctx.getProperties("mproducttype")[0].getValue();
				Date entrytime = (Date) ctx.getProperties("entrytime")[0].getValue();
				Integer itemqty = (Integer) ctx.getProperties("itemqty")[0].getValue();

				if (mproduct == null)
					this.addInvalidMessage(ctx, "mproducttype", Labels.getLabel("common.validator.empty"));
				if (entrytime == null)
					this.addInvalidMessage(ctx, "entrytime", Labels.getLabel("common.validator.empty"));
				if (itemqty == null)
					this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public ListModelList<Mproducttype> getMproducttypemodel() {
		return mproducttypemodel;
	}

	public void setMproducttypemodel(ListModelList<Mproducttype> mproducttypemodel) {
		this.mproducttypemodel = mproducttypemodel;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public Mproduct getMproduct1() {
		return mproduct1;
	}

	public void setMproduct1(Mproduct mproduct1) {
		this.mproduct1 = mproduct1;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public ListModelList<Mbranch> getMbranchmodel() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(AppData.getMbranch("0=0"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return lm;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
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

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public Paging getPaging() {
		return paging;
	}

	public void setPaging(Paging paging) {
		this.paging = paging;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public Tdeliverydata getTdeliverydata() {
		return tdeliverydata;
	}

	public void setTdeliverydata(Tdeliverydata tdeliverydata) {
		this.tdeliverydata = tdeliverydata;
	}

	public Tdeliverydata getTdlv() {
		return tdlv;
	}

	public void setTdlv(Tdeliverydata tdlv) {
		this.tdlv = tdlv;
	}

	public Tdeliverydata getObj() {
		return obj;
	}

	public void setObj(Tdeliverydata obj) {
		this.obj = obj;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

}
