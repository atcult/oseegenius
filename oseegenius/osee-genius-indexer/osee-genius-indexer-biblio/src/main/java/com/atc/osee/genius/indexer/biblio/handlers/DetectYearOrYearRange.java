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
 *  "cerca" l'anno all'interno del valore di tale sottocampo, di modo da beccare casistiche come
 *  
 *  Copyright 1983 = 1983
 *  Copyr 1970 = 1970
 *  1967- = 1967
 *  1995-1999 = 1995,1996,1997,1998,1999
 *  copyr. 1983 = 1983
 * 
 * @author agazzarini
 * @since 1.0
 */
public class DetectYearOrYearRange extends Decorator
{
	@SuppressWarnings("unchecked")
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		if (values != null && !values.isEmpty())
		{
			final List<String> result = new ArrayList<String>(values.size());
			for (final String value : values)
			{
				Object tmp = decorate(fieldName, value);
				if (tmp instanceof Collection) {
					result.addAll((Collection<String>)tmp);
				} else {
					result.add((String) tmp);
				}
			}
			return result;
		}
		return null;
	}
	
	@Override
	public Object decorate(final String fieldName, final String value) 
	{
		String firstValue = null;
		String secondValue = null;
		if (value.trim().length() > 4)
		{
			final StringBuilder valueBuilder = new StringBuilder();
			for (int i = 0; i < value.length(); i++) {
				if (Character.isDigit(value.charAt(i))) {
					valueBuilder.append(value.charAt(i));
					if (valueBuilder.length() == 4) {
						if (firstValue == null) {
							firstValue = valueBuilder.toString();
							valueBuilder.setLength(0);
						}
						/*
						else if (secondValue == null) {
							secondValue = valueBuilder.toString();
							valueBuilder.setLength(0);
							break;
						}*/
					}
				}
			}
			
			if (secondValue != null) {
				try {
					final List<String> years = new ArrayList<String>();
					int start = Integer.valueOf(firstValue);
					int end = Integer.valueOf(secondValue);
					for (int i = start; i <= end; i++) {
						years.add(String.valueOf(i));
					}
					return years;
				} catch (Exception exception) {
					return null;
				}
			} else {
				return firstValue;
			}
		}
		return value;
	}
}