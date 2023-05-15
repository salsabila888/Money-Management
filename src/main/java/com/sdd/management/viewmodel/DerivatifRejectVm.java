package com.sdd.caption.viewmodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Footer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.dao.TderivatifdataDAO;
import com.sdd.caption.dao.TderivatifproductDAO;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.caption.domain.Tderivatifdata;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.model.TderivatifdataListModel;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DerivatifRejectVm {

	private TderivatifdataListModel model;

	private Tderivatif obj;
	private TderivatifdataDAO oDao = new TderivatifdataDAO();

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;

	private String orderno;
	private String cardno;
	private String memo;
	private String filename;
	private Media media;
	private String type;
	private Integer totalupload;
	private Integer inserted;
	private Integer failed;

	private Map<Integer, Tderivatifdata> mapData = new HashMap<>();
	private List<Tembossdata> listTdata = new ArrayList<>();
	private List<Tembossdata> dataFailList = new ArrayList<>();
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Row rowUpload;
	@Wire
	private Groupbox gbHasilUpload, gbManual;
	@Wire
	private Footer footUpload, footManual;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);

		doReset();
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
				chkAll.setChecked(false);
			}
		});

		grid.setRowRenderer(new RowRenderer<Tderivatifdata>() {

			@Override
			public void render(Row row, final Tderivatifdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Tderivatifdata obj = (Tderivatifdata) checked.getAttribute("obj");
						if (checked.isChecked()) {
							if (mapData.get(data.getTderivatifdatapk()) != null) {
								checked.setChecked(false);
								Messagebox.show("Data sudah masuk kedaftar reject.", "Info", Messagebox.OK,
										Messagebox.INFORMATION);
							} else {
								mapData.put(obj.getTderivatifdatapk(), obj);
							}
						} else {
							mapData.remove(obj.getTderivatifdatapk());
						}
					}
				});
				if (mapData.get(data.getTderivatifdatapk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);
				row.getChildren().add(new Label(data.getCardno()));
				row.getChildren().add(new Label(data.getTembossdata().getNameonid()));
				row.getChildren().add(new Label(data.getTderivatifproduct().getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getTderivatif().getMbranch().getBranchname()));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getOrderdate())));
			}
		});

	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tderivatifdata obj = (Tderivatifdata) chk.getAttribute("obj");
				if (checked) {
					if (!chk.isChecked()) {
						chk.setChecked(true);
						mapData.put(obj.getTderivatifdatapk(), obj);
					}
				} else {
					if (chk.isChecked()) {
						chk.setChecked(false);
						mapData.remove(obj.getTderivatifdatapk());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doCheckedtype() {
		if (type.equals("M")) {
			footManual.setVisible(true);
			grid.setVisible(true);
			gbManual.setVisible(true);

			rowUpload.setVisible(false);
			footUpload.setVisible(false);
			gbHasilUpload.setVisible(false);
		} else {
			footManual.setVisible(false);
			grid.setVisible(false);
			gbManual.setVisible(false);

			rowUpload.setVisible(true);
			footUpload.setVisible(true);
			gbHasilUpload.setVisible(true);
		}
	}

	@Command
	@NotifyChange("*")
	public void doView() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("obj", obj);
		map.put("listFail", dataFailList);
		map.put("isFail", "Y");
		Window win = (Window) Executions.createComponents("/view/derivatif/derivatifdatafail.zul", null, map);
		win.setWidth("80%");
		win.setClosable(true);
		win.doModal();
	}

	@Command
	@NotifyChange("*")
	public void doViewSuccess() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("obj", obj);
		map.put("listSuccess", listTdata);
		map.put("isSuccess", "Y");
		Window win = (Window) Executions.createComponents("/view/derivatif/derivatifdatasuccess.zul", null, map);
		win.setWidth("80%");
		win.setClosable(true);
		win.doModal();
	}

	@Command
	@NotifyChange("filename")
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			filename = media.getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doTemplate() {
		try {
			String path = Executions.getCurrent().getDesktop().getWebApp().getRealPath(
					AppUtils.FILES_ROOT_PATH + AppUtils.PATH_DERIVATIFFILE + "/" + "TEMPLATE_UPLOAD_REJECT");
			Filedownload.save(new File(path), "text/plain");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doUpload() {
		if (orderno == null || orderno.equals("")) {
			Messagebox.show("Silahkan masukan no surat.", WebApps.getCurrent().getAppName(), Messagebox.OK,
					Messagebox.INFORMATION);
		} else {
			if (media == null) {
				Messagebox.show("Silahkan upload file untuk proses reject data.", WebApps.getCurrent().getAppName(),
						Messagebox.OK, Messagebox.INFORMATION);
			} else {
				BufferedReader reader = null;
				String error = "";
				try {
					if (media.isBinary()) {
						reader = new BufferedReader(new InputStreamReader(media.getStreamData()));
					} else {
						reader = new BufferedReader(media.getReaderData());
					}

					String line = "";
					String cardno = "";
					String keterangan = "";
					int linecontent = 1;

					obj = new TderivatifDAO().findByFilter("orderno = '" + orderno.trim().toUpperCase() + "'");

					while ((line = reader.readLine()) != null) {
						try {
							if (linecontent > 2) {

								cardno = line.trim();
								System.out.println(cardno);

								Tderivatifdata tdd = oDao.findByFilter("cardno = '" + cardno + "' and tderivatiffk = "
										+ obj.getTderivatifpk() + " and status = '" + AppUtils.STATUS_PROSES + "'");
								if (tdd != null) {
									if (mapData.get(tdd.getTderivatifdatapk()) == null) {
										System.out.println("CACTH : " + cardno);
										mapData.put(tdd.getTderivatifdatapk(), tdd);
										keterangan = "Inserted.";
										tdd.getTembossdata().setNameoncard(keterangan);
										listTdata.add(tdd.getTembossdata());
										inserted++;
									} else {
										keterangan = "Data sudah masuk kedaftar reject.";
										tdd.getTembossdata().setNameoncard(keterangan);
										dataFailList.add(tdd.getTembossdata());
										failed++;
									}
								} else {
									keterangan = "Data tidak ada di nomor surat " + orderno.trim().toUpperCase() + ".";
									Tembossdata ted = new Tembossdata();
									ted.setCardno(cardno);
									ted.setNameoncard(keterangan);
									dataFailList.add(ted);
									failed++;
								}
								totalupload++;
							}
							linecontent++;
						} catch (Exception e) {
							e.printStackTrace();
							if (error.length() > 0)
								error += ". \n";
							error += e.getMessage();
							System.out.println(line);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (error.length() > 0)
						error += ". \n";
					error += e.getMessage();
				}
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (mapData.size() > 0) {
			if (memo != null && memo.length() > 0) {
				Messagebox.show(
						"Apakah anda yakin ingin melanjutkan proses reject data? pastikan data yang akan direject sudah benar",
						"Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
						new EventListener<Event>() {
							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									Session session = StoreHibernateUtil.openSession();
									Transaction transaction = session.beginTransaction();
									try {

										for (Entry<Integer, Tderivatifdata> entry : mapData.entrySet()) {
											Tderivatifdata derData = entry.getValue();
											derData.setStatus(AppUtils.STATUS_REJECTED);
											derData.setRejectmemo(memo);
											oDao.save(session, derData);

											derData.getTderivatif()
													.setTotaldata(derData.getTderivatif().getTotaldata() - 1);
											derData.getTderivatif()
													.setTotalreject(derData.getTderivatif().getTotalreject() + 1);
											new TderivatifDAO().save(session, derData.getTderivatif());

											derData.getTderivatifproduct()
													.setTotaldata(derData.getTderivatifproduct().getTotaldata() - 1);
											new TderivatifproductDAO().save(session, derData.getTderivatifproduct());
										}
										transaction.commit();
									} catch (HibernateException e) {
										transaction.rollback();
										e.printStackTrace();
									} catch (Exception e) {
										Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(),
												Messagebox.OK, Messagebox.ERROR);
										e.printStackTrace();
									} finally {
										session.close();
									}
									Clients.showNotification("Proses reject data status berhasil", "info", null,
											"middle_center", 3000);
									doSearch();
								}
							}
						});
			} else {
				Messagebox.show("Anda harus mengisi alasan decline pada field Memo", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
		} else {
			Messagebox.show("Silahkan pilih data untuk proses submit", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			filter = "tderivatif.orderno = '" + orderno.trim().toUpperCase()
					+ "' and tderivatif.status < 7 and tderivatifdata.status = '" + AppUtils.STATUS_PROSES + "'";

			if (cardno != null && cardno.length() > 0) {
				if (filter.length() > 0)
					filter += " and ";
				filter += "tderivatifdata.cardno like '%" + cardno.trim().toUpperCase() + "%'";
			}

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange({ "pageTotalSize", "total" })
	public void refreshModel(int activePage) {
		try {
			orderby = "tderivatifdata.cardno";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new TderivatifdataListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
			if (needsPageUpdate) {
				pageTotalSize = model.getTotalSize(filter);
				needsPageUpdate = false;
			}
			paging.setTotalSize(pageTotalSize);
			grid.setModel(model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		cardno = "";
		orderno = "";
		memo = null;
		rowUpload.setVisible(false);
		footUpload.setVisible(false);
		gbHasilUpload.setVisible(false);
		type = "M";
		totalupload = 0;
		inserted = 0;
		failed = 0;
		doSearch();
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getTotalupload() {
		return totalupload;
	}

	public void setTotalupload(Integer totalupload) {
		this.totalupload = totalupload;
	}

	public Integer getInserted() {
		return inserted;
	}

	public void setInserted(Integer inserted) {
		this.inserted = inserted;
	}

	public Integer getFailed() {
		return failed;
	}

	public void setFailed(Integer failed) {
		this.failed = failed;
	}

}
