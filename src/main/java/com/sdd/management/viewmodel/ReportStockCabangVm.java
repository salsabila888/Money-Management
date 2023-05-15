package com.sdd.caption.viewmodel;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Row;

import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Muser;

public class ReportStockCabangVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private TbranchstockDAO oDao = new TbranchstockDAO();
	private String filter;
	private int branchlevel;
	
	private Mbranch mbranch;
	
	@Wire
	private Combobox cbBranch;
	@Wire
	private Grid grid;
	@Wire
	private Row rowBranch;
	@Wire
	private Button btnSearch, btnReset;
	
	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		
		if (oUser != null) {
			branchlevel = oUser.getMbranch().getBranchlevel();
			if (branchlevel < 3) {
				rowBranch.setVisible(true);
			} else {
				rowBranch.setVisible(false);
				btnSearch.setVisible(false);
				btnReset.setVisible(false);
			}
		}
	}
}
