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
 * @author agazzarini
 * @since 1.0
 */
public class DistinctValue extends TagHandler
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
					String toAdd;
					for (VariableField variableField : variableFields)
					{
						ControlField controlField = (ControlField)variableField;
						try{
							toAdd = controlField.getData().substring(startIndex, endIndex).trim();
							if(toAdd!=null && toAdd.length()>0){
								result.add(toAdd);
							}
						}catch (Exception e) {
							
							//Nothing
						}
						
					}
				} else
				{				 
					List<VariableField> fields = (List<VariableField>) record.getVariableFields(tag.substring(0, 3));
					String subFieldNamesString = tag.substring(3);
					char [] subfieldNames = new char[subFieldNamesString.length()];
					subFieldNamesString.getChars(0, subFieldNamesString.length(), subfieldNames, 0);
					if (fields != null)
					{
						boolean hasIndicatorCriterion = false;
						char _1st = ' ';
						char _2nd = ' ';
						if (tagMappings.contains(",")) {
							int indexOfComma = tagMappings.indexOf(",");
							int indexOfGt = tagMappings.indexOf(">", indexOfComma);
							if (indexOfGt != -1) {
								hasIndicatorCriterion = true;
								_1st = tagMappings.charAt(indexOfGt + 1);								
								_2nd = tagMappings.charAt(indexOfGt + 2);
								_1st = (_1st != '#') ? _1st : ' ';
								_2nd = (_2nd != '#') ? _2nd : ' ';
							}
						}
						
						for (VariableField f : fields)
						{
							DataField field = (DataField) f;
							
							if (hasIndicatorCriterion) {
								if (_1st != field.getIndicator1() || _2nd != field.getIndicator2()) {
									continue;
								}
							}
							
							
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
		
        return handleReturnValues(result);
	}
}