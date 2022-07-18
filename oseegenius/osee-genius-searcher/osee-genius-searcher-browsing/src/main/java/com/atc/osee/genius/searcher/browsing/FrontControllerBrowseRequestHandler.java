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
package com.atc.osee.genius.searcher.browsing;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OseeGenius Front controller Browse request handler.
 * 
 * As the name suggests, this is simply a front controller / facade towards the
 * several underlying SOLR cores that are in charge to maintain browsing indexes.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class FrontControllerBrowseRequestHandler extends RequestHandlerBase
{		
	public final static Logger LOGGER = LoggerFactory.getLogger(FrontControllerBrowseRequestHandler.class);
	
	private static final String NOT_AVAILABLE = "N.A.";
	private static final String INDEX_NAME_PARAMETER_NAME = "i";	
	
	@Override
	public void handleRequestBody(final SolrQueryRequest request, final SolrQueryResponse response) throws Exception
	{
		final SolrParams requestParameter = request.getParams();
		final String indexName = requestParameter.get(INDEX_NAME_PARAMETER_NAME);
		
		if (indexName == null || indexName.trim().length() == 0)
		{
			throw new IllegalArgumentException("Cannot browse over a null index.");
		}
		
		LocalSolrQueryRequest localRequest = null;
		try 
		{
			final SolrCore targetCore = request.getCore().getCoreDescriptor().getCoreContainer().getCore(indexName);
			final SolrRequestHandler browser = targetCore.getRequestHandler("browse");
			
			localRequest = new LocalSolrQueryRequest(targetCore, request.getParams());
			
			targetCore.execute(browser, localRequest, response);
		} catch (Exception exception)
		{
			LOGGER.error("Unable to browse over " +indexName+ " index.", exception);
			throw exception;
		} finally 
		{
			localRequest.close();
		}
	}
	
	@Override
	public String getDescription()
	{
		return "OseeGenius -S- Front Controller Browse Request Handler";
	}
	
	@Override
	public String getSource()
	{
		return NOT_AVAILABLE;
	}

	@Override
	public String getVersion()
	{
		return "1.0";
	}
}