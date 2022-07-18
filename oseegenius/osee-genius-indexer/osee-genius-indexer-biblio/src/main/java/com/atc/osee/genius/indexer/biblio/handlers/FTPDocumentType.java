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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the FTP content type from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPDocumentType extends TagHandler implements IConstants
{
	static enum DocumentType
	{
		DIGITAL { @Override public String toString() { return "dg";}},
		ON_LINE { @Override public String toString() { return "ol";}},
		CONTINUAZIONE { @Override public String toString() { return "ct";}},
		CARTACEO{ @Override public String toString() { return "ca";}};
	}
	
	static final class Configuration
	{	
		private final static Configuration INSTANCE = new Configuration();
		
		private final Map<Character,String> documentTypes = new HashMap<Character, String>();
		
		private final String processId;
		
		public static Configuration getInstance()
		{
			return INSTANCE;
		}
		
		private Configuration()
		{
			processId = UUID.randomUUID().toString();
			documentTypes.put('1', DocumentType.DIGITAL.toString());
			documentTypes.put('2', DocumentType.ON_LINE.toString());
			documentTypes.put('3', DocumentType.CONTINUAZIONE.toString());
			documentTypes.put('4', DocumentType.CARTACEO.toString());
			documentTypes.put(null, DocumentType.CARTACEO.toString());
		}
		
		public String getProcessId()
		{
			return processId;
		}
		
		public String getDocumentType(Character indicatorKey)
		{
			String result = documentTypes.get(indicatorKey);
			return result != null ? result : DocumentType.CARTACEO.toString();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		List<VariableField> fields = record.getVariableFields(tagMappings);
		Set<String> result = new LinkedHashSet<String>();
		if (fields == null || fields.isEmpty())
		{
    		result.add(DocumentType.CARTACEO.toString());			
    		return result;
		} 
		
		for (VariableField f : fields)
		{
			DataField field = (DataField) f;
			if (field != null)
	    	{
				Character firstIndicator = field.getIndicator1();
		    	result.add(Configuration.getInstance().getDocumentType(firstIndicator));
	    	} else 
	    	{
	    		result.add(DocumentType.CARTACEO.toString());
	    	}
		}
		return result;
	}
}