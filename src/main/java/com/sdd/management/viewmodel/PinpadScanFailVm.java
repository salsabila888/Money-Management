package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
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

import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.utils.AppUtils;

public class PinpadScanFailVm {

	private List<Torderitem> listData;
	private Torder obj;
	
	private int totalfail;

	@Wire
	private Grid grid;
	@Wire
	private Window winPinpadFail;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("listFail") List<Torderitem> listFail, @ExecutionArgParam("obj") Torder obj) {
		Selectors.wireComponents(view, this, false);
		
		this.obj = obj;
		listData = listFail;
		totalfail = listData.size();
		doRenderData();
		
	}
	
	@NotifyChange("*")
	private void doRenderData() {
		try {
			if (listData != null && !listData.isEmpty()) {
				Rows rows = new Rows();
				int index = 1;
				for(Torderitem data : listData) {
					Row row = new Row();
					Label lbl = new Label();
					lbl.setValue(String.valueOf(index));
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getTid());
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(data.getPinpadmemo());
					row.appendChild(lbl);
					
					System.out.println("SERIAL NO : " + data.getTid());
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
		Event closeEvent = new Event("onClose", this.winPinpadFail, null);
		Events.postEvent(closeEvent);
		winPinpadFail.detach();
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
				cell.setCellValue("Daftar Entry Serial Nomor Fail");
				row = sheet.createRow(rownum++);

				/*
				 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
				 */
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "No Kartu", "Keterangan" });
				no = 2;
				for (Torderitem data : listData) {
					datamap.put(no, new Object[] { no - 1, data.getTid(), data.getPinpadmemo()});
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
				String filename = "Entry_SerialNo_Fail" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
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

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}

	public int getTotalfail() {
		return totalfail;
	}

	public void setTotalfail(int totalfail) {
		this.totalfail = totalfail;
	}

}
