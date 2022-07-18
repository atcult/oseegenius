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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.IConstants;
import com.atc.osee.genius.indexer.biblio.TagHandler;

/**
 * FTP Abstract
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPGetAbstract extends TagHandler implements IConstants
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FTPGetAbstract.class);
	
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		final String language = tagMappings;
		
		Set<String> abstractLinks = getLinks(record);
		if (abstractLinks != null)
		{
			for (String link: abstractLinks)
			{
				if (link.indexOf("ABS_" + language) != -1)
				{
					ControlField field = (ControlField) record.getVariableField(IConstants._001);
					Integer amicusNumber = Integer.parseInt(field.getData());					
					String filename = System.getProperty("txt.directory") + File.separator + "abs" + File.separator + amicusNumber + "_ABS_"+language + ".txt";
					return readAbstract(filename, amicusNumber);
				}
			}
		}
		return null;		
	}
	
	private Set<String> getLinks(Record record)
	{
		Set<String> result = null;
		List<VariableField> fields = record.getVariableFields("856");
		
		if (fields != null)
		{
			result = new LinkedHashSet<String>();
			for (VariableField f : fields)
			{
				DataField dataField = (DataField) f;				
				Subfield subfield = dataField.getSubfield('3');
				if (subfield != null && "ABS".equals(subfield.getData()))
				{
					Subfield urlSubfield = dataField.getSubfield('u');
					if (urlSubfield != null)
					{
						String url = urlSubfield.getData();
						if (url != null && url.trim().length() != 0)
						{
							result.add(url);
						}
					}
				}
			}
		}
		return result;
	}	
	
	private String readAbstract(String filename, Integer amicusNumber)
	{
		File f = new File(filename);
		if (!f.canRead())
		{
			return null;
		}
		
		BufferedReader reader = null;
		try 
		{
			// ABS: 2250056_ABS_ita.txt
	
			reader = new BufferedReader(new FileReader(filename));
			StringBuilder builder = new StringBuilder();
			
			String actLine = null;
			while ( (actLine = reader.readLine()) != null)
			{
				builder.append(actLine).append("\n");
			}
			return builder.toString();
		} catch(FileNotFoundException exception)
		{ 			
			LOGGER.error("<OG-1000024> : ABS associated with AN " + amicusNumber + " cannot be found (" + filename + ")");
			return null;
		} catch(IOException exception)
		{
			exception.printStackTrace();
			return null;			
		} finally 
		{
			try{ reader.close(); } catch (Exception exception){ }
		}
	}	
}