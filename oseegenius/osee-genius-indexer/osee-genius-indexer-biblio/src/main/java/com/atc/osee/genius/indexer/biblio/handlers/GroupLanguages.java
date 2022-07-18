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
public class GroupLanguages extends Decorator
{
	private final static String [] LANGUAGES = {
		"ita",
		"eng",
		"fre",
		"ger",
		"spa",
		"por",
		"dut",
		"pol",
		"grc",
		"lat",
		"heb"};
	
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{			
		if (values == null)
		{
			return values;
		}
		
		Set<String> result = new HashSet<String>();
		for (String value : values)
		{
			Object decorated = decorate(fieldName, value);
			if (decorated != null)
			{
				result.add((String)decorated);
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
		
		for (String language : LANGUAGES)
		{
			if (language.equals(value))
			{
				return language;
			}
		}
		
		return "others";
	}
}