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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.util.NamedList;

/**
 * A value object encapsulating indexing results.
 * Basically this is a handy way to let the client know 
 * a summary of what happened during the index process.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class IndexerResult implements Serializable 
{
	private static final long serialVersionUID = -4328830442481110601L;
	
	private final NamedList<Object> attributes; 
	private final String resultName;
	
	private int indexedDocuments;
	private int failedDocuments;
	private List<String> failureDetails = new ArrayList<String>();
	
	/**
	 * Builds a new result with the given indexer name (id).
	 * 
	 * @param indexerName the name of the indexer that produced this result.
	 */
	public IndexerResult(final String indexerName)
	{
		this.resultName = indexerName;
		this.attributes = new NamedList<Object>();
	}
	
	/**
	 * Adds an info attribute to this result.
	 * 
	 * @param name the name of the attribute.
	 * @param value the value of the attribute.
	 */
	public void addAttribute(final String name, final Object value)
	{
		attributes.add(name, value);
	}
	
	/**
	 * Returns the name of this result object.
	 * 
	 * @return the name of this result object.
	 */
	public String getResultName()
	{
		return resultName;
	}
	
	/**
	 * Returns the attributes of this result.
	 * 
	 * @return the attributes of this result.
	 */
	public NamedList<Object> getAttributes()
	{
		attributes.add("indexed-count", indexedDocuments);
		
		NamedList<Object> failures = new NamedList<Object>();
		failures.add("failed-count", failedDocuments);
		failures.add("first-" + failureDetails.size() + "-failures", failureDetails);
		
		attributes.add("failures", failures);
		return attributes;
	}
	
	/**
	 * Increments the counter of successful indexed
	 * documents.
	 */
	public void incrementIndexedCounter()
	{
		indexedDocuments++;
	}
	
	/**
	 * Increments the counter of failed indexed
	 * documents.
	 */
	public void incrementFailedCounter()
	{
		failedDocuments++;
	}

	/**
	 * Adds an explanation about a failed document
	 * (i.e. a document that hasn't been indexed).
	 * 
	 * @param message the error message.
	 */
	public void addFailedDetails(final String message)
	{
		if (failureDetails.size() < 30)
		{
			failureDetails.add(message);
		}
	}
}