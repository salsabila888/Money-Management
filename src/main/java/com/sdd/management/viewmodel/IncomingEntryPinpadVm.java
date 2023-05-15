package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.dao.TincomingvendorDAO;
import com.sdd.caption.dao.TpinpaditemDAO;
import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Msupplier;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.domain.Tincomingvendor;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.domain.Tplanproduct;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class IncomingEntryPinpadVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Tincoming objForm;
	private TincomingDAO oDao = new TincomingDAO();

	private String filename;
	private String productgroup;
	private String unit;
	private Integer totaldata;
	private Integer planqty;
	private String cabang;
	private String memo;
	private boolean isEdit = false;
	private String edite;

	private Media media;
	private String arg;

	private Tplan objPlan;
	private Tplanproduct tplanproduct;

	private Mproducttype objProducttype;
	private Mproducttype mproducttype;
	private Mproducttype mproducttypeEdit;
	
	private List<Msupplier> objList = new ArrayList<Msupplier>();
	private List<Tincomingvendor> vendorList = new ArrayList<Tincomingvendor>();
	private Map<Integer, Msupplier> map = new HashMap<Integer, Msupplier>();

	@Wire
	private Caption captIncoming, caption;
	@Wire
	private Row rowPinpad, rowStartno, rowEndno;
	@Wire
	private Combobox cbProducttype, cbVendor;
	@Wire
	private Textbox valcode;
	@Wire
	private Intbox quantity;
	@Wire
	private Button btnSave, btnPlan, btnPlanreset;
	@Wire
	private Groupbox gbIncoming;
	@Wire
	private Div divRoot;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("obj") Tincoming obj, @ExecutionArgParam("isEdit") String edites) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.arg = arg;
		this.edite = edites;
		if ("".equals(edite) || edite == null) {
			valcode.setValue("Auto Generated");
		} else {
			valcode.setValue("...");
		}

		doReset();
		if (obj != null) {
			captIncoming.setVisible(true);
			try {
				filename = obj.getFilename();
				cbVendor.setValue(obj.getMsupplier().getSuppliername());
				cbProducttype.setValue(obj.getMproducttype().getProducttype());
				gbIncoming.setOpen(true);
				btnPlan.setVisible(false);
				btnPlanreset.setVisible(true);
				objForm = obj;
				System.out.println(objForm.getTplanfk().getMemono());
				if ("".equals(edite) || edite == null) {
					valcode.setValue("Auto Generated");
				} else {
					valcode.setValue(objForm.getIncomingid());
					objPlan = objForm.getTplanfk();
					cbProducttype.setDisabled(false);
				}

				objProducttype = obj.getMproducttype();
				mproducttype = null;
				if (edite.equals("Y")) {
					mproducttype = new MproducttypeDAO()
							.findByFilter("productgroupcode = '" + AppUtils.PRODUCTGROUP_PINPAD
									+ "' and mproducttypepk =" + objForm.getMproducttype().getMproducttypepk());
				} else {
					mproducttype = new MproducttypeDAO()
							.findByFilter("productgroupcode = '" + AppUtils.PRODUCTGROUP_PINPAD + "'");
				}
				System.out.println("TOTAL PROSES : " + mproducttype.getLaststock() + ", TOTAL QTY : "
						+ mproducttype.getStockinjected());
				mproducttypeEdit = mproducttype;
				mproducttypeEdit.setLaststock(mproducttype.getLaststock() + objForm.getItemqty());
				objProducttype.setLaststock(objProducttype.getLaststock() + objForm.getItemqty());
				System.out.println("TOTAL PROSES2 : " + mproducttype.getLaststock() + ", TOTAL QTY2 : "
						+ mproducttype.getStockinjected());
//				============================================================================================

				planqty = obj.getItemqty();
				System.out.println(planqty.toString());
				totaldata = obj.getItemqty();
				memo = obj.getMemo();

				isEdit = true;
				caption.setVisible(true);
				getProduct(objPlan);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@NotifyChange("*")
	public void getProduct(Tplan tplan) {
		try {
			String filterProduct = "productgroupcode = '" + AppUtils.PRODUCTGROUP_PINPAD + "'";
			if (isEdit) {
				filterProduct += " or mproducttypepk = " + mproducttypeEdit.getMproducttypepk();
			}
			List<Mproducttype> mproducttypeList = new MproducttypeDAO().listByFilter(filterProduct, "mproducttypepk");
			for (Mproducttype data : mproducttypeList) {
				Comboitem item = new Comboitem();
				item.setLabel(data.getProducttype());
				item.setValue(data);
				cbProducttype.appendChild(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doPlan() {
		gbIncoming.setOpen(true);
		Map<String, Object> map = new HashMap<>();
		map.put("arg", arg);
		map.put("isIncoming", 1);
		map.put("keypage", "1");
		Window win = (Window) Executions.createComponents("/view/planning/planninglist.zul", null, map);
		win.setWidth("70%");
		win.setClosable(true);
		win.doModal();
		win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if (event.getData() != null) {
					objPlan = (Tplan) event.getData();
					BindUtils.postNotifyChange(null, null, IncomingEntryPinpadVm.this, "objPlan");
					btnPlan.setVisible(false);
					btnPlanreset.setVisible(true);
					cbProducttype.setDisabled(false);
					planqty = objPlan.getTotalqty();
					quantity.setPlaceholder("max. " + planqty.toString() + " item...");
				}
			}
		});
	}

	@Command
	@NotifyChange("*")
	public void doPlanreset() {
		objPlan = new Tplan();
		cbProducttype.getChildren().clear();
		cbProducttype.setDisabled(true);
		cbProducttype.setValue(null);
		totaldata = 0;
		planqty = null;
		if (planqty != null)
			quantity.setPlaceholder(planqty.toString());
		btnPlan.setVisible(true);
		btnPlanreset.setVisible(false);
		gbIncoming.setOpen(false);
	}

	@SuppressWarnings("unused")
	@Command
	@NotifyChange({ "filename", "totaldata" })
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			filename = media.getName();
			if (media != null) {
				Tincoming file = oDao.findByFilter("filename = '" + media.getName() + "'");
				if (file == null) {
					if (media.getFormat().contains("xls")) {
						btnSave.setDisabled(false);

						totaldata = 0;
						String serialno = "";
						Workbook wb = null;
						if (media.getName().toLowerCase().endsWith("xlsx")) {
							wb = new XSSFWorkbook(media.getStreamData());
						} else if (media.getName().toLowerCase().endsWith("xls")) {
							wb = new HSSFWorkbook(media.getStreamData());
						}
						Sheet sheet = wb.getSheetAt(0);
						for (org.apache.poi.ss.usermodel.Row row : sheet) {
							try {
								if (row.getRowNum() < 1) {
									continue;
								}
								for (int count = 0; count <= row.getLastCellNum(); count++) {
									Cell cell = row.getCell(count,
											org.apache.poi.ss.usermodel.Row.RETURN_BLANK_AS_NULL);
									if (cell == null) {
										continue;
									}
									switch (count) {
									case 1:
										if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
											cell.setCellType(Cell.CELL_TYPE_STRING);
											serialno = cell.getStringCellValue();
										} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
											serialno = cell.getStringCellValue();
										}
										break;
									}

								}  

								if (serialno != null) {
									if (new TpinpaditemDAO().pageCount("itemno = '" + serialno.trim() + "'") == 0) {
										totaldata++;
									} 
								} 
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else {
						btnSave.setDisabled(true);
						Messagebox.show("Format data harus berupa xls/xlsx", "Exclamation", Messagebox.OK,
								Messagebox.EXCLAMATION);
					}
				} else {
					btnSave.setDisabled(true);
					Messagebox.show("File sudah pernah diupload", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			System.out.println("PROSES SAVE");
			boolean isValid = true;

			if (isValid) {
				if (!(totaldata > planqty)) {
					System.out.println(totaldata);
					System.out.println(planqty);
					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					objForm.setIncomingid(new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_INVENTORY_INCOMING));
					objForm.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGWAITAPPROVAL);
					objForm.setEntryby(oUser.getUserid());
					objForm.setProductgroup(arg);
					objForm.setItemqty(totaldata);
					objForm.setTplanfk(objPlan);
	
					if (media != null) {
						String path = "";
						path = Executions.getCurrent().getDesktop().getWebApp()
								.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_PINPAD);
	
						if (media.isBinary()) {
							Files.copy(new File(path + "/" + media.getName()), media.getStreamData());
						} else {
							BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + media.getName()));
							Files.copy(writer, media.getReaderData());
							writer.close();
						}
	
						objForm.setFilename(filename);
					}
					oDao.save(session, objForm);
					
					Tincomingvendor objVendor = new Tincomingvendor();
					objVendor.setTincoming(objForm);
					objVendor.setMsupplier(objForm.getMsupplier());
					objVendor.setPicname(objForm.getMsupplier().getPicname());
					objVendor.setPichp(objForm.getMsupplier().getPichp());
					objVendor.setSuppliername(objForm.getMsupplier().getSuppliername());
					new TincomingvendorDAO().save(session, objVendor);
	
					objPlan.setTotalprocess(objPlan.getTotalprocess() + objForm.getItemqty());
					if (objPlan.getTotalqty() <= objPlan.getTotalprocess())
						objPlan.setStatus(AppUtils.STATUS_PLANNING_DONE);
					new TplanDAO().save(session, objPlan);
	
					transaction.commit();
					session.close();
	
					if (!isEdit) {
						Mmenu mmenu = new MmenuDAO()
								.findByFilter("menupath = '/view/inventory/incoming.zul' and menuparamvalue = 'approval'");
						NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
					}
					doDone();
	
					Clients.showNotification("Entri data incoming berhasil", "info", null, "middle_center", 5000);
					doReset();
				} else {
					Messagebox.show("ok");
				}
			} else {
				Messagebox.show("Nomer seri sudah pernah didaftarkan", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}

	private void doDone() {
		try {
			divRoot.getChildren().clear();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("obj", objForm);
			if (isEdit)
				map.put("isEdit", "Y");
			map.put("arg", arg);
			Executions.createComponents("/view/inventory/incomingentryresume.zul", divRoot, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		objForm = new Tincoming();
		objForm.setEntrytime(new Date());
		objPlan = null;
		gbIncoming.setOpen(false);
		btnPlan.setVisible(true);
		btnPlanreset.setVisible(false);

		filename = "";
		totaldata = 0;
		planqty = 0;
		media = null;
		productgroup = AppData.getProductgroupLabel(arg);
		cabang = oUser.getMbranch().getBranchname();
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
		}

		cbProducttype.setValue(null);
		cbVendor.setValue(null);
		quantity.setValue(0);

		objForm.setMbranch(oUser.getMbranch());
	}

	public ListModelList<Msupplier> getMsupplier() {
		ListModelList<Msupplier> lm = null;
		try {
			lm = new ListModelList<Msupplier>(AppData.getMsupplier());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mproducttype> getMproducttypemodel() {
		ListModelList<Mproducttype> lm = null;
		try {
			lm = new ListModelList<Mproducttype>(AppData.getMproducttype("productgroupname = 'PINPAD'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {
					Mproducttype mproducttype = (Mproducttype) ctx.getProperties("mproducttype")[0].getValue();
					if (mproducttype == null)
						this.addInvalidMessage(ctx, "mproducttype", Labels.getLabel("common.validator.empty"));

					Msupplier msupplier = (Msupplier) ctx.getProperties("msupplier")[0].getValue();
					if (msupplier == null)
						this.addInvalidMessage(ctx, "msupplier", Labels.getLabel("common.validator.empty"));

					BigDecimal harga = (BigDecimal) ctx.getProperties("harga")[0].getValue();
					if (harga == null || "".equals(harga.toString()))
						this.addInvalidMessage(ctx, "harga", Labels.getLabel("common.validator.empty"));

					if (quantity.getValue() == null || "".equals(quantity.toString()))
						this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));

					String spkno = (String) ctx.getProperties("spkno")[0].getValue();
					if (spkno == null || "".equals(spkno.trim()))
						this.addInvalidMessage(ctx, "spkno", Labels.getLabel("common.validator.empty"));

					Date spkdate = (Date) ctx.getProperties("spkdate")[0].getValue();
					if (spkdate == null)
						this.addInvalidMessage(ctx, "spkdate", Labels.getLabel("common.validator.empty"));

					String pksno = (String) ctx.getProperties("pksno")[0].getValue();
					if (pksno == null || "".equals(pksno.trim()))
						this.addInvalidMessage(ctx, "pksno", Labels.getLabel("common.validator.empty"));

					Date pksdate = (Date) ctx.getProperties("pksdate")[0].getValue();
					if (pksdate == null)
						this.addInvalidMessage(ctx, "pksdate", Labels.getLabel("common.validator.empty"));

					String vendorletterno = (String) ctx.getProperties("vendorletterno")[0].getValue();
					if (vendorletterno == null || "".equals(vendorletterno.trim()))
						this.addInvalidMessage(ctx, "vendorletterno", Labels.getLabel("common.validator.empty"));

					Date vendorletterdate = (Date) ctx.getProperties("vendorletterdate")[0].getValue();
					if (vendorletterdate == null)
						this.addInvalidMessage(ctx, "vendorletterdate", Labels.getLabel("common.validator.empty"));

					Integer manufacturedate = (Integer) ctx.getProperties("manufacturedate")[0].getValue();
					if (manufacturedate == null)
						this.addInvalidMessage(ctx, "manufacturedate", Labels.getLabel("common.validator.empty"));

					String memo = (String) ctx.getProperties("memo")[0].getValue();
					if (memo == null)
						this.addInvalidMessage(ctx, "memo", Labels.getLabel("common.validator.empty"));

					if (totaldata == null || totaldata == 0)
						this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));

					if (filename == null || "".equals(filename.trim()))
						this.addInvalidMessage(ctx, "filename", Labels.getLabel("common.validator.empty"));

					if (totaldata != null) {
						if (totaldata > planqty) {
							this.addInvalidMessage(ctx, "itemqty",
									"Products Qty must be equal or less than selected planning products qty ("
											+ planqty.toString() + ")");
						}
					}
					if (objPlan == null)
						this.addInvalidMessage(ctx, "tplan", Labels.getLabel("common.validator.empty"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public Tincoming getObjForm() {
		return objForm;
	}

	public void setObjForm(Tincoming objForm) {
		this.objForm = objForm;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public String getCabang() {
		return cabang;
	}

	public void setCabang(String cabang) {
		this.cabang = cabang;
	}

	public Tplan getObjPlan() {
		return objPlan;
	}

	public void setObjPlan(Tplan objPlan) {
		this.objPlan = objPlan;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Tplanproduct getTplanproduct() {
		return tplanproduct;
	}

	public void setTplanproduct(Tplanproduct tplanproduct) {
		this.tplanproduct = tplanproduct;
	}

	public Mproducttype getMproducttype() {
		return mproducttype;
	}

	public void setMproducttype(Mproducttype mproducttype) {
		this.mproducttype = mproducttype;
	}

	public Integer getPlanqty() {
		return planqty;
	}

	public void setPlanqty(Integer planqty) {
		this.planqty = planqty;
	}

	public Mproducttype getObjProducttype() {
		return objProducttype;
	}

	public void setObjProducttype(Mproducttype objProducttype) {
		this.objProducttype = objProducttype;
	}
}