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
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Returns the timestamp that is supposed to be used as index time.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetIndexTime extends TagHandler 
{
	private long indexTime;
	
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
	   	VariableField field008 = record.getVariableField("008");
	   	if (field008 != null)
	   	{
			ControlField field = (ControlField) field008;
	   		String data = field.getData();
	   		if (data != null && data.trim().length() >= 6)
	   		{
	   			String yymmdd = data.substring(0,6);
	   			switch (yymmdd.charAt(0))
	   			{
	   				case ' ':
	   				case '0':
	   				case '1':
	   				{
	   					yymmdd = "1" + yymmdd;
	   					break;
	   				}
	   			}
	   			
	   			return Long.parseLong(yymmdd);
	   		}
	   	}
		
		return null;
	}

	/**
	 * Sets the index time onto this handler.
	 * 
	 * @param indexTime the index time.
	 */
	public void setIndexTime(long indexTime) 
	{
		this.indexTime = indexTime;
	}
}