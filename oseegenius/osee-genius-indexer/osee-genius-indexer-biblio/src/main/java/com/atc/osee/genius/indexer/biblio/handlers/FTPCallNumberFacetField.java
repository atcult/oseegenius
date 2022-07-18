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
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the FTP content type from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPCallNumberFacetField extends TagHandler implements IConstants
{
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		Set<String> result = null;
		String fieldName = tagMappings.length() == 3 ? tagMappings : tagMappings.substring(0, 3);
		List<VariableField> fields = record.getVariableFields(fieldName);
		if (fields != null && !fields.isEmpty())
		{
			result = new LinkedHashSet<String>();
			for (VariableField f : fields)
			{
				DataField field = (DataField) f;
				Subfield subfield = field.getSubfield(tagMappings.charAt(tagMappings.length()-1));
				if (subfield != null)
				{
					String data = subfield.getData();
					if (data != null && data.trim().length() != 0)
					{
						int dotIndex = data.indexOf(".");
						if (dotIndex == -1)
						{
							result.add(data);							
						} else 
						{
							result.add(data.substring(0,dotIndex));
						}
					}
				}		
			}
		}
		return result;
	}	
}