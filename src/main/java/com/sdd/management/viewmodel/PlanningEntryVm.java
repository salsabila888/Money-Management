package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
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
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.dao.TplandocDAO;
import com.sdd.caption.dao.TplanproductDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.domain.Tplandoc;
import com.sdd.caption.domain.Tplanproduct;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PlanningEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Tplan objForm;
	private Tplanproduct objData;
	private Tplandoc objDoc;
	private TplanDAO oDao = new TplanDAO();
	private TplanproductDAO dataDao = new TplanproductDAO();

	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private String productgroup;
	private String arg;
	private Integer totaldata;
	private String totalrecord;
	private Date memodate;
	private String unit;
	private String filename;
	private String producttype;
	private String cabang;

	private int pageTotalSize;
	private String filter;
	private String orderby;
	private boolean isEdit = false;
	private Media media;

	private String docfileori, docfileid;
	private Integer docfilesize;
	private Date doctime;

	private List<Tplandoc> docList;
	private List<Tplandoc> docDelList = new ArrayList<Tplandoc>();
	private Map<String, Media> mapMedia = new HashMap<String, Media>();

	private List<Mproducttype> objList = new ArrayList<Mproducttype>();
	private List<Tplanproduct> productList = new ArrayList<Tplanproduct>();
	private Map<Integer, Mproducttype> mapData = new HashMap<Integer, Mproducttype>();
	private Map<Integer, Tplanproduct> mapProduct = new HashMap<Integer, Tplanproduct>();
	private Map<Integer, Integer> mapQty = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mapQtyperunit = new HashMap<Integer, Integer>();

	@Wire
	private Grid grid, gridDoc;
	@Wire
	private Caption caption;
