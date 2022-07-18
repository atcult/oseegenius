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
 * Thrown in case a indexer cannot be properly configured. 
 * Or its configuration seems to be wrong.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class WrongConfigurationException extends Exception 
{
	private static final long serialVersionUID = 4954536429881220325L;

	/**
	 * Builds a new configuration exception.
	 */
	public WrongConfigurationException() 
	{
		super();
	}

	/**
	 * Builds a new configuration exception with the given 
	 * exception cause.
	 * 
	 * @param cause the error cause.
	 */
	public WrongConfigurationException(final Throwable cause) 
	{
		super(cause);
	}

	/**
	 * Builds a new exception with the given message.
	 * 
	 * @param message the exception message.
	 */
	public WrongConfigurationException(final String message) 
	{
		super(message);
	}
}