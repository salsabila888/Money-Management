package com.sdd.caption.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
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
import org.zkoss.image.AImage;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Image;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.domain.Micon;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.model.MmenuListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MmenuVm {

	private org.hibernate.Session session;
	private MmenuDAO menuDao = new MmenuDAO();
	private Transaction transaction;
	
	private MmenuListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mmenu obj;
	private Micon objSelected;
	private String menuname;
	private String menugroup;

	@Wire
	private Listbox listbox;
	@Wire
	private Image picsMI, picsGI, picsSI;
	@Wire
	private Button btCancel, btDelete, btSave, btnDelSI, btnDelMI, btnDelGI, btnExport;
	@Wire
	private Combobox cbExport;
	@Wire
	private Paging paging;

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
			listbox.setItemRenderer(new ListitemRenderer<Mmenu>() {

				@Override
				public void render(Listitem item, final Mmenu data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf(index + 1));
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getMenuorderno()));
					item.appendChild(cell);
					cell = new Listcell(data.getMenugroup());
					item.appendChild(cell);

					if (data.getMenugroupicon() != null && data.getMenugroupicon().trim().length() > 0) {
						cell = new Listcell();
						Image imageGI = new Image();
						imageGI.setSrc(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + data.getMenugroupicon().trim());
						imageGI.setWidth("40px");
						imageGI.setHeight("40px");
						cell.appendChild(imageGI);
						item.appendChild(cell);
					} else {
						cell = new Listcell(data.getMenugroupicon());
						item.appendChild(cell);
					}

					cell = new Listcell(data.getMenusubgroup());
					item.appendChild(cell);

					if (data.getMenusubgroupicon() != null && data.getMenusubgroupicon().trim().length() > 0) {
						cell = new Listcell();
						Image imageSI = new Image();
						imageSI.setSrc(
								AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + data.getMenusubgroupicon().trim());
						imageSI.setWidth("40px");
						imageSI.setHeight("40px");
						cell.appendChild(imageSI);
						item.appendChild(cell);
					} else {
						cell = new Listcell(data.getMenusubgroupicon());
						item.appendChild(cell);
					}

					cell = new Listcell(data.getMenuname());
					item.appendChild(cell);

					if (data.getMenuicon() != null && data.getMenuicon().trim().length() > 0) {
						cell = new Listcell();
						Image imageMI = new Image();
						imageMI.setSrc(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + data.getMenuicon().trim());
						imageMI.setWidth("40px");
						imageMI.setHeight("40px");
						cell.appendChild(imageMI);
						item.appendChild(cell);
					} else {
						cell = new Listcell(data.getMenuicon());
						item.appendChild(cell);
					}

					cell = new Listcell(data.getMenupath());
					item.appendChild(cell);
					cell = new Listcell(data.getMenuparamname());
					item.appendChild(cell);
					cell = new Listcell(data.getMenuparamvalue());
					item.appendChild(cell);
				}
			});
		}
		listbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				if (listbox.getSelectedIndex() != -1) {
					isInsert = false;
					btDelete.setDisabled(false);
					btSave.setLabel(Labels.getLabel("common.update"));
					org.zkoss.image.Image image = null;

					if (obj.getMenuicon() == null) {
						btnDelMI.setVisible(false);
						picsMI.setContent(image);
					} else {
						System.out.println(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + obj.getMenuicon());
						AImage aim = new AImage(Executions.getCurrent().getDesktop().getWebApp()
								.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + obj.getMenuicon().trim()));
						System.out.println(aim);
	
						org.zkoss.image.Image img = (org.zkoss.image.Image) aim;
						System.out.println(img);
						picsMI.setWidth("40px");
						picsMI.setHeight("40px");
						picsMI.setContent(img);
						btnDelMI.setVisible(true);
						BindUtils.postNotifyChange(null, null, MmenuVm.this, "picsMI");
					}
	
					if (obj.getMenugroupicon() == null) {
						btnDelGI.setVisible(false);
						picsGI.setContent(image);
					} else {
						System.out.println(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + obj.getMenugroupicon());
						AImage aimGI = new AImage(Executions.getCurrent().getDesktop().getWebApp().getRealPath(
								AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + obj.getMenugroupicon().trim()));
						System.out.println(aimGI);
	
						org.zkoss.image.Image imgGI = (org.zkoss.image.Image) aimGI;
						System.out.println(imgGI);
						picsGI.setWidth("40px");
						picsGI.setHeight("40px");
						picsGI.setContent(imgGI);
						btnDelGI.setVisible(true);
						BindUtils.postNotifyChange(null, null, MmenuVm.this, "picsGI");
					}
	
					if (obj.getMenusubgroupicon() == null) {
						btnDelSI.setVisible(false);
						picsSI.setContent(image);
					} else {
						System.out.println(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + obj.getMenusubgroupicon());
						AImage aimSI = new AImage(Executions.getCurrent().getDesktop().getWebApp().getRealPath(
								AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + obj.getMenusubgroupicon().trim()));
						System.out.println(aimSI);
	
						org.zkoss.image.Image imgSI = (org.zkoss.image.Image) aimSI;
						System.out.println(imgSI);
						picsSI.setWidth("40px");
						picsSI.setHeight("40px");
						picsSI.setContent(imgSI);
						btnDelSI.setVisible(true);
						BindUtils.postNotifyChange(null, null, MmenuVm.this, "picsSI");
					}
				}
			}
		});
		needsPageUpdate = true;
		doReset();
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {

			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();

			menuDao.save(session, obj);
			transaction.commit();
			session.close();

			if (isInsert) {
				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 3000);
				System.out.println("success");
			} else
				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 3000);
			System.out.println("unsuccess");

		} catch (HibernateException e) {
			transaction.rollback();
			if (isInsert)
				Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			if (isInsert)
				Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
		doReset();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command
	@NotifyChange("obj")
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

									menuDao.delete(session, obj);
									transaction.commit();
									session.close();

									Clients.showNotification(Labels.getLabel("common.delete.success"), "info", null,
											"middle_center", 3000);

									doReset();
									BindUtils.postNotifyChange(null, null, MmenuVm.this, "obj");
								} catch (HibernateException e) {
									transaction.rollback();
									Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								} catch (Exception e) {
									Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK,
											Messagebox.ERROR);
									// System.out.println(Executions.getCurrent().getDesktop().getWebApp().getRealPath(AppUtils.FILES_ROOT_PATH
									// + AppUtils.IMAGE_PATH + obj.getMenuicon()));
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
	public void doLoadIcon(@BindingParam("arg") final String arg) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("isview", "Y");

		Window win = (Window) Executions.createComponents("/view/parameter/icon.zul", null, map);
		win.setWidth("60%");
		win.setClosable(true);
		// membuat fungsi evet saat popup close
		win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				// mendapatkan gambar dari popup icon
				if (event.getData() != null) {
					objSelected = (Micon) event.getData();
					BindUtils.postNotifyChange(null, null, MmenuVm.this, "objSelected");
					if (arg.equals("MI")) {
						obj.setMenuicon(objSelected.getIconpath());
						AImage aim = new AImage(Executions.getCurrent().getDesktop().getWebApp().getRealPath(
								AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objSelected.getIconpath().trim()));
						org.zkoss.image.Image img = (org.zkoss.image.Image) aim;
						picsMI.setWidth("40px");
						picsMI.setHeight("40px");
						picsMI.setContent(img);
						btnDelMI.setVisible(true);

					} else if (arg.equals("SI")) {
						obj.setMenusubgroupicon(objSelected.getIconpath());
						AImage aim = new AImage(Executions.getCurrent().getDesktop().getWebApp().getRealPath(
								AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objSelected.getIconpath().trim()));
						org.zkoss.image.Image img = (org.zkoss.image.Image) aim;
						picsSI.setWidth("40px");
						picsSI.setHeight("40px");
						picsSI.setContent(img);
						btnDelSI.setVisible(true);

					} else if (arg.equals("GI")) {
						obj.setMenugroupicon(objSelected.getIconpath());
						AImage aim = new AImage(Executions.getCurrent().getDesktop().getWebApp().getRealPath(
								AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objSelected.getIconpath().trim()));
						org.zkoss.image.Image img = (org.zkoss.image.Image) aim;
						picsGI.setWidth("40px");
						picsGI.setHeight("40px");
						picsGI.setContent(img);
						btnDelGI.setVisible(true);

					}

					objSelected = null;
					// -----------------------------------------------------------------------------------------------
				}
			}

		});
		// -----------------------------------------------------------------------------------------------------------
		win.doModal();
	}
	// ---------------------------------------------------------------------------------------------------------------

	@NotifyChange("obj")
	private void doReset() {
		isInsert = true;
		obj = new Mmenu();
		org.zkoss.image.Image img = null;
		picsMI.setContent(img);
		picsGI.setContent(img);
		picsSI.setContent(img);
		btDelete.setDisabled(true);
		btnDelSI.setVisible(false);
		btSave.setLabel(Labels.getLabel("common.save"));
		refreshModel(pageStartNumber);
		btnDelMI.setVisible(false);
		btnDelGI.setVisible(false);
		btnDelSI.setVisible(false);
		doSearch();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Integer menuorderno = (Integer) ctx.getProperties("menuorderno")[0].getValue();
				String menugroup = (String) ctx.getProperties("menugroup")[0].getValue();
				String menupath = (String) ctx.getProperties("menupath")[0].getValue();
				
				if (menuorderno == null)
					this.addInvalidMessage(ctx, "menuorderno", Labels.getLabel("common.validator.empty"));
				if (menugroup == null || "".equals(menugroup.trim()))
					this.addInvalidMessage(ctx, "menugroup", Labels.getLabel("common.validator.empty"));
				if (menupath == null || "".equals(menupath.trim()))
					this.addInvalidMessage(ctx, "menupath", Labels.getLabel("common.validator.empty"));
				
			}
		};
	}

