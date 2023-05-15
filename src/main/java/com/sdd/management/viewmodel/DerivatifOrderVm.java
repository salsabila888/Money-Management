package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.ListModelFlyweight;
import com.sdd.utils.db.StoreHibernateUtil;

public class DerivatifOrderVm {

	private Session session;
	private Transaction transaction;

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Tderivatif obj;
	private Mproduct mproduct;
	private TderivatifDAO oDao = new TderivatifDAO();

	private String orderno;
	private String totalorder;
	private String filename;
	private Media media;
	private String isEdit;
	private Integer pageno;

	@Wire
	private Combobox cbCabang, cbProduct;
	@Wire
	private Label fileBrowse;
	@Wire
	private Button btnBrowse, btnSave;
	@Wire
	private Datebox datebox;
	@Wire
	private Intbox jumlah;
	@Wire
	private Window winEdit;
	@Wire
	private Caption caption;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("isEdit") String isEdit,
			@ExecutionArgParam("obj") Tderivatif obj, @ExecutionArgParam("pageno") Integer pageno) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		getProductAutocomplete();
		doReset();

		if (isEdit != null && isEdit.equals("Y")) {
			this.obj = obj;
			this.isEdit = isEdit;
			this.
			datebox.setDisabled(true);
			jumlah.setDisabled(true);
			caption.setVisible(true);
			if(obj.getMproduct() != null)
				cbProduct.setValue(obj.getMproduct().getProductcode());
			else cbProduct.setValue(null);
			cbCabang.setValue(obj.getMbranch().getBranchname());
			if (obj.getFilename() != null) {
				filename = obj.getFilename();
				fileBrowse.setVisible(true);
			}
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	public void getProductAutocomplete() {
		try {
			List<Mproduct> oList = new MproductDAO().listByFilter("productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'",
					"productcode");
			cbProduct.setModel(new SimpleListModel(oList) {
				public ListModel getSubModel(Object value, int nRows) {
					if (value != null && value.toString().trim().length() > AppUtils.AUTOCOMPLETE_MINLENGTH) {
						String nameStartsWith = value.toString().trim().toUpperCase();
						List data = new MproductDAO().startsWith(AppUtils.AUTOCOMPLETE_MAXROWS, nameStartsWith,
								"productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'");
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

	@Command
	@NotifyChange("filename")
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			fileBrowse.setVisible(true);
			filename = media.getName();
			if (media != null) {
				Tderivatif file = oDao.findByFilter("filename = '" + filename.trim() + "'");
				if (file != null) {
					btnSave.setDisabled(true);
					Messagebox.show("File sudah pernah diupload", "Info", Messagebox.OK, Messagebox.INFORMATION);
				} else
					btnSave.setDisabled(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		try {
			if (isEdit != null && isEdit.equals("Y")) {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				if (media != null) {
					String path = Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_DERIVATIFFILE);

					File folder = new File(path);
					if (!folder.exists())
						folder.mkdir();

					if (media.isBinary()) {
						Files.copy(new File(folder + "/" + filename), media.getStreamData());
					} else {
						BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/" + filename));
						Files.copy(writer, media.getReaderData());
						writer.close();
					}

					obj.setFilename(filename);
				}

				oDao.save(session, obj);
				transaction.commit();
				session.close();

				Map<String, Object> map = new HashMap<>();
				map.put("pageno", pageno);
				Event closeEvent = new Event("onClose", winEdit, map);
				Events.postEvent(closeEvent);
				
				Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
				doReset();
			} else {
				Tderivatif objDupliacate = oDao.findByFilter("orderno = '" + obj.getOrderno() + "'");
				if (objDupliacate == null) {
					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();

					if (media != null) {
						String path = Executions.getCurrent().getDesktop().getWebApp()
								.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.PATH_DERIVATIFFILE);

						File folder = new File(path);
						if (!folder.exists())
							folder.mkdir();

						if (media.isBinary()) {
							Files.copy(new File(folder + "/" + filename), media.getStreamData());
						} else {
							BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/" + filename));
							Files.copy(writer, media.getReaderData());
							writer.close();
						}

						obj.setFilename(filename);
					}

					obj.setStatus(AppUtils.STATUS_DERIVATIF_WAITAPPROVAL);
					obj.setProdsla(0);
					obj.setDlvsla(0);
					obj.setSlatotal(0);
					obj.setTotalreject(0);
					obj.setTotaladj(0);
					obj.setEntryby(oUser.getUserid());
					obj.setEntrytime(new Date());
					oDao.save(session, obj);

					transaction.commit();
					session.close();
					
					Mmenu mmenu = new MmenuDAO()
							.findByFilter("menupath = '/view/derivatif/derivatifapproval.zul'");
					NotifHandler.doNotif(mmenu, oUser.getMbranch(), AppUtils.PRODUCTGROUP_CARDPHOTO, oUser.getMbranch().getBranchlevel());

					Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center",
							3000);
					doReset();
				} else {
					Messagebox.show("Nomor surat sudah pernah dimasukan", WebApps.getCurrent().getAppName(),
							Messagebox.OK, Messagebox.EXCLAMATION);
				}
			}
		} catch (HibernateException e) {
			transaction.rollback();
			Messagebox.show("Nomor surat sudah pernah dimasukan", WebApps.getCurrent().getAppName(), Messagebox.OK,
					Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				String orderno = (String) ctx.getProperties("orderno")[0].getValue();
				Integer totaldata = (Integer) ctx.getProperties("totaldata")[0].getValue();
				Date orderdate = (Date) ctx.getProperties("orderdate")[0].getValue();
				Mbranch mbranch = (Mbranch) ctx.getProperties("mbranch")[0].getValue();
				Mproduct mproduct = (Mproduct) ctx.getProperties("mproduct")[0].getValue();

				if (orderno == null || "".equals(orderno.trim()))
					this.addInvalidMessage(ctx, "orderno", Labels.getLabel("common.validator.empty"));
				if (totaldata == null)
					this.addInvalidMessage(ctx, "totaldata", Labels.getLabel("common.validator.empty"));
				if (orderdate == null)
					this.addInvalidMessage(ctx, "orderdate", Labels.getLabel("common.validator.empty"));
				if (mbranch == null)
					this.addInvalidMessage(ctx, "mbranch", Labels.getLabel("common.validator.empty"));
				if (mproduct == null)
					this.addInvalidMessage(ctx, "mproduct", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		obj = new Tderivatif();
		obj.setOrderdate(new Date());
		cbCabang.setValue(null);
		cbProduct.setValue(null);
		fileBrowse.setVisible(false);
		filename = null;
		media = null;
		btnBrowse.setDisabled(false);
		btnSave.setDisabled(false);
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

	public Tderivatif getObj() {
		return obj;
	}

	public void setObj(Tderivatif obj) {
		this.obj = obj;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getTotalorder() {
		return totalorder;
	}

	public void setTotalorder(String totalorder) {
		this.totalorder = totalorder;
	}

	public Mproduct getMproduct() {
		return mproduct;
	}

	public void setMproduct(Mproduct mproduct) {
		this.mproduct = mproduct;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
