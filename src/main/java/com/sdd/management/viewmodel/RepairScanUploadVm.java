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

public class RepairScanUploadVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private List<Trepairitem> objList = new ArrayList<>();
	private List<Trepairitem> vendorList = new ArrayList<>();
	private List<Trepairitem> objFailList = new ArrayList<>();
	private List<Tpinpaditem> tpiList = new ArrayList<Tpinpaditem>();
	private List<String> itemnoList = new ArrayList<>();

	private Trepair obj;
	private Trepairitem objItem;
	private Trepairitem objForm;
	private TrepairDAO oDao = new TrepairDAO();
	private TrepairitemDAO itemDao = new TrepairitemDAO();
	private Media media;
	private BigDecimal harga;
	private int totalopr;
	private int totalvendor;
	private int totalfail;
	private int outstanding;
	private String filename;

	@Wire
	private Window winRepairItem;
	@Wire
	private Grid grid, gridVendor, gridFail;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Trepair obj)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.obj = obj;

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
			}
		});
		
		gridFail.setRowRenderer(new RowRenderer<Trepairitem>() {
			@Override
			public void render(final Row row, final Trepairitem data, int index2) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index2 + 1)));
				row.getChildren().add(new Label(data.getItemno() != null ? data.getItemno() : "-"));
				row.getChildren().add(new Label(data.getReplacement() != null ? data.getReplacement() : "-"));
				row.getChildren().add(new Label(data.getTid() != null ? data.getTid() : "-"));
				row.getChildren().add(new Label(data.getMid() != null ? data.getMid() : "-"));
				row.getChildren().add(new Label(
						data.getPinpadtype() != null ? AppData.getPinpadtypeLabel(data.getPinpadtype()) : "-"));
				row.getChildren().add(new Label(data.getPinpadmemo() != null ? data.getPinpadmemo() : "-"));
				row.getChildren().add(new Label(data.getResolution() != null ? data.getResolution() : "-"));
			}
		});
	}

	@NotifyChange("*")
	@Command
	public void doRegisterManual() {
		try {
			if (outstanding > 0) {
				if (!itemnoList.contains(objForm.getItemno().trim())) {
					objItem = itemDao.findByFilter(
							"itemno = '" + objForm.getItemno().trim() + "' and trepairfk = " + obj.getTrepairpk());
					System.out.println(objForm.getItemno());
					if (objItem != null) {
						System.out.println("MASUK");
						if (objItem.getItemstatus().equals(AppUtils.STATUS_REPAIR_PROCESSOPR)) {
							try {
								objItem.setMid(objForm.getMid());
								objItem.setTid(objForm.getTid());
								objItem.setPinpadtype(objForm.getPinpadtype());
								objItem.setPinpadmemo(objForm.getPinpadmemo());
								objItem.setResolution(objForm.getResolution());
								objList.add(objItem);
								outstanding = outstanding - 1;
								totalopr = totalopr + 1;
								itemnoList.add(objForm.getItemno());

								objForm = new Trepairitem();
							} catch (Exception e) {
								Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
								e.printStackTrace();
							}
						} else if (objItem.getItemstatus().equals(AppUtils.STATUS_REPAIR_PROCESSVENDOR)) {
							Messagebox.show(
									"Status pinpad perbaikan vendor tidak dapat diinput. silahkan menggunakan upload file.",
									"Info", Messagebox.OK, Messagebox.INFORMATION);
						}
					} else {
						System.out.println("TIDAK MASUK");
						Tpinpaditem newitem = new TpinpaditemDAO()
								.findByFilter("itemno = '" + objItem.getItemno().trim() + "'");
						if (newitem != null && newitem.getStatus().equals(AppUtils.STATUS_SERIALNO_OUTINVENTORY)) {
							objForm.setItemprice(newitem.getTincoming().getHarga());
							objForm.setTrepair(obj);
							objList.add(objForm);
							outstanding = outstanding - 1;
							totalopr = totalopr + 1;
							objForm = new Trepairitem();
							itemnoList.add(objForm.getItemno());
							tpiList.add(newitem);

							objForm = new Trepairitem();
						} else {
							Messagebox.show("No. Serial tidak ada dalam stock OPR.", "Info", Messagebox.OK,
									Messagebox.INFORMATION);
						}
					}
					doRefresh();
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
	@Command
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		try {
			UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
			media = event.getMedia();
			filename = media.getName();
			if (media != null) {
				if (media.getFormat().contains("xls")) {
					vendorList = new ArrayList<Trepairitem>();
					String serialno = "";
					String tid = "";
					String mid = "";
					String pinpadtype = "";
					String pinpadmemo = "";
					String resolution = "";
					String serialreplacement = "";
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
										serialreplacement = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
										serialreplacement = cell.getStringCellValue();
									}
									break;
								case 3:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
										cell.setCellType(Cell.CELL_TYPE_STRING);
										tid = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
										tid = cell.getStringCellValue();
									}
									break;
								case 4:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
										cell.setCellType(Cell.CELL_TYPE_STRING);
										pinpadtype = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
										pinpadtype = cell.getStringCellValue();
									}
									break;
								case 5:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
										cell.setCellType(Cell.CELL_TYPE_STRING);
										mid = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
										mid = cell.getStringCellValue();
									}
									break;
								case 6:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
										cell.setCellType(Cell.CELL_TYPE_STRING);
										pinpadmemo = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
										pinpadmemo = cell.getStringCellValue();
									}
									break;
								case 7:
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
									objItem = itemDao.findByFilter(
											"itemno = '" + serialno.trim() + "' and trepairfk = " + obj.getTrepairpk());
									if (objItem != null) {
										if (objItem.getItemstatus().equals(AppUtils.STATUS_REPAIR_PROCESSVENDOR)) {
											try {
												objItem.setMid(mid);
												objItem.setTid(tid);
												if (pinpadtype.trim().equals("CS"))
													objItem.setPinpadtype(AppUtils.PINPADTYPE_CS);
												else
													objItem.setPinpadtype(AppUtils.PINPADTYPE_TELLER);
												objItem.setPinpadmemo(pinpadmemo);
												objItem.setResolution(resolution);
												vendorList.add(objItem);
												outstanding = outstanding - 1;
												totalvendor = totalvendor + 1;
												itemnoList.add(objItem.getItemno());
											} catch (Exception e) {
												Messagebox.show(e.getMessage(), "Error", Messagebox.OK,
														Messagebox.ERROR);
												e.printStackTrace();
											}
										} else if (objItem.getItemstatus().equals(AppUtils.STATUS_REPAIR_PROCESSOPR)) {
											try {
												objItem.setMid(mid);
												objItem.setTid(tid);
												if (pinpadtype.trim().equals("CS"))
													objItem.setPinpadtype(AppUtils.PINPADTYPE_CS);
												else
													objItem.setPinpadtype(AppUtils.PINPADTYPE_TELLER);
												objItem.setPinpadmemo(pinpadmemo);
												objItem.setResolution(resolution);
												objList.add(objItem);
												outstanding = outstanding - 1;
												totalopr = totalopr + 1;
												itemnoList.add(objItem.getItemno());
											} catch (Exception e) {
												Messagebox.show(e.getMessage(), "Error", Messagebox.OK,
														Messagebox.ERROR);
												e.printStackTrace();
											}
										} else {
											Tpinpaditem newitem = new TpinpaditemDAO()
													.findByFilter("itemno = '" + serialno.trim() + "'");
											if (newitem != null && newitem.getStatus()
													.equals(AppUtils.STATUS_SERIALNO_OUTINVENTORY)) {
												objItem = new Trepairitem();
												objItem.setItemno(serialno);
												objItem.setMid(mid);
												objItem.setTid(tid);
												if (pinpadtype.trim().equals("CS"))
													objItem.setPinpadtype(AppUtils.PINPADTYPE_CS);
												else
													objItem.setPinpadtype(AppUtils.PINPADTYPE_TELLER);
												objItem.setPinpadmemo(pinpadmemo);
												objItem.setResolution(resolution);
												objItem.setItemprice(newitem.getTincoming().getHarga());
												objItem.setTrepair(obj);
												objList.add(objItem);
												outstanding = outstanding - 1;
												totalopr = totalopr + 1;
												itemnoList.add(objItem.getItemno());
											}
										}
									} else {
										if (serialreplacement != null && serialreplacement.trim().length() > 0) {
											Tpinpaditem newitem = new TpinpaditemDAO()
													.findByFilter("itemno = '" + serialno.trim() + "'");
											if (newitem == null) {
												objItem = new Trepairitem();
												objItem.setItemno(serialno);
												objItem.setMid(mid);
												objItem.setTid(tid);
												if (pinpadtype.trim().equals("CS"))
													objItem.setPinpadtype(AppUtils.PINPADTYPE_CS);
												else
													objItem.setPinpadtype(AppUtils.PINPADTYPE_TELLER);
												objItem.setPinpadmemo(pinpadmemo);
												objItem.setResolution(resolution);
												objItem.setReplacement(serialreplacement);
												objItem.setItemprice(harga);
												objItem.setTrepair(obj);
												vendorList.add(objItem);
												objFailList.add(objItem);
												outstanding = outstanding - 1;
												totalvendor = totalvendor + 1;
												totalfail = totalfail + 1;
												itemnoList.add(objItem.getItemno());
											}
										}
									}
									doRefresh();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					Messagebox.show("Format harus berupa xls/xlsx", "Exclamation", Messagebox.OK,
							Messagebox.EXCLAMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Command
	@NotifyChange("*")
	public void doSubmit() {
		if (objList.size() > 0 || vendorList.size() > 0) {
			String msg = "";
			if (outstanding > 0)
				msg = "Jumlah pemenuhan adalah " + (totalopr + totalvendor) + " dan masih ada sisa outstanding "
						+ outstanding + ". Apakah anda yakin ingin proses sebagian?";
			else
				msg = "Anda ingin melanjutkan proses pemenuhan?";
			Messagebox.show(msg, "Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
					new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							if (event.getName().equals("onOK")) {
								Session session = StoreHibernateUtil.openSession();
								Transaction transaction = session.beginTransaction();
								try {
									obj.setTotalproses(obj.getTotalproses() + (totalopr + totalvendor));
									obj.setTglpemenuhan(new Date());
									if (obj.getItemqty() == obj.getTotalproses())
										obj.setStatus(AppUtils.STATUS_REPAIR_DONEPROCESS);
									obj.setRepairfailed(0);
									oDao.save(session, obj);

									Trepairdlv trd = new Trepairdlv();
									trd.setDlvno(new TcounterengineDAO().generateSeqnum());
									trd.setIsrepairdlv("N");
									trd.setProcessby(oUser.getUsername());
									trd.setProcesstime(new Date());
									trd.setQuantity((totalopr + totalvendor));
									trd.setTotalfail(0);
									trd.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
									trd.setTrepair(obj);
									new TrepairdlvDAO().save(session, trd);

									for (Trepairitem data : objList) {
										Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
												+ data.getItemno() + "' and status = '" + data.getItemstatus() + "'");
										if (objStock != null) {
											objStock.setTid(data.getTid());
											objStock.setMid(data.getMid());
											objStock.setStatus(data.getItemstatus());
											new TbranchstockitemDAO().save(session, objStock);

											Tbranchitemtrack objTrack = new Tbranchitemtrack();
											objTrack.setItemno(data.getItemno());
											objTrack.setTracktime(new Date());
											objTrack.setTrackdesc(AppData.getStatusLabel(data.getItemstatus()));
											objTrack.setProductgroup(data.getTrepair().getMproduct().getProductgroup());
											objTrack.setMproduct(data.getTrepair().getMproduct());
											objTrack.setTrackstatus(data.getItemstatus());
											new TbranchitemtrackDAO().save(session, objTrack);
										}
										data.setItemstatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
										data.setTrepairdlv(trd);
										itemDao.save(session, data);
									}

									for (Trepairitem data : vendorList) {
										Tbranchstockitem objStock = new TbranchstockitemDAO().findByFilter("itemno = '"
												+ data.getItemno() + "' and status = '" + data.getItemstatus() + "'");
										if (objStock != null) {
											objStock.setTid(data.getTid());
											objStock.setMid(data.getMid());
											objStock.setStatus(data.getItemstatus());
											new TbranchstockitemDAO().save(session, objStock);

											Tbranchitemtrack objTrack = new Tbranchitemtrack();
											objTrack.setItemno(data.getItemno());
											objTrack.setTracktime(new Date());
											objTrack.setTrackdesc(AppData.getStatusLabel(data.getItemstatus()));
											objTrack.setProductgroup(data.getTrepair().getMproduct().getProductgroup());
											objTrack.setMproduct(data.getTrepair().getMproduct());
											objTrack.setTrackstatus(data.getItemstatus());
											new TbranchitemtrackDAO().save(session, objTrack);
										}
										data.setItemstatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
										data.setTrepairdlv(trd);
										itemDao.save(session, data);
									}

									for (Trepairitem data : objFailList) {
										Tbranchstockitem objStock = new TbranchstockitemDAO()
												.findByFilter("itemno = '" + data.getReplacement() + "' and status = '"
														+ data.getItemstatus() + "'");
										if (objStock != null) {
											objStock.setTid(data.getTid());
											objStock.setMid(data.getMid());
											objStock.setStatus(data.getItemstatus());
											new TbranchstockitemDAO().save(session, objStock);

											Tbranchitemtrack objTrack = new Tbranchitemtrack();
											objTrack.setItemno(data.getReplacement());
											objTrack.setTracktime(new Date());
											objTrack.setTrackdesc("REPLACEMENT DARI SERIAL PINPAD " + data.getItemno());
											objTrack.setProductgroup(data.getTrepair().getMproduct().getProductgroup());
											objTrack.setMproduct(data.getTrepair().getMproduct());
											objTrack.setTrackstatus(data.getItemstatus());
											new TbranchitemtrackDAO().save(session, objTrack);
										}
										data.setItemstatus(AppUtils.STATUS_REPAIR_FAILED);
										data.setTrepairdlv(trd);
										itemDao.save(session, data);
									}

									for (Tpinpaditem data : tpiList) {
										data.setStatus(AppUtils.STATUS_SERIALNO_OUTPRODUKSI);
										new TpinpaditemDAO().save(session, data);
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
									paket.setTotaldata(trd.getQuantity());
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
									paketdata.setQuantity(trd.getQuantity());
									paketdata.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
									paketdata.setTpaket(paket);
									new TpaketdataDAO().save(session, paketdata);

									Mproducttype objStock = obj.getMproduct().getMproducttype();
									objStock.setStockunused(objStock.getStockunused() + objFailList.size());
									new MproducttypeDAO().save(session, objStock);
									
									transaction.commit();

									doClose();
									Clients.showNotification("Submit proses pemenuhan berhasil.", "info", null,
											"middle_center", 3000);

									Mmenu mmenu = new MmenuDAO()
											.findByFilter("menupath = '/view/delivery/deliveryjob.zul'");
									NotifHandler.doNotif(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());

									mmenu = new MmenuDAO().findByFilter("menupath = '/view/repair/repairprocess.zul'");
									NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getMproduct().getProductgroup(),
											oUser.getMbranch().getBranchlevel());
								} catch (Exception e) {
									transaction.rollback();
									e.printStackTrace();
								} finally {
									session.close();
								}
							}
						}
					});

		} else {
			Messagebox.show("Tidak ada data.", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	public void doClose() {
		Event closeEvent = new Event("onClose", winRepairItem, true);
		Events.postEvent(closeEvent);
	}

	public void doReset() {
		try {
			harga = new BigDecimal(0);
			for (Trepairitem data : itemDao.listByFilter("trepairfk = " + obj.getTrepairpk(), "trepairitempk")) {
				harga = data.getItemprice();
			}
			filename = "";
			totalopr = 0;
			totalvendor = 0;
			totalfail = 0;
			outstanding = obj.getItemqty();
			objForm = new Trepairitem();
			doRefresh();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void doRefresh() {
		grid.setModel(new ListModelList<Trepairitem>(objList));
		gridVendor.setModel(new ListModelList<Trepairitem>(vendorList));
		gridFail.setModel(new ListModelList<Trepairitem>(objFailList));
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

	public Trepair getObj() {
		return obj;
	}

	public void setObj(Trepair obj) {
		this.obj = obj;
	}

	public int getTotalopr() {
		return totalopr;
	}

	public void setTotalopr(int totalopr) {
		this.totalopr = totalopr;
	}

	public int getTotalvendor() {
		return totalvendor;
	}

	public void setTotalvendor(int totalvendor) {
		this.totalvendor = totalvendor;
	}

	public int getOutstanding() {
		return outstanding;
	}

	public void setOutstanding(int outstanding) {
		this.outstanding = outstanding;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Trepairitem getObjItem() {
		return objItem;
	}

	public void setObjItem(Trepairitem objItem) {
		this.objItem = objItem;
	}

	public Trepairitem getObjForm() {
		return objForm;
	}

	public void setObjForm(Trepairitem objForm) {
		this.objForm = objForm;
	}

	public int getTotalfail() {
		return totalfail;
	}

	public void setTotalfail(int totalfail) {
		this.totalfail = totalfail;
	}

}
