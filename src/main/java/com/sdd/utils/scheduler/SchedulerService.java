package com.sdd.utils.scheduler;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppCleanup;
import org.zkoss.zk.ui.util.WebAppInit;

import com.sdd.caption.handler.ScheduleManager;

public class SchedulerService implements WebAppInit, WebAppCleanup {

	@Override
	public void cleanup(WebApp wapp) throws Exception {
		
	}

	@Override
	public void init(WebApp wapp) throws Exception {		
		//new ScheduleManager().initializer(wapp.getRealPath("/"));
		new ScheduleManager().initializer(wapp.getRealPath(""));
	}	
}
