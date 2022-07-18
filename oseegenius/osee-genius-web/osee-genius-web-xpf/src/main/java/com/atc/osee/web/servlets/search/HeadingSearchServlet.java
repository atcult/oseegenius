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
package com.atc.osee.web.servlets.search;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.folio.BncfElemtResearchHistory;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Advanced Search Web Controller.
 * Used for handling double step heading search
 * Activated by web.xml as standard
 * 
 * Used by: BNCF
 * @author A.guercio
 * @since 1.0
 */
public class HeadingSearchServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 71345474971254L;
	private static final Logger LOGGER = LoggerFactory.getLogger(HeadingSearchServlet.class);
	

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		request.setCharacterEncoding("UTF-8");
		request.setAttribute("specificServletTitle", "subject_search"); 
		String query = request.getParameter(HttpParameter.QUERY);
		if (query != null && query.trim().length() != 0) {
			int pageIndex = getPageIndex(request.getParameter(HttpParameter.PAGE_INDEX));	
			int pageSize = getPageSize(request.getParameter(HttpParameter.PAGE_SIZE));
			ISearchEngine searchEngine = getSearchEngine(request);
			SolrQuery solrquery = new SolrQuery("*:*");
			
			solrquery.setQuery("search:" + "\"" + query + "\"");
			
			solrquery.setStart(pageIndex * pageSize);
			solrquery.setRows(pageSize);
			solrquery.setFields("*");
			
			String params = "q=" + java.net.URLEncoder.encode(query, "UTF-8");
						
			try {				
				QueryResponse solrresponse = searchEngine.executeQuery(solrquery);	
				request.setAttribute("result", solrresponse.getResults());
				request.setAttribute("query", query);
				request.setAttribute("parameters", params);
				
				
				
				String section="browsing";
				ConfigurationTool configuration = getConfiguration(request);
				if(configuration.searchToSave()) {
					if (getVisit(request).getFolioAccount() != null && isNotNullOrEmptyString(getVisit(request).getFolioAccount().getId())) {
						if (request.getParameter("fromHistory")==null) {
							String url= getRequestUrl(request)+ "&fromHistory=true";
							BncfElemtResearchHistory history= new BncfElemtResearchHistory(
									-1,
									getVisit(request).getFolioAccount().getId(),
									query, 
									null,
									solrresponse.getResults().getNumFound(), 
									null,
									url, 
									solrquery.getFilterQueries(),
									null,
									section);
							history.save();
						}
					}
				}
				
				
				
				forwardTo(request, response, "/heading_search.vm", "/homepage.vm");
				
			} catch (SystemInternalFailureException exception) 	{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
			}
		}
		else {
			forwardTo(request, response, "/components/page/heading_search_home.vm", "/homepage.vm");
		}
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
	
	
	
	
	
	
	
	public String getSearchServletName()
	{
		return "search";
	}
	
	/**
	 * Returns the size (in terms of displayed hits) of each result page.
	 * 
	 * @param pageSizeAsString the incoming page size http request parameter.
	 * @return the size (in terms of displayed hits) of each result page.
	 */
	protected int getPageSize(final String pageSizeAsString)
	{
		try 
		{
			return pageSizeAsString != null ? Integer.parseInt(pageSizeAsString) : 10;
		} catch (Exception exception)
		{
			return 10;
		}
	}
	
	/**
	 * Returns the page index.
	 * 
	 * @param pageNumberAsString the incoming http request parameter.
	 * @return the page index that will be shown.
	 */
	protected int getPageIndex(final String pageNumberAsString)
	{
		try 
		{
			int result = pageNumberAsString != null ? Integer.parseInt(pageNumberAsString) - 1 : 0;
			return result > 0 ? result : 0;
		} catch (Exception exception)
		{
			return 0;
		}
	}
	
	/**
	 * get heading search engine
	 */
	
	protected ISearchEngine getSearchEngine(final HttpServletRequest request)
	{
		return (ISearchEngine) getServletContext().getAttribute(IConstants.SEARCH_ENGINE_HEADING_ATTRIBUTE_NAME);
	}
	
	
}