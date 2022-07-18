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

import java.util.Collection;
import java.util.LinkedHashSet;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Extracts a numeric value from a given subfield.
 * This class uses a {@link LinkedHashSet} as backing collection. If you want duplicated results see {@link NumericString2}.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class NumericString extends Decorator
{
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		if (values == null || values.isEmpty())
		{
			return null;
		}
		
		if (values.size() == 1)
		{
			return decorate(fieldName, values.iterator().next());
		}
		
		final Collection<String> result = getCollection(values.size());
		for (String value : values)
		{
			final String numericString = (String) decorate(fieldName, value);			
			if (numericString != null)
			{
				result.add(numericString);
			}
		}
		return result;
	}

	@Override
	public Object decorate(final String fieldName, final String value) 
	{
		if (value == null)
		{
			return null;
		}
		
		final StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < value.length(); i++)
		{
			final char aChar = value.charAt(i);
			if (Character.isDigit(aChar))
			{
				builder.append(aChar);
			}
		}
		return builder.length() != 0 ? builder.toString() : null;
	}
	
	public Collection<String> getCollection(final int initialCapacity)
	{	
		return new LinkedHashSet<String>(initialCapacity);
	}
}