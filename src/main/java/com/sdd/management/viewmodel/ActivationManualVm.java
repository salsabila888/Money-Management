package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.TbranchitembucketDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Tbranchitembucket;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.pojo.DataFailed;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ActivationManualVm {

	private int totalupdated;
	private int totalsuccess;
	private int totalfailed;
	private String filename;
	private Media media;

	private DataFailed objFailed;
	private DateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	List<Tbranchstockitem> objList = new ArrayList<Tbranchstockitem>();
	List<DataFailed> objListFail = new ArrayList<DataFailed>();

	@Wire
	private Button btnSave;
	@Wire
	private Groupbox gbResult;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		doReset();
	}

	@NotifyChange("filename")
	@Command
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
		media = event.getMedia();
		filename = media.getName();
		if (media != null) {
			if (media.getFormat().contains("xls")) {
				btnSave.setDisabled(false);
			} else {
				Messagebox.show("Format harus berupa xls/xlsx", "Exclamation", Messagebox.OK, Messagebox.EXCLAMATION);
				btnSave.setDisabled(true);
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (media != null) {
			try {
				List<String> itemnoList = new ArrayList<String>();
				String filter = "";
				totalupdated = 0;
				totalsuccess = 0;
				totalfailed = 0;
				Workbook wb = null;
				if (filename.trim().toLowerCase().endsWith("xlsx")) {
					wb = new XSSFWorkbook(media.getStreamData());
				} else if (filename.trim().toLowerCase().endsWith("xls")) {
					wb = new HSSFWorkbook(media.getStreamData());
				}

				Sheet sheet = wb.getSheetAt(0);
				for (Row row : sheet) {
					if (row.getRowNum() < 1) {
						continue;
					}

					int qtyperunit = 1;
					Integer numerator = 0;
					String prefix = "";
					String itemno = "";
					String qty = "";
					String productname = "";
					String branchid = "";
					String outlet = "";
					String accno = "";
					Date usedate = null;
					String nasabah = "";

					for (int count = 0; count <= row.getLastCellNum(); count++) {
						Cell cell = row.getCell(count, Row.RETURN_BLANK_AS_NULL);
						if (cell == null) {
							continue;
						}

						switch (count) {
						case 1:
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								cell.setCellType(Cell.CELL_TYPE_STRING);
								itemno = cell.getStringCellValue();
							} else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
								itemno = cell.getStringCellValue();
							break;
						case 2:
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								cell.setCellType(Cell.CELL_TYPE_STRING);
								qty = cell.getStringCellValue();
							} else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
								qty = cell.getStringCellValue();
							break;
						case 3:
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								cell.setCellType(Cell.CELL_TYPE_STRING);
								productname = cell.getStringCellValue();
							} else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
								productname = cell.getStringCellValue();
							break;
						case 4:
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
								branchid = String.valueOf(cell.getNumericCellValue());
							else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
								branchid = cell.getStringCellValue();
							break;
						case 5:
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
								outlet = String.valueOf(cell.getNumericCellValue());
							else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
								outlet = cell.getStringCellValue();
							break;
						case 6:
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
								accno = String.valueOf(cell.getNumericCellValue());
							else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
								accno = cell.getStringCellValue();
							break;
						case 7:
							if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
								if (HSSFDateUtil.isCellDateFormatted(cell))
									usedate = cell.getDateCellValue();
								else {
									usedate = getDateCustomFormat(cell);
								}
							} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
								usedate = datelocalFormatter.parse(cell.getStringCellValue().trim());
							}

							break;
						case 8:
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
								nasabah = String.valueOf(cell.getNumericCellValue());
							else if (cell.getCellType() == Cell.CELL_TYPE_STRING)
								nasabah = cell.getStringCellValue();
							break;
						}
					}

					if (itemno != null && itemno.trim().length() > 0) {
						if (productname != null && productname.trim().length() > 0) {
							List<Mproduct> product = new MproductDAO()
									.listNativeByFilter("productname = '" + productname + "'", "productname");
							if (product.size() > 0) {
								qtyperunit = product.get(0).getMproducttype().getProductunitqty();

								if (nasabah.length() > 70)
									nasabah = nasabah.substring(0, 69);

								Tbranchstock tbranchstock = null;
								Tbranchitembucket tbranchitembucket = null;
								if (!itemnoList.contains(itemno + branchid.trim() + outlet.trim())) {
									int totalqty = Integer.parseInt(qty) * qtyperunit;
									for (Integer i = 1; i <= totalqty; i++) {
										if (tbranchstock == null && numerator == 0) {
											filter = "itemno = '" + itemno.toUpperCase() + "' and productname = '"
													+ productname.trim() + "' and mbranch.branchid = '" + branchid
													+ "' and outlet = '" + outlet + "'";
										} else {
											filter = "tbranchstockfk = " + tbranchstock.getTbranchstockpk()
													+ " and prefix = '" + prefix.trim() + "' and numerator = "
													+ numerator;
										}

										objList = new TbranchstockitemDAO().listNativeByFilter(filter,
												"tbranchstockitempk desc");

										if (objList.size() > 0) {
											Tbranchstockitem obj = objList.get(0);
											System.out.println("ITEM NO : " + obj.getItemno());
											if (obj.getTbranchstock().getMbranch().getBranchid().equals(branchid)) {
												if (obj.getTbranchstock().getOutlet().equals(outlet)) {
													if (obj.getStatus().equals(AppUtils.STATUS_SERIALNO_ENTRY)) {
														Session session = StoreHibernateUtil.openSession();
														Transaction trx = session.beginTransaction();
														obj.setStatus("8");
														obj.setNasabah(nasabah);
														obj.setAccno(accno);
														obj.setDateactivated(usedate);
														new TbranchstockitemDAO().save(session, obj);

														tbranchitembucket = obj.getTbranchitembucket();
														tbranchitembucket.setOutbound(tbranchitembucket.getOutbound() + 1);
														if (tbranchitembucket.getTotalitem() <= tbranchitembucket.getOutbound()) {
															tbranchitembucket.setIsrunout("Y");
															tbranchitembucket.setCurrentno(tbranchitembucket.getItemendno());
														} else {
															tbranchitembucket.setCurrentno(obj.getNumerator() + 1);
														}
														new TbranchitembucketDAO().save(session, tbranchitembucket);
														
														tbranchstock = obj.getTbranchstock();
														tbranchstock.setStockactivated(
																tbranchstock.getStockactivated() + 1);
														tbranchstock.setStockcabang(tbranchstock.getStockcabang() - 1);
														new TbranchstockDAO().save(session, tbranchstock);
														trx.commit();
														session.close();

														if (i == 1)
															totalsuccess++;

														numerator = obj.getNumerator() + 1;
														prefix = obj.getPrefix();
														itemnoList.add(itemno);
													} else {
														if (i == 1) {
															objFailed.setItemno(itemno);
															objFailed.setDesc1(branchid);
															objFailed.setDesc2(outlet);
															objFailed.setMemo("No. serial sudah terpakai.");

															objListFail.add(objFailed);
															objFailed = new DataFailed();
															totalfailed++;
														}
													}
												} else {
													if (i == 1) {
														objFailed.setItemno(itemno);
														objFailed.setDesc1(branchid);
														objFailed.setDesc2(outlet);
														objFailed.setMemo("No. serial terdaftar di Outlet "
																+ obj.getTbranchstock().getOutlet() + ".");

														objListFail.add(objFailed);
														objFailed = new DataFailed();
														totalfailed++;
													}
												}
											} else {
												if (i == 1) {
													objFailed.setItemno(itemno);
													objFailed.setDesc1(branchid);
													objFailed.setDesc2(outlet);
													objFailed.setMemo("No. serial terdaftar di Cabang "
															+ obj.getTbranchstock().getMbranch().getBranchid() + ".");

													objListFail.add(objFailed);
													objFailed = new DataFailed();
													totalfailed++;
												}
											}
										} else {
											if (i == 1) {
												objFailed.setItemno(itemno);
												objFailed.setDesc1(branchid);
												objFailed.setDesc2(outlet);
												objFailed.setMemo("No. serial belum terdaftar.");

												objListFail.add(objFailed);
												objFailed = new DataFailed();
												totalfailed++;
											}
										}
									}

								}
							}
						} else {
							objFailed.setItemno(itemno);
							objFailed.setDesc1(branchid);
							objFailed.setDesc2(outlet);
							objFailed.setMemo("Kolom kode produk tidak boleh kosong.");

							objListFail.add(objFailed);
							objFailed = new DataFailed();
							totalfailed++;
						}
					} else {
						objFailed.setItemno(itemno);
						objFailed.setDesc1(branchid);
						objFailed.setDesc2(outlet);
						objFailed.setMemo("Kolom no serial tidak boleh kosong.");

						objListFail.add(objFailed);
						objFailed = new DataFailed();
						totalfailed++;
					}
					totalupdated++;
				}
				Messagebox.show("Proses update status selesai.", "Info", Messagebox.OK, Messagebox.INFORMATION);
				gbResult.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Silahkan upload file.", "Exclamation", Messagebox.OK, Messagebox.EXCLAMATION);
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
			datamap.put(1, new Object[] { "No", "NO SERIAL", "PRODUCT CODE", "BRANCHID", "OUTLET CODE", "STATUS",
					"ACCOUNT NO", "DATE ACTIVATED", "NASABAH" });
			datamap.put(2, new Object[] { 1, "", "", "", "", "", "", "", "" });
			datamap.put(3, new Object[] { 2, "", "", "", "", "", "", "", "" });
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
			String filename = "TEMPLATE_AKTIVASI.xlsx";
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
	public void doView() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objListFail", objListFail);
		Window win = (Window) Executions.createComponents("/view/aktivasi/activationdatafailed.zul", null, map);
		win.setWidth("80%");
		win.setClosable(true);
		win.doModal();
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totalupdated = 0;
		totalsuccess = 0;
		totalfailed = 0;
		filename = "";
		media = null;
		btnSave.setDisabled(true);
		gbResult.setVisible(false);
		objFailed = new DataFailed();
		objList = new ArrayList<Tbranchstockitem>();
		objListFail = new ArrayList<DataFailed>();
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

	public int getTotalupdated() {
		return totalupdated;
	}

	public void setTotalupdated(int totalupdated) {
		this.totalupdated = totalupdated;
	}

	public int getTotalsuccess() {
		return totalsuccess;
	}

	public void setTotalsuccess(int totalsuccess) {
		this.totalsuccess = totalsuccess;
	}

	public int getTotalfailed() {
		return totalfailed;
	}

	public void setTotalfailed(int totalfailed) {
		this.totalfailed = totalfailed;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
