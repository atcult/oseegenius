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

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the FTP content type from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTP249 extends TagHandler implements IConstants
{
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		DataField _249 = (DataField) record.getVariableField("249");
		if (_249 != null )
		{
			StringBuilder builder = new StringBuilder();
			Subfield $a = _249.getSubfield('a');
			
			if ($a != null)
			{
				builder.append($a.getData());
			}
			
			Subfield $b = _249.getSubfield('b');
			
			if ($b != null)
			{
				builder.append(" - ");
				builder.append($b.getData());
			}

			Subfield $c = _249.getSubfield('c');
			
			if ($c != null)
			{
				builder.append(", ");
				builder.append($c.getData());
			}
			return builder.toString();
		}
		//$aanno$bidFascicolo$cdescrizione
		return null;
	}
}