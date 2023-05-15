package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


@Entity
@Table(name="mexpenses")
@NamedQuery(name="Mexpenses.findAll", query="SELECT m FROM Mexpenses m")
public class Mexpenses implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mexpensespk;
	private String expenses;
	private Date createdtime;
	private String createdby;
	private Date lastupdated;
	private String updatedby;

	public Mexpenses() {
	}

	@Id
	@SequenceGenerator(name="MEXPENSES_MEXPENSESPK_GENERATOR", sequenceName = "MEXPENSES_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MEXPENSES_MEXPENSESPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMexpensespk() {
		return mexpensespk;
	}

	public void setMexpensespk(Integer mexpensespk) {
		this.mexpensespk = mexpensespk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getExpenses() {
		return expenses;
	}

	public void setExpenses(String expenses) {
		this.expenses = expenses;
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