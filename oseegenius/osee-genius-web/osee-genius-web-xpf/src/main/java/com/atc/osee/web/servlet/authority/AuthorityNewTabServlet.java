package com.atc.osee.web.servlet.authority;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.servlets.search.NewTabServlet;

public class AuthorityNewTabServlet extends NewTabServlet {

	private static final long serialVersionUID = -5252593723729898884L;
	
	@Override
	protected void forwardTo(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final String targetResource) throws ServletException, IOException 
	{
		
		forwardTo(request, response, targetResource, "auth_one_column.vm");
	}
	
	@Override
	protected void forwardTo(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final String targetResource,
			final String layout) throws ServletException, IOException 
			{
				
				super.forwardTo(request, response, "/authority" + targetResource, "auth_one_column.vm");
			}


}
