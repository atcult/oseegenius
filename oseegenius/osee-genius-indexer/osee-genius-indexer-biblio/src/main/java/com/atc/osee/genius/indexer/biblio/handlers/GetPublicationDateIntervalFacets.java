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
 * Extracts a publication date that can be used for sorting.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GetPublicationDateIntervalFacets extends Decorator 
{
	static final String THIS_YEAR = "aty";
	static final String LAST_2_YEARS = "bl2y";
	static final String LAST_5_YEARS = "cl5y";
	static final String LAST_10_YEARS = "dl10y";
	static final String LAST_50_YEARS = "el50y";
	static final String MORE_THAN_50_YEARS_AGO = "fmt50y";
	
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
		List<String> result = new ArrayList<String>();
		
		int currentYear = getCurrentYear();
		
		try
		{
			int year = Integer.parseInt(value);
			if (year == currentYear) 
			{
				result.add(THIS_YEAR);
			}
			
			if (year >= (currentYear - 1))
			{
				result.add(LAST_2_YEARS);
			}

			if (year >= (currentYear - 4))
			{
				result.add(LAST_5_YEARS);
			}
			
			if (year >= (currentYear - 9))
			{
				result.add(LAST_10_YEARS);
			}
			
			if (year >= (currentYear - 49))
			{
				result.add(LAST_50_YEARS);
			}
			
			if (year  < (currentYear - 50))
			{
				result.add(MORE_THAN_50_YEARS_AGO);
			}
			
			return result;
			
		} catch (Exception exception)
		{
			return null;
		}
	}
	
	/**
	 * Returns the current year.
	 * 
	 * @return the current year.
	 */
	int getCurrentYear()
	{
		return Calendar.getInstance().get(Calendar.YEAR);
	}
}