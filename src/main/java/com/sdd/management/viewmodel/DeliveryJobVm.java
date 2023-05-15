package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
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
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MbranchproductgroupDAO;
import com.sdd.caption.dao.McouriervendorDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproductgroupDAO;
import com.sdd.caption.dao.MusergrouplevelDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpinpadorderproductDAO;
import com.sdd.caption.dao.TrepairdlvDAO;
import com.sdd.caption.dao.TrepairitemDAO;
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.dao.TreturnitemDAO;
import com.sdd.caption.dao.TreturntrackDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mbranchproductgroup;
import com.sdd.caption.domain.Mletter;
import com.sdd.caption.domain.Mlettertype;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergrouplevel;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tpinmailerbranch;
import com.sdd.caption.domain.Trepairdlv;
import com.sdd.caption.domain.Trepairitem;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.domain.Treturntrack;
import com.sdd.caption.domain.Vbranchdelivery;
import com.sdd.caption.domain.Vproductgroupsumdata;
import com.sdd.caption.handler.BranchStockManager;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.VbranchdeliveryListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DeliveryJobVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private VbranchdeliveryListModel model;

	private TdeliveryDAO tdeliveryDao = new TdeliveryDAO();
	private TdeliverydataDAO tdeliverydataDao = new TdeliverydataDAO();
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TembossbranchDAO tembossbranchDao = new TembossbranchDAO();
	private TorderDAO torderDao = new TorderDAO();
	private TpinpadorderproductDAO tpinpadorderproductDao = new TpinpadorderproductDAO();
	private TderivatifDAO tderivatifDao = new TderivatifDAO();
	private TreturnitemDAO treturnitemDao = new TreturnitemDAO();
