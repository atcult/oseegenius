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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
 * Extracts the FTP permalink from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPPermalink extends TagHandler implements IConstants {
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) {
		Set<String> result = null;
		final List<VariableField> _856s = record.getVariableFields("856");
		if (_856s != null && !_856s.isEmpty()) {
			result = new TreeSet<String>();
			for (VariableField f : _856s)
			{
				DataField _856 = (DataField) f;			
				char i1 = _856.getIndicator1();
				char i2 = _856.getIndicator2();
				if (i1 == '4' && (i2 == ' ' || i2 == '#' || i2 == '0')) {
					final Subfield permalink = _856.getSubfield('u');
					if (permalink != null) {							
						final String data = permalink.getData();
						if (data != null && data.trim().length() !=0 && data.indexOf("libraweb") == -1) {
							result.add(data);
						}
					}					
				}
			}
		}
		return result != null && !result.isEmpty() ? result : null;
	}
}