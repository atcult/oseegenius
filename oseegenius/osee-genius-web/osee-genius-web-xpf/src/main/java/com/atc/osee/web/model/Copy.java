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

import java.util.Date;
 
/**
 * A copy, that is an instance of a given bibliographic record.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Copy 
{
	private final String shelf;
	private final String location;
	private final Date dueDate;
	private final boolean onOrder;

	private final int branchId;
	private final int mainLibraryId;
	private String branchName;

	private int copyId;
	
	private int reservationCount;
	
	private int styleClass;   //styleClass : Values: 0 | 1 (used to color rows with 2 different colors)
	private int countId; //uesd for ui. Counter field. (used to rotate logo colors on first coloumn of available table)
	
	/**
	 * Builds a new copy with the given data.
	 * 
	 * @param copyId the copy identifier.
	 * @param shelf the shelf location.
	 * @param location the location.
	 * @param dueDate the due date (in case the copy is on loan)
	 * @param onOrder true if the copy is not available but on order.
	 * @param branchId the owning branch idenfier.
	 * @param branchName the owning branch name.
	 * @param mainLibraryId the main library identifier.
	 * @param reservationCount total count of reservations.
	 */
	public Copy(
			final int copyId, 
			final String shelf, 
			final String location, 
			final Date dueDate, 
			final boolean onOrder, 
			final int branchId, 
			final String branchName, 
			final int mainLibraryId, 
			final int reservationCount) 
	{
		this.copyId = copyId;
		this.shelf = shelf;
		this.location = location;
		this.dueDate = dueDate;
		this.onOrder = onOrder;
		this.branchId = branchId;
		this.branchName = branchName;
		this.mainLibraryId = mainLibraryId;
		this.reservationCount = reservationCount;
	}

	/**
	 * Sets the owning branch name.
	 * 
	 * @param branchName the owning branch name.
	 */
	public void setBranchName(final String branchName)
	{
		this.branchName = branchName;
	}	
	
	/**
	 * TBD.
	 * 
	 * @return tbd.
	 */
	public int getCountId()
	{
		return countId;
	}

	/**
	 * Sets the TBD.
	 * 
	 * @param countId tbd.
	 */
	public void setCountId(final int countId)
	{
		this.countId = countId;
	}

	/**
	 * Sets the style class of this copy.
	 * 
	 * @param styleClass the style class of this copy.
	 */
	public void setStyleClass(final int styleClass)
	{
		this.styleClass = styleClass;
	}

	/**
	 * Returns the style class of this copy.
	 * 
	 * @return the style class of this copy.
	 */
	public int getStyleClass()
	{
		return styleClass;
	}
	
	/**
	 * Returns the copy identifier.
	 * 
	 * @return the copy identifier.
	 */
	public int getCopyId()
	{
		return copyId;
	}

	/**
	 * Setthe copy identifier.
	 * 
	 * @param copyId the copy identifier.
	 */
	public void setCopyId(final int copyId)
	{
		this.copyId = copyId;
	}

	/**
	 * Returns the shelf location.
	 * 
	 * @return the shelf location.
	 */
	public String getShelf() 
	{
		return shelf;
	}

	/**
	 * Returns the copy location.
	 * 
	 * @return the copy location.
	 */
	public String getLocation() 
	{
		return location;
	}

	/**
	 * Returns the due date. 
	 * Null if the copy is available.
	 * 
	 * @return the due date, null if the copy is available.
	 */
	public Date getDueDate() 
	{
		return dueDate;
	}

	/**
	 * Returns true if the copy is on order.
	 * 
	 * @return true if the copy is on order.
	 */
	public boolean isOnOrder() 
	{
		return onOrder;
	}

	/**
	 * Returns the owning branch identifier.
	 * 
	 * @return the owning branch identifier.
	 */
	public int getBranchId() 
	{
		return branchId;
	}

	/**
	 * Returns the owning branch name.
	 * 
	 * @return the owning branch name.
	 */
	public String getBranchName() 
	{
		return branchName;
	}
	
	/**
	 * Returns the main library identifier.
	 * 
	 * @return the main library identifier.
	 */
	public int getMainLibraryId() 
	{
		return mainLibraryId;
	}

	/**
	 * Returns the total reservation count.
	 * 
	 * @return the total reservation count.
	 */
	public int getReservationCount()
	{
		return reservationCount;
	}
	
	/**
	 * Returns true if the copy is available.
	 * 
	 * @return true if the copy is available.
	 */
	public boolean isAvailable()
	{
		return dueDate == null && !onOrder;
	}
}