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
import java.math.BigDecimal;
import java.util.Date;

/**
 * A borrower fine.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Fine implements Serializable 
{
	private static final long serialVersionUID = 6136536793557504830L;
	
	private final Date date;
	private final BigDecimal totalAmount;
	private final BigDecimal balance;
	
	/**
	 * Builds a new fine with the given data.
	 * 
	 * @param date the due date.
	 * @param totalAmount the total amount.
	 * @param balance the current balance,
	 */
	public Fine(final Date date, final BigDecimal totalAmount, final BigDecimal balance) 
	{
		this.date = date;
		this.totalAmount = totalAmount;
		this.balance = balance;
	}

	/**
	 * Returns the due date.
	 * 
	 * @return the due date.
	 */
	public Date getDate() 
	{
		return date;
	}

	/**
	 * Returns the total amount of the fine.
	 * 
	 * @return the total amount of the fine.
	 */
	public BigDecimal getTotalAmount() 
	{
		return totalAmount;
	}

	/**
	 * Returns the balance of the fine.
	 * 
	 * @return the balance of the fine.
	 */	
	public BigDecimal getBalance() 
	{
		return balance;
	}
}