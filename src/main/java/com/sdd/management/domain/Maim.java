package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


@Entity
@Table(name="maim")
@NamedQuery(name="Maim.findAll", query="SELECT m FROM Maim m")
public class Maim implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer maimpk;
	private String aim;
	private Date createdtime;
	private String createdby;
	private Date lastupdated;
	private String updatedby;

	public Maim() {
	}

	@Id
	@SequenceGenerator(name="MAIM_MAIMPK_GENERATOR", sequenceName = "MAIM_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MAIM_MAIMPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMaimpk() {
		return maimpk;
	}

	public void setMaimpk(Integer maimpk) {
		this.maimpk = maimpk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getAim() {
		return aim;
	}

	public void setAim(String aim) {
		this.aim = aim;
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