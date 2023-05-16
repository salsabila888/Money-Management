package com.sdd.management.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Type;
import java.util.Date;


/**
 * The persistent class for the mproducttype database table.
 * 
 */
@Entity
@Table(name = "mproducttype")
@NamedQuery(name = "Mproducttype.findAll", query = "SELECT m FROM Mproducttype m")
public class Mproducttype implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mproducttypepk;
	private Date alertstockpagudate;
	private Date alertstockpagurelease;
	private Date blockpagutime;
	private String doctype;
	private Date estdate;
	private Integer estdays;
	private String isalertstockpagu;
	private String isblockpagu;
	private Integer laststock;
	private Date lastupdated;
	private String productgroupcode;
	private String productgroupname;
	private String productorg;
	private String producttype;
	private String productunit;
	private Integer productunitqty;
	private String slacountertype;
	private Integer stockinjected;
	private Integer stockmin;
	private Integer stockreserved;
	private Integer stockunused;
	private Integer velocity;
	private String unblockpaguby;
	private Date unblockpagutime;	
	private String updatedby;
	private String isestcount;
	private String coano;
	private String grouptype;
	private String booktype;

	public Mproducttype() {
	}


	@Id
	@SequenceGenerator(name = "MPRODUCTTYPE_MPRODUCTTYPEPK_GENERATOR", sequenceName = "MPRODUCTTYPE_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MPRODUCTTYPE_MPRODUCTTYPEPK_GENERATOR")
	@Column(unique = true, nullable = false)
	public Integer getMproducttypepk() {
		return this.mproducttypepk;
	}

	public void setMproducttypepk(Integer mproducttypepk) {
		this.mproducttypepk = mproducttypepk;
	}

	
	public Date getAlertstockpagudate() {
		return alertstockpagudate;
	}


	public void setAlertstockpagudate(Date alertstockpagudate) {
		this.alertstockpagudate = alertstockpagudate;
	}

	
	public Date getAlertstockpagurelease() {
		return alertstockpagurelease;
	}


	public void setAlertstockpagurelease(Date alertstockpagurelease) {
		this.alertstockpagurelease = alertstockpagurelease;
	}

	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getBlockpagutime() {
		return blockpagutime;
	}


	public void setBlockpagutime(Date blockpagutime) {
		this.blockpagutime = blockpagutime;
	}

	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getDoctype() {
		return doctype;
	}


	public void setDoctype(String doctype) {
		this.doctype = doctype;
	}


	public Date getEstdate() {
		return estdate;
	}


	public void setEstdate(Date estdate) {
		this.estdate = estdate;
	}


	public Integer getEstdays() {
		return estdays;
	}


	public void setEstdays(Integer estdays) {
		this.estdays = estdays;
	}

	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsalertstockpagu() {
		return isalertstockpagu;
	}


	public void setIsalertstockpagu(String isalertstockpagu) {
		this.isalertstockpagu = isalertstockpagu;
	}

	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsblockpagu() {
		return isblockpagu;
	}


	public void setIsblockpagu(String isblockpagu) {
		this.isblockpagu = isblockpagu;
	}


	public Integer getLaststock() {
		return this.laststock;
	}

	public void setLaststock(Integer laststock) {
		this.laststock = laststock;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastupdated() {
		return this.lastupdated;
	}

	public void setLastupdated(Date lastupdated) {
		this.lastupdated = lastupdated;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductgroupcode() {
		return this.productgroupcode;
	}

	public void setProductgroupcode(String productgroupcode) {
		this.productgroupcode = productgroupcode;
	}
	
	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductgroupname() {
		return productgroupname;
	}


	public void setProductgroupname(String productgroupname) {
		this.productgroupname = productgroupname;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductorg() {
		return productorg;
	}


	public void setProductorg(String productorg) {
		this.productorg = productorg;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProducttype() {
		return this.producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getProductunit() {
		return productunit;
	}


	public void setProductunit(String productunit) {
		this.productunit = productunit;
	}


	public Integer getProductunitqty() {
		return productunitqty;
	}


	public void setProductunitqty(Integer productunitqty) {
		this.productunitqty = productunitqty;
	}

	
	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getSlacountertype() {
		return slacountertype;
	}


	public void setSlacountertype(String slacountertype) {
		this.slacountertype = slacountertype;
	}


	public Integer getStockinjected() {
		return stockinjected;
	}


	public void setStockinjected(Integer stockinjected) {
		this.stockinjected = stockinjected;
	}


	public Integer getStockmin() {
		return this.stockmin;
	}

	public void setStockmin(Integer stockmin) {
		this.stockmin = stockmin;
	}

	
	public Integer getStockreserved() {
		return stockreserved;
	}


	public void setStockreserved(Integer stockreserved) {
		this.stockreserved = stockreserved;
	}

	
	public Integer getVelocity() {
		return velocity;
	}


	public void setVelocity(Integer velocity) {
		this.velocity = velocity;
	}

	
	public String getUnblockpaguby() {
		return unblockpaguby;
	}


	public void setUnblockpaguby(String unblockpaguby) {
		this.unblockpaguby = unblockpaguby;
	}


	@Temporal(TemporalType.TIMESTAMP)
	public Date getUnblockpagutime() {
		return unblockpagutime;
	}


	public void setUnblockpagutime(Date unblockpagutime) {
		this.unblockpagutime = unblockpagutime;
	}


	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getUpdatedby() {
		return this.updatedby;
	}

	public void setUpdatedby(String updatedby) {
		this.updatedby = updatedby;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getIsestcount() {
		return isestcount;
	}


	public void setIsestcount(String isestcount) {
		this.isestcount = isestcount;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getCoano() {
		return coano;
	}


	public void setCoano(String coano) {
		this.coano = coano;
	}


	public Integer getStockunused() {
		return stockunused;
	}


	public void setStockunused(Integer stockunused) {
		this.stockunused = stockunused;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getGrouptype() {
		return grouptype;
	}


	public void setGrouptype(String grouptype) {
		this.grouptype = grouptype;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUpperCaseUserType")
	public String getBooktype() {
		return booktype;
	}


	public void setBooktype(String booktype) {
		this.booktype = booktype;
	}

}