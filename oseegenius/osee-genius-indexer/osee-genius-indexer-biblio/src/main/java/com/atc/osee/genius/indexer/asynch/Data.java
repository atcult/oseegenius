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
package com.atc.osee.genius.indexer.asynch;

/**
 * A simple value object wrapper around binary MARC data.
 * 
 * @author agazzarini
 * @since 1.1
 */
public final class Data 
{
	public final byte [] leader;
	public final byte [] body;
	public final int length;
	
	/**
	 * Builds a new wrapper.
	 * 
	 * @param leader the leader (byte array).
	 * @param body the MARC record (as byte array).
	 * @param length the record length.
	 */
	public Data(	final byte [] leader, final byte [] body, final int length)
	{
		this.leader = leader;
		this.body = body;
		this.length = length;
	}
}
