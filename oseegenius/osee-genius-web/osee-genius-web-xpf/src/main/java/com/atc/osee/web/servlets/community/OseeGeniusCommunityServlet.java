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

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.LicenseTool;

/**
 * Supertype layer for all OseeGenius -W- community servlets.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class OseeGeniusCommunityServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 7293986421238038329L;

	/**
	 * Decorates all community servlets by doing a preliminary plugin activation check.
	 * Those components shouldn't work if the corresponding community 
	 * plugin has not been enabled.
	 * 
	 * @param request the http request.
	 * @param response the http response.
	 * @throws ServletException in case of component failure.
	 * @throws IOException in case of I/O failures.
	 */
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		LicenseTool license = getLicense(request);

		// Sanity check : this shouldn't work if community plugin has not been enabled.
		if (license.isCommunityPluginEnabled())
		{
			super.service(request, response);
		} else 
		{
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}