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

import org.marc4j.marc.Record;

import com.atc.osee.genius.indexer.biblio.IDecorator;

/**
 * Null Object Value handler.
 * As the name suggests, It does nothing.
 * 
 * @author agazzarini
 * @since 1..0
 */
public class NullObjectValueHandler implements IDecorator 
{
	@Override
	public Object decorate(final Record record, final String fieldName, final Object value) 
	{
		return value;
	}
}
