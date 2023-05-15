/**
 * 
 */
package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

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
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Image;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.event.PagingEvent;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.sdd.caption.dao.McourierDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.domain.Mcourier;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Mregion;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.model.McourierListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class McourierVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private McourierListModel model;
	private McourierDAO oDao = new McourierDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private boolean isInsert;

	private Mcourier objForm;
	private String couriercode;
	private String couriername;

	private Media media;

	@Wire
	private Combobox cbVendor;
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
	private Image img;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
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
			listbox.setItemRenderer(new ListitemRenderer<Mcourier>() {
				@Override
				public void render(Listitem item, final Mcourier data, int index) throws Exception {
					Listcell cell = new Listcell(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1));
					item.appendChild(cell);
					cell = new Listcell(data.getMcouriervendor().getVendorname());
					item.appendChild(cell);
					cell = new Listcell(data.getCouriercode());
					item.appendChild(cell);
					cell = new Listcell(data.getCouriername());
					item.appendChild(cell);
					cell = new Listcell(data.getCourierphone());
					item.appendChild(cell);
					cell = new Listcell(data.getCourieremail());
					item.appendChild(cell);
					cell = new Listcell(data.getNpp());
					item.appendChild(cell);
					Button btnQr = new Button("Cetak QR");
					btnQr.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnQr.setAutodisable("self");
					btnQr.setSclass("btn btn-success btn-sm");
					btnQr.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							Filedownload.save(
									new File(
											Executions.getCurrent().getDesktop().getWebApp()
													.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.QR_PATH
															+ data.getCouriercode().trim() + ".pdf")),
									"application/pdf");
						}
					});
					cell = new Listcell();
					cell.appendChild(btnQr);
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

					cbVendor.setValue(objForm.getMcouriervendor().getVendorname());
					img.setSrc(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH + "/" + objForm.getCourierimg());
				}
			}
		});
	}

	public void refreshModel(int activePage) {
		orderby = "vendorname, couriername";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new McourierListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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
		if (couriercode != null && couriercode.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "couriercode like '%" + couriercode.trim().toUpperCase() + "%'";
		}
		if (couriername != null && couriername.trim().length() > 0) {
			if (filter.length() > 0)
				filter += " and ";
			filter += "couriername like '%" + couriername.trim().toUpperCase() + "%'";
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
	public void doUploadImg(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
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

	@Command
	@NotifyChange("objForm")
	public void save() {
		try {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			String path_root = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH);
			System.out.println(path_root);
			if (isInsert) {
				objForm.setCouriercode(new TcounterengineDAO().generateCounter(AppUtils.CODE_COURIER));
				objForm.setUpdatedby(oUser.getUserid());
				objForm.setLastupdated(new Date());
			} else {
				objForm.setUpdatedby(oUser.getUserid());
				objForm.setLastupdated(new Date());
			}
			objForm.setCourierimg(objForm.getCouriercode());
			oDao.save(session, objForm);
			transaction.commit();
			session.close();

			if (media != null) {
				// System.out.println("Content Type : " + media.getContentType());
				if (media.isBinary()) {
					Files.copy(new File(path_root + "/" + objForm.getCouriercode()), media.getStreamData());
				} else {
					BufferedWriter writer = new BufferedWriter(
							new FileWriter(path_root + "/" + objForm.getCouriercode()));
					Files.copy(writer, media.getReaderData());
					writer.close();
				}
			}
			qrGenerator(objForm);
			if (isInsert) {
				needsPageUpdate = true;
				Clients.showNotification(Labels.getLabel("common.add.success"), "info", null, "middle_center", 3000);
			} else
				Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
			doReset();
		} catch (HibernateException e) {
			transaction.rollback();
			if (isInsert)
				objForm.setMcourierpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		} catch (Exception e) {
			if (isInsert)
				objForm.setMcourierpk(null);
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
	}

	private void qrGenerator(Mcourier obj) throws Exception {
		try {
			String output = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.QR_PATH + obj.getCouriercode().trim() + ".pdf");
			/*
			 * File file = new File(output); if (file.exists()) file.delete();
			 */
			System.out.println(output);
			Document document = new Document(new Rectangle(340, 842));
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(output));
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);
			table.setWidths(new int[] { 50, 50 });
			PdfPCell cell = new PdfPCell(new Paragraph("BNI"));
			cell.setBorder(PdfPCell.NO_BORDER);
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(obj.getMcouriervendor().getVendorname()));
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			document.add(table);
			table = new PdfPTable(2);
			table.setWidthPercentage(100);
			table.setWidths(new int[] { 50, 50 });
			cell = new PdfPCell(new Paragraph(""));
			cell.setBorder(PdfPCell.NO_BORDER);
			table.addCell(cell);
			cell = new PdfPCell(new Paragraph(obj.getCouriername()));
			cell.setBorder(PdfPCell.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			document.add(table);

			String path_root = Executions.getCurrent().getDesktop().getWebApp()
					.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.IMAGE_PATH);
			System.out.println(path_root + "/img.jpg");
			com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(path_root + "/" + obj.getCourierimg());
			img.scaleAbsolute(120f, 176f);
			document.add(img);

			BarcodeQRCode q = new BarcodeQRCode(obj.getCouriercode(), 100, 100, new HashMap<EncodeHintType, Object>() {
				{
					put(EncodeHintType.CHARACTER_SET, "UTF-8");
				}
			});
			document.add(q.getImage());

			table = new PdfPTable(1);
			table.setWidthPercentage(100);
			cell = new PdfPCell(new Paragraph(obj.getCouriercode()));
			cell.setBorder(PdfPCell.NO_BORDER);
			table.addCell(cell);
			document.add(table);

			table = new PdfPTable(1);
			table.setWidthPercentage(100);
			cell = new PdfPCell(new Paragraph(obj.getCouriername()));
			cell.setBorder(PdfPCell.NO_BORDER);
			table.addCell(cell);
			document.add(table);
			table = new PdfPTable(1);
			table.setWidthPercentage(100);
			cell = new PdfPCell(new Paragraph(obj.getNpp()));
			cell.setBorder(PdfPCell.NO_BORDER);
			table.addCell(cell);
			document.add(table);

			document.close();
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
									BindUtils.postNotifyChange(null, null, McourierVm.this, "objForm");
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
		media = null;
		img.setSrc(null);
		objForm = new Mcourier();
		refreshModel(pageStartNumber);
		cbVendor.setValue(null);
		btnCancel.setDisabled(true);
		btnDelete.setDisabled(true);
		btnSave.setLabel(Labels.getLabel("common.save"));
	}

	public Validator getValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(ValidationContext ctx) {
				try {
					String couriername = (String) ctx.getProperties("couriername")[0].getValue();
					String courieremail = (String) ctx.getProperties("courieremail")[0].getValue();
					String courierphone = (String) ctx.getProperties("courierphone")[0].getValue();
					Mcouriervendor mcouriervendor = (Mcouriervendor) ctx.getProperties("mcouriervendor")[0].getValue();

					if (couriername == null || "".equals(couriername.trim()))
						this.addInvalidMessage(ctx, "couriername", Labels.getLabel("common.validator.empty"));

					if (courieremail.trim() == null || "".equals(courieremail.trim()))
						this.addInvalidMessage(ctx, "courieremail", Labels.getLabel("common.validator.empty"));
					if (!StringUtils.emailValidator(courieremail.trim()))
						this.addInvalidMessage(ctx, "courieremail", "Invalid e-mail format");

					if (courierphone == null)
						this.addInvalidMessage(ctx, "courierphone", Labels.getLabel("common.validator.empty"));
					if (!StringUtils.isNumeric(courierphone))
						this.addInvalidMessage(ctx, "courierphone", "Invalid  format");
					
					if (mcouriervendor == null)
						this.addInvalidMessage(ctx, "mcouriervendor", Labels.getLabel("common.validator.empty"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		};
	}

	public ListModelList<Mcouriervendor> getMcouriervendor() {
		ListModelList<Mcouriervendor> lm = null;
		try {
			lm = new ListModelList<Mcouriervendor>(AppData.getMcouriervendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Mcourier getObjForm() {
		return objForm;
	}

	public void setObjForm(Mcourier objForm) {
		this.objForm = objForm;
	}

	public String getCouriercode() {
		return couriercode;
	}

	public void setCouriercode(String couriercode) {
		this.couriercode = couriercode;
	}

	public String getCouriername() {
		return couriername;
	}

	public void setCouriername(String couriername) {
		this.couriername = couriername;
	}

}
