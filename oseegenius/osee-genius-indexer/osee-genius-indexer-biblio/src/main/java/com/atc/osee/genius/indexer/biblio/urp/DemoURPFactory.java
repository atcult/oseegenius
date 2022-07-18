package com.atc.osee.genius.indexer.biblio.urp;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

public class DemoURPFactory extends UpdateRequestProcessorFactory 
{
	@Override
	public UpdateRequestProcessor getInstance(
			final SolrQueryRequest request, 
			final SolrQueryResponse response, 
			final UpdateRequestProcessor next) 
	{
		return new DemoURP(next);
	}
}