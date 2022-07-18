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
package com.atc.osee.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.log.MessageCatalog;
import com.atc.osee.web.tools.ConfigurationTool;

/**
 * Advanced Search domain object.
 * Basically it is a wrapper around the "advanced" search user experience, 
 * encapsulating all what the user defined, in terms of advanced search, 
 * in his session.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class AdvancedSearchExperience implements Serializable
{
	private static final long serialVersionUID = -7539925177424701333L;
	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchExperience.class);
	
	private String orderBy;
	private int pageSize;
	
	private String startYear;
	private String endYear;
	
	private Map<String, LimitFacet> limits = new HashMap<String, LimitFacet>();
	
	private LimitFacet branchConstraints = new LimitFacet("branch");
	private LimitFacet libraryConstraints = new LimitFacet("library");
	
	private List<AdvancedSearchField> fields = new ArrayList<AdvancedSearchField>();
	
	//oggetto utilizzato per memorizzare le location utilizzate
	private LimitFacet locationsFilter = new LimitFacet("location");
	
	/**
	 * State interface for advanced search model.
	 * 
	 * @author Andrea Gazzarini
	 * @since 1.0
	 */
	private interface State extends Serializable
	{
		/**
		 * Prepares the owing model.
		 * 
		 * @param configuration the OseeGenius -W- configuration.
		 * @param searchEngine an OseeGenius -S- reference.
		 * @param request the HTTP request.
		 */
		void prepare(ConfigurationTool configuration, ISearchEngine searchEngine, HttpServletRequest request);
	}
	
	/**
	 * This is the state of this object when no initialisation has been done.
	 */
	private final State notYetInitialised = new State() 
	{	
		private static final long serialVersionUID = 6005270401381198635L;

		@Override
		public void prepare(final ConfigurationTool configuration, final ISearchEngine searchEngine, final HttpServletRequest request) 
		{
			populateWithDefaultValues(configuration);
			populateLimits(searchEngine, request);
			currentState = initialised;
		}
		
		/**
		 * Populates this model with limit facets.
		 * 
		 * @param searchEngine the OseeGenius -S- reference.
		 */
		private void populateLimits(final ISearchEngine searchEngine, final HttpServletRequest request)
		{
			SolrQuery query = new SolrQuery(ISolrConstants.ALL);
			query.setQueryType(ISolrConstants.LIMITS_QUERY_TYPE_NAME);

			if (request.getSession(false) != null)
			{
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
			}
			
			try 
			{
				QueryResponse response = searchEngine.executeQuery(query);
				List<FacetField> facets = response.getFacetFields();
				
				for (FacetField facet : facets)
				{
					if (facet.getValueCount() != 0)
					{
						LimitFacet limitfacet = new LimitFacet(facet.getName());
						for (Count count : facet.getValues())
						{
							limitfacet.addEntry(count);
						}
						limits.put(limitfacet.getName(), limitfacet);
					}
				}
			} catch (SystemInternalFailureException exception)
			{
				LOGGER.error(MessageCatalog._100014_ADVANCED_SEARCH_MODEL_LIMITS_INIT_FAILURE);
			}
		}		
		
		/**
		 * Populates this domain object with default values.
		 * Default values usually comes from configuration.
		 * 
		 * @param configuration the configuration associated with this OseeGenius -W- instance.
		 */
		private void populateWithDefaultValues(final ConfigurationTool configuration)
		{
			for (int i = 0; i < configuration.getInitialNumberOfAdvancedSearchFields(); i++)
			{
				//fix problem when initial number of fields are bigger than indexes number
				if(i < configuration.getDefaultWhereValuesForAdvancedFields().length)
					fields.add(new AdvancedSearchField(i, configuration.getDefaultWhereValuesForAdvancedFields()[i], "AND"));
				else 
					fields.add(new AdvancedSearchField(i, configuration.getDefaultWhereValuesForAdvancedFields()[1], "AND"));
			}
			
			orderBy = configuration.getDefaultOrderByCriteria();
			pageSize = configuration.getDefaultPageSize();
			
			startYear = null;
			endYear = null;
		}
	};
	
	private State initialised = new State() 
	{	
		private static final long serialVersionUID = 1055282600719228222L;

		@Override
		public void prepare(final ConfigurationTool configuration, final ISearchEngine searchEngine,  final HttpServletRequest request) 
		{
			// Nothing to be done here...
		}
	};
	
	private State currentState = notYetInitialised;
	
	/**
	 * Prepares this model.
	 * 
	 * @param configuration the OseeGenius -W- configuration.
	 * @param searchEngine the OseeGenius -S- reference.
	 */
	public void prepare(final ConfigurationTool configuration, final ISearchEngine searchEngine,  final HttpServletRequest request)
	{
		currentState.prepare(configuration, searchEngine, request);
	}
			
	/**
	 * Returns the limit associated with the given name.
	 * 
	 * @param name the limit name.
	 * @return the limit associated with the given name.
	 */
	public LimitFacet getLimit(final String name)
	{
		return limits.get(name);
	}
	
	/**
	 * Returns all the limits associated with this search experience.
	 * 
	 * @return all the limits associated with this search experience.
	 */
	public Map<String, LimitFacet> getLimits()
	{
		return limits;
	}
		
	/**
	 * Returns the search fields of this domain experience.
	 * 
	 * @return the search fields of this domain experience.
	 */
	public List<AdvancedSearchField> getSearchFields()
	{
		return fields;
	}
	
	/**
	 * Returns the currently selected order-by criteria.
	 * 
	 * @return the currently selected order-by criteria.
	 */
	public String getOrderBy()
	{
		return orderBy;
	}

	/**
	 * Returns the currently selected page size.
	 * 
	 * @return the currently selected page size.
	 */
	public int getPageSize()
	{
		return pageSize;
	}

	/**
	 * Sets the page size of this search experience.
	 * 
	 * @param pageSize the page size of this search experience.
	 */
	public void setPageSize(final int pageSize)
	{
		this.pageSize = pageSize;
	}

	/**
	 * Sets the order-by criteria of this search experience.
	 * 
	 * @param orderBy the order-by criteria of this search experience.
	 */
	public void setOrderBy(final String orderBy)
	{
		this.orderBy = orderBy;
	}

	/**
	 * Adds a new search field.
	 */
	public void addSearchField()
	{
		fields.add(new AdvancedSearchField(
				fields.size(), 
				ISolrConstants.ANY_KEYWORD_SEARCH_QUERY_TYPE_NAME, 
				ISolrConstants.AND));
	}
	
	/**
	 * Removes the search field associated with the given index.
	 * 
	 * @param index the search field associated with the given index.
	 */
	public void removeSearchField(final int index)
	{
		for (Iterator<AdvancedSearchField> iterator = fields.iterator(); iterator.hasNext();)
		{
			AdvancedSearchField field = iterator.next();
			if (field.getIndex() == index)
			{
				iterator.remove();
			}
		}
	}
	
	/** 
	 * Resets this model.
	 */
	public void resetToDefault()
	{
		fields.clear();
		limits.clear();
		startYear = null;
		endYear = null;
		branchConstraints.getValues().clear();
		libraryConstraints.getValues().clear();
		locationsFilter.getValues().clear();
		currentState = notYetInitialised;
	}

	/**
	 * Returns the start (publication year) constraint.
	 * 
	 * @return the start (publication year) constraint.
	 */	public String getStartYear() 
	{
		return startYear;
	}

	/**
	 * Sets the start (publication year) constraint.
	 * 
	 * @param startYear the start (publication year) constraint.
	 */
	public void setStartYear(final String startYear) 
	{
		this.startYear = (isValidYearForRangeSearch(startYear)) 
				? startYear
				: null;
	}

	public String getEndYear() 
	{
		return endYear;
	}

	/**
	 * Sets the end (publication year) constraint.
	 * 
	 * @param endYear the end (publication year) constraint.
	 */
	public void setEndYear(final String endYear) 
	{
		this.endYear = (isValidYearForRangeSearch(endYear)) 
				? endYear
				: null;
	}
	
	/**
	 * Returns the limit branches.
	 * 
	 * @return the limit branches.
	 */
	public LimitFacet getBranchConstraints()
	{
		return branchConstraints;
	}
	
	/**
	 * Returns the limit libraies.
	 * 
	 * @return the limit libraries.
	 */
	public LimitFacet getLibraryConstraints()
	{
		return libraryConstraints;
	}
	
	
	
	/**
	 * @return the locationsFilter
	 */
	public LimitFacet getLocationsFilter() {
		return locationsFilter;
	}

	/**
	 * Checks whetever the inserted publication year is valid.
	 * 
	 * @param yearAsString the year (start or end).
	 * @return true if the given value is a valid year (i.e. yyyy)
	 */
	private boolean isValidYearForRangeSearch(final String yearAsString)
	{
		if (yearAsString == null || yearAsString.trim().length() != 4)
		{
			return false;
		}
		
		try 
		{
			Integer.parseInt(yearAsString);
			return true;
		} catch (Exception ignore) 
		{
			return false;
		}
	}
}