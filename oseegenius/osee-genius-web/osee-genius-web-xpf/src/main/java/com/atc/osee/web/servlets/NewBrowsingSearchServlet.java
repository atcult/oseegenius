package com.atc.osee.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.IConstants;

/**
 * Servlet to manage async query for solr count in new browsing (imss, Trento, ...)
 * @author alice
 *
 */
public class NewBrowsingSearchServlet extends OseeGeniusServlet {
	private static final long serialVersionUID = 2463463460L;
	private static final Log logger = LogFactory.getLog(NewBrowsingSearchServlet.class);
	
	@Override
	protected void doGet(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException {
		
		
		String headingId = request.getParameter("headingId");
		String indexId = request.getParameter("indexId");
		String collectionId = request.getParameter("collectionId");
		String searchType =  request.getParameter("searchType");
		String catalogSource = request.getParameter("catalog_source");
		
		int count = 0;
		
		//check if heading id is valid 
		if (headingId != null && !"".equals(headingId)) {
			ISearchEngine searchEngine;
			
			if(searchType != null && "auth".equals(searchType)) {
				searchEngine = (ISearchEngine) getServletContext().getAttribute(IConstants.SEARCH_ENGINE_AUTH_ATTRIBUTE_NAME);
			}
			else {
				searchEngine = getSearchEngine(request);
			}
			
			SolrQuery query = new SolrQuery( "*:*" );		
			query.setFilterQueries( "authority_group_" + indexId + ":"+ headingId);
			
			if(collectionId != null && !"0".equals(collectionId)){
				query.addFilterQuery( "collection_code" + ":"+ collectionId);
			}
			if(catalogSource != null && !"".equals(catalogSource)) {
				query.addFilterQuery( "catalog_source" + ":"+ catalogSource);
			}
			query.setRequestHandler("def");
			query.setRows(0);		
			
			try {
				QueryResponse solrresponse = searchEngine.executeQuery(query);			
				count = (int) solrresponse.getResults().getNumFound();				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String json = "{ \"count\" : \" " + count + "\" }";
		//Posso tornare semplice testo, perchè restituisco solo un valore. Ma in caso di risposta più complessa, modificare rispondendo JSON
		response.setContentType("text/plain");
	    PrintWriter out = response.getWriter();
	    out.print(json);
	    out.flush();
	    out.close();    
	}

}
