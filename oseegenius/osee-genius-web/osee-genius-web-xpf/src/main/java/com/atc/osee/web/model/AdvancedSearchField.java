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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * An advanced search field domain object.
 * An object-oriented view of a field that can be used in advanced search. 
 * 
 * @author agazzarini
 * @since 1.0
 */
public class AdvancedSearchField implements Serializable
{
	private static final long serialVersionUID = -7406394558075663742L;
	
	private int index;
	
	private String what = "";
	private String where;
	private String booleanOperator;

	/**
	 * Builds an advanced search field with the given data.
	 * 
	 * @param index the index (id) of this field.
	 * @param where the targetAttribute
	 * @param booleanOperator the boolean operator if this field and the next one.
	 */
	public AdvancedSearchField(final int index, final String where, final String booleanOperator) 
	{
		this.index = index;
		this.where = where;
		this.booleanOperator = booleanOperator;
	}

	/**
	 * Returns the index of this field.
	 * 
	 * @return the index of this field.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * Sets the index of this field.
	 * 
	 * @param index the index of this field.
	 */
	public void setIndex(final int index)
	{
		this.index = index;
	}

	/**
	 * Returns the text that will be used as search criteria.
	 * 
	 * @return the text that will be used as search criteria.
	 */
	public String getWhat()
	{
		return what;
	}

	/**
	 * Sets the text that will be used as search criteria.
	 * 
	 * @param what the text that will be used as search criteria.
	 */
	public void setWhat(final String what)
	{
		this.what = (what != null && what.trim().length() != 0) ? what : "";
	}

	/**
	 * Returns the name of the target logical attribute associated with this field.
	 * 
	 * @return the name of the target logical attribute associated with this field.
	 */
	public String getWhere()
	{
		return where;
	}

	/**
	 * Sets the name of the target logical attribute associated with this field.
	 * 
	 * @param where the name of the target logical attribute associated with this field.
	 */
	public void setWhere(final String where)
	{
		this.where = where;
	}

	/**
	 * Returns the boolean operator associated with this field.
	 * Note that this will be considered only if, in the resulting query, there's another subsequent field.
	 * 
	 * @return the boolean operator associated with this field.
	 */
	public String getBooleanOperator()
	{
		return booleanOperator;
	}

	/**
	 * Sets the boolean operator associated with this field.
	 * Note that this will be considered only if, in the resulting query, there's another subsequent field.
	 * 
	 * @param booleanOperator the boolean operator associated with this field.
	 */
	public void setBooleanOperator(final String booleanOperator)
	{
		this.booleanOperator = booleanOperator;
	}

	/**
	 * Appends the subquery associated with this field to the given builder.
	 * 
	 * @param builder the advanced search query builder.
	 * @return true if this field is valid and its subquery has been appended.
	 */
	public boolean appendSubQuery(final StringBuilder builder) 
	{
		if (what != null && what.trim().length() != 0 && where != null && where.trim().length() != 0)
		{
			try 
			{
				String terms = what;

				// BUG 6861: ANDNOT becomes AND -(terms)
				if (builder.toString().endsWith("ANDNOT ")) {
					builder.delete(builder.length() - 4, builder.length()).append(" ");
					terms = "-" + what + "";
				}

				builder.append(where).append(':').append(URLEncoder.encode(terms, "UTF-8")).append(' ').append(booleanOperator).append(' ');
				return true;
			} catch (UnsupportedEncodingException e) 
			{
				// Nothing
			}
		}
		return false;
	}
}