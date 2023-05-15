package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

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

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.MsysparamDAO;
import com.sdd.caption.dao.MuserDAO;
import com.sdd.caption.dao.MusergroupDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mpersovendor;
import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergroup;
import com.sdd.caption.model.MuserListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MuserLdapVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private MuserListModel model;
	private MuserDAO oDao = new MuserDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Muser objForm;
	private String userid;
	private String username;
	private String nameuser;
//	private String isvendor;
	private String usergroup;
	private String branchname;
	private String iduser;

	private List<Muser> objList = new ArrayList<>();

	@Wire
	private Textbox tbUserid;
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
//	@Wire
//	private Combobox cbPersovendor;

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
			listbox.setItemRenderer(new ListitemRenderer<Muser>() {

				@Override
				public void render(Listitem item, final Muser data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getUserid());
					item.appendChild(cell);
					cell = new Listcell(data.getUsername());
					item.appendChild(cell);
					cell = new Listcell(data.getMusergroup().getUsergroupname());
					item.appendChild(cell);
					cell = new Listcell(data.getMbranch() != null ? data.getMbranch().getBranchname() : "");
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
					tbUserid.setReadonly(true);

					iduser = objForm.getUserid().trim();
					nameuser = objForm.getUsername().trim();
					usergroup = objForm.getMusergroup() != null ? objForm.getMusergroup().getUsergroupname() : "";
					branchname = objForm.getMbranch() != null ? objForm.getMbranch().getBranchname() : "";

//					if (objForm.getMpersovendor() != null) {
//						isvendor = "Y";
//						doVendor(isvendor);
//						cbPersovendor.setValue(objForm.getMpersovendor().getVendorcode());
//						BindUtils.postNotifyChange(null, null, MuserLdapVm.this, "isvendor");
//					}

					BindUtils.postNotifyChange(null, null, MuserLdapVm.this, "iduser");
					BindUtils.postNotifyChange(null, null, MuserLdapVm.this, "nameuser");
					BindUtils.postNotifyChange(null, null, MuserLdapVm.this, "usergroup");
					BindUtils.postNotifyChange(null, null, MuserLdapVm.this, "branchname");
				}
			}
		});
		needsPageUpdate = true;
		doReset();
	}

	public void refreshModel(int activePage) {
		orderby = "userid";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MuserListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (userid != null && userid.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "userid like '%" + userid.trim().toUpperCase() + "%'";
		}
		if (username != null && username.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "username like '%" + username.trim().toUpperCase() + "%'";
		}

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
	@NotifyChange("*")
	public void doSearcByUserID() {
		try {
			if (iduser != null && iduser.trim().length() > 0) {

				Muser user = oDao.findByFilter("userid = '" + iduser.trim() + "'");
				LdapContext ctx = null;
				String ldapurl = "LDAP://192.168.46.147:389";
				try {

					List<Msysparam> objList = new MsysparamDAO()
							.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_LDAP + "'", "orderno");
					for (Msysparam param : objList) {
						if (param.getParamcode().equals(AppUtils.PARAM_LDAPURL))
							ldapurl = param.getParamvalue().trim();
					}

					Hashtable<String, String> env = new Hashtable<String, String>();
					env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
					env.put(Context.SECURITY_AUTHENTICATION, "Simple");
					env.put(Context.SECURITY_PRINCIPAL, "uid=" + iduser.trim() + ",ou=accounts,o=bni,dc=co,dc=id");// input
																													// user
																													// for
																													// access
																													// to
																													// ldap
					// env.put(Context.SECURITY_CREDENTIALS, ldappassword);
					env.put(Context.PROVIDER_URL, ldapurl);
					env.put(Context.REFERRAL, "follow");
					ctx = new InitialLdapContext(env, null);

					if (user == null) {
						SearchControls cons = new SearchControls();
						cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
						String[] attrIDs = { "uid", "cn", "branchalias" };
						cons.setReturningAttributes(attrIDs);

						NamingEnumeration<SearchResult> answer = ctx.search("ou=accounts,o=bni,dc=co,dc=id",
								"uid=" + iduser.trim().trim(), cons);
						if (answer.hasMore()) {
							Attributes attrs = answer.next().getAttributes();
							nameuser = attrs.get("cn").get().toString();

							if (attrs.get("branchalias") != null) {
								if (attrs.get("branchalias").get().toString().trim().length() > 0) {
									branchname = attrs.get("branchalias").get().toString();

									SearchControls consRole = new SearchControls();
									consRole.setSearchScope(SearchControls.SUBTREE_SCOPE);
									String[] attrRole = { "captionrole" };
									consRole.setReturningAttributes(attrRole);

									NamingEnumeration<SearchResult> answerRole = ctx
											.search("ou=bniapps,o=bni,dc=co,dc=id", "uid=" + iduser.trim(), consRole);
									if (answerRole.hasMore()) {
										Attributes attrsRole = answerRole.next().getAttributes();
										if (attrs.get("captionrole") != null) {
											if (attrs.get("captionrole").get().toString().trim().length() > 0) {
												usergroup = attrsRole.get("captionrole").get().toString();
											}
										} else {
											Messagebox.show("User Role not found.");
										}
									} else {
										Messagebox.show("User Role not found.");
									}
								} else {
									Messagebox.show("Branch ID not found.");
								}
							} else {
								Messagebox.show("Branch ID not found.");
							}
						} else {
							Messagebox.show("User ID tidak ditemukan di LDAP", "Info", Messagebox.OK,
									Messagebox.INFORMATION);
						}
					} else {
						Messagebox.show("User " + iduser + " sudah terdaftar di Caption.", "Info", Messagebox.OK,
								Messagebox.INFORMATION);
					}
					System.out.println("LDAP Connection: COMPLETE");
				} catch (NamingException nex) {
					nex.printStackTrace();
					Messagebox.show("LDAP Connection: FAILED.", "Info", Messagebox.OK, Messagebox.INFORMATION);
					System.out.println(" Auth is False");
				} catch (Exception e) {
					e.printStackTrace();
					Messagebox.show(e.getMessage(), "Info", Messagebox.OK, Messagebox.INFORMATION);
				}

			} else {
				Messagebox.show("Silahkan isi User ID.", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void save() {
		try {
			List<Musergroup> musergroup = new MusergroupDAO().listByFilter("usergroupcode = '" + usergroup.trim() + "'",
					"usergroupcode");
			if (musergroup.size() > 0) {
				List<Mbranch> branchlist = new MbranchDAO().listByFilter("branchid = '" + branchname.trim() + "'",
						"branchid");
				if (branchlist.size() > 0) {
					Muser oUser = (Muser) zkSession.getAttribute("oUser");
					if (oUser == null)
						oUser = new Muser();

					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();

					objForm.setUserid(iduser.trim());
					objForm.setUsername(nameuser.trim());
					objForm.setMbranch(branchlist.get(0));
					objForm.setMusergroup(musergroup.get(0));

					if (isInsert) {
						objForm.setUpdatedby(oUser.getUserid());
						objForm.setLastupdated(new Date());
					} else {
						objForm.setUpdatedby(oUser.getUserid());
						objForm.setLastupdated(new Date());
					}

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
				} else {
					Messagebox.show("ID Cabang belum terdaftar di Caption.", "Info", Messagebox.OK,
							Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("User Role belum terdaftar di Caption.", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (HibernateException e) {
			transaction.rollback();
			if (isInsert)
				objForm.setMuserpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			if (isInsert)
				objForm.setMuserpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
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
									BindUtils.postNotifyChange(null, null, MuserLdapVm.this, "objForm");
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

//	@Command
//	public void doVendor(@BindingParam("item") String item) {
//		if (item != null && item.equals("N")) {
//			cbPersovendor.setVisible(false);
//		} else if (item.equals("Y")) {
//			cbPersovendor.setVisible(true);
//		}
//	}

	@NotifyChange("*")
	public void doReset() {
		isInsert = true;
//		isvendor = "N";
//		doVendor(isvendor);
		objForm = new Muser();
		iduser = "";
		username = "";
		usergroup = "";
		branchname = "";
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
		tbUserid.setReadonly(false);
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {

				if (branchname == null || "".equals(branchname.trim()))
					this.addInvalidMessage(ctx, "branchname", Labels.getLabel("common.validator.empty"));
				if (iduser == null || "".equals(iduser.trim()))
					this.addInvalidMessage(ctx, "userid", Labels.getLabel("common.validator.empty"));
				if (nameuser == null || "".equals(nameuser.trim()))
					this.addInvalidMessage(ctx, "username", Labels.getLabel("common.validator.empty"));
				if (usergroup == null || "".equals(usergroup.trim()))
					this.addInvalidMessage(ctx, "usergroup", Labels.getLabel("common.validator.empty"));
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
				cell.setCellValue("Daftar User Caption");
				rownum++;
				Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
				datamap.put(0, new Object[] { "No", "Kode User", "Nama User", "Group User", "Vendor Perso" });
				no = 2;
				for (Muser data : objList) {
					datamap.put(no,
							new Object[] { no - 1, data.getUserid(), data.getUsername(),
									data.getMusergroup().getUsergroupname(),
									data.getMpersovendor() != null ? data.getMpersovendor().getVendorcode() : "OPR" });
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
				String filename = "LIST_USER" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
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

	public ListModelList<Mbranch> getMbranch() {
		ListModelList<Mbranch> lm = null;
		try {
			lm = new ListModelList<Mbranch>(AppData.getMbranch());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public ListModelList<Mpersovendor> getMpersovendormodel() {
		ListModelList<Mpersovendor> lm = null;
		try {
			lm = new ListModelList<Mpersovendor>(AppData.getMpersovendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Muser getObjForm() {
		return objForm;
	}

	public void setObjForm(Muser objForm) {
		this.objForm = objForm;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsergroup() {
		return usergroup;
	}

	public void setUsergroup(String usergroup) {
		this.usergroup = usergroup;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public String getIduser() {
		return iduser;
	}

	public void setIduser(String iduser) {
		this.iduser = iduser;
	}

	public String getNameuser() {
		return nameuser;
	}

	public void setNameuser(String nameuser) {
		this.nameuser = nameuser;
	}
}
