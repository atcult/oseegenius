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

import org.apache.solr.core.SolrCore;

/**
 * A heading indexing filter.
 * A filter (unless is a dummy filter) is supposed to manipulate the
 * original heading according with a predefined index strategy.
 *  
 * @author agazzarini
 * @since 1.0
 */
public interface IHeadingFilter 
{
	/**
	 * Returns the filtered heading.
	 * 
	 * @param heading the original heading value.
	 * @return the filtered heading.
	 */
	String doFilter(String heading);
	
	/**
	 * Initialise this filter.
	 * 
	 * @param core the owning SOLR core.
	 * @throws Exception in case the filter cannot be initialized.
	 */
	void init(SolrCore core) throws Exception;
}