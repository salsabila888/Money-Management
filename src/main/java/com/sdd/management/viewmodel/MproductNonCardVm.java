package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
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
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Tr;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlNativeComponent;
import org.zkoss.zk.ui.Sessions;
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
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproductowner;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.MproductListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MproductNonCardVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private Session session;
	private Transaction transaction;

	private MproductListModel model;
	private MproductDAO oDao = new MproductDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mproduct objForm;
	private Mproducttype objItem;
	private String productcode;
	private String productname;
	private String productgroupcode;
	private String productgroupname;

	private String title;

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
	private Combobox cbProductunit, cbPrdOwner;
	@Wire
	private Intbox ibProductunitqty;
	@Wire
	private Tr f;
	@Wire
	private Div divForm;
	@Wire
	private Textbox tbJenisproduct;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg) {
		Selectors.wireComponents(view, this, false);

		productgroupcode = arg;
		productgroupname = AppData.getProductgroupLabel(productgroupcode);

		title = "Form Master Jenis Produk " + productgroupname;

		if (productgroupcode.equals(AppUtils.PRODUCTGROUP_SUPPLIES)) {
			cbProductunit.setDisabled(false);
			ibProductunitqty.setReadonly(false);
			/*
			 * HtmlNativeComponent f = (HtmlNativeComponent)
			 * divForm.getFirstChild().getChildren().get(15); f.setDynamicProperty("style",
			 * "display:none");
			 */
		} else if (productgroupcode.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			f.setVisible(true);
		} else {
			cbProductunit.setDisabled(true);
			ibProductunitqty.setReadonly(true);
			/*
			 * HtmlNativeComponent f = (HtmlNativeComponent)
			 * divForm.getFirstChild().getChildren().get(15); f.setClientAttribute("style",
			 * "display:none");
			 */
		}

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
			listbox.setItemRenderer(new ListitemRenderer<Mproduct>() {

				@Override
				public void render(Listitem item, final Mproduct data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getMproducttype().getProductgroupname());
					item.appendChild(cell);
					cell = new Listcell(String.valueOf(data.getProductcode()));
					item.appendChild(cell);
					cell = new Listcell(data.getProductname());
					item.appendChild(cell);
					cell = new Listcell(data.getMproducttype().getLaststock() == null ? "0"
							: NumberFormat.getInstance().format(data.getMproducttype().getLaststock()));
					item.appendChild(cell);
					cell = new Listcell(data.getMproducttype().getStockinjected() == null ? "0"
							: NumberFormat.getInstance().format(data.getMproducttype().getStockinjected()));
					item.appendChild(cell);
					cell = new Listcell(data.getMproducttype().getStockmin() == null ? "0"
							: NumberFormat.getInstance().format(data.getMproducttype().getStockmin()));
					item.appendChild(cell);
					cell = new Listcell(
							data.getMproducttype().getCoano() != null ? data.getMproducttype().getCoano() : "-");
					item.appendChild(cell);
					cell = new Listcell(data.getMproducttype().getMproductowner() != null
							? data.getMproducttype().getMproductowner().getMbranch().getBranchname()
							: "-");
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
					tbJenisproduct.setReadonly(true);
					cbPrdOwner.setValue(objForm.getMproducttype().getMproductowner().getMbranch().getBranchname());
				}
			}
		});
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "productgroupcode, productname";
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
		filter = "productgroupcode = '" + productgroupcode + "'";
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
	}

	@Command
	@NotifyChange("*")
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
			objForm.getMproducttype().setProducttype(objForm.getProductname());
			objForm.getMproducttype().setLastupdated(new Date());
			objForm.getMproducttype().setUpdatedby(oUser.getUserid());
			if (isInsert) {
				objForm.getMproducttype().setLaststock(0);
				objForm.getMproducttype().setIsalertstockpagu("N");
				objForm.getMproducttype().setIsblockpagu("N");
				objForm.getMproducttype().setStockinjected(0);
				if (objForm.getMproducttype().getStockmin() == null)
					objForm.getMproducttype().setStockmin(0);
			}
			mproducttypeDao.save(session, objForm.getMproducttype());
			objForm.setIsdlvhome("N");
			objForm.setIsmm("N");
			if (productgroupname.equals(AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_PINPAD))) {
				objForm.setIsopr("Y");
			}
			objForm.setIsopr("Y");
			objForm.setProductgroup(objForm.getMproducttype().getProductgroupcode());
			objForm.setLastupdated(new Date());
			objForm.setUpdatedby(oUser.getUserid());
			oDao.save(session, objForm);
			transaction.commit();
			session.close();
			if (isInsert) {
				needsPageUpdate = true;
				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 3000);
			} else
				Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
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

									mproducttypeDao.delete(session, objForm.getMproducttype());
									transaction.commit();
									session.close();

									Clients.showNotification(Labels.getLabel("common.delete.success"), "info", null,
											"middle_center", 3000);

									needsPageUpdate = true;
									doReset();
									BindUtils.postNotifyChange(null, null, MproductNonCardVm.this, "objForm");
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
			tbJenisproduct.setReadonly(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("objForm")
	public void doReset() {
		isInsert = true;
		objForm = new Mproduct();
		objForm.setMproducttype(new Mproducttype());
		objForm.getMproducttype().setProductgroupcode(productgroupcode);
		objForm.getMproducttype().setProductgroupname(productgroupname);
		objForm.getMproducttype().setSlacountertype(AppUtils.SLACOUNTERTYPE_NOCOUNTING);
		objForm.getMproducttype().setProductunit(AppUtils.PRODUCTUNIT_PCS);
		objForm.getMproducttype().setProductunitqty(1);
		cbPrdOwner.setValue(null);
		tbJenisproduct.setReadonly(false);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
		doSearch();
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				String productcode = (String) ctx.getProperties("productcode")[0].getValue();
				String productname = (String) ctx.getProperties("productname")[0].getValue();

				if (productcode == null || "".equals(productcode.trim()))
					this.addInvalidMessage(ctx, "productcode", Labels.getLabel("common.validator.empty"));
				if (productname == null || "".equals(productname.trim()))
					this.addInvalidMessage(ctx, "productname", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public ListModelList<Mproducttype> getMproducttype() {
		ListModelList<Mproducttype> lm = null;
		try {
			lm = new ListModelList<Mproducttype>(
					AppData.getMproducttype("productgroupcode != '" + productgroupcode + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
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

	public Mproduct getObjForm() {
		return objForm;
	}

	public void setObjForm(Mproduct objForm) {
		this.objForm = objForm;
	}

	public Mproducttype getObjItem() {
		return objItem;
	}

	public void setObjItem(Mproducttype objItem) {
		this.objItem = objItem;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
