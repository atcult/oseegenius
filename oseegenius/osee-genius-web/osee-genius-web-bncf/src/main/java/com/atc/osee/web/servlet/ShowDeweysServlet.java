package com.atc.osee.web.servlet;
/*
 *Serlet for Dwey Nagation.
 *Template: deweys.vm
 *Layout: advanced_search_layout.vm
 *Parameters Input:
 * @param dewey [Dewey Number]
 * @param Edclass [classe Dewey as filter]
 * @param nextIs4 [to say that in next page link are to search] 
 * @param p [page]
 * @param size [size of results]
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;


import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
//import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.servlets.OseeGeniusServlet;

public class ShowDeweysServlet extends OseeGeniusServlet {
	
	private static final long serialVersionUID = -1538553826359055472L;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		ISearchEngine searchEngine = getSearchEngine(request);
		SolrQuery query = new SolrQuery(ISolrConstants.ALL);
		query.setQueryType(ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME);
		
		String dewey = request.getParameter("dewey");
		String Edclass = request.getParameter("Edclass");

		int pageIndex = getPageIndex(request.getParameter(HttpParameter.PAGE_INDEX));
		int pageSize = getPageSize(request.getParameter(HttpParameter.PAGE_SIZE));
		int pageStart = getPageStart(pageIndex, pageSize);
		
		request.setAttribute("specificServletTitle", "dewey_navigator");
		
		HttpSession session = request.getSession(false);
		
		String logicalView = null;
		if (session != null){ logicalView = (String) session.getAttribute(HttpAttribute.LOGICAL_VIEW);}	
		if (isNotNullOrEmptyString(logicalView)) { query.addFilterQuery("catalog_source:" + logicalView); }

		if (isNotNullOrEmptyString(Edclass)) {
			query.addFilterQuery("other_classification_number:\"(" + Edclass+")\"");
			request.setAttribute("ValEdclass", Edclass);
		}
		
		int lenDewey = 0;
		
		if ( dewey!= null ){
			lenDewey = dewey.length();	
			if (lenDewey == 1) {
				String breadcome[] = {"head", dewey.substring(0,1) };
				request.setAttribute("deweyRoot",  breadcome);
				request.setAttribute("totalrecords", getNumTotalRec(searchEngine, query, dewey.substring(0,1)));
				request.setAttribute("lenDewey",  1);
				request.setAttribute("deweyorig", dewey);
			} 
			
			if (lenDewey == 2) {
				String breadcome[] = {"head", dewey.substring(0,1), dewey.substring(0,2) };
				request.setAttribute("deweyRoot",  breadcome);
				request.setAttribute("totalrecords", getNumTotalRec(searchEngine, query, dewey.substring(0,2)));
				request.setAttribute("lenDewey",  2);
				request.setAttribute("deweyorig", dewey);
			} 
			if (lenDewey == 3) {
				String breadcome[] = {"head", dewey.substring(0,1), dewey.substring(0,2), dewey.substring(0,3) };
				request.setAttribute("deweyRoot",  breadcome);
				request.setAttribute("totalrecords", getNumTotalRec(searchEngine, query, dewey.substring(0,3)));
				request.setAttribute("lenDewey",  3);
				request.setAttribute("deweyorig", dewey);
			} 

			
		} else {
			String breadcome[] = {"head"};
			request.setAttribute("deweyRoot", breadcome );
			request.setAttribute("lenDewey",  0);
		}
		
		try {
				if (lenDewey < 3) {
					LinkedHashMap<String, Long> mapDewey = new LinkedHashMap<String, Long>();
					if(dewey == null ){
						mapDewey=createListDeweys(searchEngine, query);
					}
					else{
						mapDewey=createListDeweys(searchEngine, query, dewey, lenDewey);
					}
					
					request.setAttribute("listDewey", mapDewey);
					forwardTo(request, response, "/nav_deweys.vm", "one_col_dewey.vm");
				} 
				
				if (lenDewey == 3)  {
					
					query.addFilterQuery("dewey:"+dewey+"*");
					query.clearSorts();
					query.setSort("dewey_sort", SolrQuery.ORDER.asc);
					query.set("facet.field","dewey_with_all");
					query.setFacetLimit(-1);
					//query.set("f.dewey_with_all.facet.limit", pageIndex + 1);
					//query.set("f.dewey_with_all.facet.offset", pageSize);	
					//query.setRows(0); // Because we don't need docs here, only facets.
					
					QueryResponse result = searchEngine.executeQuery(query);
					
					FacetField deweyFacet = result.getFacetField("dewey_with_all");
					Integer numClass = deweyFacet.getValueCount();
					
					if (numClass > 0) {
						List<String> deweycodes = new ArrayList<String>();
						List<String> descriptions = new ArrayList<String>();
						List<String> deweyNumbers = new ArrayList<String>();
						
						if (pageSize >= numClass ) {
							for (Count count : deweyFacet.getValues()) {
								deweyNumbers.add( Long.toString(count.getCount()) );
								String str_tmp = count.getName();
								str_tmp = str_tmp.trim();
								int pos = str_tmp.indexOf(' ');
								if (pos < 1) {
									deweycodes.add(str_tmp);
									descriptions.add(" ");
								} else {
									deweycodes.add(str_tmp.substring(0, pos));
									descriptions.add(str_tmp.substring(pos));
								}
					        }
					 	} else {
					 		if ( numClass > pageSize ) {
					 			int pageEnd = pageStart + pageSize;
					 			List<Count> facetDeweyCounts = new ArrayList<Count>();
					 			try {
					 				facetDeweyCounts = deweyFacet.getValues().subList(
					 						pageStart,  pageEnd > deweyFacet.getValues().size() ? deweyFacet.getValues().size() : pageEnd
					 						);
					 			} catch ( Exception e){
					 				
					 			}
					 			
					 			for (Count count : facetDeweyCounts) {
					 				deweyNumbers.add( Long.toString(count.getCount()) );
									String str_tmp = count.getName();
									str_tmp = str_tmp.trim();
									int pos = str_tmp.indexOf(' ');
									if (pos < 1) {
										deweycodes.add(str_tmp);
										descriptions.add(" ");
									} else {
										deweycodes.add(str_tmp.substring(0, pos));
										descriptions.add(str_tmp.substring(pos));
									}
					 			}
					 		}else {
					 			forwardTo(request, response, "/empty.vm", "one_column.vm");
					 		}
					 	}
						request.setAttribute("deweycodes", deweycodes);
						request.setAttribute("descriptions", descriptions);
						request.setAttribute("deweyNumbers", deweyNumbers);
						
						request.setAttribute("numClass", numClass.toString());
						request.setAttribute("nextIs4", "Y");
						request.setAttribute("s", new Integer(pageSize).toString());
						request.setAttribute("p", new Integer(pageIndex).toString());
						request.setAttribute("start", new Integer(pageStart).toString());
						
						forwardTo(request, response, "/nav_deweys.vm", "one_col_dewey.vm");
					}else {
						forwardTo(request, response, "/empty.vm", "one_column.vm");
					}
				}
				if (lenDewey > 3)  {
					forwardTo(request, response, "/Error.vm", "one_column.vm");
				}
				
		} catch (SystemInternalFailureException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
	}
	
	private LinkedHashMap<String, Long> createListDeweys(ISearchEngine searchEngine, SolrQuery query) throws SystemInternalFailureException{	
		LinkedHashMap<String, Long> mapDewey = new LinkedHashMap<String, Long>();
		for (Integer i=0; i<10; i++){
			SolrQuery queryInside = new SolrQuery(ISolrConstants.ALL);
			queryInside = query.getCopy();
			queryInside.addFilterQuery("dewey:"+i.toString()+"*");
			QueryResponse result = searchEngine.executeQuery(queryInside);
			if (result.getResults().getNumFound()> 0) {
				mapDewey.put(i.toString(), result.getResults().getNumFound());
			}
		}
		return mapDewey;	
	}
	
	private LinkedHashMap<String, Long> createListDeweys(ISearchEngine searchEngine, SolrQuery query, String dewey, Integer lenDewey ) throws SystemInternalFailureException{	
		LinkedHashMap<String, Long> mapDewey = new LinkedHashMap<String, Long>();
		if ((lenDewey == 1) || (lenDewey == 2))  {
			for (Integer i=0; i<10; i++){
				SolrQuery queryInside = new SolrQuery(ISolrConstants.ALL);
				queryInside = query.getCopy();
				queryInside.addFilterQuery("dewey:"+dewey+i.toString()+"*");
				QueryResponse result = searchEngine.executeQuery(queryInside);
				if (result.getResults().getNumFound()> 0) {
					mapDewey.put(dewey+i.toString(), result.getResults().getNumFound());
				}
			}
		}
		return mapDewey;	
	}
	
	private int getPageIndex(final String pageNumberAsString){
		try 
		{
			int result = pageNumberAsString != null ? Integer.parseInt(pageNumberAsString)  : 0;
			return result > 0 ? result : 0;
		} catch (Exception exception)
		{
			return 0;
		}
	}
	
	private int getPageSize(final String pageSizeAsString){
		try 
		{
			return pageSizeAsString != null ? Integer.parseInt(pageSizeAsString) : 20;
		} catch (Exception exception)
		{
			return 20;
		}
	}
	
	private int getPageStart(int pageIndex, int pageSize){
		if (pageIndex < 1) { return 0; }
		if (pageIndex == 1) { return 0; }
		if (pageSize < 1) { return 0; }
		try {
			return ((pageIndex - 1) * pageSize);
		} catch (Exception exception) {
			return 0;
		}
	}
	
	
	
	private long getNumTotalRec(ISearchEngine searchEngine, SolrQuery query, String dewey ) {
		long resultRecs = 0;
		
		SolrQuery queryInside = new SolrQuery(ISolrConstants.ALL);
		queryInside = query.getCopy();
		queryInside.addFilterQuery("dewey:"+dewey+"*");
		QueryResponse result;
		try {
			result = searchEngine.executeQuery(queryInside);
			resultRecs = result.getResults().getNumFound();
			return resultRecs;
		} catch (SystemInternalFailureException e) {
			return 0;
		}
	}

}
