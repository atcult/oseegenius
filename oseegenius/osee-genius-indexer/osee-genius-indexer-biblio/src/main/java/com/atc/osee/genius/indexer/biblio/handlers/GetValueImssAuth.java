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

import org.apache.commons.lang.ObjectUtils.Null;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.asynch.Data;
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
public class GetValueImssAuth extends TagHandler
{		
	@Override
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
		final String [] expressionFilterAndCategory = tagMappings.split(";");
		final String [] expressionAndFilter = expressionFilterAndCategory[0].split(",");	
		Set<String> result = null;
		
		
		String filter = null;
		String indicatorFilter = null;
		
		if(verifyType(record, expressionFilterAndCategory)){
		
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
					
			
			final String [] tags = expressionAndFilter[0].split(":");
			for (String tag : tags)
			{
				if (IConstants._001.equals(tag))
				{
					return record.getControlNumber();
				} else if("005".equals(tag)) {
					final ControlField field = (ControlField) record.getVariableField("005");
					return field != null ? field.getData() : null;
				} else if (tag.length() > 3)
				{
					final String tagNumber = tag.substring(0, 3);				
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
							String toAdd;
							try{
								toAdd = controlField.getData().substring(startIndex, endIndex).trim();
								if (toAdd!=null && toAdd.length()>0)
								{
									if (result == null)
									{
										result = new LinkedHashSet<String>();
									}
									
									result.add(toAdd);
								}
							}catch (Exception e) {
								
								//Nothing
							}
						}
					} else
					{				 
						final List<VariableField> fields = record.getVariableFields(tagNumber);
						
						if (fields != null && !fields.isEmpty())
						{
							final String subFieldNamesString = tag.substring(3);
							final char [] subfieldNames = new char[subFieldNamesString.length()];
							subFieldNamesString.getChars(0, subFieldNamesString.length(), subfieldNames, 0);
							for (VariableField f : fields)
							{
								DataField field = (DataField)f;
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
								
								StringBuilder buffer = null;
								for (char subfieldName : subfieldNames)
								{
									final List<Subfield> subfields = field.getSubfields(subfieldName);
									if (subfields != null && !subfields.isEmpty())
									{
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
										}
									}
								}
								String value = buffer != null ? buffer.toString().trim() : null;
								if (value != null && value.length() != 0)
								{
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
			}
		}
        return handleReturnValues(result);
	}

	protected boolean verifyType(final Record record, final String[] expressionFilterAndCategory) {
		List<VariableField> list996 = record.getVariableFields("996");
		if(list996 != null && !list996.isEmpty()){
			for (VariableField f : list996)
			{
				DataField tag996 = (DataField)f;
				final List<Subfield> subfields = tag996.getSubfields('9');
				if (subfields != null && !subfields.isEmpty())
				{
					for (Subfield subfield : subfields)
					{
						if (subfield != null)
						{
							String data = subfield.getData();
							data = (data != null && data.trim().length() != 0) ? data.trim() : null;
							//control if it is the right category
							if(expressionFilterAndCategory.length > 1){
								int lastSlash = data.lastIndexOf("/");
								if(lastSlash != -1) {
									String tempData = data.substring(0, lastSlash);
									int lastLastSlash = tempData.lastIndexOf("/");
										if(lastLastSlash != -1) {
											tempData = data.substring(lastLastSlash + 1, lastSlash);
											return expressionFilterAndCategory[1].equals(tempData);
												
										}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	protected String getSubfieldSeparator()
	{
		return " ";
	}
}