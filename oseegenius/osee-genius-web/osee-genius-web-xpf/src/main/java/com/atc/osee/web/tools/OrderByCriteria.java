package com.atc.osee.web.tools;

import java.io.Serializable;

public class OrderByCriteria implements Serializable 
{
	private static final long serialVersionUID = -650685585847249526L;
	
	private final String messageKey;
	private final String value;
	
	public OrderByCriteria(final String messageKey, final String value) 
	{
		this.messageKey = messageKey;
		this.value = value;
	}

	public String getMessageKey() 
	{
		return messageKey;
	}

	public String getValue() 
	{
		return value;
	}
	
}