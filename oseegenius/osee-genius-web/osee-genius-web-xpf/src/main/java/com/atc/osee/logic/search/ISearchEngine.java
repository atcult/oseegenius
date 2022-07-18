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
package com.atc.osee.logic.search;

import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.web.SystemInternalFailureException;

/**
 * OseeGenius -W- search engine interface.
 * Basically it's the interface of the connector towards OseeGenius -S-.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public interface ISearchEngine
{
	
	
	/**
	 * Returns the home page resources.
	 * 
	 * @return the home page resources.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	QueryResponse getHomepageResources() throws SystemInternalFailureException;

	/**
	 * Executes the given query towards OseeGenius -S-.
	 * 
	 * @param query the query that will be executed.
	 * @return the query response.
	 * @throws SystemInternalFailureException in case of system failure.
	 */	
	QueryResponse executeQuery(SolrQuery query) throws SystemInternalFailureException;

	/**
	 * Pings the underlying OseeGenius -S- instance.
	 * 
	 * @throws SystemInternalFailureException in case OseeGenius -S- is not reachable.
	 */
	void ping() throws SystemInternalFailureException;

	/**
	 * Queries OseeGenius -S- for details of the document associated with the given URI.
	 * 
	 * @param uri the document URI.
	 * @return the document associated with the given URI.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	QueryResponse findDocumentByURI(String uri) throws SystemInternalFailureException;

	/**
	 * Queries OseeGenius -S- for details of the document associated with the given URI.
	 * 
	 * @param uri the document authority_group_id.
	 * @return the document associated with the given URI.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	QueryResponse findAuthDocumentByAuthLink(String uri) throws SystemInternalFailureException;
	
	/**
	 * Queries OseeGenius -S- for more-like-this details plus of the document associated with the given URI.
	 * 
	 * @param uri the document URI.
	 * @param pageNumber the requested page number of the MLT set.
	 * @param customer the customer short name, to launch the right moreLikeThis function
	 * @return the more-like-this details of the document associated with the given URI.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	QueryResponse findDocumentByURIWithMLT(String uri, String pageNumber, String customer, String ... groupingAndFilteringCriteria) throws SystemInternalFailureException;
	
	/**
	 * Returns the list of documents associated with the given document identifiers.
	 * 
	 * @param selectedItems a set of document identifiers.
	 * @return the list of documents associated with the given document identifiers.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	QueryResponse documents(final Set<String> selectedItems) throws SystemInternalFailureException;
	
	/**
	 * Returns all location associated with the given association name.
	 * 
	 * @param associationName an association name.
	 * @return the locations associated with the given association name.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	QueryResponse findLocationsByAssocName(final String associationName) throws SystemInternalFailureException;
	
	/**
	 * Returns all help lines associated with the given association id.
	 * 
	 * @param associationId an association id.
	 * @return the helplines associated with the given association id.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	QueryResponse findHelpLinesByAssocId(final String associationId) throws SystemInternalFailureException;
}