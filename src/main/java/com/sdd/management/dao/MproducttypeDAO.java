package com.sdd.management.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sdd.management.domain.Mproducttype;
import com.sdd.management.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MproducttypeDAO {

	private Session session;

	@SuppressWarnings("unchecked")
	public List<Mproducttype> listPaging(int first, int second, String filter, String orderby) throws Exception {
		List<Mproducttype> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select * from Mproducttype " + "where " + filter + " order by " + orderby
				+ " limit " + second + " offset " + first).addEntity(Mproducttype.class).list();

		session.close();
		return oList;
	}

	@SuppressWarnings("unchecked")
	public List<Mproducttype> listNativeByFilter(String filter, String orderby) throws Exception {
		List<Mproducttype> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery(
				"select * from Mproducttype join Mproductowner on mproductownerfk = mproductownerpk where " + filter
						+ " order by " + orderby)
				.addEntity(Mproducttype.class).list();

		session.close();
		return oList;
	}

	public int pageCount(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session
				.createSQLQuery("select count(*) from Mproducttype " + "where " + filter).uniqueResult().toString());
		session.close();
		return count;
	}

	public int getSumm(String filter) throws Exception {
		int count = 0;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session
				.createSQLQuery("select coalesce(sum(laststock),0) from Mproducttype " + "where " + filter)
				.uniqueResult().toString());
		session.close();
		return count;
	}

	@SuppressWarnings("unchecked")
	public List<Mproducttype> listByFilter(String filter, String orderby) throws Exception {
		List<Mproducttype> oList = null;
		if (filter == null || "".equals(filter))
			filter = "0 = 0";
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("from Mproducttype where " + filter + " order by " + orderby).list();
		session.close();
		return oList;
	}

	public Mproducttype findByPk(Integer pk) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mproducttype oForm = (Mproducttype) session.createQuery("From Mproducttype where mproducttypepk = " + pk)
				.uniqueResult();
		session.close();
		return oForm;
	}

	public Mproducttype findByFilter(String filter) throws Exception {
		session = StoreHibernateUtil.openSession();
		Mproducttype oForm = (Mproducttype) session.createQuery("From Mproducttype where " + filter).uniqueResult();
		session.close();
		return oForm;
	}

	@SuppressWarnings("rawtypes")
	public List listStr(String fieldname) throws Exception {
		List oList = new ArrayList();
		session = StoreHibernateUtil.openSession();
		oList = session.createQuery("select " + fieldname + " from Mproducttype order by " + fieldname).list();
		session.close();
		return oList;
	}

	public void save(Session session, Mproducttype oForm) throws HibernateException, Exception {
		session.saveOrUpdate(oForm);
	}

	public void delete(Session session, Mproducttype oForm) throws HibernateException, Exception {
		session.delete(oForm);
	}
	
	public int pageCountStockHistory(String filter) throws Exception {
		int count = 0;
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery(
				"select count(*) from (select producttype, 'INCOMING' as trxtype, entrytime as trxtime, itemqty, memo "
						+ "from mproducttype join tincoming on mproducttypepk = mproducttypefk where " + filter
						+ " and tincoming.status = '" + AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED + "' " + "union all "
						+ "select mproducttype.producttype, 'OUTGOING' as trxtype, entrytime as trxtime, itemqty, toutgoing.memo "
						+ "from mproducttype join toutgoing on mproducttypepk = mproducttypefk where " + filter
						+ " and toutgoing.status = '" + AppUtils.STATUS_INVENTORY_OUTGOINGAPPROVED + "') as a ")
				.uniqueResult().toString());
		session.close();
		return count;
	}

	public Integer sumStockHistory(String filter) throws Exception {
		Integer count = 0;
		session = StoreHibernateUtil.openSession();
		count = Integer.parseInt((String) session.createSQLQuery(
				"select sum(itemqty) from (select producttype, 'INCOMING' as trxtype, entrytime as trxtime, itemqty, memo "
						+ "from mproducttype join tincoming on mproducttypepk = mproducttypefk where " + filter
						+ " and tincoming.status = '" + AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED + "' " + "union all "
						+ "select mproducttype.producttype, 'OUTGOING' as trxtype, entrytime as trxtime, itemqty, toutgoing.memo "
						+ "from mproducttype join toutgoing on mproducttypepk = mproducttypefk where " + filter
						+ " and toutgoing.status = '" + AppUtils.STATUS_INVENTORY_OUTGOINGAPPROVED + "') as a ")
				.uniqueResult().toString());
		session.close();
		return count;
	}

	public void updateBlockPagu(Session session) throws Exception {
		session.createQuery(
				"update Mproducttype set isalertstockpagu = 'Y', alertstockpagudate = date(now()), isblockpagu = 'Y', blockpagutime = now() "
						+ "where laststock < stockmin and isalertstockpagu = 'N' and productgroupcode = '"
						+ AppUtils.PRODUCTGROUP_CARD + "'")
				.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public List<Mproducttype> startsWith(int maxrow, String value, String param) {
		List<Mproducttype> oList = new ArrayList<Mproducttype>();
		session = StoreHibernateUtil.openSession();
		if (param != null)
			oList = session.createSQLQuery("select * from Mproducttype where " + param + " and producttype like '%"
					+ value + "%' order by producttype limit " + maxrow).addEntity(Mproducttype.class).list();
		else
			oList = session.createSQLQuery("select * from Mproducttype where producttype like '%" + value
					+ "%' order by producttype limit " + maxrow).addEntity(Mproducttype.class).list();
		session.close();
		return oList;
	}
}
