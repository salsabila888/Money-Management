package com.sdd.caption.viewmodel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
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
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TmissingbranchDAO;
import com.sdd.caption.dao.TmissingproductDAO;
import com.sdd.caption.dao.TnotifDAO;
import com.sdd.caption.dao.TpinmailerfileDAO;
import com.sdd.caption.dao.TpinmailerproductDAO;
import com.sdd.caption.dao.TpinmailerbranchDAO;
import com.sdd.caption.dao.TpinmailerdataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tmissingbranch;
import com.sdd.caption.domain.Tmissingproduct;
import com.sdd.caption.domain.Tnotif;
import com.sdd.caption.domain.Tpinmailerbranch;
import com.sdd.caption.domain.Tpinmailerdata;
import com.sdd.caption.domain.Tpinmailerfile;
import com.sdd.caption.domain.Tpinmailerproduct;
import com.sdd.caption.domain.Vmissingbranch;
import com.sdd.caption.domain.Vmissingproduct;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PinmailerUploadOldVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private Mmenu menu;

	private Session session;
	private Transaction transaction;

	private Tpinmailerfile objForm;

	private TpinmailerfileDAO oDao = new TpinmailerfileDAO();
	private TpinmailerproductDAO productDao = new TpinmailerproductDAO();
	private TpinmailerbranchDAO branchDao = new TpinmailerbranchDAO();
	private TpinmailerdataDAO dataDao = new TpinmailerdataDAO();
	private TmissingproductDAO tmpDao = new TmissingproductDAO();
	private TmissingbranchDAO tmbDao = new TmissingbranchDAO();

	private DateFormat datedbFormatter = new SimpleDateFormat("yyMMdd");

	private Integer totaldata;
	private Integer totalinserted;
	private Integer totalfailed;
	private int totalmissingproduct;
	private int totalmissingbranch;
	private String title;
	private String filename;
	private Media media;
	private Date date;

	private Map<String, Mbranch> mapBranch = new HashMap<String, Mbranch>();
	private Map<String, Mproduct> mapProduct = new HashMap<String, Mproduct>();
	private Map<String, Morg> mapOrg = new HashMap<String, Morg>();

	private Map<String, Tmissingproduct> mapMissingProduct;
	private Map<String, Tmissingbranch> mapMissingBranch;

	private List<Tmissingproduct> missingproductList;
	private List<Tmissingbranch> missingbranchList;
	private List<Vmissingbranch> vmissingBranchList;
	private List<Vmissingproduct> vmissingProductList;

	private Map<String, Tpinmailerbranch> mapTbranch;
	private Map<String, Tpinmailerproduct> mapTproduct;
	private Map<String, List<Tpinmailerdata>> mapTpinData;
	private Map<String, List<Tpinmailerdata>> mapTdata;
	private Map<String, List<Tpinmailerproduct>> mapListproduct;
	private List<Tpinmailerdata> listTod;
	private List<Tpinmailerproduct> listTproduct;

	@Wire
	private Button btnBrowse;
	@Wire
	private Button btnSave;
	@Wire
	private Label fileBrowse;
	@Wire
	private Groupbox gbResult;
	@Wire
	private Combobox cbProduct;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();
		try {
			mapBranch = new HashMap<String, Mbranch>();
			for (Mbranch obj : AppData.getMbranch()) {
				mapBranch.put(obj.getBranchid(), obj);
			}
			mapProduct = new HashMap<String, Mproduct>();
			for (Mproduct obj : AppData.getMproduct("productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'")) {
				mapProduct.put(obj.getProductcode() + obj.getIsinstant(), obj);
			}
			mapOrg = new HashMap<String, Morg>();
			for (Morg obj : AppData.getMorg()) {
				mapOrg.put(obj.getOrg(), obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("filename")
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			fileBrowse.setVisible(true);
			filename = media.getName();
			if (media != null) {
				Tpinmailerfile obj = oDao.findByFilter("filename = '" + media.getName().toUpperCase() + "'");
				if (obj == null) {
					btnSave.setDisabled(false);
				} else {
					Messagebox.show("File sudah pernah diupload", WebApps.getCurrent().getAppName(), Messagebox.OK,
							Messagebox.INFORMATION);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (media != null) {
			String error = "";
			Session session = StoreHibernateUtil.openSession();
			Transaction transaction = null;
			boolean isValid = false;
			try {
				transaction = session.beginTransaction();
				objForm.setTpinmailerfilepk(null);
				objForm.setBatchid(new TcounterengineDAO().generateCounter(AppUtils.CE_PINMAILER));
				objForm.setFilename(media.getName());
				objForm.setUploadedby(oUser.getUserid());
				objForm.setStatus(AppUtils.STATUS_ORDER);

				oDao.save(session, objForm);

				menu = new MmenuDAO()
						.findByFilter("menupath = '/view/pinmailer/pinmailerlist.zul' and menuparamvalue = '06'");
				NotifHandler.doNotif(session, menu, "File PIN MAILER baru");
				transaction.commit();
				isValid = true;
			} catch (Exception e) {
				transaction.rollback();
				isValid = false;
				e.printStackTrace();
				error = e.getMessage();
			} finally {
				session.close();
			}

			if (isValid) {
				BufferedReader reader = null;
				try {
					if (media.isBinary()) {
						reader = new BufferedReader(new InputStreamReader(media.getStreamData()));
					} else {
						reader = new BufferedReader(media.getReaderData());
					}

					String line = "";
					String cardno = "";
					int linecontent = 0;
					int totaldatabranch = 0;
					String branchid = "";
					boolean isContent = false;
					Tpinmailerbranch tbranch = null;
					mapTbranch = new HashMap<>();
					mapTproduct = new HashMap<>();
					mapTdata = new HashMap<>();
					mapListproduct = new HashMap<>();
					while ((line = reader.readLine()) != null) {
						try {
							if (line.trim().equals("CARD  PRODUCTION  CONTROL  REPORT")) {
								isContent = true;
								linecontent = 1;
							}

							if (isContent) {
								if (linecontent == 3 && !line.substring(5, 8).equals(branchid)) {
									if (tbranch != null) {
										Tpinmailerbranch objtmp = mapTbranch.get(branchid);
										if (objtmp != null) {
											totaldatabranch += objtmp.getTotaldata();
										}
										tbranch.setTotaldata(totaldatabranch);

										mapTbranch.put(branchid, tbranch);

										List<Tpinmailerdata> listtmp = mapTdata.get(branchid);
										if (listtmp != null) {
											listTod.addAll(listtmp);
										}

										mapTdata.put(branchid, listTod);

										List<Tpinmailerproduct> listProduct = mapListproduct.get(branchid);
										if (listProduct != null) {
											listTproduct.addAll(listProduct);
										}
										mapListproduct.put(branchid, listTproduct);

									}

									branchid = line.substring(5, 8);

									tbranch = new Tpinmailerbranch();
									tbranch.setTpinmailerfile(objForm);
									tbranch.setBranchid(branchid);
									tbranch.setStatus(AppUtils.STATUS_ORDER_WAITAPPROVAL);
									Mbranch mbranch = mapBranch.get(branchid);
									if (mbranch != null) {
										tbranch.setMbranch(mbranch);
									}
									totaldatabranch = 0;

									listTproduct = new ArrayList<>();
									listTod = new ArrayList<>();
								}
								if (linecontent > 7) {
									cardno = line.substring(9, 28).trim();
									if (cardno.length() >= 16 && !cardno.contains("*")) {
										if (line.substring(34, 35).equals("Y")) {
											Tpinmailerdata data = new Tpinmailerdata();
											data.setCardno(cardno);
											data.setName(line.substring(41, 74));
											data.setOrderdate(datedbFormatter.parse(line.substring(111, 117)));
											data.setProductcode(line.substring(127).trim());
											data.setBranchid(branchid);
											data.setKlncode(line.substring(29, 31));
											data.setSeqno(line.substring(0, 6));
											data.setIswithcard(line.substring(37, 38));

											if (data.getName().trim().equals(""))
												data.setIsinstant("Y");
											else
												data.setIsinstant("N");

											if (data.getProductcode() != null) {
												Mproduct mproduct = mapProduct
														.get(data.getProductcode() + data.getIsinstant());
												if (mproduct != null) {
													data.setMproduct(mproduct);
												}
											}

											Tpinmailerproduct tpmp = mapTproduct.get(
													data.getBranchid() + data.getProductcode() + data.getIsinstant());
											if (tpmp == null) {
												tpmp = new Tpinmailerproduct();
												tpmp.setOrderdate(data.getOrderdate());
												tpmp.setEntrytime(new Date());
												tpmp.setIsinstant(data.getIsinstant());
												tpmp.setMproduct(data.getMproduct());
												tpmp.setProductcode(data.getProductcode());
												tpmp.setStatus(AppUtils.STATUS_ORDER_WAITAPPROVAL);
												tpmp.setTotaldata(1);

												listTproduct.add(tpmp);

												if (data.getMproduct() != null) {
													Morg morg = mapOrg
															.get(data.getMproduct().getMproducttype().getProductorg());
													if (morg != null) {
														tpmp.setOrg(morg.getOrg());
													}
												}

												if (tpmp.getMproduct() == null) {
													Tmissingproduct missingProduct = mapMissingProduct
															.get(data.getProductcode() + data.getIsinstant());
													if (missingProduct == null) {
														missingProduct = new Tmissingproduct();
														missingProduct.setIsinstant(tpmp.getIsinstant());
														missingProduct.setProductcode(tpmp.getProductcode());
														missingProduct.setTpinmailerproduct(tpmp);
														missingProduct.setEntrytime(new Date());
														missingProduct.setOrderdate(data.getOrderdate());
														missingProduct.setTotaldata(1);

														totalmissingproduct++;
														missingproductList.add(missingProduct);
													} else {
														missingProduct.setTotaldata(missingProduct.getTotaldata() + 1);
													}
													mapMissingProduct.put(data.getProductcode() + data.getIsinstant(),
															missingProduct);
												}

												tpmp.setTpinmailerfile(objForm);
												date = tpmp.getOrderdate();
											} else {
												tpmp.setTotaldata(tpmp.getTotaldata() + 1);
											}
											mapTproduct.put(
													data.getBranchid() + data.getProductcode() + data.getIsinstant(),
													tpmp);

											data.setTpinmailerproduct(tpmp);
											listTod.add(data);

											totaldata++;
											totaldatabranch++;
										}
									} else {
										isContent = false;
									}
								}
								linecontent++;
							}

						} catch (Exception e) {
							e.printStackTrace();
							if (error.length() > 0)
								error += ". \n";
							error += e.getMessage();
							System.out.println(line);
						}
					}

					if (tbranch != null) {
						Tpinmailerbranch objtmp = mapTbranch.get(branchid);
						if (objtmp != null) {
							totaldatabranch += objtmp.getTotaldata();
						}
						tbranch.setTotaldata(totaldatabranch);
						mapTbranch.put(branchid, tbranch);

						List<Tpinmailerdata> listtmp = mapTdata.get(branchid);
						if (listtmp != null && listtmp.size() > 0) {
							listTod.addAll(listtmp);
						}
						mapTdata.put(branchid, listTod);

						List<Tpinmailerproduct> listProduct = mapListproduct.get(branchid);
						if (listProduct != null) {
							listTproduct.addAll(listProduct);
						}
						mapListproduct.put(branchid, listTproduct);
					}

					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					try {
						/*
						 * for (Entry<String, Tpinmailerbranch> entry : mapTbranch.entrySet()) { if
						 * (entry.getValue().getTotaldata() > 0) { Tpinmailerbranch pinBranch =
						 * entry.getValue(); if (pinBranch.getMbranch() == null) { List<Tpinmailerdata>
						 * listTdata = mapTdata.get(entry.getValue().getBranchid()); for (Tpinmailerdata
						 * data : listTdata) { if (data.getCardno().length() == 19) {
						 * System.out.println(data.getCardno().substring(10, 12)); Mbranch mbranch =
						 * mapBranch.get(data.getCardno().substring(10, 12)); if (mbranch != null) {
						 * 
						 * } } } } } }
						 */

						for (Entry<String, Tpinmailerbranch> entry : mapTbranch.entrySet()) {
							if (entry.getValue().getTotaldata() > 0) {
								entry.getValue().setOrderdate(date);
								branchDao.save(session, entry.getValue());
								List<Tpinmailerproduct> listTproduct = mapListproduct
										.get(entry.getValue().getBranchid());
								for (Tpinmailerproduct product : listTproduct) {
									product.setTpinmailerbranch(entry.getValue());
									product.setTotalproses(0);
									product.setOrderos(product.getTotaldata());
									productDao.save(session, product);
								}

								List<Tpinmailerdata> listTdata = mapTdata.get(entry.getValue().getBranchid());
								for (Tpinmailerdata data : listTdata) {
									data.setTpinmailerbranch(entry.getValue());
									dataDao.save(session, data);
									totalinserted++;
								}

								if (entry.getValue().getMbranch() == null) {
									listTdata = mapTdata.get(entry.getValue().getBranchid());
									for (Tpinmailerdata data : listTdata) {
										System.out.println("NO KARTU MISSING BRANCH : " + data.getCardno());
										if (data.getCardno().length() > 16) {
											Mbranch oBranch = mapBranch.get(data.getCardno().substring(10, 13));
											System.out
													.println("KODE CABANGNYA : " + data.getCardno().substring(10, 13));
											if (oBranch != null) {
												data.setTpinmailerbranch(entry.getValue());
												List<Tpinmailerdata> dataList = mapTpinData.get(oBranch.getBranchid());
												if(dataList == null) {
													dataList = new ArrayList<>();
												}
												dataList.add(data);
												mapTpinData.put(oBranch.getBranchid(), listTdata);
												System.out.println("CABANGNYA ADALAH : " + oBranch.getBranchname());
											}
										}
									}
								}

								/*
								 * if (entry.getValue().getMbranch() == null) { Tmissingbranch missingBranch =
								 * mapMissingBranch.get(entry.getValue().getBranchid()); if (missingBranch ==
								 * null) { missingBranch = new Tmissingbranch();
								 * missingBranch.setBranchid(entry.getValue().getBranchid());
								 * missingBranch.setEntrytime(new Date());
								 * missingBranch.setOrderdate(entry.getValue().getOrderdate());
								 * missingBranch.setTpinmailerbranch(entry.getValue());
								 * missingBranch.setTotaldata(1);
								 * 
								 * tmbDao.save(session, missingBranch);
								 * 
								 * Vmissingbranch vmissbranch = new Vmissingbranch();
								 * vmissbranch.setBranchid(missingBranch.getBranchid());
								 * vmissbranch.setTotaldata(missingBranch.getTotaldata());
								 * vmissingBranchList.add(vmissbranch);
								 * 
								 * totalmissingbranch++; missingbranchList.add(missingBranch); } else {
								 * missingBranch.setTotaldata(missingBranch.getTotaldata() + 1); }
								 * mapMissingBranch.put(entry.getValue().getBranchid(), missingBranch); }
								 */
							}
						}

						for (Entry<String, Tmissingproduct> entry : mapMissingProduct.entrySet()) {
							Tmissingproduct tmissingproduct = entry.getValue();
							tmpDao.save(session, tmissingproduct);
							Vmissingproduct vmissproduct = new Vmissingproduct();
							vmissproduct.setProductcode(tmissingproduct.getProductcode());
							vmissproduct.setIsinstant(tmissingproduct.getIsinstant());
							vmissproduct.setTotaldata(tmissingproduct.getTotaldata());
							vmissingProductList.add(vmissproduct);
						}

						objForm.setTotaldata(totalinserted);
						oDao.save(session, objForm);

						transaction.commit();
						
						transaction = session.beginTransaction();
						for(Entry<String, List<Tpinmailerdata>> entry : mapTpinData.entrySet()) {
							Tpinmailerbranch pinBranch = mapTbranch.get(entry.getKey());
							if(pinBranch != null) {
								
							} else {
								pinBranch = new Tpinmailerbranch();
								pinBranch.setBranchid(entry.getKey());
								pinBranch.setMbranch(mapBranch.get(entry.getKey()));
								pinBranch.setOrderdate(entry.getValue().get(0).getOrderdate());
							}
						}
						
					} catch (Exception e) {
						e.printStackTrace();
						if (error.length() > 0)
							error += ". \n";
						error += e.getMessage();
					} finally {
						session.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (error.length() > 0)
						error += ". \n";
					error += e.getMessage();
				}
			}

			if (error.length() > 0)
				Messagebox.show(error, WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
			else
				Messagebox.show("Proses upload file order pin mailer selesai", WebApps.getCurrent().getAppName(),
						Messagebox.OK, Messagebox.INFORMATION);

			totalfailed = totaldata - totalinserted;
			gbResult.setVisible(true);
			btnSave.setDisabled(true);
			btnBrowse.setDisabled(true);
		} else {
			Messagebox.show("Silahkan upload file order pin mailer", WebApps.getCurrent().getAppName(), Messagebox.OK,
					Messagebox.INFORMATION);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("objForm")
	public void doDelete() {
		try {
			Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								try {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									oDao.delete(session, objForm);
									transaction.commit();
									session.close();

									List<Tnotif> notifList = new TnotifDAO()
											.listByFilter("mmenufk = " + menu.getMmenupk(), "tnotifpk");
									if (notifList.size() > 0) {
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										new TnotifDAO().delete(session, notifList.get(0));
										transaction.commit();
										Mmenu menu = new MmenuDAO()
												.findByFilter("menupath = '/view/pinmailer/pinmailerupload.zul'");
										Sessions.getCurrent().setAttribute("menu", menu);
										Executions.sendRedirect("/view/index.zul");
										session.close();
									}

									Clients.showNotification(Labels.getLabel("common.delete.success"), "info", null,
											"middle_center", 3000);

									doReset();
									BindUtils.postNotifyChange(null, null, PinmailerUploadOldVm.this, "objForm");
								} catch (HibernateException e) {
									transaction.rollback();
									Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								} catch (Exception e) {
									Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								}
							}
						}

					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doView(@BindingParam("arg") String arg) {
		if (arg.equals("missingbranch")) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objList", vmissingBranchList);
			Window win = (Window) Executions.createComponents("/view/emboss/missingbranch.zul", null, map);
			win.setWidth("60%");
			win.setClosable(true);
			win.doModal();
		} else if (arg.equals("missingproduct")) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objList", vmissingProductList);
			Window win = (Window) Executions.createComponents("/view/emboss/missingproduct.zul", null, map);
			win.setWidth("60%");
			win.setClosable(true);
			win.doModal();
		} else if (arg.equals("alldata")) {
			String path = "/view/pinmailer/pinmailerbranch.zul";
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("obj", objForm);
			Window win = (Window) Executions.createComponents(path, null, map);
			win.setWidth("90%");
			win.setClosable(true);
			win.doModal();
		}

	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		objForm = new Tpinmailerfile();
		objForm.setUploadtime(new Date());
		totaldata = 0;
		totalinserted = 0;
		totalmissingproduct = 0;
		totalmissingbranch = 0;
		totalfailed = 0;
		gbResult.setVisible(false);
		btnBrowse.setDisabled(false);
		btnSave.setDisabled(true);
		fileBrowse.setVisible(false);
		cbProduct.setValue(null);
		filename = null;
		media = null;

		mapMissingProduct = new HashMap<String, Tmissingproduct>();
		mapMissingBranch = new HashMap<String, Tmissingbranch>();
		listTod = new ArrayList<>();
		missingproductList = new ArrayList<>();
		missingbranchList = new ArrayList<>();

		vmissingBranchList = new ArrayList<>();
		vmissingProductList = new ArrayList<>();
	}

	public ListModelList<Mproduct> getMproduct() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(
					AppData.getMproduct("productgroup = '" + AppUtils.PRODUCTGROUP_PINMAILER + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Tpinmailerfile getObjForm() {
		return objForm;
	}

	public void setObjForm(Tpinmailerfile objForm) {
		this.objForm = objForm;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Integer getTotalinserted() {
		return totalinserted;
	}

	public void setTotalinserted(Integer totalinserted) {
		this.totalinserted = totalinserted;
	}

	public Integer getTotalfailed() {
		return totalfailed;
	}

	public void setTotalfailed(Integer totalfailed) {
		this.totalfailed = totalfailed;
	}

	public int getTotalmissingproduct() {
		return totalmissingproduct;
	}

	public void setTotalmissingproduct(int totalmissingproduct) {
		this.totalmissingproduct = totalmissingproduct;
	}

	public int getTotalmissingbranch() {
		return totalmissingbranch;
	}

	public void setTotalmissingbranch(int totalmissingbranch) {
		this.totalmissingbranch = totalmissingbranch;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
