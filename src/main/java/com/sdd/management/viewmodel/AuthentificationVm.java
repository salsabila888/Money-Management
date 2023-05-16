package com.sdd.management.viewmodel;

import java.util.Date;

import org.hibernate.Session;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;

import com.sdd.management.dao.MuserDAO;
import com.sdd.management.domain.Mmenu;
import com.sdd.management.domain.Muser;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class AuthentificationVm {
	
	private String userid;
	private String password;
	private String lblMessage;
		
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
	}
	
	@Command
	@NotifyChange("lblMessage")
	public void doLogin() {	
		Session session = null;
		try {											
			if (userid != null && !userid.trim().equals("") && password != null && !password.trim().equals("")) {
				session = StoreHibernateUtil.openSession();
				Muser oForm = new MuserDAO().login(session, userid);
				Mmenu mmenu = null; 
				if (oForm != null) {
					if (password != null) password = password.trim();								
					String passencrypted = SysUtils.encryptionCommand(password);
					if (oForm.getPassword().equals(passencrypted)) {	
						oForm.setLastlogin(new Date());
						new MuserDAO().save(session, oForm);
						
						Sessions.getCurrent().setAttribute("oUser", oForm);	
						Sessions.getCurrent().setAttribute("menu", mmenu);	
						Executions.sendRedirect("/view/index.zul");
					} else {
						lblMessage = "Invalid your password";
					}				
				} else {
					lblMessage = "Invalid your login id";
				}							
				session.close();
			}			
		} catch (Exception e) {
			lblMessage = "Error : " + e.getMessage();
			e.printStackTrace();
		}		
	}
		
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getLblMessage() {
		return lblMessage;
	}

	public void setLblMessage(String lblMessage) {
		this.lblMessage = lblMessage;
	}
	
}
