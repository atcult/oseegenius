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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.tika.metadata.Metadata;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.DefaultHandler2;

import com.atc.osee.genius.indexer.biblio.ITagHandler;
import com.atc.osee.genius.indexer.biblio.log.MessageCatalog;

/**
 * Inferred keyword and keyphrase extractor.
 * Note that this value handler relies on the corresponding digital plugin so 
 * if you don't have that you are not allowed to execute this procedure.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class InferredKeywordExtractor implements ITagHandler 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(InferredKeywordExtractor.class);
	
	@Override
	public Object getValue(
			final String argument, 
			final Record record,
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) 
	{
		try 
		{
			final String text = String.valueOf(document.getFieldValue(argument));
			if (text == null || text.trim().length() == 0)
			{
				LOGGER.warn(
						String.format(
								MessageCatalog._000006_TEXT_TOO_SHORT,
								record.getControlNumber()));
				return null;
			}
			
			Class<?> clazz = Class.forName("com.atc.osee.genius.indexer.digital.ki.InferredKeywordsValueHandler");
			Method method = clazz.getMethod(
					"getValue", 
					String.class, 
					ContentHandler.class, 
					Metadata.class, 
					File.class, 
					SolrParams.class,
					SolrInputDocument.class);
			
			Object digitalInferredKeywordsExtractor = clazz.newInstance();
			
			ContentHandler dummyHandler = new DefaultHandler2()
			{
				public String toString() 
				{
					return String.valueOf(text);
				};
			};
			
			return method.invoke(digitalInferredKeywordsExtractor, null, dummyHandler, null, null, settings, document);
			
		} catch (ClassNotFoundException exception) 
		{
			LOGGER.error(MessageCatalog._000007_KI_PLUGIN_NOT_FOUND);		
		} catch (SecurityException exception) 
		{
			LOGGER.error(MessageCatalog._000007_KI_PLUGIN_NOT_FOUND, exception);		
		} catch (NoSuchMethodException exception) 
		{
			LOGGER.error(MessageCatalog._000007_KI_PLUGIN_NOT_FOUND, exception);		
		} catch (IllegalArgumentException exception) 
		{
			LOGGER.error(MessageCatalog._000007_KI_PLUGIN_NOT_FOUND, exception);		
		} catch (IllegalAccessException exception) 
		{
			LOGGER.error(MessageCatalog._000007_KI_PLUGIN_NOT_FOUND, exception);		
		} catch (InvocationTargetException exception) 
		{
			LOGGER.error(MessageCatalog._000007_KI_PLUGIN_NOT_FOUND, exception);		
		} catch (InstantiationException exception) 
		{
			LOGGER.error(MessageCatalog._000007_KI_PLUGIN_NOT_FOUND, exception);		
		}
		return null;
	}
}