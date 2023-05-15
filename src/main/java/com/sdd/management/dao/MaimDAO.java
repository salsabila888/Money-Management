package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Maim;
import com.sdd.caption.domain.Muser;
import com.sdd.utils.db.StoreHibernateUtil;

public class MaimDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Maim> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Maim> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Maim where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Maim.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Maim where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Maim> listByFilter(String filter, String orderby) throws Exception {		
    	List<Maim> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Maim where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Maim findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Maim oForm = (Maim) session.createQuery("from Maim where Maimpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	public Maim findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Maim oForm = (Maim) session.createQuery("from Maim where " + filter).uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Maim order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Maim oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Maim oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }
}