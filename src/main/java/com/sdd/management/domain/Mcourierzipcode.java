package com.sdd.caption.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

/**
 * The persistent class for the MCOURIERZIPCODE database table.
 * 
 */
@Entity
@Table(name = "MCOURIERZIPCODE")
@NamedQuery(name = "Mcourierzipcode.findAll", query = "SELECT m FROM Mcourierzipcode m")
public class Mcourierzipcode {
	private Integer mcourierzipcodepk;
	private String description;
	private Date lastupdated;
	private String updatedby;
	private Integer zipcodeend;
	private Integer zipcodestart;
	private Mcouriervendor mcouriervendor;

	public Mcourierzipcode() {
	}

	@Id
	@SequenceGenerator(name="MCOURIERZIPCODE_MCOURIERZIPCODEPK_GENERATOR", sequenceName = "MCOURIERZIPCODE_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MCOURIERZIPCODE_MCOURIERZIPCODEPK_GENERATOR")
	@Column(unique=true, nullable=false)	
	public Integer getMcourierzipcodepk() {
		return this.mcourierzipcodepk;
	}

	public void setMcourierzipcodepk(Integer mcourierzipcodepk) {
		this.mcourierzipcodepk = mcourierzipcodepk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public Integer getZipcodeend() {
		return this.zipcodeend;
	}

	public void setZipcodeend(Integer zipcodeend) {
		this.zipcodeend = zipcodeend;
	}

	public Integer getZipcodestart() {
		return this.zipcodestart;
	}

	public void setZipcodestart(Integer zipcodestart) {
		this.zipcodestart = zipcodestart;
	}

	// bi-directional many-to-one association to Mcourier
	@ManyToOne
	@JoinColumn(name = "MCOURIERVENDORFK")
	public Mcouriervendor getMcouriervendor() {
		return mcouriervendor;
	}

	public void setMcouriervendor(Mcouriervendor mcouriervendor) {
		this.mcouriervendor = mcouriervendor;
	}

}
