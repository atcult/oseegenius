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

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.ITagHandler;
import com.atc.osee.genius.indexer.biblio.log.MessageCatalog;

/**
 * A remote resource text extractor value handler.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetRemoteResourceContentHandler implements ITagHandler 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GetRemoteResourceContentHandler.class);
	
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		String [] tags = tagMappings.split(":");
		for (String tag : tags)
		{
			if (tag.length() == 4)
			{
				char subfieldCode = tag.charAt(3);
				List<VariableField> fields = record.getVariableFields(tag.substring(0,3));
				if (fields != null)
				{
					for (VariableField f : fields)
					{
						DataField field = (DataField)f;
						Subfield subfield = field.getSubfield(subfieldCode);
						if (subfield != null)
						{
							String address = subfield.getData();
							if (address != null && address.trim().length() != 0)
							{
								address = address.startsWith("http") ? address : "http://" + address;
								try 
								{
									String text = getResourceContent(new URL(address), core);
									if (text != null && text.trim().length() != 0)
									{
										return text;
									}
								} catch (Exception exception)
								{
									LOGGER.error("", exception);
								}
							}
						}
					}
				}
			} else 
			{
				LOGGER.error("Cannot evaluate tag expression without a subfield. " + tagMappings);
			}
		}
		return null;
	}
	
	public String getResourceContent(final URL url, SolrCore core)
	{
		InputStream inputStream = null;
		try 
		{
			inputStream = url.openStream();
			
			AutoDetectParser parser = new AutoDetectParser(new TikaConfig(core.getResourceLoader().getClassLoader()));
			BodyContentHandler handler = new BodyContentHandler();
			parser.parse(inputStream, handler, new Metadata());
	
			return handler.toString();
		} catch (Exception exception)
		{
			LOGGER.error(
					String.format(MessageCatalog._000004_EXTRACT_TEXT_FAILURE,url), 
					exception);
			return null;
		} finally
		{
			try 
			{
				inputStream.close();
			} catch (Exception exception) 
			{
				// Nothing
			}
		}
	}	
}