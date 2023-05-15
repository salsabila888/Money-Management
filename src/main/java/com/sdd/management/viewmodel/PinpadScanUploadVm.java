package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.dao.TpinpadorderdataDAO;
import com.sdd.caption.dao.TpinpadserialDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Tpinpadorderdata;
import com.sdd.caption.domain.Tpinpadserial;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PinpadScanUploadVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private Session session;
	private Transaction transaction;

	private TorderDAO oDao = new TorderDAO();
	private TpinpadserialDAO tpsDao = new TpinpadserialDAO();
	private TpinpadorderdataDAO tpodDao = new TpinpadorderdataDAO();

	private Torder obj;
	private Tpinpadserial objSerial;
	private Tpinpadorderdata objForm;
	private int outstanding;
	private int totalfail;
	private String filename;
	private String serialno;
	private Media media;
	private Window winparent;

	private List<String> listData = new ArrayList<>();
	private List<Tpinpadorderdata> snList = new ArrayList<>();
	private List<Tpinpadorderdata> listFail = new ArrayList<>();

	@Wire
	private Button btnRegister;
	@Wire
	private Grid grid;
	@Wire
	private Window winSerialUpload;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder obj,
			@ExecutionArgParam("winid") Window winparent) throws Exception {
		Selectors.wireComponents(view, this, false);
		
		if(winparent!=null)
			this.winparent = winparent;
		
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.obj = obj;
		doReset();
		outstanding = obj.getTotaldata();

		grid.setRowRenderer(new RowRenderer<Tpinpadorderdata>() {

			@Override
			public void render(final Row row, final Tpinpadorderdata data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getTpinpadserial().getSerialno()));
				row.getChildren().add(new Label(AppData.getPinpadtypeLabel(data.getTid())));
				row.getChildren().add(new Label(AppData.getPinpadtypeLabel(data.getPinpadtype())));
				row.getChildren().add(new Label(data.getMemo()));
			}
		});

	}

	@NotifyChange("filename")
	@Command
	public void doBrowse(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
		media = event.getMedia();
		filename = media.getName();
		if (media != null)
			if (media.getFormat().contains("xls")) {
				snList = new ArrayList<>();
				btnRegister.setDisabled(false);
			} else {
				Messagebox.show("Format harus berupa xls/xlsx", "Exclamation", Messagebox.OK, Messagebox.EXCLAMATION);
				btnRegister.setDisabled(true);
			}
	}

	@NotifyChange("*")
	@Command
	public void doRegister() {
		try {
			if (media != null) {
				Workbook wb = null;
				if (filename.trim().toLowerCase().endsWith("xlsx")) {
					wb = new XSSFWorkbook(media.getStreamData());
				} else if (filename.trim().toLowerCase().endsWith("xls")) {
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
									objForm.setTid(cell.getStringCellValue());
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									objForm.setTid(cell.getStringCellValue());
								}
								break;
							case 3:
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									cell.setCellType(Cell.CELL_TYPE_STRING);
									objForm.setPinpadtype(cell.getStringCellValue());
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									objForm.setPinpadtype(cell.getStringCellValue());
								}
								break;
							case 4:
								if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
									cell.setCellType(Cell.CELL_TYPE_STRING);
									objForm.setMemo(cell.getStringCellValue());
								} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
									objForm.setMemo(cell.getStringCellValue());
								}
								break;
							}
						}

						if (outstanding > 0) {
							if (!listData.contains(serialno.trim())) {
								objSerial = tpsDao.findById(serialno.trim());
								if (objSerial != null) {
									if (objSerial.getStatus().equals(AppUtils.STATUS_SERIALNO_OUTINVENTORY)) {
										if (objForm.getPinpadtype()
												.equals(AppData.getPinpadtypeLabel(AppUtils.PINPADTYPE_CS))) {
											objForm.setPinpadtype(AppUtils.PINPADTYPE_CS);
										} else {
											objForm.setPinpadtype(AppUtils.PINPADTYPE_TELLER);
										}
										objForm.setTpinpadserial(objSerial);
										snList.add(objForm);
										listData.add(objSerial.getSerialno());
										serialno = "";
										objForm = new Tpinpadorderdata();
										outstanding = obj.getTotaldata() - snList.size();
									} else if (objSerial.getStatus().equals(AppUtils.STATUS_SERIALNO_ENTRY)) {
										objForm.setTid(serialno.trim());
										objForm.setMemo("Status pinpad belum diverifikasi oleh inventori");
										listFail.add(objForm);
										objForm = new Tpinpadorderdata();
									} else {
										objForm.setTid(serialno.trim());
										objForm.setMemo("Status pinpad sudah diinject");
										listFail.add(objForm);
										objForm = new Tpinpadorderdata();
									}
								} else {
									objForm.setTid(serialno.trim());
									objForm.setMemo("Data Tidak ditemukan");
									listFail.add(objForm);
									objForm = new Tpinpadorderdata();
								}
							} else {
								objForm.setTid(serialno.trim());
								objForm.setMemo("Duplicate Data");
								listFail.add(objForm);
								objForm = new Tpinpadorderdata();
							}
						} else {
							objForm.setTid(serialno.trim());
							objForm.setMemo("Jumlah data sudah memenuhi jumlah order");
							listFail.add(objForm);
							objForm = new Tpinpadorderdata();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				refresh();
				btnRegister.setDisabled(true);
				totalfail = listFail.size();
			} else {
				Messagebox.show("Silahkan upload file nomor serial pinpad", "Exclamation", Messagebox.OK,
						Messagebox.EXCLAMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Command
	public void doSave() {
		if (outstanding == 0) {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			try {
				obj.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
				oDao.save(session, obj);

				for (Tpinpadorderdata data : snList) {
					data.getTpinpadserial().setMbranch(obj.getMbranch());
					data.getTpinpadserial().setStatus(AppUtils.STATUS_SERIALNO_OUTPRODUKSI);
					tpsDao.save(session, data.getTpinpadserial());

					data.setStatus(AppUtils.STATUS_SERIALNO_SCANPRODUKSI);
					data.setTorder(obj);
					tpodDao.save(session, data);
				}

				Tpaket paket = new Tpaket();
				paket.setMproduct(obj.getMproduct());
				paket.setOrderdate(obj.getEntrytime());
				paket.setPaketid(new TcounterengineDAO().generateYearMonthCounter(AppUtils.CE_PAKET));
				paket.setProcessedby(oUser.getUserid());
				paket.setProcesstime(new Date());
				paket.setProductgroup(obj.getMproduct().getProductgroup());
				paket.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
				paket.setTotaldata(obj.getTotaldata());
				paket.setTotaldone(1);
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
				paketdata.setQuantity(obj.getTotaldata());
				paketdata.setStatus(AppUtils.STATUS_DELIVERY_PAKETDONE);
				paketdata.setTpaket(paket);
				new TpaketdataDAO().save(session, paketdata);

				transaction.commit();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				session.close();
			}
			Clients.showNotification("Proses verified data order berhasil", "info", null, "middle_center", 3000);
			Event closeEvent = new Event("onClose", winparent, new Boolean(true));
			Events.postEvent(closeEvent);
		} else {
			Messagebox.show("Masih ada data outstanding. Silahkan selesaikan proses verifikasinya", "Info",
					Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@Command
	@NotifyChange("*")
	public void doView() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("obj", obj);
		map.put("listFail", listFail);
		map.put("isFail", "Y");
		Window win = (Window) Executions.createComponents("/view/pinpad/pinpadscanfail.zul", null, map);
		win.setWidth("80%");
		win.setClosable(true);
		win.doModal();
	}

	@NotifyChange("*")
	public void refresh() {
		grid.setModel(new ListModelList<Tpinpadorderdata>(snList));
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		filename = null;
		objSerial = null;
		serialno = "";
		totalfail = 0;
		objForm = new Tpinpadorderdata();
		btnRegister.setDisabled(true);
	}

	public Tpinpadserial getObjSerial() {
		return objSerial;
	}

	public void setObjSerial(Tpinpadserial objSerial) {
		this.objSerial = objSerial;
	}

	public Tpinpadorderdata getObjForm() {
		return objForm;
	}

	public void setObjForm(Tpinpadorderdata objForm) {
		this.objForm = objForm;
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

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}

	public int getTotalfail() {
		return totalfail;
	}

	public void setTotalfail(int totalfail) {
		this.totalfail = totalfail;
	}

}
