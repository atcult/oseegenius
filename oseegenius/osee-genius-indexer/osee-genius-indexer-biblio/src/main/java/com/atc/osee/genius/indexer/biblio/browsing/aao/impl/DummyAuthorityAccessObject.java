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
package com.atc.osee.genius.indexer.biblio.browsing.aao.impl;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;

import com.atc.osee.genius.indexer.biblio.browsing.AuthorityRecord;
import com.atc.osee.genius.indexer.biblio.browsing.IAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.amicus.ICacheStrategy;

/**
 * A fake implementation of authority access object.
 * This is used when you don't have an authority file...that means your headings
 * won't be matched against a controlled vocabulary but instead they will supposed to be
 * valid. 
 *  
 * @author agazzarini
 * @since 1.0
 */
public class DummyAuthorityAccessObject implements IAuthorityAccessObject
{
	@Override
	public AuthorityRecord getCachedAuthorityRecord(final String key) 
	{
		return ICacheStrategy.UNCONTROLLED_AUTHORITY_RECORD;
	}
	
	@Override
	public AuthorityRecord getAuthorityRecord(final String headingValue, IHeadingFilter filter)
	{
		return ICacheStrategy.UNCONTROLLED_AUTHORITY_RECORD;
	}

	@Override
	public void init(final SolrCore core, final NamedList<Object> configuration) 
	{
		// Nothing to be done here...
	}

	@Override
	public void shutdown() 
	{
		// Nothing to be done here...	
	}
	
	@Override
	public void loadHeadings(NamedList<Object> configuration) {
		// Nothing to be done here...
	}
	
	@Override
	public boolean isValidHeading(final String headingKey) {
		return true;
	}
}
