package com.sdd.management.utils;

public class AppUtils {

	public static final int AUTOCOMPLETE_MAXROWS = 13;
	public static final int AUTOCOMPLETE_MINLENGTH = 2;
	public final static String NO_PAKET = "2";

	public final static String CE_EMBOSS = "E";
	public final static String CE_ORDER = "ORD";
	public final static String CE_PINMAILER = "PM";
	public final static String CE_INVENTORY_INCOMING = "INV/IN/";
	public final static String CE_INVENTORY_OUTGOING = "INV/OUT/";
	public final static String CE_PERSO = "CP";
	public final static String CE_DERIVATIF = "DRV";
	public final static String CE_PAKET = "PCK";
	public final static String CE_EXPEDITION = "EXP";

	public final static String PREFIX_INCOMING = "I";
	public final static String PREFIX_OUTGOING = "O";

	public final static String BATCHCODE_CARD = "C";
	public final static String BATCHCODE_PINMAILER = "PM";
	public final static String CODE_COURIER = "COU";
	public final static String ID_CARD_PRODUCTION = "CP";
	public final static String ID_TOKEN_PRODUCTION = "TP";
	public final static String ID_TOKEN_BRANCH = "TC";
	public final static String ID_PINPAD_PRODUCTION = "PP";
	public final static String ID_DOCUMENT_PRODUCTION = "DP";
	public final static String ID_SUPPLIES_PRODUCTION = "SP";
	public final static String ID_PENDING = "PD";
	public final static String ID_PAKET = "PK";
	public final static String ID_DELIVERY = "DL";
	public final static String ID_VENDOR = "VP";

	public static final String PARAM_GROUP_MAIL = "MAIL";
	public static final String PARAM_SMTPNAME = "SMTPNAME";
	public static final String PARAM_SMTPPORT = "SMTPPORT";
	public static final String PARAM_MAILID = "MAILID";
	public static final String PARAM_MAILPASSWORD = "MAILPASSWORD";

	public static final String PARAM_GROUP_LDAP = "LDAP";
	public static final String PARAM_LDAPURL = "LDAPURL";
	public static final String PARAM_LDAPUSER = "LDAPUSER";
	public static final String PARAM_LDAPPASSWORD = "LDAPPASSWORD";

	public static final String PARAM_GROUP_ALERT = "ALERT";

	public static final String PARAM_GROUP_ESTSTOCK = "ESTSTOCK";
	public static final String PARAM_ESTSTOCK = "ESTSTOCK";

	public static final String PARAM_GROUP_COMPANYDATA = "COMPANYDATA";
	public static final String PARAM_COMPANYNAME = "COMPANYNAME";
	public static final String PARAM_DIVISINAME = "DIVISINAME";
	public static final String PARAM_GROUPNAME = "GROUPNAME";
	public static final String PARAM_ADDRESS1 = "ADDRESS1";
	public static final String PARAM_ADDRESS2 = "ADDRESS2";
	public static final String PARAM_ADDRESS3 = "ADDRESS3";
	public static final String PARAM_ADDRESS4 = "ADDRESS4";
	public static final String PARAM_CITY = "CITY";
	public static final String PARAM_PEMIMPIN = "PEMIMPIN";
	public static final String PARAM_TTD = "TTD";
	public static final String PARAM_PREFIXSURAT = "PREFIXSURAT";
	public static final String PARAM_ASSISTENPNH = "ASSISTENPNH";
	public static final String PARAM_PENYELIAPNC = "PENYELIAPNC";
	public static final String PARAM_PEMIMPINKCP = "PEMIMPINKCP";
	public static final String PARAM_PEMIMPINPMSR = "PEMIMPINPMSR";
	public static final String PARAM_PENYELIAPNC2 = "PENYELIAPNC2";
	public static final String PARAM_ASISTENPNC2 = "ASISTENPNC2";
	public static final String PARAM_PBN = "PBN";
	public static final String PARAM_ANALIS = "ANALIS";
	public static final String PARAM_PEMIMPINBID = "PEMIMPINBID";

