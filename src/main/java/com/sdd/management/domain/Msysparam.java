package com.sdd.management.domain;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


/**
 * The persistent class for the msysparam database table.
 * 
 */
@Entity
@Table(name="msysparam")
@NamedQuery(name="Msysparam.findAll", query="SELECT m FROM Msysparam m")
public class Msysparam implements Serializable {
	private static final long serialVersionUID = 1L;
	private String paramcode;
	private String ismasked;
	private int orderno;
	private String paramdesc;
	private String paramname;
	private String paramgroup;
	private Date lastupdated;
	private String paramvalue;
	private String updatedby;

	public Msysparam() {
	}

	
	@Id
	@Column(unique=true, nullable=false, length=30)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")		
	public String getParamcode() {
		return paramcode;
	}

	public void setParamcode(String paramcode) {
		this.paramcode = paramcode;
	}	
	
	public String getIsmasked() {
		return ismasked;
	}


	public void setIsmasked(String ismasked) {
		this.ismasked = ismasked;
	}


	public int getOrderno() {
		return orderno;
	}


	public void setOrderno(int orderno) {
		this.orderno = orderno;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")			
	public String getParamdesc() {
		return paramdesc;
	}

	public void setParamdesc(String paramdesc) {
		this.paramdesc = paramdesc;
	}
	

	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getParamname() {
		return this.paramname;
	}

	public void setParamname(String paramname) {
		this.paramname = paramname;
	}

	
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getParamgroup() {
		return paramgroup;
	}


	public void setParamgroup(String paramgroup) {
		this.paramgroup = paramgroup;
	}


	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return this.lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}


	@Column(length=100)
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getParamvalue() {
		return this.paramvalue;
	}

	public void setParamvalue(String paramvalue) {
		this.paramvalue = paramvalue;
	}


	@Column(length=15)
	public String getUpdatedby() {
		return this.updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}

}