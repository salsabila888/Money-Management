package com.sdd.caption.viewmodel;

import java.util.Date;

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
import org.zkoss.zhtml.Tr;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radio;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.McouriervendorDAO;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.McouriervendorListModel;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class McouriervendorVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private McouriervendorListModel model;
	private McouriervendorDAO oDao = new McouriervendorDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mcouriervendor objForm;
	private String vendorcode;
	private String vendorname;
	private String urltoken;
	private String urltracking;
	private String costumerauth;

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
	private Tr urltkn, urltrack, costumer;
	@Wire
	private Radio rbYes, rbNo, rbYesIC, rbNoIC;

	@NotifyChange("*")
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
			listbox.setItemRenderer(new ListitemRenderer<Mcouriervendor>() {

				@Override
				public void render(Listitem item, final Mcouriervendor data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getVendorcode());
					item.appendChild(cell);
					cell = new Listcell(data.getVendorname());
					item.appendChild(cell);
					cell = new Listcell(data.getVendorpicname());
					item.appendChild(cell);
					cell = new Listcell(data.getVendorpicphone());
					item.appendChild(cell);
					cell = new Listcell(data.getVendorpicemail());
					item.appendChild(cell);
					cell = new Listcell(data.getIstracking());
					item.appendChild(cell);
					cell = new Listcell(data.getIsintercity());
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

					if (objForm.getIstracking() != null) {
						if (objForm.getIstracking().trim().equals("Y")) {
							rbYes.setChecked(true);
							urltkn.setVisible(true);
							urltrack.setVisible(true);
							costumer.setVisible(true);
						} else if (objForm.getIstracking().trim().equals("N")) {
							rbNo.setChecked(true);
							urltkn.setVisible(false);
							urltrack.setVisible(false);
							costumer.setVisible(false);
						}
						BindUtils.postNotifyChange(null, null, McouriervendorVm.this, "rbYes");
						BindUtils.postNotifyChange(null, null, McouriervendorVm.this, "rbNo");
					} else if (objForm.getIsintercity() != null) {
						if (objForm.getIstracking().trim().equals("Y")) {
							rbYesIC.setChecked(true);
						} else if (objForm.getIstracking().trim().equals("N")) {
							rbNoIC.setChecked(true);
						}
						BindUtils.postNotifyChange(null, null, McouriervendorVm.this, "rbYesIC");
						BindUtils.postNotifyChange(null, null, McouriervendorVm.this, "rbNoIC");
					}
				}
			}
		});
	}

	@Command
	@NotifyChange("*")
	public void doChecked() {
		if (rbYes.isChecked()) {
			urltkn.setVisible(true);
			urltrack.setVisible(true);
			costumer.setVisible(true);
		} else {
			urltkn.setVisible(false);
			urltrack.setVisible(false);
			costumer.setVisible(false);
		}
	}

	public void refreshModel(int activePage) {
		orderby = "vendorname";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new McouriervendorListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (vendorcode != null && vendorcode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "vendorcode like '%" + vendorcode.trim().toUpperCase() + "%'";
		}
		if (vendorname != null && vendorname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "vendorname like '%" + vendorname.trim().toUpperCase() + "%'";
		}
		if (urltoken != null && urltoken.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "urltoken like '%" + urltoken.trim() + "%'";
		}
		if (urltracking != null && urltracking.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "urltracking like '%" + urltracking.trim() + "%'";
		}
		if (costumerauth != null && costumerauth.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "costumerauth like '%" + costumerauth.trim() + "%'";
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
			Mcouriervendor data = new McouriervendorDAO()
					.findByFilter("vendorcode = '" + objForm.getVendorcode() + "'");
			if (data != null) {
				Messagebox.show("Gagal menambah kurir ekspedisi, kode kurir ekspedisi '"
						+ objForm.getVendorcode().trim() + "' sudah terdaftar.", "Peringatan", Messagebox.OK,
						Messagebox.EXCLAMATION);
			} else {
				try {
					Muser oUser = (Muser) zkSession.getAttribute("oUser");
					if (oUser == null)
						oUser = new Muser();

					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					if (rbYes.isChecked()) {
						objForm.setIstracking("Y");
					} else if (rbNo.isChecked()) {
						objForm.setIstracking("N");
					}

					if (rbYesIC.isChecked()) {
						objForm.setIsintercity("Y");
						if (isInsert) {
							Mcouriervendor intercity = oDao.findByFilter("isintercity = 'Y'");
							if (intercity != null) {
								intercity.setIsintercity("N");
								oDao.save(session, intercity);
							}
						} else {
							Mcouriervendor intercity = oDao.findByFilter("isintercity = 'Y'");
							if (intercity != null && intercity.getMcouriervendorpk() != objForm.getMcouriervendorpk()) {
								intercity.setIsintercity("N");
								oDao.save(session, intercity);
							}
						}
					} else if (rbNoIC.isChecked()) {
						objForm.setIsintercity("N");
					}

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
				} catch (HibernateException e) {
					transaction.rollback();
					if (isInsert)
						objForm.setMcouriervendorpk(null);
					Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				} catch (Exception e) {
					if (isInsert)
						objForm.setMcouriervendorpk(null);
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
									BindUtils.postNotifyChange(null, null, McouriervendorVm.this, "objForm");
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
		objForm = new Mcouriervendor();
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		urltkn.setVisible(false);
		urltrack.setVisible(false);
		costumer.setVisible(false);
		rbNo.setChecked(true);
		rbYesIC.setChecked(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {
					String vendorcode = (String) ctx.getProperties("vendorcode")[0].getValue();
					String vendorname = (String) ctx.getProperties("vendorname")[0].getValue();
					String vendorpicname = (String) ctx.getProperties("vendorpicname")[0].getValue();
					String vendorpicphone = (String) ctx.getProperties("vendorpicphone")[0].getValue();
					String vendorpicemail = (String) ctx.getProperties("vendorpicemail")[0].getValue();

					if (rbYes.isChecked()) {
						String urltoken = (String) ctx.getProperties("urltoken")[0].getValue();
						String urltracking = (String) ctx.getProperties("urltracking")[0].getValue();
						String costumerauth = (String) ctx.getProperties("costumerauth")[0].getValue();

						if (urltoken == null || "".equals(urltoken.trim()))
							this.addInvalidMessage(ctx, "urltoken", Labels.getLabel("common.validator.empty"));
						if (urltracking == null || "".equals(urltracking.trim()))
							this.addInvalidMessage(ctx, "urltracking", Labels.getLabel("common.validator.empty"));
						if (costumerauth == null || "".equals(costumerauth.trim()))
							this.addInvalidMessage(ctx, "costumerauth", Labels.getLabel("common.validator.empty"));
					}

					if (vendorcode == null || "".equals(vendorcode.trim()))
						this.addInvalidMessage(ctx, "vendorcode", Labels.getLabel("common.validator.empty"));
					if (vendorname == null || "".equals(vendorname.trim()))
						this.addInvalidMessage(ctx, "vendorname", Labels.getLabel("common.validator.empty"));
					if (vendorpicname == null || "".equals(vendorpicname.trim()))
						this.addInvalidMessage(ctx, "vendorpicname", Labels.getLabel("common.validator.empty"));
					if (vendorpicphone == null)
						this.addInvalidMessage(ctx, "vendorpicphone", Labels.getLabel("common.validator.empty"));
					if (!StringUtils.isNumeric(vendorpicphone))
						this.addInvalidMessage(ctx, "vendorpicphone", "Invalid  format");
					if (vendorpicemail.trim() == null || "".equals(vendorpicemail.trim()))
						this.addInvalidMessage(ctx, "vendorpicemail", Labels.getLabel("common.validator.empty"));
					if (!StringUtils.emailValidator(vendorpicemail.trim()))
						this.addInvalidMessage(ctx, "vendorpicemail", "Invalid e-mail format");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public Mcouriervendor getObjForm() {
		return objForm;
	}

	public void setObjForm(Mcouriervendor objForm) {
		this.objForm = objForm;
	}

	public String getVendorcode() {
		return vendorcode;
	}

	public void setVendorcode(String vendorcode) {
		this.vendorcode = vendorcode;
	}

	public String getVendorname() {
		return vendorname;
	}

	public void setVendorname(String vendorname) {
		this.vendorname = vendorname;
	}

	public String getUrltoken() {
		return urltoken;
	}

	public void setUrltoken(String urltoken) {
		this.urltoken = urltoken;
	}

	public String getUrltracking() {
		return urltracking;
	}

	public void setUrltracking(String urltracking) {
		this.urltracking = urltracking;
	}

	public String getCostumerauth() {
		return costumerauth;
	}

	public void setCostumerauth(String costumerauth) {
		this.costumerauth = costumerauth;
	}

}