	public static final String PARAM_GROUP_BRANCHACTIVATIONHOST = "BRANCHACTIVATIONHOST";
	public static final String PARAM_BRANCHACTIVATIONHOSTIP = "BRANCHACTIVATIONHOSTIP";
	public static final String PARAM_BRANCHACTIVATIONHOSTPORT = "BRANCHACTIVATIONHOSTPORT";
	public static final String PARAM_BRANCHACTIVATIONHOSTUSERID = "BRANCHACTIVATIONHOSTUSERID";
	public static final String PARAM_BRANCHACTIVATIONHOSTPASSWORD = "BRANCHACTIVATIONHOSTPASSWORD";
	public static final String PARAM_GROUP_PAGUPERIOD = "PAGUPERIOD";

	public static final String PARAM_GROUP_ICON = "ICON";
	public static final String PARAM_ICONHOST = "ICONHOST";
	public static final String PARAM_ICONPORT = "ICONPORT";
	public static final String PARAM_ICONUSER = "ICONUSER";
	public static final String PARAM_ICONPASSWORD = "ICONPASSWORD";
	public static final String PARAM_GFXPATHTOCORE = "GFXPATHTOCORE";
	public static final String PARAM_GFXPATHTOIMS = "GFXPATHTOIMS";
	public static final String PARAM_CEMTEXTFOLDER = "CEMTEXTFOLDER";
	public static final String PARAM_PAYROLLFOLDER = "PAYROLLFOLDER";

	public static final String PARAM_GROUP_SLA = "SLA";
	public static final String PARAM_SLATOTAL = "SLATOTAL";
	public static final String PARAM_SLAWARNING = "SLAWARNING";
	public static final String PARAM_SLAOVER = "SLAOVER";

	public static final String PARAM_PAGUPERIOD = "PAGU_PERIOD";

	public static final String FILES_ROOT_PATH = "/files";
	public static final String ACTIVATION_PATH = "/aktivasi";
	public static final String PHONENUMBER_PATH = "/nohp";
	public static final String PAYROLLSTAT_PATH = "/payroll";
	public static final String IMAGES_ROOT_PATH = "/images";
	public static final String REPORT_PATH = "/report/";
	public static final String BOOKFILE_PATH = "/book/";
	public static final String REPAIR_PATH = "/repair/";
	public static final String MEMO_PATH = "/memo/";
	public static final String QR_PATH = "/qr/";
	public static final String IMAGE_PATH = "/img";
	public static final String PATH_EMBOSSFILE = "/emboss";
	public static final String PATH_DERIVATIFFILE = "/derivatif";
	public static final String PATH_TOKEN = "/token";
	public static final String PATH_TOKENDOC = "/tokendoc";
	public static final String PATH_PINPAD = "/pinpad";
	public static final String GENERATE_EMBOSS_PATH = "/generate_emboss/";
	public static final String PATH_PINMAILERFILE = "/pinmailer";
	public static final String PLAN_PATH = "/plan/";
	public static final String POD_PATH = "/pod/";
	public static final String GFXTOCORE_PATH = "/tocore/";
	public static final String GFXTOIMS_PATH = "/toims/";

	public final static String PRODUCTGROUP_CARD = "01";
	public final static String PRODUCTGROUP_CARDPHOTO = "09";
	public final static String PRODUCTGROUP_TOKEN = "02";
	public final static String PRODUCTGROUP_TOKENCABANG = "TC";
	public final static String PRODUCTGROUP_PINPAD = "03";
	public final static String PRODUCTGROUP_DOCUMENT = "04";
	public final static String PRODUCTGROUP_SUPPLIES = "05";
	public final static String PRODUCTGROUP_PINMAILER = "06";

	public final static String GROUPTYPE_DEPOSITO = "01";
	public final static String GROUPTYPE_BUKUTABUNGAN = "02";
	public final static String GROUPTYPE_BIYETGIRO = "03";
	public final static String GROUPTYPE_CEK = "04";

	public final static String PRODUCTGROUPTYPE_ALL = "ALL";
	public final static String PRODUCTGROUPTYPE_CARD = "CARD";
	public final static String PRODUCTGROUPTYPE_NONCARD = "NONCARD";

