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
import java.util.Set;
import java.util.TreeSet;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Extracts the first of available values for a given field.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Cgt extends Decorator
{
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		if ( values != null && !values.isEmpty())
		{
			final Set<String> cgts = new TreeSet<String>();
			for (String cgt : values)
			{
				String value = (String) decorate(fieldName, cgt);
				if (value != null && value.trim().length() != 0)
				{
					cgts.add(value);				
				}
			}
			return cgts;
		}
		return null;
	}

	@Override
	public Object decorate(final String fieldName, String value) 
	{
		if (value == null || value.trim().length() < 3 || value.charAt(1) != ' ')
		{
			return null;
		}
		
		value = value.trim();
		int indexOfSpace = value.indexOf(" ", 2);
		if (indexOfSpace != -1)
		{
			value = value.substring(0, indexOfSpace).trim().replace(" ", "");
		} else
		{
			value = value.replace(" ", "");
		}
		
		value = value.replaceAll(" ", "").trim();
		
		return value;
	}
}