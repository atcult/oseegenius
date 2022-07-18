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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

/**
 * A special facet used in advanced search to add limit constraints.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class LimitFacet implements Serializable
{
	private static final long serialVersionUID = 6217368937272347965L;
	private final String name;
	private List<LimitFacetEntry> entries = new ArrayList<LimitFacetEntry>();
	
	/**
	 * Builds a new facet with the given name.
	 * 
	 * @param name the facet name.
	 */
	public LimitFacet(final String name)
	{
		this.name = name;
	}

	/**
	 * Returns the facet name.
	 * 
	 * @return the name of this facet.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Adds a new entry (count - value).
	 * 
	 * @param count the new entry (count - value).
	 */
	public void addEntry(final Count count)
	{
		entries.add(new LimitFacetEntry(count, false));
	}
	
	/**
	 * Adds a new entry (count - value).
	 * 
	 * @param count the new entry (count - value).
	 */
	public void addSelectedEntry(final Count count)
	{
		LimitFacetEntry entry = new LimitFacetEntry(count, true);
		if (!entries.contains(entry))
		{
			entries.add(new LimitFacetEntry(count, true));
		}
	}
	
	public void addOrUpdate(final String name, long count, boolean checked, boolean outsider)
	{
		LimitFacetEntry targetEntry = null;
		for (LimitFacetEntry entry : entries)
		{
			if (entry.getName().equals(name))
			{
				targetEntry = entry;
				break;
			}
		}
		
		if (targetEntry == null)
		{
			targetEntry = new LimitFacetEntry(new Count(new FacetField(name), name, count), checked, outsider);
			entries.add(targetEntry);
		} else
		{
			targetEntry.setChecked(checked);
		}
	}
	
	/**
	 * Return the values of this facet.
	 * 
	 * @return the values of this facet.
	 */
	public List<LimitFacetEntry> getValues()
	{
		return entries;
	}
	
	public boolean isSelected(final String value)
	{
		for (LimitFacetEntry entry : entries)
		{
			if (value.equals(entry.getName()) && entry.isChecked())
			{
				return true;
			}
		}
		return false;
	}

	public void uncheck(final String value) 
	{
		for (Iterator<LimitFacetEntry> iterator = entries.iterator(); iterator.hasNext();)
		{
			LimitFacetEntry entry = iterator.next();
			if (value.equals(entry.getName()))
			{
				iterator.remove();
				return;
			}
		}
	}
}