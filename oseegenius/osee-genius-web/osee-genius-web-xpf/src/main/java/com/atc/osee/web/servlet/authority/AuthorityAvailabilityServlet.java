package com.atc.osee.web.servlet.authority;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.servlets.OseeGeniusServlet;

public class AuthorityAvailabilityServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 6311797245905285202L;
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorityAvailabilityServlet.class);
	
	@Override
	protected void service(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		String id = request.getParameter("hdg");
		
		try 
		{
				ISearchEngine searchAuthEngine = (ISearchEngine) getServletContext().getAttribute(IConstants.SEARCH_ENGINE_AUTH_ATTRIBUTE_NAME);
				if(searchAuthEngine!=null){
					
					SolrQuery query = new SolrQuery(
							"hdg_nbr_id"
							+ IConstants.COLON 
							+ IConstants.DOUBLE_QUOTES 
							+ id 
							+ IConstants.DOUBLE_QUOTES);
					query.setRequestHandler(ISolrConstants.DETAILS_QUERY_TYPE_NAME);
					QueryResponse resourceResponse = searchAuthEngine.executeQuery(query);
					
					if (resourceResponse != null && !resourceResponse.getResults().isEmpty())
					{	
						setStringInResponse(id, response); 
						return;
					}
				}
				
		} catch (SystemInternalFailureException exception)
		{
			LOGGER.error("Unable to retrieve authority availability for heading " + id, exception);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private void  setStringInResponse(String id, HttpServletResponse response) throws IOException{
		
		StringBuilder builder = new StringBuilder("{")
		.append("\"u\":\"").append(id).append("\"")
		.append("}");
		response.getWriter().write(builder.toString());  
		
	}
}