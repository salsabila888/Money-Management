package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Map;

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
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Tr;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproductowner;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.MproductListModel;
import com.sdd.caption.model.MproducttypeListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.caption.utils.ProductgroupBean;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MproducttypeNonCardVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private MproducttypeListModel model;
	private MproducttypeDAO oDao = new MproducttypeDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mproducttype objForm;
	private String productgroupcode;
	private String productgroupname;
	private String productorg;
	private String producttype;

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
	private Combobox cbPrdOwner;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);

		productgroupcode = AppUtils.PRODUCTGROUP_PINPAD;
		productgroupname = AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_PINPAD); 

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
			listbox.setItemRenderer(new ListitemRenderer<Mproducttype>() {

				@Override
				public void render(Listitem item, final Mproducttype data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getProductgroupname());
					item.appendChild(cell);
					cell = new Listcell(data.getProducttype());
					item.appendChild(cell);
					cell = new Listcell(
							data.getLaststock() != null ? NumberFormat.getInstance().format(data.getLaststock()) : "0");
					item.appendChild(cell);
					cell = new Listcell(
							data.getStockreserved() != null ? NumberFormat.getInstance().format(data.getStockreserved())
									: "0");
					item.appendChild(cell);
					cell = new Listcell(
							data.getStockmin() != null ? NumberFormat.getInstance().format(data.getStockmin()) : "0");
					item.appendChild(cell);

				}
			});
		}

		listbox.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				if (listbox.getSelectedIndex() != -1) {
					isInsert = false;
					cbPrdOwner.setValue(objForm.getMproductowner().getMbranch().getBranchname());
					btnSave.setLabel(Labels.getLabel("common.update"));
					btnCancel.setDisabled(false);
					btnDelete.setDisabled(false);

				}
			}
		});
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "productorg";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new MproducttypeListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		filter = "productgroupcode = '" + productgroupcode + "'";
		if (producttype != null && producttype.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "producttype like '%" + producttype.trim().toUpperCase() + "%'";
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
	@NotifyChange("*")
	public void save() {
		try {
			Mproducttype data = new MproducttypeDAO().findByFilter("producttype = '" + objForm.getProducttype() + "'");
			if (data != null) {
				Messagebox.show("Gagal menambahkan pinpad karena pinpad tipe '" + objForm.getProducttype().trim()
						+ "' sudah terdaftar.", "Peringatan", Messagebox.OK, Messagebox.EXCLAMATION);
			} else {
				try {
					Muser oUser = (Muser) zkSession.getAttribute("oUser");
					if (oUser == null)
						oUser = new Muser();

					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();

					if (isInsert) {
						objForm.setLaststock(0);
						// objForm.setStockmin(0);
						objForm.setStockreserved(0);
						objForm.setStockinjected(0);
						objForm.setIsalertstockpagu("N");
					}
					objForm.setProductgroupcode(productgroupcode);
					objForm.setProductgroupname(productgroupname);
					objForm.setMproducttypepk(objForm.getMproducttypepk());
					objForm.setLastupdated(new Date());
					objForm.setUpdatedby(oUser.getUserid());

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
					Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				} catch (Exception e) {
					Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
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
									BindUtils.postNotifyChange(null, null, MproducttypeNonCardVm.this, "*");
								} catch (HibernateException e) {
									transaction.rollback();
									Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK,
											Messagebox.ERROR);
									e.printStackTrace();
								} catch (Exception e) {
									Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK,
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
		objForm = new Mproducttype();
		objForm.setSlacountertype(AppUtils.SLACOUNTERTYPE_DATEORDER);
		objForm.setIsestcount(AppUtils.ESTIMATESTOCK_COUNTING);
		cbPrdOwner.setValue(null);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
		doSearch();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				/*
				 * String productorg = (String) ctx.getProperties("productorg")[0] .getValue();
				 */
				String producttype = (String) ctx.getProperties("producttype")[0].getValue();
				Integer stockmin = (Integer) ctx.getProperties("stockmin")[0].getValue();

				if (producttype == null || "".equals(producttype.trim()))
					this.addInvalidMessage(ctx, "producttype", Labels.getLabel("common.validator.empty"));
				if (stockmin == null)
					this.addInvalidMessage(ctx, "stockmin", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public ListModelList<Mproductowner> getMproductowners() {
		ListModelList<Mproductowner> lm = null;
		try {
			lm = new ListModelList<Mproductowner>(AppData.getMproductowner());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public Mproducttype getObjForm() {
		return objForm;
	}

	public void setObjForm(Mproducttype objForm) {
		this.objForm = objForm;
	}

	public String getProductgroupcode() {
		return productgroupcode;
	}

	public void setProductgroupcode(String productgroupcode) {
		this.productgroupcode = productgroupcode;
	}

	public String getProductorg() {
		return productorg;
	}

	public void setProductorg(String productorg) {
		this.productorg = productorg;
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

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}
}
