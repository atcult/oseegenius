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
package com.atc.osee.genius.indexer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.log.MessageCatalog;

/**
 * Osee Genius I core SOLR component.
 * Plugglable SOLR component that is in charge to manage the
 * automatic indexing process.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class IndexingHandler extends RequestHandlerBase implements SolrCoreAware
{
	private static final Logger LOGGER = LoggerFactory.getLogger(IndexingHandler.class.getName());
	
	private static final String COMMON = "common";
	private static final String IMPLEMENTOR = "implementor";
	
	private static final String TARGET_PARAMETER_NAME = "target";
	private static final String DUMP_PARAMETER_NAME = "dump";
	private static final String DUMMY_PARAMETER_NAME = "dummy";
	
	private static final String GLOBAL_FAILURE = "global-failure";
	
	private static final String SOURCE_ROOT_DIRECTORY = "source.directory";
	private static final String WORKED_DIRECTORY = "worked.directory";
	
	private static final String [] AVAILABLE_PLUGINS = {"skos", "digital", "biblio"};
	private SolrParams commonSettings;
	
	// agazzarini (16/08/2011): 
	// SolrParams accepts only flat structures but browsing
	// configuration is instead a hierarchy.
	private NamedList<Object> globalConfiguration;
	
	private Map<String, Class<? extends Indexer>> implementorRegistry = new HashMap<String, Class<? extends Indexer>>();
	private Map<String, SolrParams> settings = new HashMap<String, SolrParams>();
	
	private SolrCore core;

	@Override
	public void handleRequestBody(final SolrQueryRequest request, final SolrQueryResponse response) 
	{					
		SolrParams requestParameters = request.getParams();
		String indexingKind = requestParameters.get(TARGET_PARAMETER_NAME);
		boolean dump = requestParameters.getBool(DUMP_PARAMETER_NAME, false);
		boolean dummy = requestParameters.getBool(DUMMY_PARAMETER_NAME, false);

		if (indexingKind == null || indexingKind.trim().length() == 0)
		{
			error(MessageCatalog._000003_NULL_TARGET_PARAMETER_VALUE);
		    response.add(
		    		GLOBAL_FAILURE,
		    		MessageCatalog._000003_NULL_TARGET_PARAMETER_VALUE);
		    return;
		}

		info(MessageCatalog._000001_REQUEST_RECEIVED, indexingKind);
		
		Class<? extends Indexer> implementor = implementorRegistry.get(indexingKind);
		SolrParams configuration = settings.get(indexingKind);		
		if (implementor == null || configuration == null)
		{
		    response.add(
		    		GLOBAL_FAILURE,
		    		String.format(MessageCatalog._000002_BAD_REQUEST, indexingKind));
		    return;
		}
		
		try 
		{
			info(MessageCatalog._000004_INDEXER_START, indexingKind);
			
			Indexer indexer = implementor.newInstance();
			indexer.configure(request, globalConfiguration, commonSettings, configuration, core, dump, dummy, requestParameters);
			IndexerResult result = indexer.doIndex();

			info(MessageCatalog._000005_INDEXER_END, indexingKind);
			
			if (result != null)
			{
				response.add(result.getResultName(), result.getAttributes());
			} 
		} catch (InstantiationException exception) 
		{
			error(MessageCatalog._000006_INDEXER_INSTANTIATION_FAILURE, exception, indexingKind);
		    response.add(
		    		GLOBAL_FAILURE,
		    		String.format(MessageCatalog._000006_INDEXER_INSTANTIATION_FAILURE, indexingKind));
		} catch (IllegalAccessException exception) 
		{
			error(MessageCatalog._000006_INDEXER_INSTANTIATION_FAILURE, exception, indexingKind);
		    response.add(
		    		GLOBAL_FAILURE,
		    		String.format(MessageCatalog._000006_INDEXER_INSTANTIATION_FAILURE, indexingKind));
		} catch (WrongConfigurationException exception) 
		{
			error(MessageCatalog._000007_INDEXER_CONFIGURATION_FAILURE, exception, indexingKind);
		    response.add(
		    		GLOBAL_FAILURE,
		    		String.format(MessageCatalog._000007_INDEXER_CONFIGURATION_FAILURE, indexingKind));
		} catch (IndexingException exception) 
		{
			error(MessageCatalog._000015_INDEXER_FAILURE, exception, indexingKind);			
		    response.add(
		    		GLOBAL_FAILURE,
		    		String.format(MessageCatalog._000015_INDEXER_FAILURE, indexingKind));
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void init(final NamedList arguments)
	{		
		if (arguments != null)
		{
			this.globalConfiguration = arguments;
			commonSettings = SolrParams.toSolrParams((NamedList)arguments.get(COMMON));
			if (commonSettings == null)
			{
				error(MessageCatalog._000008_COMMON_SETTINGS_MISSING);
			    throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "");
			}
			
		    String sourceDirectoryPathName = commonSettings.get(SOURCE_ROOT_DIRECTORY);
		    if (sourceDirectoryPathName == null || !new File(sourceDirectoryPathName).canRead())
			{
		    	error(MessageCatalog._000009_INVALID_SOURCE_DIR);
			    throw new SolrException(
			    		SolrException.ErrorCode.SERVER_ERROR,
			    		MessageCatalog._000009_INVALID_SOURCE_DIR);
			}

		    String workedDirectoryPathName = commonSettings.get(WORKED_DIRECTORY);
		    if (workedDirectoryPathName == null ||  !new File(workedDirectoryPathName).canRead())
			{
		    	error(MessageCatalog._000011_INVALID_WORKED_DIR);
			    throw new SolrException(
			    		SolrException.ErrorCode.SERVER_ERROR,
			    		MessageCatalog._000011_INVALID_WORKED_DIR);
			}
		    
		    for (String pluginName : AVAILABLE_PLUGINS)
		    {
			    NamedList pluginSection = (NamedList)arguments.get(pluginName);
			    if (pluginSection != null)
			    {
					SolrParams pluginSettings = SolrParams.toSolrParams(pluginSection);
					if (pluginSettings != null)
					{
						implementorRegistry.put(pluginName, defineIndexer(pluginSettings.get(IMPLEMENTOR), pluginName));
						settings.put(pluginName, pluginSettings);
					}
			    }		    	
		    }
		}
	}
	
	@Override
	public void inform(final SolrCore core)
	{
		this.core = core;
	}

	/**
	 * Creates an indexer definition for a specific domain. 
	 * 
	 * @param className the fully qualified name of the indexer.
	 * @param domain the association domain. 
	 * @return the indexer definition.
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends Indexer> defineIndexer(final String className, final String domain)
	{
		try 
		{
			return (Class<? extends Indexer>) Class.forName(className);
		} catch (Exception exception) 
		{
			error(MessageCatalog._000010_DEFINE_INDEXER_FAILURE, domain);
		    throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, exception);
		}
	}
	
	@Override
	public String getDescription()
	{
		return "Osee Genius -I- core handler";
	}

	@Override
	public String getSource()
	{
		return "Source code not available.";
	}

	@Override
	public String getVersion()
	{
		return "1.0";
	}
	
	/**
	 * Logs out a message with INFO as severity level.
	 * 
	 * @param message the message.
	 * @param parameters the parameters that will be replaced in message placeholders.
	 */
	protected void info(final String message, final Object ... parameters)
	{
		LOGGER.info(String.format(message, parameters));
	}
	
	/**
	 * Logs out a message with ERROR as severity level.
	 * 
	 * @param message the message.
	 * @param parameters the parameters that will be replaced in message placeholders.
	 */
	protected void error(final String message, final Object ... parameters)
	{
		LOGGER.error(String.format(message, parameters));
	}

	/**
	 * Logs out a message with ERROR as severity level.
	 * 
	 * @param message the message.
	 * @param throwable the exception cause.
	 * @param parameters the parameters that will be replaced in message placeholders.
	 */
	protected void error(final String message, final Throwable throwable, final Object ... parameters)
	{
		LOGGER.error(String.format(message, parameters), throwable);
	}

	/**
	 * Logs out a message with DEBUG as severity level.
	 * 
	 * @param message the message.
	 * @param parameters the parameters that will be replaced in message placeholders.
	 */
	protected void debug(final String message, final Object ... parameters)
	{
		LOGGER.debug(String.format(message, parameters));
	}	
	
	/**
	 * Logs out a message with ERROR as severity level.
	 * 
	 * @param message the message.
	 */
	protected void error(final String message)
	{
		LOGGER.error(String.format(message));
	}
}