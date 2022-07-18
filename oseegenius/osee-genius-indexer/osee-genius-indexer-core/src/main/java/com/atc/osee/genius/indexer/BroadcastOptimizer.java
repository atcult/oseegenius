package com.atc.osee.genius.indexer;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastOptimizer extends UpdateRequestProcessor 
{	
	private Logger LOGGER = LoggerFactory.getLogger(BroadcastOptimizer.class);
	
	public BroadcastOptimizer(final UpdateRequestProcessor next) 
	{
		super(next);
	}
	
	@Override
	public void processCommit(final CommitUpdateCommand cmd) throws IOException 
	{
		final SolrCore mainCore = cmd.getReq().getCore();
		final String name = mainCore.getName();
		final CoreContainer container = mainCore.getCoreDescriptor().getCoreContainer();
		if (cmd.optimize) 
		{
			for (final SolrCore dependentCore : container.getCores())
			{
				final String coreName = dependentCore.getName();
				if (!name.equals(coreName))
				{
					final SolrServer server = new EmbeddedSolrServer(container, coreName);
					try 
					{
						LOGGER.info("Issuing an OPTIMIZE command to " + coreName + " OseeGenius core.");
						server.optimize(true, cmd.waitSearcher, cmd.maxOptimizeSegments);
					} catch (SolrServerException exception) {
						LOGGER.error("Error while commiting / optimizing " + coreName + " OseeGenius core.", exception);
					} 
				}
			}
		} 
		super.processCommit(cmd);		
	}
}