package com.sdd.caption.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;
import com.sdd.caption.dao.MaimDAO;
import com.sdd.caption.dao.MbankDAO;
import com.sdd.caption.dao.MbranchDAO;
import com.sdd.caption.dao.McourierDAO;
import com.sdd.caption.dao.McouriervendorDAO;
import com.sdd.caption.dao.MexpensesDAO;
import com.sdd.caption.dao.MincomeDAO;
import com.sdd.caption.dao.MletterDAO;
import com.sdd.caption.dao.MlettertypeDAO;
import com.sdd.caption.dao.MmenuDAO;
import com.sdd.caption.dao.MorgDAO;
import com.sdd.caption.dao.MoutletDAO;
import com.sdd.caption.dao.MpaymentDAO;
import com.sdd.caption.dao.MpendingreasonDAO;
import com.sdd.caption.dao.MpersovendorDAO;
import com.sdd.caption.dao.MproductDAO;
import com.sdd.caption.dao.MproductgroupDAO;
import com.sdd.caption.dao.MproductownerDAO;
import com.sdd.caption.dao.MproducttypeDAO;
import com.sdd.caption.dao.MregionDAO;
import com.sdd.caption.dao.MrepairreasonDAO;
import com.sdd.caption.dao.MreturnreasonDAO;
import com.sdd.caption.dao.MsupplierDAO;
import com.sdd.caption.dao.MuserDAO;
import com.sdd.caption.dao.MusergroupDAO;
import com.sdd.caption.dao.TbranchstockDAO;
import com.sdd.caption.domain.Maim;
import com.sdd.caption.domain.Mbank;
import com.sdd.caption.domain.Mbranch;
import com.sdd.caption.domain.Mcourier;
import com.sdd.caption.domain.Mcouriervendor;
import com.sdd.caption.domain.Mexpenses;
import com.sdd.caption.domain.Mholiday;
import com.sdd.caption.domain.Mincome;
import com.sdd.caption.domain.Mletter;
import com.sdd.caption.domain.Mlettertype;
import com.sdd.caption.domain.Mmenu;
import com.sdd.caption.domain.Morg;
import com.sdd.caption.domain.Moutlet;
import com.sdd.caption.domain.Mpayment;
import com.sdd.caption.domain.Mpendingreason;
import com.sdd.caption.domain.Mpersovendor;
import com.sdd.caption.domain.Mproduct;
import com.sdd.caption.domain.Mproductgroup;
import com.sdd.caption.domain.Mproductowner;
import com.sdd.caption.domain.Mproducttype;
import com.sdd.caption.domain.Mregion;
import com.sdd.caption.domain.Mrepairreason;
import com.sdd.caption.domain.Mreturnreason;
import com.sdd.caption.domain.Msupplier;
import com.sdd.caption.domain.Muser;
import com.sdd.caption.domain.Musergroup;
import com.sdd.caption.domain.Tbranchstock;

public class AppData {

	public static boolean isSchedulerActive = false;
	
	public static List<Musergroup> getMusergroup() throws Exception {
		List<Musergroup> list = new ArrayList<Musergroup>();
		list = new MusergroupDAO().listByFilter("0=0", "usergroupname");
		return list;
	}

	public static List<Muser> getMuser() throws Exception {
		List<Muser> list = new ArrayList<Muser>();
		list = new MuserDAO().listByFilter("0=0", "userid");
		return list;
	}
	
	public static List<Muser> getMuser(String filter) throws Exception {
		List<Muser> list = new ArrayList<Muser>();
		list = new MuserDAO().listByFilter(filter, "userid");
		return list;
	}

	public static List<Mregion> getMregion() throws Exception {
		List<Mregion> list = new ArrayList<Mregion>();
		list = new MregionDAO().listByFilter("0=0", "mregionpk");
		return list;
	}

	public static List<Msupplier> getMsupplier() throws Exception {
		List<Msupplier> list = new ArrayList<Msupplier>();
		list = new MsupplierDAO().listByFilter("0=0", "msupplierpk");
		return list;
	}

	public static List<Mbranch> getMbranch() throws Exception {
		List<Mbranch> list = new ArrayList<Mbranch>();
		list = new MbranchDAO().listByFilter("0=0", "branchid");
		return list;
	}

	public static List<Tbranchstock> getTbranchstock() throws Exception {
		List<Tbranchstock> list = new ArrayList<Tbranchstock>();
		list = new TbranchstockDAO().listByFilter("0=0", "tbranchstockpk");
		return list;
	}

	public static List<Mbranch> getMbranch(String filter) throws Exception {
		List<Mbranch> list = new ArrayList<Mbranch>();
		list = new MbranchDAO().listByFilter(filter, "branchid");
		return list;
	}
	
	public static List<Moutlet> getMoutlet(String filter) throws Exception {
		List<Moutlet> list = new ArrayList<Moutlet>();
		list = new MoutletDAO().listByFilter(filter, "outletcode");
		return list;
	}
	
