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
package com.atc.osee.web.servlets.browse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.BrowseTab;
import com.atc.osee.web.servlets.RememberLastVisitedResourceServlet;

/**
 * OseeGenius -W- alphabetical browser controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class BrowseServlet extends RememberLastVisitedResourceServlet 
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		setSessionAttribute(request, HttpAttribute.OG_CONTEXT, "browsing");		
		
		String indexName = request.getParameter(HttpParameter.INDEX_NAME);
		String from = request.getParameter(HttpParameter.FROM);
		String positionInResponse = request.getParameter(HttpParameter.POSITION_IN_RESPONSE);		
		int tabId = getTabId(request);
		
		BrowseTab currentTab = getBrowsingExperience(request).getTab(tabId);
		if (isNotNullOrEmptyString(indexName) || isNotNullOrEmptyString(from) && !currentTab.isNew())
		{
			ISearchEngine searchEngine = getSearchEngine(request);
			
			SolrQuery query = new SolrQuery();
			
			int indexOfDoubleUnderscore = indexName.indexOf("__");
			if (indexOfDoubleUnderscore != -1)
			{
				query.add(ISolrConstants.LOGICAL_VIEW, indexName.substring(0,indexOfDoubleUnderscore));				
				indexName = indexName.substring(indexOfDoubleUnderscore + 2);
			}
			
			query.add(ISolrConstants.BROWSING_INDEX_NAME, indexName);
			query.add(ISolrConstants.BROWSING_FROM, from);

			final String pageSize = getPageSize(request);
			query.add(HttpParameter.PAGE_SIZE, pageSize);
			query.add("sk",request.getParameter("sk"));
			
			final HttpSession session = request.getSession();
			final String logicalView = (String) session.getAttribute(HttpAttribute.LOGICAL_VIEW);
			
			final String defaultLogicalView = getConfiguration(request).getDefaultLogicalView();
			
			if (isNotNullOrEmptyString(logicalView) || isNotNullOrEmptyString(defaultLogicalView)) {
				query.add(ISolrConstants.LOGICAL_VIEW, isNotNullOrEmptyString(logicalView) ? logicalView : defaultLogicalView);
			}
			query.setRequestHandler("/browse");
			query.add(ISolrConstants.POSITION_IN_RESPONSE, positionInResponse);
			try
			{
				QueryResponse queryresponse = searchEngine.executeQuery(query);
								
				currentTab.setData(from, indexName, query, queryresponse, getVisit(request).getPreferredLocale());
				
				if (!currentTab.isEmpty() && isNullOrEmptyString(positionInResponse))
				{
					getVisit(request).getSearchHistory().addBrowseEntry(from, indexName, Integer.parseInt(pageSize), getRequestUrl(request));
				}
			} catch (SystemInternalFailureException exception)
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}
		
		currentTab.setQueryParameters(request.getQueryString());
		forwardTo(request, response, "/browsing.vm", "/browsing_layout.vm");
	}

	/**
	 * Returns the requested page size (or a default size in case the parameter is missing.) 
	 * 
	 * @param request the HTTP request.
	 * @return the page size that will be used for the current request.
	 */
	private String getPageSize(HttpServletRequest request) 
	{
		String pageSize = request.getParameter(HttpParameter.PAGE_SIZE);
		return (isNotNullOrEmptyString(pageSize)) 
				? pageSize 
				: String.valueOf(getConfiguration(request).getDefaultPageSize());
	}

	/**
	 * Returns the tab identifier associated with the incoming request.
	 * 
	 * @param request the http request.
	 * @return the tab identifier associated with the incoming request.
	 */
	protected int getTabId(final HttpServletRequest request)
	{
		String tabIdParameter = request.getParameter(HttpParameter.TAB_ID);
		int tabId = 0;
		
		// If needed because most of time this parameter is null so 
		// it's better to avoid try / catch block
		if (tabIdParameter != null)
		{
			try 
			{ 
				tabId = Integer.parseInt(tabIdParameter); 
			} catch (Exception exception) 
			{
				// Nothing to be done here...
			}
		}
		return tabId;
	}	
	
	private String getRequestUrl(HttpServletRequest request) 
	{
		StringBuffer result = request.getRequestURL();
		String queryString = request.getQueryString(); 
		if (queryString != null) 
		{
	        result.append("?").append(queryString);
	    }
	    return result.toString();
	}
}