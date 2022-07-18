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
package com.atc.osee.web.sbn;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.yaz4j.exception.ZoomException;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.model.Visit;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.LicenseTool;




public class SBNSearchServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 1L;
	
	private final Map<String, String> indexes = new HashMap<String, String>();
	{	
		indexes.put("any_bc", "any");
		indexes.put("title_bc", "title");
		indexes.put("sub_bc", "subject");		
		indexes.put("author_bc", "name");	
	}
	
	

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		LicenseTool license = getLicense(request);
		if (license.isFederatedSearchEnabled())
		{
//			setSessionAttribute(request, HttpAttribute.OG_CONTEXT, "federated");		
//			setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "home");
//			setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "metasearch");

			try
			{
				String query = request.getParameter(HttpParameter.QUERY);
				String indexName = indexes.get(request.getParameter(HttpParameter.QUERY_TYPE));
				
				if (indexName == null) 
				{ 
					indexName = "any";
				}
				
				
			
				String callForNumResult = request.getParameter("cforres");
				String queryForSearch = elaborateQueryForSearch(query);
				
				if (isNotNullOrEmptyString(callForNumResult)){
				
					try{
						
						QueryResponseSbn resp = YAZEngine.search("sbn", indexName, queryForSearch,"title");
						
						response.setCharacterEncoding("UTF-8");
						response.setContentType("application/json");
						StringBuilder builder = new StringBuilder("{\"sbnResult\":").append(resp.getNumFound()).append("}");
						response.getWriter().println(builder.toString());
						return;
					
					} catch (ZoomException e) {
						
						response.setCharacterEncoding("UTF-8");
						response.setContentType("application/json");
						StringBuilder builder = new StringBuilder("{\"sbnResult\":").append(String.valueOf(0)).append("}");
						response.getWriter().println(builder.toString());
						return;
							
					}
					
					
				}else{
	
					Visit visit = getVisit(request);
					SearchTab currentTab = null;
					
					String tab = request.getParameter(HttpParameter.TAB_ID);
					if (isNotNullOrEmptyString(tab))
					{
						currentTab =  visit.getSearchExperience().getTab(Integer.parseInt(tab));
						
					}
					else 
					{
							currentTab = visit.getSearchExperience().getCurrentTab();
							if (!currentTab.isEmpty())
							{
								List<SearchTab> tabs = visit.getSearchExperience().getTabs();
								boolean found = false;
								for (SearchTab searchTab : tabs)
								{
									if (searchTab != currentTab && searchTab.isExternalSbnSearchTab())
									{
										currentTab = searchTab;
										found = true;
										setRequestAttribute(request, HttpParameter.TAB_ID, currentTab.getId());
											break;
										}
									}
									if (!found)
									{
										currentTab = visit.getSearchExperience().addNewTab();
									}
							}
					}
					
					if(!currentTab.isNew()){
						int indFrom = currentTab.getQueryParameters().indexOf("q=");
						int indEnd = currentTab.getQueryParameters().indexOf("&");
						//Is better a fix test instead of the search because sometime the 'back' of
						//browser goes broken with code that is under here.
						//String qTab = currentTab.getQueryParameters().substring(indFrom+2, indEnd);
						String qTab = "SBN Tab";
						String queryEscape = qTab.replaceAll("\\+", " ");
						if(!queryEscape.equalsIgnoreCase(query)){
							currentTab.clearSelection();
						}
					}
					
					
					
					QueryResponseSbn resp = YAZEngine.search("sbn", indexName, queryForSearch,"title");
					
					int pageIndex = 1;
					
					String numPage = request.getParameter(HttpParameter.PAGE_INDEX);	
					if(numPage!=null){
						pageIndex= Integer.parseInt(numPage);
					}
					
					int pageSize = 10;
					String psize = request.getParameter(HttpParameter.PAGE_SIZE);
					if(psize!=null){
						pageSize = Integer.parseInt(psize);
					}
					
					
					resp.fetchPage(pageIndex, pageSize);
					
					String queryString = request.getQueryString();
					if(numPage!=null){
						int ind = queryString.indexOf("&p");
						if(ind != -1){
							queryString = queryString.substring(0, ind);
						}
					}
					
					currentTab.setForeignDataSBN(query, visit.getPreferredLocale());
					String queryParameters = queryString 
							+ (queryString.indexOf("&" + HttpParameter.TAB_ID + "=") == -1 
							? "&" + HttpParameter.TAB_ID + "=" + currentTab.getId() 
							: "");
					
					currentTab.setQueryParameters(queryParameters);
					
					currentTab.setSbnResponse(resp);
					
					visit.getSearchHistory().addFederatedSearchEntry(
							query, 
							request.getParameter(HttpParameter.QUERY_TYPE), 
							-1, 
							getSortCriteria(request), 
							getRequestUrl(request), 
							request.getParameterValues(HttpParameter.FILTER_BY));
					
					
					forwardTo(request, response, "/hits_ext_sbn.vm", "three_columns_ext_sbn.vm");
					
				}
				
			
			} catch (ZoomException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					
			} catch (TransformerException e1) {
				
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				
			} 
		} else 
		{
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
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
	
	private String getSortCriteria(HttpServletRequest request)
	{
		String criteria = request.getParameter(HttpParameter.ORDER_BY);
		if (isNotNullOrEmptyString(criteria))
		{
			return criteria;
		} else 
		{
			ConfigurationTool configuration = getConfiguration(request);
			return configuration.getDefaultOrderByCriteria();
		}
	}	
	
	private String elaborateQueryForSearch(String queryToEleaborate){
		
		String queryToReturn = "";
		
		String[] terms = queryToEleaborate.split(" ");
		
		if(terms.length > 1){
			for (int i = 0; i< terms.length; i++){
				
				if(i==0){
					queryToReturn = "\"";
				}
				if(i==terms.length -1){
					queryToReturn=queryToReturn+terms[i]+"\"";
				}else{
					queryToReturn=queryToReturn+terms[i]+" ";
				}
			}
			
		return queryToReturn;
			
		}else{
			
			return queryToEleaborate;
		}
	}
}