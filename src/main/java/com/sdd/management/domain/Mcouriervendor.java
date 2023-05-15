package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


/**
 * The persistent class for the mcouriervendor database table.
 * 
 */
@Entity
@Table(name="mcouriervendor")
@NamedQuery(name="Mcouriervendor.findAll", query="SELECT m FROM Mcouriervendor m")
public class Mcouriervendor implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mcouriervendorpk;
	private String costumerauth;
	private String isintercity;
	private String istracking;
	private Date lastupdated;
	private String updatedby;
	private String urltoken;
	private String urltracking;
	private String vendorcode;
	private String vendorname;
	private String vendorpicemail;
	private String vendorpicname;
	private String vendorpicphone;
	
	public Mcouriervendor() {
	}


	@Id
	@SequenceGenerator(name="MCOURIERVENDOR_MCOURIERVENDORPK_GENERATOR", sequenceName = "MCOURIERVENDOR_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MCOURIERVENDOR_MCOURIERVENDORPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMcouriervendorpk() {
		return this.mcouriervendorpk;
	}

	public void setMcouriervendorpk(Integer mcouriervendorpk) {
		this.mcouriervendorpk = mcouriervendorpk;
	}


	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return this.lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}


	@Column(length=15)
	public String getUpdatedby() {
		return this.updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}


	@Column(length=10)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getVendorcode() {
		return this.vendorcode;
	}

	public void setVendorcode(String vendorcode) {
		this.vendorcode = vendorcode;
	}


	@Column(length=70)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getVendorname() {
		return this.vendorname;
	}

	public void setVendorname(String vendorname) {
		this.vendorname = vendorname;
	}


	@Column(length=100)
	@Type(type = "com.sdd.utils.usertype.TrimLowerCaseUserType")
	public String getVendorpicemail() {
		return this.vendorpicemail;
	}

	public void setVendorpicemail(String vendorpicemail) {
		this.vendorpicemail = vendorpicemail;
	}


	@Column(length=40)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getVendorpicname() {
		return this.vendorpicname;
	}

	public void setVendorpicname(String vendorpicname) {
		this.vendorpicname = vendorpicname;
	}


	@Column(length=20)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getVendorpicphone() {
		return this.vendorpicphone;
	}

	public void setVendorpicphone(String vendorpicphone) {
		this.vendorpicphone = vendorpicphone;
	}


	@Column(length=200)
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getCostumerauth() {
		return costumerauth;
	}


	public void setCostumerauth(String costumerauth) {
		this.costumerauth = costumerauth;
	}


	@Column(length=1)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIstracking() {
		return istracking;
	}


	public void setIstracking(String istracking) {
		this.istracking = istracking;
	}


	@Column(length=1)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsintercity() {
		return isintercity;
	}


	public void setIsintercity(String isintercity) {
		this.isintercity = isintercity;
	}


	@Column(length=200)
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getUrltoken() {
		return urltoken;
	}
	
	public void setUrltoken(String urltoken) {
		this.urltoken = urltoken;
	}


	@Column(length=200)
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getUrltracking() {
		return urltracking;
	}


	public void setUrltracking(String urltracking) {
		this.urltracking = urltracking;
	}
	
}