package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Tuneod;
import com.sdd.utils.db.StoreHibernateUtil;

public class TuneodDAO {

	private Session session;
	
	@SuppressWarnings("unchecked")
	public List<Tuneod> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Tuneod> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Tuneod "
				+ "where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Tuneod.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Tuneod "
				+ "where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Tuneod> listByFilter(String filter, String orderby) throws Exception {		
    	List<Tuneod> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Tuneod where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Tuneod findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tuneod oForm = (Tuneod) session.createQuery("from Tuneod where tuneodpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	public Tuneod findByDate(String date) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tuneod oForm = (Tuneod) session.createQuery("from Tuneod where eoddate = '" + date + "'").uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("unchecked")
	public List<Tuneod>list() throws Exception {
       	List<Tuneod> oList = new ArrayList<Tuneod>();
       	session = StoreHibernateUtil.openSession();
        oList = session.createQuery("from Tuneod order by tuneodpk").list();                 
        session.close();        
        return oList;
	} 
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Tuneod order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Tuneod oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Tuneod oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }

}
