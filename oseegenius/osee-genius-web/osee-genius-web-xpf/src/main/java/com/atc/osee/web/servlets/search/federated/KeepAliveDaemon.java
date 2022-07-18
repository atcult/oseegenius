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
package com.atc.osee.web.servlets.search.federated;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.model.Visit;

/**
 * PazPar2 sessions keep alive. 
 * Makes sure that pazpar2 (active) sessions don't expire.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class KeepAliveDaemon extends Thread
{
	private boolean isShutdown = false;
	
	private final ServletContext application;
	
	/**
	 * Builds a new keep alive daemon associated with the given application.
	 * 
	 * @param application the application context.
	 */
	public KeepAliveDaemon(final ServletContext application)
	{
		this.application = application;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() 
	{
		while (!isShutdown)
		{
			try 
			{
				Map<String, HttpSession> activeSessions = (Map<String, HttpSession>) application.getAttribute(HttpAttribute.ACTIVE_SESSIONS);
				if (activeSessions != null)
				{
					for (HttpSession session : activeSessions.values())
					{
						List<SearchTab> searchTabs = ((Visit)session.getAttribute(HttpAttribute.VISIT)).getSearchExperience().getTabs();
						for (SearchTab tab : searchTabs)
						{
							try
							{
								Set<String> enabledTargets = (Set<String>) session.getAttribute(HttpAttribute.TARGETS_ENABLED);
								if (enabledTargets == null || !enabledTargets.isEmpty())
								{
									tab.getPazpar2().ping();
								}
							} catch (SystemInternalFailureException exception)
							{
								Log.error(MessageCatalog._100019_FEDERATED_SESSION_PING_FAILURE);
							}
						}
					}
				}
				Thread.sleep(20000);
			} catch (InterruptedException exception) 
			{
				Log.warning(MessageCatalog._100025_KA_DAEMON_SLEEP_INTERRUPTED);
			}
		}
		
		try 
		{ 
			Log.info(MessageCatalog._000017_KA_DAEMON_SHUT_DOWN); 
		} catch (Exception ignore)
		{
			// Nothing to be done here...
		}
	}
	
	/**
	 * Marks this daemon as shut down.
	 */
	public void shutdown()
	{
		isShutdown = true;
	}
}