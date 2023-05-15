package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;

public class DerivatifDataSuccessVm {

	private List<Tembossdata> listData;
	private Tderivatif obj;

	private Integer year;
	private Integer month;

	@Wire
	private Caption caption;
	@Wire
	private Grid grid;
	@Wire
	private Label totalRecord;
	@Wire
	private Window winBranchData;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("listSuccess") List<Tembossdata> listSuccess,
			@ExecutionArgParam("isSuccess") final String isSuccess, @ExecutionArgParam("obj") final Tderivatif obj)
			throws ParseException {
		Selectors.wireComponents(view, this, false);

		this.obj = obj;

		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;

		if (isSuccess != null && isSuccess.equals("Y")) {
			this.listData = listSuccess;
			doRenderData();
			caption.setLabel("Daftar Reject Data Inserted");
			totalRecord.setValue(String.valueOf(listData.size()));
		}

	}

	@NotifyChange("grid")
	private void doRenderData() {
		try {
			if (listData != null && !listData.isEmpty()) {
				Rows rows = new Rows();
				int index = 1;
				for (Tembossdata data : listData) {
					Row row = new Row();
					Label lbl = new Label();
					lbl.setValue(String.valueOf(index));
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getCardno());
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getProductcode() != null ? data.getProductcode() : "");
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getBranchid() != null ? data.getBranchid() : "");
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getNameoncard());
					row.appendChild(lbl);

					rows.getChildren().add(row);
					index++;
				}
				grid.appendChild(rows);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", this.winBranchData, null);
		Events.postEvent(closeEvent);
		winBranchData.detach();
	}

	@Command
	public void doExport() {
		try {
			if (listData != null && listData.size() > 0) {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet();
				XSSFCellStyle style = workbook.createCellStyle();
				style.setBorderTop(BorderStyle.MEDIUM);
				style.setBorderBottom(BorderStyle.MEDIUM);
				style.setBorderLeft(BorderStyle.MEDIUM);
				style.setBorderRight(BorderStyle.MEDIUM);

				int rownum = 0;
				int cellnum = 0;
				Integer no = 0;
				Integer total = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue(caption.getLabel());
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Periode");
				cell = row.createCell(1);
				cell.setCellValue(StringUtils.getMonthLabel(month) + " " + year);
				row = sheet.createRow(rownum++);

				/*
				 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
				 */
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "No Kartu", "Kode Produk", "Kode Cabang", "Keterangan" });
				no = 2;
				for (Tembossdata data : listData) {
					datamap.put(no,
							new Object[] { no - 1, data.getCardno(),
									data.getProductcode() != null ? data.getProductcode() : "",
									data.getBranchid() != null ? data.getBranchid() : "", data.getNameoncard() });
					no++;
					total++;
				}
				datamap.put(no, new Object[] { "TOTAL", total });
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 5) {
						XSSFCellStyle styleHeader = workbook.createCellStyle();
						styleHeader.setBorderTop(BorderStyle.MEDIUM);
						styleHeader.setBorderBottom(BorderStyle.MEDIUM);
						styleHeader.setBorderLeft(BorderStyle.MEDIUM);
						styleHeader.setBorderRight(BorderStyle.MEDIUM);
						styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
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
				String filename = "CAPTION_DERVATIF_GETDATA_FAIL"
						+ new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
				workbook.write(out);
				out.close();

				Filedownload.save(new File(path + "/" + filename),
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			} else {
				Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	public Tderivatif getObj() {
		return obj;
	}

	public void setObj(Tderivatif obj) {
		this.obj = obj;
	}

}
