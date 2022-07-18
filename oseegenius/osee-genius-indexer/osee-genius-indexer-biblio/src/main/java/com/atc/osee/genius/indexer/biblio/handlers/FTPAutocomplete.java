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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the FTP content type from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPAutocomplete extends TagHandler implements IConstants
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FTPAutocomplete.class);
	
	private SolrServer autocomplete;
	
	private GetValue getValue = new GetValue();
	private RemoveTrailingPunctuationTagHandler removeTrailingPunctuaction = new RemoveTrailingPunctuationTagHandler();
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		synchronized(this)  
		{
			if (autocomplete == null) 
			{
				autocomplete = new EmbeddedSolrServer(core.getCoreDescriptor().getCoreContainer(), "autocomplete");
			}
		}
		int indexOfSeparator = tagMappings.indexOf(",");
		final String kind = tagMappings.substring(0, indexOfSeparator);
		final Collection<String> views = getViews(tagMappings, record, settings, core, document);
		
		Object values = getValue.getValue(tagMappings.substring(indexOfSeparator + 1, tagMappings.length()), record, settings, core, document);
		
		if (values != null) 
		{
			try 
			{
				values = removeTrailingPunctuaction.decorate(record, "", values);
				if (values != null && values instanceof Collection) 
				{
					List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
					for (String value : ((Collection<String>)values)) 
					{
						docs.add(newAutocompleteDoc(value, kind, views)); 
					}
					autocomplete.add(docs);
				} else if (values != null && values instanceof String)
				{
					autocomplete.add(newAutocompleteDoc((String)values, kind, views));				
				}
			} catch (Exception exception) {
				LOGGER.error("<OG-1028928> : Unable to add autocomplete entries.", exception);
			}
		}

		return null;
	}	
	
	@SuppressWarnings("unchecked")
	private Collection<String> getViews(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) {
		
		Collection<String> result = new ArrayList<String>();
		
		Object values = getValue.getValue("941a,>2#", record, settings, core, document);
		if (values instanceof Collection) 
		{
			result = (Collection<String>) values;
		} else
		{
			result = new ArrayList<String>();
			result.add(String.valueOf(values));
		}
		
		Object publishers = getValue.getValue("943a", record, settings, core, document);
		if (publishers instanceof Collection) 
		{
			result.addAll((Collection<String>) publishers);
		} else
		{
			result.add(String.valueOf(publishers));
		}
		
		for (final Iterator<String> iterator = result.iterator(); iterator.hasNext();) {
			final String item = iterator.next();
			if (item == null) 
			{
				iterator.remove();
			}
		}
		
		return result;
	}
	
	public SolrInputDocument newAutocompleteDoc(final String label, final String type, Collection<String> logicalViews)
	{
		final SolrInputDocument document = new SolrInputDocument();
		document.addField("label", label);
		document.addField("type", type);
		if (logicalViews != null && !logicalViews.isEmpty())
		{
			document.addField("logical_view", logicalViews);			
		}
		
		return document;
	}
}