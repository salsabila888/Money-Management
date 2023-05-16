package com.sdd.management.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.management.domain.Mproductgroup;
import com.sdd.utils.db.StoreHibernateUtil;

public class MproductgroupDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Mproductgroup> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Mproductgroup> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Mproductgroup "
				+ "where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Mproductgroup.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Mproductgroup "
				+ "where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Mproductgroup> listByFilter(String filter, String orderby) throws Exception {		
    	List<Mproductgroup> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Mproductgroup where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Mproductgroup findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mproductgroup oForm = (Mproductgroup) session.createQuery("from Mproductgroup where Mproductgrouppk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	public Mproductgroup findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mproductgroup oForm = (Mproductgroup) session.createQuery("from Mproductgroup where " + filter).uniqueResult();
		session.close();
		return oForm;
	}
	
	public String getField(String code) throws Exception {
		session = StoreHibernateUtil.openSession();
		String data = (String) session.createQuery("select description from Mproductgroup where org = '" + code + "'").uniqueResult();
		session.close();
		return data;
	}
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Mproductgroup order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Mproductgroup oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Mproductgroup oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }

}

