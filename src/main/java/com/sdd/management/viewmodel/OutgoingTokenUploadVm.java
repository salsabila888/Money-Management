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
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
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
import com.sdd.caption.dao.TorderDAO;
import com.sdd.caption.dao.TorderitemDAO;
import com.sdd.caption.dao.ToutgoingDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Toutgoing;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.StringUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class OutgoingTokenUploadVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TtokenitemDAO ttiDao = new TtokenitemDAO();
	private ToutgoingDAO toutgoingDao = new ToutgoingDAO();

	private Session session;
	private Transaction transaction;

	private Toutgoing obj;
	private Ttokenitem objForm;
	private String itemno;
	private int outstanding;
	private String filename;
	private Media media;

	private List<Ttokenitem> objList = new ArrayList<Ttokenitem>();
	private List<String> listData = new ArrayList<>();

	@Wire
	private Window winScanToken;
	@Wire
	private Button btnRegister;
	@Wire
	private Button btnSave, btnBrowse;
	@Wire
	private Grid grid;

	@NotifyChange("*")
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Toutgoing obj)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.obj = obj;

		doReset();
		grid.setRowRenderer(new RowRenderer<Ttokenitem>() {

			@Override
			public void render(final Row row, final Ttokenitem data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getItemno()));
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
										objList.remove(data);
										listData.remove(data.getItemno().trim());
										outstanding = outstanding + 1;
										refresh();
										BindUtils.postNotifyChange(null, null, OutgoingTokenUploadVm.this, "outstanding");
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
				if (media.getFormat().contains("xls")) {
					btnSave.setDisabled(false);

					String startno = null;
					String endno = null;
					boolean isNumeric = true;
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
										startno = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
										startno = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
										cell.setCellType(Cell.CELL_TYPE_STRING);
										startno = cell.getStringCellValue();
									}
									break;
								case 3:
									if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
										cell.setCellType(Cell.CELL_TYPE_STRING);
										endno = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
										endno = cell.getStringCellValue();
									} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
										cell.setCellType(Cell.CELL_TYPE_STRING);
										endno = cell.getStringCellValue();
									}
									break;
								}
							}

							if (startno != null && endno != null) {
								System.out.println("START NO : " + startno + ", END NO : " + endno);
								isNumeric = StringUtils.isNumeric(startno);
								if (isNumeric) {
									System.out.println("THIS IS NUMERIC");
									for (Integer i = Integer.parseInt(startno); i <= Integer.parseInt(endno); i++) {
										if (outstanding > 0) {
											Ttokenitem token = new TtokenitemDAO().findByFilter("itemno = '" + i + "'");
											System.out.println("SERIAL NO : " + i);
											if (!listData.contains(token.getItemno().trim())) {
												if (token.getStatus().equals(AppUtils.STATUS_SERIALNO_ENTRY)) {
													objList.add(token);
													listData.add(token.getItemno().trim());
													refresh();
													btnBrowse.setDisabled(true);
													outstanding = outstanding - 1;
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
				} else {
					btnSave.setDisabled(true);
					Messagebox.show("Format data harus berupa xls/xlsx", "Exclamation", Messagebox.OK,
							Messagebox.EXCLAMATION);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	@Command
	public void doRegister() {
		try {
			try {
				if (outstanding > 0) {
					String in = itemno.trim().substring(0, itemno.trim().length() - 1);
					if (!listData.contains(in)) {
						objForm = ttiDao.findById(in);
						if (objForm != null) {
							if (objForm.getStatus().equals(AppUtils.STATUS_SERIALNO_ENTRY)) {
								try {
									objList.add(objForm);
									listData.add(objForm.getItemno());
									refresh();
									itemno = "";
									outstanding = outstanding - 1;
								} catch (Exception e) {
									Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
									e.printStackTrace();
								}
							} else {
								Messagebox.show("Status token sudah keluar dari inventory", "Info", Messagebox.OK,
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@NotifyChange("*")
	@Command
	public void doSave() {
		try {
			if (outstanding == 0) {
				session = StoreHibernateUtil.openSession();
				transaction = session.beginTransaction();
				obj.setStatus(AppUtils.STATUS_ORDER_PRODUKSI);
				obj.setLastupdated(new Date());
				toutgoingDao.save(session, obj);

				obj.getTorder().setStatus(AppUtils.STATUS_ORDER_PRODUKSI);
				obj.getTorder().setTotalproses(objList.size());
				new TorderDAO().save(session, obj.getTorder());

				for (Ttokenitem data : objList) {
					data.setStatus(AppUtils.STATUS_SERIALNO_OUTINVENTORY);
					ttiDao.save(session, data);

					Torderitem tori = new Torderitem();
					tori.setProductgroup(AppUtils.PRODUCTGROUP_TOKEN);
					tori.setItemno(data.getItemno());
					tori.setTtokenitem(data);
					tori.setItemprice(data.getTincoming().getHarga());
					tori.setTorder(obj.getTorder());
					new TorderitemDAO().save(session, tori);

				}

				
				Mproducttype objStock = obj.getMproduct().getMproducttype();
				objStock.setLaststock(objStock.getLaststock() - objList.size());
				objStock.setStockreserved(objStock.getStockreserved() + objList.size());
				new MproducttypeDAO().save(session, objStock);

				transaction.commit();
				session.close();
				
				Mmenu mmenu = new MmenuDAO().findByFilter(
						"menupath = '/view/inventory/outgoing.zul' and menuparamvalue = 'scan'");
				NotifHandler.delete(mmenu, oUser.getMbranch(), obj.getProductgroup(),
						oUser.getMbranch().getBranchlevel());
				
				Clients.showNotification("Proses verified data order berhasil", "info", null, "middle_center", 3000);
				Event closeEvent = new Event("onClose", winScanToken, new Boolean(true));
				Events.postEvent(closeEvent);
			} else {
				Messagebox.show("Masih ada data outstanding. Silahkan selesaikan proses verifikasinya", "Info",
						Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void refresh() {
		grid.setModel(new ListModelList<Ttokenitem>(objList));
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		objForm = null;
		listData = new ArrayList<>();
		outstanding = obj.getItemqty();
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}


	public Ttokenitem getObjForm() {
		return objForm;
	}

	public void setObjForm(Ttokenitem objForm) {
		this.objForm = objForm;
	}

	public int getOutstanding() {
		return outstanding;
	}

	public void setOutstanding(int outstanding) {
		this.outstanding = outstanding;
	}

	public String getItemno() {
		return itemno;
	}

	public void setItemno(String itemno) {
		this.itemno = itemno;
	}

	public Toutgoing getObj() {
		return obj;
	}

	public void setObj(Toutgoing obj) {
		this.obj = obj;
	}
}
