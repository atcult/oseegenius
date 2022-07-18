package com.atc.osee.genius.indexer.biblio.urp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UpdateRequestProcessor} that intercepts "optimize" requests for cleaning browsing indexes and tables.
 * 
 * @author agazzarini
 * @since 1.0
 */
public final class BrowsingIndexesCleaner extends UpdateRequestProcessor 
{
	private Logger LOGGER = LoggerFactory.getLogger(BrowsingIndexesCleaner.class);
	
	private Map<String, SolrServer> servers = new HashMap<String, SolrServer>(5);
	private DataSource datasource;
//	private final SolrIndexSearcher reader;
	 
	private SolrServer mainServer;
	
	/**
	 * Builds a new processor.
	 * 
	 * @param datasource the datasource of the embedded database.
	 * @param next the next processor in the chain.
	 */
	public BrowsingIndexesCleaner(final DataSource datasource, final SolrCore mainCore, final UpdateRequestProcessor next, final SolrIndexSearcher reader) 
	{
		super(next);
		this.datasource = datasource;
//		this.reader = reader;
		
		collectBrowsingEndpoints(mainCore);
		
		mainServer = new EmbeddedSolrServer(mainCore.getCoreDescriptor().getCoreContainer(), "main");
	}
	
	@Override
	public void processCommit(final CommitUpdateCommand command) throws IOException 
	{
		if (command.optimize)
		{
			LOGGER.info("Browsing indexes & table cleaner starts...");
			Connection connection = null;

			try 
			{
				connection = datasource.getConnection();
				
				for (Entry<String, SolrServer> entry : servers.entrySet())
				{
					String indexName = entry.getKey();
					LOGGER.info("Start cleaning index " + indexName);
					
					cleanIndex(indexName, connection, entry.getValue());
				}
			} catch(Exception exception)
			{
				LOGGER.info("Browsing cleaner caught a SQLException...", exception);
			}
		}
		super.processCommit(command);
	}
	
	private void cleanIndex(final String indexName, final Connection connection, final SolrServer searcher) 
	{
		int howManyHeadings = 0;
		int howManyHeadingsDeleted = 0;
		
		SolrQuery reusableIndexQuery = new SolrQuery();
		SolrQuery reusableCountOnMainQuery = new SolrQuery("*:*");
		reusableCountOnMainQuery.setRequestHandler("standard");
		reusableCountOnMainQuery.setRows(0);
		reusableCountOnMainQuery.setFacet(false);
		
		reusableIndexQuery.setRequestHandler("standard");
		reusableIndexQuery.setFacet(false);

		PreparedStatement countStatement = null;
		PreparedStatement selectStatement = null;
		PreparedStatement deleteStatement = null;
		ResultSet countRs = null;
		ResultSet selectRs = null;
		try 
		{
			countStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + indexName);
			selectStatement = connection.prepareStatement("SELECT SORT_KEY FROM " + indexName);
			deleteStatement = connection.prepareStatement("DELETE FROM " + indexName + " WHERE SORT_KEY=?");
			countRs = countStatement.executeQuery();
			howManyHeadings = (countRs.next()) ? countRs.getInt(1) : 0;
			
			// Sanity check: if the table is empty return immediately!
			if (howManyHeadings == 0)
			{
				LOGGER.info("Index " + indexName + " table is empty??");
				return;
			} else
			{
				LOGGER.info("Index " + indexName + " has " + howManyHeadings + " headings.");				
			}
			
			selectStatement.setFetchSize(1000);
			selectRs = selectStatement.executeQuery();
			
			while (selectRs.next())
			{
				String sortKey = selectRs.getString(1);
				reusableIndexQuery.setQuery("sortKey:\""+sortKey+"\"");
				
				QueryResponse response = searcher.query(reusableIndexQuery);
				if (response.getResults().getNumFound() == 0)
				{
					LOGGER.info("The heading <" + sortKey +"> belonging to index " + indexName + " has no entry in the browse index and it will be deleted.");				
					deleteStatement.setString(1, sortKey);
					howManyHeadingsDeleted  += deleteStatement.executeUpdate();					
				} else
				{
					SolrDocument document = response.getResults().get(0);
					if ((Boolean)document.getFieldValue("isPreferredForm"))
					{
						reusableCountOnMainQuery.setFilterQueries(indexName + ":\"" + sortKey + "\"");
						long frequency = mainServer.query(reusableCountOnMainQuery).getResults().getNumFound();
						if (frequency == 0)
						{
							LOGGER.info("The heading <" + sortKey +"> belonging to index " + indexName + " has no entry in the main index and it will be deleted.");				
							searcher.deleteById(sortKey);
							
							deleteStatement.setString(1, sortKey);
							howManyHeadingsDeleted  += deleteStatement.executeUpdate();	
						} else
						{
							LOGGER.debug("The heading <" + sortKey +"> belonging to index " + indexName + " has " + frequency + " entries in the main index");
						}
					}
				}
			}
			
			if (howManyHeadingsDeleted > 0)
			{
				LOGGER.info(
						"Browsing cleaner for index " 
						+ indexName 
						+": removed " 
						+ howManyHeadingsDeleted 
						+ " on a total of " 
						+ howManyHeadings 
						+ " headings.");
				searcher.commit();
			} else 
			{
				LOGGER.info("Browsing cleaner removed no headings for index " + indexName);				
			}
		} catch (Exception exception) 
		{
			LOGGER.error("Unable to clean index " + indexName, exception);
		} finally 
		{
			try { if (countRs != null) countRs.close(); } catch (Exception ignore) { }
			try { if (countStatement != null) countStatement.close(); } catch (Exception ignore) { }
			try { if (deleteStatement != null) deleteStatement.close(); } catch (Exception ignore) { }
			try { if (selectStatement != null) selectStatement.close(); } catch (Exception ignore) { }
			try { if (selectRs != null) selectRs.close(); } catch (Exception ignore) { }
		}
	}

	void collectBrowsingEndpoints(final SolrCore mainCore)
	{
		CoreContainer container = mainCore.getCoreDescriptor().getCoreContainer();
		for (SolrCore dependentCore : container.getCores())
		{
			 String coreName = dependentCore.getName();
	    	  if (!"main".equals(coreName) && !"autocomplete".equals(coreName))
	    	  {
	    		  servers.put(coreName, new EmbeddedSolrServer(container, coreName));
	    	  }
		}
	}
}