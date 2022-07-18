package com.atc.osee.genius.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

public class BufferedSolrServer extends EmbeddedSolrServer 
{
	private static final long serialVersionUID = 6835747832292941359L;

	private final List<SolrInputDocument> buffer;
	private final int bufferSize;
	
	public BufferedSolrServer(CoreContainer coreContainer, String coreName, int bufferSize) 
	{
		super(coreContainer, coreName);
		this.buffer = new ArrayList<SolrInputDocument>(bufferSize);
		this.bufferSize = bufferSize;
	}
	
	@Override
	public synchronized UpdateResponse add(final SolrInputDocument document) throws SolrServerException, IOException 
	{
		if (document != null)
		{
			buffer.add(document);
			if (buffer.size() >= bufferSize)
			{
				
				UpdateResponse response = add(buffer);
				buffer.clear();
				return response;
			}
		} else
		{
			if (!buffer.isEmpty())
			{
				UpdateResponse response = add(buffer);
				buffer.clear();
				return response;			
			}
		}
		return null;
	}
}
