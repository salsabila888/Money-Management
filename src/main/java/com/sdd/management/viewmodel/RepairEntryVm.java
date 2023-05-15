package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TrepairDAO;
import com.sdd.caption.dao.TrepairitemDAO;
import com.sdd.caption.dao.TrepairmemoDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mrepairreason;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Trepair;
import com.sdd.caption.domain.Trepairitem;
import com.sdd.caption.domain.Trepairmemo;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class RepairEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Trepair objForm;
	private Mproduct mproduct;
	private Mrepairreason mrepairreason;
	private Tbranchstock objStock;
	private TbranchstockDAO tbranchstockDao = new TbranchstockDAO();
	private TrepairDAO oDao = new TrepairDAO();

	private List<Tbranchstockitem> inList = new ArrayList<>();
	private List<Tbranchstockitem> objList = new ArrayList<>();
	private List<String> listData = new ArrayList<>();

	private String unit;
	private String itemno;
//	private String pinpadtype;
	private String memo;
	private Integer totaldata;
	private String filename;
	private String docfile;
	private Media media;
	private Media mediaDoc;
	private boolean isValidFile;

	@Wire
	private Row outlet, rowUpload;
	@Wire
	private Combobox cbProduct, cbReason;
	@Wire
	private Grid grid;
	@Wire
	private Div divRoot;
	@Wire
	private Textbox tbItem;
	@Wire
	private Checkbox chkbox;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();

		grid.setRowRenderer(new RowRenderer<Tbranchstockitem>() {

			@Override
			public void render(final Row row, final Tbranchstockitem data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getItemno() != null ? data.getItemno() : "-"));
				row.getChildren().add(new Label(data.getTid() != null ? data.getTid() : "-"));
				row.getChildren().add(new Label(data.getMid() != null ? data.getMid() : "-"));
				row.getChildren().add(new Label(
						data.getPinpadtype() != null ? AppData.getPinpadtypeLabel(data.getPinpadtype()) : "-"));
			}
		});

	}

	@Command
	public void doChecked() {
		if (chkbox.isChecked())
			rowUpload.setVisible(true);
		else
			rowUpload.setVisible(false);
	}

	@Command
	@NotifyChange({"filename", "totaldata"})
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			if (mproduct != null) {
				UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
				media = event.getMedia();
				filename = media.getName();
				if (media != null) {
					Trepair file = new TrepairDAO().findByFilter("filename = '" + media.getName() + "'");
					if (file == null) {
						if (media.getFormat().contains("xls")) {
							String serialno = "";
							Workbook wb = null;
							if (media.getName().toLowerCase().endsWith("xlsx")) {
								wb = new XSSFWorkbook(media.getStreamData());
							} else if (media.getName().toLowerCase().endsWith("xls")) {
								wb = new HSSFWorkbook(media.getStreamData());
							}
							Sheet sheet = wb.getSheetAt(0);
							for (org.apache.poi.ss.usermodel.Row row : sheet) {
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
									case 0:
										if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
											cell.setCellType(Cell.CELL_TYPE_STRING);
											serialno = cell.getStringCellValue();
										} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
											serialno = cell.getStringCellValue();
										}
										break;
									}
								}

								objStock = tbranchstockDao.findByFilter(
										"mbranchfk = " + oUser.getMbranch().getMbranchpk() + " and mproductfk = "
												+ mproduct.getMproductpk() + " and outlet = '00'");
								if (objStock != null) {
									objList = new TbranchstockitemDAO()
											.listNativeByFilter(
													"tbranchstockfk = " + objStock.getTbranchstockpk()
															+ " and itemno = '" + serialno.trim().toUpperCase()
															+ "' and status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "'",
													"itemno");
									System.out.println(objList.size());
									if (objList.size() > 0) {
										for (Tbranchstockitem data : objList) {
											if (!listData.contains(data.getItemno().trim())) {
												inList.add(data);
												listData.add(data.getItemno().trim());
												totaldata++;
												isValidFile = true;
											}
										}
									}
								}
							}
							if (inList.size() > 0) {
								refresh();
							} else {
								Messagebox.show("Data tidak ditemukan.", "Info", Messagebox.OK, Messagebox.INFORMATION);
							}
						} else {
							Messagebox.show("Format file tidak valid.", "Info", Messagebox.OK, Messagebox.INFORMATION);
						}
					} else {
						Messagebox.show("File sudah pernah diupload.", "Info", Messagebox.OK, Messagebox.INFORMATION);
					}
				}
			} else {
				Messagebox.show("Silahkan pilih produk terlebih dulu.", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("docfile")
	public void doBrowseDoc(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			mediaDoc = event.getMedia();
			docfile = mediaDoc.getName();
			System.out.println(docfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@NotifyChange("totaldata")
	@Command
	public void doRegister() {
		try {
			objStock = tbranchstockDao.findByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk()
					+ " and mproductfk = " + mproduct.getMproductpk() + " and outlet = '00'");
			if (objStock != null) {
				objList = new TbranchstockitemDAO()
						.listNativeByFilter("tbranchstockfk = " + objStock.getTbranchstockpk() + " and itemno = '"
								+ itemno.trim() + "' and status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "'", "itemno");
				System.out.println(objList.size());
				if (objList.size() > 0) {
					for (Tbranchstockitem data : objList) {
						if (!listData.contains(data.getItemno().trim())) {
							inList.add(data);
							listData.add(data.getItemno().trim());
							totaldata++;
						}
					}
				} else {
					Messagebox.show("Data tidak ditemukan.", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
				if (inList.size() > 0) {
					refresh();
				} else {
					Messagebox.show("Data tidak ditemukan.", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		tbItem.setValue("");
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (inList.size() > 0) {
			try {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();

				objForm.setRegid(new TcounterengineDAO().generateCounter(AppUtils.ID_PINPAD_PRODUCTION));
				objForm.setStatus(AppUtils.STATUS_REPAIR_WAITAPPROVAL);
				objForm.setItemqty(totaldata);
				objForm.setMproduct(mproduct);
				objForm.setTotalproses(0);
				objForm.setRepairfailed(0);
				objForm.setInsertedby(oUser.getUsername());
				objForm.setMrepairreason(mrepairreason);
				if (media != null && isValidFile) {
					String path = Executions.getCurrent().getDesktop().getWebApp()
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
				
				if (mediaDoc != null) {
					String path = Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_PINPAD);
					if (mediaDoc.isBinary()) {
						Files.copy(new File(path + "/" + mediaDoc.getName()), mediaDoc.getStreamData());
					} else {
						BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + mediaDoc.getName()));
						Files.copy(writer, mediaDoc.getReaderData());
						writer.close();
					}

					objForm.setDocfile(docfile);
				}
				
				oDao.save(session, objForm);

				for (Tbranchstockitem data : inList) {
					Trepairitem objData = new Trepairitem();
					objData.setItemno(data.getItemno());
					objData.setItemstatus(AppUtils.STATUS_REPAIR_WAITAPPROVAL);
					objData.setPinpadtype(data.getPinpadtype());
					objData.setTid(data.getTid());
					objData.setMid(data.getMid());
					objData.setPinpadmemo(data.getPinpadmemo());
					objData.setItemprice(data.getItemprice());
					objData.setTrepair(objForm);
					new TrepairitemDAO().save(session, objData);

					data.setStatus(AppUtils.STATUS_REPAIR_WAITAPPROVAL);
					new TbranchstockitemDAO().save(session, data);

					Tbranchitemtrack objTrack = new Tbranchitemtrack();
					objTrack.setItemno(data.getItemno());
					objTrack.setTracktime(new Date());
					objTrack.setTrackdesc(AppData.getStatusLabel(data.getStatus()));
					objTrack.setProductgroup(data.getProductgroup());
					objTrack.setMproduct(objForm.getMproduct());
					objTrack.setTrackstatus(AppUtils.STATUS_REPAIR_WAITAPPROVAL);
					new TbranchitemtrackDAO().save(session, objTrack);

				}

				objStock.setStockactivated(objStock.getStockactivated() + inList.size());
				objStock.setStockcabang(objStock.getStockcabang() - inList.size());
				new TbranchstockDAO().save(session, objStock);

				Trepairmemo objMemo = new Trepairmemo();
				objMemo.setMemo(memo);
				objMemo.setMemoby(oUser.getUsername());
				objMemo.setMemotime(new Date());
				objMemo.setTrepair(objForm);
				new TrepairmemoDAO().save(session, objMemo);

				transaction.commit();
				session.close();

				Mmenu mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/repair/repairapproval.zul' and menuparamvalue = 'E1'");
				NotifHandler.doNotif(mmenu, oUser.getMbranch(), objForm.getMproduct().getProductgroup(),
						oUser.getMbranch().getBranchlevel());

				doDone();

				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 5000);

				doReset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Tidak ada data.", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	private void doDone() {
		try {
			divRoot.getChildren().clear();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("obj", objForm);
			Executions.createComponents("/view/repair/repairentryresume.zul", divRoot, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void refresh() {
		grid.setModel(new ListModelList<Tbranchstockitem>(inList));
	}

	@NotifyChange("*")
	public void doReset() {
		isValidFile = false;
		objForm = new Trepair();
		objForm.setInserttime(new Date());
		objForm.setMbranch(oUser.getMbranch());
		memo = "";
		totaldata = 0;
		itemno = "";
//		pinpadtype = "";
		inList = new ArrayList<Tbranchstockitem>();
		listData = new ArrayList<String>();
		cbProduct.setValue(null);
		cbReason.setValue(null);
		mproduct = null;
		mrepairreason = null;
		unit = "02";
		refresh();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
//				try {
//					Mproduct mproduct = (Mproduct) ctx.getProperties("mproduct")[0].getValue();
//					if (mproduct == null)
//						this.addInvalidMessage(ctx, "mproduct", Labels.getLabel("common.validator.empty"));
//					
//					if (memo == null || "".trim().equals(memo))
//						this.addInvalidMessage(ctx, "memo", Labels.getLabel("common.validator.empty"));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
		};
	}

	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct("productgroup = '03'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mrepairreason> getMrepairreasonmodel() {
		ListModelList<Mrepairreason> lm = null;
		try {
			lm = new ListModelList<Mrepairreason>(AppData.getMrepairreason());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getItemno() {
		return itemno;
	}

	public void setItemno(String itemno) {
		this.itemno = itemno;
	}

	public Trepair getObjForm() {
		return objForm;
	}

	public void setObjForm(Trepair objForm) {
		this.objForm = objForm;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public Mrepairreason getMrepairreason() {
		return mrepairreason;
	}

	public void setMrepairreason(Mrepairreason mrepairreason) {
		this.mrepairreason = mrepairreason;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

	public String getDocfile() {
		return docfile;
	}

	public void setDocfile(String docfile) {
		this.docfile = docfile;
	}

	public Media getMediaDoc() {
		return mediaDoc;
	}

	public void setMediaDoc(Media mediaDoc) {
		this.mediaDoc = mediaDoc;
	}

}