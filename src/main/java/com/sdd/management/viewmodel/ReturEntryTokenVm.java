package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.zkoss.bind.annotation.ExecutionArgParam;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.dao.TreturnitemDAO;
import com.sdd.caption.dao.TreturntrackDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mreturnreason;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Treturnitem;
import com.sdd.caption.domain.Treturnmemo;
import com.sdd.caption.domain.Treturntrack;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ReturEntryTokenVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private Treturn objForm;
	private Mproduct mproduct;
	private Tbranchstock objStock;
	private Mreturnreason mreturnreason;

	private TreturnDAO oDao = new TreturnDAO();
	private TbranchstockDAO tbranchstockDao = new TbranchstockDAO();

	private List<Tbranchstockitem> objList = new ArrayList<>();
	private List<String> listData = new ArrayList<>();

	private String memo;
	private Integer totaldata;
	private String productgroup;
	private String filename;
	private Media media;
	private String arg;

	@Wire
	private Combobox cbProduct, cbReason;
	@Wire
	private Button btnSave;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.arg = arg;
		doReset();
	}

	@SuppressWarnings("unused")
	@Command
	@NotifyChange({ "filename", "totaldata" })
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			filename = media.getName();
			System.out.println(filename);
			if (media != null) {
				Treturn file = oDao.findByFilter("filename = '" + media.getName() + "'");
				if (file == null) {
					if (media.getFormat().contains("xls")) {
						objStock = tbranchstockDao.findByFilter("mbranchfk = " + oUser.getMbranch().getMbranchpk()
								+ " and mproductfk = " + mproduct.getMproductpk() + " and outlet = '00'");
						if (objStock != null) {
							btnSave.setDisabled(false);

							totaldata = 0;
							String serialno = "";
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

									if (startno != null && endno != null) {
										isNumeric = StringUtils.isNumeric(startno);
										if (isNumeric) {
											for (Integer i = Integer.parseInt(startno); i <= Integer
													.parseInt(endno); i++) {
												Tbranchstockitem item = new TbranchstockitemDAO()
														.findByFilter("tbranchstockfk = " + objStock.getTbranchstockpk()
																+ " and itemno = '" + i + "'");
												if (item != null
														&& item.getStatus().equals(AppUtils.STATUS_SERIALNO_ENTRY)) {
													if (!listData.contains(item.getItemno())) {
														objList.add(item);
														listData.add(item.getItemno());
														totaldata++;
														System.out.println(totaldata);
													}
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
							Messagebox.show(
									"Cabang " + oUser.getMbranch().getBranchname() + " belum memiliki stok produk "
											+ mproduct.getProductname(),
									"Exclamation", Messagebox.OK, Messagebox.EXCLAMATION);
						}
					} else {
						btnSave.setDisabled(true);
						Messagebox.show("Format data harus berupa xls/xlsx.", "Exclamation", Messagebox.OK,
								Messagebox.EXCLAMATION);
					}
				} else {
					btnSave.setDisabled(true);
					Messagebox.show("File sudah pernah diupload.", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (totaldata > 0) {
			try {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();

				objForm.setRegid(new TcounterengineDAO().generateCounter(AppUtils.ID_TOKEN_BRANCH));
				objForm.setReturnlevel(oUser.getMbranch().getBranchlevel());
				objForm.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
				objForm.setItemqty(totaldata);
				objForm.setMproduct(mproduct);
				objForm.setMreturnreason(mreturnreason);
				objForm.setInsertedby(oUser.getUsername());
				objForm.setLettertype(mproduct.getMproducttype().getDoctype());
				objForm.setIsdestroy(mreturnreason.getIsDestroy());

				if (media != null) {
					String path = Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_TOKENDOC);
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

				for (Tbranchstockitem data : objList) {
					Treturnitem objData = new Treturnitem();
					objData.setItemno(data.getItemno());
					objData.setItemstatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
					objData.setItemprice(data.getItemprice());
					objData.setTreturn(objForm);
					new TreturnitemDAO().save(session, objData);

					data.setStatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
					new TbranchstockitemDAO().save(session, data);

					Tbranchitemtrack objTrack = new Tbranchitemtrack();
					objTrack.setItemno(data.getItemno());
					objTrack.setTracktime(new Date());
					objTrack.setTrackdesc(AppData.getStatusLabel(data.getStatus()));
					objTrack.setProductgroup(data.getProductgroup());
					objTrack.setMproduct(objForm.getMproduct());
					objTrack.setTrackstatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
					new TbranchitemtrackDAO().save(session, objTrack);

				}

				objStock.setStockactivated(objStock.getStockactivated() + totaldata);
				objStock.setStockcabang(objStock.getStockcabang() - totaldata);
				new TbranchstockDAO().save(session, objStock);

				Treturnmemo objMemo = new Treturnmemo();
				objMemo.setMemo(memo);
				objMemo.setMemoby(oUser.getUsername());
				objMemo.setMemotime(new Date());
				objMemo.setTreturn(objForm);
				new TreturnDAO().save(session, objForm);

				Treturntrack objTrack = new Treturntrack();
				objTrack.setTreturn(objForm);
				objTrack.setTrackstatus(AppUtils.STATUS_RETUR_WAITAPPROVAL);
				objTrack.setTrackdesc(AppData.getStatusLabel(objTrack.getTrackstatus()));
				objTrack.setTracktime(new Date());
				new TreturntrackDAO().save(session, objTrack);

				Mmenu mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/return/return.zul' and menuparamvalue = 'approval'");
				NotifHandler.doNotif(mmenu, oUser.getMbranch(), objForm.getMproduct().getProductgroup(),
						oUser.getMbranch().getBranchlevel());

				transaction.commit();
				session.close();

				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 5000);

				doReset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@NotifyChange("*")
	public void doReset() {
		objForm = new Treturn();
		objForm.setInserttime(new Date());
		objForm.setMbranch(oUser.getMbranch());
		memo = "";
		filename = "";
		totaldata = 0;
		objList = new ArrayList<Tbranchstockitem>();
		listData = new ArrayList<String>();
		cbProduct.setValue(null);
		cbReason.setValue(null);
		mproduct = null;
		mreturnreason = null;
		productgroup = AppData.getProductgroupLabel(arg);
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {
//					Mproduct mproduct = (Mproduct) ctx.getProperties("mproduct")[0].getValue();
//					if (mproduct == null)
//						this.addInvalidMessage(ctx, "mproduct", Labels.getLabel("common.validator.empty"));

//					Mreturnreason mreturnreason = (Mreturnreason) ctx.getProperties("mreturnreason")[0].getValue();
//					if (mreturnreason == null)
//						this.addInvalidMessage(ctx, "mreturnreason", Labels.getLabel("common.validator.empty"));

					if (memo == null || "".trim().equals(memo))
						this.addInvalidMessage(ctx, "memo", Labels.getLabel("common.validator.empty"));
					if (filename == null || "".trim().equals(filename))
						this.addInvalidMessage(ctx, "filename", Labels.getLabel("common.validator.empty"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct("productgroup = '02'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mreturnreason> getMreturnreasonmodel() {
		ListModelList<Mreturnreason> lm = null;
		try {
			lm = new ListModelList<Mreturnreason>(AppData.getMreturnreason("productgroup = '02'"));
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

	public Treturn getObjForm() {
		return objForm;
	}

	public void setObjForm(Treturn objForm) {
		this.objForm = objForm;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public Mreturnreason getMreturnreason() {
		return mreturnreason;
	}

	public void setMreturnreason(Mreturnreason mreturnreason) {
		this.mreturnreason = mreturnreason;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public Tbranchstock getObjStock() {
		return objStock;
	}

	public void setObjStock(Tbranchstock objStock) {
		this.objStock = objStock;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

}
