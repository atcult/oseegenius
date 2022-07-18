package com.atc.osee.genius.indexer.biblio.urp;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

/**
 * Handles dynamic fields that must be i8nized (at the moment FTP only).
 * 
 * @author agazzarini
 * @since 1.0
 */
public class DynamicI18nFieldsHandlerFactory extends UpdateRequestProcessorFactory 
{
	private String [] fields;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void init(final NamedList args) 
	{
		SolrParams parameters = SolrParams.toSolrParams(args);
		fields = parameters.get("field-names").split(",");
	}
	
	@Override
	public UpdateRequestProcessor getInstance(
			final SolrQueryRequest request, 
			final SolrQueryResponse response, 
			final UpdateRequestProcessor next) 
	{
		return new DynamicI18nFieldsHandler(next, fields);
	}
}