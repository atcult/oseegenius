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

/**
 * Thrown in case the indexing process fails.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class IndexingException extends Exception 
{
	private static final long serialVersionUID = -7164204546050691054L;

	/**
	 * Builds a new indexing exception with the given message and cause.
	 * 
	 * @param message the error message.
	 * @param cause the exception cause.
	 */
	public IndexingException(final String message, final Throwable cause) 
	{
		super(message, cause);
	}
}