package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
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
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TrepairDAO;
import com.sdd.caption.dao.TrepairitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Trepair;
import com.sdd.caption.domain.Trepairitem;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class RepairCheckingVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Trepair obj;
	private TrepairDAO oDao = new TrepairDAO();
	private TrepairitemDAO itemDao = new TrepairitemDAO();

	private List<Trepairitem> vendorList = new ArrayList<>();
	private List<Trepairitem> objList = new ArrayList<>();

	private Integer totaldata;
	private Integer totalvendor;
	private String filter;

	@Wire
	private Grid grid, gridVendor;
	@Wire
	private Window winSerial;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Trepair obj)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.obj = obj;

		doReset();
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Trepairitem>() {

				@Override
				public void render(final Row row, final Trepairitem data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked()) {
								vendorList.add(data);
								objList.remove(data);
							}

							doRefresh();
							BindUtils.postNotifyChange(null, null, RepairCheckingVm.this, "totaldata");
							BindUtils.postNotifyChange(null, null, RepairCheckingVm.this, "totalvendor");
						}
					});
					row.getChildren().add(check);
					row.getChildren().add(new Label(data.getItemno() != null ? data.getItemno() : "-"));
					row.getChildren().add(new Label(data.getTid() != null ? data.getTid() : "-"));
					row.getChildren().add(new Label(data.getMid() != null ? data.getMid() : "-"));
					row.getChildren().add(new Label(
							data.getPinpadtype() != null ? AppData.getPinpadtypeLabel(data.getPinpadtype()) : "-"));
				}
			});
		}

		if (gridVendor != null) {
			gridVendor.setRowRenderer(new RowRenderer<Trepairitem>() {

				@Override
				public void render(final Row row, final Trepairitem data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getItemno() != null ? data.getItemno() : "-"));
					row.getChildren().add(new Label(data.getTid() != null ? data.getTid() : "-"));
					row.getChildren().add(new Label(data.getMid() != null ? data.getMid() : "-"));
					row.getChildren().add(new Label(
							data.getPinpadtype() != null ? AppData.getPinpadtypeLabel(data.getPinpadtype()) : "-"));

					Button btn = new Button("Cancel");
					btn.setStyle(
							"border-radius: 8px; background-color: #ce0012 !important; color: #ffffff !important; float: right !important;");
					btn.setAutodisable("self");
					btn.setSclass("btn btn-danger btn-sm");
					btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
									Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

										@Override
										public void onEvent(Event event) throws Exception {
											vendorList.remove(data);
											objList.add(data);
											doRefresh();
											BindUtils.postNotifyChange(null, null, RepairCheckingVm.this, "totaldata");
											BindUtils.postNotifyChange(null, null, RepairCheckingVm.this,
													"totalvendor");
										}
									});
						}
					});

					Div div = new Div();
					div.appendChild(btn);
					row.getChildren().add(div);
				}
			});
		}
	}

	@NotifyChange("*")
	public void doReset() {
		try {
			filter = "trepairfk = " + obj.getTrepairpk();
			objList = itemDao.listNativeByFilter(filter, "trepairitempk");
			doRefresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doRefresh() {
		try {
			grid.setModel(new ListModelList<Trepairitem>(objList));
			gridVendor.setModel(new ListModelList<Trepairitem>(vendorList));

			totaldata = objList.size();
			totalvendor = vendorList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("*")
	public void doSave() {
		Messagebox.show("Apakah anda ingin melanjutkan proses checking?", "Confirm Dialog",
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getName().equals("onOK")) {
							Session session = StoreHibernateUtil.openSession();
							Transaction transaction = session.beginTransaction();
							try {
								obj.setStatus(AppUtils.STATUS_REPAIR_PENDINGPROCESS);
								oDao.save(session, obj);

								for (Trepairitem data : objList) {
									Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
											+ data.getItemno() + "' and status = '" + data.getItemstatus() + "'");
									if (objStock != null) {
										objStock.setStatus(AppUtils.STATUS_REPAIR_PROCESSOPR);
										new TbranchstockitemDAO().save(session, objStock);

										Tbranchitemtrack objTrack = new Tbranchitemtrack();
										objTrack.setItemno(data.getItemno());
										objTrack.setTracktime(new Date());
										objTrack.setTrackdesc(AppData.getStatusLabel(data.getItemstatus()));
										objTrack.setProductgroup(data.getTrepair().getMproduct().getProductgroup());
										objTrack.setMproduct(data.getTrepair().getMproduct());
										objTrack.setTrackstatus(obj.getStatus());
										new TbranchitemtrackDAO().save(session, objTrack);
									}
									data.setItemstatus(AppUtils.STATUS_REPAIR_PROCESSOPR);
									itemDao.save(session, data);
								}

								for (Trepairitem data : vendorList) {
									Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
											+ data.getItemno() + "' and status = '" + data.getItemstatus() + "'");
									if (objStock != null) {
										objStock.setStatus(AppUtils.STATUS_REPAIR_PROCESSVENDOR);
										new TbranchstockitemDAO().save(session, objStock);

										Tbranchitemtrack objTrack = new Tbranchitemtrack();
										objTrack.setItemno(data.getItemno());
										objTrack.setTracktime(new Date());
										objTrack.setTrackdesc(AppData.getStatusLabel(data.getItemstatus()));
										objTrack.setProductgroup(data.getTrepair().getMproduct().getProductgroup());
										objTrack.setMproduct(data.getTrepair().getMproduct());
										objTrack.setTrackstatus(obj.getStatus());
										new TbranchitemtrackDAO().save(session, objTrack);
									}
									data.setItemstatus(AppUtils.STATUS_REPAIR_PROCESSVENDOR);
									itemDao.save(session, data);
								}
								transaction.commit();

								Clients.showNotification("Submit data berhasil", "info", null, "middle_center", 3000);
								Event closeEvent = new Event("onClose", winSerial, new Boolean(true));
								Events.postEvent(closeEvent);
							} catch (Exception e) {
								transaction.rollback();
								e.printStackTrace();
							} finally {
								session.close();
							}

							try {
								Mmenu mmenu = new MmenuDAO()
										.findByFilter("menupath = '/view/repair/repairprocess.zul'");
								NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
										oUser.getMbranch().getBranchlevel());

								mmenu = new MmenuDAO().findByFilter("menupath = '/view/repair/repairlist.zul'");
								NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
										oUser.getMbranch().getBranchlevel());

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Integer getTotalvendor() {
		return totalvendor;
	}

	public void setTotalvendor(Integer totalvendor) {
		this.totalvendor = totalvendor;
	}
}
