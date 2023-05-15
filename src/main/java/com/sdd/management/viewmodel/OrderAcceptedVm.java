package com.sdd.caption.viewmodel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.io.Files;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TswitchDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.handler.BranchStockManager;
import com.sdd.caption.model.TorderListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OrderAcceptedVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private TorderListModel model;
	private Mbranch mbranch;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String arg;

	private String productgroup;
	private String memono;
	private Date processtime;
	private Integer totaldata;
	private Integer totalselected;
	private Integer totaldataselected;
	private Date tglterima;
	private String penerima;
	private String filename;
	private Media media;

	private TorderDAO oDao = new TorderDAO();
	private Map<Integer, Torder> mapData = new HashMap<>();

	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Combobox cbCabang, cbProduk;
	@Wire
	private Checkbox chkAll;
	@Wire
	private Textbox tbPenerima;
	@Wire
	private Label lbFileBrowse;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String argp)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.arg = argp;
		this.mbranch = oUser.getMbranch();

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
				chkAll.setChecked(false);
			}
		});

		grid.setRowRenderer(new RowRenderer<Torder>() {
			@Override
			public void render(Row row, final Torder data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));
				Checkbox check = new Checkbox();
				check.setAttribute("obj", data);
				check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Checkbox checked = (Checkbox) event.getTarget();
						Torder obj = (Torder) checked.getAttribute("obj");
						if (checked.isChecked()) {
							mapData.put(data.getTorderpk(), data);
							totaldataselected += obj.getTotalproses();
						} else {
							mapData.remove(data.getTorderpk());
							totaldataselected -= obj.getTotalproses();
						}
						totalselected = mapData.size();
						BindUtils.postNotifyChange(null, null, OrderAcceptedVm.this, "totalselected");
						BindUtils.postNotifyChange(null, null, OrderAcceptedVm.this, "totaldataselected");
					}
				});
				if (mapData.get(data.getTorderpk()) != null)
					check.setChecked(true);
				row.getChildren().add(check);

				row.getChildren().add(new Label(data.getOrderid()));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getInserttime())));

				A a = new A(data.getMemono());
				a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("obj", data);
						map.put("arg", arg);
						Window win = (Window) Executions.createComponents("/view/order/orderdetail.zul", null, map);
						win.setWidth("60%");
						win.setClosable(true);
						win.doModal();
					}
				});
				row.getChildren().add(a);
				row.getChildren().add(new Label(data.getMproduct() != null ? data.getMproduct().getProductname()
						: AppData.getProductgroupLabel(arg)));
				row.getChildren().add(new Label(data.getOrderoutlet()));
				row.getChildren()
						.add(new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
				row.getChildren().add(new Label(
						data.getTotalqty() != null ? NumberFormat.getInstance().format(data.getTotalqty()) : "0"));
				row.getChildren()
						.add(new Label(
								data.getTotalproses() != null ? NumberFormat.getInstance().format(data.getTotalproses())
										: "0"));
				row.getChildren().add(new Label(data.getInsertedby()));
			}
		});
		doReset();
	}

	@Command
	@NotifyChange({ "totalselected", "totaldataselected" })
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			if (grid.getRows() != null && grid.getRows().getChildren() != null) {
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Checkbox chk = (Checkbox) comp.getChildren().get(1);
					Torder obj = (Torder) chk.getAttribute("obj");
					if (checked) {
						if (!chk.isChecked()) {
							chk.setChecked(true);
							mapData.put(obj.getTorderpk(), obj);
							totaldataselected += obj.getTotalproses();
						}
					} else {
						if (chk.isChecked()) {
							chk.setChecked(false);
							mapData.remove(obj.getTorderpk());
							totaldataselected = 0;
						}
					}
				}
				totalselected = mapData.size();
			}
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
			filename = media.getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			filter = "tglterima is null and penerima is null and mbranchfk = " + mbranch.getMbranchpk()
					+ " and torder.status = '" + AppUtils.STATUS_DELIVERY_DELIVERY + "' and orderoutlet != '00'";

			if (processtime != null)
				filter += " and DATE(Torder.inserttime) = '" + dateFormatter.format(processtime) + "'";

			if (memono != null && memono.trim().length() > 0)
				filter += " and memono = '" + memono.trim().toUpperCase() + "'";

			needsPageUpdate = true;
			paging.setActivePage(0);
			pageStartNumber = 0;
			refreshModel(pageStartNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	@Command
	public void doResetSelected() {
		if (mapData.size() > 0) {
			Messagebox.show("Anda ingin mereset data-data yang sudah anda pilih?", "Confirm Dialog",
					Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								doReset();
								BindUtils.postNotifyChange(null, null, OrderAcceptedVm.this, "*");
							}
						}
					});
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totaldata = 0;
		totalselected = 0;
		totaldataselected = 0;
		mapData = new HashMap<>();
		chkAll.setChecked(false);
		processtime = new Date();
		processtime = null;
		productgroup = AppData.getProductgroupLabel(arg);
		tglterima = new Date();
		filename = null;
		penerima = null;
		tbPenerima.setValue(null);
		lbFileBrowse.setValue(null);

		pageTotalSize = 0;
		paging.setTotalSize(pageTotalSize);
		if (grid.getRows() != null) {
			grid.getRows().getChildren().clear();
		}

		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void refreshModel(int activePage) {
		try {
			orderby = "torderpk asc";
			paging.setPageSize(SysUtils.PAGESIZE);
			model = new TorderListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	@NotifyChange("*")
	public void doManifest() {
		Session session = StoreHibernateUtil.openSession();
		Transaction transaction = session.beginTransaction();
		if (mapData.size() > 0) {
			if (filename == null || tglterima == null || penerima == null || penerima.trim().length() == 0) {
				Messagebox.show("Silahkan isi Tanggal Terima, Nama Penerima, dan Tanda Terima", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			} else {
				Messagebox.show("Apakah anda yakin ingin menyelesaikan pengiriman?", "Confirm Dialog",
						Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									try {
										for (Entry<Integer, Torder> entry : mapData.entrySet()) {
											Torder obj = entry.getValue();
											obj.setFileterima(filename);
											obj.setPenerima(penerima);
											obj.setTglterima(tglterima);
											obj.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
											oDao.save(session, obj);

											Tswitch objSwitch = new TswitchDAO()
													.findByFilter("torderfk = " + obj.getTorderpk());
											if (objSwitch != null) {
												objSwitch.setStatus(AppUtils.STATUS_DELIVERY_DELIVERED);
												new TswitchDAO().save(session, objSwitch);
											}

											BranchStockManager.manageNonCard(obj, obj.getProductgroup());
										}
										transaction.commit();

										if (media != null) {
											String path = Executions.getCurrent().getDesktop().getWebApp()
													.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.POD_PATH);
											if (media.isBinary()) {
												Files.copy(new File(path + "/" + media.getName()),
														media.getStreamData());
											} else {
												BufferedWriter writer = new BufferedWriter(
														new FileWriter(path + "/" + media.getName()));
												Files.copy(writer, media.getReaderData());
												writer.close();
											}
										}

										Clients.showNotification(Labels.getLabel("common.add.success"), "info", null,
												"middle_center", 3000);
										doReset();
									} catch (Exception e) {
										transaction.rollback();
										Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(),
												Messagebox.OK, Messagebox.ERROR);
										e.printStackTrace();
									} finally {
										session.close();
									}

								}
							}

						});
			}
		} else {
			Messagebox.show("Tidak ada data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Date getProcesstime() {
		return processtime;
	}

	public void setProcesstime(Date processtime) {
		this.processtime = processtime;
	}

	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public Date getTglterima() {
		return tglterima;
	}

	public void setTglterima(Date tglterima) {
		this.tglterima = tglterima;
	}

	public String getPenerima() {
		return penerima;
	}

	public void setPenerima(String penerima) {
		this.penerima = penerima;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getMemono() {
		return memono;
	}

	public void setMemono(String memono) {
		this.memono = memono;
	}
}