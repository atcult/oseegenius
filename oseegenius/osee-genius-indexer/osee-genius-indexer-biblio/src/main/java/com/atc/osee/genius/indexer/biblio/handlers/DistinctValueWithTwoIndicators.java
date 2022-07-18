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
 * A tag handler that simply extracts a value from a marc record based on a given expression and selection by indicators.
 * Insert in setup two indicators is mandatory.
 * An '*' is for every indicator
 * An '#' is equal to ' '
 * You can list indicars with a string as '123ty'
 * value(954abr,>#2) 
 * value(954a:954b:954r,1,#) sottocampi diversi + indicatori
 * value(954a:954b:954r,123,#) sottocampi diversi + indicatori con lista per il primo
 * value(954a:954b:954r,*,*)  sottocampi diversi con ogni indicatore, indicazione obbligatoria 
 * 
 * Called with: distinctIndic
 * Used by: Teologiaca, Seminario, Gregoriana
 * @author ztajoli
 * @since 1.2
 */
public class DistinctValueWithTwoIndicators extends TagHandler
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
		String indicatorFilter[ ];
		indicatorFilter = new String[2];
		
		if (expressionAndFilter.length == 3){
			indicatorFilter[0] = expressionAndFilter[1].replaceAll("#", " ");
			indicatorFilter[1] = expressionAndFilter[2].replaceAll("#", " ");
		} else 	{
			return null;
		}
				
		Set<String> result = null;
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
							boolean indicatorCheck = false;
							if (indicatorFilter != null) {
								boolean FirstInd = false;
								boolean SecondInd = false;
								if ( (indicatorFilter[0].equals("*")) || (indicatorFilter[0].indexOf(field.getIndicator1() )>-1) ) {
									FirstInd = true;
								}
								if ( (indicatorFilter[1].equals("*")) || (indicatorFilter[1].indexOf(field.getIndicator2() )>-1) ) {
									SecondInd = true;
								}
								if (FirstInd && SecondInd ) {
									indicatorCheck = true;
								}
							} else 	{
								indicatorCheck = true;
							}
							
							if (!indicatorCheck){
								continue;
							}
							
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
												if (result == null)
												{
													result = new LinkedHashSet<String>();
												}
												
												result.add(data);
											}
										}
									}
								}
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