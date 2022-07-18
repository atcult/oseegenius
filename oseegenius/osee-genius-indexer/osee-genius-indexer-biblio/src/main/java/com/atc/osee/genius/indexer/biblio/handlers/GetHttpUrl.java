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
 * Extracts the HTTP URL from the 856 tag.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetHttpUrl extends TagHandler
{
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String attachmentType, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document)
	{
		List<VariableField> fields = record.getVariableFields(IConstants._856);
		
		if (fields != null)
		{
			for(VariableField f : fields) {
				DataField dataField = (DataField) f;
				Subfield subfield = dataField.getSubfield(IConstants._3);
				if (subfield != null && attachmentType.equals(subfield.getData()))
				{
					Subfield urlSubfield = dataField.getSubfield(IConstants.U);
					if (urlSubfield != null)
					{
						String url = urlSubfield.getData();
						if (url != null && url.trim().length() != 0)
						{
							return url;
						}
					}
				}
			}
		}
		return null;
	}
}