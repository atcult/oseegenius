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
 * Extracts the collocation a given marc record.
 * 
 * Take tag one or more  960 $d[3-...] + 960$e[54-...]
 * 
 * 
 * @author aguercio
 * @since 1.2
 */
public class GetCollocationBNCF extends TagHandler implements IConstants
{
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document)  {
		
		LinkedList<String> result = new LinkedList<String>();
		final List<VariableField> fields = record.getVariableFields("960");
		for(VariableField f : fields) {
			DataField field = (DataField) f;
			Subfield subfield_d = field.getSubfield('d');
			Subfield subfield_e = field.getSubfield('e');
			StringBuilder buffer = null;
			
			if (subfield_d != null) {
				String data = subfield_d.getData();
				if (data.length() > 4) {
					if (buffer == null)
					{
						buffer = new StringBuilder();
					}
					buffer.append(data.substring(3).trim());						
				}
			}
			if (subfield_e != null) {
				String data = subfield_e.getData();
				if (data.length() >= 44) {
					if (buffer == null)
					{
						buffer = new StringBuilder();
					}
					buffer.append(" "+data.substring(14,44).trim());	
				}
			}
			if (buffer != null) {
				result.add(buffer.toString().replaceAll("\\s{2,}", " "));
			}
		}
		return handleReturnValues(result);		
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
		
		
		GetCollocationBNCF handler = new GetCollocationBNCF();		
		Object result = handler.getValue("", record, null, null, null);
		System.out.println(result);
	}
}