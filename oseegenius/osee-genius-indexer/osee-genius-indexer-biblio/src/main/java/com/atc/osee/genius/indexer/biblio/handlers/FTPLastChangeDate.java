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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.Record;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts the FTP content type from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPLastChangeDate extends TagHandler implements IConstants
{
	final ThreadLocal<SimpleDateFormat> formatters = new ThreadLocal<SimpleDateFormat>() {
		
		@Override
		protected SimpleDateFormat initialValue() 
		{
			final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			formatter.setLenient(false);
			return formatter;
		};
	};
	
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		final ControlField field = (ControlField) record.getVariableField("008");
		String value = null;
		if (field != null) 
		{
			// 030429
			final String data = ((ControlField) field).getData();
			if (data != null && data.length() >= 5) 
			{
				value = data.substring(0, 6);
				if (value.startsWith("0") || value.startsWith("1"))
				{
					value = "20" +  value; 
				} else
				{
					value = "19" + value;
				}
			}
		} else
		{
			value = formatters.get().format(new Date());
		}
		
		return value;	
	}
}