package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Msysparam;
import com.sdd.utils.db.StoreHibernateUtil;

public class MsysparamDAO {
	
	Session session;
	
	@SuppressWarnings("unchecked")
	public List<Msysparam> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Msysparam> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select * from Msysparam where " + filter + " order by " + orderby + " limit " + first +", " + second)
				.addEntity(Msysparam.class).list();
		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createQuery("select count(*) from Msysparam where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Msysparam> listByFilter(String filter, String orderby) throws Exception {		
    	List<Msysparam> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Msysparam where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	public Msysparam findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Msysparam oForm = (Msysparam) session.createQuery("from Msysparam where msysparampk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	public Msysparam findById(String id) throws Exception {
		session = StoreHibernateUtil.openSession();
		Msysparam oForm = (Msysparam) session.createQuery("from Msysparam where paramcode = '" + id + "'").uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Msysparam order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Msysparam oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Msysparam oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }

	public void scriptExecute(String sql) throws HibernateException, Exception {
		session = StoreHibernateUtil.openSession();
		session.createSQLQuery(sql).executeUpdate();
		session.close();
    }
	
}

