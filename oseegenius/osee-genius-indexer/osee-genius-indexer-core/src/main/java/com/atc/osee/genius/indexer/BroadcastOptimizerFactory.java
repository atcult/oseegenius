package com.atc.osee.genius.indexer;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

/**
 * A SOLR URP for broadcasting commit and optimize commands among all SOLR cores.
 * 
 * @author agazzarini
 * @since 1.2
 */
public class BroadcastOptimizerFactory extends UpdateRequestProcessorFactory 
{	
	@Override
	public UpdateRequestProcessor getInstance(
			final SolrQueryRequest req,
			final SolrQueryResponse rsp, 
			final UpdateRequestProcessor next) 
	{
		return new BroadcastOptimizer(next);
	}
}