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
import com.atc.osee.genius.indexer.biblio.Utils;

/**
 * Concatenate a set of values separating them with a double minus.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ConcatenateWithSingleMinus extends TagHandler 
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
        Set<String> result = null;

        final String[] fieldTags = tagMappings.split(":");
        for (String tag :  fieldTags)
        {
            if (tag.length() < 3)
            {
                continue;
            }

            final String fieldTag = tag.substring(0, 3);
            final String subfieldTags = tag.substring(3);

            final List<VariableField> variableFields = record.getVariableFields(fieldTag);
            if (variableFields != null && !variableFields.isEmpty())
            {
                for (VariableField variableField : variableFields)
                {
                    final DataField dataField = (DataField) variableField;
                    StringBuilder buffer = null;
                    final List<Subfield> subfields = dataField.getSubfields();
                    if (subfields != null && !subfields.isEmpty())
                    {	
	                    for (Subfield subfield : subfields)
	                    {
	                    	if (subfieldTags.indexOf(subfield.getCode()) != -1)
	                        {
	                        	final String data = subfield.getData();
	                        	if (data != null && data.trim().length() != 0)
	                        	{               
	                        		if (buffer == null)
	                        		{
	                        			buffer = new StringBuilder();
	                        		}
	                        		
		                            if (buffer.length() > 0)
		                            {
		                            	switch(subfield.getCode())
		                            	{
		                            		case 'v':
		                            		case 'x':
		                            		case 'y':
		                            		case 'z':
		    	                            	buffer.append(" - ");
		                            			break;
		                            		default:
		    	                            	buffer.append(" ");	                            			
		                            	}
		                            }	                            
		                            buffer.append(data.trim());
	                        	}
	                        }
	                    }
                    } 
                    
                    if (buffer != null && buffer.length() > 0)
                    {
                    	if (result == null)
                    	{
                    		result = new LinkedHashSet<String>();
                    	}
                    	
                        result.add(Utils.cleanData(buffer.toString()));
                    }
                }
            }
        }
        
        return handleReturnValues(result);
	}
}