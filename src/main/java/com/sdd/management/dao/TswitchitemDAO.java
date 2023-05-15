package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Treturn;
import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.domain.Tswitchitem;
import com.sdd.utils.db.StoreHibernateUtil;

public class TswitchitemDAO {
	
	private Session session;

	@SuppressWarnings("unchecked")
	public List<Tswitchitem> listPaging(int first, int second, String filter, String orderby) throws Exception {		
    	List<Tswitchitem> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
    	oList = session.createSQLQuery("select * from Tswitchitem "
				+ "where " + filter + " order by " + orderby + " limit " + second +" offset " + first)
				.addEntity(Tswitchitem.class).list();		

		session.close();
        return oList;
    }	
	
	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Tswitchitem "
				+ "where " + filter).uniqueResult().toString());
		session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Tswitchitem> listByFilter(String filter, String orderby) throws Exception {		
    	List<Tswitchitem> oList = null;
    	if (filter == null || "".equals(filter))
			filter = "0 = 0";
    	session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Tswitchitem where " + filter + " order by " + orderby).list();
		session.close();
        return oList;
    }	
	
	@SuppressWarnings("unchecked")
	public List<Tswitchitem> listNativeByFilter(String filter, String orderby) throws Exception {		
    	List<Tswitchitem> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select Tswitchitem.* from Tswitchitem where " + filter + " order by " + orderby).addEntity(Tswitchitem.class).list();
		session.close();
        return oList;
    }	
	
	public Tswitchitem findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tswitchitem oForm = (Tswitchitem) session.createQuery("from Tswitchitem where tswitchitempk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}
	
	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
       	session = StoreHibernateUtil.openSession();
       	oList = session.createQuery("select " + fieldname + " from Tswitchitem order by " + fieldname).list();   
        session.close();
        return oList;
	}
		
	public void save(Session session, Tswitchitem oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}
	
	public void delete(Session session, Tswitchitem oForm) throws HibernateException, Exception {
		session.delete(oForm);    
    }

}
