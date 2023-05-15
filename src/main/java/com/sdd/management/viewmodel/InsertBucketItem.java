package com.sdd.caption.viewmodel;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.TbranchitembucketDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.dao.TpilotingDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Tbranchitembucket;
import com.sdd.caption.domain.Tbranchstock;
import com.sdd.caption.domain.Tpiloting;
import com.sdd.utils.db.StoreHibernateUtil;

public class InsertBucketItem {

	public static void main(String[] args) {
		try {
			List<Tpiloting> objList = new TpilotingDAO().listByFilter("status = 'DC' and tpilotingpk != 12",
					"tpilotingpk");
			Session session = StoreHibernateUtil.openSession();
			Transaction trx = session.beginTransaction();
			for (Tpiloting oForm : objList) {
				Mbranch mbranch = new MbranchDAO().findByFilter("branchid = '" + oForm.getBranchid().trim() + "'");
				Mproduct mproduct = new MproductDAO()
						.findByFilter("productname = '" + oForm.getProducttype().trim() + "'");

				Tbranchstock tbs = new TbranchstockDAO()
						.findByFilter("mbranchfk = " + mbranch.getMbranchpk() + " and outlet = '"
								+ oForm.getOutlet().trim() + "' and mproductfk = " + mproduct.getMproductpk() + " and productgroup = '04'");

				Tbranchitembucket tbib = new Tbranchitembucket();
				tbib.setCurrentno(oForm.getStartno());
				tbib.setIsgenerate("N");
				tbib.setIspod("N");
				tbib.setIsrunout("N");
				tbib.setItemendno(oForm.getEndno());
				tbib.setItemprice(oForm.getItemprice());
				tbib.setItemstartno(oForm.getStartno());
				tbib.setOutbound(0);
				tbib.setOutlet(oForm.getOutlet());
				tbib.setPrefix(oForm.getPrefix());
				tbib.setTbranchstock(tbs);
				tbib.setTotalitem(oForm.getTotalqty());
				tbib.setInserttime(new Date());
				new TbranchitembucketDAO().save(session, tbib);
				
				oForm.setStatus("WA");
				oForm.setTbranchitembucket(tbib);
				new TpilotingDAO().save(session, oForm);
			}
			trx.commit();
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