//	@Wire
//	private Window winPlanEntry;
	@Wire
	private Div divRoot;
	@Wire
	private Row rowUpload, rowDoc;
	@Wire
	private Label lblTotal;
	@Wire
	private Column colJumlah, colKonversi;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("isDetail") String isDetail, @ExecutionArgParam("obj") Tplan obj) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		this.arg = arg;
		doReset();

		if (isDetail != null && isDetail.equals("Y")) {
			caption.setVisible(true);

		}

		if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			lblTotal.setValue("Total Konversi(Buku/Set/Lembar)");
			colKonversi.setVisible(true);
			colJumlah.setLabel("Jumlah Buku");
		} else {
			lblTotal.setValue("Total Data");
			colKonversi.setVisible(false);
			colJumlah.setLabel("Jumlah Unit");
		}

		if (obj != null) {
			objForm = obj;
			productList = dataDao.listByFilter("tplanfk = " + obj.getTplanpk(), "tplanfk desc");
			for (Tplanproduct data : productList) {
				mapData.put(data.getMproducttype().getMproducttypepk(), data.getMproducttype());
				mapQty.put(data.getMproducttype().getMproducttypepk(), data.getUnitqty());
				mapQtyperunit.put(data.getMproducttype().getMproducttypepk(), data.getQtyperunit());
				mapProduct.put(data.getMproducttype().getMproducttypepk(), data);
			}

			docList = new TplandocDAO().listByFilter("tplanfk = " + obj.getTplanpk(), "tplanfk");
			gridDoc.setModel(new ListModelList<>(docList));

			totaldata = obj.getTotalqty();
			memodate = obj.getMemodate();
			cabang = obj.getMbranch().getBranchname();

			if (obj.getMemofileori() != null && obj.getMemofileori().trim().length() > 0) {
				filename = obj.getMemofileori();
			}

			isEdit = true;
			caption.setVisible(true);
		}

		grid.setRowRenderer(new RowRenderer<Mproducttype>() {
			@Override
			public void render(Row row, final Mproducttype data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				Intbox intbox = new Intbox();
				intbox.setValue(0);
				intbox.setDisabled(true);
				intbox.setCols(30);
				intbox.setStyle("text-align:right");
				intbox.setFormat("#,###");

				Intbox intboxUnit = new Intbox();
				intbox.setValue(0);
				intboxUnit.setDisabled(true);
				intboxUnit.setCols(30);
				intboxUnit.setStyle("text-align:right");
				intboxUnit.setFormat("#,###");

				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Mproducttype obj = (Mproducttype) checked.getAttribute("obj");
						if (checked.isChecked()) {
							intbox.setDisabled(false);
							intbox.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
								@Override
								public void onEvent(Event event) throws Exception {
									Intbox intBox = (Intbox) event.getTarget();
									if(intBox.getValue() == null)
										intbox.setValue(0);
									if (mapQty.get(data.getMproducttypepk()) != null) {
										totaldata -= mapQtyperunit.get(data.getMproducttypepk());
										mapQty.remove(data.getMproducttypepk());
										mapQtyperunit.remove(data.getMproducttypepk());
										mapData.remove(data.getMproducttypepk());
									}

									mapData.put(data.getMproducttypepk(), obj);
									mapQty.put(data.getMproducttypepk(), intBox.getValue());
									mapQtyperunit.put(data.getMproducttypepk(),
											intBox.getValue() * data.getProductunitqty());
									totaldata += mapQtyperunit.get(data.getMproducttypepk());

									intboxUnit.setValue(intBox.getValue() * data.getProductunitqty());
									BindUtils.postNotifyChange(null, null, PlanningEntryVm.this, "totaldata");
//									}
								}
							});
						} else {
							if (mapQty.get(data.getMproducttypepk()) != null) {
								totaldata -= mapQtyperunit.get(data.getMproducttypepk());
								mapQty.remove(data.getMproducttypepk());
								mapQtyperunit.remove(data.getMproducttypepk());
							}
							mapData.remove(data.getMproducttypepk());
							intbox.setValue(0);
							intbox.setDisabled(true);
							intboxUnit.setValue(0);
							BindUtils.postNotifyChange(null, null, PlanningEntryVm.this, "totaldata");
						}
					}
				});

				if (mapData.get(data.getMproducttypepk()) != null) {
					intbox.setValue(mapQty.get(data.getMproducttypepk()));
					intboxUnit.setValue(mapQtyperunit.get(data.getMproducttypepk()));
				}

				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getProducttype()));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getProductunitqty())));
				row.getChildren().add(intbox);
				row.getChildren().add(intboxUnit);
			}
		});

	}

	@Command
	@NotifyChange("docfileori")
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			if (media.getName().length() < 200) {
				Tplandoc doc = new Tplandoc();
				doc.setDocfileori(media.getName());
				doc.setDocfileid(
						"PLANDOC" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + "." + media.getFormat());
				doc.setDocfilesize(docfilesize);
				doc.setDoctime(new Date());
				docList.add(doc);
				mapMedia.put(doc.getDocfileid(), media);
				gridDoc.setModel(new ListModelList<>(docList));
			} else {
				Messagebox.show("Nama file tidak boleh melebihi 200 karakter.", "Informasi", Messagebox.OK,
						Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doRenderDoc() {
		if (gridDoc != null) {
			gridDoc.setRowRenderer(new RowRenderer<Tplandoc>() {
				public void render(Row row, Tplandoc data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getDocfileori() != null ? data.getDocfileori() : ""));
					Button btnDel = new Button("Delete");
					btnDel.setAutodisable("self");
					btnDel.setClass("btn-danger");
					btnDel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							if (data.getTplandocpk() != null) {
								docDelList.add(data);
							}
							mapMedia.remove(data.getDocfileid());
							docList.remove(data);
							gridDoc.setModel(new ListModelList<>(docList));
						}
					});
					row.getChildren().add(btnDel);
				}
			});
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			if (mapData.size() > 0) {
				String prefix = "";
				if (arg.equals("01"))
					prefix = "PC";
				else if (arg.equals("02"))
					prefix = "PT";
				else if (arg.equals("03"))
					prefix = "PP";
				else if (arg.equals("04"))
					prefix = "PD";

				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				if (!isEdit) {
					objForm.setPlanno(new TcounterengineDAO().generateCounter(prefix));
					objForm.setProductgroup(arg);
				}
				objForm.setMbranch(oUser.getMbranch());
				objForm.setMemodate(memodate);
				objForm.setTotalqty(totaldata);
				objForm.setTotalprocess(0);
				if (arg.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
					objForm.setStatus(AppUtils.STATUS_PLANNING_WAITAPPROVALOPR);
				} else {
					objForm.setStatus(AppUtils.STATUS_PLANNING_WAITAPPROVAL);
				}
				oDao.save(session, objForm);

				for (Entry<Integer, Mproducttype> entry : mapData.entrySet()) {
					Mproducttype mproducttype = entry.getValue();
					objData = mapProduct.get(mproducttype.getMproducttypepk());
					if (objData == null) {
						objData = new Tplanproduct();
						objData.setTplan(objForm);
						objData.setTotalprocess(0);
						objData.setMproducttype(mproducttype);
					}
					objData.setUnitqty(mapQty.get(mproducttype.getMproducttypepk()));
					objData.setQtyperunit(mapQtyperunit.get(mproducttype.getMproducttypepk()));
					dataDao.save(session, objData);
				}

				String path = Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.MEMO_PATH);
				for (Tplandoc plandoc : docList) {
					plandoc.setTplan(objForm);
					new TplandocDAO().save(session, plandoc);

					Media mediaDoc = mapMedia.get(plandoc.getDocfileid());
					if (mediaDoc != null) {
						if (mediaDoc.isBinary()) {
							Files.copy(new File(path + plandoc.getDocfileid()), mediaDoc.getStreamData());
						} else {
							BufferedWriter writer = new BufferedWriter(new FileWriter(path + plandoc.getDocfileid()));
							Files.copy(writer, mediaDoc.getReaderData());
							writer.close();
						}
					}
				}

				for (Tplandoc plandoc : docDelList) {
					new TplandocDAO().delete(session, plandoc);

					File file = new File(path + plandoc.getDocfileid());
					if (file.exists()) {
						System.out.println("FILE : " + plandoc.getDocfileid());
						file.delete();
					}
				}

				if (!isEdit) {
					if (!arg.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
						Mmenu mmenu = new MmenuDAO().findByFilter(
								"menupath = '/view/planning/planning.zul' and menuparamvalue = 'approval'");
						NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
					} else {
						Mmenu mmenu = new MmenuDAO().findByFilter(
								"menupath = '/view/planning/planingapprovalbydiv.zul' and menuparamvalue = 'opr'");
						NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
					}
				}
				doDone();
				doReset();

