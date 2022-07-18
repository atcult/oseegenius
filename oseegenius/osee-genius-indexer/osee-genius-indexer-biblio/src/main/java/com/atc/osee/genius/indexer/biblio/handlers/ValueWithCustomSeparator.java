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


/**
 * A tag handler that simply extracts a value from a marc record based on a given expression.
 * Each subfield is separated by a custom separator.
 * 
 * value(954abr,-) 
 * value(954a:954b:954r,pippo,>1#) sottocampi diversi + filtro + indicatori
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ValueWithCustomSeparator extends GetValue
{	
	protected String getSubfieldSeparator()
	{
		return " - ";
	}
}