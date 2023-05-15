package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Mregion;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vsumproductstock;
import com.sdd.caption.domain.Vsumregionstock;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class ReportRegionStockVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	List<Vsumregionstock> objList = new ArrayList<>();
	private TbranchstockDAO oDao = new TbranchstockDAO();
	private String filter;

	private BigDecimal totalmasuk;
	private BigDecimal totalcabang;
	private BigDecimal totalkeluar;
	private BigDecimal totaldestroy;
	private String productname;
	private Mregion mregion;
	private Mregion mregionparam;
	private Vsumproductstock obj;
	private Integer totalrecord;

	@Wire
	private Combobox cbRegion;
	@Wire
	private Grid grid;
	@Wire
	private Row rowRegion;
	@Wire
	private Button btnSearch, btnReset;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("obj") Vsumproductstock obj, @ExecutionArgParam("mregion") Mregion mregion) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		if (oUser != null) {
			if (mregion != null) {
				rowRegion.setVisible(false);
				btnSearch.setVisible(false);
				btnReset.setVisible(false);
			} else {
				rowRegion.setVisible(true);
				btnSearch.setVisible(true);
				btnReset.setVisible(true);
			}
		}

		if (mregion != null)
			this.mregionparam = mregion;

		this.obj = obj;
		productname = AppData.getProductgroupLabel(arg) + " - " + obj.getProductname();
		doReset();

		grid.setRowRenderer(new RowRenderer<Vsumregionstock>() {
			@Override
			public void render(Row row, final Vsumregionstock data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getRegionname()));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalmasuk())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalkeluar())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldestroy())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalstock())));

				Button btndetail = new Button("Detail");
				btndetail.setAutodisable("self");
				btndetail.setClass("btn btn-default btn-sm");
				btndetail.setStyle("border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						map.put("obj", obj);
						map.put("objRegion", data);
						map.put("arg", arg);
						Window win = (Window) Executions.createComponents("/view/report/reportbranchstock.zul", null,
								map);
						win.setWidth("90%");
						win.setClosable(true);
						win.doModal();
					}
				});
				row.getChildren().add(btndetail);
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
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("Laporan Stock ");
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Tanggal");
				cell = row.createCell(1);
				cell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

				row = sheet.createRow(rownum++);

				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "Cabang", "Incoming", "Outgoing", "Destroyed", "Stock" });
				no = 2;
				for (Vsumregionstock data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getRegionname(),
									NumberFormat.getInstance().format(data.getTotalmasuk()),
									NumberFormat.getInstance().format(data.getTotalkeluar()),
									NumberFormat.getInstance().format(data.getTotaldestroy()),
									NumberFormat.getInstance().format(data.getTotalstock()) });
					no++;
				}
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
				String filename = "CIMS_BRANCHSTOCK_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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
	
	@NotifyChange("*")
	public void refreshModel() {
		try {
			totalmasuk = new BigDecimal(0);
			totalcabang = new BigDecimal(0);
			totalkeluar = new BigDecimal(0);
			totaldestroy = new BigDecimal(0);
			objList = oDao.listStockRegion(filter);
			grid.setModel(new ListModelList<>(objList));

			for (Vsumregionstock data : objList) {
				totalmasuk = totalmasuk.add(new BigDecimal(data.getTotalmasuk()));
				totalcabang = totalcabang.add(new BigDecimal(data.getTotalstock()));
				totalkeluar = totalkeluar.add(new BigDecimal(data.getTotalkeluar()));
				totaldestroy = totaldestroy.add(new BigDecimal(data.getTotalkeluar()));
			}

			totalrecord = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		if (oUser != null) {
			filter = "MPRODUCTPK = " + obj.getMproductpk();

			if (mregion != null) {
				if (filter.trim().length() > 0)
					filter += " AND ";
				filter += "MREGIONPK = " + mregion.getMregionpk();
			}

			refreshModel();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		if (mregionparam != null)
			mregion = mregionparam;
		else
			mregion = null;
		cbRegion.setValue(null);
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		doSearch();
	}

	public ListModelList<Mregion> getMregionmodel() {
		ListModelList<Mregion> lm = null;
		try {
			lm = new ListModelList<Mregion>(AppData.getMregion());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mproducttype> getMproducttypemodel() {
		ListModelList<Mproducttype> lm = null;
		try {
			lm = new ListModelList<Mproducttype>(AppData.getMproducttype());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public BigDecimal getTotalmasuk() {
		return totalmasuk;
	}

	public void setTotalmasuk(BigDecimal totalmasuk) {
		this.totalmasuk = totalmasuk;
	}

	public BigDecimal getTotalcabang() {
		return totalcabang;
	}

	public void setTotalcabang(BigDecimal totalcabang) {
		this.totalcabang = totalcabang;
	}

	public BigDecimal getTotalkeluar() {
		return totalkeluar;
	}

	public void setTotalkeluar(BigDecimal totalkeluar) {
		this.totalkeluar = totalkeluar;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public Integer getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(Integer totalrecord) {
		this.totalrecord = totalrecord;
	}

	public Mregion getMregion() {
		return mregion;
	}

	public void setMregion(Mregion mregion) {
		this.mregion = mregion;
	}

	public BigDecimal getTotaldestroy() {
		return totaldestroy;
	}

	public void setTotaldestroy(BigDecimal totaldestroy) {
		this.totaldestroy = totaldestroy;
	}
}
