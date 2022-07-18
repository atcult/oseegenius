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
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.atc.osee.web.IConstants;

/**
 * OseeGenius browse tab.
 * 
 * @author agazzarini
 * @author ggazzarini
 * @since 1.0
 */
public class BrowseTab implements Serializable
{
	private static final long serialVersionUID = 6396393863855402941L;

	private int id;
	private String queryParameters;
	private boolean isNew = true;
	private int resultsPerPage = 10;
	
	private Locale locale;
	
	private SolrQuery query;
	private QueryResponse response;
				
	private String from;
	private String indexKey;

	private final String tabIdEquals = "t=";
	
	/**
	 * Builds and initializes a new search tab.
	 * 
	 * @param locale the currently user preferred locale.
	 * @param index the index of this tab.
	 */
	public BrowseTab(final Locale locale, final int index)
	{
		this.id = index;
		queryParameters = tabIdEquals + id;
		this.locale = locale;
	}

	/**
	 * Returns the tab identifier.
	 * 
	 * @return the tab identifier.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Returns the title of this tab.
	 * 
	 * @return the title of this tab.
	 */
	public String getTitle()
	{
		ResourceBundle bundle = ResourceBundle.getBundle(IConstants.DEFAULT_MESSAGE_BUNDLE_NAME, locale);
		if (isNew)
		{
			return bundle.getString("defaultTitleTab");
		}
		StringBuilder builder = new StringBuilder(bundle.getString(indexKey));
		if (from != null && from.trim().length() != 0)
		{
			String text = from;
			if (text.length() > 10) text = text.substring(0,10) + "...";
			builder.append(" (").append(text).append(")");
		}
		return builder.toString();
	}

	/**
	 * Returns the number of result per page of this tab.
	 * 
	 * @return the number of result per page of this tab.
	 */
	public int getResultsPerPage()
	{
		return resultsPerPage;
	}

	/**
	 * Returns the SOLR response wrapped by this tab.
	 * 
	 * @return the SOLR response wrapped by this tab.
	 */
	public QueryResponse getResponse()
	{
		return response;
	}

	/**
	 * Sets the number of result per page onto this tab.
	 * 
	 * @param resultsPerPage the number of result per page onto this tab.
	 */
	public void setResultsPerPage(final int resultsPerPage)
	{
		this.resultsPerPage = resultsPerPage;		
	}
	
	public void clear()
	{
		response = null;
		queryParameters = null;
		isNew = true;
		resultsPerPage = 10;
		
		query = null;
		response = null;
					
		from = null;
		indexKey = null;
	}
	
	/**
	 * Sets the data of this tab.
	 * 
	 * @param response the data of this tab.
	 */
	public void setData(final QueryResponse response)
	{
		isNew = false;
		this.response = response;
	}
	
	/**
	 * @return the from
	 */
	public String getFrom()
	{
		return from;
	}

	/**
	 * @return the indexKey
	 */
	public String getIndexKey()
	{
		return indexKey;
	}

	/**
	 * Sets the browsing data of this tab.
	 * 
	 * @param from the from text.
	 * @param indexKey the target index code.
	 * @param query the SOLR query. 
	 * @param response the SOLR response.
	 * @param locale the user current locale.
	 */
	public void setData(final String from, final String indexKey, final SolrQuery query, final QueryResponse response, final Locale locale)
	{
		this.locale = locale;
		this.from = from;
		this.indexKey = indexKey;
		
		this.query = query;

		isNew = false;
		this.response = response;
	}
	
	@Override
	public String toString()
	{
		return getTitle();
	}
		
	/**
	 * Returns true if this tab never held data.
	 * 
	 * @return true if this tab never held data.
	 */
	public boolean isNew()
	{
		return isNew;
	}
	
	/**
	 * Returns true if this tab contains no data.
	 * 
	 * @return true if this tab contains no data.
	 */
	public boolean isEmpty()
	{
		return isNew || response == null || response.getResponse().getAll("heading").isEmpty();
	}

	/**
	 * Returns the originating query parameters of this tab.
	 * 
	 * @return the originating query parameters of this tab.
	 */
	public String getQueryParameters()
	{
		return queryParameters;
	}

	/**
	 * Injects onto this tab the originating query parameters.
	 * Those parameters are needed for rebuiling the search state of the 
	 * tab.
	 * 
	 *  @param queryParameters the originating query parameters.
	 */
	public void setQueryParameters(final String queryParameters)
	{
		this.queryParameters = queryParameters;
		if (queryParameters.indexOf(tabIdEquals) == -1)
		{
			this.queryParameters += "&" + tabIdEquals + id;
		}
	}

	/**
	 * Returns the query associated with this tab.
	 * 
	 * @return the query associated with this tab.
	 */
	public SolrQuery getQuery()
	{
		return query;
	}
			
	@Override
	public int hashCode() 
	{
		return id;
	}
	
	@Override
	public boolean equals(final Object obj) 
	{
		try 
		{
			BrowseTab tab = (BrowseTab) obj;
			return id == tab.id;
		} catch (Exception exception) 
		{
			return false;
		}
	}
}