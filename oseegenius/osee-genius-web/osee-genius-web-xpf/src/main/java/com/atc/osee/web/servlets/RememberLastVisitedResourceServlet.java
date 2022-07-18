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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.IConstants;

/**
 * An (abstract) servlet that remembers last visited page.
 * This is useful when in a web page there are actions that don't require 
 * a forward to another page / resource. 
 * It's a lightweight way to emulate the "actionListener" behaviour of JSF.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class RememberLastVisitedResourceServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = -8040140106585343040L;

	@Override
	public final void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		String queryString = request.getQueryString();
		
		String language = request.getParameter("l");
		if (queryString != null && language != null)
		{
			queryString = queryString.replaceFirst("&l=" + language, "").replaceFirst("l=" + language, "");
		}
		
		String currentRequest = new StringBuilder(request.getRequestURI()).append('?').append(queryString).toString();			
		request.setAttribute(IConstants.CURRENT_REQUEST_KEY, currentRequest);
		doGet(request, response);
	}
}
