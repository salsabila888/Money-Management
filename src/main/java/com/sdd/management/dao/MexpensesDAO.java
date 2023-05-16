package com.sdd.management.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.management.domain.Mexpenses;
import com.sdd.utils.db.StoreHibernateUtil;

public class MexpensesDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Mexpenses> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Mexpenses> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Mexpenses where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Mexpenses.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Mexpenses where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Mexpenses> listByFilter(String filter, String orderby) throws Exception {		
    	List<Mexpenses> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Mexpenses where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Mexpenses findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mexpenses oForm = (Mexpenses) session.createQuery("from Mexpenses where Mexpensespk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Mexpenses order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Mexpenses oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Mexpenses oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }
}