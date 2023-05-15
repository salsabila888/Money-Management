package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.sdd.caption.pojo.DataFailed;
import com.sdd.caption.utils.AppUtils;

public class ActivationDataFailedVm {
	
	List<DataFailed> objListFail = new ArrayList<DataFailed>();
	
	private int totalfail;

	@Wire
	private Grid grid;
	@Wire
	private Window winFailedData;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("objListFail") List<DataFailed> objListFail) {
		Selectors.wireComponents(view, this, false);
		
		this.objListFail = objListFail;
		totalfail = objListFail.size();
		doRenderData();
		
	}
	
	@NotifyChange("*")
	private void doRenderData() {
		try {
			if (objListFail != null && !objListFail.isEmpty()) {
				Rows rows = new Rows();
				int index = 1;
				for(DataFailed data : objListFail) {
					Row row = new Row();
					Label lbl = new Label();
					lbl.setValue(String.valueOf(index));
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getItemno());
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getDesc1());
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getDesc2());
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getMemo());
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
		Event closeEvent = new Event("onClose", this.winFailedData, null);
		Events.postEvent(closeEvent);
		winFailedData.detach();
	}
	
	@Command
	public void doExport() {
		try {
			if (objListFail != null && objListFail.size() > 0) {
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
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("Daftar no serial gagal upload.");
				row = sheet.createRow(rownum++);

				/*
				 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
				 */
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "No Serial", "ID Cabang", "Outlet", "Keterangan" });
				no = 2;
				for (DataFailed data : objListFail) {
					datamap.put(no, new Object[] { no - 1, data.getItemno(), data.getDesc1(), data.getDesc2(), data.getMemo()});
					no++;
				}
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 3) {
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
				String filename = "DATA_GAGAL_UPLOAD" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
						+ ".xlsx";
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

	public int getTotalfail() {
		return totalfail;
	}

	public void setTotalfail(int totalfail) {
		this.totalfail = totalfail;
	}
}
