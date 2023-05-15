package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


/**
 * The persistent class for the tscheduler database table.
 * 
 */
@Entity
@Table(name="TSCHEDULER")
@NamedQuery(name="Tscheduler.findAll", query="SELECT t FROM Tscheduler t")
public class Tscheduler implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer tschedulerpk;
	private String jobclass;
	private Date lastupdated;
	private Integer repeatinterval;
	private String schedulerdesc;
	private String schedulergroup;
	private String schedulername;
	private String schedulerrepeattype;
	private String schedulerstatus;
	private String updatedby;

	public Tscheduler() {
	}

	@Id
	@Column(unique=true, nullable=false)
	public int getTschedulerpk() {
		return this.tschedulerpk;
	}

	public void setTschedulerpk(int tschedulerpk) {
		this.tschedulerpk = tschedulerpk;
	}
	
	@Type(type="com.sdd.utils.usertype.TrimUserType")
	public String getJobclass() {
		return jobclass;
	}


	public void setJobclass(String jobclass) {
		this.jobclass = jobclass;
	}


	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return this.lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}


	public int getRepeatinterval() {
		return this.repeatinterval;
	}

	public void setRepeatinterval(int repeatinterval) {
		this.repeatinterval = repeatinterval;
	}


	@Column(length=200)
	@Type(type="com.sdd.utils.usertype.TrimUserType")
	public String getSchedulerdesc() {
		return this.schedulerdesc;
	}

	public void setSchedulerdesc(String schedulerdesc) {
		this.schedulerdesc = schedulerdesc;
	}


	@Column(length=30)
	@Type(type="com.sdd.utils.usertype.TrimUserType")	
	public String getSchedulergroup() {
		return schedulergroup;
	}


	public void setSchedulergroup(String schedulergroup) {
		this.schedulergroup = schedulergroup;
	}


	@Column(length=30)
	@Type(type="com.sdd.utils.usertype.TrimUserType")
	public String getSchedulername() {
		return this.schedulername;
	}

	public void setSchedulername(String schedulername) {
		this.schedulername = schedulername;
	}


	@Type(type="com.sdd.utils.usertype.TrimUserType")
	public String getSchedulerrepeattype() {
		return schedulerrepeattype;
	}


	public void setSchedulerrepeattype(String schedulerrepeattype) {
		this.schedulerrepeattype = schedulerrepeattype;
	}


	@Column(length=3)
	@Type(type="com.sdd.utils.usertype.TrimUserType")
	public String getSchedulerstatus() {
		return this.schedulerstatus;
	}

	public void setSchedulerstatus(String schedulerstatus) {
		this.schedulerstatus = schedulerstatus;
	}


	@Column(length=30)
	public String getUpdatedby() {
		return this.updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}

}