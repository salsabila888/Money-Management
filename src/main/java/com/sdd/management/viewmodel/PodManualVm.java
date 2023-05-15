package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
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
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
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
import com.sdd.caption.dao.TdeliverycourierDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverycourier;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.model.TdeliveryListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PodManualVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private TdeliveryListModel model;
	private Mbranch mbranch;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;

	private String vendorcode;
	private String dlvid;
	private String produk;
	private Date processtime;
	private Integer totaldata;
	private Integer totalselected;
	private Integer totaldataselected;
	private Date tglterima;
	private String penerima;

	private Tdelivery obj;
	private TdeliveryDAO oDao = new TdeliveryDAO();
	private Map<Integer, Tdelivery> mapData = new HashMap<>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Combobox cbCabang, cbProduk;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);

		oUser = (Muser) zkSession.getAttribute("oUser");

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
				chkAll.setChecked(false);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tdelivery>() {

			@Override
			public void render(Row row, final Tdelivery data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tdelivery obj = (Tdelivery) checked.getAttribute("obj");
						if (checked.isChecked()) {
							mapData.put(data.getTdeliverypk(), data);
							totaldataselected += obj.getTotaldata();
						} else {
							mapData.remove(data.getTdeliverypk());
							totaldataselected -= obj.getTotaldata();
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, PodManualVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, PodManualVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTdeliverypk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);

				A a = new A(data.getDlvid());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);

						Window win = (Window) Executions.createComponents("/view/delivery/deliverydata.zul", null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								Boolean isSaved = (Boolean) event.getData();
								if (isSaved != null && isSaved) {
									needsPageUpdate = true;
									refreshModel(pageStartNumber);
									BindUtils.postNotifyChange(null, null, PodManualVm.this, "pageTotalSize");
								}
							}
						});
						win.doModal();
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getProcesstime())));
				row.getChildren().add(new Label(data.getMbranch().getBranchid()));
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(String.valueOf(data.getTotaldata())));
				row.getChildren().add(new Label(data.getMcouriervendor().getVendorcode()));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
			}
		});
		doReset();
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Tdelivery obj = (Tdelivery) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getTdeliverypk(), obj);
							totaldataselected += obj.getTotaldata();
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getTdeliverypk());
							totaldataselected = 0;
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
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (mbranch != null) {
				filter = "tglterima is null and mbranchfk = " + mbranch.getMbranchpk() + " and tdelivery.status = '"
						+ AppUtils.STATUS_DELIVERY_DELIVERY + "'";

				if (produk.equals("09")) {
					filter += " and tdelivery.productgroup = '01' and isproductphoto = 'Y'";
				} else {
					filter += " and tdelivery.productgroup = '" + produk + "' and isproductphoto = 'N'";
				}

				if (dlvid != null && dlvid.length() > 0)
					filter += " and dlvid like '%" + dlvid.trim().toUpperCase() + "%'";
				if (vendorcode != null && vendorcode.length() > 0)
					filter += " and vendorcode like '%" + vendorcode.trim().toUpperCase() + "%'";
				if (processtime != null)
					filter += " and DATE(tdelivery.processtime) = '" + dateFormatter.format(processtime) + "'";

				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);

			}

		} catch (Exception e) {
			e.printStackTrace();
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
								BindUtils.postNotifyChange(null, null, PodManualVm.this, "*");
							}
						}
					});
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totaldata = 0;
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
		chkAll.setChecked(false);

		dlvid = "";
		vendorcode = "";
		processtime = null;

		produk = "01";
		penerima = null;
		tglterima = new Date();
		obj = new Tdelivery();
		pageTotalSize = 0;
		paging.setTotalSize(pageTotalSize);
		if (grid.getRows() != null) {
			grid.getRows().getChildren().clear();
		}
	}

	@Command
	@NotifyChange("*")
	public void refreshModel(int activePage) {
		try {
			orderby = "tdeliverypk desc";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new TdeliveryListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
	@NotifyChange("*")
	public void doManifest() {
		Session session = StoreHibernateUtil.openSession();
		Transaction transaction = session.beginTransaction();
		if (mapData.size() > 0) {
			if (tglterima == null || penerima == null || penerima.trim().length() == 0) {
				Messagebox.show("Silahkan isi Tanggal Terima dan Nama Penerima", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				try {
					Tdeliverycourier objForm = new Tdeliverycourier();
					objForm.setMcouriervendor(mbranch.getMcouriervendor());
					objForm.setProductgroup(produk);
					objForm.setDlvcourierid(new TcounterengineDAO().generateCounter(AppUtils.CE_EXPEDITION));
					objForm.setTotaldata(mapData.size());
					objForm.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
					objForm.setProcessedby(oUser.getUserid());
					objForm.setProcesstime(new Date());
					objForm.setIsurgent("N");
					objForm.setCourierbranchpool(oUser.getMbranch().getBranchid());
					new TdeliverycourierDAO().save(session, objForm);

					for (Entry<Integer, Tdelivery> entry : mapData.entrySet()) {
						Tdelivery obj = entry.getValue();
						obj.setTdeliverycourier(objForm);
						obj.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
						obj.setIsurgent("N");
						obj.setTglterima(tglterima);
						obj.setPenerima(penerima);
						oDao.save(session, obj);

						List<Tdeliverydata> deliverydataList = new TdeliverydataDAO()
								.listByFilter("tdeliveryfk = " + obj.getTdeliverypk(), "tdeliverydatapk");
						for (Tdeliverydata deliverydata : deliverydataList) {
							if (produk.equals(AppUtils.PRODUCTGROUP_CARD)) {
								Tembossbranch teb = deliverydata.getTpaketdata().getTembossbranch();
								teb.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
								new TembossbranchDAO().save(session, teb);
							} else if (produk.equals(AppUtils.PRODUCTGROUP_CARDPHOTO)) {
								Tderivatif tdrv = deliverydata.getTpaketdata().getTpaket().getTderivatifproduct()
										.getTderivatif();
								tdrv.setStatus(AppUtils.STATUS_DERIVATIF_DELIVERED);
								new TderivatifDAO().save(session, tdrv);
							} else if (produk.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
								Torder tord = deliverydata.getTpaketdata().getTpaket().getTorder();
								tord.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
								new TorderDAO().save(session, tord);
							}
						}
					}
					transaction.commit();
					Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center",
							3000);

					doReset();

				} catch (Exception e) {
					transaction.rollback();
					Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				} finally {
					session.close();
				}
			}
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}

	}

	public ListModelList<Mbranch> getMbranchmodel() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(AppData.getMbranch());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Tdelivery getObj() {
		return obj;
	}

	public void setObj(Tdelivery obj) {
		this.obj = obj;
	}

	public String getVendorcode() {
		return vendorcode;
	}

	public void setVendorcode(String vendorcode) {
		this.vendorcode = vendorcode;
	}

	public String getDlvid() {
		return dlvid;
	}

	public void setDlvid(String dlvid) {
		this.dlvid = dlvid;
	}

	public String getProduk() {
		return produk;
	}

	public void setProduk(String produk) {
		this.produk = produk;
	}

	public Date getProcesstime() {
		return processtime;
	}

	public void setProcesstime(Date processtime) {
		this.processtime = processtime;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
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

	public Date getTglterima() {
		return tglterima;
	}

	public void setTglterima(Date tglterima) {
		this.tglterima = tglterima;
	}

	public String getPenerima() {
		return penerima;
	}

	public void setPenerima(String penerima) {
		this.penerima = penerima;
	}

}
