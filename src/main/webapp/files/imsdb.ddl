CREATE SEQUENCE mproductgroup_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE mbranchproductgroup_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tbookfile_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tplan_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tplanproduct_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE ttokenitem_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tpinpaditem_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tsecuritiesitem_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tordermemo_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE torderitem_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tordertrack_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tdestroy_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tdestroyitem_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tdestroymemo_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tswitch_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tswitchitem_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE tswitchmemo_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE trepair_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE trepairitem_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE trepairmemo_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE treturnitem_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
CREATE SEQUENCE treturnmemo_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;

CREATE TABLE TRETURN (
	TRETURNPK INTEGER NOT NULL,
	MBRANCHFK INTEGER,	
	MPRODUCTFK INTEGER,
	REGID CHAR(20) DEFAULT '',
	RETURNLEVEL INTEGER DEFAULT 0,
	ITEMQTY INTEGER DEFAULT 0,
	STATUS CHAR(2) DEFAULT '',
	INSERTEDBY CHAR(40) DEFAULT '',
	INSERTTIME TIMESTAMP,
	DECISIONBY CHAR(40) DEFAULT '',
	DECISIONTIME TIMESTAMP,
	PRIMARY KEY (TRETURNPK),
	CONSTRAINT TRETURN_FK1 FOREIGN KEY (MBRANCHFK) REFERENCES MBRANCH (MBRANCHPK) ON DELETE RESTRICT,
	CONSTRAINT TRETURN_FK2 FOREIGN KEY (MPRODUCTFK) REFERENCES MPRODUCT (MPRODUCTPK) ON DELETE RESTRICT
);

CREATE TABLE TRETURNITEM (
	TRETURNITEMPK INTEGER NOT NULL,
	TRETURNFK INTEGER,	
	ITEMNO CHAR(20) DEFAULT '',
	ITEMSTATUS CHAR(2) DEFAULT '',	
	PRIMARY KEY (TRETURNITEMPK),
	CONSTRAINT TRETURNITEM_FK1 FOREIGN KEY (TRETURNFK) REFERENCES TRETURN (TRETURNPK) ON DELETE CASCADE
);

CREATE TABLE TRETURNMEMO (
	TRETURNMEMOPK INTEGER NOT NULL,
	TRETURNFK INTEGER,
	MEMO CHAR(200) DEFAULT '',
	MEMOBY CHAR(40) DEFAULT '',
	MEMOTIME TIMESTAMP,	
	PRIMARY KEY (TRETURNMEMOPK), 
	CONSTRAINT TRETURNMEMO_FK1 FOREIGN KEY (TRETURNFK) REFERENCES TRETURN (TRETURNPK) ON DELETE CASCADE
);

CREATE TABLE TRETURNTRACK (
	TRETURNTRACKPK INTEGER NOT NULL,
	TRETURNFK INTEGER,
	TRACKTIME TIMESTAMP,
	TRACKSTATUS CHAR(2) DEFAULT '',
	TRACKDESC CHAR(100) DEFAULT '',
	PRIMARY KEY (TRETURNTRACKPK),
	CONSTRAINT TRETURNTRACK_FK1 FOREIGN KEY (TRETURNFK) REFERENCES TRETURN (TRETURNPK) ON DELETE CASCADE
);

CREATE TABLE TPLAN(
	TPLANPK INTEGER NOT NULL,	
	MBRANCHFK INTEGER,
	PRODUCTGROUP CHAR(2) DEFAULT '',
	PLANNO CHAR(20) DEFAULT '',
	MEMONO CHAR(20) DEFAULT '',
	MEMODATE DATE,
	MEMOFILEORI CHAR(100) DEFAULT '',
	MEMOFILEID CHAR(100) DEFAULT '',
	ANGGARAN DECIMAL(19,2) DEFAULT 0,
	TOTALQTY INTEGER DEFAULT 0,
	STATUS CHAR(3) DEFAULT '',
	INPUTER CHAR(40) DEFAULT '',
	INPUTTIME TIMESTAMP,
	DECISIONBY CHAR(40) DEFAULT '',
	DECISIONTIME TIMESTAMP,
	DECISIONDESC CHAR(200) DEFAULT '',
	PRIMARY KEY (TPLANPK),
	CONSTRAINT TPLAN_FK1 FOREIGN KEY (MBRANCHFK) REFERENCES MBRANCH (MBRANCHPK) ON 
DELETE 
    RESTRICT
);

