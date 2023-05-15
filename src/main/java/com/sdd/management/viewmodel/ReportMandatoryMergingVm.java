/**
 * 
 */
package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TproductmmDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Tproductmm;
import com.sdd.caption.model.TproductmmListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportMandatoryMergingVm {
	
	private TproductmmDAO oDao = new TproductmmDAO();			
	
	private TproductmmListModel model;	
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;	
	private String filter;
	private String orderby;	
	private String productgroup;
	private String productorg;
	private Morg morg;
	private Integer totalmatch;
	private Integer totalunmatch;
	private Integer totalos;

	private Date trxdate;
	private Map<String, String> mapOrg;

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	@Wire
	private Paging paging;	
	@Wire
	private Grid grid;
	@Wire
	private Groupbox gbSearch;
	@Wire
	private Combobox cbProductorg;	
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("orderdate") Date orderdate) {		
		Selectors.wireComponents(view, this, false);		
		orderby = "orderdate, org, productcode";
		try {
			mapOrg = AppData.getOrgmap();
			if (orderdate != null) {
				trxdate = orderdate;
				gbSearch.setVisible(false);
			}
			doReset();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {				
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);				
			}		
		});		
		
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tproductmm>() {
				@Override
				public void render(Row row, Tproductmm data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));		
					row.getChildren().add(new Label(mapOrg.get(data.getOrg())));
					row.getChildren().add(new Label(data.getProducttype()));
					row.getChildren().add(new Label(data.getProductcode()));
					row.getChildren().add(new Label(data.getProductname()));
					row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalmerge())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalos())));
					row.getChildren().add(new Label(data.getRekontime() != null ? datetimeLocalFormatter.format(data.getRekontime()) : ""));
					
				}
			});
		}				
	}	

	@NotifyChange("*")
	public void refreshModel(int activePage) {		
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TproductmmListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {					
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
			try {
				totalmatch = oDao.pageCount("orderdate = '" + dateFormatter.format(trxdate) + "' and ismatch = 'Y'");
				totalunmatch = oDao.pageCount("orderdate = '" + dateFormatter.format(trxdate) + "' and ismatch = 'N'");
				totalos = oDao.pageCount("orderdate = '" + dateFormatter.format(trxdate) + "' and ismatch = ''");
			} catch (Exception e) {
				e.printStackTrace();			
			}
		}
		paging.setTotalSize(pageTotalSize);	
		grid.setModel(model);		
	}
	
	@Command
	@NotifyChange("*")
	public void doSearch() {
		if (trxdate != null) {
			filter = "orderdate = '" + dateFormatter.format(trxdate) + "'";
			if (morg != null)
				filter += " and org = '" + morg.getOrg() + "'";			
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}		
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		morg = null;
		cbProductorg.setValue(null);
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		doSearch();			
	}	

	@Command
	public void doExport() {
		try {
			if (filter != null && !filter.equals("")) {
				List<Tproductmm> oList = new TproductmmDAO().listByFilter(filter, orderby);
				if (oList != null && oList.size() > 0) {
					XSSFWorkbook workbook = new XSSFWorkbook();
					XSSFSheet sheet = workbook.createSheet();
					
					int rownum = 0;
					int cellnum = 0;
					Integer no = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("Laporan Mandatory Merging");
					rownum++;
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Tanggal Data");	
					cell = row.createCell(1);
					cell.setCellValue(dateFormatter.format(trxdate));
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Org");
					cell = row.createCell(1);
					cell.setCellValue(morg != null ? mapOrg.get(morg.getOrg()) : "ALL");
					
					rownum++;
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Total Product Match");	
					cell = row.createCell(1);
					cell.setCellValue(totalmatch);
					
					cell = row.createCell(2);
					cell.setCellValue("Total Product Unmatch");	
					cell = row.createCell(3);
					cell.setCellValue(totalunmatch);
					
					cell = row.createCell(4);
					cell.setCellValue("Total Product Unreconcile");	
					cell = row.createCell(5);
					cell.setCellValue(totalos);
					rownum++;
					
					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1, new Object[] {"No", "Org", "Tipe Produk", "Kode Produk", "Jenis Produk", "Tanggal Data", "Jumlah Order", "Jumlah Merging", "Selisih", "Waktu Rekon"});
					no = 2;
					for (Tproductmm data: oList) {
						datamap.put(no, new Object[] {no-1, mapOrg.get(data.getOrg()), data.getProducttype(), data.getProductcode(),  data.getProductname(), data.getOrderdate() != null ? dateLocalFormatter.format(data.getOrderdate()) : "",
								data.getTotaldata(), data.getTotalmerge(), data.getTotalos(), data.getRekontime() != null ? datetimeLocalFormatter.format(data.getRekontime()) : ""});
						no++;
					}
					Set<Integer> keyset = datamap.keySet();			
					for (Integer key : keyset) {
						row = sheet.createRow(rownum++);
					    Object [] objArr = datamap.get(key);
					    cellnum = 0;
					    for (Object obj : objArr) {
					       cell = row.createCell(cellnum++);
					       if(obj instanceof String)
					            cell.setCellValue((String)obj);
					       else if(obj instanceof Integer)
					            cell.setCellValue((Integer)obj);				       
					    }
					}
					
					String path = Executions.getCurrent().getDesktop().getWebApp()
			    			.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
					String filename = "CAPTION_MANDATORYMERGING_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				    FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
				    workbook.write(out);
				    out.close();
					
				    Filedownload.save(new File(path + "/" + filename), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");			    
				} else {
					Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Silahkan lengkapi field pencarian", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}				
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
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

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Date getTrxdate() {
		return trxdate;
	}

	public void setTrxdate(Date trxdate) {
		this.trxdate = trxdate;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getProductorg() {
		return productorg;
	}

	public void setProductorg(String productorg) {
		this.productorg = productorg;
	}

	public Morg getMorg() {
		return morg;
	}

	public void setMorg(Morg morg) {
		this.morg = morg;
	}

	public Integer getTotalmatch() {
		return totalmatch;
	}

	public void setTotalmatch(Integer totalmatch) {
		this.totalmatch = totalmatch;
	}

	public Integer getTotalunmatch() {
		return totalunmatch;
	}

	public void setTotalunmatch(Integer totalunmatch) {
		this.totalunmatch = totalunmatch;
	}

	public Integer getTotalos() {
		return totalos;
	}

	public void setTotalos(Integer totalos) {
		this.totalos = totalos;
	}	
		
}
