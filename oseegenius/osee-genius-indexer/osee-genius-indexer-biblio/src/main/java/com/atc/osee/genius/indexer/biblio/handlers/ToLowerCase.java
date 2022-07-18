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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Extracts the first of available values for a given field.
 * 
 * @author aguercio
 * @since 1.2
 */
public class ToLowerCase extends Decorator
{
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		if (values != null && !values.isEmpty())
		{
			List<String> result = new ArrayList<String>(values.size());
			for (String value : values)
			{
				result.add((String)decorate(fieldName, value));
			}
			return result;
		}
		return null;
	}
	
	@Override
	public Object decorate(final String fieldName, String value) 
	{
		return value.toLowerCase();
	}
}