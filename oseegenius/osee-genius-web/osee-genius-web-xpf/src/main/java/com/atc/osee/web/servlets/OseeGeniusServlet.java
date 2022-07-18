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
package com.atc.osee.web.servlets;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.view.ServletUtils;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.folio.FolioRestApi;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.BrowsingExperience;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.model.community.WorkspaceSelection;
import com.atc.osee.web.servlets.writers.AbstractOutputWriter;
import com.atc.osee.web.servlets.writers.HtmlOutputWriter;
import com.atc.osee.web.servlets.writers.Marc21OutputWriter;
import com.atc.osee.web.servlets.writers.MarcXmlOutputWriter;
import com.atc.osee.web.servlets.writers.PDFOutputWriter;
import com.atc.osee.web.tools.BreadcrumbHandlingTool;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.LicenseTool;

/**
 * Supertype layer for all OseeGenius -W- web controllers.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class OseeGeniusServlet extends HttpServlet 
{
	private static final long serialVersionUID = -7017478656625819154L;
	public FolioRestApi folioAPI = new FolioRestApi();
	protected static Map<String, AbstractOutputWriter> writers = new HashMap<String, AbstractOutputWriter>();
	static 
	{
		writers.put("m21", new Marc21OutputWriter());
		writers.put("mxml", new MarcXmlOutputWriter());
		writers.put("pdf", new PDFOutputWriter());
		writers.put("html", new HtmlOutputWriter());
	}
		
	/**
	 * Forwards to target resource.
	 * 
	 * @param request the http request.
	 * @param response the http response.
	 * @param targetResource the target resource.
	 * @throws ServletException in case of servlet failure.
	 * @throws IOException in case of I/O failure.
	 */
	protected void forwardTo(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final String targetResource) throws ServletException, IOException
	{
		setRequestAttribute(request, "servletName", getServletName());
		response.setCharacterEncoding(IConstants.UTF_8);
		RequestDispatcher dispatcher = request.getRequestDispatcher(targetResource);
		dispatcher.forward(request, response);
	}

	/**
	 * Forwards to target resource.
	 * 
	 * @param request the http request.
	 * @param response the http response.
	 * @param targetResource the target resource.
	 * @param layout the name of the layout that should be applied.
	 * @throws ServletException in case of servlet failure.
	 * @throws IOException in case of I/O failure.
	 */
	protected void forwardTo(
			final HttpServletRequest request, 
			final HttpServletResponse response, final String targetResource, 
			final String layout) throws ServletException, IOException
	{
		setRequestAttribute(request, "servletName", getServletName());
		response.setCharacterEncoding(IConstants.UTF_8);
		response.setContentType("text/html; charset=" + IConstants.UTF_8);
		request.setAttribute(IConstants.LAYOUT_KEY, layout);
		RequestDispatcher dispatcher = request.getRequestDispatcher(targetResource);
		dispatcher.forward(request, response);
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
		return (ISearchEngine) getServletContext().getAttribute(IConstants.SEARCH_ENGINE_ATTRIBUTE_NAME);
	}
	
	protected BreadcrumbHandlingTool getI18nTool(final HttpServletRequest request)
	{
		return (BreadcrumbHandlingTool) ServletUtils.findTool(
				"breadcrumbTool", 
				getServletContext());	
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
				getServletContext());	
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
				getServletContext());	
	}
	
	/**
	 * Sets the global error flag onto the current HTTP request.
	 * 
	 * @param request the HTTP request.
	 */
	protected void setErrorFlag(final HttpServletRequest request)
	{
		setRequestAttribute(request, "error", true);
	}
	
	/**
	 * Returns the workspace selection container.
	 * 
	 * @param request the HTTP request.
	 * @return the workspace selection container. 
	 */
	protected WorkspaceSelection getWorkspaceSelection(final HttpServletRequest request)
	{
		WorkspaceSelection selection = (WorkspaceSelection) request.getSession(true).getAttribute(HttpAttribute.WORKSPACE_SELECTION);
		if (selection == null)
		{
			selection = new WorkspaceSelection();
			request.getSession().setAttribute(HttpAttribute.WORKSPACE_SELECTION, selection);
		}
		return selection;
	}	
	
	protected boolean isDataserviceAvailable(final HttpServletRequest request)
	{
		Toolbox toolbox = (org.apache.velocity.tools.Toolbox) getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
		if (toolbox != null)
		{
			Integer startUnavailability = (Integer) toolbox.get("unavailavility-start");
			Integer endUnavailability = (Integer) toolbox.get("unavailavility-end");
			
			if (startUnavailability != null && endUnavailability != null)
			{
				int nowhh = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				return nowhh < startUnavailability || nowhh >= endUnavailability;
			}
		}		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Account> getAuthTokens(final HttpServletRequest request) {
		Map<String, Account> tokens = (Map<String, Account>) getServletContext().getAttribute("auth-tokens");
		if (tokens == null) {
			tokens = new HashMap<String, Account>();
			getServletContext().setAttribute("auth-tokens", tokens);
		}
		
		return tokens;
	}
}