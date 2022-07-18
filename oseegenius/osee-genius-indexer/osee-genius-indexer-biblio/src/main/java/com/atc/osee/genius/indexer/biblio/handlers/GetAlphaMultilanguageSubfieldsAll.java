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
 * Estrae i sottocampi alfabetici e inserisce la lingua in base al valore del sottocampo '2'.
 * Se $2 non è presente assegna il dato ad entrambe le lingue
 * Creato per i soggetti in multilingua del Museo Galielo (IMSS)
 * 
 * @author aguercio
 * @since 1.2
 */
public class GetAlphaMultilanguageSubfieldsAll extends TagHandler 
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
	                        		//bug 5871 - comment #3
	                        		case 'd':
	                        			builder.append(" ");
	                        			break;
                            	/*
                            		case 'v':
                            		case 'x':
                            		case 'y':
                            		case 'z':
                            			builder.append(" -- ");
                            			break;
                            		default:
                            			builder.append(" ");	
                            	*/
	                        	default:
	                        		builder.append(" -- ");	                        	
	                        	
                            	}
	                        } 
	                        builder.append(subfield.getData().trim());
                        }
                    }
                    
                    Subfield langPattern = dataField.getSubfield(subfieldWithanguage);
                    if( langPattern!=null) {
                    	if (langPattern.getData().contains("eng")){
							if (builder.length() != 0) 
							{
								builder.append(' ');	
							}
							builder.append("eng");
						}
                    	//if ita
                    	else {
							if (builder.length() != 0) 
							{
								builder.append(' ');	
							}
							builder.append("ita");
						}
                    }
                    
                    if (builder != null && builder.length() > 0)
                    {
                    	if (resultSet == null)
                    	{
                    		resultSet = new LinkedHashSet<String>();
                    	}
                    	
                    	if (langPattern!=null) {                    	
                    		resultSet.add(builder.toString());
                    	}
                    	//if no language specified, added twice (one in eng, one in ita)
                    	else {                         		
                    		resultSet.add(builder.toString() + " " + "ita");
                    		resultSet.add(builder.toString() + " " + "eng");                    		
                    	}
                    }
                }
            }
        }
        return resultSet;		
	}
	
	public static void main(final String[] args) throws IOException 
	{
		String directoryOrFileName = "/home/maura/Desktop/13354.mrc"; //args[0];

		File inputFile = new File(directoryOrFileName);
	
		if (inputFile.exists())
		{
			MarcReader reader = inputFile.isFile() 
					? new MarcStreamReader(new FileInputStream(inputFile)) 
					: new MarcDirStreamReader(inputFile);
		//	BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			
			while (reader.hasNext())
			{
				Record record = reader.next();
				GetAlphaMultilanguageSubfields f = new GetAlphaMultilanguageSubfields();
				Object result =f.getValue("650", record, null, null, null);
				System.out.println("Risultato: "+result);
				
//				writer.write(record.toString());
//				writer.newLine();
			}
			
			//writer.close();
		}
	}
}