	public final static String PRODUCTORG_100 = "100";
	public final static String PRODUCTORG_200 = "200";
	public final static String PRODUCTORG_210 = "210";
	public final static String PRODUCTORG_220 = "220";
	public final static String PRODUCTORG_230 = "230";
	public final static String PRODUCTORG_300 = "300";
	public final static String PRODUCTORG_400 = "400";

	public final static String PRODUCTUNIT_BOX = "B";
	public final static String PRODUCTUNIT_PCS = "P";

	public final static String INV_PROCESSTYPE_INCOMING = "I";
	public final static String INV_PROCESSTYPE_OUTGOING = "O";

	public final static String PROCESSTYPE_REGULAR = "R";
	public final static String PROCESSTYPE_URGENT = "U";

	public final static String PINMAILERTYPE_REGULAR = "REG";
	public final static String PINMAILERTYPE_SYARIAH = "SYR";

	public final static String PINPADTYPE_CS = "C";
	public final static String PINPADTYPE_TELLER = "T";
	public final static String PINPADTYPE_BOTH = "B";

	public final static String PENDINGTYPE_ALL = "A";
	public final static String PENDINGTYPE_GROUP = "G";
	public final static String PENDINGTYPE_SINGLE = "S";

	public final static String STATUS_PLANNING_WAITAPPROVAL = "U1";
	public final static String STATUS_PLANNING_WAITAPPROVALOPR = "U2";
	public final static String STATUS_PLANNING_WAITAPPROVALPFA = "U3";
	public final static String STATUS_PLANNING_APPROVED = "U4";
	public final static String STATUS_PLANNING_DECLINEBYLEAD = "U5";
	public final static String STATUS_PLANNING_DECLINEBYOPR = "U6";
	public final static String STATUS_PLANNING_DECLINEBYPFA = "U7";
	public final static String STATUS_PLANNING_DONE = "U8";

	public final static String STATUS_INVENTORY_INCOMINGWAITAPPROVAL = "I11";
	public final static String STATUS_INVENTORY_INCOMINGDECLINE = "I12";
	public final static String STATUS_INVENTORY_INCOMINGAPPROVED = "I19";
	public final static String STATUS_INVENTORY_OUTGOINGWAITAPPROVAL = "I21";
	public final static String STATUS_INVENTORY_OUTGOINGDECLINE = "I22";
	public final static String STATUS_INVENTORY_OUTGOINGAPPROVED = "I29";
	public final static String STATUS_INVENTORY_OUTGOINGSCAN = "S21";
	public final static String STATUS_INVENTORY_OUTGOINGWAITSCAN = "S22";
	public final static String STATUS_INVENTORY_OUTGOINGWAITAPPROVALOPR = "I23";
	public final static String STATUS_INVENTORY_OUTGOINGREPAIRWAITAPPROVALOPR = "I24";
	public final static String STATUS_INVENTORY_OUTGOINGREPAIRAPPROVEOPR = "I25";
	public final static String STATUS_INVENTORY_OUTGOINGREPAIRDECLINEOPR = "I26";

	public final static int STATUS_DERIVATIF_WAITAPPROVAL = 0;
	public final static int STATUS_DERIVATIF_ORDERDECLINE = 1;
	public final static int STATUS_DERIVATIF_GETDATA = 2;
	public final static int STATUS_DERIVATIF_VERIFYDATA = 3;
	public final static int STATUS_DERIVATIF_SCAN = 3;
	public final static int STATUS_DERIVATIF_CROP = 4;
	public final static int STATUS_DERIVATIF_MERGE = 5;
	public final static int STATUS_DERIVATIF_ORDERPERSO = 6;
	public final static int STATUS_DERIVATIF_ORDERPERSOAPPROVAL = 7;
	public final static int STATUS_DERIVATIF_ORDERPERSODECLINE = 8;
	public final static int STATUS_DERIVATIF_ORDERPERSOINVENTORYAPPROVAL = 9;
	public final static int STATUS_DERIVATIF_ORDERPERSOINVENTORYDECLINE = 10;
	public final static int STATUS_DERIVATIF_PRODUKSI = 11;
	public final static int STATUS_DERIVATIF_ORDERPAKET = 12;
	public final static int STATUS_DERIVATIF_PAKET = 13;
	public final static int STATUS_DERIVATIF_ORDERDELIVERY = 14;
	public final static int STATUS_DERIVATIF_DELIVERY = 15;
	public final static int STATUS_DERIVATIF_APPROVEADJ = 16;
	public final static int STATUS_DERIVATIF_REJECTED = 17;
	public final static int STATUS_DERIVATIF_DELIVERED = 18;

