package com.sdd.caption.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Vdeliverytotal;
import com.sdd.caption.domain.Vgroupbybranch;
import com.sdd.caption.domain.Vgroupbyregion;
import com.sdd.caption.domain.Vgroupbysla;
import com.sdd.caption.domain.Vinventory;
import com.sdd.caption.domain.Vpersodeliv;
import com.sdd.caption.domain.Vproductprod;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class DashboardDAO {
	
	private Session session;
	
	public int usermenuChecker(String filter) throws Exception {
        int count = 0;
        if (filter == null || "".equals(filter)) {
            filter = "0 = 0";
        }
        session = StoreHibernateUtil.openSession();
        count = Integer.parseInt(session.createSQLQuery("select count(*) from muser join musergroup on musergroupfk = musergrouppk "
        		+ "join musergroupmenu on musergrouppk = musergroupmenu.musergroupfk join mmenu on mmenufk = mmenupk "
        		+ "where " + filter).uniqueResult().toString());
        session.close();
        return count;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vinventory> getStockEstimation() throws Exception {		
    	List<Vinventory> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select morg.org as org,morg.description as description,coalesce(a,0) as a,coalesce(b,0) as b,coalesce(c,0) as c from morg left join (" + 
				"select org,description,count(*) as a from mproducttype join morg on productorg = org where estdays >= 180 and isestcount = 'Y' group by org,description order by org) as a on morg.org = a.org left join (" + 
				"select org,description,count(*) as b from mproducttype join morg on productorg = org where estdays between 90 and 180 and isestcount = 'Y' group by org,description order by org) as b on morg.org = b.org left join (" + 
				"select org,description,count(*) as c from mproducttype join morg on productorg = org where estdays < 90 and isestcount = 'Y' group by org,description order by org) as c on morg.org = c.org " + 
				"order by org").addEntity(Vinventory.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vpersodeliv> listOrdOnOrdVsProd(String date) throws Exception {		
    	List<Vpersodeliv> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select morg.org, description, coalesce(sum(totaldata),0) as totaldata from morg left join mproducttype on org = productorg " + 
				"left join mproduct on mproducttypepk = mproducttypefk left join tembossproduct on mproductpk = mproductfk " + 
				"and orderdate = '" + date + "' group by morg.org, description order by morg.org").addEntity(Vpersodeliv.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vpersodeliv> listProdOnOrdVsProd(String date) throws Exception {		
    	List<Vpersodeliv> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select morg.org, description, coalesce(sum(totaldata),0) as totaldata from morg left join mproducttype on org = productorg " + 
				"left join mproduct on mproducttypepk = mproducttypefk left join tperso on mproductpk = mproductfk " + 
				"and status = '" + AppUtils.STATUS_PERSO_DONE + "' and orderdate = '" + date + "' group by morg.org, description order by morg.org").addEntity(Vpersodeliv.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vpersodeliv> listProdOnProdVsDlv(String date) throws Exception {		
    	List<Vpersodeliv> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select org, description, coalesce(sum(totaldata),0) as totaldata from morg left join mproducttype on org = productorg " + 
				"left join mproduct on mproducttypepk = mproducttypefk left join tperso on mproducttypepk = mproducttypefk " + 
				"and status = '" + AppUtils.STATUS_PERSO_DONE + "' and orderdate = '" + date + "' group by org, description order by org").addEntity(Vpersodeliv.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vpersodeliv> listDlvOnProdVsDlv(String date) throws Exception {		
    	List<Vpersodeliv> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select org, description, coalesce(sum(quantity),0) as totaldata from morg left join mproducttype on org = productorg left join mproduct on mproducttypepk = mproducttypefk " + 
				"left join tdeliverydata on mproductpk = mproductfk " + 
				"and orderdate = '" + date + "' group by org, description order by org").addEntity(Vpersodeliv.class).list();
		session.close();
        return oList;
    }

	@SuppressWarnings("unchecked")
	public List<Vpersodeliv> listOrdOnOrdVsProdProcCod(String date, String org) throws Exception {		
    	List<Vpersodeliv> oList = null;
    	String filterorg = org.equals("200") ? "productorg like '2%'" : "productorg = '"+ org +"'";
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select tembossproduct.productcode as org, '' as description, coalesce(sum(totaldata),0) as totaldata from Tembossproduct join mproduct on mproductfk = mproductpk join mproducttype on mproducttypefk = mproducttypepk " + 
				"where " + filterorg + 
				" and orderdate = '" + date + "' group by tembossproduct.productcode order by tembossproduct.productcode").addEntity(Vpersodeliv.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vpersodeliv> listProdOnOrdVsProdProcCod(String date, String org) throws Exception {		
    	List<Vpersodeliv> oList = null;
    	String filterorg = org.equals("200") ? "productorg like '2%'" : "productorg = '"+ org +"'";
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select productcode as org, '' as description, coalesce(sum(totaldata),0) as totaldata from tperso join mproduct on mproductfk = mproductpk " + 
				"join mproducttype on mproduct.mproducttypefk = mproducttypepk where " + filterorg + 
				" and  status = '" + AppUtils.STATUS_PERSO_DONE + "' and orderdate = '" + date + "' group by org order by org").addEntity(Vpersodeliv.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vgroupbyregion> listOrdOnOrdVsProdRegion(String date, String productcode) throws Exception {		
    	List<Vgroupbyregion> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select mregionpk,regionname,coalesce(sum(tembossbranch.totaldata),0) as total " + 
				"from Tembossproduct join Tembossbranch on tembossproductfk = tembossproductpk join mbranch on mbranchfk = mbranchpk join mregion on mregionfk = mregionpk " + 
				"where productcode = '" + productcode + "' and tembossbranch.orderdate = '" + date + "' group by mregionpk,regionname order by regionname").addEntity(Vgroupbyregion.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vgroupbyregion> listProdOnOrdVsProdRegion(String date, String productcode) throws Exception {		
    	List<Vgroupbyregion> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select mregionpk,regionname,coalesce(sum(quantity),0) as total from tperso join tpersodata on tpersopk = tpersofk join mproduct on tperso.mproductfk = mproductpk " + 
				"join mbranch on mbranchfk = mbranchpk join mregion on mregionfk = mregionpk where mproduct.productcode = '" + productcode + "' and tperso.status = '" + AppUtils.STATUS_PERSO_DONE + "' "
						+ "and tperso.orderdate = '" + date + "'" + 
				"group by mregionpk,regionname order by regionname").addEntity(Vgroupbyregion.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vgroupbybranch> listOrdOnOrdVsProdBranch(String date, String productcode, Integer region) throws Exception {		
    	List<Vgroupbybranch> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select mbranchpk,branchname,coalesce(sum(tembossbranch.totaldata),0) as total " + 
				"from Tembossproduct join Tembossbranch on tembossproductfk = tembossproductpk join Mbranch on mbranchfk = mbranchpk where productcode = '" + productcode + "' and tembossbranch.orderdate = '" + date + 
				"' and mregionfk = " + region + " group by mbranchpk,branchname order by branchname").addEntity(Vgroupbybranch.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vgroupbybranch> listProdOnOrdVsProdBranch(String date, String productcode, Integer region) throws Exception {		
    	List<Vgroupbybranch> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select mbranchpk,branchname,coalesce(sum(quantity),0) as total from tperso "
				+ "join tpersodata on tpersopk = tpersofk join mproduct on tperso.mproductfk = mproductpk " 
				+ "join mbranch on mbranchfk = mbranchpk where mproduct.productcode = '" + productcode + "' and tperso.status = '" + AppUtils.STATUS_PERSO_DONE + "' "
				+ "and tperso.orderdate = '" + date + "' " + " and mregionfk = " + region + " group by mbranchpk,branchname order by branchname").addEntity(Vgroupbybranch.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vdeliverytotal> listProdOnProdVsDlvProcod(String date, String org) throws Exception {		
    	List<Vdeliverytotal> oList = null;
    	String filterorg = org.equals("200") ? "productorg like '2%'" : "productorg = '"+ org +"'";
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select productcode as id, coalesce(sum(totaldata),0) as totaldata "
				+ "from tperso join mproduct on mproductfk = mproductpk join mproducttype on mproduct.mproducttypefk = mproducttypepk" + 
				" where status = '" + AppUtils.STATUS_PERSO_DONE + "' and orderdate = '" + date + "' and " + filterorg + " group by id").addEntity(Vdeliverytotal.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vdeliverytotal> listDlvOnProdVsDlvProcod(String date, String org) throws Exception {		
    	List<Vdeliverytotal> oList = null;
    	String filterorg = org.equals("200") ? "productorg like '2%'" : "productorg = '"+ org +"'";
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select mproduct.productcode as id, coalesce(sum(quantity),0) as totaldata "
				+ "from tdeliverydata join mproduct on mproductfk = mproductpk join mproducttype on mproduct.mproducttypefk = mproducttypepk" + 
				" where orderdate = '" + date + "' and "+ filterorg +" group by id ").addEntity(Vdeliverytotal.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vgroupbybranch> listProdOnProdVsDlvBranch(String date, String productcode, Integer mregionpk) throws Exception {		
    	List<Vgroupbybranch> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select mbranchpk,branchname,coalesce(sum(quantity),0) as total from tperso join mproduct on mproductfk = mproductpk join " + 
				"tpersodata on tpersopk = tpersofk " + 
				"join mbranch on mbranchfk = mbranchpk " + 
				"where tperso.status = '" + AppUtils.STATUS_PERSO_DONE + "' and tperso.orderdate = '" + date + "' and mproduct.productcode = '" + productcode + "' and mregionfk = " + mregionpk + " group by mbranchpk,branchname " + 
				"order by branchname").addEntity(Vgroupbybranch.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vgroupbybranch> listDlvOnProdVsDlvBranch(String date, String productcode, Integer mregionpk) throws Exception {		
    	List<Vgroupbybranch> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select mbranchpk,mbranch.branchname,coalesce(sum(quantity),0) as total from tdeliverydata join mbranch on mbranchfk = mbranchpk " + 
				"where orderdate = '" + date + "' and productcode = '" + productcode + "' and mregionfk = " + mregionpk + " group by mbranchpk,mbranch.branchname " + 
				"order by branchname").addEntity(Vgroupbybranch.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Vinventory> listStockVsPagu () throws Exception {		
    	List<Vinventory> oList = null;
    	session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery("select morg.org as org,morg.description as description,coalesce(a,0) as a,coalesce(b,0) as b,coalesce(c,0) as c from morg left join (" + 
				"select org,description,count(*) as a from (" + 
				"select org,description,mproducttypepk,producttype,round(laststock-stockmin)/stockmin*100 as persen from mproducttype join morg on productorg = org " + 
				"where productgroupcode = '01' and stockmin > 0 order by org,producttype) as a where persen >= 50 group by org,description order by org) as a on morg.org = a.org left join (" + 
				"select org,description,count(*) as b from (" + 
				"select org,description,mproducttypepk,producttype,round(laststock-stockmin)/stockmin*100 as persen from mproducttype join morg on productorg = org " + 
				"where productgroupcode = '01' and stockmin > 0 order by org,producttype) as a where persen between 25 and 50 group by org,description order by org) as b on morg.org = b.org left join (" + 
				"select org,description,count(*) as c from (" + 
				"select org,description,mproducttypepk,producttype,round(laststock-stockmin)/stockmin*100 as persen from mproducttype join morg on productorg = org " + 
				"where productgroupcode = '01' and stockmin > 0 order by org,producttype) as a where persen < 25 group by org,description order by org) as c on morg.org = c.org " + 
				"order by org").addEntity(Vinventory.class).list();
		session.close();
        return oList;
    }
	
	@SuppressWarnings("unchecked")
	public List<Mproducttype> listProducttypeestimate(String org, String filterpersen) throws Exception {
		List<Mproducttype> oList = new ArrayList<Mproducttype>();
		String filterorg = org.equals("200") ? "productorg like '2%" : "productorg = '"+ org;
       	session = StoreHibernateUtil.openSession();
       	oList = session.createSQLQuery("select mproducttype.* from mproducttype join ( " + 
       			"select mproducttypepk,round(laststock-stockmin)/stockmin*100 as persen from mproducttype " + 
       			"where productgroupcode = '" + AppUtils.PRODUCTGROUP_CARD + "' and stockmin > 0 and " + filterorg + "') as a on mproducttype.mproducttypepk = a.mproducttypepk where " + filterpersen).addEntity(Mproducttype.class).list();   
        session.close();
        return oList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Vproductprod> getDataPersoPlan(String date) throws Exception {
		List<Vproductprod> oList = null;
		session = StoreHibernateUtil.openSession();
		oList = session.createSQLQuery(
				"select productcode, sum(totaldata) as totaldata, min(orderdate) as orderdate from tperso"
						+ " join mproduct on mproductfk = mproductpk where status = '" + AppUtils.STATUS_PERSO_PRODUKSI + "' and date(persostarttime) = '" + date + "'"
						+ " group by productcode order by orderdate")
				.addEntity(Vproductprod.class).list();
		session.close();
		return oList;
	}
}
