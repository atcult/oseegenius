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
package com.atc.osee.genius.holdings;

import javax.sql.DataSource;

import org.apache.solr.common.util.NamedList;

/**
 * Behavioural interface for loading holdings data.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface DataLoaderStrategy
{
	String HOLDS_COUNT = "holds-count";
	String ON_ORDER = "on-order";
	String SHLEF_LIST = "shelf-list";
	String LOCATION = "location";
	String BARCODE = "barcode";
	String DUE_DATE = "due-date";
	String COPIES_AVAILABLE = "copies-available";
	String UNAVAILABILITY_REASON_CODE = "unavailability-reason-code";
	String INSTITUTION_ID = "institution-id";
	String INSTITUTION_NAME = "institution-name";
	String INSTITUTION_SYMBOL = "institution-symbol";
	String SUB_INSTITUTION_ID = "sub-institution-id";
	String SUB_INSTITUTION_NAME = "sub-institution-name";
	String SUB_INSTITUTION_SYMBOL = "sub-institution-symbol";			
	String ORDERS_COUNT ="orders";
	String STATUS = "status";
	
	/**
	 * Returns the holdings data associated with the given (document) URI.
	 * Some libraries use a dummy borrower identifier in order to enable virtual "loans". 
	 * 
	 * @param searchUri the uri that will be used for searching holding data. 
	 * @return the holdings data associated with the given (document) URI.
	 * @throws UnableToLoadHoldingsDataException in case the holdings data load fails.
	 */
	NamedList<NamedList<Object>> getHoldingData(String documentUri) throws UnableToLoadHoldingsDataException;
	 
	/**
	 * Injects the datasource onto this strategy.
	 * 
	 * @param datasource the datasource.
	 */
	void setDataSource(DataSource datasource);
}