	public final static String STATUS_DECLINE = "D";
	public final static String STATUS_APPROVED = "A";

	public final static String STATUS_TOKEN_ENTRY = "E";
	public final static String STATUS_PINPAD_ENTRY = "E";
	public final static String STATUS_SECURITIES_ENTRY = "E";

	public final static String STATUS_DATA_ONPROSES = "OP";
	public final static String STATUS_DATA_PRODUCTUNREG = "PU";
	public final static String STATUS_DATA_BRANCHUNREG = "BU";
	public final static String STATUS_DATA_BRANCHINVALID = "BI";
	public final static String STATUS_DATA_DATAUNREG = "DU";
	public final static String STATUS_DATA_ORDERBRANCHREG = "OBR";
	public final static String STATUS_DATA_FULLDATA = "FD";

	public final static String STATUS_ORDER = "O";
	public final static String STATUS_PROSES = "P";
	public final static String STATUS_REJECTED = "R";

	public final static String STATUSBRANCH_PENDINGORDER = "000";
	public final static String STATUSBRANCH_PENDINGPRODUKSI = "P01";
	public final static String STATUSBRANCH_PROSESPRODUKSI = "P02";
	public final static String STATUSBRANCH_PENDINGPAKET = "D01";
	public final static String STATUSBRANCH_PROSESPAKET = "D02";
	public final static String STATUSBRANCH_PENDINGDELIVERY = "D03";
	public final static String STATUSBRANCH_PROSESDELIVERY = "D04";
	public final static String STATUSBRANCH_DELIVERED = "D05";

	public final static String STATUS_INVENTORY_SCAN = "IS";

	public final static String STATUS_ORDER_WAITAPPROVALCAB = "000";
	public final static String STATUS_ORDER_WAITAPPROVALWIL = "001";
	public final static String STATUS_ORDER_DECLINECAB = "002";
	public final static String STATUS_ORDER_DECLINEWIL = "003";
	public final static String STATUS_ORDER_DECLINEDIV = "004";
	public final static String STATUS_ORDER_OUTGOINGAPPROVAL = "005";
	public final static String STATUS_ORDER_OUTGOINGDECLINE = "006";
	public final static String STATUS_ORDER_PRODUKSI = "007";
	public final static String STATUS_ORDER_DONE = "008";
	public final static String STATUS_ORDER_WAITSCANPRODUKSI = "009";
	public final static String STATUS_ORDER_REQUESTORDER = "010";
	public final static String STATUS_ORDER_WAITAPPROVAL = "011";
	public final static String STATUS_ORDER_WAITAPPROVALJAL = "012";
	public final static String STATUS_ORDER_WAITAPPROVALOPR = "013";
	public final static String STATUS_ORDER_WAITSCANPRODUKSIOPR = "014";
	public final static String STATUS_ORDER_REJECTED = "015";
	public final static String STATUS_ORDER_DECLINEJAL = "016";

	public final static String STATUS_RETUR_WAITAPPROVAL = "R1";
	public final static String STATUS_RETUR_DECLINE = "R2";
	public final static String STATUS_RETUR_WAITAPPROVALWILAYAH = "R3";
	public final static String STATUS_RETUR_DECLINEAPPROVALWILAYAH = "R4";
	public final static String STATUS_RETUR_WAITAPPROVALPFA = "R5";
	public final static String STATUS_RETUR_APPROVALPFA = "R6";
	public final static String STATUS_RETUR_DECLINEAPPROVALPFA = "R7";
	public final static String STATUS_RETUR_WAITAPPROVALOPR = "R8";
	public final static String STATUS_RETUR_APPROVALOPR = "R9";
	public final static String STATUS_RETUR_DECLINEAPPROVALOPR = "R10";
	public final static String STATUS_RETUR_VENDORPROCESS = "R11";
	public final static String STATUS_RETUR_INPATCH = "R12";
	public final static String STATUS_RETUR_DESTROYED = "R13";
	public final static String STATUS_RETUR_RETURNPFA = "R14";
	public final static String STATUS_RETUR_RETURNEDPFA = "R15";
	public final static String STATUS_RETUR_RECEIVED = "R16";
	public final static String STATUS_RETUR_RETURNOPR = "R17";
	public final static String STATUS_RETUR_PROCESSWIL = "R18";
	public final static String STATUS_RETUR_PROCESSPFA = "R19";

