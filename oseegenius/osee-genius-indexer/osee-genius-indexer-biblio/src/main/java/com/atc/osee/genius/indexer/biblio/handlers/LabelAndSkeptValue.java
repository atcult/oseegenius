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
public class LabelAndSkeptValue extends TagHandler
{	
	private Properties tags2SkipIndicator;
	
	final static String SEPARATOR = "£££";
	private static final Logger LOGGER = LoggerFactory.getLogger(LabelAndSkeptValue.class);
	
	private RemoveTrailingPunctuationTagHandler removeTrailingPunctuation = new RemoveTrailingPunctuationTagHandler();
	
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
		
		return getValue(tagMappings, record);
	}
	
	public Object getValue(final String tagMappings, final Record record)
	{		
		Set<String> result = new LinkedHashSet<String>();
		String [] tags = tagMappings.split(":");
		for (String tag : tags)
		{
			final String tagNumber = tag.substring(0, 3);				
			final List<VariableField> fields = record.getVariableFields(tagNumber);
			if (fields != null && !fields.isEmpty())
			{
				final String subFieldNamesString = tag.substring(3);
				char [] subfieldNames = new char[subFieldNamesString.length()];
				subFieldNamesString.getChars(0, subFieldNamesString.length(), subfieldNames, 0);
				for (VariableField f : fields)
				{
					DataField field = (DataField)f;
					int skipCount = Utils.howManySkipChars(field, tagNumber, tags2SkipIndicator);
					
					StringBuilder buffer = new StringBuilder();
					StringBuilder skipBuffer = new StringBuilder();
					for (char subfieldName : subfieldNames)
					{
						final List<Subfield> subfields = field.getSubfields(subfieldName);
						if (subfields != null && !subfields.isEmpty())
						{
							for (Subfield subfield : subfields)
							{
								String data = subfield.getData();
								String skeptData = null;
								if (data != null && subfield.getCode() == 'a' && data.length() >= skipCount)
								{
									skeptData = data.substring(skipCount);
								}
								
								data = (data != null && data.trim().length() != 0) ? data.trim() : null;
								if (data != null)
								{
									if (buffer.length() != 0) 
									{
										buffer.append(' ');	
									}
									buffer.append(data);
									
									if (skeptData != null && skeptData.trim().length() != 0)
									{
										if (skipBuffer.length() != 0) 
										{
											skipBuffer.append(' ');	
										}
										skipBuffer.append(skeptData);
									} else 
									{
										if (skipBuffer.length() != 0) 
										{
											skipBuffer.append(' ');	
										}
										skipBuffer.append(data);
									}
								}
							}
						}
					}
					
					String value = (String) removeTrailingPunctuation.decorate("NA", buffer.toString().trim());
					if (value.length() != 0)
					{
						result.add(value + SEPARATOR  + skipBuffer.toString());
					}
				}
			}
		}
		
        return handleReturnValues(result);
	}
}