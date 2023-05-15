package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Mnotif;
import com.sdd.utils.db.StoreHibernateUtil;

public class MnotifDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Mnotif> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Mnotif> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Mnotif "
				+ "where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Mnotif.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Mnotif "
				+ "where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Mnotif> listByFilter(String filter, String orderby) throws Exception {		
    	List<Mnotif> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Mnotif where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Mnotif findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mnotif oForm = (Mnotif) session.createQuery("from Mnotif where mnotifpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Mnotif order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Mnotif oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Mnotif oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }

}

