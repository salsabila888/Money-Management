package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import com.sdd.caption.dao.McouriervendorDAO;
import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PodVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TdeliveryDAO tdeliveryDao = new TdeliveryDAO();

	private Mcouriervendor mcouriervendor;

	private int totalupdated;
	private String filename;
	private Media media;

	private DateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbCouriervendor;
	@Wire
	private Button btnSave;
	@Wire
	private Groupbox gbResult;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();
	}

	@NotifyChange("filename")
	@Command
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
		media = event.getMedia();
		filename = media.getName();
		if (media != null)
			btnSave.setDisabled(false);
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (media != null) {
			if (mcouriervendor != null) {
				Map<Integer, Torder> mapOrder = new HashMap<Integer, Torder>();

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

							String dlvid = null;
							Date tglterima = null;
							Date tglterima2 = null;
							String penerima = "";
							String penerima2 = "";
							String awb = null;
							String vendorcode = null;
							Tdelivery data = null;
							for (int count = 0; count <= row.getLastCellNum(); count++) {
								Cell cell = row.getCell(count, Row.RETURN_BLANK_AS_NULL);
								if (cell == null) {
									continue;
								}

								switch (count) {
								case 1:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
										cell.setCellType(Cell.CELL_TYPE_STRING);
										awb = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
										awb = cell.getStringCellValue();
									break;
								case 2:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
										cell.setCellType(Cell.CELL_TYPE_STRING);
										dlvid = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
										dlvid = cell.getStringCellValue();
									break;
								case 3:
									if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
										if (HSSFDateUtil.isCellDateFormatted(cell))
											tglterima = cell.getDateCellValue();
										else {
											tglterima = getDateCustomFormat(cell);
										}
									} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
										tglterima = datelocalFormatter.parse(cell.getStringCellValue().trim());
									}

									break;
								case 4:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
										penerima = String.valueOf(cell.getNumericCellValue());
									else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
										penerima = cell.getStringCellValue();
									break;
								case 5:
									if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
										if (HSSFDateUtil.isCellDateFormatted(cell))
											tglterima2 = cell.getDateCellValue();
										else {
											tglterima2 = getDateCustomFormat(cell);
										}
									} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
										tglterima2 = datelocalFormatter.parse(cell.getStringCellValue().trim());
									}

									break;
								case 6:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
										penerima2 = String.valueOf(cell.getNumericCellValue());
									else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
										penerima2 = cell.getStringCellValue();
									break;
								case 7:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
										vendorcode = String.valueOf(cell.getNumericCellValue());
									else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
										vendorcode = cell.getStringCellValue();
									break;
								}
							}

							if (dlvid != null) {
								data = tdeliveryDao.findByFilter("dlvid = '" + dlvid.trim().toUpperCase()
										+ "' and branchpool = '" + oUser.getMbranch().getBranchid() + "'");
								if (data != null) {
									Session session = StoreHibernateUtil.openSession();
									Transaction transaction = session.beginTransaction();
									try {
										data.setTglterima(tglterima);
										data.setPenerima(penerima);
										data.setTglterima2(tglterima2);
										data.setPenerima2(penerima2);
										data.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
										data.setAwb(awb);
										if (vendorcode != null && vendorcode.trim().length() > 0) {
											mcouriervendor = new McouriervendorDAO().findByFilter(
													"vendorcode = '" + vendorcode.trim().toUpperCase() + "'");
											if (mcouriervendor != null && mcouriervendor.getMcouriervendorpk() != data
													.getMcouriervendor().getMcouriervendorpk()) {
												data.setCouriervendor2(mcouriervendor.getVendorname());
											}
										}
										tdeliveryDao.save(session, data);

										List<Tdeliverydata> tddList = new TdeliverydataDAO()
												.listByFilter("tdeliveryfk = " + data.getTdeliverypk(), "tdeliveryfk");
										for (Tdeliverydata tdd : tddList) {
											if (tdd.getTpaketdata().getTpaket().getTorder() != null) {
												mapOrder.put(tdd.getTpaketdata().getTpaket().getTorder().getTorderpk(),
														tdd.getTpaketdata().getTpaket().getTorder());
											}
										}
										totalupdated++;
										transaction.commit();
									} catch (Exception e) {
										e.printStackTrace();
									} finally {
										session.close();
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					try {
						Session session = StoreHibernateUtil.openSession();
						Transaction transaction = session.beginTransaction();
						for (Entry<Integer, Torder> entry : mapOrder.entrySet()) {
							Torder order = entry.getValue();
							order.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
							new TorderDAO().save(session, order);
						}
						transaction.commit();
						session.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Messagebox.show("Proses update POD selesai.", "Info", Messagebox.OK, Messagebox.INFORMATION);
					gbResult.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Messagebox.show("Silahkan pilih vendor expedisi", "Exclamation", Messagebox.OK, Messagebox.EXCLAMATION);
			}
		} else {
			Messagebox.show("Silahkan upload file kiriman expedisi", "Exclamation", Messagebox.OK,
					Messagebox.EXCLAMATION);
		}
	}

	@Command
	public void doTemplate() {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet();
			XSSFCellStyle style = workbook.createCellStyle();

			style.setBorderTop(BorderStyle.MEDIUM);
			style.setBorderBottom(BorderStyle.MEDIUM);
			style.setBorderLeft(BorderStyle.MEDIUM);
			style.setBorderRight(BorderStyle.MEDIUM);

			int rownum = 0;
			int cellnum = 0;
			org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum);
			Cell cell = row.createCell(0);
			Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
			datamap.put(1, new Object[] { "No", "NoPOD", "No Warkat", "Tgl Terima 1", "Nama Penerima 1", "Tgl Terima 2",
					"Nama Penerima 2", "Kode Ekspedisi" });
			datamap.put(2, new Object[] { 1, "", "", "", "", "", "", "" });
			datamap.put(3, new Object[] { 2, "", "", "", "", "", "", "" });
			Set<Integer> keyset = datamap.keySet();
			for (Integer key : keyset) {
				row = sheet.createRow(rownum++);
				Object[] objArr = datamap.get(key);
				cellnum = 0;
				if (rownum == 1) {
					XSSFCellStyle styleHeader = workbook.createCellStyle();
					styleHeader.setBorderTop(BorderStyle.MEDIUM);
					styleHeader.setBorderBottom(BorderStyle.MEDIUM);
					styleHeader.setBorderLeft(BorderStyle.MEDIUM);
					styleHeader.setBorderRight(BorderStyle.MEDIUM);
					styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
					styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
					for (Object obj : objArr) {
						cell = row.createCell(cellnum++);
						CellUtil.setCellStyleProperty(cell, workbook, CellUtil.VERTICAL_ALIGNMENT,
								VerticalAlignment.CENTER);
						if (obj instanceof String) {
							cell.setCellValue((String) obj);
							cell.setCellStyle(styleHeader);
						} else if (obj instanceof Integer) {
							cell.setCellValue((Integer) obj);
							cell.setCellStyle(styleHeader);
						} else if (obj instanceof Double) {
							cell.setCellValue((Double) obj);
							cell.setCellStyle(styleHeader);
						}
					}
				} else {
					for (Object obj : objArr) {
						cell = row.createCell(cellnum++);
						if (obj instanceof String) {
							cell.setCellValue((String) obj);
							cell.setCellStyle(style);
						} else if (obj instanceof Integer) {
							cell.setCellValue((Integer) obj);
							cell.setCellStyle(style);
						} else if (obj instanceof Double) {
							cell.setCellValue((Double) obj);
							cell.setCellStyle(style);
						}
					}
				}
			}

			String path = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
			String filename = "TEMPLATE_POD.xlsx";
			FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
			workbook.write(out);
			out.close();

			Filedownload.save(new File(path + "/" + filename),
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		cbCouriervendor.setValue(null);
		mcouriervendor = null;
		btnSave.setDisabled(true);
		gbResult.setVisible(false);
	}

	private Date getDateCustomFormat(Cell cell) throws Exception {
		Date date = null;
		DataFormatter poiFormatter = new DataFormatter();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
		DataFormat format = cell.getSheet().getWorkbook().createDataFormat();
		style.setDataFormat(format.getFormat("[$-809]yyyy-MM-dd;@"));
		cell.setCellStyle(style);
		String sdate = poiFormatter.formatCellValue(cell);
		System.out.println("getDateCustomFormat sdate " + sdate);
		date = dateFormat.parse(sdate);
		return date;
	}

	public ListModelList<Mcouriervendor> getMcouriervendormodel() {
		ListModelList<Mcouriervendor> lm = null;
		try {
			lm = new ListModelList<Mcouriervendor>(AppData.getMcouriervendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Mcouriervendor getMcouriervendor() {
		return mcouriervendor;
	}

	public void setMcouriervendor(Mcouriervendor mcouriervendor) {
		this.mcouriervendor = mcouriervendor;
	}

	public int getTotalupdated() {
		return totalupdated;
	}

	public void setTotalupdated(int totalupdated) {
		this.totalupdated = totalupdated;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
