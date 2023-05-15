package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


/**
 * The persistent class for the mproduct database table.
 * 
 */
@Entity
@Table(name = "mproduct")
@NamedQuery(name = "Mproduct.findAll", query = "SELECT m FROM Mproduct m")
public class Mproduct implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mproductpk;
	private Integer comboref;
	private String ismm;
	private String isinstant;
	private Date lastupdated;
	private String productcode;
	private String productgroup;
	private String productname;
	private String updatedby;
	private Mproducttype mproducttype;
	private String isdlvhome;
	private String isopr;
	
	public Mproduct() {
	}


	@Id
	@SequenceGenerator(name = "MPRODUCT_MPRODUCTPK_GENERATOR", sequenceName = "MPRODUCT_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MPRODUCT_MPRODUCTPK_GENERATOR")
	@Column(unique = true, nullable = false)
	public Integer getMproductpk() {
		return this.mproductpk;
	}

	public void setMproductpk(Integer mproductpk) {
		this.mproductpk = mproductpk;
	}

	
	public Integer getComboref() {
		return comboref;
	}


	public void setComboref(Integer comboref) {
		this.comboref = comboref;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsmm() {
		return ismm;
	}


	public void setIsmm(String ismm) {
		this.ismm = ismm;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsinstant() {
		return this.isinstant;
	}

	public void setIsinstant(String isinstant) {
		this.isinstant = isinstant;
	}


	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return this.lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductcode() {
		return this.productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductgroup() {
		return productgroup;
	}


	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductname() {
		return this.productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getUpdatedby() {
		return this.updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}


	//bi-directional many-to-one association to Mproducttype
	@ManyToOne
	@JoinColumn(name="mproducttypefk")
	public Mproducttype getMproducttype() {
		return this.mproducttype;
	}

	public void setMproducttype(Mproducttype mproducttype) {
		this.mproducttype = mproducttype;
	}


	public String getIsdlvhome() {
		return isdlvhome;
	}


	public void setIsdlvhome(String isdlvhome) {
		this.isdlvhome = isdlvhome;
	}


	public String getIsopr() {
		return isopr;
	}


	public void setIsopr(String isopr) {
		this.isopr = isopr;
	}

}