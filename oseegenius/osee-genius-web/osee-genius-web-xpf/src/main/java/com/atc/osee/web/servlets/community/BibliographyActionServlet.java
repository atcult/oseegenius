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
package com.atc.osee.web.servlets.community;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Account;
import com.atc.osee.web.plugin.CommunityPlugin;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * Adds or removes a document to / from user bibliography.
 * Note that in order to properly work, this OseeGenius -W- instance
 * must have the community extension enabled.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class BibliographyActionServlet extends OseeGeniusServlet
{
	private static final long serialVersionUID = 1L;
       
	private static final String ADD_ACTION = "add";
	private static final String REMOVE_ACTION = "remove";
	
	/**
	 * A simple action / command interface used for abstracting 
	 * bibliography actions.
	 * 
	 * @author Andrea Gazzarini
	 * @since 1.0
	 */
	private interface Action 
	{
		/**
		 * Executes a specific action.
		 * The logic of the action depends on the concrete implementor.
		 * 
		 * @param plugin the community plugin.
		 * @param account the user account.
		 * @param documentUri the document URI (identifier).
		 * @throws SystemInternalFailureException in case of system failure.
		 */
		void execute(CommunityPlugin plugin, Account account, String documentUri) throws SystemInternalFailureException;
	}
	
	Map<String, Action> actions = new HashMap<String, Action>(4);
	{
		actions.put(ADD_ACTION, new Action() 
		{	
			@Override
			public void execute(
					final CommunityPlugin plugin, 
					final Account account, 
					final String documentUri) throws SystemInternalFailureException 
			{
				plugin.addToBibliography(account.getId(), documentUri);
			}
		});

		actions.put(REMOVE_ACTION, new Action() 
		{	
			@Override
			public void execute(
					final CommunityPlugin plugin, 
					final Account account, 
					final String documentUri) throws SystemInternalFailureException 
			{
				plugin.removeFromBibliography(account.getId(), documentUri);
			}
		});
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String documentId = request.getParameter(HttpParameter.URI);
		String action = request.getParameter(HttpParameter.ACTION);
		
		try 
		{
			actions.get(action).execute(getLicense(request).getCommunityPlugin(), getVisit(request).getAccount(), documentId);
		} catch (NullPointerException exception)
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);			
		} catch (Exception exception)
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}