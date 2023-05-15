package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TpinpaditemDAO;
import com.sdd.caption.dao.TsecuritiesitemDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.domain.Tpinpaditem;
import com.sdd.caption.domain.Tsecuritiesitem;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.handler.InquiryHandler;
import com.sdd.caption.pojo.InquiryDetailBean;
import com.sdd.caption.pojo.InquiryOrder;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class InquiryDocumentVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private int pageTotalSize;
	private String itemno, productgroup, productname;
	private Integer branchlevel;

	private List<InquiryOrder> objList = new ArrayList<InquiryOrder>();
	private List<InquiryDetailBean> listInqdetailbean = new ArrayList<InquiryDetailBean>();

	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		productgroup = arg;

		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<InquiryOrder>() {
				@Override
				public void render(Row row, final InquiryOrder data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getItemno()));
					row.getChildren().add(new Label(data.getBranchname()));
					row.getChildren().add(new Label(data.getMproducttype().getProducttype()));
					row.getChildren().add(new Label(data.getStatus()));

					Button btn = new Button("Lihat Detail");
					btn.setAutodisable("self");
					btn.setSclass("btn btn-default btn-sm");
					btn.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("obj", data);
							map.put("objList", listInqdetailbean);
							map.put("arg", arg);
							win = (Window) Executions.createComponents("/view/inquiry/inquirydatadetail.zul", null,
									map);
							win.setWidth("80%");
							win.setClosable(true);
							win.doModal();
						}
					});

					Div div = new Div();
					div.appendChild(btn);
					row.getChildren().add(div);
				}
			});
		}
		doReset();
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		try {
			System.out.println(itemno);
			if (itemno != null && itemno.trim().length() > 0) {
				if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD)) {
					List<Tembossdata> itemList = new TembossdataDAO().listByFilter("cardno = '" + itemno.trim().toUpperCase() + "'",
							"tembossdatapk");
					if (itemList.size() > 0) {
						for (Tembossdata data : itemList) {
							InquiryOrder obj = new InquiryOrder();
							obj.setItemno(data.getCardno());
							obj.setMbranch(data.getTembossbranch().getMbranch());
							obj.setMproducttype(data.getMproduct().getMproducttype());

							Map<String, Object> mapInq = new InquiryHandler(obj).doInquiry();
							listInqdetailbean = (List<InquiryDetailBean>) mapInq.get("data");
							obj.setStatus((String) mapInq.get("lastStatus"));

							objList.add(obj);
						}

						pageTotalSize = objList.size();
						grid.setModel(new ListModelList<InquiryOrder>(objList));
					} else {
						Messagebox.show("Data tidak ditemukan");
					}
				} else if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
					List<Tsecuritiesitem> itemList = new TsecuritiesitemDAO()
							.listByFilter("itemno = '" + itemno.trim().toUpperCase() + "'", "tsecuritiesitempk");
					if (itemList.size() > 0) {
						for (Tsecuritiesitem data : itemList) {
							InquiryOrder obj = new InquiryOrder();
							obj.setItemno(data.getItemno());
							obj.setMbranch(data.getTincoming().getMbranch());
							obj.setMproducttype(data.getTincoming().getMproducttype());

							Map<String, Object> mapInq = new InquiryHandler(obj).doInquiry();
							listInqdetailbean = (List<InquiryDetailBean>) mapInq.get("data");
							String[] arData = ((String) mapInq.get("lastStatus")).split("\\|");
							obj.setStatus(arData[0]);
							obj.setBranchname(arData[1]);
							objList.add(obj);
						}

						pageTotalSize = objList.size();
						grid.setModel(new ListModelList<InquiryOrder>(objList));
					} else {
						Messagebox.show("Data tidak ditemukan");
					}
				} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
					List<Tpinpaditem> itemList = new TpinpaditemDAO().listByFilter("itemno = '" + itemno.trim().toUpperCase() + "'",
							"tpinpaditempk");
					if (itemList.size() > 0) {
						for (Tpinpaditem data : itemList) {
							InquiryOrder obj = new InquiryOrder();
							obj.setItemno(data.getItemno());
							obj.setMbranch(data.getTincoming().getMbranch());
							obj.setMproducttype(data.getTincoming().getMproducttype());

							Map<String, Object> mapInq = new InquiryHandler(obj).doInquiry();
							listInqdetailbean = (List<InquiryDetailBean>) mapInq.get("data");
							String[] arData = ((String) mapInq.get("lastStatus")).split("\\|");
							obj.setStatus((String) mapInq.get("lastStatus"));
							obj.setBranchname(data.getTincoming().getMbranch().getBranchname());
//							obj.setBranchname(arData[1]);
							objList.add(obj);
						}

						pageTotalSize = objList.size();
						grid.setModel(new ListModelList<InquiryOrder>(objList));
					} else {
						Messagebox.show("Data tidak ditemukan");
					}
				} else if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
					List<Ttokenitem> itemList = new TtokenitemDAO().listByFilter("itemno = '" + itemno.trim().toUpperCase() + "'",
							"ttokenitempk");
					if (itemList.size() > 0) {
						for (Ttokenitem data : itemList) {
							InquiryOrder obj = new InquiryOrder();
							obj.setItemno(data.getItemno());
							obj.setMbranch(data.getTincoming().getMbranch());
							obj.setMproducttype(data.getTincoming().getMproducttype());

							Map<String, Object> mapInq = new InquiryHandler(obj).doInquiry();
							listInqdetailbean = (List<InquiryDetailBean>) mapInq.get("data");
							String[] arData = ((String) mapInq.get("lastStatus")).split("\\|");
							obj.setStatus((String) mapInq.get("lastStatus"));
							obj.setBranchname(arData[1]);
							objList.add(obj);
						}

						pageTotalSize = objList.size();
						grid.setModel(new ListModelList<InquiryOrder>(objList));
					} else {
						Messagebox.show("Data tidak ditemukan");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		itemno = "";
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch_() {
		if (oUser != null) {
			if (itemno != null && itemno.trim().length() > 0) {
				try {
					if (branchlevel == 1) {
						List<Tsecuritiesitem> itemList = new TsecuritiesitemDAO()
								.listByFilter("itemno = '" + itemno.trim() + "'", "tsecuritiesitempk");
						if (itemList.size() > 0) {
							for (Tsecuritiesitem data : itemList) {
								InquiryOrder obj = new InquiryOrder();
								obj.setItemno(data.getItemno());
								obj.setMbranch(data.getTincoming().getMbranch());
								obj.setMproducttype(data.getTincoming().getMproducttype());
								obj.setStatus(data.getStatus());

								Map<String, Object> mapInq = new InquiryHandler(obj).doInquiry();
								listInqdetailbean = (List<InquiryDetailBean>) mapInq.get("data");
								obj.setStatus((String) mapInq.get("lastStatus"));

								objList.add(obj);
							}

							pageTotalSize = objList.size();
							grid.setModel(new ListModelList<InquiryOrder>(objList));
						} else {
							Messagebox.show("Data tidak ditemukan");
						}
					} else if (branchlevel == 2) {
						List<Tbranchstockitem> itemList = new TbranchstockitemDAO()
								.listNativeByFilter("itemno = '" + itemno.trim() + "' ", "tbranchstockitempk");
						if (itemList.size() > 0) {
							for (Tbranchstockitem data : itemList) {
								InquiryOrder obj = new InquiryOrder();
								obj.setItemno(data.getItemno());
								obj.setMbranch(data.getTbranchstock().getMbranch());
								obj.setMproducttype(data.getTbranchstock().getMproduct().getMproducttype());
								obj.setStatus(data.getStatus());

								objList.add(obj);
							}

							pageTotalSize = objList.size();
							grid.setModel(new ListModelList<InquiryOrder>(objList));
						} else {
							Messagebox.show("Data tidak ditemukan");
						}
					} else {
						List<Tbranchstockitem> itemList = new TbranchstockitemDAO()
								.listNativeByFilter("itemno = '" + itemno.trim() + "'", "tbranchstockitempk");
						if (itemList.size() > 0) {
							for (Tbranchstockitem data : itemList) {
								InquiryOrder obj = new InquiryOrder();
								obj.setItemno(data.getItemno());
								obj.setMbranch(data.getTbranchstock().getMbranch());
								obj.setMproducttype(data.getTbranchstock().getMproduct().getMproducttype());
								obj.setStatus(data.getStatus());

								objList.add(obj);
							}

							pageTotalSize = objList.size();
							grid.setModel(new ListModelList<InquiryOrder>(objList));
						} else {
							Messagebox.show("Data tidak ditemukan");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		itemno = null;
		objList = new ArrayList<>();
		grid.setModel(new ListModelList<>(objList));
		productname = AppData.getProductgroupLabel(productgroup);
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getItemno() {
		return itemno;
	}

	public void setItemno(String itemno) {
		this.itemno = itemno;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

}
