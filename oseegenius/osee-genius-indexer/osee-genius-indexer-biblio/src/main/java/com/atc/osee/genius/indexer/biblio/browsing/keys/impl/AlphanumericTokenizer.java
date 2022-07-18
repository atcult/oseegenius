package com.atc.osee.genius.indexer.biblio.browsing.keys.impl;

import java.io.Reader;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

/**
 * A lucene tokenizer split over characters and digits.
 * 
 * @author agazzarini
 * @since 1.0
 */
class AlphanumericTokenizer extends CharTokenizer 
{
	/**
	 * Buidls a new tokenizer with the given version.
	 * 
	 * @param version the Lucene match version.
	 * @param reader the input reader.
	 */
	AlphanumericTokenizer(final Version version, final Reader reader) 
	{
		super(version, reader);
	}

	@Override
	protected boolean isTokenChar(final int c) 
	{
		return Character.isLetter(c) || Character.isDigit(c) || Character.isWhitespace(c);
	}
}