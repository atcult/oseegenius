package com.atc.osee.genius.indexer.biblio.browsing.keys.impl;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

/**
 * OseeGenius default heading analyzer.
 * Makes use of {@link AlphanumericAndWhitespaceTokenizer}, {@link LowerCaseFilter} and {@link ASCIIFoldingFilter}.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AmicusSortFormAnalyzer extends Analyzer 
{
	private final Version matchVersion;
	
	class AlphanumericAndWhitespaceTokenizer extends CharTokenizer 
	{
		/**
		 * Buidls a new tokenizer with the given version.
		 * 
		 * @param version the Lucene match version.
		 * @param reader the input reader.
		 */
		AlphanumericAndWhitespaceTokenizer(final Version version, final Reader reader) 
		{
			super(version, reader);
		}

		@Override
		protected boolean isTokenChar(final int c) 
		{
			return Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == 31 || c == ' ';
		}
	}
	
	class UpperCaseFilter extends TokenFilter
	{
		private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
		
		UpperCaseFilter(Version matchVersion, TokenStream in) 
		{
			super(in);
		}
		
		@Override
		public final boolean incrementToken() throws IOException 
		{
			if (input.incrementToken()) 
			{
				final char[] buffer = termAtt.buffer();
				final int length = termAtt.length();
				for (int i = 0; i < length;) 
				{
					i += Character.toChars(Character.toUpperCase(Character.codePointAt(buffer, i)), buffer, i);
				}
				return true;
			} else
			{
				return false;
			}
		}
		
		@Override
		public void reset() throws IOException {
			// TODO Auto-generated method stub
			super.reset();
		}
		
		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			super.close();
		}
	}
	
	/**
	 * Builds a new analyzer with the given Lucene match version.
	 * 
	 * @param matchVersion the (lucene) match version.
	 */
	public AmicusSortFormAnalyzer(final Version matchVersion) 
	{
		this.matchVersion = matchVersion;
	}

	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) 
	{
		final AlphanumericAndWhitespaceTokenizer tokenizer = new AlphanumericAndWhitespaceTokenizer(matchVersion, reader);
		TokenStream tokenStream = new UpperCaseFilter(matchVersion, tokenizer);
		return new TokenStreamComponents(tokenizer, tokenStream) {
			@Override
			protected void setReader(Reader reader) throws IOException {
				// TODO Auto-generated method stub
				super.setReader(reader);
			}
		};
	}
}