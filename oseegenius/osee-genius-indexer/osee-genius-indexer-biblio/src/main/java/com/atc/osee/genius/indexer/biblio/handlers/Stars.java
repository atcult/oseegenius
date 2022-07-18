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
import java.util.LinkedHashSet;
import java.util.Set;

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Handler CBT per gestire delle faccette.
 * 
 * @see http://bugzilla.atcult.it/show_bug.cgi?id=4207
 * @author agazzarini
 * @since 1.0
 */
public class Stars extends Decorator
{
	@Override
	public Object decorate(String fieldName, Collection<String> values) {
		final Set<String> result = new LinkedHashSet<String>(values.size());
		for (final String value : values) {
			final String facet = (String) decorate(fieldName, value);
			if (facet != null) {
				result.add(facet); 
			}
		}
		return result;
	}

	@Override
	public Object decorate(String fieldName, String value) {
		if (value == null) {
			return null;
		}
		
		try {
			final int openSquareStartIndex = value.indexOf("[");
			if (openSquareStartIndex != -1) {
				final int commaStartIndex = value.indexOf(",", openSquareStartIndex);
				if (commaStartIndex != -1) {
					final int closeSquareIndex = value.indexOf("]", commaStartIndex);
					if (closeSquareIndex != -1) {
						return value.substring(commaStartIndex + 1, closeSquareIndex).toLowerCase().trim();
					}
				}
			}
			return null;
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}	
}