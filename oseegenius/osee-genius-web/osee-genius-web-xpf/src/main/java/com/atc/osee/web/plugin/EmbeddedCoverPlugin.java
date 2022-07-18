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
package com.atc.osee.web.plugin;

import org.apache.solr.common.SolrDocument;

/**
 * Embedded cover plugin.
 * An implementor of this interface is supposed to find for document cover within the 
 * document itself.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public interface EmbeddedCoverPlugin extends Plugin
{
	/**
	 * Returns the URL of the small cover for the given document.
	 * 
	 * @param document the document.
	 * @return the URL of the small cover for the given document.
	 */
	String getSmallCoverUrl(SolrDocument document);

	/**
	 * Returns the URL of the medium cover for the given document.
	 * 
	 * @param document the document.
	 * @return the URL of the medium cover for the given document.
	 */
	String getMediumCoverUrl(SolrDocument document);
	
	/**
	 * Returns the URL of the big cover for the given document.
	 * 
	 * @param document the document.
	 * @return the URL of the big cover for the given document.
	 */
	String getBigCoverUrl(SolrDocument document);	
}