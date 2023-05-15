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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MsupplierDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.dao.TincomingvendorDAO;
import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.dao.TplanproductDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mmenu;
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

public class IncomingEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Tincoming objForm;
	private TincomingDAO oDao = new TincomingDAO();
	private TplanproductDAO planproductDao = new TplanproductDAO();

	private String filename;
	private String productgroup;
	private String endno;
	private Integer startno;
	private String unit;
	private Integer totaldata;
	private Integer totaldatamax;
	private String prefix;
	private String cabang;
	private String memo;
	private boolean isEdit = false;

	private Media media;
	private String arg;

	private Tplan objPlan;
	private Tplanproduct tplanproduct;
	private Tplanproduct tplanproductEdit;

	private List<Msupplier> objList = new ArrayList<Msupplier>();
	private List<Tincomingvendor> vendorList = new ArrayList<Tincomingvendor>();
	private Map<Integer, Msupplier> map = new HashMap<Integer, Msupplier>();

	@Wire
	private Row rowPinpad, rowStartno, rowEndno;
	@Wire
	private Combobox cbProducttype;
	@Wire
	private Intbox quantity;
	@Wire
	private Button btnSave, btnPlan, btnPlanreset;
	@Wire
	private Groupbox gbIncoming;
	@Wire
	private Div divRoot;
	@Wire
	private Caption caption;
	@Wire
	private Grid grid;
	@Wire
	private Column colCheck;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("obj") Tincoming obj) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.arg = arg;

		if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD) || arg.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
			rowPinpad.setVisible(true);
			rowStartno.setVisible(false);
			rowEndno.setVisible(false);
			quantity.setReadonly(true);
		}

		doReset();
		if (obj != null) {
			try {
				gbIncoming.setOpen(true);
				btnPlan.setVisible(false);
				btnPlanreset.setVisible(true);
				objForm = obj;
				objPlan = obj.getTplanfk();
				tplanproduct = planproductDao.findByFilter("tplanfk = " + objPlan.getTplanpk()
						+ " and mproducttypefk = " + obj.getMproducttype().getMproducttypepk());

				tplanproductEdit = tplanproduct;
				tplanproductEdit.setTotalprocess(tplanproductEdit.getTotalprocess() - objForm.getItemqty());

				objPlan.setTotalprocess(objPlan.getTotalprocess() - objForm.getItemqty());

				totaldata = obj.getItemqty();
				totaldatamax = tplanproductEdit.getQtyperunit() - tplanproductEdit.getTotalprocess();
				prefix = obj.getPrefix();
				startno = obj.getItemstartno();
				endno = String.valueOf(startno + totaldata - 1);
				memo = obj.getMemo();
				cbProducttype.setValue(obj.getMproducttype().getProducttype());

				isEdit = true;
				caption.setVisible(true);
				getProduct(objPlan);

				vendorList = new TincomingvendorDAO().listByFilter("tincomingfk = " + obj.getTincomingpk(),
						"tincomingvendorpk");
				for (Tincomingvendor vendor : vendorList) {
					objList.add(vendor.getMsupplier());
					map.put(vendor.getMsupplier().getMsupplierpk(), vendor.getMsupplier());
				}

				refreshModel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Msupplier>() {
				@Override
				public void render(Row row, final Msupplier data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);

					if (map.get(data.getMsupplierpk()) != null)
						check.setChecked(true);

					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked()) {
								map.put(data.getMsupplierpk(), data);
							} else {
								map.remove(data.getMsupplierpk());
							}
						}
					});

					row.getChildren().add(check);
					row.getChildren().add(new Label(data.getSuppliername()));
				}
			});
		}
	}

	@NotifyChange("*")
	public void getProduct(Tplan tplan) {
		try {
			String filterProduct = "tplanfk = " + tplan.getTplanpk() + " and qtyperunit > totalprocess";
			if (isEdit) {
				filterProduct += " or tplanproductpk = " + tplanproductEdit.getTplanproductpk();
			}

			List<Tplanproduct> planproductList = new TplanproductDAO().listByFilter(filterProduct, "mproducttypefk");
			for (Tplanproduct data : planproductList) {
				Comboitem item = new Comboitem();
				item.setLabel(data.getMproducttype().getProducttype());
				item.setValue(data);
				cbProducttype.appendChild(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange({ "totaldata", "cbproducttype", "endno" })
	public void doSelect() {
		try {
			if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
				if (isEdit && tplanproduct.getTplanproductpk().equals(tplanproductEdit.getTplanproductpk())) {
					totaldata = tplanproductEdit.getQtyperunit() - tplanproductEdit.getTotalprocess();
				} else {
					totaldata = tplanproduct.getQtyperunit() - tplanproduct.getTotalprocess();
				}

				totaldatamax = totaldata;
				doGetEndno();
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
					BindUtils.postNotifyChange(null, null, IncomingEntryVm.this, "objPlan");
					btnPlan.setVisible(false);
					btnPlanreset.setVisible(true);

					getProduct(objPlan);
				}
			}
		});
	}

	@Command
	@NotifyChange("*")
	public void doPlanreset() {
		objPlan = new Tplan();
		cbProducttype.getChildren().clear();
		cbProducttype.setValue(null);
		tplanproduct = null;
		totaldata = 0;
		btnPlan.setVisible(true);
		btnPlanreset.setVisible(false);
		gbIncoming.setOpen(false);
	}

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
//						String serialno = null;
						String startno = null;
						String endno = null;
						boolean isNumeric = true;
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
									if (arg.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
										switch (count) {
										case 1:
											if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
												cell.setCellType(Cell.CELL_TYPE_STRING);
												startno = cell.getStringCellValue();
											} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
												startno = cell.getStringCellValue();
											} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
												cell.setCellType(Cell.CELL_TYPE_STRING);
												startno = cell.getStringCellValue();
											}
											break;
										case 3:
											if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
												cell.setCellType(Cell.CELL_TYPE_STRING);
												endno = cell.getStringCellValue();
											} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
												endno = cell.getStringCellValue();
											} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
												cell.setCellType(Cell.CELL_TYPE_STRING);
												endno = cell.getStringCellValue();
											}
											break;
										}
									}
								}

								if (startno != null && endno != null) {
									isNumeric = StringUtils.isNumeric(startno);
									if (isNumeric) {
										if (new TtokenitemDAO().pageCount(
												"cast(itemno as integer) between " + startno + " and " + endno) == 0) {
											for (Integer i = Integer.parseInt(startno); i <= Integer
													.parseInt(endno); i++) {
												totaldata++;
											}
										}
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
			if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
				String filterDuplicate = "prefix = '" + prefix.trim().toUpperCase() + "' and (" + startno
						+ " between itemstartno and (itemstartno + itemqty - 1) or " + endno
						+ "  between itemstartno and (itemstartno + itemqty - 1)) and tincoming.status != '"
						+ AppUtils.STATUS_INVENTORY_INCOMINGDECLINE + "'";

				if (objForm.getTincomingpk() != null)
					filterDuplicate += " and tincomingpk != " + objForm.getTincomingpk();
				if (tplanproduct.getMproducttype().getGrouptype().equals(AppUtils.GROUPTYPE_DEPOSITO)
						|| tplanproduct.getMproducttype().getGrouptype().equals(AppUtils.GROUPTYPE_BUKUTABUNGAN)) {
					filterDuplicate += " and mproducttypefk = " + tplanproduct.getMproducttype().getMproducttypepk();
					List<Tincoming> duplicate = oDao.listNativeByFilter(filterDuplicate, "tincomingpk");
					if (duplicate.size() > 0) {
						isValid = false;
					}
				} else {
					filterDuplicate += " and grouptype = '" + tplanproduct.getMproducttype().getGrouptype() + "'";
					System.out.println(filterDuplicate);
					List<Tincoming> duplicate = oDao.listNativeByFilter(filterDuplicate, "tincomingpk");
					if (duplicate.size() > 0) {
						isValid = false;
					}
				}
			}

			if (isValid) {
				if (map.size() > 0) {
					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					if (!isEdit) {
						objForm.setIncomingid(
								new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_INVENTORY_INCOMING));
						objForm.setEntryby(oUser.getUserid());
					}
					objForm.setProductgroup(arg);
					objForm.setItemqty(totaldata);
					objForm.setItemstartno(startno);
					objForm.setPrefix(prefix);
					objForm.setTplanfk(objPlan);
					objForm.setMproducttype(tplanproduct.getMproducttype());
					objForm.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGWAITAPPROVAL);

					if (media != null) {
						String path = "";
						if (arg.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
							path = Executions.getCurrent().getDesktop().getWebApp()
									.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_TOKEN);
						}
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
					
					if(vendorList.size() > 0) {
						for (Tincomingvendor vendor : vendorList) {
							new TincomingvendorDAO().delete(session, vendor);
						}
					}
					
					for(Entry<Integer, Msupplier> entry : map.entrySet()) {
						Msupplier msupplier = entry.getValue();
						Tincomingvendor objVendor = new Tincomingvendor();
						objVendor.setTincoming(objForm);
						objVendor.setMsupplier(msupplier);
						objVendor.setPicname(msupplier.getPicname());
						objVendor.setPichp(msupplier.getPichp());
						objVendor.setSuppliername(msupplier.getSuppliername());
						new TincomingvendorDAO().save(session, objVendor);
					}

					if (tplanproductEdit != null
							&& tplanproduct.getTplanproductpk().equals(tplanproductEdit.getTplanproductpk())) {
						tplanproduct = tplanproductEdit;
						tplanproduct.setTotalprocess(tplanproduct.getTotalprocess() + objForm.getItemqty());
						new TplanproductDAO().save(session, tplanproduct);
					} else {
						if (tplanproductEdit != null)
							new TplanproductDAO().save(session, tplanproductEdit);

						tplanproduct.setTotalprocess(tplanproduct.getTotalprocess() + objForm.getItemqty());
						new TplanproductDAO().save(session, tplanproduct);
					}

					objPlan.setTotalprocess(objPlan.getTotalprocess() + objForm.getItemqty());
					if (objPlan.getTotalqty() <= objPlan.getTotalprocess())
						objPlan.setStatus(AppUtils.STATUS_PLANNING_DONE);
					new TplanDAO().save(session, objPlan);

					transaction.commit();
					session.close();

					if (!isEdit) {
						Mmenu mmenu = new MmenuDAO().findByFilter(
								"menupath = '/view/inventory/incoming.zul' and menuparamvalue = 'approval'");
						NotifHandler.doNotif(mmenu, oUser.getMbranch(), arg, oUser.getMbranch().getBranchlevel());
					}
					doDone();

					Clients.showNotification("Input data persediaan berhasil", "info", null, "middle_center", 5000);
					doReset();
				} else {
					Messagebox.show("Silahkan pilih vendor terlebih dahulu.", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
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

	@Command
	@NotifyChange({ "endno", "totaldata" })
	public void doGetEndno() {
		try {
			if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
				if (totaldata <= totaldatamax) {
					if (startno != null && startno > 0) {
						if (totaldata != null && totaldata > 0) {
							if (prefix != null && !"".equals(prefix)) {
								endno = String.valueOf(startno + totaldata - 1);
								if (Integer.valueOf(endno) > 9999999) {
									endno = String.valueOf(9999999);
									if (totaldata > 9999999)
										totaldata = 9999999;
									Messagebox.show("Nomer seri melebihi 7 digit.", "Info", Messagebox.OK,
											Messagebox.INFORMATION);
								}
							}
						}
					}
				} else {
					totaldata = totaldatamax;
					if (startno != null && startno > 0) {
						if (totaldata != null && totaldata > 0) {
							if (prefix != null && !"".equals(prefix)) {
								endno = String.valueOf(startno + totaldata - 1);
								if (Integer.valueOf(endno) > 9999999) {
									endno = String.valueOf(9999999);
									if (totaldata > 9999999)
										totaldata = 9999999;
									Messagebox.show("Nomer seri melebihi 9,999,999 digit.", "Info", Messagebox.OK,
											Messagebox.INFORMATION);
								}
							}
						}
					}
					Messagebox.show("Jumlah data tidak boleh melebihi jumlah planning.", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void refreshModel() {
		try {
			objList = new MsupplierDAO().listByFilter("0=0", "msupplierpk");
			grid.setModel(new ListModelList<>(objList));
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
		endno = "";
		prefix = "";
		totaldata = 0;
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

		objForm.setMbranch(oUser.getMbranch());
		refreshModel();
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

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {
					if (tplanproduct == null)
						this.addInvalidMessage(ctx, "mproducttype", Labels.getLabel("common.validator.empty"));

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

					if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD) || arg.equals(AppUtils.PRODUCTGROUP_TOKEN)) {
						if (filename == null || "".equals(filename.trim()))
							this.addInvalidMessage(ctx, "filename", Labels.getLabel("common.validator.empty"));
					} else {
						if (prefix == null || "".equals(prefix))
							this.addInvalidMessage(ctx, "startno", Labels.getLabel("common.validator.empty"));

						if (startno == null || startno == 0)
							this.addInvalidMessage(ctx, "startno", Labels.getLabel("common.validator.empty"));

						if (totaldata > totaldatamax)
							this.addInvalidMessage(ctx, "itemqty",
									Labels.getLabel("Jumlah item incoming melebihi jumlah item planning"));
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

	public String getEndno() {
		return endno;
	}

	public void setEndno(String endno) {
		this.endno = endno;
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

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Integer getStartno() {
		return startno;
	}

	public void setStartno(Integer startno) {
		this.startno = startno;
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

}
