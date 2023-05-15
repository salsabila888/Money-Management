package com.sdd.caption.domain;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "tmutationdoc")
@NamedQuery(name = "Tmutationdoc.findAll", query = "SELECT t FROM Tmutationdoc t")
public class Tmutationdoc {
	private static final long serialVersionUID = 1L;
	private Integer tmutationdocpk;
	private String docfileori;
	private String docfileid;
	private Integer docfilesize;
	private Date doctime;
	private Tmutation tmutation;
	
	public Tmutationdoc() {
		
	}
	
	@Id
	@SequenceGenerator(name = "TMUTATIONDOC_TMUTATIONDOCPK_GENERATOR", sequenceName = "TMUTATIONDOC_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TMUTATIONDOC_TMUTATIONDOCPK_GENERATOR")
	@Column(unique = true, nullable = false)
	public Integer getTmutationdocpk() {
		return tmutationdocpk;
	}

	public void setTmutationdocpk(Integer tmutationdocpk) {
		this.tmutationdocpk = tmutationdocpk;
	}
	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 100)
	public String getDocfileori() {
		return docfileori;
	}
	public void setDocfileori(String docfileori) {
		this.docfileori = docfileori;
	}
	
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	@Column(length = 100)
	public String getDocfileid() {
		return docfileid;
	}
	public void setDocfileid(String docfileid) {
		this.docfileid = docfileid;
	}
	public Integer getDocfilesize() {
		return docfilesize;
	}
	public void setDocfilesize(Integer docfilesize) {
		this.docfilesize = docfilesize;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDoctime() {
		return doctime;
	}
	public void setDoctime(Date doctime) {
		this.doctime = doctime;
	}
	
	@ManyToOne
	@JoinColumn(name="tmutationfk")
	public Tmutation getTmutation() {
		return tmutation;
	}
	public void setTmutation(Tmutation tmutation) {
		this.tmutation = tmutation;
	}
}
