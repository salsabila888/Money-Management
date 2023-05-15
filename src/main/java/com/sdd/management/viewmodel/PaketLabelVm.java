package com.sdd.caption.viewmodel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sdd.caption.dao.TembossdataDAO;
import com.sdd.caption.dao.TpaketDAO;
import com.sdd.caption.dao.TpaketdataDAO;
import com.sdd.caption.domain.Msysparam;
import com.sdd.caption.domain.Tembossdata;
import com.sdd.caption.domain.Tpaket;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.model.TpaketdataListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;
import com.sdd.utils.db.StoreHibernateUtil;

public class PaketLabelVm {
	
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();

	private TpaketdataListModel model;
		
	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	
	private TpaketdataDAO tpaketdataDao = new TpaketdataDAO();
	private TembossdataDAO torderdataDao = new TembossdataDAO();
	private TpaketDAO oDao = new TpaketDAO();

	private Tpaket obj;
	private String branchname;
	private String producttype;
	private String productcode;
	private String productname;
	private Integer totalselected;
	private Integer totaldataselected;	
	private Boolean isSaved;
	private Map<Integer, Tpaketdata> mapData;
	private Map<Integer, Tpaket> map;
	private List<Tpaketdata> objSelected = new ArrayList<Tpaketdata>();
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat dateLocalFormatter = new SimpleDateFormat("dd-MM-yyyy");

	@Wire
	private Window winPaketlabel;
	@Wire
	private Paging paging;
	@Wire
	private Grid grid;
	@Wire
	private Checkbox chkAll;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Tpaket tpaket, 
			@ExecutionArgParam("mapData") Map<Integer, Tpaketdata> mapData)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		obj = tpaket;
		this.mapData = mapData;
		List<Tpaketdata> objList = new ArrayList<>();
		for (Entry<Integer, Tpaketdata> entry: mapData.entrySet()) {
			Tpaketdata data = entry.getValue();
			objList.add(data);
		}	
		
		grid.setModel(new ListModelList<>(objList));
		grid.setRowRenderer(new RowRenderer<Tpaketdata>() {

			@Override
			public void render(Row row, final Tpaketdata data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf(index + 1)));	
				row.getChildren().add(new Label(data.getNopaket()));					
				row.getChildren().add(new Label(data.getTpaket().getMproduct().getMproducttype().getProducttype()));
				row.getChildren().add(new Label(data.getTpaket().getMproduct().getProductcode()));
				row.getChildren().add(new Label(data.getTpaket().getMproduct().getProductname()));	
				row.getChildren().add(new Label(data.getMbranch().getBranchname()));
				row.getChildren().add(new Label(data.getOrderdate() != null ? datelocalFormatter.format(data.getOrderdate()) : ""));		
				
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
				List<Tpaketdata> objList = new ArrayList<>();								
				List<Row> components = grid.getRows().getChildren();
				for (Row comp : components) {
					Spinner spin = (Spinner) comp.getChildren().get(7);
					for (int i=1; i<=spin.getValue(); i++) {
						objList.add((Tpaketdata) spin.getAttribute("obj"));
					}
				}
				Collections.sort(objList, Tpaketdata.branchidComparator);
				zkSession.setAttribute("objList", objList);
				zkSession.setAttribute("reportPath", Executions.getCurrent().getDesktop().getWebApp().getRealPath(SysUtils.JASPER_PATH + "/paketlabel.jasper"));
				Executions.getCurrent().sendRedirect("/view/jasperViewer.zul", "_blank");
			} catch (Exception e) {
				e.printStackTrace();
			}	
	}
	
	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "tpaketfk = " + obj.getTpaketpk();
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event( "onClose", winPaketlabel, isSaved);
		Events.postEvent(closeEvent);
	}
	
	
	public Tpaket getObj() {
		return obj;
	}

	public void setObj(Tpaket obj) {
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

}
