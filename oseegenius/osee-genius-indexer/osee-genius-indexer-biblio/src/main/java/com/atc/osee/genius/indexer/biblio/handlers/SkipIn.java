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
 * A tag handler that simply extracts a value from a marc record based on a given expression.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class SkipIn extends TagHandler
{	
	private static final Logger LOGGER = LoggerFactory.getLogger(SkipIn.class);
	
	private Properties tags2SkipIndicator;
	
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
		
		final String [] expressionAndFilter = tagMappings.split(",");
		Set<String> result = null;
		final String [] tags = expressionAndFilter[0].split(":");
		for (String tag : tags)
		{
			if (tag.length() > 3)
			{
				final String tagNumber = tag.substring(0, 3);				
				final List<VariableField> fields = record.getVariableFields(tagNumber);
				if (fields != null)
				{
					String subFieldNamesString = tag.substring(3);
					char [] subfieldNames = new char[subFieldNamesString.length()];
					subFieldNamesString.getChars(0, subFieldNamesString.length(), subfieldNames, 0);
					
					for (VariableField f : fields)
					{
						DataField field = (DataField)f;
						final int skipCount = Utils.howManySkipChars(field, tagNumber, tags2SkipIndicator);
						StringBuilder buffer = null;
						for (char subfieldName : subfieldNames)
						{
							final List<Subfield> subfields = field.getSubfields(subfieldName);
							for (Subfield subfield : subfields)
							{
								if (subfield != null)
								{
									String data = subfield.getData();
									if (data != null && subfield.getCode() == 'a' && data.length() >= skipCount)
									{
										data = data.substring(skipCount);
									}

									data = (data != null && data.trim().length() != 0) ? data.trim() : null;
									if (data != null)
									{
										if (buffer == null)
										{
											buffer = new StringBuilder();
										}
										
										if (buffer.length() != 0) 
										{
											buffer.append(' ');	
										}
										buffer.append(data);
									}
								}
							}
						}
						
						String value = buffer != null ? buffer.toString().trim() : null;
						if (value != null && value.length() != 0)
						{
							if (result == null)
	                    	{
								result = new LinkedHashSet<String>();
	                    	}
							
							result.add(buffer.toString().trim());
						}
					}
				}
			}
		}
        return handleReturnValues(result);
	}
}