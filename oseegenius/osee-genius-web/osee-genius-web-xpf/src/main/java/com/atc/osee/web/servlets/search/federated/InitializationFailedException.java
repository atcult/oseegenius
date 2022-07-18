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
package com.atc.osee.web.servlets.search.federated;

/**
 * Thrown in case a federated interaction cannot be initialised.
 * 
 * @author agazzarini
 * @since 1.0
 */
class InitializationFailedException extends Exception
{
	private static final long serialVersionUID = -7755265109945975563L;
	private final int statusCode;
	
	/**
	 * Builds a new exception with the given status code.
	 * 
	 * @param statusCode the status code.
	 */
	InitializationFailedException(final int statusCode)
	{
		this.statusCode = statusCode;
	}
	
	/**
	 * Returns the status code wrapped by this exception.
	 * 
	 * @return the status code wrapped by this exception.
	 */
	int getStatusCode()
	{
		return statusCode;
	}
}