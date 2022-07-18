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
 * Enumerative interface for all OseeGenius -W- constants.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface IConstants 
{
	String MODULE_NAME = "OSEE-GENIUS-W";

	String NA = "NA";
	String UTF_8 = "UTF-8";
	String OPEN_PARENTHESIS = "(";
	String CLOSE_PARENTHESIS = ")";
	String STAR = "*";
	
	String COLON = ":";
	String BLANK = " ";
	String DOUBLE_QUOTES = "\"";
	String COMMA = ",";

	String EMPTY_STRING = "";
	String ADDITIONAL_MESSAGE_BUNDLE_NAME = "additional_resources";
	String DEFAULT_MESSAGE_BUNDLE_NAME = "resources";
	String FOLIO_BUNDLE_NAME = "folio_config";
	String FOLIO_TEST_BUNDLE_NAME = "folio_test_config";

	String AMPERSAND = "&";
	
	String COVER_KEY = "cover";
	String LICENSE_KEY = "license";
	String CONFIGURATION_KEY = "configuration";
	String LAYOUT_KEY = "layout";

	String CURRENT_REQUEST_KEY = "current";
	String MORE_LIKE_THIS_RESULT_KEY = "similarItems";
	String RESOURCE_KEY = "resource";

	String SEARCH_LAYOUT = "three_columns.vm";

	String MAIL_HEADER_MSG = "Osee Genius - Search and Discovery";
	String MAIL_FOOTER_MSG = "powered by @Cult - http://www.atcult.it - info@atcult.it";

	String SEARCH_ENGINE_AUTH_ATTRIBUTE_NAME = "authSearchEngine";
	String SEARCH_ENGINE_HEADING_ATTRIBUTE_NAME = "headingSearchEngine";
	String SEARCH_ENGINE_ATTRIBUTE_NAME = "searchEngine";
	String TH_SEARCH_ENGINE_ATTRIBUTE_NAME = "thSearchEngine";
	
	String HOLDING_DATA_ATTRIBUTE_NAME = "copies";
	String SUBSCRPTIONS_DATA_ATTRIBUTE_NAME = "subscriptions";
			
	String RSS_QUERY_PARAMETER_NAME = "s=";
	String RSS_QUERY_TYPE_PARAMETER_NAME = "t=";
	String RSS_FILTER_QUERY_PARAMETER_NAME = "l=";

	String RECORD_LABEL_RAWDATA_LEADER = "LEADER";
	String RECORD_LABEL_MARC21_LEADER = "LDR";

	String SEARCHER_URLS_PARAMETER = "ir.urls";
	String HIGHLIGHTER_URLS_PARAMETER = "hl.urls";
	String TH_URLS_PARAMETER = "th.ir.urls";

	String FEDERATED_SEARCH_ENDPOINT_PARAMETER = "federated.search.endpoint";

	String TOP_CONCEPTS = "topConcepts";
	
	String AND = "AND";
	String OR = "OR";

	String LIBRARIES_BY_SIMBOL = "mainLibraries";
	String ML_NAMES = "mainLibraryNames";

	String MAIN_LIBRARIES = "mainLibrariesList";

	String MAIN_LIBRARIES_BY_ID = "mlbyid";

	String ALL_LIBRARIES_BY_SIMBOL = "allLibraries";
	String MAIN_LIBRARIES_BY_BRANCH_ID = "mlbbid";

	String ALL_LIBRARIES_BY_ID = "albid";
	
	int FACET_PER_PAGE = 300;

	String SOLR_CONNECTION_TIMEOUT = "solrj.connection.timeout";
	String SOLR_SO_CONNECTION_TIMEOUT = "solrj.so.connection.timeout";
	
	//constant for different mail service for imss bug (5528)
	String FEEDBACK = "feedback";
	String INFO = "info";
	String BOOKING ="booking";
	String AUTH_FEEDBACK = "auth_feedback";
	
	final String HTML_FULL = "FULL";
	final String HTML_SHORT = "SHORT";
	
	final String PRINT_BOOKING = "printBooking";
}
