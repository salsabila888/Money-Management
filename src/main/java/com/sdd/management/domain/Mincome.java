package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


@Entity
@Table(name="mincome")
@NamedQuery(name="Mincome.findAll", query="SELECT m FROM Mincome m")
public class Mincome implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mincomepk;
	private String incomesource;
	private Date createdtime;
	private String createdby;
	private Date lastupdated;
	private String updatedby;

	public Mincome() {
	}

	@Id
	@SequenceGenerator(name="MINCOME_MINCOMEPK_GENERATOR", sequenceName = "MINCOME_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MINCOME_MINCOMEPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMincomepk() {
		return mincomepk;
	}

	public void setMincomepk(Integer mincomepk) {
		this.mincomepk = mincomepk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIncomesource() {
		return incomesource;
	}

	public void setIncomesource(String incomesource) {
		this.incomesource = incomesource;
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