package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;
import org.zkoss.zk.ui.Executions;

import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.domain.Vsla;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;

public class ReportSlaVm {

	private TembossbranchDAO oDao = new TembossbranchDAO();

	private String filter;
	private Integer year;
	private Integer month1;
	private Integer month2;
	private List<Vsla> objList;
	private Map<String, Integer> map;
	
	@Wire
	private Combobox cbMonth1;
	@Wire
	private Combobox cbMonth2;
	@Wire
	private Grid grid;	

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		doReset();
		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item1 = new Comboitem();
			item1.setLabel(months[i]);
			item1.setValue(i + 1);
			cbMonth1.appendChild(item1);
			Comboitem item2 = new Comboitem();
			item2.setLabel(months[i]);
			item2.setValue(i + 1);
			cbMonth2.appendChild(item2);
		}		
	}

	@Command
	public void doSearch() {
		filter = "";
		try {
			if (year != null && month1 != null && month2 != null) {
				filter = "extract(year from orderdate) = " + year
						+ " and extract(month from orderdate) between " + month1
						+ " and " + month2;
				objList = oDao.getPerformansiBySLA(filter);								
				for (Vsla data: objList) {
					if (data.getSla() > 7)
						map.put(data.getId() + "8", data.getTotal());
					else map.put(data.getId() + String.valueOf(data.getSla()), data.getTotal());
				}
				
				grid.getRows().getChildren().clear();
				for (int i=month1; i<=month2; i++) {
					final Integer month = i;
					Integer h0 = map.get(i + "0");
					Integer h1 = map.get(i + "1");
					Integer h2 = map.get(i + "2");
					Integer h3 = map.get(i + "3");
					Integer h4 = map.get(i + "4");
					Integer h5 = map.get(i + "5");
					Integer h6 = map.get(i + "6");
					Integer h7 = map.get(i + "7");
					Integer h8 = map.get(i + "8");
					
					Row row = new Row();
					row.appendChild(new Label(String.valueOf(i)));
					row.appendChild(new Label(StringUtils.getMonthLabel(i)));
					
					if (h0 != null && h0 != 0) {
						A a = new A(String.valueOf(h0));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								String path = "/view/perso/persodatadetail.zul";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("year", year);
								map.put("month", month);
								map.put("sla", 0);
								
								Window win = (Window) Executions.createComponents(path, null, map);
								win.setWidth("90%");
								win.setClosable(true);
								win.doModal();		
							}
						});
						row.appendChild(a);
					} else row.appendChild(new Label("0"));
					if (h1 != null && h1 != 0) {
						A a = new A(String.valueOf(h1));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								String path = "/view/perso/persodatadetail.zul";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("year", year);
								map.put("month", month);
								map.put("sla", 1);
								
								Window win = (Window) Executions.createComponents(path, null, map);
								win.setWidth("90%");
								win.setClosable(true);
								win.doModal();		
							}
						});
						row.appendChild(a);
					} else row.appendChild(new Label("0"));
					if (h2 != null && h2 != 0) {
						A a = new A(String.valueOf(h2));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								String path = "/view/perso/persodatadetail.zul";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("year", year);
								map.put("month", month);
								map.put("sla", 2);
								
								Window win = (Window) Executions.createComponents(path, null, map);
								win.setClosable(true);
								win.doModal();		
							}
						});
						row.appendChild(a);
					} else row.appendChild(new Label("0"));
					if (h3 != null && h3 != 0) {
						A a = new A(String.valueOf(h3));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								String path = "/view/perso/persodatadetail.zul";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("year", year);
								map.put("month", month);
								map.put("sla", 3);
								
								Window win = (Window) Executions.createComponents(path, null, map);
								win.setWidth("90%");
								win.setClosable(true);
								win.doModal();		
							}
						});
						row.appendChild(a);
					} else row.appendChild(new Label("0"));
					if (h4 != null && h4 != 0) {
						A a = new A(String.valueOf(h4));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								String path = "/view/perso/persodatadetail.zul";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("year", year);
								map.put("month", month);
								map.put("sla", 4);
								
								Window win = (Window) Executions.createComponents(path, null, map);
								win.setWidth("90%");
								win.setClosable(true);
								win.doModal();		
							}
						});
						row.appendChild(a);
					} else row.appendChild(new Label("0"));
					if (h5 != null && h5 != 0) {
						A a = new A(String.valueOf(h5));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								String path = "/view/perso/persodatadetail.zul";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("year", year);
								map.put("month", month);
								map.put("sla", 5);
								
								Window win = (Window) Executions.createComponents(path, null, map);
								win.setWidth("90%");
								win.setClosable(true);
								win.doModal();		
							}
						});
						row.appendChild(a);
					} else row.appendChild(new Label("0"));
					if (h6 != null && h6 != 0) {
						A a = new A(String.valueOf(h6));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								String path = "/view/perso/persodatadetail.zul";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("year", year);
								map.put("month", month);
								map.put("sla", 6);
								
								Window win = (Window) Executions.createComponents(path, null, map);
								win.setClosable(true);
								win.doModal();		
							}
						});
						row.appendChild(a);
					} else row.appendChild(new Label("0"));
					if (h7 != null && h7 != 0) {
						A a = new A(String.valueOf(h7));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								String path = "/view/perso/persodatadetail.zul";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("year", year);
								map.put("month", month);
								map.put("sla", 7);
								
								Window win = (Window) Executions.createComponents(path, null, map);
								win.setWidth("90%");
								win.setClosable(true);
								win.doModal();		
							}
						});
						row.appendChild(a);
					} else row.appendChild(new Label("0"));
					if (h8 != null && h8 != 0) {
						A a = new A(String.valueOf(h8));
						a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								String path = "/view/perso/persodatadetail.zul";
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("year", year);
								map.put("month", month);
								map.put("sla", 8);
								
								Window win = (Window) Executions.createComponents(path, null, map);
								win.setWidth("90%");
								win.setClosable(true);
								win.doModal();		
							}
						});
						row.appendChild(a);
					} else row.appendChild(new Label("0"));
					
					grid.getRows().appendChild(row);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		filter = "";
		year = Calendar.getInstance().get(Calendar.YEAR);
		month1 = 1;
		month2 = Calendar.getInstance().get(Calendar.MONTH) + 1;
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		map = new HashMap<String, Integer>();
	}

	@Command
	public void doExport() {
		try {
			if (objList != null && objList.size() > 0) {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet();

				int rownum = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("Laporan Performansi SLA");
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Periode");
				cell = row.createCell(1);
				cell.setCellValue(StringUtils.getMonthLabel(month1) + " s/d "
						+ StringUtils.getMonthLabel(month2) + " " + year);
								
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Periode");
				cell = row.createCell(1);
				cell.setCellValue("H+0");
				cell = row.createCell(2);
				cell.setCellValue("H+1");
				cell = row.createCell(3);
				cell.setCellValue("H+2");
				cell = row.createCell(4);
				cell.setCellValue("H+3");
				cell = row.createCell(5);
				cell.setCellValue("H+4");
				cell = row.createCell(6);
				cell.setCellValue("H+5");
				cell = row.createCell(7);
				cell.setCellValue("H+6");
				cell = row.createCell(8);
				cell.setCellValue("H+7");
				cell = row.createCell(9);
				cell.setCellValue("H>7");
								
				for (int i=month1; i<=month2; i++) {
					row = sheet.createRow(rownum++);
					Integer h0 = map.get(i + "0");
					Integer h1 = map.get(i + "1");
					Integer h2 = map.get(i + "2");
					Integer h3 = map.get(i + "3");
					Integer h4 = map.get(i + "4");
					Integer h5 = map.get(i + "5");
					Integer h6 = map.get(i + "6");
					Integer h7 = map.get(i + "7");
					Integer h8 = map.get(i + "8");
					
					cell = row.createCell(0);
					cell.setCellValue(StringUtils.getMonthLabel(i));
					cell = row.createCell(1);
					cell.setCellValue(h0 != null ? h0 : 0);
					cell = row.createCell(2);
					cell.setCellValue(h1 != null ? h1 : 0);
					cell = row.createCell(3);
					cell.setCellValue(h2 != null ? h2 : 0);
					cell = row.createCell(4);
					cell.setCellValue(h3 != null ? h3 : 0);
					cell = row.createCell(5);
					cell.setCellValue(h4 != null ? h4 : 0);
					cell = row.createCell(6);
					cell.setCellValue(h5 != null ? h5 : 0);
					cell = row.createCell(7);
					cell.setCellValue(h6 != null ? h6 : 0);
					cell = row.createCell(8);
					cell.setCellValue(h7 != null ? h7 : 0);
					cell = row.createCell(9);
					cell.setCellValue(h8 != null ? h8 : 0);
				}

				String path = Executions
						.getCurrent()
						.getDesktop()
						.getWebApp()
						.getRealPath(
								AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
				String filename = "CIMS_PERFORMANSI_SLA_"
						+ new SimpleDateFormat("yyMMddHHmm").format(new Date())
						+ ".xlsx";
				FileOutputStream out = new FileOutputStream(new File(path + "/"
						+ filename));
				workbook.write(out);
				out.close();

				Filedownload
						.save(new File(path + "/" + filename),
								"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			} else {
				Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error",
					Messagebox.OK, Messagebox.ERROR);
		}
	}
	
	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth1() {
		return month1;
	}

	public void setMonth1(Integer month1) {
		this.month1 = month1;
	}

	public Integer getMonth2() {
		return month2;
	}

	public void setMonth2(Integer month2) {
		this.month2 = month2;
	}	

}

