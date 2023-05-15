package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


/**
 * The persistent class for the mletter database table.
 * 
 */
@Entity
@Table(name="mletter")
@NamedQuery(name="Mletter.findAll", query="SELECT m FROM Mletter m")
public class Mletter implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mletterpk;
	private String description;
	private String letterprefix;
	private String productgroup;
	private Date lastupdated;
	private String updatedby;

	public Mletter() {
	}


	@Id
	@SequenceGenerator(name="MLETTER_MLETTERPK_GENERATOR", sequenceName = "MLETTER_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MLETTER_MLETTERPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMletterpk() {
		return this.mletterpk;
	}

	public void setMletterpk(Integer mletterpk) {
		this.mletterpk = mletterpk;
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
	public String getLetterprefix() {
		return letterprefix;
	}


	public void setLetterprefix(String letterprefix) {
		this.letterprefix = letterprefix;
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