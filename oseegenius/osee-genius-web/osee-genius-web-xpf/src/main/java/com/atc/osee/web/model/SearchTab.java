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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;
import org.jfree.util.Log;
import org.w3c.tools.codec.Base64Encoder;

import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.sbn.QueryResponseSbn;
import com.atc.osee.web.servlets.search.federated.PazPar2;
import com.atc.osee.web.servlets.search.federated.PazPar2Proxy;
import com.atc.osee.web.util.Utils;

/**
 * OseeGenius -W- search tab.
 * 
 * @author ggazzarini
 * @since 1.0
 */
public class SearchTab implements Serializable, ItemSelection
{
	private static final long serialVersionUID = 6396393863855402941L;

	private int id;
	private String title;
	private String queryParameters;
	private boolean isNew = true;
	private int resultsPerPage = 10;

	private SolrQuery query;
	private QueryResponse response;
	private String didYouMeanTerms;
	
	private Map<String, FederatedItemSelection> selectedFederatedItems = new HashMap<String, FederatedItemSelection>();
	private Map<String, FederatedItemSelection> federatedItemsThatCanBeSent = new HashMap<String, FederatedItemSelection>();
	
	private Set<String> selectedItems = new HashSet<String>();
	private Set<String> itemsThatCanBeSent = new HashSet<String>();
	private Map<String, NamedList<String>> selectedCopies = new HashMap<String, NamedList<String>>();
		
	private String rssQueryString;

	private boolean externalSearchTab;
	private PazPar2  pazpar2;
	
	private String orderBy;
	
	private SearchExperience parent;
	
	private Set<String> pinnedFilters = new HashSet<String>();
	
	private Set<String> selectedItem = new HashSet<String>();
	
	//bug 3603
	private String nssQuery;
	
	

	/**
	 * @return the nssQuery
	 */
	public String getNssQuery() {
		return nssQuery;
	}

	/**
	 * @param nssQuery the nssQuery to set
	 */
	public void setNssQuery(String nssQuery) {
		this.nssQuery = nssQuery;
	}
	
	/**************************SBN***********************************/
	private boolean externalSbnSearchTab;
	
	private QueryResponseSbn sbnResponse;
	
	public boolean isExternalSbnSearchTab() {
		return externalSbnSearchTab;
	}

	public void setExternalSbnSearchTab(boolean externalSbnSearchTab) {
		this.externalSbnSearchTab = externalSbnSearchTab;
	}

	public QueryResponseSbn getSbnResponse() {
		return sbnResponse;
	}

	public void setSbnResponse(QueryResponseSbn sbnResponse) {
		this.sbnResponse = sbnResponse;
	}

	public void setForeignDataSBN(final String query, final Locale locale)
	{
		this.didYouMeanTerms = null;
		this.title = query;
		externalSearchTab = true;
		externalSbnSearchTab = true;
		isNew = false;	
	}
	
	private Map<String, SBNFederatedItemSelection> selectedSBNItems = new HashMap<String, SBNFederatedItemSelection>();
	private Map<String, SBNFederatedItemSelection> SBNItemsThatCanBeSent = new HashMap<String, SBNFederatedItemSelection>();
	
	public Map<String, SBNFederatedItemSelection> getSelectedSBNItemsForExportOrDownload()
	{
		return selectedSBNItems;
	}
	
	
	public int howManySelectedForSBNExport()
	{
		return  selectedSBNItems.size();
	}
	
	public int howManySelectedForSBNSend()
	{
		return SBNItemsThatCanBeSent.size();
	}
	
	/*************************************************************/
	
	

