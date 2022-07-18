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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Extracts the first of available values for a given field.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class toUTC extends Decorator
{
	static ThreadLocal<SimpleDateFormat> FORMATTERS = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
			inputFormat.setLenient(false);
			return inputFormat;
		};
	};
	
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{
		if (values != null && !values.isEmpty())
		{
			List<Date> result = new ArrayList<Date>(values.size());
			for (String value : values)
			{
				Date d = (Date) decorate(fieldName, value);
				result.add(d);
			}
			return result;
		}
		return null;
	}
	
	@Override
	public Object decorate(final String fieldName, String value) 
	{
		if (value != null && value.trim().length() >= 8)
		{
			try 
			{
				return FORMATTERS.get().parse(value.substring(0,8));
			} catch (Exception exception)
			{
				return null;
			}
		}
		return null;
	}
}