//				if (isEdit) {
//					Event closeEvent = new Event("onClose", winPlanEntry, null);
//					Events.postEvent(closeEvent);
//				}

				transaction.commit();
				session.close();
				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 5000);

				doReset();
			} else {
				Messagebox.show("Tidak ada data yang dipilih.", "Informasi", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doDone() {
		try {
			divRoot.getChildren().clear();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("obj", objForm);
			map.put("objData", objData);
			if (isEdit)
				map.put("isEdit", "Y");
			map.put("arg", arg);
			Executions.createComponents("/view/planning/planningentryresume.zul", divRoot, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			orderby = "producttype";
			filter = "productgroupcode = '" + arg + "' and mproductowner.mbranchfk = "
					+ oUser.getMbranch().getMbranchpk();

			if (producttype != null && producttype.trim().length() > 0)
				filter += " and producttype like '%" + producttype.toUpperCase() + "%'";

			objList = mproducttypeDao.listNativeByFilter(filter, orderby);
			totalrecord = NumberFormat.getInstance().format(objList.size());
			grid.setModel(new ListModelList<>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		productgroup = AppData.getProductgroupLabel(arg);
		cabang = oUser.getMbranch().getBranchname();
		totaldata = 0;
		totalrecord = "0";
		filename = "";
		docfileori = "";
		memodate = null;
		doctime = null;
		docfilesize = null;
		if (arg.equals("01")) {
			unit = "01";
		} else if (arg.equals("02")) {
			unit = "01";
		} else if (arg.equals("03")) {
			unit = "01";
		} else if (arg.equals("04")) {
			unit = "12";
		} else if (arg.equals("09")) {
			unit = "01";
		} else if (arg.equals("07")) {
			unit = "12";
		} else if (arg.equals("08")) {
			unit = "12";
		} else if (arg.equals("10")) {
			unit = "12";
		} else if (arg.equals("11")) {
			unit = "12";
		} else if (arg.equals("12")) {
			unit = "12";
		}

		objForm = new Tplan();
		objForm.setInputtime(new Date());
		objForm.setInputer(oUser.getUserid());

		mapData = new HashMap<Integer, Mproducttype>();
		mapQty = new HashMap<Integer, Integer>();
		docList = new ArrayList<Tplandoc>();
		mapMedia = new HashMap<String, Media>();
		gridDoc.setModel(new ListModelList<>(docList));
		doRenderDoc();
		doSearch();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				BigDecimal anggaran = (BigDecimal) ctx.getProperties("anggaran")[0].getValue();
				String memono = (String) ctx.getProperties("memono")[0].getValue();

				if (anggaran == null)
					this.addInvalidMessage(ctx, "anggaran", Labels.getLabel("common.validator.empty"));
				if (memono == null || "".equals(memono.trim()))
					this.addInvalidMessage(ctx, "memono", Labels.getLabel("common.validator.empty"));
				if (memodate == null)
					this.addInvalidMessage(ctx, "memodate", Labels.getLabel("common.validator.empty"));

			}
		};
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public Tplan getObjForm() {
		return objForm;
	}

	public void setObjForm(Tplan objForm) {
		this.objForm = objForm;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Date getMemodate() {
		return memodate;
	}

	public void setMemodate(Date memodate) {
		this.memodate = memodate;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(String totalrecord) {
		this.totalrecord = totalrecord;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public String getCabang() {
		return cabang;
	}

	public void setCabang(String cabang) {
		this.cabang = cabang;
	}

	public String getDocfileori() {
		return docfileori;
	}

	public void setDocfileori(String docfileori) {
		this.docfileori = docfileori;
	}

	public String getDocfileid() {
		return docfileid;
	}

	public void setDocfileid(String docfileid) {
		this.docfileid = docfileid;
	}

	public Integer getDocfilesize() {
		return docfilesize;
	}

	public void setDocfilesize(Integer docfilesize) {
		this.docfilesize = docfilesize;
	}

	public Date getDoctime() {
		return doctime;
	}

	public void setDoctime(Date doctime) {
		this.doctime = doctime;
	}

	public Tplandoc getObjDoc() {
		return objDoc;
	}

	public void setObjDoc(Tplandoc objDoc) {
		this.objDoc = objDoc;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}
}