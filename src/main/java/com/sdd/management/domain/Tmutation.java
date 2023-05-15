package com.sdd.caption.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;


@Entity
@Table(name="tmutation")
@NamedQuery(name="Tmutation.findAll", query="SELECT t FROM Tmutation t")
public class Tmutation implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer tmutationpk;
	private Mincome mincome;
	private Mexpenses mexpenses;
	private Maim maim;
	private Mpayment mpayment;
	private Mbank mbank;
	private Date mutationdate;
	private String mutationno;
	private BigDecimal mutationamount;
	private Date createdtime;
	private String createdby;
	private Date lastupdated;
	private String updatedby;

	public Tmutation() {
	}

	@Id
	@SequenceGenerator(name="TMUTATION_MUTATIONPK_GENERATOR", sequenceName = "TMUTATION_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="TMUTATION_TMUTATIONPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getTmutationpk() {
		return tmutationpk;
	}

	public void setTmutationpk(Integer tmutationpk) {
		this.tmutationpk = tmutationpk;
	}

	@ManyToOne
	@JoinColumn(name = "mincomefk")
	public Mincome getMincome() {
		return mincome;
	}

	public void setMincome(Mincome mincome) {
		this.mincome = mincome;
	}

	@ManyToOne
	@JoinColumn(name = "mexpensesfk")
	public Mexpenses getMexpenses() {
		return mexpenses;
	}

	public void setMexpenses(Mexpenses mexpenses) {
		this.mexpenses = mexpenses;
	}

	@ManyToOne
	@JoinColumn(name = "maimfk")
	public Maim getMaim() {
		return maim;
	}

	public void setMaim(Maim maim) {
		this.maim = maim;
	}

	@ManyToOne
	@JoinColumn(name = "mpaymentfk")
	public Mpayment getMpayment() {
		return mpayment;
	}

	public void setMpayment(Mpayment mpayment) {
		this.mpayment = mpayment;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getMutationno() {
		return mutationno;
	}

	public void setMutationno(String mutationno) {
		this.mutationno = mutationno;
	}

	public BigDecimal getMutationamount() {
		return mutationamount;
	}

	public void setMutationamount(BigDecimal mutationamount) {
		this.mutationamount = mutationamount;
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

	@ManyToOne
	@JoinColumn(name = "mbankfk")
	public Mbank getMbank() {
		return mbank;
	}

	public void setMbank(Mbank mbank) {
		this.mbank = mbank;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getMutationdate() {
		return mutationdate;
	}

	public void setMutationdate(Date mutationdate) {
		this.mutationdate = mutationdate;
	}

	
}