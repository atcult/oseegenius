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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.atc.osee.genius.indexer.biblio.log.MessageCatalog;

/**
 * Concatenate a set of values separating them with a double minus.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class CesvotSubject extends TagHandler 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CesvotSubject.class);

	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
        Set<String> result = new LinkedHashSet<String>();

        String[] fieldTags = tagMappings.split(":");
        for (String tag : fieldTags)
        {
            if (tag.length() < 3)
            {
            	LOGGER.error(String.format(MessageCatalog._000001_INVALID_TAG, tag));
                continue;
            }

            String fieldTag = tag.substring(0, 3);
            String subfieldTags = tag.substring(3);

            List<VariableField> variableFields = record.getVariableFields(fieldTag);
            if (!variableFields.isEmpty())
            {
                Pattern subfieldPattern = Pattern.compile(subfieldTags.length() == 0 ? "." : "[" + subfieldTags + "]");
                for (VariableField variableField : variableFields)
                {
                    DataField dataField = (DataField) variableField;
                    StringBuffer buffer = new StringBuffer();
                    List<Subfield> subfields = dataField.getSubfields();
                    
                    for (Subfield subfield : subfields)
                    {
                        Matcher matcher = subfieldPattern.matcher(String.valueOf(subfield.getCode()));
                        if (matcher.matches())
                        {
                        	String data = subfield.getData();
                        	if (data != null && data.trim().length() != 0)
                        	{                        	
	                            if (buffer.length() > 0)
	                            {
	                            	switch(subfield.getCode())
	                            	{
	                            		case 'a':
	    	                            	buffer.append(" -- ");
	                            			break;
	                            		default:
	    	                            	buffer.append(" ");	                            			
	                            	}
	                            }	                            
	                            buffer.append(data.trim());
                        	}
                        }
                    }
                    if (buffer.length() > 0)
                    {
                        result.add(Utils.cleanData(buffer.toString()));
                    }
                }
            }
        }
        
        return handleReturnValues(result);
	}
}