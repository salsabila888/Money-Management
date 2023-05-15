package com.sdd.caption.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

@Entity
public class Mcoa {
	private Integer mcoapk;
	private String accno;
	private String accname;
	private String acctype;
	private String coatype;
	private String updatedby;
	private Date lastupdated;
	private Integer branchlevel;
	private Mproducttype mproducttype;
	private Mbranch mbranch;

	public Mcoa() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	public Integer getMcoapk() {
		return mcoapk;
	}

	public void setMcoapk(Integer mcoapk) {
		this.mcoapk = mcoapk;
	}

	@Column(length=20)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getAccno() {
		return accno;
	}

	public void setAccno(String accno) {
		this.accno = accno;
	}

	@Column(length=100)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getAccname() {
		return accname;
	}

	public void setAccname(String accname) {
		this.accname = accname;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length=1)
	public String getAcctype() {
		return acctype;
	}

	public void setAcctype(String acctype) {
		this.acctype = acctype;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length=1)
	public String getCoatype() {
		return coatype;
	}

	public void setCoatype(String coatype) {
		this.coatype = coatype;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length=40)
	public String getUpdatedby() {
		return updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}

	public Integer getBranchlevel() {
		return branchlevel;
	}

	public void setBranchlevel(Integer branchlevel) {
		this.branchlevel = branchlevel;
	}

	@ManyToOne
	@JoinColumn(name = "mproducttypefk")
	public Mproducttype getMproducttype() {
		return mproducttype;
	}

	public void setMproducttype(Mproducttype mproducttype) {
		this.mproducttype = mproducttype;
	}

	@ManyToOne
	@JoinColumn(name = "mbranchfk")
	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

}