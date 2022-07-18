package com.atc.osee.genius.indexer.biblio.urp;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For those OG that use embedded (e.g. HSQL) database, a clean is periodically needed 
 * in order to remove unused headings from indexes and tables.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class BrowsingIndexesCleanerFactory extends UpdateRequestProcessorFactory 
{
	private Logger LOGGER = LoggerFactory.getLogger(BrowsingIndexesCleanerFactory.class);
	private DataSource datasource;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void init(final NamedList args) 
	{
		SolrParams parameters = SolrParams.toSolrParams(args);
		String datasourceJndiName = parameters.get("datasource");
		try 
		{
			datasource = (DataSource) new InitialContext().lookup(datasourceJndiName);
			datasource.getConnection().close();
		} catch (Exception exception) 
		{
			LOGGER.error("Unable to lookup / use the embedded datasource " + datasourceJndiName, exception);
			datasource = null;
		}
	}
	
	@Override
	public UpdateRequestProcessor getInstance(
			final SolrQueryRequest request, 
			final SolrQueryResponse response, 
			final UpdateRequestProcessor next) 
	{
		if (datasource != null)
		{
			return new BrowsingIndexesCleaner(datasource, request.getCore(), next, request.getSearcher());
		} else
		{
			LOGGER.warn("The browser cleaner interceptor is unable to work...missing datasource. Check your configuration!");
			return next;
		}
	}
}