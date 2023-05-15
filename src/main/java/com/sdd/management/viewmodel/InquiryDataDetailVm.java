package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.A;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TsecuritiesitemDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tsecuritiesitem;
import com.sdd.caption.handler.InquiryHandler;
import com.sdd.caption.pojo.InquiryDetailBean;
import com.sdd.caption.pojo.InquiryOrder;
import com.sdd.caption.utils.AppUtils;

public class InquiryDataDetailVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private Mbranch mbranch;

	private InquiryOrder obj;
	private Integer branchlevel;
	private String productgroup;

	List<InquiryDetailBean> objList = new ArrayList<InquiryDetailBean>();

	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd MMMMM yyyy HH:mm");

	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") InquiryOrder obj,
			@ExecutionArgParam("objList") List<InquiryDetailBean> objList, @ExecutionArgParam("arg") String arg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		productgroup = arg;
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();

		this.obj = obj;
		this.objList = objList;
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<InquiryDetailBean>() {
				@Override
				public void render(Row row, final InquiryDetailBean data, int index) throws Exception {

					row.getChildren().add(new Label(data.getTanggal()));
					row.getChildren().add(new Label(data.getKeterangan()));
					row.getChildren().add(new Label(data.getProcessby()));
					row.getChildren().add(new Label(data.getLocation()));

					if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD)) {
						A a = new A(data.getTembossdata().getBranchid());
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								Map<String, Object> map = new HashMap<>();
								Window win = new Window();
								if (data.getTdelivery() != null) {
									map.put("obj", data.getTdelivery());
									map.put("isInquiry", "Y");
									win = (Window) Executions.createComponents("/view/delivery/deliverydata.zul", null,
											map);
									System.out.println("DLV");
								} else if (data.getTpaket() != null) {
									map.put("obj", data.getTpaket());
									map.put("isInquiry", "Y");
									win = (Window) Executions.createComponents("/view/delivery/paketdata.zul", null,
											map);
									System.out.println("PAKET");
								} else if (data.getToutgoing() != null) {
									map.put("obj", data.getToutgoing());
									win = (Window) Executions.createComponents("/view/inventory/outgoingdata.zul", null,
											map);
									System.out.println("OUT");
								} else if (data.getTpersodata() != null) {
									map.put("obj", data.getTpersodata());
									map.put("isInquiry", "Y");
									win = (Window) Executions.createComponents("/view/perso/persodata.zul", null, map);
									System.out.println("ORDER");
								} else if (data.getTembossdata() != null) {
									map.put("obj", data.getTembossdata());
									map.put("isInquiry", "Y");
									win = (Window) Executions.createComponents("/view/inquiry/inquirydetail.zul", null,
											map);
									System.out.println("IN");
								}

								win.setWidth("50%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(a);

					} else {
//						System.out.println(productgroup);
						A a = new A(data.getOrderid());
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<>();
								Window win = new Window();
								if (data.getTdelivery() != null) {
									map.put("obj", data.getTdelivery());
									map.put("isInquiry", "Y");
									win = (Window) Executions.createComponents("/view/delivery/deliverydata.zul", null,
											map);
									System.out.println("DLV");
								} else if (data.getTpaket() != null) {
									map.put("obj", data.getTpaket());
									map.put("isInquiry", "Y");
									win = (Window) Executions.createComponents("/view/delivery/paketdata.zul", null,
											map);
									System.out.println("PAKET");
								} else if (data.getToutgoing() != null) {
									map.put("obj", data.getToutgoing());
									win = (Window) Executions.createComponents("/view/inventory/outgoingdata.zul", null,
											map);
									System.out.println("OUT");
								} else if (data.getTorder() != null) {
									map.put("obj", data.getTorder());
									map.put("isInquiry", "Y");
									win = (Window) Executions.createComponents("/view/order/orderitem.zul", null, map);
									System.out.println("ORDER");
								} else if (data.getTincoming() != null) {
									if (data.getTincoming().getProductgroup().trim()
											.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
										map.put("obj", data.getTincoming());
										map.put("isInqSecurities", "Y");
										win = (Window) Executions.createComponents(
												"/view/inventory/incomingsecuritiesdata.zul", null, map);
										System.out.println("IN");
									} else if (data.getTincoming().getProductgroup().trim()
											.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
										map.put("obj", data.getTincoming());
										map.put("isInqPinpad", "Y");
										win = (Window) Executions
												.createComponents("/view/inventory/incomingpinpaddata.zul", null, map);
										System.out.println("IN");
									} else if (data.getTincoming().getProductgroup().trim()
											.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
										map.put("obj", data.getTincoming());
										map.put("isInqToken", "Y");
										win = (Window) Executions
												.createComponents("/view/inventory/incomingtokendata.zul", null, map);
										System.out.println("IN");
									}
								} else if (data.getTreturn() != null) {
									map.put("obj", data.getTreturn());
									win = (Window) Executions.createComponents("/view/return/returproductitem.zul", null, map);
									System.out.println("RETUR");
								}

								win.setWidth("50%");
								win.setClosable(true);
								win.doModal();
							}
						});
						row.getChildren().add(a);
					}
				}
			});
		}
		doReset();
	}

	public void doReset() {
		try {
			if (objList == null) {
				Map<String, Object> mapInq = new InquiryHandler(obj).doInquiry();
				objList = (List<InquiryDetailBean>) mapInq.get("data");
			}
			grid.setModel(new ListModelList<InquiryDetailBean>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doReset_() {
		try {
			List<Tsecuritiesitem> incomingList = new TsecuritiesitemDAO().listNativeByFilter("mproducttypefk = "
					+ obj.getMproducttype().getMproducttypepk() + " and itemno = '" + obj.getItemno().trim() + "'",
					"tsecuritiesitempk desc");
			if (incomingList.size() > 0) {
				Tsecuritiesitem incoming = incomingList.get(0);
				InquiryDetailBean bean = null;
				if (branchlevel == 1) {
					bean = new InquiryDetailBean();
					bean.setTanggal(datetimelocalFormatter.format(incoming.getTincoming().getEntrytime()));
					bean.setKeterangan("INCOMING");
					bean.setProcessby(incoming.getTincoming().getEntryby());
					bean.setLocation(incoming.getTincoming().getMbranch().getBranchname());
					bean.setTincoming(incoming.getTincoming());
					bean.setOrderid(incoming.getTincoming().getIncomingid());
					objList.add(bean);
				}

				List<Torderitem> orderList = new TorderitemDAO().listNativeByFilter("mproducttypefk = "
						+ obj.getMproducttype().getMproducttypepk() + " and itemno = '" + obj.getItemno().trim() + "'",
						"torderitempk desc");
				if (orderList.size() > 0) {
					Torderitem order = orderList.get(0);

					bean = new InquiryDetailBean();
					bean.setTanggal(datetimelocalFormatter.format(order.getTorder().getInserttime()));
					bean.setKeterangan("ORDER");
					bean.setProcessby(order.getTorder().getInsertedby());
					bean.setLocation(order.getTorder().getMbranch().getBranchname());
					bean.setTorder(order.getTorder());
					bean.setOrderid(order.getTorder().getOrderid());
					objList.add(bean);

					List<Toutgoing> outgoingList = new ToutgoingDAO()
							.listByFilter("torderfk = " + order.getTorder().getTorderpk(), "toutgoingpk desc");
					if (outgoingList.size() > 0) {
						Toutgoing outgoing = outgoingList.get(0);

						bean = new InquiryDetailBean();
						bean.setTanggal(datetimelocalFormatter.format(outgoing.getEntrytime()));
						bean.setKeterangan("OUTGOING INVENTORY");
						bean.setProcessby(outgoing.getEntryby());
						bean.setLocation(outgoing.getTorder().getMbranch().getBranchname());
						bean.setToutgoing(outgoing);
						bean.setOrderid(outgoing.getOutgoingid());
						objList.add(bean);

						Tpaket paket = new TpaketDAO().findByFilter("torderfk = " + order.getTorder().getTorderpk());
						if (paket != null) {
							bean = new InquiryDetailBean();
							bean.setTanggal(datetimelocalFormatter.format(paket.getProcesstime()));
							bean.setKeterangan("DELIVERY");
							bean.setProcessby(paket.getProcessedby());
							mbranch = new MbranchDAO().findByFilter("branchid = '" + paket.getBranchpool() + "'");
							bean.setLocation(mbranch.getBranchname());
							bean.setTpaket(paket);
							bean.setOrderid(paket.getPaketid());
							objList.add(bean);

							Tpaketdata paketdata = new TpaketdataDAO()
									.findByFilter("tpaketfk = " + paket.getTpaketpk());
							Tdeliverydata deliverydata = new TdeliverydataDAO()
									.findByFilter("tpaketdatafk = " + paketdata.getTpaketdatapk());
							if (deliverydata != null) {
								Tdelivery delivery = new TdeliveryDAO()
										.findByFilter("tdeliverypk = " + deliverydata.getTdelivery().getTdeliverypk());

								bean = new InquiryDetailBean();
								bean.setTanggal(datetimelocalFormatter.format(delivery.getProcesstime()));
								bean.setKeterangan("KIRIM");
								bean.setProcessby(delivery.getProcessedby());
								bean.setLocation(mbranch.getBranchname());
								bean.setTdelivery(delivery);
								bean.setOrderid(delivery.getDlvid());
								objList.add(bean);
							}
						}
					}
				}
			}
			grid.setModel(new ListModelList<InquiryDetailBean>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}