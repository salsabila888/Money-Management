package com.sdd.caption.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Type;
import java.util.List;


/**
 * The persistent class for the mmenu database table.
 * 
 */
@Entity
@Table(name="mmenu")
@NamedQuery(name="Mmenu.findAll", query="SELECT m FROM Mmenu m")
public class Mmenu implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer mmenupk;
	private String menugroup;
	private String menuname;
	private Integer menuorderno;
	private String menuparamname;
	private String menuparamvalue;
	private String menupath;
	private String menusubgroup;
	private String menugroupicon;
	private String menusubgroupicon;
	private String menuicon;
	private List<Musergroupmenu> musergroupmenus;

	public Mmenu() {
	}


	@Id
	@SequenceGenerator(name="MMENU_MMENUPK_GENERATOR", sequenceName="MMENU_SEQ", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="MMENU_MMENUPK_GENERATOR")
	@Column(unique=true, nullable=false)
	public Integer getMmenupk() {
		return this.mmenupk;
	}

	public void setMmenupk(Integer mmenupk) {
		this.mmenupk = mmenupk;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	@Column(length=20)
	public String getMenugroup() {
		return this.menugroup;
	}

	public void setMenugroup(String menugroup) {
		this.menugroup = menugroup;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	@Column(length=40)
	public String getMenuname() {
		return this.menuname;
	}

	public void setMenuname(String menuname) {
		this.menuname = menuname;
	}


	public Integer getMenuorderno() {
		return this.menuorderno;
	}

	public void setMenuorderno(Integer menuorderno) {
		this.menuorderno = menuorderno;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getMenuparamname() {
		return this.menuparamname;
	}

	public void setMenuparamname(String menuparamname) {
		this.menuparamname = menuparamname;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getMenuparamvalue() {
		return this.menuparamvalue;
	}

	public void setMenuparamvalue(String menuparamvalue) {
		this.menuparamvalue = menuparamvalue;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	@Column(length=40)
	public String getMenupath() {
		return this.menupath;
	}

	public void setMenupath(String menupath) {
		this.menupath = menupath;
	}

	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	@Column(length=20)
	public String getMenusubgroup() {
		return this.menusubgroup;
	}

	public void setMenusubgroup(String menusubgroup) {
		this.menusubgroup = menusubgroup;
	}
	
	
	@Column(length = 50)
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getMenugroupicon() {
		return this.menugroupicon;
	}

	public void setMenugroupicon(String menugroupicon) {
		this.menugroupicon = menugroupicon;
	}

	@Column(length = 50)
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getMenuicon() {
		return this.menuicon;
	}

	public void setMenuicon(String menuicon) {
		this.menuicon = menuicon;
	}
	
	
	@Column(length = 50)
	@Type(type = "com.sdd.utils.usertype.TrimUserType")
	public String getMenusubgroupicon() {
		return this.menusubgroupicon;
	}

	public void setMenusubgroupicon(String menusubgroupicon) {
		this.menusubgroupicon = menusubgroupicon;
	}


	//bi-directional many-to-one association to Musergroupmenu
	@OneToMany(mappedBy="mmenu")
	public List<Musergroupmenu> getMusergroupmenus() {
		return this.musergroupmenus;
	}

	public void setMusergroupmenus(List<Musergroupmenu> musergroupmenus) {
		this.musergroupmenus = musergroupmenus;
	}

}