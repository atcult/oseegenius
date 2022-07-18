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
import java.util.Date;

/**
 * A user review domain object.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Review implements Serializable 
{
	private static final long serialVersionUID = 166909226613473438L;
	
	private final long id;
	private final String text;
	private final Date date;
	private boolean toBeModerated;
	
	/**
	 * Builds a new review with the given data.
	 * 
	 * @param id the review identifier.
	 * @param text the review text.
	 * @param date the review date.
	 * @param toBeModerated a flag indicating if the review needds to be moderated.
	 */
	public Review(final long id, final String text, final Date date, final boolean toBeModerated) 
	{
		this.id = id;
		this.text = text;
		this.date = date;
		this.toBeModerated = toBeModerated;
	}

	/**
	 * Returns true if the review has to be moderated.
	 * 
	 * @return true if the review has to be moderated.
	 */
	public boolean isToBeModerated() 
	{
		return toBeModerated;
	}

	/**
	 * Sets the "to be moderated" flag for this review.
	 * 
	 * @param toBeModerated the flag for this review.
	 */
	public void setToBeModerated(final boolean toBeModerated) 
	{
		this.toBeModerated = toBeModerated;
	}

	/**
	 * Returns the review identifier.
	 * 
	 * @return the review identifier.
	 */
	public long getId() 
	{
		return id;
	}

	/**
	 * Returns the review text.
	 * 
	 * @return the review text.
	 */
	public String getText() 
	{
		return text;
	}

	/**
	 * Returns the review date.
	 * 
	 * @return the review date.
	 */
	public Date getDate() 
	{
		return date;
	}
}