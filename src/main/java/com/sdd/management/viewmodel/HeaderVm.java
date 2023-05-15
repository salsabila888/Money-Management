package com.sdd.caption.viewmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zhtml.Button;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Muser;

public class HeaderVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private Muser oUser;
	private Integer totalnotif;
	private String user;
	private String label;
	private String producttype;

	@Wire
	private Div divNotif;
	@Wire
	private Label lblNotif, lblUser;
	@Wire
	private Button btnNotif;
	@Wire
	private Grid grid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		if (oUser != null) {
			user = oUser.getUserid() + " - " + oUser.getMusergroup().getUsergroupname();
		}
//		doNotif();

	}

	@Command
	public void doCheck() {
		Window win = (Window) Executions.createComponents("/accountinformation.zul", null, null);
		win.setWidth("40%");
		win.setClosable(true);
		win.doModal();
	}

	@Command
	public void doLogout() {
		if (zkSession.getAttribute("oUser") != null) {
			zkSession.removeAttribute("oUser");
		}
		Executions.sendRedirect("/logout.zul");
	}

//	@NotifyChange("*")
//	public void doNotif() {
//		try {
//			totalnotif = 0;
//			List<Mproducttype> objList = new MproducttypeDAO().listByFilter("laststock < stockmin", "producttype");
//			if (objList.size() > 0) {
//				label = " Ada " + objList.size() + " produk yang memiliki jumlah stok dibawah stok pagu.";
//
//				Rows rows = new Rows();
//				int index = 1;
//				for (Mproducttype obj : objList) {
//					Row row = new Row();
//					Label label = new Label(String.valueOf(index));
//					row.appendChild(label);
//					label = new Label(obj.getProducttype());
//					row.appendChild(label);
//					label = new Label(String.valueOf(obj.getLaststock()));
//					row.appendChild(label);
//
//					rows.getChildren().add(row);
//					index++;
//					totalnotif++;
//				}
//				grid.appendChild(rows);
//				btnNotif.setSclass("btn btn-danger active btn-sm dropdown-toggle");
//			} else {
//				btnNotif.setVisible(false);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			List<Mproducttype> objList = new ArrayList<Mproducttype>();
			if (producttype != null && !"".equals(producttype)) {
				objList = new MproducttypeDAO().listByFilter(
						"producttype like '%" + producttype.toUpperCase() + "%' and laststock < stockmin",
						"producttype");
			} else {
				objList = new MproducttypeDAO().listByFilter("laststock < stockmin", "producttype");
			}

			if (objList.size() > 0) {
				grid.getRows().getChildren().clear();
				Rows rows = grid.getRows();
				int index = 1;
				for (Mproducttype obj : objList) {
					Row row = new Row();
					Label label = new Label(String.valueOf(index));
					row.appendChild(label);
					label = new Label(obj.getProducttype());
					row.appendChild(label);
					label = new Label(String.valueOf(obj.getLaststock()));
					row.appendChild(label);

					rows.getChildren().add(row);
					index++;
				}
				grid.appendChild(rows);
				btnNotif.setSclass("btn btn-danger active btn-sm dropdown-toggle");
			} else {
				Messagebox.show("Data tidak ditemukan..", "Info", Messagebox.OK, Messagebox.INFORMATION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Integer getTotalnotif() {
		return totalnotif;
	}

	public void setTotalnotif(Integer totalnotif) {
		this.totalnotif = totalnotif;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}
}
