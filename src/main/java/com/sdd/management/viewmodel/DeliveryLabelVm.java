package com.sdd.caption.viewmodel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tdeliverycourier;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class DeliveryLabelVm {

	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private int pageTotalSize;

	private Tdeliverycourier obj;
	private String branchname;
	private String producttype;
	private String productcode;
	private String productname;
	private Integer totalselected;
	private Integer totaldataselected;
	private Boolean isSaved;
	private String productgroup;
	private Map<Integer, Tdelivery> mapData;
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private String islabelsurat;

	@Wire
	private Window winDeliverylabel;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("obj") Tdeliverycourier tdeliverycourier,
			@ExecutionArgParam("mapData") Map<Integer, Tdelivery> mapData,
			@ExecutionArgParam("productgroup") String productgroup,
			@ExecutionArgParam("islabelsurat") final String islabelsurat) throws ParseException {
		Selectors.wireComponents(view, this, false);
		obj = tdeliverycourier;
		this.productgroup = productgroup;
		this.mapData = mapData;
		this.islabelsurat = islabelsurat;
		List<Tdelivery> objList = new ArrayList<>();
		for (Entry<Integer, Tdelivery> entry : mapData.entrySet()) {
			Tdelivery data = entry.getValue();
			objList.add(data);
		}

		grid.setModel(new ListModelList<>(objList));
		grid.setRowRenderer(new RowRenderer<Tdelivery>() {

			@Override
			public void render(Row row, final Tdelivery data, int index) throws Exception {
				row.getChildren().add(new Label(String.valueOf(index + 1)));
				row.getChildren().add(new Label(data.getDlvid()));
				row.getChildren().add(new Label(AppData.getProductgroupLabel(data.getProductgroup())));
				row.getChildren().add(new Label(dateLocalFormatter.format(data.getProcesstime())));
				row.getChildren().add(new Label(data.getMbranch().getBranchid()));
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(String.valueOf(data.getTotaldata())));
				row.getChildren().add(new Label(data.getLettertype()));
				row.getChildren().add(new Label(data.getMcouriervendor().getVendorcode()));
				row.getChildren().add(new Label(AppData.getStatusLabel(data.getStatus())));

				Spinner spin = new Spinner();
				spin.setConstraint("no empty,min 1 max 100: between 1 to 100");
				spin.setAttribute("obj", data);
				spin.setValue(1);
				spin.setMaxlength(3);
				spin.setCols(3);
				row.appendChild(spin);
			}
		});
	}

	@Command
	public void doPrintLabel() {
		try {
			List<Tdelivery> objList = new ArrayList<>();
			List<Row> components = grid.getRows().getChildren();
			for (Row comp : components) {
				Spinner spin = (Spinner) comp.getChildren().get(10);
				for (int i = 1; i <= spin.getValue(); i++) {
					objList.add((Tdelivery) spin.getAttribute("obj"));
				}
			}

			/*
			 * zkSession.setAttribute("objList", objList);
			 * zkSession.setAttribute("reportPath",
			 * Executions.getCurrent().getDesktop().getWebApp().getRealPath(SysUtils.
			 * JASPER_PATH + "/labelpaket.jasper"));
			 * Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
			 */

			Collections.sort(objList, Tdelivery.branchidComparator);
			Map<String, String> parameters = new HashMap<>();

			if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER))
				parameters.put("PAKETTYPE", "SR");
			else if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD))
				parameters.put("PAKETTYPE", "BC - BNICARD");
			zkSession.setAttribute("objList", objList);
			zkSession.setAttribute("parameters", parameters);
			if (islabelsurat != null && islabelsurat.equals("Y")) {
				if (productgroup.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
					zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(SysUtils.JASPER_PATH + "/labelnosuratdoc.jasper"));
				else
					zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(SysUtils.JASPER_PATH + "/labelnosurat.jasper"));
			} else {
				if (productgroup.equals(AppUtils.PRODUCTGROUP_PINMAILER))
					zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(SysUtils.JASPER_PATH + "/labelpinmailerdlv.jasper"));
				else if (productgroup.equals(AppUtils.PRODUCTGROUP_CARD))
					zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(SysUtils.JASPER_PATH + "/labeldlv.jasper"));
				else if (productgroup.equals(AppUtils.PRODUCTGROUP_TOKEN))
					zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(SysUtils.JASPER_PATH + "/labeldlv.jasper"));
				else if (productgroup.equals(AppUtils.PRODUCTGROUP_PINPAD))
					zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp()
							.getRealPath(SysUtils.JASPER_PATH + "/labeldlv.jasper"));
			}

			Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Command
	public void doClose() {
		Event closeEvent = new Event("onClose", winDeliverylabel, isSaved);
		Events.postEvent(closeEvent);
	}

	public Tdeliverycourier getObj() {
		return obj;
	}

	public void setObj(Tdeliverycourier obj) {
		this.obj = obj;
	}

	public String getBranchname() {
		return branchname;
	}

	public void setBranchname(String branchname) {
		this.branchname = branchname;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

	public String getProductgroup() {
		return productgroup;
	}

	public void setProductgroup(String productgroup) {
		this.productgroup = productgroup;
	}

}
