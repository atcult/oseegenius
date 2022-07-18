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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.LicenseTool;

/**
 * OseeGenius -W- PazPar2 proxy servlet.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class PazPar2Servlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 1L;   
	
	/**
	 * Pazpar2 command executor interface.
	 * 
	 * @author agazzarini
	 * @since 1.0
	 */
	interface PazPar2CommandExecutor
	{
		/**
		 * Executes a given command.
		 * 
		 * @param proxy the PazPar2 proxy.
		 * @param request the http request.
		 * @param response the http response.
		 * @throws ServletException in case this executor raises an exception.
		 * @throws IOException in case of I/O problems.
		 * @throws SystemInternalFailureException in case of system failure.
		 */
		void execute(PazPar2 proxy, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SystemInternalFailureException;
	}
	
	private final PazPar2CommandExecutor show = new PazPar2CommandExecutor() 
	{	
		@Override
		public void execute(
				final PazPar2 proxy, 
				final HttpServletRequest request, 
				final HttpServletResponse response) throws ServletException, IOException, SystemInternalFailureException 
		{
			response.setCharacterEncoding("UTF-8");
			String pazpar2Response = proxy.show(request);
			Log.debug("pazpar2Response " + pazpar2Response);
			response.getWriter().println(pazpar2Response);
		}		
	};

	private final PazPar2CommandExecutor termlist = new PazPar2CommandExecutor() 
	{	
		@Override
		public void execute(
				final PazPar2 proxy, 
				final HttpServletRequest request, 
				final HttpServletResponse response) throws ServletException, IOException, SystemInternalFailureException 
		{
			response.setCharacterEncoding("UTF-8");
			
			String pazpar2Response = proxy.termlist(request.getQueryString());
			Log.debug("request.getQueryString() " + request.getQueryString());
			Log.debug("pazpar2Response " + pazpar2Response);
			collectTargetsOnTermlistResponse(request, pazpar2Response);
			response.getWriter().println(pazpar2Response);
		}
	};

	private final PazPar2CommandExecutor ping = new PazPar2CommandExecutor() 
	{	
		@Override
		public void execute(
				final PazPar2 proxy, 
				final HttpServletRequest request, 
				final HttpServletResponse response) throws ServletException, IOException, SystemInternalFailureException 
		{
			proxy.ping();
		}
	};

	private final PazPar2CommandExecutor stat = new PazPar2CommandExecutor() 
	{	
		@Override
		public void execute(
				final PazPar2 proxy, 
				final HttpServletRequest request, 
				final HttpServletResponse response) throws ServletException, IOException, SystemInternalFailureException 
		{
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(proxy.stat());
		}
	};
	
	private final Map<String, PazPar2CommandExecutor> executors = new HashMap<String, PazPar2CommandExecutor>(5);
	{
		executors.put("show", show);
		executors.put("termlist", termlist);
		executors.put("ping", ping);
		executors.put("stat", stat);
	}
	
	@Override
	protected void doGet(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		LicenseTool license = getLicense(request);
		if (license.isFederatedSearchEnabled())
		{
			response.setCharacterEncoding("UTF-8");
			String command = request.getParameter(HttpParameter.COMMAND);
			command = isNotNullOrEmptyString(command) ? command : "ping";
			response.setContentType("text/xml; charset=UTF-8");
			  // Set to expire far in the past.
			response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");

			  // Set standard HTTP/1.1 no-cache headers.
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

			  // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
			response.addHeader("Cache-Control", "post-check=0, pre-check=0");

			  // Set standard HTTP/1.0 no-cache header.
			response.setHeader("Pragma", "no-cache");
			
			try
			{
				executors.get(command).execute(
						getSearchExperience(request).getCurrentTab().getPazpar2(), 
						request, 
						response);
			} catch (SystemInternalFailureException exception)
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else
		{
			response.sendError(
					HttpServletResponse.SC_FORBIDDEN, 
					"Federated search has not been enabled on this OseeGenius -W- instance.");
		}
	}
	
	@Override
	protected long getLastModified(HttpServletRequest req) 
	{
		return System.currentTimeMillis();
	}
	
	/**
	 * Collects the target using the given Pazpar2 response.
	 * 
	 * @param request the HTTP request.
	 * @param pazpar2Response the PazPar2 response.
	 */
	private void collectTargetsOnTermlistResponse(final HttpServletRequest request, final String pazpar2Response)
	{
		if (pazpar2Response.indexOf("<list name=\"xtargets\">") != -1)
		{
			int startIndex = 0;
			int indexOfId = -1;
			while ((indexOfId = pazpar2Response.indexOf("<id>", startIndex)) != -1)
			{
				if (indexOfId != -1)
				{
					int indexOfIdValue = indexOfId + 4;
					if (indexOfIdValue != -1)
					{
						String id = pazpar2Response.substring(indexOfIdValue, pazpar2Response.indexOf("</id>", indexOfIdValue + 5)).trim();
	
						int indexOfName = pazpar2Response.indexOf("<name>", indexOfIdValue);
						if (indexOfName != -1)
						{
							String name = pazpar2Response.substring(indexOfName + 6, pazpar2Response.indexOf("</name>", indexOfName + 7));
							collect(id, name, request);
							startIndex = indexOfName;
						}
					}
				}
			}
		}
	}	

	/**
	 * Collects the given target name.
	 * 
	 * @param id the target identifier.
	 * @param name the target name.
	 * @param request the HTTP request.
	 */
	@SuppressWarnings("unchecked")
	private void collect(final String id, final String name, final HttpServletRequest request)
	{
		Map<String, String> federatedTargets = (Map<String, String>) request.getSession(true).getServletContext().getAttribute(HttpAttribute.FEDERATED_SEARCH_TARGETS);
		if (federatedTargets == null)
		{
			federatedTargets =  new HashMap<String, String>();
			request.getSession(true).getServletContext().setAttribute(HttpAttribute.FEDERATED_SEARCH_TARGETS, federatedTargets);
		}
		
		if (!federatedTargets.containsKey(id))
		{
			Log.debug("New XTarget has been collected (" + id + "=" + name);
			federatedTargets.put(id, name);
		}
	}
}