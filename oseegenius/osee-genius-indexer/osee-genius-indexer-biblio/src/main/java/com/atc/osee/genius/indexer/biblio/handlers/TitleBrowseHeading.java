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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
 * Dedicated handler for title heading browsing values.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class TitleBrowseHeading extends TagHandler
{	
	Properties tags2SkipIndicator;
	
	final static String SEPARATOR = "£££";
	private static final Logger LOGGER = LoggerFactory.getLogger(TitleBrowseHeading.class);
	
	private RemoveTrailingPunctuationTagHandler removeTrailingPunctuation = new RemoveTrailingPunctuationTagHandler();
	
	private Map<String, SubTagHandler> handlers = null;
	
	interface SubTagHandler
	{
		public Set<String> process(Record record);
	}
	
	class AllFieldsTil implements SubTagHandler
	{
		private final String tag;
		private final char skipSubfield;
		private final Character tilSubfield;
		
		public AllFieldsTil(final String tag, char skipSubfield, Character tilSubfield)
		{
			this.tag = tag;
			this.skipSubfield = skipSubfield;
			this.tilSubfield = tilSubfield;
		}
				
		@Override
		public Set<String> process(final Record record) 
		{
			final Set<String> result = new LinkedHashSet<String>();
			final List<VariableField> fields = record.getVariableFields(tag);
			if (fields != null && !fields.isEmpty())
			{
				for (VariableField f : fields)
				{
					DataField field = (DataField)f;
					final StringBuilder labelBuffer = new StringBuilder();
					final StringBuilder valueBuffer = new StringBuilder();
					final int skipCount = Utils.howManySkipChars(field, tag, tags2SkipIndicator);
					for (Object subfieldObj : field.getSubfields())
					{
						final Subfield subfield = (Subfield) subfieldObj;
						if (subfield != null)
						{
							if (tilSubfield != null && subfield.getCode() == tilSubfield)
							{
								break;
							}
							
							final String data = subfield.getData();
							if (data != null && data.trim().length() != 0)
							{
								if (labelBuffer.length() != 0) 
								{
									labelBuffer.append(' ');	
								}
								labelBuffer.append(data);
								
								if (valueBuffer.length() != 0) 
								{
									valueBuffer.append(' ');	
								}
								if (subfield.getCode() == skipSubfield)
								{
									valueBuffer.append(data.substring(skipCount));
								} else
								{
									valueBuffer.append(data);
								}
							}
						}
					}
					String label = (String) removeTrailingPunctuation.decorate("NA", labelBuffer.toString().trim());
					if (label.length() != 0)
					{
						result.add(label + SEPARATOR  + removeTrailingPunctuation.decorate("NA", valueBuffer.toString().trim()));
					}
				}
			}
			
			return result;
		}	
	};
	
	class AllFieldsLess implements SubTagHandler
	{
		private final String tag;
		private final char skipSubfield;
		private final char less;
		
		public AllFieldsLess(final String tag, char skipSubfield, char less)
		{
			this.tag = tag;
			this.skipSubfield = skipSubfield;
			this.less = less;
		}
		
		@Override
		public Set<String> process(final Record record) 
		{
			final Set<String> result = new LinkedHashSet<String>();
			final List<VariableField> fields = record.getVariableFields(tag);
			if (fields != null && !fields.isEmpty())
			{
				for (VariableField f : fields)
				{
					DataField field = (DataField)f;
					final StringBuilder labelBuffer = new StringBuilder();
					final StringBuilder valueBuffer = new StringBuilder();
					final int skipCount = Utils.howManySkipChars(field, tag, tags2SkipIndicator);
					for (Object subfieldObj : field.getSubfields())
					{
						final Subfield subfield = (Subfield) subfieldObj;
						if (subfield != null && subfield.getCode() != less)
						{
							final String data = subfield.getData();
							if (data != null && data.trim().length() != 0)
							{
								if (labelBuffer.length() != 0) 
								{
									labelBuffer.append(' ');	
								}
								labelBuffer.append(data);
								
								if (valueBuffer.length() != 0) 
								{
									valueBuffer.append(' ');	
								}
								if (subfield.getCode() == skipSubfield)
								{
									valueBuffer.append(data.substring(skipCount));
								} else
								{
									valueBuffer.append(data);
								}
							}
						}
					}
					String label = (String) removeTrailingPunctuation.decorate("NA", labelBuffer.toString().trim());
					if (label.length() != 0)
					{
						result.add(label + SEPARATOR  + valueBuffer.toString().trim());
					}
				}
			}
			return result;
		}		
	};
	
	class AllFieldsBetween implements SubTagHandler
	{
		private final String tag;
		private final char skipSubfield;
		private final char start;
		private final char end;
		
		public AllFieldsBetween(final String tag, char skipSubfield, char start, char end)
		{
			this.tag = tag;
			this.skipSubfield = skipSubfield;
			this.start = start;
			this.end = end;
		}
		
		@Override
		public Set<String> process(final Record record) 
		{
			final Set<String> result = new LinkedHashSet<String>();
			final List<VariableField> fields = record.getVariableFields(tag);
			if (fields != null && !fields.isEmpty())
			{
				for (VariableField f : fields)
				{
					DataField field = (DataField)f;
					final StringBuilder labelBuffer = new StringBuilder();
					final StringBuilder valueBuffer = new StringBuilder();
					final int skipCount = Utils.howManySkipChars(field, tag, tags2SkipIndicator);
					boolean include = false;
					for (Object subfieldObj : field.getSubfields())
					{
						final Subfield subfield = (Subfield) subfieldObj;
						if (subfield != null)
						{
							char code = subfield.getCode();
							
							if (code == end)
							{
								break;
							}
							
							if (!include && code == start)
							{
								include = true;
							}
											
							if (include)
							{
								final String data = subfield.getData();
								if (data != null && data.trim().length() != 0)
								{
									if (labelBuffer.length() != 0) 
									{
										labelBuffer.append(' ');	
									}
									labelBuffer.append(data);
									
									if (valueBuffer.length() != 0) 
									{
										valueBuffer.append(' ');	
									}
									if (subfield.getCode() == skipSubfield)
									{
										valueBuffer.append(data.substring(skipCount));
									} else
									{
										valueBuffer.append(data);
									}
								}
							}
						}
					}
					String label = (String) removeTrailingPunctuation.decorate("NA", labelBuffer.toString().trim());
					if (label.length() != 0)
					{
						result.add(label + SEPARATOR  + valueBuffer.toString().trim());
					}
				}
			}
			return result;
		}		
	};	
	
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
		
		if (handlers == null)
		{
			handlers = new HashMap<String, SubTagHandler>();
			handlers.put("130", new AllFieldsTil("130",'a',null));			
			handlers.put("240", new AllFieldsTil("240",'a',null));
			handlers.put("245", new AllFieldsTil("245",'a','c'));
			handlers.put("246", new AllFieldsLess("246",'a','i'));
			handlers.put("440", new AllFieldsTil("440",'a','v'));			
			handlers.put("730", new AllFieldsTil("730",'a',null));			
			handlers.put("740", new AllFieldsTil("740",'a',null));
			handlers.put("800", new AllFieldsBetween("100",'t','t','v'));
			handlers.put("810", new AllFieldsBetween("110",'t','t','v'));
			handlers.put("811", new AllFieldsBetween("111",'t','t','v'));
			handlers.put("800", new AllFieldsBetween("700",'t','t','v'));
			handlers.put("810", new AllFieldsBetween("710",'t','t','v'));
			handlers.put("811", new AllFieldsBetween("711",'t','t','v'));
			handlers.put("800", new AllFieldsBetween("800",'t','t','v'));
			handlers.put("810", new AllFieldsBetween("810",'t','t','v'));
			handlers.put("811", new AllFieldsBetween("811",'t','t','v'));
			handlers.put("830", new AllFieldsTil("830",'a','v'));
			
		}
		
		final Set<String> result = new LinkedHashSet<String>();
		final String [] expressions = tagMappings.split(":");
		for (String tag : expressions)
		{
			final SubTagHandler handler = handlers.get(tag);
			if (handler != null)
			{
				result.addAll(handler.process(record));
			}
		}
		return handleReturnValues(result);
	}
}