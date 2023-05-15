package com.sdd.management.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.management.domain.Tscheduler;
import com.sdd.utils.db.StoreHibernateUtil;

public class TschedulerDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Tscheduler> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Tscheduler> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Tscheduler "
				+ "where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Tscheduler.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Tscheduler "
				+ "where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Tscheduler> listByFilter(String filter, String orderby) throws Exception {		
    	List<Tscheduler> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Tscheduler where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Tscheduler findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tscheduler oForm = (Tscheduler) session.createQuery("from Tscheduler where tschedulerpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	public Tscheduler findById(String id) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tscheduler oForm = (Tscheduler) session.createQuery("from Tscheduler where schedulerid = '" + id + "'").uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("unchecked")
	public List<Tscheduler>list() throws Exception {
       	List<Tscheduler> oList = new ArrayList<Tscheduler>();
       	session = StoreHibernateUtil.openSession();
        oList = session.createQuery("from Tscheduler order by tschedulerpk").list();                 
        session.close();        
        return oList;
	} 
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Tscheduler order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Tscheduler oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Tscheduler oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }

}
