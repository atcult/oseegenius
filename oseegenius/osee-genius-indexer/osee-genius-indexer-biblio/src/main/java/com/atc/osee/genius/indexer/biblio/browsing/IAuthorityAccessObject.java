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
package com.atc.osee.genius.indexer.biblio.browsing;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;


/**
 * A behavioural interface for authority access objects
 * Concrete implementors must specify how to get access to the
 * specific authority data.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface IAuthorityAccessObject
{
	/**
	 * Retrieves the authority record associated with the given heading value.
	 * 
	 * @param headingValue the heading value (used as query string).
	 * @param filter the heading filter (for heading pre-processing).
	 * @return the authority record associated with the given heading value.
	 */
	AuthorityRecord getAuthorityRecord(String headingValue, IHeadingFilter filter );
	
	/**
	 * Retrieves the authority record associated with the given heading value.
	 * 
	 * @param headingValue the heading value (used as query string).
	 * @param filter the heading filter (for heading pre-processing).
	 * @return the authority record associated with the given heading value.
	 */
	AuthorityRecord getCachedAuthorityRecord(String key);
	
	void loadHeadings(NamedList<Object> configuration) throws Exception;
	
	boolean isValidHeading(final String headingKey);
	
	/**
	 * Initialises this access object with its configuration.
	 * Incoming object is generic because implementations are very differnet and 
	 * in order to properly work they need completely different kind of parameters.
	 * 
	 * @param core the hosting SOLR core.
	 * @param configuration the AAO configuration.
	 * @throws InitialisationException in case of initialisation failure.
	 */
	void init(SolrCore core, NamedList<Object> configuration) throws InitialisationException;
	
	void shutdown();
}