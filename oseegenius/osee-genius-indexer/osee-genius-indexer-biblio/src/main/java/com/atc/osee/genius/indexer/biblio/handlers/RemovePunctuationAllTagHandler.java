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
import com.atc.osee.genius.indexer.biblio.IConstants;

/**
 * Removes trailing punctuation.
 * 
 * @author agazzarini
 * @since 1.0
 */
public final class RemovePunctuationAllTagHandler extends Decorator
{
		
	public static void main(String[] args) {
		RemovePunctuationAllTagHandler a = new RemovePunctuationAllTagHandler();
		System.out.println(a.decorate("",(String)a.decorate("", "Studi superiori [Carocci]//")));
	}
	
	@Override
	public Object decorate(final String fieldName, final Collection<String> value) 
	{
		if (value == null || value.isEmpty())
		{
			return IConstants.EMPTY_STRING_LIST;
		}
		
		if (value.size() == 1)
		{
			return decorate(fieldName, value.iterator().next());
		}
		
		final List<String> result = new ArrayList<String>(value.size());
		for (String aString : value)
		{
			result.add((String)decorate(fieldName, aString));
		}
		return result;
	}

	@Override
	public Object decorate(final String fieldName, final String value) 
	{
		if (value == null )
		{
			return value;
		}	
		
		return value.replaceAll(";", "").replaceAll("[.]", "").replaceAll(",", "");
	}
	
}