	public static Morg getMorgobj(String code) throws Exception {
		Morg obj = new MorgDAO().findById(code);
		return obj;
	}

	public static List<Morg> getMorg() throws Exception {
		List<Morg> list = new ArrayList<Morg>();
		list = new MorgDAO().listByFilter("0=0", "org");
		return list;
	}

	public static List<Morg> getMorgByFilter(String filter) throws Exception {
		List<Morg> list = new ArrayList<Morg>();
		list = new MorgDAO().listByFilter(filter, "org");
		return list;
	}

	public static Map<String, String> getOrgmap() throws Exception {
		Map<String, String> map = new HashMap<>();
		for (Morg obj : getMorg()) {
			map.put(obj.getOrg(), obj.getDescription());
		}
		return map;
	}

	public static Map<String, String> getVendormap() throws Exception {
		Map<String, String> map = new HashMap<>();
		for (Mpersovendor obj : getMpersovendor()) {
			map.put(obj.getVendorname(), obj.getVendorcode());
		}
		return map;
	}

	public static List<Mmenu> getMmenu(String filter) throws Exception {
		List<Mmenu> list = new ArrayList<Mmenu>();
		list = new MmenuDAO().listByFilter(filter, "menuname");
		return list;
	}

	public static List<Mproducttype> getMproducttype(String filter) throws Exception {
		List<Mproducttype> list = new ArrayList<Mproducttype>();
		list = new MproducttypeDAO().listByFilter(filter, "productorg, producttype");
		return list;
	}

	public static List<Mproducttype> getMproducttype() throws Exception {
		List<Mproducttype> list = new ArrayList<Mproducttype>();
		list = new MproducttypeDAO().listByFilter("0=0", "productorg, producttype");
		return list;
	}

	public static List<Mproduct> getMproduct() throws Exception {
		List<Mproduct> list = new ArrayList<Mproduct>();
		list = new MproductDAO().listByFilter("0=0",
				"mproducttype.productgroupcode, mproducttype.productorg, productname");
		return list;
	}

	public static List<Mproduct> getMproduct(String filter) throws Exception {
		List<Mproduct> list = new ArrayList<Mproduct>();
		list = new MproductDAO().listByFilter(filter,
				"mproducttype.productgroupcode, mproducttype.productorg, productname");
		return list;
	}

	public static List<Mproduct> getMproduct(String filter, String orderby) throws Exception {
		List<Mproduct> list = new ArrayList<Mproduct>();
		list = new MproductDAO().listByFilter(filter, orderby);
		return list;
	}

	public static List<Mpersovendor> getMpersovendor() throws Exception {
		List<Mpersovendor> list = new ArrayList<Mpersovendor>();
		list = new MpersovendorDAO().listByFilter("0=0", "vendorname");
		return list;
	}

	public static List<Mpersovendor> getMpersovendor(String filter) throws Exception {
		List<Mpersovendor> list = new ArrayList<Mpersovendor>();
		list = new MpersovendorDAO().listByFilter(filter, "vendorname");
		return list;
	}

	public static List<Mcouriervendor> getMcouriervendor() throws Exception {
		List<Mcouriervendor> list = new ArrayList<Mcouriervendor>();
		list = new McouriervendorDAO().listByFilter("0=0", "vendorname");
		return list;
	}

	public static List<Mcourier> getMcourier() throws Exception {
		List<Mcourier> list = new ArrayList<Mcourier>();
		list = new McourierDAO().listByFilter("0=0", "couriername");
		return list;
	}

	public static List<Mcourier> getMcourier(String filter) throws Exception {
		List<Mcourier> list = new ArrayList<Mcourier>();
		list = new McourierDAO().listByFilter(filter, "couriercode");
		return list;
	}
	