CREATE TABLE TPLANPRODUCT(
	TPLANPRODUCTPK INTEGER NOT NULL,
	TPLANFK INTEGER,
	MPRODUCTTYPEFK INTEGER,
	UNITQTY INTEGER DEFAULT 0,
	PRIMARY KEY (TPLANPRODUCTPK),
	CONSTRAINT TPLANPRODUCT_FK1 FOREIGN KEY (TPLANFK) REFERENCES TPLAN (TPLANPK) ON 
DELETE 
    CASCADE,
	CONSTRAINT TPLANPRODUCT_FK2 FOREIGN KEY (MPRODUCTTYPEFK) REFERENCES MPRODUCTTYPE (MPRODUCTTYPEPK) ON 
DELETE 
    RESTRICT
);

	
CREATE TABLE 
    TTOKENITEM 
    ( 
        TTOKENITEMPK   INTEGER NOT NULL, 
        TINCOMINGFK      INTEGER, 
        ITEMNO         CHARACTER(20) DEFAULT ''::bpchar, 
        ITEMNOINJECT CHARACTER(20) DEFAULT ''::bpchar, 
		STATUS CHAR(3) DEFAULT '',
        PRIMARY KEY (TTOKENITEMPK),         
    CONSTRAINT TTOKENITEM_FK1 FOREIGN KEY (TINCOMINGFK) REFERENCES "tincoming" ("tincomingpk") 
ON 
DELETE 
    CASCADE,
    UNIQUE (ITEMNO) 
    );
	
CREATE TABLE 
    TPINPADITEM 
    ( 
        TPINPADITEMPK   INTEGER NOT NULL, 
        TINCOMINGFK      INTEGER, 
        ITEMNO         CHARACTER(20) DEFAULT ''::bpchar,         
		STATUS CHAR(3) DEFAULT '',
        PRIMARY KEY (TPINPADITEMPK),         
    CONSTRAINT TPINPADITEM_FK1 FOREIGN KEY (TINCOMINGFK) REFERENCES "tincoming" ("tincomingpk") 
ON 
DELETE 
    CASCADE,
    UNIQUE (ITEMNO) 
    );
	
CREATE TABLE 
    TSECURITIESITEM 
    ( 
        TSECURITIESITEMPK   INTEGER NOT NULL, 
        TINCOMINGFK      INTEGER, 
        ITEMNO         CHARACTER(20) DEFAULT ''::bpchar,         
		STATUS CHAR(3) DEFAULT '',
        PRIMARY KEY (TSECURITIESITEMPK),         
    CONSTRAINT TSECURITIESITEM_FK1 FOREIGN KEY (TINCOMINGFK) REFERENCES "tincoming" ("tincomingpk") 
ON 
DELETE 
    CASCADE,
    UNIQUE (ITEMNO) 
    );

CREATE TABLE TORDERMEMO (
	TORDERMEMOPK INTEGER NOT NULL,
	TORDERFK INTEGER,
	MEMO CHAR(200) DEFAULT '',
	MEMOBY CHAR(40) DEFAULT '',
	MEMOTIME TIMESTAMP,	
	PRIMARY KEY (TORDERMEMOPK), 
	CONSTRAINT TORDERMEMO_FK1 FOREIGN KEY (TORDERFK) REFERENCES TORDER (TORDERPK) ON DELETE CASCADE
);

