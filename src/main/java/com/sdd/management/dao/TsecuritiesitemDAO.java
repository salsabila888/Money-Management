package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.caption.domain.Tsecuritiesitem;
import com.sdd.utils.db.StoreHibernateUtil;

public class TsecuritiesitemDAO {

	private Session session;

	@SuppressWarnings("unchecked")
	public List<Tsecuritiesitem> listPaging(int first, int second, String filter, String orderby) throws Exception {
		List<Tsecuritiesitem> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session
				.createSQLQuery("select * from Tsecuritiesitem join Tincoming on tincomingfk = tincomingpk " + "where "
						+ filter + " order by " + orderby + " limit " + second + " offset " + first)
				.addEntity(Tsecuritiesitem.class).list();

		session.close();
		return oList;
	}

	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery(
				"select count(*) from Tsecuritiesitem join Tincoming on tincomingfk = tincomingpk where " + filter)
				.uniqueResult().toString());
		session.close();
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Tsecuritiesitem> listByFilter(String filter, String orderby) throws Exception {
		List<Tsecuritiesitem> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Tsecuritiesitem where " + filter + " order by " + orderby).list();
		session.close();
		return oList;
	}

	public Tsecuritiesitem findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tsecuritiesitem oForm = (Tsecuritiesitem) session
				.createQuery("from Tsecuritiesitem where Tsecuritiesitempk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}

	public Tsecuritiesitem findById(String id) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tsecuritiesitem oForm = (Tsecuritiesitem) session
				.createQuery("from Tsecuritiesitem where (itemno = '" + id + "' or itemnoinject = '" + id + "')")
				.uniqueResult();
		session.close();
		return oForm;
	}

	public Tsecuritiesitem findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Tsecuritiesitem oForm = (Tsecuritiesitem) session.createQuery("from Tsecuritiesitem where " + filter)
				.uniqueResult();
		session.close();
		return oForm;
	}

	public String getField(String code) throws Exception {
		session = StoreHibernateUtil.openSession();
		String data = (String) session.createQuery("select description from Tsecuritiesitem where org = '" + code + "'")
				.uniqueResult();
		session.close();
		return data;
	}

	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("select " + fieldname + " from Tsecuritiesitem order by " + fieldname).list();
		session.close();
		return oList;
	}

	@SuppressWarnings("unchecked")
	public List<Tsecuritiesitem> listNativeByFilter(String filter, String orderby) throws Exception {
		List<Tsecuritiesitem> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session
				.createSQLQuery("select * from Tsecuritiesitem join Tincoming on tincomingfk = tincomingpk where "
						+ filter + " order by " + orderby)
				.addEntity(Tsecuritiesitem.class).list();
		session.close();
		return oList;
	}

	public void save(Session session, Tsecuritiesitem oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}

	public void delete(Session session, Tsecuritiesitem oForm) throws HibernateException, Exception {
		session.delete(oForm);
	}

}
