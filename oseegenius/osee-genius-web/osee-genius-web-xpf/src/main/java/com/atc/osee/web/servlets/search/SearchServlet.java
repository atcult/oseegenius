/* 
 	Copyright (c) 2010 @Cult s.r.l. All rights reserved.
	
	@Cult s.r.l. makes no representations or warranties about the 
	suitability of the software, either express or implied, including
	but not limited to the implied warranties of merchantability, fitness
	for a particular purpose, or non-infringement. 
	
	@Cult s.r.l.not be liable for any damage suffered by 
	licensee as a result of using, modifying or distributing this software 
	or its derivates.
	
	This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.web.servlets.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.print.attribute.standard.PrinterLocation;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.velocity.tools.Toolbox;

import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.jfree.util.Log;
import org.w3c.dom.Document;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.XsltUtility;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.servlets.RememberLastVisitedResourceServlet;
import com.atc.osee.web.servlets.writers.AbstractOutputWriter;
import com.atc.osee.web.tools.ConfigurationTool;

import com.atc.osee.web.folio.BncfElemtResearchHistory;




/**
 * OseeGenius -W- search controller.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class SearchServlet extends RememberLastVisitedResourceServlet 
{
	private static final long serialVersionUID = -760895485203089280L;
	
	protected final Map<String, String> orderByAttributes = new HashMap<String, String>();
	{	
		orderByAttributes.put("score", "score desc,title_sort asc,author_sort asc,publication_date_sort desc");
		orderByAttributes.put("title", "title_sort asc,author_sort asc,score desc");
		orderByAttributes.put("author", "author_sort asc,score desc");		
		orderByAttributes.put("year", "publication_date_sort desc,title_sort asc,score desc");
		orderByAttributes.put("yearAsc", "publication_date_sort asc,title_sort asc,score desc");
		orderByAttributes.put("id", "id_sort desc");		
		orderByAttributes.put("dist", "geodist() asc");	
		orderByAttributes.put("newest", "index_time desc, publication_date desc");	
		orderByAttributes.put("rnewest", "update_time desc, publication_date desc");
		//ordinamento authority
		orderByAttributes.put("score_auth", "score desc, author_sort asc, discipline_sort asc");
		orderByAttributes.put("score_100_110_111", "author_sort asc, score desc");
		orderByAttributes.put("score_discipline", "discipline_sort asc, score desc");
	}
	
	protected final Map<String, String> operatorMappings = new HashMap<String, String>(5);
	{
		operatorMappings.put(IConstants.AND, IConstants.AND + " ");
		operatorMappings.put(IConstants.OR, IConstants.OR + " ");
		operatorMappings.put("ANDNOT", "AND -");
		operatorMappings.put("ORNOT", "OR -");		
		operatorMappings.put("EAND", IConstants.AND + " ");
		operatorMappings.put("EOR", IConstants.OR + " ");
		operatorMappings.put("EANDNOT", "AND -");
		operatorMappings.put("EORNOT", "OR -");		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");	
				
		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "home");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "suggest");
		setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, getServletName());
		
		// Sanity check: request is coming from "closeTab" and this tab was empty (no parameters other than t)
		@SuppressWarnings("rawtypes")
		Map map = request.getParameterMap();
		if (map != null && map.size() == 1 && map.containsKey("t")) {
			goToSearchResultsPage(request, response);
			return;
		}
		
		HttpSession session = request.getSession(true);
		if (session == null) {
			setSessionAttribute(request, HttpAttribute.OG_CONTEXT, null);		
		}
		
		String query = request.getParameter(HttpParameter.QUERY);
		int tabId = getTabId(request);
		SearchTab currentTab = getSearchExperience(request).getTab(tabId);
		
		setRequestAttribute(request, HttpAttribute.CURRENT_TAB, currentTab);
		
		int pageIndex = getPageIndex(request.getParameter(HttpParameter.PAGE_INDEX));		
		
		if (getLicense(request).isFederatedSearchEnabled()) {
			Set<String> enabledTargets = (Set<String>) session.getAttribute(HttpAttribute.TARGETS_ENABLED);
			
			if (enabledTargets == null || (!enabledTargets.isEmpty() && enabledTargets.size()>1) ||
					(!enabledTargets.isEmpty() && enabledTargets.size()==1 && enabledTargets.iterator().next().indexOf("opac.sbn.it")==-1)) {
				try {
					currentTab.getPazpar2().search(request);
				} catch (final Exception exception) {
					// Should be already logged out.
				}
			} else {
				if (!enabledTargets.isEmpty() && enabledTargets.size()==1 && enabledTargets.iterator().next().indexOf("opac.sbn.it")!=-1){
					if (query == null || query.trim().length() == 0 || ISolrConstants.ALL.equals(query) || "adv".equals(request.getParameter(HttpParameter.QUERY_TYPE))) {
						request.setAttribute(HttpAttribute.FEDERATED_SEARCH_APPLICABLE, false);
					} else {
						request.setAttribute(HttpAttribute.FEDERATED_SEARCH_APPLICABLE, true);
					}					
				}
			}
		} 
		
		if (isNullOrEmptyString(query)) {
			if (request.getParameter("dls") != null && request.getParameter("t") != null) {
				goToSearchResultsPage(request, response);
				return;
			} else {
				query = ISolrConstants.ALL;				
			}
		} 
		//else 
			int pageSize = getPageSize(request.getParameter(HttpParameter.PAGE_SIZE));
			
			String queryType = getQueryType(query, request.getParameter(HttpParameter.QUERY_TYPE));
			
			
			String [] filters = request.getParameterValues(HttpParameter.FILTER_BY);
			
				
			
			//////////IMSS
			
			Toolbox toolbox = (org.apache.velocity.tools.Toolbox) getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
			String instituition = (String) toolbox.get("institution");
			if("IMSS".equals(instituition)){	
				String collection = null;
				if (request.getParameter("collection_data")!=null) {
					collection = request.getParameter("collection_data").replaceAll("[.:,;()><&|^#*/=~'\"-+]", "");
				}
			
				if("0".equals(collection)){
					session.removeAttribute("collection");
					session.removeAttribute(HttpAttribute.LOGICAL_VIEW);
					filters = removeFilterIndex("collection_code", filters);	
				}					
				else {					
					if(collection != null) {						
						session.setAttribute("collection", collection);	
						session.setAttribute(HttpAttribute.LOGICAL_VIEW, collection);
						if(getFilterIndex("collection_code", filters) != -1) {
							filters[getFilterIndex("collection_code", filters)] = collection;
						}
						if(getFilterIndex("catalog_source", filters) != -1) {
							filters[getFilterIndex("catalog_source", filters)] = collection;
						}
					}
					//caso del bottone di breadcrumb: ripulisco i filtri per evitare la proliferazione
					else {
						if(getFilterIndex("collection_code", filters) != -1) {
							filters = removeFilterIndex("collection_code", filters);
						}
						if(getFilterIndex("catalog_source", filters) != -1) {
							filters = removeFilterIndex("catalog_source", filters);
						}
					}
				}			
			}
				////////////
				 
				 
														
			String orderBy = request.getParameter(HttpParameter.ORDER_BY);
			if (orderBy!=null) {
				orderBy=orderBy.replaceAll("[.:,;()><&|^#*/=~'\"-+]", "");
			}
			
			List<Double> latLonPt = getLatLon(request.getParameter(HttpParameter.GEO_PT));
			int distance = getDistance(request.getParameter(HttpParameter.GEO_DISTANCE));
			
			ISearchEngine searchEngine = getSearchEngine(request);
			boolean enableFieldCollapsing = getLicense(request).isFieldCollapsingEnabled() &&  "ll".equals(request.getParameter("v"));
			SolrQuery solrquery = createSolrQuery(query, pageIndex, pageSize, queryType, filters, request, orderBy, enableFieldCollapsing);
			
			try 
			{
				QueryResponse solrresponse = searchEngine.executeQuery(solrquery);
				
				String hrQuery=null;
				if (!solrresponse.getResults().isEmpty() && pageIndex == 0 && request.getRequestURL().indexOf("authSearch")==-1 )
				{	
					
					if (!ISolrConstants.ADVANCED_SEARCH_QUERY_TYPE_NAME.equals(queryType))
					{
						getVisit(request).getSearchHistory().addSimpleSearchEntry(
								query+"", 
								queryType, 
								solrresponse.getResults().getNumFound(), 
								orderBy,
								getRequestUrl(request), 
								solrquery.getFilterQueries(),
								latLonPt,
								distance);
					} else 
					{
						hrQuery = solrquery.get("hrq");
						solrquery.remove("hrq"); 
						getVisit(request).getSearchHistory().addAdvancedSearchEntry(
								hrQuery, 
								queryType, 
								solrresponse.getResults().getNumFound(), 
								orderBy,
								getRequestUrl(request), 
								solrquery.getFilterQueries());	
						}
				}
				
				String queryToSave;
				String section;
				if (!ISolrConstants.ADVANCED_SEARCH_QUERY_TYPE_NAME.equals(queryType)) {
					section="simple";
					queryToSave=query;
				}
					
				else {
					section="advanced";
					queryToSave= hrQuery;
				}
					

				if(!isHeadingSearch(filters)) {
					ConfigurationTool configuration = getConfiguration(request);
					if(configuration.searchToSave()) {
						if (getVisit(request).getFolioAccount() != null && isNotNullOrEmptyString(getVisit(request).getFolioAccount().getId())) {
							if (request.getParameter("fromHistory")==null) {
								String url= getRequestUrl(request)+ "&fromHistory=true";
								BncfElemtResearchHistory history= new BncfElemtResearchHistory(
										-1,
										getVisit(request).getFolioAccount().getId(),
										queryToSave, 
										queryType,
										solrresponse.getResults().getNumFound(), 
										orderBy,
										url, 
										solrquery.getFilterQueries(),
										null,
										section);
								history.save();
							}
						}
					}
				}

				AbstractOutputWriter writer = writers.get(request.getAttribute(HttpParameter.WRITE_MODE));
				if (writer == null)
				{
					html(request, response, query, solrquery, solrresponse, pageIndex, pageSize, orderBy, currentTab);
				} else 
				{
					writer.write(request, response, solrresponse.getResults());
				}			
			} catch (SystemInternalFailureException exception) 
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
			}		
		//}
	}
	
	
	
	
	
	private boolean isHeadingSearch(String [] filters) {
		if(filters!=null)
			for (String str: filters) {
				String [] filter = str.split(":");
				if (filter[0].equals("heading_subject_id"))
					return true;
			}
		return false;
	}
	
	/**
	 * Writes out html response.
	 * 
	 * @param request the HTTP request.
	 * @param response the HTTP response.
	 * @param query the query string.
	 * @param solrquery the SOLR query object.
	 * @param solrresponse the SOLR response object.
	 * @param pageIndex the current page index.
	 * @param pageSize the page size in use.
	 * @param orderBy the order by criteria.
	 * @param currentTab the current search tab.
	 * @throws ServletException in case of servlet container failure
	 * @throws IOException in case of I/O failures.
	 */
	protected void html(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final String query, 
			final SolrQuery solrquery,
			final QueryResponse solrresponse, 
			final int pageIndex, 
			final int pageSize,
			final String orderBy,
			final SearchTab currentTab) throws ServletException, IOException
	{
		Visit visit = getVisit(request);
		
		if (isNullOrEmptyString(request.getParameter("dls")))
		{
			currentTab.clearSelection();
		}
		
		if (currentTab.isNew() && query == null)
		{
			goToSearchResultsPage(request, response);
		} else
		{
			

			if (solrresponse.getResults().getNumFound() == 1 && solrresponse.getResults().size() > 0 && solrresponse.getGroupResponse() == null)
			{
				try {
					solrresponse.getResults().get(0);
				} catch (Exception e) {
					Log.error("Errore in search per la query " + query + " ---- solrQuery: " + solrquery.toString() + " ---- getResult size: " + solrresponse.getResults().size() + " ---- numFound: " + solrresponse.getResults().getNumFound());
				}
				SolrDocument document = solrresponse.getResults().get(0);
				StringBuilder url = new StringBuilder(getContext()).append("?uri=").append(document.get("id"));
				
				if ("association".equals(document.getFieldValue(ISolrConstants.CATEGORY_CODE)))
				{
					url.append("&ax=e");
				}
				
				if("ar".equals(document.getFieldValue("category_code"))) {
					url.append("&cc=ar");
				}
				url.append("&found=1");
				if (request.getParameter(HttpParameter.LANGUAGE) != null) {
					url.append("&l=" + request.getParameter(HttpParameter.LANGUAGE));
				}
				  
				//bug 3603:ricerca federata da dettaglio(Gregoriana)
				Toolbox toolbox = (org.apache.velocity.tools.Toolbox) getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
				String instituition = (String) toolbox.get("institution");
				if("Pontificia Università Gregoriana".equals(instituition)){
					setParametersForFederatedFromDetail(request, response, query, solrquery,solrresponse, pageIndex, pageSize,orderBy,currentTab);
				}
				
				response.sendRedirect(url.toString());
				return;
			}
		    
			//caricamente xslt specifici di IMSS
			// inefficiente perchè ogni volta si carica il file, nonostante sia lo stesso
//			try{
//				Document recordDetail;
//				String htmlTransformed;
//				SolrDocumentList list = solrresponse.getResults();				
//				for (SolrDocument s : list){
//					System.out.println(s.getFieldValue("marc_xml"));
//					recordDetail = XsltUtility.transformXSLT(this.getServletContext(), 							 
//							(String) s.getFieldValue("marc_xml"), 
//							"lv_display_record__marc_21_bibliographic__txt");
//					htmlTransformed = XsltUtility.printString(recordDetail);
//					System.out.println(htmlTransformed);
//					s.addField("html", htmlTransformed);
//				}				
//			}catch (Exception e) {
//				e.printStackTrace();
//			}
			//
			
		
		    String tabName = getTabName(query, visit.getPreferredLocale());
			currentTab.setData(tabName, solrquery, solrresponse, pageIndex);
		    currentTab.setNssQuery("");
		    //for xss vulnerabilities I need to escape url, so I need to separate paramter name from its value
		    Map<String,String> paramsMap = new HashMap();
			
			String params = "q=" + java.net.URLEncoder.encode(query, "UTF-8") + "&h=" + solrquery.getRequestHandler() + "&s=" + pageSize + "&t=" + currentTab.getId() + "&dls=true";
			paramsMap.put("q", query);
			paramsMap.put("h", solrquery.getRequestHandler());
			paramsMap.put("s", pageSize + "");
			paramsMap.put("t", currentTab.getId() + "");
			paramsMap.put("dls", "true");
			
			String viewMode = request.getParameter("v");
			viewMode = isNotNullOrEmptyString(viewMode) ? viewMode : "l";
			if (isNotNullOrEmptyString(viewMode))
			{
				params += "&v=" + viewMode.replaceAll("[.:,;()><&|^#*/=~'\"-+]", "");
				paramsMap.put("v", viewMode.replaceAll("[.:,;()><&|^#*/=~'\"-+]", ""));
			} 
			
			if (isNotNullOrEmptyString(orderBy))
			{
				params += "&o=" + orderBy.replaceAll("[.:,;()><&|^#*/=~'\"-+]", "");
				paramsMap.put("o", orderBy.replaceAll("[.:,;()><&|^#*/=~'\"-+]", ""));
			}
			List<String> fParameter = new ArrayList();
			String [] currentlyAppliedFilterQueries = solrquery.getFilterQueries();
			if (currentlyAppliedFilterQueries != null)
			{
				
				for (String filterQuery : currentlyAppliedFilterQueries)
				{
					params += "&f=" + java.net.URLEncoder.encode(filterQuery, "UTF-8");
					fParameter.add(filterQuery);
				}
			}
			
				
			String sfield = request.getParameter(HttpParameter.GEO_SFIELD);
			if (isNotNullOrEmptyString(sfield))
			{
				params += "&sfield=" + sfield;
				paramsMap.put("sfield", sfield);
			} 
			
			String pt = request.getParameter(HttpParameter.GEO_PT);
			String d = request.getParameter(HttpParameter.GEO_DISTANCE);
			
			if (isNotNullOrEmptyString(pt))
			{
				params += "&pt=" + pt;
				paramsMap.put("pt", pt);
				if (isNotNullOrEmptyString(d))
				{
					params += "&d=" + d;
					paramsMap.put("d", d);
				} 
				else params += "&d=" + "1000";
				paramsMap.put("d", "1000");
			} 
			
			params = params.replace("#", "%23");
			
		    
			currentTab.setQueryParameters(params + "&p=" + (pageIndex + 1));
			
			additionalPinnedFilters(request, currentTab);
			
			request.setAttribute("parameters", params);
			request.setAttribute("parametersMap", paramsMap);
			request.setAttribute("filterParams", fParameter);
				
						
			goToSearchResultsPage(request, response);
		} 		
	}

	
	private void setParametersForFederatedFromDetail(
			final HttpServletRequest request, 
			final HttpServletResponse response, 
			final String query, 
			final SolrQuery solrquery,
			final QueryResponse solrresponse, 
			final int pageIndex, 
			final int pageSize,
			final String orderBy,
			final SearchTab currentTab) throws IOException{
					
					 String params ="";
					 String queryParametersForTab = null;

				 
					Visit visit = getVisit(request);
							
					params = "q=" + java.net.URLEncoder.encode(query, "UTF-8") + "&h=" + solrquery.getRequestHandler() + "&s=" + pageSize + "&t=" + currentTab.getId() + "&dls=true";
							
					String viewMode = request.getParameter("v");
					viewMode = isNotNullOrEmptyString(viewMode) ? viewMode : "l";
					if (isNotNullOrEmptyString(viewMode))
					{
						params += "&v=" + viewMode;
						
					} 
					
					if (isNotNullOrEmptyString(orderBy))
					{
						params += "&o=" + orderBy.replaceAll("[.:,;()><&|^#*/=~'\"-+]", "");
					}
					
					String [] currentlyAppliedFilterQueries = solrquery.getFilterQueries();
					if (currentlyAppliedFilterQueries != null)
					{
						for (String filterQuery : currentlyAppliedFilterQueries)
						{
							params += "&f=" + java.net.URLEncoder.encode(filterQuery, "UTF-8");
						}
					}
					
						
					String sfield = request.getParameter(HttpParameter.GEO_SFIELD);
					if (isNotNullOrEmptyString(sfield))
					{
						params += "&sfield=" + sfield;
					} 
					
					String pt = request.getParameter(HttpParameter.GEO_PT);
					String d = request.getParameter(HttpParameter.GEO_DISTANCE);
					
					if (isNotNullOrEmptyString(pt))
					{
						params += "&pt=" + pt;
						if (isNotNullOrEmptyString(d))
						{
							params += "&d=" + d;
						} 
						else params += "&d=" + "1000";
					} 
					
					params = params.replace("#", "%23");
					
					if(currentTab.getQueryParameters()!=null){
						int ind = currentTab.getQueryParameters().indexOf("&p");
						if(ind!=-1 && params.contains(currentTab.getQueryParameters().substring(0,ind))
								&& !params.equals(currentTab.getQueryParameters().substring(0,ind))){
							 queryParametersForTab=currentTab.getQueryParameters();
							 currentTab.setQueryParameters(queryParametersForTab);
						}else{
							 String tabName = getTabName(query, visit.getPreferredLocale());
							 currentTab.setData(tabName, solrquery, solrresponse, pageIndex);
							 currentTab.setQueryParameters(params + "&p=" + (pageIndex + 1)); 
							 currentTab.setNssQuery("nss");
							additionalPinnedFilters(request, currentTab);
							request.setAttribute("parameters", params);
						}
						
					}else{
						 String tabName = getTabName(query, visit.getPreferredLocale());
						 currentTab.setData(tabName, solrquery, solrresponse, pageIndex);
						 currentTab.setQueryParameters(params + "&p=" + (pageIndex + 1)); 
						 currentTab.setNssQuery("nss");
						additionalPinnedFilters(request, currentTab);
						request.setAttribute("parameters", params);
					}	
	
	}
	
	
	
	
	
	/**
	 * Return a correct context for Servlet mapping
	 * @return
	 */
	public String getContext()
	{
		return "resource";
	}
	
	/**
	 * Sets a request attibrute containing additional pinned filters.
	 * 
	 * @param request the {@link HttpServletRequest}.
	 * @param tab the current {@link SearchTab}.
	 */
	private void additionalPinnedFilters(final HttpServletRequest request, final SearchTab tab)
	{
		if (getLicense(request).isPinFiltersEnabled() && tab.hasPinnedFilters())
		{
			StringBuilder builder = new StringBuilder();
			for (String filter : tab.getPinnedFilters())
			{
				builder.append("&f=").append(filter);
			}
			request.setAttribute(HttpAttribute.ADDITIONAL_PINNED_FILTERS, builder.toString());
		} else 
		{
			request.setAttribute(HttpAttribute.ADDITIONAL_PINNED_FILTERS, IConstants.EMPTY_STRING);
		}
	}

	/**
	 * Returns the tab identifier associated with the incoming request.
	 * 
	 * @param request the http request.
	 * @return the tab identifier associated with the incoming request.
	 */
	protected int getTabId(final HttpServletRequest request)
	{
		String tabIdParameter = request.getParameter(HttpParameter.TAB_ID);
		int tabId = 0;
		
		// If needed because most of time this parameter is null so 
		// it's better to avoid try / catch block
		if (tabIdParameter != null)
		{
			try 
			{ 
				tabId = Integer.parseInt(tabIdParameter); 
			} catch (Exception exception) 
			{
				// Nothing
			}
		}
		return tabId;
	}
	
	/**
	 * Forwards to results page.
	 * 
	 * @param request the http request.
	 * @param response the http response.
	 * @throws ServletException in case of servlet container exception.
	 * @throws IOException in case of I/O exception.
	 */
	protected void goToSearchResultsPage(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		 request.setAttribute("specificServletTitle", "results_search"); 
		 forwardTo(request, response, "/hits.vm");
	}
	
	/**
	 * Returns the size (in terms of displayed hits) of each result page.
	 * 
	 * @param pageSizeAsString the incoming page size http request parameter.
	 * @return the size (in terms of displayed hits) of each result page.
	 */
	protected int getPageSize(final String pageSizeAsString)
	{
		try 
		{
			return pageSizeAsString != null ? Integer.parseInt(pageSizeAsString) : 10;
		} catch (Exception exception)
		{
			return 10;
		}
	}
	
	/**
	 * Returns the page index.
	 * 
	 * @param pageNumberAsString the incoming http request parameter.
	 * @return the page index that will be shown.
	 */
	protected int getPageIndex(final String pageNumberAsString)
	{
		try 
		{
			int result = pageNumberAsString != null ? Integer.parseInt(pageNumberAsString) - 1 : 0;
			return result > 0 ? result : 0;
		} catch (Exception exception)
		{
			return 0;
		}
	}

	/**
	 * Returns the query type that will be used for the current search.
	 * 
	 * @param q the incoming query http request parameter.
	 * @param qt the incoming query type http request parameter.
	 * @return the query type that will be used for the current search.
	 */
	protected String getQueryType(final String q, final String qt) {
		//String result = "*:*".equals(q) ? ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME : qt;
		return (qt != null) ? qt : "any_bc";
	}
	
	/**
	 * Creates and returns a valid SOLR query using the incoming http parameters.
	 * 
	 * @param queryString the query string.
	 * @param pageIndex the page index.
	 * @param pageSize the page size-
	 * @param queryType the query type.
	 * @param filters (optional) filter that will be applied.
	 * @param request the HTTP request.
	 * @param orderBy the order by criteria.
	 * @return a valid SOLR query.
	 */
	protected SolrQuery createSolrQuery(
			final String queryString, 
			final int pageIndex, 
			final int pageSize, 
			final String queryType, 
			final String [] filters, 
			final HttpServletRequest request,
			final String orderBy,
			final boolean enableCollapsing)
	{
		final SolrQuery query = new SolrQuery();
		query.setRequestHandler(queryType);
		
		final StringBuilder hrQueryBuilder = new StringBuilder();
		String solrQuery = null;
		if (ISolrConstants.ADVANCED_SEARCH_QUERY_TYPE_NAME.equals(queryType))
		{
			solrQuery = buildAdvancedQuery(queryString, request, hrQueryBuilder);
		} else 
		{
			if (!ISolrConstants.ALL.equals(queryString))
			{
				solrQuery = escapeQueryChars(queryString.replace("-", " "));
			} else
			{
				solrQuery = queryString;
			}
		}

		if (ISolrConstants.ADVANCED_SEARCH_QUERY_TYPE_NAME.equals(queryType))
		{
			String hrq = hrQueryBuilder.toString();
			query.add("hrq", hrq);
			request.setAttribute("hrq", hrq);
		}
		
		String orderByFragment = orderByAttributes.get(orderBy);
		if (isNotNullOrEmptyString(orderByFragment))
		{
			query.add("sort", orderByFragment);
		}
		
		query.setQuery(solrQuery.isEmpty() ? "*:*" : solrQuery);

		if (filters != null) 
		{ 
			query.addFilterQuery(filters);
		}
		
		HttpSession session = request.getSession(false);
		if (session != null)
		{
			//avoid adding collection filter to authSearch for imss
			Toolbox toolbox = (org.apache.velocity.tools.Toolbox) getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
			String instituition = (String) toolbox.get("institution");
			if (request.getRequestURI() != null && request.getRequestURI().contains("authSearch") && "IMSS".equals(instituition)) {
				//don't add any collection filter!
			}
			else {
				if ("IMSS".equals(instituition)) {
					String collectionCode = (String) session.getAttribute("collection");
					if (isNotNullOrEmptyString(collectionCode) && !"0".equals(collectionCode)){
						query.addFilterQuery("collection_code:" + collectionCode);
					}
					else {
						String logicalView = (String) session.getAttribute(HttpAttribute.LOGICAL_VIEW);
						if (isNotNullOrEmptyString(logicalView))
						{
							query.addFilterQuery("catalog_source:" + logicalView);
						}	
					}
				}
				else {
					String logicalView = (String) session.getAttribute(HttpAttribute.LOGICAL_VIEW);
					if (isNotNullOrEmptyString(logicalView))
					{
						query.addFilterQuery("catalog_source:" + logicalView);
					}	
				}
					
			}
		}	
		

		//bug 3118 (Trento):Aggiunto filtro per formato e-book per query da homepage su anteprima novità di narrativa
		final String [] formatFilters = (String []) request.getAttribute("formatFilter");
		if (formatFilters != null)
		{
			for (final String filter : formatFilters) 
			{
				query.addFilterQuery(filter);				
			}
		}
		
		
		if (enableCollapsing)
		{
			query.set("group", true);
			query.add("group.field", "group_id");
			query.set("group.ngroups", true);
			query.set("group.limit",30);
		}
		
		String sfield = request.getParameter(HttpParameter.GEO_SFIELD);
		query.add(HttpParameter.GEO_SFIELD, sfield);
		
		String pt = request.getParameter(HttpParameter.GEO_PT);
		String d = request.getParameter(HttpParameter.GEO_DISTANCE);
		
		if (isNotNullOrEmptyString(pt)){
			query.add(HttpParameter.GEO_PT, pt);
			if (isNotNullOrEmptyString(d)){
				query.add(HttpParameter.GEO_DISTANCE, d);	
			}
			else query.add(HttpParameter.GEO_DISTANCE, "1000");	 
		}
		
		
		query.setStart(pageIndex * pageSize);
		query.setRows(pageSize);
		return query;
	}
	
	public String booleanOperatorsTokens() {
		return "ORNOT|ANDNOT|OR|AND";
	}
	
	public String advancedSearchQueryParser() {
		return "edismax";
	}
	
	/**
	 * Builds the advanced query.
	 * 
	 * @param q the incoming query string.
	 * @param request the HTTP request.
	 * @return the complete advanced query.
	 */
	private String buildAdvancedQuery(final String q, final HttpServletRequest request, StringBuilder humanReadableQueryBuilder)
	{		
		ConfigurationTool configuration = getConfiguration(request);
		Locale preferredLocale = getVisit(request).getPreferredLocale();
		ResourceBundle messages = ResourceBundle.getBundle("resources", preferredLocale);
		
		ResourceBundle add_messages = ResourceBundle.getBundle("additional_resources", preferredLocale);
		
		String queryString = q;
		StringBuilder builder = new StringBuilder();
		
		String [] tokens = queryString.split(booleanOperatorsTokens());
		
		//Bug 6378: you must order element before a replace, to avoid multiple replace on a single itaration
		Arrays.sort(tokens, new java.util.Comparator<String>() {
		    @Override
		    public int compare(String s1, String s2) {
		        // TODO: Argument validation (nullity, length)
		        return s2.length() - s1.length();// comparision
		    }
		});
		
		for (String token : tokens)
		{
			queryString = queryString.replace(token, " ");
		}
		
		queryString = queryString.trim();
		String [] operators = isNotNullOrEmptyString(queryString) ? queryString.trim().split(" +") : null;

		int index = 0;
		for (String token : tokens)
		{
			int indexOfColon = token.indexOf(IConstants.COLON);
			if (indexOfColon != -1)
			{			
				String fieldName = token.substring(0, indexOfColon).trim();
				String fieldValue = token.substring(indexOfColon + 1).trim();

				String fv = fieldValue;
				if (add_messages!=null && add_messages.containsKey(fieldName)) {
					if (fieldValue.startsWith("-")) {
						humanReadableQueryBuilder.append(" " + messages.getObject("nc") + " ");
						fv = fieldValue.substring(1);
					}
					humanReadableQueryBuilder.append(fv).append(" (").append(add_messages.getObject(fieldName.equals("*")? "all_results" : fieldName)).append(")");
				}
				else {
					if (fieldValue.startsWith("-")) {
						humanReadableQueryBuilder.append(" " + messages.getObject("nc") + " ");
						fv = fieldValue.substring(1);
					}
					humanReadableQueryBuilder.append(fv).append(" (").append(messages.getObject(fieldName.equals("*")? "all_results" : fieldName)).append(")");
				}

				if (ISolrConstants.ALL.equals(queryString)) {
                    builder.append(queryString);
                } else {
                	boolean negation=false;
                	if(fieldValue.startsWith("-")) {
                		negation=true;
                    	fieldValue = fieldValue.replaceFirst("-", "");
                	}
                    builder.append("_query_:\"{!" + advancedSearchQueryParser() + " qf=$" + fieldName + "_query pf=$" + fieldName + "_phrase " + (fieldValue.startsWith("-") ? "mm=100" : "" + "") + "}");
                    if (fieldValue.startsWith("-") && !fieldValue.contains("\"")) {
                    	if(fieldName.equals("classification")) 
                    		fieldValue = fieldValue.replace(" ", " +").replace("(", "(\"+").replace(")", "\")");
                    	else
                    		fieldValue = fieldValue.replace(" ", " +").replace("(", "(+");
                    }
                    boolean isDoubleQuoted = fieldValue.indexOf("\"") != -1;
                    if (isDoubleQuoted) {
                        fieldValue = fieldValue.replaceAll("\\\"", "\\\\\"");
                        if(negation)
                        	builder.append("-");
                        builder.append(fieldValue);
                    } else {
                        boolean needsDoubleQuotes = configuration.isQuotedMetaAttribute(fieldName) && !fieldValue.startsWith("\"");
                        if (needsDoubleQuotes) {
                        	if(negation)
                        		builder.append("-").append("\\\"").append(fieldValue.replaceAll(" ", "")).append("\\\"");
                        	else
                        		builder.append("\\\"").append(fieldValue.replaceAll(" ", "")).append("\\\"");
                        } else {
                        	if(negation)
                            	builder.append("-");
                            builder.append(fieldValue);
                        }
                    }

                    builder.append("\"");
                    if (operators != null && operators.length > 0 && index < operators.length) {
                        String operator = operators[index].trim();
                        builder.append(" ").append(operatorMappings.get(operator));
                        if (add_messages != null && add_messages.containsKey(operator)) {
                            humanReadableQueryBuilder.append(" ").append(add_messages.getString(operator)).append(" ");
                        } else {
                            humanReadableQueryBuilder.append(" ").append(messages.getString(operator)).append(" ");
                        }
                        index++;
                    }
                }
			}
		}
		return builder.toString();
	}
	
	/**
	 * Returns the name / title that will be assigned to the current tab.
	 * 
	 * @param query the issued query.
	 * @param locale the user preferred locale.
	 * @return the name / title that will be assigned to the current tab.
	 */
	protected String getTabName(final String query, final Locale locale)
	{
		ResourceBundle messageBundle = ResourceBundle.getBundle(IConstants.DEFAULT_MESSAGE_BUNDLE_NAME, locale);
		if (ISolrConstants.ALL.equals(query))
		{
			return messageBundle.getString("all_results");
		} else if (query.indexOf(":") != -1)
		{
			return messageBundle.getString("advanced_search");
		}
		return query;
	}
	
	private String getRequestUrl(HttpServletRequest request) 
	{
		StringBuffer result = request.getRequestURL();
		String queryString = request.getQueryString(); 
		if (queryString != null) 
		{
	        result.append("?").append(queryString);
	    }
	    return result.toString();
	}
	
	protected int getDistance(final String distance)
	{
		
	  return distance != null ? Integer.parseInt(distance) : 0;
		
	}
	
	protected List<Double> getLatLon(final String pt)
	{
		List<Double> latLon = null;
		if(pt!=null){
			String[] latLonArray = pt.split(",");
			latLon = new ArrayList<Double>();
			for (String s : latLonArray) {
				latLon.add(Double.parseDouble(s.trim()));
			}
		}
		return latLon;
	}
	
	public String escapeQueryChars(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '('
					|| c == ')' || c == ':' || c == '^' || c == '[' || c == ']'
					|| c == '*' || c == '?' 
					|| c == '{' || c == '}' || c == '~' || c == '|' || c == '&'
					|| c == ';') {
				sb.append('\\');
			}

			if (c == '\"' && i != 0 && i != (s.length() - 1)) {
				sb.append('\\');
			}

			sb.append(c);
		}

		return sb.toString();
	}
	
	private String[] removeFilterIndex(String filter_name, String [] array) {	
		if (array == null) return array;
		List<String> newArray = new ArrayList<String>();
		for (int i = 0; i < array.length; i ++) {
			if (array[i] !=null && !array[i].startsWith(filter_name)){
				newArray.add(array[i]);
			}
		}
		return newArray.toArray(new String [newArray.size()]);		
	}
	
	private int getFilterIndex(String filter_name, String [] array) {			
		if (array == null) return -1;
		for (int i = 0; i < array.length; i ++) {
			if (array[i] != null && array[i].startsWith((filter_name))){
				return i;
			}
		}
		return -1;
	}
}