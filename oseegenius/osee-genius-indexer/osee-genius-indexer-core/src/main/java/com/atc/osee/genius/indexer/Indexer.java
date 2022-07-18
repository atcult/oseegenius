/*
 	Copyright (c) 2010 @Cult s.r.l. All rights reserved.
	
	@Cult s.r.l. makes no representations or warranties about the 
	suitability of the software, either express or implied, including
	but not limited to the implied warranties of merchantability, fitness
	for a particular purpose, or non-infringement. 
	
	@Cult s.r.l.not be liable for any damage suffered by 
	licensee as a result of using, modifying or distributing this software 
	or its derivates.
	
	This copyright notice must appear in all copies of this software.
 */
package com.atc.osee.genius.indexer;

import java.io.File;
import java.util.Collection;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.log.MessageCatalog;

/**
 * Osee Genius-I core indexer. 
 * This is basically a supertype layer for all (concrete) indexers.
 * 
 * @author agazzarini 
 * @since 1.0
 */
public abstract class Indexer 
{
	public static final String EMPTY_STRING = "";
	protected static final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);
	private static final String MOVE_ON_COMPLETE_PARAMETER_NAME = "moveAfter";
	
	protected SolrQueryRequest request;
	
	protected NamedList<Object> globalConfiguration;
	protected SolrParams requestParams;
	protected SolrParams commonSettings;

	protected SolrCore core;
	protected SolrServer indexer;
	protected int commitTrunkSize;
	
	protected boolean dump;
	protected boolean dummy;
	protected boolean moveAfterComplete;
	
	protected File workedOutDirectory;
	
	/**
	 * Parses a pseudo code line.
	 * Parses a pseudo code line and split it in several tokens.
	 * 
	 * @param line the pseudo code line.
	 * @return an array containing pseudo code line tokens.
	 */
	protected String [] getParsedMappingLine(final String line)
	{
		String [] tokens = line.split("\\(");
		for (int i = 0; i < tokens.length; i++)
		{
			tokens[i] = tokens[i].replaceAll("\\)", "").trim();
		}
		return tokens;
	}
	
	/**
	 * Manages the update of the given SOLR document.
	 *  
	 * @param document the SOLR input document.
	 * @param fieldName the name of target SOLR field.
	 * @param value the value for that document / field. 
	 */
	@SuppressWarnings("rawtypes")
	protected void handleValue(final SolrInputDocument document, final String fieldName, final Object value)
	{
		if (value != null)
		{
			if (value instanceof Collection && !((Collection)value).isEmpty())
			{
				document.setField(fieldName, value);				
			} else 
			{
				document.setField(fieldName, value);
			}
		}
	}
		
	/**
	 * Returns an argument of a given command tokens.
	 * 
	 * @param commandsAndArguments the pseudo command line tokenized.
	 * @return the argument that will passed to the target expression.
	 */
	protected String getArgument(final String [] commandsAndArguments) 
	{
		return (commandsAndArguments != null && commandsAndArguments.length != 0) 
			? commandsAndArguments[commandsAndArguments.length - 1] 
            : EMPTY_STRING;
	}
	
	/**
	 * Indexes the given document in SOLR.
	 * Note that this is an on-line indexing...that means SOLR must be running and up.
	 * 
	 * @param document the SOLR input document.
	 * @param result the result accumulator for the current indexing process.
	 */
	protected void indexDocument(final SolrInputDocument document, final IndexerResult result) throws Exception
	{
		if (!dummy && !document.isEmpty())
		{
			try 
			{
		        indexer.add(document);
		        result.incrementIndexedCounter();
			} catch (Exception exception)
			{
				result.incrementFailedCounter();
				result.addFailedDetails(exception.getMessage());

				LOGGER.error("Unable to index the document associated with id " + document.getFieldValue("id") + ", reason: " + exception.getMessage());
				throw exception;
			}
		} 
		
		if (dump)
		{
			LOGGER.info("---------------------------------------------------");
			for (Entry<String, SolrInputField> entry : document.entrySet())
			{
				LOGGER.info(entry.getKey() + " = " + entry.getValue().getValue());
			}
		}
	}
	
	/**
	 * Sends a commit command.
	 * 
	 * @throws Exception if the commit fails.
	 */
	protected void commitTrunk() throws Exception
	{
		if (!dummy)
		{
			indexer.commit();
		}
	}
	
	/**
	 * Indexing method.
	 * Each concrete implementor must specifcy here what needs to be done.
	 * 
	 * @return the indexer result.
	 * @throws IndexingException in case the indexing process fails.
	 */
	public abstract IndexerResult doIndex() throws IndexingException;
	
	/**
	 * Called once in order to configure a concrete indexer.
	 * 
	 * @param globalConfiguration the indexer raw and global configuration.
	 * @param commonSettings common configuration settings.
	 * @param settings indexer specific configuration settings.
	 * @param core the SOLR core.
	 * @param dummy a flag indicating if the indexer should concretely index or not.
	 * @param dump a flag indicating if the indexer should dump (on log file) the indexing results.
	 * @param requestParams the incoming request parameters.
	 * @throws WrongConfigurationException in case something is wrong with the given configuration.
	 */
	public void configure(
			final SolrQueryRequest request,
			final NamedList<Object> globalConfiguration, 
			final SolrParams commonSettings, 
			final SolrParams settings, 
			final SolrCore core, 
			final boolean dump, 
			final boolean dummy, 
			final SolrParams requestParams) throws WrongConfigurationException
	{
		this.request = request;
		this.moveAfterComplete = requestParams.getBool(	MOVE_ON_COMPLETE_PARAMETER_NAME, true);
		
		this.commonSettings = commonSettings;
		this.globalConfiguration = globalConfiguration;
		this.dummy = dummy;
		this.dump = dump;
		
		this.requestParams = requestParams;
		
		this.commitTrunkSize = commonSettings.getInt("commit.trunk.size", 500);
		this.workedOutDirectory = new File(commonSettings.get("worked.directory"));
//		if (commonSettings.getBool("use.buffered.indexer", true))
//		{
//			this.indexer = new BufferedSolrServer(core.getCoreDescriptor().getCoreContainer(), core.getName(), 10000);			
//		} else
//		{
			this.indexer = new EmbeddedSolrServer(core.getCoreDescriptor().getCoreContainer(), core.getName());
//		}

		doConfigure(settings, core);
	}
	
	/**
	 * Each concrete indexer implementor must define here configuration specific requirements.
	 * 
	 * @param settings the indexer settings.
	 * @param core the solr core
	 * @throws WrongConfigurationException in case of a configuration error.
	 */
	protected abstract void doConfigure(SolrParams settings, SolrCore core) throws WrongConfigurationException;
	
	/**
	 * Moves the given file onto the worked out directory.
	 * This directory is supposed to contain all processed file so when a file is 
	 * processed is immediately moved there in order to further avoid a duplicated process.
	 *  
	 * @param file the file that has been processed.
	 */
	protected void moveToWorkedDirectory(final File file)
	{
		if (file.isDirectory())
		{
			for (File aFile : file.listFiles())
			{
				moveFileToWorkedDirectory(aFile);
			}
		} else 
		{
			moveFileToWorkedDirectory(file);			
		}
	}
	
	/**
	 * Moves the given file under the worked out directory.
	 * 
	 * @param file the file that will be moved.
	 */
	private void moveFileToWorkedDirectory(final File file)
	{
		LOGGER.info(
				String.format(
						MessageCatalog._000012_MOVING_FILE_2_WORKED_DIR, 
						file.getAbsolutePath()));
		
		boolean hasBeenMoved = file.renameTo(new File(workedOutDirectory, file.getName()));
		
		if (hasBeenMoved)
		{
			LOGGER.info(
					MessageCatalog._000013_FILE_MOVED_2_WORKED_DIR,
					file.getName(), 
					workedOutDirectory.getAbsolutePath());
		} else 
		{
			LOGGER.error(
					MessageCatalog._000014_UNABLE_TO_MOVE_WORKED_FILE, 
					file.getName());			
		}
	}
}