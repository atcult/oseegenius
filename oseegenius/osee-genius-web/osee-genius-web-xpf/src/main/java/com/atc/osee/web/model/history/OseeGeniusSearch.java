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
package com.atc.osee.web.model.history;

import java.util.Arrays;
import java.util.List;

/**
 * A search executed within OseeGenius.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class OseeGeniusSearch extends HistoryEntry
{	
	private static final long serialVersionUID = 8457364227484069834L;

	private final String query;
	private final String targetMetaAttribute;
	private final long howManyResults;
	private final String orderByCriteria;
	
	private String [] filters;
	
	private final String searchUri;
	
	private final List<Double> latLon;
	private final int distance;
	
	private final int hashCode;
	
	/**
	 * Builds a new simple search history entry with the given data. 
	 * 
	 * @param query the user entered query string.
	 * @param targetMetaAttribute the target attribute.
	 * @param howManyResults how many results for this search.
	 * @param orderByCriteria the order by criteria.
	 * @param searchUri the complete search URI.
	 * @param filters the filter queries applied to this search.
	 */
	public OseeGeniusSearch(
			final int id,
			final String query, 
			final String targetMetaAttribute, 
			final long howManyResults, 
			final String orderByCriteria, 
			final String searchUri,
			final String [] filters,
			final List<Double> latLon,
			final int distance) 
	{
		super(id);
		this.query = query;
		this.targetMetaAttribute = targetMetaAttribute;
		this.howManyResults = howManyResults;
		this.orderByCriteria = orderByCriteria != null ? orderByCriteria : "score";
		this.searchUri = searchUri;
		this.filters = filters;
		this.latLon=latLon;
		this.distance=distance;
		
		hashCode = query.hashCode() + (targetMetaAttribute != null ? targetMetaAttribute.hashCode() : 0) + (orderByCriteria != null ? orderByCriteria.hashCode() : 0) 
					+ (filters != null ? Arrays.hashCode(filters) : 0)
					+(latLon != null ? Arrays.hashCode(latLon.toArray()) : 0)
					+distance;
	}

	/**
	 * Returns the query string for this search.
	 * 
	 * @return the query string for this search.
	 */
	public String getQuery() 
	{
		return query;
	}

	/**
	 * Returns the target meta-attribute for this search.
	 * 
	 * @return the target meta-attribute for this search.
	 */
	public String getTargetMetaAttribute() 
	{
		return targetMetaAttribute;
	}

	/**
	 * Returns the search (requested) page size.
	 * 
	 * @return the search (requested) page size.
	 */
	public long getHowManyResults() 
	{
		return howManyResults;
	}

	/**
	 * Returns the (requested) order by criteria of this search.
	 * 
	 * @return the (requested) order by criteria of this search.
	 */
	public String getOrderByCriteria() 
	{
		return orderByCriteria;
	}

	/**
	 * Returns the search URI.
	 * 
	 * @return the search URI.
	 */
	public String getSearchUri() 
	{
		return searchUri;
	}
	
	public String[] getFilters()
	{
		return filters;	
	}
	
	
	/**
	 * @return the latLon
	 */
	public List<Double> getLatLon() {
		return latLon;
	}
	

	/**
	 * @return the distance
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * @return the hashCode
	 */
	public int getHashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object anotherSearch) 
	{
		try 
		{
			OseeGeniusSearch search = (OseeGeniusSearch) anotherSearch;
			return (hashCode == search.hashCode);
		} catch (Exception ignore) 
		{
			return false;
		}
	}
	
	@Override
	public int hashCode() 
	{
		return hashCode;
	}
}