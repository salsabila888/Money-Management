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
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class TokenScanEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TorderDAO oDao = new TorderDAO();
	private TtokenitemDAO ttiDao = new TtokenitemDAO();

	private Session session;
	private Transaction transaction;

	private Torder obj;
	private Ttokenitem objForm;
	private int total;
	private int outstanding;
	private String itemno;

	private List<Ttokenitem> inList = new ArrayList<>();
	private List<String> listData = new ArrayList<>();

	@Wire
	private Window winSerial;
	@Wire
	private Textbox tbItem;
	@Wire
	private Button btnRegister;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder obj)
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
				btn.setStyle("border-radius: 8px; background-color: #ce0012 !important; color: #ffffff !important;");
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
										BindUtils.postNotifyChange(null, null, TokenScanEntryVm.this, "outstanding");
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

			Window win = (Window) Executions.createComponents("/view/token/tokenscanbatch.zul", null, map);
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
						BindUtils.postNotifyChange(null, null, TokenScanEntryVm.this, "outstanding");
					}
				}
			});
		} else {
			Messagebox.show("Tidak bisa melakukan batch register karena data sudah memenuhi jumlah order", "Info",
					Messagebox.OK, Messagebox.INFORMATION);
		}
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
	@Command
	public void doRegister() {
		try {
			if (outstanding > 0) {
				String in = itemno.trim().substring(0, itemno.trim().length() - 1);
				if (!listData.contains(in)) {
					objForm = ttiDao.findById(in);
					if (objForm != null) {
						if (objForm.getStatus().equals(AppUtils.STATUS_SERIALNO_INJECTED)) {
							try {
								inList.add(objForm);
								listData.add(objForm.getItemno());
								refresh();
								itemno = "";
							} catch (Exception e) {
								Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
								e.printStackTrace();
							}
						} else if (objForm.getStatus().equals(AppUtils.STATUS_SERIALNO_ENTRY)) {
							Messagebox.show("Status token belum diverifikasi oleh inventori", "Info", Messagebox.OK,
									Messagebox.INFORMATION);
						} else {
							if (objForm.getStatus().equals(AppUtils.STATUS_SERIALNO_OUTPRODUKSI))
								Messagebox.show("Token sudah pernah discan", "Info", Messagebox.OK,
										Messagebox.INFORMATION);
							else
								Messagebox.show("Token tidak bisa discan", "Info", Messagebox.OK,
										Messagebox.INFORMATION);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doSave() {
		if (outstanding == 0) {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			try {
				obj.setTotalproses(inList.size());
				obj.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
				oDao.save(session, obj);

				for (Ttokenitem data : inList) {
					Torderitem ttorderitem = new Torderitem();
					ttorderitem.setTtokenitem(data);
					ttorderitem.setTorder(obj);
					ttorderitem.setProductgroup(AppUtils.PRODUCTGROUP_TOKEN);
					ttorderitem.setItemno(data.getItemno());
					ttorderitem.setItemprice(data.getTincoming().getHarga());
					new TorderitemDAO().save(session, ttorderitem);

					data.setStatus(AppUtils.STATUS_SERIALNO_OUTPRODUKSI);
					ttiDao.save(session, data);
				}
				
				String status = "";
				if (oUser.getMbranch().getBranchlevel() == 3) {
					status = AppUtils.STATUS_DELIVERY_DELIVERED;
				} else {
					status = AppUtils.STATUS_DELIVERY_PAKETDONE;
				}

				Tpaket paket = new Tpaket();
				paket.setMproduct(obj.getMproduct());
				paket.setOrderdate(obj.getInserttime());
				paket.setPaketid(new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_PAKET));
				paket.setProcessedby(oUser.getUserid());
				paket.setProcesstime(new Date());
				paket.setProductgroup(obj.getMproduct().getProductgroup());
				paket.setStatus(status);
				paket.setTotaldata(obj.getTotalproses());
				paket.setTotaldone(1);
				paket.setBranchpool(oUser.getMbranch().getBranchid());
				paket.setTorder(obj);
				new TpaketDAO().save(session, paket);

				Tpaketdata paketdata = new Tpaketdata();
				paketdata.setNopaket(new TcounterengineDAO().generateNopaket());
				paketdata.setIsdlv("N");
				if (oUser.getMbranch().getBranchlevel() == 3) {
					paketdata.setIsdlv("Y");
				}
				paketdata.setMbranch(obj.getMbranch());
				paketdata.setOrderdate(paket.getOrderdate());
				paketdata.setPaketfinishby(oUser.getUserid());
				paketdata.setPaketfinishtime(new Date());
				paketdata.setPaketstartby(oUser.getUserid());
				paketdata.setPaketstarttime(new Date());
				paketdata.setProductgroup(paket.getProductgroup());
				paketdata.setQuantity(obj.getTotalproses());
				paketdata.setStatus(status);
				paketdata.setTpaket(paket);
				new TpaketdataDAO().save(session, paketdata);
				
				Mproducttype objStock = obj.getMproduct().getMproducttype();
				objStock.setStockinjected(objStock.getStockinjected() - inList.size());
				new MproducttypeDAO().save(session, objStock);
				
				transaction.commit();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				session.close();
			}
			
			try {
				if (oUser.getMbranch().getBranchlevel() == 1) {
					Mmenu mmenu = new MmenuDAO().findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
					NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getProductgroup(),
							oUser.getMbranch().getBranchlevel());
				}
				
				Mmenu mmenu = new MmenuDAO().findByFilter(
						"menupath = '/view/order/orderdashboard.zul' and menuparamvalue = 'req'");
				NotifHandler.delete(mmenu, obj.getMbranch(), obj.getProductgroup(),
						oUser.getMbranch().getBranchlevel());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Clients.showNotification("Proses verified data order berhasil", "info", null, "middle_center", 3000);
			Event closeEvent = new Event("onClose", winSerial, new Boolean(true));
			Events.postEvent(closeEvent);
		} else {
			Messagebox.show("Masih ada data outstanding. Silahkan selesaikan proses verifikasinya", "Info",
					Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				if (itemno == null || itemno.trim().length() == 0)
					this.addInvalidMessage(ctx, "serialno", Labels.getLabel("common.validator.empty"));
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

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
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

}
