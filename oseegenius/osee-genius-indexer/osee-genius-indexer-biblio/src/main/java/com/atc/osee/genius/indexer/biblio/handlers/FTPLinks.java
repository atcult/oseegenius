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
public class FTPLinks extends TagHandler implements IConstants
{
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		final String type = tagMappings;
		Set<String> result = null;
		@SuppressWarnings("unchecked")
		List<VariableField> fields =record.getVariableFields("856");
		
		if (fields != null)
		{
			result = new LinkedHashSet<String>();
			for (VariableField f : fields)
			{
				DataField dataField = (DataField) f;
				if ("TOC".equals(tagMappings))
				{
					char i1 = dataField.getIndicator2();
					if (i1 != '0') 
					{
						continue;
					}
				}
				
				Subfield subfield = dataField.getSubfield('3');
				if (subfield != null && type.equals(subfield.getData()))
				{
					Subfield urlSubfield = dataField.getSubfield('u');
					if (urlSubfield != null)
					{
						String url = urlSubfield.getData();
						if (url != null && url.trim().length() != 0)
						{
							result.add(url);
						}
					}
				}
			}
		}
		return result;	
	}
}