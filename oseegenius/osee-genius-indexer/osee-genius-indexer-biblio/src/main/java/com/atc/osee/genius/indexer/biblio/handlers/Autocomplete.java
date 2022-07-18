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
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Autocomplete indexer.
 * 
 * @author agazzarini
 * @since 1.0
 */
public final class Autocomplete extends Decorator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Autocomplete.class);
	
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
			final SolrInputDocument autocompleteDocument = execute(fieldName, aString, false);
			if (autocompleteDocument != null) {
				list.add(autocompleteDocument);
			}
		}
		
		try  {
			solr.add(list);
		} catch (final Exception exception) {
			LOGGER.error("Unable to update the autocomplete index.", exception);
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
		String logicalView = null;
		final int compoundValueSeparatorIndex = value.indexOf(GetCompoundValue.SEPARATOR);
		if (compoundValueSeparatorIndex != -1) {
			heading = value.substring(0, compoundValueSeparatorIndex);
			logicalView = value.substring(compoundValueSeparatorIndex + GetCompoundValue.SEPARATOR.length());
		}

		try {
			final SolrInputDocument document = addNewAutocompleteDocument(
			        heading,
                    fieldName.replaceFirst(prefix + "_","").trim(),
                    logicalView);
			if (indexToo) {
				solr.add(document);
			}
			return document;
		} catch (Exception exception) {
			LOGGER.error("Unable to update the autocomplete index.", exception);
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
	private SolrInputDocument addNewAutocompleteDocument(
			final String label, 
			final String type, 
			final String logicalView)   {
		final SolrInputDocument document = new SolrInputDocument();
		document.addField("label", label);
		document.addField("type", type);

		if (type.endsWith("bc")) {
            document.addField("type", "any_bc");
        } else if (type.endsWith("ar")) {
            document.addField("type", "any_ar");
        }

		if (logicalView != null) {
			document.addField("logical_view", logicalView);			
		}
		return document;
	}
}