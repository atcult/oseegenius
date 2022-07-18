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
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * New Tab servlet.
 * Provides and handles logic associated with the creation of a 
 * new search tab.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class NewTabServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	protected void service(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		SearchTab newTab = getSearchExperience(request).addNewTab();
		if (getLicense(request).isFederatedSearchEnabled())
		{
			Set<String> enabledTargets = (Set<String>) request.getSession().getAttribute(HttpAttribute.TARGETS_ENABLED);
			if (enabledTargets != null && !enabledTargets.isEmpty())
			{
				try 
				{
					newTab.getPazpar2().disableAndOrEnableTargets(request);
				} catch (SystemInternalFailureException e) 
				{
					// Ignore
				}
			}
		}
		forwardTo(request, response, "/hits.vm");
	}
}