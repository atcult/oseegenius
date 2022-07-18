package com.atc.osee.genius.indexer.biblio.browsing.keys.impl;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

/**
 * OseeGenius default heading analyzer.
 * Makes use of {@link AlphanumericTokenizer}, {@link LowerCaseFilter} and {@link ASCIIFoldingFilter}.
 * 
 * @author agazzarini
 * @since 1.0
 */
class HeadingAnalyzer extends Analyzer
{
	private final Version matchVersion;
	
	
	
	/**
	 * Builds a new analyzer with the given Lucene match version.
	 * 
	 * @param matchVersion the (lucene) match version.
	 */
	HeadingAnalyzer(final Version matchVersion) 
	{
		this.matchVersion = matchVersion;
	}

	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) 
	{
		final AlphanumericTokenizer tokenizer = new AlphanumericTokenizer(matchVersion, reader);
		TokenStream tokenStream = new StandardFilter(matchVersion, tokenizer);
		tokenStream = new LowerCaseFilter(matchVersion, tokenStream);
		return new TokenStreamComponents(tokenizer, tokenStream); 
	}
}