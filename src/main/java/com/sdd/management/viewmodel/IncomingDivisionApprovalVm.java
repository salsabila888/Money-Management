package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TincomingDAO;
import com.sdd.caption.dao.TpilotingDAO;
import com.sdd.caption.dao.TregisterstockDAO;
import com.sdd.caption.dao.TsecuritiesitemDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tincoming;
import com.sdd.caption.domain.Tpiloting;
import com.sdd.caption.domain.Tregisterstock;
import com.sdd.caption.domain.Tsecuritiesitem;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class IncomingDivisionApprovalVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TpilotingDAO oDao = new TpilotingDAO();

	private String action;
	private Integer totalrecord;

	private List<Tpiloting> objList = new ArrayList<Tpiloting>();
	private List<Tpiloting> objSelected = new ArrayList<Tpiloting>();
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimeLocalFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg)
			throws Exception {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");

		doReset();
		if (grid != null) {
			grid.setRowRenderer(new RowRenderer<Tpiloting>() {
				@Override
				public void render(Row row, final Tpiloting data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					Checkbox check = new Checkbox();
					check.setAttribute("obj", data);
					check.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Checkbox checked = (Checkbox) event.getTarget();
							if (checked.isChecked()) {
								objSelected.add((Tpiloting) checked.getAttribute("obj"));
							} else {
								objSelected.remove((Tpiloting) checked.getAttribute("obj"));
							}
						}
					});
					row.getChildren().add(check);
					row.getChildren().add(new Label(data.getProducttype()));
					row.getChildren().add(new Label(data.getBranchname()));
					row.getChildren().add(new Label(data.getOutlet()));
					row.getChildren().add(new Label(data.getPrefix() != null ? data.getPrefix() : "-"));
					row.getChildren()
							.add(new Label(data.getStartno() != null ? String.valueOf(data.getStartno()) : "-"));
					row.getChildren().add(new Label(data.getEndno() != null ? String.valueOf(data.getEndno()) : "-"));
					row.getChildren().add(new Label(
							data.getTotalitem() != null ? NumberFormat.getInstance().format(data.getTotalqty()) : ""));
					row.getChildren()
							.add(new Label(data.getItemprice() != null
									? "Rp." + NumberFormat.getNumberInstance().format(data.getItemprice())
									: "Rp.0"));
					row.getChildren().add(new Label(AppData.getStatusPilotingLabel(data.getStatus())));
					row.getChildren().add(new Label(data.getSpkno() != null ? data.getSpkno() : "-"));
					row.getChildren().add(
							new Label(data.getSpkdate() != null ? dateLocalFormatter.format(data.getSpkdate()) : "-"));
					row.getChildren().add(new Label(data.getPksno() != null ? data.getPksno() : "-"));
					row.getChildren().add(
							new Label(data.getPksdate() != null ? dateLocalFormatter.format(data.getPksdate()) : "-"));
					row.getChildren().add(new Label(data.getVendorletterno() != null ? data.getVendorletterno() : "-"));
					row.getChildren()
							.add(new Label(data.getVendorletterdate() != null
									? dateLocalFormatter.format(data.getVendorletterdate())
									: "-"));
					row.getChildren().add(new Label(
							data.getManufacturedate() != null ? String.valueOf(data.getManufacturedate()) : "-"));
					row.getChildren()
							.add(new Label(data.getMsupplier() != null ? data.getMsupplier().getSuppliername() : "-"));
					row.getChildren().add(new Label(data.getMemo() != null ? data.getMemo() : "-"));
					row.getChildren().add(new Label(data.getInsertedby() != null ? data.getInsertedby() : "-"));
					row.getChildren().add(new Label(
							data.getInserttime() != null ? datetimeLocalFormatter.format(data.getInserttime()) : "-"));
					A a = new A(data.getFilename());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Sessions.getCurrent().setAttribute("reportPath",
									AppUtils.FILES_ROOT_PATH + AppUtils.REPORT_PATH + data.getFilename());
							Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
						}
					});
					if (data.getFilename() != null)
						row.getChildren().add(a);
					else
						row.getChildren().add(new Label("-"));
				}
			});
		}
	}

	@Command
	public void doCheckedall(@BindingParam("checked") Boolean checked) {
		try {
			objSelected = new ArrayList<Tpiloting>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Checkbox chk = (Checkbox) comp.getChildren().get(1);
				Tpiloting obj = (Tpiloting) chk.getAttribute("obj");
				if (checked) {
					chk.setChecked(true);
					objSelected.add(obj);
				} else {
					chk.setChecked(false);
					objSelected.remove(obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (objSelected.size() > 0) {
			if (action != null && action.length() > 0) {
				if (action.equals(AppUtils.STATUS_APPROVED)) {
					Session session = null;
					Transaction transaction = null;
					try {
						for (Tpiloting data : objSelected) {
							session = StoreHibernateUtil.openSession();
							transaction = session.beginTransaction();
							data.setStatus("DN");
							data.setDecisionby(oUser.getUsername());
							data.setDecisiontime(new Date());
							oDao.save(session, data);

							Tincoming obj = data.getTincoming();
							obj.setStatus(AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED);
							obj.setDecisionby(oUser.getUsername());
							obj.setDecisiontime(new Date());
							obj.setLastupdated(new Date());
							obj.setUpdatedby(oUser.getUserid());
							new TincomingDAO().save(session, obj);

							Tregisterstock objregstock = new Tregisterstock();
							objregstock.setBranch(oUser.getMbranch().getBranchname());
							objregstock.setProductgroup(obj.getProductgroup());
							objregstock.setMproduct(obj.getMproducttype().getProducttype());
							objregstock.setTglincoming(new Date());
							objregstock.setPrefix(obj.getPrefix());
							objregstock.setNumerawalinc(obj.getPrefix().trim() + obj.getItemstartno());
							Integer numerakhirinc = obj.getItemstartno() + obj.getItemqty() - 1;
							objregstock.setNumerakhirinc(obj.getPrefix().trim() + numerakhirinc);
							objregstock.setJumlahinc(obj.getItemqty());
							objregstock.setTgloutgoing(null);
							objregstock.setNumerawaloutg(null);
							objregstock.setNumerakhiroutg(null);
							objregstock.setJumlahoutg(null);
							objregstock.setNumerawalouts(obj.getPrefix().trim() + obj.getItemstartno());
							objregstock.setNumerakhirouts(obj.getPrefix().trim() + numerakhirinc);
							objregstock.setJumlahouts(obj.getItemqty());
							objregstock.setTincomingfk(obj);
							objregstock.setBranchlevel(oUser.getMbranch().getBranchlevel());
							new TregisterstockDAO().save(session, objregstock);

							for (Integer i = data.getStartno(); i <= data.getEndno(); i++) {
								Tsecuritiesitem item = new Tsecuritiesitem();
								item.setTincoming(obj);
								item.setItemno(obj.getPrefix() + i);
								item.setNumerator(i);
								item.setStatus(AppUtils.STATUS_SERIALNO_ENTRY);
								new TsecuritiesitemDAO().save(session, item);
							}

							Mproducttype mproducttype = obj.getMproducttype();
							mproducttype.setLaststock(
									(mproducttype.getLaststock() == null ? 0 : mproducttype.getLaststock())
											+ obj.getItemqty());
							if (mproducttype.getLaststock() >= mproducttype.getStockmin()
									&& mproducttype.getIsalertstockpagu().equals("Y")) {
								mproducttype.setIsalertstockpagu("N");
								mproducttype.setIsblockpagu("N");
								mproducttype.setAlertstockpagurelease(new Date());
							} else {
								mproducttype.setIsalertstockpagu("N");
								mproducttype.setAlertstockpagurelease(new Date());
							}
							new MproducttypeDAO().save(session, mproducttype);

							transaction.commit();
							session.close();
						}
						doReset();
						Clients.showNotification("Proses persetujuan data berhasil disetujui", "info", null,
								"middle_center", 3000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Session session = null;
					Transaction transaction = null;
					try {
						for (Tpiloting data : objSelected) {
							session = StoreHibernateUtil.openSession();
							transaction = session.beginTransaction();
							new TincomingDAO().delete(session, data.getTincoming());

							data.setStatus("DC");
							data.setTincoming(null);
							oDao.save(session, data);
							transaction.commit();
							session.close();
						}
						doReset();
						Clients.showNotification("Proses persetujuan data berhasil ditolak", "info", null,
								"middle_center", 3000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				Messagebox.show("Silahkan pilih action", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} else {
			Messagebox.show("Silahkan pilih data untuk proses submit", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	@NotifyChange("*")
	public void doSearch() {
		try {
			objList = oDao.listByFilter("status = 'WA' and branchid = '" + oUser.getMbranch().getBranchid() + "' and tincomingfk is not null",
					"tpilotingpk");
			grid.setModel(new ListModelList<Tpiloting>(objList));
			totalrecord = objList.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void doReset() {
		objSelected = new ArrayList<Tpiloting>();
		totalrecord = 0;
		doSearch();
	}

	public Integer getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(Integer totalrecord) {
		this.totalrecord = totalrecord;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
