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
import com.atc.osee.web.model.Account;

/**
 * Associates a new tag with a document.
 * Note that in order to properly work, this OseeGenius -W- instance
 * must have the community extension enabled.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class AddNewTagServlet extends OseeGeniusCommunityServlet
{
	private static final long serialVersionUID = 1L;
       
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String label = request.getParameter(HttpParameter.TAG);
		String uri = request.getParameter(HttpParameter.URI);
		
		if (isNotNullOrEmptyString(label) && isNotNullOrEmptyString(label))
		{
			try 
			{
				Account account = getVisit(request).getAccount();
				getLicense(request).getCommunityPlugin().addNewTag(account.getId(), uri, label);
			} catch (SystemInternalFailureException exception)
			{
				// Avoid duplicate log statement: that should be already logged on plugin side.
			}
		}
	}
}