package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.utils.AppUtils;

public class ReportBranchStockDataDetailVm {

	private Tbranchstock obj;
	private int pageTotalSize;
	private String filter;
	private String orderby;

	private String cardno;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private List<Tembossdata> objList = new ArrayList<Tembossdata>();

	@Wire
	private Window winOrderdata;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tbranchstock obj)
			throws Exception {
		Selectors.wireComponents(view, this, false);

		this.obj = obj;
		doReset();

		grid.setRowRenderer(new RowRenderer<Tembossdata>() {

			@Override
			public void render(Row row, final Tembossdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getCardno()));
				row.getChildren().add(
						new Label(data.getOrderdate() == null ? "" : datelocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(data.getMproduct().getMproducttype().getProducttype()));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getMproduct() != null ? data.getMproduct().getProductname() : ""));
				row.getChildren().add(new Label(data.getBranchname()));
				row.getChildren().add(new Label(data.getBranchid()));
				row.getChildren().add(new Label(data.getKlncode()));
			}
		});
	}

	@Command
	public void doExport() {
		try {
			if (objList != null && objList.size() > 0) {
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
				Cell cell = row.createCell(1);
				cell.setCellValue("Org : ");
				cell = row.createCell(2);
				cell.setCellValue(obj.getMproduct().getMproducttype().getProductorg());
				row = sheet.createRow(rownum++);
				cell = row.createCell(1);
				cell.setCellValue("Tipe Produk : ");
				cell = row.createCell(2);
				cell.setCellValue(obj.getMproduct().getMproducttype().getProducttype());
				row = sheet.createRow(rownum++);
				cell.setCellValue("Kode Produk : ");
				cell = row.createCell(2);
				cell.setCellValue(obj.getMproduct().getProductcode());
				row = sheet.createRow(rownum++);
				cell.setCellValue("Jenis Produk : ");
				cell = row.createCell(2);
				cell.setCellValue(obj.getMproduct().getProductname());
				row = sheet.createRow(rownum++);

				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "No Kartu", "Tgl Order", "Tipe Produk", "Kode Produk",
						"Jenis Produk", "Nama Cabang", "Kode Cabang", "Kode KCP" });
				no = 2;
				for (Tembossdata data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getCardno(), datelocalFormatter.format(data.getOrderdate()),
									data.getMproduct().getMproducttype().getProducttype(), data.getProductcode(),
									data.getMproduct().getProductname(), data.getMbranch().getBranchname(),
									data.getBranchid() != null ? data.getBranchid() : "", data.getKlncode() });
					no++;
					total++;
				}
				datamap.put(no, new Object[] { "TOTAL", total });
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 6) {
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
				String filename = "DAFTAR_DATA_STOCKCABANG_" + obj.getMbranch().getBranchname() + ".xlsx";
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

	@NotifyChange("pageTotalSize")
	public void refreshModel() {
		try {
			orderby = "tembossdatapk desc";
			objList = new TembossdataDAO().dataStockList(filter, orderby);
			grid.setModel(new ListModelList<>(objList));
			pageTotalSize = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "tembossdata.mbranchfk = " + obj.getMbranch().getMbranchpk() + " and tembossdata.mproductfk = "
				+ obj.getMproduct().getMproductpk()
				+ " and tembossdata.isactivated is null and tembossbranch.status in ('D04', 'D05')";

		if (cardno != null && cardno.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "tembossdata.cardno like '%" + cardno.trim().toUpperCase() + "%'";
		}

		refreshModel();
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winOrderdata, null);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		cardno = "";
		doSearch();
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Tbranchstock getObj() {
		return obj;
	}

	public void setObj(Tbranchstock obj) {
		this.obj = obj;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
}