	public final static String STATUS_PRODUCTION_PRODUKSIWAITAPPROVAL = "PI1";
	public final static String STATUS_PRODUCTION_APPROVEDPRODUKSI = "PI2";
	public final static String STATUS_PRODUCTION_DECLINEPRODUKSI = "PI3";

	public final static String STATUS_REPAIR_WAITAPPROVAL = "E1";
	public final static String STATUS_REPAIR_WAITAPPROVALREPAIROPR = "E2";
	public final static String STATUS_REPAIR_DECLINEAPPROVAL = "E3";
	public final static String STATUS_REPAIR_DECLINEREPAIROPR = "E4";
	public final static String STATUS_REPAIR_PROCESSCHECKING = "E5";
	public final static String STATUS_REPAIR_PROCESSOPR = "E6";
	public final static String STATUS_REPAIR_PROCESSVENDOR = "E7";
	public final static String STATUS_REPAIR_PENDINGPROCESS = "E8";
	public final static String STATUS_REPAIR_DONEPROCESS = "E9";
	public final static String STATUS_REPAIR_FAILED = "E10";
	public final static String STATUS_REPAIR_RECEIVED = "E11";

	public final static String STATUS_PERSO_PERSOWAITAPPROVAL = "P11";
	public final static String STATUS_PERSO_PERSODECLINE = "P12";
	public final static String STATUS_PERSO_OUTGOINGWAITAPPROVAL = "P13";
	public final static String STATUS_PERSO_OUTGOINGDECLINE = "P14";
	public final static String STATUS_PERSO_PRODUKSI = "P15";
	public final static String STATUS_PERSO_DONE = "P19";

	public final static String STATUS_DELIVERY_PAKETORDER = "D11";
	public final static String STATUS_DELIVERY_PAKETPROSES = "D12";
	public final static String STATUS_DELIVERY_PAKETDONE = "D19";

	public final static String STATUS_DELIVERY_WAITAPPROVAL = "D20";
	public final static String STATUS_DELIVERY_DELIVERYORDER = "D21";
	public final static String STATUS_DELIVERY_EXPEDITIONORDER = "D22";
	public final static String STATUS_DELIVERY_DELIVERY = "D23";
	public final static String STATUS_DELIVERY_DELIVERED = "D29";

	public final static String STATUS_CLOSED_COMPLAINT = "CC";

	public final static String STATUS_SERIALNO_ENTRY = "S01";
	public final static String STATUS_SERIALNO_SCANINVENTORY = "S02";
	public final static String STATUS_SERIALNO_OUTINVENTORY = "S03";
	public final static String STATUS_SERIALNO_TERPAKAI = "S04";
	public final static String STATUS_SERIALNO_CLOSED = "S05";

	public final static String STATUS_SERIALNO_INJECTED = "S11";
	public final static String STATUS_SERIALNO_SCANPRODUKSI = "S12";
	public final static String STATUS_SERIALNO_OUTPRODUKSI = "S13";
	public final static String STATUS_SERIALNO_DESTROY = "S14";

	public final static String STATUS_SWITCH_WAITAPPROVAL = "SW1";
	public final static String STATUS_SWITCH_WAITAPPROVALPOOL = "SW2";
	public final static String STATUS_SWITCH_HANDOVER = "SW3";
	public final static String STATUS_SWITCH_REJECTEDREQ = "SW4";
	public final static String STATUS_SWITCH_REJECTEDPOOL = "SW5";
	public final static String STATUS_SWITCH_ENTRY = "SW0";
	public final static String STATUS_SWITCH_DECLINEREQ = "SW6";

