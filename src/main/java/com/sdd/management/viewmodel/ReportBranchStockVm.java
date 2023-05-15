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

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vsumbranchstock;
import com.sdd.caption.domain.Vsumproductstock;
import com.sdd.caption.domain.Vsumregionstock;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class ReportBranchStockVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	List<Vsumbranchstock> objList = new ArrayList<>();
	private TbranchstockDAO oDao = new TbranchstockDAO();
	private String filter;

	private BigDecimal totalmasuk;
	private BigDecimal totalcabang;
	private BigDecimal totalkeluar;
	private BigDecimal totaldestroy;
	private String productcode;
	private String productname;
	private Mbranch mbranch;
	private Mbranch mbranchparam;
	private Vsumproductstock objProduct;
	private Vsumregionstock objRegion;
	private int branchlevel;
	private Integer totalrecord;

	@Wire
	private Combobox cbBranch;
	@Wire
	private Grid grid;
	@Wire
	private Row rowBranch;
	@Wire
	private Button btnSearch, btnReset;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("obj") Vsumproductstock objProduct,
			@ExecutionArgParam("objRegion") Vsumregionstock objRegion, @ExecutionArgParam("mbranch") Mbranch mbranch) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		if (oUser != null) {
			branchlevel = oUser.getMbranch().getBranchlevel();
			if (branchlevel == 3 || mbranch != null) {
				rowBranch.setVisible(false);
				btnSearch.setVisible(false);
				btnReset.setVisible(false);
			} else {
				rowBranch.setVisible(true);
				btnSearch.setVisible(true);
				btnReset.setVisible(true);
			}
		}

		if (mbranch != null)
			this.mbranchparam = mbranch;

		if (objProduct != null) {
			this.objProduct = objProduct;
			productname = AppData.getProductgroupLabel(arg) + " - " + objProduct.getProductname();
		}

		if (objRegion != null) {
			this.objRegion = objRegion;
		}

		doReset();

		grid.setRowRenderer(new RowRenderer<Vsumbranchstock>() {
			@Override
			public void render(Row row, final Vsumbranchstock data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getBranchname()));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalmasuk())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalkeluar())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldestroy())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalstock())));

				Button btndetail = new Button("Detail");
				btndetail.setAutodisable("self");
				btndetail.setClass("btn btn-default btn-sm");
				btndetail.setStyle(
						"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
				btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<>();
						Mbranch objBranch = new MbranchDAO().findByPk(data.getMbranchpk());
						map.put("objBranch", objBranch);
						map.put("obj", objProduct);
						map.put("arg", arg);

						Window win = (Window) Executions.createComponents("/view/report/reportoutletstock.zul", null,
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
				cell.setCellValue("Laporan Stock Cabang");
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
				for (Vsumbranchstock data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getBranchname(),
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
			objList = oDao.listStockBranch(filter);
			grid.setModel(new ListModelList<>(objList));

			for (Vsumbranchstock data : objList) {
				totalmasuk = totalmasuk.add(new BigDecimal(data.getTotalmasuk()));
				totalcabang = totalcabang.add(new BigDecimal(data.getTotalstock()));
				totalkeluar = totalkeluar.add(new BigDecimal(data.getTotalkeluar()));
				totaldestroy = totaldestroy.add(new BigDecimal(data.getTotaldestroy()));
			}

			totalrecord = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (oUser != null) {
				filter = "MPRODUCTPK = " + objProduct.getMproductpk();
				if (branchlevel == 2) {
					filter += " AND MREGIONFK = " + oUser.getMbranch().getMregion().getMregionpk();
				} else if (branchlevel == 3) {
					filter += " AND MBRANCHFK = " + oUser.getMbranch().getMbranchpk();
				}

				if (objRegion != null)
					filter += " AND MREGIONFK = " + objRegion.getMregionpk();

				if (mbranch != null) {
					if (filter.trim().length() > 0)
						filter += " AND ";
					filter += "MBRANCHFK = " + mbranch.getMbranchpk();
				}
				refreshModel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		productcode = "";
		if (mbranchparam != null)
			mbranch = mbranchparam;
		else
			mbranch = null;
		cbBranch.setValue(null);
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		doSearch();
	}

	public ListModelList<Mbranch> getMbranchmodel() {
		ListModelList<Mbranch> lm = null;
		try {
			if (oUser != null) {
				if (Integer.parseInt(oUser.getMbranch().getBranchid().trim()) >= 600
						&& Integer.parseInt(oUser.getMbranch().getBranchid().trim()) < 700) {
					lm = new ListModelList<Mbranch>(
							AppData.getMbranch("mregionfk = " + oUser.getMbranch().getMregion().getMregionpk()));
				} else {
					lm = new ListModelList<Mbranch>(AppData.getMbranch());
				}
			}
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

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
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

	public BigDecimal getTotaldestroy() {
		return totaldestroy;
	}

	public void setTotaldestroy(BigDecimal totaldestroy) {
		this.totaldestroy = totaldestroy;
	}
}
