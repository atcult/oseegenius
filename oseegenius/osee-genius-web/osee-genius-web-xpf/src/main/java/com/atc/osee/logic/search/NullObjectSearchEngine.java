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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import com.atc.osee.web.SystemInternalFailureException;

/**
 * Null Object Search Engine.
 * The search engine implementation that is used in case the given
 * implementator fails somewhere during initialization.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class NullObjectSearchEngine implements ISearchEngine 
{	
	static final QueryResponse EMPTY_RESPONSE = new QueryResponse()
	{
		private static final long serialVersionUID = 1L;

		private final SolrDocumentList emptyList = new SolrDocumentList();
		private final List<FacetField> emptyFacets = new ArrayList<FacetField>();

		public SolrDocumentList getResults() 
		{
			return emptyList;
		};
		
		public List<FacetField> getFacetFields() 
		{
			return emptyFacets;
		};
	};
	

	@Override
	public QueryResponse getHomepageResources()
	{
		return EMPTY_RESPONSE;
	}
	
	@Override
	public QueryResponse executeQuery(final SolrQuery query)
	{
		return EMPTY_RESPONSE;
	}

	@Override
	public QueryResponse findDocumentByURI(final String uri)
	{
		return EMPTY_RESPONSE;
	}
	
	@Override
	public QueryResponse findAuthDocumentByAuthLink(final String uri)
	{
		return EMPTY_RESPONSE;
	}

	@Override
	public void ping() throws SystemInternalFailureException 
	{
		// Nothing to be done here.
	}

	@Override
	public QueryResponse documents(final Set<String> selectedItems) throws SystemInternalFailureException 
	{
		return EMPTY_RESPONSE;
	}
	
	@Override
	public QueryResponse findLocationsByAssocName(final String associationName) throws SystemInternalFailureException
	{
		return EMPTY_RESPONSE;
	}
	
	@Override
	public QueryResponse findHelpLinesByAssocId(final String associationId) throws  SystemInternalFailureException
	{
		return EMPTY_RESPONSE;
	}

	@Override
	public QueryResponse findDocumentByURIWithMLT(final String uri, final String pageNumber, final String institution, final String ... filter) {
		return null;
	}
}