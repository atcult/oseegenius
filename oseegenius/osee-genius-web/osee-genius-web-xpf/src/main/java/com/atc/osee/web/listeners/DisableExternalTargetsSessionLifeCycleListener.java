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
package com.atc.osee.web.listeners;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;

/** 
 * At the beginning of a new session disables all federated targets.
 * This listener assumes that the federated target choose component has been licensed.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class DisableExternalTargetsSessionLifeCycleListener implements HttpSessionListener
{
	/**
	 * A new session has been created.
	 * 
	 * @param event the HTTP session event.
	 */
	public void sessionCreated(final HttpSessionEvent event)
	{
		HttpSession session = event.getSession();
		
		Set<String> enabledTargets = new HashSet<String>();
		session.setAttribute(HttpAttribute.TARGETS_ENABLED, enabledTargets);
	}
	
	/**
	 * A session has been destroyed.
	 * 
	 * @param event the HTTP session event.
	 */
	@SuppressWarnings("unchecked")
	public void sessionDestroyed(final HttpSessionEvent event)
	{
		HttpSession session = event.getSession();
		Map<String, HttpSession> activeSessions = (Map<String, HttpSession>)session.getServletContext().getAttribute(HttpAttribute.ACTIVE_SESSIONS);
		if (activeSessions != null)
		{
			activeSessions.remove(session.getId());
		}
		
		Log.debug(
				MessageCatalog._000009_SESSION_HAS_BEEN_DESTROYED, 
				session.getId());
	}
}