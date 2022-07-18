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

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.model.SearchExperience;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * A web controller that redirects the user to the search page.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ReturnToSearchServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		SearchTab tab = getSearchExperience(request).getCurrentTab();
		response.sendRedirect("thsearch?" + tab.getQueryParameters());
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