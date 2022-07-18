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
package com.atc.osee.web.servlets.search.federated;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import com.atc.osee.web.SystemInternalFailureException;

/**
 * Pazpar2 proxy behavioural interface.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface PazPar2 extends Serializable
{
	/**
	 * Executes a pazpar2 "record" command.
	 * Note that, as expected by pazpar2, a previous "search" command needs to be issued before 
	 * executing a "record". In addition, the record must included in the "search" resultset.
	 * 
	 * @param uri the record identifier.
	 * @param offset an optional parameter is an integer which, when given, makes Pazpar2 return the original record for a specific target.
	 * @return the detailed record information (in XML).
	 * @throws SystemInternalFailureException in case of system failure.
	 * @throws NoSuchResourceException in case the resource cannot be found.
	 */
	String record(String uri, Integer offset) throws SystemInternalFailureException, NoSuchResourceException;

	/**
	 * Executes a pazpar2 search command.
	 * 
	 * @param query the query string.
	 * @param otherParameters the other query parameters.
	 * @throws SystemInternalFailureException in case of system internal failure.
	 */
	void search(String query, String otherParameters) throws SystemInternalFailureException;

	/**
	 * Executes a search using parameters included in the given HTTP request.
	 * 
	 * @param request the HTTP request.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	void search(HttpServletRequest request) throws SystemInternalFailureException;
	
	/**
	 * Executes a pazpar2 show command.
	 * 
	 * @param parameters the other query parameters.
	 * @return the show response.
	 * @throws SystemInternalFailureException in case of system internal failure.
	 */
	String show(HttpServletRequest request) throws SystemInternalFailureException;
	
	/**
	 * Executes a pazpar2 termlist command.
	 * 
	 * @param names the term (facets) names .
	 * @return the termlist response.
	 * @throws SystemInternalFailureException in case of system internal failure.
	 */
	String termlist(String names) throws SystemInternalFailureException;
	
	/**
	 * Executes a pazpar2 stat command.
	 * 
	 * @return the stat response.
	 * @throws SystemInternalFailureException in case of system internal failure.
	 */
	String stat() throws SystemInternalFailureException;

	/**
	 * Executes a pazpar2 ping command.
	 * 
	 * @throws SystemInternalFailureException in case of system internal failure.
	 */
	void ping() throws SystemInternalFailureException;
	
	/**
	 * Sends a "settings" command to pazpar2.
	 * 
	 * @param enabledTargets the list of target to enable. 
	 * @throws SystemInternalFailureException
	 */
	void disableAndOrEnableTargets(HttpServletRequest request) throws SystemInternalFailureException;
}