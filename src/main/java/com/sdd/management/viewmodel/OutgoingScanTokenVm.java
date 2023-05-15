/**
 * 
 */
package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Tr;
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
import org.zkoss.zul.Div;
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
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OutgoingScanTokenVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private ToutgoingDAO toutgoingDao = new ToutgoingDAO();
	private TtokenitemDAO ttokenitemDao = new TtokenitemDAO();

	private Session session;
	private Transaction transaction;

	private Toutgoing obj;
	private Ttokenitem objForm;
	private int outstanding;
	private String itemno;
	private String memo;

	private List<Ttokenitem> inList = new ArrayList<>();
	private List<String> listData = new ArrayList<>();

	@Wire
	private Window winScanToken;
	@Wire
	private Textbox tbItem;
	@Wire
	private Button btnRegister;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;
	@Wire
	private Button btnRegisterBatch;
	@Wire
	private Tr trmemo;
	@Wire
	private Checkbox cbUnit;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Toutgoing obj)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.obj = obj;

		grid.setRowRenderer(new RowRenderer<Ttokenitem>() {

			@Override
			public void render(final Row row, final Ttokenitem data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getItemno()));
				Button btn = new Button("Cancel");
				btn.setAutodisable("self");
				btn.setSclass("btn btn-danger btn-sm");
				btn.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important; float: right !important;");
				btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
								Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

									@Override
									public void onEvent(Event event) throws Exception {
										inList.remove(data);
										listData.remove(data.getItemno().trim());
										refresh();
										BindUtils.postNotifyChange(null, null, OutgoingScanTokenVm.this, "outstanding");
									}
								});
					}
				});

				Div div = new Div();
				div.appendChild(btn);
				row.getChildren().add(div);
			}
		});

		doReset();
	}

	@Command
	@NotifyChange("*")
	public void doCheck() {
		if (cbUnit.isChecked()) {
			trmemo.setVisible(true);
		} else
			trmemo.setVisible(false);

	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		itemno = "";
		objForm = null;
		tbItem.setFocus(true);
		listData = new ArrayList<>();
		inList = new ArrayList<>();
		refresh();
	}

	@NotifyChange("*")
	public void refresh() {
		grid.setModel(new ListModelList<Ttokenitem>(inList));
		outstanding = obj.getItemqty() - inList.size();
	}

	@NotifyChange("*")
	@Command
	public void doRegisterBatch() {
		if (outstanding > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("obj", obj);
			map.put("outstanding", outstanding);
			map.put("listData", listData);

			Window win = (Window) Executions.createComponents("/view/inventory/outgoingscanbatch.zul", null, map);
			win.setWidth("50%");
			win.setClosable(true);
			win.doModal();
			win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

				@SuppressWarnings("unchecked")
				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getData() != null) {
						for (Ttokenitem data : (List<Ttokenitem>) event.getData()) {
							inList.add(data);
							listData.add(data.getItemno());
						}
						refresh();
						BindUtils.postNotifyChange(null, null, OutgoingScanTokenVm.this, "outstanding");
					}
				}
			});
		} else {
			Messagebox.show("Tidak bisa melakukan batch register karena data sudah memenuhi jumlah order", "Info",
					Messagebox.OK, Messagebox.INFORMATION);
		}

	}

	@NotifyChange("*")
	@Command
	public void doRegister() {
		try {
			if (outstanding > 0) {
				String in = "";
				if (obj.getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
					in = itemno.trim().substring(0, itemno.trim().length() - 1);
				} else {
					in = itemno.trim();
				}
				if (!listData.contains(in)) {
					if (obj.getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN))
						objForm = ttokenitemDao.findById(itemno.trim().substring(0, itemno.trim().length() - 1));
					else
						objForm = ttokenitemDao.findById(itemno.trim());
					if (objForm != null) {
						if (objForm.getStatus().equals(AppUtils.STATUS_SERIALNO_ENTRY)) {
							try {
								inList.add(objForm);
								listData.add(objForm.getItemno());
								refresh();
								itemno = "";
							} catch (Exception e) {
								Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
								e.printStackTrace();
							}
						} else {
							Messagebox.show("Data sudah keluar", "Info", Messagebox.OK, Messagebox.INFORMATION);
						}
					} else {
						Messagebox.show("Data tidak ditemukan", "Info", Messagebox.OK, Messagebox.INFORMATION);
					}
				} else {
					Messagebox.show("Duplicate Data", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Jumlah data sudah memenuhi jumlah order", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
			tbItem.setFocus(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	public void doSave() {
		if (outstanding == 0) {
			try {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				obj.setStatus(AppUtils.STATUS_ORDER_PRODUKSI);
				obj.setLastupdated(new Date());
				toutgoingDao.save(session, obj);

				obj.getTorder().setStatus(AppUtils.STATUS_ORDER_PRODUKSI);
				obj.getTorder().setTotalproses(inList.size());
				new TorderDAO().save(session, obj.getTorder());

				for (Ttokenitem data : inList) {
					data.setStatus(AppUtils.STATUS_SERIALNO_OUTINVENTORY);
					ttokenitemDao.save(session, data);

					Torderitem tori = new Torderitem();
					tori.setProductgroup(AppUtils.PRODUCTGROUP_TOKEN);
					tori.setItemno(data.getItemno());
					tori.setTtokenitem(data);
					tori.setItemprice(data.getTincoming().getHarga());
					tori.setTorder(obj.getTorder());
					new TorderitemDAO().save(session, tori);

				}

				Tordermemo objMemo = new Tordermemo();
				objMemo.setMemo(memo);
				objMemo.setMemoby(oUser.getUsername());
				objMemo.setMemotime(new Date());
				objMemo.setTorder(obj.getTorder());
				new TordermemoDAO().save(session, objMemo);
				
				Mproducttype objStock = obj.getMproduct().getMproducttype();
				objStock.setLaststock(objStock.getLaststock() - inList.size());
				objStock.setStockreserved(objStock.getStockreserved() + inList.size());
				new MproducttypeDAO().save(session, objStock);

				transaction.commit();
				session.close();
				
				Mmenu mmenu = new MmenuDAO().findByFilter(
						"menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'scan'");
				NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(),
						oUser.getMbranch().getBranchlevel());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Clients.showNotification("Proses verified data order berhasil", "info", null, "middle_center", 3000);
			Event closeEvent = new Event("onClose", winScanToken, new Boolean(true));
			Events.postEvent(closeEvent);

		} else {
			if (inList.size() > 0) {
				Messagebox.show(
						obj.getMproduct().getProductname() + " yang terpenuhi berjumlah " + inList.size()
								+ ", apakah anda yakin ingin melanjutkan proses?",
						"Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									if (memo != null && memo.trim().length() > 0) {
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										try {
											obj.setItemqty(inList.size());
											obj.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGSCAN);
											obj.setLastupdated(new Date());
											toutgoingDao.save(session, obj);

											obj.getTorder().setTotalproses(inList.size());
											obj.getTorder().setStatus(AppUtils.STATUS_ORDER_PRODUKSI);
											new TorderDAO().save(session, obj.getTorder());

											for (Ttokenitem data : inList) {
												data.setStatus(AppUtils.STATUS_SERIALNO_OUTINVENTORY);
												ttokenitemDao.save(session, data);

												Torderitem tori = new Torderitem();
												tori.setProductgroup(AppUtils.PRODUCTGROUP_TOKEN);
												tori.setItemno(data.getItemno());
												tori.setTtokenitem(data);
												tori.setTorder(obj.getTorder());
												new TorderitemDAO().save(session, tori);

											}

											Tordermemo objMemo = new Tordermemo();
											objMemo.setMemo(memo);
											objMemo.setMemoby(oUser.getUsername());
											objMemo.setMemotime(new Date());
											objMemo.setTorder(obj.getTorder());
											new TordermemoDAO().save(session, objMemo);

											transaction.commit();
										} catch (Exception e) {
											e.printStackTrace();
										} finally {
											session.close();
										}
										
										Mmenu mmenu = new MmenuDAO()
												.findByFilter("menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'scan'");
										NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(),
												oUser.getMbranch().getBranchlevel());

										Clients.showNotification("Proses verified data order berhasil", "info", null,
												"middle_center", 3000);
										Event closeEvent = new Event("onClose", winScanToken, new Boolean(true));
										Events.postEvent(closeEvent);
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
	public Ttokenitem getObjForm() {
		return objForm;
	}

	public void setObjForm(Ttokenitem objForm) {
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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Tr getTrmemo() {
		return trmemo;
	}

	public void setTrmemo(Tr trmemo) {
		this.trmemo = trmemo;
	}

	public Checkbox getCbUnit() {
		return cbUnit;
	}

	public void setCbUnit(Checkbox cbUnit) {
		this.cbUnit = cbUnit;
	}

}