CREATE TABLE TORDERITEM (
	TORDERITEMPK INTEGER NOT NULL,
	TORDERFK INTEGER,
	TTOKENITEMFK INTEGER,
	TPINPADITEMFK INTEGER,
	TSECURITIESITEMFK INTEGER,
	PRODUCTGROUP CHAR(2) DEFAULT '',
	ITEMNO CHAR(20) DEFAULT '',	
	PRIMARY KEY (TORDERITEMPK), 
	CONSTRAINT TORDERITEM_FK1 FOREIGN KEY (TORDERFK) REFERENCES TORDER (TORDERPK) ON DELETE CASCADE, 
	CONSTRAINT TORDERITEM_FK2 FOREIGN KEY (TTOKENITEMFK) REFERENCES TTOKENITEM (TTOKENITEMPK) ON DELETE RESTRICT,
	CONSTRAINT TORDERITEM_FK3 FOREIGN KEY (TPINPADITEMFK) REFERENCES TPINPADITEM (TPINPADITEMPK) ON DELETE RESTRICT,
	CONSTRAINT TORDERITEM_FK4 FOREIGN KEY (TSECURITIESITEMFK) REFERENCES TSECURITIESITEM (TSECURITIESITEMPK) ON DELETE RESTRICT
);

CREATE TABLE TORDERTRACK (
	TORDERTRACKPK INTEGER NOT NULL,
	TORDERFK INTEGER,
	TRACKTIME TIMESTAMP,
	TRACKSTATUS CHAR(3) DEFAULT '',
	TRACKDESC CHAR(100) DEFAULT '',
	PRIMARY KEY (TORDERTRACKPK),
	CONSTRAINT TORDERTRACK_FK1 FOREIGN KEY (TORDERFK) REFERENCES TORDER (TORDERPK) ON DELETE CASCADE
);

CREATE TABLE TDESTROY (
	TDESTROYPK INTEGER NOT NULL,
	MBRANCHFK INTEGER,	
	MPRODUCTFK INTEGER,
	REGID CHAR(20) DEFAULT '',
	ITEMQTY INTEGER DEFAULT 0,
	STATUS CHAR(3) DEFAULT '',
	INSERTEDBY CHAR(40) DEFAULT '',
	INSERTTIME TIMESTAMP,
	DECISIONBY CHAR(40) DEFAULT '',
	DECISIONTIME TIMESTAMP,
	PRIMARY KEY (TDESTROYPK),
	CONSTRAINT TDESTROY_FK1 FOREIGN KEY (MBRANCHFK) REFERENCES MBRANCH (MBRANCHPK) ON DELETE RESTRICT
);

CREATE TABLE TDESTROYITEM (
	TDESTROYITEMPK INTEGER NOT NULL,
	TDESTROYFK INTEGER,
	ITEMNO CHAR(20) DEFAULT '',
	ITEMSTATUS CHAR(3) DEFAULT '',	
	PRIMARY KEY (TDESTROYITEMPK),
	CONSTRAINT TDESTROYITEMPK_FK1 FOREIGN KEY (TDESTROYFK) REFERENCES TDESTROY (TDESTROYPK) ON DELETE CASCADE
);

CREATE TABLE TDESTROYMEMO (
	TDESTROYMEMOPK INTEGER NOT NULL,
	TDESTROYFK INTEGER,
	MEMO CHAR(200) DEFAULT '',
	MEMOBY CHAR(40) DEFAULT '',
	MEMOTIME TIMESTAMP,
	PRIMARY KEY (TDESTROYMEMOPK), 
	CONSTRAINT TDESTROYMEMO_FK1 FOREIGN KEY (TDESTROYFK) REFERENCES TDESTROY (TDESTROYPK) ON DELETE CASCADE
);

