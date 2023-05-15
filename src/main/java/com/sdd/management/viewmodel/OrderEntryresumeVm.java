package com.sdd.caption.viewmodel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;

import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Torder;
import com.sdd.caption.domain.Tordermemo;
import com.sdd.caption.utils.AppData;
import com.sdd.caption.utils.AppUtils;

public class OrderEntryresumeVm {
	private org.zkoss.zk.ui.Session zkSession = Sessions.getCurrent();
	private String arg, lborderid, lborderdate, lborderbranch, lbkcpoutlet, lbwilayah, lbjenisproduk, lbjumlah,
			lbalasanorder;

	private Muser oUser;
	private Torder objOrder;
	private Tordermemo objMemo;
	private String orderpinpadtype;
	private BigDecimal totalqty = new BigDecimal(0);
	private Integer branchlevel;

	@Wire
	private Window winOER;
	@Wire
	private Row rowKCPoutlet, rowJenis, rowTipepinpad, rowTotal;
	@Wire
	private Button btnCreate, btnCancel;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view,
			@ExecutionArgParam("content") Div divContent, @ExecutionArgParam("arg") String arg,
			@ExecutionArgParam("objorder") Torder objorderp, @ExecutionArgParam("objmemo") Tordermemo objmemop,
			@ExecutionArgParam("isEdit") String isEdit) {
		Selectors.wireComponents(view, this, false);
		oUser = (Muser) zkSession.getAttribute("oUser");
		branchlevel = oUser.getMbranch().getBranchlevel();
		this.objOrder = objorderp;
		this.objMemo = objmemop;
		this.arg = arg;

		if (objOrder.getMproduct() != null) {
			totalqty = new BigDecimal(
					objOrder.getItemqty() * objOrder.getMproduct().getMproducttype().getProductunitqty());
			rowJenis.setVisible(true);
		}
		if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
			if (objorderp.getOrderpinpadtype() != null && objorderp.getOrderpinpadtype().trim().length() > 0) {
				orderpinpadtype = AppData.getPinpadtypeLabel(objorderp.getOrderpinpadtype());
				rowTipepinpad.setVisible(true);
			}
		} else if (arg.equals(AppUtils.PRODUCTGROUP_DOCUMENT)) {
			rowTotal.setVisible(true);
		}

		if (isEdit != null) {
			btnCreate.setVisible(false);
			btnCancel.setVisible(false);
		}

	}

	@Command
	public void doCreate() {
		Div divRoot = (Div) winOER.getParent();
		divRoot.getChildren().clear();
		Map<String, String> map = new HashMap<String, String>();
		map.put("arg", arg);
		if (arg.equals(AppUtils.PRODUCTGROUP_PINPAD)) {
			if (branchlevel == 1) {
				Executions.createComponents("/view/order/orderentry.zul", divRoot, map);
			} else 
				Executions.createComponents("/view/order/orderentrypinpad.zul", divRoot, map);
		} else 
			Executions.createComponents("/view/order/orderentry.zul", divRoot, map);
	}

	@Command
	public void doList() {
		Div divRoot = (Div) winOER.getParent();
		divRoot.getChildren().clear();
		Map<String, String> map = new HashMap<String, String>();
		map.put("arg", arg);
		if (oUser.getMbranch().getBranchlevel() == 1) {
			map.put("isOPR", "Y");
		}
		Executions.createComponents("/view/order/orderlist.zul", divRoot, map);
	}

	public Torder getObjOrder() {
		return objOrder;
	}

	public Tordermemo getObjMemo() {
		return objMemo;
	}

	public void setObjOrder(Torder objOrder) {
		this.objOrder = objOrder;
	}

	public void setObjMemo(Tordermemo objMemo) {
		this.objMemo = objMemo;
	}

	public String getLborderid() {
		return lborderid;
	}

	public void setLborderid(String lborderid) {
		this.lborderid = lborderid;
	}

	public String getLborderdate() {
		return lborderdate;
	}

	public void setLborderdate(String lborderdate) {
		this.lborderdate = lborderdate;
	}

	public String getLborderbranch() {
		return lborderbranch;
	}

	public void setLborderbranch(String lborderbranch) {
		this.lborderbranch = lborderbranch;
	}

	public String getLbkcpoutlet() {
		return lbkcpoutlet;
	}

	public void setLbkcpoutlet(String lbkcpoutlet) {
		this.lbkcpoutlet = lbkcpoutlet;
	}

	public String getLbwilayah() {
		return lbwilayah;
	}

	public void setLbwilayah(String lbwilayah) {
		this.lbwilayah = lbwilayah;
	}

	public String getLbjenisproduk() {
		return lbjenisproduk;
	}

	public void setLbjenisproduk(String lbjenisproduk) {
		this.lbjenisproduk = lbjenisproduk;
	}

	public String getLbjumlah() {
		return lbjumlah;
	}

	public void setLbjumlah(String lbjumlah) {
		this.lbjumlah = lbjumlah;
	}

	public String getLbalasanorder() {
		return lbalasanorder;
	}

	public void setLbalasanorder(String lbalasanorder) {
		this.lbalasanorder = lbalasanorder;
	}

	public String getOrderpinpadtype() {
		return orderpinpadtype;
	}

	public void setOrderpinpadtype(String orderpinpadtype) {
		this.orderpinpadtype = orderpinpadtype;
	}

	public BigDecimal getTotalqty() {
		return totalqty;
	}

	public void setTotalqty(BigDecimal totalqty) {
		this.totalqty = totalqty;
	}
}
