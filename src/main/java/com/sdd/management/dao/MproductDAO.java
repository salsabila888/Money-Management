package com.sdd.management.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.management.domain.Mproduct;
import com.sdd.utils.db.StoreHibernateUtil;

public class MproductDAO {

	private Session session;

	@SuppressWarnings("unchecked")
	public List<Mproduct> listPaging(int first, int second, String filter, String orderby) throws Exception {
		List<Mproduct> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session
				.createSQLQuery("select Mproduct.* from Mproduct join Mproducttype on mproducttypefk = mproducttypepk "
						+ "where " + filter + " order by " + orderby + " limit " + second + " offset " + first)
				.addEntity(Mproduct.class).list();

		session.close();
		return oList;
	}

	@SuppressWarnings("unchecked")
	public List<Mproduct> listProduct(String filter, String orderby) throws Exception {
		List<Mproduct> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session
				.createSQLQuery("select Mproduct.* from Mproduct join Mproducttype on mproducttypefk = mproducttypepk "
						+ "where " + filter + " order by " + orderby)
				.addEntity(Mproduct.class).list();

		session.close();
		return oList;
	}

	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session
				.createSQLQuery("select count(*) from Mproduct join Mproducttype on mproducttypefk = mproducttypepk "
						+ "where " + filter)
				.uniqueResult().toString());
		session.close();
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Mproduct> listByFilter(String filter, String orderby) throws Exception {
		List<Mproduct> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Mproduct where " + filter + " order by " + orderby).list();
		session.close();
		return oList;
	}

	@SuppressWarnings("unchecked")
	public List<Mproduct> listByFilterconcat(String filter, String orderby) throws Exception {
		List<Mproduct> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery(
				"select productgroupcode || ' - ' ||productname as productgroupcode, productgroupcode, productname, qtymin, picname, picemail, productcode, mproductpk, mproductjenisfk  from Mproduct")
				.addEntity(Mproduct.class).list();
		session.close();
		return oList;
	}

	public Mproduct findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mproduct oForm = (Mproduct) session.createQuery("from Mproduct where mproductpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}

	public Mproduct findById(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mproduct oForm = (Mproduct) session.createQuery("from Mproduct where mproductpk = " + pk).uniqueResult();
		session.close();
		return oForm;
	}

	public Mproduct findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mproduct oForm = (Mproduct) session.createQuery("from Mproduct where " + filter).uniqueResult();
		session.close();
		return oForm;
	}

	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("select " + fieldname + " from Mproduct order by " + fieldname).list();
		session.close();
		return oList;
	}

	@SuppressWarnings("rawtypes")
	public List getProductOnOrder(String filter) throws Exception {
		List oList = new ArrayList();
		session = StoreHibernateUtil.openSession();
		oList = session
				.createQuery("select distinct productcode from Torderdata where " + filter + " order by productcode")
				.list();
		session.close();
		return oList;
	}

	@SuppressWarnings("unchecked")
	public List<Mproduct> listNativeByFilter(String filter, String orderby) throws Exception {
		List<Mproduct> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select Mproduct.* from Mproduct where " + filter + " order by " + orderby)
				.addEntity(Mproduct.class).list();

		session.close();
		return oList;
	}

	/*
	 * @SuppressWarnings("unchecked") public List<Vproductparam>
	 * getProductparam(String filter) throws Exception { List<Vproductparam> oList =
	 * new ArrayList<Vproductparam>(); session = StoreHibernateUtil.openSession();
	 * oList = session.
	 * createSQLQuery("select mproduct.productcode,productname from Torderdata join Mproduct on mproductpk = mproductfk "
	 * + "where " + filter +
	 * " group by mproduct.productcode,productname order by mproduct.productcode").
	 * addEntity(Vproductparam.class).list(); session.close(); return oList; }
	 */

	public void save(Session session, Mproduct oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}

	public void delete(Session session, Mproduct oForm) throws HibernateException, Exception {
		session.delete(oForm);
	}

	@SuppressWarnings("unchecked")
	public List<Mproduct> startsWith(int maxrow, String value, String param) {
		List<Mproduct> oList = new ArrayList<Mproduct>();
		session = StoreHibernateUtil.openSession();
		if (param != null)
			oList = session.createSQLQuery("select * from Mproduct where " + param + " and productcode like '%" + value
					+ "%' order by productcode limit " + maxrow).addEntity(Mproduct.class).list();
		else
			oList = session.createSQLQuery("select * from Mproduct where productcode like '%" + value
					+ "%' order by productcode limit " + maxrow).addEntity(Mproduct.class).list();
		session.close();
		return oList;
	}

}
