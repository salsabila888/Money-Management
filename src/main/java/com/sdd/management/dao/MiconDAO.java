package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Micon;
import com.sdd.utils.db.StoreHibernateUtil;

public class MiconDAO {
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Micon> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Micon> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Micon "
				+ "where " + filter + " order by " + orderby + " offset " + first +" rows fetch next " + second + " rows only")
				.addEntity(Micon.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Micon "
				+ "where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Micon> listByFilter(String filter, String orderby) throws Exception {		
    	List<Micon> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Micon where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Micon findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Micon oForm = (Micon) session.createQuery("from Micon where Miconpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Micon order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Micon oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Micon oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }
}