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
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
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
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Mletter;
import com.sdd.caption.domain.Mlettertype;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergrouplevel;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Trepairdlv;
import com.sdd.caption.domain.Trepairitem;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.domain.Treturntrack;
import com.sdd.caption.handler.BranchStockManager;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DeliveryManifestCreateVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TdeliveryDAO tdeliveryDao = new TdeliveryDAO();
	private TdeliverydataDAO tdeliverydataDao = new TdeliverydataDAO();
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TderivatifDAO tderivatifDao = new TderivatifDAO();
	private TembossbranchDAO tembossbranchDao = new TembossbranchDAO();
	private TorderDAO torderDao = new TorderDAO();
	private ListModelList<Mproduct> mproductmodel = new ListModelList<>();
	private List<Tpaketdata> objList = new ArrayList<Tpaketdata>();
	private List<Tpaketdata> objSelected = new ArrayList<Tpaketdata>();

	private String filter, beratitem;

	private Tdelivery objForm;
	private Mletter mletter;
	private Mlettertype mlettertype;
	private Mbranch mbranch;

	private Date orderdate;
	private Date orderdate2;
	private int totalrecord;
	private int totaldata;
	private int totalcard;
	private int totaltoken;
	private int totalpinpad;
	private int totaldoc;
	private int totalsupplies;
	private String productgroup;
	private String productgroupname;
	private String producttype;
	private String productcode;
	private String productname;
	private Date prodfinishdate;
	private String type;
	private String letterno;
	private Boolean isSaved;
	private String inserttime;
	private String memo;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	private static final String PRODUCTGROUP_CARDPHOTO = "09";

	@Wire
	private Window winDelivery;
	@Wire
	private Combobox cbCouriervendor;
	@Wire
	private Combobox cbLetter;
	@Wire
	private Textbox tbLetter;
	@Wire
	private Grid grid;
	@Wire
	private Row rowcbLetter, rowBerat;
	@Wire
	private Row rowtbLetter;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("mbranch") Mbranch mbranch, @ExecutionArgParam("productgroup") String productgroup)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.mbranch = mbranch;
		this.productgroup = productgroup;

		if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
			productgroupname = AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_CARD);
		else
			productgroupname = AppData.getProductgroupLabel(productgroup);
		if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
			chkAll.setDisabled(true);
		else
			chkAll.setDisabled(false);
		type = "A";
		rowcbLetter.setVisible(true);
		rowtbLetter.setVisible(false);

		System.out.println(productgroup);
		System.out.println(AppUtils.PRODUCTGROUP_DOCUMENT);

		if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			rowcbLetter.setVisible(false);
			rowBerat.setVisible(true);
		} else {
			rowBerat.setVisible(false);
		}

		letterno = "";
		doReset();
		grid.setRowRenderer(new RowRenderer<Tpaketdata>() {

			@Override
			public void render(Row row, final Tpaketdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				Checkbox chkbox = new Checkbox();
				chkbox.setChecked(true);
				chkbox.setAttribute("obj", data);
				chkbox.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tpaketdata obj = (Tpaketdata) checked.getAttribute("obj");
						if (checked.isChecked()) {
							objSelected.add(obj);
							totaldata += obj.getQuantity();
						} else {
							objSelected.remove(obj);
							totaldata -= obj.getQuantity();
						}
						BindUtils.postNotifyChange(null, null, DeliveryManifestCreateVm.this, "totaldata");
					}
				});
				if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
					chkbox.setDisabled(true);
				row.getChildren().add(chkbox);
				row.getChildren().add(new Label(data.getNopaket()));
				row.getChildren().add(new Label(data.getTpaket().getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getTpaket().getMproduct().getProductname()));
				row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));

				if (data.getTpaket().getTorder() != null)
					inserttime = datetimelocalFormatter.format(data.getTpaket().getTorder().getInserttime());
				else if (data.getTpaket().getTreturn() != null)
					inserttime = datetimelocalFormatter.format(data.getTpaket().getTreturn().getInserttime());
				else if (data.getTpaket().getTrepairdlv() != null)
					inserttime = datetimelocalFormatter.format(data.getTpaket().getTrepairdlv().getProcesstime());
				else if (data.getTpaket().getTpinpadorderproduct() != null)
					inserttime = datetimelocalFormatter
							.format(data.getTpaket().getTpinpadorderproduct().getTorder().getInserttime());
				else
					inserttime = datetimelocalFormatter.format(data.getTpaket().getTperso().getPersofinishtime());

				row.getChildren().add(new Label(inserttime));
				row.getChildren().add(new Label(datetimelocalFormatter.format(data.getPaketstarttime())));
				row.getChildren().add(new Label(
						data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "0"));
				Button btndetail = new Button("Detail");
				btndetail.setAutodisable("self");
				btndetail.setClass("btn btn-default btn-sm");
				btndetail.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();

						Window win = new Window();
						if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD)) {
							map.put("tembossbranch", data.getTembossbranch());
							win = (Window) Executions.createComponents("/view/emboss/embossdata.zul", null, map);
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
							if (data.getTpaket().getTrepairdlv() != null) {
								map.put("objDlv", data.getTpaket().getTrepairdlv());
								win = (Window) Executions.createComponents("/view/repair/repairitem.zul", null, map);
							} else if (data.getTpaket().getTpinpadorderproduct() != null) {
								map.put("obj", data.getTpaket().getTpinpadorderproduct().getTorder());
								win = (Window) Executions.createComponents("/view/order/orderitem.zul", null, map);
							} else {
								map.put("obj", data.getTpaket().getTorder());
								win = (Window) Executions.createComponents("/view/order/orderitem.zul", null, map);
							}
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
							map.put("obj", data.getTpaket().getTorder());
							win = (Window) Executions.createComponents("/view/order/tokendata.zul", null, map);
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
							map.put("obj", data.getTpinmailerbranch());
							win = (Window) Executions.createComponents("/view/pinmailer/pinmailerdata.zul", null, map);
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_CARDPHOTO)) {
							map.put("obj", data.getTpaket().getTderivatifproduct());
							win = (Window) Executions.createComponents("/view/derivatif/derivatifdata.zul", null, map);
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
							if (data.getTpaket().getTreturn() != null) {
								map.put("obj", data.getTpaket().getTreturn());
								win = (Window) Executions.createComponents("/view/return/returproductitem.zul", null,
										map);
							} else {
								map.put("obj", data.getTpaket().getTorder());
								win = (Window) Executions.createComponents("/view/order/orderitem.zul", null, map);
							}

						}
						win.setWidth("60%");
						win.setClosable(true);
						win.doModal();
					}
				});
				row.getChildren().add(btndetail);
			}
		});
	}

	@Command
	public void doPersotypeSelected(@BindingParam("type") String type) {
		if (type.equals("A")) {
			rowcbLetter.setVisible(true);
			rowtbLetter.setVisible(false);
		} else if (type.equals("M")) {
			rowcbLetter.setVisible(false);
			rowtbLetter.setVisible(true);
		}
	}

	@Command
	@NotifyChange("totaldata")
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tpaketdata obj = (Tpaketdata) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						chk.setChecked(true);
						objSelected.add(obj);
						totaldata += obj.getQuantity();
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						objSelected.remove(obj);
						totaldata -= obj.getQuantity();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange({ "totalrecord", "totaldata" })
	public void doSearch() {
		try {
			if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
				filter = "mbranch.mbranchpk = " + mbranch.getMbranchpk()
						+ " and isdlv = 'N' and tpaket.tderivatifproductfk is not null and branchpool = '"
						+ oUser.getMbranch().getBranchid() + "' and tpaketdata.zipcode is null";
			else
				filter = "mbranch.mbranchpk = " + mbranch.getMbranchpk()
						+ " and isdlv = 'N' and tpaket.productgroup = '" + productgroup
						+ "' and tpaket.tderivatifproductfk is null and branchpool = '"
						+ oUser.getMbranch().getBranchid() + "' and tpaketdata.zipcode is null";

			if (productcode != null && productcode.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
			}
			if (productname != null && productname.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
			}
			if (orderdate != null) {
				if (orderdate2 != null) {
					if (filter.length() > 0)
						filter += " and ";
					filter += "Tpaket.orderdate between '" + dateFormatter.format(orderdate) + "' and '"
							+ dateFormatter.format(orderdate2) + "'";
				} else {
					if (filter.length() > 0)
						filter += " and ";
					filter += "tpaket.orderdate = '" + dateFormatter.format(orderdate) + "'";
				}
			}

			objSelected = new ArrayList<>();
			objList = tpaketdataDao.listDelivery(filter);
			objSelected.addAll(objList);
			totalrecord = objList.size();
			for (Tpaketdata data : objList) {
				totaldata += data.getQuantity();
			}
			grid.setModel(new ListModelList<Tpaketdata>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totaldata = 0;
		totalrecord = 0;
		objForm = new Tdelivery();
		objForm.setMcouriervendor(mbranch.getMcouriervendor());
		objForm.setProcesstime(new Date());
		orderdate = null;
		orderdate2 = null;
		if (objForm.getMcouriervendor() != null)
			cbCouriervendor.setValue(objForm.getMcouriervendor().getVendorcode());
		objSelected = new ArrayList<>();
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (objSelected.size() > 0) {
			try {
				Messagebox.show("Anda ingin membuat manifest delivery?", "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									boolean isError = false;
									String strError = "";

									Session session = null;
									Transaction transaction = null;

									Map<Integer, Tderivatif> mapTderivatif = new HashMap<>();
									Map<Integer, Torder> mapTorder = new HashMap<>();
									Map<Integer, Treturn> mapTreturn = new HashMap<>();
									Map<Integer, Trepairdlv> mapTrepairdlv = new HashMap<>();
									Map<Integer, Tembossproduct> mapTembossproduct = new HashMap<>();
									try {
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										objForm.setMbranch(mbranch);
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
													objForm.setDlvid(new TcounterengineDAO()
															.generateBranchLetterNo(mbranch.getBranchcode(), "SB"));
												} else {
													objForm.setDlvid(new TcounterengineDAO().generateBranchLetterNo(
															mbranch.getBranchcode(), mletter.getLetterprefix()));
												}
											} else {
												if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
													objForm.setDlvid(new TcounterengineDAO()
															.generateBranchLetterNo("PFA", "SB"));
												} else {
													objForm.setDlvid(new TcounterengineDAO()
															.generateLetterNo(mletter.getLetterprefix()));
												}
											}
										} else if (type.equals("M")) {
											objForm.setDlvid(letterno);
										}
										objForm.setLettertype(mlettertype.getLettertype());
										objForm.setTotaldata(objSelected.size());
										if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
											objForm.setStatus(AppUtils.STATUS_DELIVERY_WAITAPPROVAL);
										else
											objForm.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
										objForm.setProcessedby(oUser.getUserid());
										objForm.setBranchpool(oUser.getMbranch().getBranchid());
										objForm.setAddress1(mbranch.getBranchaddress());
										objForm.setAddress2("");
										objForm.setAddress3("");
										objForm.setZipcode("");
										objForm.setIsdlvcust("N");

										BigDecimal totalprice = new BigDecimal(0);
										BigDecimal totalamount = new BigDecimal(0);
										for (Tpaketdata data : objSelected) {
											Tdeliverydata tdeliverydata = new Tdeliverydata();
											tdeliverydata.setTdelivery(objForm);
											tdeliverydata.setTpaketdata(data);
											if (data.getTpaket().getTorder() != null)
												tdeliverydata.setOrderid(data.getTpaket().getTorder().getOrderid());
											tdeliverydata.setMproduct(data.getTpaket().getMproduct());
											tdeliverydata.setOrderdate(data.getOrderdate());
											tdeliverydata.setQuantity(data.getQuantity());
											tdeliverydata.setProductgroup(objForm.getProductgroup());
											tdeliverydataDao.save(session, tdeliverydata);

											data.setIsdlv("Y");
											tpaketdataDao.save(session, data);

											if (data.getTpaket().getTorder() != null) {
												data.getTpaket().getTorder()
														.setStatus(AppUtils.STATUS_DELIVERY_WAITAPPROVAL);
												torderDao.save(session, data.getTpaket().getTorder());

												mapTorder.put(data.getTpaket().getTorder().getTorderpk(),
														data.getTpaket().getTorder());

												if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)
														|| productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)
														|| productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
													totalprice = new TorderitemDAO().sumTotalPriceByDlv(
															"torderfk = " + data.getTpaket().getTorder().getTorderpk());
													totalamount = totalamount.add(totalprice);
												}

												FlowHandler.doFlow(session, null, data.getTpaket().getTorder(),
														objForm.getMemo(), oUser.getUserid());

											} else if (data.getTembossbranch() != null) {
												data.getTembossbranch().setStatus(AppUtils.STATUSBRANCH_PROSESDELIVERY);
												tembossbranchDao.save(session, data.getTembossbranch());

												mapTembossproduct.put(
														data.getTpaket().getTembossproduct().getTembossproductpk(),
														data.getTpaket().getTembossproduct());

											} else if (data.getTpaket().getTreturn() != null) {
												data.getTpaket().getTreturn()
														.setStatus(AppUtils.STATUS_DELIVERY_WAITAPPROVAL);
												new TreturnDAO().save(session, data.getTpaket().getTreturn());

												mapTreturn.put(data.getTpaket().getTreturn().getTreturnpk(),
														data.getTpaket().getTreturn());

												Treturntrack objrt = new Treturntrack();
												objrt.setTreturn(data.getTpaket().getTreturn());
												objrt.setTracktime(new Date());
												objrt.setTrackstatus(AppUtils.STATUS_DELIVERY_WAITAPPROVAL);
												objrt.setTrackdesc(AppData.getStatusLabel(objrt.getTrackstatus()));
												new TreturntrackDAO().save(session, objrt);

												if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)
														|| productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)
														|| productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
													totalprice = new TreturnitemDAO().sumTotalPriceByDlv("treturnfk = "
															+ data.getTpaket().getTreturn().getTreturnpk());
													totalamount = totalamount.add(totalprice);
												}

											} else if (data.getTpaket().getTrepairdlv() != null) {
												data.getTpaket().getTrepairdlv()
														.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
												new TrepairdlvDAO().save(session, data.getTpaket().getTrepairdlv());

												mapTrepairdlv.put(data.getTpaket().getTrepairdlv().getTrepairdlvpk(),
														data.getTpaket().getTrepairdlv());

												totalprice = new TrepairitemDAO().sumTotalPriceByDlv("trepairdlvfk = "
														+ data.getTpaket().getTrepairdlv().getTrepairdlvpk());
												totalamount = totalamount.add(totalprice);

											} else if (data.getTpaket().getTpinpadorderproduct() != null) {
												data.getTpaket().getTpinpadorderproduct()
														.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
												new TpinpadorderproductDAO().save(session,
														data.getTpaket().getTpinpadorderproduct());

												totalprice = new TorderitemDAO().sumTotalPriceByDlv(
														"Tpinpadorderproductfk = " + data.getTpaket()
																.getTpinpadorderproduct().getTpinpadorderproductpk());
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

											Mmenu mmenu = new MmenuDAO()
													.findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
											NotifHandler.delete(mmenu, oUser.getMbranch(), productgroup,
													oUser.getMbranch().getBranchlevel());
										}
										objForm.setTotalamount(totalamount);
										tdeliveryDao.save(session, objForm);

										if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
											Musergrouplevel grouplevel = new MusergrouplevelDAO()
													.findByFilter("branchlevel = " + oUser.getMbranch().getBranchlevel()
															+ " and " + objForm.getTotalamount()
															+ " between amountstart and amountend");
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
										}

										transaction.commit();
										session.close();

										for (Entry<Integer, Tembossproduct> embossBranch : mapTembossproduct
												.entrySet()) {
											BranchStockManager.manageCard(embossBranch.getValue(), objForm.getMbranch());
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
													tri.setItemstatus(AppUtils.STATUS_DELIVERY_WAITAPPROVAL);
													new TreturnitemDAO().save(session, tri);
												}
											}

											transaction.commit();
											session.close();

										}

										for (Entry<Integer, Trepairdlv> objTrepairdlv : mapTrepairdlv.entrySet()) {
											session = StoreHibernateUtil.openSession();
											transaction = session.beginTransaction();

											List<Trepairitem> repairList = new TrepairitemDAO().listByFilter(
													"trepairdlvfk = " + objTrepairdlv.getValue().getTrepairdlvpk(),
													"trepairdlvfk");
											if (repairList.size() > 0) {
												for (Trepairitem tri : repairList) {
													Tbranchstockitem objStock = new TbranchstockitemDAO()
															.findByFilter("itemno = '" + tri.getItemno()
																	+ "' and status = '" + tri.getItemstatus() + "'");
													if (objStock != null) {
														objStock.setStatus(tri.getItemstatus());
														new TbranchstockitemDAO().save(session, objStock);

														Tbranchitemtrack objTrack = new Tbranchitemtrack();
														objTrack.setItemno(tri.getItemno());
														objTrack.setTracktime(new Date());
														objTrack.setTrackdesc(
																AppData.getStatusLabel(tri.getItemstatus()));
														objTrack.setProductgroup(
																tri.getTrepair().getMproduct().getProductgroup());
														objTrack.setMproduct(tri.getTrepair().getMproduct());
														objTrack.setTrackstatus(tri.getItemstatus());
														new TbranchitemtrackDAO().save(session, objTrack);
													}
													if (tri.getItemstatus().trim()
															.equals(AppUtils.STATUS_DELIVERY_PAKETDONE)) {
														tri.setItemstatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
														new TrepairitemDAO().save(session, tri);
													}
												}
											}

											transaction.commit();
											session.close();
										}

									} catch (HibernateException e) {
										isError = true;
										if (strError.length() > 0)
											strError += ". \n";
										strError += e.getMessage();
										e.printStackTrace();
									} catch (Exception e) {
										isError = true;
										if (strError.length() > 0)
											strError += ". \n";
										strError += e.getMessage();
										e.printStackTrace();
									} finally {
										if (session.isOpen())
											session.close();
									}

									if (isError) {
										objForm.setTdeliverypk(null);
										Messagebox.show(strError, WebApps.getCurrent().getAppName(), Messagebox.OK,
												Messagebox.ERROR);
									} else {
										if (objList.size() == objSelected.size()) {
											Clients.showNotification("Submit data delivery berhasil", "info", null,
													"middle_center", 2000);
											isSaved = new Boolean(true);
											doClose();
										} else {
											Messagebox.show(
													"Pembuatan manifest delivery berhasil. No Manifest : "
															+ objForm.getDlvid(),
													WebApps.getCurrent().getAppName(), Messagebox.OK,
													Messagebox.INFORMATION);
											doReset();
											BindUtils.postNotifyChange(null, null, DeliveryManifestCreateVm.this, "*");
										}
									}
								}
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Tidak ada data", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winDelivery, isSaved);
		Events.postEvent(closeEvent);
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
					String beratitem = (String) ctx.getProperties("beratitem")[0].getValue();
					if (beratitem == null || "".equals(beratitem))
						this.addInvalidMessage(ctx, "beratitem", Labels.getLabel("common.validator.empty"));
				}

				Mcouriervendor mcouriervendor = (Mcouriervendor) ctx.getProperties("mcouriervendor")[0].getValue();
				if (mcouriervendor == null)
					this.addInvalidMessage(ctx, "mcouriervendor", Labels.getLabel("common.validator.empty"));

				if (!productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
					if (type.equals("A")) {
						if (mletter == null)
							this.addInvalidMessage(ctx, "mletter", Labels.getLabel("common.validator.empty"));
					} else if (type.equals("M")) {
						if (letterno == null || letterno.trim().length() == 0)
							this.addInvalidMessage(ctx, "letterno", Labels.getLabel("common.validator.empty"));
					}

					if (mlettertype == null)
						this.addInvalidMessage(ctx, "mlettertype", Labels.getLabel("common.validator.empty"));
				}
			}
		};
	}

	public ListModelList<Mletter> getMletterModel() {
		ListModelList<Mletter> lm = null;
		try {
			if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
				lm = new ListModelList<Mletter>(
						AppData.getMletter("productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'"));
			else
				lm = new ListModelList<Mletter>(AppData.getMletter("productgroup = '" + productgroup + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mlettertype> getMlettertypeModel() {
		ListModelList<Mlettertype> lm = null;
		try {
			if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
				lm = new ListModelList<Mlettertype>(
						AppData.getMlettertype("productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'"));
			else
				lm = new ListModelList<Mlettertype>(AppData.getMlettertype("productgroup = '" + productgroup + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mcouriervendor> getMcouriervendor() {
		ListModelList<Mcouriervendor> lm = null;
		try {
			lm = new ListModelList<Mcouriervendor>(AppData.getMcouriervendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Tdelivery getObjForm() {
		return objForm;
	}

	public void setObjForm(Tdelivery objForm) {
		this.objForm = objForm;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	public int getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(int totalrecord) {
		this.totalrecord = totalrecord;
	}

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
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

	public ListModelList<Mproduct> getMproductmodel() {
		return mproductmodel;
	}

	public void setMproductmodel(ListModelList<Mproduct> mproductmodel) {
		this.mproductmodel = mproductmodel;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

	public Mletter getMletter() {
		return mletter;
	}

	public void setMletter(Mletter mletter) {
		this.mletter = mletter;
	}

	public Date getOrderdate2() {
		return orderdate2;
	}

	public void setOrderdate2(Date orderdate2) {
		this.orderdate2 = orderdate2;
	}

	public String getProducttype() {
		return producttype;
	}

	public String getProductcode() {
		return productcode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLetterno() {
		return letterno;
	}

	public void setLetterno(String letterno) {
		this.letterno = letterno;
	}

	public String getProductname() {
		return productname;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public Mlettertype getMlettertype() {
		return mlettertype;
	}

	public void setMlettertype(Mlettertype mlettertype) {
		this.mlettertype = mlettertype;
	}

	public Date getProdfinishdate() {
		return prodfinishdate;
	}

	public void setProdfinishdate(Date prodfinishdate) {
		this.prodfinishdate = prodfinishdate;
	}

	public Boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(Boolean isSaved) {
		this.isSaved = isSaved;
	}

	public String getBeratitem() {
		return beratitem;
	}

	public void setBeratitem(String beratitem) {
		this.beratitem = beratitem;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

}
