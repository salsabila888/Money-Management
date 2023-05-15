package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TbranchitemtrackDAO;
import com.sdd.caption.dao.TbranchstockitemDAO;
import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpinpaditemDAO;
import com.sdd.caption.dao.TrepairDAO;
import com.sdd.caption.dao.TrepairdlvDAO;
import com.sdd.caption.dao.TrepairitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tbranchitemtrack;
import com.sdd.caption.domain.Tbranchstockitem;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tpinpaditem;
import com.sdd.caption.domain.Trepair;
import com.sdd.caption.domain.Trepairdlv;
import com.sdd.caption.domain.Trepairitem;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class RepairScanPinpadVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private String filter, orderby, tid, mid, pinpadtype, pinpadmemo;
	private Integer outstanding, totalrecord, totalvendor;
	private Boolean isSaved;

	private Trepair obj;
	private Trepairitem objForm;
	private Tpinpaditem objPinpad;
	private BigDecimal harga;
	private String filename;
	private Media media;

	private TrepairitemDAO oDao = new TrepairitemDAO();

	private List<Trepairitem> objList = new ArrayList<>();
	private List<String> itemnoList = new ArrayList<>();
	private List<String> newitemnoList = new ArrayList<>();
	private List<Trepairitem> vendorList = new ArrayList<>();

	@Wire
	private Window winRepairItem;
	@Wire
	private Grid grid, gridVendor;
	@Wire
	private Label lbTitle;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Trepair trepair)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		obj = trepair;
		oUser = (Muser) zkSession.getAttribute("oUser");
		doReset();

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Trepairitem>() {
				@Override
				public void render(final Row row, final Trepairitem data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					row.getChildren().add(new Label(data.getItemno() != null ? data.getItemno() : "-"));
					row.getChildren().add(new Label(data.getTid() != null ? data.getTid() : "-"));
					row.getChildren().add(new Label(data.getMid() != null ? data.getMid() : "-"));
					row.getChildren().add(new Label(
							data.getPinpadtype() != null ? AppData.getPinpadtypeLabel(data.getPinpadtype()) : "-"));
					row.getChildren().add(new Label(data.getPinpadmemo() != null ? data.getPinpadmemo() : "-"));
					row.getChildren().add(new Label(data.getResolution() != null ? data.getResolution() : "-"));

					Button btn = new Button("Cancel");
					btn.setAutodisable("self");
					btn.setSclass("btn btn-danger btn-sm");
					btn.setStyle(
							"border-radius: 8px; background-color: #ce0012 !important; color: #ffffff !important;");
					btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
									Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {
										@Override
										public void onEvent(Event event) throws Exception {
											if (itemnoList.contains(data.getItemno().trim())) {
												vendorList.remove(data);
											}
											outstanding = outstanding + 1;
											totalrecord = totalrecord + 1;
											refresh();
											BindUtils.postNotifyChange(null, null, RepairScanPinpadVm.this,
													"outstanding");
											BindUtils.postNotifyChange(null, null, RepairScanPinpadVm.this,
													"totalrecord");
										}
									});
						}
					});

					Div div = new Div();
					div.appendChild(btn);
					row.getChildren().add(div);
				}
			});
		}

		gridVendor.setRowRenderer(new RowRenderer<Trepairitem>() {
			@Override
			public void render(final Row row, final Trepairitem data, int index2) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index2 + 1)));
				row.getChildren().add(new Label(data.getItemno() != null ? data.getItemno() : "-"));
				row.getChildren().add(new Label(data.getTid() != null ? data.getTid() : "-"));
				row.getChildren().add(new Label(data.getMid() != null ? data.getMid() : "-"));
				row.getChildren().add(new Label(
						data.getPinpadtype() != null ? AppData.getPinpadtypeLabel(data.getPinpadtype()) : "-"));
				row.getChildren().add(new Label(data.getPinpadmemo() != null ? data.getPinpadmemo() : "-"));
				row.getChildren().add(new Label(data.getResolution() != null ? data.getResolution() : "-"));
				Button btn = new Button("Cancel");
				btn.setAutodisable("self");
				btn.setSclass("btn btn-danger btn-sm");
				btn.setStyle("border-radius: 8px; background-color: #ce0012 !important; color: #ffffff !important;");
				btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
								Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {
									@Override
									public void onEvent(Event event) throws Exception {
										if (itemnoList.contains(data.getItemno().trim())) {
											vendorList.remove(data);
										}
										outstanding = outstanding + 1;
										totalrecord = totalrecord + 1;
										refresh();
										BindUtils.postNotifyChange(null, null, RepairScanPinpadVm.this, "outstanding");
										BindUtils.postNotifyChange(null, null, RepairScanPinpadVm.this, "totalrecord");
									}
								});
					}
				});

				Div div = new Div();
				div.appendChild(btn);
				row.getChildren().add(div);
			}
		});
	}

	@Command
	@NotifyChange("*")
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			filename = media.getName();
			if (media != null) {
				String serialno = "";
				String resolution = "";
				Workbook wb = null;
				if (media.getName().toLowerCase().endsWith("xlsx")) {
					wb = new XSSFWorkbook(media.getStreamData());
				} else if (media.getName().toLowerCase().endsWith("xls")) {
					wb = new HSSFWorkbook(media.getStreamData());
				}
				Sheet sheet = wb.getSheetAt(0);
				for (org.apache.poi.ss.usermodel.Row row : sheet) {
					try {
						if (row.getRowNum() < 1) {
							continue;
						}
						for (int count = 0; count <= row.getLastCellNum(); count++) {
							Cell cell = row.getCell(count, org.apache.poi.ss.usermodel.Row.RETURN_BLANK_AS_NULL);
							if (cell == null) {
								continue;
							}
							switch (count) {
							case 1:
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									cell.setCellType(Cell.CELL_TYPE_STRING);
									serialno = cell.getStringCellValue();
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									serialno = cell.getStringCellValue();
								}
								break;
							case 2:
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									cell.setCellType(Cell.CELL_TYPE_STRING);
									resolution = cell.getStringCellValue();
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									resolution = cell.getStringCellValue();
								}
								break;
							}

						}

						if (serialno != null) {
							if (outstanding > 0) {
								objForm = oDao.findByFilter("itemno = '" + serialno.trim() + "' and itemstatus = '"
										+ AppUtils.STATUS_REPAIR_PROCESSVENDOR + "'");
								if (objForm != null) {
									try {
										vendorList.add(objForm);
										listData.add(objForm.getItemno());
										refresh();
										serialno = "";
										outstanding = outstanding - 1;
									} catch (Exception e) {
										Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
										e.printStackTrace();
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		totalrecord = 0;
		outstanding = obj.getItemqty();
		harga = new BigDecimal(0);
		objForm = new Trepairitem();
		if (grid.getRows() != null)
			grid.getRows().getChildren().clear();
		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			filter = "trepairfk = " + obj.getTrepairpk() + " and itemstatus = '" + AppUtils.STATUS_REPAIR_PENDINGPROCESS
					+ "'";
			refreshModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			orderby = "trepairitempk";
			objList = oDao.listByFilter(filter, orderby);
			for (Trepairitem data : objList) {
				harga = data.getItemprice();
				itemnoList.add(data.getItemno().trim());
			}
			grid.setModel(new ListModelList<>(objList));
			totalrecord = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	@Command
	public void doRegisterManual() {
		try {
			if (outstanding > 0) {
				if (!newitemnoList.contains(objForm.getItemno().trim())) {
					objForm.setTrepair(obj);
					objForm.setItemprice(harga);
					successList.add(objForm);
					newitemnoList.add(objForm.getItemno().trim());
					refresh();
					objForm = new Trepairitem();
					outstanding = outstanding - 1;
				} else {
					Messagebox.show("Data telah digunakan.", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Jumlah data sudah mencapai batas maksimal.", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void refresh() {
		grid.setModel(new ListModelList<Trepairitem>(objList));
		gridSuccess.setModel(new ListModelList<Trepairitem>(successList));
	}

	@Command
	@NotifyChange("*")
	public void doSubmit() {
		if (successList.size() > 0) {
			String msg = "";
			if (outstanding > 0)
				msg = "Jumlah pemenuhan adalah " + successList.size() + " dan masih ada sisa outstanding " + outstanding
						+ ". Apakah anda yakin ingin proses sebagian?";
			else
				msg = "Anda ingin melanjutkan proses paket?";
			Messagebox.show(msg, "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
					new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								try {
									Session session = null;
									Transaction transaction = null;
									session = StoreHibernateUtil.openSession();
									transaction = session.beginTransaction();
									obj.setTotalproses(obj.getTotalproses() + successList.size());
									obj.setTglpemenuhan(new Date());
									if (obj.getItemqty() == obj.getTotalproses())
										obj.setStatus(AppUtils.STATUS_REPAIR_DONEPROCESS);
									obj.setRepairfailed(failList.size());
									new TrepairDAO().save(session, obj);

									Trepairdlv trd = new Trepairdlv();
									trd.setDlvno(new TcounterengineDAO().generateSeqnum());
									trd.setIsrepairdlv("N");
									trd.setProcessby(oUser.getUsername());
									trd.setProcesstime(new Date());
									trd.setQuantity(successList.size());
									trd.setTotalfail(failList.size());
									trd.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
									trd.setTrepair(obj);
									new TrepairdlvDAO().save(session, trd);

									for (Trepairitem datarep : successList) {
										datarep.setItemstatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
										datarep.setTrepairdlv(trd);
										oDao.save(session, datarep);
									}

									for (Trepairitem datarep : failList) {
										Tbranchstockitem objStock = new TbranchstockitemDAO()
												.findByFilter("itemno = '" + datarep.getItemno() + "' and status = '"
														+ datarep.getItemstatus() + "'");
										if (objStock != null) {
											objStock.setStatus(datarep.getItemstatus());
											new TbranchstockitemDAO().save(session, objStock);

											Tbranchitemtrack objTrack = new Tbranchitemtrack();
											objTrack.setItemno(datarep.getItemno());
											objTrack.setTracktime(new Date());
											objTrack.setTrackdesc(AppData.getStatusLabel(datarep.getItemstatus()));
											objTrack.setProductgroup(
													datarep.getTrepair().getMproduct().getProductgroup());
											objTrack.setMproduct(datarep.getTrepair().getMproduct());
											objTrack.setTrackstatus(datarep.getItemstatus());
											new TbranchitemtrackDAO().save(session, objTrack);
										}

										datarep.setItemstatus(AppUtils.STATUS_REPAIR_FAILED);
										datarep.setTrepairdlv(trd);
										oDao.save(session, datarep);

										List<Tpinpaditem> tpiList = new TpinpaditemDAO().listNativeByFilter(
												"mproducttypefk = "
														+ obj.getMproduct().getMproducttype().getMproducttypepk()
														+ " and itemno = '" + datarep.getItemno().trim() + "'",
												"itemno");
										if (tpiList.size() > 0) {
											for (Tpinpaditem tpi : tpiList) {
												tpi.setStatus(AppUtils.STATUS_REPAIR_FAILED);
												new TpinpaditemDAO().save(session, tpi);
											}
										}
									}

									Tpaket paket = new Tpaket();
									paket.setMproduct(obj.getMproduct());
									paket.setOrderdate(obj.getInserttime());
									paket.setPaketid(
											new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_PAKET));
									paket.setProcessedby(oUser.getUserid());
									paket.setProcesstime(new Date());
									paket.setProductgroup(obj.getMproduct().getProductgroup());
									paket.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
									paket.setTotaldata(obj.getItemqty());
									paket.setTotaldone(1);
									paket.setBranchpool(oUser.getMbranch().getBranchid());
									paket.setTrepairdlv(trd);
									new TpaketDAO().save(session, paket);

									Tpaketdata paketdata = new Tpaketdata();
									paketdata.setNopaket(new TcounterengineDAO().generateNopaket());
									paketdata.setIsdlv("N");
									paketdata.setMbranch(obj.getMbranch());
									paketdata.setOrderdate(paket.getOrderdate());
									paketdata.setPaketfinishby(oUser.getUserid());
									paketdata.setPaketfinishtime(new Date());
									paketdata.setPaketstartby(oUser.getUserid());
									paketdata.setPaketstarttime(new Date());
									paketdata.setProductgroup(paket.getProductgroup());
									paketdata.setQuantity(obj.getItemqty());
									paketdata.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
									paketdata.setTpaket(paket);
									new TpaketdataDAO().save(session, paketdata);

									Mproducttype objStock = obj.getMproduct().getMproducttype();
									objStock.setStockunused(objStock.getStockunused() + failList.size());
									new MproducttypeDAO().save(session, objStock);

									transaction.commit();
									session.close();

									doClose();
									Clients.showNotification("Submit proses paket berhasil.", "info", null,
											"middle_center", 3000);

									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());

									mmenu = new MmenuDAO().findByFilter("menupath = '/view/repair/repairprocess.zul'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					});
		} else {
			Messagebox.show("Tidak ada data.", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winRepairItem, isSaved);
		Events.postEvent(closeEvent);
	}

	public Validator getValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(ValidationContext ctx) {
				try {
					String itemno = (String) ctx.getProperties("itemno")[0].getValue();
					if (itemno == null || "".equals(itemno.trim()))
						this.addInvalidMessage(ctx, "itemno", Labels.getLabel("common.validator.empty"));
					String tid = (String) ctx.getProperties("tid")[0].getValue();
					if (tid == null || "".equals(tid.trim()))
						this.addInvalidMessage(ctx, "tid", Labels.getLabel("common.validator.empty"));
					String mid = (String) ctx.getProperties("mid")[0].getValue();
					if (mid == null || "".equals(mid.trim()))
						this.addInvalidMessage(ctx, "mid", Labels.getLabel("common.validator.empty"));
				} catch (Exception e) {
					e.printStackTrace();

				}

			}
		};
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getPinpadtype() {
		return pinpadtype;
	}

	public void setPinpadtype(String pinpadtype) {
		this.pinpadtype = pinpadtype;
	}

	public String getPinpadmemo() {
		return pinpadmemo;
	}

	public void setPinpadmemo(String pinpadmemo) {
		this.pinpadmemo = pinpadmemo;
	}

	public Trepair getObj() {
		return obj;
	}

	public void setObj(Trepair obj) {
		this.obj = obj;
	}

	public Trepairitem getObjForm() {
		return objForm;
	}

	public void setObjForm(Trepairitem objForm) {
		this.objForm = objForm;
	}

	public Tpinpaditem getObjPinpad() {
		return objPinpad;
	}

	public void setObjPinpad(Tpinpaditem objPinpad) {
		this.objPinpad = objPinpad;
	}

	public List<Trepairitem> getObjList() {
		return objList;
	}

	public void setObjList(List<Trepairitem> objList) {
		this.objList = objList;
	}

	public Integer getOutstanding() {
		return outstanding;
	}

	public void setOutstanding(Integer outstanding) {
		this.outstanding = outstanding;
	}

	public Integer getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(Integer totalrecord) {
		this.totalrecord = totalrecord;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Integer getTotalvendor() {
		return totalvendor;
	}

	public void setTotalvendor(Integer totalvendor) {
		this.totalvendor = totalvendor;
	}
}