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
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.domain.Vproductorgstock;
import com.sdd.caption.domain.Vproducttypestock;
import com.sdd.caption.domain.Vproducttypestocksum;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class ReportStockProductVm {

	private MproducttypeDAO oDao = new MproducttypeDAO();

	private Vproductorgstock obj;
	private Vproducttypestock objForm;
	private String productgroup;
	private String producttype;

	private Integer total;
	private String datereport;	
	private Vproducttypestocksum objtotal = new Vproducttypestocksum();
	private List<Vproducttypestock> objList = new ArrayList<>();
	private Map<String, String> mapOrg;

	@Wire
	private Window winReportstockproduct;
	@Wire
	private Listbox listbox;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Vproductorgstock obj) {
		Selectors.wireComponents(view, this, false);
		this.obj = obj;
		try {
			mapOrg = AppData.getOrgmap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		datereport = new SimpleDateFormat("dd MMM yyyy").format(new Date());				
		doReset();
		
		listbox.setItemRenderer(new ListitemRenderer<Vproducttypestock>() {

			@Override
			public void render(Listitem item, Vproducttypestock data, int index) throws Exception {
				Listcell cell = new Listcell(String.valueOf(index + 1));
				item.appendChild(cell);	
				cell = new Listcell(data.getProducttype());
				item.appendChild(cell);
				cell = new Listcell(data.getTotalincoming() != null ? NumberFormat.getInstance().format(data.getTotalincoming()) : "0");
				item.appendChild(cell);
				cell = new Listcell(data.getTotaloutgoing() != null ? NumberFormat.getInstance().format(data.getTotaloutgoing()) : "0");
				item.appendChild(cell);
				cell = new Listcell(data.getTotalstock() != null ? NumberFormat.getInstance().format(data.getTotalstock()) : "0");
				item.appendChild(cell);						
			}
			
		});		
		
		listbox.addEventListener(Events.ON_SELECT, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				Map<String, Object> map = new HashMap<>();
				map.put("obj", objForm);

				Window win = (Window) Executions
						.createComponents(
								"/view/report/reportstockhistory.zul",
								null, map);
				win.setWidth("90%");
				win.setClosable(true);
				win.doModal();
				Listitem item = ((Listbox) event.getTarget()).getSelectedItem();
				item.setSelected(false);
			}
		});
		
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			//total = oDao.getSumm(filter);
			objList = oDao.listProducttypestock(AppUtils.PRODUCTGROUP_CARD, obj.getProductorg(), producttype == null ? "" : producttype.trim().toUpperCase());
			objtotal = oDao.getSumProducttypestock(AppUtils.PRODUCTGROUP_CARD, obj.getProductorg(), producttype == null ? "" : producttype.trim().toUpperCase());
			listbox.setModel(new ListModelList<>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		refreshModel();
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event( "onClose", winReportstockproduct, null);
		Events.postEvent(closeEvent);
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		doSearch();
	}
	
	@Command
	public void doExport() {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet();

			int rownum = 0;
			int cellnum = 0;
			Integer no = 0;
			org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
			Cell cell = row.createCell(0);
			cell.setCellValue("Laporan Stock Produk");
			row = sheet.createRow(rownum++);
			cell = row.createCell(0);
			cell.setCellValue("Tanggal");
			cell = row.createCell(1);
			cell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
			
			row = sheet.createRow(rownum++);
			row = sheet.createRow(rownum++);
			cell = row.createCell(0);
			cell.setCellValue("Org");
			cell = row.createCell(1);
			cell.setCellValue(mapOrg.get(obj.getProductorg()));			
			
			Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
			datamap.put(1, new Object[] { "No", "Tipe Kartu", "Total Incoming", "Total Outgoing",
					"Total Stock" });
			no = 2;
			for (Vproducttypestock data: objList) {								
				datamap.put(
						no,
						new Object[] {
								no - 1,
								data.getProducttype(),
								data.getTotalincoming(), data.getTotaloutgoing(), data.getTotalstock() });
				no++;
			}
			datamap.put(
					no,
					new Object[] {
							"",
							"TOTAL", objtotal.getTotalincoming(), objtotal.getTotaloutgoing(), objtotal.getTotalstock() });
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
			String filename = "CIMS_STOCKPRODUCT_"
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
	
	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
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

	public Vproducttypestock getObjForm() {
		return objForm;
	}

	public void setObjForm(Vproducttypestock objForm) {
		this.objForm = objForm;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public Vproductorgstock getObj() {
		return obj;
	}

	public void setObj(Vproductorgstock obj) {
		this.obj = obj;
	}

	public Vproducttypestocksum getObjtotal() {
		return objtotal;
	}

	public void setObjtotal(Vproducttypestocksum objtotal) {
		this.objtotal = objtotal;
	}	

}
