package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Ttokenorderdata;
import com.sdd.utils.db.StoreHibernateUtil;

public class TtokenorderdataDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Ttokenorderdata> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Ttokenorderdata> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Ttokenorderdata "
				+ "where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Ttokenorderdata.class).list();		

		session.close();
        return oList;
    }	
	
	public Ttokenorderdata findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Ttokenorderdata oForm = (Ttokenorderdata) session.createQuery("from Ttokenorderdata where " + filter).uniqueResult();
		session.close();
		return oForm;
	}
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Ttokenorderdata "
				+ "where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Ttokenorderdata> listByFilter(String filter, String orderby) throws Exception {		
    	List<Ttokenorderdata> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Ttokenorderdata where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Ttokenorderdata findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Ttokenorderdata oForm = (Ttokenorderdata) session.createQuery("from Ttokenorderdata where ttokenorderdatapk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}	
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Ttokenorderdata order by " + fieldname).list();   
        session.close();
        return oList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Ttokenorderdata> listSerialnoLetter(String filter) throws Exception {		
    	List<Ttokenorderdata> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Ttokenorderdata join ttokenitem on ttokenitemfk = ttokenitempk " + 
    			"join torder on ttokenorderdata.torderfk = torderpk join tpaket on tpaket.torderfk = torderpk " + 
    			"join tpaketdata on tpaketfk = tpaketpk join tdeliverydata on tpaketdatafk = tpaketdatapk " + 
    			"join tdelivery on tdeliveryfk = tdeliverypk where " + filter)
				.addEntity(Ttokenorderdata.class).list();		

		session.close();
        return oList;
    }
		
	public void save(Session session, Ttokenorderdata oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Ttokenorderdata oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }

}
