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
package com.atc.osee.web.plugin;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;

/**
 * Supertype layer for all database-based plugin.
 * 
 * @author Giorgio Gazzarini
 * @since 1.0
 */
public interface DatabasePlugin
{
	String OLISUITE_PROVIDER_URL_PROPERTY_NAME = "olisuite-provider-url";
	String OLISUITE_NAMING_CONTEXT_FACTORY_PROPERTY_NAME = "olisuite-naming-context-factory";
	String OLISUITE_BORROWER_MANAGEMENT_SERVICE_NAME = "olisuite-borrower-management-service-name";
	String OLISUITE_HOLD_MANAGEMENT_SERVICE_NAME = "olisuite-hold-management-service-name";
	String OLISUITE_CIRCULATION_SERVICE_NAME = "olisuite-circulation-service-name";
	String OLISUITE_ONLINE_SERVICE_NAME = "olisuite-online-service-name";
	
	/**
	 * Inits this plugin with the given datasource.
	 * 
	 * @param datasource the datasource.
	 */
	void init(DataSource datasource);
	
	/**
	 * Inits this plugin with the given external naming context.
	 * 
	 * @param externalNamingContext the external naming context.
	 */
	void init(ValueParser configuration);
}