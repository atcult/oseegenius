package com.atc.osee.genius.indexer.biblio.handlers;

import java.util.ArrayList;
import java.util.Collection;


import com.atc.osee.genius.indexer.biblio.Decorator;

public class OlsHeading extends Decorator{
	private final static String DUMMY_VALUE = String.valueOf(Integer.MIN_VALUE);
	public final static String VALID_PREFIX = "(OLS)";
	
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		if (values.size() == 1)
		{
			return decorate(fieldName, values.iterator().next());
		} 
		 
		final Collection<String> ids = getCollection(5);
		for (String value : values)
		{
			ids.add((String) decorate(fieldName, value));
		}
		return ids;
		
	}

	@Override
	public Object decorate(final String fieldName, final String value) 
	{
		if (value == null)
		{
			return null;
		}
		final StringBuilder builder = new StringBuilder(value);
		String result =
				(builder.length() > 5 
					&& builder.charAt(0) == VALID_PREFIX.charAt(0)
					&& builder.charAt(1) == VALID_PREFIX.charAt(1)
					&& builder.charAt(2) == VALID_PREFIX.charAt(2)
					&& builder.charAt(3) == VALID_PREFIX.charAt(3)
					&& builder.charAt(4) == VALID_PREFIX.charAt(4))
					? builder.toString()
					//: DUMMY_VALUE;
					: null;
					
		if (result != null) {
//			int indexOfSpace = result.indexOf(" ");
//			return indexOfSpace == -1 ? result : result.substring(0, indexOfSpace);
			int lastSlash = result.lastIndexOf('/');		
			if(lastSlash != -1 && result.length() >= lastSlash + 1) {
				result = result.substring(lastSlash + 1, result.length());
			}
			return result;
		}
					
		return result;
	}
			
		
		
	public Collection<String> getCollection(final int initialCapacity) {
		return new ArrayList<String>(initialCapacity);
	}
}
