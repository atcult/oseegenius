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

import org.apache.solr.client.solrj.response.FacetField.Count;

import com.atc.osee.web.IConstants;

/**
 * A single value of a given "limit" facet.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class LimitFacetEntry implements Serializable
{
	private static final long serialVersionUID = -5946355000029735782L;
	private final Count count;
	private boolean checked;
	private final boolean outsider;
	
	/**
	 * Builds a new entry with the given data.
	 * 
	 * @param count the total count.
	 * @param checked a flag indicating if this value has been selected.
	 */
	public LimitFacetEntry(final Count count, final boolean checked)
	{
		this.count = count;
		this.checked = checked;
		this.outsider = false;
	}

	/**
	 * Builds a new entry with the given data.
	 * 
	 * @param count the total count.
	 * @param checked a flag indicating if this value has been selected.
	 */
	public LimitFacetEntry(final Count count, final boolean checked, final boolean outsider)
	{
		this.count = count;
		this.checked = checked;
		this.outsider = outsider;
	}

	/**
	 * Returns the count of the entry.
	 * 
	 * @return the count of this entry.
	 */
	public long getCount()
	{
		return count.getCount();
	}

	/**
	 * Returns true if this entry has been checked.
	 * 
	 * @return true if this entry has been checked.
	 */
	public boolean isChecked()
	{
		return checked;
	}

	/**
	 * Sets the checked flag for this entry.
	 * 
	 * @param checked the checked flag for this entry.
	 */
	public void setChecked(final boolean checked)
	{
		this.checked = checked;
	}
	
	/**
	 * Returns the name of this entry.
	 * 
	 * @return the name of this entry.
	 */
	public String getName()
	{
		return count != null ? count.getName() : IConstants.EMPTY_STRING;
	}
	
	/**
	 * Returns true if the entry is not shown in the advanced search page but was selected outside.
	 * 
	 * @return true if the entry is not shown in the advanced search page but was selected outside.
	 */
	public boolean isOutsider() 
	{
		return outsider;
	}

	@Override
	public boolean equals(Object obj) 
	{
		return obj instanceof LimitFacetEntry && getName().equals(((LimitFacetEntry)obj).getName());
	}
}