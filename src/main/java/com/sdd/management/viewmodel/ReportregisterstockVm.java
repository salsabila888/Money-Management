package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TregisterstockDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tregisterstock;
import com.sdd.caption.model.ToutgoingListModel;
import com.sdd.caption.model.TregisterstockListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;

public class ReportregisterstockVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private List<Tregisterstock> objList = new ArrayList<>();
	private TregisterstockDAO oDao = new TregisterstockDAO();

	private String filter;
	private Integer year;
	private Integer month;
	private String prefix;

	private String productgroup;
	private int totalrecord;
	private Tregisterstock tregisterstock;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("argid") String argp)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		productgroup = argp;
		doReset();

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tregisterstock>() {
				@Override
				public void render(Row row, final Tregisterstock data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(
							data.getTglincoming() != null ? dateLocalFormatter.format(data.getTglincoming()) : "-"));
					row.getChildren().add(new Label(data.getMproduct() != null ? data.getMproduct().trim() : "-"));
					row.getChildren()
							.add(new Label(data.getNumerawalinc() != null && data.getNumerakhirinc() != null
									? data.getNumerawalinc() + "-" + data.getNumerakhirinc()
									: "-"));
					row.getChildren()
							.add(new Label(
									data.getJumlahinc() != null ? NumberFormat.getInstance().format(data.getJumlahinc())
											: "0"));

					row.getChildren().add(new Label(
							data.getTgloutgoing() != null ? dateLocalFormatter.format(data.getTgloutgoing()) : "-"));
					row.getChildren()
							.add(new Label(data.getNumerawaloutg() != null && data.getNumerakhiroutg() != null
									? data.getNumerawaloutg() + "-" + data.getNumerakhiroutg()
									: "-"));
					row.getChildren()
							.add(new Label(data.getJumlahoutg() != null
									? NumberFormat.getInstance().format(data.getJumlahoutg())
									: "0"));

					row.getChildren()
							.add(new Label(data.getNumerawalouts() != null && data.getNumerakhirouts() != null
									? data.getNumerawalouts() + "-" + data.getNumerakhirouts()
									: "-"));
					row.getChildren()
							.add(new Label(data.getJumlahouts() != null
									? NumberFormat.getInstance().format(data.getJumlahouts())
									: "0"));
				}
			});
		}

		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item = new Comboitem();
			item.setLabel(months[i]);
			item.setValue(i + 1);
			cbMonth.appendChild(item);
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (year != null && month != null) {
				filter = "(extract(year from tglincoming) = " + year + " or extract(year from tgloutgoing) = " + year
						+ ") and (extract(month from tglincoming) = " + month + " or extract(year from tgloutgoing) = "
						+ year + ") and productgroup = '" + productgroup + "'";

				if (prefix != "" || !"".equals(prefix))
					filter += " and prefix = '" + prefix.toUpperCase().trim() + "'";

				refreshModel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doExport() {
		try {
			if (objList != null && objList.size() > 0) {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet();
				XSSFCellStyle style = workbook.createCellStyle();
				style.setBorderTop(BorderStyle.DOTTED);
				style.setBorderBottom(BorderStyle.DOTTED);
				style.setBorderLeft(BorderStyle.DOTTED);
				style.setBorderRight(BorderStyle.DOTTED);

				int rownum = 0;
				int cellnum = 0;
				Integer no = 0;
				Integer total = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("Report Register Produk " + AppData.getProductgroupLabel(productgroup));
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Periode");
				cell = row.createCell(1);
				cell.setCellValue(StringUtils.getMonthLabel(month) + " " + year);
				row = sheet.createRow(rownum++);

				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				List<Integer> markList = new ArrayList<Integer>();
				datamap.put(1,
						new Object[] { "No", "Tanggal Incoming", "Nama Produk", "Nomor Seri Incoming",
								"Jumlah Incoming", "Tanggal Outgoing", "Nomor Seri Outgoing", "Jumlah Outgoing",
								"nomor Seri Outstanding", "Jumlah Outstanding" });
				no = 2;
				for (Tregisterstock data : objList) {
					datamap.put(no, new Object[] { no - 1,
							data.getTglincoming() != null ? dateLocalFormatter.format(data.getTglincoming()) : "-",
							data.getMproduct() != null ? data.getMproduct().trim() : "-",
							data.getNumerawalinc() != null && data.getNumerakhirinc() != null
									? data.getNumerawalinc() + "-" + data.getNumerakhirinc()
									: "-",
							data.getJumlahinc() != null ? data.getJumlahinc() : 0,

							data.getTgloutgoing() != null ? dateLocalFormatter.format(data.getTgloutgoing()) : "-",
							data.getNumerawaloutg() != null && data.getNumerakhiroutg() != null
									? data.getNumerawaloutg() + "-" + data.getNumerakhiroutg()
									: "-",
							data.getJumlahoutg() != null ? data.getJumlahoutg() : 0,

							data.getNumerawalouts() != null && data.getNumerakhirouts() != null
									? data.getNumerawalouts() + "-" + data.getNumerakhirouts()
									: "-",
							data.getJumlahouts() != null ? data.getJumlahouts() : 0 });

					if (data.getTgloutgoing() == null)
						markList.add(no);

					no++;
					total++;
				}
				datamap.put(no, new Object[] { "TOTAL RECORD ", total });

				no = 1;
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (no == 1) {
						XSSFCellStyle styleHeader = workbook.createCellStyle();
						styleHeader.setBorderTop(BorderStyle.MEDIUM);
						styleHeader.setBorderBottom(BorderStyle.MEDIUM);
						styleHeader.setBorderLeft(BorderStyle.MEDIUM);
						styleHeader.setBorderRight(BorderStyle.MEDIUM);
						styleHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
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
					} else if (markList.contains(no)) {
						XSSFCellStyle styleBody = workbook.createCellStyle();
						styleBody.setBorderTop(BorderStyle.DOTTED);
						styleBody.setBorderBottom(BorderStyle.DOTTED);
						styleBody.setBorderLeft(BorderStyle.DOTTED);
						styleBody.setBorderRight(BorderStyle.DOTTED);
						styleBody.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
						styleBody.setFillPattern(CellStyle.SOLID_FOREGROUND);
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String) {
								cell.setCellValue((String) obj);
								cell.setCellStyle(styleBody);
							} else if (obj instanceof Integer) {
								cell.setCellValue((Integer) obj);
								cell.setCellStyle(styleBody);
							} else if (obj instanceof Double) {
								cell.setCellValue((Double) obj);
								cell.setCellStyle(styleBody);
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
					no++;
				}

				String path = Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
				String filename = "REPORT_REGISTER_STOCK_" + AppData.getProductgroupLabel(productgroup) + "_"
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

	@Command
	@NotifyChange("*")
	public void doReset() {
		tregisterstock = null;
		prefix = "";
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		doSearch();
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			totalrecord = 0;
			objList = oDao.listByFilter(filter, "prefix, tregisterstockpk");
			grid.setModel(new ListModelList<>(objList));
			totalrecord = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public int getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(int totalrecord) {
		this.totalrecord = totalrecord;
	}
}