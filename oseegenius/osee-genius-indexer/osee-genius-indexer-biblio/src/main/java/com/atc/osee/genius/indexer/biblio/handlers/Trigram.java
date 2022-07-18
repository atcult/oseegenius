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
import java.util.HashSet;
import java.util.Set;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * A value handler that divides in trigrams a given value.
 * We found that useful for example with languages , where a lot of libraries 
 * doesn't retain the single language code separated (i.e. ita, eng, ger) but instead the whole 
 * language is a concatenation of all languages code (i.e. itaengger)
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Trigram extends Decorator
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{			
		if (values == null)
		{
			return values;
		}
		
		Set<String> result = new HashSet<String>();
		for (Object value : values)
		{
			Object newValue = null;
			
			if (value instanceof Collection)
			{
				newValue = decorate(fieldName, (Collection)value);
			} else 
			{
				newValue = decorate(fieldName, (String)value);				
			}
			if (newValue != null)
			{
				if (newValue instanceof Collection)
				{
					Set<String> trigrams = (Set<String>) decorate(fieldName, (Collection)newValue);
					result.addAll(trigrams);
				} else 
				{
					result.add((String) newValue);
				}
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
		
		int length = value.length();
		
		if (length <= 3)
		{
			return value;
		}
		
		int howManyTrigrams = (length % 3 == 0) ? (length / 3) : ((length / 3) + 1);
		Set<String> trigrams = new HashSet<String>(howManyTrigrams);
		
		for (int i = 0; i < howManyTrigrams; i += 3)
		{
			trigrams.add(value.substring(i, i + 3));
		}
		return trigrams;
	}
}