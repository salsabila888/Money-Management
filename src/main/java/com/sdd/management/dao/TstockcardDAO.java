package com.sdd.caption.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Tstockcard;
import com.sdd.caption.domain.Vuneod;
import com.sdd.utils.db.StoreHibernateUtil;

public class TstockcardDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Tstockcard> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Tstockcard> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Tstockcard join Mproducttype on mproducttypefk = mproducttypepk "
				+ "where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Tstockcard.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Tstockcard join Mproducttype on mproducttypefk = mproducttypepk "
				+ "where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Tstockcard> listByFilter(String filter, String orderby) throws Exception {		
    	List<Tstockcard> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Tstockcard where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	@SuppressWarnings("unchecked")
	public List<Tstockcard> listNative(String filter, String orderby) throws Exception {
    	List<Tstockcard> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 != 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select * from Tstockcard where " + filter + " order by " + orderby).addEntity(Tstockcard.class).list();
		session.close();
        return oList;
    }
	
	public Tstockcard findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tstockcard oForm = (Tstockcard) session.createQuery("from Tstockcard where tstockcardpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	public Tstockcard findById(String id) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tstockcard oForm = (Tstockcard) session.createQuery("from Tstockcard where stockcardid = '" + id + "'").uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("unchecked")
	public List<Vuneod> listUneod(String filter, String orderby) throws Exception {
    	List<Vuneod> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 != 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select  distinct(trxdate) from tstockcard where " + filter + " order by " + orderby).addEntity(Vuneod.class).list();
		session.close();
        return oList;
    }
		
	public void save(Session session, Tstockcard oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Tstockcard oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }
	
	public void deleteBySQL(Session session, String filter) throws HibernateException, Exception {
		session.createSQLQuery("delete from Tstockcard where " + filter).executeUpdate();    
    }
	
}
