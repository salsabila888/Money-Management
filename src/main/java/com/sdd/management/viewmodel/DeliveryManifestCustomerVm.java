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
import org.zkoss.zul.Window;

import com.sdd.caption.dao.McourierzipcodeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Mcourierzipcode;
import com.sdd.caption.domain.Mletter;
import com.sdd.caption.domain.Mlettertype;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.handler.BranchStockManager;
import com.sdd.caption.handler.CemtextGenerator;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DeliveryManifestCustomerVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TdeliveryDAO tdeliveryDao = new TdeliveryDAO();
	private TdeliverydataDAO tdeliverydataDao = new TdeliverydataDAO();
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TembossbranchDAO tembossbranchDao = new TembossbranchDAO();
	private ListModelList<Mproduct> mproductmodel = new ListModelList<>();
	private List<Tpaketdata> objList = new ArrayList<Tpaketdata>();
	private List<Tpaketdata> objSelected = new ArrayList<Tpaketdata>();

	private String filter;

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
	private String cardno;
	private Date prodfinishdate;
	private Boolean isSaved;
	private String inserttime;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	private static final String PRODUCTGROUP_CARDPHOTO = "09";

	private Map<String, Mproductgroup> mapProductgroup;

	@Wire
	private Window winDelivery;
	@Wire
	private Combobox cbLetter;
	@Wire
	private Grid grid;
	@Wire
	private Row rowcbLetter;
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
		mapProductgroup = AppData.getMproductgroup();

			productgroupname = AppData.getProductgroupLabel(productgroup);
		if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
			chkAll.setDisabled(true);
		else
			chkAll.setDisabled(false);
		rowcbLetter.setVisible(true);
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
						BindUtils.postNotifyChange(null, null, DeliveryManifestCustomerVm.this, "totaldata");
					}
				});
				if (productgroup.equals(PRODUCTGROUP_CARDPHOTO))
					chkbox.setDisabled(true);
				row.getChildren().add(chkbox);
				row.getChildren().add(new Label(data.getNopaket()));
				row.getChildren().add(new Label(data.getCardno()));
				row.getChildren().add(new Label(data.getCustname()));
				row.getChildren().add(new Label(data.getTpaket().getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getTpaket().getMproduct().getProductname()));
				row.getChildren().add(new Label(datelocalFormatter.format(data.getOrderdate())));

				if (data.getTpaket().getTorder() != null)
					inserttime = datetimelocalFormatter.format(data.getTpaket().getTorder().getInserttime());
				else if (data.getTpaket().getTreturn() != null)
					inserttime = datetimelocalFormatter.format(data.getTpaket().getTreturn().getInserttime());
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
							map.put("obj", data.getTpaket().getTorder());
							win = (Window) Executions.createComponents("/view/order/pinpaddata.zul", null, map);
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
							map.put("obj", data.getTpaket().getTorder());
							win = (Window) Executions.createComponents("/view/order/tokendata.zul", null, map);
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER)) {
							map.put("obj", data.getTpinmailerbranch());
							win = (Window) Executions.createComponents("/view/pinmailer/pinmailerdata.zul", null, map);
						} else if (productgroup.equals(AppUtils.PRODUCTGROUP_CARDPHOTO)) {
							map.put("obj", data.getTpaket().getTderivatifproduct());
							win = (Window) Executions.createComponents("/view/derivatif/derivatifdata.zul", null, map);
						}
						win.setWidth("90%");
						win.setClosable(true);
						win.doModal();
					}
				});
				row.getChildren().add(btndetail);
			}
		});
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
			filter = "mbranch.mbranchpk = " + mbranch.getMbranchpk() + " and isdlv = 'N' and tpaket.productgroup = '"
					+ productgroup + "' and tpaket.tderivatifproductfk is null and branchpool = '"
					+ oUser.getMbranch().getBranchid() + "' and tpaketdata.zipcode is not null";

			if (cardno != null && cardno.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "cardno like '%" + cardno.trim().toUpperCase() + "%'";
			}
			if (orderdate != null) {
				if (orderdate2 != null) {
					if (filter.length() > 0)
						filter += " and ";
					filter += "orderdate between '" + dateFormatter.format(orderdate) + "' and '"
							+ dateFormatter.format(orderdate2) + "'";
				} else {
					if (filter.length() > 0)
						filter += " and ";
					filter += "orderdate = '" + dateFormatter.format(orderdate) + "'";
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

									Map<Integer, Tembossproduct> mapTembossproduct = new HashMap<>();
									try {
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										
										for (Tpaketdata data : objSelected) {
											Mcourierzipcode courier = new McourierzipcodeDAO().findByFilter(data.getZipcode()
													+ " BETWEEN ZIPCODESTART AND ZIPCODEEND ");
											
											objForm.setMbranch(mbranch);
											objForm.setIsproductphoto("N");
											objForm.setProductgroup(productgroup);
											objForm.setMcouriervendor(courier != null ? courier.getMcouriervendor() : null);
											objForm.setDlvid(
													new TcounterengineDAO().generateLetterNo(mletter.getLetterprefix()));

											objForm.setLettertype(mlettertype.getLettertype());
											objForm.setTotaldata(1);
											objForm.setStatus(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER);
											objForm.setProcessedby(oUser.getUserid());
											objForm.setBranchpool(oUser.getMbranch().getBranchid());
											objForm.setIsdlvcust("Y");
											objForm.setAccno(data.getAccno());
											objForm.setAddress1(data.getAddress1());
											objForm.setAddress2(data.getAddress2());
											objForm.setAddress3(data.getAddress3());
											objForm.setCity(data.getCity());
											objForm.setZipcode(data.getZipcode());
											objForm.setHpno(data.getHpno());
											objForm.setCardno(data.getCardno());
											objForm.setCustname(data.getCustname());
											objForm.setTotalamount(new BigDecimal(0));
											tdeliveryDao.save(session, objForm);
											
											Tdeliverydata tdeliverydata = new Tdeliverydata();
											tdeliverydata.setTdelivery(objForm);
											tdeliverydata.setTpaketdata(data);
											tdeliverydata.setMproduct(data.getTpaket().getMproduct());
											tdeliverydata.setOrderdate(data.getOrderdate());
											tdeliverydata.setQuantity(data.getQuantity());
											tdeliverydata.setProductgroup(objForm.getProductgroup());
											tdeliverydataDao.save(session, tdeliverydata);

											data.setIsdlv("Y");
											tpaketdataDao.save(session, data);

											if (data.getTembossbranch() != null) {
												data.getTembossbranch().setStatus(AppUtils.STATUSBRANCH_PROSESDELIVERY);
												tembossbranchDao.save(session, data.getTembossbranch());

												mapTembossproduct.put(data.getTpaket().getTembossproduct().getTembossproductpk(),
														data.getTpaket().getTembossproduct());

											}

										}

										transaction.commit();
										session.close();

										for (Entry<Integer, Tembossproduct> embossProduct : mapTembossproduct.entrySet()) {
											BranchStockManager.manageCard(embossProduct.getValue(), mbranch);
										}

//										Mproductgroup mproductgroup = mapProductgroup.get(objForm.getProductgroup());
//										if (mproductgroup.getIscoa().equals("Y")) {
//											String path = Executions.getCurrent().getDesktop().getWebApp()
//													.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.BOOKFILE_PATH);
//											System.out.println(path);
//											new CemtextGenerator().doGenerate(path, objForm);
//										}
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
											BindUtils.postNotifyChange(null, null, DeliveryManifestCustomerVm.this,
													"*");
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
				if (mlettertype == null)
					this.addInvalidMessage(ctx, "mlettertype", Labels.getLabel("common.validator.empty"));
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

	public void setProducttype(String producttype) {
		this.producttype = producttype;
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

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

}
