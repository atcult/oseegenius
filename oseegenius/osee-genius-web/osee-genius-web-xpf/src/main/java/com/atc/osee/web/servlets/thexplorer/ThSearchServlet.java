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
import com.atc.osee.web.IConstants;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.servlets.search.SearchServlet;

/**
 * ThGenius search web controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ThSearchServlet extends SearchServlet 
{
	private static final long serialVersionUID = -760895485203089280L;

	@Override
	protected void goToSearchResultsPage(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "thexplorer");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "thsuggest");
		forwardTo(request, response, "/hits_thexp.vm", "three_columns_thexp.vm");
	}
	
	@Override
	public ISearchEngine getSearchEngine(final HttpServletRequest request)
	{
		return (ISearchEngine) request.getSession().getServletContext().getAttribute(IConstants.TH_SEARCH_ENGINE_ATTRIBUTE_NAME);
	}
	
	@Override
	protected SearchExperience getSearchExperience(final HttpServletRequest request)
	{
		return getVisit(request).getThSearchExperience();
	}
}