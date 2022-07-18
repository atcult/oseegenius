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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * OseeGenius -W- user (session) search history.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class SearchHistory implements Serializable 
{
	private static final long serialVersionUID = -4491709203985349546L;
	
	private List<OseeGeniusSearch> simple = new LinkedList<OseeGeniusSearch>();
	private List<OseeGeniusSearch> advanced = new LinkedList<OseeGeniusSearch>();
	private List<OseeGeniusSearch> browse = new LinkedList<OseeGeniusSearch>();
	private List<OseeGeniusSearch> federated = new LinkedList<OseeGeniusSearch>();

	private int lastUsedId;
	
	/**
	 * Adds a new simple search entry to this history.
	 * 
	 * @param query the query string.
	 * @param targetMetaAttribute the target search attribute.
	 * @param howManyResults how many results for this search.
	 * @param orderByCriteria the order by criteria.
	 * @param searchUri the search URI.
	 */
	public void addSimpleSearchEntry(
			final String query, 
			final String targetMetaAttribute, 
			final long howManyResults, 
			final String orderByCriteria, 
			final String searchUri,
			final String [] filters,
			final List<Double> latLon,
			final int distance)
	{
		internalAdd(new OseeGeniusSearch(lastUsedId++, query, targetMetaAttribute, howManyResults, orderByCriteria, searchUri, filters, latLon,distance), simple);
	}
	
	/**
	 * Adds a new simple search entry to this history.
	 * 
	 * @param query the query string.
	 * @param targetMetaAttribute the target search attribute.
	 * @param howManyResults how many results for this search.
	 * @param orderByCriteria the order by criteria.
	 * @param searchUri the search URI.
	 */
	public void addFederatedSearchEntry(
			final String query, 
			final String targetMetaAttribute, 
			final long howManyResults, 
			final String orderByCriteria, 
			final String searchUri,
			final String [] filters)
	{
		internalAdd(new OseeGeniusSearch(lastUsedId++, query, targetMetaAttribute, howManyResults, orderByCriteria, searchUri, filters,null,0), federated);
	}
	
	/**
	 * Adds a new simple search entry to this history.
	 * 
	 * @param query the query string.
	 * @param targetMetaAttribute the target search attribute.
	 * @param howManyResults how many results for this search.
	 * @param orderByCriteria the order by criteria.
	 * @param searchUri the search URI.
	 */
	public void addAdvancedSearchEntry(
			final String query, 
			final String targetMetaAttribute, 
			final long howManyResults, 
			final String orderByCriteria, 
			final String searchUri,
			final String [] filters)
	{
		internalAdd(new OseeGeniusSearch(lastUsedId++, query, targetMetaAttribute, howManyResults, orderByCriteria, searchUri, filters, null, 0), advanced);
	}	
	
	/**
	 * Adds a new simple search entry to this history.
	 * 
	 * @param query the query string.
	 * @param targetMetaAttribute the target search attribute.
	 * @param pageSize the page size.
	 * @param orderByCriteria the order by criteria.
	 * @param searchUri the search URI.
	 */
	public void addBrowseEntry(
			final String from, 
			final String targetMetaAttribute, 
			final int pageSize, 
			final String searchUri)
	{
		internalAdd(new OseeGeniusSearch(lastUsedId++, from, targetMetaAttribute, pageSize, null, searchUri, null, null, 0), browse);
	}	
	
	/**
	 * Returns the simple search entries in this (session) history.
	 * 
	 * @return the simple search entries in this (session) history.
	 */
	public List<OseeGeniusSearch> getSimpleSearchEntries()
	{
		return simple;
	}
	
	/**
	 * Returns the federated search entries in this (session) history.
	 * 
	 * @return the federated search entries in this (session) history.
	 */
	public List<OseeGeniusSearch> getFederatedSearchEntries()
	{
		return federated;
	}

	/**
	 * Returns the advanced search entries in this (session) history.
	 * 
	 * @return the advanced search entries in this (session) history.
	 */
	public List<OseeGeniusSearch> getAdvancedSearchEntries()
	{
		return advanced;
	}
	
	/**
	 * Returns the advanced search entries in this (session) history.
	 * 
	 * @return the advanced search entries in this (session) history.
	 */
	public List<OseeGeniusSearch> getBrowsingEntries()
	{
		return browse;
	}
	
	/**
	 * Returns true if this history is empty.
	 * The history is supposed to be empty if there are no entries.
	 * 
	 * @return true if this history is empty.
	 */
	public boolean isEmpty() 
	{
		return simple.isEmpty() && advanced.isEmpty() && browse.isEmpty() && federated.isEmpty();
	}
	
	/**
	 * Returns true if the simple search history is not empty.
	 * 
	 * @return true if the simple search history is not empty.
	 */
	public boolean isSimpleSearchListNotEmpty()
	{
		return !simple.isEmpty();
	}
	
	/**
	 * Returns true if the advanced search history is not empty.
	 * 
	 * @return true if the advanced search history is not empty.
	 */
	public boolean isAdvancedSearchListNotEmpty()
	{
		return !advanced.isEmpty();
	}

	/**
	 * Returns true if the browsing history is not empty.
	 * 
	 * @return true if the browsing history is not empty.
	 */
	public boolean isBrowsingListNotEmpty()
	{
		return !browse.isEmpty();
	}
	
	/**
	 * Returns true if the federated search history is not empty.
	 * 
	 * @return true if the federated search history is not empty.
	 */
	public boolean isFederatedSearchListNotEmpty()
	{
		return !federated.isEmpty();
	}
	
	/**
	 * Internal method used for adding new search histories.
	 * The entry will be insert at first position only if it doesn't exist.
	 * 
	 * @param search the search entry.
	 * @param container the container for the specific search type.
	 */
	private void internalAdd(final OseeGeniusSearch search, final List<OseeGeniusSearch> container)
	{
		if (!container.contains(search))
		{
			container.add(0, search);
		}
	}

	/**
	 * Removes the history entry associated with the given id.
	 * 
	 * @param historyEntryId the history entry id.
	 */
	public void removeEntry(final String historyEntryId) 
	{
		try 
		{
			int id = Integer.parseInt(historyEntryId);
			if (internalRemove(id, simple))
			{
				return;
			}
			
			if (internalRemove(id, browse))
			{
				return;
			}
			
			if (internalRemove(id, advanced))
			{
				return;
			}
			
			if (internalRemove(id, federated))
			{
				return;
			}			
		} catch (Exception exception)
		{	
		}
	}
	
	private boolean internalRemove(final int id, final List<OseeGeniusSearch> entries)
	{
		for (Iterator<OseeGeniusSearch> iterator = entries.iterator(); iterator.hasNext();)
		{
			OseeGeniusSearch entry = iterator.next();
			if (entry.getId() == id)
			{
				iterator.remove();
				return true;
			}
		}
		return false;
	}
}