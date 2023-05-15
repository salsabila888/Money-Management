package com.sdd.management.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.management.domain.Tmutationdoc;
import com.sdd.utils.db.StoreHibernateUtil;

public class TmutationdocDAO {

	private Session session;

	@SuppressWarnings("unchecked")
	public List<Tmutationdoc> listPaging(int first, int second, String filter, String orderby) throws Exception {
		List<Tmutationdoc> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select * from Tmutationdoc where " + filter + " order by " + orderby + " limit "
				+ second + " offset " + first).addEntity(Tmutationdoc.class).list();

		session.close();
		return oList;
	}

	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Tmutationdoc where " + filter)
				.uniqueResult().toString());
		session.close();
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Tmutationdoc> listByFilter(String filter, String orderby) throws Exception {
		List<Tmutationdoc> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Tmutationdoc where " + filter + " order by " + orderby).list();
		session.close();
		return oList;
	}

	public Tmutationdoc findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tmutationdoc oForm = (Tmutationdoc) session.createQuery("from Tmutationdoc where Tmutationdocpk = " + pk)
				.uniqueResult();
		session.close();
		return oForm;
	}

	public Tmutationdoc findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tmutationdoc oForm = (Tmutationdoc) session.createQuery("from Tmutationdoc where " + filter).uniqueResult();
		session.close();
		return oForm;
	}

	public void save(Session session, Tmutationdoc oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}

	public void delete(Session session, Tmutationdoc oForm) throws HibernateException, Exception {
		session.delete(oForm);
	}

}
