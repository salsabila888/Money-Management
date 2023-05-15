package com.sdd.management.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;


/**
 * The persistent class for the MICON database table.
 * 
 */
@Entity
@Table(name="MICON")
@NamedQuery(name="Micon.findAll", query="SELECT m FROM Micon m")
public class Micon implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer miconpk;
	private String iconname;
	private String iconpath;
	private String url;

	public Micon() {
	}


	@Id
	@SequenceGenerator(name="MICON_MICONPK_GENERATOR", sequenceName="MICON_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MICON_MICONPK_GENERATOR")
	@Column(unique = true, nullable = false)
	public Integer getMiconpk() {
		return this.miconpk;
	}

	public void setMiconpk(Integer miconpk) {
		this.miconpk = miconpk;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length=50)
	public String getIconname() {
		return this.iconname;
	}

	public void setIconname(String iconname) {
		this.iconname = iconname;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	@Column(length=50)
	public String getIconpath() {
		return this.iconpath;
	}

	public void setIconpath(String iconpath) {
		this.iconpath = iconpath;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	@Column(length=300)
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}