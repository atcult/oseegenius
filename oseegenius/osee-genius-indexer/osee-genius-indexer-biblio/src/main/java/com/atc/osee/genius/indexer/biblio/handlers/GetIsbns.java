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
package com.atc.osee.genius.indexer.biblio.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.Record;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the ISBN(s) from a given marc record.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetIsbns extends TagHandler 
{
	private static final Pattern _10 = Pattern.compile("^\\d{9}[\\dX].*");
	private static final Pattern _13 = Pattern.compile("^(978|979)\\d{9}[X\\d].*");
	private static final Pattern _13_ANY = Pattern.compile("^\\d{12}[X\\d].*");

	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		Set<String> isbns = new HashSet<String>();

		checkAndAddIsbn(isbns, getValues(IConstants._020A, record));
		checkAndAddIsbn(isbns, getValues(IConstants._020Z, record));
		
		return isbns.isEmpty() ? null : isbns;	
	}
	
	/**
	 * Checks (and adds) the ISBN according to a given set of patterns.
	 * 
	 * @param accumulator the list that contains valid ISBNs.
	 * @param toCheck the ISBN to be checked.
	 */
	private void checkAndAddIsbn(
			final Set<String> accumulator, 
			final List<String> toCheck)
	{
		for (String isbn : toCheck)
		{
			 if (_13.matcher(isbn).matches()) 
			 {
				 accumulator.add(isbn.substring(0, 13));
			 } else if (_10.matcher(isbn).matches() && !_13_ANY.matcher(isbn).matches()) 
			 {
				 accumulator.add(isbn.substring(0, 10));
			 }
		}
	}			
}