package com.sdd.caption.dao;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class TcounterengineDAO {
	
	private Session session;	
	private Transaction transaction;
	
	private MsysparamDAO oDao = new MsysparamDAO();
	
	public String generateAwbCounter(String prefix) throws Exception {
		Integer lastCounter = 0;
		String strCounter = "";
		String finalNumber = "";
		char[] fillUploadid = new char[7];
		
		try {
			String year = new SimpleDateFormat("YY").format(new Date());
			String counterName = "AWB" + year;
			Random random = new Random();
			String rndnum = "000000" + (random.nextInt(100000));
			String rndnumfix = rndnum.substring(rndnum.length()-6, rndnum.length());
			String lastnum =  rndnumfix.substring(rndnumfix.length()-1);
			String quadrate = String.valueOf(Integer.parseInt(lastnum) * Integer.parseInt(lastnum));
			String checksum = quadrate.substring(quadrate.length()-1);
			session = StoreHibernateUtil.openSession();
    		transaction = session.beginTransaction();
			Query q = session.createQuery("select lastcounter from Tcounterengine where countername = '" + counterName + "'");
			lastCounter = (Integer) q.uniqueResult();
			if (lastCounter != null) {
				lastCounter++;
				session.createSQLQuery("update Tcounterengine set lastcounter = lastcounter + 1 where countername = '" + counterName + "'").executeUpdate();				
			} else {
				lastCounter = 1;
				session.createSQLQuery("insert into Tcounterengine values ('" + counterName + "', " + lastCounter + ")").executeUpdate();
			}			
			transaction.commit();
			session.close();
			Arrays.fill(fillUploadid, '0');
			strCounter = new String(fillUploadid) + lastCounter;
			finalNumber = prefix + checksum + year + strCounter.substring(strCounter.length()-7, strCounter.length());			
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return finalNumber;
	}
		
	public String generateCounter(String prefix) throws Exception {
		Integer lastCounter = 0;
		String strCounter = "";
		String finalCounter = "";
		int len = 7;
		char[] fillUploadid = new char[len];
		
		try {
			String counterName = prefix + new SimpleDateFormat("YY").format(new Date());
			session = StoreHibernateUtil.openSession();
    		transaction = session.beginTransaction();
			Query q = session.createQuery("select lastcounter from Tcounterengine where countername = '" + counterName + "'");
			lastCounter = (Integer) q.uniqueResult();
			if (lastCounter != null) {
				lastCounter++;
				session.createSQLQuery("update Tcounterengine set lastcounter = lastcounter + 1 where countername = '" + counterName + "'").executeUpdate();				
			} else {
				lastCounter = 1;
				session.createSQLQuery("insert into Tcounterengine values ('" + counterName + "', " + lastCounter + ")").executeUpdate();
			}			
			transaction.commit();
			session.close();
			Arrays.fill(fillUploadid, '0');
			strCounter = new String(fillUploadid) + lastCounter;
			finalCounter = counterName + strCounter.substring(strCounter.length()-len, strCounter.length());			
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}		
		return finalCounter;
	}
	
	public String generateYearMonthCounter(String prefix) throws Exception {
		Integer lastCounter = 0;
		String strCounter = "";
		String finalCounter = "";
		int len = 4;
		char[] fillUploadid = new char[len];
		
		try {
			String counterName = prefix + new SimpleDateFormat("YY").format(new Date()) + new SimpleDateFormat("MM").format(new Date());
			session = StoreHibernateUtil.openSession();
    		transaction = session.beginTransaction();
			Query q = session.createQuery("select lastcounter from Tcounterengine where countername = '" + counterName + "'");
			lastCounter = (Integer) q.uniqueResult();
			if (lastCounter != null) {
				lastCounter++;
				session.createSQLQuery("update Tcounterengine set lastcounter = lastcounter + 1 where countername = '" + counterName + "'").executeUpdate();				
			} else {
				lastCounter = 1;
				session.createSQLQuery("insert into Tcounterengine values ('" + counterName + "', " + lastCounter + ")").executeUpdate();
			}			
			transaction.commit();
			session.close();
			Arrays.fill(fillUploadid, '0');
			strCounter = new String(fillUploadid) + lastCounter;
			finalCounter = counterName + strCounter.substring(strCounter.length()-len, strCounter.length());			
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}		
		return finalCounter;
	}
	
	public String generateCounterVendor(String prefix) throws Exception {
		Integer lastCounter = 0;
		String strCounter = "";
		String finalCounter = "";
		int len = 5;
		char[] fillUploadid = new char[len];
		
		try {
			String counterName = prefix + new SimpleDateFormat("YY").format(new Date());
			session = StoreHibernateUtil.openSession();
    		transaction = session.beginTransaction();
			Query q = session.createQuery("select lastcounter from Tcounterengine where countername = '" + counterName + "'");
			lastCounter = (Integer) q.uniqueResult();
			if (lastCounter != null) {
				lastCounter++;
				session.createSQLQuery("update Tcounterengine set lastcounter = lastcounter + 1 where countername = '" + counterName + "'").executeUpdate();				
			} else {
				lastCounter = 1;
				session.createSQLQuery("insert into Tcounterengine values ('" + counterName + "', " + lastCounter + ")").executeUpdate();
			}			
			transaction.commit();
			session.close();
			Arrays.fill(fillUploadid, '0');
			strCounter = new String(fillUploadid) + lastCounter;
			finalCounter = counterName + strCounter.substring(strCounter.length()-len, strCounter.length());			
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}		
		return finalCounter;
	}
	
	public String generateBranchLetterNo(String branchcode, String prefix) throws Exception {
		Integer lastCounter = 0;
		String strCounter = "";
		String finalCounter = "";
		int len = 5;
		char[] fillUploadid = new char[len];
		
		try {
			String counterName = prefix + new SimpleDateFormat("YY").format(new Date());
			session = StoreHibernateUtil.openSession();
    		transaction = session.beginTransaction();
			Query q = session.createQuery("select lastcounter from Tcounterengine where countername = '" + counterName + "'");
			lastCounter = (Integer) q.uniqueResult();
			if (lastCounter != null) {
				lastCounter++;
				session.createSQLQuery("update Tcounterengine set lastcounter = lastcounter + 1 where countername = '" + counterName + "'").executeUpdate();				
			} else {
				lastCounter = 1;
				session.createSQLQuery("insert into Tcounterengine values ('" + counterName + "', " + lastCounter + ")").executeUpdate();
			}			
			transaction.commit();
			session.close();
			Arrays.fill(fillUploadid, '0');
			strCounter = new String(fillUploadid) + lastCounter;
			finalCounter = branchcode + "/" + new SimpleDateFormat("YY").format(new Date()) + "/" + strCounter.substring(strCounter.length()-len, strCounter.length()) + "/" + prefix;			
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}		
		return finalCounter;
	}
	
	public String generateLetterNo(String prefix) throws Exception {
		Integer lastCounter = 0;
		String strCounter = "";
		String finalCounter = "";
		int len = 5;
		char[] fillUploadid = new char[len];
		
		try {
			String counterName = prefix + new SimpleDateFormat("YY").format(new Date());
			Msysparam params = oDao.findById(AppUtils.PARAM_PREFIXSURAT);
			session = StoreHibernateUtil.openSession();
    		transaction = session.beginTransaction();
			Query q = session.createQuery("select lastcounter from Tcounterengine where countername = '" + counterName + "'");
			lastCounter = (Integer) q.uniqueResult();
			if (lastCounter != null) {
				lastCounter++;
				session.createSQLQuery("update Tcounterengine set lastcounter = lastcounter + 1 where countername = '" + counterName + "'").executeUpdate();				
			} else {
				lastCounter = 1;
				session.createSQLQuery("insert into Tcounterengine values ('" + counterName + "', " + lastCounter + ")").executeUpdate();
			}			
			transaction.commit();
			session.close();
			Arrays.fill(fillUploadid, '0');
			strCounter = new String(fillUploadid) + lastCounter;
			finalCounter = params.getParamvalue() + new SimpleDateFormat("YY").format(new Date()) + "/" + strCounter.substring(strCounter.length()-len, strCounter.length()) + "/" + prefix;			
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}		
		return finalCounter;
	}
	
	public String generateNopaket() throws Exception {
		Integer lastCounter = 0;
		String strCounter = "";
		String finalCounter = "";
		char[] fillUploadid = new char[7];
		String counterName = "NOPAKET";
		try {
			session = StoreHibernateUtil.openSession();
    		transaction = session.beginTransaction();
			Query q = session.createQuery("select lastcounter from Tcounterengine where countername = '" + counterName + "'");
			lastCounter = (Integer) q.uniqueResult();
			if (lastCounter != null) {
				lastCounter++;
				session.createSQLQuery("update Tcounterengine set lastcounter = lastcounter + 1 where countername = '" + counterName + "'").executeUpdate();				
			} else {
				lastCounter = 1;
				session.createSQLQuery("insert into Tcounterengine values ('" + counterName + "', " + lastCounter + ")").executeUpdate();
			}			
			transaction.commit();
			session.close();
			Arrays.fill(fillUploadid, '0');
			strCounter = new String(fillUploadid) + lastCounter;
			finalCounter = strCounter.substring(strCounter.length()-7, strCounter.length());			
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}		
		return finalCounter;
	}
	
	public String generateSeqnum() throws Exception {
		Integer lastCounter = 0;
		String strCounter = "";
		String finalCounter = "";
		int len = 4;
		char[] fillUploadid = new char[len];
		
		try {
			String counterName = new SimpleDateFormat("YY").format(new Date()) + new SimpleDateFormat("MM").format(new Date());
			session = StoreHibernateUtil.openSession();
    		transaction = session.beginTransaction();
			Query q = session.createQuery("select lastcounter from Tcounterengine where countername = '" + counterName + "'");
			lastCounter = (Integer) q.uniqueResult();
			if (lastCounter != null) {
				lastCounter++;
				session.createSQLQuery("update Tcounterengine set lastcounter = lastcounter + 1 where countername = '" + counterName + "'").executeUpdate();				
			} else {
				lastCounter = 1;
				session.createSQLQuery("insert into Tcounterengine values ('" + counterName + "', " + lastCounter + ")").executeUpdate();
			}			
			transaction.commit();
			session.close();
			Arrays.fill(fillUploadid, '0');
			strCounter = new String(fillUploadid) + lastCounter;
			finalCounter = counterName + strCounter.substring(strCounter.length()-len, strCounter.length());			
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}		
		return finalCounter;
	}
	
	public Integer getLastcounter(String prefix) throws Exception {
		Integer lastCounter = 0;
		try {
			session = StoreHibernateUtil.openSession();
    		Query q = session.createQuery("select lastcounter from Tcounterengine where countername = '" + prefix + "'");
			lastCounter = (Integer) q.uniqueResult();
			if (lastCounter == null) {
				lastCounter = 0;	
				transaction = session.beginTransaction();
				session.createSQLQuery("insert into Tcounterengine values ('" + prefix + "', " + lastCounter + ")").executeUpdate();
				transaction.commit();				
			}	
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return lastCounter;
	}
	
	public void save(String prefix, int val) throws Exception {
		try {
			session = StoreHibernateUtil.openSession();
    		transaction = session.beginTransaction();
			session.createSQLQuery("update Tcounterengine set lastcounter = " + val + " where countername = '" + prefix + "'").executeUpdate();			
			transaction.commit();
			session.close();			
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}
	}

}
