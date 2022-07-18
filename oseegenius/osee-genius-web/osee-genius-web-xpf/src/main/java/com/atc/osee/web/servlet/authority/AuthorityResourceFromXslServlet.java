package com.atc.osee.web.servlet.authority;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.velocity.tools.Toolbox;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.XsltUtility;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.servlets.search.ResourceFromXslServlet;


public class AuthorityResourceFromXslServlet extends ResourceFromXslServlet {

	private static final long serialVersionUID = -8523454757470183299L;
	
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
		
		forwardTo(request, response, targetResource, "no_header_layout.vm");
	}
	
	@Override
	protected void forwardTo(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final String targetResource,
			final String layout) throws ServletException, IOException 
			{
				
				super.forwardTo(request, response, "/authority" + targetResource, "no_header_layout.vm");
			}

}
