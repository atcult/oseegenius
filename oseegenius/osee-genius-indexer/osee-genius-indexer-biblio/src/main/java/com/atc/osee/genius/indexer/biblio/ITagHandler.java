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
package com.atc.osee.genius.indexer.biblio;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.marc4j.marc.Record;

/**
 * Tag handler definition interface.
 * 
 * @author agazzarini
 * @since 1.0
 */
public interface ITagHandler 
{
	/**
	 * Extracts a value from the given data.
	 * Returns a value object computed according with the concrete implementor
	 * rules.
	 * 
	 * @param tagMappings the tag mappings.
	 * @param record the marc record.
	 * @param settings the settings of biblio indexer.
	 * @param core the SOLR core.
	 * @param document the current SOLR input document.
	 * @return a value computed from the data above according with concrete implementor rules.
	 */
	Object getValue(String tagMappings, Record record, SolrParams settings, SolrCore core, SolrInputDocument document);
}