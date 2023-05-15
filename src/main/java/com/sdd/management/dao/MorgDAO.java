package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Morg;
import com.sdd.utils.db.StoreHibernateUtil;

public class MorgDAO {

	private Session session;

	@SuppressWarnings("unchecked")
	public List<Morg> listPaging(int first, int second, String filter, String orderby) throws Exception {
		List<Morg> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select * from Morg " + "where " + filter + " order by " + orderby + " limit "
				+ second + " offset " + first).addEntity(Morg.class).list();

		session.close();
		return oList;
	}

	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Morg " + "where " + filter)
				.uniqueResult().toString());
		session.close();
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Morg> listByFilter(String filter, String orderby) throws Exception {
		List<Morg> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Morg where " + filter + " order by " + orderby).list();
		session.close();
		return oList;
	}

	public Morg findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Morg oForm = (Morg) session.createQuery("from Morg where morgpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}

	public Morg findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Morg oForm = (Morg) session.createQuery("from Morg where " + filter).uniqueResult();
		session.close();
		return oForm;
	}

	public Morg findById(String id) throws Exception {
		session = StoreHibernateUtil.openSession();
		Morg oForm = (Morg) session.createQuery("from Morg where org = '" + id + "'").uniqueResult();
		session.close();
		return oForm;
	}

	public String getField(String code) throws Exception {
		session = StoreHibernateUtil.openSession();
		String data = (String) session.createQuery("select description from Morg where org = '" + code + "'")
				.uniqueResult();
		session.close();
		return data;
	}

	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("select " + fieldname + " from Morg order by " + fieldname).list();
		session.close();
		return oList;
	}

	public void save(Session session, Morg oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}

	public void delete(Session session, Morg oForm) throws HibernateException, Exception {
		session.delete(oForm);
	}

}
