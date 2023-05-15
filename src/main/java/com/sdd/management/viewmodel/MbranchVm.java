package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
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
import org.zkoss.zul.Radio;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TmissingbranchDAO;
import com.sdd.caption.dao.TpinmailerbranchDAO;
import com.sdd.caption.dao.TpinmailerdataDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mregion;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tmissingbranch;
import com.sdd.caption.domain.Tpinmailerbranch;
import com.sdd.caption.model.MbranchListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MbranchVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private MbranchListModel model;
	private MbranchDAO oDao = new MbranchDAO();
	private TembossdataDAO todDao = new TembossdataDAO();
	private TmissingbranchDAO tmbDao = new TmissingbranchDAO();
	private TembossbranchDAO tebDao = new TembossbranchDAO();
	private TpinmailerbranchDAO tpmbDao = new TpinmailerbranchDAO();
	private TpinmailerdataDAO tpmdDao = new TpinmailerdataDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mbranch objForm;
	private String branchid;
	private String branchcode;
	private String branchname;
	private String vendorcode;
	private String intercity;

	List<Mbranch> objList = new ArrayList<>();

	@Wire
	private Combobox cbRegion;
	@Wire
	private Combobox cbCourier;
	@Wire
	private Button btnSave;
	@Wire
	private Button btnCancel;
	@Wire
	private Button btnDelete;
	@Wire
	private Paging paging;
	@Wire
	private Listbox listbox;
	@Wire
	private Radio rbYes, rbNo;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
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
			listbox.setItemRenderer(new ListitemRenderer<Mbranch>() {
				@Override
				public void render(Listitem item, final Mbranch data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getBranchid());
					item.appendChild(cell);
					cell = new Listcell(data.getBranchcode());
					item.appendChild(cell);
					cell = new Listcell(data.getBranchname());
					item.appendChild(cell);
					cell = new Listcell(data.getBranchaddress());
					item.appendChild(cell);
					cell = new Listcell(data.getBranchcity());
					item.appendChild(cell);
					cell = new Listcell(data.getMregion() != null ? data.getMregion().getRegionname() : "");
					item.appendChild(cell);
					cell = new Listcell(
							data.getMcouriervendor() != null ? data.getMcouriervendor().getVendorcode() : "");
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
					cbRegion.setValue(objForm.getMregion().getRegionname());
					// cbCourier.setValue(objForm.getMcouriervendor().getVendorname());
					cbCourier.setValue(
							objForm.getMcouriervendor() != null ? objForm.getMcouriervendor().getVendorname() : null);
					if (objForm.getIsheadoffice() != null) {
						if (objForm.getIsheadoffice().trim().equals("Y")) {
							rbYes.setChecked(true);
						}
						if (objForm.getIsheadoffice().trim().equals("N")) {
							rbNo.setChecked(true);
						}
						BindUtils.postNotifyChange(null, null, MbranchVm.this, "rbYes");
						BindUtils.postNotifyChange(null, null, MbranchVm.this, "rbNo");
					}

					if (objForm.getIsintercity() != null) {
						intercity = objForm.getIsintercity();
						BindUtils.postNotifyChange(null, null, MbranchVm.this, "intercity");
					}
				}
			}
		});
	}

	public void refreshModel(int activePage) {
		orderby = "branchid";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MbranchListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (branchid != null && branchid.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "branchid like '%" + branchid.trim().toUpperCase() + "%'";
		}
		if (branchcode != null && branchcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "branchcode like '%" + branchcode.trim().toUpperCase() + "%'";
		}
		if (branchname != null && branchname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "branchname like '%" + branchname.trim().toUpperCase() + "%'";
		}
		if (vendorcode != null && vendorcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "vendorcode like '%" + vendorcode.trim().toUpperCase() + "%'";
		}
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
//			Mbranch databranch = new MbranchDAO().findByFilter("branchid = '" + objForm.getBranchid() + "'");
//			Mbranch databranch2 = new MbranchDAO().findByFilter("branchcode = '" + objForm.getBranchcode() + "'");
//			if (databranch != null && databranch2 != null) {
//				Messagebox.show(
//						"Gagal menambah cabang, id cabang '" + objForm.getBranchid().trim() + "' dan kode cabang '"
//								+ objForm.getBranchcode().trim() + "' sudah terdaftar.",
//						"Peringatan", Messagebox.OK, Messagebox.EXCLAMATION);
//			} else if (databranch != null && databranch2 == null) {
//				Messagebox.show(
//						"Gagal menambah cabang, id wilayah '" + objForm.getBranchid().trim() + "' sudah terdaftar.",
//						"Peringatan", Messagebox.OK, Messagebox.EXCLAMATION);
//			} else if (databranch == null && databranch2 != null) {
//				Messagebox.show(
//						"Gagal menambah cabang, kode cabang '" + objForm.getBranchcode().trim() + "' sudah terdaftar.",
//						"Peringatan", Messagebox.OK, Messagebox.EXCLAMATION);
//			} else if (databranch == null && databranch2 == null) {
				try {
					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					objForm.setIsintercity(intercity);
					String s = objForm.getBranchid();
					Integer nilai = Integer.parseInt(s);
					if (nilai < 600) {
						objForm.setBranchlevel(3);
					} else if (nilai >= 600 && nilai < 700) {
						objForm.setBranchlevel(2);
					} else if (nilai >= 700) {
						objForm.setBranchlevel(1);
					}
					if (isInsert) {
						objForm.setUpdatedby(oUser.getUserid());
						objForm.setLastupdated(new Date());
					} else {
						objForm.setUpdatedby(oUser.getUserid());
						objForm.setLastupdated(new Date());
					}

					if (rbYes.isChecked()) {
						objForm.setIsheadoffice("Y");
						Mbranch isHeadOffice = oDao.findByFilter("isheadoffice = 'Y'");
						System.out.println(objForm.getBranchid());
						if (isHeadOffice != null) {
							System.out.println(isHeadOffice.getBranchid());
							if (!isHeadOffice.getBranchid().trim().equals(objForm.getBranchid().trim())) {
								System.out.println("TEST");
								isHeadOffice.setIsheadoffice("N");
								oDao.save(session, isHeadOffice);
							}
						}
					} else if (rbNo.isChecked()) {
						objForm.setIsheadoffice("N");
					}
					oDao.save(session, objForm);
					transaction.commit();

					/* CEK TMISSINGBRANCH */
					transaction = session.beginTransaction();
					List<Tmissingbranch> tmbList = tmbDao.listByFilter("branchid = '" + objForm.getBranchid() + "'",
							"branchid");
					if (tmbList.size() > 0) {
						List<Tembossbranch> tebList = tebDao.listByFilter("branchid = '" + objForm.getBranchid() + "'",
								"branchid");
						if (tebList.size() > 0) {
							for (Tembossbranch data : tebList) {
								data.setMbranch(objForm);
								tebDao.save(session, data);

							}
							todDao.updateMbranchByEmbossBranch(session, objForm.getMbranchpk(), objForm.getBranchid());
						}

						List<Tpinmailerbranch> tpbList = tpmbDao
								.listByFilter("branchid = '" + objForm.getBranchid() + "'", "branchid");
						if (tpbList.size() > 0) {
							for (Tpinmailerbranch obj : tpbList) {
								obj.setMbranch(objForm);
								tpmbDao.save(session, obj);
							}
							tpmdDao.updateMbranchByPinmailerBranch(session, objForm);
						}

						for (Tmissingbranch tmb : tmbList) {
							tmbDao.delete(session, tmb);
						}

						transaction.commit();
					}
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
						objForm.setMbranchpk(null);
					Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				} catch (Exception e) {
					if (isInsert)
						objForm.setMbranchpk(null);
					Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				}
		} catch (Exception e) {
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

									needsPageUpdate = true;
									doReset();
									BindUtils.postNotifyChange(null, null, MbranchVm.this, "objForm");
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
		intercity = "Y";
		objForm = new Mbranch();
		rbYes.setChecked(false);
		rbNo.setChecked(true);
		refreshModel(pageStartNumber);
		cbRegion.setValue(null);
		cbCourier.setValue(null);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				String branchid = (String) ctx.getProperties("branchid")[0].getValue();
				String branchcode = (String) ctx.getProperties("branchcode")[0].getValue();
				String branchname = (String) ctx.getProperties("branchname")[0].getValue();
				Mregion mregion = (Mregion) ctx.getProperties("mregion")[0].getValue();
				Mcouriervendor mcouriervendor = (Mcouriervendor) ctx.getProperties("mcouriervendor")[0].getValue();

				if (branchid == null || "".equals(branchid.trim()))
					this.addInvalidMessage(ctx, "branchid", Labels.getLabel("common.validator.empty"));
				if (branchcode == null || "".equals(branchcode.trim()))
					this.addInvalidMessage(ctx, "branchcode", Labels.getLabel("common.validator.empty"));
				if (branchname == null || "".equals(branchname.trim()))
					this.addInvalidMessage(ctx, "branchname", Labels.getLabel("common.validator.empty"));
				if (mregion == null)
					this.addInvalidMessage(ctx, "mregion", Labels.getLabel("common.validator.empty"));
				if (mcouriervendor == null)
					this.addInvalidMessage(ctx, "mcouriervendor", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	@Command
	public void doExport() {
		try {
			objList = oDao.listByFilter(filter, "branchid");
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
				cell.setCellValue("Daftar Cabang");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(0, new Object[] { "No", "ID Cabang", "Code Cabang", "Cabang", "Alamat", "Kota", "Wilayah",
						"Expedisi" });
				no = 2;
				for (Mbranch data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getBranchid(), data.getBranchcode(), data.getBranchname(),
									data.getBranchaddress(), data.getBranchcity(),
									data.getMregion() != null ? data.getMregion().getRegionname() : "",
									data.getMcouriervendor() != null ? data.getMcouriervendor().getVendorcode() : "" });
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
				String filename = "LIST_CABANG" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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

	public ListModelList<Mregion> getMregion() {
		ListModelList<Mregion> lm = null;
		try {
			lm = new ListModelList<Mregion>(AppData.getMregion());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
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

	public Mbranch getObjForm() {
		return objForm;
	}

	public void setObjForm(Mbranch objForm) {
		this.objForm = objForm;
	}

	public String getBranchid() {
		return branchid;
	}

	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}

	public String getBranchcode() {
		return branchcode;
	}

	public void setBranchcode(String branchcode) {
		this.branchcode = branchcode;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public String getVendorcode() {
		return vendorcode;
	}

	public void setVendorcode(String vendorcode) {
		this.vendorcode = vendorcode;
	}

	public String getIntercity() {
		return intercity;
	}

	public void setIntercity(String intercity) {
		this.intercity = intercity;
	}

}
