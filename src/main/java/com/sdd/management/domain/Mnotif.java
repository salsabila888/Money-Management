package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;


/**
 * The persistent class for the mnotif database table.
 * 
 */
@Entity
@Table(name="mnotif")
@NamedQuery(name="Mnotif.findAll", query="SELECT t FROM Mnotif t")
public class Mnotif implements Serializable {
	private static final long serialVersionUID = 1L;
	private String notifid;	
	private String notifname;
	private String notifpagepath;
	private String notiftype;

	public Mnotif() {
	}


	@Id
	@Column(unique=true, nullable=false)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getNotifid() {
		return this.notifid;
	}

	public void setNotifid(String notifid) {
		this.notifid = notifid;
	}

	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getNotifname() {
		return notifname;
	}


	public void setNotifname(String notifname) {
		this.notifname = notifname;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getNotifpagepath() {
		return notifpagepath;
	}


	public void setNotifpagepath(String notifpagepath) {
		this.notifpagepath = notifpagepath;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUserType")	
	public String getNotiftype() {
		return notiftype;
	}


	public void setNotiftype(String notiftype) {
		this.notiftype = notiftype;
	}

}