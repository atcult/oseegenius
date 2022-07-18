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
package com.atc.osee.web.log;

import com.atc.osee.web.IConstants;

/**
 * OseeGenius message catalogue.
 * A dummy interface used for enumerating OseeGenius module messages.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface MessageCatalog 
{
	String LT = "<";
	
	// INFO	
	String _000000_STARTING = LT + IConstants.MODULE_NAME + "-000000> : Starting Osee Genius -W- instance.";
	String _000001_RUNNING   = LT + IConstants.MODULE_NAME + "-000001> : Osee Genius -W- open for e-business.";
	String _000002_SHUTTING_DOWN  = LT + IConstants.MODULE_NAME + "-000002> : Shut down procedure has been initiated for Osee Genius -W- instance.";
	String _000003_SHUT_DOWN = LT + IConstants.MODULE_NAME + "-000003> : Osee Genius -W- has been shut down.";
	String _000004_IR_URLS = LT + IConstants.MODULE_NAME + "-000004> : Osee Genius -W- is going to be connected with searcher(s) %s.";
	String _000005_NAMING_SERVICE_FAILURE  = LT + IConstants.MODULE_NAME + "-000005> : Osee Genius -W- is not able to lookup a naming resource with name %s.";
	String _000006_ACCOUNT_PLUGIN_DISABLED  = LT + IConstants.MODULE_NAME + "-000006> : Account management plugin has been disabled on this Osee Genius -W- instance.";
	String _000007_CIRCULATION_PLUGIN_DISABLED  = LT + IConstants.MODULE_NAME + "-000007> : Circulation plugin has been disabled on this Osee Genius -W- instance.";
		
	String _000008_SESSION_HAS_BEEN_CREATED  = LT + IConstants.MODULE_NAME + "-000008> : New access has been detected (session ID %s).";
	String _000009_SESSION_HAS_BEEN_DESTROYED  = LT + IConstants.MODULE_NAME + "-000007> : Session associated with ID %s has been destroyed.";

	String _000010_COMMUNITY_PLUGIN_DISABLED  = LT + IConstants.MODULE_NAME + "-000010> : Community plugin has been disabled on this Osee Genius -W- instance.";
	String _000011_LIBRARY_PLUGIN_DISABLED  = LT + IConstants.MODULE_NAME + "-000011> : Library plugin has been disabled on this Osee Genius -W- instance.";

	String _000012_FEDERATED_SEARCH_SESSION_INIT_START  = LT 
			+ IConstants.MODULE_NAME + "-000012> : Federated search session seems to be not initialized...proceed with initialisation.";
	
	String _000013_FEDERATED_SEARCH_SESSION_INIT_END  = LT + IConstants.MODULE_NAME + "-000013> : Federated search session has been initialized with session ID %s";
	String _000014_FEDERATED_SEARCH_SEARCH  = LT + IConstants.MODULE_NAME + "-000014> : Launching federated search with %s as query.";
	String _000015_FEDERATED_SEARCH_SEARCH_AGAIN  = LT + IConstants.MODULE_NAME + "-000015> : Launching again federated search with %s as query.";
	String _000016_FEDERATED_SEARCH_SESSION_EXPIRED  = LT + IConstants.MODULE_NAME + "-000016> : Federated session associated with ID %s has been expired.";
	String _000017_KA_DAEMON_SHUT_DOWN  = LT + IConstants.MODULE_NAME + "-000017> : Keep alive daemon has been shut down.";
	
	// ERROR
	String COMMON_LOG_MESSAGE  = " See below for detailed stacktrace.";
	String _100002_DATA_ACCESS_FAILURE   = LT + IConstants.MODULE_NAME + "-100002> : Data Access Failure."  +  COMMON_LOG_MESSAGE;
	String _100003_SEARCH_ENGINE_FAILURE   = LT + IConstants.MODULE_NAME + "-100003> : Search Engine Failure." +  COMMON_LOG_MESSAGE;
	String _100004_MALFORMED_IR_URL  = LT + IConstants.MODULE_NAME + "-100004> : One or more malformed IR urls has been detected. Search service will be deactivated.";
	String _100011_REPORT_ENGINE_FAILURE  = LT + IConstants.MODULE_NAME + "-100011> : Report Engine Failure."  +  COMMON_LOG_MESSAGE;
	
	String _100012_ADVANCED_SEARCH_INITIALIZATION_FAILURE  = LT 
			+ IConstants.MODULE_NAME 
			+ "-100012> : Advanced Search domain model failed initialization."  
			+  COMMON_LOG_MESSAGE;
	
	String _100013_ADVANCED_SEARCH_FIELD_REMOVAL_FAILURE  = LT 
			+ IConstants.MODULE_NAME 
			+ "-100013> : Unable to delete a search field. Probably the given index (%s) is invalid."  
			+  COMMON_LOG_MESSAGE;
	
	String _100014_ADVANCED_SEARCH_MODEL_LIMITS_INIT_FAILURE  = LT 
			+ IConstants.MODULE_NAME 
			+ "-100014> : Unable to properly initialise limit facets on advanced search model."  
			+  COMMON_LOG_MESSAGE;
	
	String _100015_INVALID_PAGE_SIZE  = LT 
			+ IConstants.MODULE_NAME 
			+ "-100015> : Unable to set the page size. Probably the given value (%s) is invalid."  
			+  COMMON_LOG_MESSAGE;
	
	String _100016_FEDERATED_SESSION_STARTUP_FAILURE  = LT + IConstants.MODULE_NAME + "-100016> : Unable to intialize a federated session."  +  COMMON_LOG_MESSAGE;
	String _100017_FEDERATED_SEARCH_FAILURE  = LT + IConstants.MODULE_NAME + "-100017> : Federated search failure. Response was %s";
	String _100018_KA_DAEMON_SEARCH_FAILURE  = LT + IConstants.MODULE_NAME + "-100018> : Keep alive daemon failure. " +  COMMON_LOG_MESSAGE;;
	
	String _100019_FEDERATED_SESSION_PING_FAILURE  = LT 
			+ IConstants.MODULE_NAME + "-100019> : Unable to keep alive (ping) federated interaction associated with session ID %s.";
	
	String _100020_BROWSING_CLASSIFICATION_LOAD_FAILURE  = LT + IConstants.MODULE_NAME + "-100020> : Unable to load the browsing classification. " +  COMMON_LOG_MESSAGE;;
	
	String _100021_UNABLE_TO_LOAD_TCONCEPTS  = LT 
			+ IConstants.MODULE_NAME 
			+ "-100021> : ThGenius was unable to load the top concepts from the configured thesaurus. " 
			+  COMMON_LOG_MESSAGE;;
	
	String _100022_UNABLE_TO_START_SEARCH_SERVICE  = LT 
			+ IConstants.MODULE_NAME + "-100022> : OseeGenius -W- was unable to connect with a OseeGenius -S- search searvice. " 
			+  COMMON_LOG_MESSAGE;;
	
	String _100023_START_UP_FAILURE  = LT + IConstants.MODULE_NAME + "-100023> : OseeGenius -W- lifecycle listener was unable to startup correctly. " +  COMMON_LOG_MESSAGE;;
	String _100024_UNSUPPORTED_ENCODING  = LT + IConstants.MODULE_NAME + "-100024> : Encoding not supported. " +  COMMON_LOG_MESSAGE;;
	String _100025_KA_DAEMON_SLEEP_INTERRUPTED = 
			LT 
			+ IConstants.MODULE_NAME 
			+ "-100025> : Keep alive daemon has been suddendly interrupted while sleeping..." 
			+ "are you shutting down OseeGenius -W-?";
	
	//DEBUG
	String _200001_FEDERATED_SEARCH_ALREADY_EXECUTED = LT + IConstants.MODULE_NAME + "-200001> : A query for this search (%s) already exists so we won't repeat again.";
	
	//PLUGIN
	String COMMON_PLUGIN_NOT_INSTALLED = LT + IConstants.MODULE_NAME + "-900000> : ";
	String COMMON_PLUGIN_INSTALLED = LT + IConstants.MODULE_NAME + "-0> : ";

	/*
	String COMMON_DB_PLUGIN_NOT_INSTALLED = COMMON_PLUGIN_NOT_INSTALLED  +  " : Osee Genius was unable to load a valid RDBMS plugin for ";
	String COMMON_DB_PLUGIN_INSTALLED = COMMON_PLUGIN_INSTALLED  +  " : RDBMS plugin found for ";

	String LIBRARIES = " LIBRARIES.";
	String COPIES = " COPIES.";
	String ACCOUNT_MANAGEMENT =" ACCOUNT MANAGEMENT";
	String _0_LIBRARIES_PLUGIN_INSTALLED = COMMON_DB_PLUGIN_INSTALLED  +  LIBRARIES;
	String _900000_LIBRARIES_PLUGIN_NOT_INSTALLED = COMMON_DB_PLUGIN_NOT_INSTALLED  +  LIBRARIES;
	String _0_COPIES_PLUGIN_INSTALLED = COMMON_DB_PLUGIN_INSTALLED  +  COPIES;
	String _900000_COPIES_PLUGIN_NOT_INSTALLED = COMMON_DB_PLUGIN_NOT_INSTALLED  +  COPIES;
	String _0_ACCOUNT_MNGMT_PLUGIN_INSTALLED = COMMON_DB_PLUGIN_INSTALLED  +  ACCOUNT MANAGEMENT;
	String _900000_ACCOUNT_MNGMT_PLUGIN_NOT_INSTALLED = COMMON_DB_PLUGIN_NOT_INSTALLED  +  ACCOUNT MANAGEMENT;
	String _0_SOCIAL_PLUGIN_INSTALLED = COMMON_DB_PLUGIN_INSTALLED  +  " SOCIAL.";
	String _900000_SOCIAL_PLUGIN_NOT_INSTALLED  = COMMON_DB_PLUGIN_NOT_INSTALLED  +  " SOCIAL.";
	*/
}