/*	public void refreshModel() {
		orderby = "menuorderno";
		try {
			objList = new MmenuDAO().listByFilter(filter, orderby);
			listbox.setModel(new ListModelList<Mmenu>(objList));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public void refreshModel(int activePage) {
		orderby = "menuorderno";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MmenuListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		listbox.setModel(model);
	}

	@Command
	@NotifyChange("obj")
	public void doCancel() {
		doReset();
	}

	@Command
	public void delIcon(@BindingParam("arg") String arg) {
		org.zkoss.image.Image img = null;
		if (arg.equals("delMI")) {
			picsMI.setContent(img);
			obj.setMenuicon(null);
			btnDelMI.setVisible(false);
		}
		if (arg.equals("delGI")) {
			picsGI.setContent(img);
			obj.setMenugroupicon(null);
			btnDelGI.setVisible(false);
		}
		if (arg.equals("delSI")) {
			picsSI.setContent(img);
			obj.setMenusubgroupicon(null);
			btnDelSI.setVisible(false);
		}
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		filter = "";
		if (menugroup != null && menugroup.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += " menugroup like '%" + menugroup.trim() + "%'";
		}
		if (menuname != null && menuname.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += " menuname like '%" + menuname.trim() + "%'";
		}

		refreshModel(pageStartNumber);
		isInsert = false;
		btSave.setLabel(Labels.getLabel("common.update"));
		btCancel.setDisabled(false);
		btDelete.setDisabled(false);
	}

	public ListModelList<Mmenu> getModelIcon() {
		ListModelList<Mmenu> model = null;
		try {
			List<Mmenu> objList = menuDao.listByFilter("0=0", "menuorderno");
			model = new ListModelList<>(objList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	public Mmenu getObj() {
		return obj;
	}

	public void setObj(Mmenu obj) {
		this.obj = obj;
	}

	public Micon getObjSelected() {
		return objSelected;
	}

	public void setObjSelected(Micon objSelected) {
		this.objSelected = objSelected;
	}

	public String getMenuname() {
		return menuname;
	}

	public void setMenuname(String menuname) {
		this.menuname = menuname;
	}

	public String getMenugroup() {
		return menugroup;
	}

	public void setMenugroup(String menugroup) {
		this.menugroup = menugroup;
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

}
