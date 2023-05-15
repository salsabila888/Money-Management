package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Torderitem;
import com.sdd.caption.domain.Ttokenitem;
import com.sdd.caption.domain.Ttokenorderdata;
import com.sdd.caption.domain.Vtokenserial;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class TtokenitemDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Ttokenitem> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Ttokenitem> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Ttokenitem join Tincoming on tincomingfk = tincomingpk "
				+ "where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Ttokenitem.class).list();		

		session.close();
        return oList;
    }	
	
	@SuppressWarnings("unchecked")
	public List<Ttokenitem> listNativeByFilter(String filter, String orderby) throws Exception {
		List<Ttokenitem> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select * from Ttokenitem join Tincoming on tincomingfk = tincomingpk where " + filter + " order by " + orderby)
				.addEntity(Ttokenitem.class).list();
		session.close();
		return oList;
	}
	
	public Ttokenitem findById(String id) throws Exception {
		session = StoreHibernateUtil.openSession();
		Ttokenitem oForm = (Ttokenitem) session
				.createQuery("from Ttokenitem where (itemno = '" + id + "' or itemnoinject = '" + id + "')")
				.uniqueResult();
		session.close();
		return oForm;
	}
	
	public Ttokenitem findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Ttokenitem oForm = (Ttokenitem) session.createQuery("from Ttokenitem where " + filter).uniqueResult();
		session.close();
		return oForm;
	}
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Ttokenitem join Tincoming on tincomingfk = tincomingpk "
				+ "where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Ttokenitem> listByFilter(String filter, String orderby) throws Exception {		
    	List<Ttokenitem> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Ttokenitem where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Ttokenitem findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Ttokenitem oForm = (Ttokenitem) session.createQuery("from Ttokenitem where Ttokenitempk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}	
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Ttokenitem order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Ttokenitem oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Ttokenitem oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }
	
	@SuppressWarnings("unchecked")
	public List<Vtokenserial> countSerialStatus() throws Exception {
		List<Vtokenserial> oList = null;
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select count(*) as totaldata, "
				+ "count(case when status = '" + AppUtils.STATUS_SERIALNO_ENTRY + "' then 1 end) as outstanding, "
				+ "count(case when status = '" + AppUtils.STATUS_SERIALNO_INJECTED + "' then 1 end) as injected, "
				+ "count(case when status = '" + AppUtils.STATUS_SERIALNO_OUTPRODUKSI + "' then 1 end) as outproduksi from Ttokenitem")
				.addEntity(Vtokenserial.class).list();
		session.close();
		return oList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Torderitem> listSerialnoLetter(String filter) throws Exception {		
    	List<Torderitem> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Torderitem join ttokenitem on ttokenitemfk = ttokenitempk " + 
    			"join torder on Torderitem.torderfk = torderpk join tpaket on tpaket.torderfk = torderpk " + 
    			"join tpaketdata on tpaketfk = tpaketpk join tdeliverydata on tpaketdatafk = tpaketdatapk " + 
    			"join tdelivery on tdeliveryfk = tdeliverypk where " + filter)
				.addEntity(Torderitem.class).list();		

		session.close();
        return oList;
    }

}
