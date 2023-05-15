package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Tnotif;
import com.sdd.caption.domain.Vnotif;
import com.sdd.utils.db.StoreHibernateUtil;

public class TnotifDAO {

	private Session session;

	@SuppressWarnings("unchecked")
	public List<Tnotif> listPaging(int first, int second, String filter, String orderby) throws Exception {
		List<Tnotif> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select * from Tnotif " + "where " + filter + " order by " + orderby + " limit "
				+ second + " offset " + first).addEntity(Tnotif.class).list();
		session.close();
		return oList;
	}

	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery("select count(*) from Tnotif " + "where " + filter)
				.uniqueResult().toString());
		session.close();
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Tnotif> listNativeByFilter(String filter, String orderby) throws Exception {
		List<Tnotif> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery(
				"select * from Tnotif join Mbranch on mbranchfk = mbranchpk where " + filter + " order by " + orderby)
				.addEntity(Tnotif.class).list();

		session.close();
		return oList;
	}

	@SuppressWarnings("unchecked")
	public List<Tnotif> listByFilter(String filter, String orderby) throws Exception {
		List<Tnotif> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Tnotif where " + filter + " order by " + orderby).list();
		session.close();
		return oList;
	}

	public Tnotif findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tnotif oForm = (Tnotif) session.createQuery("from Tnotif where tnotifpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}

	public Tnotif findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tnotif oForm = (Tnotif) session.createQuery("from Tnotif where " + filter).uniqueResult();
		session.close();
		return oForm;
	}

	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("select " + fieldname + " from Tnotif order by " + fieldname).list();
		session.close();
		return oList;
	}

	@SuppressWarnings("unchecked")
	public List<Vnotif> listNotif(String filter) throws Exception {
		List<Vnotif> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session
				.createSQLQuery("select musergrouppk, mmenupk, sum(notifcount) as totalnotif, notiftxt from Tnotif "
						+ "join Mmenu on mmenufk = mmenupk join Musergroupmenu on musergroupmenu.mmenufk = mmenu.mmenupk "
						+ "join Musergroup on musergroupfk = musergrouppk where " + filter
						+ " group by musergrouppk, mmenupk, notiftxt")
				.addEntity(Vnotif.class).list();
		session.close();
		return oList;
	}

	public void save(Session session, Tnotif oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}

	public void delete(Session session, Tnotif oForm) throws HibernateException, Exception {
		session.delete(oForm);
	}
}