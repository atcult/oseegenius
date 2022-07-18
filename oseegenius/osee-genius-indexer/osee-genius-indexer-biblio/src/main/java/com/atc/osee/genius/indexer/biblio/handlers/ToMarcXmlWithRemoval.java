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

import java.io.OutputStream;

import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;

/**
 * Extracts the MARCXML data of a given record.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ToMarcXmlWithRemoval extends ToMarcWithRemoval 
{
	@Override
	protected MarcWriter getMarcWriter(final OutputStream outputStream, final String charset) 
	{
		return new MarcXmlWriter(outputStream, charset);
	}
}
