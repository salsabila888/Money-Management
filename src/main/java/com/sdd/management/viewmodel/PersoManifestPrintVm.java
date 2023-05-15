package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Mpersovendor;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.utils.AppData;

public class PersoManifestPrintVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private Muser muser;
	
	private String format;
	private List<Muser> listData = new ArrayList<>();
	
	@Wire
	private Window winOperators;
	@Wire
	private Combobox cbOperator;
	@Wire
	private Grid grid;
	@Wire
	private Row rowOperator, perso;
	@Wire
	private Button btnAdd;

	@AfterCompose
	@NotifyChange("*")	
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("isOrder") String isOrder)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if(isOrder != null && isOrder.equals("Y"))
			perso.setVisible(false);
		doReset();
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		format = "pdf";
		listData = new ArrayList<>();
		grid.getRows().getChildren().clear();
	}
	
	@Command
	@NotifyChange("muser")
	public void doAdd() {
		try {
			if (muser == null) {
				Messagebox.show("Silahkan pilih operator", "Info", Messagebox.OK, Messagebox.INFORMATION);
			} else if (listData.contains(muser)) {
				Messagebox.show("Operator sudah ada dalam daftar", "Info", Messagebox.OK, Messagebox.INFORMATION);
			} else {
				Row row = new Row();
				row.setAttribute("obj", muser);
				row.appendChild(new Label(muser.getUserid() + muser.getUsername()));
				Button btn = new Button("Cancel");
				btn.setAutodisable("self");
				btn.setClass("btn btn-danger btn-sm");
				btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Muser obj = (Muser) event.getTarget().getParent().getAttribute("obj");
						listData.remove(obj);
						grid.getRows().removeChild((Row) event.getTarget().getParent());
					}
				});
				row.getChildren().add(btn);
				grid.getRows().insertBefore(row, grid.getRows().getFirstChild());
				listData.add(muser);
				muser = null;
				cbOperator.setValue(null);
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Command
	public void doSave() {
		try {
			int count = 1;
			String operators = "";
			for (Muser obj : listData) {
				operators += obj.getUserid();
				if (count < listData.size())
					operators += " / ";
				count++;
			}
						
			Map<String, Object> map = new HashMap<>();
			map.put("format", format);
			map.put("operators", operators);
			Event closeEvent = new Event( "onClose", winOperators, map);
			Events.postEvent(closeEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ListModelList<Muser> getMusermodel() {
		ListModelList<Muser> lm = null;
		try {
			lm = new ListModelList<Muser>(AppData.getMuser("mbranchfk = " + oUser.getMbranch().getMbranchpk()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public ListModelList<Mpersovendor> getMpersovendormodel() {
		ListModelList<Mpersovendor> lm = null;
		try {
			lm = new ListModelList<Mpersovendor>(AppData.getMpersovendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Muser getMuser() {
		return muser;
	}

	public void setMuser(Muser muser) {
		this.muser = muser;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