	public static List<Mpendingreason> getMpendingreason() throws Exception {
		List<Mpendingreason> list = new ArrayList<Mpendingreason>();
		list = new MpendingreasonDAO().listByFilter("0=0", "pendingreason");
		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<String> getPendingreason() throws Exception {
		List<String> list = new ArrayList<String>();
		list = new MpendingreasonDAO().listStr("pendingreason");
		return list;
	}

	public static List<Mletter> getMletter(String filter) throws Exception {
		List<Mletter> list = new ArrayList<Mletter>();
		list = new MletterDAO().listByFilter(filter, "letterprefix");
		return list;
	}

	public static List<Mlettertype> getMlettertype(String filter) throws Exception {
		List<Mlettertype> list = new ArrayList<Mlettertype>();
		list = new MlettertypeDAO().listByFilter(filter, "mlettertypepk");
		return list;
	}
	
	public static List<Mlettertype> getMlettertype() throws Exception {
		List<Mlettertype> list = new ArrayList<Mlettertype>();
		list = new MlettertypeDAO().listByFilter("0=0", "lettertype");
		return list;
	}
	
	public static List<Mproductowner> getMproductowner() throws Exception {
		List<Mproductowner> list = new ArrayList<Mproductowner>();
		list = new MproductownerDAO().listByFilter("0=0", "mproductownerpk");
		return list;
	}
	
	public static List<Mincome> getMincome() throws Exception {
		List<Mincome> list = new ArrayList<Mincome>();
		list = new MincomeDAO().listByFilter("0=0", "incomesource");
		return list;
	}
	
	public static List<Mexpenses> getMexpenses() throws Exception {
		List<Mexpenses> list = new ArrayList<Mexpenses>();
		list = new MexpensesDAO().listByFilter("0=0", "expenses");
		return list;
	}
	
	public static List<Mpayment> getMpayment() throws Exception {
		List<Mpayment> list = new ArrayList<Mpayment>();
		list = new MpaymentDAO().listByFilter("0=0", "paymenttype");
		return list;
	}
	
	public static List<Mbank> getMbank() throws Exception {
		List<Mbank> list = new ArrayList<Mbank>();
		list = new MbankDAO().listByFilter("0=0", "bankname");
		return list;
	}
	
	public static List<Maim> getMaim() throws Exception {
		List<Maim> list = new ArrayList<Maim>();
		list = new MaimDAO().listByFilter("0=0", "aim");
		return list;
	}

	public static List<ProductgroupBean> getProductgroup(String productgrouptype) throws Exception {
		List<ProductgroupBean> list = new ArrayList<ProductgroupBean>();
		ProductgroupBean obj = null;

		if (productgrouptype.equals(AppUtils.PRODUCTGROUPTYPE_ALL)
				|| productgrouptype.equals(AppUtils.PRODUCTGROUPTYPE_CARD)) {
			obj = new ProductgroupBean();
			obj.setProductgroupcode(AppUtils.PRODUCTGROUP_CARD);
			AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_CARD);
			list.add(obj);
		}
		obj = new ProductgroupBean();
		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_TOKEN);
		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_TOKEN);
		list.add(obj);
		obj = new ProductgroupBean();
		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_PINPAD);
		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_PINPAD);
		list.add(obj);
		obj = new ProductgroupBean();
		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_DOCUMENT);
		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_DOCUMENT);
		list.add(obj);
		obj = new ProductgroupBean();
		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_SUPPLIES);
		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_SUPPLIES);
		list.add(obj);
		obj = new ProductgroupBean();
		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_PINMAILER);
		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_PINMAILER);
		list.add(obj);

