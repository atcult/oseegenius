package com.atc.osee.web.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.velocity.tools.Toolbox;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;

public class ShowChildrenServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
        //setSessionAttribute(request, HttpAttribute.LOGICAL_VIEW, "ARC");

		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "home");
		setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "search");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "suggest");
		setRequestAttribute(request, HttpAttribute.DONT_SHOW_PERSPECTIVE_BUTTON, true);
		
		try 
		{
			String parentUri = request.getParameter(HttpParameter.URI);
			if (isNotNullOrEmptyString(parentUri))
			{
				ISearchEngine searchEngine = getSearchEngine(request);
				QueryResponse parentData = searchEngine.findDocumentByURI(parentUri);
				SolrDocument resource =  parentData.getResults().get(0);
				setRequestAttribute(request, "resource", resource);
				
				SolrQuery childrenQuery = new SolrQuery("parent_id:\"" + parentUri +"\"");
				childrenQuery.setSortField("order_nbr", ORDER.asc);
				childrenQuery.setQueryType(ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME);
				childrenQuery.setRows(500);
				
				QueryResponse childrenData = searchEngine.executeQuery(childrenQuery);
				setRequestAttribute(request, "children", childrenData);
			
				//MLT
				String pageNumber = request.getParameter(HttpParameter.PAGE_INDEX);
				String levelCodeValue = (String)resource.getFieldValue("level_code");
				StringBuilder levelCodeFilter = createFilter("level_code",levelCodeValue);
				
				if(!"FO".equals(levelCodeValue)){
				    List<String> archiveCodes = ((List<String>)resource.getFieldValue("archive_code"));
				    if (archiveCodes != null && !archiveCodes.isEmpty()) {
                        String classificationValue = ((List<String>) resource.getFieldValue("archive_code")).get(0).toString();
                        StringBuilder classificatonFilter = createFilter("archive_code", classificationValue);
                        Toolbox toolbox = (org.apache.velocity.tools.Toolbox) getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
            			String instituition = (String) toolbox.get("institution");		
                        setRequestAttribute(
                                request,
                                IConstants.MORE_LIKE_THIS_RESULT_KEY,
                                searchEngine.findDocumentByURIWithMLT(parentUri, pageNumber, levelCodeFilter.toString(), instituition, classificatonFilter.toString()));
                    } else {
                        setRequestAttribute(
                                request,
                                IConstants.MORE_LIKE_THIS_RESULT_KEY,
                                searchEngine.findDocumentByURIWithMLT(parentUri, pageNumber, levelCodeFilter.toString()));
                    }
				}else{
					setRequestAttribute(
							request, 
							IConstants.MORE_LIKE_THIS_RESULT_KEY, 
							searchEngine.findDocumentByURIWithMLT(parentUri, pageNumber,levelCodeFilter.toString()));	
					
				}
				
			}
		} catch (SystemInternalFailureException exception) 
		{
			// Ignore...hopefully this is a temporary error so we won't show funds.
		}
		forwardTo(request, response, "/arc-master-details.vm", "one_column.vm");
	}
	
	
	private StringBuilder createFilter(String name , String value){
		StringBuilder filter = new StringBuilder();
		filter.append(name).append(":\"").append(value).append("\"");
		return filter;
	}
	
}