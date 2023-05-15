package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MnotifDAO;
import com.sdd.caption.dao.MusergroupnotifDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mnotif;
import com.sdd.caption.domain.Musergroup;
import com.sdd.caption.domain.Musergroupmenu;
import com.sdd.caption.domain.Musergroupnotif;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MusergroupnotifVm {

	private Session session;
	private Transaction transaction;

	private MusergroupnotifDAO oDao = new MusergroupnotifDAO();

	private Musergroup obj;
	private List<String> listId = new ArrayList<>();
	private List<String> listAlertId = new ArrayList<>();

	@Wire
	private Window winPopup;
	@Wire
	private Grid grid;
	@Wire
	private Grid gridAlert;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Musergroup obj) {
		Selectors.wireComponents(view, this, false);

		try {
			this.obj = obj;
			listId = oDao.listStr("notifid", "musergroup.musergrouppk = " + obj.getMusergrouppk() + " and notiftype = '" + AppUtils.NOTIF_TYPE_MENU + "'");
			
			Row row = new Row();
			Checkbox chkbox = new Checkbox(AppUtils.NOTIF_INCOMINGAPPROVAL_LABEL);
			chkbox.setAttribute("obj", AppUtils.NOTIF_INCOMINGAPPROVAL_ID);
			if (listId.contains(AppUtils.NOTIF_INCOMINGAPPROVAL_ID))
				chkbox.setChecked(true);
			chkbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					Checkbox chkbox = (Checkbox) event.getTarget();
					String id = (String) chkbox.getAttribute("obj");
					if (chkbox.isChecked()) {
						listId.add(id);
					} else {
						listId.remove(id);
					}
				}
			});
			row.appendChild(chkbox);
			grid.getRows().appendChild(row);
			
			row = new Row();
			chkbox = new Checkbox(AppUtils.NOTIF_OUTGOINGAPPROVAL_LABEL);
			chkbox.setAttribute("obj", AppUtils.NOTIF_OUTGOINGAPPROVAL_ID);
			if (listId.contains(AppUtils.NOTIF_OUTGOINGAPPROVAL_ID))
				chkbox.setChecked(true);
			chkbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					Checkbox chkbox = (Checkbox) event.getTarget();
					String id = (String) chkbox.getAttribute("obj");
					if (chkbox.isChecked()) {
						listId.add(id);
					} else {
						listId.remove(id);
					}
				}
			});
			row.appendChild(chkbox);
			grid.getRows().appendChild(row);
			
			row = new Row();
			chkbox = new Checkbox(AppUtils.NOTIF_ORDERAPPROVAL_LABEL);
			chkbox.setAttribute("obj", AppUtils.NOTIF_ORDERAPPROVAL_ID);
			if (listId.contains(AppUtils.NOTIF_ORDERAPPROVAL_ID))
				chkbox.setChecked(true);
			chkbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					Checkbox chkbox = (Checkbox) event.getTarget();
					String id = (String) chkbox.getAttribute("obj");
					if (chkbox.isChecked()) {
						listId.add(id);
					} else {
						listId.remove(id);
					}
				}
			});
			row.appendChild(chkbox);
			grid.getRows().appendChild(row);
			
			row = new Row();
			chkbox = new Checkbox(AppUtils.NOTIF_PRODAPPROVAL_LABEL);
			chkbox.setAttribute("obj", AppUtils.NOTIF_PRODAPPROVAL_ID);
			if (listId.contains(AppUtils.NOTIF_PRODAPPROVAL_ID))
				chkbox.setChecked(true);
			chkbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					Checkbox chkbox = (Checkbox) event.getTarget();
					String id = (String) chkbox.getAttribute("obj");
					if (chkbox.isChecked()) {
						listId.add(id);
					} else {
						listId.remove(id);
					}
				}
			});
			row.appendChild(chkbox);
			grid.getRows().appendChild(row);
			
			row = new Row();
			chkbox = new Checkbox(AppUtils.NOTIF_BLOCKSTOCKPAGUAPPROVAL_LABEL);
			chkbox.setAttribute("obj", AppUtils.NOTIF_BLOCKSTOCKPAGUAPPROVAL_ID);
			if (listId.contains(AppUtils.NOTIF_BLOCKSTOCKPAGUAPPROVAL_ID))
				chkbox.setChecked(true);
			chkbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					Checkbox chkbox = (Checkbox) event.getTarget();
					String id = (String) chkbox.getAttribute("obj");
					if (chkbox.isChecked()) {
						listId.add(id);
					} else {
						listId.remove(id);
					}
				}
			});
			row.appendChild(chkbox);
			grid.getRows().appendChild(row);
			
			listAlertId = oDao.listStr("notifid", "musergroup.musergrouppk = " + obj.getMusergrouppk() + " and notiftype = '" + AppUtils.NOTIF_TYPE_ALERT + "'");
			List<Mnotif> listNotifAlert = new MnotifDAO().listByFilter("0=0", "notifid");
			for (Mnotif mnotif: listNotifAlert) {
				row = new Row();
				chkbox = new Checkbox(mnotif.getNotifname());
				chkbox.setAttribute("obj", mnotif.getNotifid());
				if (listAlertId.contains(mnotif.getNotifid()))
					chkbox.setChecked(true);
				chkbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox chkbox = (Checkbox) event.getTarget();
						String id = (String) chkbox.getAttribute("obj");
						if (chkbox.isChecked()) {
							listAlertId.add(id);
						} else {
							listAlertId.remove(id);
						}
					}
				});
				row.appendChild(chkbox);
				gridAlert.getRows().appendChild(row);
				
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void save() {
		try {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			oDao.deleteBySQL(session, "musergroupfk = " + obj.getMusergrouppk());
			for (String data : listId) {
				Musergroupnotif objForm = new Musergroupnotif();
				objForm.setMusergroup(obj);
				objForm.setNotifid(data);
				objForm.setNotiftype(AppUtils.NOTIF_TYPE_MENU);
				oDao.save(session, objForm);
			}
			
			for (String data : listAlertId) {
				Musergroupnotif objForm = new Musergroupnotif();
				objForm.setMusergroup(obj);
				objForm.setNotifid(data);
				objForm.setNotiftype(AppUtils.NOTIF_TYPE_ALERT);
				oDao.save(session, objForm);
			}
			
			transaction.commit();
			session.close();
			Clients.showNotification("Pengaturan Notifikasi Sukses", "info",
					null, "middle_center", 3000);
			Event closeEvent = new Event( "onClose", winPopup, null);
			Events.postEvent(closeEvent);
		} catch (HibernateException e) {
			transaction.rollback();
			Messagebox.show("Error : " + e.getMessage(), "Error",
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			Messagebox.show("Error : " + e.getMessage(), "Error",
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	public Musergroup getObj() {
		return obj;
	}

	public void setObj(Musergroup obj) {
		this.obj = obj;
	}

}
