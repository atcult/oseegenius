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
import javax.servlet.http.HttpSession;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.Layout;
import com.atc.osee.web.Page;

/** 
 * Osee Genius -W- homepage web controller..
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class HomeServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "home");
		setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "search");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "suggest");
		setRequestAttribute(request, HttpAttribute.DONT_SHOW_PERSPECTIVE_BUTTON, true);
		request.removeAttribute(HttpAttribute.OG_CONTEXT);
		getSearchExperience(request).resetTabs();

        HttpSession session = request.getSession();
        session.removeAttribute(HttpAttribute.LOGICAL_VIEW);

		setSessionAttribute(request, HttpAttribute.OG_CONTEXT, "simple");

        String logicalView = request.getParameter(HttpParameter.LOGICAL_VIEW);

        if (isNullOrEmptyString(logicalView)) {
/*
            forwardTo(
                    request,
                    response,
                    "/index-before-choice.vm",
                    Layout.HOME_PAGE);
*/
        } else {
            session.setAttribute(HttpAttribute.LOGICAL_VIEW, logicalView);
/*
            forwardTo(
                    request,
                    response,
                    logicalView.equals("ARC") ? "/archome" : "/index.vm",
                    Layout.HOME_PAGE);
*/
        }
        request.setAttribute("specificServletTitle", "simple_search"); 
		forwardTo(
				request, 
				response, 
				Page.HOME_PAGE,
				Layout.HOME_PAGE);
	}
}
