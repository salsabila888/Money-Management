package com.sdd.caption.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.Type;

@Entity
public class Mkolomreport implements Serializable {
	private static final long serialVersionUID = 1L;
//	====================
//	private String pmemono;
//	private Mbranch pmbranchfk;
//	private Mproducttype improducttypefk;
//	private Date pmemodate;
//	private Integer punitqty;
//	private Integer ptotalprocess;
//	private String pstatus;
//	private String pinputer;
//	private Date pinputime;
//	private String ispkno;
//	private Date ispkdate;
//	private String iprefix;
//	private Integer iitemstartno;
//	private BigDecimal iharga;
//	private Integer iitemqty;
//	private Msupplier imsupplierfk;
//	private String istatus;
//	private String imemo;
//	private String ientryby;
//	private Date ientrytime;
//	private String omemono;
//	private Date omemodate;
//	private Mbranch ombranchfk;
//	private String oorderoutlet;
//	private Date oorderdate;
//	private Integer ototalproses;
//	private String ostatus;
//	private String oinsertedby;
//	private Date oinserttime;
//
//	public String getPmemono() {
//		return pmemono;
//	}
//
//	public void setPmemono(String pmemono) {
//		this.pmemono = pmemono;
//	}
//
//	public Mbranch getPmbranchfk() {
//		return pmbranchfk;
//	}
//
//	public void setPmbranchfk(Mbranch pmbranchfk) {
//		this.pmbranchfk = pmbranchfk;
//	}
//
//	public Mproducttype getImproducttypefk() {
//		return improducttypefk;
//	}
//
//	public void setImproducttypefk(Mproducttype improducttypefk) {
//		this.improducttypefk = improducttypefk;
//	}
//
//	public Date getPmemodate() {
//		return pmemodate;
//	}
//
//	public void setPmemodate(Date pmemodate) {
//		this.pmemodate = pmemodate;
//	}
//
//	public Integer getPunitqty() {
//		return punitqty;
//	}
//
//	public void setPunitqty(Integer punitqty) {
//		this.punitqty = punitqty;
//	}
//
//	public Integer getPtotalprocess() {
//		return ptotalprocess;
//	}
//
//	public void setPtotalprocess(Integer ptotalprocess) {
//		this.ptotalprocess = ptotalprocess;
//	}
//
//	public String getPstatus() {
//		return pstatus;
//	}
//
//	public void setPstatus(String pstatus) {
//		this.pstatus = pstatus;
//	}
//
//	public String getPinputer() {
//		return pinputer;
//	}
//
//	public void setPinputer(String pinputer) {
//		this.pinputer = pinputer;
//	}
//
//	public Date getPinputime() {
//		return pinputime;
//	}
//
//	public void setPinputime(Date pinputime) {
//		this.pinputime = pinputime;
//	}
//
//	public String getIspkno() {
//		return ispkno;
//	}
//
//	public void setIspkno(String ispkno) {
//		this.ispkno = ispkno;
//	}
//
//	public Date getIspkdate() {
//		return ispkdate;
//	}
//
//	public void setIspkdate(Date ispkdate) {
//		this.ispkdate = ispkdate;
//	}
//
//	public String getIprefix() {
//		return iprefix;
//	}
//
//	public void setIprefix(String iprefix) {
//		this.iprefix = iprefix;
//	}
//
//	public Integer getIitemstartno() {
//		return iitemstartno;
//	}
//
//	public void setIitemstartno(Integer iitemstartno) {
//		this.iitemstartno = iitemstartno;
//	}
//
//	public BigDecimal getIharga() {
//		return iharga;
//	}
//
//	public void setIharga(BigDecimal iharga) {
//		this.iharga = iharga;
//	}
//
//	public Integer getIitemqty() {
//		return iitemqty;
//	}
//
//	public void setIitemqty(Integer iitemqty) {
//		this.iitemqty = iitemqty;
//	}
//
//	public Msupplier getImsupplierfk() {
//		return imsupplierfk;
//	}
//
//	public void setImsupplierfk(Msupplier imsupplierfk) {
//		this.imsupplierfk = imsupplierfk;
//	}
//
//	public String getIstatus() {
//		return istatus;
//	}
//
//	public void setIstatus(String istatus) {
//		this.istatus = istatus;
//	}
//
//	public String getImemo() {
//		return imemo;
//	}
//
//	public void setImemo(String imemo) {
//		this.imemo = imemo;
//	}
//
//	public String getIentryby() {
//		return ientryby;
//	}
//
//	public void setIentryby(String ientryby) {
//		this.ientryby = ientryby;
//	}
//
//	public Date getIentrytime() {
//		return ientrytime;
//	}
//
//	public void setIentrytime(Date ientrytime) {
//		this.ientrytime = ientrytime;
//	}
//
//	public String getOmemono() {
//		return omemono;
//	}
//
//	public void setOmemono(String omemono) {
//		this.omemono = omemono;
//	}
//
//	public Date getOmemodate() {
//		return omemodate;
//	}
//
//	public void setOmemodate(Date omemodate) {
//		this.omemodate = omemodate;
//	}
//
//	public Mbranch getOmbranchfk() {
//		return ombranchfk;
//	}
//
//	public void setOmbranchfk(Mbranch ombranchfk) {
//		this.ombranchfk = ombranchfk;
//	}
//
//	public String getOorderoutlet() {
//		return oorderoutlet;
//	}
//
//	public void setOorderoutlet(String oorderoutlet) {
//		this.oorderoutlet = oorderoutlet;
//	}
//
//	public Date getOorderdate() {
//		return oorderdate;
//	}
//
//	public void setOorderdate(Date oorderdate) {
//		this.oorderdate = oorderdate;
//	}
//
//	public Integer getOtotalproses() {
//		return ototalproses;
//	}
//
//	public void setOtotalproses(Integer ototalproses) {
//		this.ototalproses = ototalproses;
//	}
//
//	public String getOstatus() {
//		return ostatus;
//	}
//
//	public void setOstatus(String ostatus) {
//		this.ostatus = ostatus;
//	}
//
//	public String getOinsertedby() {
//		return oinsertedby;
//	}
//
//	public void setOinsertedby(String oinsertedby) {
//		this.oinsertedby = oinsertedby;
//	}
//
//	public Date getOinserttime() {
//		return oinserttime;
//	}
//
//	public void setOinserttime(Date oinserttime) {
//		this.oinserttime = oinserttime;
//	}

//	====================

//	
//	
	private String planno;
	private Date inputtime;
	private String productgroup;
	private BigDecimal anggaran;
	private Integer totalqty;
	private String totalprocess;
	private String memono;
	private Date pdecisiontime;
	private String pstatus;

