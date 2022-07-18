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

import com.atc.osee.genius.indexer.biblio.Decorator;

/**
 * Field collapsing feature of SOLR doesn't allow multiple fields for grouping.
 * That's mean we need something that, at index time, computes a group id
 * that will be later used as group.field.
 * This is exactly the goal of this decorator. The group ID function is based on
 *{@link String#hashCode()} method so it's not really really robust...
 * 
 * @author agazzarini
 * @since 1.0
 */
public class GenerateGroupId extends Decorator
{
	@Override
	public Object decorate(final String fieldName, final Collection<String> values) 
	{			
		if (values == null || values.isEmpty())
		{
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		for (String aValue : values)
		{
			builder.append(aValue);
		}
		
		return builder.toString().hashCode();
	}

	@Override
	public Object decorate(final String fieldName, final String value) 
	{
		if (value == null) 
		{
			return null;
		}
		
		return value.hashCode();
	}
}