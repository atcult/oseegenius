package com.atc.osee.web.filters;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.velocity.tools.view.ServletUtils;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.model.BrowsingExperience;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.model.community.ItemCommunityData;
import com.atc.osee.web.plugin.CommunityPlugin;
import com.atc.osee.web.tools.BreadcrumbHandlingTool;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.LicenseTool;

public class CommunityFilter implements Filter 
{
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException 
	{
		// Nothing
	}

	@Override
	public void doFilter(
			final ServletRequest request, 
			final ServletResponse response, 
			final FilterChain chain) throws IOException, ServletException 
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		Visit visit = getVisit(httpRequest);
		
		try 
		{
			if (visit.isAuthenticated() && getLicense(httpRequest).isCommunityPluginEnabled())
			{
				boolean loadOnlyUserCommunityData = getConfiguration(httpRequest).isCommunityDataPrivate();
				CommunityPlugin plugin = getLicense(httpRequest).getCommunityPlugin();
				
				SearchTab tab = getSearchExperience(httpRequest).getCurrentTab();
				
				QueryResponse qresponse = tab.getResponse();
				if (qresponse != null && !qresponse.getResults().isEmpty())
				{
					SolrDocumentList page = qresponse.getResults();
					String [] pageIds = new String [page.size()];
					
					int index = 0;
					for (SolrDocument document : qresponse.getResults())
					{
						pageIds [index] =  (String) document.getFieldValue(ISolrConstants.ID_FIELD_NAME);
						index++;
					}
					
					Map<String, ItemCommunityData> pageCommunityData = plugin.getMultipleItemsCommunityData(
							visit.getAccount().getId(), 
							pageIds, 
							loadOnlyUserCommunityData);
					
					httpRequest.setAttribute("community", pageCommunityData);
				}
			}
		} catch (Exception exception)
		{
			// Ignore
		} finally 
		{
			chain.doFilter(request, response);			
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns the visit associated with the current user session.
	 * 
	 * @param request the http request.
	 * @return the visit associated with the current user session.
	 */
	protected Visit getVisit(final HttpServletRequest request)
	{
		return (Visit) request.getSession().getAttribute(HttpAttribute.VISIT);
	}
	
	/**
	 * Returns the search experience associated with the current user session.
	 * 
	 * @param request the http request.
	 * @return the search experience associated with the current user session.
	 */
	protected SearchExperience getSearchExperience(final HttpServletRequest request)
	{
		return getVisit(request).getSearchExperience();
	}	
	
	/**
	 * Returns the search experience associated with the current user session.
	 * 
	 * @param request the http request.
	 * @return the search experience associated with the current user session.
	 */
	protected BrowsingExperience getBrowsingExperience(final HttpServletRequest request)
	{
		return getVisit(request).getBrowsingExperience();
	}		
	
	/**
	 * Returns the search experience associated with the current user session.
	 * 
	 * @param request the http request.
	 * @return the search experience associated with the current user session.
	 */
	protected SearchExperience getThSearchExperience(final HttpServletRequest request)
	{
		return getVisit(request).getThSearchExperience();
	}	
	
	/**
	 * Returns the search engine instance associated with the current user session.
	 * 
	 * @param request the http request.
	 * @return the search engine instance associated with the current user session.
	 */
	protected ISearchEngine getSearchEngine(final HttpServletRequest request)
	{
		return (ISearchEngine) request.getSession().getServletContext().getAttribute(IConstants.SEARCH_ENGINE_ATTRIBUTE_NAME);
	}
	
	protected BreadcrumbHandlingTool getI18nTool(final HttpServletRequest request)
	{
		return (BreadcrumbHandlingTool) ServletUtils.findTool(
				"breadcrumbTool", 
				request.getSession(true).getServletContext());	
	}
	
	/**
	 * Sets / replaces an attribute within the SESSION scope.
	 * 
	 * @param request the http request.
	 * @param name the attribute name.
	 * @param value the attribute value.
	 */
	protected void setSessionAttribute(final HttpServletRequest request, final String name, final Object value)
	{
		request.getSession(true).setAttribute(name, value);
	}	
	
	/**
	 * Sets / replaces an attribute within the REQUEST scope.
	 * 
	 * @param request the http request.
	 * @param name the attribute name.
	 * @param value the attribute value.
	 */
	protected void setRequestAttribute(final HttpServletRequest request, final String name, final Object value)
	{
		request.setAttribute(name, value);
	}
	
	/**
	 * Returns true if the given string is not null and is not an empty string.
	 * 
	 * @param aValue the value to be checked.
	 * @return true if the given string is not null and is not an empty string.
	 */
	protected boolean isNotNullOrEmptyString(final String aValue)
	{
		return aValue != null && aValue.trim().length() != 0;
	}
	
	/**
	 * Returns true if the given string is null or is an empty string.
	 * 
	 * @param aValue the value to be checked.
	 * @return true if the given string is null or is an empty string.
	 */
	protected boolean isNullOrEmptyString(final String aValue)
	{
		return aValue == null || aValue.trim().length() == 0;
	}
	
	/**
	 * Returns the license manager associated with this Osee Genius -W- instance.
	 * 
	 * @param request the http request.
	 * @return the license manager associated with this Osee Genius -W- instance.
	 */
	protected LicenseTool getLicense(final HttpServletRequest request)
	{
		return (LicenseTool) ServletUtils.findTool(
				IConstants.LICENSE_KEY, 
				request.getSession(true).getServletContext());	
	}
	
	/**
	 * Returns the configuration associated with this Osee Genius -W- instance.
	 * 
	 * @param request the http request.
	 * @return the configuration associated with this Osee Genius -W- instance.
	 */
	protected ConfigurationTool getConfiguration(final HttpServletRequest request)
	{
		return (ConfigurationTool) ServletUtils.findTool(
				IConstants.CONFIGURATION_KEY, 
				request.getSession(true).getServletContext());	
	}	
}