	public final static String STATUS_CEMTEXT_WAITAPPROVAL = "C01";
	public final static String STATUS_CEMTEXT_UPLOADED = "C02";
	public final static String STATUS_CEMTEXT_UNDEFINED = "C03";
	public final static String STATUS_CEMTEXT_DONE = "C04";
	public final static String STATUS_CEMTEXT_ERRORDATA = "C05";

	public final static String STATUS_AKTIVASI_REISSUESTOP = "RS";
	public final static String STATUS_AKTIVASI_REISSUE = "RI";
	public final static String STATUS_AKTIVASI_RETURNED = "RT";
	public final static String STATUS_AKTIVASI_HOT = "HT";
	public final static String STATUS_AKTIVASI_CLOSED = "CL";
	public final static String STATUS_AKTIVASI_EXPIRED = "EX";
	public final static String STATUS_AKTIVASI_NORMAL = "NR";
	public final static String STATUS_AKTIVASI_WARM = "WR";
	public final static String STATUS_AKTIVASI_UNKNOWN = "UN";

	public final static String STATUS_AKTIVASI_UNUSED = "1";
	public final static String STATUS_AKTIVASI_REJECT = "2";
	public final static String STATUS_AKTIVASI_USED = "8";
	public final static String STATUS_AKTIVASI_TUTUP = "9";

	public final static int STATUS_ORDER_ENTRY = 0;
	public final static int STATUS_ORDER_SCAN = 1;
	public final static int STATUS_ORDER_CROP = 2;
	public final static int STATUS_ORDER_MERGE = 3;
	public final static int STATUS_ORDER_PERSO = 4;

	public final static String ENTRYTYPE_BATCH = "B";
	public final static String ENTRYTYPE_MANUAL = "P";
	public final static String ENTRYTYPE_MANUAL_BRANCH = "C";
	public final static String ENTRYTYPE_COMBO = "O";

	public static final String SCHEDULER_ENABLE_LABEL = "ENABLE";
	public static final String SCHEDULER_ENABLE_VALUE = "1";
	public static final String SCHEDULER_DISABLE_LABEL = "DISABLE";
	public static final String SCHEDULER_DISABLE_VALUE = "0";
	public static final String SCHEDULER_REPEAT_PERMINUTE = "PER MINUTE";
	public static final String SCHEDULER_REPEAT_ATHOUR = "AT HOUR";

	public static final String EXPEDISI_GAIDO = "GAIDO";
	public static final String EXPEDISI_PANDU = "PSS";
	public static final String EXPEDISI_NCS = "NCS";
	public static final String EXPEDISI_DLM = "DLM";

	public static final String SLACOUNTERTYPE_DATEORDER = "D";
	public static final String SLACOUNTERTYPE_DATEPRODUCTION = "P";
	public static final String SLACOUNTERTYPE_NOCOUNTING = "N";

	public static final String ESTIMATESTOCK_COUNTING = "Y";
	public static final String ESTIMATESTOCK_NOCOUNTING = "N";

	public static final String LETTERTYPE_REG = "REG";
	public static final String LETTERTYPE_TREG = "TREG";
	public static final String LETTERTYPE_PREG = "PREG";
	public static final String LETTERTYPE_BWD = "BWD";
	public static final String LETTERTYPE_PRE = "PRE";
	public static final String LETTERTYPE_PRY = "PRY";
	public static final String LETTERTYPE_SR = "SR";
	public static final String LETTERTYPE_TBG = "TBG";
	public static final String LETTERTYPE_DEPO = "DEPO";
	public static final String LETTERTYPE_GIRO = "GIRO";
	public static final String LETTERTYPE_WKT = "WKT";
	public static final String LETTERTYPE_TKN = "TKN";

