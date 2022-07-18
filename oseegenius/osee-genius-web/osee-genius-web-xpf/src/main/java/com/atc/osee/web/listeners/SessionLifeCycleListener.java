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

import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.http.impl.client.CloseableHttpClient;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;
import com.atc.osee.web.model.Visit;

/** 
 * Osee Genius session lifecycle listener.
 * 
 * @author Giorgio Gazzarini
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class SessionLifeCycleListener implements HttpSessionListener
{
	/**
	 * A new session has been created.
	 * 
	 * @param event the HTTP session event.
	 */
	@SuppressWarnings("unchecked")
	public void sessionCreated(final HttpSessionEvent event)
	{
		HttpSession session = event.getSession();
	
		Log.debug(MessageCatalog._000008_SESSION_HAS_BEEN_CREATED, session.getId());
		
		Map<String, HttpSession> activeSessions = (Map<String, HttpSession>) session.getServletContext().getAttribute(HttpAttribute.ACTIVE_SESSIONS);
		if (activeSessions != null)
		{
			activeSessions.put(session.getId(), session);
		}
		
		initialiseUserExperienceDomainModel(session);
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
	
	private final static Random RANDOMIZER = new Random();
	
	/**
	 * Creates the domain model associated with the given user session.
	 * 
	 * @param session the user session
	 * @return the visit domain object.
	 */
	protected Visit initialiseUserExperienceDomainModel(final HttpSession session)
	{
		ServletContext application = session.getServletContext();
		
		String federatedSearchEngines= (String)application.getAttribute(HttpAttribute.FEDERATED_SEARCH_ENDPOINT_URL);
		String federatedSearchEngineURL = null;
		if (federatedSearchEngines != null)
		{
			String [] urls = federatedSearchEngines.split(",");
			federatedSearchEngineURL = urls[RANDOMIZER.nextInt(urls.length)];
		}
		
		Visit visit = new Visit(
				(CloseableHttpClient)application.getAttribute(HttpAttribute.FEDERATED_SEARCH_HTTP_CLIENT),
				federatedSearchEngineURL);

		session.setAttribute(HttpAttribute.VISIT, visit);
		return visit;
	}
}