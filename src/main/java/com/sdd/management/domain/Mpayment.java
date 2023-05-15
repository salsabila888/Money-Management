package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


@Entity
@Table(name="mpayment")
@NamedQuery(name="Mpayment.findAll", query="SELECT m FROM Mpayment m")
public class Mpayment implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mpaymentpk;
	private String paymenttype;
	private String isactive;
	private Date createdtime;
	private String createdby;
	private Date lastupdated;
	private String updatedby;

	public Mpayment() {
	}

	@Id
	@SequenceGenerator(name="MPAYMENT_MPAYMENTPK_GENERATOR", sequenceName = "MPAYMENT_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MPAYMENT_MPAYMENTPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMpaymentpk() {
		return mpaymentpk;
	}

	public void setMpaymentpk(Integer mpaymentpk) {
		this.mpaymentpk = mpaymentpk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getPaymenttype() {
		return paymenttype;
	}

	public void setPaymenttype(String paymenttype) {
		this.paymenttype = paymenttype;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsactive() {
		return isactive;
	}

	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedtime() {
		return createdtime;
	}

	public void setCreatedtime(Date createdtime) {
		this.createdtime = createdtime;
	}

	public String getCreatedby() {
		return createdby;
	}

	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}

	public String getUpdatedby() {
		return updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}

	
}