//		obj = new ProductgroupBean();
//		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_BUKUTABUNGAN);
//		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_BUKUTABUNGAN);
//		list.add(obj);
//		obj = new ProductgroupBean();
//		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_BUKUTABUNGANGIRO);
//		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_BUKUTABUNGANGIRO);
//		list.add(obj);
//		obj = new ProductgroupBean();
//		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_BILYETGIRO);
//		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_BILYETGIRO);
//		list.add(obj);
//		obj = new ProductgroupBean();
//		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_WARKATGARANSIBANK);
//		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_WARKATGARANSIBANK);
//		list.add(obj);
//		obj = new ProductgroupBean();
//		obj.setProductgroupcode(AppUtils.PRODUCTGROUP_BILYETDEPOSITO);
//		AppData.getProductgroupLabel(AppUtils.PRODUCTGROUP_BILYETDEPOSITO);
//		list.add(obj);

		return list;
	}

	public static String getProductgroupLabel(String code) {
		if (code.equals(AppUtils.PRODUCTGROUP_CARD))
			return "KARTU";
		else if (code.equals(AppUtils.PRODUCTGROUP_CARDPHOTO))
			return "KARTU BERFOTO";
		else if (code.equals(AppUtils.PRODUCTGROUP_TOKEN))
			return "TOKEN";
		else if (code.equals(AppUtils.PRODUCTGROUP_TOKENCABANG))
			return "TOKEN CABANG";
		else if (code.equals(AppUtils.PRODUCTGROUP_PINPAD))
			return "PINPAD";
		else if (code.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
			return "SURAT BERHARGA";
		else if (code.equals(AppUtils.PRODUCTGROUP_SUPPLIES))
			return "SUPPLIES";
		else if (code.equals(AppUtils.PRODUCTGROUP_PINMAILER))
			return "PIN MAILER";
//		else if (code.equals(AppUtils.PRODUCTGROUP_BILYETDEPOSITO))
//			return "BILYET DEPOSITO";
//		else if (code.equals(AppUtils.PRODUCTGROUP_BUKUTABUNGAN))
//			return "BUKU TABUNGAN";
//		else if (code.equals(AppUtils.PRODUCTGROUP_BUKUTABUNGANGIRO))
//			return "BUKU TABUNGAN GIRO";
//		else if (code.equals(AppUtils.PRODUCTGROUP_BILYETGIRO))
//			return "BILYET GIRO";
//		else if (code.equals(AppUtils.PRODUCTGROUP_WARKATGARANSIBANK))
//			return "WARKAT GARANSI BANK";
		else
			return code;
	}

	public static String getProductgroupTitle(String code) {
		if (code.equals(AppUtils.PRODUCTGROUP_CARD))
			return "Kartu";
		else if (code.equals(AppUtils.PRODUCTGROUP_TOKEN))
			return "Token";
		else if (code.equals(AppUtils.PRODUCTGROUP_PINPAD))
			return "Pinpad";
		else if (code.equals(AppUtils.PRODUCTGROUP_DOCUMENT))
			return "Surat Berharga";
		else if (code.equals(AppUtils.PRODUCTGROUP_SUPPLIES))
			return "Supplies";
		else if (code.equals(AppUtils.PRODUCTGROUP_PINMAILER))
			return "Pin Mailer";
		else
			return code;
	}

	/*
	 * public static String getProductorgLabel(String code) { if (code == null) code
	 * = ""; if (code.equals(AppUtils.PRODUCTORG_100)) return "REGULAR"; else if
	 * (code.equals(AppUtils.PRODUCTORG_200)) return "DERIVATIF CO-BRAND"; else if
	 * (code.equals(AppUtils.PRODUCTORG_210)) return "DERIVATIF INVINITY"; else if
	 * (code.equals(AppUtils.PRODUCTORG_220)) return
	 * "DERIVATIF KARTU ANGGOTA/PEGAWAI"; else if
	 * (code.equals(AppUtils.PRODUCTORG_230)) return "DERIVATIF KARTU MAHASISWA";
	 * else if (code.equals(AppUtils.PRODUCTORG_300)) return "PROGRAM"; else if
	 * (code.equals(AppUtils.PRODUCTORG_400)) return "SYARIAH"; else return code; }
	 */

	public static List<Mreturnreason> getMreturnreason() throws Exception {
		List<Mreturnreason> list = new ArrayList<Mreturnreason>();
		list = new MreturnreasonDAO().listByFilter("0=0", "returnreason");
		return list;
	}
	
	public static List<Mreturnreason> getMreturnreason(String filter) throws Exception {
		List<Mreturnreason> list = new ArrayList<Mreturnreason>();
		list = new MreturnreasonDAO().listByFilter(filter, "returnreason");
		return list;
	}
	
	public static List<Mrepairreason> getMrepairreason() throws Exception {
		List<Mrepairreason> list = new ArrayList<Mrepairreason>();
		list = new MrepairreasonDAO().listByFilter("0=0", "repairreason");
		return list;
	}

	public static String getProductunitLabel(String code) {
		if (code.equals(AppUtils.PRODUCTUNIT_BOX))
			return "BOX";
		else if (code.equals(AppUtils.PRODUCTUNIT_PCS))
			return "PCS";
		else
			return code;
	}

	public static String getPinpadtypeLabel(String code) {
		if (code.equals(AppUtils.PINPADTYPE_CS))
			return "CS";
		else if (code.equals(AppUtils.PINPADTYPE_TELLER))
			return "TELLER";
		else if (code.equals(AppUtils.PINPADTYPE_BOTH))
			return "CS dan TELLER";
		else
			return code;
	}

	public static String getInvProcesstypeLabel(String code) {
		if (code.equals(AppUtils.INV_PROCESSTYPE_INCOMING))
			return "INCOMING";
		else if (code.equals(AppUtils.INV_PROCESSTYPE_OUTGOING))
			return "OUTGOING";
		else
			return code;
	}

	public static String getProcesstypeLabel(String code) {
		if (code.equals(AppUtils.PROCESSTYPE_REGULAR))
			return "REGULAR";
		else if (code.equals(AppUtils.PROCESSTYPE_URGENT))
			return "URGENT";
		else
			return code;
	}

	public static String getFlowgroup(String code) {
		if (code.equals(AppUtils.PROSES_INCOMING))
			return "INCOMING";
		else if (code.equals(AppUtils.PROSES_OUTGOING))
			return "OUTGOING";
		else if (code.equals(AppUtils.PROSES_ORDER))
			return "ORDER";
		else if (code.equals(AppUtils.PROSES_PERSO))
			return "PERSO";
		else if (code.equals(AppUtils.PROSES_PAKET))
			return "PAKET";
		else if (code.equals(AppUtils.PROSES_DELIVERY))
			return "DELIVERY";
		else
			return code;
	}

	public static String getEntrytypeLabel(String code) {
		if (code.equals(AppUtils.ENTRYTYPE_BATCH))
			return "BATCH";
		else if (code.equals(AppUtils.ENTRYTYPE_MANUAL))
			return "PRODUKSI";
		else if (code.equals(AppUtils.ENTRYTYPE_MANUAL_BRANCH))
			return "CABANG";
		else if (code.equals(AppUtils.ENTRYTYPE_COMBO))
			return "COMBO";
		else
			return code;
	}

	public static String getStatusPilotingLabel(String code) {
		if (code.trim().equals("WA"))
			return "MENUNGGU PERSETUJUAN PEMIMPIN";
		else if (code.trim().equals("DN"))
			return "DISETUJUI";
		else if (code.trim().equals("DC"))
			return "DITOLAK";
		else
			return "";
	}
	
	public static String getStatusLabel(String code) {
		if (code.trim().equals(AppUtils.STATUS_ORDER))
			return "ENTRY";
		else if (code.trim().equals(AppUtils.STATUS_PROSES))
			return "PROCESSED";
		// STATUS CABANG
		else if (code.trim().equals(AppUtils.STATUSBRANCH_PENDINGPRODUKSI))
			return "PENDING PRODUKSI";
		else if (code.trim().equals(AppUtils.STATUSBRANCH_PROSESPRODUKSI))
			return "PROSES PRODUKSI";
		else if (code.trim().equals(AppUtils.STATUSBRANCH_PENDINGPAKET))
			return "PENDING PAKET";
		else if (code.trim().equals(AppUtils.STATUSBRANCH_PROSESPAKET))
			return "PROSES PAKET";
		else if (code.trim().equals(AppUtils.STATUSBRANCH_PENDINGDELIVERY))
			return "PENDING DELIVERY";
		else if (code.trim().equals(AppUtils.STATUSBRANCH_PROSESDELIVERY))
			return "PROSES DELIVERY";
		else if (code.trim().equals(AppUtils.STATUSBRANCH_DELIVERED))
			return "DELIVERED";

		// STATUS ORDER MANUAL
		else if (code.trim().equals(AppUtils.STATUS_ORDER_WAITAPPROVAL))
			return "PROSES PERSETUJUAN PEMIMPIN/UNIT";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_WAITAPPROVALCAB))
			return "MENUNGGU PERSETUJUAN SUPERVISOR CABANG";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_WAITAPPROVALWIL))
			return "MENUNGGU PERSETUJUAN SUPERVISOR WILAYAH";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_DECLINECAB))
			return "DITOLAK OLEH PEMIMPIN CABANG";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_DECLINEWIL))
			return "DITOLAK OLEH PEMIMPIN WILAYAH";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_DECLINEDIV))
			return "DITOLAK OLEH PEMIMPIN DIVISI";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_DECLINEJAL))
			return "DITOLAK OLEH PEMIMPIN JAL";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_OUTGOINGAPPROVAL))
			return "PROSES PERSETUJUAN PEMENUHAN";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_OUTGOINGDECLINE))
			return "REJECTED BY INVENTORY";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_REJECTED))
			return "REVISI";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_PRODUKSI))
			return "PRODUKSI";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_DONE))
			return "DONE";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_WAITSCANPRODUKSI))
			return "MENUNGGU SCAN PRODUKSI";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_REQUESTORDER))
			return "REQUEST ORDER CABANG";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_WAITAPPROVALOPR))
			return "PROSES PERSETUJUAN OPR";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_WAITSCANPRODUKSIOPR))
			return "MENUNGGU SCAN PRODUKSI OPR";
		else if (code.trim().equals(AppUtils.STATUS_ORDER_WAITAPPROVALJAL))
			return "PROSES PERSETUJUAN JAL";

		// STATUS RETUR
		else if (code.trim().equals(AppUtils.STATUS_RETUR_WAITAPPROVAL))
			return "PROSES PERSETUJUAN PEMIMPIN/UNIT";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_DECLINE))
			return "DITOLAK OLEH PEMIMPIN";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_WAITAPPROVALWILAYAH))
			return "PROSES PERSETUJUAN WILAYAH";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALWILAYAH))
			return "DITOLAK OLEH WILAYAH";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_WAITAPPROVALPFA))
			return "PROSES PERSETUJUAN PFA";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_APPROVALPFA))
			return "DISETUJUI PFA";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALPFA))
			return "DITOLAK OLEH PFA";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_WAITAPPROVALOPR))
			return "PROSES PERSETUJUAN OPR";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_APPROVALOPR))
			return "DISETUJUI OPR";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_DECLINEAPPROVALOPR))
			return "DITOLAK OLEH OPR";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_VENDORPROCESS))
			return "PENCETAKAN VENDOR";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_INPATCH))
			return "DALAM PATCH";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_DESTROYED))
			return "DIMUSNAHKAN";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_RETURNPFA))
			return "DIKEMBALIKAN KE PFA";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_RETURNEDPFA))
			return "DIKEMBALIKAN OLEH PFA";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_RECEIVED))
			return "RETUR DITERIMA";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_RETURNOPR))
			return "DIKEMBALIKAN KE OPR";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_PROCESSWIL))
			return "PROSES PENGECEKAN WILAYAH";
		else if (code.trim().equals(AppUtils.STATUS_RETUR_PROCESSPFA))
			return "PROSES PENGECEKAN PFA";

		// STATUS PLANNING
		else if (code.trim().equals(AppUtils.STATUS_PLANNING_WAITAPPROVAL))
			return "PROSES PERSETUJUAN PEMIMPIN/UNIT";
		else if (code.trim().equals(AppUtils.STATUS_PLANNING_WAITAPPROVALOPR))
			return "PROSES PERSETUJUAN OPR";
		else if (code.trim().equals(AppUtils.STATUS_PLANNING_WAITAPPROVALPFA))
			return "PROSES PERSETUJUAN PEMIMPIN PFA";
		else if (code.trim().equals(AppUtils.STATUS_PLANNING_APPROVED))
			return "USULAN DISETUJUI";
		else if (code.trim().equals(AppUtils.STATUS_PLANNING_DECLINEBYLEAD))
			return "USULAN DITOLAK";
		else if (code.trim().equals(AppUtils.STATUS_PLANNING_DECLINEBYOPR))
			return "USULAN DITOLAK OPR";
		else if (code.trim().equals(AppUtils.STATUS_PLANNING_DECLINEBYPFA))
			return "USULAN DITOLAK PFA";
		else if (code.trim().equals(AppUtils.STATUS_PLANNING_DONE))
			return "HABIS";

		// STATUS INVENTORY
		else if (code.trim().equals(AppUtils.STATUS_INVENTORY_INCOMINGWAITAPPROVAL))
			return "PROSES PERSETUJUAN PERSEDIAAN";
		else if (code.trim().equals(AppUtils.STATUS_INVENTORY_INCOMINGDECLINE))
			return "PERSEDIAAN DITOLAK";
		else if (code.trim().equals(AppUtils.STATUS_INVENTORY_INCOMINGAPPROVED))
			return "PERSEDIAAN DISETUJUI";
		else if (code.trim().equals(AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVAL))
			return "PROSES PERSETUJUAN PEMENUHAN";
		else if (code.trim().equals(AppUtils.STATUS_INVENTORY_OUTGOINGWAITAPPROVALOPR))
			return "WAIT APPROVAL OUTGOING OPR";
		else if (code.trim().equals(AppUtils.STATUS_INVENTORY_OUTGOINGDECLINE))
			return "OUTGOING DITOLAK";
		else if (code.trim().equals(AppUtils.STATUS_INVENTORY_OUTGOINGAPPROVED))
			return "PERSIAPAN PACKING";
		else if (code.trim().equals(AppUtils.STATUS_INVENTORY_OUTGOINGSCAN))
			return "SELESAI PACKING";
		else if (code.trim().equals(AppUtils.STATUS_INVENTORY_OUTGOINGWAITSCAN))
			return "PERSIAPAN PACKING";
		// STATUS PERSO
		else if (code.trim().equals(AppUtils.STATUS_PERSO_PERSOWAITAPPROVAL))
			return "WAIT APPROVAL PERSO";
		else if (code.trim().equals(AppUtils.STATUS_PERSO_PERSODECLINE))
			return "PERSO REJECTED BY PRODUKSI";
		else if (code.trim().equals(AppUtils.STATUS_PERSO_OUTGOINGWAITAPPROVAL))
			return "WAIT APPROVAL INVENTORY (OUTGOING)";
		else if (code.trim().equals(AppUtils.STATUS_PERSO_OUTGOINGDECLINE))
			return "PERSO REJECTED BY INVENTORY";
		else if (code.trim().equals(AppUtils.STATUS_PERSO_PRODUKSI))
			return "PRODUKSI";
		else if (code.trim().equals(AppUtils.STATUS_PERSO_DONE))
			return "PERSO DONE";
		// STATUS DELIVERY
		else if (code.trim().equals(AppUtils.STATUS_DELIVERY_PAKETORDER))
			return "ORDER PAKET";
		else if (code.trim().equals(AppUtils.STATUS_DELIVERY_PAKETPROSES))
			return "PROSES PAKET";
		else if (code.trim().equals(AppUtils.STATUS_DELIVERY_PAKETDONE))
			return "PAKET SIAP KIRIM";
		else if (code.trim().equals(AppUtils.STATUS_DELIVERY_DELIVERYORDER))
			return "ORDER DELIVERY";
		else if (code.trim().equals(AppUtils.STATUS_DELIVERY_EXPEDITIONORDER))
			return "PEMILIHAN EKSPEDISI";
		else if (code.trim().equals(AppUtils.STATUS_DELIVERY_DELIVERY))
			return "PROSES PENGIRIMAN";
		else if (code.trim().equals(AppUtils.STATUS_DELIVERY_DELIVERED))
			return "DITERIMA";
		else if (code.trim().equals(AppUtils.STATUS_DELIVERY_WAITAPPROVAL))
			return "PROSES PERSETUJUAN PENGIRIMAN";
		// STATUS SERIAL NUMBER
		else if (code.trim().equals(AppUtils.STATUS_SERIALNO_ENTRY))
			return "PERSEDIAAN BARANG MASUK";
		else if (code.trim().equals(AppUtils.STATUS_SERIALNO_SCANINVENTORY))
			return "SCAN INVENTORY";
		else if (code.trim().equals(AppUtils.STATUS_SERIALNO_OUTINVENTORY))
			return "PERSEDIAAN BARANG KELUAR";
		else if (code.trim().equals(AppUtils.STATUS_SERIALNO_TERPAKAI))
			return "TERPAKAI";
		else if (code.trim().equals(AppUtils.STATUS_SERIALNO_CLOSED))
			return "PERSEDIAAN DITUTUP";
		else if (code.trim().equals(AppUtils.STATUS_SERIALNO_INJECTED))
			return "INJECTED";
		else if (code.trim().equals(AppUtils.STATUS_SERIALNO_SCANPRODUKSI))
			return "SCAN PRODUKSI";
		else if (code.trim().equals(AppUtils.STATUS_SERIALNO_OUTPRODUKSI))
			return "OUT PRODUKSI";
		// STATUS MANUAL PRODUKSI
		else if (code.trim().equals(AppUtils.STATUS_PRODUCTION_PRODUKSIWAITAPPROVAL))
			return "WAIT APPROVAL ORDER";
		else if (code.trim().equals(AppUtils.STATUS_PRODUCTION_APPROVEDPRODUKSI))
			return "PRODUKSI APPROVED";
		else if (code.trim().equals(AppUtils.STATUS_PRODUCTION_DECLINEPRODUKSI))
			return "PRODUKSI DITOLAK";

		// STATUS REPAIR
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_WAITAPPROVAL))
			return "PROSES PERSETUJUAN PEMIMPIN/UNIT";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_WAITAPPROVALREPAIROPR))
			return "PROSES PERSETUJUAN OPR";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_DECLINEAPPROVAL))
			return "DITOLAK OLEH PEMIMPIN";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_DECLINEREPAIROPR))
			return "DITOLAK OLEH OPR";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_PROCESSCHECKING))
			return "PROSES PENGECEKAN";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_PROCESSOPR))
			return "DALAM PERBAIKAN OPR";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_PROCESSVENDOR))
			return "DALAM PERBAIKAN VENDOR";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_PENDINGPROCESS))
			return "PENDING PEMENUHAN OPR";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_DONEPROCESS))
			return "SELESAI DIPROSES";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_FAILED))
			return "GAGAL PERBAIKAN";
		else if (code.trim().equals(AppUtils.STATUS_REPAIR_RECEIVED))
			return "REPAIR DITERIMA";
		

		// STATUS SWITCHING
		else if (code.trim().equals(AppUtils.STATUS_SWITCH_ENTRY))
			return "PENDING ORDER SWITCHING";
		else if (code.trim().equals(AppUtils.STATUS_SWITCH_WAITAPPROVAL))
			return "PROSES PERSETUJUAN REQUESTOR";
		else if (code.trim().equals(AppUtils.STATUS_SWITCH_WAITAPPROVALPOOL))
			return "PROSES PERSETUJUAN CABANG TUJUAN";
		else if (code.trim().equals(AppUtils.STATUS_SWITCH_HANDOVER))
			return "PROSES PENYERAHAN";
		else if (code.trim().equals(AppUtils.STATUS_SWITCH_REJECTEDREQ))
			return "REVISI";
		else if (code.trim().equals(AppUtils.STATUS_SWITCH_REJECTEDPOOL))
			return "DITOLAK OLEH CABANG TUJUAN";
		else if (code.trim().equals(AppUtils.STATUS_SWITCH_DECLINEREQ))
			return "DITOLAK OLEH REQUESTOR";
		
		// STATUS CEMTEXT
		else if (code.trim().equals(AppUtils.STATUS_CEMTEXT_WAITAPPROVAL))
			return "MENUNGGU PERSETUJUAN PEMBUKUAN";
		else if (code.trim().equals(AppUtils.STATUS_CEMTEXT_UPLOADED))
			return "PROSES PEMBUKUAN";
		else if (code.trim().equals(AppUtils.STATUS_CEMTEXT_UNDEFINED))
			return "PARAMETER CEMTEXT TIDAK DITEMUKAN";
		else if (code.trim().equals(AppUtils.STATUS_CEMTEXT_DONE))
			return "SELESAI PEMBUKUAN";
		else if (code.trim().equals(AppUtils.STATUS_CEMTEXT_ERRORDATA))
			return "GAGAL PROSES";

		// AKTIVASI
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_REISSUESTOP))
			return "STOP PENERBITAN ULANG KARTU";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_REISSUE))
			return "PENERBITAN ULANG KARTU";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_RETURNED))
			return "KARTU KEMBALI KE BANK";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_HOT))
			return "KARTU DIBLOKIR PERMANEN";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_CLOSED))
			return "KARTU DITUTUP";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_EXPIRED))
			return "MASA BERLAKU KARTU HABIS";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_NORMAL))
			return "KARTU AKTIF";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_WARM))
			return "KARTU DIBLOKIR";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_UNKNOWN))
			return "UNKNOWN";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_UNUSED))
			return "BELUM TERPAKAI/SIAP JUAL";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_REJECT))
			return "REJECT";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_USED))
			return "TERPAKAI";
		else if (code.trim().equals(AppUtils.STATUS_AKTIVASI_TUTUP))
			return "TUTUP";
		else
			return code;
	}
	
	public static String getGroupLevelLabel(int code) {
		if(code == 1)
			return "PEMIMPIN";
		else if(code == 2)
			return "WAKIL PEMIMPIN";
		else if(code == 3)
			return "KELOMPOK/BIDANG";
		else
			return "";
	}
	
	public static String getBranchLevelLabel(int code) {
		if(code == 1)
			return "DIVISI";
		else if(code == 2)
			return "WILAYAH";
		else if(code == 3)
			return "CABANG";
		else
			return "";
	}

	public static String getStatusDerivatifLabel(int code) {
		if (code == AppUtils.STATUS_DERIVATIF_WAITAPPROVAL)
			return "WAIT APPROVAL ORDER";
		else if (code == AppUtils.STATUS_DERIVATIF_ORDERDECLINE)
			return "REJECTED BY PRODUKSI";
		else if (code == AppUtils.STATUS_DERIVATIF_GETDATA)
			return "GET DATA";
		else if (code == AppUtils.STATUS_DERIVATIF_SCAN)
			return "WAIT SCANNING";
		else if (code == AppUtils.STATUS_DERIVATIF_CROP)
			return "WAIT CROPING";
		else if (code == AppUtils.STATUS_DERIVATIF_MERGE)
			return "WAIT MERGING";
		else if (code == AppUtils.STATUS_DERIVATIF_ORDERPERSO)
			return "ORDER PERSO";
		else if (code == AppUtils.STATUS_DERIVATIF_ORDERPERSODECLINE)
			return "PERSO REJECTED BY PRODUKSI";
		else if (code == AppUtils.STATUS_DERIVATIF_ORDERPERSOAPPROVAL)
			return "WAIT APPROVAL PERSO";
		else if (code == AppUtils.STATUS_DERIVATIF_ORDERPERSOINVENTORYAPPROVAL)
			return "WAIT APPROVAL INVENTORY (OUTGOING)";
		else if (code == AppUtils.STATUS_DERIVATIF_ORDERPERSOINVENTORYDECLINE)
			return "PERSO REJECTED BY INVENTORY";
		else if (code == AppUtils.STATUS_DERIVATIF_PRODUKSI)
			return "PRODUKSI";
		else if (code == AppUtils.STATUS_DERIVATIF_ORDERPAKET)
			return "ORDER PAKET";
		else if (code == AppUtils.STATUS_DERIVATIF_PAKET)
			return "PROSES PAKET";
		else if (code == AppUtils.STATUS_DERIVATIF_ORDERDELIVERY)
			return "ORDER DELIVERY";
		else if (code == AppUtils.STATUS_DERIVATIF_DELIVERY)
			return "DELIVERY";
		else if (code == AppUtils.STATUS_DERIVATIF_DELIVERED)
			return "DELIVERED";
		else if (code == AppUtils.STATUS_DERIVATIF_APPROVEADJ)
			return "WAIT APPROVAL ADJUSTMENT";
		return String.valueOf(code);
	}

	public static String getDerivatifFailLabel(String code) {
		if (code.equals(AppUtils.STATUS_DATA_ONPROSES))
			return "Nomer kartu sudah diproses";
		else if (code.equals(AppUtils.STATUS_DATA_PRODUCTUNREG))
			return "Produk belum terdaftar";
		else if (code.equals(AppUtils.STATUS_DATA_BRANCHUNREG))
			return "Cabang belum terdaftar";
		else if (code.equals(AppUtils.STATUS_DATA_BRANCHINVALID))
			return "Cabang surat order dan data kartu tidak sama";
		else if (code.equals(AppUtils.STATUS_DATA_DATAUNREG))
			return "Nomer kartu belum terdaftar";
		else if (code.equals(AppUtils.STATUS_DATA_ORDERBRANCHREG))
			return "Nomer kartu sudah pernah di GET";
		else if (code.equals(AppUtils.STATUS_DATA_FULLDATA))
			return "Data melebihi jumlah order";
		else
			return code;
	}

	public static Map<String, Mproductgroup> getMproductgroup() {
		Map<String, Mproductgroup> map = new HashMap<String, Mproductgroup>();
		try {
			for (Mproductgroup obj : new MproductgroupDAO().listByFilter("0=0", "productgroupcode")) {
				map.put(obj.getProductgroupcode(), obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static int slaCounter(Map<Date, Mholiday> mapHoliday, Date startdate, Date finishdate) {
		int sla = 0;
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startdate);
			while (finishdate.compareTo(cal.getTime()) >= 0) {
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
						&& mapHoliday.get(cal.getTime()) == null)
					sla++;
				cal.add(Calendar.DATE, 1);
			}
			if (sla > 0)
				sla--;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sla;
	}
}
