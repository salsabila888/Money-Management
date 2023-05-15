package com.sdd.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.zkoss.zul.Messagebox;

import com.sdd.caption.dao.MsysparamDAO;
import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.utils.AppUtils;

public class SysUtils {

	public static final int PAGESIZE = 10;
	public static final int MODELSIZE = 1000;
	public static final int IDPROCS_INSERT = 0;
	public static final int IDPROCS_UPDATE = 1;
	public static final int IDPROCS_DELETE = 2;
	public static final int IDPROCS_VIEW = 3;
	public static final String USERS_PASSWORD_DEFAULT = "123456";

	public static final String PATH_FILES_ROOT = "/files";
	public static final String PATH_DATA = "/fromtaspen/";
	public static final String JASPER_PATH = "/jasper";
	public static final String REPORT_PATH = "/report";
	public static final String PATH_TOOLS = "/themes/tools/";

	public static final String FILEHOST_AUTHENTICATION_PASSWORD_LABEL = "PASSWORD";
	public static final String FILEHOST_AUTHENTICATION_PASSWORD_VALUE = "P";
	public static final String FILEHOST_AUTHENTICATION_KEY_LABEL = "KEY FILE";
	public static final String FILEHOST_AUTHENTICATION_KEY_VALUE = "K";

	public static final String STATUS_DOWNLOAD_APPROVAL = "Data Approval";
	public static final String STATUS_REJECTED = "Rejected";
	public static final String STATUS_BLOKIR_REQUEST = "Wait Approval";
	public static final String STATUS_BLOKIR_APPROVED = "Approved";
	public static final String STATUS_BLOKIR_APPROVED_BO = "Approved Back Office";

	public static String token = "";
	public static final String BASE_URL = "https://posindonesia.co.id";
	public static final String URL_PORT = "https://api.posindonesia.co.id:8245";
	public static final String URL_PORT_PATH = "https://api.posindonesia.co.id:8245/utility/1.0.0";
	public static final int PORT = 8245;
	public static final String METHOD_TOKEN = "/token";
	public static final String METHOD_GETFEE = "/getFee";
	public static final String METHOD_GETTRACK = "/getTrackAndTrace";
	public static final String METHOD_GETDETAIL = "/getTrackAndTraceDetail";
	public static final String METHOD_GETSTATUS = "/getTrackAndTraceLastStatus";
	public static final String METHOD_GETTRACK_LN = "/getTrackAndTraceLn";

	public static final String CONSUMER_KEY = "NllJYnRlemZnZ0l2NFdPaXgzdElKWjRoN3BFYTpxY2VETGtreFMyZHJ1a3k0T0YwemdCR2hOZUFh";

	@SuppressWarnings("unused")
	public static Boolean doLoginLDAP(String username, String password) throws Exception {

		Boolean accessible = false;
		LdapContext ctx = null;
		String ldapurl = "LDAP://192.168.12.134:389";
		String message = "";
		try {

			List<Msysparam> objList = new MsysparamDAO()
					.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_LDAP + "'", "orderno");
			for (Msysparam param : objList) {
				if (param.getParamcode().equals(AppUtils.PARAM_LDAPURL)) {
					ldapurl = param.getParamvalue().trim();
				}
			}

			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "Simple");
			// env.put(Context.SECURITY_PRINCIPAL,
			// "cn=devldap;cn=Users;o=bni;dc=co;dc=id");// input user for access to ldap
			env.put(Context.SECURITY_PRINCIPAL, username);// input user & password for access to ldap caption
			env.put(Context.SECURITY_CREDENTIALS, password);
			env.put(Context.PROVIDER_URL, ldapurl);
			env.put(Context.REFERRAL, "follow");
			ctx = new InitialLdapContext(env, null);

			System.out.println("LDAP Connection: COMPLETE");
			accessible = true;

		} catch (AuthenticationNotSupportedException ex) {
			System.out.println("The authentication is not supported by the server");
			message = "The authentication is not supported by the server";
			Messagebox.show(message, "Info", Messagebox.OK, Messagebox.INFORMATION);
			accessible = false;
		} catch (AuthenticationException ex) {
			message = "incorrect password or username";
			System.out.println("incorrect password or username");
			Messagebox.show(message, "Info", Messagebox.OK, Messagebox.INFORMATION);
			accessible = false;
		} catch (NamingException nex) {
			message = "error when trying to create the context : " + nex.getMessage();
			System.out.println("error when trying to create the context : " + nex.getMessage());
			Messagebox.show(message, "Info", Messagebox.OK, Messagebox.INFORMATION);
			accessible = false;
		}
		return accessible;
	}

	public static void doConnectLdap(Muser user, String userid, String username, String usergroup, String branchname) throws Exception {
		LdapContext ctx = null;
		String ldapurl = "LDAP://192.168.46.147:389";
		try {

			List<Msysparam> objList = new MsysparamDAO()
					.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_MAIL + "'", "orderno");
			for (Msysparam param : objList) {
				if (param.getParamcode().equals(AppUtils.PARAM_LDAPURL))
					ldapurl = param.getParamvalue().trim();
			}

			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "Simple");
			env.put(Context.SECURITY_PRINCIPAL, "uid=" + userid + ",ou=accounts,o=bni,dc=co,dc=id");// input user for access to ldap
			//env.put(Context.SECURITY_CREDENTIALS, ldappassword);
			env.put(Context.PROVIDER_URL, ldapurl);
			env.put(Context.REFERRAL, "follow");
			ctx = new InitialLdapContext(env, null);

			if (user == null) {
				SearchControls cons = new SearchControls();
				cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String[] attrIDs = { "uid", "cn", "branchalias" };
				cons.setReturningAttributes(attrIDs);

				NamingEnumeration<SearchResult> answer = ctx.search("ou=accounts,o=bni,dc=co,dc=id", "uid=" + userid,
						cons);
				if (answer.hasMore()) {
					Attributes attrs = answer.next().getAttributes();
					username = attrs.get("cn").get().toString();
					branchname = attrs.get("branchalias").get().toString();
					
					SearchControls consRole = new SearchControls();
					consRole.setSearchScope(SearchControls.SUBTREE_SCOPE);
					String[] attrRole = { "captionrole" };
					consRole.setReturningAttributes(attrRole);
					
					NamingEnumeration<SearchResult> answerRole = ctx.search("ou=bniapps,o=bni,dc=co,dc=id", "uid=" + userid,
							consRole);
					if (answerRole.hasMore()) {
						Attributes attrsRole = answerRole.next().getAttributes();
						usergroup = attrsRole.get("captionrole").get().toString();
					}
				} else {
					Messagebox.show("User ID tidak ditemukan di LDAP", "Info", Messagebox.OK, Messagebox.INFORMATION);
				}
			} else {
				Messagebox.show("User " + userid + " sudah terdaftar di Caption.", "Info", Messagebox.OK,
						Messagebox.INFORMATION);
			}
			System.out.println("LDAP Connection: COMPLETE");
		} catch (NamingException nex) {
			nex.printStackTrace();
			System.out.println(" Auth is False");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String encryptionCommand(String text) throws NoSuchAlgorithmException, Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		byte byteData[] = md5.digest(text.getBytes());

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public static void main(String stArgs[]) {
		try {
			System.out.println("pass : " + encryptionCommand("a"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
