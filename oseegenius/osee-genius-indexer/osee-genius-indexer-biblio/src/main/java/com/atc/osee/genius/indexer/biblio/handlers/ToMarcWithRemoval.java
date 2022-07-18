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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.MarcWriter;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.ITagHandler;

/**
 * Supertype layer for all "MARC" extractor.
 * As a common feature, it is possible to declare one or more fields that will be 
 * skept.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class ToMarcWithRemoval implements ITagHandler 
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(ToMarcWithRemoval.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		final String tags [] = tagMappings.split(":");
		List<VariableField> fieldsToRemove = null;
		if (tags.length > 0)
		{
			fieldsToRemove = new ArrayList<VariableField>(tags.length);
			for (String tag : tags)
			{
				fieldsToRemove.addAll(record.getVariableFields(tag));
				for (VariableField field : fieldsToRemove)
				{
					record.removeVariableField(field);			
				}
			}
		}
		
		MarcWriter writer = null;
        try
        {
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        writer = getMarcWriter(outputStream, IConstants.UTF8);
	        writer.write(record);
	        writer.close();
            return outputStream.toString(IConstants.UTF8);
        } catch (UnsupportedEncodingException exception)
        {
        	LOGGER.error("Unable to stream out the given record.", exception);
        	return null;
        } finally 
        {
        	try  { if (writer != null) writer.close(); } catch (Exception exception)  { }
        	
    		if (fieldsToRemove != null)
    		{
        		for (VariableField field : fieldsToRemove)
        		{
        			record.addVariableField(field);			
        		}
    		}
        }
	}
	
	/**
	 * Returns the MARC writer used for the concrete export.
	 * 
	 * @param outputStream the output stream.
	 * @param charset the charset encoding that will be used.
	 * @return the MARC writer implementation.
	 */
	protected abstract MarcWriter getMarcWriter(OutputStream outputStream, String charset);
}
