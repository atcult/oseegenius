package com.atc.osee.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.LimitFacet;
import com.atc.osee.web.servlets.OseeGeniusServlet;

public class ShowAllLimitsServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 6311797245905285202L;
	
	@Override
	protected void service(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		String bundle = request.getParameter("bundle");
		String facetName = request.getParameter(HttpParameter.FILTER_BY);
		String offsetParamAsString = request.getParameter("offset");
		int offset = 0;
		
		try {
			if (offsetParamAsString != null)
			{
				offset = Integer.parseInt(offsetParamAsString);
			}
		} catch (Exception e) {
			// Ignore
		}
		
		LimitFacet limit = getVisit(request).getAdvancedSearchExperience().getLimit(facetName);
		if (isNotNullOrEmptyString(facetName))
		{
			SolrQuery query = new SolrQuery("*:*");
			query.setQueryType("def");
			query.setRows(0);
			query.setFacet(true);
			query.addFacetField(facetName);
			query.set("f."+facetName+".facet.limit", IConstants.FACET_PER_PAGE + 1);			
			query.set("f."+facetName+".facet.offset", offset);	
			
			//Bug 6624 for logic view filter in query
			String lv = (String) request.getSession().getAttribute(HttpAttribute.LOGICAL_VIEW);
			if (lv != null)
			{
				query.setFilterQueries("catalog_source:\"" + lv + "\"");
			}
			// For imss
			else {											
					String collection_code = (String) request.getSession().getAttribute("collection");
					if (collection_code != null) {
						query.setFilterQueries("catalog_source:\"" + collection_code + "\"");
					}
			}
			
			try 
			{
				QueryResponse qresponse = getSearchEngine(request).executeQuery(query);
				FacetField ff = qresponse.getFacetField(facetName);
				if (ff.getValueCount() > IConstants.FACET_PER_PAGE) {
					setRequestAttribute(request, "next", true);					
					setRequestAttribute(request, "nextOffset", offset + IConstants.FACET_PER_PAGE);											
					
				}
				
				if (offset > 0)
				{
					setRequestAttribute(request, "back", true);										
					setRequestAttribute(request, "backOffset", offset - IConstants.FACET_PER_PAGE);																					
				}
				
				setRequestAttribute(request, HttpAttribute.LIMIT_NAME, facetName);
				setRequestAttribute(request, HttpAttribute.LIMIT, ff);
				setRequestAttribute(request, "limitFacet", limit);
				setRequestAttribute(request, "bundle", bundle);
				forwardTo(request, response, "/show_all_limit.vm", "advanced_search_layout.vm");
			} catch (SystemInternalFailureException e) 
			{
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}
}
