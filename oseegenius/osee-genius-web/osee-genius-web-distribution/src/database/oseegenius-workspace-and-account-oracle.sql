CREATE TABLE OSEEGENIUS.BIBLIOGRAPHY (
  ID NUMBER(19) NOT NULL,
  ITEM_ID VARCHAR(50) DEFAULT NULL,
  USER_ID NUMBER(11) NOT NULL,
  CONSTRAINT BIBLIOGRAPHY_PK PRIMARY KEY (ID)
    USING INDEX TABLESPACE BBL_IDX1
)
TABLESPACE BBL1;

CREATE TABLE OSEEGENIUS.WISH_LIST (
  ID NUMBER(19) NOT NULL,
  NAME VARCHAR(50) NOT NULL,
  CREATION_DATE DATE DEFAULT SYSDATE,
  USER_ID NUMBER(11) NOT NULL,
  CONSTRAINT WISH_LIST_PK PRIMARY KEY (ID)
    USING INDEX TABLESPACE BBL_IDX1
)
TABLESPACE BBL1;

CREATE TABLE OSEEGENIUS.WISH_LIST_DOC (
  ITEM_ID VARCHAR(50) NOT NULL,
  WISH_LIST_ID NUMBER(19) NOT NULL,
  CONSTRAINT WL_DOC_PK PRIMARY KEY (ITEM_ID,WISH_LIST_ID)
   USING INDEX TABLESPACE BBL_IDX1,
  CONSTRAINT FK_WISH_LIST_DOC_WISH_LIST1 FOREIGN KEY (WISH_LIST_ID) REFERENCES WISH_LIST (ID)
)
TABLESPACE BBL1;

CREATE TABLE OSEEGENIUS.REVIEW (
  ID NUMBER(19) NOT NULL,
  TBR CHAR(1) DEFAULT '1',
  ITEM_ID VARCHAR(50) NOT NULL,
  CREATION_DATE DATE DEFAULT SYSDATE,
  REVIEW VARCHAR2(1000) NOT NULL,
  USER_ID NUMBER(11) NOT NULL,
  CONSTRAINT REVIEW_PK PRIMARY KEY (ID)
    USING INDEX TABLESPACE BBL_IDX1
)
TABLESPACE BBL1; 

CREATE TABLE OSEEGENIUS.TAG (
  ID NUMBER(19) NOT NULL,
  LABEL VARCHAR(50) NOT NULL,
  CONSTRAINT TAG_PK PRIMARY KEY (ID)
    USING INDEX TABLESPACE BBL_IDX1
)
TABLESPACE BBL1; 

CREATE TABLE OSEEGENIUS.TAG_ASSOCIATION (
  TAG_ID NUMBER(19) NOT NULL,
  USER_ID NUMBER(11) NOT NULL,
  ITEM_ID VARCHAR(45) NOT NULL,
  CONSTRAINT TAG_ASSOCIATION_PK PRIMARY KEY (TAG_ID,USER_ID,ITEM_ID)
    USING INDEX TABLESPACE BBL_IDX1,
  CONSTRAINT FK_TAG FOREIGN KEY (TAG_ID) REFERENCES TAG (ID)
)
TABLESPACE BBL1;

CREATE TABLE OSEEGENIUS.OSEEGENIUS_USER (
  ID NUMBER(11) NOT NULL,
  USERNAME VARCHAR(50) NOT NULL,
  PASSWORD VARCHAR(50) NOT NULL,
  CONSTRAINT OG_USER_PK PRIMARY KEY (ID)
    USING INDEX TABLESPACE BBL_IDX1
)
TABLESPACE BBL1;

CREATE TABLE OSEEGENIUS.MY_ACCOUNT (
  ID NUMBER(11) NOT NULL,
  NAME VARCHAR(50) NOT NULL,
  SURNAME VARCHAR(50) NOT NULL,
  GENDER CHAR(1) NOT NULL,
  ZIP_CODE VARCHAR(45),
  STREET VARCHAR(45),
  CITY VARCHAR(45),
  STATE VARCHAR(45),
  INTRO VARCHAR(45),
  OSEEGENIUS_USER_ID NUMBER(11) NOT NULL,
  CONSTRAINT MY_ACCOUNT_PK PRIMARY KEY (ID)
    USING INDEX TABLESPACE BBL_IDX1,
  CONSTRAINT FK_MY_ACCOUNT_OSEEGENIUS_USER1 FOREIGN KEY (OSEEGENIUS_USER_ID) REFERENCES OSEEGENIUS_USER (ID)
)
TABLESPACE BBL1;  