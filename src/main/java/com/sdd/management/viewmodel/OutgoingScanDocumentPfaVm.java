package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.ImmutableFields;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.lang.Library;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Table;
import org.zkoss.zhtml.Td;
import org.zkoss.zhtml.Tr;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TbranchitembucketDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TregisterstockDAO;
import com.sdd.caption.dao.TsecuritiesitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitembucket;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tregisterstock;
import com.sdd.caption.domain.Tsecuritiesitem;
import com.sdd.caption.handler.BranchStockManager;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.ItemTrackHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OutgoingScanDocumentPfaVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private Toutgoing obj;
	private Tsecuritiesitem objForm;
	private int outstanding, maxFlush, maxBatchCommit, flushCounter;
	private String itemno, itemnoend, memo, prefix, productgroup, filter;
	private Integer branchlevel, valouts, defvalouts, jumlahoutstanding;
	private boolean isValid;
	private String numoutstart;

	private ToutgoingDAO toutgoingDao = new ToutgoingDAO();
	private TsecuritiesitemDAO tsecuritiesitemDao = new TsecuritiesitemDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();
	private TbranchstockDAO tbranchstockDao = new TbranchstockDAO();

	private List<Tsecuritiesitem> securitiesitemList = new ArrayList<>();
	private List<Tsecuritiesitem> objItemList = new ArrayList<>();
	private List<String> listData = new ArrayList<>();
	private List<Tregisterstock> listRegstock = new ArrayList<>();
	private List<Tbranchitembucket> bucketList = new ArrayList<>();

	private Session session;
	private Transaction transaction;

	private Tr tr;

	@Wire
	private Window winItem;
	@Wire
	private Textbox tbPrefix, tbItem, tbItemend;
	@Wire
	private Button btnCheck;
	@Wire
	private Button btnSave, btnAdd;
	@Wire
	private Grid grid;
	@Wire
	private Button btnRegisterBatch;
	@Wire
	private Checkbox chkbox;
	@Wire
	private Tr trmemo;
	@Wire
	private Table table;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Toutgoing obj,
			@ExecutionArgParam("arg") String arg) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		branchlevel = oUser.getMbranch().getBranchlevel();
		maxFlush = Integer.parseInt(Library.getProperty("maxFlush"));
		maxBatchCommit = Integer.parseInt(Library.getProperty("maxBatchCommit"));
		this.obj = obj;

		if (arg != null)
			productgroup = arg;

		grid.setRowRenderer(new RowRenderer<Tsecuritiesitem>() {
			@Override
			public void render(final Row row, final Tsecuritiesitem data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getItemno()));
			}

		});
		doReset();
	}

	@Command
	@NotifyChange("*")
	public void doChecked() {
		if (chkbox.isChecked())
			trmemo.setVisible(true);
		else
			trmemo.setVisible(false);
	}

	@Command
	@NotifyChange("itemnoend")
	public void doChange() {
		try {
			itemnoend = String.valueOf(Integer.parseInt(itemno) + obj.getItemqty() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange({ "itemno", "itemnoend" })
	public void getStartno() {
		try {
			securitiesitemList = tsecuritiesitemDao
					.listNativeByFilter(
							"mproducttypefk = " + obj.getMproduct().getMproducttype().getMproducttypepk()
									+ " and prefix = '" + prefix.trim().toUpperCase()
									+ "' and tsecuritiesitem.status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "'",
							"tsecuritiesitempk");
			if (securitiesitemList.size() > 0) {
				itemno = String.valueOf(securitiesitemList.get(0).getNumerator());
				itemnoend = String.valueOf(Integer.parseInt(itemno) + obj.getItemqty() - 1);
			} else {
				Messagebox.show(
						"Tidak ada item dengan prefix " + prefix.toUpperCase() + " pada produk "
								+ obj.getMproduct().getMproducttype().getProducttype() + ".",
						"Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		itemno = "";
		objForm = null;
		filter = "";
		valouts = null;
		defvalouts = null;
		numoutstart = null;
		jumlahoutstanding = null;
		tbPrefix.setFocus(true);
		btnAdd.setDisabled(true);
		listData = new ArrayList<>();
		listRegstock = new ArrayList<>();
		securitiesitemList = new ArrayList<>();
		objItemList = new ArrayList<Tsecuritiesitem>();
		refresh();
	}

	@NotifyChange("*")
	public void refresh() {
		grid.setModel(new ListModelList<Tsecuritiesitem>(objItemList));
		outstanding = obj.getItemqty() - listData.size();
	}

	@Command
	@NotifyChange("*")
	public void doAdd() {
		if (outstanding <= 0) {
			Messagebox.show(
					"Tidak bisa menambah data karna jumlah data yang dimasukan sudah setara atau melebihi outstanding.",
					"Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			btnAdd.setDisabled(true);
			tr = new Tr();
			Td td = new Td();
			td.setColspan(2);
			Textbox txtPrefix = new Textbox();
			txtPrefix.setPlaceholder("Prefix");
			txtPrefix.setCols(5);
			txtPrefix.setMaxlength(5);
			Label label1 = new Label(" ");
			Textbox txtAwal = new Textbox();
			txtAwal.setPlaceholder("Entri Nomor Seri Awal");
			Label label2 = new Label(" ");
			Textbox txtAkhir = new Textbox();
			txtAkhir.setPlaceholder("Entri Nomor Seri Akhir");
			txtAkhir.setCols(20);
			txtAkhir.setMaxlength(40);
			Label label3 = new Label(" ");
			Button btn = new Button("Check Data");
			btn.setAutodisable("self");
			btn.setSclass("btn btn-info btn-sm");
			btn.setStyle("border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
			btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					if (txtPrefix == null || txtPrefix.getValue().trim().length() == 0)
						Messagebox.show("Silahkan isi Prefix data.", "Info", Messagebox.OK, Messagebox.INFORMATION);
					else if (txtAwal == null || txtAwal.getValue().trim().length() == 0)
						Messagebox.show("Silahkan isi data nomor item awal dengan data numerik.", "Info", Messagebox.OK,
								Messagebox.INFORMATION);
					else if (txtAkhir == null || txtAkhir.getValue().trim().length() == 0)
						Messagebox.show("Silahkan isi data nomor item akhir dengan data numerik.", "Info",
								Messagebox.OK, Messagebox.INFORMATION);
					else {
						getNumerator(txtPrefix.getValue(), txtAwal.getValue(), txtAkhir.getValue());

						if (isValid) {
							txtPrefix.setReadonly(true);
							txtAwal.setReadonly(true);
							txtAkhir.setReadonly(true);
							btn.setDisabled(true);
							btnAdd.setDisabled(false);
							refresh();
							BindUtils.postNotifyChange(null, null, OutgoingScanDocumentPfaVm.this, "outstanding");
						} else {
							Messagebox.show("Data yang dicari tidak ditemukan atau sudah masuk kedalam daftar.", "Info",
									Messagebox.OK, Messagebox.INFORMATION);
						}
					}
				}
			});

			td.appendChild(txtPrefix);
			td.appendChild(label1);
			td.appendChild(txtAwal);
			td.appendChild(label2);
			td.appendChild(txtAkhir);
			td.appendChild(label3);
			td.appendChild(btn);
			tr.appendChild(td);
			table.appendChild(tr);
		}
	}

	@NotifyChange("*")
	@Command
	public void doRegister() {
		try {
			outstanding = obj.getItemqty();
			if (outstanding > 0) {
				getNumerator(prefix.trim(), itemno, itemnoend);
				if (isValid) {
					tbPrefix.setReadonly(true);
					tbItem.setReadonly(true);
					tbItemend.setReadonly(true);
					btnCheck.setDisabled(true);
					btnAdd.setDisabled(false);
				}
			} else {
				Messagebox.show("Jumlah data sudah memenuhi jumlah order.", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
			tbPrefix.setFocus(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getNumerator(String prefix, String startno, String endno) {
		try {
			isValid = false;
			String incomingid = "";
			BigDecimal itemprice = new BigDecimal(0);
			if (branchlevel == 1) {
				securitiesitemList = tsecuritiesitemDao.listNativeByFilter("mproducttypefk = "
						+ obj.getMproduct().getMproducttype().getMproducttypepk() + " and prefix = '"
						+ prefix.trim().toUpperCase() + "' and numerator between " + startno.trim() + " and "
						+ endno.trim() + " and tsecuritiesitem.status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "'",
						"tsecuritiesitempk");

				int index = 0;
				if (securitiesitemList.size() > 0) {
					for (Tsecuritiesitem data : securitiesitemList) {
						if (!listData.contains(data.getItemno().trim())) {
							if (outstanding > 0) {
								objItemList.add(data);
								listData.add(data.getItemno().trim());
								outstanding = outstanding - 1;
								incomingid = data.getTincoming().getIncomingid();
								itemprice = data.getTincoming().getHarga();
								numoutstart = String.valueOf(data.getNumerator() + 1);
								index++;
							}
						}
					}

					Tbranchitembucket item = new Tbranchitembucket();
					item.setOrderid(obj.getTorder().getOrderid());
					item.setItemstartno(Integer.parseInt(startno.trim()));
					item.setItemendno(Integer.parseInt(startno.trim()) + index - 1);
					item.setCurrentno(Integer.parseInt(startno.trim()));
					item.setPrefix(prefix.trim());
					item.setOutlet(obj.getTorder().getOrderoutlet());
					item.setTotalitem(item.getItemendno() - item.getItemstartno() + 1);
					item.setOutbound(0);
					item.setIspod("N");
					item.setIsrunout("N");
					item.setIncomingid(incomingid);
					item.setItemprice(itemprice);
					item.setInserttime(new Date());
					bucketList.add(item);

					System.out.println("OUTSTANDING NUMERATOR AWAL : " + numoutstart);
					String upperprefix = prefix.toUpperCase().toString().trim();
					for (Tregisterstock relatedregisterproduct : new TregisterstockDAO().findByFilteruniq(
							"branch = '" + oUser.getMbranch().getBranchname().trim() + "' and productgroup = '"
									+ productgroup + "' and mproduct = '" + obj.getMproduct().getProductname().trim()
									+ "' and prefix = '" + upperprefix + "' and branchlevel = " + branchlevel)) {

						Tregisterstock objregstock = new Tregisterstock();
						objregstock.setBranch(oUser.getMbranch().getBranchname());
						objregstock.setProductgroup(productgroup);
						objregstock.setMproduct(obj.getMproduct().getProductname());
						objregstock.setTglincoming(relatedregisterproduct.getTglincoming());
						objregstock.setPrefix(prefix);
						objregstock.setNumerawalinc(relatedregisterproduct.getNumerawalinc());
						objregstock.setNumerakhirinc(relatedregisterproduct.getNumerakhirinc());
						objregstock.setJumlahinc(relatedregisterproduct.getJumlahinc());
						objregstock.setTgloutgoing(new Date());
						objregstock.setNumerawaloutg(prefix.trim() + startno);
						objregstock.setNumerakhiroutg(prefix.trim() + (Integer.parseInt(numoutstart) - 1));

						Integer valnumouts = 0;
						if (listRegstock.size() > 0) {
							filter += " and numerator not between " + startno + " and " + endno;
							Integer valoutg = Integer.valueOf(endno) - Integer.valueOf(startno) + 1;
							objregstock.setJumlahoutg(valoutg);
							valnumouts = valnumouts + valoutg;
						} else {
							filter = "status = 'S01' and tincomingfk = '"
									+ relatedregisterproduct.getTincomingfk().getTincomingpk()
									+ "' and numerator not between " + itemno + " and " + itemnoend;
							Integer valoutg = Integer.valueOf(itemnoend) - Integer.valueOf(itemno) + 1;
							objregstock.setJumlahoutg(valoutg);
							valnumouts = valnumouts + valoutg;
						}
						Integer relatedsecuritiesitemmin = new TregisterstockDAO().findByFilteroutstandingmin(filter);
						Integer relatedsecuritiesitemmax = new TregisterstockDAO().findByFilteroutstandingmax(filter);

						if (relatedsecuritiesitemmin != null)
							objregstock.setNumerawalouts(prefix.trim() + numoutstart);
						else
							objregstock.setNumerawalouts(relatedregisterproduct.getNumerawalinc());

						if (relatedsecuritiesitemmax != null)
							objregstock.setNumerakhirouts(prefix.trim() + relatedsecuritiesitemmax);
						else
							objregstock.setNumerakhirouts(relatedregisterproduct.getNumerakhirinc());

						if (listRegstock.size() > 0) {
							if (valouts != null) {
								valouts -= valouts - (Integer.valueOf(endno) - Integer.valueOf(itemno) + 1);

								if (defvalouts != null || defvalouts != 0)
									jumlahoutstanding = defvalouts - valouts;
								else
									jumlahoutstanding = 0;
							} else {
								valouts = 0;

								if (defvalouts != null || defvalouts != 0)
									jumlahoutstanding = defvalouts - valouts;
								else
									jumlahoutstanding = 0;
							}
							objregstock.setJumlahouts(jumlahoutstanding);
						} else {
							valouts = (Integer.parseInt(itemnoend) > relatedregisterproduct.getTincomingfk()
									.getItemqty() ? relatedregisterproduct.getTincomingfk().getItemqty()
											: Integer.parseInt(itemnoend))
									- Integer.valueOf(itemno) + 1;
							defvalouts = relatedregisterproduct.getJumlahouts() != 0
									? relatedregisterproduct.getJumlahouts()
									: 0;
							objregstock.setJumlahouts(relatedregisterproduct.getJumlahouts() - valouts);
						}
						objregstock.setTincomingfk(relatedregisterproduct.getTincomingfk());
						objregstock.setBranchlevel(oUser.getMbranch().getBranchlevel());

						listRegstock.add(objregstock);
					}

					isValid = true;
				} else {
					Messagebox.show("Data tidak ditemukan.", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			}
			tbPrefix.setFocus(true);
			refresh();
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
			if (branchlevel == 3) {
				status = AppUtils.STATUS_DELIVERY_DELIVERED;
			} else {
				status = AppUtils.STATUS_DELIVERY_PAKETDONE;
			}
			obj.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGSCAN);
			obj.setLastupdated(new Date());
			toutgoingDao.save(session, obj);

			if (listRegstock.size() > 0) {
				for (Tregisterstock registerstockdata : listRegstock) {
					new TregisterstockDAO().save(session, registerstockdata);
				}
			}

			obj.getTorder().setStatus(status);
			obj.getTorder().setTotalproses(listData.size());
			new TorderDAO().save(session, obj.getTorder());

			FlowHandler.doFlow(session, null, obj.getTorder(), AppData.getStatusLabel(obj.getStatus()),
					oUser.getUserid());
			transaction.commit();
			session.close();

			if (objItemList.size() > 0) {
				try {
					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					for (Tsecuritiesitem data : objItemList) {
						data.setStatus(AppUtils.STATUS_SERIALNO_OUTINVENTORY);
						tsecuritiesitemDao.save(session, data);

						Torderitem tori = new Torderitem();
						tori.setProductgroup(productgroup);
						tori.setPrefix(data.getTincoming().getPrefix());
						tori.setItemno(data.getItemno());
						tori.setNumerator(data.getNumerator());
						tori.setTorder(obj.getTorder());
						tori.setItemprice(data.getTincoming().getHarga());
						tori.setTsecuritiesitem(data);
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
			}

			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();

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
			paket.setBranchpool(oUser.getMbranch().getBranchid());
			new TpaketDAO().save(session, paket);

			Tpaketdata paketdata = new Tpaketdata();
			paketdata.setNopaket(new TcounterengineDAO().generateNopaket());
			paketdata.setIsdlv("N");
			if (branchlevel == 3)
				paketdata.setIsdlv("Y");

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
			paketdata.setOrderid(obj.getTorder().getOrderid());
			new TpaketdataDAO().save(session, paketdata);

			for (Tbranchitembucket bucketitem : bucketList) {
				new TbranchitembucketDAO().save(session, bucketitem);
			}

			if (branchlevel == 1) {
				Mproducttype objStock = mproducttypeDao
						.findByPk(obj.getMproduct().getMproducttype().getMproducttypepk());
				if (objStock != null) {
					objStock.setLaststock(objStock.getLaststock() - obj.getTorder().getTotalproses());
					mproducttypeDao.save(session, objStock);
				}
			} else if (branchlevel == 2) {
				Tbranchstock objStock = tbranchstockDao.findByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk()
						+ " and mproductfk = " + obj.getTorder().getMproduct().getMproductpk());
				if (objStock != null) {
					objStock.setStockactivated(objStock.getStockactivated() + obj.getTorder().getTotalproses());
					objStock.setStockcabang(objStock.getStockcabang() - obj.getTorder().getTotalproses());
					tbranchstockDao.save(session, objStock);
				}
			} else if (branchlevel == 3) {
				Tbranchstock objStock = tbranchstockDao.findByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk()
						+ " and mproductfk = " + obj.getTorder().getMproduct().getMproductpk() + " and outlet = '00'");
				if (objStock != null) {
					objStock.setStockactivated(objStock.getStockactivated() + obj.getItemqty());
					objStock.setStockcabang(objStock.getStockcabang() - obj.getTorder().getTotalproses());
					tbranchstockDao.save(session, objStock);
				}
				BranchStockManager.manageNonCard(obj.getTorder(), obj.getTorder().getProductgroup());
			}

			transaction.commit();
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (branchlevel < 3) {
			try {
				Mmenu mmenu = new MmenuDAO().findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
				NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getProductgroup(),
						oUser.getMbranch().getBranchlevel());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			Mmenu mmenu = new MmenuDAO()
					.findByFilter("menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'scan'");
			NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(), oUser.getMbranch().getBranchlevel());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Clients.showNotification("Proses verified data order berhasil", "info", null, "middle_center", 3000);
		Event closeEvent = new Event("onClose", winItem, new Boolean(true));
		Events.postEvent(closeEvent);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	public void doSave() {
		if (outstanding == 0) {
			doProcess();
		} else {
			if (listData.size() > 0) {
				Messagebox.show(
						obj.getMproduct().getProductname() + " yang dapat terpenuhi hanya berjumlah " + listData.size()
								+ ", apakah anda yakin ingin melanjutkan proses?",
						"Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {
							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									if (memo != null && memo.trim().length() > 0) {
										doProcess();

										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										Tordermemo objMemo = new Tordermemo();
										objMemo.setMemo(memo);
										objMemo.setMemoby(oUser.getUsername());
										objMemo.setMemotime(new Date());
										objMemo.setTorder(obj.getTorder());
										new TordermemoDAO().save(session, objMemo);
										transaction.commit();
										session.close();
									} else {
										Messagebox.show("Alasan pemenuhan harus diisi terlebih dahulu.", "Info",
												Messagebox.OK, Messagebox.INFORMATION);
									}
								}
							}
						});
			} else {
				Messagebox.show("Belum ada data.", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		}
	}

	public Validator getValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(ValidationContext ctx) {
				if (itemno == null || itemno.trim().length() == 0)
					this.addInvalidMessage(ctx, "itemno", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	@ImmutableFields
	public Tsecuritiesitem getObjForm() {
		return objForm;
	}

	public void setObjForm(Tsecuritiesitem objForm) {
		this.objForm = objForm;
	}

	public Toutgoing getObj() {
		return obj;
	}

	public void setObj(Toutgoing obj) {
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