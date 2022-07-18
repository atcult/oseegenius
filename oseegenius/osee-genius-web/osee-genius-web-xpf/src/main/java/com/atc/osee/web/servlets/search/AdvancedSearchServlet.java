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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.log.MessageCatalog;
import com.atc.osee.web.model.AdvancedSearchExperience;
import com.atc.osee.web.model.AdvancedSearchField;
import com.atc.osee.web.model.LimitFacet;
import com.atc.osee.web.model.LimitFacetEntry;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * Advanced Search Web Controller.
 * Used for handling advanced search operations.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class AdvancedSearchServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 7134243549068671254L;
	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchServlet.class);

	private static final String ADD_SEARCH_FIELD_ACTION_PARAMETER_VALUE = "add";
	private static final String REMOVE_SEARCH_FIELD_ACTION_PARAMETER_VALUE = "remove";
	private static final String RESET_ACTION_PARAMETER_VALUE = "reset";
	private static final String REMOVE_LIB_FILTER_ACTION_PARAMETER_VALUE = "filterLibRemove";
	private static final String REMOVE_BRANCH_FILTER_ACTION_PARAMETER_VALUE = "filterBranchRemove";
	
	/**
	 * Command interface for advanced search actions.
	 * 
	 * @author Andrea Gazzarini
	 * @since 1.0
	 */
	private interface AdvancedSearchAction
	{
		/**
		 * Executes a given action.
		 *  
		 * @param request the http request.
		 * @param model the advanced search model.
		 */
		void execute(HttpServletRequest request, AdvancedSearchExperience model);
	}
	
	/**
	 * Adds a new search field.
	 */
	private AdvancedSearchAction addNewSearchField = new AdvancedSearchAction() 
	{	
		@Override
		public void execute(
				final HttpServletRequest request, 
				final AdvancedSearchExperience model) 
		{
			model.addSearchField();
		}
	};
	
	/**
	 * Removes a branch filter.
	 */
	private AdvancedSearchAction removeLibraryFilter = new AdvancedSearchAction() 
	{	
		@Override
		public void execute(
				final HttpServletRequest request, 
				final AdvancedSearchExperience model) 
		{
			model.getLibraryConstraints().uncheck(request.getParameter(HttpParameter.FILTER));
		}
	};
	
	/**
	 * Removes a branch filter.
	 */
	private AdvancedSearchAction removeBranchFilter = new AdvancedSearchAction() 
	{	
		@Override
		public void execute(
				final HttpServletRequest request, 
				final AdvancedSearchExperience model) 
		{
			model.getBranchConstraints().uncheck(request.getParameter(HttpParameter.FILTER));
		}
	};	

	/**
	 * Resets the advanced form.
	 */
	private AdvancedSearchAction reset = new AdvancedSearchAction() 
	{	
		@Override
		public void execute(
				final HttpServletRequest request, 
				final AdvancedSearchExperience model) 
		{
			model.resetToDefault();
			model.prepare(getConfiguration(request), getSearchEngine(request), request);
		}
	};	

	/**
	 * Removes a search field.
	 */
	private AdvancedSearchAction removeSearchField = new AdvancedSearchAction() 
	{	
		@Override
		public void execute(final HttpServletRequest request, final AdvancedSearchExperience model) 
		{
			String fieldIndexAsString = request.getParameter(HttpParameter.INDEX);
			try
			{
				model.removeSearchField(Integer.parseInt(fieldIndexAsString));
			} catch (Exception exception)
			{
				LOGGER.error(
						String.format(
								MessageCatalog._100013_ADVANCED_SEARCH_FIELD_REMOVAL_FAILURE,
								fieldIndexAsString),
						exception);
			}
		}
	};

	/**
	 * Null Object action.
	 * As the name suggests, it does nothing.
	 */
	private AdvancedSearchAction doNothing = new AdvancedSearchAction() 
	{	
		@Override
		public void execute(final HttpServletRequest request, final AdvancedSearchExperience model) 
		{
			// Nothing to be done here...
		}
	};
	
	// Action registry
	private Map<String, AdvancedSearchAction> actions = new HashMap<String, AdvancedSearchAction>(2);
	{
		actions.put(ADD_SEARCH_FIELD_ACTION_PARAMETER_VALUE, addNewSearchField);
		actions.put(REMOVE_SEARCH_FIELD_ACTION_PARAMETER_VALUE, removeSearchField);		
		actions.put(RESET_ACTION_PARAMETER_VALUE, reset);				
		actions.put(REMOVE_LIB_FILTER_ACTION_PARAMETER_VALUE, removeLibraryFilter);	
		actions.put(REMOVE_BRANCH_FILTER_ACTION_PARAMETER_VALUE, removeBranchFilter);	
		actions.put(null, doNothing);		
	}
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		setRequestAttribute(request, HttpAttribute.HOME_LOGICAL_NAME, "home");
		setRequestAttribute(request, HttpAttribute.CURRENT_SEARCHER, "search");
		setRequestAttribute(request, HttpAttribute.SUGGESTOR, "suggest");
		setRequestAttribute(request, HttpAttribute.DONT_SHOW_PERSPECTIVE_BUTTON, true);
		setRequestAttribute(request, HttpAttribute.DONT_SHOW_ADVANCED_SEARCH_BUTTON, true);
		
		
		AdvancedSearchExperience model = getAdvancedSearchExperience(request);
		model.prepare(getConfiguration(request), getSearchEngine(request), request);

		String actionParameterValue = request.getParameter(HttpParameter.ACTION);
		AdvancedSearchAction action = actions.get(actionParameterValue);
		
		try 
		{
			action.execute(request, model);
		} catch (Exception exception)
		{
			// Ignore: this block is here just to avoid annoying exceptions
			// caused by idiots.
		}

		if (isNullOrEmptyString(actionParameterValue) || !actionParameterValue.startsWith("filter"))
		{
			request.setAttribute("specificServletTitle", "advanced_search_link"); 
			forwardTo(request, response, "/advanced_search.vm", "advanced_search_layout.vm");
		}
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		request.setCharacterEncoding("UTF-8");
		
		AdvancedSearchExperience model = getAdvancedSearchExperience(request);
		model.setOrderBy(request.getParameter(HttpParameter.ADVANCED_SEARCH_ORDER_BY));
		 
		model.setStartYear(request.getParameter("start_year"));
		model.setEndYear(request.getParameter("end_year"));
				
		try 
		{
			model.setPageSize(Integer.parseInt(request.getParameter(HttpParameter.ADVANCED_SEARCH_PAGE_SIZE)));
		} catch (Exception exception) {
			model.setPageSize(getConfiguration(request).getDefaultPageSize());
			LOGGER.error(
					String.format(
							MessageCatalog._100015_INVALID_PAGE_SIZE,
							request.getParameter(HttpParameter.ADVANCED_SEARCH_PAGE_SIZE)),
					exception);
		}		
		
		StringBuilder advancedQueryBuilder = new StringBuilder()
			.append("&s=").append(model.getPageSize())
			.append("&o=").append(model.getOrderBy())
			.append("&h=adv");

		if(request.getParameter("collection_data") != null) {
			advancedQueryBuilder.append("&collection_data=").append(request.getParameter("collection_data"));
		}

		advancedQueryBuilder.append("&q=");				
			
		
		List<AdvancedSearchField> searchFields = model.getSearchFields();
		int howManySearchFields = searchFields.size();
		
		AdvancedSearchField lastValidField = null;	
		int lastIndexValid = 0;
		for (int i = 0; i < howManySearchFields; i++) {
			AdvancedSearchField field = searchFields.get(i);
			if ("inventory".equals(request.getParameter(HttpParameter.ADVANCED_SEARCH_WHERE_SUFFIX + i))) {
				field.setWhat((request.getParameter(HttpParameter.ADVANCED_SEARCH_WHAT_SUFFIX + i)));
			}
			else {
				field.setWhat((request.getParameter(HttpParameter.ADVANCED_SEARCH_WHAT_SUFFIX + i)).toLowerCase());
			}
			field.setWhere(request.getParameter(HttpParameter.ADVANCED_SEARCH_WHERE_SUFFIX + i));
			if (request.getParameter(HttpParameter.ADVANCED_SEARCH_BOOLEAN_OPERATOR_SUFFIX + i) != null ) {
				field.setBooleanOperator(request.getParameter(HttpParameter.ADVANCED_SEARCH_BOOLEAN_OPERATOR_SUFFIX + i));
			}
			if(field.appendSubQuery(advancedQueryBuilder)) {
				lastValidField = field;
				lastIndexValid = i+1;
			}
			else {
				lastValidField = lastValidField;	
			}
		}
		
		if(lastIndexValid > 0) {
			request.getSession().setAttribute("advancedSearchedField", lastIndexValid);
		}

		if (lastValidField != null)
		{
			String booleanOperatorToRemove = lastValidField.getBooleanOperator();
			if (isNotNullOrEmptyString(booleanOperatorToRemove))
			{
				int currentQueryLength = advancedQueryBuilder.length();
				
				advancedQueryBuilder.replace((currentQueryLength - (booleanOperatorToRemove.length() + 1)), currentQueryLength, "");
			}
		} else 
		{
			advancedQueryBuilder.append("*:*");
		}
		
		// Date range search...only if at least one of two dates is not null
		if (isNotNullOrEmptyString(model.getStartYear()) || isNotNullOrEmptyString(model.getEndYear()))
		{
			advancedQueryBuilder.append("&f=")
				//.append("publication_date:[")
				.append("publication_date:%5B")
				.append(isNotNullOrEmptyString(model.getStartYear()) ? model.getStartYear() : "*")
				.append(" TO ")
				.append(isNotNullOrEmptyString(model.getEndYear()) ? model.getEndYear() : "*")
				//.append("]");
				.append("%5D");
		}
		
		addLibraryFilters(advancedQueryBuilder, model);
		addLimitFilters(advancedQueryBuilder, request, model);
		additionalPinnedFilters(request, getSearchExperience(request).getCurrentTab(), advancedQueryBuilder);
		
		//response.sendRedirect("search?" + advancedQueryBuilder.toString());
		
		String destination = request.getParameter("destination");
		if(isNullOrEmptyString(destination)){
			response.sendRedirect(getSearchServletName() + "?" + advancedQueryBuilder.toString());
		}else{
			response.sendRedirect(destination);
		}
		
	
	}	
	
	
	/**
	 * Sets a request attibrute containing additional pinned filters.
	 * 
	 * @param request the {@link HttpServletRequest}.
	 * @param tab the current {@link SearchTab}.
	 */
	private void additionalPinnedFilters(final HttpServletRequest request, final SearchTab tab, StringBuilder builder)
	{
		if (getLicense(request).isPinFiltersEnabled() && tab.hasPinnedFilters())
		{
			for (String filter : tab.getPinnedFilters())
			{
				builder.append("&f=").append(filter);
			}
		}
	}
	
	/**
	 * Returns the (user) advanced search model.
	 * 
	 * @param request the http request.
	 * @return the (user) advanced search model.
	 */
	private AdvancedSearchExperience getAdvancedSearchExperience(final HttpServletRequest request)
	{
		return getVisit(request).getAdvancedSearchExperience();
	}	
	
	/**
	 * Adds a query constraint / filter.
	 * Note that at the moment this method is used only for library and branche filters.
	 * 
	 * @param advancedQueryBuilder the advanced query builder.
	 * @param limit the filter.
	 */

	private void addLibraryFilters(final StringBuilder advancedQueryBuilder, AdvancedSearchExperience model)
	{
		LimitFacet libraries = model.getLibraryConstraints();
		LimitFacet branches = model.getBranchConstraints();
		LimitFacet locations = model.getLocationsFilter();
		
		boolean isTheFirstCheckedValue = true;
		
		for (LimitFacetEntry entry : libraries.getValues())
		{
			if (entry.isChecked())
			{
				if (isTheFirstCheckedValue)
				{
					isTheFirstCheckedValue = false;
					advancedQueryBuilder.append("&f=");
				} else
				{
					advancedQueryBuilder.append(" OR ");						
				}
				
				advancedQueryBuilder
					.append(libraries.getName())
					.append(":\"")
					.append(entry.getName())
					.append("\"");
			}			
		}
		
		for (LimitFacetEntry entry : branches.getValues())
		{
			if (entry.isChecked())
			{
				if (isTheFirstCheckedValue)
				{
					isTheFirstCheckedValue = false;
					advancedQueryBuilder.append("&f=");
				} else
				{
					advancedQueryBuilder.append(" OR ");						
				}
				
				advancedQueryBuilder
					.append(branches.getName())
					.append(":\"")
					.append(entry.getName())
					.append("\"");
			}		
		}
		
		Map<String, List<String>> filterLocation = new HashMap<String, List<String>>();
		createStructForLocationsQuery(filterLocation, locations);
		
		if(!filterLocation.isEmpty()){
			
			Iterator it = filterLocation.entrySet().iterator();
			
			while (it.hasNext()) {
				
				Map.Entry entry = (Map.Entry)it.next();
				
					if (isTheFirstCheckedValue)
					{
						isTheFirstCheckedValue = false;
						advancedQueryBuilder.append("&f=(");
					} else
					{
						advancedQueryBuilder.append(" OR ( ");						
					}
					
					advancedQueryBuilder
						.append("branch")
						.append(":\"")
						.append(entry.getKey())
						.append("\"");
					
				
					advancedQueryBuilder.append(" AND ( ");
					for (int i=0; i< filterLocation.get(entry.getKey()).size(); i++)
						{
							if(i==0){
								advancedQueryBuilder.append(" location");
							}else{
								advancedQueryBuilder.append(" OR location");			
							}
							
							advancedQueryBuilder
							.append(":\"")
							.append(filterLocation.get(entry.getKey()).get(i))
							.append("\"");
						}
						advancedQueryBuilder.append(" ))");			
			}
		}

	}
	
	/**
	 * Adds constraints / filters.
	 * 
	 * @param advancedQueryBuilder the advanced query builder.
	 * @param request the HTTP request.
	 * @param model the {@link AdvancedSearchExperience} datamodel.
	 */
	protected void addLimitFilters(
			final StringBuilder advancedQueryBuilder, 
			final HttpServletRequest request, 
			final AdvancedSearchExperience model)
	{
		Map<String, LimitFacet> limits = model.getLimits();
		for (Map.Entry<String, LimitFacet> entry : limits.entrySet())
		{
			String name = entry.getKey();
			LimitFacet limit = entry.getValue();

			boolean isTheFirstCheckedEntryOfThisLimit = true;
			
			for (LimitFacetEntry limitEntry : limit.getValues())
			{
				/* bug 2359 tolto per bottone "altro" su filtri per tutte le installazioni*/
//				if (!limitEntry.isOutsider())
//				{
//					String enabled = request.getParameter(name + ":" + limitEntry.getName());
//					limitEntry.setChecked(HttpParameter.CHECK_BOX_TRUE.equals(enabled));
//				}				
				
				if (limitEntry.isChecked())
				{
					String filterDerivedByLimit = null;
					
					try 
					{
						filterDerivedByLimit = new StringBuilder()
							.append(name)
							.append(":\"")
							.append(URLEncoder.encode(limitEntry.getName(), "UTF-8"))
							.append("\"").toString();
					} catch (UnsupportedEncodingException exception)
					{
						// Ignore encoding
						filterDerivedByLimit = new StringBuilder()
						.append(name)
						.append(":\"")
						.append(limitEntry.getName())
						.append("\"").toString();						
					}

					if (advancedQueryBuilder.indexOf(filterDerivedByLimit) == -1)
					{
						if (isTheFirstCheckedEntryOfThisLimit)
						{
							isTheFirstCheckedEntryOfThisLimit = false;
							advancedQueryBuilder.append("&f=");
						} else
						{
							advancedQueryBuilder.append(" OR ");						
						}
						
						try 
						{
							advancedQueryBuilder
								.append(name)
								.append(":\"")
								.append(URLEncoder.encode(limitEntry.getName(), "UTF-8"))
								.append("\"");
						} catch (UnsupportedEncodingException exception)
						{
							// Ignore encoding
							advancedQueryBuilder
							.append(name)
							.append(":\"")
							.append(limitEntry.getName())
							.append("\"");						
						}
					}
				}
			}
		}		
	}
	
	public String getSearchServletName()
	{
		return "search";
	}
	
	private void createStructForLocationsQuery(Map<String, List<String>> filterLocation, LimitFacet locations){

		for (LimitFacetEntry entry : locations.getValues()){
			String branchName = null;
			String loc = null;
			if (entry.isChecked())
			{
				branchName = entry.getName().substring(0,entry.getName().indexOf("#"));
				loc =  entry.getName().substring(entry.getName().indexOf("#")+1);
				if(filterLocation.containsKey(branchName)){
					filterLocation.get(branchName).add(loc);
				} else {
					List<String> locs = new ArrayList<String>();
					locs.add(loc);
					filterLocation.put(branchName, locs);	
				}
						
			}
			
		}
	}
	
}