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

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * A tag handler that simply extracts a value from a marc record based on a given expression.
 * 
 * value(954abr) 
 * value(954a:954b:954r,pippo,>1#) sottocampi diversi + filtro + indicatori
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetFirstNumericValueForEachTag extends TagHandler
{	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
		
		String tag = tagMappings.substring(0, 3);
		char subFieldName = tagMappings.charAt(3);
		List<String> result = null;
		List<VariableField> fields = record.getVariableFields(tag);
		if (fields != null)
		{
			result = new ArrayList<String>(fields.size());
			for(VariableField f : fields) {
				DataField field = (DataField) f;
				List<Subfield> subfields = field.getSubfields(subFieldName);
				for (Subfield subfield : subfields)
				{
					if (subfield != null)
					{
						String data = subfield.getData();
						try 
						{
							Integer.parseInt(data);
							result.add(data);
//							break;
						} catch (Exception exception)
						{
							continue;
						}
					}
				}
			}
		}		
        return handleReturnValues(result);
	}
	
	protected String getSubfieldSeparator()
	{
		return " ";
	}
}