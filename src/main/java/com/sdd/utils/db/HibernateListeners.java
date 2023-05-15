package com.sdd.utils.db;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppCleanup;
import org.zkoss.zk.ui.util.WebAppInit;

public class HibernateListeners implements WebAppInit, WebAppCleanup  {

	@Override
	public void init(WebApp wapp) throws Exception {
		StoreHibernateUtil.getSessionFactory();				
	}
	
	@Override
	public void cleanup(WebApp wapp) throws Exception {
		StoreHibernateUtil.getSessionFactory().close();		
	}	

}
