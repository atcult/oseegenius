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
 * @author agazzarini
 * @since 1.0
 */
public class GetFirstAvailableYearValue extends Decorator
{
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		if (values != null && !values.isEmpty())
		{
			final List<String> result = new ArrayList<String>(values.size());
			for (String value : values)
			{
				final String year = (String)decorate(fieldName, value);
				if (year != null) {
					result.add(year);
				}
			}
			return result;
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(">>>" + new GetFirstAvailableYearValue().decorate("8237382", "absbc 277327 sdndm"));
	}
	 
	@Override
	public Object decorate(final String fieldName, String value) 
	{
		if (value.trim().length() > 4)
		{
			StringBuilder builder = new StringBuilder();
			int consecutiveNumbersCount = 0;
			for (int i = 0; i < value.length(); i++) {
				char ch = value.charAt(i);
				if (Character.isDigit(ch)) {
					builder.append(ch);
					consecutiveNumbersCount++;
					if (consecutiveNumbersCount == 4) {
						break;
					}
				} else {
					consecutiveNumbersCount = 0;
					builder.setLength(0);
				}
			}
			
			if (builder.length() == 4) {
				return builder.toString();
			}
			return null;
		}
		return null;
	}
}