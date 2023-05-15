package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


/**
 * The persistent class for the morg database table.
 * 
 */
@Entity
@Table(name="morg")
@NamedQuery(name="Morg.findAll", query="SELECT m FROM Morg m")
public class Morg implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer morgpk;
	private String description;
	private Date lastupdated;
	private Integer oprcapacity;
	private String isneeddoc;
	private String org;
	private String updatedby;

	public Morg() {
	}


	@Id
	@SequenceGenerator(name="MORG_MORGPK_GENERATOR", sequenceName = "MORG_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MORG_MORGPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMorgpk() {
		return this.morgpk;
	}

	public void setMorgpk(Integer morgpk) {
		this.morgpk = morgpk;
	}


	@Column(length=40)
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

	
	public Integer getOprcapacity() {
		return oprcapacity;
	}


	public void setOprcapacity(Integer oprcapacity) {
		this.oprcapacity = oprcapacity;
	}


	@Column(length=3)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getOrg() {
		return this.org;
	}

	public void setOrg(String org) {
		this.org = org;
	}


	@Column(length=15)
	public String getUpdatedby() {
		return this.updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}


	@Column(length=40)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsneeddoc() {
		return isneeddoc;
	}


	public void setIsneeddoc(String isneeddoc) {
		this.isneeddoc = isneeddoc;
	}

}