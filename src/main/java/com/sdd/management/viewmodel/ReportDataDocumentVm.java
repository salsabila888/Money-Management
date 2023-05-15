package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
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
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Column;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.sdd.caption.dao.TbranchitembucketDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Vreportdatadocument;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class ReportDataDocumentVm {

	List<Vreportdatadocument> objList = new ArrayList<Vreportdatadocument>();
	List<String> columnList = new ArrayList<String>();
	List<Integer> indexList = new ArrayList<Integer>();
	Map<Integer, String> mapIndex = new HashMap<Integer, String>();

	private TbranchitembucketDAO oDao = new TbranchitembucketDAO();
	private String filter;

	private String productname;
	private Mbranch mbranch;
	private Integer totalrecord;
	private int colsindex;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbBranch;
	@Wire
	private Grid grid;
	@Wire
	private Column colProductname;
	@Wire
	private Menupopup editPopup;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		doReset();

		grid.setRowRenderer(new RowRenderer<Vreportdatadocument>() {
			@Override
			public void render(Row row, final Vreportdatadocument data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getProductname()));
				row.getChildren().add(new Label(data.getPrefix()));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getCurrentno())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getItemendno())));
				row.getChildren().add(new Label(data.getIsrunout().equals("N") ? "TIDAK" : "YA"));
				row.getChildren().add(new Label(data.getBranchname()));
				row.getChildren().add(new Label(data.getOutlet()));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getItemprice())));
				row.getChildren().add(new Label(data.getPlanmemono()));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getPlanmemodate())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getPlanqty())));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getAnggaran())));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getPlanapprovetime())));
				row.getChildren().add(new Label(
						data.getPlantimepfa() != null ? dateLocalFormatter.format(data.getPlantimepfa()) : "-"));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getIncomingtime())));
				row.getChildren().add(new Label(data.getVendorletterno() != null ? data.getVendorletterno() : "-"));
				row.getChildren()
						.add(new Label(data.getVendorletterdate() != null
								? dateLocalFormatter.format(data.getVendorletterdate())
								: "-"));
				row.getChildren().add(new Label(data.getSpkno()));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getSpkdate())));
				row.getChildren().add(new Label(data.getPksno() != null ? data.getPksno() : "-"));
				row.getChildren()
						.add(new Label(data.getPksdate() != null ? dateLocalFormatter.format(data.getPksdate()) : "-"));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getIncomingqty())));
				row.getChildren().add(new Label(data.getSuppliername()));
				row.getChildren().add(new Label(data.getManufacturedate() != null ? String.valueOf(data.getManufacturedate()) : "-"));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
				row.getChildren().add(new Label(data.getPlanmemono()));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getPlanmemodate())));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderapprovetime())));
				row.getChildren().add(new Label(data.getOrdermemo() != null ? data.getOrdermemo() : "-"));
				row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getOrderqty())));
				row.getChildren().add(new Label(data.getOutapprovetime() != null ? dateLocalFormatter.format(data.getOutapprovetime()) : "-"));
				row.getChildren().add(new Label(data.getOutgoingmemo() != null ? data.getOutgoingmemo() : "-"));
				row.getChildren().add(new Label(data.getSwitchtime() != null ? dateLocalFormatter.format(data.getSwitchtime()) : "-"));
				row.getChildren().add(new Label(data.getSwitchapprovetime() != null ? dateLocalFormatter.format(data.getSwitchapprovetime()) : "-"));
				row.getChildren().add(
						new Label(data.getPakettime() != null ? dateLocalFormatter.format(data.getPakettime()) : "-"));
				row.getChildren().add(new Label(
						data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "-"));
				row.getChildren().add(new Label(data.getDlvid() != null ? data.getDlvid() : "-"));
				row.getChildren().add(new Label(data.getBeratitem() != null ? data.getBeratitem() : "-"));
				row.getChildren().add(new Label(data.getVendorname() != null ? data.getVendorname() : "-"));
				row.getChildren()
						.add(new Label(data.getDlvtime() != null ? dateLocalFormatter.format(data.getDlvtime()) : "-"));
				row.getChildren().add(
						new Label(data.getTglterima() != null ? dateLocalFormatter.format(data.getTglterima()) : "-"));
				row.getChildren().add(new Label(data.getPenerima() != null ? data.getPenerima() : "-"));
				row.getChildren()
						.add(new Label(
								data.getBookapprovetime() != null ? dateLocalFormatter.format(data.getBookapprovetime())
										: "-"));
			}
		});
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			objList = oDao.reportList(filter);
			grid.setModel(new ListModelList<>(objList));
			totalrecord = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			filter = "";

			if (productname != null && productname.trim().length() > 0) {
				if (filter.trim().length() > 0)
					filter += " AND ";
				filter += "mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
			}

			if (mbranch != null) {
				if (filter.trim().length() > 0)
					filter += " AND ";
				filter += "mbranch.branchname = '" + mbranch.getBranchname() + "'";
			}

			refreshModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		productname = "";
		mbranch = null;
		cbBranch.setValue(null);
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();

		renderMenupopup();
		doSearch();
	}

	@Command
	public void doExport() {
		try {
			int index = 0;
			Object[] cols = new Object[columnList.size()];
			for (String col : columnList) {
				cols[index++] = col;
			}

			if (objList.size() > 0) {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet();
				XSSFCellStyle style = workbook.createCellStyle();
				style.setBorderTop(BorderStyle.MEDIUM);
				style.setBorderBottom(BorderStyle.MEDIUM);
				style.setBorderLeft(BorderStyle.MEDIUM);
				style.setBorderRight(BorderStyle.MEDIUM);

				int rownum = 0;
				int cellnum = 0;
				int cellindex = 0;
				Integer no = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum);
				Cell cell = row.createCell(0);
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, cols);
				no = 2;
				for (Vreportdatadocument data : objList) {
					datamap.put(no, new Object[] { no - 1, data.getProductname(), data.getPrefix(),
							NumberFormat.getInstance().format(data.getCurrentno()),
							NumberFormat.getInstance().format(data.getItemendno()),
							data.getIsrunout().equals("N") ? "TIDAK" : "YA", data.getBranchname(),
							data.getOutlet(), NumberFormat.getInstance().format(data.getItemprice()),
							data.getPlanmemono(), dateLocalFormatter.format(data.getPlanmemodate()),
							NumberFormat.getInstance().format(data.getPlanqty()),
							NumberFormat.getInstance().format(data.getAnggaran()),
							dateLocalFormatter.format(data.getPlanapprovetime()),
							data.getPlantimepfa() != null ? dateLocalFormatter.format(data.getPlantimepfa()) : "-",
							dateLocalFormatter.format(data.getIncomingtime()),
							data.getVendorletterno() != null ? data.getVendorletterno() : "-",
							data.getVendorletterdate() != null ? dateLocalFormatter.format(data.getVendorletterdate())
									: "-",
							data.getSpkno(), dateLocalFormatter.format(data.getSpkdate()),
							data.getPksno() != null ? data.getPksno() : "-",
							data.getPksdate() != null ? dateLocalFormatter.format(data.getPksdate()) : "-",
							NumberFormat.getInstance().format(data.getIncomingqty()), data.getSuppliername(),
							data.getManufacturedate() != null
									? NumberFormat.getInstance().format(data.getManufacturedate())
									: "-",
							dateLocalFormatter.format(data.getOrderdate()), data.getPlanmemono(),
							dateLocalFormatter.format(data.getPlanmemodate()),
							dateLocalFormatter.format(data.getOrderapprovetime()),
							data.getOrdermemo() != null ? data.getOrdermemo() : "-",
							NumberFormat.getInstance().format(data.getOrderqty()),
							data.getOutapprovetime() != null ? dateLocalFormatter.format(data.getOutapprovetime()) : "-",
							data.getOutgoingmemo() != null ? data.getOutgoingmemo() : "-",
							data.getPakettime() != null ? dateLocalFormatter.format(data.getPakettime()) : "-",
							data.getQuantity() != null ? NumberFormat.getInstance().format(data.getQuantity()) : "-",
							data.getDlvid() != null ? data.getDlvid() : "-",
							data.getBeratitem() != null ? data.getBeratitem() : "-",
							data.getVendorname() != null ? data.getVendorname() : "-",
							data.getDlvtime() != null ? dateLocalFormatter.format(data.getDlvtime()) : "-",
							data.getTglterima() != null ? dateLocalFormatter.format(data.getTglterima()) : "-",
							data.getPenerima() != null ? data.getPenerima() : "-",
							data.getBookapprovetime() != null ? dateLocalFormatter.format(data.getBookapprovetime())
									: "-" });
					no++;
				}
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					cellindex = 0;
					if (rownum == 1) {
						XSSFCellStyle styleHeader = workbook.createCellStyle();
						styleHeader.setBorderTop(BorderStyle.MEDIUM);
						styleHeader.setBorderBottom(BorderStyle.MEDIUM);
						styleHeader.setBorderLeft(BorderStyle.MEDIUM);
						styleHeader.setBorderRight(BorderStyle.MEDIUM);
						styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
						for (Object obj : objArr) {
							if (mapIndex.get(cellindex) != null) {
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
							cellindex++;
						}
					} else {
						for (Object obj : objArr) {
							if (mapIndex.get(cellindex) != null) {
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
							cellindex++;
						}
					}
				}

				String path = Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
				String filename = "REPORT_IMS" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
				workbook.write(out);
				out.close();

				Filedownload.save(new File(path + "/" + filename),
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void renderMenupopup() {
		List<Column> components = grid.getColumns().getChildren();
		colsindex = 0;
		for (Column comp : components) {
			System.out.println(comp.getLabel());
			comp.setId(String.valueOf(colsindex));
			Menuitem menuitem = new Menuitem();
			menuitem.setLabel(comp.getLabel());
			menuitem.setChecked(true);
			menuitem.setAutocheck(true);
			menuitem.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					try {
						if (menuitem.isChecked()) {
							comp.setVisible(true);
							System.out.println("INDEX : " + comp.getId());
							mapIndex.put(Integer.parseInt(comp.getId()), comp.getLabel());
							editPopup.open(1000, 50);
							System.out.println("SIZE TRUE : " + mapIndex.size());
						} else {
							comp.setVisible(false);
							System.out.println("INDEX : " + comp.getId());
							mapIndex.remove(Integer.parseInt(comp.getId()));
							editPopup.open(1000, 50);
							System.out.println("SIZE FALSE : " + mapIndex.size());
						}
						BindUtils.postNotifyChange(null, null, ReportDataDocumentVm.this, "comp");
						BindUtils.postNotifyChange(null, null, ReportDataDocumentVm.this, "grid.getColumns()");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			editPopup.appendChild(menuitem);
			columnList.add(comp.getLabel());
			mapIndex.put(colsindex, comp.getLabel());
			colsindex++;
			System.out.println("SIZE : " + mapIndex.size());
		}
	}

	public ListModelList<Mbranch> getMbranchmodel() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(AppData.getMbranch());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public Integer getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(Integer totalrecord) {
		this.totalrecord = totalrecord;
	}

}
