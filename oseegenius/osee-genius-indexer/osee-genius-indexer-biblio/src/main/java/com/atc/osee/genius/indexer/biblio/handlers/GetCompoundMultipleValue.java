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
 * A tag handler extract tags value and pairs them with another tag value, separated by a separator
 * 
 * @author aguercio
 * @since 1.2
 */
public class GetCompoundMultipleValue extends TagHandler
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
		List<String> results = new ArrayList<String>();
		List<String> valueFirstTag = new ArrayList();
		List<String> valueSecondTag = new ArrayList();
		
		String [] labelAndValuePairs = tagMappings.split(",");
		
		String[] tagToPair = labelAndValuePairs[0].split(":");
		
		//read firstTag values
		for (int i = 0; i < tagToPair.length; i ++) {
			String part = tagToPair[i];
			valueFirstTag.addAll(getValue(part, record));			
		}		
		//read secondTag values			
		if(labelAndValuePairs.length > 1) {
			String secondTag = labelAndValuePairs[1];
			valueSecondTag.addAll(getValue(secondTag, record));
		}
		
		for (String first : valueFirstTag) {
			for(String second : valueSecondTag) {
				results.add(removeTrailingPunctuation.decorate("NA",first) + SEPARATOR + second);
			}
			//da vedere se necessario aggiungere anche gli elementi della lista senza la collection, per il caso di
			//ricerca su tutte le banche dati
			if(valueSecondTag.isEmpty()) {
				results.add(removeTrailingPunctuation.decorate("NA",first) + "");
			}
		}
		if(results.isEmpty()) return null;
		return results;

	}
	
	@SuppressWarnings("unchecked")
	public List<String> getValue(final String tagMappings, final Record record) {
		List<String> results = new ArrayList<String>();				
		String [] expressionAndFilter = tagMappings.split(",");
		String filter = (expressionAndFilter.length == 2) ? expressionAndFilter[1] : null;
		Set<String> result = new LinkedHashSet<String>();
		String [] tags = expressionAndFilter[0].split(":");
		for (String tag : tags)
		{
			if (IConstants._001.equals(tag))
			{
				results.add(record.getControlNumber());
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
						results.add(controlField.getData().substring(startIndex, endIndex));
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
								results.add(value);
							}
						}
					}
				}		
			}
		}
		
        return results;
	}
}