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
 * Extracts the first character of a given value.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetFirstCharacter extends Decorator
{
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		List<String> result = new ArrayList<String>(values.size());
		for (String dewey : values) 
		{
			if (dewey != null && dewey.trim().length() > 1)
			{
				result.add((String) decorate(fieldName, dewey));
			}
		}
		return result;
	}

	@Override
	public Object decorate(final String fieldName, final String value) 
	{
		return value.subSequence(0, 1);
	}
}