package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


@Entity
@Table(name="mbank")
@NamedQuery(name="Mbank.findAll", query="SELECT m FROM Mbank m")
public class Mbank implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mbankpk;
	private String bankname;
	private String accountno;
	private Date createdtime;
	private String createdby;
	private Date lastupdated;
	private String updatedby;

	public Mbank() {
	}

	@Id
	@SequenceGenerator(name="MBANK_MBANKPK_GENERATOR", sequenceName = "MBANK_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MBANK_MBANKPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMbankpk() {
		return mbankpk;
	}

	public void setMbankpk(Integer mbankpk) {
		this.mbankpk = mbankpk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getBankname() {
		return bankname;
	}

	public void setBankname(String bankname) {
		this.bankname = bankname;
	}

	public String getAccountno() {
		return accountno;
	}

	public void setAccountno(String accountno) {
		this.accountno = accountno;
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