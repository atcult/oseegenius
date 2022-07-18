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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
 * Dedicated handler for N/T title heading browsing values.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class NTTitleBrowseHeading extends TagHandler
{	
	final static String SEPARATOR = "£££";
	
	private RemoveTrailingPunctuationTagHandler removeTrailingPunctuation = new RemoveTrailingPunctuationTagHandler();
	
	private Map<String, SubTagHandler> handlers = null;
	
	interface SubTagHandler
	{
		public Set<String> process(Record record);
	}
	
	class _8xx implements SubTagHandler
	{
		private final String tag;
		private final char skipSubfield;
		private final char tilSubfield;
		
		public _8xx(final String tag)
		{
			this.tag = tag;
			this.skipSubfield = 't';
			this.tilSubfield = 'v';
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
					
					// Check : if we don't have at least one t subfield skip this field.
					List<Subfield> ts = field.getSubfields('t');
					if (ts == null || ts.isEmpty())
					{
						continue;
					}
					
					final int skipCount = Utils.howManySkipChars(field, tag, null);
					
					boolean atLeastOneIsValid = false;
					for (Subfield subfield : ts)
					{
						if (subfield != null && subfield.getData() != null && subfield.getData().trim().length() != 0)
						{
							atLeastOneIsValid = true;
							break;
						}
					}
					
					if (!atLeastOneIsValid)
					{
						continue;
					}
					
					boolean tSubfieldHasBeenMet = false;
					boolean specialCharHasBeenInserted = false;
					for (Object subfieldObj : field.getSubfields())
					{
						final Subfield subfield = (Subfield) subfieldObj;
						if (subfield != null)
						{
							if (subfield.getCode() == tilSubfield)
							{
								break;
							}
							
							if (!tSubfieldHasBeenMet && subfield.getCode() == skipSubfield)
							{
								tSubfieldHasBeenMet = true;
							}
							
							final String data = subfield.getData();
							if (data != null && data.trim().length() != 0)
							{
								
								if (labelBuffer.length() != 0) 
								{
									labelBuffer.append(' ');	
								}
								
								labelBuffer.append(data);

								if (tSubfieldHasBeenMet)
								{
									if (valueBuffer.length() != 0) 
									{
										valueBuffer.append(' ');	
									}
									if (subfield.getCode() == skipSubfield)
									{
										valueBuffer.append(data.substring(skipCount));
									} else
									{
										if (!specialCharHasBeenInserted)
										{
											valueBuffer.append("Z");
											specialCharHasBeenInserted = true;
										}
										valueBuffer.append(data);
									}
								}
							}
						}
					}
					String label = (String) removeTrailingPunctuation.decorate("NA", labelBuffer.toString().trim());
					if (label.length() != 0)
					{
						valueBuffer.append(" ").append(label);
						if (valueBuffer.length() > 100)
						{
							valueBuffer.setLength(100);
						}
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
		if (handlers == null)
		{
			handlers = new HashMap<String, SubTagHandler>();
			handlers.put("100", new _8xx("100"));
			handlers.put("110", new _8xx("110"));
			handlers.put("111", new _8xx("111"));
			handlers.put("700", new _8xx("700"));
			handlers.put("710", new _8xx("710"));
			handlers.put("711", new _8xx("711"));
			handlers.put("800", new _8xx("800"));
			handlers.put("810", new _8xx("810"));
			handlers.put("811", new _8xx("811"));
		}
		
		final Set<String> result = new LinkedHashSet<String>();
		final String [] expressions = tagMappings.split(":");
		for (final String tag : expressions)
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