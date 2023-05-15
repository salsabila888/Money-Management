package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TdeliveryDAO;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.model.TdeliveryListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ManifestDalkotListVm {
	
	private TdeliveryListModel model;
	
	private TdeliveryDAO tdeliveryDao = new TdeliveryDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer totaldata;	
	private Date startdate;
	private Date enddate;
	private Date tglterima;
	private Integer totalterima;
	private String percentterima;
	private Integer totalbelumterima;
	private String percentbelumterima;
	private String percentdata;
	private String filterterima;
	private String filterbelumterima;
	
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Grid gridSummary;
	@Wire
	private Groupbox groupbox;
	@Wire
	private Button excel;
	
	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);				
			}
		});
		
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tdelivery>() {

				@Override
				public void render(Row row, final Tdelivery data, int index)
						throws Exception {
					row.getChildren()
							.add(new Label(
									String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
											+ index + 1)));		
					//row.getChildren().add(new Label(data.getMcouriervendor().getVendorcode()));
					row.getChildren().add(new Label(data.getDlvid()));
					row.getChildren().add(new Label(data.getMbranch().getBranchname()));
					row.getChildren().add(
							new Label(data.getTdeliverycourier().getProcesstime() != null ? 
									dateLocalFormatter.format(data.getTdeliverycourier().getProcesstime()) : ""));
					/*row.getChildren().add(
							new Label(data.getTglterima() != null ? dateLocalFormatter.format(
									data.getTglterima()) : ""));
					row.getChildren().add(new Label(data.getPenerima() != null ? data.getPenerima() : ""));*/
					if (data.getStatus().equals(AppUtils.STATUS_DELIVERY_DELIVERY)) {
						row.getChildren().add(new Label("Belum Diterima"));
					} else if (data.getStatus().equals(AppUtils.STATUS_DELIVERY_DELIVERED)) {
						row.getChildren().add(new Label("Diterima"));
					} 
					//row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));					
				}

			});			
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doSearch() {
		if (startdate != null && enddate != null) {
			filter = "date(tdeliverycourier.processtime) between '" + dateFormatter.format(startdate) + "' and '" + 
					dateFormatter.format(enddate) + "' and mcouriervendor.vendorcode = '" + AppUtils.EXPEDISI_DLM + "'";
			filterterima = filter + " and tdelivery.status = '" + AppUtils.STATUS_DELIVERY_DELIVERED + "'"; 
			filterbelumterima = filter + " and tdelivery.status = '" + AppUtils.STATUS_DELIVERY_DELIVERY + "'"; 
			
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}
		
		/*if (tglterima == null) {
			filter += " and tdelivery.status = '" + AppUtils.STATUS_DELIVERY_INCOURIER + "'";
		} else {
			filter += " and tdelivery.status = '" + AppUtils.STATUS_DELIVERED + "'";
		}*/
		
		
	}

	@Command
	@NotifyChange("*")
	public void doReset() {	
		Calendar cal = Calendar.getInstance();
		enddate = new Date();
		cal.setTime(enddate);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		startdate = cal.getTime();
		needsPageUpdate = true;
		pageTotalSize = 0;
		paging.setTotalSize(pageTotalSize);
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();	
		groupbox.setVisible(false);
		excel.setDisabled(true);
		//doSearch();
	}

	@NotifyChange("*")
	public void refreshModel(int activePage) {
		try {
			orderby = "tdeliverypk desc";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new TdeliveryListModel(activePage, SysUtils.PAGESIZE, filter,
					orderby);
			if (needsPageUpdate) {
				pageTotalSize = model.getTotalSize(filter);
				needsPageUpdate = false;
			}
			
			paging.setTotalSize(pageTotalSize);
			grid.setModel(model);
			groupbox.setVisible(true);
			excel.setDisabled(false);
			
			totaldata = tdeliveryDao.sumData(filter);
			totalterima = tdeliveryDao.sumData(filterterima);
			totalbelumterima = tdeliveryDao.sumData(filterbelumterima);
			if (totaldata == 0) {
				percentterima = " (0.0%)";
				percentbelumterima = " (0.0%)";
				percentdata = " (0.0%)";
			} else {
				double terima = (new Double(totalterima)*100)/new Double(totaldata);
				double blmterima = (new Double(totalbelumterima)*100)/new Double(totaldata);
				percentterima = " (" + String.format("%.1f", terima) + "%)";
				percentbelumterima = " (" + String.format("%.1f", blmterima) + "%)";
				percentdata = " (100.0%)";
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@Command
	public void doExport() {
		try {
			List<Tdelivery> listData = tdeliveryDao.listFilter(filter, orderby);	
			if (listData != null && listData.size() > 0) {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet();

				int rownum = 0;
				int cellnum = 0;
				Integer no = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("Daftar Manifest Ekspedisi Dalam Kota");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Tanggal Data " + dateLocalFormatter.format(startdate) + " s/d "
						+ dateLocalFormatter.format(enddate));
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Total Data : " + totaldata + percentdata);
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Total Terima : " + totalterima + percentterima);
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Total Belum Terima : " + totalbelumterima + percentbelumterima);
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "No. Wakat", "Cabang",
						"Tanggal Kirim", "Status", "Total Data", "Penerima", "Tanggal Terima" });
				no = 2;
				for (Tdelivery data: listData) {								
					datamap.put(
							no,
							new Object[] {
									no - 1, data.getDlvid(), data.getMbranch().getBranchname(), 
									data.getTdeliverycourier().getProcesstime() != null ? dateLocalFormatter.format(data.getTdeliverycourier().getProcesstime()) : "",  
									data.getStatus().equals(AppUtils.STATUS_DELIVERY_DELIVERED) ? "Diterima" : "Belum Diterima", 
									data.getTotaldata(), data.getPenerima(), data.getTglterima() });
					no++;
				}
				datamap.put(no, new Object[] { "", "TOTAL", "", "", "", totaldata });
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
				String filename = "CAPTION_MANIFEST_DALAM_KOTA_"
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

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Date getStartdate() {
		return startdate;
	}

	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}

	public Date getEnddate() {
		return enddate;
	}

	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}

	public Date getTglterima() {
		return tglterima;
	}

	public void setTglterima(Date tglterima) {
		this.tglterima = tglterima;
	}

	public TdeliveryDAO getTdeliveryDao() {
		return tdeliveryDao;
	}

	public void setTdeliveryDao(TdeliveryDAO tdeliveryDao) {
		this.tdeliveryDao = tdeliveryDao;
	}

	public Integer getTotalterima() {
		return totalterima;
	}

	public void setTotalterima(Integer totalterima) {
		this.totalterima = totalterima;
	}

	public String getPercentterima() {
		return percentterima;
	}

	public void setPercentterima(String percentterima) {
		this.percentterima = percentterima;
	}

	public Integer getTotalbelumterima() {
		return totalbelumterima;
	}

	public void setTotalbelumterima(Integer totalbelumterima) {
		this.totalbelumterima = totalbelumterima;
	}

	public String getPercentbelumterima() {
		return percentbelumterima;
	}

	public void setPercentbelumterima(String percentbelumterima) {
		this.percentbelumterima = percentbelumterima;
	}

	public String getPercentdata() {
		return percentdata;
	}

	public void setPercentdata(String percentdata) {
		this.percentdata = percentdata;
	}	
}
