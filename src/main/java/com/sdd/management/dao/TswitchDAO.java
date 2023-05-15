package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Tswitch;
import com.sdd.caption.domain.Vsumbyproductgroup;
import com.sdd.utils.db.StoreHibernateUtil;

public class TswitchDAO {

	private Session session;

	@SuppressWarnings("unchecked")
	public List<Tswitch> listPaging(int first, int second, String filter, String orderby) throws Exception {
		List<Tswitch> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery(
				"select * from Tswitch join Mbranch on mbranchfk = mbranchpk join Mproduct on mproductfk = mproductpk "
						+ "where " + filter + " order by " + orderby + " limit " + second + " offset " + first)
				.addEntity(Tswitch.class).list();

		session.close();
		return oList;
	}

	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery(
				"select count(*) from Tswitch join Mbranch on mbranchfk = mbranchpk join Mproduct on mproductfk = mproductpk "
						+ "where " + filter)
				.uniqueResult().toString());
		session.close();
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Tswitch> listByFilter(String filter, String orderby) throws Exception {
		List<Tswitch> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Tswitch where " + filter + " order by " + orderby).list();
		session.close();
		return oList;
	}

	@SuppressWarnings("unchecked")
	public List<Tswitch> listNativeByFilter(String filter, String orderby) throws Exception {
		List<Tswitch> oList = null;
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery(
				"select * from Tswitch join Mbranch on mbranchfk = mbranchpk where " + filter + " order by " + orderby)
				.addEntity(Tswitch.class).list();
		session.close();
		return oList;
	}

	public Tswitch findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tswitch oForm = (Tswitch) session.createQuery("from Tswitch where tswitchpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}

	public Tswitch findById(String id) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tswitch oForm = (Tswitch) session.createQuery("from Tswitch where regid = '" + id + "'").uniqueResult();
		session.close();
		return oForm;
	}

	public Tswitch findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tswitch oForm = (Tswitch) session.createQuery("from Tswitch where " + filter).uniqueResult();
		session.close();
		return oForm;
	}

	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("select " + fieldname + " from Tswitch order by " + fieldname).list();
		session.close();
		return oList;
	}

	@SuppressWarnings("unchecked")
	public List<Vsumbyproductgroup> getSumdataByProductgroup(String filter) throws Exception {
		List<Vsumbyproductgroup> oList = null;
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery(
				"select productgroup, count(tswitchpk) as total from Tswitch join Mbranch on mbranchfk = mbranchpk join Mproduct on mproductfk = mproductpk where "
						+ filter + " group by productgroup")
				.addEntity(Vsumbyproductgroup.class).list();
		session.close();
		return oList;
	}

	public void save(Session session, Tswitch oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}

	public void delete(Session session, Tswitch oForm) throws HibernateException, Exception {
		session.delete(oForm);
	}

}
