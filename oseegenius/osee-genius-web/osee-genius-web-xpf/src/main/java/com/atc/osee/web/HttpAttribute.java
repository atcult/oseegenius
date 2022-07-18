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
 * Enumerative interface for all OseeGenius -W- HTTP attributes.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface HttpAttribute
{
	String VISIT = "visit";
	String IN_ERROR = "inError";
	String ACTIVE_SESSIONS = "as";
	String FEDERATED_SEARCH_HTTP_CLIENT = "fshc";
	String FEDERATED_SEARCH_ENDPOINT_URL = "fseu";
	String RESOURCE = "resource";
	String FEDERATED_SEARCH_APPLICABLE = "federatedSearchApplicable";
	String FEDERATED_SEARCH_TARGETS = "federatedSearchTargets";
	String CURRENT_SEARCHER = "searcher";
	String CURRENT_TAB = "currentTab";
	String SUGGESTOR = "suggestor";
	String HOME_LOGICAL_NAME = "home";
	String ACCOUNT = "account";
	String ISBN = "isbn";
	String RECID = "id";
	String DONT_SHOW_HOME_IN_BREADCRUMB = "nohomelink";
	String DONT_SHOW_PERSPECTIVE_BUTTON = "hidePerspectiveButton";
	String DONT_SHOW_ADVANCED_SEARCH_BUTTON = "hideAdvancedSearchButton";
	String LANGUAGE_FACETS = "languages";
	String COLLECTION_FACETS = "collections";
	String PUB_INTERVAL_FACETS = "pubIntervals";
	String FACETFIELD = "facetField";
	String BUNDLE= "bundle";
	String FILTERFACET = "f";
	String SEARCH_HISTORY_ENTRIES = "entries";
	String HISTORY_SECTION = "hsection";
	String OG_CONTEXT = "ogcontext";
	String LOGICAL_VIEW = "lv";
	String WORKSPACE_SELECTION = "wsel";
	String CURRENTLY_SELECTED_TAG_ID = "csti";
	String CURRENTLY_SELECTED_WISHLIST_ID = "cswli";	
	String MONEY_FINES_IN_USE = "moneyFinesInUse";
	String ACTIVE_WORKSPACE_SECTION = "section";
	String ADDITIONAL_PINNED_FILTERS = "apf";
	String HITS = "hits";
	String EXTERNAL_TARGETS = "externalTarget";
	String TARGETS_ENABLED = "targetsEnabled";
	String HOLDS_BY_LIBRARIES = "holdsByLibraries";
	String LIBRARY = "library";
	String HOLD = "hold";
	String LIMIT = "limit";
	String LIMIT_NAME = "limitname";
	String PERMANENT_FILTER = "permanentFilter";
	String COLLECTION = "collection";
}