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
package com.atc.osee.web.model.community;

import java.io.Serializable;

/**
 * A user tag. 
 * 
 * @author agazzarini
 * @since 1.0
 */
public final class Tag implements Serializable 
{
	private final long id;
	private final String label;
	
	private static final long serialVersionUID = 5308382337343486389L;

	/**
	 * Builds a new tag with the given data.
	 * 
	 * @param id the tag identifier.
	 * @param label the tag label
	 */
	public Tag(final long id, final String label)
	{
		this.id = id;
		this.label = label;
	}

	/**
	 * Returns the tag identifier.
	 * 
	 * @return the tag identifier.
	 */
	public long getId() 
	{
		return id;
	}

	/**
	 * Returns the tag label.
	 * 
	 * @return the tag label.
	 */
	public String getLabel() 
	{
		return label;
	}
}