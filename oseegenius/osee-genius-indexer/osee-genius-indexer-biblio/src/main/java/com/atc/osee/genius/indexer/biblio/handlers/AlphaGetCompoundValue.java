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
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.Record;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts 2 different values: the first is a label composed by all alpha subfields with (optional) exceptrion,
 * the second is the value.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AlphaGetCompoundValue extends TagHandler
{	
	final static String SEPARATOR = "£££";
	
	private RemoveTrailingPunctuationTagHandler removeTrailingPunctuation = new RemoveTrailingPunctuationTagHandler();
	private GetAlphabeticalSubfields labelExtractor = new GetAlphabeticalSubfields();
	private GetValue valueExtractor = new GetValue();
		
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
		List<String> result = null;
		String [] labelAndValuePairs = tagMappings.split("\\|");
		
		for (String pair : labelAndValuePairs)
		{
			String [] parts = pair.split(",");
					
			if (parts != null && parts.length > 1)
			{
				List<List<String>> values = new ArrayList<List<String>>();
				int minDetectedLength = 1;
				int index = 0;
				for (String part : parts)
				{
					Object value = index == 0 
							? labelExtractor.getValue(part, record, null, null, null)
							: valueExtractor.getValue(part, record, null, null, null);
					if (value != null)
					{
						if (value instanceof Collection)
						{
							@SuppressWarnings("unchecked")
							Collection<String> collection = (Collection<String>) value;
							int size = collection.size();
							if (size > minDetectedLength) 
							{
								minDetectedLength = size;
							}
							
							values.add(new ArrayList<String>(collection));
						} else 
						{
							List<String> collectionWithOneValue = new ArrayList<String>(1);
							collectionWithOneValue.add(String.valueOf(value));
							values.add(collectionWithOneValue);
							
							if (minDetectedLength > 1) 
							{
								minDetectedLength = 1;
							}
						}
					}
					index++;
				}
				
				try 
				{
					for (int i = 0; i < minDetectedLength; i++)
					{
						StringBuilder builder = new StringBuilder();
						for (List<String> labels : values)
						{
							String label = (String) removeTrailingPunctuation.decorate("NA", labels.get(i));
							builder.append(label);
							builder.append(SEPARATOR);
						}
						
						if (builder.length() > SEPARATOR.length())
						{
							builder.delete(builder.length() - SEPARATOR.length(), builder.length());
							
							if (result == null)
							{
								result = new ArrayList<String>();
							}
							
							result.add(builder.toString());
						}
					}
				} catch(IndexOutOfBoundsException exception)
				{
					// Nothing...just skip the current heading
				}
			}
		}
		
		if (result == null || result.isEmpty())
		{
			return null;
		}
		
		if (result.size() == 1)
		{
			return result.get(0);
		}
		
		return result;
	}
}