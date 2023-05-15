package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PinpadIncomingEntry {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TincomingDAO tincomingDao = new TincomingDAO();
	private List<String> serialno = new ArrayList<String>();

	private Tincoming objForm;
	private String incomingid;
	private String productgroupcode;
	private String productgroupname;
	private int totalinserted;
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
		productgroupcode = AppUtils.PRODUCTGROUP_PINPAD;
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
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_PINPAD);
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
						if (row.getRowNum() < 1) {
							continue;
						}
						for (int count = 0; count <= row.getLastCellNum(); count++) {
							Cell cell = row.getCell(count, Row.RETURN_BLANK_AS_NULL);
							if (cell == null) {
								continue;
							}

							switch (count) {
							case 1:
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									cell.setCellType(Cell.CELL_TYPE_STRING);
									serialno.add(cell.getStringCellValue());
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									serialno.add(cell.getStringCellValue());
								}
								break;
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				totalinserted = serialno.size();
				
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				
				objForm.setIncomingid(new TcounterengineDAO().generateCounter(AppUtils.CE_INVENTORY_INCOMING));
				objForm.setProductgroup(productgroupcode);
				objForm.setFilename(filename);
				objForm.setEntrytime(new Date());
				objForm.setEntryby(oUser.getUserid());
				objForm.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGWAITAPPROVAL);
				objForm.setItemqty(totalinserted);
				objForm.setLastupdated(new Date());
				objForm.setUpdatedby(oUser.getUserid());
				objForm.setMbranch(oUser.getMbranch());
				tincomingDao.save(session, objForm);

				transaction.commit();
				session.close();

				if (media.isBinary()) {
					Files.copy(new File(path + "/" + media.getName()), media.getStreamData());
				} else {
					BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + media.getName()));
					Files.copy(writer, media.getReaderData());
					writer.close();
				}

				Clients.showNotification("Entri data incoming berhasil", "info", null, "middle_center", 3000);
				incomingid = objForm.getIncomingid();
				gbResult.setVisible(true);
				btnSave.setDisabled(true);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Silahkan upload file serial pinpad", "Exclamation", Messagebox.OK, Messagebox.EXCLAMATION);
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

		cbProduct.setValue(null);
		btnSave.setDisabled(true);
		gbResult.setVisible(false);
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
//				Mproducttype mproducttype = (Mproducttype) ctx.getProperties("mproducttype")[0].getValue();
//				if (mproducttype == null)
//					this.addInvalidMessage(ctx, "mproducttype", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	/*
	 * private Date getDateCustomFormat(Cell cell) throws Exception { Date date =
	 * null; DataFormatter poiFormatter = new DataFormatter(); DateFormat dateFormat
	 * = new SimpleDateFormat("yyyy-MM-dd"); CellStyle style =
	 * cell.getSheet().getWorkbook().createCellStyle(); DataFormat format =
	 * cell.getSheet().getWorkbook().createDataFormat();
	 * style.setDataFormat(format.getFormat("[$-809]yyyy-MM-dd;@"));
	 * cell.setCellStyle(style); String sdate = poiFormatter.formatCellValue(cell);
	 * System.out.println("getDateCustomFormat sdate "+sdate); date =
	 * dateFormat.parse(sdate); return date; }
	 */

	public ListModelList<Mproducttype> getMproducttype() {
		ListModelList<Mproducttype> lm = null;
		try {
			lm = new ListModelList<Mproducttype>(
					AppData.getMproducttype("productgroupcode = '" + AppUtils.PRODUCTGROUP_PINPAD + "'"));
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

}
