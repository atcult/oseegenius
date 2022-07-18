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
import java.util.Iterator;
import java.util.List;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * A partire da due date, genera tutte i possibili anni dell'intervallo.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ExpandDate extends Decorator
{
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		if (values != null)
		{
			if (values.size() == 1)
			{
				return decorate(fieldName, values.iterator().next());
			}
			
			if (values.size() >= 2)
			{
				Iterator<String> iterator = values.iterator();
				String shouldBeTheStartDate = iterator.next();
				String shouldBeTheEndDate = iterator.next();
				
				if (shouldBeTheStartDate != null && shouldBeTheStartDate.length() > 4)
				{
					if (shouldBeTheEndDate != null && shouldBeTheEndDate.length() > 4)
					{
						List<String> result = new ArrayList<String>();
						int startYear = Integer.parseInt(shouldBeTheStartDate.substring(0, 4));
						int endYear = Integer.parseInt(shouldBeTheEndDate.substring(0, 4));
						
						for (int year = startYear; year <= endYear; year++)
						{
							result.add(String.valueOf(year));							
						}
						return result;
					} else
					{
						return decorate(fieldName, shouldBeTheStartDate);
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public Object decorate(final String fieldName, String value) 
	{
		// Se abbiamo un solo valore allora deve essere restituito solamente questo.
		if (value != null && value.trim().length() > 4)
		{
			String yearCleaned = value.replaceAll("/", "");
			String year = yearCleaned.substring(0,4);
			Integer.parseInt(year);
			return year;
		}
		return null;
	}
}