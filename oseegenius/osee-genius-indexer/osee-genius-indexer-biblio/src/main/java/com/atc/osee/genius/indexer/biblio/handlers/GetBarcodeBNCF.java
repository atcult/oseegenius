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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.Format;
import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the barcode of a given marc records.
 * 
 * Take tag 960 $e 
 * if copy is CF substring 2-13
 * else, substring 0-13
 * 
 * @author aguercio
 * @since 1.2
 */
public class GetBarcodeBNCF extends TagHandler implements IConstants
{
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document)  {
		
		List<String> result = null;
		final List<VariableField> fields = record.getVariableFields("960");
		for(VariableField f : fields) {
			DataField field = (DataField) f;	
			Subfield subfield = field.getSubfield('e');
			if (subfield != null) {
				String data = subfield.getData();
				if (data.length() > 13) {
					String copyInstitution = data.substring(0,2);
					if ("CF".equals(copyInstitution)) {
						if (result == null) {
							result = new LinkedList<String>();
						}					
						
						result.add(data.substring(2,14).trim());
					}
					else {
						if (result == null) {
							result = new LinkedList<String>();
						}	
						String barcode = data.trim().substring(0,14).trim();
						//adjust barcode to what render on opac
						barcode = barcode.replaceAll("\\s+", " ");
						result.add(barcode);
					}
				}
			}
		}
		return handleReturnValues(result);
		
	}	
	
	public static void main (String [] args)  {
		final MarcFactory factory = MarcFactory.newInstance();
		final Record record = factory.newRecord();
		final DataField _960 = factory.newDataField("960",'0', '0');
		//_960.addSubfield(factory.newSubfield('e', "CF   006234274"));
		_960.addSubfield(factory.newSubfield('e', "MF   000148267"));
		
		record.addVariableField(_960);
		
		GetBarcodeBNCF handler = new GetBarcodeBNCF();		
		Object result = handler.getValue("", record, null, null, null);
		System.out.println(result);
	}
}