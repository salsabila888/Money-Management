package com.sdd.caption.viewmodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Tembossproduct;

public class PersoProductSelectedVm {
	
	private SimpleDateFormat datelocalFormatter = new SimpleDateFormat("dd-MM-yyyy");
	
	private Integer totalselected;
	private Integer totaldataselected;
	
	@Wire
	private Window winData;
	@Wire
	private Grid grid;
	
	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view, @ExecutionArgParam("mapData") Map<Integer, Tembossproduct> mapData, 
			@ExecutionArgParam("totalselected") Integer totalselected, @ExecutionArgParam("totaldataselected") Integer totaldataselected) 
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		this.totalselected = totalselected;
		this.totaldataselected = totaldataselected;
		
		List<Tembossproduct> objList = new ArrayList<>();
		for (Entry<Integer, Tembossproduct> entry: mapData.entrySet()) {
			Tembossproduct obj = entry.getValue();
			objList.add(obj);
		}		
		grid.setModel(new ListModelList<>(objList));
		grid.setRowRenderer(new RowRenderer<Tembossproduct>() {

			@Override
			public void render(Row row, final Tembossproduct data, int index)
					throws Exception {
				row.getChildren()
						.add(new Label(
								String.valueOf(index + 1)));				
				row.getChildren().add(new Label(data.getTembossfile().getFilename()));
				row.getChildren().add(new Label(data.getMproduct().getMproducttype().getProducttype()));
				row.getChildren().add(new Label(data.getProductcode()));
				row.getChildren().add(new Label(data.getMproduct().getProductname()));	
				row.getChildren().add(new Label(data.getOrderdate() != null ? datelocalFormatter.format(data.getOrderdate()) : ""));
				row.getChildren().add(new Label(data.getTotaldata() != null ? NumberFormat.getInstance().format(data.getTotaldata()) : "0"));				
			}
		});	
	}
	
	@Command
	public void doClose() {
		Event closeEvent = new Event( "onClose", winData, null);
		Events.postEvent(closeEvent);
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public Integer getTotaldataselected() {
		return totaldataselected;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

	public void setTotaldataselected(Integer totaldataselected) {
		this.totaldataselected = totaldataselected;
	}

}
