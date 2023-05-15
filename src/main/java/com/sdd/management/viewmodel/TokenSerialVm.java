package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
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
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Tplan;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.domain.Ttokenserial;
import com.sdd.caption.model.TtokenitemListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;

public class TokenSerialVm {

	private TtokenitemListModel model;
	private TtokenitemDAO ttiDao = new TtokenitemDAO();
	

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private Integer year;
	private Integer month;

	@Wire
	private Groupbox gbBatchInfo;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Window winTokendata;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("isTotal") String isTotal, @ExecutionArgParam("isOutstanding") String isOutstanding,
			@ExecutionArgParam("isInjected") String isInjected,
			@ExecutionArgParam("isOutproduksi") String isOutproduksi) throws ParseException {
		Selectors.wireComponents(view, this, false);

		if (isTotal != null && isTotal.equals("Y")) {
			filter = "";
			winTokendata.setTitle("Daftar Total Nomor Item");
			System.out.println("TOTAL");
		} else if (isOutstanding != null && isOutstanding.equals("Y")) {
			filter = "ttokenitem.status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "'";
			winTokendata.setTitle("Daftar Nomor Serial Outstanding");
			System.out.println("OUTSTANDING");
		} else if (isInjected != null && isInjected.equals("Y")) {
			filter = "ttokenitem.status = '" + AppUtils.STATUS_SERIALNO_INJECTED + "'";
			winTokendata.setTitle("Daftar Nomor Serial Produksi");
			System.out.println("INJECTED");
		} else if (isOutproduksi != null && isOutproduksi.equals("Y")) {
			filter = "ttokenitem.status = '" + AppUtils.STATUS_SERIALNO_OUTPRODUKSI + "'";
			winTokendata.setTitle("Daftar Nomor Serial Keluar Produksi");
			System.out.println("OUT PRODUKSI");
		}

		doSearch();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Ttokenitem>() {

				@Override
				public void render(Row row, final Ttokenitem data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					row.getChildren().add(new Label(data.getItemno()));
//					row.getChildren().add(new Label(data.getMproducttype().getProducttype()));
//					if (data.getMbranch() != null)
//						row.getChildren().add(new Label(data.getMbranch().getBranchname()));
//					else
//						row.getChildren().add(new Label());
				}
			});
			
			String[] months = new DateFormatSymbols().getMonths();
		    for (int i = 0; i < months.length; i++) {
		      Comboitem item = new Comboitem();
		      item.setLabel(months[i]);
		      item.setValue(i+1);
		    }
			doReset();
			
		}
	}
	
	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Ttokenitem> listData = ttiDao.listNativeByFilter(filter, orderby);
				if (listData != null && listData.size() > 0) {
					XSSFWorkbook workbook = new XSSFWorkbook();
					XSSFSheet sheet = workbook.createSheet();

					int rownum = 0;
					int cellnum = 0;
					Integer no = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue(winTokendata.getTitle());
					row = sheet.createRow(rownum++);

					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1, new Object[] { "No", "Nomor Item"});
					no = 2;
					for (Ttokenitem data : listData) {
						datamap.put(no,
								new Object[] { no - 1, data.getItemno() });
						no++;
					//	total += data.getTotalqty();
					}
				//	datamap.put(no, new Object[] { "", "TOTAL", "", total });
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

					String path = Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
					String filename = "CAPTION_DAFTAR_NOMOR_SERIAL_" + new SimpleDateFormat("yyMMddHHmm").format(new Date())
							+ ".xlsx";
					FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
					workbook.write(out);
					out.close();

					Filedownload.save(new File(path + "/" + filename),
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				} else {
					Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "ttokenitempk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TtokenitemListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException {
		doSearch();
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

}
