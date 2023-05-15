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
import org.zkoss.bind.annotation.BindingParam;
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
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Treportplanf;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class ReportplanfixVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	List<Treportplanf> objList = new ArrayList<>();
	private TplanDAO oDao = new TplanDAO();
	private String productgroup, filter, memono, planno;
	private int totalrecord;
//	private List<String> filcol = new ArrayList<>();
//	private List<Object> filcol2 = new ArrayList<>();

	private SimpleDateFormat datenormalFormatter = new SimpleDateFormat("dd-MM-YYYY");

	@Wire
	private Grid grid;
	@Wire
	private Groupbox gbFilter;
	@Wire
	private Label cardTitle, layout;
	@Wire
	private Button btnSearch, btnReset, btnFilter, btnFilterclose;
	@Wire
	private Checkbox chkbox0, chkbox1, chkbox2, chkbox3, chkbox4, chkbox5, chkbox6, chkbox7, chkbox8, chkbox9, chkbox10,
			chkbox11, chkbox12;
	@Wire
	private Column clproducttype, clnoplan, clbranch, clinputdate, clanggaran, cltotalitem, clnomemo, clmemodate, cldoc,
			cltotalproses, clstatusplan, clstatusinc;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("argid") String argid)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.productgroup = argid;
		doReset();

		if (grid != null) {
			if (oUser.getMbranch().getBranchlevel() == 1) {
				grid.setRowRenderer(new RowRenderer<Treportplanf>() {
					@Override
					public void render(Row row, final Treportplanf data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						row.getChildren().add(new Label(data.getProducttype()));
						row.getChildren().add(new Label(data.getPlanno()));
						row.getChildren().add(new Label(data.getBranchname()));
						row.getChildren().add(new Label(datenormalFormatter.format(data.getEntrytime())));
						row.getChildren().add(
								new Label(data.getAnggaran() != null ? "Rp." + data.getAnggaran().toString() : "Rp.0"));
						row.getChildren()
								.add(new Label(data.getTotalqty() != null ? data.getTotalqty().toString() : "0"));
						row.getChildren().add(new Label(data.getMemono()));
						row.getChildren().add(new Label(datenormalFormatter.format(data.getMemodate())));

						if (data.getDocfileori() != null) {
							A a = new A("• " + data.getDocfileori().trim());
							a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
								@Override
								public void onEvent(Event event) throws Exception {
									Sessions.getCurrent().setAttribute("reportPath",
											AppUtils.FILES_ROOT_PATH + AppUtils.MEMO_PATH + data.getDocfileid());
									Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
								}
							});
							row.getChildren().add(a);
						} else {
							row.getChildren().add(new Label("-"));
						}

						row.getChildren().add(
								new Label(data.getTotalprocess() != null ? data.getTotalprocess().toString() : "0"));
						row.getChildren().add(
								new Label(data.getPstatus() != null ? AppData.getStatusLabel(data.getPstatus()) : "-"));
						row.getChildren().add(
								new Label(data.getIstatus() != null ? AppData.getStatusLabel(data.getIstatus()) : "-"));
					}
				});
			}
		}
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (checked) {
				chkbox1.setChecked(true);
				chkbox2.setChecked(true);
				chkbox3.setChecked(true);
				chkbox4.setChecked(true);
				chkbox5.setChecked(true);
				chkbox6.setChecked(true);
				chkbox7.setChecked(true);
				chkbox8.setChecked(true);
				chkbox9.setChecked(true);
				chkbox10.setChecked(true);
				chkbox11.setChecked(true);
				chkbox12.setChecked(true);
			} else if (!checked) {
//				filcol = new ArrayList<>();
//				filcol2 = new ArrayList<>();
				chkbox1.setChecked(false);
				chkbox2.setChecked(false);
				chkbox3.setChecked(false);
				chkbox4.setChecked(false);
				chkbox5.setChecked(false);
				chkbox6.setChecked(false);
				chkbox7.setChecked(false);
				chkbox8.setChecked(false);
				chkbox9.setChecked(false);
				chkbox10.setChecked(false);
				chkbox11.setChecked(false);
				chkbox12.setChecked(false);
			}
			doChecked();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doShowfilter() {
		btnFilterclose.setVisible(true);
		btnFilter.setVisible(false);
		chkbox0.setVisible(true);
		gbFilter.setOpen(true);
	}

	@Command
	@NotifyChange("*")
	public void doClosefilter() {
		btnFilterclose.setVisible(false);
		btnFilter.setVisible(true);
		chkbox0.setVisible(false);
		gbFilter.setOpen(false);
	}

	@Command
	@NotifyChange("*")
	public void doChecked() {
		try {
			if (chkbox1.isChecked() && chkbox2.isChecked() && chkbox3.isChecked() && chkbox4.isChecked()
					&& chkbox5.isChecked() && chkbox6.isChecked() && chkbox7.isChecked() && chkbox8.isChecked()
					&& chkbox9.isChecked() && chkbox10.isChecked() && chkbox11.isChecked() && chkbox12.isChecked())
				chkbox0.setChecked(true);
			else
				chkbox0.setChecked(false);

			layout.setValue(
					(chkbox1.isChecked()
							? chkbox1.getLabel() + (chkbox2.isChecked() || chkbox3.isChecked() || chkbox4.isChecked()
									|| chkbox5.isChecked() || chkbox6.isChecked() || chkbox7.isChecked()
									|| chkbox8.isChecked() || chkbox9.isChecked() || chkbox10.isChecked()
									|| chkbox11.isChecked() || chkbox12.isChecked() ? ", " : "")
							: "")
							+ (chkbox2.isChecked()
									? chkbox2.getLabel() + (chkbox3.isChecked() || chkbox4.isChecked()
											|| chkbox5.isChecked() || chkbox6.isChecked() || chkbox7.isChecked()
											|| chkbox8.isChecked() || chkbox9.isChecked() || chkbox10.isChecked()
											|| chkbox11.isChecked() || chkbox12.isChecked() ? ", " : "")
									: "")
							+ (chkbox3.isChecked()
									? chkbox3.getLabel() + (chkbox4.isChecked() || chkbox5.isChecked()
											|| chkbox6.isChecked() || chkbox7.isChecked() || chkbox8.isChecked()
											|| chkbox9.isChecked() || chkbox10.isChecked() || chkbox11.isChecked()
											|| chkbox12.isChecked() ? ", " : "")
									: "")
							+ (chkbox4.isChecked() ? chkbox4.getLabel() + (chkbox5.isChecked() || chkbox6.isChecked()
									|| chkbox7.isChecked() || chkbox8.isChecked() || chkbox9.isChecked()
									|| chkbox10.isChecked() || chkbox11.isChecked() || chkbox12.isChecked() ? ", " : "")
									: "")
							+ (chkbox5.isChecked()
									? chkbox5.getLabel() + (chkbox6.isChecked() || chkbox7.isChecked()
											|| chkbox8.isChecked() || chkbox9.isChecked() || chkbox10.isChecked()
											|| chkbox11.isChecked() || chkbox12.isChecked() ? ", " : "")
									: "")
							+ (chkbox6.isChecked() ? chkbox6.getLabel() + (chkbox7.isChecked()
									|| chkbox8.isChecked() || chkbox9.isChecked() || chkbox10.isChecked()
									|| chkbox11.isChecked() || chkbox12.isChecked() ? ", " : "") : "")
							+ (chkbox7.isChecked()
									? chkbox7.getLabel()
											+ (chkbox8.isChecked() || chkbox9.isChecked() || chkbox10.isChecked()
													|| chkbox11.isChecked() || chkbox12.isChecked() ? ", " : "")
									: "")
							+ (chkbox8.isChecked()
									? chkbox8.getLabel() + (chkbox9.isChecked() || chkbox10.isChecked()
											|| chkbox11.isChecked() || chkbox12.isChecked() ? ", " : "")
									: "")
							+ (chkbox9.isChecked() ? chkbox9.getLabel()
									+ (chkbox10.isChecked() || chkbox11.isChecked() || chkbox12.isChecked() ? ", " : "")
									: "")
							+ (chkbox10.isChecked()
									? chkbox10.getLabel() + (chkbox11.isChecked() || chkbox12.isChecked() ? ", " : "")
									: "")
							+ (chkbox11.isChecked() ? chkbox11.getLabel() + (chkbox12.isChecked() ? ", " : "") : "")
							+ (chkbox12.isChecked() ? chkbox12.getLabel() : ""));
			layout.setStyle("font-weight: bold;");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doExport() {
		try {
			if (objList != null && objList.size() > 0) {
//				filcol2 = null;
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
//				if (mregion != null)
				cell.setCellValue("Laporan Planning Produk '" + AppData.getProductgroupLabel(productgroup) + "' Divisi "
						+ oUser.getMbranch().getBranchname().toString().trim());
//				else if (mbranch != null)
//					cell.setCellValue("Laporan Stock Cabang " + mbranch.getBranchname());
//				else {
//					if (branchlevel == 1)
//						cell.setCellValue("Laporan Stock Seluruh Wilayah");
//					else if (branchlevel == 2)
//						cell.setCellValue("Laporan Stock Seluruh Cabang");
//				}
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Tanggal: ");
				cell = row.createCell(1);
				cell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
				row = sheet.createRow(rownum++);
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();

//				filcol.add("No.");
//				if (chkbox1.isChecked())
//					filcol.add(chkbox1.getLabel());
//
//				if (chkbox2.isChecked())
//					filcol.add(chkbox2.getLabel());
//
//				if (chkbox3.isChecked())
//					filcol.add(chkbox3.getLabel());
//
//				if (chkbox4.isChecked())
//					filcol.add(chkbox4.getLabel());
//
//				if (chkbox5.isChecked())
//					filcol.add(chkbox5.getLabel());
//
//				if (chkbox6.isChecked())
//					filcol.add(chkbox6.getLabel());
//
//				if (chkbox7.isChecked())
//					filcol.add(chkbox7.getLabel());
//
//				if (chkbox8.isChecked())
//					filcol.add(chkbox8.getLabel());
//
//				if (chkbox9.isChecked())
//					filcol.add(chkbox9.getLabel());
//
//				if (chkbox10.isChecked())
//					filcol.add(chkbox10.getLabel());
//
//				if (chkbox11.isChecked())
//					filcol.add(chkbox11.getLabel());
//
//				if (chkbox12.isChecked())
//					filcol.add(chkbox12.getLabel());
//
//				Object[] oh = filcol.toArray();
				datamap.put(1,
						new Object[] { "No.", "Tipe Produk", "No. Planning", "Cabang/Divisi", "Tanggal Input",
								"Anggaran", "Total Item", "No. Memo", "Tanggal Memo", "Dokumen Planning",
								"Jumlsah Proses", "Status Planning", "Status Incoming" });

				no = 2;
				for (Treportplanf data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getProducttype(), data.getPlanno(), data.getBranchname(),
									datenormalFormatter.format(data.getEntrytime()),
									(data.getAnggaran() != null ? "Rp." + data.getAnggaran().toString() : "Rp.0"),
									(data.getTotalqty() != null ? data.getTotalqty().toString() : "0"),
									data.getMemono(), datenormalFormatter.format(data.getMemodate()),
									(data.getDocfileori() != null ? data.getDocfileori().trim() : "-"),
									(data.getTotalprocess() != null ? data.getTotalprocess().toString() : "0"),
									(data.getPstatus() != null ? AppData.getStatusLabel(data.getPstatus()) : "-"),
									(data.getIstatus() != null ? AppData.getStatusLabel(data.getIstatus()) : "-") });

					no++;
				}

//				for (Treportplanf datax : objList) {
//					filcol2.add(no - 1);
//					if (chkbox1.isChecked())
//						filcol2.add(datax.getMproducttype().getProducttype());
//
//					if (chkbox2.isChecked())
//						filcol2.add(datax.getTplan().getPlanno());
//
//					if (chkbox3.isChecked())
//						filcol2.add(datax.getMbranch().getBranchname());
//
//					if (chkbox4.isChecked())
//						filcol2.add(datenormalFormatter.format(datax.getTplan().getInputtime()));
//
//					if (chkbox5.isChecked())
//						filcol2.add(datax.getAnggaran() != null ? "Rp." + datax.getAnggaran().toString() : "Rp.0");
//
//					if (chkbox6.isChecked())
//						filcol2.add(datax.getTotalqty() != null ? datax.getTotalqty() : 0);
//
//					if (chkbox7.isChecked())
//						filcol2.add(datax.getMemono());
//
//					if (chkbox8.isChecked())
//						filcol2.add(datenormalFormatter.format(datax.getMemodate()));
//
//					if (chkbox9.isChecked())
//						filcol2.add(datax.getTplandoc() != null ? datax.getTplandoc().getDocfileori().trim() : "-");
//
//					if (chkbox10.isChecked())
//						filcol2.add(datax.getTotalprocess() != null ? datax.getTotalprocess() : 0);
//
//					if (chkbox11.isChecked())
//						filcol2.add(
//								datax.getTplan() != null ? AppData.getStatusLabel(datax.getTplan().getStatus()) : "-");
//
//					if (chkbox12.isChecked())
//						filcol2.add(datax.getStatus() != null ? AppData.getStatusLabel(datax.getStatus()) : "-");
//					no++;
//				}

//				Object[] ob = filcol2.toArray();
//				for (Treportplanf data : objList) {
//					datamap.put(no, ob);
//					no++;
//				}

//				for (Object datas : filcol2) {
//					System.out.println(datas.toString());
//				}
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
				String filename = "REPORT_PLANNING_DIVISI " + oUser.getMbranch().getBranchname() + "_"
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

	@NotifyChange("*")
	public void refreshModel() {
		try {
			totalrecord = 0;
			objList = oDao.listByFilterpr(filter, "tplan.inputtime, tplan.memodate");
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
			if (oUser.getMbranch().getBranchlevel() == 1) {
				cardTitle.setValue("Report Planning Produk " + AppData.getProductgroupLabel(productgroup));
			}

			filter = "tincoming.productgroup = '" + productgroup + "'";
			if (memono.trim() != "" || !"".equals(memono.trim()))
				filter += " and tplan.memono = '" + memono + "'";

			if (planno.trim() != "" || !"".equals(planno.trim()))
				filter += " and tplan.planno = '" + planno + "'";

//			filcol.add("No.");5
//			filcol2.add(collumnlist0);
			if (chkbox1.isChecked()) {
//				filcol.add(chkbox1.getLabel());
//				filcol2.add(collumnlist1);
				clproducttype.setVisible(true);
			} else if (!chkbox1.isChecked()) {
//				filcol.remove(chkbox1.getLabel());
//				filcol2.remove(collumnlist1);
				clproducttype.setVisible(false);
			}

			if (chkbox2.isChecked()) {
//				filcol.add(chkbox2.getLabel());
//				filcol2.add(collumnlist2);
				clnoplan.setVisible(true);
			} else if (!chkbox2.isChecked()) {
//				filcol.remove(chkbox2.getLabel());
//				filcol2.remove(collumnlist2);
				clnoplan.setVisible(false);
			}

			if (chkbox3.isChecked()) {
//				filcol.add(chkbox3.getLabel());
//				filcol2.add(collumnlist3);
				clbranch.setVisible(true);
			} else if (!chkbox3.isChecked()) {
//				filcol.remove(chkbox3.getLabel());
//				filcol2.remove(collumnlist3);
				clbranch.setVisible(false);
			}

			if (chkbox4.isChecked()) {
//				filcol.add(chkbox4.getLabel());
//				filcol2.add(collumnlist4);
				clinputdate.setVisible(true);
			} else if (!chkbox4.isChecked()) {
//				filcol.remove(chkbox4.getLabel());
//				filcol2.remove(collumnlist4);
				clinputdate.setVisible(false);
			}

			if (chkbox5.isChecked()) {
//				filcol.add(chkbox5.getLabel());
//				filcol2.add(collumnlist5);
				clanggaran.setVisible(true);
			} else if (!chkbox5.isChecked()) {
//				filcol.remove(chkbox5.getLabel());
//				filcol2.remove(collumnlist5);
				clanggaran.setVisible(false);
			}

			if (chkbox6.isChecked()) {
//				filcol.add(chkbox6.getLabel());
//				filcol2.add(collumnlist6);
				cltotalitem.setVisible(true);
			} else if (!chkbox6.isChecked()) {
//				filcol.remove(chkbox6.getLabel());
//				filcol2.remove(collumnlist6);
				cltotalitem.setVisible(false);
			}

			if (chkbox7.isChecked()) {
//				filcol.add(chkbox7.getLabel());
//				filcol2.add(collumnlist7);
				clnomemo.setVisible(true);
			} else if (!chkbox7.isChecked()) {
//				filcol.remove(chkbox7.getLabel());
//				filcol2.remove(collumnlist7);
				clnomemo.setVisible(false);
			}

			if (chkbox8.isChecked()) {
//				filcol.add(chkbox8.getLabel());
//				filcol2.add(collumnlist8);
				clmemodate.setVisible(true);
			} else if (!chkbox8.isChecked()) {
//				filcol.remove(chkbox8.getLabel());
//				filcol2.remove(collumnlist8);
				clmemodate.setVisible(false);
			}

			if (chkbox9.isChecked()) {
//				filcol.add(chkbox9.getLabel());
//				filcol2.add(collumnlist9);
				cldoc.setVisible(true);
			} else if (!chkbox9.isChecked()) {
//				filcol.remove(chkbox9.getLabel());
//				filcol2.remove(collumnlist9);
				cldoc.setVisible(false);
			}

			if (chkbox10.isChecked()) {
//				filcol.add(chkbox10.getLabel());
//				filcol2.add(collumnlist10);
				cltotalproses.setVisible(true);
			} else if (!chkbox10.isChecked()) {
//				filcol.remove(chkbox10.getLabel());
//				filcol2.remove(collumnlist10);
				cltotalproses.setVisible(false);
			}

			if (chkbox11.isChecked()) {
//				filcol.add(chkbox11.getLabel());
//				filcol2.add(collumnlist11);
				clstatusplan.setVisible(true);
			} else if (!chkbox11.isChecked()) {
//				filcol.remove(chkbox11.getLabel());
//				filcol2.remove(collumnlist11);
				clstatusplan.setVisible(false);
			}

			if (chkbox12.isChecked()) {
//				filcol.add(chkbox12.getLabel());
//				filcol2.add(collumnlist12);
				clstatusinc.setVisible(true);
			} else if (!chkbox12.isChecked()) {
//				filcol.remove(chkbox12.getLabel());
//				filcol2.remove(collumnlist12);
				clstatusinc.setVisible(false);
			}

			refreshModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		chkbox0.setChecked(true);
		chkbox1.setChecked(true);
		chkbox2.setChecked(true);
		chkbox3.setChecked(true);
		chkbox4.setChecked(true);
		chkbox5.setChecked(true);
		chkbox6.setChecked(true);
		chkbox7.setChecked(true);
		chkbox8.setChecked(true);
		chkbox9.setChecked(true);
		chkbox10.setChecked(true);
		chkbox11.setChecked(true);
		chkbox12.setChecked(true);
		layout.setValue(chkbox1.getLabel() + ", " + chkbox2.getLabel() + ", " + chkbox3.getLabel() + ", "
				+ chkbox4.getLabel() + ", " + chkbox5.getLabel() + ", " + chkbox6.getLabel() + ", " + chkbox7.getLabel()
				+ ", " + chkbox8.getLabel() + ", " + chkbox9.getLabel() + ", " + chkbox10.getLabel() + ", "
				+ chkbox11.getLabel() + ", " + chkbox12.getLabel());
		layout.setStyle("font-weight: bold;");
		memono = "";
		planno = "";
		doSearch();
	}

	public String getMemono() {
		return memono;
	}

	public void setMemono(String memono) {
		this.memono = memono;
	}

	public String getPlanno() {
		return planno;
	}

	public void setPlanno(String planno) {
		this.planno = planno;
	}

	public int getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(int totalrecord) {
		this.totalrecord = totalrecord;
	}

	public Checkbox getChkbox1() {
		return chkbox1;
	}

	public void setChkbox1(Checkbox chkbox1) {
		this.chkbox1 = chkbox1;
	}

	public Checkbox getChkbox2() {
		return chkbox2;
	}

	public void setChkbox2(Checkbox chkbox2) {
		this.chkbox2 = chkbox2;
	}

	public Checkbox getChkbox3() {
		return chkbox3;
	}

	public void setChkbox3(Checkbox chkbox3) {
		this.chkbox3 = chkbox3;
	}

	public Checkbox getChkbox4() {
		return chkbox4;
	}

	public void setChkbox4(Checkbox chkbox4) {
		this.chkbox4 = chkbox4;
	}

	public Checkbox getChkbox5() {
		return chkbox5;
	}

	public void setChkbox5(Checkbox chkbox5) {
		this.chkbox5 = chkbox5;
	}

	public Checkbox getChkbox6() {
		return chkbox6;
	}

	public void setChkbox6(Checkbox chkbox6) {
		this.chkbox6 = chkbox6;
	}

	public Checkbox getChkbox7() {
		return chkbox7;
	}

	public void setChkbox7(Checkbox chkbox7) {
		this.chkbox7 = chkbox7;
	}

	public Checkbox getChkbox8() {
		return chkbox8;
	}

	public void setChkbox8(Checkbox chkbox8) {
		this.chkbox8 = chkbox8;
	}

	public Checkbox getChkbox9() {
		return chkbox9;
	}

	public void setChkbox9(Checkbox chkbox9) {
		this.chkbox9 = chkbox9;
	}

	public Checkbox getChkbox10() {
		return chkbox10;
	}

	public void setChkbox10(Checkbox chkbox10) {
		this.chkbox10 = chkbox10;
	}

	public Checkbox getChkbox11() {
		return chkbox11;
	}

	public void setChkbox11(Checkbox chkbox11) {
		this.chkbox11 = chkbox11;
	}

	public Checkbox getChkbox12() {
		return chkbox12;
	}

	public void setChkbox12(Checkbox chkbox12) {
		this.chkbox12 = chkbox12;
	}

}