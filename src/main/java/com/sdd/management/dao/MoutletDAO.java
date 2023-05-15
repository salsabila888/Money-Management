package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Moutlet;
import com.sdd.utils.db.StoreHibernateUtil;

public class MoutletDAO {

	private Session session;

	@SuppressWarnings("unchecked")
	public List<Moutlet> listPaging(int first, int second, String filter, String orderby) throws Exception {
		List<Moutlet> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select * from Moutlet join mbranch on mbranchfk = mbranchpk where " + filter + " order by " + orderby + " limit "
				+ second + " offset " + first).addEntity(Moutlet.class).list();

		session.close();
		return oList;
	}

	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Moutlet join mbranch on mbranchfk = mbranchpk where " + filter)
				.uniqueResult().toString());
		session.close();
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Moutlet> listByFilter(String filter, String orderby) throws Exception {
		List<Moutlet> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Moutlet where " + filter + " order by " + orderby).list();
		session.close();
		return oList;
	}

	public Moutlet findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Moutlet oForm = (Moutlet) session.createQuery("from Moutlet where Moutletpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}

	public Moutlet findByCode(String code) throws Exception {
		session = StoreHibernateUtil.openSession();
		Moutlet oForm = (Moutlet) session.createQuery("from Moutlet where outletcode = '" + code + "'").uniqueResult();
		session.close();
		return oForm;
	}

	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("select " + fieldname + " from Moutlet order by " + fieldname).list();
		session.close();
		return oList;
	}

	public Moutlet findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Moutlet oForm = (Moutlet) session.createQuery("from Moutlet where " + filter).uniqueResult();
		session.close();
		return oForm;
	}

	public void save(Session session, Moutlet oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}

	public void delete(Session session, Moutlet oForm) throws HibernateException, Exception {
		session.delete(oForm);
	}
}
