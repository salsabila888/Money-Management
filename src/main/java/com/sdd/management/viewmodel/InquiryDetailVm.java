package com.sdd.caption.viewmodel;

import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TderivatifdataDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpersoDAO;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tderivatifdata;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.domain.Tpinmailerdata;
import com.sdd.caption.utils.AppData;

public class InquiryDetailVm {

	private Tembossdata obj;
	private Tperso objPerso;
	private Tpaketdata objPaket;
	private Tdeliverydata objDlv;

	private String productgroup;
	private String status;

	@Wire
	private Window winInqdetail;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tembossdata obj,
			@ExecutionArgParam("isDrvCard") String isDrvCard, @ExecutionArgParam("drv") Tderivatifdata data,
			@ExecutionArgParam("isPM") String isPM, @ExecutionArgParam("pm") Tpinmailerdata pinData) throws Exception {
		Selectors.wireComponents(view, this, false);
		try {
			if (obj != null && obj.getTembossproduct().getIsneeddoc().equals("Y")) {
				List<Tderivatifdata> drvList = new TderivatifdataDAO()
						.listByFilter("tembossdatafk = " + obj.getTembossdatapk(), "tembossdatafk");
				if (drvList.size() > 0) {
					isDrvCard = "Y";
					if (drvList.size() == 1) {
						data = drvList.get(0);
					} else {
						for(Tderivatifdata drv : drvList) {
							if(data == null) {
								data = drv;
							} else {
								if(drv.getTderivatif().getStatus() > data.getTderivatif().getStatus())
									data = drv;
							}
						}
					}
				}
			}

			if (isDrvCard != null && isDrvCard.equals("Y")) {
				this.obj = data.getTembossdata();
				status = AppData.getStatusDerivatifLabel(data.getTderivatif().getStatus());
				productgroup = AppData.getProductgroupLabel(data.getTderivatif().getMproduct().getProductgroup());

				List<Tperso> persoList = new TpersoDAO().listByFilter(
						"tderivatifproductfk = " + data.getTderivatifproduct().getTderivatifproductpk(), "tpersopk");
				if (persoList.size() > 0) {
					objPerso = persoList.get(0);
					status = AppData.getStatusLabel(objPerso.getStatus());
				} else {
					objPerso = new Tperso();
				}

				List<Tpaketdata> paketList = new TpaketdataDAO()
						.listByFilter("nopaket = '" + data.getTderivatifproduct().getNopaket() + "'", "tpaketdatapk");
				if (paketList.size() > 0) {
					objPaket = paketList.get(0);
					status = AppData.getStatusLabel(objPaket.getTpaket().getStatus());
				} else {
					objPaket = new Tpaketdata();
				}

				if (objPaket.getTpaketdatapk() != null) {
					List<Tdeliverydata> dlvList = new TdeliverydataDAO()
							.listByFilter("tpaketdatafk = " + objPaket.getTpaketdatapk(), "tdeliverydatapk");
					if (dlvList.size() > 0) {
						objDlv = dlvList.get(0);
						status = AppData.getStatusLabel(objDlv.getTdelivery().getStatus());
					} else {
						objDlv = new Tdeliverydata();
					}
				}

			} else if (isPM != null && isPM.equals("Y")) {
				List<Tembossdata> dataList = new TembossdataDAO()
						.listByFilter("cardno = '" + pinData.getCardno().trim() + "'", "tembossdatapk");
				if (dataList.size() > 0) {
					this.obj = dataList.get(0);

					Torder order = new TorderDAO().findByFilter("tpinmailerfilefk = "
							+ pinData.getTpinmailerbranch().getTpinmailerfile().getTpinmailerfilepk());

					status = AppData.getStatusLabel(order.getStatus());
					productgroup = AppData.getProductgroupLabel(order.getProductgroup());

					objPerso = new Tperso();

					objPaket = new TpaketdataDAO().findByFilter(
							"tpinmailerbranchfk = " + pinData.getTpinmailerbranch().getTpinmailerbranchpk());
					if (objPaket == null) {
						objPaket = new Tpaketdata();
					} else {
						status = AppData.getStatusLabel(objPaket.getTpaket().getStatus());
					}

					if (objPaket.getTpaketdatapk() != null) {
						List<Tdeliverydata> dlvList = new TdeliverydataDAO()
								.listByFilter("tpaketdatafk = " + objPaket.getTpaketdatapk(), "tdeliverydatapk");
						if (dlvList.size() > 0) {
							objDlv = dlvList.get(0);
							status = AppData.getStatusLabel(objDlv.getTdelivery().getStatus());
						} else {
							objDlv = new Tdeliverydata();
						}
					}
				} else {
					Messagebox.show("Nomer Kartu belum ada file embossnya", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				}
			} else {
				this.obj = obj;
				status = AppData.getStatusLabel(obj.getTembossbranch().getStatus());
				productgroup = AppData.getProductgroupLabel(obj.getMproduct().getProductgroup());

				List<Tperso> persoList = new TpersoDAO().listByFilter(
						"tembossproductfk = " + obj.getTembossproduct().getTembossproductpk(), "tpersopk");
				if (persoList.size() > 0) {
					objPerso = persoList.get(0);
				} else {
					objPerso = new Tperso();
				}

				List<Tpaketdata> paketList = new TpaketdataDAO().listByFilter(
						"tembossbranchfk = " + obj.getTembossbranch().getTembossbranchpk(), "tpaketdatapk");
				if (paketList.size() > 0) {
					objPaket = paketList.get(0);
				} else {
					objPaket = new Tpaketdata();
				}

				if (objPaket.getTpaketdatapk() != null) {
					List<Tdeliverydata> dlvList = new TdeliverydataDAO()
							.listByFilter("tpaketdatafk = " + objPaket.getTpaketdatapk(), "tdeliverydatapk");
					if (dlvList.size() > 0) {
						objDlv = dlvList.get(0);
					} else {
						objDlv = new Tdeliverydata();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * @Command public void doTracking() throws Exception { Map<String, Object> map
	 * = new HashMap<String, Object>(); Tdelivery dlv = new
	 * TdeliveryDAO().findByPk(obj.getTdelivery().getTdeliverypk()); courier = new
	 * McouriervendorDAO().findByPk(dlv.getMcouriervendor().getMcouriervendorpk());
	 * reqAll = new FmtReqAll(); reqAll.setBarcode(obj.getAwb()); String token =
	 * RequestPOS.getToken(courier); String rsp = RequestPOS.getDetail(reqAll,
	 * courier); JSONObject objRsp = new JSONObject(rsp); if
	 * (objRsp.isNull("response")) { Messagebox.show("No AWB belum terdaftar",
	 * "Info", Messagebox.OK, Messagebox.INFORMATION); } else { map.put("detail",
	 * obj); map.put("isTrackDetail", "Y"); Window win = (Window)
	 * Executions.createComponents("/view/tracking/onlinetracking.zul", null, map);
	 * win.setWidth("80%"); win.setClosable(true); win.doModal(); } }
	 */

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winInqdetail, null);
		Events.postEvent(closeEvent);
	}

	public Tembossdata getObj() {
		return obj;
	}

	public void setObj(Tembossdata obj) {
		this.obj = obj;
	}

	public Tperso getObjPerso() {
		return objPerso;
	}

	public void setObjPerso(Tperso objPerso) {
		this.objPerso = objPerso;
	}

	public Tpaketdata getObjPaket() {
		return objPaket;
	}

	public void setObjPaket(Tpaketdata objPaket) {
		this.objPaket = objPaket;
	}

	public Tdeliverydata getObjDlv() {
		return objDlv;
	}

	public void setObjDlv(Tdeliverydata objDlv) {
		this.objDlv = objDlv;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
