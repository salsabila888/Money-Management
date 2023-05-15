package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.pojo.FmtReqAll;
import com.sdd.caption.services.RequestPOS;

public class OnlineTrackingVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private FmtReqAll reqAll;
	private Tdelivery obj;
	private Tembossdata data;

	private String tracking;
	private String trackingno;

	private Rows rows;

	@Wire
	private Textbox trackingBox;
	@Wire
	private Combobox cbTracking;
	@Wire
	private Grid grid;
	@Wire
	private Groupbox gbSearch;
	@Wire
	private Caption caption;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("isTrack") String isTrack, @ExecutionArgParam("obj") Tdelivery obj) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		doReset();

		if (isTrack != null && isTrack.equals("Y")) {
			this.obj = obj;
			gbSearch.setVisible(false);
			caption.setVisible(true);
			if (obj.getMcouriervendor().getIstracking().trim().equals("Y")) {
				reqAll.setBarcode(obj.getAwb());
				doGetAPI();
			} else {
				Messagebox.show("Courier vendor tidak mendukung online tracking", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
		}

	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (tracking.equals("AWB")) {
				if (trackingno == null || trackingno.trim().length() == 0) {
					Messagebox.show("Masukan " + cbTracking.getValue(), "Info", Messagebox.OK, Messagebox.INFORMATION);
				} else {
					if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 700) {
						obj = new TdeliveryDAO().findByFilter("awb = '" + trackingno + "'");
					} else if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) < 700
							&& Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600) {
						List<Tdelivery> objList = new TdeliveryDAO().trackingByFilter("awb = '" + trackingno
								+ "' and mbranch.mregionfk = " + oUser.getMbranch().getMregion().getMregionpk(), "awb");
						if (objList.size() > 0) {
							obj = objList.get(0);
						}
					} else {
						obj = new TdeliveryDAO().findByFilter(
								"awb = '" + trackingno + "' and mbranchfk = " + oUser.getMbranch().getMbranchpk());
					}

					if (obj != null) {
						if (obj.getMcouriervendor().getIstracking().equals("Y")) {
							reqAll.setBarcode(trackingno);
							refresh();
							doGetAPI();
						} else {
							refresh();
							Messagebox.show("Courier vendor tidak mendukung online tracking", "Info", Messagebox.OK,
									Messagebox.INFORMATION);
						}
					} else {
						refresh();
						Messagebox.show("No AWB tidak ditemukan", "Info", Messagebox.OK, Messagebox.INFORMATION);
					}
				}
			} else if (tracking.equals("NS")) {
				if (trackingno == null || trackingno.trim().length() == 0) {
					Messagebox.show("Masukan " + cbTracking.getValue(), "Info", Messagebox.OK, Messagebox.INFORMATION);
				} else {
					if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 700) {
						obj = new TdeliveryDAO().findByFilter("dlvid = '" + trackingno + "'");
					} else if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) < 700
							&& Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600) {
						List<Tdelivery> objList = new TdeliveryDAO().trackingByFilter("awb = '" + trackingno
								+ "' and mbranch.mregionfk = " + oUser.getMbranch().getMregion().getMregionpk(), "awb");
						if (objList.size() > 0) {
							obj = objList.get(0);
						}
					} else {
						obj = new TdeliveryDAO().findByFilter(
								"dlvid = '" + trackingno + "' and mbranchfk = " + oUser.getMbranch().getMbranchpk());
					}

					if (obj != null) {
						if (obj.getAwb() != null || !obj.getAwb().trim().equals("")) {
							if (obj.getMcouriervendor().getIstracking().equals("Y")) {
								reqAll.setBarcode(obj.getAwb());
								refresh();
								doGetAPI();
							} else {
								refresh();
								Messagebox.show("Courier vendor tidak mendukung online tracking", "Info", Messagebox.OK,
										Messagebox.INFORMATION);
							}
						} else {
							refresh();
							Messagebox.show("No Surat belum memiliki no AWB", "Info", Messagebox.OK,
									Messagebox.INFORMATION);
						}
					} else {
						refresh();
						Messagebox.show("Data tidak ditemukan", "Info", Messagebox.OK, Messagebox.INFORMATION);
					}
				}

			} else {
				Messagebox.show("Silahkan pilih jenis pencarian", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@NotifyChange("*")
	public void refresh() {
		if (grid.getRows() != null) {
			grid.removeChild(rows);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		tracking = "";
		trackingno = "";
		reqAll = new FmtReqAll();
		refresh();
	}

	@NotifyChange("grid")
	private void doGetAPI() {
		try {
			RequestPOS.getToken(obj);
			String rsp = RequestPOS.getDetail(reqAll, obj);
			JSONObject objRsp = new JSONObject(rsp);
			if (objRsp.isNull("response") == false) {
				JSONObject response = objRsp.getJSONObject("response");
				System.out.println("OBJ DATA : " + response);
				JSONArray objArr = response.getJSONArray("data");

				if (objArr.length() > 0) {
					rows = new Rows();

					for (int i = 0; i < objArr.length(); i++) {
						System.out.println("KEY" + objArr.get(i));
						JSONObject objData = objArr.getJSONObject(i);

						Row row = new Row();
						Label lbl = new Label();
						Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.parse(objData.get("eventDate").toString());
						lbl.setValue(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date));

						row.appendChild(lbl);
						lbl = new Label();
						int length = objData.get("description").toString().indexOf("~");
						lbl.setValue(objData.get("description").toString().substring(0, length));
						row.appendChild(lbl);

						rows.getChildren().add(row);
					}
					grid.appendChild(rows);
				}
			} else {
				refresh();
				Messagebox.show("Nomor AWB belum terdaftar di expedisi", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getTracking() {
		return tracking;
	}

	public void setTracking(String tracking) {
		this.tracking = tracking;
	}

	public String getTrackingno() {
		return trackingno;
	}

	public void setTrackingno(String trackingno) {
		this.trackingno = trackingno;
	}

	public Tdelivery getObj() {
		return obj;
	}

	public void setObj(Tdelivery obj) {
		this.obj = obj;
	}

	public Tembossdata getData() {
		return data;
	}

	public void setData(Tembossdata data) {
		this.data = data;
	}

}
