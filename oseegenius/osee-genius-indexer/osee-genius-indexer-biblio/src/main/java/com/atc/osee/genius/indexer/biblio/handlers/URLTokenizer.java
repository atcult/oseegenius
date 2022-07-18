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
 * PAT: Non trova medialibrary: è indicizzato il tag 856 fra le note?
 * ATCULT: Cosa si vuole indicizzare? il link? Il problema è dovuto al fatto che se ho una URL del tipo http//medialibrary.com/paperino indicizzando si renderà questo record ricercabile per medialibrary e anche per paperino...è questo che si vuole? Altrimenti chiarire l'esigenza
 * PAT: Sì va bene
 * 
 * @author agazzarini
 * @since 1.0
 */
public class URLTokenizer extends Decorator
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
		
		Set<String> tokens = new HashSet<String>();
		for (String value : values)
		{
			tokenize(value, tokens);
		}
		return !tokens.isEmpty() ? tokens : null;
	}

	@Override
	public Object decorate(final String fieldName, final String value) 
	{
		if (value == null)
		{
			return null;
		}
		
		Set<String> tokens = new HashSet<String>();
		tokenize(value, tokens);
		
		return !tokens.isEmpty() ? tokens : null;
	}	
	
	private void tokenize(final String value, final Set<String> accumulator) 
	{
		if (value != null && value.trim().length() != 0)
		{
			String withoutScheme = value.replace("https://", "").replace("http://", "").replace(".com", "").replace(".it", "").replace("www", "");
			String [] tokens = withoutScheme.split("/");
			if (tokens != null && tokens.length > 0)
			{
				String [] hostParts = tokens[0].split("\\.");
				for (String hostPart : hostParts)
				{
					if (hostPart != null && hostPart.trim().length() > 0)
					{
						accumulator.add(hostPart);
					}
				}
			}

			if (tokens.length > 0)
			{
				for (int index = 1; index < tokens.length; index++)
				{
					String part = tokens[index];
					if (part != null && part.trim().length() > 0)
					{
						accumulator.add(part);
					}
				}
			}
		}
	}	
}