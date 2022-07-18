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
 * value(954abr) 
 * value(954a:954b:954r,pippo,>1#) sottocampi diversi + filtro + indicatori
 * 
 * @author agazzarini
 * @since 1.0
 */
public class FilteredLink extends TagHandler
{	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
		String [] expressionAndFilter = tagMappings.split(",");
		String filter = null;
		String indicatorFilter = null;
		if (expressionAndFilter.length == 2)
		{
			String tmp = expressionAndFilter[1];
			if (tmp.indexOf(">") != -1)
			{
				indicatorFilter = expressionAndFilter [1].substring(1).replace("#", " ");
			} else
			{
				filter = expressionAndFilter [1];
			}
		} else if (expressionAndFilter.length == 3)
		{
			filter = expressionAndFilter[1];
			indicatorFilter = expressionAndFilter[2].substring(1).replace("#", " ");
		}

				
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
						result.add(controlField.getData().substring(startIndex, endIndex).trim());
					}
				} else
				{				 
					List<VariableField> fields = (List<VariableField>) record.getVariableFields(tag.substring(0, 3));
					String subFieldNamesString = tag.substring(3);
					char [] subfieldNames = new char[subFieldNamesString.length()];
					subFieldNamesString.getChars(0, subFieldNamesString.length(), subfieldNames, 0);
					if (fields != null)
					{
						for (VariableField f : fields)
						{
							DataField field = (DataField) f;							
							boolean indicatorCheck = true;
							if (indicatorFilter != null) 
							{
								String indicators = new StringBuilder().append(field.getIndicator1()).append(field.getIndicator2()).toString();
								indicatorCheck = indicators.equals(indicatorFilter);
							}	
							if (!indicatorCheck)
							{
								continue;
							}
							
							StringBuilder buffer = new StringBuilder();
							for (char subfieldName : subfieldNames)
							{
								List<Subfield> subfields = field.getSubfields(subfieldName);
								Subfield tagPattern = field.getSubfield('3');
								for (Subfield subfield : subfields)
								{
									if (subfield != null)
									{
										String data = subfield.getData();
										data = (data != null && data.trim().length() != 0) ? data.trim() : null;
										if(data!=null && tagPattern!=null && filter!=null && tagPattern.getData().equals(filter) ){
											if (buffer.length() != 0) 
											{
												buffer.append(getSubfieldSeparator());	
											}
											buffer.append(data);
										}
										if(data!=null && filter==null && (tagPattern==null 
												||(!tagPattern.getData().equals("FCOV") && !tagPattern.getData().equals("EVL")) ) ){
											if (buffer.length() != 0) 
											{
												buffer.append(getSubfieldSeparator());	
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
	
	protected String getSubfieldSeparator()
	{
		return " ";
	}
}