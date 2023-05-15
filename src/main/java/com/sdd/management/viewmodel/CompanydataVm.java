package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

import com.sdd.caption.dao.MsysparamDAO;
import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class CompanydataVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	String path_root;

	private MsysparamDAO oDao = new MsysparamDAO();

	private Media media;

	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		try {
			path_root = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH);
			List<Msysparam> params = oDao.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_COMPANYDATA + "'",
					"orderno");
			Row row = null;
			for (Msysparam obj : params) {
				row = new Row();
				row.appendChild(new Label(obj.getParamname()));
				if (obj.getIsmasked().equals("I")) {
					final Image img = new Image(
							AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + obj.getParamvalue());
					img.setHeight("70px");
					img.setWidth("130px");
					Button btn = new Button("Browse");
					btn.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btn.setUpload("true");
					btn.addEventListener(Events.ON_UPLOAD, new EventListener<UploadEvent>() {

						@Override
						public void onEvent(UploadEvent event) throws Exception {
							try {
								media = event.getMedia();
								if (media instanceof org.zkoss.image.Image) {
									img.setContent((org.zkoss.image.Image) media);
								} else {
									media = null;
									Messagebox.show("Not an image: " + media, "Error", Messagebox.OK, Messagebox.ERROR);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					Div div = new Div();
					div.appendChild(img);
					div.appendChild(btn);
					row.appendChild(div);
				} else {
					Textbox tbox = new Textbox(obj.getParamvalue());
					tbox.setCols(100);
					tbox.setMaxlength(100);
					if (obj.getIsmasked().equals("Y"))
						tbox.setType("password");
					row.appendChild(tbox);
				}
				// row.setId(obj.getParamcode());
				row.setAttribute("obj", obj);
				grid.getRows().appendChild(row);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doSave() {
		Session session = null;
		Transaction transaction = null;
		try {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();

			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				if (comp.getChildren().size() > 1) {
					if (comp.getChildren().get(1) instanceof Textbox) {
						Textbox tbox = (Textbox) comp.getChildren().get(1);
						Msysparam obj = (Msysparam) comp.getAttribute("obj");
						obj.setParamvalue(tbox.getValue());
						obj.setUpdatedby(oUser.getUserid());
						obj.setLastupdated(new Date());
						oDao.save(session, obj);
					}
				}
			}

			if (media != null) {
				System.out.println(path_root);
				if (media.isBinary()) {
					Files.copy(new File(path_root + "/" + media.getName()), media.getStreamData());
				} else {
					BufferedWriter writer = new BufferedWriter(new FileWriter(path_root + "/" + media.getName()));
					Files.copy(writer, media.getReaderData());
					writer.close();
				}
				Msysparam obj = oDao.findById("TTD");
				obj.setParamvalue(media.getName());
				obj.setUpdatedby(oUser.getUserid());
				obj.setLastupdated(new Date());
				oDao.save(session, obj);
			}

			transaction.commit();
			Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		} finally {
			session.close();
		}
	}

}
