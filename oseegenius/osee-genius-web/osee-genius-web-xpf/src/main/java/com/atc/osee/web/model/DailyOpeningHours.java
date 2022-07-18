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
package com.atc.osee.web.model;

import java.io.Serializable;

/**
 * Library (branch) opening and closing hours
 * .
 * @author agazzarini
 * @since 1.0
 */
public class DailyOpeningHours implements Serializable
{
	private static final long serialVersionUID = -7630270841005932796L;
	
	private final int dayOfTheWeek;
	
	private final boolean morningClosed;
	private final int openingMorningHours;
	private final int openingMorningMinutes;

	private final int closingMorningHours;
	private final int closingMorningMinutes;

	private final boolean afternoonClosed;
	private final int openingAfternoonHours;
	private final int openingAfternoonMinutes;
	
	private final int closingAfternoonHours;
	private final int closingAfternoonMinutes;

	private final boolean eveningClosed;
	private final int openingEveningHours;
	private final int openingEveningMinutes;

	private final int closingEveningHours;
	private final int closingEveningMinutes;

	/**
	 * Builds a new hours with the given data.
	 * 
	 * @param dayOfTheWeek the (code) day of the week.
	 * @param morningClosed true if closed in the morning.
	 * @param openingMorningHours the morning opening hours.
	 * @param openingMorningMinutes the morning opening minutes.
	 * @param closingMorningHours the morning closing hours.
	 * @param closingMorningMinutes the morning closing minutes.
	 * @param afternoonClosed true if closed in the afternoon.
	 * @param openingAfternoonHours the afternoon opening hours.
	 * @param openingAfternoonMinutes the afternoon opening minutes.
	 * @param closingAfternoonHours the afternoon closing hours.
	 * @param closingAfternoonMinutes the afternoon closing minutes.
	 * @param eveningClosed true if closed in the evening.
	 * @param openingEveningHours the evening opening hours.
	 * @param openingEveningMinutes the evening opening minutes.
	 * @param closingEveningHours the evening closing hours.
	 * @param closingEveningMinutes the evening closing minutes.
	 */
	public DailyOpeningHours(
			final int dayOfTheWeek, 
			final boolean morningClosed, final int openingMorningHours, final int openingMorningMinutes, final int closingMorningHours, final int closingMorningMinutes,
			final boolean afternoonClosed, final int openingAfternoonHours, final int openingAfternoonMinutes, 
			final int closingAfternoonHours, final int closingAfternoonMinutes, 
			final boolean eveningClosed, final int openingEveningHours, 
			final int openingEveningMinutes, final int closingEveningHours, final int closingEveningMinutes)
	{
		this.dayOfTheWeek = dayOfTheWeek;
		this.morningClosed = morningClosed;
		this.openingMorningHours = openingMorningHours;
		this.openingMorningMinutes = openingMorningMinutes;
		this.closingMorningHours = closingMorningHours;
		this.closingMorningMinutes = closingMorningMinutes;
		this.afternoonClosed = afternoonClosed;
		this.openingAfternoonHours = openingAfternoonHours;
		this.openingAfternoonMinutes = openingAfternoonMinutes;
		this.closingAfternoonHours = closingAfternoonHours;
		this.closingAfternoonMinutes = closingAfternoonMinutes;
		this.eveningClosed = eveningClosed;
		this.openingEveningHours = openingEveningHours;
		this.openingEveningMinutes = openingEveningMinutes;
		this.closingEveningHours = closingEveningHours;
		this.closingEveningMinutes = closingEveningMinutes;
	}

	/**
	 * Returns the day of the week.
	 * 
	 * @return the day of the week,
	 */
	public int getDayOfTheWeek()
	{
		return dayOfTheWeek;
	}

	/**
	 * Returns true if the library is closed in the morning.
	 * 
	 * @return true if the library is closed in the morning.
	 */
	public boolean isMorningClosed()
	{
		return morningClosed;
	}

	/**
	 * Returns the morning opening hours.
	 * 
	 * @return the morning opening hours.
	 */
	public int getOpeningMorningHours()
	{
		return openingMorningHours;
	}
	
	/**
	 * Returns the morning opening minutes.
	 * 
	 * @return the morning opening minutes.
	 */
	public int getOpeningMorningMinutes()
	{
		return openingMorningMinutes;
	}

	/**
	 * Returns the morning closing hours.
	 * 
	 * @return the morning closing hours.
	 */
	public int getClosingMorningHours()
	{
		return closingMorningHours;
	}

	/**
	 * Returns the morning closing minutes.
	 * 
	 * @return the morning closing minutes.
	 */
	public int getClosingMorningMinutes()
	{
		return closingMorningMinutes;
	}

	/**
	 * Returns true if the library is closed in the afternoon.
	 * 
	 * @return true if the library is closed in the afternoon.
	 */

	public boolean isAfternoonClosed()
	{
		return afternoonClosed;
	}

	/**
	 * Returns the afternoon opening hours.
	 * 
	 * @return the afternoon opening hours.
	 */
	public int getOpeningAfternoonHours()
	{
		return openingAfternoonHours;
	}

	/**
	 * Returns the afternoon opening minutes.
	 * 
	 * @return the afternoon opening minutes.
	 */
	public int getOpeningAfternoonMinutes()
	{
		return openingAfternoonMinutes;
	}

	/**
	 * Returns the afternoon closing hours.
	 * 
	 * @return the afternoon closing hours.
	 */
	public int getClosingAfternoonHours()
	{
		return closingAfternoonHours;
	}

	/**
	 * Returns the afternoon closing minutes.
	 * 
	 * @return the afternoon closing minutes.
	 */
	public int getClosingAfternoonMinutes()
	{
		return closingAfternoonMinutes;
	}

	/**
	 * Returns true if the library is closed in the evening.
	 * 
	 * @return true if the library is closed in the evening.
	 */
	public boolean isEveningClosed()
	{
		return eveningClosed;
	}

	/**
	 * Returns the evening opening hours.
	 * 
	 * @return the evening opening hours.
	 */
	public int getOpeningEveningHours()
	{
		return openingEveningHours;
	}

	/**
	 * Returns the evening opening minutes.
	 * 
	 * @return the evening opening minutes.
	 */
	public int getOpeningEveningMinutes()
	{
		return openingEveningMinutes;
	}

	/**
	 * Returns the evening closing hours.
	 * 
	 * @return the evening closing hours.
	 */
	public int getClosingEveningHours()
	{
		return closingEveningHours;
	}

	/**
	 * Returns the evening closing minutes.
	 * 
	 * @return the evening closing minutes.
	 */
	public int getClosingEveningMinutes()
	{
		return closingEveningMinutes;
	}
}