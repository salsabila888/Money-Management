package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MbranchproductgroupDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproductgroupDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverycourierDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TpinpadorderproductDAO;
import com.sdd.caption.dao.TrepairdlvDAO;
import com.sdd.caption.dao.TrepairitemDAO;
import com.sdd.caption.dao.TreturnitemDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Mcourier;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverycourier;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Trepairdlv;
import com.sdd.caption.domain.Trepairitem;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.domain.Vcouriervendorsumdata;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DeliveryManifestCourierVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TdeliveryDAO oDao = new TdeliveryDAO();

	private Mcourier objCourier;

	private String productgroup;
//	private String couriercode;
	private int totalcard;
	private int totaltoken;
	private int totalpinpad;
	private int totaldoc;
	private int totalsupplies;
	private int totalpinmailer;
	private String isurgent;
	private Mcouriervendor mcouriervendor;
	private Mcourier mcourier;

	private Map<String, Integer> map = new HashMap<>();
	private List<Mcouriervendor> listVendor = new ArrayList<>();
	private List<Tdelivery> objList = new ArrayList<>();
	private List<Tdelivery> listSelected = new ArrayList<>();
	private ListModelList<Mcourier> mcouriermodel;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Grid grid, gridCard, gridToken, gridPinpad, gridPinmailer, gridDocument;
	@Wire
	private Div divHeader;
	@Wire
	private Combobox cbProductgroup, cbVendor, cbCourier;
	@Wire
	private Textbox tbVendor, tbCourierName;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		try {
			listVendor = AppData.getMcouriervendor();
			doReset();
			
			grid.setRowRenderer(new RowRenderer<Tdelivery>() {

				@Override
				public void render(Row row, final Tdelivery data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					Checkbox chkbox = new Checkbox();
					chkbox.setChecked(true);
					chkbox.setAttribute("obj", data);
					chkbox.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							Tdelivery obj = (Tdelivery) checked.getAttribute("obj");
							if (checked.isChecked()) {
								listSelected.add(obj);
							} else {
								listSelected.remove(obj);
							}
							BindUtils.postNotifyChange(null, null, DeliveryManifestCourierVm.this, "total");
						}
					});
					row.getChildren().add(chkbox);

					A a = new A(data.getDlvid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);

							Window win = (Window) Executions.createComponents("/view/delivery/deliverydata.zul", null,
									map);
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.getChildren().add(new Label(datelocalFormatter.format(data.getProcesstime())));
					row.getChildren()
							.add(new Label(
									data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata())
											: "0"));
					row.getChildren().add(new Label(data.getMbranch().getBranchname()));
					row.getChildren()
							.add(new Label(data.getMbranch() != null ? data.getMbranch().getBranchaddress() : "-"));
					row.getChildren()
							.add(new Label(data.getMbranch() != null ? data.getMbranch().getBranchcity() : "-"));
				}
			});

			doRenderTab();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doRenderTab() {
		try {
			List<Mproductgroup> listProductgroup = new ArrayList<Mproductgroup>();
			if (oUser.getMbranch().getBranchlevel() == 1) {
				for (Mbranchproductgroup obj : new MbranchproductgroupDAO()
						.listNativeByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and productgroupcode != '09'", "mbranchproductgrouppk")) {
					listProductgroup.add(obj.getMproductgroup());
				}
			} else {
				String filterbranch = "";
				if (oUser.getMbranch().getBranchlevel() == 2)
					filterbranch = "productgroupcode = '04'";
				else
					filterbranch = "productgroupcode in ('02', '03', '04')";
				listProductgroup = new MproductgroupDAO().listByFilter(filterbranch, "productgroupcode");
			}

			String firstproductgroup = "";
			for (Mproductgroup obj : listProductgroup) {
				if (firstproductgroup.length() == 0)
					firstproductgroup = obj.getProductgroupcode();

				Div divColHeader = new Div();
				divColHeader.setClass("col-md-6");
				divColHeader.setParent(divHeader);
				Groupbox groupbox = new Groupbox();
				Caption caption = new Caption(obj.getProductgroupcode().equals("01") ? "KARTU" : obj.getProductgroup());
				caption.setStyle("text-align: right");
				caption.setParent(groupbox);
				// groupbox.setTitle(obj.getProductgroup());
				groupbox.setMold("3d");
				groupbox.setOpen(false);
				groupbox.setParent(divColHeader);
				groupbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						cbProductgroup.setValue(obj.getProductgroupcode().equals("01") ? "KARTU" : obj.getProductgroup());
						productgroup = obj.getProductgroupcode();
					}
				});
				Div divCard = new Div();
				divCard.setClass("card");
				divCard.setParent(groupbox);
				Div divCardFooter = new Div();
				divCardFooter.setClass("card-footer");
				divCardFooter.setParent(divCard);
				Grid grid = new Grid();
				grid.setParent(divCardFooter);
				Columns columns = new Columns();
				columns.setParent(grid);
				Column colExpName = new Column("Nama Exp");
				colExpName.setStyle("background-color: #81bf3d !important; border-color: #f1f2f2 !important;");
				colExpName.setParent(columns);
				Column colExpCode = new Column("Kode Exp");
				colExpCode.setStyle("background-color: #81bf3d !important; border-color: #f1f2f2 !important;");
				colExpCode.setParent(columns);
				Column colPaket = new Column("Paket");
				colPaket.setStyle("background-color: #81bf3d !important; border-color: #f1f2f2 !important;");
				colPaket.setParent(columns);
				Rows rows = new Rows();
				rows.setParent(grid);
				Integer total = 0;
				for (Mcouriervendor exp : listVendor) {
					Row row = new Row();
					row.appendChild(new Label(exp.getVendorname()));
					row.appendChild(new Label(exp.getVendorcode()));
					row.appendChild(new Label(map.get(obj.getProductgroupcode() + exp.getVendorcode() + "M") != null
							? String.valueOf(map.get(obj.getProductgroupcode() + exp.getVendorcode() + "M"))
							: "0"));
					grid.getRows().appendChild(row);

					total += map.get(obj.getProductgroupcode() + exp.getVendorcode() + "M") != null
							? map.get(obj.getProductgroupcode() + exp.getVendorcode() + "M")
							: 0;
				}
				// lblQty.setValue(NumberFormat.getInstance().format(total));
				caption.setLabel(caption.getLabel() + " : " + NumberFormat.getInstance().format(total));
				divColHeader.appendChild(new Separator());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void getProduct() {
		try {
			if (oUser.getMbranch().getBranchlevel() > 1) {
				List<Mproductgroup> productgroupList = new MproductgroupDAO().listByFilter("productgroupcode not in ('09', '06')", "mproductgrouppk");
				for (Mproductgroup data : productgroupList) {
//					productgroup = data.getProductgroupcode();
					Comboitem item = new Comboitem();
					item.setLabel(data.getProductgroup());
					item.setValue(data.getProductgroupcode());
					cbProductgroup.appendChild(item);
				}
			} else {
				List<Mbranchproductgroup> productgroupList = new MbranchproductgroupDAO()
						.listNativeByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and productgroupcode != '09'", "mproductgroupfk");
				for (Mbranchproductgroup data : productgroupList) {
//					productgroup = data.getMproductgroup().getProductgroupcode();
					Comboitem item = new Comboitem();
					item.setLabel(data.getMproductgroup().getProductgroupcode().equals("01") ? "KARTU" : data.getMproductgroup().getProductgroup());
					item.setValue(data.getMproductgroup().getProductgroupcode());
					cbProductgroup.appendChild(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	private void refreshSummary() {
		try {
			totalcard = 0;
			totaltoken = 0;
			totalpinpad = 0;
			totaldoc = 0;
			totalsupplies = 0;
			totalpinmailer = 0;

			gridCard.getRows().getChildren().clear();
			for (Mcouriervendor obj : listVendor) {
				Row row = new Row();
				row.appendChild(new Label(obj.getVendorcode()));
				row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_CARD + obj.getVendorcode() + "M") != null
						? String.valueOf(map.get(AppUtils.PRODUCTGROUP_CARD + obj.getVendorcode() + "M"))
						: "0"));
				// row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_CARD +
				// obj.getVendorcode() + "D") != null ?
				// String.valueOf(map.get(AppUtils.PRODUCTGROUP_CARD + obj.getVendorcode() +
				// "D")) : "0"));
				gridCard.getRows().appendChild(row);

				totalcard += map.get(AppUtils.PRODUCTGROUP_CARD + obj.getVendorcode() + "D") != null
						? map.get(AppUtils.PRODUCTGROUP_CARD + obj.getVendorcode() + "D")
						: 0;
			}

			gridToken.getRows().getChildren().clear();
			for (Mcouriervendor obj : listVendor) {
				Row row = new Row();
				row.appendChild(new Label(obj.getVendorcode()));
				row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_TOKEN + obj.getVendorcode() + "M") != null
						? String.valueOf(map.get(AppUtils.PRODUCTGROUP_TOKEN + obj.getVendorcode() + "M"))
						: "0"));
				// row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_TOKEN +
				// obj.getVendorcode() + "D") != null ?
				// String.valueOf(map.get(AppUtils.PRODUCTGROUP_TOKEN + obj.getVendorcode() +
				// "D")) : "0"));
				gridToken.getRows().appendChild(row);

				totaltoken += map.get(AppUtils.PRODUCTGROUP_TOKEN + obj.getVendorcode() + "D") != null
						? map.get(AppUtils.PRODUCTGROUP_TOKEN + obj.getVendorcode() + "D")
						: 0;

			}

			gridPinpad.getRows().getChildren().clear();
			for (Mcouriervendor obj : listVendor) {
				Row row = new Row();
				row.appendChild(new Label(obj.getVendorcode()));
				row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_PINPAD + obj.getVendorcode() + "M") != null
						? String.valueOf(map.get(AppUtils.PRODUCTGROUP_PINPAD + obj.getVendorcode() + "M"))
						: "0"));
				// row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_PINPAD +
				// obj.getVendorcode() + "D") != null ?
				// String.valueOf(map.get(AppUtils.PRODUCTGROUP_PINPAD + obj.getVendorcode() +
				// "D")) : "0"));
				gridPinpad.getRows().appendChild(row);

				totalpinpad += map.get(AppUtils.PRODUCTGROUP_PINPAD + obj.getVendorcode() + "D") != null
						? map.get(AppUtils.PRODUCTGROUP_PINPAD + obj.getVendorcode() + "D")
						: 0;
			}

