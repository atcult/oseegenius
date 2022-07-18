package com.atc.osee.web.servlet.authority;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.Layout;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * 
 * @author Maura Braddi
 * @since 1.0
 */
public class ViewAuthorityServlet extends OseeGeniusServlet 
{	
	private static final long serialVersionUID = -7879345161436752038L;

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		
		try 
		{
			String id = request.getParameter("id");
				
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
						SolrDocument resource = resourceResponse.getResults().get(0);
						setRequestAttribute(request, IConstants.RESOURCE_KEY, resource);
					}
					forwardTo(request, response, "authority/authority_card_from_record.vm", Layout.DUMMY_LAYOUT);
				}
				
		} catch (SystemInternalFailureException exception) 
		{
			setErrorFlag(request);
			request.setAttribute("error_info", "msg_500");
			forwardTo(request, response, "authority/authority_card_from_record.vm", Layout.DUMMY_LAYOUT);
			return;
		}
	}

}
