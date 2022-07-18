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
package com.atc.osee.genius.indexer.biblio.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Heading indexer.
 * 
 * @author aguercio
 * @since 1.2
 */
public final class Heading extends Decorator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Heading.class);
	
	private SolrServer solr;
	private String prefix;
	
	@Override
	public Object decorate(final String fieldName, final Collection<String> value) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		if (value.size() == 1) {
			decorate(fieldName, value.iterator().next());
		}
		
		final List<SolrInputDocument> list = new ArrayList<SolrInputDocument>(value.size());
		for (String aString : value) {
			final SolrInputDocument headingDocument = execute(fieldName, aString, false);
			if (headingDocument != null) {
				list.add(headingDocument);
			}
		}
		
		try  {
			solr.add(list);
		} catch (final Exception exception) {
			LOGGER.error("Unable to update the heading index.", exception);
		} 
	
		return null;
	}

	@Override
	public Object decorate(final String fieldName, final String value)  {
		execute(fieldName, value, true);
		return null;
	}

	private SolrInputDocument execute(final String fieldName, final String value, final boolean indexToo) {
		String heading = value;
		String id = null;
		List<String> headingPart = null;		
		final int compoundValueSeparatorIndex = value.indexOf(GetHeadingValue.SEPARATOR);
		if (compoundValueSeparatorIndex != -1) {
			heading = value.substring(0, compoundValueSeparatorIndex);
			id = value.substring(compoundValueSeparatorIndex + GetHeadingValue.SEPARATOR.length());
			headingPart = new ArrayList<String>(Arrays.asList(heading.split(" - ")));			
		}

		try {
			final SolrInputDocument document = addNewHeadingDocument(
			        heading,
			        headingPart,
                    id);
			if (indexToo) {
				solr.add(document);
			}
			return document;
		} catch (Exception exception) {
			LOGGER.error("Unable to update the heading index.", exception);
			return null;
		}
	}	
	
	/**
	 * Injects the SOLR server in this indexer.
	 * 
	 * @param solr the SOLR server.
     * @param prefix ??
	 */
	public void setSolr(final SolrServer solr, final String prefix) {
		this.solr = solr;
		this.prefix = prefix;
	}
	
	/**
	 * Adds a new document within the autocomplete index.
	 * 
	 * @param label the heading label.
	 * @param type the heading type.
	 */
	private SolrInputDocument addNewHeadingDocument(
			final String label, 
			final List<String> headingPart,
			final String id)   {
		final SolrInputDocument document = new SolrInputDocument();
		document.addField("label", label);
		document.addField("heading_part", headingPart);
		document.addField("id", id);
		
		return document;
	}
}