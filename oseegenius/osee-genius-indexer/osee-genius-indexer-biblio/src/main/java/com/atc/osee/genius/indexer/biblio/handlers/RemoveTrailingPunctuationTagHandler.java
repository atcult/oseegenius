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
public final class RemoveTrailingPunctuationTagHandler extends Decorator
{
	private final static String PUNCTUATION_PATTERN_END = "[\\.,;:/]$";
	private final static String PUNCTUATION_PATTERN_START = "^[\\.,;:/]";
	private final static String [] PROTECTED_SUFFIX = {
		"arciv.",
		"card.",
		"s.",
		"ss.",
		"collab.",
		"comment.",
		"red.",
		"trad.",
		"ill.",
		"cur.",
		"pref.",
		"intr.",
		"postf.",
		"sac.",
		"ed.",
		"etc.",
		"coll.",
		"fotogr."};
	
	public static void main(String[] args) {
		RemoveTrailingPunctuationTagHandler a = new RemoveTrailingPunctuationTagHandler();
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
		if (value == null || value.endsWith(")") || isProtectedSuffix(value))
		{
			return value;
		}

		int lastIndexOfSpace = value.lastIndexOf(' ');
		if (value.endsWith(".") && value.charAt(value.length() -2) != ' ' && value.charAt(value.length() -2) != '.')
		{
			if ((value.length() - lastIndexOfSpace <= 5) && value.charAt(value.length() -2) != '>')
			{
				return value;
			}
			
			int indexOfDot = value.indexOf('.', lastIndexOfSpace);
			int indexOfLastDot = value.lastIndexOf('.');
			
			if (indexOfDot != indexOfLastDot)
			{
				return value;
			}
		}
		
		return value.trim().replaceAll(PUNCTUATION_PATTERN_END, "").replaceAll(PUNCTUATION_PATTERN_START, "").trim();
	}
	
	private boolean isProtectedSuffix(String value)
	{
		for (String suffix : PROTECTED_SUFFIX)
		{
			if (value.toLowerCase().endsWith(suffix))
			{
				return true;
			}
		}
		return false;
	}
}