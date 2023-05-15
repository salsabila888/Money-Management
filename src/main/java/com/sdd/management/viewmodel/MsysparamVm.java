package com.sdd.caption.viewmodel;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

import com.sdd.caption.dao.MsysparamDAO;
import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class MsysparamVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	
	private MsysparamDAO oDao = new MsysparamDAO();

	@Wire
	private Grid grid;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");				
		try {
			List<Msysparam> params = oDao.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_MAIL + "'", "orderno");
			Row row = new Row();
			Cell cellGroup = new Cell();
			cellGroup.setColspan(3);
			cellGroup.setAlign("center");
			Label lblGroup = new Label("Mail Configuration");
			lblGroup.setStyle("font-size: 14px; font-weight: bold");
			cellGroup.appendChild(lblGroup);
			row.appendChild(cellGroup);
			grid.getRows().appendChild(row);
			for (Msysparam obj: params) {
				row = new Row();
				row.appendChild(new Label(obj.getParamname()));
				row.appendChild(new Label(obj.getParamdesc()));
				Textbox tbox = new Textbox(obj.getParamvalue());
				tbox.setCols(45);
				tbox.setMaxlength(100);
				if (obj.getIsmasked().equals("Y"))
					tbox.setType("password");
				row.appendChild(tbox);
				//row.setId(obj.getParamcode());
				row.setAttribute("obj", obj);
				grid.getRows().appendChild(row);
			}
			
			params = oDao.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_ALERT + "'", "orderno");
			row = new Row();
			cellGroup = new Cell();
			cellGroup.setColspan(3);
			cellGroup.setAlign("center");
			lblGroup = new Label("Alert Configuration");
			lblGroup.setStyle("font-size: 14px; font-weight: bold");
			cellGroup.appendChild(lblGroup);
			row.appendChild(cellGroup);
			grid.getRows().appendChild(row);
			for (Msysparam obj: params) {
				row = new Row();
				row.appendChild(new Label(obj.getParamname()));
				row.appendChild(new Label(obj.getParamdesc()));
				Textbox tbox = new Textbox(obj.getParamvalue());
				tbox.setCols(45);
				tbox.setMaxlength(100);
				if (obj.getIsmasked().equals("Y"))
					tbox.setType("password");
				row.appendChild(tbox);
				//row.setId(obj.getParamcode());
				row.setAttribute("obj", obj);
				grid.getRows().appendChild(row);
			}
			
			params = oDao.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_SLA + "'", "orderno");
			row = new Row();
			cellGroup = new Cell();
			cellGroup.setColspan(3);
			cellGroup.setAlign("center");
			lblGroup = new Label("SLA Configuration");
			lblGroup.setStyle("font-size: 14px; font-weight: bold");
			cellGroup.appendChild(lblGroup);
			row.appendChild(cellGroup);
			grid.getRows().appendChild(row);
			for (Msysparam obj: params) {
				row = new Row();
				row.appendChild(new Label(obj.getParamname()));
				row.appendChild(new Label(obj.getParamdesc()));
				Textbox tbox = new Textbox(obj.getParamvalue());
				tbox.setCols(45);
				tbox.setMaxlength(100);
				if (obj.getIsmasked().equals("Y"))
					tbox.setType("password");
				row.appendChild(tbox);
				//row.setId(obj.getParamcode());
				row.setAttribute("obj", obj);
				grid.getRows().appendChild(row);
			}
			
			params = oDao.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_ESTSTOCK + "'", "orderno");
			row = new Row();
			cellGroup = new Cell();
			cellGroup.setColspan(3);
			cellGroup.setAlign("center");
			lblGroup = new Label("Estimasi Stock Configuration");
			lblGroup.setStyle("font-size: 14px; font-weight: bold");
			cellGroup.appendChild(lblGroup);
			row.appendChild(cellGroup);
			grid.getRows().appendChild(row);
			for (Msysparam obj: params) {
				row = new Row();
				row.appendChild(new Label(obj.getParamname()));
				row.appendChild(new Label(obj.getParamdesc()));
				Textbox tbox = new Textbox(obj.getParamvalue());
				tbox.setCols(45);
				tbox.setMaxlength(100);
				if (obj.getIsmasked().equals("Y"))
					tbox.setType("password");
				row.appendChild(tbox);
				//row.setId(obj.getParamcode());
				row.setAttribute("obj", obj);
				grid.getRows().appendChild(row);
			}
			
			params = oDao.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_BRANCHACTIVATIONHOST + "'", "orderno");
			row = new Row();
			cellGroup = new Cell();
			cellGroup.setColspan(3);
			cellGroup.setAlign("center");
			lblGroup = new Label("Branch Activation Host Configuration");
			lblGroup.setStyle("font-size: 14px; font-weight: bold");
			cellGroup.appendChild(lblGroup);
			row.appendChild(cellGroup);
			grid.getRows().appendChild(row);
			for (Msysparam obj: params) {
				row = new Row();
				row.appendChild(new Label(obj.getParamname()));
				row.appendChild(new Label(obj.getParamdesc()));
				Textbox tbox = new Textbox(obj.getParamvalue());
				tbox.setCols(45);
				tbox.setMaxlength(100);
				if (obj.getIsmasked().equals("Y"))
					tbox.setType("password");
				row.appendChild(tbox);
				//row.setId(obj.getParamcode());
				row.setAttribute("obj", obj);
				grid.getRows().appendChild(row);
			}
			
			params = oDao.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_PAGUPERIOD + "'", "orderno");
			row = new Row();
			cellGroup = new Cell();
			cellGroup.setColspan(3);
			cellGroup.setAlign("center");
			lblGroup = new Label("Pagu Stock Period");
			lblGroup.setStyle("font-size: 14px; font-weight: bold");
			cellGroup.appendChild(lblGroup);
			row.appendChild(cellGroup);
			grid.getRows().appendChild(row);
			for (Msysparam obj: params) {
				row = new Row();
				row.appendChild(new Label(obj.getParamname()));
				row.appendChild(new Label(obj.getParamdesc()));
				Textbox tbox = new Textbox(obj.getParamvalue());
				tbox.setCols(45);
				tbox.setMaxlength(100);
				if (obj.getIsmasked().equals("Y"))
					tbox.setType("password");
				row.appendChild(tbox);
				//row.setId(obj.getParamcode());
				row.setAttribute("obj", obj);
				grid.getRows().appendChild(row);
			}
			
			params = oDao.listByFilter("paramgroup = '" + AppUtils.PARAM_GROUP_ICON + "'", "orderno");
			row = new Row();
			cellGroup = new Cell();
			cellGroup.setColspan(3);
			cellGroup.setAlign("center");
			lblGroup = new Label("ICON Configuration");
			lblGroup.setStyle("font-size: 14px; font-weight: bold");
			cellGroup.appendChild(lblGroup);
			row.appendChild(cellGroup);
			grid.getRows().appendChild(row);
			for (Msysparam obj: params) {
				row = new Row();
				row.appendChild(new Label(obj.getParamname()));
				row.appendChild(new Label(obj.getParamdesc()));
				Textbox tbox = new Textbox(obj.getParamvalue());
				tbox.setCols(45);
				tbox.setMaxlength(100);
				if (obj.getIsmasked().equals("Y"))
					tbox.setType("password");
				row.appendChild(tbox);
				//row.setId(obj.getParamcode());
				row.setAttribute("obj", obj);
				grid.getRows().appendChild(row);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@Command
	public void doSave() {
		Session session = null;
		Transaction transaction = null;
		try {
			session = StoreHibernateUtil.openSession();
			transaction = session.beginTransaction();
			
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				if (comp.getChildren().size() > 1) {
					Textbox tbox = (Textbox) comp.getChildren().get(2);
					Msysparam obj = (Msysparam) comp.getAttribute("obj");
					obj.setParamvalue(tbox.getValue());
					obj.setUpdatedby(oUser.getUserid());
					obj.setLastupdated(new Date());
					oDao.save(session, obj);
				}				
			}			
			transaction.commit();
			Clients.showNotification(Labels.getLabel("common.update.success"), "info", null, "middle_center", 3000);
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Error : " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		} finally {
			session.close();
		}
	}
	
}
