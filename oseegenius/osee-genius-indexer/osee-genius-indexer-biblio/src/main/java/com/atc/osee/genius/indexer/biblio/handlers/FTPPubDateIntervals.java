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
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Extracts the FTP content type from a MARC record.
 *        
 * @author agazzarini
 * @since 1.2
 */
public class FTPPubDateIntervals extends Decorator 
{
	
	@SuppressWarnings("unchecked")
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		Set<String> result = new TreeSet<String>();
		for (String value : values)
		{
			Object decoratedResult = decorate(fieldName, value);
			if (decoratedResult != null)
			{
				result.addAll((List<String>) decoratedResult);
			}
		}
		return result;
	}
	
	@Override
	public Object decorate(final String fieldName, final String value)
	{ 
		if (value != null)
		{
			final List<String> result = new ArrayList<String>();
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			int year = Integer.parseInt(value);
	
			if (year == currentYear) 
			{
				result.add("api");
				result.add("bpi");
				result.add("cpi");				
				return result;
			}

			if (year >= (currentYear - 2))
			{
				result.add("bpi");
				result.add("cpi");				
				return result;
			} 
			
			if (year >= (currentYear - 9))
			{
				return "cpi";
			} 

			return "zpi";									
		}
		return null;		
	}
}