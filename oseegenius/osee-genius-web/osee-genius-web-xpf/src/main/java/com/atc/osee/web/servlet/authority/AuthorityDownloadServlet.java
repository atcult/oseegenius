package com.atc.osee.web.servlet.authority;

import javax.servlet.http.HttpServletRequest;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.servlets.DownloadServlet;

public class AuthorityDownloadServlet extends DownloadServlet {
	
	private static final long serialVersionUID = 2013305875339071431L;

	protected ISearchEngine getSearchEngine(final HttpServletRequest request)
	{
		return (ISearchEngine) getServletContext().getAttribute(IConstants.SEARCH_ENGINE_AUTH_ATTRIBUTE_NAME);
	}

}
