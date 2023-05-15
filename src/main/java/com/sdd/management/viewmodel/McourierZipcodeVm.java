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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
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
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.McourierzipcodeDAO;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Mcourierzipcode;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.McouriervendorListModel;
import com.sdd.caption.model.McourierzipcodeListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class McourierZipcodeVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;
	private McourierzipcodeDAO oDao = new McourierzipcodeDAO();
	private McourierzipcodeListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mcourierzipcode objForm;
	private String vendorname;
	private Integer zipcodestart;
	private Integer zipcodeend;

	@Wire
	private Button btnSave;
	@Wire
	private Button btnCancel;
	@Wire
	private Button btnDelete;
	@Wire
	private Listbox listbox;
	@Wire
	private Combobox cbCourier;
	@Wire
	private Paging paging;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		needsPageUpdate = true;
		doReset();

		if (listbox != null) {
			listbox.setItemRenderer(new ListitemRenderer<Mcourierzipcode>() {

				@Override
				public void render(Listitem item, final Mcourierzipcode data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf(index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getMcouriervendor().getVendorname());
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getZipcodestart()));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getZipcodeend()));
					item.appendChild(cell);
					cell = new Listcell(data.getDescription());
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
					if (objForm.getMcouriervendor().getVendorname() != null)
						cbCourier.setValue(objForm.getMcouriervendor().getVendorname());
				}
			}
		});
	}

	public void refreshModel(int activePage) {
		orderby = "zipcodestart";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new McourierzipcodeListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		listbox.setModel(model);

	}

	@Command
	public void doSearch() {
		filter = "";

		if (vendorname != null && vendorname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "vendorname like '%" + vendorname.trim().toUpperCase() + "%'";
		}

		if (zipcodestart != null)
			filter += "zipcodestart = " + zipcodestart;

		if (zipcodeend != null)
			filter += "zipcodeend = " + zipcodeend;

		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("objForm")
	public void cancel() {
		doReset();
	}

	@Command
	@NotifyChange("objForm")
	public void save() {
		try {
			Mcourierzipcode objDuplicate = oDao
					.findByFilter(objForm.getZipcodestart() + " between zipcodestart and zipcodeend");

			if (objDuplicate == null) {
				Muser oUser = (Muser) zkSession.getAttribute("oUser");
				if (oUser == null)
					oUser = new Muser();

				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
//			objForm.setZipcodeend(String.valueOf(objForm.getZipcodeend()));
				objForm.setUpdatedby(oUser.getUserid());
				objForm.setLastupdated(new Date());
				oDao.save(session, objForm);
				transaction.commit();
				session.close();
				if (isInsert) {
					Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center",
							3000);
				} else
					Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center",
							3000);
				doReset();
			} else {
				filter = "mcourierzipcodepk = " + objDuplicate.getMcourierzipcodepk();
				
				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
				
				Messagebox.show("Kode pos sudah terdaftar dikurir " + objDuplicate.getMcouriervendor().getVendorname(),
						"Information", Messagebox.OK, Messagebox.EXCLAMATION);
			}
		} catch (HibernateException e) {
			transaction.rollback();
			if (isInsert)
				objForm.setMcourierzipcodepk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			if (isInsert)
				objForm.setMcourierzipcodepk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("objForm")
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

									doReset();
									BindUtils.postNotifyChange(null, null, McourierZipcodeVm.this, "objForm");
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

	@NotifyChange("objForm")
	public void doReset() {
		isInsert = true;
		objForm = new Mcourierzipcode();
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
		cbCourier.setValue(null);
	}

	@Command
	public void doExportExcel() {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("List Zipcode");
			List<Mcourierzipcode> objList = oDao.listByFilter(filter, orderby);

			int rownum = 0;
			int cellnum = 0;
			Integer no = 0;

			org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
			Cell cell = row.createCell(0);
			cell.setCellValue("List Zipcode");

			row = sheet.createRow(rownum++);

			Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
			datamap.put(1, new Object[] { "No", "Kurir", "Kode Kurir", "Zipcode Start", "Zipcode End" });
			no = 2;
			for (Mcourierzipcode data : objList) {

				datamap.put(no, new Object[] { no - 1, data.getMcouriervendor().getVendorname(),
						data.getMcouriervendor().getVendorcode(), data.getZipcodestart(), data.getZipcodeend() });
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

			String path = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
			String filename = "COURIERZIPCODE_" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
			FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
			workbook.write(out);
			out.close();

			Filedownload.save(new File(path + "/" + filename),
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ListModelList<Mcouriervendor> getMcouriervendor() {
		ListModelList<Mcouriervendor> lm = null;
		try {
			lm = new ListModelList<Mcouriervendor>(AppData.getMcouriervendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Integer zipcodestart = (Integer) ctx.getProperties("zipcodestart")[0].getValue();
				Integer zipcodeend = (Integer) ctx.getProperties("zipcodeend")[0].getValue();
				Mcouriervendor mcouriervendor = (Mcouriervendor) ctx.getProperties("mcouriervendor")[0].getValue();

				if (zipcodestart == null)
					this.addInvalidMessage(ctx, "zipcodestart", Labels.getLabel("common.validator.empty"));
				if (zipcodeend == null)
					this.addInvalidMessage(ctx, "zipcodeend", Labels.getLabel("common.validator.empty"));
				if (mcouriervendor == null)
					this.addInvalidMessage(ctx, "mcouriervendor", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public Mcourierzipcode getObjForm() {
		return objForm;
	}

	public void setObjForm(Mcourierzipcode objForm) {
		this.objForm = objForm;
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

	public String getVendorname() {
		return vendorname;
	}

	public void setVendorname(String vendorname) {
		this.vendorname = vendorname;
	}

	public Integer getZipcodestart() {
		return zipcodestart;
	}

	public void setZipcodestart(Integer zipcodestart) {
		this.zipcodestart = zipcodestart;
	}

	public Integer getZipcodeend() {
		return zipcodeend;
	}

	public void setZipcodeend(Integer zipcodeend) {
		this.zipcodeend = zipcodeend;
	}

}
