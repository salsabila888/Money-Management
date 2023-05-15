package com.sdd.caption.viewmodel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.impl.ParseException;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Tembossproduct;
import com.sdd.caption.domain.Mpersovendor;
import com.sdd.caption.domain.Tperso;
import com.sdd.caption.utils.AppData;

public class PersoManifestGenerateVm {

	private Tperso objForm;
	private Integer totaldata;
	private Integer totalselected;
	private Boolean isSaved;

	@Wire
	private Window winPersomanifests;
	@Wire
	private Combobox cbPersovendor;
	@Wire
	private Grid grid;
	@Wire
	private Label totalRecord, totalData;
	@Wire
	private Row rowVendor;
	@Wire
	private Radio rbInternal, rbExternal;

	@AfterCompose
	@NotifyChange("*")
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("mapData") Map<String, Tembossproduct> mapData) throws ParseException {
		Selectors.wireComponents(view, this, false);

		totalselected = mapData.size();
		totaldata = 0;

		for (Entry<String, Tembossproduct> entry : mapData.entrySet()) {
			Tembossproduct obj = entry.getValue();
			totaldata += obj.getTotaldata();
		}

		objForm = new Tperso();
		objForm.setPersostarttime(new Date());
		rowVendor.setVisible(false);
	}

	@Command
	public void doPersotypeSelected() {
		if (rbInternal.isChecked()) {
			rowVendor.setVisible(false);
			cbPersovendor.setValue(null);
			objForm.setMpersovendor(null);
		} else {
			rowVendor.setVisible(true);
		}
	}

	@Command
	public void doSave() {
		isSaved = true;
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("isSaved", isSaved);
			map.put("objForm", objForm);
			Event closeEvent = new Event("onClose", winPersomanifests, map);
			Events.postEvent(closeEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {
				Mpersovendor mpersovendor = (Mpersovendor) ctx.getProperties("mpersovendor")[0].getValue();

				if (mpersovendor == null)
					this.addInvalidMessage(ctx, "mpersovendor", Labels.getLabel("common.validator.empty"));
			}
		};
	}

	public ListModelList<Mpersovendor> getMpersovendormodel() {
		ListModelList<Mpersovendor> lm = null;
		try {
			lm = new ListModelList<Mpersovendor>(AppData.getMpersovendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Tperso getObjForm() {
		return objForm;
	}

	public void setObjForm(Tperso objForm) {
		this.objForm = objForm;
	}

	public Integer getTotaldata() {
		return totaldata;
	}

	public Integer getTotalselected() {
		return totalselected;
	}

	public void setTotaldata(Integer totaldata) {
		this.totaldata = totaldata;
	}

	public void setTotalselected(Integer totalselected) {
		this.totalselected = totalselected;
	}

}
