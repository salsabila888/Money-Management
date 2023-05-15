package com.sdd.caption.viewmodel;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TderivatifDAO;
import com.sdd.caption.domain.Tderivatif;
import com.sdd.utils.db.StoreHibernateUtil;

public class DerivatifVerifyVm {
	
	private TderivatifDAO oDao = new TderivatifDAO();
	
	private Tderivatif objForm;
	private Boolean isSaved;
	
	@Wire
	private Window winVerify;
	@Wire
	private Checkbox chkScan;
	@Wire
	private Checkbox chkCrop;
	@Wire
	private Checkbox chkMerge;
	@Wire
	private Datebox dateScan;
	@Wire
	private Datebox dateCrop;
	@Wire
	private Datebox dateMerge;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tderivatif obj) {
		Selectors.wireComponents(view, this, false);
		objForm = obj;
		if (objForm.getScandate() != null) {
			chkScan.setChecked(true);
			chkScan.setDisabled(true);
			dateScan.setDisabled(true);
		}
		if (objForm.getCropdate() != null) {
			chkCrop.setChecked(true);
			chkCrop.setDisabled(true);
			dateCrop.setDisabled(true);
		}
		if (objForm.getMergedate() != null) {
			chkMerge.setChecked(true);
			chkMerge.setDisabled(true);
			dateMerge.setDisabled(true);
		}
	}
	
	@Command
	public void doSave() {
		if (!chkScan.isChecked() && !chkCrop.isChecked() && !chkMerge.isChecked()) {
			Messagebox.show("Tidak ada status yang terisi", WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.EXCLAMATION);
		} else {
			Session session = StoreHibernateUtil.openSession();
			Transaction transaction = session.beginTransaction();
			try {
				if (chkScan.isChecked()) {
					objForm.setIsscan("Y");
				}
				if (chkCrop.isChecked()) {
					objForm.setIscrop("Y");
				}
				if (chkMerge.isChecked()) {
					objForm.setIsmerge("Y");
				}
				
				String msg = "";
				if (chkScan.isChecked() && chkCrop.isChecked() && chkMerge.isChecked()) {
					objForm.setStatus(objForm.getStatus() + 1);
					isSaved = true;
					msg = "Proses verifikasi foto selesai";				
				} else {
					isSaved = false;
					msg = "Update status proses verifikasi foto berhasil";
				}
				
				oDao.save(session, objForm);
				transaction.commit();
				
				Clients.showNotification(msg, "info", null, "middle_center", 3000);			
				doClose();
			} catch (Exception e) {
				transaction.rollback();
				e.printStackTrace();
				Messagebox.show(e.getMessage(), WebApps.getCurrent().getAppName(), Messagebox.OK, Messagebox.ERROR);
			} finally {
				session.close();
			}
		}
		
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winVerify, isSaved);
		Events.postEvent(closeEvent);
	}
	
	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {				
				Date scandate = (Date) ctx.getProperties("scandate")[0].getValue();
				Date cropdate = (Date) ctx.getProperties("cropdate")[0].getValue();
				Date mergedate = (Date) ctx.getProperties("mergedate")[0].getValue();
				
				if (chkScan.isChecked() && scandate == null)
					this.addInvalidMessage(ctx, "scandate", Labels.getLabel("common.validator.empty"));				
				if (chkCrop.isChecked() && cropdate == null)
					this.addInvalidMessage(ctx, "cropdate", Labels.getLabel("common.validator.empty"));
				if (chkMerge.isChecked() && mergedate == null)
					this.addInvalidMessage(ctx, "mergedate", Labels.getLabel("common.validator.empty"));
				
				if (scandate != null && cropdate != null) {
					if (scandate.compareTo(cropdate) > 0) {
						this.addInvalidMessage(ctx, "cropdate", "Tanggal proses crop lebih kecil dari tanggal proses scan");
					} 
				} 				
				if (cropdate != null && mergedate != null) {
					if (cropdate.compareTo(mergedate) > 0) {
						this.addInvalidMessage(ctx, "mergedate", "Tanggal proses merge lebih kecil dari tanggal proses crop");
					} 
				}
			}
		};
	}

	public Tderivatif getObjForm() {
		return objForm;
	}

	public void setObjForm(Tderivatif objForm) {
		this.objForm = objForm;
	}

}
