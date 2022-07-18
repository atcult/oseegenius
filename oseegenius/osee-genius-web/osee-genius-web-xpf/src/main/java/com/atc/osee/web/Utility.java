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
package com.atc.osee.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * OseeGenius -W- utility class.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class Utility
{
	/**
	 * Convert the input string in TitleCase format (Ex: HELLO WORLD--> Hello World).
	 * 
	 * @param text the input stext.
	 * @return the given string converted into TitleCase format. If input String is null return empty String.
	 */
	public static String getTitleCase(final String text)
	{
		if (text == null || text.trim().length() == 0) 
		{ 
			return IConstants.EMPTY_STRING; 
		}
		
		String [] tokens = text.split(" ");
		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < tokens.length; index++)
		{
			String word = tokens[index];
			if (word.trim().length() != 0)
			{			
				builder
					.append(Character.toUpperCase(word.charAt(0)))
					.append(word.substring(1).toLowerCase());
				
				if (index < (tokens.length - 1)) 
				{
					builder.append(' ');
				}
			}
		}
		
		StringBuffer result = new StringBuffer();
		Matcher m = Pattern.compile("([^'-]*)(['-]*)(.*)").matcher(builder.toString());
		while (m.find()) {			
			StringBuilder concatString = new StringBuilder();
			concatString.append( m.group(1));
			if ( m.group(2) != null) {
				concatString.append( m.group(2))
					.append(StringUtils.capitalize(m.group(3)));					
			}
			
		    m.appendReplacement(result, concatString.toString() );
		}				
		return result.toString();
	}
}