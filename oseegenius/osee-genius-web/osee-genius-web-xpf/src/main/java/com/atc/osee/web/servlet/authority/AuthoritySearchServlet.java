package com.atc.osee.web.servlet.authority;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.servlets.search.SearchServlet;

public class AuthoritySearchServlet extends SearchServlet {
	
	private static final long serialVersionUID = -4904910542579419059L;

	protected ISearchEngine getSearchEngine(final HttpServletRequest request)
	{
		return (ISearchEngine) getServletContext().getAttribute(IConstants.SEARCH_ENGINE_AUTH_ATTRIBUTE_NAME);
	}
	
	@Override
	protected void forwardTo(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final String targetResource) throws ServletException, IOException 
	{
		
		forwardTo(request, response, targetResource, "auth_three_columns.vm");
	}
	
	@Override
	protected void forwardTo(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final String targetResource,
			final String layout) throws ServletException, IOException 
			{
				
				super.forwardTo(request, response, "/authority" + targetResource, "auth_three_columns.vm");
			}
	
	@Override
	public String getContext() {
		return "authResource";
	}

}
