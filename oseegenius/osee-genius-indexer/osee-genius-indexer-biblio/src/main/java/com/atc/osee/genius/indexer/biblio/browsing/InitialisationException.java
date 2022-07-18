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
package com.atc.osee.genius.indexer.biblio.browsing;

/**
 * Thrown when a component raises an exception at startup.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class InitialisationException extends Exception
{
	private static final long serialVersionUID = -4624972938573583161L;
	
	/**
	 * Builds a new exception with the given cause.
	 * 
	 * @param cause the exception cause.
	 */
	public InitialisationException(final Throwable cause)
	{
		super(cause);
	}
}