//	private TbranchstockitemDAO tbranchstockitemDao = new TbranchstockitemDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;

	private Date orderdate;
	private int totaldata;
	private Integer totalselected;
	private Integer totaldataselected;
	private int totalcard;
	private int totalcardphoto;
	private int totaltoken;
	private int totalpinpad;
	private int totalpinmailer;
	private int totaldocument;
	private String branchid;
	private String branchname;
	private String productgroup;
	private Tpaketdata obj;
	private Boolean isSaved;

	private Map<String, Vbranchdelivery> mapData;

	private Map<String, Mbranch> mapBranch = new HashMap<String, Mbranch>();
	Map<String, Tbranchstock> mapTbranchstock = new HashMap<>();

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	private static final String PRODUCTGROUP_CARDPHOTO = "09";

	private Map<String, Integer> mapSum = new HashMap<String, Integer>();

	@Wire
	private Combobox cbBranch;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Div divHeader;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroup = "";
		try {
			mapBranch = new HashMap<String, Mbranch>();
			for (Mbranch obj : AppData.getMbranch()) {
				mapBranch.put(obj.getBranchid(), obj);
			}

			paging.addEventListener("onPaging", new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					PagingEvent pe = (PagingEvent) event;
					pageStartNumber = pe.getActivePage();
					refreshModel(pageStartNumber);
					chkAll.setChecked(false);
				}
			});

			grid.setRowRenderer(new RowRenderer<Vbranchdelivery>() {

				@Override
				public void render(final Row row, final Vbranchdelivery data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							Vbranchdelivery obj = (Vbranchdelivery) checked.getAttribute("obj");
							if (checked.isChecked()) {
								mapData.put(data.getBranchid(), obj);
								totaldataselected += obj.getTotal();
							} else {
								mapData.remove(obj.getBranchid());
								totaldataselected -= obj.getTotal();
							}
							totalselected = mapData.size();
							BindUtils.postNotifyChange(null, null, DeliveryJobVm.this, "totalselected");
							BindUtils.postNotifyChange(null, null, DeliveryJobVm.this, "totaldataselected");
						}
					});
					if (mapData.get(data.getBranchid()) != null)
						check.setChecked(true);
					row.getChildren().add(check);
					row.getChildren().add(new Label(data.getBranchid()));
					row.getChildren().add(new Label(data.getBranchname()));
					row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(
							data.getTotal() != null ? NumberFormat.getInstance().format(data.getTotal()) : "0"));
					Button btnManifest = new Button("Paket");
					btnManifest.setAutodisable("self");
					btnManifest.setClass("btn btn-default btn-sm");
					btnManifest.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnManifest.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("mbranch", mapBranch.get(data.getBranchid()));
							map.put("productgroup", productgroup);

							Window win = (Window) Executions
									.createComponents("/view/delivery/deliverymanifestcreate.zul", null, map);
							win.setWidth("90%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									Boolean isSaved = (Boolean) event.getData();
									if (isSaved != null && isSaved) {
										doReset();
										BindUtils.postNotifyChange(null, null, DeliveryJobVm.this, "*");
									}
								}
							});
						}
					});
					row.getChildren().add(btnManifest);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		doReset();
	}

	@SuppressWarnings("deprecation")
	@NotifyChange("*")
	private void doRenderTab() {
		try {
			divHeader.getChildren().clear();
			List<Mproductgroup> listProductgroup = new ArrayList<Mproductgroup>();
			if (oUser.getMbranch().getBranchlevel() == 1) {
				String filterproduct = "";
				if (oUser.getMbranch().getBranchid().equals("723"))
					filterproduct = "mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and productgroupcode = '04'";
				else
					filterproduct = "mbranchfk = " + oUser.getMbranch().getMbranchpk();
				for (Mbranchproductgroup obj : new MbranchproductgroupDAO().listNativeByFilter(filterproduct,
						"mbranchproductgrouppk")) {
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
				divColHeader.setClass("col-md-3");
				divColHeader.setParent(divHeader);
				Div divCard = new Div();
				divCard.setClass("card");
				divCard.setParent(divColHeader);
				Div divCardHeader = new Div();
				divCardHeader.setClass("card-header");
				divCardHeader.setAlign("center");
				divCardHeader.setParent(divCard);
				Label lblProductgroup = new Label(obj.getProductgroup());
				lblProductgroup.setStyle("font-size: 14px; font-weight: bold");
				lblProductgroup.setParent(divCardHeader);

				Div divCardBody = new Div();
				divCardBody.setClass("card-body");
				divCardBody.setAlign("right");
				divCardBody.setParent(divCard);
				Label lblPaket = new Label("Jumlah Paket :");
				lblPaket.setStyle("font-size: 14px");
				lblPaket.setParent(divCardBody);
				Integer qty = mapSum.get(obj.getProductgroupcode());
				Label lblPaketQty = new Label(qty != null ? NumberFormat.getInstance().format(qty) : "0");
				lblPaketQty.setStyle("font-size: 14px");
				lblPaketQty.setParent(divCardBody);
				A a = new A();
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						doSearchGroup(obj.getProductgroupcode());
						BindUtils.postNotifyChange(null, null, DeliveryJobVm.this, "pageTotalSize");
					}
				});
				Div divLink = new Div();
				divLink.setClass("card-footer");
				divLink.setAlign("center");
				divLink.setParent(a);
				Label lblShow = new Label("Tampilkan Data");
				lblShow.setParent(divLink);
				a.setParent(divCard);
				divColHeader.appendChild(new Separator());
				divColHeader.appendChild(new Separator());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange({ "pageTotalSize", "totaldata" })
	public void refreshModel(int activePage) {
		try {
			orderby = "branchid";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new VbranchdeliveryListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
			if (needsPageUpdate) {
				pageTotalSize = model.getTotalSize(filter);
				needsPageUpdate = false;
			}
			paging.setTotalSize(pageTotalSize);
			grid.setModel(model);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@NotifyChange("*")
	private void doSummary() {
		try {
			totalcard = 0;
			totalcardphoto = 0;
			totaltoken = 0;
			totalpinpad = 0;
			totalpinmailer = 0;
			totaldocument = 0;

			String sumFilter = "isdlv = 'N' and tderivatifproductfk is null and branchpool = '"
					+ oUser.getMbranch().getBranchid() + "' and tpaketdata.zipcode is null";
			String sumFilterDerivatif = "isdlv = 'N' and tderivatifproductfk is not null and branchpool = '"
					+ oUser.getMbranch().getBranchid() + "' and tpaketdata.zipcode is null";

			List<Vproductgroupsumdata> listSum = tpaketdataDao.geSumPaketByProductGroup(sumFilter);
			for (Vproductgroupsumdata objSum : listSum) {
				mapSum.put(objSum.getProductgroup(), objSum.getTotal());
				/*
				 * switch (objSum.getProductgroup()) { case AppUtils.PRODUCTGROUP_CARD:
				 * totalcard = objSum.getTotal(); break; case AppUtils.PRODUCTGROUP_TOKEN:
				 * totaltoken = objSum.getTotal(); break; case AppUtils.PRODUCTGROUP_PINPAD:
				 * totalpinpad = objSum.getTotal(); break; case AppUtils.PRODUCTGROUP_PINMAILER:
				 * totalpinmailer = objSum.getTotal(); break; case
				 * AppUtils.PRODUCTGROUP_BILYETDEPOSITO: totaldocument = objSum.getTotal();
				 * break; }
				 */
			}

			listSum = tpaketdataDao.geSumPaketByProductGroup(sumFilterDerivatif);
			for (Vproductgroupsumdata objSum : listSum) {
				mapSum.put(objSum.getProductgroup(), objSum.getTotal());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				if (comp.getChildren() != null && comp.getChildren().size() > 0) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Vbranchdelivery obj = (Vbranchdelivery) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getBranchid(), obj);
							totaldataselected += obj.getTotal();
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getBranchid());
							totaldataselected -= obj.getTotal();
						}
					}
				}
			}
			totalselected = mapData.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doViewSelected() {
		if (mapData.size() > 0) {
			Map<String, Object> map = new HashMap<>();
			map.put("mapData", mapData);
			map.put("totalselected", totalselected);
			map.put("totaldataselected", totaldataselected);

			Window win = (Window) Executions.createComponents("/view/delivery/deliverybranchselected.zul", null, map);
			win.setWidth("70%");
			win.setClosable(true);
			win.doModal();
		} else {
			Messagebox.show("Tidak ada data yang dipilih");
		}
	}

	@NotifyChange("*")
	@Command
	public void doResetSelected() {
		if (mapData.size() > 0) {
			Messagebox.show("Anda ingin mereset data-data yang sudah anda pilih?", "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								totalselected = 0;
								totaldataselected = 0;
								mapData = new HashMap<>();
								refreshModel(pageStartNumber);
								BindUtils.postNotifyChange(null, null, DeliveryJobVm.this, "*");
							}
						}
					});
		}
	}

	@Command
	public void doDeliveryGroup() {
		if (mapData.size() == 0) {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		} else {
			List<Vbranchdelivery> objList = new ArrayList<>();
			for (Entry<String, Vbranchdelivery> entry : mapData.entrySet()) {
				Vbranchdelivery data = entry.getValue();
				objList.add(data);
			}
			Map<String, Object> map = new HashMap<>();
			map.put("objSelected", objList);
			map.put("productgroup", productgroup);
			Window win = (Window) Executions.createComponents("/view/delivery/deliverymanifestgenerate.zul", null, map);
			win.setClosable(true);
			win.doModal();
			win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getData() != null) {
						boolean isError = false;
						String strError = "";
						Session session = null;
						Transaction transaction = null;

						@SuppressWarnings("unchecked")
						Map<String, Object> map = (Map<String, Object>) event.getData();
						Tdelivery tdelivery = (Tdelivery) map.get("obj");
						Mletter mletter = (Mletter) map.get("prefix");
						Mlettertype mlettertype = (Mlettertype) map.get("lettertype");
						String type = (String) map.get("type");
						String kurir = (String) map.get("kurir");
						String filterpaket = "";
						if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
							filterpaket = "isdlv = 'N' and tpaket.tderivatifproductfk is not null and branchpool = '"
									+ oUser.getMbranch().getBranchid() + "' and tpaketdata.zipcode is null";
						else
							filterpaket = "isdlv = 'N' and tpaket.tderivatifproductfk is null and tpaket.productgroup = '"
									+ productgroup + "' and branchpool = '" + oUser.getMbranch().getBranchid()
									+ "' and tpaketdata.zipcode is null";

						for (Entry<String, Vbranchdelivery> entry : mapData.entrySet()) {
							Vbranchdelivery objDelivery = entry.getValue();
							session = StoreHibernateUtil.openSession();
							transaction = session.beginTransaction();
							Map<Integer, Tderivatif> mapTderivatif = new HashMap<>();
							Map<Integer, Treturn> mapTreturn = new HashMap<>();
							Map<Integer, Trepairdlv> mapTrepair = new HashMap<>();
							Map<Integer, Tembossproduct> mapTembossproduct = new HashMap<>();
							Map<Integer, Tpinmailerbranch> mapTpinmailer = new HashMap<>();
							try {
								Tdelivery objForm = new Tdelivery();
								objForm.setMbranch(mapBranch.get(objDelivery.getBranchid()));
								if (kurir.equals("D")) {
									if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
										if (objForm.getMbranch().getIsintercity().equals("Y"))
											objForm.setMcouriervendor(
													new McouriervendorDAO().findByFilter("vendorcode = 'PND'"));
										else
											objForm.setMcouriervendor(objForm.getMbranch().getMcouriervendor());
									}

									if (objForm.getMbranch().getMcouriervendor() != null)
										objForm.setMcouriervendor(objForm.getMbranch().getMcouriervendor());
									else
										objForm.setMcouriervendor(
												new McouriervendorDAO().findByFilter("vendorcode = 'DLM'"));
								} else
									objForm.setMcouriervendor(tdelivery.getMcouriervendor());
								if (productgroup.equals(PRODUCTGROUP_CARDPHOTO)) {
									objForm.setIsproductphoto("Y");
									objForm.setProductgroup(AppUtils.PRODUCTGROUP_CARD);
								} else {
									objForm.setIsproductphoto("N");
									objForm.setProductgroup(productgroup);
								}
								if (type.equals("A")) {
									if (oUser.getMbranch().getBranchlevel() > 1) {
										if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
											objForm.setDlvid(new TcounterengineDAO().generateBranchLetterNo(
													mapBranch.get(objDelivery.getBranchid()).getBranchcode(), "SB"));
										} else {
											objForm.setDlvid(new TcounterengineDAO().generateBranchLetterNo(
													mapBranch.get(objDelivery.getBranchid()).getBranchcode(),
													mletter.getLetterprefix()));
										}
									} else {
										if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
											objForm.setDlvid(
													new TcounterengineDAO().generateBranchLetterNo("PFA", "SB"));
										} else {
											objForm.setDlvid(new TcounterengineDAO()
													.generateLetterNo(mletter.getLetterprefix()));
										}
									}
								} else
									objForm.setDlvid(
											tdelivery.getDlvid() != null && !"".equals(tdelivery.getDlvid().trim())
													? tdelivery.getDlvid()
													: new TcounterengineDAO().generateLetterNo("CRD"));
								objForm.setBeratitem(tdelivery.getBeratitem());
								if (!productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
									objForm.setLettertype(mlettertype.getLettertype());
								objForm.setTotaldata(objDelivery.getTotal());
								if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
									objForm.setStatus(AppUtils.STATUS_DELIVERY_WAITAPPROVAL);
								else
									objForm.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
								objForm.setMemo(tdelivery.getMemo());
								objForm.setProcesstime(tdelivery.getProcesstime());
								objForm.setProcessedby(oUser.getUserid());
								objForm.setBranchpool(oUser.getMbranch().getBranchid());
								objForm.setAddress1(mapBranch.get(objDelivery.getBranchid()).getBranchaddress());
								objForm.setAddress2("");
								objForm.setAddress3("");
								objForm.setZipcode("");
								objForm.setIsdlvcust("N");

								BigDecimal totalprice = new BigDecimal(0);
								BigDecimal totalamount = new BigDecimal(0);
								List<Tpaketdata> objList = tpaketdataDao.listDelivery("mbranch.mbranchpk = "
										+ objForm.getMbranch().getMbranchpk() + " and " + filterpaket);
								for (Tpaketdata data : objList) {
									if (data.getTpaket().getTorder() != null) {
										if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
											data.getTpaket().getTorder()
													.setStatus(AppUtils.STATUS_DELIVERY_WAITAPPROVAL);
										} else
											data.getTpaket().getTorder()
													.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
										torderDao.save(session, data.getTpaket().getTorder());

										if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)
												|| productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)
												|| productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
											totalprice = new TorderitemDAO().sumTotalPriceByDlv(
													"torderfk = " + data.getTpaket().getTorder().getTorderpk());
											totalamount = totalamount.add(totalprice);
										}

										if (data.getTpinmailerbranch() != null) {
											mapTpinmailer.put(data.getTpinmailerbranch().getTpinmailerbranchpk(),
													data.getTpinmailerbranch());
										}

										FlowHandler.doFlow(session, null, data.getTpaket().getTorder(),
												tdelivery.getMemo(), oUser.getUserid());

									} else if (data.getTembossbranch() != null) {
										data.getTembossbranch().setStatus(AppUtils.STATUSBRANCH_PROSESDELIVERY);
										data.getTembossbranch().setDlvfinishtime(new Date());
										tembossbranchDao.save(session, data.getTembossbranch());
										mapTembossproduct.put(
												data.getTpaket().getTembossproduct().getTembossproductpk(),
												data.getTpaket().getTembossproduct());

									} else if (data.getTpaket().getTreturn() != null) {
										data.getTpaket().getTreturn()
												.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
										new TreturnDAO().save(session, data.getTpaket().getTreturn());

										mapTreturn.put(data.getTpaket().getTreturn().getTreturnpk(),
												data.getTpaket().getTreturn());

										Treturntrack objrt = new Treturntrack();
										objrt.setTreturn(data.getTpaket().getTreturn());
										objrt.setTracktime(new Date());
										objrt.setTrackstatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
										objrt.setTrackdesc(AppData.getStatusLabel(objrt.getTrackstatus()));
										new TreturntrackDAO().save(session, objrt);

										if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)
												|| productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
											totalprice = new TreturnitemDAO().sumTotalPriceByDlv(
													"treturnfk = " + data.getTpaket().getTreturn().getTreturnpk());
											totalamount = totalamount.add(totalprice);
										}

									} else if (data.getTpaket().getTrepairdlv() != null) {
										data.getTpaket().getTrepairdlv()
												.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
										new TrepairdlvDAO().save(session, data.getTpaket().getTrepairdlv());

										mapTrepair.put(data.getTpaket().getTrepairdlv().getTrepairdlvpk(),
												data.getTpaket().getTrepairdlv());
										totalprice = new TrepairitemDAO().sumTotalPriceByDlv(
												"trepairdlvfk = " + data.getTpaket().getTrepairdlv().getTrepairdlvpk());
										totalamount = totalamount.add(totalprice);

									} else if (data.getTpaket().getTpinpadorderproduct() != null) {
										data.getTpaket().getTpinpadorderproduct().getTorder()
												.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
										new TorderDAO().save(session,
												data.getTpaket().getTpinpadorderproduct().getTorder());

										data.getTpaket().getTpinpadorderproduct()
												.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
										new TpinpadorderproductDAO().save(session,
												data.getTpaket().getTpinpadorderproduct());

										totalprice = new TorderitemDAO().sumTotalPriceByDlv("Tpinpadorderproductfk = "
												+ data.getTpaket().getTpinpadorderproduct().getTpinpadorderproductpk());
										totalamount = totalamount.add(totalprice);
									}

									if (data.getTpaket().getTswitch() != null) {
										data.getTpaket().getTswitch()
												.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
										new TswitchDAO().save(session, data.getTpaket().getTswitch());
									}

									if (productgroup.equals(PRODUCTGROUP_CARDPHOTO)) {
										mapTderivatif.put(
												data.getTpaket().getTderivatifproduct().getTderivatif()
														.getTderivatifpk(),
												data.getTpaket().getTderivatifproduct().getTderivatif());
									}

									objForm.setTotalamount(totalamount);
									tdeliveryDao.save(session, objForm);

									Tdeliverydata deliverydata = new Tdeliverydata();
									deliverydata.setTdelivery(objForm);
									deliverydata.setTpaketdata(data);
									if (data.getTpaket().getTorder() != null)
										deliverydata.setOrderid(data.getTpaket().getTorder().getOrderid());
									deliverydata.setMproduct(data.getTpaket().getMproduct());
									deliverydata.setProductgroup(data.getTpaket().getProductgroup());
									deliverydata.setOrderdate(data.getOrderdate());
									deliverydata.setQuantity(data.getQuantity());
									tdeliverydataDao.save(session, deliverydata);

									data.setIsdlv("Y");
									tpaketdataDao.save(session, data);

									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());

								}

								transaction.commit();
								session.close();

								for (Entry<Integer, Tembossproduct> emboss : mapTembossproduct.entrySet()) {
									BranchStockManager.manageCard(emboss.getValue(), objForm.getMbranch());
								}

								for (Entry<Integer, Tpinmailerbranch> pinmailer : mapTpinmailer.entrySet()) {
									BranchStockManager.managePinmailer(pinmailer.getValue());
								}

								for (Entry<Integer, Tderivatif> objTderivatif : mapTderivatif.entrySet()) {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();

									Tderivatif data = objTderivatif.getValue();
									data.setTdelivery(objForm);
									data.setStatus(AppUtils.STATUS_DERIVATIF_DELIVERY);
									data.setDlvfinishtime(new Date());
									tderivatifDao.save(session, data);

									transaction.commit();
									session.close();

									BranchStockManager.manageCardDerivatif(data);

								}

								for (Entry<Integer, Treturn> objTreturn : mapTreturn.entrySet()) {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();

									List<Treturnitem> returnList = new TreturnitemDAO().listByFilter(
											"treturnfk = " + objTreturn.getValue().getTreturnpk(), "treturnfk");

									if (returnList.size() > 0) {
										for (Treturnitem tri : returnList) {
											tri.setItemstatus(AppUtils.STATUS_DELIVERY_DELIVERYORDER);
											treturnitemDao.save(session, tri);
										}
									}

									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/delivery/deliverymanifestcourier.zul'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());

									transaction.commit();
									session.close();

								}

								for (Entry<Integer, Trepairdlv> objTrepair : mapTrepair.entrySet()) {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();

									List<Trepairitem> repairList = new TrepairitemDAO().listByFilter(
											"trepairdlvfk = " + objTrepair.getValue().getTrepairdlvpk(),
											"trepairitempk");
									if (repairList.size() > 0) {
										for (Trepairitem tri : repairList) {
											Tbranchstockitem objStock = new TbranchstockitemDAO()
													.findByFilter("itemno = '" + tri.getItemno() + "' and status = '"
															+ tri.getItemstatus() + "'");
											if (objStock != null) {
												objStock.setStatus(tri.getItemstatus());
												new TbranchstockitemDAO().save(session, objStock);

												Tbranchitemtrack objTrack = new Tbranchitemtrack();
												objTrack.setItemno(tri.getItemno());
												objTrack.setTracktime(new Date());
												objTrack.setTrackdesc(AppData.getStatusLabel(tri.getItemstatus()));
												objTrack.setProductgroup(
														tri.getTrepair().getMproduct().getProductgroup());
												objTrack.setMproduct(tri.getTrepair().getMproduct());
												objTrack.setTrackstatus(tri.getItemstatus());
												new TbranchitemtrackDAO().save(session, objTrack);
											}

											if (tri.getItemstatus().trim().equals(AppUtils.STATUS_DELIVERY_PAKETDONE)) {
												tri.setItemstatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
												new TrepairitemDAO().save(session, tri);
											}
										}
									}

									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/delivery/deliverymanifestcourier.zul'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
											oUser.getMbranch().getBranchlevel());

									transaction.commit();
									session.close();
								}

								if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
									Musergrouplevel grouplevel = new MusergrouplevelDAO().findByFilter(
											"branchlevel = " + oUser.getMbranch().getBranchlevel() + " and "
													+ objForm.getTotalamount() + " between amountstart and amountend");
									if (grouplevel != null) {
										if (grouplevel.getGrouplevel() == 3) {
											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/delivery/cardapprovaldelivery.zul' and menuparamvalue = 'kelompok'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
													oUser.getMbranch().getBranchlevel());
										} else if (grouplevel.getGrouplevel() == 2) {
											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/delivery/cardapprovaldelivery.zul' and menuparamvalue = 'wakil'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
													oUser.getMbranch().getBranchlevel());
										} else if (grouplevel.getGrouplevel() == 1) {
											Mmenu mmenu = new MmenuDAO().findByFilter(
													"menupath = '/view/delivery/cardapprovaldelivery.zul' and menuparamvalue = 'pimpinan'");
											NotifHandler.doNotif(mmenu, oUser.getMbranch(), productgroup,
													oUser.getMbranch().getBranchlevel());
										}
									}
								} else {
									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/delivery/deliverymanifestcourier.zul'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), objForm.getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								}
							} catch (HibernateException e) {
								transaction.rollback();
								isError = true;
								if (strError.length() > 0)
									strError += ". ";
								strError += e.getMessage();
								e.printStackTrace();
							} catch (Exception e) {
								transaction.rollback();
								isError = true;
								if (strError.length() > 0)
									strError += ". ";
								strError += e.getMessage();
								e.printStackTrace();
							} finally {
								if (session.isOpen())
									session.close();
							}

						}

						if (isError) {
							Messagebox.show("Terdapat masalah pada proses pembuatan manifest pengiriman. \n" + strError,
									WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
						} else {
							Messagebox.show("Proses pembuatan manifest pengiriman berhasil" + strError,
									WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
							doReset();
							BindUtils.postNotifyChange(null, null, DeliveryJobVm.this, "*");
						}
					}
				}

			});

		}
	}

	@Command
	@NotifyChange("*")
	public void doSearchGroup(@BindingParam("item") String item) {
		try {
			productgroup = item;
			doSearch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			totaldata = 0;
			totalselected = 0;
			if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
				filter = "isdlv ='N' and tderivatifproductfk is not null and branchpool = '"
						+ oUser.getMbranch().getBranchid() + "' and tpaketdata.zipcode is null";
			else
				filter = "tpaket.productgroup = '" + productgroup
						+ "' and isdlv ='N' and tderivatifproductfk is null and branchpool = '"
						+ oUser.getMbranch().getBranchid() + "' and tpaketdata.zipcode is null";

			if (branchid != null && branchid.trim().length() > 0)
				filter += " and mbranch.branchid like '%" + branchid.trim().toUpperCase() + "%'";
			if (branchname != null && branchname.trim().length() > 0)
				filter += " and mbranch.branchname like '%" + branchname.trim().toUpperCase() + "%'";
			if (orderdate != null)
				filter += " and orderdate = '" + dateFormatter.format(orderdate) + "'";

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		try {
			obj = new Tpaketdata();
			totaldata = 0;
			totalselected = 0;
			totaldataselected = 0;
			mapData = new HashMap<>();
			mapSum = new HashMap<String, Integer>();
			if (grid.getRows() != null)
				grid.getRows().getChildren().clear();
			doSearch();

			doSummary();
			doRenderTab();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ListModelList<Mbranch> getMbranch() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(AppData.getMbranch());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Tpaketdata getObj() {
		return obj;
	}

	public void setObj(Tpaketdata obj) {
		this.obj = obj;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public String getBranchid() {
		return branchid;
	}

	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public int getTotalcard() {
		return totalcard;
	}

	public int getTotaltoken() {
		return totaltoken;
	}

	public int getTotalpinpad() {
		return totalpinpad;
	}

	public void setTotalcard(int totalcard) {
		this.totalcard = totalcard;
	}

	public void setTotaltoken(int totaltoken) {
		this.totaltoken = totaltoken;
	}

	public void setTotalpinpad(int totalpinpad) {
		this.totalpinpad = totalpinpad;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}

	public int getTotalpinmailer() {
		return totalpinmailer;
	}

	public void setTotalpinmailer(int totalpinmailer) {
		this.totalpinmailer = totalpinmailer;
	}

	public int getTotalcardphoto() {
		return totalcardphoto;
	}

	public void setTotalcardphoto(int totalcardphoto) {
		this.totalcardphoto = totalcardphoto;
	}

	public int getTotaldocument() {
		return totaldocument;
	}

	public void setTotaldocument(int totaldocument) {
		this.totaldocument = totaldocument;
	}

}