//			gridPinmailer.getRows().getChildren().clear();
//			for (Mcouriervendor obj : listVendor) {
//				Row row = new Row();
//				row.appendChild(new Label(obj.getVendorcode()));
//				row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_PINMAILER + obj.getVendorcode() + "M") != null
//						? String.valueOf(map.get(AppUtils.PRODUCTGROUP_PINMAILER + obj.getVendorcode() + "M"))
//						: "0"));
//				// row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_SUPPLIES +
//				// obj.getVendorcode() + "D") != null ?
//				// String.valueOf(map.get(AppUtils.PRODUCTGROUP_SUPPLIES + obj.getVendorcode() +
//				// "D")) : "0"));
//				gridPinmailer.getRows().appendChild(row);
//
//				totalpinmailer += map.get(AppUtils.PRODUCTGROUP_PINMAILER + obj.getVendorcode() + "D") != null
//						? map.get(AppUtils.PRODUCTGROUP_PINMAILER + obj.getVendorcode() + "D")
//						: 0;
//			}

			gridDocument.getRows().getChildren().clear();
			for (Mcouriervendor obj : listVendor) {
				Row row = new Row();
				row.appendChild(new Label(obj.getVendorcode()));
				row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_DOCUMENT + obj.getVendorcode() + "M") != null
						? String.valueOf(map.get(AppUtils.PRODUCTGROUP_DOCUMENT + obj.getVendorcode() + "M"))
						: "0"));
				// row.appendChild(new Label(map.get(AppUtils.PRODUCTGROUP_SUPPLIES +
				// obj.getVendorcode() + "D") != null ?
				// String.valueOf(map.get(AppUtils.PRODUCTGROUP_SUPPLIES + obj.getVendorcode() +
				// "D")) : "0"));
				gridDocument.getRows().appendChild(row);

				totaldoc += map.get(AppUtils.PRODUCTGROUP_DOCUMENT + obj.getVendorcode() + "D") != null
						? map.get(AppUtils.PRODUCTGROUP_DOCUMENT + obj.getVendorcode() + "D")
						: 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	@Command
