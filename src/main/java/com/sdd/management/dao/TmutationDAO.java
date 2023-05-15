package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Tmutation;
import com.sdd.utils.db.StoreHibernateUtil;

public class TmutationDAO {

	private Session session;

	@SuppressWarnings("unchecked")
	public List<Tmutation> listPaging(int first, int second, String filter, String orderby) throws Exception {
		List<Tmutation> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select * from Tmutation where " + filter + " order by " + orderby + " limit "
				+ second + " offset " + first).addEntity(Tmutation.class).list();

		session.close();
		return oList;
	}

	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Tmutation where " + filter)
				.uniqueResult().toString());
		session.close();
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Tmutation> listByFilter(String filter, String orderby) throws Exception {
		List<Tmutation> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Tmutation where " + filter + " order by " + orderby).list();
		session.close();
		return oList;
	}

	public Tmutation findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tmutation oForm = (Tmutation) session.createQuery("from Tmutation where TmutationPK = " + pk).uniqueResult();
		session.close();
		return oForm;
	}

	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("select " + fieldname + " from Mexpenses order by " + fieldname).list();
		session.close();
		return oList;
	}

	public void save(Session session, Tmutation oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}

	public void delete(Session session, Tmutation oForm) throws HibernateException, Exception {
		session.delete(oForm);
	}
}