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
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.Record;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the ISSN(s) from a given marc record.
 * 
 * @author mbraddi
 * @since 1.3
 */
public class GetUnimarcIssns extends TagHandler 
{
	private static final Pattern ISSN_PATTERN = Pattern.compile("^\\d{4}-\\d{3}[X\\d]$");

	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		Set<String> result = new HashSet<String>();
		
	    result.addAll(getValues("011a", record));
	    result.addAll(getValues("011z", record));
	   
	    for (Iterator<String> iterator = result.iterator(); iterator.hasNext();) 
	    {
			String issn = iterator.next().trim();	
			if (!ISSN_PATTERN.matcher(issn).matches())
			{
				iterator.remove();
			}
		}
	    return result;
	}
}