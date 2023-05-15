package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;

/**
 * The persistent class for the mbranch database table.
 * 
 */
@Entity
@Table(name = "mbranch")
@NamedQuery(name = "Mbranch.findAll", query = "SELECT m FROM Mbranch m")
public class Mbranch implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mbranchpk;
	private String branchaddress;
	private String branchcity;
	private String branchcode;
	private String branchid;
	private String branchname;
	private String isheadoffice;
	private String isintercity;
	private Date lastupdated;
	private String updatedby;
	private Integer branchlevel;
	private String zipcode;
	private Mregion mregion;
	private Mcouriervendor mcouriervendor;

	public Mbranch() {

	}

	@Id
	@SequenceGenerator(name = "MBRANCH_MBRANCHPK_GENERATOR", sequenceName = "MBRANCH_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MBRANCH_MBRANCHPK_GENERATOR")
	@Column(unique = true, nullable = false)
	public Integer getMbranchpk() {
		return this.mbranchpk;
	}

	public void setMbranchpk(Integer mbranchpk) {
		this.mbranchpk = mbranchpk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getBranchaddress() {
		return branchaddress;
	}

	public void setBranchaddress(String branchaddress) {
		this.branchaddress = branchaddress;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getBranchcity() {
		return branchcity;
	}

	public void setBranchcity(String branchcity) {
		this.branchcity = branchcity;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getBranchcode() {
		return this.branchcode;
	}

	public void setBranchcode(String branchcode) {
		this.branchcode = branchcode;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getBranchid() {
		return branchid;
	}

	public void setBranchid(String branchid) {
		this.branchid = branchid;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getBranchname() {
		return this.branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsheadoffice() {
		return isheadoffice;
	}

	public void setIsheadoffice(String isheadoffice) {
		this.isheadoffice = isheadoffice;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsintercity() {
		return isintercity;
	}

	public void setIsintercity(String isintercity) {
		this.isintercity = isintercity;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return this.lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getUpdatedby() {
		return this.updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}

	@ManyToOne
	@JoinColumn(name = "mregionfk", nullable = false)
	public Mregion getMregion() {
		return mregion;
	}

	public void setMregion(Mregion mregion) {
		this.mregion = mregion;
	}

	@ManyToOne
	@JoinColumn(name = "mcouriervendorfk")
	public Mcouriervendor getMcouriervendor() {
		return mcouriervendor;
	}

	public void setMcouriervendor(Mcouriervendor mcouriervendor) {
		this.mcouriervendor = mcouriervendor;
	}

	public Integer getBranchlevel() {
		return branchlevel;
	}

	public void setBranchlevel(Integer branchlevel) {
		this.branchlevel = branchlevel;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

}