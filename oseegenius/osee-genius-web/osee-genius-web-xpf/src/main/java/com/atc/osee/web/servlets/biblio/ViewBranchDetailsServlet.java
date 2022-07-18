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
package com.atc.osee.web.servlets.biblio;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Branch;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.LicenseTool;

/**
 * Web controlled for viewing branches detail.
 * 
 * @author agazzarini
 * @since 1.0
 */ 
public class ViewBranchDetailsServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 1L;
     
	@Override
	protected void doGet(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		LicenseTool license = getLicense(request);
		if (license.isLibraryPluginEnabled())
		{
			String branchIdParameter = request.getParameter(HttpParameter.ID);
			if (isNotNullOrEmptyString(branchIdParameter))
			{
				try 
				{
					int branchId = Integer.parseInt(branchIdParameter);
					Branch branch = license.getLibraryPlugin().getBranchDetails(branchId);
					setRequestAttribute(request, HttpParameter.BRANCH, branch);
					
				} catch (SystemInternalFailureException exception) 
				{
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);		
				}
				forwardTo(request, response, "/components/branch.vm");
			} else 
			{
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);				
			}
		} else 
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
