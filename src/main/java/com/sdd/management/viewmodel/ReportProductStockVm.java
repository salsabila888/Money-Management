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
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Mregion;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Vsumproductstock;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class ReportProductStockVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	List<Vsumproductstock> objList = new ArrayList<>();
	private TbranchstockDAO oDao = new TbranchstockDAO();
	private String filter;

	private BigDecimal totalmasuk;
	private BigDecimal totalcabang;
	private BigDecimal totalkeluar;
	private BigDecimal totaldestroy;
	private String productcode;
	private String productgroup;
	private String branchname;
	private String regionname;
	private String productname;
	private Mbranch mbranch;
	private Mproducttype mproducttype;
	private Mregion mregion;
	private Morg morg;
	private int branchlevel;
	private int totalrecord;
	private String arg;

	@Wire
	private Combobox cbBranch, cbProducttype, cbRegion, cbOrg;
	@Wire
	private Grid grid;
	@Wire
	private Row rowBranch, rowRegion, rowOrg;
	@Wire
	private Button btnSearch, btnReset;
	@Wire
	private Div divRegion, divBranch;
	@Wire
	private Caption caption;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("isIndex") String isIndex) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		this.arg = arg;
		if (oUser != null) {
			branchlevel = oUser.getMbranch().getBranchlevel();
			if (branchlevel == 3) {
				rowBranch.setVisible(false);
				rowRegion.setVisible(false);
//				btnSearch.setVisible(false);
//				btnReset.setVisible(false);
			} else if (branchlevel == 1) {
				rowBranch.setVisible(false);
				rowRegion.setVisible(true);
				divRegion.setVisible(true);
				regionname = "SELURUH WILAYAH";
			} else if (branchlevel == 2) {
				rowBranch.setVisible(true);
				rowRegion.setVisible(false);
				divBranch.setVisible(true);
				branchname = "SELURUH CABANG";
			}
		}

		if (arg.equals(AppUtils.PRODUCTGROUP_CARD))
			rowOrg.setVisible(true);
		
		if(isIndex != null && isIndex.equals("Y"))
			caption.setVisible(true);

		productgroup = AppData.getProductgroupLabel(arg);
		doReset();

		grid.setRowRenderer(new RowRenderer<Vsumproductstock>() {
			@Override
			public void render(Row row, final Vsumproductstock data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getProductname()));
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
						map.put("obj", data);
						map.put("arg", arg);
						Window win = new Window();
						if (branchlevel == 3) {
							map.put("objBranch", oUser.getMbranch());
							win = (Window) Executions.createComponents("/view/report/reportoutletstock.zul", null, map);
						} else if (branchlevel == 2) {
							map.put("mbranch", mbranch);
							win = (Window) Executions.createComponents("/view/report/reportbranchstock.zul", null, map);
						} else if (branchlevel == 1) {
							map.put("mregion", mregion);
							win = (Window) Executions.createComponents("/view/report/reportregionstock.zul", null, map);
						}
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
				if (mregion != null)
					cell.setCellValue("Laporan Stock Wilayah " + mregion.getRegionname());
				else if (mbranch != null)
					cell.setCellValue("Laporan Stock Cabang " + mbranch.getBranchname());
				else {
					if (branchlevel == 1)
						cell.setCellValue("Laporan Stock Seluruh Wilayah");
					else if (branchlevel == 2)
						cell.setCellValue("Laporan Stock Seluruh Cabang");
				}
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Tanggal");
				cell = row.createCell(1);
				cell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

				row = sheet.createRow(rownum++);

				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "Kode Produk", "Nama Product", "Incoming", "Outgoing", "Destroyed", "Stock" });
				no = 2;
				for (Vsumproductstock data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getProductcode(), data.getProductname(),
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
			totalrecord = 0;
			objList = oDao.listStockProduct(filter);
			grid.setModel(new ListModelList<>(objList));

			for (Vsumproductstock data : objList) {
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

				if (arg.equals(AppUtils.PRODUCTGROUP_CARD))
					filter = "TBRANCHSTOCK.PRODUCTGROUP in ('01', '09')";
				else
					filter = "TBRANCHSTOCK.PRODUCTGROUP = '" + arg + "'";

				if (branchlevel == 2) {
					filter += " AND MREGIONFK = " + oUser.getMbranch().getMregion().getMregionpk();
				} else if (branchlevel == 3) {
					filter += " AND MBRANCHFK = " + oUser.getMbranch().getMbranchpk();
				}

				if (mbranch != null) {
					branchname = "CABANG " + mbranch.getBranchname();
					if (filter.trim().length() > 0)
						filter += " AND ";
					filter += "MBRANCHFK = " + mbranch.getMbranchpk();
				} else {
					branchname = "SELURUH CABANG";
				}

				if (productcode != null && productcode.trim().length() > 0) {
					if (filter.trim().length() > 0)
						filter += " AND ";
					filter = "PRODUCTCODE LIKE '%" + productcode.trim().toUpperCase() + "%'";
				}
				
				if (productname != null && productname.trim().length() > 0) {
					if (filter.trim().length() > 0)
						filter += " AND ";
					filter = "PRODUCTNAME LIKE '%" + productname.trim().toUpperCase() + "%'";
				}

				if (mproducttype != null) {
					if (filter.trim().length() > 0)
						filter += " AND ";
					filter = "MPRODUCTTYPEFK = " + mproducttype.getMproducttypepk();
				}

				if (mregion != null) {
					regionname = mregion.getRegionname();
					if (filter.trim().length() > 0)
						filter += " AND ";
					filter = "MREGIONFK = " + mregion.getMregionpk();
				} else {
					regionname = "SELURUH WILAYAH";
				}

				if (morg != null) {
					if (filter.trim().length() > 0)
						filter += " AND ";
					filter = "productorg = '" + morg.getOrg() + "'";
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
		mproducttype = null;
		mbranch = null;
		mregion = null;
		cbRegion.setValue(null);
		cbBranch.setValue(null);
		cbProducttype.setValue(null);
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
			lm = new ListModelList<Mproducttype>(AppData.getMproducttype("productgroupcode = '" + arg.trim() + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
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

	public ListModelList<Morg> getMorgmodel() {
		ListModelList<Morg> lm = null;
		try {
			lm = new ListModelList<Morg>(AppData.getMorg());
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

	public Mproducttype getMproducttype() {
		return mproducttype;
	}

	public void setMproducttype(Mproducttype mproducttype) {
		this.mproducttype = mproducttype;
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

	public Mregion getMregion() {
		return mregion;
	}

	public void setMregion(Mregion mregion) {
		this.mregion = mregion;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public String getRegionname() {
		return regionname;
	}

	public void setRegionname(String regionname) {
		this.regionname = regionname;
	}

	public Morg getMorg() {
		return morg;
	}

	public void setMorg(Morg morg) {
		this.morg = morg;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public BigDecimal getTotaldestroy() {
		return totaldestroy;
	}

	public void setTotaldestroy(BigDecimal totaldestroy) {
		this.totaldestroy = totaldestroy;
	}
}
