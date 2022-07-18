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
import java.util.Set;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Adding ARC literal
 * 
 * @author aguercio
 * @since 1.2
 */
public class BibDecorator extends Decorator
{
	final String suffix = "BIB";
	
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		if (values == null || values.isEmpty())
		{
			return values;
		}
		
		if (values.size() == 1)
		{
			return decorate(fieldName, values.iterator().next());
		}
		
		final Set<String> result = new LinkedHashSet<String>(values.size());
		for (String value : values)
		{
			final String decorated = (String) decorate(fieldName, value);			
			if (decorated != null)
			{
				result.add(decorated);
			}
		}
		return result;
	}

	@Override
	public Object decorate(final String fieldName, final String value) 
	{
		return (value != null && value.trim().length() > 0) ?  value + suffix : null;
	}
}