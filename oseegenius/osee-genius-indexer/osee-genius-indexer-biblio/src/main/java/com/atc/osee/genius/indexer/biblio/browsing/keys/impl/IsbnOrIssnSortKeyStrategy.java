package com.atc.osee.genius.indexer.biblio.browsing.keys.impl;

import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;
import com.atc.osee.genius.indexer.biblio.browsing.ISortKeyStrategy;

/**
 * The default algorithm used for computing sort and inverted sort keys.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class IsbnOrIssnSortKeyStrategy implements ISortKeyStrategy 
{
	@Override
	public String sortKey(final String heading, final IHeadingFilter filter) 
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < heading.length(); i++)
		{
			char ch = heading.charAt(i);
			if (isValidIsnPart(ch))
			{
				builder.append(ch);
			}
		}
		
		return builder.toString();
	}
	
	private boolean isValidIsnPart(final char ch)
	{
		return Character.isDigit(ch) || ch == 'X' || ch == 'x';
	}
}