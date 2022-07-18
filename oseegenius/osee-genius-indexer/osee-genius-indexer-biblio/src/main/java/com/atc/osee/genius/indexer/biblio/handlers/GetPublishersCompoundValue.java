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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.Record;


/**
 * A tag handler that simply extracts a value from a marc record based on a given expression.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetPublishersCompoundValue extends GetCompoundValue
{	
	private final GetPublishers extractor = new GetPublishers();
	private final RemoveTrailingPunctuationTagHandler remove = new RemoveTrailingPunctuationTagHandler();
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) {
		Object result = extractor.getValue(tagMappings, record, null, null, null);
		if (result != null) {
			if (result instanceof Collection){
				Collection<String> publishers = (Collection<String>) result;
				Set<String> publisherResult = new LinkedHashSet<String>(publishers.size());
				for (String publisher : publishers) {
					publisherResult.add(buildInvert(publisher));
				}
				return publisherResult;
			} else {
				return buildInvert(String.valueOf(result));
			}
		}
		return result;
	}
	
	public String buildInvert(final String heading) {
		int indexOfColon = heading.indexOf(':');
		if (indexOfColon != -1) {
			final String firstPart = (String) remove.decorate("", heading.substring(0, indexOfColon).trim());
			final String secondPart = (String) remove.decorate("", heading.substring(indexOfColon + 1).trim());
			final StringBuilder builder = new StringBuilder((String)remove.decorate("", heading)).append(GetCompoundValue.SEPARATOR);
			builder.append(secondPart);
			return builder.toString();
		}
		return heading;
	}
	
	public Object getValue(final String tagMappings, final Record record)
	{
		return extractor.getValue(tagMappings, record, null, null, null);
	}
}