	public static final String NOTIF_TYPE_MENU = "M";
	public static final String NOTIF_TYPE_ALERT = "A";
	public static final String NOTIF_ORDERAPPROVAL_ID = "N01";
	public static final String NOTIF_ORDERAPPROVAL_MENUGROUP = "Kartu";
	public static final String NOTIF_ORDERAPPROVAL_MENUSUBGROUP = NOTIF_ORDERAPPROVAL_MENUGROUP + "Kartu";
	public static final String NOTIF_ORDERAPPROVAL_MENUNAME = NOTIF_ORDERAPPROVAL_MENUGROUP + "Approval Order";
	public static final String NOTIF_ORDERAPPROVAL_LABEL = "Approval Order";
	public static final String NOTIF_PRODAPPROVAL_ID = "N02";
	public static final String NOTIF_PRODAPPROVAL_MENUGROUP = "Kartu";
	public static final String NOTIF_PRODAPPROVAL_MENUSUBGROUP = NOTIF_PRODAPPROVAL_MENUGROUP + "Kartu";
	public static final String NOTIF_PRODAPPROVAL_MENUNAME = NOTIF_PRODAPPROVAL_MENUGROUP + "Approval Perso";
	public static final String NOTIF_PRODAPPROVAL_LABEL = "Approval Perso";
	public static final String NOTIF_INCOMINGAPPROVAL_ID = "N11";
	public static final String NOTIF_INCOMINGAPPROVAL_MENUGROUP = "Inventory";
	public static final String NOTIF_INCOMINGAPPROVAL_MENUSUBGROUP = NOTIF_INCOMINGAPPROVAL_MENUGROUP + "Kartu";
	public static final String NOTIF_INCOMINGAPPROVAL_MENUNAME = NOTIF_INCOMINGAPPROVAL_MENUGROUP
			+ "Approval Incoming Kartu";
	public static final String NOTIF_INCOMINGAPPROVAL_LABEL = "Approval Incoming Kartu";
	public static final String NOTIF_OUTGOINGAPPROVAL_ID = "N12";
	public static final String NOTIF_OUTGOINGAPPROVAL_MENUGROUP = "Inventory";
	public static final String NOTIF_OUTGOINGAPPROVAL_MENUSUBGROUP = NOTIF_OUTGOINGAPPROVAL_MENUGROUP + "Kartu";
	public static final String NOTIF_OUTGOINGAPPROVAL_MENUNAME = NOTIF_OUTGOINGAPPROVAL_MENUGROUP
			+ "Approval Outgoing Kartu";
	public static final String NOTIF_OUTGOINGAPPROVAL_LABEL = "Approval Outgoing Kartu";

	public static final String NOTIF_BLOCKSTOCKPAGUAPPROVAL_ID = "N13";
	public static final String NOTIF_BLOCKSTOCKPAGUAPPROVAL_MENUGROUP = "Inventory";
	public static final String NOTIF_BLOCKSTOCKPAGUAPPROVAL_MENUSUBGROUP = NOTIF_BLOCKSTOCKPAGUAPPROVAL_MENUGROUP
			+ "Kartu";
	public static final String NOTIF_BLOCKSTOCKPAGUAPPROVAL_MENUNAME = NOTIF_BLOCKSTOCKPAGUAPPROVAL_MENUGROUP
			+ "Approval Block Pagu";
	public static final String NOTIF_BLOCKSTOCKPAGUAPPROVAL_LABEL = "Approval Block Pagu";

	public static final String ALERT_STOCKPAGU_ID = "A01";
	public static final String ALERT_UNEOD_ID = "A02";
	public static final String ALERT_ORDEROUTSTANDING_ID = "A03";
	public static final String[] COLOR = { "#00cc00", "#e6e600", "#cc0000" };

	public static final String PROSES_INCOMING = "INC";
	public static final String PROSES_OUTGOING = "OUT";
	public static final String PROSES_ORDER = "ODR";
	public static final String PROSES_PERSO = "PRS";
	public static final String PROSES_PAKET = "PKT";
	public static final String PROSES_DELIVERY = "DLV";
	public static final String PROSES_POD = "POD";
	public static final String PROSES_ROLLBACK = "RLB";

	public static final String PINMAILERTYPE_REG = "REGULAR";
	public static final String PINMAILERTYPE_SYA = "SYARIAH";

	public static final String DOKUMEN_JAMINAN = "01";
	public static final String DOKUMEN_GARANSI = "02";

}
