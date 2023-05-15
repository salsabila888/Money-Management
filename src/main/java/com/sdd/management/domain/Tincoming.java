package com.sdd.caption.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import java.util.Date;

/**
 * The persistent class for the tincoming database table.
 * 
 */
@Entity
@Table(name = "tincoming")
@NamedQuery(name = "Tincoming.findAll", query = "SELECT t FROM Tincoming t")
public class Tincoming implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer tincomingpk;
	private String productgroup;
	private String incomingid;
	private String filename;
	private Integer itemqty;
	private BigDecimal harga;
	private String memo;
	private String status;
	private Date entrytime;
	private String entryby;
	private String decisionby;
	private Date decisiontime;
	private String decisionmemo;
	private Date lastupdated;
	private String updatedby;
	private Integer itemstartno;
	private String spkno;
	private Date spkdate;
	private Integer manufacturedate;
	private String prefix;
	private String vendorletterno;
	private Date vendorletterdate;
	private String pksno;
	private Date pksdate;
	private Msupplier msupplier;
	private Mproducttype mproducttype;
	private Mbranch mbranch;
	private Tplan tplanfk;

	public Tincoming() {
	}

	@Id
	@SequenceGenerator(name = "TINCOMING_TINCOMINGPK_GENERATOR", sequenceName = "TINCOMING_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TINCOMING_TINCOMINGPK_GENERATOR")
	public Integer getTincomingpk() {
		return this.tincomingpk;
	}

	public void setTincomingpk(Integer tincomingpk) {
		this.tincomingpk = tincomingpk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getDecisionby() {
		return this.decisionby;
	}

	public void setDecisionby(String decisionby) {
		this.decisionby = decisionby;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getDecisionmemo() {
		return this.decisionmemo;
	}

	public void setDecisionmemo(String decisionmemo) {
		this.decisionmemo = decisionmemo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getDecisiontime() {
		return this.decisiontime;
	}

	public void setDecisiontime(Date decisiontime) {
		this.decisiontime = decisiontime;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getEntryby() {
		return this.entryby;
	}

	public void setEntryby(String entryby) {
		this.entryby = entryby;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getEntrytime() {
		return this.entrytime;
	}

	public void setEntrytime(Date entrytime) {
		this.entrytime = entrytime;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIncomingid() {
		return this.incomingid;
	}

	public void setIncomingid(String incomingid) {
		this.incomingid = incomingid;
	}

	public Integer getItemqty() {
		return this.itemqty;
	}

	public void setItemqty(Integer itemqty) {
		this.itemqty = itemqty;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return this.lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getMemo() {
		return this.memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductgroup() {
		return this.productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUpdatedby() {
		return this.updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}

	// bi-directional many-to-one association to Torder
	@ManyToOne
	@JoinColumn(name = "mproducttypefk")
	public Mproducttype getMproducttype() {
		return mproducttype;
	}

	public void setMproducttype(Mproducttype mproducttype) {
		this.mproducttype = mproducttype;
	}

	@ManyToOne
	@JoinColumn(name = "msupplierfk")
	public Msupplier getMsupplier() {
		return msupplier;
	}

	public void setMsupplier(Msupplier msupplier) {
		this.msupplier = msupplier;
	}

	public Integer getItemstartno() {
		return itemstartno;
	}

	public void setItemstartno(Integer itemstartno) {
		this.itemstartno = itemstartno;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getSpkno() {
		return spkno;
	}

	public void setSpkno(String spkno) {
		this.spkno = spkno;
	}

	@Temporal(TemporalType.DATE)
	public Date getSpkdate() {
		return spkdate;
	}

	public void setSpkdate(Date spkdate) {
		this.spkdate = spkdate;
	}

	public BigDecimal getHarga() {
		return harga;
	}

	public void setHarga(BigDecimal harga) {
		this.harga = harga;
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
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@ManyToOne
	@JoinColumn(name = "tplanfk")
	public Tplan getTplanfk() {
		return tplanfk;
	}

	public void setTplanfk(Tplan tplanfk) {
		this.tplanfk = tplanfk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getVendorletterno() {
		return vendorletterno;
	}

	public void setVendorletterno(String vendorletterno) {
		this.vendorletterno = vendorletterno;
	}

	@Temporal(TemporalType.DATE)
	public Date getVendorletterdate() {
		return vendorletterdate;
	}

	public void setVendorletterdate(Date vendorletterdate) {
		this.vendorletterdate = vendorletterdate;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getPksno() {
		return pksno;
	}

	public void setPksno(String pksno) {
		this.pksno = pksno;
	}

	@Temporal(TemporalType.DATE)
	public Date getPksdate() {
		return pksdate;
	}

	public void setPksdate(Date pksdate) {
		this.pksdate = pksdate;
	}

	public Integer getManufacturedate() {
		return manufacturedate;
	}

	public void setManufacturedate(Integer manufacturedate) {
		this.manufacturedate = manufacturedate;
	}

}