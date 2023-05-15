package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.utils.AppData;

public class InquiryCardVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private int pageTotalSize;
	private String cardno;
	private String nameonid;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	List<Tembossdata> objList = new ArrayList<>();

	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tembossdata>() {

				@Override
				public void render(Row row, final Tembossdata data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));

					A a = new A(data.getCardno());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);

							Window win = (Window) Executions.createComponents("/view/inquiry/inquirydetail.zul", null, map);
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(data.getNameonid()));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(data.getBranchname()));
					row.getChildren()
							.add(new Label(data.getMproduct() != null ? data.getMproduct().getProductname() : ""));
					row.getChildren().add(new Label(AppData.getStatusLabel(data.getTembossbranch().getStatus())));
				}
			});
		}
		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (oUser != null) {
			if (cardno != null && cardno.trim().length() > 0) {

				try {
					objList = new TembossdataDAO().inqList(
							"tembossdata.cardno = '" + cardno.trim() + "' and tembossdatafk is null", "tembossdatapk");
					if (objList.size() > 0) {
						if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) < 700
								&& Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600) {
							if (!objList.get(0).getMbranch().getMregion().getMregionpk()
									.equals(oUser.getMbranch().getMregion().getMregionpk())) {
								objList = new ArrayList<>();
								Messagebox.show("Data tidak ditemukan");
							}
						} else if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) < 600) {
							if (!objList.get(0).getMbranch().getMbranchpk().equals(oUser.getMbranch().getMbranchpk())) {
								objList = new ArrayList<>();
								Messagebox.show("Data tidak ditemukan");
							}
						}

						pageTotalSize = objList.size();
						grid.setModel(new ListModelList<>(objList));
					} else {
						Messagebox.show("Data tidak ditemukan");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		/*
		 * if(oUser != null) { if(oUser.getMbranch() != null) { filter +=
		 * "and mbranchfk = " + oUser.getMbranch().getMbranchpk(); } }
		 */

	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		cardno = null;
		nameonid = null;
		objList = new ArrayList<>();
		grid.setModel(new ListModelList<>(objList));
		// doSearch();
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public String getNameonid() {
		return nameonid;
	}

	public void setNameonid(String nameonid) {
		this.nameonid = nameonid;
	}
}
