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
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.model.MproducttypeListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportStockEstimateVm {

	private MproducttypeListModel model;
	private MproducttypeDAO oDao = new MproducttypeDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private String productgroup;
	private String producttype;	
	private Morg morg;

	private String total;
	private String datereport;	
	private Map<String, String> mapOrg;
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MMM-yy");

	@Wire
	private Button btnSave;
	@Wire
	private Button btnCancel;
	@Wire
	private Button btnDelete;
	@Wire
	private Combobox cbProductGroup;
	@Wire
	private Combobox cbProductorg;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg) {
		Selectors.wireComponents(view, this, false);
		
		try {
			productgroup = arg;
			datereport = new SimpleDateFormat("dd MMM yyyy").format(new Date());
			needsPageUpdate = true;
			doReset();
			mapOrg = AppData.getOrgmap();
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
	
		grid.setRowRenderer(new RowRenderer<Mproducttype>() {

			@Override
			public void render(Row row, Mproducttype data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				row.getChildren().add(new Label(mapOrg.get(data.getProductorg())));
				row.getChildren().add(new Label(data.getProducttype()));
				row.getChildren().add(new Label(data.getLaststock() != null ? NumberFormat.getInstance().format(data.getLaststock()) : "0"));
				row.getChildren().add(new Label(data.getStockmin() != null ? NumberFormat.getInstance().format(data.getStockmin()) : "0"));
				row.getChildren().add(new Label(data.getVelocity() != null ? String.valueOf(data.getVelocity()) : ""));
				row.getChildren().add(new Label(data.getEstdays() != null ? String.valueOf(data.getEstdays()) : ""));
				row.getChildren().add(new Label(data.getEstdate() != null ? datelocalFormatter.format(data.getEstdate()) : ""));
			}
		});		
	}
	
	@NotifyChange({"pageTotalSize", "total"})
	public void refreshModel(int activePage) {
		try {
			orderby = "productorg, productgroupcode, producttype";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new MproducttypeListModel(activePage, SysUtils.PAGESIZE, filter,
					orderby);
			if (needsPageUpdate) {
				pageTotalSize = model.getTotalSize(filter);
				needsPageUpdate = false;
			}
			paging.setTotalSize(pageTotalSize);
			grid.setModel(model);
			
			total = NumberFormat.getInstance().format(oDao.getSumm(filter));
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Command
	@NotifyChange({"pageTotalSize", "total"})
	public void doSearch() {
		filter = "isestcount = 'Y'";		
		
		if (productgroup != null) {
			filter += " and productgroupcode = '" + productgroup + "'";
		}
		
		if (morg != null) {
			filter += " and productorg = '" + morg.getOrg() + "'";
		}		
		if (producttype != null && producttype.trim().length() > 0) {
			filter += " and producttype like '%" + producttype.trim().toUpperCase() + "%'";
		}
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {		
		morg = null;
		cbProductorg.setValue(null);
		doSearch();
	}
	
	@Command
	public void doExport() {
		try {
			List<Mproducttype> objList = oDao.listByFilter(filter, orderby);
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet();

			int rownum = 0;
			int cellnum = 0;
			Integer no = 0;
			org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
			Cell cell = row.createCell(0);
			cell.setCellValue("Laporan Estimasi Stock");
			rownum++;
			row = sheet.createRow(rownum++);
			cell = row.createCell(0);
			cell.setCellValue("Tanggal");
			cell = row.createCell(1);
			cell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
			
			row = sheet.createRow(rownum++);
			row = sheet.createRow(rownum++);
			cell = row.createCell(0);
			cell.setCellValue("Grup Produk");
			cell = row.createCell(1);
			cell.setCellValue(productgroup != null && productgroup.trim().length() > 0 ? AppData.getProductgroupLabel(productgroup) : "ALL");
			row = sheet.createRow(rownum++);
			cell = row.createCell(0);			
			
			Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
			datamap.put(1, new Object[] { "No", "Org", "Tipe Kartu",
					"Stock", "Stock Pagu", "Kecepatan/Hari", "Estimasi Habis (Hari)", "Estimasi Habis (Tgl)" });
			no = 2;
			for (Mproducttype data: objList) {								
				datamap.put(
						no,
						new Object[] {
								no - 1,
								mapOrg.get(data.getProductorg()), data.getProducttype(),
								data.getLaststock(), data.getStockmin(), data.getVelocity() != null ? data.getVelocity() : 0, 
								data.getEstdays() != null ? data.getEstdays() : 0, data.getEstdate() != null ? datelocalFormatter.format(data.getEstdate()) : "" });
				no++;
			}
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
			String filename = "CIMS_STOCKESTIMATE_"
					+ new SimpleDateFormat("yyMMddHHmm").format(new Date())
					+ ".xlsx";
			FileOutputStream out = new FileOutputStream(new File(path + "/"
					+ filename));
			workbook.write(out);
			out.close();

			Filedownload
					.save(new File(path + "/" + filename),
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error",
					Messagebox.OK, Messagebox.ERROR);
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

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getDatereport() {
		return datereport;
	}

	public void setDatereport(String datereport) {
		this.datereport = datereport;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}
		
	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public Morg getMorg() {
		return morg;
	}

	public void setMorg(Morg morg) {
		this.morg = morg;
	}	

}
