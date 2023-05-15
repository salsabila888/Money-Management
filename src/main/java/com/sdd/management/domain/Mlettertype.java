package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


/**
 * The persistent class for the mlettertype database table.
 * 
 */
@Entity
@Table(name="mlettertype")
@NamedQuery(name="Mlettertype.findAll", query="SELECT m FROM Mlettertype m")
public class Mlettertype implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mlettertypepk;
	private String description;
	private String lettertype;
	private String productgroup;
	private Date lastupdated;
	private String updatedby;

	public Mlettertype() {
	}


	@Id
	@SequenceGenerator(name="MLETTERTYPE_MLETTERTYPEPK_GENERATOR", sequenceName = "MLETTERTYPE_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MLETTERTYPE_MLETTERTYPEPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMlettertypepk() {
		return this.mlettertypepk;
	}

	public void setMlettertypepk(Integer mlettertypepk) {
		this.mlettertypepk = mlettertypepk;
	}


	@Column(length=70)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getLettertype() {
		return lettertype;
	}


	public void setLettertype(String lettertype) {
		this.lettertype = lettertype;
	}

	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductgroup() {
		return productgroup;
	}


	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
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