	/**
	 * Checks if the current query contains the given filter.
	 * 
	 * @param filter the filter query.
	 * @param request the current HTTP request.
	 * @return true if the current query contains the given filter.
	 */
	public boolean containsFilter(final String filter, final HttpServletRequest request)
	{
		try
		{
			return URLDecoder.decode(request.getQueryString(), IConstants.UTF_8).indexOf(URLDecoder.decode(StringEscapeUtils.unescapeHtml(filter), IConstants.UTF_8)) != -1;
		} catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * Builds and initializes a new search tab.
	 * 
	 * @param index the tab index.
	 * @param parent the parent search experience.
	 */
	public SearchTab(final int index, final SearchExperience parent)
	{
		this.title = IConstants.NA;
		this.id = index;
		this.queryParameters = "t=" + id;
		this.parent = parent;
	}
	
	/**
	 * Injects onto this tab the federated search metadata.
	 * 
	 * @param client the HTTP (federated) client.
	 * @param url the target URL.
	 */
	public void injectFederatedSearchStuff(final CloseableHttpClient client, final String url)
	{
		pazpar2 = new PazPar2Proxy(client, url);
	}
	
	/**
	 * Injects onto this tab the federated search metadata.
	 * 
	 * @param client the HTTP (federated) client.
	 * @param url the target URL.
	 */
	public void injectFederatedSearchStuff(final PazPar2 pazpar2)
	{
		this.pazpar2 = pazpar2;
	}
	
	
	/**
	 * Returns the ordering criteria of this tab.
	 * 
	 * @return the ordering criteria of this tab.
	 */
	public String getOrderBy()
	{
		return orderBy;
	}

	/**
	 * Sets the order by criteria of this tab.
	 * 
	 * @param orderBy the order by criteria of this tab.
	 */
	public void setOrderBy(final String orderBy)
	{
		this.orderBy = orderBy;
	}

	/**
	 * Returns the identifier of this tab.
	 * 
	 * @return the identifier of this tab.
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
		return title;
	}

	/**
	 * Returns how many items are displayed on each page of this tab.
	 * 
	 * @return how many items are displayed on each page of this tab.
	 */
	public int getResultsPerPage()
	{
		return resultsPerPage;
	}

	/**
	 * Returns the current set (page) of documents held by this tab.
	 * 
	 * @return the current set (page) of documents held by this tab.
	 */
	public QueryResponse getResponse()
	{
		return response;
	}

	/**
	 * Sets how many items will be displayed on each page of this tab.
	 * 
	 * @param resultsPerPage how many items will be displayed on each page of this tab.
	 */
	public void setResultsPerPage(final int resultsPerPage)
	{
		this.resultsPerPage = resultsPerPage;		
	}
	
	/**
	 * Sets the data model of this tab.
	 * 
	 * @param title the tab title.
	 * @param query the query of the executed search.
	 * @param response the query response.
	 * @param currentPageIndex the current page index.
	 */
	public void setData(final String title, final SolrQuery query, final QueryResponse response, final int currentPageIndex)
	{
		this.didYouMeanTerms = null;
		this.query = query;
		this.title = title;
		
		isNew = false;
		this.response = response;
		
		if (response != null && response.getResults().getNumFound() != 0)
		{
			createRssQueryString();
		} else if (response.getSpellCheckResponse() != null)
		{
			List<Suggestion> suggestions = response.getSpellCheckResponse().getSuggestions();
			StringBuilder builder = new StringBuilder("[");
			for (Suggestion suggestion : suggestions)
			{
				List<String> terms = suggestion.getAlternatives();
				List<Integer> frequencies = suggestion.getAlternativeFrequencies();
				int index = 0;
				for (String term : terms)
				{
					builder
						.append("{text:\"")
						.append(term)
						.append("\",weight:")
						.append((double)(frequencies.get(index)))
						.append(", url: \"javascript:searchBySuggestion('")
						.append(term).append("');\"}");
					builder.append(",");
					index++;
				}
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append("]");
			
			didYouMeanTerms = builder.length() > 3 ? builder.toString() : null;
		}
	}

	/**
	 * Injects federated results onto this tab.
	 * 
	 * @param query the query string.
	 * @param locale the current user locale.
	 */
	public void setForeignData(final String query, final Locale locale)
	{
		this.didYouMeanTerms = null;
		this.title = query;
		externalSearchTab = true;
		isNew = false;	
	}

	@Override
	public String toString()
	{
		return title;
	}
		
	/**
	 * Returns the currently applied filter queries.
	 * 
	 * @param request the HTTP request.
	 * @return the currently applied filter queries.
	 */
	public String [] getFilterQueries(final HttpServletRequest request)
	{
		if (!externalSearchTab)
		{
			return query != null ? query.getFilterQueries() : new String [0];
		} else 
		{
			String [] filters = request.getParameterValues("f");	
			return filters;
		}
	}
	
	/**
	 * Creates a base 64 encoded query string for RSS feeds.
	 */
	private void createRssQueryString()
	{
		try
		{
			StringBuilder builder = new StringBuilder()
				.append("q=")
				.append(URLEncoder.encode(query.getQuery(), "UTF-8"));
			
			String queryType = query.getRequestHandler();
			if (queryType != null && queryType.trim().length() != 0)
			{
				builder
					.append(IConstants.AMPERSAND)
					.append("qt=")
					.append(queryType);
			}
			
			builder.append("&wt=xslt");
			
			String [] filters = query.getFilterQueries();
			if (filters != null)
			{
				for (String filter : filters)
				{
					builder.append(IConstants.AMPERSAND).append("fq=").append(filter);
				}
			}
			try 
			{
				rssQueryString = new Base64Encoder(builder.toString()).processString();
			} catch (Exception ignore) {}
		} catch (UnsupportedEncodingException exception)
		{
			// Ignore: no RSS for this search
		}
	}

	/**
	 * Returns the RSS query string.
	 * 
	 * @return the RSS query string.
	 */
	public String getRssQueryString() 
	{
		return rssQueryString;
	}
	
	/**
	 * Returns true if this tab never held search results.
	 * 
	 * @return true if this tab never held search results.
	 */
	public boolean isNew()
	{
		return isNew;
	}
	
	/**
	 * Returns true if this tab doesn't have search results.
	 * 
	 * @return true if this tab doesn't have search results.
	 */
	public boolean isEmpty()
	{
		return isNew || response == null || response.getResults().getNumFound() == 0;
	}
	
	/**
	 * Returns number of search results.
	 * 
	 * @return number of search results.
	 */
	public int howManyResult()
	{
		return (response != null)? (int) response.getResults().getNumFound() : 0;
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
	}

	/**
	 * Returns the SOLR query held by this tab.
	 * 
	 * @return the SOLR query held by this tab.
	 */
	public SolrQuery getQuery()
	{
		return query;
	}
	
	/**
	 * Selects / deselects the copy associated with the given identifier.
	 * 
	 * @param id the copy identifier.
	 * @return true if a copy have been selected, false if has been deselected.
	 */
	public boolean toggleCopySelection (final String barcode, final String title, final String author, final String inventory, final String location, final String collocation, final String recordId)
	{
		if (selectedCopies.containsKey(barcode))
		{
			return null != selectedCopies.remove(barcode);
		} else 
		{
			NamedList<String> copy = new NamedList<String>();
			copy.add("barcode", barcode);
			copy.add("title", title);
			copy.add("inventory", inventory);
			copy.add("location", location);
			copy.add("collocation", collocation);
			copy.add("recordId", recordId);
			copy.add("author", author);
			selectedCopies.put(barcode, copy);
			parent.add2CopyFolder(copy);
			return true;
		}
	}
	
		
	/**
	 * Selects / deselects the document associated witht the given identifier.
	 * 
	 * @param id the document identifier.
	 * @return true if a document have been selected, false if has been deselected.
	 */
	public boolean toggleSelection(final String id)
	{
		if (selectedItems.contains(id))
		{
			return !selectedItems.remove(id);
		} else 
		{
			selectedItems.add(id);
			SolrDocument selectedDocument = getItem(id);
			if (selectedDocument != null)
			{
				parent.add2Folder(selectedDocument);
			}
			return true;
		}
	}

	private SolrDocument getItem(final String id)
	{
		for (SolrDocument document : response.getResults())
		{
			if (document.getFieldValue(ISolrConstants.ID_FIELD_NAME).equals(id))
			{
				return document;
			}
		}
		return null;
	}
	
	/**
	 * Selects / deselects the federated document associated witht the given identifier and record id..
	 * 
	 * @param id the document identifier.
	 * @param recid the clustered record identifier.
	 * @param offset the offset of the record within the cluster.
	 * @return true if a document have been selected, false if has been deselected.
	 */
	public boolean toggleFederatedSelection(final String id, final String recid, int offset) 
	{
		if (selectedFederatedItems.containsKey(id)) 
		{
			selectedFederatedItems.remove(id);
			return false;
		} else 
		{
			selectedFederatedItems.put(id, new FederatedItemSelection(recid, id, offset));
			try 
			{
				// offset = null means we want whole pazpar2 record, not just MARCXML
				String pazpar2res = getPazpar2().record(recid, null);
				SolrDocument pazpar2SolrDocument = Utils.createSolrDocument(pazpar2res, recid, id);
				if (pazpar2SolrDocument != null)
				{
					try 
					{
						pazpar2SolrDocument.setField(ISolrConstants.MARC_21_FIELD_NAME, getPazpar2().record(recid, offset));
					} catch (Exception exception)
					{
						Log.error("Unable to retrieve MARCXML representation for record #" + recid, exception);
					}
					parent.add2Folder(pazpar2SolrDocument);				
				}
			} catch (Exception exception)
			{
				Log.error("Unable to retrieve federated record #" + recid, exception);
			}
			return true;
		}
	}
	
	
	public boolean toggleSBNFederatedSelection(final String id, final long posInResultSet) 
	{
		if (selectedSBNItems.containsKey(id)) 
		{
			selectedSBNItems.remove(id);
			return false;
		} else 
		{
			selectedSBNItems.put(id, new SBNFederatedItemSelection(id , posInResultSet));
			return true;
		}
	}
	
	/**
	 * Selects / deselects an item that can be sent by email.
	 * 
	 * @param id the document identifier.
	 * @return true if a document have been selected, false if has been deselected.
	 */
	public boolean toggleSendableSelection(final String id)
	{
		if (itemsThatCanBeSent.contains(id))
		{
			return !itemsThatCanBeSent.remove(id);
		} else 
		{
			itemsThatCanBeSent.add(id);
			return true;
		}
	}
	
	/**
	 * Selects / deselects a federated item that can be sent by email.
	 * 
	 * @param id the document identifier.
	 * @param recid the clustered record identifier.
	 * @param offset the offset of the record within the cluster.
	 * @return true if a document have been selected, false if has been deselected.
	 */
	public boolean toggleFederatedSendableSelection(final String id, final String recid, int offset)
	{
		if (federatedItemsThatCanBeSent.containsKey(id))
		{
			federatedItemsThatCanBeSent.remove(id);
			return false;
		} else 
		{
			federatedItemsThatCanBeSent.put(id, new FederatedItemSelection(recid, id, offset));
			return true;
		}
	}	
	
	public boolean toggleSBNFederatedSendableSelection(final String id, final long posInResultSet)
	{
		if (SBNItemsThatCanBeSent.containsKey(id))
		{
			SBNItemsThatCanBeSent.remove(id);
			return false;
		} else 
		{
			SBNItemsThatCanBeSent.put(id, new SBNFederatedItemSelection(id, posInResultSet));
			return true;
		}
	}	
	
	/**
	 * Returns the total number of documents that can be exported.
	 * 
	 * @return the total number of documents that can be exported. 
	 */
	public int howManySelectedForExport()
	{
		return selectedItems.size() + selectedFederatedItems.size() + selectedSBNItems.size() + selectedItem.size();
	}
	
	/**
	 * Returns the total number of documents that can be sent by email.
	 * 
	 * @return the total number of documents that can be sent by email. 
	 */
	public int howManySelectedForSend()
	{
		return itemsThatCanBeSent.size() + federatedItemsThatCanBeSent.size() + SBNItemsThatCanBeSent.size();
	}
	
	/**
	 * Returns true if the document associated with the given id is selected.
	 * 
	 * @param id the document identifier.
	 * @return true if the document associated with the given id is selected.
	 */
	public boolean isSelected(final String id)
	{
		return selectedItems.contains(id) 
				|| itemsThatCanBeSent.contains(id) 
				|| selectedFederatedItems.containsKey(id) 
				|| federatedItemsThatCanBeSent.containsKey(id)
				|| selectedSBNItems.containsKey(id)
				|| SBNItemsThatCanBeSent.containsKey(id);
	}
	
	/**
	 * Deselects all documents.
	 */
	public void clearSelection()
	{
		selectedItems.clear();
		itemsThatCanBeSent.clear();

		selectedFederatedItems.clear();
		federatedItemsThatCanBeSent.clear();
		
		selectedSBNItems.clear();
		SBNItemsThatCanBeSent.clear();
	}
	
	/**
	 * Returns the selected documents that can be sent by email.
	 * 
	 * @return the selected documents that can be sent by email.
	 */
	public Set<String> getSelectedItemsForEmail()
	{
		return itemsThatCanBeSent;
	}
	
	/**
	 * Returns the selected (federated) documents that can be sent by email.
	 * 
	 * @return the (federated and selected) documents that can be sent by email.
	 */
	public Map<String, FederatedItemSelection> getSelectedFederatedItemsForEmail()
	{
		return federatedItemsThatCanBeSent;
	}
	
	/**
	 * Returns the selected documents that can be exported.
	 * 
	 * @return the selected documents that can be exported.
	 */
	public Set<String> getSelectedItemsForExportOrDownload()
	{
		if(selectedItem==null ||selectedItem.isEmpty()){
			return selectedItems;
		}
		else return selectedItem;
			
	}
	
	/**
	 * Returns the federated selected documents that can be exported.
	 * 
	 * @return the (federated and selected) selected documents that can be exported.
	 */
	public Map<String, FederatedItemSelection> getSelectedFederatedItemsForExportOrDownload()
	{
		return selectedFederatedItems;
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
			SearchTab tab = (SearchTab) obj;
			return id == tab.id;
		} catch (Exception exception) 
		{
			return false;
		}
	}
	
	/**
	 * Returns the suggestions of the current search.
	 * 
	 * @return the suggestions of the current search.
	 */
	public String getDidYouMeanTerms()
	{
		return didYouMeanTerms;
	}

	/**
	 * Sets the title of this tab.
	 * 
	 * @param title the title.
	 * @param locale the user current locale.
	 */
	public void setTitle(final String title, final Locale locale) 
	{
		this.title = new StringBuilder(title)
			.append(" (")
			.append(ResourceBundle.getBundle("resources").getString("federated"))
			.append(" )")
			.toString();
	}

	/**
	 * Returns true if this tab is holding federated search results.
	 * 
	 * @return true if this tab is holding federated search results.
	 */
	public boolean isExternalSearchTab() 
	{
		return externalSearchTab;
	}

	/**
	 * If true, flags this tab as an external search tab.
	 * An external search tab is a tab that holds federated search results.
	 * 
	 * @param isExternalSearchTab true if this tab will hold federated search results.
	 */
	public void setExternalSearchTab(final boolean isExternalSearchTab) 
	{
		this.externalSearchTab = isExternalSearchTab;
	}

	/**
	 * Returns the federated search proxy.
	 * 
	 * @return the federated search proxy.
	 */
	public PazPar2 getPazpar2() 
	{
		return pazpar2;
	}

	public void clear() 
	{
		query = null;
		response = null;
		title = IConstants.NA;
		queryParameters = null;
		isNew = true;
		didYouMeanTerms = null;
		
		selectedItems.clear();
		itemsThatCanBeSent.clear();
			
		rssQueryString = null;

		externalSearchTab = false;
		
		nssQuery = null;
		
		externalSbnSearchTab = false;
		sbnResponse = null;
		
		selectedSBNItems.clear();
		SBNItemsThatCanBeSent.clear();
	}
	
	public void pinFilter(final String filter) 
	{
		pinnedFilters.add(filter);
	}
	
	public void unpinFilter(final String filter) 
	{
		pinnedFilters.remove(filter);
	}		
	
	public boolean filterIsPinned(String filter)
	{
		return pinnedFilters.contains(filter);
	}
	
	public Set<String> getPinnedFilters()
	{
		return pinnedFilters;
	}
	
	public boolean hasPinnedFilters()
	{
		return !pinnedFilters.isEmpty();
	}

	/**
	 * @return the selectedItem
	 */
	public Set<String> getSelectedItem() {
		return selectedItem;
	}

	public void selectOrUnselectAll(boolean checkAll) {
		for (SolrDocument document : response.getResults())
		{
			final String id = (String) document.getFieldValue(ISolrConstants.ID_FIELD_NAME);
			if (checkAll) 
			{
				selectedItems.add(id);
				itemsThatCanBeSent.add(id);
			} else 
			{
				selectedItems.remove(id);
				itemsThatCanBeSent.remove(id);
			}
		}
	}
}