	private String incomingid;
	private Mproducttype mproducttypefk;
	private Date ientrytime;
	private String ientryby;
	private String prefix;
	private Integer itemstartno;
	private Integer itemqty;
	private String istatus;
	private Msupplier msupplierfk;
	private String spkno;
	private Date spkdate;

	private String orderid;
	private Date orderdate;
	private String orderoutlet;
	private Integer oitemqty;
	private Date inserttime;
	private Integer totalproses;
	private String oordertype;
	private String ostatus;

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 20)
	public String getPlanno() {
		return planno;
	}

	public void setPlanno(String planno) {
		this.planno = planno;
	}

	@Id
	@Temporal(TemporalType.TIMESTAMP)
	public Date getInputtime() {
		return inputtime;
	}

	public void setInputtime(Date inputtime) {
		this.inputtime = inputtime;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

	@Id
	public BigDecimal getAnggaran() {
		return anggaran;
	}

	public void setAnggaran(BigDecimal anggaran) {
		this.anggaran = anggaran;
	}

	@Id
	public Integer getTotalqty() {
		return totalqty;
	}

	public void setTotalqty(Integer totalqty) {
		this.totalqty = totalqty;
	}

	@Id
	public String getTotalprocess() {
		return totalprocess;
	}

	public void setTotalprocess(String totalprocess) {
		this.totalprocess = totalprocess;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 20)
	public String getMemono() {
		return memono;
	}

	public void setMemono(String memono) {
		this.memono = memono;
	}

	@Id
	@Temporal(TemporalType.TIMESTAMP)
	public Date getPdecisiontime() {
		return pdecisiontime;
	}

	public void setPdecisiontime(Date pdecisiontime) {
		this.pdecisiontime = pdecisiontime;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 2)
	public String getPstatus() {
		return pstatus;
	}

	public void setPstatus(String pstatus) {
		this.pstatus = pstatus;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIncomingid() {
		return incomingid;
	}

	public void setIncomingid(String incomingid) {
		this.incomingid = incomingid;
	}

	@Id
	@ManyToOne
	@JoinColumn(name = "mproducttypefk")
	public Mproducttype getMproducttypefk() {
		return mproducttypefk;
	}

	public void setMproducttypefk(Mproducttype mproducttypefk) {
		this.mproducttypefk = mproducttypefk;
	}

	@Id
	@Temporal(TemporalType.TIMESTAMP)
	public Date getIentrytime() {
		return ientrytime;
	}

	public void setIentrytime(Date ientrytime) {
		this.ientrytime = ientrytime;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIentryby() {
		return ientryby;
	}

	public void setIentryby(String ientryby) {
		this.ientryby = ientryby;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Id
	public Integer getItemstartno() {
		return itemstartno;
	}

	public void setItemstartno(Integer itemstartno) {
		this.itemstartno = itemstartno;
	}

	@Id
	public Integer getItemqty() {
		return itemqty;
	}

	public void setItemqty(Integer itemqty) {
		this.itemqty = itemqty;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 2)
	public String getIstatus() {
		return istatus;
	}

	public void setIstatus(String istatus) {
		this.istatus = istatus;
	}

	@Id
	@ManyToOne
	@JoinColumn(name = "msupplierfk")
	public Msupplier getMsupplierfk() {
		return msupplierfk;
	}

	public void setMsupplierfk(Msupplier msupplierfk) {
		this.msupplierfk = msupplierfk;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getSpkno() {
		return spkno;
	}

	public void setSpkno(String spkno) {
		this.spkno = spkno;
	}

	@Id
	@Temporal(TemporalType.TIMESTAMP)
	public Date getSpkdate() {
		return spkdate;
	}

	public void setSpkdate(Date spkdate) {
		this.spkdate = spkdate;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	@Id
	@Temporal(TemporalType.TIMESTAMP)
	public Date getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(Date orderdate) {
		this.orderdate = orderdate;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getOrderoutlet() {
		return orderoutlet;
	}

	public void setOrderoutlet(String orderoutlet) {
		this.orderoutlet = orderoutlet;
	}

	@Id
	public Integer getOitemqty() {
		return oitemqty;
	}

	public void setOitemqty(Integer oitemqty) {
		this.oitemqty = oitemqty;
	}

	@Id
	@Temporal(TemporalType.TIMESTAMP)
	public Date getInserttime() {
		return inserttime;
	}

	public void setInserttime(Date inserttime) {
		this.inserttime = inserttime;
	}

	@Id
	public Integer getTotalproses() {
		return totalproses;
	}

	public void setTotalproses(Integer totalproses) {
		this.totalproses = totalproses;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getOordertype() {
		return oordertype;
	}

	public void setOordertype(String oordertype) {
		this.oordertype = oordertype;
	}

	@Id
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	@Column(length = 2)
	public String getOstatus() {
		return ostatus;
	}

	public void setOstatus(String ostatus) {
		this.ostatus = ostatus;
	}

////	private Tplan tplan;
////	private Tincoming tincoming;
////	private Torder torder;
////
////	@Id
////	@ManyToOne
////	@JoinColumn(name = "tplanfk")
////	public Tplan getTplan() {
////		return tplan;
////	}
////
////	public void setTplan(Tplan tplan) {
////		this.tplan = tplan;
////	}
////
////	@Id
////	@ManyToOne
////	@JoinColumn(name = "mproducttypefk")
////	public Tincoming getTincoming() {
////		return tincoming;
////	}
////
////	public void setTincoming(Tincoming tincoming) {
////		this.tincoming = tincoming;
////	}
////
////	@Id
////	@ManyToOne
////	@JoinColumn(name = "mproductfk")
////	public Torder getTorder() {
////		return torder;
////	}
////
////	public void setTorder(Torder torder) {
////		this.torder = torder;
////	}

}