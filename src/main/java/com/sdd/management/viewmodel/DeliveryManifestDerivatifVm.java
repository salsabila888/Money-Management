package com.sdd.caption.viewmodel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
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
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.sdd.caption.dao.TcounterengineDAO;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Mletter;
import com.sdd.caption.domain.Mlettertype;
import com.sdd.caption.domain.Tdelivery;
import com.sdd.caption.domain.Tpaketdata;
import com.sdd.caption.domain.Vbranchdelivery;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class DeliveryManifestDerivatifVm {
	
	private Tdelivery objForm;
	private Integer totaldata;
	private Integer totalselected;
	private Mletter mletter;
	private Mlettertype mlettertype;
	private Mcouriervendor mcouriervendor;
	private String type;
	private String dlvid;
	private String kurir;
	
	@Wire
	private Window winDeliverymanifests;
	@Wire
	private Combobox cbCouriervendor;
	@Wire
	private Grid grid;
	@Wire
	private Row rowcbLetter;
	@Wire
	private Row rowtbLetter;
	@Wire
	private Combobox cbLetter;
	@Wire
	private Textbox tbLetter;
	@Wire
	private Row rowcbEkpedisi;

	@AfterCompose
	@NotifyChange("*")	
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view)
			throws ParseException {
		Selectors.wireComponents(view, this, false);
		type = "A";
		kurir = "D";
		dlvid = "";
		objForm = new Tdelivery();
		objForm.setProcesstime(new Date());
	}	
	
	@Command
	public void doLetterNoType(@BindingParam("type") String type) {
		if (type.equals("A")) {
			rowcbLetter.setVisible(true);
			rowtbLetter.setVisible(false);
		} else if (type.equals("M")) {
			rowcbLetter.setVisible(false);
			rowtbLetter.setVisible(true);
		} 
	}
	
	@Command
	public void doEkspedisitypeSelected(@BindingParam("kurir") String kurir) {
		if (kurir.equals("D")) {
			rowcbEkpedisi.setVisible(false);
		} else if (kurir.equals("M")) {
			rowcbEkpedisi.setVisible(true);
		} 
	}
	
	@Command
	public void doSave() {
		Map<String, Object> map = new HashMap<>();
		map.put("obj", objForm);
		map.put("type", type);
		map.put("kurir", kurir);
		map.put("prefix", mletter);
		map.put("lettertype", mlettertype);
		Event closeEvent = new Event( "onClose", winDeliverymanifests, map);
		Events.postEvent(closeEvent);		
	}
	
	public Validator getValidator() {
		return new AbstractValidator() {

			@Override
			public void validate(ValidationContext ctx) {	
				String dlvid = (String) ctx
						.getProperties("dlvid")[0].getValue();
				Mcouriervendor mcouriervendor = (Mcouriervendor) ctx
						.getProperties("mcouriervendor")[0].getValue();
				
				if(kurir.equals("M"))
					if (mcouriervendor == null) 
						this.addInvalidMessage(ctx, "mcouriervendor",
								Labels.getLabel("common.validator.empty"));	
				if (type.equals("A"))
					if (mletter == null) 
						this.addInvalidMessage(ctx, "mletter",
							Labels.getLabel("common.validator.empty"));	
				if (type.equals("M"))
					if (dlvid == null || dlvid.trim().length() == 0) 
						this.addInvalidMessage(ctx, "dlvid",
							Labels.getLabel("common.validator.empty"));	
				if (mlettertype == null) 
					this.addInvalidMessage(ctx, "mlettertype",
							Labels.getLabel("common.validator.empty"));		
			}
		};
	}
	
	public ListModelList<Mletter> getMletterModel() {
		ListModelList<Mletter> lm = null;
		try {
			lm = new ListModelList<Mletter>(AppData.getMletter("productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}	
	
	public ListModelList<Mlettertype> getMlettertypeModel() {
		ListModelList<Mlettertype> lm = null;
		try {
			lm = new ListModelList<Mlettertype>(AppData.getMlettertype("productgroup = '" + AppUtils.PRODUCTGROUP_CARD + "'"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}
	
	public ListModelList<Mcouriervendor> getMcouriervendorModel() {
		ListModelList<Mcouriervendor> lm = null;
		try {
			lm = new ListModelList<Mcouriervendor>(AppData.getMcouriervendor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lm;
	}

	public Tdelivery getObjForm() {
		return objForm;
	}

	public void setObjForm(Tdelivery objForm) {
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

	public Mletter getMletter() {
		return mletter;
	}

	public void setMletter(Mletter mletter) {
		this.mletter = mletter;
	}

	public Mlettertype getMlettertype() {
		return mlettertype;
	}

	public void setMlettertype(Mlettertype mlettertype) {
		this.mlettertype = mlettertype;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDlvid() {
		return dlvid;
	}

	public void setDlvid(String dlvid) {
		this.dlvid = dlvid;
	}

	public Mcouriervendor getMcouriervendor() {
		return mcouriervendor;
	}

	public void setMcouriervendor(Mcouriervendor mcouriervendor) {
		this.mcouriervendor = mcouriervendor;
	}

	public String getKurir() {
		return kurir;
	}

	public void setKurir(String kurir) {
		this.kurir = kurir;
	}

	
}
