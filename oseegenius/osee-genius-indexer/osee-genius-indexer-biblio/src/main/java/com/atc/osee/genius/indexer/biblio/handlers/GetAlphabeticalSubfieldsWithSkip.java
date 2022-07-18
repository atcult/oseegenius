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

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.TagHandler;
import com.atc.osee.genius.indexer.biblio.Utils;

/**
 * Extracts all the alphabetical subfields of a given tag.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetAlphabeticalSubfieldsWithSkip extends TagHandler 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GetAlphabeticalSubfieldsWithSkip.class);
	private Properties tags2SkipIndicator;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		
		if (tags2SkipIndicator == null)
		{
			tags2SkipIndicator = new Properties();
			try 
			{
				tags2SkipIndicator.load(core.getResourceLoader().openResource("skip.properties"));
			} catch (IOException exception) 
			{
				LOGGER.error("Unable to load skip-in properties file.", exception);
			}
		}
		
        Set<String> resultSet = new LinkedHashSet<String>();

        String [] tags = tagMappings.split(":");
        for (String tag : tags)
        {
        	String skipSubfields = null;
        	if (tag.length() > 3)
        	{
        		skipSubfields = tag.substring(3);
        		tag = tag.substring(0,3);
        	}
        	
            for (VariableField variableField : (List<VariableField>)record.getVariableFields(tag))
            {
                StringBuilder builder = new StringBuilder(500);
                DataField dataField = (DataField) variableField;
                if (dataField != null)
                {
                    for (Subfield subfield : (List<Subfield>)dataField.getSubfields())
                    {
                    	char code = subfield.getCode();
                        if (Character.isLetter(code) && (skipSubfields == null || skipSubfields.indexOf(String.valueOf(code)) == -1))
                        {
                        	
	                        if (builder.length() > 0) 
	                        {
                                builder.append(' ');
	                        }

	                        String data = subfield.getData();
	                        if (data != null && subfield.getCode() == 'a')
							{
	                        	int skipCount = Utils.howManySkipChars(dataField, tag, tags2SkipIndicator);
								data = data.substring(skipCount);
							}
	                        builder.append(data.trim());
                        }
                    }
                    if (builder.length() > 0)
                    {
                    	resultSet.add(builder.toString());
                    }
                }
            }
        }
        return resultSet;		
	}
}