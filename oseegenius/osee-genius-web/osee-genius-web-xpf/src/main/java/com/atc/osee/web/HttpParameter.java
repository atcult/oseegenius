/*
 	Copyright (c) 2010 @Cult s.r.l. All rights reserved.
	
	@Cult s.r.l. makes no representations or warranties about the 
	suitability of the software, either express or implied, including
	but not limited to the implied warranties of merchantability, fitness
	for a particular purpose, or non-infringement. 
	
	@Cult s.r.l.not be liable for any damage suffered by 
	licensee as a result of using, modifying or distributing this software 
	or its derivates.
	
	This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.web;

/**
 * Enumeration of all OseeGenius -W- HTTP parameters.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface HttpParameter 
{
	String PAGE_SIZE_IN_ADVANCED_SEARCH = "num";
	String SORT_IN_ADVANCED_SEARCH = "sort";
	String ATTACHMENT_TYPE = "attachment";
	String SECTION = "section";
	String COMMAND = "command";
	String CURRENT_PAGE_URL = "location";
	String TAG = "tag";
	String COVER_SIZE = "size";
	String IMAGE_URL = "url";
	String BRANCH = "branch";
	String ID = "id";
	String REVIEW_TEXT = "reviewText";
	String LANGUAGE = "l";
	String WRITE_MODE = "w";
	String VIEW_MODE = "v";
	String URI = "uri";
	String RECID = "recid";
	String OFFSET = "offset";	
	String QUERY = "q";
	String ORDER_BY = "o";
	String PAGE_INDEX = "p";
	String PAGE_SIZE = "s";	
	String QUERY_TYPE = "h";
	String TAB_ID = "t";
	String FILTER_BY = "f";
	String ACTION = "a";
	String CAN_BE_EXPORTED_OR_DOWNLOADED = "cbs";
	String RECIPIENTS = "recipients";
	String SUBJECT = "subject";
	String MESSAGE = "message";
	String RESOURCE_ID = "resourceId";
	String ADVANCED_SEARCH_ORDER_BY = "orderBy";
	String ADVANCED_SEARCH_PAGE_SIZE = "pageSize";	
	String ADVANCED_SEARCH_WHAT_SUFFIX = "what_";
	String ADVANCED_SEARCH_WHERE_SUFFIX = "where_";
	String ADVANCED_SEARCH_BOOLEAN_OPERATOR_SUFFIX = "booleanOperator_";
	String INDEX = "index";
	String PERSPECTIVE = "perspective";
	String INDEX_NAME = "i";
	String FROM = "from";
	String POSITION_IN_RESPONSE = "pos";
	String CHECK_BOX_TRUE = "on";
	String ISBN = HttpAttribute.ISBN;
	String BUNDLE = "b";
	String LOGICAL_VIEW = HttpAttribute.LOGICAL_VIEW;
	String SELECTION_CONTEXT = "sctx";
	String FOLDER_SELECTION_CONTEXT = "folder";
	String FORMAT = "format";
	String GEO_SFIELD = "sfield";
	String GEO_PT = "pt";
	String GEO_DISTANCE = "d";
	String NAME = "name";
	String WISH_LIST_ID = "wid";
	String TAG_ID = "tagid";
	String COPY_ID = "copy";
	String FILTER = "filter";
	String USERNAME = "username";
	String MAIN_LIBRARY_ID = "mlid";
	String CATEGORY_CODE = "cc";
	String FILTER_BY_VALUE = "fv";	
	String DIRECTION = "dir";
	String SERVICE_TYPE = "serviceType";
	String PHONE = "phone";
	String ADDRESS = "address";
	String RESOURCE_TITLE = "resourceTitle";
	String PUBLISHER = "publisher";
	String YEAR = "year";
	String AUTHOR = "author";
	String DATE = "date";
	String PURPOSE = "purpose";
	String USER_EMAIL = "user_email";
	String CARD_NUMBER = "cardNumber";
	String PERMANENT_FILTER_PAR = "permf";
	String AUTHORITY_GROUP_URI= "authGroupId";
	String AN = "id";
	String COLLOCATION = "collocation";
	String BIBLIOGRAPHIC_LEVEL = "bibliographicLevel";
	String VOLUME = "volume";
	String FASCICOLO = "fascicolo";
	String PAGES = "pages";
	String PREVIOUS_QUERY_DONE = "previousQueryDone";
}