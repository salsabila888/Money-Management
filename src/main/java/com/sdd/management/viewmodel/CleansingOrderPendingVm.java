package com.sdd.caption.viewmodel;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;

import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.ListModelFlyweight;
import com.sdd.utils.db.StoreHibernateUtil;

public class CleansingOrderPendingVm {
	
	private TembossdataDAO oDao = new TembossdataDAO();
	private MproductDAO mproductDao = new MproductDAO();
	
	private ListModelList<Mproducttype> mproducttypemodel;
	
	private Session session;
	private Transaction transaction;
		
	private Mproduct mproduct;
	private Mproduct mproduct1;
	private Integer total;
	private String filter;

	
	@Wire
	private Combobox cbProduct;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;
	@Wire
	private Button btnDelete;
	
	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		Selectors.wireComponents(view, this, false);				
		try {
			setProductAutocomplete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		doReset();
	}
	
	@Command
	@NotifyChange("mproducttypemodel")
	public void doProductLoad(@BindingParam("item") String item) {
		if (item != null) {
			try {
				mproducttypemodel = new ListModelList<>(AppData.getMproducttype("productorg = " + item + "'"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Command
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public void setProductAutocomplete() {
		try {
			List<Mproduct> oList = mproductDao.listByFilter(
					"productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'",
					"productcode");
			cbProduct.setModel(new SimpleListModel(oList) {
				public ListModel getSubModel(Object value, int nRows) {
					if (value != null
							&& value.toString().trim().length() > AppUtils.AUTOCOMPLETE_MINLENGTH) {
						String nameStartsWith = value.toString().trim()
								.toUpperCase();
						List data = mproductDao
								.startsWith(
										AppUtils.AUTOCOMPLETE_MAXROWS,
										nameStartsWith,
										"productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'");
						return ListModelFlyweight.create(data, nameStartsWith,
								"mproduct");
					}
					return ListModelFlyweight.create(Collections.emptyList(),
							"", "mproduct");
				}
			});

			cbProduct.setItemRenderer(new ComboitemRenderer<Mproduct>() {

				@Override
				public void render(Comboitem item, Mproduct data, int index)
						throws Exception {
					item.setLabel(data.getProductcode());
					item.setDescription(data.getProductname());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@Command
	@NotifyChange("*")
	public void doDelete(Tembossdata obj) {
		if (mproduct != null) {
			Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

				@Override
				public void onEvent(Event event)
						throws Exception {
					if (event.getName().equals("onOK")) {
						session = StoreHibernateUtil.openSession();
						transaction = session.beginTransaction();
						try {						
							oDao.deleteByFilter(session, filter);		
							transaction.commit();			
							Messagebox.show("Cleansing data order \npending berhasil", "info", Messagebox.OK, Messagebox.INFORMATION);
							doReset();
							BindUtils.postNotifyChange(null, null, CleansingOrderPendingVm.this, "*");
						} catch (HibernateException e){
							transaction.rollback();			
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							session.close();
						}																											
					} 									
				}				
			});	
		}
	}
	
	@Command
	@NotifyChange("*")
	public void doSearch() {
		if (mproduct != null) {
			filter = "torderdata.productcode = '" +  mproduct.getProductcode() + "'";
			try {
				mproduct1 = mproduct;
				total = oDao.pageCount(filter);
				btnDelete.setDisabled(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
		
	@NotifyChange("*")
	public void doReset() {
		cbProduct.setValue(null);
		total = null;
		mproduct1 = null;
		btnDelete.setDisabled(true);
	}
	
	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mproducttype mproduct = (Mproducttype) ctx
						.getProperties("mproducttype")[0].getValue();
				Date entrytime = (Date) ctx.getProperties("entrytime")[0].getValue();
				Integer itemqty = (Integer) ctx.getProperties("itemqty")[0].getValue();
				
				if (mproduct == null)
					this.addInvalidMessage(ctx, "mproducttype",
							Labels.getLabel("common.validator.empty"));
				if (entrytime == null)
					this.addInvalidMessage(ctx, "entrytime",
							Labels.getLabel("common.validator.empty"));
				if (itemqty == null)
					this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public ListModelList<Mproducttype> getMproducttypemodel() {
		return mproducttypemodel;
	}

	public void setMproducttypemodel(ListModelList<Mproducttype> mproducttypemodel) {
		this.mproducttypemodel = mproducttypemodel;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public Mproduct getMproduct1() {
		return mproduct1;
	}

	public void setMproduct1(Mproduct mproduct1) {
		this.mproduct1 = mproduct1;
	}
	
	

}
