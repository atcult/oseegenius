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
import java.io.InputStream;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.ITagHandler;
import com.atc.osee.genius.indexer.biblio.log.MessageCatalog;

/**
 * A local text extractor value handler.
 * Even if inherits from tag handler it is not really a tag handler.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class TextHandler implements ITagHandler 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TextHandler.class);
	
	@Override
	public Object getValue(
			final String fileSuffix, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		String directoryName = settings.get("fulltext.directory");
		if (directoryName == null)
		{
			LOGGER.warn(MessageCatalog._000002_MISSING_FT_DIR);
			return null;
		}
		
		File directory = new File(directoryName);
		if (!directory.canRead())
		{
			LOGGER.warn(String.format(MessageCatalog._000003_INVALID_FT_DIR, directory.getAbsolutePath()));
			return null;			
		}
		
		File fulltext = getFullTextFile(fileSuffix, directory, record.getControlNumber());
		if (fulltext != null)
		{
			try 
			{
				AutoDetectParser parser = new AutoDetectParser(new TikaConfig(core.getResourceLoader().getClassLoader()));
				Metadata metadata = new Metadata();
				BodyContentHandler handler = new BodyContentHandler();
				InputStream inputStream = new FileInputStream(fulltext);
				parser.parse(inputStream, handler, metadata);
				inputStream.close();			
				return handler.toString();
			} catch (Exception exception)
			{
				LOGGER.error(
						String.format(MessageCatalog._000004_EXTRACT_TEXT_FAILURE, fulltext.getAbsolutePath()), 
						exception);
				return null;
			}
		} else 
		{
			LOGGER.warn(String.format(MessageCatalog._000005_FULL_TEXT_NOT_FOUND, record.getControlNumber()));			
			return null;
		}
	}

	/**
	 * Returns the file object that represents the fulltext document.
	 * 
	 * @param fileSuffix the file suffix.
	 * @param directory the source directory.
	 * @param uri the document URI.
	 * @return a file object that represents the fulltext document.
	 */
	private File getFullTextFile(final String fileSuffix, final File directory, final String uri)
	{
		// e.g. 0000000828383 --> 828383
		int id = Integer.parseInt(uri);
		
		// e.g. 8373237_DOC
		String filename = new StringBuilder().append(id).append(fileSuffix.toLowerCase()).toString();
		
		return scanDirectory(directory, filename);
	}
	
	/**
	 * TBD.
	 * 
	 * @param directory tbd.
	 * @param filename tbd.
	 * @return tbd.
	 */
	private File scanDirectory(final File directory, final String filename)
	{
		for (File entry : directory.listFiles())
		{
			if (entry.isFile())
			{
				String name = entry.getName().toLowerCase();
				if (name.indexOf(filename) != -1)
				{
					return entry;
				}
			} else 
			{
				File file = scanDirectory(entry, filename);
				if (file != null)
				{
					return file;
				}
			}
		}
		return null;
	}
}