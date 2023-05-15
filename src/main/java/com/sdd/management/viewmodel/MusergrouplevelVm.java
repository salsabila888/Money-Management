package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MuserDAO;
import com.sdd.caption.dao.MusergroupDAO;
import com.sdd.caption.dao.MusergrouplevelDAO;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Mproductowner;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergroup;
import com.sdd.caption.domain.Musergrouplevel;
import com.sdd.caption.model.MusergrouplevelListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MusergrouplevelVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private MusergrouplevelListModel model;
	private MusergrouplevelDAO oDao = new MusergrouplevelDAO();
	private MusergroupDAO uDao = new MusergroupDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;
	private String currentuser;
	private String branchlevel;
	private String grouplevel;

	private Musergrouplevel objForm;

	private List<Musergrouplevel> objList = new ArrayList<>();

	@Wire
	private Button btnSave, btnCancel, btnDelete;
	@Wire
	private Paging paging;
	@Wire
	private Listbox listbox;
	@Wire
	private Textbox tbAmStart, tbAmEnd;
	@Wire 
	private Combobox cbUserLevel, cbBranchLevel, cbGroupLevel;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);

		paging.addEventListener("onPaging", new EventListener() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (listbox != null) {
			listbox.setItemRenderer(new ListitemRenderer<Musergrouplevel>() {
				@Override
				public void render(Listitem item, final Musergrouplevel data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getMusergoup().getUsergroupname());
					item.appendChild(cell);
					cell = new Listcell(AppData.getBranchLevelLabel(data.getBranchlevel()));
					item.appendChild(cell);
					cell = new Listcell(AppData.getGroupLevelLabel(data.getGrouplevel()));
					item.appendChild(cell);
					cell = new Listcell("Rp " + NumberFormat.getInstance().format(data.getAmountstart()));
					item.appendChild(cell);
					cell = new Listcell("Rp " + NumberFormat.getInstance().format(data.getAmountend()));
					item.appendChild(cell);
				}
			});
		}

		listbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if (listbox.getSelectedIndex() != -1) {
					isInsert = false;
					btnSave.setLabel(Labels.getLabel("common.update"));
					btnCancel.setDisabled(false);
					btnDelete.setDisabled(false);
					branchlevel = String.valueOf(objForm.getBranchlevel());
					grouplevel = String.valueOf(objForm.getGrouplevel());
					
					cbUserLevel.setValue(objForm.getMusergoup().getUsergroupname());
					cbBranchLevel.setValue(AppData.getBranchLevelLabel(objForm.getBranchlevel()));
					cbGroupLevel.setValue(AppData.getGroupLevelLabel(objForm.getGrouplevel()));
				}
			}
		});
		needsPageUpdate = true;
		doReset();
	}

	public void refreshModel(int activePage) {
		orderby = "musergrouplevelpk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MusergrouplevelListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		listbox.setModel(model);
	}

	@Command
	public void doSearch() {
		filter = "musergrouplevelpk";
		
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void cancel() {
		doReset();
	}

	@Command
	@NotifyChange("objForm")
	public void save() {
		try {
			Musergrouplevel data = null;
			if(isInsert)
				data = oDao.findByFilter("musergroupfk = " + objForm.getMusergoup().getMusergrouppk());
			
			if (data != null) {
				Messagebox.show("Gagal menambah level user grup,  '" + objForm.getMusergoup().getUsergroupname() + "' sudah terdaftar.",
						"Peringatan", Messagebox.OK, Messagebox.EXCLAMATION);
			} else {
				try {
					Muser oUser = (Muser) zkSession.getAttribute("oUser");
					if (oUser == null)
						oUser = new Muser();

					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();

					if (isInsert) {
						objForm.setMusergoup(objForm.getMusergoup());
						objForm.setAmountstart(objForm.getAmountstart());
						objForm.setAmountend(objForm.getAmountend());
					} 
					
					System.out.println(branchlevel);
					System.out.println(grouplevel);
					objForm.setBranchlevel(Integer.parseInt(branchlevel));
					objForm.setGrouplevel(Integer.parseInt(grouplevel));
					oDao.save(session, objForm);
					transaction.commit();
					session.close();
					if (isInsert) {
						needsPageUpdate = true;
						Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center",
								3000);
					} else
						Clients.showNotification(Labels.getLabel("common.update.success"), "info", null,
								"middle_center", 3000);
					doReset();
				} catch (HibernateException e) {
					transaction.rollback();
					if (isInsert)
						objForm.setMusergrouplevelpk(null);
					Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				} catch (Exception e) {
					if (isInsert)
						objForm.setMusergrouplevelpk(null);
					Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("*")
	public void delete() {
		try {
			Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								try {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									oDao.delete(session, objForm);
									transaction.commit();
									session.close();

									Clients.showNotification(Labels.getLabel("common.delete.success"), "info", null,
											"middle_center", 3000);

									needsPageUpdate = true;
									doReset();
									BindUtils.postNotifyChange(null, null, MusergrouplevelVm.this, "objForm");
								} catch (HibernateException e) {
									transaction.rollback();
									Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								} catch (Exception e) {
									Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								}
							}
						}

					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		isInsert = true;
		objForm = new Musergrouplevel();
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
		cbBranchLevel.setValue(null);
		cbGroupLevel.setValue(null);
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
			}
		};
	}

	@Command
	public void doExport() {
		try {
			objList = oDao.listByFilter(filter, orderby);
			if (objList != null && objList.size() > 0) {
				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet();
				XSSFCellStyle style = workbook.createCellStyle();
				style.setBorderTop(BorderStyle.MEDIUM);
				style.setBorderBottom(BorderStyle.MEDIUM);
				style.setBorderLeft(BorderStyle.MEDIUM);
				style.setBorderRight(BorderStyle.MEDIUM);

				int rownum = 0;
				int cellnum = 0;
				Integer no = 0;
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
				Cell cell = row.createCell(0);
				cell.setCellValue("Daftar Level Grup User");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(0, new Object[] { "No", "Grup User", "Level Cabang", "Level Grup", "Amount Start", "Amount End"});
				no = 2;
				for (Musergrouplevel data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getMusergoup().getUsergroupname(), 
									AppData.getBranchLevelLabel(data.getBranchlevel()),
									AppData.getBranchLevelLabel(data.getGrouplevel()),
									"Rp " + NumberFormat.getInstance().format(data.getAmountstart()),
									"Rp " + NumberFormat.getInstance().format(data.getAmountend())});
					no++;
				}
				Set<Integer> keyset = datamap.keySet();
				for (Integer key : keyset) {
					row = sheet.createRow(rownum++);
					Object[] objArr = datamap.get(key);
					cellnum = 0;
					if (rownum == 2) {
						XSSFCellStyle styleHeader = workbook.createCellStyle();
						styleHeader.setBorderTop(BorderStyle.MEDIUM);
						styleHeader.setBorderBottom(BorderStyle.MEDIUM);
						styleHeader.setBorderLeft(BorderStyle.MEDIUM);
						styleHeader.setBorderRight(BorderStyle.MEDIUM);
						styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
						styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String) {
								cell.setCellValue((String) obj);
								cell.setCellStyle(styleHeader);
							} else if (obj instanceof Integer) {
								cell.setCellValue((Integer) obj);
								cell.setCellStyle(styleHeader);
							} else if (obj instanceof Double) {
								cell.setCellValue((Double) obj);
								cell.setCellStyle(styleHeader);
							}
						}
					} else {
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String) {
								cell.setCellValue((String) obj);
								cell.setCellStyle(style);
							} else if (obj instanceof Integer) {
								cell.setCellValue((Integer) obj);
								cell.setCellStyle(style);
							} else if (obj instanceof Double) {
								cell.setCellValue((Double) obj);
								cell.setCellStyle(style);
							}
						}
					}
				}

				String path = Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
				String filename = "LIST_LEVEL_GRUP_USER" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
				FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
				workbook.write(out);
				out.close();

				Filedownload.save(new File(path + "/" + filename),
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			} else {
				Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	public ListModelList<Musergroup> getMusergroup() {
		ListModelList<Musergroup> lm = null;
		try {
			lm = new ListModelList<Musergroup>(AppData.getMusergroup());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Musergrouplevel getObjForm() {
		return objForm;
	}

	public void setObjForm(Musergrouplevel objForm) {
		this.objForm = objForm;
	}

	public String getBranchlevel() {
		return branchlevel;
	}

	public void setBranchlevel(String branchlevel) {
		this.branchlevel = branchlevel;
	}

	public String getGrouplevel() {
		return grouplevel;
	}

	public void setGrouplevel(String grouplevel) {
		this.grouplevel = grouplevel;
	}

	
}
