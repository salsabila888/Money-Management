package com.sdd.caption.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zhtml.H3;
import org.zkoss.zhtml.Hr;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TmissingbranchDAO;
import com.sdd.caption.dao.TmissingproductDAO;
import com.sdd.caption.domain.Tmissingbranch;
import com.sdd.caption.domain.Tmissingproduct;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Vmissingbranch;
import com.sdd.caption.domain.Vmissingproduct;

public class MissingBranchProductVm {

	private int totalrecord;
	private Torder objForm;
	private TmissingproductDAO tmissingproductDao = new TmissingproductDAO();
	private TmissingbranchDAO tmissingbranchDao = new TmissingbranchDAO();
	private List<Tmissingproduct> missingproductList = new ArrayList<>();
	private List<Tmissingbranch> missingbranchList = new ArrayList<>();
	private List<Vmissingbranch> vbranchList = new ArrayList<>();
	private List<Vmissingproduct> vproductList = new ArrayList<>();

	private String unregister;

	@Wire
	private Caption caption, close;
	@Wire
	private H3 h3produk, h3cabang;
	@Wire
	private Hr hr;
	@Wire
	private Grid grid;
	@Wire
	private Groupbox gbFile;
	@Wire
	private Window winBranchData;
	@Wire
	private Column isinstant, code, branchid;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("missingproductList") List<Tmissingproduct> missingproductList,
			@ExecutionArgParam("missingbranchList") List<Tmissingbranch> missingbranchList,
			@ExecutionArgParam("isMissingProduct") final String isMissingProduct,
			@ExecutionArgParam("isMissingBranch") final String isMissingBranch,
			@ExecutionArgParam("objForm") final Torder objForm) throws Exception {
		Selectors.wireComponents(view, this, false);

		unregister = arg;
		if (unregister == null) {
			this.objForm = objForm;
			h3produk.setVisible(false);
			h3cabang.setVisible(false);
			hr.setVisible(false);
			if (isMissingBranch != null && isMissingBranch.equals("Y")) {
				this.missingbranchList = missingbranchList;
				caption.setLabel("Cabang Belum Terdaftar");
				isinstant.setVisible(false);
				code.setVisible(false);
				doRenderDataBranch();
				totalrecord = missingbranchList.size();
			} else if (isMissingProduct != null && isMissingProduct.equals("Y")) {
				this.missingproductList = missingproductList;
				caption.setLabel("Produk Belum Terdaftar");
				branchid.setVisible(false);
				doRenderDataProduct();
				totalrecord = missingproductList.size();
			}
		} else {
			if (unregister.trim().equals("MP")) {
				h3cabang.setVisible(false);
				close.setVisible(false);
				gbFile.setVisible(false);
				branchid.setVisible(false);
				vproductList = tmissingproductDao.listGroupby("0=0");
				totalrecord = vproductList.size();
				doRenderVproduct();
			} else if (unregister.trim().equals("MB")) {
				close.setVisible(false);
				h3produk.setVisible(false);
				gbFile.setVisible(false);
				isinstant.setVisible(false);
				code.setVisible(false);
				vbranchList = tmissingbranchDao.listGroupby("0=0");
				totalrecord = vbranchList.size();
				doRenderVbranch();
			}
		}

	}

	@NotifyChange("grid")
	private void doRenderVproduct() {
		if (vproductList.size() > 0) {
			Rows rows = new Rows();
			int index = 1;
			for (Vmissingproduct obj : vproductList) {
				Row row = new Row();
				Label lbl = new Label();
				lbl.setValue(String.valueOf(index));
				row.appendChild(lbl);
				lbl = new Label();
				lbl.setValue("");
				row.appendChild(lbl);
				lbl = new Label();
				lbl.setValue(obj.getProductcode());
				row.appendChild(lbl);
				lbl = new Label();
				lbl.setValue("");
				row.appendChild(lbl);
				lbl = new Label();
				if (obj.getIsinstant().trim().equals("Y"))
					lbl.setValue("Ya");
				if (obj.getIsinstant().trim().equals("N"))
					lbl.setValue("Tidak");
				row.appendChild(lbl);
				lbl = new Label();
				lbl.setValue(String.valueOf(obj.getTotaldata()));
				row.appendChild(lbl);
				/*
				 * Button btndetail = new Button("Detail"); btndetail.setAutodisable("self");
				 * btndetail.setClass("btn btn-default btn-sm");
				 * btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				 * 
				 * @Override public void onEvent(Event event) throws Exception { Map<String,
				 * Object> map = new HashMap<>(); map.put("objProduct", obj); map.put("isView",
				 * "P");
				 * 
				 * Window win = (Window)
				 * Executions.createComponents("/view/missingbranchproduct.zul", null, map);
				 * win.setWidth("60%"); win.setClosable(true); win.doModal(); } });
				 * row.getChildren().add(btndetail);
				 */

				rows.getChildren().add(row);
				index++;
			}
			grid.appendChild(rows);
		}
	}

	@NotifyChange("grid")
	private void doRenderVbranch() {
		try {
			if (vbranchList.size() > 0) {
				Rows rows = new Rows();
				int index = 1;
				for (Vmissingbranch obj : vbranchList) {
					Row row = new Row();
					Label lbl = new Label();
					lbl.setValue(String.valueOf(index));
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(obj.getBranchid());
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue("");
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue("");
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue("");
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(String.valueOf(obj.getTotaldata()));
					row.appendChild(lbl);

					/*
					 * Button btndetail = new Button("Detail"); btndetail.setAutodisable("self");
					 * btndetail.setClass("btn btn-default btn-sm");
					 * btndetail.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					 * 
					 * @Override public void onEvent(Event event) throws Exception { Map<String,
					 * Object> map = new HashMap<>(); map.put("objBranch", obj); map.put("isView",
					 * "B");
					 * 
					 * Window win = (Window)
					 * Executions.createComponents("/view/missingbranchproduct.zul", null, map);
					 * win.setWidth("60%"); win.setClosable(true); win.doModal(); } });
					 * row.getChildren().add(btndetail);
					 */

					rows.getChildren().add(row);
					index++;
				}
				grid.appendChild(rows);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@NotifyChange("grid")
	private void doRenderDataProduct() {
		if (missingproductList.size() > 0) {
			Rows rows = new Rows();
			int index = 1;
			for (Tmissingproduct obj : missingproductList) {
				Row row = new Row();
				Label lbl = new Label();
				lbl.setValue(String.valueOf(index));
				row.appendChild(lbl);
				lbl = new Label();
				lbl.setValue(obj.getProductcode());
				row.appendChild(lbl);
				lbl = new Label();
				lbl.setValue("");
				row.appendChild(lbl);
				/*lbl = new Label();
				lbl.setValue(String.valueOf(dateLocalFormatter.format(obj.getOrderdate())));
				row.appendChild(lbl);*/
				lbl = new Label();
				if (obj.getIsinstant().trim().equals("Y"))
					lbl.setValue("Ya");
				if (obj.getIsinstant().trim().equals("N"))
					lbl.setValue("Tidak");
				row.appendChild(lbl);
				lbl = new Label();
				lbl.setValue(String.valueOf(obj.getTotaldata()));
				row.appendChild(lbl);
				lbl = new Label();
				lbl.setValue("");
				row.appendChild(lbl);

				rows.getChildren().add(row);
				index++;
			}
			grid.appendChild(rows);
		}
	}

	@NotifyChange("grid")
	private void doRenderDataBranch() {
		try {
			if (missingbranchList.size() > 0) {
				Rows rows = new Rows();
				int index = 1;
				for (Tmissingbranch obj : missingbranchList) {
					Row row = new Row();
					Label lbl = new Label();
					lbl.setValue(String.valueOf(index));
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(obj.getBranchid());
					row.appendChild(lbl);
					/*lbl = new Label();
					lbl.setValue(obj.getTembossbranch().getTembossproduct().getProductcode());
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(String.valueOf(dateLocalFormatter.format(obj.getOrderdate())));
					row.appendChild(lbl);*/
					lbl = new Label();
					lbl.setValue("");
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue(String.valueOf(obj.getTotaldata()));
					row.appendChild(lbl);
					lbl = new Label();
					lbl.setValue("");
					row.appendChild(lbl);

					rows.getChildren().add(row);
					index++;
				}
				grid.appendChild(rows);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", this.winBranchData, null);
		Events.postEvent(closeEvent);
		winBranchData.detach();
	}

	public Torder getObjForm() {
		return objForm;
	}

	public void setObjForm(Torder objForm) {
		this.objForm = objForm;
	}

	public int getTotalrecord() {
		return totalrecord;
	}

	public void setTotalrecord(int totalrecord) {
		this.totalrecord = totalrecord;
	}
}
