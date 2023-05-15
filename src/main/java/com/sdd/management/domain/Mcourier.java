package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


/**
 * The persistent class for the mcourier database table.
 * 
 */
@Entity
@Table(name="mcourier")
@NamedQuery(name="Mcourier.findAll", query="SELECT m FROM Mcourier m")
public class Mcourier implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mcourierpk;
	private String couriercode;	
	private String courieremail;
	private String courierimg;
	private String couriername;
	private String courierphone;
	private String npp;
	private Date lastupdated;
	private String updatedby;
	private Mcouriervendor mcouriervendor;
	public Mcourier() {
	}


	@Id
	@SequenceGenerator(name="MCOURIER_MCOURIERPK_GENERATOR", sequenceName = "MCOURIER_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MCOURIER_MCOURIERPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMcourierpk() {
		return this.mcourierpk;
	}

	public void setMcourierpk(Integer mcourierpk) {
		this.mcourierpk = mcourierpk;
	}


	@Column(length=10)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getCouriercode() {
		return this.couriercode;
	}

	public void setCouriercode(String couriercode) {
		this.couriercode = couriercode;
	}


	@Column(length=100)
	@Type(type = "com.sdd.utils.usertype.TrimLowerCaseUserType")
	public String getCourieremail() {
		return this.courieremail;
	}

	public void setCourieremail(String courieremail) {
		this.courieremail = courieremail;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getCourierimg() {
		return courierimg;
	}


	public void setCourierimg(String courierimg) {
		this.courierimg = courierimg;
	}


	@Column(length=70)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getCouriername() {
		return this.couriername;
	}

	public void setCouriername(String couriername) {
		this.couriername = couriername;
	}


	@Column(length=20)
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getCourierphone() {
		return this.courierphone;
	}

	public void setCourierphone(String courierphone) {
		this.courierphone = courierphone;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getNpp() {
		return npp;
	}


	public void setNpp(String npp) {
		this.npp = npp;
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


	//bi-directional many-to-one association to Mcouriervendor
	@ManyToOne
	@JoinColumn(name="mcouriervendorfk")
	public Mcouriervendor getMcouriervendor() {
		return this.mcouriervendor;
	}

	public void setMcouriervendor(Mcouriervendor mcouriervendor) {
		this.mcouriervendor = mcouriervendor;
	}

}