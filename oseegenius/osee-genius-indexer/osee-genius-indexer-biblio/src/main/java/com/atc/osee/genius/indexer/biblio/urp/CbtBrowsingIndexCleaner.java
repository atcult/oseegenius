package com.atc.osee.genius.indexer.biblio.urp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.browsing.IAuthorityAccessObject;
import com.atc.osee.genius.indexer.biblio.browsing.aao.impl.DummyAuthorityAccessObject;

@SuppressWarnings("rawtypes")
public class CbtBrowsingIndexCleaner extends UpdateRequestProcessor {
	private Logger LOGGER = LoggerFactory.getLogger(CbtBrowsingIndexCleaner.class);
	private SolrServer thisServer;
	
	private NamedList params;
	
	final String indexName;
	IAuthorityAccessObject dummy = new DummyAuthorityAccessObject();
	IAuthorityAccessObject aao; 
	
	/**
	 * Builds a new processor.
	 * 
	 * @param datasource the datasource of the embedded database.
	 * @param next the next processor in the chain.
	 */
	public CbtBrowsingIndexCleaner(
			final NamedList params,
			final SolrQueryRequest request,
			final SolrCore thisCore,
			final UpdateRequestProcessor next, 
			final IAuthorityAccessObject aao) {
		super(next);
		
		this.params = params;
		this.indexName = thisCore.getName();
		this.aao = aao;
		thisServer = new EmbeddedSolrServer(thisCore.getCoreDescriptor().getCoreContainer(), thisCore.getName());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void processCommit(final CommitUpdateCommand command) throws IOException 
	{
		if (Boolean.getBoolean("ignore.cleaner")) {
			LOGGER.info("Browsing Index cleaner has been ignored for this indexing process.");
			super.processCommit(command);
			return;
		}
		
		if (command.optimize)
		{	
			try {
				aao.loadHeadings(params);
			} catch (final Exception exception) 
			{
				// Exception has been already logged out in AAO.
				aao = dummy;
				super.processCommit(command);
				return;
			}	
			
			LOGGER.info("Browsing index cleaner starts...");
			
			String lastSentCursorMark = "*";
			String nextCursorMark = null;

			final SolrQuery queryOnBrowser = new SolrQuery("*:*");
			queryOnBrowser.setRows(1000);
			queryOnBrowser.setSort("sortKey", ORDER.asc);
			queryOnBrowser.set("cursorMark", lastSentCursorMark);
			
			
			QueryResponse sizeResponse = null;
			
			try {
				sizeResponse = thisServer.query(queryOnBrowser);	
			} catch (Exception e) {
				throw new IOException(e);
			}
			
			final int howManyHeadings = (int) sizeResponse.getResults().getNumFound();
			
			try {
				
				LOGGER.info("Browsing Index cleaner found " + howManyHeadings + " to be checked for " + indexName + " index.");
				
				final List<String> deleteCandidates = new ArrayList<String>();
				
				while (!lastSentCursorMark.equals(nextCursorMark)) {
					final QueryResponse response = thisServer.query(queryOnBrowser);
					lastSentCursorMark = queryOnBrowser.get("cursorMark");
					nextCursorMark = response.getNextCursorMark();
					
					queryOnBrowser.set("cursorMark", nextCursorMark);
					final SolrDocumentList results = response.getResults();
					if (results.getNumFound() == 0) {
						break;
					}
					
					for (final SolrDocument document : results) {
						try {
							final String recordKey = (String) document.getFieldValue("sortKey");
							if (!aao.isValidHeading(recordKey)) {
								deleteCandidates.add(recordKey);																	
							} 
						} catch (final Exception ignore) {
							LOGGER.error("Error while checking the heading consistency.", ignore);
						} 
					}
				}

				LOGGER.info("Browsing Index cleaner " + indexName + " detected " + deleteCandidates.size() + " candidates.");
				
				for (final String key : deleteCandidates) {
					thisServer.deleteById(key);
				}

				LOGGER.info("Browsing Index cleaner " + indexName + " deleted " + deleteCandidates.size() + " candidates.");

				thisServer.commit();
				aao.shutdown();
			} catch (final Exception exception) {
				LOGGER.error("Error while deep paging", exception);
			}
		}
		super.processCommit(command);
	}
}
