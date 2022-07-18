package com.atc.osee.web.listeners;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.model.Visit;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

public class LogicalViewChangeListener  implements HttpSessionAttributeListener
{
	@Override
	public void attributeAdded(HttpSessionBindingEvent event)
	{
		clearLogicalViewContext(event.getSession(), event.getName());
	}

	@Override
	public void attributeRemoved(HttpSessionBindingEvent event) 
	{
		clearLogicalViewContext(event.getSession(), event.getName());
	}

	@Override
	public void attributeReplaced(HttpSessionBindingEvent event) 
	{
		clearLogicalViewContext(event.getSession(), event.getName());
	}
	
	private void clearLogicalViewContext(final HttpSession session, final String name)
	{
		if (HttpAttribute.LOGICAL_VIEW.equals(name))
		{
			Visit visit = (Visit) session.getAttribute(HttpAttribute.VISIT);
			visit.getSearchExperience().resetTabs();
			visit.getBrowsingExperience().resetTabs();
		}
	}
}
