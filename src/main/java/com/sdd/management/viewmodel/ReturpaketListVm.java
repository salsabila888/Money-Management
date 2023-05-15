package com.sdd.caption.viewmodel;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import org.zkoss.zul.Checkbox;
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
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.dao.TreturnitemDAO;
import com.sdd.caption.dao.TreturntrackDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.domain.Treturntrack;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TreturnListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ReturpaketListVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TreturnListModel model;
	private TreturnDAO oDao = new TreturnDAO();
	private TreturnitemDAO treturpaketDao = new TreturnitemDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby, filter;
	private Integer month, year;
	private String paketid, productcode, productname;
	private Date processtime;
	private String type;
	private String status;
	private Integer totalselected;
	private Integer totaldataselected;
	private Map<Integer, Treturn> mapData;
	private Date date;
	private Integer total;
	private Integer totaldone;

	List<Treturnitem> objList;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		doResetListSelected();
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
			grid.setRowRenderer(new RowRenderer<Treturn>() {
				@Override
				public void render(Row row, final Treturn data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							Treturn obj = (Treturn) checked.getAttribute("obj");
							if (checked.isChecked()) {
								mapData.put(obj.getTreturnpk(), obj);
								totaldataselected += obj.getItemqty();
							} else {
								mapData.remove(obj.getTreturnpk());
								totaldataselected -= obj.getItemqty();
							}
							totalselected = mapData.size();
							BindUtils.postNotifyChange(null, null, ReturpaketListVm.this, "totalselected");
							BindUtils.postNotifyChange(null, null, ReturpaketListVm.this, "totaldataselected");
						}
					});
					if (mapData.get(data.getTreturnpk()) != null)
						check.setChecked(true);
					row.getChildren().add(check);
					A a = new A(data.getRegid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("obj", data);
							win = (Window) Executions.createComponents("/view/return/returproductitem.zul", null, map);
							win.setWidth("50%");
							win.setClosable(true);
							win.doModal();
						}
					});
						row.getChildren().add(a);
					row.getChildren()
							.add(new Label(AppData.getProductgroupLabel(data.getMproduct().getProductgroup())));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getInserttime())));
					row.getChildren().add(new Label(
							data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : "0"));
					row.getChildren().add(new Label(data.getInsertedby()));
				}
			});
		}
		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item = new Comboitem();
			item.setLabel(months[i]);
			item.setValue(i + 1);
			cbMonth.appendChild(item);
		}
		doReset();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "treturnpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TreturnListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
					Treturn obj = (Treturn) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getTreturnpk(), obj);
							totaldataselected += obj.getItemqty();
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getTreturnpk());
							totaldataselected -= obj.getItemqty();
						}
					}
				}
				totalselected = mapData.size();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange({ "totalselected", "totaldataselected" })
	private void doResetListSelected() {
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
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
								BindUtils.postNotifyChange(null, null, ReturpaketListVm.this, "*");
							}
						}
					});
		}
	}

	@Command
	public void doDoneSorting() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			boolean isValid = true;
			for (Entry<Integer, Treturn> entry : mapData.entrySet()) {
				Treturn obj = entry.getValue();
				if (!obj.getStatus().equals(AppUtils.STATUS_RETUR_RETURNPFA) && !obj.getStatus().equals(AppUtils.STATUS_RETUR_RETURNOPR)) {
					isValid = false;
					Messagebox.show(
							"Proses update status tidak bisa dilakukan karena terdapat data dengan status bukan proses paket. Silahkan periksa kembali data-data yang anda pilih",
							"Info", Messagebox.OK, Messagebox.INFORMATION);
					break;
				}
			}

			if (isValid) {
				Messagebox.show("Anda ingin update status done?", "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL,
						Messagebox.QUESTION, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									String error = "";
									for (Entry<Integer, Treturn> entry : mapData.entrySet()) {
										Treturn objreturpaket = entry.getValue();
										Session session = StoreHibernateUtil.openSession();
										Transaction transaction = session.beginTransaction();
										try {
											objreturpaket.setStatus(AppUtils.STATUS_RETUR_RETURNEDPFA);
											
											oDao.save(session, objreturpaket);

											totaldone = 0;
											List<Treturnitem> listreturitem = treturpaketDao.listByFilter(
													"treturn.treturnpk = " + objreturpaket.getTreturnpk(),
													"treturnitempk");
											for (Treturnitem objreturpaketitem : listreturitem) {
												Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '" + objreturpaketitem.getItemno() + "' and status = '" + objreturpaketitem.getItemstatus() + "'");
												if (objStock != null) {
													objStock.setStatus(AppUtils.STATUS_RETUR_RETURNEDPFA);
													new TbranchstockitemDAO().save(session, objStock);
													
													Tbranchitemtrack objTrack = new Tbranchitemtrack();
													objTrack.setItemno(objreturpaketitem.getItemno());
													objTrack.setTracktime(new Date());
													objTrack.setTrackdesc(AppData.getStatusLabel(objreturpaketitem.getItemstatus()));
													objTrack.setProductgroup(objreturpaketitem.getTreturn().getMproduct().getProductgroup());
													objTrack.setMproduct(objreturpaketitem.getTreturn().getMproduct());
													objTrack.setTrackstatus(AppUtils.STATUS_RETUR_RETURNEDPFA);
													new TbranchitemtrackDAO().save(session, objTrack);
												}
												objreturpaketitem.setItemstatus(objreturpaket.getStatus());
												objreturpaket.setStatus(AppUtils.STATUS_RETUR_RETURNEDPFA);
												treturpaketDao.save(session, objreturpaketitem);
											}

											Treturntrack objrt = new Treturntrack();
											objrt.setTreturn(objreturpaket);
											objrt.setTracktime(new Date());
											objrt.setTrackstatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
											objrt.setTrackdesc(AppData.getStatusLabel(objrt.getTrackstatus()));
											new TreturntrackDAO().save(session, objrt);

											Tpaket paket = new Tpaket();
											paket.setMproduct(objreturpaket.getMproduct());
											paket.setOrderdate(objreturpaket.getInserttime());
											paket.setPaketid(new TcounterengineDAO()
													.generateYearMonthCounter(AppUtils.CE_PAKET));
											paket.setProcessedby(oUser.getUserid());
											paket.setProcesstime(new Date());
											paket.setProductgroup(objreturpaket.getMproduct().getProductgroup());
											paket.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
											paket.setTotaldata(objreturpaket.getItemqty());
											paket.setTotaldone(1);
											paket.setTreturn(objreturpaket);
											paket.setBranchpool(oUser.getMbranch().getBranchid());
											new TpaketDAO().save(session, paket);

											Tpaketdata paketdata = new Tpaketdata();
											paketdata.setNopaket(new TcounterengineDAO().generateNopaket());
											paketdata.setIsdlv("N");
											paketdata.setMbranch(objreturpaket.getMbranch());
											paketdata.setOrderdate(paket.getOrderdate());
											paketdata.setPaketfinishby(oUser.getUserid());
											paketdata.setPaketfinishtime(new Date());
											paketdata.setPaketstartby(oUser.getUserid());
											paketdata.setPaketstarttime(new Date());
											paketdata.setProductgroup(paket.getProductgroup());
											paketdata.setQuantity(objreturpaket.getItemqty());
											paketdata.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
											paketdata.setTpaket(paket);
											new TpaketdataDAO().save(session, paketdata);

											transaction.commit();
										} catch (HibernateException e) {
											transaction.rollback();
											e.printStackTrace();
											if (error.length() > 0)
												error += ". \n";
											error += e.getMessage();
										} catch (Exception e) {
											e.printStackTrace();
											if (error.length() > 0)
												error += ". \n";
											error += e.getMessage();
										} finally {
											session.close();
										}
										
										Mmenu mmenu = new MmenuDAO()
												.findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
										NotifHandler.doNotif(mmenu, oUser.getMbranch(),
												objreturpaket.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
										
										mmenu = new MmenuDAO()
												.findByFilter("menupath = '/view/return/return.zul' and menuparamvalue = 'returpaket'");
										NotifHandler.delete(mmenu, objreturpaket.getMbranch(), objreturpaket.getMproduct().getProductgroup(),
												oUser.getMbranch().getBranchlevel());
									}

									if (error.length() > 0)
										Messagebox.show(error, WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.ERROR);
									else
										Messagebox.show("Proses update status done paket selesai",
												WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.INFORMATION);
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
									doResetListSelected();
									BindUtils.postNotifyChange(null, null, ReturpaketListVm.this, "*");
								}
							}
						});
			}
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (month != null && year != null) {
			filter = "extract(month from inserttime) = " + month + " and extract(year from inserttime) = " + year
					+ " and status in ('" + AppUtils.STATUS_RETUR_RETURNPFA + "', '" 
					+ AppUtils.STATUS_RETUR_RETURNOPR + "') and productgroup != '"
					+ AppUtils.PRODUCTGROUP_TOKEN + "'";
		}
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		status = AppUtils.STATUS_RETUR_RETURNPFA;
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

	public String getPaketid() {
		return paketid;
	}

	public Integer getTotaldone() {
		return totaldone;
	}

	public void setTotaldone(Integer totaldone) {
		this.totaldone = totaldone;
	}

	public void setPaketid(String paketid) {
		this.paketid = paketid;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public Date getProcesstime() {
		return processtime;
	}

	public void setProcesstime(Date processtime) {
		this.processtime = processtime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
}
