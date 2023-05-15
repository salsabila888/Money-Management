package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
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
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class TokenIncomingEntry {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TincomingDAO tincomingDao = new TincomingDAO();

	private Tincoming objForm;
	private String incomingid;
	private String productgroupcode;
	private String productgroupname;
	private int totalinserted;
	private int totalduplicated;
	private String filename;
	private Media media;

	@Wire
	private Combobox cbProduct;
	@Wire
	private Button btnSave;
	@Wire
	private Groupbox gbResult;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroupcode = AppUtils.PRODUCTGROUP_TOKEN;
		productgroupname = AppData.getProductgroupLabel(productgroupcode);
		doReset();
	}

	@NotifyChange("filename")
	@Command
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			filename = media.getName();
			if (media != null) {
				Tincoming file = tincomingDao.findByFilter("filename = '" + media.getName() + "'");
				if (file == null) {
					if (media.getFormat().contains("xls")) {
						btnSave.setDisabled(false);
					} else {
						Messagebox.show("Format data harus berupa xls/xlsx", "Exclamation", Messagebox.OK,
								Messagebox.EXCLAMATION);
					}
				} else {
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
		if (media != null) {
			Session session = null;
			Transaction transaction = null;

			String path = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_TOKEN);

			try {
				Workbook wb = null;
				if (filename.trim().toLowerCase().endsWith("xlsx")) {
					wb = new XSSFWorkbook(media.getStreamData());
				} else if (filename.trim().toLowerCase().endsWith("xls")) {
					wb = new HSSFWorkbook(media.getStreamData());
				}
				Sheet sheet = wb.getSheetAt(0);
				for (Row row : sheet) {
					try {
						if (row.getRowNum() < 2) {
							continue;
						}
						String startno = null;
						String endno = null;

						for (int count = 0; count <= row.getLastCellNum(); count++) {
							Cell cell = row.getCell(count, Row.RETURN_BLANK_AS_NULL);
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
							if (new TtokenitemDAO()
									.pageCount("cast(itemno as integer) between " + startno + " and " + endno) == 0) {
								for (Integer i = Integer.parseInt(startno); i <= Integer.parseInt(endno); i++) {
									totalinserted++;
								}
							} else {
								totalduplicated++;
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				try {
					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					objForm.setIncomingid(new TcounterengineDAO().generateCounter(AppUtils.CE_INVENTORY_INCOMING));
					objForm.setProductgroup(productgroupcode);
					objForm.setFilename(filename);
					objForm.setEntrytime(new Date());
					objForm.setEntryby(oUser.getUserid());
					objForm.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGWAITAPPROVAL);
					objForm.setLastupdated(new Date());
					objForm.setUpdatedby(oUser.getUserid());
					objForm.setItemqty(totalinserted);
					objForm.setMbranch(oUser.getMbranch());
					tincomingDao.save(session, objForm);

					Mmenu mmenu = new MmenuDAO()
							.findByFilter("menupath = '/view/inventory/incomingapproval.zul' and menuparamvalue = '02'");
					NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_TOKEN,
							oUser.getMbranch().getBranchlevel());

					transaction.commit();
					
					if (media.isBinary()) {
						Files.copy(new File(path + "/" + media.getName()), media.getStreamData());
					} else {
						BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + media.getName()));
						Files.copy(writer, media.getReaderData());
						writer.close();
					}

				} catch (Exception e) {
					transaction.rollback();
					e.printStackTrace();
				} finally {
					session.close();
				}

				Clients.showNotification("Entri data incoming berhasil", "info", null, "middle_center", 3000);
				incomingid = objForm.getIncomingid();
				gbResult.setVisible(true);
				btnSave.setDisabled(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Silahkan upload file serial token", "Exclamation", Messagebox.OK, Messagebox.EXCLAMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		objForm = new Tincoming();
		objForm.setEntrytime(new Date());
		incomingid = null;
		filename = null;
		totalinserted = 0;
		totalduplicated = 0;
		cbProduct.setValue(null);
		btnSave.setDisabled(true);
		gbResult.setVisible(false);
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mproducttype mproducttype = (Mproducttype) ctx.getProperties("mproducttype")[0].getValue();
				if (mproducttype == null)
					this.addInvalidMessage(ctx, "mproducttype", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public ListModelList<Mproducttype> getMproducttype() {
		ListModelList<Mproducttype> lm = null;
		try {
			lm = new ListModelList<Mproducttype>(
					AppData.getMproducttype("productgroupcode = '" + AppUtils.PRODUCTGROUP_TOKEN + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public int getTotalinserted() {
		return totalinserted;
	}

	public void setTotalinserted(int totalinserted) {
		this.totalinserted = totalinserted;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Tincoming getObjForm() {
		return objForm;
	}

	public void setObjForm(Tincoming objForm) {
		this.objForm = objForm;
	}

	public String getIncomingid() {
		return incomingid;
	}

	public void setIncomingid(String incomingid) {
		this.incomingid = incomingid;
	}

	public String getProductgroupcode() {
		return productgroupcode;
	}

	public void setProductgroupcode(String productgroupcode) {
		this.productgroupcode = productgroupcode;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

	public int getTotalduplicated() {
		return totalduplicated;
	}

	public void setTotalduplicated(int totalduplicated) {
		this.totalduplicated = totalduplicated;
	}

}
