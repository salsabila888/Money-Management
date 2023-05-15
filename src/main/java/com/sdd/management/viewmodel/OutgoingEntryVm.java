package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.SimpleListModel;

import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TnotifDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tnotif;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.ListModelFlyweight;
import com.sdd.utils.db.StoreHibernateUtil;

public class OutgoingEntryVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private ToutgoingDAO oDao = new ToutgoingDAO();
	private TnotifDAO tnotifDao = new TnotifDAO();
	private MproductDAO mproductDao = new MproductDAO();
	// private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private Session session;
	private Transaction transaction;

	private Toutgoing objForm;
	private Integer total;
	private int gridno;

	private ListModelList<Mproduct> mproductmodel;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Combobox cbProduct;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Toutgoing obj) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();
		if (obj != null) {
			objForm = obj;
			btnSave.setVisible(false);
			grid.setVisible(false);
		}
	}

	@Command
	@NotifyChange("mproductmodel")
	public void doProductLoad(@BindingParam("item") String item) {
		if (item != null) {
			try {
				mproductmodel = new ListModelList<>(
						AppData.getMproduct("productgroup = '" + item + "'", "productcode"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Command
	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public void setProductAutocomplete(@BindingParam("item") final String item) {
		if (item != null) {
			try {
				List<Mproduct> oList = mproductDao.listByFilter("productgroup = '" + item + "'", "productcode");
				cbProduct.setModel(new SimpleListModel(oList) {
					public ListModel getSubModel(Object value, int nRows) {
						if (value != null && value.toString().trim().length() > AppUtils.AUTOCOMPLETE_MINLENGTH) {
							String nameStartsWith = value.toString().trim().toUpperCase();
							List data = mproductDao.startsWith(AppUtils.AUTOCOMPLETE_MAXROWS, nameStartsWith,
									"productgroup = '" + item + "'");
							return ListModelFlyweight.create(data, nameStartsWith, "mproduct");
						}
						return ListModelFlyweight.create(Collections.emptyList(), "", "mproduct");
					}
				});

				cbProduct.setItemRenderer(new ComboitemRenderer<Mproduct>() {

					@Override
					public void render(Comboitem item, Mproduct data, int index) throws Exception {
						item.setLabel(data.getProductcode());
						item.setDescription(data.getProductname());
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addGriddata(final Toutgoing data) {
		final Row row = new Row();
		row.appendChild(new Label(String.valueOf(++gridno)));
		row.appendChild(new Label(data.getOutgoingid()));
		row.appendChild(new Label(datelocalFormatter.format(data.getEntrytime())));
		row.appendChild(new Label(data.getMproduct().getMproducttype().getProducttype()));
		row.appendChild(new Label(data.getMproduct().getProductcode()));
		row.appendChild(new Label(data.getMproduct().getProductname()));
		row.appendChild(new Label(NumberFormat.getInstance().format(data.getItemqty())));
		row.appendChild(new Label(data.getMemo()));
		Button btndel = new Button("Hapus");
		btndel.setAutodisable("self");
		btndel.setClass("btn btn-danger btn-sm");
		btndel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									doDelete(data);
									grid.getRows().removeChild(row);
								}
							}
						});
			}

		});
		row.appendChild(btndel);
		grid.getRows().insertBefore(row, grid.getRows().getFirstChild());
	}

	@Command
	@NotifyChange("*")
	public void doDelete(Toutgoing obj) {
		if (obj.getStatus().equals(AppUtils.STATUS_INVENTORY_OUTGOINGAPPROVED)) {
			Messagebox.show("Data tidak bisa dihapus karena sudah di approve ", "Info", Messagebox.OK,
					Messagebox.ERROR);
		} else {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			try {
				obj.getMproduct().getMproducttype()
						.setStockreserved(obj.getMproduct().getMproducttype().getStockreserved() - obj.getItemqty());
				oDao.delete(session, obj);
				transaction.commit();
				Clients.showNotification("Hapus data outgoing berhasil.", "info", null, "middle_center", 3000);
			} catch (HibernateException e) {
				transaction.rollback();
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				session.close();
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		Integer stockreserved = objForm.getMproduct().getMproducttype().getStockreserved();
		if (stockreserved == null)
			stockreserved = 0;
		if (objForm.getMproduct().getMproducttype().getLaststock() != null
				&& objForm.getItemqty() <= objForm.getMproduct().getMproducttype().getLaststock() - stockreserved) {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			try {
				objForm.setEntryby(oUser.getUserid());
				objForm.setEntrytime(new Date());
				objForm.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL);
				objForm.setLastupdated(new Date());
				objForm.setUpdatedby(oUser.getUserid());
				oDao.save(session, objForm);

				Mproducttype objStock = objForm.getMproduct().getMproducttype();
				objStock.setLaststock(objStock.getLaststock() - objForm.getItemqty());
				objStock.setStockreserved(objStock.getStockreserved() - stockreserved);
				if (objStock.getStockreserved() < 0)
					objStock.setStockreserved(0);
				new MproducttypeDAO().save(session, objStock);

				transaction.commit();
				Clients.showNotification("Entri data outgoing berhasil", "info", null, "middle_center", 3000);
				addGriddata(objForm);
				doReset();
			} catch (HibernateException e) {
				transaction.rollback();
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				session.close();
			}
		} else {
			Messagebox.show("Stock tidak mencukupi", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@NotifyChange("*")
	public void doReset() {
		objForm = new Toutgoing();
		objForm.setEntrytime(new Date());
		cbProduct.setValue(null);
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				String outgoingid = (String) ctx.getProperties("outgoingid")[0].getValue();
				Mproduct mproduct = (Mproduct) ctx.getProperties("mproduct")[0].getValue();
				Date entrytime = (Date) ctx.getProperties("entrytime")[0].getValue();
				Integer itemqty = (Integer) ctx.getProperties("itemqty")[0].getValue();

				if (outgoingid == null || outgoingid.trim().length() == 0)
					this.addInvalidMessage(ctx, "outgoingid", Labels.getLabel("common.validator.empty"));
				if (mproduct == null)
					this.addInvalidMessage(ctx, "mproduct", Labels.getLabel("common.validator.empty"));
				if (entrytime == null)
					this.addInvalidMessage(ctx, "entrytime", Labels.getLabel("common.validator.empty"));
				if (itemqty == null)
					this.addInvalidMessage(ctx, "itemqty", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public Toutgoing getObjForm() {
		return objForm;
	}

	public void setObjForm(Toutgoing objForm) {
		this.objForm = objForm;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public ListModelList<Mproduct> getMproductmodel() {
		return mproductmodel;
	}

	public void setMproductmodel(ListModelList<Mproduct> mproductmodel) {
		this.mproductmodel = mproductmodel;
	}

}
