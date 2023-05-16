package com.sdd.caption.domain;

import java.io.Serializable;

import javax.persistence.*;

/**
 * The persistent class for the musergroupmenu database table.
 * 
 */@Entity
 @Table(name = "musergroupmenu")
 @NamedQuery(name = "Musergroupmenu.findAll", query = "SELECT m FROM Musergroupmenu m")
 public class Musergroupmenu implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private Integer musergroupmenupk;
 	private Musergroup musergroup;
 	private Mmenu mmenu;
 

 	public Musergroupmenu() {
 	}

 	@Id
 	@SequenceGenerator(name = "MUSERGROUPMENU_MUSERGROUPMENUPK_GENERATOR", sequenceName = "MUSERGROUPMENU_SEQ", initialValue = 1, allocationSize = 1)
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MUSERGROUPMENU_MUSERGROUPMENUPK_GENERATOR")
 	@Column(unique = true, nullable = false)
 	public Integer getMusergroupmenupk() {
 		return this.musergroupmenupk;
 	}

 	public void setMusergroupmenupk(Integer musergroupmenupk) {
 		this.musergroupmenupk = musergroupmenupk;
 	}

	@ManyToOne
	@JoinColumn(name = "musergroupfk")
	public Musergroup getMusergroup() {
		return musergroup;
	}
	
	public void setMusergroup(Musergroup musergroup) {
		this.musergroup = musergroup;
	}

	@ManyToOne
	@JoinColumn(name = "mmenufk")
	public Mmenu getMmenu() {
		return mmenu;
	}

	public void setMmenu(Mmenu mmenu) {
		this.mmenu = mmenu;
	}
 		
 }