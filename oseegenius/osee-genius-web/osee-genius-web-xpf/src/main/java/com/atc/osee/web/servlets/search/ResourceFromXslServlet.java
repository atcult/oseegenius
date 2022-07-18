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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.velocity.tools.Toolbox;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.XsltUtility;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.servlets.RememberLastVisitedResourceServlet;
import com.atc.osee.web.servlets.writers.AbstractOutputWriter;

/**
 * Resource loader servlet.
 * Loads details for a requested resource.
 * 
 * @author aguercio
 * @since 1.2
 */
public class ResourceFromXslServlet extends RememberLastVisitedResourceServlet 
{
	private static final long serialVersionUID = 1L;
	
	
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "home");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "suggest");
		setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "search");
		
		String uri = request.getParameter(HttpParameter.URI);
		
		final String mode = request.getParameter("mode");
		
		if (isNotNullOrEmptyString(uri))
		{
			uri = uri.replaceFirst("^0+(?!$)", "");
			try
			{
				ISearchEngine searchEngine = getSearchEngine(request);
				QueryResponse resourceResponse = searchEngine.findDocumentByURI(uri);
				
				if (resourceResponse != null && !resourceResponse.getResults().isEmpty())
				{
					AbstractOutputWriter writer = writers.get(request.getAttribute(HttpParameter.WRITE_MODE));
					
					
					
					if (writer == null)
					{
						
						html(request, response, resourceResponse, uri, searchEngine, mode);
					} else 
					{
						writer.write(request, response, resourceResponse.getResults());
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
		else {
			String auth_group_uri = request.getParameter(HttpParameter.AUTHORITY_GROUP_URI);
			if(isNotNullOrEmptyString(auth_group_uri)) {
				try
				{
					ISearchEngine searchEngine = getSearchEngine(request);
					QueryResponse resourceResponse = searchEngine.findAuthDocumentByAuthLink(auth_group_uri);
					if (resourceResponse != null && !resourceResponse.getResults().isEmpty())
					{
						AbstractOutputWriter writer = writers.get(request.getAttribute(HttpParameter.WRITE_MODE));
						if (writer == null)
						{
							
							html(request, response, resourceResponse, auth_group_uri, searchEngine, mode);
						} else 
						{
							writer.write(request, response, resourceResponse.getResults());
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
	}
	
	/**
	 * Retrieves resource details and forward to the appropriate view component. 
	 * 
	 * @param request the http request.
	 * @param response the http response.
	 * @param resourceResponse the incoming response from the search engine.
	 * @param uri the uri of the requested resource.
	 * @param searchEngine the search engine reference.	 
	 * @throws IOException in case of I/O failures.
	 * @throws SystemInternalFailureException in case of system failure.
	 * @throws ServletException in case of general servlet container exception.
	 */
	private void html(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final QueryResponse resourceResponse, 
			final String uri, 
			final ISearchEngine searchEngine,
			final String mode) throws IOException, SystemInternalFailureException, ServletException 
	{
		SolrDocument resource = resourceResponse.getResults().get(0);
		
		setRequestAttribute(
				request, 
				IConstants.RESOURCE_KEY, 
				resource);
		
		if (mode == null) {
			//Bug 5190 caricamento delle info in base al xslt di IMSS
			Toolbox toolbox = (org.apache.velocity.tools.Toolbox) getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
			String instituition = (String) toolbox.get("institution");
			
		
				Visit visit = getVisit(request);
				Locale locale = getVisit(request).getPreferredLocale();	
				//I need to check request parameter, because getVisit Locale will be set later in OseeGenoisFrontController.java
				if(request.getParameter(HttpParameter.LANGUAGE) != null ) {
					locale = new Locale(request.getParameter("l"));
				}
				org.w3c.dom.Document recordDetail = XsltUtility.transformXSLT(request, locale, (String) resource.getFieldValue("marc_xml"), "stile");
				String recordDatailString = XsltUtility.printString(recordDetail);
				request.setAttribute("recordDetail", recordDatailString);		
			}
		
			
			forwardTo(request, response, "/recordXslt.vm", "no_header_layout.vm");
	}


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
				// Nothing
			}
		}
		return tabId;
	}

	
}
