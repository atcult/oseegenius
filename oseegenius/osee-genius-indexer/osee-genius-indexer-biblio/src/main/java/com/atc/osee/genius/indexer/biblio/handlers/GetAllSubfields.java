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

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Extracts all the subfields of a given tag.
 * 
 * @author mbraddi
 * @since 1.0
 */
public class GetAllSubfields extends TagHandler 
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
        Set<String> resultSet = null;

        final String [] tags = tagMappings.split(":");
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
                StringBuilder builder = null;
                final DataField dataField = (DataField) variableField;
                if (dataField != null)
                {
                    for (Subfield subfield : (List<Subfield>)dataField.getSubfields())
                    {
                    	final char code = subfield.getCode();
                        if (skipSubfields == null || skipSubfields.indexOf(String.valueOf(code)) == -1)
                        {
                        	if (builder == null)
                        	{
                        		builder = new StringBuilder();
                        	}
                        	
	                        if (builder.length() > 0) 
	                        {
                                builder.append(' ');
	                        } 
	                        
	                        builder.append(subfield.getData().trim());
                        }
                    }
                    
                    if (builder != null && builder.length() > 0)
                    {
                    	if (resultSet == null)
                    	{
                    		resultSet = new LinkedHashSet<String>();
                    	}
                    	
                    	resultSet.add(builder.toString());
                    }
                }
            }
        }
        return handleReturnValues(resultSet);
	}
}