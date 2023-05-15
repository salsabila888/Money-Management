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
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TorderdataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mregion;
import com.sdd.caption.domain.Vreportdaily;
import com.sdd.caption.model.VorderreportdailyListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class ReportMonthlyVm {
	
	private VorderreportdailyListModel model;
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	
	private MproductDAO mproductDao = new MproductDAO();
	private TembossdataDAO oDao = new TembossdataDAO();
	private String filter;
	private String productgroup;
	private String productorg;
	private Integer year;
	private Integer month;
	private Integer totalorder;
	private Integer totalprod;
	private Integer totalpending;
	private Integer totaldlv;
	private Integer totalos;
	private int gridno;
	private String producttype;
	private String productcode;
	private String productname;
	private Mregion mregion;
	private Mbranch mbranch;
	private Morg morg;
	private ListModelList<Mbranch> mbranchmodel;
	private List<Mproduct> listProduct;
	private List<Vreportdaily> listData;
	private Map<String, Vreportdaily> map;
	
	private Map<String, String> mapOrg;
	
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Combobox cbBranch;
	@Wire
	private Combobox cbMonth;
	@Wire
	private Combobox cbProductorg;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		try {
			mapOrg = AppData.getOrgmap();
			doReset();
			setMonthList();
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
		
		grid.setRowRenderer(new RowRenderer<Vreportdaily>() {

			@Override
			public void render(Row row, final Vreportdaily data, int index)
					throws Exception {
				row.getChildren()
				.add(new Label(
						String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
								+ index + 1)));
				row.getChildren().add(new Label(mapOrg.get(data.getProductorg())));
				row.getChildren().add(new Label(data.getProducttype()));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getProductname()));
				row.getChildren().add(new Label(data != null ? NumberFormat.getInstance().format(data.getTotal()) : "0"));
				row.getChildren().add(new Label(data != null ? NumberFormat.getInstance().format(data.getPerso()) : "0"));
				row.getChildren().add(new Label(data != null ? NumberFormat.getInstance().format(data.getPending()) : "0"));
				row.getChildren().add(new Label(data != null ? NumberFormat.getInstance().format(data.getDelivery()) : "0"));	
				row.getChildren().add(new Label(data != null ? NumberFormat.getInstance().format(data.getTotal() - data.getDelivery()) : "0"));
			}
		});	
	}
	
	@NotifyChange({"pageTotalSize", "total"})
	public void refreshModel(int activePage) {
		try {
			orderby = "";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new VorderreportdailyListModel(activePage, SysUtils.PAGESIZE, filter,
					orderby);
			if (needsPageUpdate) {
				pageTotalSize = model.getTotalSize(filter);
				needsPageUpdate = false;
			}
			paging.setTotalSize(pageTotalSize);
			grid.setModel(model);			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private void setMonthList() {
		String[] months = new DateFormatSymbols().getMonths();
	    for (int i = 0; i < months.length; i++) {
	      Comboitem item1 = new Comboitem();
	      item1.setLabel(months[i]);
	      item1.setValue(i+1);
	      cbMonth.appendChild(item1);	      
	    }
	}
	
	@Command
	@NotifyChange("mbranchmodel")
	public void doBranchLoad(@BindingParam("item") Mregion item) {
		if (item != null) {
			try {
				mbranchmodel = new ListModelList<>(AppData.getMbranch("mregion.mregionpk = " + item.getMregionpk()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doSearch() {
		if (year != null && month != null) {
			try {				
				filter = "extract(year from tembossbranch.orderdate) = " + year + " and "
						+ "extract(month from tembossbranch.orderdate) = " + month;
				if (productgroup != null && productgroup.trim().length() > 0) {
					filter += " and torderdata.productgroup = '" + productgroup + "'";
				}
				if (morg != null) {
					filter += " and mproducttype.productorg = '" + morg.getOrg() + "'";
				}
				if (producttype != null && producttype.trim().length() > 0) {
					filter += " and mproducttype.producttype like '%" + producttype.trim().toUpperCase() + "%'";
				}
				if (productcode != null && productcode.trim().length() > 0) {
					filter += " and mproduct.productcode like '%" + productcode.trim().toUpperCase() + "%'";
				}
				if (productname != null && productname.trim().length() > 0) {
					filter += " and mproduct.productname like '%" + productname.trim().toUpperCase() + "%'";
				}
				if (mregion != null) {
					filter += " and mregionfk = " + mregion.getMregionpk();
				}
				if (mbranch != null) {
					filter += " and mbranchfk = " + mbranch.getMbranchpk();
				}
				
				/*totalorder = 0;
				totalprod = 0;
				totalpending = 0;
				totaldlv = 0;
				totalos = 0;
				map = new HashMap<String, Vreportdaily>();
				listData = torderdataDao.getReportOrder(filter);				
				for (Vreportdaily data: listData) {
					map.put(String.valueOf(data.getId()), data);
				}				
				listProduct = mproductDao.listByFilter(filterproduct, "mproducttype.productgroupcode, mproducttype.productorg, productname");
				gridSetup();*/
				
				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
				
				listData = oDao.listReportDaily(filter);
				
				for (Vreportdaily data: listData) {
					totalorder += data.getTotal();
					totalprod += data.getPerso();
					totalpending += data.getPending();
					totaldlv += data.getDelivery();
					totalos = data.getTotal() - data.getDelivery();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
	
	/*@NotifyChange("*")
	private void gridSetup() {
		try {
			gridno = 1;
			for (Mproduct mproduct: listProduct) {
				Row row = new Row();
				row.appendChild(new Label(String.valueOf(gridno++)));
				row.appendChild(new Label(mapOrg.get(mproduct.getMproducttype().getProductorg())));
				row.appendChild(new Label(mproduct.getMproducttype().getProducttype()));
				row.appendChild(new Label(mproduct.getProductcode()));
				row.appendChild(new Label(mproduct.getProductname()));
				//row.appendChild(new Label(mproduct.getMproducttype().getLaststock() != null ? NumberFormat.getInstance().format(mproduct.getMproducttype().getLaststock()) : "0"));
				
				Vreportdaily data = map.get(String.valueOf(mproduct.getMproductpk()));
				row.appendChild(new Label(data != null ? NumberFormat.getInstance().format(data.getTotal()) : "0"));
				row.appendChild(new Label(data != null ? NumberFormat.getInstance().format(data.getPerso()) : "0"));
				row.appendChild(new Label(data != null ? NumberFormat.getInstance().format(data.getPending()) : "0"));
				row.appendChild(new Label(data != null ? NumberFormat.getInstance().format(data.getDelivery()) : "0"));
				row.appendChild(new Label(data != null ? NumberFormat.getInstance().format(data.getTotal() - data.getDelivery()) : "0"));	
				grid.getRows().appendChild(row);
				
				if (data != null) {
					totalorder += data.getTotal();
					totalprod += data.getPerso();
					totalpending += data.getPending();
					totaldlv += data.getDelivery();
					totalos += data.getTotal() - data.getDelivery();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		//gridno = 1;
		mregion = null;				
		mbranch = null;		
		year = Calendar.getInstance().get(Calendar.YEAR);		
		month = null;
		totalorder = 0;
		totalprod = 0;
		totalpending = 0;
		totaldlv = 0;
		totalos = 0;
		//map = new HashMap<String, Vreportdaily>();		
		cbBranch.setValue(null);		
		morg = null;
		cbProductorg.setValue(null);
		//grid.getRows().getChildren().clear();
	}
	
	@Command
	public void doExport() {
		try {
			
			if (listData != null && listData.size() > 0) {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet();

				int rownum = 0;
				int cellnum = 0;
				Integer no = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("Laporan Bulanan Produksi Kartu");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Periode " + DateFormatSymbols.getInstance().getMonths()[month-1] + " " +  year);
				rownum++;
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Grup Produk");
				cell = row.createCell(1);
				cell.setCellValue(productgroup != null && productgroup.trim().length() > 0 ? AppData.getProductgroupLabel(productgroup) : "ALL");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Org");
				cell = row.createCell(1);
				cell.setCellValue(morg != null ? mapOrg.get(morg.getOrg()) : "ALL");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Wilayah");
				cell = row.createCell(1);
				cell.setCellValue(mregion != null ? mregion.getRegionname() : "ALL");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue("Cabang");
				cell = row.createCell(1);
				cell.setCellValue(mbranch != null ? mbranch.getBranchname() : "ALL");
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);				
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(1, new Object[] { "No", "Org", "Tipe Produk", "Kode Produk",
						"Jenis Produk", "Order", "Produksi", "Pending", "Delivery",
						"Outstanding" });
				no = 2;
				for (Vreportdaily data: listData) {								
					datamap.put(
							no,
							new Object[] {
									no - 1, mapOrg.get(data.getProductorg()), 
									data.getProducttype(),
									data.getProductcode(), data.getProductname(), 
									data != null ? data.getTotal() : 0, data != null ? data.getPerso() : 0, data != null ? data.getPending() : 0,
									data != null ? data.getDelivery() : 0, data != null ? data.getTotal() - data.getDelivery() : 0 });
					no++;
				}
				datamap.put(
						no,
						new Object[] {
								"", "TOTAL", 
								"", "", "", 
								totalorder, totalprod, totalpending, totaldlv, totalos });
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
				String filename = "CAPTION_MONTHLY_"
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
	
	public ListModelList<Morg> getMorgmodel() {
		ListModelList<Morg> lm = null;
		try {
			lm = new ListModelList<Morg>(AppData.getMorg());
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
	
	public ListModelList<Mbranch> getMbranchmodel() {
		return mbranchmodel;
	}

	public void setMbranchmodel(ListModelList<Mbranch> mbranchmodel) {
		this.mbranchmodel = mbranchmodel;
	}

	public Mregion getMregion() {
		return mregion;
	}

	public void setMregion(Mregion mregion) {
		this.mregion = mregion;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
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

	public Integer getTotalorder() {
		return totalorder;
	}

	public void setTotalorder(Integer totalorder) {
		this.totalorder = totalorder;
	}

	public Integer getTotalprod() {
		return totalprod;
	}

	public void setTotalprod(Integer totalprod) {
		this.totalprod = totalprod;
	}

	public Integer getTotaldlv() {
		return totaldlv;
	}

	public void setTotaldlv(Integer totaldlv) {
		this.totaldlv = totaldlv;
	}

	public Integer getTotalos() {
		return totalos;
	}

	public void setTotalos(Integer totalos) {
		this.totalos = totalos;
	}

	public Integer getTotalpending() {
		return totalpending;
	}

	public void setTotalpending(Integer totalpending) {
		this.totalpending = totalpending;
	}

	public String getProducttype() {
		return producttype;
	}

	public String getProductcode() {
		return productcode;
	}

	public String getProductname() {
		return productname;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public void setProductname(String productname) {
		this.productname = productname;
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

	public int getPageStartNumber() {
		return pageStartNumber;
	}

	public void setPageStartNumber(int pageStartNumber) {
		this.pageStartNumber = pageStartNumber;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public boolean isNeedsPageUpdate() {
		return needsPageUpdate;
	}

	public void setNeedsPageUpdate(boolean needsPageUpdate) {
		this.needsPageUpdate = needsPageUpdate;
	}

	public String getOrderby() {
		return orderby;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

}
