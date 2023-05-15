package com.sdd.management.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.management.domain.Mbank;
import com.sdd.utils.db.StoreHibernateUtil;

public class MbankDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Mbank> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Mbank> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Mbank where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Mbank.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Mbank where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Mbank> listByFilter(String filter, String orderby) throws Exception {		
    	List<Mbank> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Mbank where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Mbank findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mbank oForm = (Mbank) session.createQuery("from Mbank where Mbankpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Mbank order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Mbank oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Mbank oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }
}