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
package com.atc.osee.genius.indexer.biblio.browsing.filter.impl;

import org.apache.solr.core.SolrCore;

import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;

/**
 * A dummy heading filter.
 * Basically it does nothing, simply returning the input value as it is.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class DummyHeadingFilter implements IHeadingFilter 
{
	@Override
	public String doFilter(final String heading) 
	{
		return heading;
	}

	@Override
	public void init(final SolrCore core) 
	{
		// Nothign to do here..
	}
}
