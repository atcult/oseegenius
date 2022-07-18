package com.atc.osee.web.model;

import java.io.Serializable;
import java.sql.Date;

/**
 * An order for a given bibliographic record.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class Order implements Serializable 
{
	private static final long serialVersionUID = 2662648800159234393L;
	
	private final int mainLibraryId;
	private final Date date;
	private final int quantity;
	private final int receivedQuantity;
	private final String statusCode;

	/**
	 * Builds a new {@link Order} with the given data.
	 * 
	 * @param mainLibraryId the main library identifier.
	 * @param date the order date.
	 * @param quantity the order (item) quantity.
	 * @param receivedQuantity the received quantity.
	 * @param statusCode the order status code.
	 */
	public Order(
			final int mainLibraryId,
			final Date date, 
			final int quantity, 
			final int receivedQuantity, 
			final int statusCode) 
	{
		this.mainLibraryId = mainLibraryId;
		this.date = date;
		this.quantity = quantity;
		this.receivedQuantity = receivedQuantity;
		this.statusCode = String.valueOf(statusCode);
	}
	
	/**
	 * Returns the date of this order.
	 * 
	 * @return the date of this order.
	 */
	public Date getDate() 
	{
		return date;
	}

	/**
	 * Returns the ordered quantity of this order.
	 * 
	 * @return the ordered quantity of this order.
	 */
	public int getQuantity() 
	{
		return quantity;
	}
	
	/**
	 * Returns the received quantity of this order.
	 * 
	 * @return the received quantity of this order.
	 */
	public int getReceivedQuantity() 
	{
		return receivedQuantity;
	}

	/**
	 * Returns the status code of this order.
	 * 
	 * @return the status code of this order.
	 */
	public String getStatusCode() 
	{
		return statusCode;
	}
	
	/**
	 * Returns the main library id of this order.
	 * 
	 * @return the main library id of this order.
	 */
	public int getMainLibraryId()
	{
		return mainLibraryId;
	}
}