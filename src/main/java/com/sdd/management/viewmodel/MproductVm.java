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
import org.zkoss.zul.Textbox;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MorgDAO;
import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.TembossbranchDAO;
import com.sdd.caption.dao.TembossproductDAO;
import com.sdd.caption.dao.TmissingproductDAO;
import com.sdd.caption.dao.TpinmailerdataDAO;
import com.sdd.caption.dao.TpinmailerproductDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tembossbranch;
import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Tmissingproduct;
import com.sdd.caption.domain.Tpinmailerproduct;
import com.sdd.caption.model.MproductListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MproductVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private MproductListModel model;
	private MproductDAO oDao = new MproductDAO();
	private TembossdataDAO todDao = new TembossdataDAO();
	private TmissingproductDAO tmpDao = new TmissingproductDAO();
	private TembossproductDAO tepDao = new TembossproductDAO();
	private TembossbranchDAO tebDao = new TembossbranchDAO();
	private TpinmailerproductDAO tpmpDao = new TpinmailerproductDAO();
	private TpinmailerdataDAO tpmdDao = new TpinmailerdataDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;
	private String currentproductcode;

	private Mproduct objForm;

	private String producttype;
	private String productcode;
	private String productname;
	private String productgroupcode;
	private String productgroupname;
	private Morg morgsearch;

	private Mproduct mproduct;
	private Map<String, String> mapOrg;
	private List<Mproduct> objList = new ArrayList<>();

	@Wire
	private Button btnSave;
	@Wire
	private Button btnCancel;
	@Wire
	private Button btnDelete;
	@Wire
	private Combobox cbProducttype;
	@Wire
	private Combobox cbProduct;
	@Wire
	private Paging paging;
	@Wire
	private Listbox listbox;
	@Wire
	private Textbox tbproductcode;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);

		productgroupcode = AppUtils.PRODUCTGROUP_CARD;
		productgroupname = AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_CARD);

		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		try {
			mapOrg = AppData.getOrgmap();
		} catch (Exception e) {
			e.printStackTrace();
		}

		needsPageUpdate = true;
		doReset();

		listbox.setItemRenderer(new ListitemRenderer<Mproduct>() {

			@Override
			public void render(Listitem item, final Mproduct data, int index) throws Exception {
				Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
				item.appendChild(cell);
				cell = new Listcell(mapOrg.get(data.getMproducttype().getProductorg()));
				item.appendChild(cell);
				cell = new Listcell(data.getMproducttype().getProducttype());
				item.appendChild(cell);
				cell = new Listcell(String.valueOf(data.getProductcode()));
				item.appendChild(cell);
				cell = new Listcell(data.getProductname());
				item.appendChild(cell);
				cell = new Listcell(data.getIsinstant().equals("Y") ? "Ya" : "Tidak");
				item.appendChild(cell);
				cell = new Listcell(data.getIsmm().equals("Y") ? "Ya" : "Tidak");
				item.appendChild(cell);
				cell = new Listcell(data.getIsdlvhome().equals("Y") ? "Rumah" : "Cabang");
				item.appendChild(cell);

			}
		});

		listbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				if (listbox.getSelectedIndex() != -1) {
					isInsert = false;
					btnSave.setLabel(Labels.getLabel("common.update"));
					btnCancel.setDisabled(false);
					btnDelete.setDisabled(false);

					cbProducttype.setValue(objForm.getMproducttype().getProducttype());
					tbproductcode.setDisabled(true);
					if (objForm.getComboref() != null) {
						mproduct = oDao.findByPk(objForm.getComboref());
						if (mproduct != null) {
							cbProduct.setValue(mproduct.getProductcode());
							BindUtils.postNotifyChange(null, null, MproductVm.this, "mproduct");
						}
					}

					currentproductcode = objForm.getProductcode() + objForm.getIsinstant();
				}
			}
		});
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "productorg, productgroupcode, productname";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MproductListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		listbox.setModel(model);
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		try {
			filter = "productgroupcode = '" + AppUtils.PRODUCTGROUP_CARD + "'";
			if (morgsearch != null) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "productorg = '" + morgsearch.getOrg() + "'";
			}
			if (producttype != null && producttype.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "producttype like '%" + producttype.trim().toUpperCase() + "%'";
			}
			if (productcode != null && productcode.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "productcode like '%" + productcode.trim().toUpperCase() + "%'";
			}
			if (productname != null && productname.trim().length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "productname like '%" + productname.trim().toUpperCase() + "%'";
			}

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("objForm")
	public void cancel() {
		doReset();
	}

	@Command
	@NotifyChange("*")
	public void save() {
		try {

			Muser oUser = (Muser) zkSession.getAttribute("oUser");
			if (oUser == null)
				oUser = new Muser();

			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			if (isInsert) {
				Mproduct productDuplicate = oDao.findByFilter("productcode = '" + objForm.getProductcode().toUpperCase()
						+ "' and isinstant = '" + objForm.getIsinstant().toUpperCase() + "'");
				if (productDuplicate == null) {
					if (mproduct != null)
						objForm.setComboref(mproduct.getMproductpk());
					objForm.setLastupdated(new Date());
					objForm.setUpdatedby(oUser.getUserid());
					oDao.save(session, objForm);
					transaction.commit();
					/* CEK TMISSINGPRODUCT */
					List<Tmissingproduct> tmpList = tmpDao.listByFilter(
							"productcode = '" + objForm.getProductcode().toUpperCase() + "' and isinstant = '"
									+ objForm.getIsinstant().toUpperCase() + "'",
							"productcode");
					if (tmpList.size() > 0) {
						transaction = session.beginTransaction();
						Morg morg = new MorgDAO().findById(objForm.getMproducttype().getProductorg());
						List<Tembossproduct> tepList = tepDao
								.listByFilter(
										"productcode = '" + objForm.getProductcode().toUpperCase()
												+ "' and isinstant = '" + objForm.getIsinstant().toUpperCase() + "'",
										"productcode");
						if (tepList.size() > 0) {
							for (Tembossproduct data : tepList) {
								data.setMproduct(objForm);
								data.setOrg(morg.getOrg());
								data.setIsneeddoc(morg.getIsneeddoc());
								List<Tembossbranch> branchList = new TembossbranchDAO()
										.listByFilter("tembossproductfk = " + data.getTembossproductpk(), "branchid");
								for (Tembossbranch embossBranch : branchList) {
									embossBranch.setMproduct(objForm);
									tebDao.save(session, embossBranch);
								}
								/*
								 * Tembossbranch embossBranch = new TembossbranchDAO()
								 * .findByFilter("tembossproductfk = " + data.getTembossproductpk());
								 */
								tepDao.save(session, data);

							}

							todDao.updateMproductByEmbossProduct(session, objForm);

						}

						List<Tpinmailerproduct> tpmpList = tpmpDao
								.listByFilter(
										"productcode = '" + objForm.getProductcode().toUpperCase()
												+ "' and isinstant = '" + objForm.getIsinstant().toUpperCase() + "'",
										"productcode");
						if (tpmpList.size() > 0) {
							for (Tpinmailerproduct pinProduct : tpmpList) {
								pinProduct.setMproduct(objForm);
								pinProduct.setOrg(morg.getOrg());
								tpmpDao.save(session, pinProduct);
							}

							tpmdDao.updateMproductByPinmailerBranch(session, objForm);
						}

						for (Tmissingproduct tmp : tmpList) {
							tmpDao.delete(session, tmp);
						}
						transaction.commit();

					}

					needsPageUpdate = true;
					Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center",
							3000);
				} else {
					filter = "productcode = '" + objForm.getProductcode().toUpperCase() + "' and isinstant = '"
							+ objForm.getIsinstant().toUpperCase() + "'";

					needsPageUpdate = true;
					paging.setActivePage(0);
					pageStartNumber = 0;
					refreshModel(pageStartNumber);
					Messagebox.show("Produk duplicate", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			} else {
				String newproductcode = objForm.getProductcode() + objForm.getIsinstant();

				if (currentproductcode.equals(newproductcode)) {
					if (mproduct != null)
						objForm.setComboref(mproduct.getMproductpk());
					objForm.setLastupdated(new Date());
					objForm.setUpdatedby(oUser.getUserid());
					oDao.save(session, objForm);
					transaction.commit();
					Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
				} else {
					Mproduct productDuplicate = oDao.findByFilter("productcode = '" + objForm.getProductcode().toUpperCase()
							+ "' and isinstant = '" + objForm.getIsinstant().toUpperCase() + "'");
					if (productDuplicate == null) {
						if (mproduct != null)
							objForm.setComboref(mproduct.getMproductpk());
						objForm.setLastupdated(new Date());
						objForm.setUpdatedby(oUser.getUserid());
						oDao.save(session, objForm);
						transaction.commit();
						Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
					} else {
						filter = "productcode = '" + objForm.getProductcode().toUpperCase() + "' and isinstant = '"
								+ objForm.getIsinstant().toUpperCase() + "'";

						needsPageUpdate = true;
						paging.setActivePage(0);
						pageStartNumber = 0;
						refreshModel(pageStartNumber);
						Messagebox.show("Produk duplicate", "Info", Messagebox.OK, Messagebox.INFORMATION);
					}
				}
				/*
				 * String filterproduct = ""; if (objForm.getIsinstant().equals("Y"))
				 * filterproduct = "productcode = '" + objForm.getProductcode().toUpperCase() +
				 * "' and isinstant = 'N'"; else if (objForm.getIsinstant().equals("N"))
				 * filterproduct = "productcode = '" + objForm.getProductcode().toUpperCase() +
				 * "' and isinstant = 'Y'"; Mproduct mproduct =
				 * oDao.findByFilter(filterproduct); if (mproduct != null) {
				 * mproduct.setIsmm(objForm.getIsmm()); oDao.save(session, mproduct); }
				 */
			}
			session.close();

			doReset();

		} catch (HibernateException e) {
			transaction.rollback();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("objForm")
	public void delete() {
		try {
			Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

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
									BindUtils.postNotifyChange(null, null, MproductVm.this, "objForm");
								} catch (HibernateException e) {
									transaction.rollback();
									Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								} catch (Exception e) {
									Messagebox.show("Gagal hapus, produk ini sudah terpakai.", "Informasi",
											Messagebox.OK, Messagebox.EXCLAMATION);
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
		objForm = new Mproduct();
		objForm.setProductgroup(productgroupcode);
		objForm.setIsinstant("N");
		objForm.setIsmm("Y");
		objForm.setIsdlvhome("N");
		mproduct = null;
		cbProducttype.setValue(null);
		cbProducttype.setDisabled(false);
		tbproductcode.setDisabled(false);
		cbProduct.setValue(null);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
		doSearch();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mproducttype mproducttype = (Mproducttype) ctx.getProperties("mproducttype")[0].getValue();
				String productcode = (String) ctx.getProperties("productcode")[0].getValue();
				String productname = (String) ctx.getProperties("productname")[0].getValue();

				if (mproducttype == null)
					this.addInvalidMessage(ctx, "mproducttype", Labels.getLabel("common.validator.empty"));
				if (productcode == null || "".equals(productcode.trim()))
					this.addInvalidMessage(ctx, "productcode", Labels.getLabel("common.validator.empty"));
				if (productname == null || "".equals(productname.trim()))
					this.addInvalidMessage(ctx, "productname", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	@Command
	public void doExport() {
		try {
			objList = oDao.listProduct(filter, "productorg, productgroupcode, productname");
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
				cell.setCellValue("Daftar Jenis Kartu");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(0, new Object[] { "No", "Org", "Tipe Kartu", "Kode Kartu", "Jenis Kartu", "Instant?",
						"Mandatory Merging?" });
				no = 2;
				for (Mproduct data : objList) {
					datamap.put(no,
							new Object[] { no - 1, mapOrg.get(data.getMproducttype().getProductorg()),
									data.getMproducttype().getProducttype(), data.getProductcode(),
									data.getProductname(), data.getIsinstant().equals("Y") ? "YA" : "TIDAK",
									data.getIsmm().equals("Y") ? "YA" : "TIDAK" });
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
				String filename = "LIST_PRODUCT" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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

	public ListModelList<Morg> getMorgmodel() {
		ListModelList<Morg> lm = null;
		try {
			lm = new ListModelList<Morg>(AppData.getMorg());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mproducttype> getMproducttype() {
		ListModelList<Mproducttype> lm = null;
		try {
			lm = new ListModelList<Mproducttype>(
					AppData.getMproducttype("productgroupcode = '" + productgroupcode + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mproduct> getMproductmodel() {
		ListModelList<Mproduct> lm = null;
		try {
			lm = new ListModelList<Mproduct>(AppData.getMproduct());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	@SuppressWarnings("unchecked")
	public ListModelList<String> getProductcomboModel() {
		ListModelList<String> lm = null;
		try {
			lm = new ListModelList<String>(new MproductDAO().listStr("productcode"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Mproduct getObjForm() {
		return objForm;
	}

	public void setObjForm(Mproduct objForm) {
		this.objForm = objForm;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getProductgroupcode() {
		return productgroupcode;
	}

	public void setProductgroupcode(String productgroupcode) {
		this.productgroupcode = productgroupcode;
	}

	public String getProductgroupname() {
		return productgroupname;
	}

	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public Morg getMorgsearch() {
		return morgsearch;
	}

	public void setMorgsearch(Morg morgsearch) {
		this.morgsearch = morgsearch;
	}
}
