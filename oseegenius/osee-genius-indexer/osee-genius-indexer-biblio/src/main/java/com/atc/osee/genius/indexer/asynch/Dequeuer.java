package com.atc.osee.genius.indexer.asynch;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.IndexerResult;
import com.atc.osee.genius.indexer.biblio.CompiledExpression;
import com.atc.osee.genius.indexer.biblio.IDecorator;
import com.atc.osee.genius.indexer.biblio.MarcRecordBuilder;
import com.atc.osee.genius.indexer.biblio.handlers.ToMarcXmlWithRemoval;

public class Dequeuer extends Thread 
{
	final SynchronousQueue<Data> queue;
	final int totalCount;
	final AtomicInteger counter = new AtomicInteger(0);
	final ExecutorService executors;
	final MarcRecordBuilder builder = new MarcRecordBuilder();
	final Map<String, CompiledExpression> expressions;
	final Map<String, CompiledExpression> dependentExpressions;
	final SolrCore core;
	final SolrParams settings;
	final IndexerResult result;
	final SolrServer indexer;
	int threadsNo;
	final DataSource datasource;
	final static String HOLDING_DATA_HAS_BEEN_REMOVED_BECAUSE_TOO_LONG = "1";
	final HoldingDataDAO dao;
	final ToMarcXmlWithRemoval marcxml =  new ToMarcXmlWithRemoval();
	final Boolean isUnimarcUtf8;  //this parameter tells me if it is the case of unimarc  && utf8 encoding
	final Boolean isMarc21;
	final Boolean isMarc_8;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Dequeuer.class);
	
	private final String idFieldName;
	private final UnimarcPunctuactor punct;
	
	public Dequeuer(
			final SynchronousQueue<Data> queue, 
			final int totalCount, 
			final Map<String, CompiledExpression> expressions, 
			final SolrParams settings, final SolrCore core, 
			final IndexerResult result, 
			final Map<String, CompiledExpression> dependentExpressions, 
			final SolrServer indexer,
			final int threadNo, 
			final DataSource datasource,
			final Boolean isUnimarcUtf8,
			final Boolean isMarc21,
			final Boolean isMarc_8) 
	{
		this.queue = queue;
		this.totalCount = totalCount;
		this.expressions = expressions;
		this.core = core;
		this.settings = settings;
		this.result = result;
		this.dependentExpressions = dependentExpressions;
		this.indexer = indexer;
		this.executors = Executors.newFixedThreadPool(threadNo); 
		this.datasource = datasource;
		this.dao = new HoldingDataDAO(datasource, MarcFactory.newInstance());
		this.punct = new UnimarcPunctuactor();
		this.isUnimarcUtf8 = isUnimarcUtf8;
		this.isMarc21 = isMarc21;
		this.isMarc_8 = isMarc_8;
		
		idFieldName = System.getProperty("id.field.name") != null ? System.getProperty("id.field.name") : "id";
		
		LOGGER.info("Starting a new Dequeuer with " + threadNo + " workers and a total count of records of " + totalCount);
	}
	
	//metdodo creato per il problema dell'indicizzazione dell'unimarc con leader errate
		protected Record buildRecord(final byte [] leader, final byte [] body, final int length) {
			try {
				final Record record = MarcFactory.newInstance().newRecord();  				
				final byte [] l = isUnimarcUtf8 ? update(leader) : leader;
				
				builder.parseRecord(record, l, body, length);
				
				if (isUnimarcUtf8) {
					record.setLeader(MarcFactory.newInstance().newLeader(new String(leader)));
				}
				return record;
			} catch (Exception exception) {
				LOGGER.info("MARC build record failure.", exception);
				counter.incrementAndGet();
				return null;
			}
		}
		
	/**
	 * It create a new Leader without first blank character when the file is Unimarc and utf8
	 * 
	 * @param leader
	 * @return byte[] newleader
	 */
	
	private byte[] update(byte[] leader) {	
		String stringLeader = new String(leader);
		stringLeader = stringLeader.substring(0, 9) + "a" + stringLeader.substring(10);		
		return stringLeader.getBytes();
	}
	
	@Override
	public void run() 
	{
		while (counter.intValue() < totalCount)
		{
			final Data data = queue.poll();
			if (data != null)
			{
				counter.incrementAndGet();
				executors.execute(new Runnable()
				{
					@Override
					public void run() 
					{
						// TODO: Cache Marc records
						Record record = MarcFactory.newInstance().newRecord(); 
												
						builder.parseRecord(record, data.leader, data.body, data.length);
						final SolrInputDocument document = new SolrInputDocument();
						
						//fix punteggiatura unimarc	
						if (!isMarc21) {
							
							//fix diacritici in unimarc
							if(isUnimarcUtf8) {
								record = buildRecord(data.leader, data.body, data.length);
							}
							
							String marc = (String) marcxml.getValue("990", record, null, null, document);
							document.setField("marc_xml", marc);
							record = punct.record(record);
						}
							
						// #BZ 3569
						int amicusNumber = -1;
						final DataField recordWithoutHoldingDataMarkerField = (DataField) record.getVariableField("947");
						if (recordWithoutHoldingDataMarkerField != null) {
							LOGGER.error("HOLDING DATA REMOVED MARKER IS NOT NULL!");
							final Subfield subfield = recordWithoutHoldingDataMarkerField.getSubfield('a');
							LOGGER.error("HOLDING DATA REMOVED MARKER IS NOT NULL --> SUBFIELD " + subfield);
							if (subfield != null && HOLDING_DATA_HAS_BEEN_REMOVED_BECAUSE_TOO_LONG.equals(subfield.getData())) {
								try {
									amicusNumber = getAmicusNumber(record);
									dao.addholdingData(record, amicusNumber);
									LOGGER.info("Record " + amicusNumber + " has been enriched with " + record.getVariableFields("852").size() + " 852 tags");
								} catch (Exception exception) {
									LOGGER.error("Unable to gather holding data for record " + amicusNumber, exception);
								}
							}
						}
						
						try 
						{							
							for (Entry<String, CompiledExpression> entry : expressions.entrySet())
							{
								final String field = entry.getKey();
								final CompiledExpression expression = entry.getValue();
								Object value = expression.tagHandler.getValue(expression.argument, record, settings, core, document);
								for (IDecorator decorator : expression.chain)
								{
									value = decorator.decorate(record, field, value);
								}
								
								handleValue(document, field, value);	
							}

							try 
							{
								indexDocument(document, result);				
								for (Entry<String, CompiledExpression> entry : dependentExpressions.entrySet())
								{
									final String field = entry.getKey();
									final CompiledExpression expression = entry.getValue();
									Object value = expression.tagHandler.getValue(expression.argument, record, settings, core, document);
									for (IDecorator decorator : expression.chain)
									{
										value = decorator.decorate(record, field, value);
									}
									
									handleValue(document, field, value);	
								}
							} catch (Exception mainDocumentNotIndexed)
							{
								// Ignore
							}		
						} catch (Exception exception)
						{
							LOGGER.error("Unable to create / manipulate the document associated with id " + document.getFieldValue(idFieldName) + ", reason: " + exception.getMessage(), exception);
						} 
					}
				});
			}
		}
		
		LOGGER.info(totalCount + " MARC records has been queued [" + (Runtime.getRuntime().freeMemory() / 1048576.0f) + " / " + (Runtime.getRuntime().maxMemory()/1048576.0f) + "]");		
		
		long previousCheckpoint = 0L;
		while (((ThreadPoolExecutor)executors).getCompletedTaskCount() < totalCount)
		{
			try { Thread.sleep(10000); } catch (Exception e) { }
			long completedTasks = ((ThreadPoolExecutor)executors).getCompletedTaskCount();
			
			long percentage  = (completedTasks - previousCheckpoint) / 10;
			previousCheckpoint = completedTasks;
			
			LOGGER.info( completedTasks + " / " + totalCount + " (about " + percentage + " rec/sec)  [" + (Runtime.getRuntime().freeMemory() / 1048576.0f) + " / " + (Runtime.getRuntime().maxMemory()/1048576.0f) + "]");
		}

		LOGGER.info( totalCount + " MARC records have been indexed.");
		
		executors.shutdown();
	}
	
	public void shutdown() {
		try { counter.set(0); } catch (Exception ignore) { }
		try { executors.shutdownNow(); } catch (Exception ignore) { }
		
	}
	
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
	 * Indexes the given document in SOLR.
	 * Note that this is an on-line indexing...that means SOLR must be running and up.
	 * 
	 * @param document the SOLR input document.
	 * @param result the result accumulator for the current indexing process.
	 */
	protected void indexDocument(final SolrInputDocument document, final IndexerResult result) throws Exception
	{
		if (!document.isEmpty())
		{
			try 
			{
		        indexer.add(document);
		        result.incrementIndexedCounter();
			} catch (Exception exception)
			{
				result.incrementFailedCounter();
				result.addFailedDetails(exception.getMessage());
				
				LOGGER.error("Unable to index the document associated with id " + document.getFieldValue(idFieldName) + ", reason: " + exception.getMessage(), exception);
				throw exception;
			}
		} 
		
//		if (false)
//		{
//			LOGGER.info("---------------------------------------------------");
//			for (Entry<String, SolrInputField> entry : document.entrySet())
//			{
//				LOGGER.info(entry.getKey() + " = " + entry.getValue().getValue());
//			}
//		}
	}
	
	protected int getAmicusNumber(Record record) {
		String tmpResult = null;
		DataField f = (DataField) record.getVariableField("036");
		if (f != null) {
			Subfield s = f.getSubfield('a');
			if (s != null) {
				String data = s.getData();
				if (data != null && data.trim().length() != 0) {
					return toInt(data);
				}
			}
		} 
		
		String an = record.getControlNumber();
		return toInt(an);
	}
	
	protected int toInt(String value) {
		StringBuilder builder = new StringBuilder();
		boolean firstValidDigitHasBeenMet = false;
		for (int i = 0; i < value.length(); i++) {
			final char ch = value.charAt(i);
			
			if (firstValidDigitHasBeenMet) {
				builder.append(ch);				
				continue;
			}
			
			if (ch != '0' && !firstValidDigitHasBeenMet) {
				firstValidDigitHasBeenMet = true;
				builder.append(ch);			
			} else {
				continue;
			}
		}
		return Integer.parseInt(builder.toString());
	}
}
