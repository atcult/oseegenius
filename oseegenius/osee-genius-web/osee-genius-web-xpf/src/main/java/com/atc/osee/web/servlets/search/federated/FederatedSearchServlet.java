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
package com.atc.osee.web.servlets.search.federated;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.LicenseTool;

import com.atc.osee.web.folio.BncfElemtResearchHistory;
/**
 * OseeGenius -W- MetaSearch web controller.
 * Be aware that It is in charge to control the view flow of a meta-search, not  the meta-search itself.
 * That is, this is handling only the "search" phase, while for subsequent phases there's another specific
 * web controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class FederatedSearchServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		LicenseTool license = getLicense(request);
		if (license.isFederatedSearchEnabled())
		{
			setSessionAttribute(request, HttpAttribute.OG_CONTEXT, "federated");		
			setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "home");
			setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "metasearch");
			
			Visit visit = getVisit(request);
			SearchTab currentTab = null;
			
			String tab = request.getParameter(HttpParameter.TAB_ID);
			if (isNotNullOrEmptyString(tab))
			{
				currentTab =  visit.getSearchExperience().getTab(Integer.parseInt(tab));
			} else 
			{
				currentTab = visit.getSearchExperience().getCurrentTab();
				if (!currentTab.isEmpty())
				{
					List<SearchTab> tabs = visit.getSearchExperience().getTabs();
					boolean found = false;
					for (SearchTab searchTab : tabs)
					{
						if (searchTab != currentTab && searchTab.getPazpar2() == currentTab.getPazpar2())
						{
							currentTab = searchTab;
							found = true;
							break;
						}
					}
					if (!found)
					{
						currentTab = visit.getSearchExperience().addNewTab(currentTab.getPazpar2());
					}
				}
			}
			
			String query = request.getParameter(HttpParameter.QUERY);
			String queryString = request.getQueryString();
			currentTab.setForeignData(query, visit.getPreferredLocale());
			String queryParameters = queryString 
					+ (queryString.indexOf("&" + HttpParameter.TAB_ID + "=") == -1 
					? "&" + HttpParameter.TAB_ID + "=" + currentTab.getId() 
					: "");
			
			currentTab.setQueryParameters(queryParameters);
			
			PazPar2 pazpar2 = currentTab.getPazpar2();
			try
			{
				pazpar2.search(request);
				
				visit.getSearchHistory().addFederatedSearchEntry(
						query, 
						request.getParameter(HttpParameter.QUERY_TYPE), 
						-1, 
						getSortCriteria(request), 
						getRequestUrl(request), 
						request.getParameterValues(HttpParameter.FILTER_BY));
				
				ConfigurationTool configuration = getConfiguration(request);
				if(configuration.searchToSave()) {
					if (getVisit(request).getFolioAccount() != null && isNotNullOrEmptyString(getVisit(request).getFolioAccount().getId())) {
						if (request.getParameter("fromHistory")==null) {
							String url= getRequestUrl(request)+ "&fromHistory=true";
							BncfElemtResearchHistory history= new BncfElemtResearchHistory(
									-1,
									getVisit(request).getFolioAccount().getId(),
									query, 
									request.getParameter(HttpParameter.QUERY_TYPE),
									-1, 
									getSortCriteria(request),
									url, 
									request.getParameterValues(HttpParameter.FILTER_BY),
									null,
									"federated");
							history.save();
						}
					}
				}

				
				forwardTo(request, response, "/hits_ext.vm", "three_columns_ext.vm");
			} catch (SystemInternalFailureException exception)
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else 
		{
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
	
	/**
	 * Returns the sort criteria inlcuded in the current (search) request.
	 * If not, the returned value will be the default sort criteria found in configuration.
	 * 
	 * @param request the HTTP request.
	 * @return the sort criteria inlcuded in the current (search) request.
	 */
	private String getSortCriteria(HttpServletRequest request)
	{
		String criteria = request.getParameter(HttpParameter.SORT_IN_ADVANCED_SEARCH);
		if (isNotNullOrEmptyString(criteria))
		{
			return criteria;
		} else 
		{
			ConfigurationTool configuration = getConfiguration(request);
			return configuration.getDefaultOrderByCriteria();
		}
	}	
}