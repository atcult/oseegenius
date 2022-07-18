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
import java.util.Date;

/**
 * A borrower hold.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Hold implements Serializable 
{
	private static final long serialVersionUID = -3057024920067871935L;
	
	private final int amicusNumber;
	private final String pickupBranchName;
	private final int holdType;
	private final Date date;
	private final String title;
	private final String author;
	private final Date startDate;
	private final Date endDate;
	private boolean online;
	private Date dueDate;
	private String shelfList;
	private int copyId;
	
	public int getCopyId() {
		return copyId;
	}

	public void setCopyId(int copyId) {
		this.copyId = copyId;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	/**
	 * Builds a new hold with the given data.
	 * 
	 * @param amicusNumber the bibliographic record identifier.
	 * @param pickupBranchName the pickup branch name.
	 * @param holdType the hold type.
	 * @param date the hold date.
	 * @param title the title of the target item.
	 * @param author the author of the target item.
	 * @param startDate the start date (in case of time hold).
	 * @param endDate the end date (in case of time hold).
	 */
	public Hold(
			final int amicusNumber, 
			final String pickupBranchName, 
			final int holdType,
			final Date date, 
			final String title, 
			final String author, 
			final Date startDate, 
			final Date endDate) 
	{
		this.amicusNumber = amicusNumber;
		this.pickupBranchName = pickupBranchName;
		this.holdType = holdType;
		this.date = date;
		this.title = title;
		this.author = author;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public String getShelfList() {
		return shelfList;
	}

	public void setShelfList(String shelfList) {
		this.shelfList = shelfList;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * Returns the bibliographic record identifier.
	 * 
	 * @return the bibliographic record identifier.
	 */
	public int getAmicusNumber() 
	{
		return amicusNumber;
	}

	/**
	 * Returns the branch name.
	 * 
	 * @return the branch name.
	 */
	public String getPickupBranchName() 
	{
		return pickupBranchName;
	}

	/**
	 * Returns the hold type.
	 * 
	 * @return the hold type.
	 */
	public int getHoldType() 
	{
		return holdType;
	}

	/**
	 * Returns the hold date.
	 * 
	 * @return the hold date.
	 */
	public Date getDate() 
	{
		return date;
	}

	/**
	 * Returns the target item title.
	 * 
	 * @return the target item title.
	 */
	public String getTitle() 
	{
		return title;
	}

	/**
	 * Returns the target item author.
	 * 
	 * @return the target item author.
	 */
	public String getAuthor() 
	{
		return author;
	}

	/**
	 * Returns the hold start date.
	 * 
	 * @return the hold start date,
	 */
	public Date getStartDate() 
	{
		return startDate;
	}

	/**
	 * Returns the hold end date.
	 * 
	 * @return the hold end date,
	 */
	public Date getEndDate() 
	{
		return endDate;
	}
}