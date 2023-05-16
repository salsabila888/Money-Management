package com.sdd.management.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "mproductgroup")
@NamedQuery(name = "Mproductgroup.findAll", query = "SELECT m FROM Mproductgroup m")
public class Mproductgroup implements Serializable  {
	private static final long serialVersionUID = 1L;
	private Integer mproductgrouppk;
	private String productgroupcode;
	private String productgroup;
	
	@Id
	@SequenceGenerator(name = "MPRODUCTGROUP_MPRODUCTGROUPPK_GENERATOR", sequenceName = "MPRODUCTGROUP_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MPRODUCTGROUP_MPRODUCTGROUPPK_GENERATOR")
	@Column(unique = true, nullable = false)
	public Integer getMproductgrouppk() {
		return mproductgrouppk;
	}
	public void setMproductgrouppk(Integer mproductgrouppk) {
		this.mproductgrouppk = mproductgrouppk;
	}
	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductgroupcode() {
		return productgroupcode;
	}
	public void setProductgroupcode(String productgroupcode) {
		this.productgroupcode = productgroupcode;
	}
	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductgroup() {
		return productgroup;
	}
	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}
}
