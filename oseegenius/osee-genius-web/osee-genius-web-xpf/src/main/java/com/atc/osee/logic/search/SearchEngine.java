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
package com.atc.osee.logic.search;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;

import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;

/**
 * Default OseeGenius -W- search engine reference.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class SearchEngine implements ISearchEngine 
{
	private final SolrServer searcher;
	private final SolrServer highlighter;

	/**
	 * Query for home page resources.
	 */
	private static final SolrQuery HOMEPAGE_QUERY = new SolrQuery(ISolrConstants.ALL);
	static 
	{
		HOMEPAGE_QUERY.setRequestHandler(ISolrConstants.HOMEPAGE_QUERY_TYPE_NAME);
	}

	/**
	 * Builds a new search engine with the given data.
	 * 
	 * @param searcher the searcher reference.
	 * @param highlighter the highlighter reference.
	 */
	public SearchEngine(final SolrServer searcher, final SolrServer highlighter)
	{
		this.searcher = searcher;
		this.highlighter = highlighter;
	}

	/**
	 * Builds a new search engine with the given searcher.
	 * 
	 * @param searcher the searcher reference.
	 */
	public SearchEngine(final SolrServer searcher)
	{
		this(searcher, null);
	}
	
	@Override
	public QueryResponse getHomepageResources() throws SystemInternalFailureException
	{
		try
		{
			return searcher.query(HOMEPAGE_QUERY);
		} catch (Exception exception) 
		{
			Log.error(MessageCatalog._100003_SEARCH_ENGINE_FAILURE, exception);
			throw new SystemInternalFailureException();
		}
	}
	
	

	
	@Override
	public QueryResponse executeQuery(final SolrQuery query) throws SystemInternalFailureException
	{
		try
		{
			QueryResponse response = searcher.query(query);
			if (response != null)
			{
				if (response.getGroupResponse() != null)
				{
					GroupCommand command = response.getGroupResponse().getValues().iterator().next();
					
					SolrDocumentList list = new SolrDocumentList();
					list.setNumFound(command.getNGroups());

					for (Group group : command.getValues())
					{
						SolrDocumentList groupedItems = group.getResult();
						
						if (groupedItems != null && !groupedItems.isEmpty())
						{
							SolrDocument head = new SolrDocument();
							head.putAll(groupedItems.get(0));
							head.addField(ISolrConstants.GROUPED_ITEMS, groupedItems);
							list.add(head);
						}
					}
					
					NamedList<Object> adaptedResponse = new NamedList<Object>();
					adaptedResponse.add("response", list);
					response.setResponse(adaptedResponse);
				}
				
//				SolrDocumentList result = response.getResults();
//				if (highlighter != null 
//						&& result != null 
//						&& result.getNumFound() != 0 
//						&& (!"adv".equals(query.getQueryType()) && !"def".equals(query.getQueryType())))
//				{
//					highligting(result, query.getQuery()); 
//				}
			}
			return response;
		} catch (Exception exception) 
		{
			Log.error(
					MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
					exception);
			throw new SystemInternalFailureException();
		}
	}
	
	/**
	 * Highlighting method.
	 * 
	 * @param documents the documents to be highlighted.
	 * @param queryString the query string.
	 */
	@SuppressWarnings("unused")
	private void highligting(final SolrDocumentList documents, final String queryString) 
	{
		SolrQuery query = new SolrQuery(queryString);
		int indexOfOpeningBrackets = queryString.indexOf("{!sort");
		if (indexOfOpeningBrackets != -1)
		{
			int indexOfClosingBrackets = queryString.indexOf("}", indexOfOpeningBrackets);
			if (indexOfClosingBrackets != -1)
			{
				query.setQuery(queryString.substring(indexOfClosingBrackets + 1));
			}
		}
		
		StringBuilder builder = new StringBuilder(ISolrConstants.ID_FIELD_NAME + ":(");
		for (SolrDocument document : documents)
		{
			builder.append(document.get(ISolrConstants.ID_FIELD_NAME)).append(" ");
		}
		
		builder.setCharAt(builder.length() - 1, ')');
		
		query.addFilterQuery(builder.toString());
		query.setRows(documents.size());
		try 
		{
			QueryResponse response = highlighter.query(query);
			Map<String, Map<String, List<String>>> hlResponse = response.getHighlighting();
			
			if (hlResponse != null && !hlResponse.isEmpty())
			{
				for (Entry<String, Map<String, List<String>>> entry : hlResponse.entrySet())
				{
					String id = entry.getKey();
					for (SolrDocument document : documents)
					{
						if (id.equals(document.get(ISolrConstants.ID_FIELD_NAME)))
						{
							document.setField(
									ISolrConstants.HIGHLIGHT, 
									entry.getValue().get(ISolrConstants.FULLTEXT));
						}
					}
				}
			}

		} catch (SolrServerException exception)
		{
			Log.error(
					MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
					exception);
		}
	}

	@Override
	public QueryResponse findDocumentByURI(final String uri) throws SystemInternalFailureException
	{
		try 
		{
			SolrQuery query = new SolrQuery(
					ISolrConstants.DOCUMENTI_URI 
					+ IConstants.COLON 
					+ IConstants.DOUBLE_QUOTES 
					+ uri 
					+ IConstants.DOUBLE_QUOTES);
			query.setRequestHandler(ISolrConstants.DETAILS_QUERY_TYPE_NAME);
			return searcher.query(query);
		} catch (Exception exception) 
		{
			Log.error(
					MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
					exception);
			throw new SystemInternalFailureException();
		}		
	}
	@Override
	public QueryResponse findAuthDocumentByAuthLink (final String uri) throws SystemInternalFailureException
	{
		try {
			SolrQuery query = new SolrQuery(
					ISolrConstants.AUTHORITY_ID
					+ IConstants.COLON  
					+ IConstants.DOUBLE_QUOTES 
					+ uri 
					+ IConstants.DOUBLE_QUOTES);			
			query.setRequestHandler(ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME);			
			QueryResponse queryResponse = searcher.query(query);
			return searcher.query(query);
		} catch (Exception exception) 
		{
			Log.error(
					MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
					exception);
			throw new SystemInternalFailureException();
		}		
	}

	@Override
	public QueryResponse findDocumentByURIWithMLT(final String uri, String pageNumber, final String institution, final String ... groupingAndFilteringCriteria) throws SystemInternalFailureException
	{
		try 
		{
			SolrQuery query = new SolrQuery(
					ISolrConstants.DOCUMENTI_URI 
					+ IConstants.COLON 
					+ IConstants.DOUBLE_QUOTES 
					+ uri 
					+ IConstants.DOUBLE_QUOTES);
				        			
	         if("IMSS".equals(institution)){
				query.add(
						"f",
						groupingAndFilteringCriteria != null
							? "\"" + groupingAndFilteringCriteria + "\""
							: String.valueOf(Integer.MAX_VALUE));
			}
	         else {
	        	 for(String filter: groupingAndFilteringCriteria){
		            	if (filter != null && filter.trim().length() != 0)
		            		query.addFilterQuery(filter);
		         }
	         }
						
			query.setRequestHandler(ISolrConstants.MORE_LIKE_THIS_QUERY_TYPE_NAME);
			
			
//			HttpSession session = request.getSession(false);
//			if (session != null)
//			{
//				String logicalView = (String) session.getAttribute(HttpAttribute.LOGICAL_VIEW);
//				if (isNotNullOrEmptyString(logicalView))
//				{
//					query.addFilterQuery("catalog_source:" + logicalView);
//				}
//			}	
			
			if (pageNumber != null && pageNumber.trim().length() != 0)
			{
				query.setStart( (Integer.parseInt(pageNumber) - 1) * 10);
			}
			return searcher.query(query);
		} catch (Exception exception) 
		{
			Log.error(
					MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
					exception);
			throw new SystemInternalFailureException();
		}		
	}

	@Override
	public void ping() throws SystemInternalFailureException 
	{
		try 
		{
			searcher.ping();
		} catch (Exception e) 
		{
			Log.error("Unable to ping OG searcher.", e);
			throw new SystemInternalFailureException();
		} 
	}

	@Override
	public QueryResponse documents(final Set<String> ids) throws SystemInternalFailureException 
	{
		try 
		{
			StringBuilder builder = new StringBuilder("(");
			for (String uri : ids)
			{
				builder.append(IConstants.DOUBLE_QUOTES).append(uri).append(IConstants.DOUBLE_QUOTES).append(' ');
			}
			builder.setCharAt(builder.length() - 1, ')');
			
			SolrQuery query = new SolrQuery(builder.toString());
			query.setRequestHandler(ISolrConstants.DETAILS_QUERY_TYPE_NAME);
			query.setRows(ids.size());
			return searcher.query(query);
		} catch (Exception exception) 
		{
			Log.error(
					MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
					exception);
			throw new SystemInternalFailureException();
		} 
	}
	
	/**
	 * Shuts down this search engine.
	 */
	public void shutdown()
	{
		try
		{
			searcher.shutdown();
		} catch (Exception exception)
		{
			Log.error(
					MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
					exception);			
		}

		try
		{
			if (highlighter != null)
			{
				highlighter.shutdown();
			}
		} catch (Exception exception)
		{
			Log.error(
					MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
					exception);			
		}
	}

	@Override
	public QueryResponse findLocationsByAssocName(String associationName)throws SystemInternalFailureException 
	{
			try 
			{
				final SolrQuery query = new SolrQuery(ISolrConstants.ALL);
				query.addFilterQuery("category_code:\"branch\"","association_name:\""+associationName+"\"");
				query.setRequestHandler(ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME);
				query.addSort("region",ORDER.asc);
				query.addSort("city",ORDER.asc);
				query.setRows(1000);
				return searcher.query(query);
			} catch (Exception exception) 
			{
				Log.error(
						MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
						exception);
				throw new SystemInternalFailureException();
			}	
	}
	
	@Override
	public QueryResponse findHelpLinesByAssocId(String associationId)throws SystemInternalFailureException 
	{	
			try 
			{
				SolrQuery query = new SolrQuery(ISolrConstants.ALL);
				query.addFilterQuery("category_code:\"hl\"","association_id:\""+associationId+"\"");
				query.setRequestHandler(ISolrConstants.STANDARD_NO_DISMAX_QUERY_TYPE_NAME);
				query.setRows(1000);
				return searcher.query(query);
			} catch (Exception exception) 
			{
				Log.error(
						MessageCatalog._100003_SEARCH_ENGINE_FAILURE,
						exception);
				throw new SystemInternalFailureException();
			}	
	}
}