CREATE TABLE TSWITCH (
	TSWITCHPK INTEGER NOT NULL,
	MBRANCHFK INTEGER,	
	MPRODUCTFK INTEGER,
	BRANCHIDREQ CHAR(3) DEFAULT '',
	OUTLETREQ CHAR(2) DEFAULT '',
	BRANCHIDPOOL CHAR(3) DEFAULT '',
	OUTLETPOOL CHAR(2) DEFAULT '',
	REGID CHAR(20) DEFAULT '',
	ITEMQTY INTEGER DEFAULT 0,
	STATUS CHAR(3) DEFAULT '',
	INSERTEDBY CHAR(40) DEFAULT '',
	INSERTTIME TIMESTAMP,
	DECISIONBY CHAR(40) DEFAULT '',
	DECISIONTIME TIMESTAMP,
	PRIMARY KEY (TSWITCHPK),
	CONSTRAINT TSWITCHPK_FK1 FOREIGN KEY (MBRANCHFK) REFERENCES MBRANCH (MBRANCHPK) ON DELETE RESTRICT,
	CONSTRAINT TSWITCHPK_FK2 FOREIGN KEY (MPRODUCTFK) REFERENCES MPRODUCT (MPRODUCTPK) ON DELETE RESTRICT
);

CREATE TABLE TSWICTHITEM (
	TSWICTHITEMPK INTEGER NOT NULL,
	TSWITCHFK INTEGER,
	ITEMNO CHAR(20) DEFAULT '',
	PRIMARY KEY (TSWICTHITEMPK),
	CONSTRAINT TSWICTHITEM_FK1 FOREIGN KEY (TSWITCHFK) REFERENCES TSWITCH (TSWITCHPK) ON DELETE CASCADE
);

CREATE TABLE TSWITCHMEMO (
	TSWITCHMEMOPK INTEGER NOT NULL,
	TSWITCHFK INTEGER,
	MEMO CHAR(200) DEFAULT '',
	MEMOBY CHAR(40) DEFAULT '',
	MEMOTIME TIMESTAMP,
	PRIMARY KEY (TSWITCHMEMOPK), 
	CONSTRAINT TSWITCHMEMO_FK1 FOREIGN KEY (TSWITCHFK) REFERENCES TSWITCH (TSWITCHPK) ON DELETE CASCADE
);

CREATE TABLE TREPAIR (
	TREPAIRPK INTEGER NOT NULL,
	MBRANCHFK INTEGER,	
	MPRODUCTFK INTEGER,
	REGID CHAR(20) DEFAULT '',
	ITEMQTY INTEGER DEFAULT 0,
	STATUS CHAR(3) DEFAULT '',
	INSERTEDBY CHAR(40) DEFAULT '',
	INSERTTIME TIMESTAMP,
	DECISIONBY CHAR(40) DEFAULT '',
	DECISIONTIME TIMESTAMP,
	PRIMARY KEY (TREPAIRPK),
	CONSTRAINT TREPAIR_FK1 FOREIGN KEY (MBRANCHFK) REFERENCES MBRANCH (MBRANCHPK) ON DELETE RESTRICT,
	CONSTRAINT TREPAIR_FK2 FOREIGN KEY (MPRODUCTFK) REFERENCES MPRODUCT (MPRODUCTPK) ON DELETE RESTRICT
);

CREATE TABLE TREPAIRITEM (
	TREPAIRITEMPK INTEGER NOT NULL,
	TREPAIRFK INTEGER,	
	ITEMNO CHAR(20) DEFAULT '',
	ITEMSTATUS CHAR(3) DEFAULT '',	
	PRIMARY KEY (TREPAIRITEMPK),
	CONSTRAINT TREPAIRITEM_FK1 FOREIGN KEY (TREPAIRFK) REFERENCES TREPAIR (TREPAIRPK) ON DELETE CASCADE
);

