package com.sdd.caption.viewmodel;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.MsysparamDAO;
import com.sdd.caption.dao.MuserDAO;
import com.sdd.caption.dao.MusergroupDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergroup;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class AuthentificationLdapVm {

	private String userid;
	private String password;
	private String ldapurl;
	private String lblMessage;

	private Muser oForm;

	Boolean accessible = false;

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

				oForm = new MuserDAO().login(session, userid.trim());
				if (oForm == null) {
					oForm = new Muser();
				}

				oForm.setUserid(userid.trim());
				Mmenu mmenu = null;
				try {
					ldapurl = "LDAP://192.168.46.147:389";
					List<Msysparam> objList = new MsysparamDAO()
							.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_LDAP + "'", "orderno");
					for (Msysparam param : objList) {
						if (param.getParamcode().equals(AppUtils.PARAM_LDAPURL))
							ldapurl = param.getParamvalue().trim();
					}
					LdapContext ldapContext = getLdapContext();
					SearchControls searchControls = getSearchControls();
					SearchControls searchControlsRole = getSearchRole();
					getUserInfo(ldapContext, searchControls, searchControlsRole);

					if (accessible) {
						Transaction transaction = session.beginTransaction();
						oForm.setLastlogin(new Date());
						Sessions.getCurrent().setAttribute("oUser", oForm);
						Sessions.getCurrent().setAttribute("menu", mmenu);
						
						new MuserDAO().save(session, oForm);
						transaction.commit();
						Executions.sendRedirect("/view/index.zul");
					}

					
				} catch (Exception e) {
					lblMessage = e.getMessage();
					e.printStackTrace();
				}

				session.close();
			}
		} catch (Exception e) {
			lblMessage = "Error : " + e.getMessage();
			e.printStackTrace();
		}
	}

	private LdapContext getLdapContext() throws Exception {
		LdapContext ctx = null;
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "Simple");
			env.put(Context.SECURITY_PRINCIPAL, "uid=" + userid.trim() + ",ou=accounts,o=bni,dc=co,dc=id");
			env.put(Context.SECURITY_CREDENTIALS, password.trim());
			env.put(Context.PROVIDER_URL, ldapurl);
			env.put(Context.REFERRAL, "follow");
			ctx = new InitialLdapContext(env, null);
		} catch (NamingException nex) {
			lblMessage = "Invalid Password.";
			accessible = false;
			nex.printStackTrace();
		}
		return ctx;
	}

	private SearchControls getSearchControls() {
		SearchControls cons = new SearchControls();
		cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String[] attrIDs = { "uid", "cn", "branchalias" };
		cons.setReturningAttributes(attrIDs);
		return cons;
	}

	private SearchControls getSearchRole() {
		SearchControls consRole = new SearchControls();
		consRole.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String[] attrIDs = { "captionrole" };
		consRole.setReturningAttributes(attrIDs);
		return consRole;
	}

	private void getUserInfo(LdapContext ctx, SearchControls searchControls, SearchControls searchControlsRole) {
		System.out.println("*** " + oForm.getUserid().trim() + " ***");
		try {
			NamingEnumeration<SearchResult> answer = ctx.search("ou=accounts,o=bni,dc=co,dc=id",
					"uid=" + oForm.getUserid().trim(), searchControls);

			if (answer.hasMore()) {
				Attributes attrs = answer.next().getAttributes();
				if (attrs.get("cn") != null) {
					oForm.setUsername(attrs.get("cn").get().toString().trim());
				} else {
					oForm.setUsername(oForm.getUserid());
				}

				boolean isBranchValid = true;
				boolean isBranch = true;
				if (attrs.get("branchalias") != null) {
					if (attrs.get("branchalias").get().toString().trim().length() > 0) {
						List<Mbranch> branch = new MbranchDAO().listByFilter(
								"branchid = '" + attrs.get("branchalias").get().toString().trim() + "'", "branchid");
						if (branch.size() > 0) {
							oForm.setMbranch(branch.get(0));
						} else {
							isBranch = false;
						}
					} else {
						isBranchValid = false;
					}
				} else {
					isBranchValid = false;
				}

				if (isBranchValid) {
					if (isBranch) {
						answer = ctx.search("ou=bniapps,o=bni,dc=co,dc=id", "uid=" + oForm.getUserid().trim(),
								searchControlsRole);
						if (answer.hasMore()) {
							attrs = answer.next().getAttributes();
							if (attrs.get("captionrole") != null) {
								System.out.println(
										"attribute name : Role, value : " + attrs.get("captionrole").get().toString());
								if (attrs.get("captionrole").get().toString().trim().length() > 0) {
									List<Musergroup> musergroup = new MusergroupDAO().listByFilter("usergroupcode = '"
											+ attrs.get("captionrole").get().toString().trim() + "'", "usergroupcode");
									if (musergroup.size() > 0) {
										oForm.setMusergroup(musergroup.get(0));
										accessible = true;
									} else {
										lblMessage = "User role not registered in CAPTION.";
										accessible = false;
									}
								} else {
									lblMessage = "User Role not found.";
									accessible = false;
								}
							} else {
								lblMessage = "User Role not found.";
								accessible = false;
							}
						} else {
							lblMessage = "User ID not Valid";
							accessible = false;
						}
					} else {
						lblMessage = "Branch ID not registered in CAPTION.";
						accessible = false;
					}
				} else {
					lblMessage = "Branch ID not found.";
					accessible = false;
				}
			} else {
				lblMessage = "User ID not Valid";
				accessible = false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getLblMessage() {
		return lblMessage;
	}

	public void setLblMessage(String lblMessage) {
		this.lblMessage = lblMessage;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
