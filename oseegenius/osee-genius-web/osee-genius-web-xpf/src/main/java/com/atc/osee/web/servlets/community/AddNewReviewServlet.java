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

import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.Layout;
import com.atc.osee.web.Page;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Visit;

/**
 * OseeGenius -W- "Add a new review" web controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AddNewReviewServlet extends OseeGeniusCommunityServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * Simply moves to the new review form page.
	 * 
	 * @param request the http request.
	 * @param response the http response.
	 * @throws ServletException in case of component failure.
	 * @throws IOException in case of I/O failures.
	 */
	@Override
	protected void doGet(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		try 
		{
			if (getVisit(request).isAuthenticated() && getLicense(request).isCommunityPluginEnabled())
			{
				String outcome = request.getHeader("referer");
				String uri = request.getParameter(HttpParameter.URI);
				
				if (isNullOrEmptyString(uri))
				{
					uri = (String) request.getAttribute(HttpParameter.URI);
				}
				
				if (isNotNullOrEmptyString(uri))
				{
					request.setAttribute(HttpParameter.URI, uri);
					request.setAttribute("outcome", outcome);
					
					QueryResponse qresponse = getSearchEngine(request).findDocumentByURI(uri);
					if (!qresponse.getResults().isEmpty())
					{
						setRequestAttribute(request, HttpAttribute.RESOURCE, qresponse.getResults().get(0));
						forwardTo(request, response, Page.NEW_REVIEW, "new_review_layout.vm");
					}
				} else 
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);				
				}
			} else 
			{
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		} catch (Exception exception)
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);			
		}
	}
	
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
		if (visit.isAuthenticated())
		{
			String uri = request.getParameter(HttpParameter.URI);
			String reviewText = request.getParameter(HttpParameter.REVIEW_TEXT);
			String outcome = request.getParameter("outcome");
			if (isNotNullOrEmptyString(uri) && isNotNullOrEmptyString(reviewText))
			{
				try 
				{
					getLicense(request).getCommunityPlugin().addNewReview(visit.getAccount().getId(), uri, reviewText);
					response.sendRedirect(outcome);
				} catch (SystemInternalFailureException exception) 
				{
					setRequestAttribute(request, HttpAttribute.IN_ERROR, true);
					setRequestAttribute(request, HttpParameter.URI, uri);
					setRequestAttribute(request, HttpParameter.REVIEW_TEXT, reviewText);
					request.setAttribute("outcome", outcome);
					forwardTo(request, response, Page.NEW_REVIEW ,  "new_review_layout.vm");		
				}
			} else 
			{
				setRequestAttribute(request, HttpAttribute.IN_ERROR, true);
				setRequestAttribute(request, HttpParameter.URI, uri);
				setRequestAttribute(request, HttpParameter.REVIEW_TEXT, reviewText);
				request.setAttribute("outcome", outcome);
				forwardTo(request, response, Page.NEW_REVIEW, "new_review_layout.vm");
			}
		}
	}
}