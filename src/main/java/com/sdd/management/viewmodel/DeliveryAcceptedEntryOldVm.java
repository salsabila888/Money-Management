package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverycourierDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverycourier;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.model.TdeliveryListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DeliveryAcceptedEntryOldVm {
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

	private Tdelivery obj;
	private TdeliveryDAO oDao = new TdeliveryDAO();
	private List<Tdelivery> objSelected = new ArrayList<Tdelivery>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Datebox datebox;
	@Wire
	private Combobox cbCabang, cbProduk;
	@Wire
	private Textbox penerima;

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
						if (checked.isChecked())
							objSelected.add((Tdelivery) checked.getAttribute("obj"));
						else
							objSelected.remove((Tdelivery) checked.getAttribute("obj"));
					}
				});
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
									BindUtils.postNotifyChange(null, null, DeliveryAcceptedEntryOldVm.this,
											"pageTotalSize");
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
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				objSelected = new ArrayList<Tdelivery>();
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					if (checked) {
						chk.setChecked(true);
						objSelected.add((Tdelivery) chk.getAttribute("obj"));
					} else {
						chk.setChecked(false);
						objSelected.remove((Tdelivery) chk.getAttribute("obj"));
					}
				}
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
				filter = "tglterima is null and tdelivery.productgroup = '" + produk + "' and mbranchfk = "
						+ mbranch.getMbranchpk();

				if (produk.equals("09"))
					filter += " and isproductphoto = 'Y'";
				else
					filter += " and isproductphoto = 'N'";

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

	@Command
	@NotifyChange("*")
	public void doReset() {
		dlvid = "";
		vendorcode = "";
		processtime = null;

		produk = "01";
		penerima.setValue(null);
		datebox.setValue(new Date());
		obj = new Tdelivery();
		objSelected = new ArrayList<Tdelivery>();
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
		if (objSelected.size() > 0) {
			if (penerima.getValue().equals("") || penerima.getValue() == null) {
				Messagebox.show("Silahkan masukan tgl terima", "Info", Messagebox.OK, Messagebox.INFORMATION);
			} else {
				try {
					Tdeliverycourier objForm = new Tdeliverycourier();
					objForm.setMcouriervendor(mbranch.getMcouriervendor());
					objForm.setProductgroup(produk);
					objForm.setDlvcourierid(new TcounterengineDAO().generateCounter(AppUtils.CE_EXPEDITION));
					objForm.setTotaldata(objSelected.size());
					objForm.setStatus(AppUtils.STATUS_DELIVERY_DELIVERY);
					objForm.setProcessedby(oUser.getUserid());
					objForm.setProcesstime(new Date());
					objForm.setIsurgent("N");
					new TdeliverycourierDAO().save(session, objForm);

					for (Tdelivery obj : objSelected) {
						obj.setTdeliverycourier(objForm);
						obj.setStatus(AppUtils.STATUS_DELIVERY_DELIVERY);
						obj.setIsurgent("N");
						obj.setTglterima(datebox.getValue());
						obj.setPenerima(penerima.getValue());
						oDao.save(session, obj);

						List<Tdeliverydata> deliverydataList = new TdeliverydataDAO()
								.listByFilter("tdeliveryfk = " + obj.getTdeliverypk(), "tdeliverydatapk");
						for (Tdeliverydata deliverydata : deliverydataList) {
							if (deliverydata.getTpaketdata().getTembossbranch() != null) {
								FlowHandler.doFlow(session, deliverydata.getTpaketdata().getTembossbranch(), null,
										AppUtils.PROSES_POD,
										deliverydata.getTpaketdata().getTembossbranch().getTembossfile().getMemo(),
										oUser.getUserid());
							} else {
								FlowHandler.doFlow(session, null, deliverydata.getTpaketdata().getTpaket().getTorder(),
										AppUtils.PROSES_POD,
										deliverydata.getTpaketdata().getTpaket().getTorder().getMemo(),
										oUser.getUserid());
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
			Messagebox.show("Silahkan pilih data untuk proses submit", "Info", Messagebox.OK, Messagebox.INFORMATION);
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

}
