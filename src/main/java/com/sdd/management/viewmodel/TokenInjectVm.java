package com.sdd.caption.viewmodel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.TtokenitemDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.domain.Vtokenserial;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class TokenInjectVm {

	private Vtokenserial obj;
	private TtokenitemDAO oDao = new TtokenitemDAO();
	private MproducttypeDAO mproducttypeDao = new MproducttypeDAO();

	private int totaldata;
	private int totalupdated;
	private int totalnotupdated;
	private String filename;
	private int total;
	private Media media;

	@Wire
	private Button btnBrowse;
	@Wire
	private Button btnSave;
	@Wire
	private Listbox listbox;
	@Wire
	private Paging paging;
	@Wire
	private Groupbox gbResult;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) throws Exception {
		Selectors.wireComponents(view, this, false);
		doReset();
	}

	@NotifyChange("filename")
	@Command
	public void doBrowseProduct(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
		media = event.getMedia();
		filename = media.getName();
		if (media != null)
			btnSave.setDisabled(false);

	}

	@Command
	@NotifyChange("*")
	public void doSave() {
		if (media != null) {
			BufferedReader reader = null;
			try {
				if (media.isBinary()) {
					reader = new BufferedReader(new InputStreamReader(media.getStreamData()));
				} else {
					reader = new BufferedReader(media.getReaderData());
				}

				String line = "";
				while ((line = reader.readLine()) != null) {
					String itemno = line.trim().substring(0, line.trim().length());
					Ttokenitem obj = oDao.findById(itemno);
					if (obj != null) {
						if (obj.getStatus().equals(AppUtils.STATUS_SERIALNO_OUTINVENTORY)) {
							Session session = StoreHibernateUtil.openSession();
							Transaction transaction = session.beginTransaction();
							try {
								obj.setItemnoinject(line.trim());
								obj.setStatus(AppUtils.STATUS_SERIALNO_INJECTED);
								oDao.save(session, obj);
								
								Mproducttype objStock = obj.getTincoming().getMproducttype();
								objStock.setStockinjected(objStock.getStockinjected() + 1);
								objStock.setStockreserved(objStock.getStockreserved() - 1);
								mproducttypeDao.save(session, objStock);

								transaction.commit();
								totalupdated++;
							} catch (Exception e) {
								transaction.rollback();
								e.printStackTrace();
							} finally {
								session.close();
							}
						} else {
							totalnotupdated++;
						}
					} else {
						totalnotupdated++;
					}
					totaldata++;
				}
				Clients.showNotification("Upload data inject token selesai", "info", null, "middle_center", 1500);
				gbResult.setVisible(true);
				btnSave.setDisabled(true);
			} catch (HibernateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Messagebox.show("Silahkan browse file inject token", "Info", Messagebox.OK, Messagebox.INFORMATION);
		}

	}
	
	@Command
	@NotifyChange("*")
	public void doView(@BindingParam("arg") String arg) {
		Map<String, Object> map = new HashMap<>();
		
		if (arg.equals("total")) {
			map.put("isTotal", "Y");
		} else if (arg.equals("outstanding")) {
			map.put("isOutstanding", "Y");
		} else if (arg.equals("injected")) {
			map.put("isInjected", "Y");
		} else if (arg.equals("outproduksi")) {
			map.put("isOutproduksi", "Y");
		}
		
		Window win = (Window) Executions.createComponents("/view/token/tokenserial.zul", null, map);
		win.setWidth("40%");
		win.setClosable(true);
		win.doModal();
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws Exception {
		totaldata = 0;
		totalupdated = 0;
		media = null;
		gbResult.setVisible(false);
		filename = null;
		btnBrowse.setDisabled(false);
		btnSave.setDisabled(true);
		List<Vtokenserial> objList = oDao.countSerialStatus();
		for(Vtokenserial obj : objList) {
			this.obj = obj;
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getTotaldata() {
		return totaldata;
	}

	public void setTotaldata(int totaldata) {
		this.totaldata = totaldata;
	}

	public Vtokenserial getObj() {
		return obj;
	}

	public void setObj(Vtokenserial obj) {
		this.obj = obj;
	}

	public int getTotalupdated() {
		return totalupdated;
	}

	public void setTotalupdated(int totalupdated) {
		this.totalupdated = totalupdated;
	}

	public int getTotalnotupdated() {
		return totalnotupdated;
	}

	public void setTotalnotupdated(int totalnotupdated) {
		this.totalnotupdated = totalnotupdated;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

}