//	public void doScan() {
//		if (productgroup != null && productgroup.length() > 0) {
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("productgroup", productgroup);
//			Window win = (Window) Executions.createComponents("/view/delivery/kurirscan.zul", null, map);
//			win.setClosable(true);
//			win.doModal();
//			win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
//
//				@Override
//				public void onEvent(Event event) throws Exception {
//					if (event.getData() != null) {
//						objCourier = (Mcourier) event.getData();
//						BindUtils.postNotifyChange(null, null, DeliveryManifestCourierVm.this, "objCourier");
//						BindUtils.postNotifyChange(null, null, DeliveryManifestCourierVm.this, "total");
//						refreshModel();
//					}
//				}
//			});
//		} else {
//			Messagebox.show("Silahkan pilih isian grup produk", "Info", Messagebox.OK, Messagebox.INFORMATION);
//		}
//	}

//	@Command
//	@NotifyChange({ "objCourier", "couriercode" })
//	public void doIdentify() {
//		try {
//			if (productgroup != null && productgroup.length() > 0) {
//				if (couriercode != null && couriercode.trim().length() > 0) {
//					objCourier = new McourierDAO().findById(couriercode.trim().toUpperCase());
//					if (objCourier != null) {
//						couriercode = null;
//						refreshModel();
//					} else {
//						Messagebox.show("Data kurir tidak dikenal", "Info", Messagebox.OK, Messagebox.INFORMATION);
//					}
//				}
//			} else {
//				Messagebox.show("Silahkan pilih isian grup produk", "Info", Messagebox.OK, Messagebox.INFORMATION);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public void doVerified() {
		try {
			listSelected = new ArrayList<>();
			objList = oDao.listByFilter("mcouriervendor.mcouriervendorpk = "
					+ mcouriervendor.getMcouriervendorpk() + " and " + "productgroup = '" + productgroup
					+ "' and status = '" + AppUtils.STATUS_DELIVERY_EXPEDITIONORDER + "' and branchpool = '"
					+ oUser.getMbranch().getBranchid() + "'", "tdeliverypk");
			listSelected.addAll(objList);
			grid.setModel(new ListModelList<>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Tdelivery obj = (Tdelivery) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							listSelected.add(obj);
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							listSelected.remove(obj);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		try {
			objCourier = null;
			listSelected = new ArrayList<>();
			List<Vcouriervendorsumdata> listSum = oDao
					.getCouriervendorsumdata("status = '" + AppUtils.STATUS_DELIVERY_EXPEDITIONORDER
							+ "' and branchpool = '" + oUser.getMbranch().getBranchid() + "'");
			for (Vcouriervendorsumdata objSum : listSum) {
				map.put(objSum.getProductgroup() + objSum.getVendorcode() + "M",
						objSum.getTotalmanifest() != null ? objSum.getTotalmanifest() : 0);
				map.put(objSum.getProductgroup() + objSum.getVendorcode() + "D",
						objSum.getTotaldata() != null ? objSum.getTotaldata() : 0);
			}
			if (grid.getRows() != null)
				grid.getRows().getChildren().clear();

			getProduct();
			cbProductgroup.setValue(null);
			cbVendor.setValue(null);
			cbCourier.setValue(null);
			tbVendor.setValue(null);
			tbCourierName.setValue(null);
			// refreshSummary();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			if (listSelected.size() == 0) {
				Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
			} else {
				Map<String, Object> map = new HashMap<>();

				Window win = (Window) Executions.createComponents("/view/delivery/deliverymanifestprint.zul", null,
						map);
				win.setClosable(true);
				win.doModal();
				win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@SuppressWarnings("unchecked")
					@Override
					public void onEvent(Event event) throws Exception {
						if (event.getData() != null) {
							Map<String, Object> map = (Map<String, Object>) event.getData();
							isurgent = (String) map.get("isurgent");
							doManifest();
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Command
	@NotifyChange("*")
	public void doManifest() {
		Session session = StoreHibernateUtil.openSession();
		Transaction transaction = session.beginTransaction();
		Map<Integer, Torder> mapOrder = new HashMap<Integer, Torder>();
		Map<Integer, Treturn> mapTreturn = new HashMap<>();
		Map<Integer, Trepairdlv> mapTrepair = new HashMap<>();
		try {
			Tdeliverycourier objForm = new Tdeliverycourier();
			objForm.setMcourier(mcourier);
			objForm.setMcouriervendor(mcouriervendor);
			objForm.setProductgroup(productgroup);
			objForm.setDlvcourierid(new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_EXPEDITION));
			objForm.setTotaldata(listSelected.size());
			objForm.setStatus(AppUtils.STATUS_DELIVERY_DELIVERY);
			objForm.setProcessedby(oUser.getUserid());
			objForm.setProcesstime(new Date());
			objForm.setCourierbranchpool(oUser.getMbranch().getBranchid());
			objForm.setIsurgent(isurgent);
			new TdeliverycourierDAO().save(session, objForm);

			for (Tdelivery data : listSelected) {
				data.setTdeliverycourier(objForm);
				data.setStatus(AppUtils.STATUS_DELIVERY_DELIVERY);
				data.setIsurgent(isurgent);
				oDao.save(session, data);

				List<Tdeliverydata> tddList = new TdeliverydataDAO()
						.listByFilter("tdeliveryfk = " + data.getTdeliverypk(), "tdeliveryfk");
				for (Tdeliverydata tdd : tddList) {
					if (tdd.getTpaketdata().getTpaket().getTorder() != null) {
						mapOrder.put(tdd.getTpaketdata().getTpaket().getTorder().getTorderpk(),
								tdd.getTpaketdata().getTpaket().getTorder());
					} else if (tdd.getTpaketdata().getTpaket().getTrepairdlv() != null) {
						tdd.getTpaketdata().getTpaket().getTrepairdlv().setStatus(AppUtils.STATUS_DELIVERY_DELIVERY);
						new TrepairdlvDAO().save(session, tdd.getTpaketdata().getTpaket().getTrepairdlv());

						mapTrepair.put(tdd.getTpaketdata().getTpaket().getTrepairdlv().getTrepairdlvpk(),
								tdd.getTpaketdata().getTpaket().getTrepairdlv());

					} else if (tdd.getTpaketdata().getTpaket().getTpinpadorderproduct() != null) {
						tdd.getTpaketdata().getTpaket().getTpinpadorderproduct()
								.setStatus(AppUtils.STATUS_DELIVERY_DELIVERY);
						new TpinpadorderproductDAO().save(session,
								tdd.getTpaketdata().getTpaket().getTpinpadorderproduct());
						
						tdd.getTpaketdata().getTpaket().getTpinpadorderproduct().getTorder()
						.setStatus(AppUtils.STATUS_DELIVERY_DELIVERY);
						new TorderDAO().save(session,
						tdd.getTpaketdata().getTpaket().getTpinpadorderproduct().getTorder());
					}

					if (tdd.getTpaketdata().getTpaket().getTswitch() != null) {
						tdd.getTpaketdata().getTpaket().getTswitch().setStatus(AppUtils.STATUS_DELIVERY_DELIVERY);
						new TswitchDAO().save(session, tdd.getTpaketdata().getTpaket().getTswitch());
					}
				}
			}

			for (Entry<Integer, Torder> entry : mapOrder.entrySet()) {
				Torder order = entry.getValue();
				order.setStatus(AppUtils.STATUS_DELIVERY_DELIVERY);
				new TorderDAO().save(session, order);

				FlowHandler.doFlow(session, null, order, AppData.getStatusLabel(order.getStatus()), oUser.getUserid());
			}

			for (Entry<Integer, Treturn> objTreturn : mapTreturn.entrySet()) {

				List<Treturnitem> returnList = new TreturnitemDAO()
						.listByFilter("treturnfk = " + objTreturn.getValue().getTreturnpk(), "treturnfk");

				if (returnList.size() > 0) {
					for (Treturnitem tri : returnList) {
						tri.setItemstatus(AppUtils.STATUS_DELIVERY_DELIVERY);
						new TreturnitemDAO().save(session, tri);
					}
				}

			}

			for (Entry<Integer, Trepairdlv> objTrepair : mapTrepair.entrySet()) {

				List<Trepairitem> repairList = new TrepairitemDAO()
						.listByFilter("trepairdlvfk = " + objTrepair.getValue().getTrepairdlvpk(), "trepairitempk");
				if (repairList.size() > 0) {
					for (Trepairitem tri : repairList) {
						Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
								+ tri.getItemno() + "' and status = '" + tri.getItemstatus() + "'");
						if (objStock != null) {
							objStock.setStatus(tri.getItemstatus());
							new TbranchstockitemDAO().save(session, objStock);

							Tbranchitemtrack objTrack = new Tbranchitemtrack();
							objTrack.setItemno(tri.getItemno());
							objTrack.setTracktime(new Date());
							objTrack.setTrackdesc(AppData.getStatusLabel(tri.getItemstatus()));
							objTrack.setProductgroup(tri.getTrepair().getMproduct().getProductgroup());
							objTrack.setMproduct(tri.getTrepair().getMproduct());
							objTrack.setTrackstatus(tri.getItemstatus());
							new TbranchitemtrackDAO().save(session, objTrack);
						}
						
						if (tri.getItemstatus().trim().equals(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER)) {
							tri.setItemstatus(AppUtils.STATUS_DELIVERY_DELIVERY);
							new TrepairitemDAO().save(session, tri);
						}
					}
				}

			}

			transaction.commit();
			
			Mmenu mmenu = new MmenuDAO().findByFilter(
					"menupath = '/view/delivery/deliverymanifestcourier.zul'");
			NotifHandler.delete(mmenu, oUser.getMbranch(), objForm.getProductgroup(),
					oUser.getMbranch().getBranchlevel());
			
			Clients.showNotification("Submit data manifest delivery kurir berhasil", "info", null, "middle_center",
					2000);
			
			doReset();
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
			Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
		} finally {
			session.close();
		}

	}
	
	public ListModelList<Mcouriervendor> getMcouriervendormodel() {
		ListModelList<Mcouriervendor> lm = null;
		try {
			lm = new ListModelList<Mcouriervendor>(AppData.getMcouriervendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	@Command
	@NotifyChange("mcouriermodel")
	public void doCourierLoad(@BindingParam("item") Mcouriervendor item) {
		if (item != null) {
			try {
				cbCourier.setValue(null);
				tbVendor.setValue(null);
				tbCourierName.setValue(null);
				mcouriermodel = new ListModelList<>(AppData.getMcourier("mcouriervendor.mcouriervendorpk = " + item.getMcouriervendorpk()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int getTotalcard() {
		return totalcard;
	}

	public void setTotalcard(int totalcard) {
		this.totalcard = totalcard;
	}

	public int getTotaltoken() {
		return totaltoken;
	}

	public void setTotaltoken(int totaltoken) {
		this.totaltoken = totaltoken;
	}

	public int getTotalpinpad() {
		return totalpinpad;
	}

	public void setTotalpinpad(int totalpinpad) {
		this.totalpinpad = totalpinpad;
	}

	public int getTotaldoc() {
		return totaldoc;
	}

	public void setTotaldoc(int totaldoc) {
		this.totaldoc = totaldoc;
	}

	public int getTotalsupplies() {
		return totalsupplies;
	}

	public void setTotalsupplies(int totalsupplies) {
		this.totalsupplies = totalsupplies;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public Mcourier getObjCourier() {
		return objCourier;
	}

	public void setObjCourier(Mcourier objCourier) {
		this.objCourier = objCourier;
	}

	public String getIsurgent() {
		return isurgent;
	}

	public void setIsurgent(String isurgent) {
		this.isurgent = isurgent;
	}

	public int getTotalpinmailer() {
		return totalpinmailer;
	}

	public void setTotalpinmailer(int totalpinmailer) {
		this.totalpinmailer = totalpinmailer;
	}
	
	public Mcouriervendor getMcouriervendor() {
		return mcouriervendor;
	}

	public void setMcouriervendor(Mcouriervendor mcouriervendor) {
		this.mcouriervendor = mcouriervendor;
	}

	public Mcourier getMcourier() {
		return mcourier;
	}

	public void setMcourier(Mcourier mcourier) {
		this.mcourier = mcourier;
	}

	public ListModelList<Mcourier> getMcouriermodel() {
		return mcouriermodel;
	}

	public void setMcouriermodel(ListModelList<Mcourier> mcouriermodel) {
		this.mcouriermodel = mcouriermodel;
	}



}
