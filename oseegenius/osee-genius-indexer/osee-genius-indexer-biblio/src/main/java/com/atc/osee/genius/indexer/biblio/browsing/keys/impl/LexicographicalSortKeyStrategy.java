package com.atc.osee.genius.indexer.biblio.browsing.keys.impl;

import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;
import com.atc.osee.genius.indexer.biblio.browsing.ISortKeyStrategy;

/**
 * A strategy that uses the incoming literal (heading) value as sort key.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class LexicographicalSortKeyStrategy implements ISortKeyStrategy 
{	
	@Override
	public String sortKey(final String heading, final IHeadingFilter filter) 
	{
		return filter.doFilter(heading.toLowerCase());
	}
}