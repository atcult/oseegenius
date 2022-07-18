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
package com.atc.osee.web.servlet.authority;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.servlets.HomeServlet;

public class HomeAuthorityServlet extends HomeServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
	
		getSearchExperience(request).resetTabs();
		
		HttpSession session = request.getSession(); 
		if(session!=null){
			if(session.getAttribute(HttpAttribute.LOGICAL_VIEW)!=null){
				session.removeAttribute(HttpAttribute.LOGICAL_VIEW);
			}
		}
	
		forwardTo(
				request, 
				response, 
				"/authority/index_auth.vm",
				"homepage_auth.vm");
		}
	
}
