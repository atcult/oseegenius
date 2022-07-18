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
 * A tag handler that simply extracts a value from a marc record based on a given expression and filter inside a subfieled.
 * 
 * value(954abr) 
 * value(954a:954b:954r,4,390) sottocampi diversi + sub+ filtro
 * 
 * @author ztajoli
 * @since 1.0
 */
public class GetValueWithSubFilter extends TagHandler
{	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document){
		
		final String [] expressionAndFilter = tagMappings.split(",");
		char subFilter = 0;
		String valueFilter = null;
		if (expressionAndFilter.length == 3){
			subFilter = expressionAndFilter[1].charAt(0);
			valueFilter = expressionAndFilter[2];
		}
				
		Set<String> result = null;
		final String [] tags = expressionAndFilter[0].split(":");
		for (String tag : tags){
			if (IConstants._001.equals(tag)){
				return record.getControlNumber();
			} else if("005".equals(tag)) {
				final ControlField field = (ControlField) record.getVariableField("005");
				return field != null ? field.getData() : null;
			} else if (tag.length() > 3){
				final String tagNumber = tag.substring(0, 3);				
				if (isControlField(tagNumber)){
					String indexes = tag.substring(tag.indexOf("[") + 1, tag.indexOf("]"));
					String [] startAndEndIndex = indexes.split("-");
					int startIndex = Integer.parseInt(startAndEndIndex[0]);
					int endIndex = Integer.parseInt(startAndEndIndex[1]) + 1;
			
					List<VariableField> variableFields = record.getVariableFields(tagNumber);
					for (VariableField variableField : variableFields){
						ControlField controlField = (ControlField)variableField;
						String toAdd;
						try{
							toAdd = controlField.getData().substring(startIndex, endIndex).trim();
							if (toAdd!=null && toAdd.length()>0){
								if (result == null){
									result = new LinkedHashSet<String>();
								}
								result.add(toAdd);
							}
						} catch (Exception e) {
							//Nothing
						}
					}
				} else {				 
					final List<VariableField> fields = record.getVariableFields(tagNumber);
					//System.out.println("Entrato nel sistema"+"\n");
					if (fields != null && !fields.isEmpty()){
						final String subFieldNamesString = tag.substring(3);
						final char [] subfieldNames = new char[subFieldNamesString.length()];
						subFieldNamesString.getChars(0, subFieldNamesString.length(), subfieldNames, 0);
						for (VariableField f : fields){
							DataField field = (DataField) f;
							//System.out.println("1:"+ field +"\n");
							boolean valueCheck = false;
							if (valueFilter != null) {
								Subfield sub_to_check = field.getSubfield(subFilter);
								if (sub_to_check !=null){
									if (!sub_to_check.toString().isEmpty()) {
										if (sub_to_check.getData().equals(valueFilter)) {
											valueCheck = true;
										}
									}
								}
							}
							//System.out.println("2:"+ valueCheck +"\n");
							if (!valueCheck){
								continue;
							}
							//System.out.println("3 Si valora su:"+ field +"\n");
							StringBuilder buffer = null;
							for (char subfieldName : subfieldNames){
								final List<Subfield> subfields = field.getSubfields(subfieldName);
								if (subfields != null && !subfields.isEmpty()){
									for (Subfield subfield : subfields){
										if (subfield != null){
											String data = subfield.getData();
											data = (data != null && data.trim().length() != 0) ? data.trim() : null;
											if (data != null ) {
												if (buffer == null){
													buffer = new StringBuilder();
												}
												
												if (buffer.length() != 0) {
													buffer.append(getSubfieldSeparator());	
												}
												buffer.append(data);
											}
										}
									}
								}
							}
							String value = buffer != null ? buffer.toString().trim() : null;
							if (value != null && value.length() != 0){
								if (result == null){
									result = new LinkedHashSet<String>();
								}
								result.add(value);
							}
						}
					}
				}		
			}
		}
		//System.out.println("4 valore che esce:"+ result +"\n");
        return handleReturnValues(result);
	}
	
	protected String getSubfieldSeparator()
	{
		return " ";
	}
}