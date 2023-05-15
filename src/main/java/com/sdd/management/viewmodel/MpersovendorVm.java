package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MpersovendorDAO;
import com.sdd.caption.dao.MuserDAO;
import com.sdd.caption.domain.Mpersovendor;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.MpersovendorListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MpersovendorVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private MpersovendorListModel model;
	private MpersovendorDAO oDao = new MpersovendorDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mpersovendor objForm;
	private String vendorcode;
	private String vendorname;
	private String path_root;

	private Media media;

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
	private Image imgvendor;
	@Wire
	private Textbox txcode;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);

		path_root = Executions.getCurrent().getDesktop().getWebApp()
				.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH);

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
			listbox.setItemRenderer(new ListitemRenderer<Mpersovendor>() {

				@Override
				public void render(Listitem item, final Mpersovendor data, int index) throws Exception {
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
					txcode.setReadonly(true);

					if (objForm.getVendorlogo() != null && objForm.getVendorlogo().trim().length() > 0) {
						imgvendor
								.setSrc(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objForm.getVendorlogo());
					} else
						imgvendor.setSrc(null);

				}
			}
		});
	}

	public void refreshModel(int activePage) {
		orderby = "vendorname";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MpersovendorListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
			Mpersovendor data = null;
			if (isInsert) {
				data = new MpersovendorDAO().findByFilter("vendorcode = '" + objForm.getVendorcode() + "'");	
			}
			if (data != null) {
				Messagebox.show("Gagal menambah vendor perso, kode vendor '" + objForm.getVendorcode().trim()
						+ "' sudah terdaftar.", "Peringatan", Messagebox.OK, Messagebox.EXCLAMATION);
			} else {
				try {
					Muser oUser = (Muser) zkSession.getAttribute("oUser");
					if (oUser == null)
						oUser = new Muser();

					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					if (isInsert) {
						objForm.setUpdatedby(oUser.getUserid());
						objForm.setLastupdated(new Date());
					} else {
						objForm.setUpdatedby(oUser.getUserid());
						objForm.setLastupdated(new Date());
					}
					if (media != null) {
						System.out.println(path_root);
						if (media.isBinary()) {
							Files.copy(new File(path_root + "/" + media.getName()), media.getStreamData());
						} else {
							BufferedWriter writer = new BufferedWriter(
									new FileWriter(path_root + "/" + media.getName()));
							Files.copy(writer, media.getReaderData());
							writer.close();
						}
						objForm.setVendorlogo(media.getName());
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
						objForm.setMpersovendorpk(null);
					Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				} catch (Exception e) {
					if (isInsert)
						objForm.setMpersovendorpk(null);
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
									BindUtils.postNotifyChange(null, null, MpersovendorVm.this, "objForm");
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

	@Command
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			if (media instanceof org.zkoss.image.Image) {
				imgvendor.setContent((org.zkoss.image.Image) media);
			} else {
				Messagebox.show("Not an image: " + media.getName(), "Error", Messagebox.OK, Messagebox.ERROR);
				media = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("objForm")
	public void doReset() {
		isInsert = true;
		objForm = new Mpersovendor();
		refreshModel(pageStartNumber);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
		media = null;
		imgvendor.setSrc(null);
		txcode.setReadonly(false);
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				try {
					String vendorcode = (String) ctx.getProperties("vendorcode")[0].getValue();
					String vendorname = (String) ctx.getProperties("vendorname")[0].getValue();
					String vendorpicname = (String) ctx.getProperties("vendorpicname")[0].getValue();
					Integer vendorpicphone = (Integer) ctx.getProperties("vendorpicphone")[0].getValue();
					String vendorpicemail = (String) ctx.getProperties("vendorpicemail")[0].getValue();

					if (vendorcode == null || "".equals(vendorcode.trim()))
						this.addInvalidMessage(ctx, "vendorcode", Labels.getLabel("common.validator.empty"));
					if (vendorname == null || "".equals(vendorname.trim()))
						this.addInvalidMessage(ctx, "vendorname", Labels.getLabel("common.validator.empty"));
					if (vendorpicname == null || "".equals(vendorpicname.trim()))
						this.addInvalidMessage(ctx, "vendorpicname", Labels.getLabel("common.validator.empty"));

					if (vendorpicphone == null)
						this.addInvalidMessage(ctx, "vendorpicphone", Labels.getLabel("common.validator.empty"));

					if (vendorpicemail.trim() == null || "".equals(vendorpicemail.trim()))
						this.addInvalidMessage(ctx, "vendorpicemail", Labels.getLabel("common.validator.empty"));
					if (!StringUtils.emailValidator(vendorpicemail))
						this.addInvalidMessage(ctx, "vendorpicemail", "Invalid e-mail format");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public Mpersovendor getObjForm() {
		return objForm;
	}

	public void setObjForm(Mpersovendor objForm) {
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

}
