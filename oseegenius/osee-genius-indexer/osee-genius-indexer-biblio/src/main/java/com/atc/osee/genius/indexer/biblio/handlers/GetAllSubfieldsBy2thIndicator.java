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
public class GetAllSubfieldsBy2thIndicator extends TagHandler 
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
		String [] tagsAndIndicator = tagMappings.split(",");
        Set<String> resultSet = new LinkedHashSet<String>();
        String [] tags = tagsAndIndicator[0].split(":");

        for (String tag : tags)
        {
        	char [] subfields = null;
        	if (tag.length() > 3)
        	{
        		tag = tag.substring(0,3);
        		subfields = tag.substring(3).toCharArray();
        	}
        	
            for (VariableField variableField : (List<VariableField>)record.getVariableFields(tag))
            {
                StringBuilder builder = new StringBuilder(500);
                DataField dataField = (DataField) variableField;
                if (dataField != null)
                {
                	char requestedIndicator = tagsAndIndicator[1].trim().charAt(0);
                	char secondIndicator = dataField.getIndicator2();
                
                	
                	boolean mustProceed = false;
                	if (requestedIndicator == '#')
                	{
                		try 
                		{
                			Integer.parseInt(String.valueOf(secondIndicator));
                			mustProceed = false;
						} catch (Exception e) 
						{
							mustProceed = true;
						}
                	} else 
                	{
                		mustProceed = (requestedIndicator == secondIndicator);
                	}
                
                	if (mustProceed)
                	{
	                    for (Subfield subfield : (List<Subfield>)dataField.getSubfields())
	                    {
//	                        if (Character.isLetter(subfield.getCode()))
//                          {
	                        	if (subfields == null || mustBeProcessed(subfield.getCode(), subfields))
	                        	{
			                        if (builder.length() > 0) 
			                        {
		                                builder.append(' ');
			                        } 
			                        builder.append(subfield.getData().trim());
	                        	}
	                     // }
	                    }
	                    if (builder.length() > 0)
	                    {
	                    	resultSet.add(builder.toString());
	                    }
                	}
                }
            }
        }
        return resultSet;		
	}
	
	private boolean mustBeProcessed(char code, char[] subfields) 
	{
		for (char subfield : subfields)
		{
			if (subfield == code)
			{
				return true;
			}
		}
		return false;
	}
}