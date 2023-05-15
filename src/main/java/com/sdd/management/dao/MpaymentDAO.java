package com.sdd.management.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.management.domain.Mpayment;
import com.sdd.utils.db.StoreHibernateUtil;

public class MpaymentDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Mpayment> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Mpayment> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Mpayment where " + filter 
    			+ " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Mpayment.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Mpayment where " 
		+ filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Mpayment> listByFilter(String filter, String orderby) throws Exception {		
    	List<Mpayment> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Mpayment where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Mpayment findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mpayment oForm = (Mpayment) session.createQuery("from Mpayment where Mpaymentpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Mpayment order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Mpayment oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Mpayment oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }
}