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
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.Layout;
import com.atc.osee.web.Page;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * OseeGenius -W- resource viewer web controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class MetaResourceServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String uri = request.getParameter(HttpAttribute.RECID);
		if (isNotNullOrEmptyString(uri))
		{
			PazPar2 pazpar2 = getSearchExperience(request).getCurrentTab().getPazpar2();
			try
			{
				String record = pazpar2.record(URLEncoder.encode(uri, "UTF-8"), null);
				setRequestAttribute(request, HttpAttribute.RESOURCE, record);
				
				forwardTo(
						request, 
						response, 
						Page.FEDERATED_SEARCH_RESOURCE_PAGE,
						Layout.ONE_COLUMN_FEDERATED_SEARCH);
			} catch (SystemInternalFailureException exception)
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (NoSuchResourceException exception) 
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}
	}
	
	@Override
	protected long getLastModified(HttpServletRequest req) 
	{
		return System.currentTimeMillis();
	}
}