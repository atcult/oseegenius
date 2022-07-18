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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.servlets.RememberLastVisitedResourceServlet;
import com.atc.osee.web.servlets.writers.AbstractOutputWriter;

/**
 * ThGenius Resource loader servlet.
 * Loads details for a requested resource.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class ThResourceServlet extends RememberLastVisitedResourceServlet 
{
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "thexplorer");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "thsuggest");
		setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "thsearch");
		String uri = request.getParameter(HttpParameter.URI);
		
		if (isNotNullOrEmptyString(uri))
		{
			try
			{
				ISearchEngine searchEngine = getSearchEngine(request);
				QueryResponse resourceResponse = searchEngine.findDocumentByURI(uri);
				
				if (resourceResponse != null && !resourceResponse.getResults().isEmpty())
				{
					AbstractOutputWriter writer = writers.get(request.getAttribute(HttpParameter.WRITE_MODE));
					if (writer == null)
					{
						html(request, response, resourceResponse, uri, searchEngine);
					} 
				} else 
				{
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			} catch (SystemInternalFailureException exception)
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
			}
		}
	}
	
	/**
	 * Injects the ThResource onto the view scope and renders the HTML page.
	 * 
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param resourceResponse the SOLR resource data.
	 * @param uri the target resource URI.
	 * @param searchEngine the OseeGenius -S- reference.
	 * @throws IOException in case of I/O failure.
	 * @throws SystemInternalFailureException in case of system failure.
	 * @throws ServletException in case of servlet container failure.
	 */
	private void html(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final QueryResponse resourceResponse, 
			final String uri, 
			final ISearchEngine searchEngine) throws IOException, SystemInternalFailureException, ServletException 
	{
		setRequestAttribute(
				request, 
				IConstants.RESOURCE_KEY, 
				resourceResponse.getResults().get(0));		
		forwardTo(request, response, "/hit_thexp.vm", "skos_detail_thexp.vm");				
	}
	
	@Override
	protected ISearchEngine getSearchEngine(final HttpServletRequest request)
	{
		return (ISearchEngine) request.getSession().getServletContext().getAttribute(IConstants.TH_SEARCH_ENGINE_ATTRIBUTE_NAME);
	}
	
	@Override
	protected SearchExperience getSearchExperience(final HttpServletRequest request)
	{
		return getVisit(request).getThSearchExperience();
	}		
}