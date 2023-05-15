package com.sdd.management.domain;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;

/**
 * The persistent class for the muser database table.
 * 
 */
@Entity
@Table(name = "muser")
@NamedQuery(name = "Muser.findAll", query = "SELECT m FROM Muser m")
public class Muser implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer muserpk;
	private Musergroup musergroup;
	private String userid;
	private String username;
	private String password;
	private Date lastlogin;
	private Date lastupdated;
	private String updatedby;
	
	public Muser() {
	}

	@Id
	@SequenceGenerator(name = "MUSER_MUSERPK_GENERATOR", sequenceName = "MUSER_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MUSER_MUSERPK_GENERATOR")
	@Column(unique = true, nullable = false)
	public Integer getMuserpk() {
		return this.muserpk;
	}

	public void setMuserpk(Integer muserpk) {
		this.muserpk = muserpk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 15)
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 40)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	@Column(length = 70)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastlogin() {
		return lastlogin;
	}

	public void setLastlogin(Date lastlogin) {
		this.lastlogin = lastlogin;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 15)
	public String getUpdatedby() {
		return updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}

	@ManyToOne
	@JoinColumn(name = "musergroupfk")
	public Musergroup getMusergroup() {
		return musergroup;
	}

	public void setMusergroup(Musergroup musergroup) {
		this.musergroup = musergroup;
	}
}