/**
 * 
 */
package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
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
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.lang.Library;
import org.zkoss.zhtml.Table;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TbranchitembucketDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitembucket;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DocumentSwitchingVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TswitchDAO tswitchDao = new TswitchDAO();
	private TbranchstockDAO tbranchstockDao = new TbranchstockDAO();
	private TbranchitembucketDAO bucketDao = new TbranchitembucketDAO();

	private Session session;
	private Transaction transaction;

	private Tswitch obj;
	private int outstanding, maxFlush, maxBatchCommit, flushCounter;
	private String itemno;
	private String itemnoend;
	private String memo;
	private String prefix;
	private boolean isUse;
	private int lastcount;

	private List<Tbranchitembucket> bucketList = new ArrayList<Tbranchitembucket>();
	private List<Tbranchitembucket> bucketUsedList = new ArrayList<Tbranchitembucket>();

	private Map<Integer, Integer> mapQty = new HashMap<Integer, Integer>();
	private Map<Integer, Tbranchstock> mapStock = new HashMap<Integer, Tbranchstock>();

	@Wire
	private Window winItem;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;
	@Wire
	private Table table;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tswitch obj)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		maxFlush = Integer.parseInt(Library.getProperty("maxFlush"));
		maxBatchCommit = Integer.parseInt(Library.getProperty("maxBatchCommit"));
		this.obj = obj;

		doReset();
		grid.setRowRenderer(new RowRenderer<Tbranchitembucket>() {
			@Override
			public void render(final Row row, final Tbranchitembucket data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getPrefix()));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getCurrentno())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getItemendno())));
				row.getChildren()
						.add(new Label(NumberFormat.getInstance().format(data.getTotalitem() - data.getOutbound())));

				Intbox intbox = new Intbox();
				intbox.setStyle("text-align:right");
				intbox.setFormat("#,###");
				intbox.setCols(12);
				intbox.setMaxlength(12);
				intbox.setPlaceholder("0");

				Button btnAdd = new Button("Simpan");
				btnAdd.setAutodisable("self");
				btnAdd.setClass("btn-default");
				btnAdd.setStyle("border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");

				if (!isUse) {
					intbox.setDisabled(true);
					btnAdd.setStyle("border-radius: 8px;");
					btnAdd.setDisabled(true);
				}

				if (mapQty.get(data.getTbranchitembucketpk()) != null) {
					intbox.setValue(mapQty.get(data.getTbranchitembucketpk()));
					intbox.setDisabled(true);
					btnAdd.setStyle("border-radius: 8px;");
					btnAdd.setDisabled(true);
					isUse = true;
				} else {
					isUse = false;
					intbox.setValue(0);
				}

				Div div = new Div();
				div.appendChild(intbox);
				row.getChildren().add(div);

				btnAdd.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (intbox.getValue() <= (data.getTotalitem() - data.getOutbound())) {
							if (intbox.getValue() <= outstanding) {
								outstanding = outstanding - intbox.getValue();
								if (intbox.getValue() != null && intbox.getValue() > 0) {
									mapQty.put(data.getTbranchitembucketpk(), intbox.getValue());
									bucketUsedList.add(data);
									intbox.setDisabled(true);
									btnAdd.setDisabled(true);
									btnAdd.setStyle("border-radius: 8px;");
									if ((data.getTotalitem() - data.getOutbound()) < (outstanding
											+ intbox.getValue())) {
										doRenderBucket();
									}
								}
								BindUtils.postNotifyChange(null, null, DocumentSwitchingVm.this, "outstanding");
							} else {
								intbox.setValue(0);
								Messagebox.show("Jumlah data yang dimasukan melebihi outstanding.", "Info",
										Messagebox.OK, Messagebox.INFORMATION);
							}
						} else {
							intbox.setValue(0);
							Messagebox.show("Jumlah data yang dimasukan melebihi jumlah stok yang tersedia.", "Info",
									Messagebox.OK, Messagebox.INFORMATION);
						}
					}
				});

				Div divBtn = new Div();
				divBtn.appendChild(btnAdd);
				row.appendChild(divBtn);
			}
		});

	}

	@NotifyChange("*")
	public void doRenderBucket() {
		try {
			String filterBranchstock = "mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and mproductfk = "
					+ obj.getMproduct().getMproductpk() + " and outlet = '" + obj.getOutletpool() + "'";
			Tbranchstock objStock = tbranchstockDao.findByFilter(filterBranchstock);
			if (objStock != null) {
				String filter = "tbranchstockfk = " + objStock.getTbranchstockpk()
						+ " and ispod = 'Y' and isrunout = 'N'";
				bucketList = bucketDao.listNativeByFilter(filter, "tbranchitembucketpk");
			}
			lastcount = bucketList.size();
			System.out.println("LAST COUNT : " + lastcount);
			grid.setModel(new ListModelList<Tbranchitembucket>(bucketList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		try {
			outstanding = obj.getItemqty();
			isUse = true;
			mapQty = new HashMap<Integer, Integer>();
			mapStock = new HashMap<Integer, Tbranchstock>();
			bucketUsedList = new ArrayList<Tbranchitembucket>();
			bucketList = new ArrayList<Tbranchitembucket>();
			doRenderBucket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doProcess() {
		try {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			String status = "";
			flushCounter = 0;
			if (!obj.getOutletreq().equals("00")) {
				status = AppUtils.STATUS_DELIVERY_DELIVERY;
			} else {
				status = AppUtils.STATUS_DELIVERY_PAKETDONE;
			}

			obj.setStatus(status);
			tswitchDao.save(session, obj);

			obj.getTorder().setStatus(status);
			obj.getTorder().setTotalproses(obj.getTorder().getTotalproses() - outstanding);
			new TorderDAO().save(session, obj.getTorder());
			
			FlowHandler.doFlow(session, null, obj.getTorder(), "", oUser.getUserid());
			transaction.commit();
			session.close();

			for (Tbranchitembucket bucketitem : bucketUsedList) {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();

				Tbranchitembucket item = new Tbranchitembucket();
				item.setOrderid(obj.getTorder().getOrderid());
				item.setItemstartno(bucketitem.getCurrentno());
				item.setItemendno(bucketitem.getCurrentno() + mapQty.get(bucketitem.getTbranchitembucketpk()) - 1);
				item.setCurrentno(item.getItemstartno());
				item.setPrefix(bucketitem.getPrefix());
				item.setOutlet(obj.getTorder().getOrderoutlet());
				item.setTotalitem(mapQty.get(bucketitem.getTbranchitembucketpk()));
				item.setOutbound(0);
				item.setIspod("N");
				item.setIsrunout("N");
				item.setIncomingid(bucketitem.getIncomingid());
				item.setItemprice(bucketitem.getItemprice());
				new TbranchitembucketDAO().save(session, item);

				bucketitem.setCurrentno(item.getItemendno() + 1);
				bucketitem.setOutbound(bucketitem.getOutbound() + mapQty.get(bucketitem.getTbranchitembucketpk()));
				if (bucketitem.getTotalitem() <= bucketitem.getOutbound()) {
					bucketitem.setIsrunout("Y");
					bucketitem.setCurrentno(item.getItemendno());
				} else {
					bucketitem.setCurrentno(item.getItemendno() + 1);
				}
				new TbranchitembucketDAO().save(session, bucketitem);

				transaction.commit();
				session.close();

				try {
					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();

					List<Tbranchstockitem> itemList = new TbranchstockitemDAO().listNativeByFilter(
							"tbranchitembucketfk = " + bucketitem.getTbranchitembucketpk() + " and numerator between "
									+ item.getItemstartno() + " and " + item.getItemendno()
									+ " and tbranchstockitem.status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "'",
							"tbranchstockitempk");

					for (Tbranchstockitem data : itemList) {
						data.setStatus(AppUtils.STATUS_SERIALNO_OUTINVENTORY);
						new TbranchstockitemDAO().save(session, data);

						Torderitem tori = new Torderitem();
						tori.setProductgroup(AppUtils.PRODUCTGROUP_DOCUMENT);
						tori.setPrefix(item.getPrefix());
						tori.setItemno(data.getItemno());
						tori.setNumerator(data.getNumerator());
						tori.setTorder(obj.getTorder());
						tori.setItemprice(data.getItemprice());
						new TorderitemDAO().save(session, tori);

						if (flushCounter % maxFlush == 0) {
							session.flush();
							session.clear();
						}

						if (flushCounter % maxBatchCommit == 0) {
							transaction.commit();
							session.close();

							session = StoreHibernateUtil.openSession();
							transaction = session.beginTransaction();
						}
						flushCounter++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (session.isOpen()) {
						transaction.commit();
						session.close();
					}
				}

				mapStock.put(bucketitem.getTbranchstock().getTbranchstockpk(), bucketitem.getTbranchstock());
			}

			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();

			for (Entry<Integer, Tbranchstock> entry : mapStock.entrySet()) {
				Tbranchstock objStock = entry.getValue();
				objStock.setStockactivated(objStock.getStockactivated() + obj.getTorder().getTotalproses());
				objStock.setStockcabang(objStock.getStockcabang() - obj.getTorder().getTotalproses());
				tbranchstockDao.save(session, objStock);

			}

			Tpaket paket = new Tpaket();
			paket.setMproduct(obj.getMproduct());
			paket.setOrderdate(obj.getTorder().getOrderdate());
			paket.setPaketid(new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_PAKET));
			paket.setProcessedby(oUser.getUserid());
			paket.setProcesstime(new Date());
			paket.setProductgroup(obj.getMproduct().getProductgroup());
			paket.setStatus(status);
			paket.setTotaldata(obj.getTorder().getTotalproses());
			paket.setTotaldone(1);
			paket.setTorder(obj.getTorder());
			paket.setTswitch(obj);
			paket.setBranchpool(oUser.getMbranch().getBranchid());
			new TpaketDAO().save(session, paket);

			Tpaketdata paketdata = new Tpaketdata();
			paketdata.setNopaket(new TcounterengineDAO().generateNopaket());
			paketdata.setIsdlv("N");
			if (!obj.getOutletreq().equals("00")) {
				paketdata.setIsdlv("Y");
			}
			paketdata.setMbranch(obj.getTorder().getMbranch());
			paketdata.setOrderdate(paket.getOrderdate());
			paketdata.setPaketfinishby(oUser.getUserid());
			paketdata.setPaketfinishtime(new Date());
			paketdata.setPaketstartby(oUser.getUserid());
			paketdata.setPaketstarttime(new Date());
			paketdata.setProductgroup(paket.getProductgroup());
			paketdata.setQuantity(obj.getTorder().getTotalproses());
			paketdata.setStatus(status);
			paketdata.setTpaket(paket);
			new TpaketdataDAO().save(session, paketdata);

			transaction.commit();
			session.close();

			try {
				if (obj.getOutletreq().equals("00")) {
					Mmenu mmenu = new MmenuDAO().findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
					NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
							oUser.getMbranch().getBranchlevel());
				}
				Mmenu mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/switching/switching.zul' and menuparamvalue = 'listpool'");
				NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
						oUser.getMbranch().getBranchlevel());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doSave() {
		if (outstanding == 0) {
			doProcess();

			Clients.showNotification("Proses switching data berhasil", "info", null, "middle_center", 3000);
			Event closeEvent = new Event("onClose", winItem, new Boolean(true));
			Events.postEvent(closeEvent);
		} else {
			Messagebox.show(
					"Tidak dapat melanjutkan proses karena masih terdapat jumlah outstanding : " + outstanding + ".",
					"Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	public Tswitch getObj() {
		return obj;
	}

	public void setObj(Tswitch obj) {
		this.obj = obj;
	}

	public int getOutstanding() {
		return outstanding;
	}

	public void setOutstanding(int outstanding) {
		this.outstanding = outstanding;
	}

	public String getItemno() {
		return itemno;
	}

	public void setItemno(String itemno) {
		this.itemno = itemno;
	}

	public String getItemnoend() {
		return itemnoend;
	}

	public void setItemnoend(String itemnoend) {
		this.itemnoend = itemnoend;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
