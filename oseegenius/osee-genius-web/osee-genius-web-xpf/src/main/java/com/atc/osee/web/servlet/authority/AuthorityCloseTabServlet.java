package com.atc.osee.web.servlet.authority;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.servlets.OseeGeniusServlet;

public class AuthorityCloseTabServlet extends OseeGeniusServlet {

	private static final long serialVersionUID = -2169693693370171627L;
	
	@Override
	protected void service(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		getSearchExperience(request).closeTab();
		SearchTab tab = getSearchExperience(request).getCurrentTab();
		if (tab.isExternalSearchTab())
		{
			response.sendRedirect("metasearch?" + tab.getQueryParameters());
			return;
		} else
		{
			response.sendRedirect("authSearch?" + tab.getQueryParameters());
			return;			
		}
	}

}
