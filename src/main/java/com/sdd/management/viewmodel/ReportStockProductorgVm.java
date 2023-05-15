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
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
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
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class ReportStockProductorgVm {

	private MproducttypeDAO oDao = new MproducttypeDAO();
	private Vproductorgstock objForm;
	private String productgroup;
	
	private Integer totalincoming;
	private Integer totaloutgoing;
	private Integer totalstock;
	private String datereport;
	private List<Vproductorgstock> objList = new ArrayList<>();	
	private Map<String, String> mapOrg;

	@Wire
	private Listbox listbox;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		
		try {
			mapOrg = AppData.getOrgmap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		datereport = new SimpleDateFormat("dd MMM yyyy").format(new Date());
				
		doReset();
		
		listbox.setItemRenderer(new ListitemRenderer<Vproductorgstock>() {

			@Override
			public void render(Listitem item, Vproductorgstock data, int index) throws Exception {
				Listcell cell = new Listcell(String.valueOf(index + 1));
				item.appendChild(cell);					
				cell = new Listcell(mapOrg.get(data.getProductorg()));
				item.appendChild(cell);
				cell = new Listcell(data.getTotalincoming() != null ? NumberFormat.getInstance().format(data.getTotalincoming()) : "0");
				item.appendChild(cell);
				cell = new Listcell(data.getTotaloutgoing() != null ? NumberFormat.getInstance().format(data.getTotaloutgoing()) : "0");
				item.appendChild(cell);
				cell = new Listcell(data.getTotalstock() != null ? NumberFormat.getInstance().format(data.getTotalstock()) : "0");
				item.appendChild(cell);
				
				totalincoming += data.getTotalincoming() != null ? data.getTotalincoming() : 0;
				totaloutgoing += data.getTotaloutgoing() != null ? data.getTotaloutgoing() : 0;
				totalstock += data.getTotalstock() != null ? data.getTotalstock() : 0;
				
				BindUtils.postNotifyChange(null, null, ReportStockProductorgVm.this, "*");
			}
			
		});		
		
		listbox.addEventListener(Events.ON_SELECT, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				Map<String, Object> map = new HashMap<>();
				map.put("obj", objForm);

				Window win = (Window) Executions
						.createComponents(
								"/view/report/reportstockproduct.zul",
								null, map);
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
			totalincoming = 0;
			totaloutgoing = 0;
			totalstock = 0;
			objList = oDao.listProductorgstock(AppUtils.PRODUCTGROUP_CARD);
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
			cell.setCellValue("Laporan Stock");
			row = sheet.createRow(rownum++);
			cell = row.createCell(0);
			cell.setCellValue("Tanggal");
			cell = row.createCell(1);
			cell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
			
			row = sheet.createRow(rownum++);
			
			Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
			datamap.put(1, new Object[] { "No", "Org", "Total Incoming",
					"Total Outgoing", "Total Stock" });
			no = 2;
			for (Vproductorgstock data: objList) {								
				datamap.put(
						no,
						new Object[] {
								no - 1,
								mapOrg.get(data.getProductorg()), 
								data.getTotalincoming(), data.getTotaloutgoing(), data.getTotalstock() });
				no++;
			}
			datamap.put(
					no,
					new Object[] {"", "TOTAL", 
							totalincoming, totaloutgoing, totalstock });
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
			String filename = "CIMS_STOCK_"
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

	public Vproductorgstock getObjForm() {
		return objForm;
	}

	public void setObjForm(Vproductorgstock objForm) {
		this.objForm = objForm;
	}

	public Integer getTotalincoming() {
		return totalincoming;
	}

	public void setTotalincoming(Integer totalincoming) {
		this.totalincoming = totalincoming;
	}

	public Integer getTotaloutgoing() {
		return totaloutgoing;
	}

	public void setTotaloutgoing(Integer totaloutgoing) {
		this.totaloutgoing = totaloutgoing;
	}

	public Integer getTotalstock() {
		return totalstock;
	}

	public void setTotalstock(Integer totalstock) {
		this.totalstock = totalstock;
	}	

}
