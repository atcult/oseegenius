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
package com.atc.osee.web.folio;

import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import javax.naming.InitialContext;
			
public class BncfElemtResearchHistory 
{	
	protected static DataSource datasource;
	protected BncfDao dao;

	private final String userId;
	private final String query;
	private final long howManyResults;
	private final String orderByCriteria;
	private final String queryType;
	private String [] filters;
	private final String searchUri;
	private final String data;
	private final String section;
	private final int id_search;
	

	/**
	 * Builds a new simple search history entry with the given data. 
	 * 
	 * @param query the user entered query string.
	 * @param howManyResults how many results for this search.
	 * @param orderByCriteria the order by criteria.
	 * @param searchUri the complete search URI.
	 * @param filters the filter queries applied to this search.
	 */
	public BncfElemtResearchHistory(
			final int id_search,
			final String userId,
			final String query, 
			final String queryType,
			final long howManyResults, 
			final String orderByCriteria, 
			final String searchUri,
			final String [] filters,
			final String data,
			final String section
			) 
	{
		
		try {
			final InitialContext cxt = new InitialContext();
			datasource = (DataSource) cxt.lookup("java:/comp/env/jdbc/pg");
			dao = new BncfDao(datasource);
		} catch (Exception ignore) {
		
		}
		this.id_search=id_search;
		this.userId=userId;
		this.query = query;
		this.queryType = queryType;
		this.howManyResults = howManyResults;
		this.orderByCriteria = orderByCriteria != null ? orderByCriteria : "score";
		this.searchUri = searchUri;
		this.filters = filters;	
		this.data=data;
		this.section=section;
	}

	
	
	
	public void save() {
		try {
			this.dao.insertSearch(userId,
					query, 
					queryType,
					howManyResults, 
					orderByCriteria,
					searchUri, 
					createString(filters),
					section);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	private String createString(String [] listOfString) {
		String res =null;
		if (listOfString!=null) {
			for (String str : listOfString) {
				if(res==null)
					res=str + ", ";
				else
					res=res+ str + ", ";
			}
			return res.substring(0, res.length()-2);
		}
		return null;
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
	 * Returns the query type string for this search.
	 * 
	 * @return the query type string for this search.
	 */
	public String getQueryType() 
	{
		return queryType;
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
	 * Returns data of research.
	 * 
	 * @return data of research.
	 */
	public String getData() 
	{
		return data;
	}
	
	
	
	/**
	 * Returns section of research(simple, adv, federated).
	 * 
	 * @return section of research.
	 */
	public String getSection() 
	{
		return section;
	}
	
	/**
	 * Returns id of research.
	 * 
	 * @return id of research.
	 */
	public int getIdSearch() 
	{
		return id_search;
	}
	
	
	
}