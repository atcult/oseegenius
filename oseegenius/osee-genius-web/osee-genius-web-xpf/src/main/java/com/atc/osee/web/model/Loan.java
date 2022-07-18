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
 * A borrower loan.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Loan implements Serializable 
{
	private static final long serialVersionUID = 1983915606413218659L;

	private final Date recallNotificationDate;
	private final Date date;
	private final Date dueDate;
	private final String barcode;
	private final String title;
	private final String author;
	private final int amicusNumber;
	private final int copyId;
	private final String shelflist;
	private final int renewalCount;
	private final Date firstOverdueDate;
	private final Date secondOverdueDate;
	private final Date thirdOverdueDate;
	private int branchId;
	private int checkinBranchId;
	
	/**
	 * Builds a new loan with the given date.
	 * 
	 * @param recallNotificationDate the recall date.
	 * @param date the loan date.
	 * @param dueDate the due date.
	 * @param barcode the barcode of the target copy.
	 * @param title the title of the underlying copy.
	 * @param author the author of the underlying copy.
	 * @param amicusNumber the identifier of the bibliographic record.
	 */
	public Loan(
			final Date recallNotificationDate, final Date date, final Date dueDate,
			final String barcode, final String title, final String author, final int amicusNumber, 
			final int copyId, final String shelflist, final int renewalCount,
			final Date firstOverdueDate, final Date secondOverdueDate, final Date thirdOverdueDate) 
	{
		this.recallNotificationDate = recallNotificationDate;
		this.date = date;
		this.dueDate = dueDate;
		this.barcode = barcode;
		this.title = title;
		this.author = author;
		this.amicusNumber = amicusNumber;
		this.copyId = copyId;
		this.shelflist = shelflist;
		this.renewalCount = renewalCount;
		this.firstOverdueDate = firstOverdueDate;
		this.secondOverdueDate = secondOverdueDate;
		this.thirdOverdueDate = thirdOverdueDate;
	}

	public int getBranchId() 
	{
		return branchId;
	}

	public void setBranchId(int branchId) 
	{
		this.branchId = branchId;
	}
	
	
	
	public int getCheckinBranchId() {
		return checkinBranchId;
	}

	public void setCheckinBranchId(int checkinBranchId) {
		this.checkinBranchId = checkinBranchId;
	}

	/**
	 * Returns the recall date.
	 * 
	 * @return the recall date.
	 */
	public Date getRecallNotificationDate() 
	{
		return recallNotificationDate;
	}

	/**
	 * Returns the loan date.
	 * 
	 * @return the loan date.
	 */
	public Date getDate() 
	{
		return date;
	}

	/**
	 * Returns the loan due date.
	 * 
	 * @return the loan due date.
	 */
	public Date getDueDate() 
	{
		return dueDate;
	}

	/**
	 * Returns the copy barcode.
	 * 
	 * @return the copy barcode.
	 */
	public String getBarcode() 
	{
		return barcode;
	}

	/**
	 * Returns the title of the copy.
	 * 
	 * @return the title of the copy.
	 */
	public String getTitle() 
	{
		return title;
	}

	/**
	 * Returns the copy author.
	 * 
	 * @return the copy author.
	 */
	public String getAuthor() 
	{
		return author;
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
	 * Returns the copy identifier.
	 * 
	 * @return the copy identifier.
	 */
	public int getCopyId()
	{
		return copyId;
	}

	public String getShelflist() 
	{
		return shelflist;
	}

	public int getRenewalCount() 
	{
		return renewalCount;
	}

	public Date getFirstOverdueDate() 
	{
		return firstOverdueDate;
	}

	public Date getSecondOverdueDate() 
	{
		return secondOverdueDate;
	}

	public Date getThirdOverdueDate() 
	{
		return thirdOverdueDate;
	}
}