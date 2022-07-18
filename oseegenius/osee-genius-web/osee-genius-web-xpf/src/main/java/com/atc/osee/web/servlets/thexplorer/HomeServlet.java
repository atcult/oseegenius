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
package com.atc.osee.web.servlets.thexplorer;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * OseeGenius THExplorer  home web controller.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class HomeServlet extends OseeGeniusServlet 
{
	private static final Random RANDOMIZER = new Random();
	private static final long serialVersionUID = 4432580256728693619L;

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "thexplorer");
		setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "thsearch");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "thsuggest");

		SolrQuery query = new SolrQuery("*:*");
		query.setQueryType("plain");
		query.setRows(20);
		query.setStart(0); 
		try 
		{
			QueryResponse queryResponse = getSearchEngine(request).executeQuery(query);
			setRequestAttribute(request, "cloud", buildCloudData(queryResponse.getResults()));
		} catch (Exception exception)
		{
			// Nothing to be done here
		}

		
		forwardTo(request, response, "/index_thexp.vm", "homepage_thexp.vm");
	}
	
	/**
	 * Builds the homepage cloud.
	 * 
	 * @param documents the SOLR document list.
	 * @return the could in JSON format.
	 */
	private String buildCloudData(final SolrDocumentList documents)
	{
		StringBuilder builder = new StringBuilder("[");
		for (SolrDocument document : documents)
		{
			builder
				.append("{text:\"")
				.append(((String) document.get(ISolrConstants.PREFERRED_LABEL)).replace("\"", ""))
				.append("\",weight:")
				.append((RANDOMIZER.nextInt(20) + 1))
				.append(", url: \"").append("javascript:vtr('").append(document.get(ISolrConstants.ID_FIELD_NAME)).append("')").append("\"}");
			builder.append(",");
		}
		
		builder.deleteCharAt(builder.length() - 1);
		builder.append("]");
		return builder.toString();
	}
	
	@Override
	protected ISearchEngine getSearchEngine(final HttpServletRequest request)
	{
		return (ISearchEngine) request.getSession().getServletContext().getAttribute(IConstants.TH_SEARCH_ENGINE_ATTRIBUTE_NAME);
	}	
}