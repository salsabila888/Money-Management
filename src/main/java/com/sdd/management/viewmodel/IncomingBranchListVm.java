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
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TpilotingDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tpiloting;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class IncomingBranchListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpilotingDAO oDao = new TpilotingDAO();

	private String filter;
	private String orderby;
	private int branchlevel;
	private Date processtime;
	private Integer pageTotalSize;
	private String producttype;

	private List<Tpiloting> objList = new ArrayList<Tpiloting>();
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Grid grid;
	@Wire
	private Column colOutlet;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		branchlevel = oUser.getMbranch().getBranchlevel();

		if (branchlevel == 2)
			colOutlet.setVisible(false);

		doReset();
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tpiloting>() {
				@Override
				public void render(Row row, final Tpiloting data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getProducttype()));
					row.getChildren().add(new Label(data.getBranchname()));
					row.getChildren().add(new Label(data.getOutlet()));
					row.getChildren().add(new Label(data.getPrefix() != null ? data.getPrefix() : "-"));
					row.getChildren()
							.add(new Label(data.getStartno() != null ? String.valueOf(data.getStartno()) : "-"));
					row.getChildren().add(new Label(data.getEndno() != null ? String.valueOf(data.getEndno()) : "-"));
					row.getChildren().add(new Label(
							data.getTotalitem() != null ? NumberFormat.getInstance().format(data.getTotalqty()) : ""));
					row.getChildren().add(new Label(AppData.getStatusPilotingLabel(data.getStatus())));
					row.getChildren().add(new Label(data.getInsertedby() != null ? data.getInsertedby() : "-"));
					row.getChildren().add(new Label(data.getDecisionby() != null ? data.getDecisionby() : "-"));
					row.getChildren().add(new Label(
							data.getInserttime() != null ? datetimeLocalFormatter.format(data.getInserttime()) : "-"));

					Button btnEdit = new Button();
					btnEdit.setLabel("Edit");
					btnEdit.setAutodisable("self");
					btnEdit.setSclass("btn-light");
					btnEdit.setStyle(
							"border-radius: 8px; background-color: #eeba0b !important; color: #000000 !important;");
					btnEdit.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);
							map.put("isEdit", "Y");
							Window win = new Window();
							win = (Window) Executions.createComponents("/view/inventory/incomingbranchentry.zul", null,
									map);
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									doReset();
									BindUtils.postNotifyChange(null, null, IncomingBranchListVm.this, "*");
								}
							});
						}
					});
					

					Div div = new Div();
					if (data.getStatus().equals("WA") && oUser.getUserid().equals(data.getInsertedby())) {
						div.appendChild(btnEdit);
					}
					row.appendChild(div);
				}
			});
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		orderby = "tpilotingpk";
		filter = "branchid = '" + oUser.getMbranch().getBranchid() + "'";

		if (processtime != null)
			filter += " and date(inserttime) = '" + processtime + "'";
		if (producttype != null && producttype.trim().length() > 0)
			filter += " and producttype like '%" + producttype.toUpperCase() + "%'";

		doRefresh();
	}

	@NotifyChange("*")
	public void doRefresh() {
		try {
			objList = oDao.listByFilter(filter, orderby);
			grid.setModel(new ListModelList<Tpiloting>(objList));
			pageTotalSize = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		producttype = null;
		processtime = null;
		doSearch();
	}

	@Command
	@NotifyChange("*")
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
				cell.setCellValue("DAFTAR PERSEDIAAN INTEGRASI CABANG/WILAYAH");
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue(datetimeLocalFormatter.format(new Date()));
				row = sheet.createRow(rownum++);

				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1,
						new Object[] { "No", "Nama Produk", "Cabang/Wilayah", "Kode Outlet", "Prefix", "No Seri Awal",
								"No Seri Akhir", "Jumlah Lembar/Buku", "Status", "Diinput Oleh", "Disetujui Oleh",
								"Tgl Input" });
				no = 2;
				for (Tpiloting data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getProducttype(), data.getBranchname(), data.getOutlet(),
									data.getPrefix(), String.valueOf(data.getStartno()),
									String.valueOf(data.getEndno()), data.getTotalqty(),
									AppData.getStatusPilotingLabel(data.getStatus()), data.getInsertedby(),
									data.getDecisionby() != null ? data.getDecisionby() : "-",
									datetimeLocalFormatter.format(data.getInserttime()) });
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
				String filename = "DAFTAR_PERSEDIAAN" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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
		}
	}

	public Date getProcesstime() {
		return processtime;
	}

	public void setProcesstime(Date processtime) {
		this.processtime = processtime;
	}

	public Integer getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(Integer pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}
}
