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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.hdfs.server.namenode.nn_005fbrowsedfscontent_jsp;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.MarcDirStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * Estrae i sottocampi alfabetici e inserisce la lingua in base ai seguenti criteri:
 *
 * - inserisce ita nel caso non ci sia alcun $2 eng
 * - inserisce eng nel caso ci sia un $2 eng, oppure nel caso che non sia contenuto x o y
 * 
 * Creato per i soggetti (tag 600) in multilingua del Museo Galielo (IMSS)
 * 
 * @author aguercio
 * @since 1.2
 */
public class GetAlphaMultilanguageSubfieldsSubjPers extends TagHandler  {
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
        
        final String [] tagsAndSubfield = tagMappings.split(";");
        final char subfieldWithanguage = tagsAndSubfield[1].charAt(0);
        final String [] tags = tagsAndSubfield[0].split(":");
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
                	boolean containsXorY = false;
                    for (Subfield subfield : (List<Subfield>)dataField.getSubfields())
                    {
                    	char code = subfield.getCode();
                    	
                        if (Character.isLetter(code) && (skipSubfields == null || skipSubfields.indexOf(String.valueOf(code)) == -1))
                        {
                        	if (builder == null)
                        	{
                        		builder = new StringBuilder();
                        	}
                        	
	                        if (builder.length() > 0) 
	                        {
	                        	switch(subfield.getCode())
                            	{
	                        	
                            	case 'b':
                            		builder.append(" ");                            		
                            		break;
                            		
                            	case 'c':
                            		builder.append(" ");                            		
                            		break;
	                        	
	                        	//bug 5871 - comment #3
                        		case 'd':
                        			builder.append(" ");
                        			break;
                            	
                        		case 'x':
                            		builder.append(" -- ");	 
                            		containsXorY = true;
                            		break;
                            	case 'y':
                            		builder.append(" -- ");	
                            		containsXorY = true;
                            		break;
                            		
                            	case 'v':
                            		builder.append(" -- ");	
                            		containsXorY = true;
                            		break;
                            		
                            	case 'z':
                            		builder.append(" -- ");	
                            		containsXorY = true;
                            		break;
	                        	default:
	                        		builder.append(" -- ");	                        	
	                        	
                            	}
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
                    	 Subfield langPattern = dataField.getSubfield(subfieldWithanguage);
                         if( (langPattern!=null && langPattern.getData().contains("eng")) || 
                         		(!containsXorY)){
                        	 
                        	 	resultSet.add(builder.toString() + " " + "eng");
     						}
                         if( (langPattern != null && !langPattern.getData().contains("eng")) ||
                         		(langPattern == null)) {
                        	 
                        	 resultSet.add(builder.toString() + " " + "ita");
     						}
                    	
                    }
                }
            }
        }
        return resultSet;		
	}
}