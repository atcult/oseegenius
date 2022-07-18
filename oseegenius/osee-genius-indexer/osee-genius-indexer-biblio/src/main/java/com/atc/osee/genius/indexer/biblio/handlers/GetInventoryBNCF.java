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

import java.util.LinkedList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the inventory of a given marc records from SBNWEB.
 * 
 * Take tag 960 $e , substring 0-13
 * then delete the 'blank' inside the string
 * 
 * @author ztajoli
 * @since 1.2
 */
public class GetInventoryBNCF extends TagHandler implements IConstants{
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
					if (result == null) {
						result = new LinkedList<String>();
					}
					String temp = data.trim().substring(0,14).trim();
					temp = temp.replaceAll("\\s","");
					result.add(temp);
				}
			}
		}
		return handleReturnValues(result);
		
	}	
}