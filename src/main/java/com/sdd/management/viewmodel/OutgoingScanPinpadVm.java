/**
 * 
 */
package com.sdd.caption.viewmodel;

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
import org.zkoss.bind.annotation.ImmutableFields;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Tr;
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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.TordermemoDAO;
import com.sdd.caption.dao.ToutgoingDAO;
//import com.sdd.caption.dao.TpinpadorderdataDAO;
import com.sdd.caption.dao.TpinpaditemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Tpinpaditem;
//import com.sdd.caption.domain.Tpinpadorderdata;
//import com.sdd.caption.domain.Tsecuritiesitem;
//import com.sdd.caption.handler.FlowHandler;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OutgoingScanPinpadVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private ToutgoingDAO toutgoingDao = new ToutgoingDAO();
	private TpinpaditemDAO tpiDao = new TpinpaditemDAO();

	private Session session;
	private Transaction transaction;

	private Toutgoing obj;
	private Tpinpaditem objForm;
	private int outstanding;
	private String serialno, memo;
	private String filename;
	private Media media;

//	private List<Tpinpaditem> inList = new ArrayList<>();
	private List<Tpinpaditem> snList = new ArrayList<>();
	private List<String> listData = new ArrayList<>();

	@Wire
	private Window winSerial;
	@Wire
	private Textbox tbSerial;
	@Wire
	private Button btnRegister;
	@Wire
	private Button btnSave;
	@Wire
	private Grid grid;
	@Wire
	private Button btnRegisterBatch;
	@Wire
	private Checkbox chkbox;
	@Wire
	private Tr trmemo;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Toutgoing obj)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.obj = obj;

		grid.setRowRenderer(new RowRenderer<Tpinpaditem>() {

			@Override
			public void render(final Row row, final Tpinpaditem data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getItemno()));
				Button btn = new Button("Cancel");
				btn.setStyle(
						"border-radius: 8px; background-color: #ce0012 !important; color: #ffffff !important; float: right !important;");
				btn.setAutodisable("self");
				btn.setSclass("btn btn-danger btn-sm");
				btn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						Messagebox.show(Labels.getLabel("common.delete.confirm"), "Confirm Dialog",
								Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {

									@Override
									public void onEvent(Event event) throws Exception {
										snList.remove(data);
										listData.remove(data.getItemno().trim());
										refresh();
										outstanding = outstanding + 1;
										BindUtils.postNotifyChange(null, null, OutgoingScanPinpadVm.this,
												"outstanding");
									}
								});
					}
				});

				Div div = new Div();
				div.appendChild(btn);
				row.getChildren().add(div);
			}
		});

		doReset();
	}

	@Command
	@NotifyChange("*")
	public void doChecked() {
		if (chkbox.isChecked())
			trmemo.setVisible(true);
		else
			trmemo.setVisible(false);
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
							}

						}

						if (serialno != null) {
							if (outstanding > 0) {
								objForm = tpiDao.findById(serialno.trim());
								if (objForm != null) {
									if (objForm.getTincoming().getMproducttype().getMproducttypepk()
											.equals(obj.getMproduct().getMproducttype().getMproducttypepk())) {
										if (objForm.getStatus().equals(AppUtils.STATUS_SERIALNO_ENTRY)) {
											try {
												snList.add(objForm);
												listData.add(objForm.getItemno());
												refresh();
												serialno = "";
												outstanding = outstanding - 1;
											} catch (Exception e) {
												Messagebox.show(e.getMessage(), "Error", Messagebox.OK,
														Messagebox.ERROR);
												e.printStackTrace();
											}
										}
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
		serialno = "";
		memo = "";
		objForm = null;
		tbSerial.setFocus(true);
		listData = new ArrayList<>();
		snList = new ArrayList<>();
		refresh();
		outstanding = obj.getItemqty();
	}

	@NotifyChange("*")
	public void refresh() {
		grid.setModel(new ListModelList<Tpinpaditem>(snList));
	}

	@NotifyChange("*")
	@Command
	public void doRegister() {
		try {
			if (outstanding > 0) {
				String sn = "";
				sn = serialno.trim();
				if (!listData.contains(sn)) {
					objForm = tpiDao.findById(serialno.trim());
					if (objForm != null) {
						if (objForm.getTincoming().getMproducttype().getMproducttypepk()
								.equals(obj.getMproduct().getMproducttype().getMproducttypepk())) {
							if (objForm.getStatus().equals(AppUtils.STATUS_SERIALNO_ENTRY)) {
								try {
									snList.add(objForm);
									listData.add(objForm.getItemno());
									refresh();
									serialno = "";
									outstanding = outstanding - 1;
								} catch (Exception e) {
									Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
									e.printStackTrace();
								}
							} else {
								Messagebox.show("Data sudah keluar", "Info", Messagebox.OK, Messagebox.INFORMATION);
							}
						} else {
							Messagebox.show("Jenis produk nomor seri tidak sesuai.", "Info", Messagebox.OK,
									Messagebox.INFORMATION);
						}
					} else {
						Messagebox.show("Data tidak ditemukan", "Info", Messagebox.OK, Messagebox.INFORMATION);
					}
				} else {
					Messagebox.show("Duplicate Data", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("Jumlah data sudah memenuhi jumlah order", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
			tbSerial.setFocus(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	public void doSave() {
		if (outstanding == 0) {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			try {
				obj.setStatus(AppUtils.STATUS_INVENTORY_OUTGOINGSCAN);
				obj.setLastupdated(new Date());
				toutgoingDao.save(session, obj);

				obj.getTorder().setStatus(AppUtils.STATUS_ORDER_PRODUKSI);
				obj.getTorder().setTotalproses(snList.size());
				obj.getTorder().setOrderdate(new Date());
				new TorderDAO().save(session, obj.getTorder());

				for (Tpinpaditem data : snList) {
					data.setStatus(AppUtils.STATUS_SERIALNO_OUTINVENTORY);
					tpiDao.save(session, data);

					Torderitem tpod = new Torderitem();
					tpod.setProductgroup(AppUtils.PRODUCTGROUP_PINPAD);
					tpod.setItemno(data.getItemno());
					tpod.setTpinpaditem(data);
					tpod.setTorder(obj.getTorder());
					tpod.setItemprice(data.getTincoming().getHarga());
					new TorderitemDAO().save(session, tpod);
				}

				Mproducttype objStock = new MproducttypeDAO()
						.findByPk(obj.getMproduct().getMproducttype().getMproducttypepk());
				if (objStock != null) {
					objStock.setLaststock(objStock.getLaststock() - obj.getItemqty());
					objStock.setStockreserved(objStock.getStockreserved() + obj.getItemqty());
					new MproducttypeDAO().save(session, objStock);
				}

//				Mmenu menu = new MmenuDAO()
//						.findByFilter("menupath = '/view/order/orderlist.zul' and menuparamvalue = '03'");
//				NotifHandler.doNotif(menu, oUser.getMbranch(), obj.getProductgroup(),
//						oUser.getMbranch().getBranchlevel());

				Mmenu mmenu = new MmenuDAO()
						.findByFilter("menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'scan'");
				NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(),
						oUser.getMbranch().getBranchlevel());

				transaction.commit();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				session.close();
			}
			Clients.showNotification("Proses verified data order berhasil", "info", null, "middle_center", 3000);
			Event closeEvent = new Event("onClose", winSerial, new Boolean(true));
			Events.postEvent(closeEvent);
		} else {
			if (snList.size() > 0) {
				Messagebox.show(
						obj.getMproduct().getProductname() + " yang terpenuhi berjumlah " + snList.size()
								+ ", apakah anda yakin ingin melanjutkan proses?",
						"Confirm Dialog", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {

							@Override
							public void onEvent(Event event) throws Exception {
								if (event.getName().equals("onOK")) {
									if (memo != null && memo.trim().length() > 0) {
										session = StoreHibernateUtil.openSession();
										transaction = session.beginTransaction();
										try {
											obj.setItemqty(snList.size());
											obj.setStatus(AppUtils.STATUS_ORDER_PRODUKSI);
											obj.setLastupdated(new Date());
											toutgoingDao.save(session, obj);

											obj.getTorder().setTotalproses(snList.size());
											obj.getTorder().setOrdertype(AppUtils.ENTRYTYPE_MANUAL_BRANCH);
											obj.getTorder().setStatus(AppUtils.STATUS_ORDER_PRODUKSI);
											new TorderDAO().save(session, obj.getTorder());

											for (Tpinpaditem data : snList) {
												data.setStatus(AppUtils.STATUS_SERIALNO_OUTINVENTORY);
												tpiDao.save(session, data);

												Torderitem tpod = new Torderitem();
												tpod.setProductgroup(AppUtils.PRODUCTGROUP_PINPAD);
												tpod.setItemno(data.getItemno());
												tpod.setTpinpaditem(data);
												tpod.setTorder(obj.getTorder());
												tpod.setItemprice(data.getTincoming().getHarga());
												new TorderitemDAO().save(session, tpod);
											}

											Tordermemo objMemo = new Tordermemo();
											objMemo.setMemo(memo);
											objMemo.setMemoby(oUser.getUsername());
											objMemo.setMemotime(new Date());
											objMemo.setTorder(obj.getTorder());
											new TordermemoDAO().save(session, objMemo);

											transaction.commit();
										} catch (Exception e) {
											e.printStackTrace();
										} finally {
											session.close();
										}

										Mmenu mmenu = new MmenuDAO().findByFilter(
												"menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'scan'");
										NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(),
												oUser.getMbranch().getBranchlevel());

										Clients.showNotification("Proses verified data order berhasil", "info", null,
												"middle_center", 3000);
										Event closeEvent = new Event("onClose", winSerial, new Boolean(true));
										Events.postEvent(closeEvent);
									} else {
										Messagebox.show("Alasan pemenuhan harus diisi terlebih dahulu.", "Info",
												Messagebox.OK, Messagebox.INFORMATION);
									}
								}
							}
						});
			} else {
				Messagebox.show("Belum ada data.", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		}
	}

	public Validator getValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(ValidationContext ctx) {
				if (serialno == null || serialno.trim().length() == 0)
					this.addInvalidMessage(ctx, "serialno", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	@ImmutableFields
	public Tpinpaditem getObjForm() {
		return objForm;
	}

	public void setObjForm(Tpinpaditem objForm) {
		this.objForm = objForm;
	}

	public Toutgoing getObj() {
		return obj;
	}

	public void setObj(Toutgoing obj) {
		this.obj = obj;
	}

	public int getOutstanding() {
		return outstanding;
	}

	public void setOutstanding(int outstanding) {
		this.outstanding = outstanding;
	}

	public String getSerialno() {
		return serialno;
	}

	public void setSerialno(String serialno) {
		this.serialno = serialno;
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

}
