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
package com.atc.osee.web.servlets.community;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Visit;

/**
 * OseeGenius -W- "Add a new review" web controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class RemoveFromTagServlet extends OseeGeniusCommunityServlet
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Handles the review insertion.
	 * 
	 * @param request the http request.
	 * @param response the http response.
	 * @throws ServletException in case of component failure.
	 * @throws IOException in case of I/O failures.
	 */
	@Override
	protected void doPost(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		Visit visit = getVisit(request);
		if (visit.isAuthenticated() && getLicense(request).isCommunityPluginEnabled())
		{
			String uri = request.getParameter(HttpParameter.URI);
			String id = request.getParameter(HttpParameter.TAG_ID);
			
			if (isNotNullOrEmptyString(uri) && isNotNullOrEmptyString(id))
			{
				try 
				{
					getLicense(request).getCommunityPlugin().removeFromTag(visit.getAccount().getId(), Long.parseLong(id),uri);
				} catch (SystemInternalFailureException exception) 
				{
				}
			} 
		}
	}
}