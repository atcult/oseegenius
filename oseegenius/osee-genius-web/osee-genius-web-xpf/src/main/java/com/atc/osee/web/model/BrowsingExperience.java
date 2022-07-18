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
import java.util.List;

/**
 * OseeGenius -W- User browsing experience.
 * 
 * @author agazzarini
 * @since 1.0
 */
public final class BrowsingExperience implements Serializable
{
	private static final long serialVersionUID = 8581485468726471540L;
	private final List<BrowseTab> tabs;
	private BrowseTab currentTab;
	private final Visit visit;
	
	private int tabIndex;
	
	/**
	 * Initialize array for tabs.
	 * 
	 * @param visit the visit associated with the user session.
	 */
	public BrowsingExperience(final Visit visit)
	{
		this.visit = visit;
		tabs = new ArrayList<BrowseTab>();
		currentTab = new BrowseTab(visit.getPreferredLocale(), ++tabIndex);
		currentTab.setResultsPerPage(10);
		tabs.add(currentTab);
	}
	
	/**
	 * Returns the tab associated with the given identifier.
	 * 
	 * @param tabId the tab identifier.
	 * @return the tab associated with the given identifier.
	 */
	public BrowseTab getTab(final int tabId)
	{		
		for (BrowseTab tab : tabs)
		{
			if (tabId == tab.getId())
			{
				currentTab = tab;
			}
		}
		
		return currentTab;
	}
	
	/**
	 * Returns the browsing tabs.
	 * 
	 * @return the browsing tabs.
	 */
	public List<BrowseTab> getTabs()
	{
		return tabs;
	}
	
	/**
	 * Returns the current browsing tab.
	 * 
	 * @return the current browsing tab.
	 */
	public BrowseTab getCurrentTab()
	{
		return currentTab;
	}
	
	/**
	 * Closes the current tab.
	 */
	public void closeTab()
	{
		int indexOfCurrentTab = tabs.indexOf(currentTab);
		tabs.remove(currentTab);

		if (tabs.size() == 0)
		{
			currentTab = new BrowseTab(visit.getPreferredLocale(), ++tabIndex);
			currentTab.setResultsPerPage(10);
			tabs.add(currentTab);					
		} else 
		{
			int index = (indexOfCurrentTab - 1);
			currentTab = tabs.get(index  >= 0 ? index : 0);
		}
	}
	
	/**
	 * Adds a new browsing tab.
	 */
	public void addNewTab()
	{
		currentTab = new BrowseTab(visit.getPreferredLocale(), ++tabIndex);
		currentTab.setResultsPerPage(10);
		tabs.add(currentTab);		
	}

	/**
	 * Sets the current tab.
	 * 
	 * @param tabId the tab identifier.
	 */
	public void setCurrentTab(final int tabId)
	{
		for (BrowseTab tab : tabs)
		{
			if (tabId == tab.getId())
			{
				currentTab = tab;
			}
		}
	}
	
	/**
	 * Checks if the tab contains no data.
	 * 
	 * @return true if the tab contains no data.
	 */
	public boolean isEmpty()
	{
		return tabs.size() == 1 && currentTab.isEmpty();
	}
	
	/**
	 * Closes and resets all tabs.
	 */
	public void resetTabs()
	{
		currentTab.clear();
		tabs.clear();
		tabs.add(currentTab);
	}
}