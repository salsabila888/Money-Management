package com.sdd.management.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sdd.management.dao.MaimDAO;
import com.sdd.management.dao.MbankDAO;
import com.sdd.management.dao.MexpensesDAO;
import com.sdd.management.dao.MincomeDAO;
import com.sdd.management.dao.MmenuDAO;
import com.sdd.management.dao.MpaymentDAO;
import com.sdd.management.dao.MproductDAO;
import com.sdd.management.dao.MproductgroupDAO;
import com.sdd.management.dao.MproducttypeDAO;
import com.sdd.management.dao.MuserDAO;
import com.sdd.management.dao.MusergroupDAO;
import com.sdd.management.domain.Maim;
import com.sdd.management.domain.Mbank;
import com.sdd.management.domain.Mexpenses;
import com.sdd.management.domain.Mincome;
import com.sdd.management.domain.Mmenu;
import com.sdd.management.domain.Mpayment;
import com.sdd.management.domain.Mproduct;
import com.sdd.management.domain.Mproductgroup;
import com.sdd.management.domain.Mproducttype;
import com.sdd.management.domain.Muser;
import com.sdd.management.domain.Musergroup;

public class AppData {
	
	public static List<Musergroup> getMusergroup() throws Exception {
		List<Musergroup> list = new ArrayList<Musergroup>();
		list = new MusergroupDAO().listByFilter("0=0", "usergroupname");
		return list;
	}

	public static List<Muser> getMuser() throws Exception {
		List<Muser> list = new ArrayList<Muser>();
		list = new MuserDAO().listByFilter("0=0", "userid");
		return list;
	}
	
	public static List<Muser> getMuser(String filter) throws Exception {
		List<Muser> list = new ArrayList<Muser>();
		list = new MuserDAO().listByFilter(filter, "userid");
		return list;
	}

	public static List<Mmenu> getMmenu(String filter) throws Exception {
		List<Mmenu> list = new ArrayList<Mmenu>();
		list = new MmenuDAO().listByFilter(filter, "menuname");
		return list;
	}

	public static List<Mproducttype> getMproducttype(String filter) throws Exception {
		List<Mproducttype> list = new ArrayList<Mproducttype>();
		list = new MproducttypeDAO().listByFilter(filter, "productorg, producttype");
		return list;
	}

	public static List<Mproducttype> getMproducttype() throws Exception {
		List<Mproducttype> list = new ArrayList<Mproducttype>();
		list = new MproducttypeDAO().listByFilter("0=0", "productorg, producttype");
		return list;
	}

	public static List<Mproduct> getMproduct() throws Exception {
		List<Mproduct> list = new ArrayList<Mproduct>();
		list = new MproductDAO().listByFilter("0=0",
				"mproducttype.productgroupcode, mproducttype.productorg, productname");
		return list;
	}

	public static List<Mproduct> getMproduct(String filter) throws Exception {
		List<Mproduct> list = new ArrayList<Mproduct>();
		list = new MproductDAO().listByFilter(filter,
				"mproducttype.productgroupcode, mproducttype.productorg, productname");
		return list;
	}

	public static List<Mproduct> getMproduct(String filter, String orderby) throws Exception {
		List<Mproduct> list = new ArrayList<Mproduct>();
		list = new MproductDAO().listByFilter(filter, orderby);
		return list;
	}
	
	public static List<Mincome> getMincome() throws Exception {
		List<Mincome> list = new ArrayList<Mincome>();
		list = new MincomeDAO().listByFilter("0=0", "incomesource");
		return list;
	}
	
	public static List<Mexpenses> getMexpenses() throws Exception {
		List<Mexpenses> list = new ArrayList<Mexpenses>();
		list = new MexpensesDAO().listByFilter("0=0", "expenses");
		return list;
	}
	
	public static List<Mpayment> getMpayment() throws Exception {
		List<Mpayment> list = new ArrayList<Mpayment>();
		list = new MpaymentDAO().listByFilter("0=0", "paymenttype");
		return list;
	}
	
	public static List<Mbank> getMbank() throws Exception {
		List<Mbank> list = new ArrayList<Mbank>();
		list = new MbankDAO().listByFilter("0=0", "bankname");
		return list;
	}
	
	public static List<Maim> getMaim() throws Exception {
		List<Maim> list = new ArrayList<Maim>();
		list = new MaimDAO().listByFilter("0=0", "aim");
		return list;
	
	}

	public static Map<String, Mproductgroup> getMproductgroup() {
		Map<String, Mproductgroup> map = new HashMap<String, Mproductgroup>();
		try {
			for (Mproductgroup obj : new MproductgroupDAO().listByFilter("0=0", "productgroupcode")) {
				map.put(obj.getProductgroupcode(), obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
}
