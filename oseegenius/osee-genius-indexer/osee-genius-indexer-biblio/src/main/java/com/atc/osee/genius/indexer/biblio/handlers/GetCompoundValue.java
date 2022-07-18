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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * A tag handler that simply extracts a value from a marc record based on a given expression.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetCompoundValue extends TagHandler
{	
	final static String SEPARATOR = "£££";
	
	private RemoveTrailingPunctuationTagHandler removeTrailingPunctuation = new RemoveTrailingPunctuationTagHandler();
	
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
		List<String> result = new ArrayList<String>();
		String [] labelAndValuePairs = tagMappings.split("\\|");
		
		for (String pair : labelAndValuePairs)
		{
			String [] parts = pair.split(",");
					
			if (parts != null && parts.length > 1)
			{
				List<List<String>> values = new ArrayList<List<String>>();
				int minDetectedLength = 1;
				for (String part : parts)
				{
					Object value = getValue(part, record);
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
							result.add(builder.toString());
						}
					}
				} catch(IndexOutOfBoundsException exception)
				{
					// Nothing...just skip the current heading
				}
			}
		}
		
		if (result.isEmpty())
		{
			return null;
		}
		
		if (result.size() == 1)
		{
			return result.get(0);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Object getValue(final String tagMappings, final Record record)
	{
		// e.g. "BIB" (a literal value)
		if (tagMappings.startsWith("\"") && tagMappings.endsWith("\""))
		{
			return tagMappings.substring(1, tagMappings.length() - 1);
		}
				
		String [] expressionAndFilter = tagMappings.split(",");
		String filter = (expressionAndFilter.length == 2) ? expressionAndFilter[1] : null;
		Set<String> result = new LinkedHashSet<String>();
		String [] tags = expressionAndFilter[0].split(":");
		for (String tag : tags)
		{
			if (IConstants._001.equals(tag))
			{
				return record.getControlNumber();
			} else if (tag.length() > 3)
			{
				String tagNumber = tag.substring(0, 3);				
				if (isControlField(tagNumber))
				{
					String indexes = tag.substring(tag.indexOf("[") + 1, tag.indexOf("]"));
					String [] startAndEndIndex = indexes.split("-");
					int startIndex = Integer.parseInt(startAndEndIndex[0]);
					int endIndex = Integer.parseInt(startAndEndIndex[1]) + 1;
			
					List<VariableField> variableFields = record.getVariableFields(tagNumber);
					for (VariableField variableField : variableFields)
					{
						ControlField controlField = (ControlField)variableField;
						result.add(controlField.getData().substring(startIndex, endIndex));
					}
				} else
				{				 
					List<VariableField> fields = record.getVariableFields(tag.substring(0, 3));
					String subFieldNamesString = tag.substring(3);
					char [] subfieldNames = new char[subFieldNamesString.length()];
					subFieldNamesString.getChars(0, subFieldNamesString.length(), subfieldNames, 0);
					if (fields != null)
					{
						for(VariableField f : fields) {
							DataField field = (DataField) f;
							StringBuilder buffer = new StringBuilder();
							for (char subfieldName : subfieldNames)
							{
								List<Subfield> subfields = field.getSubfields(subfieldName);
								for (Subfield subfield : subfields)
								{
									if (subfield != null)
									{
										String data = subfield.getData();
										data = (data != null && data.trim().length() != 0) ? data.trim() : null;
										if (data != null && (filter == null || (filter != null && data.indexOf(filter) == -1)))
										{
											if (buffer.length() != 0) 
											{
												buffer.append(' ');	
											}
											buffer.append(data);
										}
									}
								}
							}
							String value = buffer.toString().trim();
							if (value.length() != 0)
							{
								result.add(value);
							}
						}
					}
				}		
			}
		}
		
        return handleReturnValues(result);
	}
}