CREATE TABLE TREPAIRMEMO (
	TREPAIRMEMOPK INTEGER NOT NULL,
	TREPAIRFK INTEGER,
	MEMO CHAR(200) DEFAULT '',
	MEMOBY CHAR(40) DEFAULT '',
	MEMOTIME TIMESTAMP,
	PRIMARY KEY (TREPAIRMEMOPK), 
	CONSTRAINT TREPAIRMEMO_FK1 FOREIGN KEY (TREPAIRFK) REFERENCES TREPAIR (TREPAIRPK) ON DELETE CASCADE
);

CREATE TABLE 
    mproductgroup 
    ( 
        mproductgrouppk  INTEGER NOT NULL, 
        productgroupcode CHARACTER(2) DEFAULT ''::bpchar, 
        productgroup     CHARACTER(40) DEFAULT ''::bpchar, 
        iscoa            CHARACTER(1), 
        PRIMARY KEY (mproductgrouppk) 
    );

CREATE TABLE 
    mbranchproductgroup 
    ( 
        mbranchproductgrouppk INTEGER NOT NULL, 
        mbranchfk             INTEGER, 
        mproductgroupfk       INTEGER, 
        PRIMARY KEY (mbranchproductgrouppk), 
        CONSTRAINT mbranchproductgroup_fk1 FOREIGN KEY (mbranchfk) REFERENCES "mbranch" 
        ("mbranchpk") ON 
DELETE 
    CASCADE, 
    CONSTRAINT mbranchproductgroup_fk2 FOREIGN KEY (mproductgroupfk) REFERENCES "mproductgroup" 
    ("mproductgrouppk") 
ON 
DELETE 
    CASCADE 
    );
    
    CREATE TABLE TBOOKFILE (
	TBOOKFILEPK INTEGER NOT NULL,
	TDELIVERYFK INTEGER,
	BOOKID CHAR(35) DEFAULT '',
	TOTALDATA INTEGER DEFAULT 0,
	TOTALAMOUNT DECIMAL(17,0) DEFAULT 0,
	BOOKTIME TIMESTAMP,
	BOOKEDBY CHAR(40) DEFAULT '',
	STATUS CHAR(2) DEFAULT '',
	STATUSDESC CHAR(200) DEFAULT '',
	PRIMARY KEY (TBOOKFILEPK)
);
ALTER TABLE TBOOKFILE ADD CONSTRAINT TBOOKFILE_FK1 FOREIGN KEY (TDELIVERYFK) REFERENCES TDELIVERY (TDELIVERYPK) ON DELETE RESTRICT;

