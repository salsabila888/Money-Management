package com.sdd.caption.viewmodel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Separator;

import com.sdd.caption.dao.TproductmmDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Tproductmm;
import com.sdd.utils.db.StoreHibernateUtil;

public class RekonCardMergingVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private TproductmmDAO tproductmmDao = new TproductmmDAO();
	private Date orderdate;	
	private Media media;
	
	private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	
	@Wire
	private Button btnBrowse;
	@Wire
	private Button btnSave;
	@Wire
	private Paging paging;
	@Wire
	private Div divFiles;
	@Wire
	private Div divResult;
	@Wire
	private Div divResultList;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");		
		doReset();
	}

	@Command
	public void doBrowseProduct(
			@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
		media = event.getMedia();
		divFiles.appendChild(new Label(media.getName()));
		divFiles.appendChild(new Separator());
		if (media != null)
			btnSave.setDisabled(false);
	}
	
	@Command
	@NotifyChange("*")
	public void doSubmit() {	
		if (orderdate == null) {
			Messagebox.show("Silahkan isi tanggal data", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else if (media == null) {
			Messagebox.show("Silahkan browse file report merging", "Info", Messagebox.OK, Messagebox.INFORMATION);
		} else {
			BufferedReader reader = null;
			Session session = StoreHibernateUtil.openSession();
			Transaction transcation = null;
			try {
				Map<String, Tproductmm> mapProductmm = new HashMap<>();
				for (Tproductmm productmm: tproductmmDao.listByFilter("orderdate = '" + dateFormatter.format(orderdate) + "'", "productcode")) {
					mapProductmm.put(productmm.getProductcode(), productmm);
				}
				
				if (media.isBinary()) {
					reader = new BufferedReader(new InputStreamReader(
							media.getStreamData()));
				} else {
					reader = new BufferedReader(media.getReaderData());
				}
				
				String line = "";
				String productcode = "";
				Integer total = 0;				
				while ((line = reader.readLine()) != null) {
					try {
						if (line.trim().startsWith("Product")) {
							productcode = (line.substring(line.indexOf(":") + 1)).trim().toUpperCase();
						}
						if (line.trim().startsWith("Total")) {
							total = Integer.parseInt((line.substring(line.indexOf(":") + 1)).trim());							
							Tproductmm tproductmm = mapProductmm.get(productcode);
							if (tproductmm != null) {
								try {
									transcation = session.beginTransaction();
									tproductmm.setTotalmerge(tproductmm.getTotalmerge() + total);
									tproductmm.setTotalos(tproductmm.getTotaldata() - tproductmm.getTotalmerge());
									tproductmm.setRekontime(new Date());
									tproductmm.setRekonby(oUser.getUserid());
									if (tproductmm.getTotalos().equals(0))
										tproductmm.setIsmatch("Y");
									else tproductmm.setIsmatch("N");
																		
									tproductmmDao.save(session, tproductmm);
									transcation.commit();
								} catch (Exception e) {
									e.printStackTrace();
								}								
							}							
						}															
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				btnBrowse.setDisabled(true);
				btnSave.setDisabled(true);
			} catch (HibernateException e) {
				e.printStackTrace();					
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				session.close();
			}
			Clients.showNotification("Proses rekonsiliasi mandatory merging selesai",
					"info", null, "middle_center", 1500);
			divResult.setVisible(true);
			btnSave.setDisabled(true);	
			
			Map<String, Object> mapResult = new HashMap<>();
			mapResult.put("orderdate", orderdate);
			Executions.createComponents("/view/report/reportmandatorymerging.zul", divResultList, mapResult);
		}
	}	
	
	@Command
	@NotifyChange("*")
	public void doReset() {
		orderdate = new Date();
		btnBrowse.setDisabled(false);
		btnSave.setDisabled(true);
		divFiles.getChildren().clear();
		media = null;
		divResult.setVisible(false);
		divResultList.getChildren().clear();
	}

	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}
	

}
