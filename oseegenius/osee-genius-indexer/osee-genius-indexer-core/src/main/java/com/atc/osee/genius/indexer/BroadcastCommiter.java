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

public class BroadcastCommiter extends UpdateRequestProcessor 
{	
	private Logger LOGGER = LoggerFactory.getLogger(BroadcastCommiter.class);
	
	public BroadcastCommiter(final UpdateRequestProcessor next) 
	{
		super(next);
	}
	
	@Override
	public void processCommit(final CommitUpdateCommand cmd) throws IOException 
	{
		final SolrCore mainCore = cmd.getReq().getCore();
		final String name = mainCore.getName();
		final CoreContainer container = mainCore.getCoreDescriptor().getCoreContainer();
		if (!cmd.optimize) 
		{
			for (final SolrCore dependentCore : container.getCores())
			{
				final String coreName = dependentCore.getName();
				if (!name.equals(coreName))
				{
					final SolrServer server = new EmbeddedSolrServer(container, coreName);
					try 
					{
						LOGGER.info("Issuing a COMMIT command to " + coreName + " OseeGenius core.");
						server.commit(true, cmd.waitSearcher);
					} catch (SolrServerException exception) {
						LOGGER.error("Error while commiting / optimizing " + coreName + " OseeGenius core.", exception);
					} 
				}
			}
		}
		super.processCommit(cmd);		
	}
}