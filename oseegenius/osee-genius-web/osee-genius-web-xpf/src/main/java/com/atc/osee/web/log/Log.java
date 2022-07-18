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
package com.atc.osee.web.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.IConstants;

/**
 * OseeGenius Logger component.
 * Basically it is a Booch utility class with all methods 
 * needed for log.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public abstract class Log 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(IConstants.MODULE_NAME);
	
	/**
	 * Logs out the given message with INFO as severity level.
	 * 
	 * @param message the message that will be logged out.
	 */
	public static void info(final String message)
	{
		LOGGER.info(message);
	}
	
	/**
	 * Logs out the given message with INFO as severity level.
	 * 
	 * @param message the log message.
	 * @param parameters the placeholder value that will be replacred at runtime.
	 */
	public static void info(final String message, final Object ... parameters)
	{
		LOGGER.debug(String.format(message, parameters));
	}
	
	/**
	 * Logs out the given message with DEBUG as severity level.
	 * 
	 * @param message the log message.
	 * @param placeholderValue the placeholder value that will be replacred at runtime.
	 */
	public static void debug(final String message, final String placeholderValue)
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(String.format(message, placeholderValue));
		}
	}
	
	/**
	 * Logs out the given message with DEBUG severity level.
	 * 
	 * @param message the message.
	 */
	public static void debug(final String message)
	{
		LOGGER.debug(message);
	}

	/**
	 * Logs out the given exception with WARNING severity level.
	 * 
	 * @param message the message.
	 * @param throwable the exception.
	 */
	public static void warning(final String message, final Throwable throwable)
	{
		LOGGER.warn(message, throwable);
	}
	
	/**
	 * Logs out the given message with WARNING severity level.
	 * 
	 * @param message the message.
	 */
	public static void warning(final String message)
	{
		LOGGER.warn(message);
	}	
	
	/**
	 * Logs out the given exception with DEBUG severity level.
	 * 
	 * @param throwable the exception.
	 */
	public static void debug(final Throwable throwable)
	{
		LOGGER.debug("", throwable);
	}

	/**
	 * Logs out the given message and throwable with ERROR severity level.
	 * 
	 * @param message the message.
	 * @param throwable the exception.
	 */
	public static void error(final String message, final Throwable throwable)
	{
		LOGGER.error(message, throwable);		
	}

	/**
	 * Logs out the given message with ERROR severity level.
	 * 
	 * @param message the message.
	 * @param placeholderValues the placeholder values.
	 */	
	public static void error(final String message, final Object ... placeholderValues)
	{
		LOGGER.error(String.format(message, placeholderValues));		
	}

	/**
	 * Logs out the given message with ERROR severity level.
	 * 
	 * @param message the message.
	 * @param throwable the exception.
	 * @param placeholderValues the placeholder values.
	 */		
	public static void error(final String message, final Throwable throwable, final Object ... placeholderValues)
	{
		LOGGER.error(String.format(message, placeholderValues), throwable);		
	}

	/**
	 * Logs out the given message with ERROR severity level.
	 * 
	 * @param message the message.
	 */	
	public static void error(final String message)
	{
		LOGGER.error(message);		
	}
}