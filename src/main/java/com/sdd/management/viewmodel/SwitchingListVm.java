package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.TdeliverydataDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.dao.TswitchitemDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tdeliverydata;
import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.domain.Tswitchitem;
import com.sdd.caption.model.TswitchListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class SwitchingListVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TswitchDAO oDao = new TswitchDAO();
	private TswitchListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private Integer year;
	private Integer month;
	private String status;
	private String arg;
	private int branchlevel;
	private String productgroup;

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMMM yyyy");

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("productgroup") String productgroup) throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null)
			branchlevel = oUser.getMbranch().getBranchlevel();

		this.arg = arg;
		this.productgroup = productgroup;
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tswitch>() {

				@Override
				public void render(Row row, final Tswitch data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
					A a = new A(data.getRegid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("obj", data);
							win = (Window) Executions.createComponents("/view/switching/switchingitem.zul", null, map);
							win.setWidth("50%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);
					row.appendChild(new Label(
							data.getInserttime() != null ? dateLocalFormatter.format(data.getInserttime()) : "-"));
					row.appendChild(new Label(data.getMbranch() != null ? data.getMbranch().getBranchname() : "-"));
					row.appendChild(new Label(data.getOutletreq() != null ? data.getOutletreq() : "-"));
					row.appendChild(new Label(
							data.getMproduct().getProductname() != null ? data.getMproduct().getProductname() : "-"));
					row.appendChild(new Label(
							data.getMproduct().getProductcode() != null ? data.getMproduct().getProductcode() : "-"));

					if (data.getBranchidpool() != null) {
						Mbranch branch = new MbranchDAO().findByFilter("branchid = '" + data.getBranchidpool() + "'");
						row.appendChild(new Label(branch.getBranchname()));
					} else {
						row.appendChild(new Label("-"));
					}
					row.appendChild(new Label(data.getOutletpool() != null ? data.getOutletpool() : "-"));

					row.appendChild(
							new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
					row.appendChild(new Label(
							data.getItemqty() != null ? NumberFormat.getInstance().format(data.getItemqty()) : "-"));

					if (data.getTorder().getTorderpk() != null) {
						Button btnOrder = new Button("Lihat Data Pemesanan");
						btnOrder.setAutodisable("self");
						btnOrder.setClass("btn-default");
						btnOrder.setStyle(
								"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
						btnOrder.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("obj", data.getTorder());
								map.put("arg", arg);
								Window win = (Window) Executions.createComponents("/view/order/orderdetail.zul", null,
										map);
								win.setWidth("60%");
								win.setClosable(true);
								win.doModal();
							}
						});

						Div div = new Div();
						div.appendChild(btnOrder);
						row.appendChild(div);
					} else {
						row.getChildren().add(new Label("-"));
					}

					row.appendChild(new Label(data.getInsertedby() != null ? data.getInsertedby() : "-"));
					row.appendChild(new Label(data.getDecisionby() != null ? data.getDecisionby() : "-"));
					row.appendChild(new Label(
							data.getDecisiontime() != null ? dateLocalFormatter.format(data.getDecisiontime()) : "-"));
					Button btndel = new Button("Hapus");
					btndel.setAutodisable("self");
					btndel.setClass("btn btn-danger btn-sm");
					btndel.setStyle("border-radius: 8px; color: #ffffff !important;");
					btndel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
									Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

										@Override
										public void onEvent(Event event) throws Exception {
											if (event.getName().equals("onOK")) {
												Session session = StoreHibernateUtil.openSession();
												Transaction transaction = session.beginTransaction();
												try {
													oDao.delete(session, data);
													transaction.commit();
													Clients.showNotification("Hapus data berhasil.", "info", null,
															"middle_center", 3000);
												} catch (HibernateException e) {
													transaction.rollback();
													e.printStackTrace();
												} catch (Exception e) {
													e.printStackTrace();
												} finally {
													session.close();
												}

												doSearch();
												BindUtils.postNotifyChange(null, null, SwitchingListVm.this,
														"pageTotalSize");
											}
										}
									});
						}
					});

					Button btnEdit = new Button();
					btnEdit.setLabel("Revisi");
					btnEdit.setAutodisable("self");
					btnEdit.setSclass("btn btn-success btn-sm");
					btnEdit.setStyle("border-radius: 8px; color: #ffffff !important;");
					btnEdit.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("objSwitch", data);
							map.put("arg", data.getMproduct().getProductgroup().trim());
							map.put("isEdit", "Y");
							Window win = (Window) Executions.createComponents("/view/switching/switchingentry.zul",
									null, map);
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									doReset();
									BindUtils.postNotifyChange(null, null, SwitchingListVm.this, "*");
								}
							});
						}
					});

					Button btnMemo = new Button();
					btnMemo.setLabel("Memo");
					btnMemo.setAutodisable("self");
					btnMemo.setSclass("btn btn-default btn-sm");
					btnMemo.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnMemo.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data.getTorder());
							Window win = (Window) Executions.createComponents("/view/order/ordermemo.zul", null, map);
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									doReset();
									BindUtils.postNotifyChange(null, null, SwitchingListVm.this, "*");
								}
							});
						}
					});

					Button btnProses = new Button();
					btnProses.setLabel("Proses");
					btnProses.setAutodisable("self");
					btnProses.setSclass("btn btn-default btn-sm");
					btnProses.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnProses.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							map.put("obj", data);
							Window win = new Window();
							if (data.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_PINPAD)) {
								win = (Window) Executions.createComponents("/view/pinpad/pinpadscanentry.zul", null,
										map);
							} else if (data.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_TOKEN)) {
								win = (Window) Executions.createComponents("/view/token/tokenscanentry.zul", null, map);
							} else if (data.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
								win = (Window) Executions.createComponents("/view/switching/documentswitching.zul",
										null, map);
							} else if (data.getMproduct().getProductgroup().equals(AppUtils.PRODUCTGROUP_CARD)) {
								win = (Window) Executions.createComponents("/view/switching/cardswitching.zul", null,
										map);
							}
							win.setWidth("70%");
							win.setClosable(true);
							win.doModal();
							win.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {
									if (event.getData() != null) {
										needsPageUpdate = true;
										pageStartNumber = 0;
										refreshModel(pageStartNumber);

										BindUtils.postNotifyChange(null, null, SwitchingListVm.this, "pageTotalSize");
									}
								}
							});
						}
					});
					row.getChildren().add(btnMemo);

					Button btnLetter = new Button();
					btnLetter.setLabel("Tanda Terima");
					btnLetter.setAutodisable("self");
					btnLetter.setSclass("btn btn-default btn-sm");
					btnLetter.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnLetter.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							try {
								String filename = "LETTER" + new SimpleDateFormat("yyMMddHHmmss").format(new Date())
										+ ".pdf";
								Font font = new Font(Font.FontFamily.HELVETICA, 10);
								Font fontbold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
								Font fonttable = new Font(Font.FontFamily.HELVETICA, 9);
								Font fontheadertable = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
								String output = Executions.getCurrent().getDesktop().getWebApp()
										.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + filename);
								Document document = new Document(new Rectangle(PageSize.A4), 72, 72, 72, 72);
								PdfWriter.getInstance(document, new FileOutputStream(output));
								document.open();

								String[] hari = { "Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu" };
								String[] months = new DateFormatSymbols().getMonths();

								Calendar cal = Calendar.getInstance();
								cal.setTime(new Date());

								String dlvno = "";

								List<Tdeliverydata> dlvList = new TdeliverydataDAO()
										.listManifestSwitching("tswitchfk = " + data.getTswitchpk());
								if (dlvList.size() > 0)
									dlvno = dlvList.get(0).getTdelivery().getDlvid();

								PdfPTable table = null;
								PdfPCell cell = null;

								table = new PdfPTable(1);
								table.setWidthPercentage(100);
								table.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell = new PdfPCell(new Paragraph("TANDA TERIMA BARANG (SWITCHING)", fontbold));
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingTop(30);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(1);
								table.setWidthPercentage(100);
								cell = new PdfPCell(new Paragraph("Pada hari " + hari[cal.get(Calendar.DAY_OF_WEEK) - 1]
										+ " Tanggal " + cal.get(Calendar.DATE) + " Bulan "
										+ months[(cal.get(Calendar.MONTH))] + " Tahun " + cal.get(Calendar.YEAR)
										+ " kami sampaikan sejumlah barang dalam rangka switching "
										+ "kepada Wilayah/Cabang " + data.getMbranch().getBranchname()
										+ " dengan perincian sebagai berikut : ", font));
								cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingTop(30);
								cell.setPaddingBottom(20);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(3);
								table.setHorizontalAlignment(Element.ALIGN_LEFT);
								table.setWidthPercentage(100);
								table.setWidths(new int[] { 8, 42, 50 });
								cell = new PdfPCell(new Paragraph("1. ", font));
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph("Kantor Wilayah/Cabang Tujuan", font));
								cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(10);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph(" : " + data.getMbranch().getBranchname(), font));
								cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(10);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(3);
								table.setHorizontalAlignment(Element.ALIGN_LEFT);
								table.setWidthPercentage(100);
								table.setWidths(new int[] { 8, 42, 50 });
								cell = new PdfPCell(new Paragraph("2. ", font));
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph("Surat Permintaan Barang", font));
								cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(10);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph(" : " + data.getTorder().getOrderid(), font));
								cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(10);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(1);
								table.setHorizontalAlignment(Element.ALIGN_LEFT);
								table.setWidthPercentage(100);
								cell = new PdfPCell(new Paragraph("\n", font));
								cell.setBorder(PdfPCell.NO_BORDER);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(4);
								table.setHorizontalAlignment(Element.ALIGN_LEFT);
								table.setWidthPercentage(100);
								table.setWidths(new int[] { 10, 40, 20, 30 });
								cell = new PdfPCell(new Paragraph("NO", fontheadertable));
								cell.setUseBorderPadding(true);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph("JENIS BARANG", fontheadertable));
								cell.setUseBorderPadding(true);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph("JUMLAH", fontheadertable));
								cell.setUseBorderPadding(true);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph("KETERANGAN", fontheadertable));
								cell.setUseBorderPadding(true);
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
								table.addCell(cell);
								document.add(table);
								int no = 1;
								for (Tswitchitem data : new TswitchitemDAO()
										.listNativeByFilter("tswitchfk = " + data.getTswitchpk(), "tswitchitempk")) {
									table = new PdfPTable(4);
									table.setHorizontalAlignment(Element.ALIGN_LEFT);
									table.setWidthPercentage(100);
									table.setWidths(new int[] { 10, 40, 20, 30 });
									cell = new PdfPCell(new Paragraph(String.valueOf(no++), fonttable));
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table.addCell(cell);
									cell = new PdfPCell(
											new Paragraph(data.getTswitch().getMproduct().getProductname(), fonttable));
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table.addCell(cell);
									cell = new PdfPCell(new Paragraph("1 Lembar/Buku", fonttable));
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table.addCell(cell);
									cell = new PdfPCell(new Paragraph(data.getItemno(), fonttable));
									cell.setHorizontalAlignment(Element.ALIGN_CENTER);
									table.addCell(cell);
									document.add(table);
								}

								table = new PdfPTable(1);
								table.setWidthPercentage(100);
								table.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell = new PdfPCell(
										new Paragraph("** Daftar Perincian No Seri formulir berharga terlampir", font));
								cell.setHorizontalAlignment(Element.ALIGN_CENTER);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(3);
								table.setHorizontalAlignment(Element.ALIGN_LEFT);
								table.setWidthPercentage(100);
								table.setWidths(new int[] { 8, 42, 50 });
								cell = new PdfPCell(new Paragraph("3. ", font));
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph("Delivery Order No ", font));
								cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph(" : " + dlvno, font));
								cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(1);
								table.setWidthPercentage(100);
								cell = new PdfPCell(new Paragraph(
										"Sebagai penerimaan harap Saudara tanda-tangani tanda terima barang"
												+ "(switching) ini dikembalikan / faximile kepada kami setelah diterima surat berharga, "
												+ "sebelumnya mencocokan Jumlah dan Nomor Seri. Informasi lebih lanjut dapat menghubungi "
												+ "No Phone : ......................... Email: .............................",
										font));
								cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setUseBorderPadding(true);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(1);
								table.setWidthPercentage(100);
								cell = new PdfPCell(new Paragraph(
										"Demikianlah untuk dimaklumi dan atas kerjasamanya kami ucapkan terima kasih.",
										font));
								cell.setHorizontalAlignment(Element.ALIGN_LEFT);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingTop(10);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(1);
								table.setWidthPercentage(100);
								cell = new PdfPCell(
										new Paragraph("Jakarta, " + dateFormatter.format(new Date()), font));
								cell.setHorizontalAlignment(Element.ALIGN_LEFT);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingTop(20);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								document.add(table);
								table = new PdfPTable(1);
								table.setWidthPercentage(100);
								Mbranch mbranch = new MbranchDAO()
										.findByFilter("branchid = '" + data.getBranchidpool() + "'");
								cell = new PdfPCell(
										new Paragraph("Kantor Wilayah/Cabang " + mbranch.getBranchname(), font));
								cell.setHorizontalAlignment(Element.ALIGN_LEFT);
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(2);
								table.setHorizontalAlignment(Element.ALIGN_LEFT);
								table.setWidthPercentage(100);
								table.setWidths(new int[] { 60, 40 });
								cell = new PdfPCell(new Paragraph("Yang Menyerahkan Barang", font));
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph("Tanggal ", font));
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								document.add(table);

								table = new PdfPTable(2);
								table.setHorizontalAlignment(Element.ALIGN_LEFT);
								table.setWidthPercentage(100);
								table.setWidths(new int[] { 60, 40 });
								cell = new PdfPCell(new Paragraph(" ", font));
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								cell = new PdfPCell(new Paragraph("Yang Menerima Barang", font));
								cell.setBorder(PdfPCell.NO_BORDER);
								cell.setPaddingBottom(5);
								table.addCell(cell);
								document.add(table);

								document.close();

								Filedownload.save(
										new File(Executions.getCurrent().getDesktop().getWebApp().getRealPath(
												AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + filename)),
										"application/pdf");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					Div div = new Div();
					div.setClass("btn-sm");

					if (arg.equals("pool")) {
						if (data.getStatus().equals(AppUtils.STATUS_SWITCH_HANDOVER)) {
							div.appendChild(btnProses);
							row.getChildren().add(div);
						} else if (data.getStatus().equals(AppUtils.STATUS_DELIVERY_DELIVERYORDER)
								|| data.getStatus().equals(AppUtils.STATUS_DELIVERY_DELIVERED)) {
							div.appendChild(btnLetter);
							row.getChildren().add(div);
						} else
							row.appendChild(new Label());
					} else {
						if (data.getStatus().equals(AppUtils.STATUS_SWITCH_ENTRY)
								|| data.getStatus().equals(AppUtils.STATUS_SWITCH_REJECTEDREQ)) {
							div.appendChild(btnEdit);
							row.getChildren().add(div);
						} else
							row.appendChild(new Label());
					}
				}

			});
		}

		String[] months = new DateFormatSymbols().getMonths();
		for (int i = 0; i < months.length; i++) {
			Comboitem item = new Comboitem();
			item.setLabel(months[i]);
			item.setValue(i + 1);
			cbMonth.appendChild(item);
		}

		doReset();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tswitchpk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TswitchListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		if (year != null && month != null) {
			filter = "extract(year from inserttime) = " + year + " and " + "extract(month from inserttime) = " + month
					+ " and productgroup = '" + productgroup + "'";
			if (arg.equals("req")) {
				if (branchlevel == 2)
					filter += " and mregionfk = " + oUser.getMbranch().getMregion().getMregionpk();
				else if (branchlevel == 3)
					filter += " and mbranchfk = " + oUser.getMbranch().getMbranchpk();
			} else {
				filter += " and branchidpool = '" + oUser.getMbranch().getBranchid() + "'";
			}
			if (status.length() > 0)
				filter += " and status = '" + status + "'";
			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		}
	}

	@Command
	public void doExport() {
		try {
			if (filter.length() > 0) {
				List<Tswitch> listData = oDao.listNativeByFilter(filter, orderby);
				if (listData != null && listData.size() > 0) {
					XSSFWorkbook workbook = new XSSFWorkbook();
					XSSFSheet sheet = workbook.createSheet();

					int rownum = 0;
					int cellnum = 0;
					Integer no = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("Daftar Switching");
					rownum++;
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Periode");
					cell = row.createCell(1);
					cell.setCellValue(StringUtils.getMonthLabel(month) + " " + year);
					row = sheet.createRow(rownum++);

					/*
					 * row = sheet.createRow(rownum++); cell = row.createCell(0); rownum++;
					 */
					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1,
							new Object[] { "No", "Switching ID", "Tanggal Request", "ID Cabang Pemenuhan",
									"Outlet Pemenuhan", "Nama Barang", "Kode Barang", "Jumlah", "ID Cabang Tujuan",
									"Outlet Tujuan", "Direquest oleh", "Pemutus", "Tanggal Keputusan" });
					no = 2;
					for (Tswitch data : listData) {
						Mbranch branch = null;
						if (data.getBranchidpool() != null) {
							branch = new MbranchDAO().findByFilter("branchid = '" + data.getBranchidpool() + "'");
						}
						datamap.put(no,
								new Object[] { no - 1, data.getRegid(), dateLocalFormatter.format(data.getInserttime()),
										data.getMbranch() != null ? data.getMbranch().getBranchname() : "-",
										data.getOutletreq() != null ? data.getOutletreq() : "-",
										data.getMproduct().getProductname(), data.getMproduct().getProductcode(),
										NumberFormat.getInstance().format(data.getItemqty()),
										branch != null ? branch.getBranchname() : "-", data.getOutletpool(),
										data.getInsertedby(), data.getDecisionby(),
										dateLocalFormatter.format(data.getDecisiontime()) });
						no++;
					}

					Set<Integer> keyset = datamap.keySet();
					for (Integer key : keyset) {
						row = sheet.createRow(rownum++);
						Object[] objArr = datamap.get(key);
						cellnum = 0;
						for (Object obj : objArr) {
							cell = row.createCell(cellnum++);
							if (obj instanceof String)
								cell.setCellValue((String) obj);
							else if (obj instanceof Integer)
								cell.setCellValue((Integer) obj);
							else if (obj instanceof Double)
								cell.setCellValue((Double) obj);
						}
					}

					String path = Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
					String filename = "CAPTION_DAFTAR_SWITCHING_"
							+ new SimpleDateFormat("yyMMddHHmm").format(new Date()) + ".xlsx";
					FileOutputStream out = new FileOutputStream(new File(path + "/" + filename));
					workbook.write(out);
					out.close();

					Filedownload.save(new File(path + "/" + filename),
							"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				} else {
					Messagebox.show("Data tidak tersedia", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		year = Calendar.getInstance().get(Calendar.YEAR);
		month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		status = "";
		doSearch();
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
