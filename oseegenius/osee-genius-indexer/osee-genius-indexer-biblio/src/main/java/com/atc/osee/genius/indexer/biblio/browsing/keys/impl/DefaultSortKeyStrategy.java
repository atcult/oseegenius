package com.atc.osee.genius.indexer.biblio.browsing.keys.impl;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.browsing.IHeadingFilter;
import com.atc.osee.genius.indexer.biblio.browsing.ISortKeyStrategy;

/**
 * The default algorithm used for computing sort and inverted sort keys.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class DefaultSortKeyStrategy implements ISortKeyStrategy 
{
	private final HeadingAnalyzer analyzer = new HeadingAnalyzer(IConstants.LUCENE_VERSION);

	@Override
	public String sortKey(final String heading, final IHeadingFilter filter) 
	{
		try 
		{
			String text = filter.doFilter(heading.toLowerCase());
			TokenStream stream = new ASCIIFoldingFilter(analyzer.tokenStream("sortkey", new StringReader(text)));
			CharTermAttribute term = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);
			StringBuilder builder = new StringBuilder();
			stream.reset();
			while (stream.incrementToken()) 
			{
				builder.append(term.toString());
			}
			return builder.toString();
		} catch(IOException exception)
		{
			return heading;
		}	
	}
}