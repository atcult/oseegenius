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

import java.util.Collection;

import org.marc4j.marc.Record;

/**
 * Supertype layer of all decorator value handler.
 * According with decorator DP role, defines a handler
 * that decorates a value produced by another handler.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class Decorator implements IDecorator 
{
	@SuppressWarnings("unchecked")
	@Override
	public Object decorate(final Record record, final String fieldName, final Object value) 
	{
		if (value == null)
		{
			return null;
		}
		
		if (value instanceof Collection)
		{
			return decorate(fieldName, (Collection<String>) value);
		} 
		return decorate(fieldName, String.valueOf(value));
	}

	/**
	 * Decorates a collection of values.
	 * 
	 * @param fieldName the field name.
	 * @param value the input value (a collection);
	 * @return the decorated value (most probably a collection of decorated values).
	 */
	public abstract Object decorate(String fieldName, Collection<String> value);

	/**
	 * Decorates the given value.
	 * 
	 * @param fieldName the field name.
	 * @param value the input value;
	 * @return the decorated value.
	 */
	public abstract Object decorate(String fieldName, String value);
}