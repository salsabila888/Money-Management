package com.sdd.caption.viewmodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.sdd.caption.dao.TplanDAO;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.TReportPlan;
import com.sdd.caption.utils.AppData;

public class PlanningBranchlistVm {
	private Session zkSession = Sessions.getCurrent();
	private Muser oUser;

	private TplanDAO oDao = new TplanDAO();

	private String productgroup, filter, orderby, memono, spkno, incstatus;

//	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("MMM YYYY");
	private SimpleDateFormat datenormalFormatter = new SimpleDateFormat("dd-MM-YYYY");

	@Wire
	private Combobox cbIncomingstat;
	@Wire
	private Grid grid;
	@Wire
	private Label cardTitle;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("argid") String argid)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		this.productgroup = argid;
		doReset();

		if (grid != null) {
			if (oUser.getMbranch().getBranchlevel() == 1) {
				grid.setRowRenderer(new RowRenderer<TReportPlan>() {
					@Override
					public void render(Row row, final TReportPlan data, int index) throws Exception {
						row.getChildren().add(new Label(String.valueOf(index + 1)));
						row.getChildren().add(new Label(data.getMemono()));
						row.getChildren().add(new Label(datenormalFormatter.format(data.getMemodate())));
						row.getChildren().add(new Label(data.getMproducttype().getProducttype()));
						row.getChildren().add(new Label(data.getAnggaran().toString()));
						row.getChildren().add(new Label(data.getTotalqty().toString()));
						row.getChildren().add(new Label(data.getTotalprocess().toString()));
						row.getChildren().add(
								new Label(data.getStatus() != null ? AppData.getStatusLabel(data.getStatus()) : "-"));
						row.getChildren().add(new Label(data.getSpkno()));
						row.getChildren().add(new Label(datenormalFormatter.format(data.getSpkdate())));
						row.getChildren().add(new Label(data.getMbranch().getBranchname()));
						row.getChildren().add(new Label(data.getHarga().toString()));
						row.getChildren().add(new Label(data.getPrefix()));
						row.getChildren().add(new Label(data.getItemstartno().toString()));
						row.getChildren().add(new Label(data.getMsupplier().getSuppliername()));
						row.getChildren()
								.add(new Label(data.getIncomingused() != null ? "Telah Digunakan" : "Belum Digunakan"));

					}
				});
			}
		}
	}

	@Command
	@NotifyChange("*")
	public void doReset() {
		if (oUser.getMbranch().getBranchlevel() == 1) {
			memono = "";
			spkno = "";
			incstatus = "";
		}
		doSearch();
	}

	@Command
	@NotifyChange("*")
	public void doSearch() {
		try {
			if (oUser != null) {
				if (oUser.getMbranch().getBranchlevel() == 1) {
					cardTitle.setValue("Daftar Planning Produk " + AppData.getProductgroupLabel(productgroup));
					
					filter = "tincoming.productgroup = '" + productgroup + "'";
					if (memono.trim() != "" || !"".equals(memono))
						filter += " and tplan.memono = '" + memono + "'";

					if (spkno.trim() != "" || !"".equals(spkno))
						filter += " and tincoming.spkno = '" + spkno + "'";

					if (incstatus.trim() == "1" || "1".equals(incstatus))
						filter += " and tplan.incomingused = 1";
					else if (incstatus.trim() == "2" || "2".equals(incstatus))
						filter += " and tplan.incomingused is null";
					else
						filter += " and (tplan.incomingused = 1 or tplan.incomingused is null)";

					orderby = "tplan.memono, tplan.memodate";
				}
				refreshModel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("*")
	public void refreshModel() {
		try {
			if (oUser.getMbranch().getBranchlevel() == 1) {
				List<TReportPlan> objList = new ArrayList<>();
				objList = oDao.listReportplan(filter, orderby);
				grid.setModel(new ListModelList<>(objList));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getMemono() {
		return memono;
	}

	public void setMemono(String memono) {
		this.memono = memono;
	}

	public String getSpkno() {
		return spkno;
	}

	public void setSpkno(String spkno) {
		this.spkno = spkno;
	}

	public String getIncstatus() {
		return incstatus;
	}

	public void setIncstatus(String incstatus) {
		this.incstatus = incstatus;
	}
}