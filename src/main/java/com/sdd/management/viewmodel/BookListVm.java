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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
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

import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MsysparamDAO;
import com.sdd.caption.dao.MusergrouplevelDAO;
import com.sdd.caption.dao.TbookdataDAO;
import com.sdd.caption.dao.TbookfileDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergrouplevel;
import com.sdd.caption.domain.Tbookdata;
import com.sdd.caption.domain.Tbookfile;
import com.sdd.caption.handler.CemtextGenerator;
import com.sdd.caption.handler.NotifHandler;
import com.sdd.caption.model.TbookFileListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.FTPController;
import com.sdd.utils.StringUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class BookListVm {

	Session zkSession = Sessions.getCurrent();

	private TbookFileListModel model;
//	private TbookfileDAO oDao = new TbookfileDAO();
	private TbookdataDAO oDao = new TbookdataDAO();

	private Muser oUser;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String orderby;
	private String filter;
	private String filterExport;
	private Integer year;
	private Integer month;
	private String status;
	private String producttype;
	private String arg;
	private String groupname;

	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	Map<String, Mbranch> mapBranch = new HashMap<String, Mbranch>();

	@Wire
	private Combobox cbMonth;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Row rowStatus;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		try {
			for (Mbranch data : AppData.getMbranch()) {
				mapBranch.put(data.getBranchid(), data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.arg = arg;

		if (!arg.equals("generate"))
			rowStatus.setVisible(false);

		doReset();

		System.out.println("GROUP NAME : " + groupname);
		paging.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);

			}
		});

		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tbookfile>() {

				@Override
				public void render(Row row, final Tbookfile data, int index) throws Exception {
					if (data.getStatus().trim().equals(AppUtils.STATUS_CEMTEXT_WAITAPPROVAL)) {
						Musergrouplevel grouplevel = new MusergrouplevelDAO()
								.findByFilter("musergroupfk = " + oUser.getMusergroup().getMusergrouppk());
						if (grouplevel != null) {
							groupname = grouplevel.getMusergoup().getUsergroupname();
						}
					} else {
						groupname = "";
					}

					row.getChildren().add(new Label(String.valueOf((SysUtils.PAGESIZE * pageStartNumber) + index + 1)));

//					row.getChildren().add(new Label(data.getBookid()));
					A a = new A(data.getBookid());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							Map<String, Object> map = new HashMap<>();
							Window win = new Window();
							map.put("obj", data);
							map.put("arg", arg);
							win = (Window) Executions.createComponents("/view/pembukuan/bookdata.zul", null, map);
							win.setWidth("80%");
							win.setClosable(true);
							win.doModal();
						}
					});
					row.getChildren().add(a);

					row.getChildren().add(new Label(
							data.getBooktime() != null ? datetimeLocalFormatter.format(data.getBooktime()) : "-"));

					row.getChildren()
							.add(new Label(mapBranch.get(data.getTdelivery().getBranchpool()).getBranchname()));
					row.getChildren().add(new Label(data.getTdelivery().getMbranch().getBranchname()));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotaldata())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalsuccess())));
					row.getChildren().add(new Label(NumberFormat.getInstance().format(data.getTotalerror())));
					row.getChildren()
							.add(new Label(data.getTotalamount() != null
									? NumberFormat.getInstance().format(data.getTotalamount())
									: "-"));
					row.getChildren().add(new Label(data.getStatusdesc() + " " + groupname));

					Div div = new Div();
					div.setClass("btn-group");

					Button btnResend = new Button("Resend");
					btnResend.setAutodisable("self");
					btnResend.setClass("btn btn-primary btn-sm");
					btnResend.setStyle("border-radius: 8px;");
					btnResend.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
							org.hibernate.Session session = StoreHibernateUtil.openSession();
							Transaction trx = null;
							try {
								String status = "";
								String statusdesc = "";
								String host = "";
								int port = 21;
								String username = "";
								String password = "";
								String folder = "";
								for (Msysparam sysparam : new MsysparamDAO().listByFilter("paramgroup = 'ICON'",
										"paramcode")) {
									if (sysparam.getParamcode().equals(AppUtils.PARAM_ICONHOST))
										host = sysparam.getParamvalue().trim();
									else if (sysparam.getParamcode().equals(AppUtils.PARAM_ICONPORT))
										port = Integer.parseInt(sysparam.getParamvalue().trim());
									else if (sysparam.getParamcode().equals(AppUtils.PARAM_ICONUSER))
										username = sysparam.getParamvalue().trim();
									else if (sysparam.getParamcode().equals(AppUtils.PARAM_ICONPASSWORD))
										password = sysparam.getParamvalue();
									if (sysparam.getParamcode().equals(AppUtils.PARAM_CEMTEXTFOLDER))
										folder = sysparam.getParamvalue();
								}

								if (host.length() > 0) {
									String path = Executions.getCurrent().getDesktop().getWebApp()
											.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.BOOKFILE_PATH);
									status = new FTPController(host, port, username, password,
											path + data.getBookid().trim(), folder).upload();
								} else {
									status = AppUtils.STATUS_CEMTEXT_UNDEFINED;
									statusdesc = AppData.getStatusLabel(AppUtils.STATUS_CEMTEXT_UNDEFINED);
								}

								trx = session.beginTransaction();
								data.setStatus(status);
								data.setStatusdesc(statusdesc);
								new TbookfileDAO().save(session, data);

								for (Tbookdata bookdata : new TbookdataDAO()
										.listByFilter("tbookfilefk = " + data.getTbookfilepk(), "tbookdatapk")) {
									bookdata.setStatus(status);
									bookdata.setLastupdated(new Date());
									new TbookdataDAO().save(session, bookdata);
								}
								trx.commit();
							} catch (HibernateException e) {
								trx.rollback();
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								session.close();
							}

						}

					});

					Button btndownload = new Button("Download File");
					btndownload.setAutodisable("self");
					btndownload.setClass("btn btn-success btn-sm");
					btndownload.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btndownload.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@Override
						public void onEvent(Event event) throws Exception {
//							String path = Executions.getCurrent().getDesktop().getWebApp()
//									.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.BOOKFILE_PATH);
//							Filedownload.save(new File(path + "/" + data.getBookid().trim()), "text/plain");

							doExport(data);
						}

					});

					Button btnApprove = new Button("Approve");
					btnApprove.setAutodisable("self");
					btnApprove.setClass("btn btn-primary btn-sm");
					btnApprove.setStyle(
							"border-radius: 8px; background-color: #002459 !important; color: #ffffff !important;");
					btnApprove.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

						@SuppressWarnings({ "rawtypes", "unchecked" })
						@Override
						public void onEvent(Event event) throws Exception {
							Messagebox.show("Apakah anda yakin ingin melanjutkan proses pembukuan?", "Confirm Dialog",
									Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener() {
										@Override
										public void onEvent(Event event) throws Exception {
											if (event.getName().equals("onOK")) {
												
//												----------- UNTUK INTEGRASI PEMBUKUAN KE CORE --------------------------------
												String path = Executions.getCurrent().getDesktop().getWebApp()
														.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.BOOKFILE_PATH);
												System.out.println(path);
												new CemtextGenerator().doGenerate(path, data, oUser);
//												-------------------------------------------------------------------------------
												
//												----------- UNTUK PEMBUKUAN TANPA INTEGRASI KE CORE ---------------------------
//												doApprove(data);
//												-------------------------------------------------------------------------------
												
												Musergrouplevel grouplevel = new MusergrouplevelDAO().findByFilter(
														"musergroupfk = " + oUser.getMusergroup().getMusergrouppk());
												if (grouplevel != null) {
													if (grouplevel.getGrouplevel() == 3) {
														Mmenu mmenu = new MmenuDAO().findByFilter(
																"menupath = '/view/pembukuan/booklist.zul' and menuparamvalue = 'kelompok'");
														NotifHandler.delete(mmenu, oUser.getMbranch(),
																data.getTdelivery().getProductgroup(),
																oUser.getMbranch().getBranchlevel());
													} else if (grouplevel.getGrouplevel() == 2) {
														Mmenu mmenu = new MmenuDAO().findByFilter(
																"menupath = '/view/pembukuan/booklist.zul' and menuparamvalue = 'wakil'");
														NotifHandler.delete(mmenu, oUser.getMbranch(),
																data.getTdelivery().getProductgroup(),
																oUser.getMbranch().getBranchlevel());
													} else if (grouplevel.getGrouplevel() == 1) {
														Mmenu mmenu = new MmenuDAO().findByFilter(
																"menupath = '/view/pembukuan/booklist.zul' and menuparamvalue = 'pimpinan'");
														NotifHandler.delete(mmenu, oUser.getMbranch(),
																data.getTdelivery().getProductgroup(),
																oUser.getMbranch().getBranchlevel());
													}
												}

												Clients.showNotification("Proses pembukuan berhasil.", "info", null,
														"middle_center", 5000);

												doReset();
											}
										}
									});
						}

					});

					if (!arg.equals("generate"))
						div.appendChild(btnApprove);
					else if (data.getStatus().equals(AppUtils.STATUS_CEMTEXT_UPLOADED))
						div.appendChild(btndownload);
					else if(!data.getStatus().equals(AppUtils.STATUS_CEMTEXT_UPLOADED) && !data.getStatus().equals(AppUtils.STATUS_CEMTEXT_WAITAPPROVAL))
						div.appendChild(btnResend);
					row.getChildren().add(div);
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

	}
	
	@NotifyChange("*")
	public void doApprove(Tbookfile obj) {
		try {
			org.hibernate.Session session = StoreHibernateUtil.openSession();
			Transaction trx = session.beginTransaction();
			obj.setStatus(AppUtils.STATUS_CEMTEXT_UPLOADED);
			obj.setStatusdesc(AppData.getStatusLabel(obj.getStatus()));
			obj.setDecisiontime(new Date());
			obj.setDecisionby(oUser.getUserid());
			new TbookfileDAO().save(session, obj);

			for (Tbookdata bookdata : new TbookdataDAO().listByFilter("tbookfilefk = " + obj.getTbookfilepk(),
					"tbookdatapk")) {
				bookdata.setStatus(AppUtils.STATUS_CEMTEXT_UPLOADED);
				bookdata.setLastupdated(new Date());
				new TbookdataDAO().save(session, bookdata);
			}
			trx.commit();
			session.close();
			doReset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		try {
			if (year != null && month != null) {
				filter = "extract(year from booktime) = " + year + " and " + "extract(month from booktime) = " + month
						+ " and tdelivery.mbranchfk = " + oUser.getMbranch().getMbranchpk();

				if (!arg.equals("generate")) {
					filter += " and tbookfile.status = '" + AppUtils.STATUS_CEMTEXT_WAITAPPROVAL + "'";
					if (oUser.getMbranch().getBranchlevel() < 3) {
						Musergrouplevel grouplevel = new MusergrouplevelDAO()
								.findByFilter("musergroupfk = " + oUser.getMusergroup().getMusergrouppk());
						if (grouplevel != null) {
							filter += " and (tbookfile.totalamount between " + grouplevel.getAmountstart() + " and "
									+ grouplevel.getAmountend() + ")";
							groupname = grouplevel.getMusergoup().getUsergroupname();
						}
					}
				}

				if (status != null && status.length() > 0)
					filter += " and tbookfile.status = '" + status + "'";

				needsPageUpdate = true;
				paging.setActivePage(0);
				pageStartNumber = 0;
				refreshModel(pageStartNumber);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doExport(Tbookfile objBook) {
		try {
			filterExport = "tbookfilefk = " + objBook.getTbookfilepk();
			if (filterExport.length() > 0) {
				List<Tbookdata> listData = oDao.listNativeByFilter(filterExport, orderby);
				if (listData != null && listData.size() > 0) {
					XSSFWorkbook workbook = new XSSFWorkbook();
					XSSFSheet sheet = workbook.createSheet();
					XSSFCellStyle style = workbook.createCellStyle();
					style.setBorderTop(BorderStyle.MEDIUM);
					style.setBorderBottom(BorderStyle.MEDIUM);
					style.setBorderLeft(BorderStyle.MEDIUM);
					style.setBorderRight(BorderStyle.MEDIUM);

					int rownum = 0;
					int cellnum = 0;
					Integer no = 0;
					org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
					Cell cell = row.createCell(0);
					cell.setCellValue("DETAIL PEMBUKUAN SURAT BERHARGA");
					rownum++;
					row = sheet.createRow(rownum++);
					cell = row.createCell(0);
					cell.setCellValue("Periode");
					cell = row.createCell(1);
					cell.setCellValue(StringUtils.getMonthLabel(month) + " " + year);
					row = sheet.createRow(rownum++);

					Map<Integer, Object[]> datamap = new TreeMap<Integer, Object[]>();
					datamap.put(1,
							new Object[] { "No", "ID Pembukuan", "Tanggal Pembukuan", "Dibukukan oleh", "Total Data",
									"Total Amount", "Deskripsi Status", "ID Delivery", "Tanggal Pengiriman",
									"Diproses oleh", "Nama Ekspedisi", "Penerima", "Tanggal Terima", "Seq Number",
									"Total Quantity", "Kode Error", "Deskripsi Error" });
					no = 2;
					for (Tbookdata data : listData) {
						datamap.put(no, new Object[] { no - 1, data.getTbookfile().getBookid(),
								datetimeLocalFormatter.format(data.getTbookfile().getBooktime()),
								data.getTbookfile().getBookedby(),
								NumberFormat.getInstance().format(data.getTbookfile().getTotaldata()),
								NumberFormat.getInstance().format(data.getTbookfile().getTotalamount()),
								data.getTbookfile().getStatusdesc(), data.getTdeliverydata().getTdelivery().getDlvid(),
								datetimeLocalFormatter.format(data.getTdeliverydata().getTdelivery().getProcesstime()),
								data.getTdeliverydata().getTdelivery().getProcessedby(),
								data.getTdeliverydata().getTdelivery().getMcouriervendor().getVendorname(),
								data.getTdeliverydata().getTdelivery().getPenerima(),
								datetimeLocalFormatter.format(data.getTdeliverydata().getTdelivery().getTglterima()),
								data.getSeqnum(), data.getQuantity(),
								data.getErrcode() != null ? data.getErrcode() : "-",
								data.getErrdesc() != null ? data.getErrdesc() : "-", });
						no++;
//						total += data.getTbookfile().getTotaldata();
					}
//					datamap.put(no, new Object[] { "", "TOTAL", "", "", total, "", "", "", "", "", "",  });
					Set<Integer> keyset = datamap.keySet();
					for (Integer key : keyset) {
						row = sheet.createRow(rownum++);
						Object[] objArr = datamap.get(key);
						cellnum = 0;
						if (rownum == 5) {
							XSSFCellStyle styleHeader = workbook.createCellStyle();
							styleHeader.setBorderTop(BorderStyle.MEDIUM);
							styleHeader.setBorderBottom(BorderStyle.MEDIUM);
							styleHeader.setBorderLeft(BorderStyle.MEDIUM);
							styleHeader.setBorderRight(BorderStyle.MEDIUM);
							styleHeader.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
							styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
							for (Object obj : objArr) {
								cell = row.createCell(cellnum++);
								if (obj instanceof String) {
									cell.setCellValue((String) obj);
									cell.setCellStyle(styleHeader);
								} else if (obj instanceof Integer) {
									cell.setCellValue((Integer) obj);
									cell.setCellStyle(styleHeader);
								} else if (obj instanceof Double) {
									cell.setCellValue((Double) obj);
									cell.setCellStyle(styleHeader);
								}
							}
						} else {
							for (Object obj : objArr) {
								cell = row.createCell(cellnum++);
								if (obj instanceof String) {
									cell.setCellValue((String) obj);
									cell.setCellStyle(style);
								} else if (obj instanceof Integer) {
									cell.setCellValue((Integer) obj);
									cell.setCellStyle(style);
								} else if (obj instanceof Double) {
									cell.setCellValue((Double) obj);
									cell.setCellStyle(style);
								}
							}
						}
					}

					String path = Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH);
					String filename = "CAPTION_DETAIL_PEMBUKUAN_"
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
		status = AppUtils.STATUS_CEMTEXT_WAITAPPROVAL;
		groupname = "";
		doSearch();
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "tbookfilepk desc";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TbookFileListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		grid.setModel(model);
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

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public Muser getoUser() {
		return oUser;
	}

	public void setoUser(Muser oUser) {
		this.oUser = oUser;
	}

}
