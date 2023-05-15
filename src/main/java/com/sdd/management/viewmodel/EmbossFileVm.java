package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.bind.annotation.ContextParam;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TembossfileDAO;
import com.sdd.caption.domain.Tembossfile;
import com.sdd.caption.model.TembossfileListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;

public class EmbossFileVm {
	
	private TembossfileListModel model;
	
	private TembossfileDAO oDao = new TembossfileDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String filename;
	private Date processdate;
	private Integer year;
	private Integer month;
	private String productgroup;
	private String productgroupname;
	private String title;
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg) 
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		productgroup = arg;
		productgroupname = AppData.getProductgroupLabel(productgroup);
		
		title = "Daftar Order " + productgroupname;
		
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
		
		grid.setRowRenderer(new RowRenderer<Tembossfile>() {

			@Override
			public void render(Row row, final Tembossfile data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
										+ index + 1)));
				A a = new A(data.getEmbossid());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {						
						String path = "/view/emboss/embossproduct.zul";
						Map<String, Object> map = new HashMap<>();
						map.put("obj", data);
						
						Window win = (Window) Executions
								.createComponents(
										path,
										null, map);
						win.setWidth("90%");
						win.setClosable(true);
						win.doModal();	
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(datetimeLocalFormatter.format(data.getUploadtime())));
				row.getChildren().add(new Label(data.getFilename()));
				row.getChildren().add(new Label(data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));
				row.getChildren().add(new Label(data.getMemo()));							
			}

		});
		
		String[] months = new DateFormatSymbols().getMonths();
	    for (int i = 0; i < months.length; i++) {
	      Comboitem item = new Comboitem();
	      item.setLabel(months[i]);
	      item.setValue(i+1);
	      cbMonth.appendChild(item);
	    }
	    
		doReset();
	}

	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Tembossfile> listData = oDao.listByFilter(filter, orderby);
				if (listData != null && listData.size() > 0) {
					XSSFWorkbook workbook = new XSSFWorkbook();
					XSSFSheet sheet = workbook.createSheet();

					int rownum = 0;
					int cellnum = 0;
					Integer no = 0;
					Integer total = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("Daftar File Emboss");
					rownum++;
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Periode");
					cell = row.createCell(1);
					cell.setCellValue(StringUtils.getMonthLabel(month) + " " + year);
					row = sheet.createRow(rownum++);
					
					/*row = sheet.createRow(rownum++);
					cell = row.createCell(0);				
					rownum++;*/
					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1, new Object[] { "No", "File Id", "Nama File", "Total Data", "Memo" });
					no = 2;
					for (Tembossfile data: listData) {								
						datamap.put(
								no,
								new Object[] {
										no - 1, data.getEmbossid(), data.getFilename(), data.getTotaldata(), data.getMemo()});
						no++;
						total += data.getTotaldata();
					}
					datamap.put(
							no,
							new Object[] {
									"", "TOTAL", 
									"", total });
					Set<Integer> keyset = datamap.keySet();
					for (Integer key : keyset) {
						row = sheet.createRow(rownum++);
						Object[] objArr = datamap.get(key);
						cellnum = 0;
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);
							else if (obj instanceof Double)
								cell.setCellValue((Double) obj);
						}
					}									

					String path = Executions
							.getCurrent()
							.getDesktop()
							.getWebApp()
							.getRealPath(
									AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
					String filename = "CAPTION_EMBOSSFILE_"
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
			}			
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(),
					Messagebox.OK, Messagebox.ERROR);
		}
	}
	
	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (year != null && month != null) {
			filter = "extract(year from uploadtime) = " + year + " and "
					+ "extract(month from uploadtime) = " + month;
			if (processdate != null)
				filter += " and date(uploadtime) = '" + dateFormatter.format(processdate) + "'";
			if (filename != null && filename.trim().length() > 0)
				filter += " and filename like '%" + filename.trim().toUpperCase() + "%'";
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}		
	}

	@Command
	@NotifyChange("*")
	public void doReset() {		
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tembossfilepk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TembossfileListModel(activePage, SysUtils.PAGESIZE, filter,
				orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Date getProcessdate() {
		return processdate;
	}

	public void setProcessdate(Date processdate) {
		this.processdate = processdate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

}
