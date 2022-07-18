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
package com.atc.osee.web.model.history;

import java.io.Serializable;
import java.util.Date;

/**
 * Supertype layer for all search history entries.
 * 
 * @author agazzarini
 * @since 1.0
 */
public abstract class HistoryEntry implements Serializable
{
	private static final long serialVersionUID = -6084413501106048517L;
	
	protected final int id;
	protected final Date date;
	
	/**
	 * Builds a new entry with the given id.
	 * 
	 * @param id the entry identifier.
	 */
	public HistoryEntry(int id)
	{
		this.id = id;
		this.date = new Date();
	}
	
	/**
	 * Returns the internal identifier assigned to this entry.
	 * 
	 * @return the internal identifier assigned to this entry.
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * Returns the execution date of this search entry.
	 * 
	 * @return the execution date of this search entry.
	 */
	public Date getDate()
	{
		return date;
	}
}