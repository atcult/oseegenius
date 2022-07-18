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
import java.util.Locale;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.velocity.tools.Toolbox;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.SearchTab;

import net.sf.cglib.core.Local;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * Servlet che consente di recuperare il numero totale di faccette. 
 * 
 * @author Maura Braddi
 * @since 1.0
 */
public class ShowAllFacetsServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = -977345971109555502L;
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String facetName = request.getParameter(HttpParameter.FILTER_BY);
		String orderBy = request.getParameter(HttpParameter.ORDER_BY);
		
		/****IMSS****/
		
		Toolbox toolbox = (org.apache.velocity.tools.Toolbox) getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
		String instituition = (String) toolbox.get("institution");
		if("IMSS".equals(instituition)){
			Locale preferredLocale = getVisit(request).getPreferredLocale();
			if(request.getParameter(HttpParameter.LANGUAGE) != null ) {
				preferredLocale = new Locale(request.getParameter("l"));
			}
			if(facetName != null && facetName.length() > 3) {				
				if("ita".equals(facetName.substring(facetName.length()-3, facetName.length())) && "en".equals(preferredLocale.toString())) {
					facetName = facetName.substring(0, facetName.length()-3) + "eng";
				}
				if("eng".equals(facetName.substring(facetName.length()-3, facetName.length())) && "it".equals(preferredLocale.toString())) {
					facetName = facetName.substring(0, facetName.length()-3) + "ita";
				}
			}
		}
		/**************/
		
		String offsetParamAsString = request.getParameter("offset");
		int offset = 0;
		
		try {
			if (offsetParamAsString != null)
			{
				offset = Integer.parseInt(offsetParamAsString);
			}
		} catch (Exception e) {
			// Ignore
		}
		
		String action = request.getParameter(HttpParameter.ACTION);
		if ("remove".equals(action))
		{
			String filterName = request.getParameter(HttpParameter.FILTER_BY);
			String filterValue = request.getParameter(HttpParameter.FILTER_BY_VALUE);
			
			if (isNotNullOrEmptyString(filterName) && isNotNullOrEmptyString(filterValue))
			{
				String filterQuery = new StringBuilder(filterName).append(":\"").append(filterValue).append("\"").toString();
				SearchTab  tab = getSearchExperience(request).getCurrentTab();
				SolrQuery query = tab.getQuery();
				query.removeFilterQuery(filterQuery);
				String queryParameters = tab.getQueryParameters();
				String stringToReplace="&f="+java.net.URLEncoder.encode(filterQuery, "UTF-8");
				tab.setQueryParameters(queryParameters.replaceFirst(Pattern.quote(stringToReplace), ""));
				//tab.setQueryParameters(queryParameters.replaceFirst("&f="+filterQuery, "").replaceFirst(filterQuery, ""));
				response.sendRedirect("search?"+tab.getQueryParameters());
			}
		} else
		{
			SolrQuery query = getSearchExperience(request).getCurrentTab().getQuery();
			if (query != null)
			{
				SolrQuery queryCopy = query.getCopy();
				if (isNotNullOrEmptyString(orderBy))
				{
					queryCopy.set("f."+facetName+".facet.sort", orderBy);
				}
		
				queryCopy.set("f."+facetName+".facet.limit", IConstants.FACET_PER_PAGE + 1);
				queryCopy.set("f."+facetName+".facet.offset", offset);			
				queryCopy.setRows(0);
				
				ISearchEngine searchEngine = getSearchEngine(request);
				try 
				{
					QueryResponse queryResponse = searchEngine.executeQuery(queryCopy);
					
					FacetField ff = queryResponse.getFacetField(facetName);
					if (ff.getValueCount() > IConstants.FACET_PER_PAGE) {
						setRequestAttribute(request, "next", true);					
						setRequestAttribute(request, "nextOffset", offset + IConstants.FACET_PER_PAGE);											
					}
					
					if (offset > 0)
					{
						setRequestAttribute(request, "back", true);										
						setRequestAttribute(request, "backOffset", offset - IConstants.FACET_PER_PAGE);											
					}

					request.setAttribute("unlimitedFacetQueryResponse", queryResponse);
					request.setAttribute(HttpAttribute.FACETFIELD, queryResponse.getFacetField(facetName));
					
					String messageBundleName = getI18nTool(request).getResourceBundleName(request, facetName);
					
					if (messageBundleName != null)
					{
						Object messageBundle = null;
						if (messageBundleName.startsWith("*"))
						{
							messageBundle = request.getSession(true).getServletContext().getAttribute(messageBundleName.substring(1));
						} else
						{
							messageBundle = getI18nTool(request).getResourceBundle(request, facetName);
						}	
						request.setAttribute(HttpAttribute.BUNDLE, messageBundle);
					}
					request.setAttribute(HttpAttribute.FILTERFACET, facetName);
					
					forwardTo(request, response, "/allFacets.vm", "one_column.vm");
					
				} catch (SystemInternalFailureException exception) 
				{
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);		
				}
			} else 
			{
				forwardTo(request, response, "/allFacets.vm", "one_column.vm");
			}
		} 
	}
}