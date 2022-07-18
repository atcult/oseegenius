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

import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * Close Search Tab servlet.
 * Closes the current (search) tab, moving focus to the previous one.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class CloseTabServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		getSearchExperience(request).closeTab();
		SearchTab tab = getSearchExperience(request).getCurrentTab();
		if (tab.isExternalSearchTab())
		{
			response.sendRedirect("metasearch?" + tab.getQueryParameters());
			return;
		} else
		{
			response.sendRedirect("search?" + tab.getQueryParameters());
			return;			
		}
	}
}