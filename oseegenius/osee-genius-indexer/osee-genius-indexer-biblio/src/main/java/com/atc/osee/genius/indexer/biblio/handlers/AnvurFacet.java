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

import java.util.Iterator;
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

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * A tag handler that simply extracts a value from a marc record based on a given expression.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AnvurFacet extends TagHandler
{	
	@Override
	public Object getValue(
			final String tagMappings,
			final Record record,
			final SolrParams settings,
			final SolrCore core,
			final SolrInputDocument document)
	{
		final List<VariableField> fields =  record.getVariableFields("590");
		final Set<String> result = new LinkedHashSet<String>();
		if (fields != null) {
			for (VariableField f : fields) {
				DataField field = (DataField)f;
				if ('8' == field.getIndicator1() && (' ' == field.getIndicator2() || '#' == field.getIndicator2())) {
					List<Subfield> _a = field.getSubfields('a');
					final String facet = createValue(_a);
					if (facet != null && facet.trim().length() != 0) {
						result.add(facet);
					}
				}
			}
		}
		
        return handleReturnValues(result);
	}
	
	public String createValue(final List<Subfield> values) {
		if (values != null && values.size() > 2) 
		{
			final Iterator<Subfield> iterator = values.iterator();
			iterator.next();
			iterator.next();			
			Subfield sf = iterator.next();
			if (sf != null && sf.getData() != null) {
				return sf.getData();
			}
		}
		return null;
	}
}