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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
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

import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.dao.TreturnDAO;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Treturn;
import com.sdd.caption.model.TreturnListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ReturnListVm {
	
	private TreturnListModel model;
	
	private TreturnDAO oDao = new TreturnDAO();
	private TincomingDAO tincomingDao = new TincomingDAO();
	
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;	
	private Integer year;
	private Integer month;
	private String status;	
	private String productgroup;
	
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
		
	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view)
			throws ParseException {
		Selectors.wireComponents(view, this, false);				
		productgroup = AppUtils.PRODUCTGROUP_DOCUMENT;
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});
		
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Treturn>() {

				@Override
				public void render(Row row, final Treturn data, int index)
						throws Exception {
					row.getChildren()
							.add(new Label(
									String.valueOf((SysUtils.PAGESIZE * pageStartNumber)
											+ index + 1)));
					row.getChildren().add(
							new Label(dateLocalFormatter.format(data.getReturndate())));					
					row.getChildren().add(new Label(data.getMproduct().getProductname()));
					row.getChildren().add(new Label(data.getCardno()));
					row.getChildren().add(new Label(data.getName()));
					row.getChildren().add(new Label(data.getMpersovendor() != null ? data.getMpersovendor().getVendorcode() : "OPR"));
					row.getChildren().add(new Label(data.getDescription()));
					row.getChildren().add(new Label(data.getProdoprname()));
					Button btndel = new Button("Hapus");
					btndel.setAutodisable("self");
					btndel.setClass("btn btn-danger btn-sm");
					btndel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
								Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

									@Override
									public void onEvent(Event event)
											throws Exception {
										if (event.getName().equals("onOK")) {
											Session session = StoreHibernateUtil.openSession();
											Transaction transaction = session.beginTransaction();
											try {						
												oDao.delete(session, data);			
												transaction.commit();			
												Clients.showNotification(
														"Hapus data berhasil.",
														"info", null, "middle_center", 3000);
											} catch (HibernateException e){
												transaction.rollback();			
												e.printStackTrace();
											} catch (Exception e) {
												e.printStackTrace();
											} finally {
												session.close();
											}
											
											doSearch();
											BindUtils.postNotifyChange(null, null, ReturnListVm.this, "pageTotalSize");
										} 									
									}				
								});				
						}
						
					});
					row.getChildren().add(btndel);
				}

			});
		}
		
		String[] months = new DateFormatSymbols().getMonths();
	    for (int i = 0; i < months.length; i++) {
	      Comboitem item = new Comboitem();
	      item.setLabel(months[i]);
	      item.setValue(i+1);
	      cbMonth.appendChild(item);
	    }
	    
		doReset();
	}
	
	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "treturnpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TreturnListModel(activePage, SysUtils.PAGESIZE, filter,
				orderby);
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
		if (year != null && month != null) {
			filter = "extract(year from returndate) = " + year + " and "
					+ "extract(month from returndate) = " + month;
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}		
	}
		
	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Treturn> listData = oDao.listByFilter(filter, orderby);
				if (listData != null && listData.size() > 0) {
					XSSFWorkbook workbook = new XSSFWorkbook();
					XSSFSheet sheet = workbook.createSheet();

					int rownum = 0;
					int cellnum = 0;
					Integer no = 0;
					Integer total = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("Daftar Return");
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
					datamap.put(1, new Object[] { "No", "Tanggal", "Jenis Kartu", "No Kartu", "Nama",
							"Diproses Oleh", "Keterangan", "Pemroses"});
					no = 2;
					for (Treturn data: listData) {								
						datamap.put(
								no,
								new Object[] {
										no - 1, dateLocalFormatter.format(data.getEntrytime()), data.getMproduct().getProductname(), 
										data.getCardno(), data.getName(), data.getMpersovendor() != null ? data.getMpersovendor().getVendorcode() : "OPR",
										data.getDescription(), data.getProdoprname()});
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
					String filename = "CAPTION_DAFTAR_ORDER_"
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
			Messagebox.show("Error : " + e.getMessage(), "Error",
					Messagebox.OK, Messagebox.ERROR);
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		status = "";
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}	
		
}
