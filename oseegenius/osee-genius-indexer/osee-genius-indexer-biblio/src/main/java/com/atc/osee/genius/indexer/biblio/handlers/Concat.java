package com.atc.osee.genius.indexer.biblio.handlers;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.Record;

import com.atc.osee.genius.indexer.biblio.TagHandler;

public class Concat extends TagHandler {
	
	private GetValue extractor = new GetValue();
	
	@Override
	public Object getValue(
			final String tagMappings, 
			final Record record, 
			final SolrParams settings, 
			final SolrCore core, 
			final SolrInputDocument document) {
		final String [] expressions = tagMappings.split("\\|\\|");
		
		if (expressions == null || expressions.length == 0) {
			throw new IllegalArgumentException("Expressions list cannot be empty.");
		}
		
		final StringBuilder builder = new StringBuilder();
		for (final String expression : expressions) {
			final Object result = extractor.getValue(expression, record, settings, core, document); 
			if (result == null || !(result instanceof String)) {
				throw new IllegalArgumentException("Expression " + expression + " has been evaluated to null or it is not a single value.");				
			}
			
			builder.append(result);
		}
		return builder.toString();
	}
}
