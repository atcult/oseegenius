package com.atc.osee.genius.indexer;

import java.io.IOException;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerUtils;
import org.apache.solr.handler.loader.ContentStreamLoader;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;

/**
 * OseeGenius UpdateRequestHandler.
 * Basically provide facade behaviour on top of the standard XmlUpdateRequestHandler.
 * 
 * Specifically, each time a commit or optimize command is issued, a corresponding commit or optimize
 * command is sent to each dependent core (e.g. browsing indexes).
 * 
 * @author agazzarini
 * @since 1.0
 */
public class UpdateRequestHandler extends org.apache.solr.handler.UpdateRequestHandler 
{
	@Override
	public void handleRequestBody(SolrQueryRequest request, SolrQueryResponse response) throws Exception 
	{
		SolrParams params = request.getParams();
		String updateChainName = null;
		updateChainName = (String) initArgs.get(UpdateParams.UPDATE_CHAIN);
		
		UpdateRequestProcessorChain processorChain = request.getCore().getUpdateProcessingChain(updateChainName);
		UpdateRequestProcessor processor = processorChain.createProcessor(request, response);

		try 
		{
			ContentStreamLoader documentLoader = newLoader(request, processor);

			Iterable<ContentStream> streams = request.getContentStreams();
			if (streams == null) 
			{
				if (
						!handleCommit(request, processor, params, false) 
						&& 
						!RequestHandlerUtils.handleRollback(request, processor, params, false)) 
				{
					throw new SolrException(
							SolrException.ErrorCode.BAD_REQUEST,
							"missing content stream");
				}
			} else 
			{
				for (ContentStream stream : streams) 
				{
					documentLoader.load(request, response, stream, processor);
				}

				handleCommit(request, processor, params, false);
				RequestHandlerUtils.handleRollback(request, processor, params, false);
			}
		} finally 
		{
			processor.finish();
		}
	}
	
	/**
	 * 
	 * @param request
	 * @param processor
	 * @param params
	 * @param force
	 * @return
	 * @throws IOException
	 */
	public static boolean handleCommit(SolrQueryRequest request, UpdateRequestProcessor processor, SolrParams params, boolean force ) throws IOException
	{
		if( params == null ) 
		{
	    	params = new MapSolrParams( new HashMap<String, String>() ); 
		}
	    
	    boolean optimize = params.getBool( UpdateParams.OPTIMIZE, false );
	    boolean commit   = params.getBool( UpdateParams.COMMIT,   false );
	    
	    if( optimize || commit || force ) 
	    {
	      CommitUpdateCommand cmd = new CommitUpdateCommand(request, optimize );
	      cmd.waitSearcher = params.getBool( UpdateParams.WAIT_SEARCHER, cmd.waitSearcher );
	      cmd.expungeDeletes = params.getBool( UpdateParams.EXPUNGE_DELETES, cmd.expungeDeletes);      
	      cmd.maxOptimizeSegments = params.getInt(UpdateParams.MAX_OPTIMIZE_SEGMENTS, cmd.maxOptimizeSegments);
	      processor.processCommit( cmd );
	      
	      SolrCore mainCore = request.getCore();
	      String name = mainCore.getName();
	      CoreContainer container = mainCore.getCoreDescriptor().getCoreContainer();
	      for (SolrCore dependentCore : container.getCores())
	      {
	    	  String coreName = dependentCore.getName();
	    	  if (!name.equals(coreName))
	    	  {
	    		  SolrServer server = new EmbeddedSolrServer(container, coreName);
	    		  try 
	    		  {
	    			  if (optimize)
	    			  {
	    				  log.info("Issuing an OPTIMIZE command to " + coreName + " OseeGenius core.");
	    				  server.optimize(true,  cmd.waitSearcher, cmd.maxOptimizeSegments);
	    			  } else 
	    			  {
	    				  log.info("Issuing a COMMIT command to " + coreName + " OseeGenius core.");
	    				  server.commit(true,  cmd.waitSearcher);
	    			  }
	    		  } catch (SolrServerException exception) 
	    		  {
	    			  log.error("Error while commiting / optimizing "+ coreName + " OseeGenius core.", exception);
	    		  }
	    	  }
	      }
	      return true;
	    }
	    return false;
	  }	
}