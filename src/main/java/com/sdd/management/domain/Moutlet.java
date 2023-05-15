package com.sdd.caption.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "moutlet")
@NamedQuery(name = "Moutlet.findAll", query = "SELECT m FROM Moutlet m")
public class Moutlet implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer moutletpk;
	private String outletcode;
	private String bicode;
	private String outletname;
	private String address;
	private String zipcode;
	private String outletcity;
	private String status;
	private Mbranch mbranch;
	
	public Moutlet() {
		
	}

	@Id
	@SequenceGenerator(name = "MOUTLET_MOUTLETPK_GENERATOR", sequenceName = "MOUTLET_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MOUTLET_MOUTLETPK_GENERATOR")
	@Column(unique = true, nullable = false)
	public Integer getMoutletpk() {
		return moutletpk;
	}

	public void setMoutletpk(Integer moutletpk) {
		this.moutletpk = moutletpk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getOutletcode() {
		return outletcode;
	}

	public void setOutletcode(String outletcode) {
		this.outletcode = outletcode;
	}

	public String getBicode() {
		return bicode;
	}

	public void setBicode(String bicode) {
		this.bicode = bicode;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getOutletname() {
		return outletname;
	}

	public void setOutletname(String outletname) {
		this.outletname = outletname;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@ManyToOne
	@JoinColumn(name = "mbranchfk")
	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getOutletcity() {
		return outletcity;
	}

	public void setOutletcity(String outletcity) {
		this.outletcity = outletcity;
	}

}
