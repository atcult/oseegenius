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
package com.atc.osee.genius.holdings;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.lucene.document.Document;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holdings data SOLR search component.
 * From Library of Congress web site:
 * 
 * <pre>
 * Organization holding the item or from which it is available. 
 * May also contain detailed information about how to locate the item in a collection.
 * Several subfields duplicate those in the 863-865 Enumeration and Chronology fields. 
 * They are used in field 852 when there is no 863-868 field in the holdings information, 
 * for example, for holdings for single-part items or for multipart and serial items reported 
 * at Holdings level 1 or 2 (Leader/17, Encoding level, code 1 or 2).
 * Field is repeated when holdings are reported for multiple copies of an item and the location data elements vary. 
 * When other holdings information fields are associated with multiple 852 fields, the configuration of the 
 * holdings report must be considered to assure that these fields are implicitly linked. 
 * A description of the treatment required for 852 holdings information clusters is given under the heading 
 * Separate and Embedded Holdings Information in the Introduction to this publication. 
 * Subfield $8 is used in this field to sequence multiple related holdings records."
 * 
 * </pre>
 * 
 * @author agazzarini
 * @since 1.0
 */
public class HoldingsComponent extends SearchComponent 
{
	public static final String INITIAL_CONTEXT_FACTORY_PARAMETER_NAME = "naming-context-factory";
	public static final String PROVIDER_URL_PARAMETER_NAME = "naming-provider-url";
	public static final String DATASOURCE_PARAMETER_NAME = "datasource-jndi-name";;
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(HoldingsComponent.class);
		
	protected static final String COMPONENT_NAME = "holdings";
		
	protected SolrParams parameters;

	protected DataLoaderStrategy strategy;
	protected DataSource datasource;
	
	@Override
	public String getDescription() 
	{
		return "Osee Genius -S- holdings data plugin.";
	}

	@Override
	public String getSource() 
	{
	    return "N.A.";
	}
	
	@Override
	public String getVersion() 
	{
		return "1.0";
	}
	
	@Override
	public void prepare(final ResponseBuilder builder) throws IOException 
	{		
		// Nothing to be done here...
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void init(final NamedList args) 
	{
		parameters = SolrParams.toSolrParams(args);
		
		try
		{
			strategy = (DataLoaderStrategy) Class.forName(parameters.get("strategy-implementor")).newInstance();
			
			Hashtable environment = new Hashtable();
			
			String datasourceName = parameters.get("datasource-jndi-name");
			if (datasourceName == null || datasourceName.trim().length() == 0)
			{
				throw new IllegalArgumentException("Datasource name cannot be null.");
			}
			
			String namingFactory = parameters.get("naming-context-factory");
			String providerUrl = parameters.get("naming-provider-url");
			
			if (namingFactory != null && namingFactory.trim().length() != 0)
			{
				environment.put(Context.INITIAL_CONTEXT_FACTORY, namingFactory);				
			}

			if (providerUrl != null && providerUrl.trim().length() != 0)
			{
				environment.put(Context.PROVIDER_URL, providerUrl);				
			}
			
			Context namingContext = environment.isEmpty() ? new InitialContext() : new InitialContext(environment);
			datasource = (DataSource) namingContext.lookup(datasourceName);
			datasource.getConnection().close();
			strategy.setDataSource(datasource);
		} catch (Exception exception)
		{
			LOGGER.error(getName() + " cannot be started.", exception);
			strategy = new NullDataLoaderStrategy();
		} 
	}	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void process(final ResponseBuilder builder) throws IOException 
	{
		if (builder.getResults() == null)
		{
			return;
		}
		
		SolrIndexSearcher searcher = builder.req.getSearcher();
		IndexSchema schema = searcher.getSchema();

		Set<String> fieldset = new HashSet<String>(1);
		fieldset.add(schema.getUniqueKeyField().getName());
		
		NamedList result = new SimpleOrderedMap();
		for (DocIterator iterator = builder.getResults().docList.iterator(); iterator.hasNext();)
		{
			int documentId = iterator.nextDoc();
			Document document = searcher.doc(documentId, fieldset);

			String bibliographicLevelCode = document.get("bibliographic_level");
			
			String searchDocumentUri = null;
			
			String documentUri = null;
			
			// BZ #1609
			if (bibliographicLevelCode != null && ("a".equals(bibliographicLevelCode) || "b".equals(bibliographicLevelCode)))
			{
				searchDocumentUri = document.get("parent_id");
				documentUri = schema.printableUniqueKey(document);
				if (searchDocumentUri == null || searchDocumentUri.trim().length() == 0)
				{
					searchDocumentUri = documentUri;
				}
			} else 
			{
				final String originalId = document.get("original_id");
				if (originalId != null && originalId.trim().length() != 0)
				{
					documentUri = schema.printableUniqueKey(document);
					searchDocumentUri = originalId;
				} else
				{
					searchDocumentUri = schema.printableUniqueKey(document);
					documentUri = searchDocumentUri;
				}
			}
			//fix gregoriana archive and bib
			final String BIB_SUFFIX = "BIB";
			final String ARC_SUFFIX = "ARC";
			if (searchDocumentUri.endsWith(BIB_SUFFIX) || searchDocumentUri.endsWith(ARC_SUFFIX)) {
				searchDocumentUri = searchDocumentUri.replaceAll(BIB_SUFFIX, "").replaceAll(ARC_SUFFIX, "");
			}
			
			try
			{
				result.add(documentUri, strategy.getHoldingData(searchDocumentUri));
			} catch (UnableToLoadHoldingsDataException exception)
			{
				result.add(documentUri, "ERROR");
			}
		}
		
		if (result.size() != 0)
		{
			builder.rsp.add(COMPONENT_NAME, result);
		}
	}
}
