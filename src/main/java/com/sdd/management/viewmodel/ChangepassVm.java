package com.sdd.caption.viewmodel;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MuserDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class ChangepassVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	
	private Session session;
	private Transaction transaction;
	
	private MuserDAO oDao = new MuserDAO();
		
	private String oldPass;
	private String newPass;
	private String confirmNewPass;
	
	@Wire
	private Window win;	
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
	}
	
	@Command("save")
	@NotifyChange("*")
	public void save() {		
		Muser oUser = (Muser) zkSession.getAttribute("oUser");
		try {			
			Muser oForm = (Muser) oDao.findByPk(oUser.getMuserpk());
			String passencrypted = SysUtils.encryptionCommand(oldPass.trim());
			if (oForm.getPassword().equals(passencrypted)) {
				try {
					session = StoreHibernateUtil.openSession();
					transaction = session.beginTransaction();
					oForm.setPassword(SysUtils.encryptionCommand(newPass.trim()));
					oDao.save(session, oForm);
					transaction.commit();
					session.close();
					Clients.showNotification("Perubahan Password Sukses", "info",
							null, "middle_center", 3000);
					Event closeEvent = new Event( "onClose", win, null);
					Events.postEvent(closeEvent);
					/*oldPass = "";
					newPass = "";
					confirmNewPass = "";
					compFocs.setFocus(true);
					Messagebox.show("Change Password Successful", null, Messagebox.OK,  Messagebox.INFORMATION);*/
				} catch (Exception e) {
					transaction.rollback();
					Messagebox.show(e.getMessage(), null, Messagebox.OK,  Messagebox.ERROR);
					e.printStackTrace();
				}
			} else {
				Messagebox.show("Invalid your password", null, Messagebox.OK,  Messagebox.EXCLAMATION);
			}
		} catch (Exception e) {
			Messagebox.show(e.getMessage(), null, Messagebox.OK,  Messagebox.ERROR);
			e.printStackTrace();			
		}
		
	}

	public Validator getValidator() {
		return new AbstractValidator() {
			public void validate(ValidationContext ctx) {
				String oldPass = (String) ctx.getProperties("oldPass")[0]
						.getValue();
				String newPass = (String) ctx
						.getProperties("newPass")[0].getValue();
				String confirmNewPass = (String) ctx
						.getProperties("confirmNewPass")[0].getValue();
				
				if (oldPass == null || "".equals(oldPass.trim()))
					this.addInvalidMessage(ctx, "oldPass",
							"You must enter this field");
				if (newPass == null || "".equals(newPass.trim()))
					this.addInvalidMessage(ctx, "newPass",
							"You must enter this field");
				if (confirmNewPass == null || "".equals(confirmNewPass.trim()))
					this.addInvalidMessage(ctx, "confirmNewPass",
							"You must enter this field");
				
				if (newPass != null && confirmNewPass != null) {
					if (!newPass.equals(confirmNewPass)) 
						this.addInvalidMessage(ctx, "confirmNewPass",
								"Confirm New Password not matched with New Password");
					if (newPass.length() < 6)
						this.addInvalidMessage(ctx, "newPass",
								"Minimal length of new password is 6 chacarter");
				}				
			}

		};
	}
	
	public String getOldPass() {
		return oldPass;
	}

	public void setOldPass(String oldPass) {
		this.oldPass = oldPass;
	}

	public String getNewPass() {
		return newPass;
	}

	public void setNewPass(String newPass) {
		this.newPass = newPass;
	}

	public String getConfirmNewPass() {
		return confirmNewPass;
	}

	public void setConfirmNewPass(String confirmNewPass) {
		this.confirmNewPass = confirmNewPass;
	}
		
}
