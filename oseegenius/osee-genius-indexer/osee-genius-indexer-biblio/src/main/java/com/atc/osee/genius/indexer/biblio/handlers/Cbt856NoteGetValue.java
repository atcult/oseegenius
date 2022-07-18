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
 * @author mbraddi
 * @since 1.0
 */
public class Cbt856NoteGetValue extends TagHandler
{	
	@Override
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
			
		final String [] expressionAndFilter = tagMappings.split(",");
		String filter = null;
				
		Set<String> result = null;
		final String [] tags = expressionAndFilter[0].split(":");
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
					final String indexes = tag.substring(tag.indexOf("[") + 1, tag.indexOf("]"));
					final String [] startAndEndIndex = indexes.split("-");
					final int startIndex = Integer.parseInt(startAndEndIndex[0]);
					final int endIndex = Integer.parseInt(startAndEndIndex[1]) + 1;
			
					final List<VariableField> variableFields = record.getVariableFields(tagNumber);
					for (VariableField variableField : variableFields)
					{
						ControlField controlField = (ControlField)variableField;
						String toAdd;
						try
						{
							toAdd = controlField.getData().substring(startIndex, endIndex).trim();
							if (toAdd !=null && toAdd.length() > 0)
							{
								if (result == null)
								{
									result = new LinkedHashSet<String>();
								}
								
								result.add(toAdd);
							}
						} catch (Exception e) 
						{						
							//Nothing
						}
					}
				} else
				{				 
					final List<VariableField> fields = (List<VariableField>) record.getVariableFields(tag.substring(0, 3));
					if (fields != null && !fields.isEmpty())
					{
						final String subFieldNamesString = tag.substring(3);
						final char [] subfieldNames = new char[subFieldNamesString.length()];
						subFieldNamesString.getChars(0, subFieldNamesString.length(), subfieldNames, 0);
						int i = 0;
						
						for (VariableField f : fields) {						
							DataField field = (DataField)f;
							StringBuilder buffer = null;
							for (char subfieldName : subfieldNames)
							{
								final List<Subfield> subfields = field.getSubfields(subfieldName);
								for (Subfield subfield : subfields)
								{
									if (subfield != null)
									{
										String data = subfield.getData();
										data = (data != null && data.trim().length() != 0) ? data.trim() : null;
										if (data != null && (filter == null || (filter != null && data.indexOf(filter) == -1)))
										{
											if (buffer == null) 
											{
												buffer = new StringBuilder();	
											}
											
											if (buffer.length() != 0) 
											{
												buffer.append(getSubfieldSeparator());	
											}
											buffer.append(data);
										}
									}
									else
									{
										if (buffer == null) 
										{
											buffer = new StringBuilder();	
										}
										
										buffer.append(subfield);
									}
								}
								
							}
							i++;
							String value = null;
							if (buffer !=null && buffer.length()!=0)
							{
								value = buffer.toString().trim();
							} else
							{
								value = "undefined_note_"+i;
							}
							
							if (result == null)
							{
								result = new LinkedHashSet<String>();
							}
							
							result.add(value);
						}
					}
				}		
			}
		}
        return handleReturnValues(result);
	}
	
	protected String getSubfieldSeparator()
	{
		return " ";
	}
}