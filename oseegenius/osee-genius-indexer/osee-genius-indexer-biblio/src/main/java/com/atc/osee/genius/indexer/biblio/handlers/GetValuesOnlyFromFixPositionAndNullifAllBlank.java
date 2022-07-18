/*
 	Copyright (c) 2019 @Cult s.r.l. All rights reserved.
	
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


import java.util.LinkedList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * A tag handler that extracts data from subfields only with fix positions and output in order as they appear in record.
 * With data in the same order of tags inside mapping. Only one extraction every tag configured
 * If all data are blanck, the output is null
 * Es: 960d[14-45]:960e[55-]:921a[4-88]
 * 
 * @author aguercio, ztajoli
 * @since 1.2
 */
public class GetValuesOnlyFromFixPositionAndNullifAllBlank extends TagHandler {	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
				
		LinkedList<String> result = null;
		char preserveBlanks = 'N';
		final String [] tags = tagMappings.split(":");
		for (String tag : tags)	{
			String tagNumber = tag.substring(0, 3);
			String subfieldtag = tag.substring(3, 4);
			if (preserveBlanks == 'Y') { } else {
				preserveBlanks= tag.substring(4, 5).charAt(0);
			}
			String indexes = tag.substring(tag.indexOf("[") + 1, tag.indexOf("]"));
			String [] startAndEndIndex = indexes.split("-");
			int startIndex = Integer.parseInt(startAndEndIndex[0]);
			int endIndex = 0;
			if (1 >= startAndEndIndex.length) { } else {
				endIndex = Integer.parseInt(startAndEndIndex[1]) + 1;
			}

			List<VariableField> fields = record.getVariableFields(tagNumber);
			if (fields != null && !fields.isEmpty()){
				for (VariableField f : fields)
				{
					DataField field = (DataField)f;
					StringBuilder buffer = null;
					final List<Subfield> subfields = field.getSubfields();
					
					if (subfields != null && !subfields.isEmpty())	{
						for (Subfield subfield : subfields) {
							if (subfieldtag != null && subfieldtag.contains(subfield.getCode()+"" )) {
								String data = subfield.getData();
								
								if (endIndex == 0) {
									data = data.substring(startIndex);
								} else  {
									data = data.substring(startIndex, endIndex);
								} 
								
								if (data != null ){
									if (buffer == null) {
										buffer = new StringBuilder();
									}
									
									buffer.append(data);
								}							
							}
						}
					}
					try{
						String value = buffer.toString();
						if (value != null && value.length() != 0){
							if (result == null) {
								result = new LinkedList<String>();
							}
							result.add(value);
						}
					}catch (Exception e) {
						//Nothing
					}
				}
			}
		}		
		if (preserveBlanks == 'Y') {
			return handleReturnValues(result);
		}
		StringBuilder sb = new StringBuilder();
		try {
			for (String s : result){
				sb.append(s);
			}
			String check = sb.toString().trim();
			if (check.length()<1) {
				return null;
			}else	{
				return handleReturnValues(result);
			}
		}catch (Exception e) {
			return null;
		}
	}
	

	
	public static void main (String [] args)  {
		final MarcFactory factory = MarcFactory.newInstance();
		final Record record = factory.newRecord();
		
		final DataField _960 = factory.newDataField("960",'0', '0');
		_960.addSubfield(factory.newSubfield('d', "CF00623   4274 23   etwe"));
		record.addVariableField(_960);
		final DataField _960b = factory.newDataField("960",'0', '0');
		_960b.addSubfield(factory.newSubfield('d', "CF00625   7277 55   arrw"));
		record.addVariableField(_960b);
		
		
		GetValuesOnlyFromFixPositionAndNullifAllBlank handler = new  GetValuesOnlyFromFixPositionAndNullifAllBlank();		
		Object result = handler.getValue("960dY[7-8]", record, null, null, null);
		System.out.println(result);
	}
	
	
	
	
}