package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


/**
 * The persistent class for the mholiday database table.
 * 
 */
@Entity
@Table(name="mholiday")
@NamedQuery(name="Mholiday.findAll", query="SELECT m FROM Mholiday m")
public class Mholiday implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mholidaypk;
	private String description;
	private Date holiday;
	private Date lastupdated;
	private String updatedby;

	public Mholiday() {
	}


	@Id
	@SequenceGenerator(name="MHOLIDAY_MHOLIDAYPK_GENERATOR", sequenceName = "MHOLIDAY_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MHOLIDAY_MHOLIDAYPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMholidaypk() {
		return this.mholidaypk;
	}

	public void setMholidaypk(Integer mholidaypk) {
		this.mholidaypk = mholidaypk;
	}


	@Column(length=70)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Temporal(TemporalType.DATE)
	public Date getHoliday() {
		return this.holiday;
	}

	public void setHoliday(Date holiday) {
		this.holiday = holiday;
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

}