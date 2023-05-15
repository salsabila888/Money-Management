package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.A;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;
import org.zkoss.zul.event.PagingEvent;

import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Torderdoc;
import com.sdd.caption.model.TorderdocListModel;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;
import com.sdd.utils.SysUtils;

public class OrderDocumentVm {

	private Session zkSession = Sessions.getCurrent();

	private TorderdocListModel model;

	private int pageStartNumber;
	private int pageTotalSize;
	private boolean needsPageUpdate;
	private String filter;
	private String orderby;
	private String arg;

	private Torder obj;
	private Torderdoc objdoc;

	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat datetimelocalFormatter = new SimpleDateFormat("dd MMMMM yyyy HH:mm");

	@Wire
	private Window winOrderDoc;
	@Wire
	private Paging paging;
	@Wire
	private Grid gridDocument;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("obj") Torder torder,
			@ExecutionArgParam("arg") String arg) throws Exception {
		Selectors.wireComponents(view, this, false);
		if (torder != null) {
			obj = torder;
			doSearch();
		}

		paging.addEventListener("onPaging", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				PagingEvent pe = (PagingEvent) event;
				pageStartNumber = pe.getActivePage();
				refreshModel(pageStartNumber);
			}
		});

		if (gridDocument != null) {
			gridDocument.setRowRenderer(new RowRenderer<Torderdoc>() {
				@Override
				public void render(Row row, Torderdoc data, int index) throws Exception {
					row.getChildren().add(new Label(String.valueOf(index + 1)));
					A a = new A(data.getDocfileori());
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						@Override
						public void onEvent(Event event) throws Exception {
							Sessions.getCurrent().setAttribute("reportPath",
									AppUtils.FILES_ROOT_PATH + AppUtils.MEMO_PATH + data.getDocfileid());
							Executions.getCurrent().sendRedirect("/view/docviewer.zul", "_blank");
						}
					});
					row.getChildren().add(a);
				}
			});
		}
	}

	@NotifyChange("pageTotalSize")
	public void refreshModel(int activePage) {
		orderby = "torderdocpk";
		paging.setPageSize(SysUtils.PAGESIZE);
		model = new TorderdocListModel(activePage, SysUtils.PAGESIZE, filter, orderby);
		if (needsPageUpdate) {
			pageTotalSize = model.getTotalSize(filter);
			needsPageUpdate = false;
		}
		paging.setTotalSize(pageTotalSize);
		gridDocument.setModel(model);
	}

	@Command
	@NotifyChange("pageTotalSize")
	public void doSearch() {
		filter = "torderfk = " + obj.getTorderpk();
		needsPageUpdate = true;
		paging.setActivePage(0);
		pageStartNumber = 0;
		refreshModel(pageStartNumber);
	}

	@Command
	@NotifyChange("*")
	public void doReset() throws ParseException {
		doSearch();
	}

	public Torder getObj() {
		return obj;
	}

	public void setObj(Torder obj) {
		this.obj = obj;
	}

	public int getPageTotalSize() {
		return pageTotalSize;
	}

	public void setPageTotalSize(int pageTotalSize) {
		this.pageTotalSize = pageTotalSize;
	}

	public Torderdoc getObjdoc() {
		return objdoc;
	}

	public void setObjdoc(Torderdoc objdoc) {
		this.objdoc = objdoc;
	}

}