ALTER TABLE MBRANCH ADD COLUMN BRANCHLEVEL INTEGER;
ALTER TABLE TPLAN ADD COLUMN TOTALDATA INTEGER DEFAULT 0;
ALTER TABLE TINCOMING ADD COLUMN MBRANCHFK INTEGER;
ALTER TABLE TINCOMING ADD CONSTRAINT TINCOMING_FK3 FOREIGN KEY (MBRANCHFK) REFERENCES MBRANCH (MBRANCHPK) ON DELETE RESTRICT;
ALTER TABLE TINCOMING ADD COLUMN HARGA DECIMAL(19,2) DEFAULT 0;
ALTER TABLE TORDER ADD COLUMN TOTALPROSES INTEGER DEFAULT 0;
ALTER TABLE TORDERITEM ADD COLUMN TID CHAR(20) DEFAULT '';
ALTER TABLE TORDERITEM ADD COLUMN MID CHAR(20) DEFAULT '';
ALTER TABLE TORDERITEM ADD COLUMN PINPADTYPE CHAR(1) DEFAULT '';
ALTER TABLE TORDERITEM ADD COLUMN PINPADMEMO CHAR(100) DEFAULT '';
ALTER TABLE TORDERITEM ADD COLUMN NUMERATOR INTEGER;
ALTER TABLE TORDERITEM ADD COLUMN ITEMPRICE DECIMAL(6,0) DEFAULT 0;
ALTER TABLE TSECURITIESITEM ADD COLUMN NUMERATOR INTEGER;
ALTER TABLE TBRANCHSTOCKITEM ADD COLUMN NUMERATOR INTEGER;
ALTER TABLE TBRANCHSTOCKITEM ADD COLUMN ITEMPRICE DECIMAL(6,0) DEFAULT 0;
ALTER TABLE TRETURN ADD COLUMN LETTERTYPE CHAR(10) DEFAULT '';
ALTER TABLE TRETURN ADD COLUMN MRETURNREASONFK INTEGER;
ALTER TABLE TRETURN ADD CONSTRAINT TRETURN_FK3 FOREIGN KEY (MRETURNREASONFK) REFERENCES MRETURNREASON (MRETURNREASONPK) ON DELETE RESTRICT;
ALTER TABLE MRETURNREASON ADD COLUMN ISDESTROY CHAR(1) DEFAULT '';
ALTER TABLE TSWITCH ADD COLUMN TORDERFK INTEGER;
ALTER TABLE TSWITCH ADD CONSTRAINT TSWITCH_FK3 FOREIGN KEY (TORDERFK) REFERENCES TORDER (TORDERPK) ON DELETE CASCADE;
ALTER TABLE TPAKET ADD COLUMN TRETURNFK INTEGER;
ALTER TABLE TPAKET ADD CONSTRAINT TPAKET_FK6 FOREIGN KEY (TRETURNFK) REFERENCES TRETURN (TRETURNPK) ON DELETE CASCADE;
ALTER TABLE TPAKET ADD COLUMN TSWITCHFK INTEGER;
ALTER TABLE TPAKET ADD CONSTRAINT TPAKET_FK7 FOREIGN KEY (TSWITCHFK) REFERENCES TSWITCH (TSWITCHPK) ON DELETE CASCADE;
ALTER TABLE TPAKET ADD COLUMN BRANCHPOOL CHAR(3);
ALTER TABLE TDELIVERY ADD COLUMN ISRETURN CHAR(1);
ALTER TABLE TDELIVERY ADD COLUMN BRANCHPOOL CHAR(3);
ALTER TABLE TNOTIF ADD COLUMN BRANCHLEVEL INTEGER;
ALTER TABLE TNOTIF ADD COLUMN PRODUCTGROUP CHAR(2);
ALTER TABLE TNOTIF ADD COLUMN MBRANCHFK INTEGER;
ALTER TABLE TNOTIF ADD COLUMN NOTIFTIME TIMESTAMP(6) WITHOUT TIME ZONE;
ALTER TABLE TNOTIF ADD CONSTRAINT TNOTIF_FK2 FOREIGN KEY (MBRANCHFK) REFERENCES MBRANCH (MBRANCHPK) ON DELETE RESTRICT;
ALTER TABLE TREPAIR ADD COLUMN MRETURNREASONFK INTEGER;
ALTER TABLE TREPAIR ADD CONSTRAINT TREPAIR_FK3 FOREIGN KEY (MRETURNREASONFK) REFERENCES MRETURNREASON (MRETURNREASONPK) ON DELETE RESTRICT;
ALTER TABLE MPRODUCTTYPE ADD COLUMN COANO CHAR(20);
ALTER TABLE TORDER ADD COLUMN TOTALPROSES INTEGER;
ALTER TABLE TORDER ADD COLUMN ORDERDATE DATE;
ALTER TABLE TORDER ADD COLUMN ORDERLEVEL INTEGER;
ALTER TABLE TORDER ADD COLUMN ORDEROUTLET CHAR(2);
ALTER TABLE TOUTGOING ADD COLUMN TREPAIRFK INTEGER;
ALTER TABLE TOUTGOING ADD CONSTRAINT TOUTGOING_FK6 FOREIGN KEY (TREPAIRFK) REFERENCES TREPAIR (TREPAIRPK) ON DELETE CASCADE;