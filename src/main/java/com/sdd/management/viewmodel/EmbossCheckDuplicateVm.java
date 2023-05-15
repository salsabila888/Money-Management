package com.sdd.caption.viewmodel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import org.zkoss.io.Files;
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
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.dao.TmissingbranchDAO;
import com.sdd.caption.dao.TmissingproductDAO;
import com.sdd.caption.dao.TembossfileDAO;
import com.sdd.caption.dao.TproductmmDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Tmissingbranch;
import com.sdd.caption.domain.Tmissingproduct;
import com.sdd.caption.domain.Tembossfile;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.domain.Tproductmm;
import com.sdd.caption.domain.Vmissingbranch;
import com.sdd.caption.domain.Vmissingproduct;
import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class EmbossCheckDuplicateVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Tembossfile objForm;

	private TembossfileDAO oDao = new TembossfileDAO();
	private TembossbranchDAO branchDao = new TembossbranchDAO();
	private TembossproductDAO productDao = new TembossproductDAO();
	private TembossdataDAO todDao = new TembossdataDAO();
	private TmissingproductDAO tmpDao = new TmissingproductDAO();
	private TmissingbranchDAO tmbDao = new TmissingbranchDAO();
	private TproductmmDAO tproductmmDao = new TproductmmDAO();

	private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private DateFormat datedbFormatter = new SimpleDateFormat("yyMMdd");

	private int totaldata;
	private int totalinserted;
	private int totalmissingproduct;
	private int totalmissingbranch;
	private int totalfailed;
	private int duplicate;
	private String filename;
	private Media media;

	private Map<String, Mbranch> mapBranch = new HashMap<String, Mbranch>();
	private Map<String, Mproduct> mapProduct = new HashMap<String, Mproduct>();
	private Map<String, Morg> mapOrg = new HashMap<String, Morg>();

	private Map<String, Tembossproduct> mapEmbossProduct;
	private Map<String, Tembossbranch> mapEmbossBranch;
	private Map<String, Tmissingproduct> mapMissingProduct;
	private Map<String, Tmissingbranch> mapMissingBranch;
	private List<Tembossdata> listTod;
	private List<Tmissingproduct> missingproductList;
	private List<Tmissingbranch> missingbranchList;
	private List<Vmissingbranch> vmissingBranchList;
	private List<Vmissingproduct> vmissingProductList;

	@Wire
	private Button btnBrowse;
	@Wire
	private Button btnSave;
	@Wire
	private Label fileBrowse;
	@Wire
	private Groupbox gbResult;

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
				Tembossfile obj = oDao.findByFilter("filename = '" + media.getName().toUpperCase() + "'");
				if (obj == null) {
					btnSave.setDisabled(false);
				} else {
					Messagebox.show("File sudah pernah diupload", "Info", Messagebox.OK, Messagebox.INFORMATION);
					btnSave.setDisabled(true);
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
			Session session = null;
			Transaction transaction = null;
			boolean isValid = false;
			int total = 0;
			Map<String, Integer> mapProductmm = new HashMap<>();
			String path = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_EMBOSSFILE);
			System.out.println(path);

			try {
				total = 0;
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				objForm.setTembossfilepk(null);
				objForm.setEmbossid(new TcounterengineDAO().generateCounter(AppUtils.CE_EMBOSS));
				objForm.setFilename(media.getName());
				objForm.setUploadedby(oUser.getUserid());

				oDao.save(session, objForm);
				transaction.commit();
				isValid = true;
			} catch (Exception e) {
				transaction.rollback();
				isValid = false;
				e.printStackTrace();
				Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
			} finally {
				session.close();
			}

			if (isValid) {
				BufferedReader reader = null;
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				try {
					if (media.isBinary()) {
						reader = new BufferedReader(new InputStreamReader(media.getStreamData()));
					} else {
						reader = new BufferedReader(media.getReaderData());
					}

					String line = "";
					String orderdate = "";
					while ((line = reader.readLine()) != null) {
						try {
							totaldata++;
							System.out.println(line.substring(1, 20));

							List<Tembossdata> embossdataList = todDao
									.listByFilter("cardno = '" + line.substring(1, 20) + "'", "cardno");
							if (embossdataList == null || embossdataList.size() == 0) {
								Tembossdata embossdata = new Tembossdata();
								embossdata.setTembossfile(objForm);
								embossdata.setCardno(line.substring(1, 20));
								embossdata.setNameoncard(line.substring(52, 82));
								embossdata.setNameonid(line.substring(472, 502));
								embossdata.setOrderdate(datedbFormatter.parse(line.substring(652, 658)));
								if (orderdate.equals(""))
									orderdate = dateFormatter.format(embossdata.getOrderdate());
								embossdata.setProductcode(line.substring(670, 674));
								embossdata.setBranchid(line.substring(701, 704));
								embossdata.setKlncode(line.substring(705, 707));
								embossdata.setSeqno(line.substring(755, 761));

								if (embossdata.getNameoncard().trim().equals(""))
									embossdata.setIsinstant("Y");
								else
									embossdata.setIsinstant("N");

								if (embossdata.getBranchid() != null) {
									Mbranch mbranch = mapBranch.get(embossdata.getBranchid().trim());
									if (mbranch != null) {
										embossdata.setBranchname(mbranch.getBranchname());
										embossdata.setMbranch(mbranch);
									}
								}
								if (embossdata.getProductcode() != null) {
									Mproduct mproduct = mapProduct
											.get(embossdata.getProductcode().trim() + embossdata.getIsinstant().trim());
									if (mproduct != null) {
										embossdata.setMproduct(mproduct);
									}
								}

								Tembossproduct objEmbossProduct = mapEmbossProduct
										.get(embossdata.getProductcode() + embossdata.getIsinstant());
								if (objEmbossProduct == null) {
									objEmbossProduct = new Tembossproduct();
									objEmbossProduct.setEntrytime(new Date());
									objEmbossProduct.setIsinstant(embossdata.getIsinstant());
									objEmbossProduct.setMproduct(embossdata.getMproduct());
									objEmbossProduct.setOrderdate(embossdata.getOrderdate());
									objEmbossProduct.setProductcode(embossdata.getProductcode());
									objEmbossProduct.setStatus(AppUtils.STATUS_ORDER);
									objEmbossProduct.setTotaldata(1);

									if (embossdata.getMproduct() != null) {
										Morg morg = mapOrg
												.get(embossdata.getMproduct().getMproducttype().getProductorg());
										if (morg != null) {
											objEmbossProduct.setOrg(morg.getOrg());
											objEmbossProduct.setIsneeddoc(morg.getIsneeddoc());
										}
									}

									objEmbossProduct.setTembossfile(objForm);
								} else {
									objEmbossProduct.setTotaldata(objEmbossProduct.getTotaldata() + 1);
								}
								mapEmbossProduct.put(embossdata.getProductcode() + embossdata.getIsinstant(),
										objEmbossProduct);

								if (objEmbossProduct.getMproduct() == null) {
									Tmissingproduct missingProduct = mapMissingProduct
											.get(embossdata.getProductcode() + embossdata.getIsinstant());
									if (missingProduct == null) {
										missingProduct = new Tmissingproduct();
										missingProduct.setIsinstant(objEmbossProduct.getIsinstant());
										missingProduct.setProductcode(objEmbossProduct.getProductcode());
										missingProduct.setTembossproduct(objEmbossProduct);
										missingProduct.setEntrytime(new Date());
										missingProduct.setOrderdate(embossdata.getOrderdate());
										missingProduct.setTotaldata(1);

										totalmissingproduct++;
										missingproductList.add(missingProduct);
									} else {
										missingProduct.setTotaldata(missingProduct.getTotaldata() + 1);
									}
									mapMissingProduct.put(embossdata.getProductcode() + embossdata.getIsinstant(),
											missingProduct);
								}

								Tembossbranch objEmbossBranch = mapEmbossBranch.get(embossdata.getProductcode()
										+ embossdata.getIsinstant() + embossdata.getBranchid());
								if (objEmbossBranch == null) {
									objEmbossBranch = new Tembossbranch();
									objEmbossBranch.setTembossproduct(objEmbossProduct);
									objEmbossBranch.setEntrytime(new Date());
									objEmbossBranch.setBranchid(embossdata.getBranchid());
									objEmbossBranch.setMbranch(embossdata.getMbranch());
									objEmbossBranch.setMproduct(embossdata.getMproduct());
									objEmbossBranch.setOrderdate(embossdata.getOrderdate());
									objEmbossBranch.setTembossfile(objForm);
									objEmbossBranch.setStatus(AppUtils.STATUSBRANCH_PENDINGPRODUKSI);
									objEmbossBranch.setTotaldata(1);
								} else {
									objEmbossBranch.setTotaldata(objEmbossBranch.getTotaldata() + 1);
								}
								mapEmbossBranch.put(embossdata.getProductcode() + embossdata.getIsinstant()
										+ embossdata.getBranchid(), objEmbossBranch);

								if (objEmbossBranch.getMbranch() == null) {
									Tmissingbranch missingBranch = mapMissingBranch.get(embossdata.getBranchid());
									if (missingBranch == null) {
										missingBranch = new Tmissingbranch();
										missingBranch.setBranchid(embossdata.getBranchid());
										missingBranch.setEntrytime(new Date());
										missingBranch.setOrderdate(embossdata.getOrderdate());
										missingBranch.setTembossbranch(objEmbossBranch);
										missingBranch.setTotaldata(1);

										totalmissingbranch++;
										missingbranchList.add(missingBranch);
									} else {
										missingBranch.setTotaldata(missingBranch.getTotaldata() + 1);
									}
									mapMissingBranch.put(embossdata.getBranchid(), missingBranch);
								}

								embossdata.setTembossbranch(objEmbossBranch);
								embossdata.setTembossproduct(objEmbossProduct);
								listTod.add(embossdata);

								totalinserted++;
								total++;

								if (embossdata.getMproduct() != null
										&& embossdata.getMproduct().getIsmm().equals("Y")) {
									Integer totalmm = mapProductmm.get(embossdata.getProductcode());
									if (totalmm != null) {
										mapProductmm.put(embossdata.getProductcode(), totalmm + 1);
									} else {
										mapProductmm.put(embossdata.getProductcode(), 1);
									}
								}
							} else
								duplicate++;
						} catch (HibernateException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					transaction.commit();

					File folder = new File(path + "/" + orderdate);
					if (!folder.exists())
						folder.mkdir();

					if (media.isBinary()) {
						Files.copy(new File(folder + "/" + media.getName()), media.getStreamData());
					} else {
						BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/" + media.getName()));
						Files.copy(writer, media.getReaderData());
						writer.close();
					}

					if (totalinserted > 0) {
						transaction = session.beginTransaction();

						for (Entry<String, Tembossproduct> entry : mapEmbossProduct.entrySet()) {
							Tembossproduct tembossProduct = entry.getValue();
							tembossProduct.setTotalproses(0);
							tembossProduct.setOrderos(tembossProduct.getTotaldata());
							productDao.save(session, tembossProduct);
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

						for (Entry<String, Tembossbranch> entryBranch : mapEmbossBranch.entrySet()) {
							Tembossbranch tembossBranch = entryBranch.getValue();
							tembossBranch.setTotalproses(0);
							tembossBranch.setTotalos(tembossBranch.getTotaldata());
							tembossBranch.setProdsla(0);
							tembossBranch.setDlvsla(0);
							tembossBranch.setSlatotal(0);
							branchDao.save(session, tembossBranch);

							FlowHandler.doFlow(session, tembossBranch, null, AppUtils.PROSES_ORDER, objForm.getMemo(),
									oUser.getUserid());
						}

						for (Entry<String, Tmissingbranch> entryBranch : mapMissingBranch.entrySet()) {
							Tmissingbranch tmissingbranch = entryBranch.getValue();
							tmbDao.save(session, tmissingbranch);

							Vmissingbranch vmissbranch = new Vmissingbranch();
							vmissbranch.setBranchid(tmissingbranch.getBranchid());
							vmissbranch.setTotaldata(tmissingbranch.getTotaldata());
							vmissingBranchList.add(vmissbranch);
						}

						for (Tembossdata tod : listTod) {
							todDao.save(session, tod);
						}

						for (Entry<String, Integer> entry : mapProductmm.entrySet()) {
							Tproductmm objMm = tproductmmDao.findByFilter(
									"orderdate = '" + orderdate + "' and productcode = '" + entry.getKey() + "'");
							if (objMm == null) {
								objMm = new Tproductmm();
								objMm.setTotaldata(0);
								objMm.setTotalmerge(0);
								objMm.setTotalos(0);
								objMm.setIsmatch("");
							}
							objMm.setTembossfile(objForm);
							objMm.setOrderdate(dateFormatter.parse(orderdate));
							objMm.setProductcode(entry.getKey());
							objMm.setTotaldata(objMm.getTotaldata() + entry.getValue());
							objMm.setTotalos(objMm.getTotaldata() - objMm.getTotalmerge());
							objMm.setEntrytime(new Date());

							Mproduct mproduct = mapProduct.get(objMm.getProductcode().trim() + "N");
							if (mproduct == null)
								mproduct = mapProduct.get(objMm.getProductcode().trim() + "Y");

							if (mproduct != null) {
								objMm.setProductname(mproduct.getProductname());
								objMm.setProducttype(mproduct.getMproducttype().getProducttype());
								objMm.setOrg(mproduct.getMproducttype().getProductorg());
							}

							tproductmmDao.save(session, objMm);
						}

						transaction.commit();
					}

				} catch (HibernateException e) {
					transaction.rollback();
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					session.close();
				}
				totalfailed = totaldata - totalinserted - duplicate;

				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				try {
					objForm.setTotaldata(total);
					oDao.save(session, objForm);
					transaction.commit();
				} catch (Exception e) {
					transaction.rollback();
					e.printStackTrace();
					Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
				} finally {
					session.close();
				}
			}
			Messagebox.show("Proses upload file emboss selesai", WebApps.getCurrent().getAppName(), Messagebox.OK,
					Messagebox.INFORMATION);
			gbResult.setVisible(true);
			btnSave.setDisabled(true);
			btnBrowse.setDisabled(true);
		} else {
			Messagebox.show("Silahkan upload file emboss", WebApps.getCurrent().getAppName(), Messagebox.OK,
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

									Clients.showNotification(Labels.getLabel("common.delete.success"), "info", null,
											"middle_center", 3000);

									doReset();
									BindUtils.postNotifyChange(null, null, EmbossCheckDuplicateVm.this, "objForm");
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
			map.put("isUpload", "Y");
			Window win = (Window) Executions.createComponents("/view/emboss/missingbranch.zul", null, map);
			win.setWidth("60%");
			win.setClosable(true);
			win.doModal();
		} else if (arg.equals("missingproduct")) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("objList", vmissingProductList);
			map.put("isUpload", "Y");
			Window win = (Window) Executions.createComponents("/view/emboss/missingproduct.zul", null, map);
			win.setWidth("60%");
			win.setClosable(true);
			win.doModal();
		} else if (arg.equals("alldata")) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("obj", objForm);
			map.put("isUpload", "Y");
			Window win = (Window) Executions.createComponents("/view/emboss/embossproduct.zul", null, map);
			win.setWidth("90%");
			win.setClosable(true);
			win.doModal();
		}

	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		objForm = new Tembossfile();
		objForm.setUploadtime(new Date());
		totaldata = 0;
		totalinserted = 0;
		totalmissingproduct = 0;
		totalmissingbranch = 0;
		totalfailed = 0;
		duplicate = 0;
		gbResult.setVisible(false);
		btnBrowse.setDisabled(false);
		btnSave.setDisabled(true);
		fileBrowse.setVisible(false);
		filename = null;
		media = null;

		mapEmbossProduct = new HashMap<String, Tembossproduct>();
		mapEmbossBranch = new HashMap<String, Tembossbranch>();
		mapMissingProduct = new HashMap<String, Tmissingproduct>();
		mapMissingBranch = new HashMap<String, Tmissingbranch>();
		listTod = new ArrayList<>();
		missingproductList = new ArrayList<>();
		missingbranchList = new ArrayList<>();

		vmissingBranchList = new ArrayList<>();
		vmissingProductList = new ArrayList<>();
	}

	public Tembossfile getObjForm() {
		return objForm;
	}

	public void setObjForm(Tembossfile objForm) {
		this.objForm = objForm;
	}

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
	}

	public int getTotalinserted() {
		return totalinserted;
	}

	public void setTotalinserted(int totalinserted) {
		this.totalinserted = totalinserted;
	}

	public int getTotalfailed() {
		return totalfailed;
	}

	public void setTotalfailed(int totalfailed) {
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

	public int getDuplicate() {
		return duplicate;
	}

	public void setDuplicate(int duplicate) {
		this.duplicate = duplicate;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
