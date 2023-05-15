package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the mcouriervendorbranch database table.
 * 
 */
@Entity
@Table(name="mcouriervendorbranch")
@NamedQuery(name="Mcouriervendorbranch.findAll", query="SELECT m FROM Mcouriervendorbranch m")
public class Mcouriervendorbranch implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mcouriervendorbranchpk;
	private Mcouriervendor mcouriervendor;
	private Mbranch mbranch;

	public Mcouriervendorbranch() {
	}


	@Id
	@SequenceGenerator(name="MCOURIERVENDORBRANCH_MCOURIERVENDORBRANCHPK_GENERATOR", sequenceName = "MCOURIERVENDORBRANCH_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MCOURIERVENDORBRANCH_MCOURIERVENDORBRANCHPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMcouriervendorbranchpk() {
		return this.mcouriervendorbranchpk;
	}

	public void setMcouriervendorbranchpk(Integer mcouriervendorbranchpk) {
		this.mcouriervendorbranchpk = mcouriervendorbranchpk;
	}


	//bi-directional many-to-one association to Mcouriervendor
	@ManyToOne
	@JoinColumn(name="mcouriervendorfk", nullable=false)
	public Mcouriervendor getMcouriervendor() {
		return this.mcouriervendor;
	}

	public void setMcouriervendor(Mcouriervendor mcouriervendor) {
		this.mcouriervendor = mcouriervendor;
	}


	//bi-directional many-to-one association to Mbranch
	@ManyToOne
	@JoinColumn(name="mbranchfk", nullable=false)
	public Mbranch getMbranch() {
		return this.mbranch;
	}

	public void setMbranch(Mbranch mbranch) {
		this.mbranch = mbranch;
	}

}