package com.sdd.management.domain;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;
import java.util.List;


/**
 * The persistent class for the musergroup database table.
 * 
 */@Entity
 @Table(name = "musergroup")
 @NamedQuery(name = "Musergroup.findAll", query = "SELECT m FROM Musergroup m")
 public class Musergroup implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private Integer musergrouppk;
 	private String usergroupcode;
 	private String usergroupname;
 	private String usergroupdesc;
 	private String status;
 	private Date lastupdated;
 	private String updatedby;
	private List<Muser> musers;
/*	private List<Musergroupmenu> musergroupmenus;
*/
 	public Musergroup() {
 	}

 	@Id
 	@SequenceGenerator(name = "MUSERGROUP_MUSERGROUPPK_GENERATOR", sequenceName = "MUSERGROUP_SEQ", initialValue = 1, allocationSize = 1)
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MUSERGROUP_MUSERGROUPPK_GENERATOR")
 	@Column(unique = true, nullable = false)
 	public Integer getMusergrouppk() {
 		return this.musergrouppk;
 	}

 	public void setMusergrouppk(Integer musergrouppk) {
 		this.musergrouppk = musergrouppk;
 	}
 	
 	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 20)
	public String getUsergroupcode() {
		return usergroupcode;
	}

	public void setUsergroupcode(String usergroupcode) {
		this.usergroupcode = usergroupcode;
	}
 	
 	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 40)
	public String getUsergroupname() {
		return usergroupname;
	}

	public void setUsergroupname(String usergroupname) {
		this.usergroupname = usergroupname;
	}

 	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 100)
	public String getUsergroupdesc() {
		return usergroupdesc;
	}

	public void setUsergroupdesc(String usergroupdesc) {
		this.usergroupdesc = usergroupdesc;
	}

 	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 1)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	@OneToMany(mappedBy="musergroup")
	public List<Muser> getMusers() {
		return this.musers;
	}

	public void setMusers(List<Muser> musers) {
		this.musers = musers;
	}
	
	/*@OneToMany(mappedBy="musergroup")
	public List<Musergroupmenu> getMusergroupmenus() {
		return this.musergroupmenus;
	}

	public void setMusergroupmenus(List<Musergroupmenu> musergroupmenus) {
		this.musergroupmenus = musergroupmenus;
	}*/
	
 }