package com.sdd.caption.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.Type;

/**
 * The persistent class for the tnotif database table.
 * 
 */
@Entity
@Table(name = "tnotif")
@NamedQuery(name = "Tnotif.findAll", query = "SELECT t FROM Tnotif t")
public class Tnotif implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer tnotifpk;
	private Integer notifcount;
	private String notiftxt;
	private String productgroup;
	private Integer branchlevel;
	private Date notiftime;
	private Mmenu mmenu;
	private Mbranch mbranch;

	public Tnotif() {
	}

	@Id
	@SequenceGenerator(name = "TNOTIF_TNOTIFPK_GENERATOR", sequenceName = "TNOTIF_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TNOTIF_TNOTIFPK_GENERATOR")
	@Column(unique = true, nullable = false)
	public Integer getTnotifpk() {
		return this.tnotifpk;
	}

	public void setTnotifpk(Integer tnotifpk) {
		this.tnotifpk = tnotifpk;
	}

	public Integer getNotifcount() {
		return this.notifcount;
	}

	public void setNotifcount(Integer notifcount) {
		this.notifcount = notifcount;
	}

	@Column(length = 100)
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getNotiftxt() {
		return this.notiftxt;
	}

	public void setNotiftxt(String notiftxt) {
		this.notiftxt = notiftxt;
	}

	// bi-directional many-to-one association to Mmenu
	@ManyToOne
	@JoinColumn(name = "mmenufk")
	public Mmenu getMmenu() {
		return mmenu;
	}

	public void setMmenu(Mmenu mmenu) {
		this.mmenu = mmenu;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	@ManyToOne
	@JoinColumn(name = "mbranchfk")
	public Mbranch getMbranch() {
		return mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

	public Integer getBranchlevel() {
		return branchlevel;
	}

	public void setBranchlevel(Integer branchlevel) {
		this.branchlevel = branchlevel;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getNotiftime() {
		return notiftime;
	}

	public void setNotiftime(Date notiftime) {
		this.notiftime = notiftime;
	}

}