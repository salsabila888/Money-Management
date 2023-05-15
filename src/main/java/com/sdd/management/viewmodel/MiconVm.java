package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

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
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.image.AImage;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MiconDAO;
import com.sdd.caption.domain.Micon;
import com.sdd.caption.model.MiconListModel;
import com.sdd.caption.model.MmenuListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

@SuppressWarnings("unused")
public class MiconVm {

	private org.hibernate.Session session;
	private MiconDAO iconDao = new MiconDAO();
	private Transaction transaction;

	private MiconListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private boolean isInsert;

	private Media media;
	private Micon objIcon;
	private String iconname;

	
	@Wire
	private Button btCancel;
	@Wire
	private Button btDelete;
	@Wire
	private Button btSave;
	@Wire
	private Listbox listbox;
	@Wire
	private Image pics;
	@Wire
	private Grid grid;
	@Wire
	private Window winParameterIcon;
	@Wire
	private Listheader headerAction;
	@Wire
	private Textbox textbox;
	@Wire
	private Paging paging;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("isview") final String isview) {
		Selectors.wireComponents(view, this, false);
		System.out.println("Icon View Model");

		// hide groupbox pada popup di mmenu
		if (isview != null && isview.equals("Y")) {
			grid.setVisible(false);
			headerAction.setVisible(true);
		}
		// -----------------------------------

		paging.addEventListener("onPaging", new EventListener() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});
		
		if (listbox != null) {
			listbox.setItemRenderer(new ListitemRenderer<Micon>() {

				@Override
				public void render(Listitem item, final Micon data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf(index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getIconname());
					item.appendChild(cell);
					cell = new Listcell(data.getIconpath());
					item.appendChild(cell);
					cell = new Listcell(data.getUrl());
					item.appendChild(cell);

					// memasukan data berupa gambar pada cell listbox
					cell = new Listcell();
					Image image = new Image();
					image.setSrc(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + data.getIconpath());
					image.setWidth("50px");
					image.setHeight("50px");
					cell.appendChild(image);
					item.appendChild(cell);
					// ---------------------------------------------------

					cell = new Listcell();
					Button btSelect = new Button("Select");
					btSelect.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {

							Event closeEvent = new Event("onClose", winParameterIcon, data);
							Events.postEvent(closeEvent);
						}
					});

					// memunculkan btn select saat popup pada mmenu dibuka
					if (isview != null)
						if (isview.equals("Y"))
							cell.appendChild(btSelect);
						else 
							cell.appendChild(new Label());
					item.appendChild(cell);

				}
				// ------------------------------------------------------------------------------------------------

			});
		}

		listbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {

				/*
				 * Map<String, Micon> map = new HashMap<String, Micon>(); map.put("img",
				 * objIcon);
				 */

				
				
				btDelete.setDisabled(false);
				btSave.setDisabled(false);
				btSave.setLabel(Labels.getLabel("common.update"));

				// memunculkan kembali gambar yang disimpan ketika cell pada listbox diklik
				if (objIcon.getIconpath() != null) {
					System.out.println(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objIcon.getIconpath());
					AImage aim = new AImage(Executions.getCurrent().getDesktop().getWebApp().getRealPath(
							AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objIcon.getIconpath().trim()));
					System.out.println(aim);

					org.zkoss.image.Image img = (org.zkoss.image.Image) aim;
					System.out.println(img);
					pics.setWidth("100px");
					pics.setHeight("100px");
					pics.setContent(img);
					BindUtils.postNotifyChange(null, null, MiconVm.this, "pics");

				}
				// --------------------------------------------------------------------------
				
			}
		});
		needsPageUpdate = true;
		doReset();
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			boolean isValid = true;
			if (media != null) {
				objIcon.setIconpath(media.getName());
				String path = Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objIcon.getIconpath());
				objIcon.setUrl(path);
				
				File file = new File(Executions.getCurrent().getDesktop().getWebApp()
						.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objIcon.getIconpath()));

				if (file.exists()) {
					isValid = false;
					Messagebox.show("Icon file is already exist", "Error", Messagebox.OK, Messagebox.INFORMATION);
				} else {
					if (media.isBinary()) {
						Files.copy(
								new File(Executions.getCurrent().getDesktop().getWebApp()
										.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objIcon.getIconpath())),
								media.getStreamData());

					} else {
						BufferedWriter writer = new BufferedWriter(new FileWriter(Executions.getCurrent().getDesktop()
								.getWebApp().getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objIcon.getIconpath())));
						Files.copy(writer, media.getReaderData());
						writer.close();
					}

					System.out.println(Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objIcon.getIconpath()));
					
				}

			}
			
			if (isValid) {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				iconDao.save(session, objIcon);
				transaction.commit();
				session.close();
				
				if (isInsert) {
					Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center",
							3000);
					System.out.println("success");
				} else
					Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center",
							3000);
				System.out.println("unsuccess");
				
				doReset();
			}
		} catch (HibernateException e) {
			transaction.rollback();
			if (isInsert)
				objIcon.setMiconpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}

	}

	/*public void refreshModel() {
		orderby = ""
				+ "iconname";
		try {
			listbox.setModel(new ListModelList<Micon>(new MiconDAO().listByFilter(filter, orderby)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public void refreshModel(int activePage) {
		orderby = "iconname";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MiconListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		listbox.setModel(model);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("objIcon")
	public void doDelete() {
		try {
			Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								try {
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();

									File file = new File(Executions.getCurrent().getDesktop().getWebApp().getRealPath(
											AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objIcon.getIconpath()));
									if (file.exists())
										file.delete();

									iconDao.delete(session, objIcon);
									transaction.commit();
									session.close();

									Clients.showNotification(Labels.getLabel("common.delete.success"), "info", null,
											"middle_center", 3000);

									doReset();
									BindUtils.postNotifyChange(null, null, MiconVm.this, "objIcon");
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

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				String iconname = (String) ctx.getProperties("iconname")[0].getValue();

				if (iconname == null || "".equals(iconname.trim()))
					this.addInvalidMessage(ctx, "iconname", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	@Command
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
		media = event.getMedia();
		if (media instanceof org.zkoss.image.Image) {
			org.zkoss.image.Image img = (org.zkoss.image.Image) media;
			pics.setWidth("100px");
			pics.setHeight("100px");
			pics.setContent(img);
			btSave.setDisabled(false);
		} else {
			Messagebox.show("Not an image: " + media, "Error", Messagebox.OK, Messagebox.ERROR);
			// break; //not to show too many errors
		}
	}

	@Command
	@NotifyChange("objIcon")
	public void doCancel() {
		doReset();
		objIcon.setIconpath(null);
	}

	
	@NotifyChange("objIcon")
	private void doReset() {
		isInsert = true;
		objIcon = new Micon();
		org.zkoss.image.Image img = null;
		pics.setContent(img);
		pics.setWidth("");
		pics.setHeight("");
		btDelete.setDisabled(true);
		btSave.setDisabled(true);
		btSave.setLabel(Labels.getLabel("common.save"));
		refreshModel(pageStartNumber);
		media = null;
	}

	@NotifyChange("*")
	@Command
	public void doSearch() {
		filter = "";
		if (iconname != null && iconname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += " iconname like '%" + iconname.trim().toUpperCase() + "%'";
		}

		refreshModel(pageStartNumber);
		isInsert = false;
		btCancel.setDisabled(false);
		btDelete.setDisabled(false);
	}

	public ListModelList<Micon> getModelIcon() {
		ListModelList<Micon> model = null;
		try {
			List<Micon> objList = iconDao.listByFilter("0=0", "miconpk ");
			model = new ListModelList<>(objList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	public Micon getObjIcon() {
		return objIcon;
	}

	public void setObjIcon(Micon objIcon) {
		this.objIcon = objIcon;
	}

	public String getIconname() {
		return iconname;
	}

	public void setIconname(String iconname) {
		this.iconname = iconname;
	}


}
