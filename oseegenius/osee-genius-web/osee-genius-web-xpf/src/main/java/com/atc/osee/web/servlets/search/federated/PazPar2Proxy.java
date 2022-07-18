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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.velocity.tools.view.ServletUtils;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.ISolrConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;
import com.atc.osee.web.tools.AvailableTargetsTool;

/**
 * OseeGenius -W- default implementation for PazPar2 proxy.
 * 
 * @author agazzarini
 * @since 1.0
 */
public final class PazPar2Proxy implements PazPar2, Serializable
{
	private static final long serialVersionUID = -138944757985823001L;

	private final PazPar2 notYetInitialized = new PazPar2()
	{
		private static final long serialVersionUID = -6589215162714834508L;

		@Override
		public String termlist(final String names) throws SystemInternalFailureException
		{
			try
			{ 
				init();
				currentState = ready;
				return currentState.termlist(names);
			} catch (final InitializationFailedException exception)
			{
				// Nothing to be done here...the state mustn't change and exception has been logged outside
			}
			throw new SystemInternalFailureException();
		}
		
		@Override
		public String show(final HttpServletRequest request) throws SystemInternalFailureException
		{
			try
			{ 
				init();
				currentState = ready;
				return currentState.show(request);
			} catch (final InitializationFailedException exception)
			{
				// Nothing to be done here...the state mustn't change and exception has been logged outside
			}
			throw new SystemInternalFailureException();
		}
		
		@Override
		public void search(final String query, final String otherParameters) throws SystemInternalFailureException
		{
			try
			{ 
				init();
				currentState = ready;
				currentState.search(query, otherParameters);
			} catch (final InitializationFailedException exception)
			{
				// Nothing to be done here...the state mustn't change and exception has been logged outside
			}
		}
		
		@Override
		public String record(final String uri, Integer offset) throws SystemInternalFailureException, NoSuchResourceException
		{
			try
			{ 
				init();
				currentState = ready;
				String response = currentState.record(uri, offset);
				if (response.indexOf("<error code=\"7\"") != -1)
				{
					throw new NoSuchResourceException();
				}
			} catch (InitializationFailedException exception)
			{
				// Nothing to be done here...the state mustn't change and exception has been logged outside
			}
			throw new SystemInternalFailureException();
		}

		@Override
		public void ping() throws SystemInternalFailureException
		{
			try
			{ 
				init();
				currentState = ready;
				currentState.ping();
			} catch (InitializationFailedException exception)
			{
				// Nothing to be done here...the state mustn't change and exception has been logged outside
			}
		}

		@Override
		public String stat() throws SystemInternalFailureException
		{
			try
			{ 
				init();
				currentState = ready;
				return currentState.stat();
			} catch (InitializationFailedException exception)
			{
				// Nothing to be done here...the state mustn't change and exception has been logged outside
			}
			throw new SystemInternalFailureException();
		}

		@Override
		public void search(final HttpServletRequest request) throws SystemInternalFailureException
		{
			try
			{ 
				init();
				currentState = ready;
				currentState.search(request);
			} catch (InitializationFailedException exception)
			{
				// Nothing to be done here...the state mustn't change and exception has been logged outside
			}
		}

		@Override
		public void disableAndOrEnableTargets(HttpServletRequest request) throws SystemInternalFailureException 
		{
			try
			{ 
				init();
				currentState = ready;
				currentState.disableAndOrEnableTargets(request);
			} catch (InitializationFailedException exception)
			{
				// Nothing to be done here...the state mustn't change and exception has been logged outside
			}
		}
	};
	
	private final PazPar2 ready = new PazPar2()
	{
		private static final long serialVersionUID = 3927340710286965425L;
		private String workingQuery;
		private String workingParameters;
		
		@Override
		public String termlist(final String names) throws SystemInternalFailureException
		{
			try
			{
				return command(TERMLIST_COMMAND, names);
			} catch (final SessionExpiredException e)
			{
				throw new SystemInternalFailureException(e);
			} 	
		}
		
		@Override
		public String show(final HttpServletRequest request) throws SystemInternalFailureException
		{
			try
			{
				return command(SHOW_COMMAND, request);
			} catch (final SessionExpiredException e)
			{
				throw new SystemInternalFailureException(e);
			}
		}
		
		@Override
		public void search(final HttpServletRequest request) throws SystemInternalFailureException
		{
			try
			{
				String query = request.getParameter(HttpParameter.QUERY);
				final String queryType = request.getParameter(HttpParameter.QUERY_TYPE);
				if (query == null || query.trim().length() == 0 || ISolrConstants.ALL.equals(query) || "adv".equals(queryType))
				{
					request.setAttribute(HttpAttribute.FEDERATED_SEARCH_APPLICABLE, false);
					return;
				}
				request.setAttribute(HttpAttribute.FEDERATED_SEARCH_APPLICABLE, true);
				
				query = buildPazPar2Query(request);
				if (workingQuery != null && workingQuery.equals(query))
				{
					Log.debug(
							MessageCatalog._200001_FEDERATED_SEARCH_ALREADY_EXECUTED, 
							workingQuery);					
				} else 
				{
					String result = command(SEARCH_COMMAND, query);
					if (result == null || result.indexOf("OK") == -1)
					{
						Log.error(MessageCatalog._100017_FEDERATED_SEARCH_FAILURE, result);
					} else 
					{
						workingQuery = query;
					}
				}
			} catch (final SessionExpiredException exception)
			{
				currentState = notYetInitialized;
				currentState.search(request);
			} catch (final UnsupportedEncodingException exception)
			{
				Log.error(
						MessageCatalog._100024_UNSUPPORTED_ENCODING, 
						exception);
			}			
		}
		
		@Override
		public void search(final String query, final String otherParameters) throws SystemInternalFailureException
		{
			try
			{
				if (workingQuery != null && workingQuery.equals(query) && workingParameters != null && workingParameters.equals(otherParameters))
				{
					Log.debug(
							MessageCatalog._200001_FEDERATED_SEARCH_ALREADY_EXECUTED, 
							workingQuery);
				} else 
				{
					final String result = command(
							SEARCH_COMMAND, 
							"query=" + URLEncoder.encode(query, "UTF-8") + (otherParameters != null ? "&" + otherParameters : ""));
					
					if (result == null || result.indexOf("OK") == -1)
					{
						Log.error(MessageCatalog._100017_FEDERATED_SEARCH_FAILURE, result);
					} else 
					{
						workingQuery = query;
						workingParameters = otherParameters;
					}
				}
			} catch (final SessionExpiredException exception)
			{
				currentState = notYetInitialized;
				currentState.search(query, otherParameters);
			} catch (final UnsupportedEncodingException exception)
			{
				Log.error(
						MessageCatalog._100024_UNSUPPORTED_ENCODING, 
						exception);
			}
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public String record(final String uri, Integer offset) throws SystemInternalFailureException, NoSuchResourceException
		{
			try
			{
				return command(RECORD_COMMAND, "id=" + URLEncoder.encode(uri,"UTF-8")+((offset != null) ? "&offset="+offset : ""));
			} catch (final SessionExpiredException exception)
			{
				currentState = notYetInitialized;
				return currentState.record(uri, offset);
			} catch (final UnsupportedEncodingException exception)
			{
				try {
					return command(RECORD_COMMAND, "id=" + URLEncoder.encode(uri)+((offset != null) ? "&offset="+offset : ""));
				} catch (final SessionExpiredException e) 
				{
					currentState = notYetInitialized;
					return currentState.record(uri, offset);
				}
			}
		}

		@Override
		public void ping() throws SystemInternalFailureException
		{
			try
			{
				command("ping", (String)null);
			} catch (final SessionExpiredException exception)
			{
				currentState = notYetInitialized;
				currentState.ping();
			}
		}

		@Override
		public String stat() throws SystemInternalFailureException
		{
			try
			{
				return command(STAT_COMMAND, (String)null);
			} catch (final SessionExpiredException exception)
			{
				currentState = notYetInitialized;
				return currentState.stat();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void disableAndOrEnableTargets(HttpServletRequest request) throws SystemInternalFailureException 
		{
			final AvailableTargetsTool availableTargets = (AvailableTargetsTool) ServletUtils.findTool(
					"targets", 
					request.getSession().getServletContext());	
			
			if (availableTargets != null)
			{
				try
		        {
		          for (String id : availableTargets.getTargetList())
		          {
		        	  
		        	  // if(id.indexOf("opac.sbn.it")==-1) { 
		        		  PazPar2Proxy.this.command("settings", new StringBuilder().append(URLEncoder.encode(new StringBuilder().append("pz:allow[").append(id).append("]").toString(), "UTF-8")).append("=0").toString());
		        	  // }
		          }

		          Set<String> enabledTargets = (Set<String>)request.getSession().getAttribute("targetsEnabled");
		          if ((enabledTargets != null) && (!enabledTargets.isEmpty()))
		          {
		            for (String id : enabledTargets)
		            {
			             // if(id.indexOf("opac.sbn.it")==-1) {
			            	  PazPar2Proxy.this.command("settings", new StringBuilder().append(URLEncoder.encode(new StringBuilder().append("pz:allow[").append(id).append("]").toString(), "UTF-8")).append("=1").toString());
			             //}	 
		            }
					workingQuery = null;
					workingParameters = null;
		          }
				} catch (SessionExpiredException exception)
				{
					currentState = notYetInitialized;
					currentState.disableAndOrEnableTargets(request);
				} catch (Exception exception)
				{
					Log.error("", exception);
				}
			}
		}
	};
	
	private static final String SEARCH_COMMAND = "search";
	private static final String RECORD_COMMAND = "record";
	private static final String TERMLIST_COMMAND = "termlist";
	private static final String SHOW_COMMAND = "show";
	private static final String STAT_COMMAND = "stat";
	
	private String sessionId;
	private final transient CloseableHttpClient pazpar2;
	private final String url;
	
	private final String initCommand;
	
	private PazPar2 currentState = notYetInitialized;
	
	/**
	 * Builds a new proxy with the given parameters.
	 * 
	 * @param pazpar2 the HTTP client towards PazPar2.
	 * @param url the PazPar2 HTTP url.
	 */
	public PazPar2Proxy(final CloseableHttpClient pazpar2, final String url)
	{
		this.pazpar2 = pazpar2;
		this.url = url;
		this.initCommand = url + "?command=init";
	}
	
	@Override
	public String record(final String uri, final Integer offset) throws SystemInternalFailureException, NoSuchResourceException
	{
		return currentState.record(uri, offset);
	}

	@Override
	public synchronized void search(final String query, final String otherParameters) throws SystemInternalFailureException
	{
		currentState.search(query, otherParameters);
	}

	@Override
	public String show(final HttpServletRequest request) throws SystemInternalFailureException
	{
		return currentState.show(request);
	}

	@Override
	public synchronized String termlist(final String names) throws SystemInternalFailureException
	{
		return currentState.termlist(names);
	}
	
	/**
	 * Initializes a federated session.
	 * 
	 * @throws InitializationFailedException in case of initialization failure.
	 */
	private void init() throws InitializationFailedException
	{
		Log.debug(MessageCatalog._000012_FEDERATED_SEARCH_SESSION_INIT_START);
		CloseableHttpResponse response = null;
		
		try
		{
			final HttpGet method = new HttpGet(initCommand);
			response = pazpar2.execute(method);
			final HttpEntity entity = response.getEntity();
			final int statusCode = response.getStatusLine().getStatusCode();
			switch(statusCode)
			{
				case HttpStatus.SC_OK:
					final String pazpar2response = EntityUtils.toString(entity, "UTF-8");
					final int startIndex = pazpar2response.indexOf("<session>");
					if (startIndex != -1)
					{
						sessionId = pazpar2response.substring(startIndex + 9, pazpar2response.indexOf("</session>"));	
						Log.debug(
								MessageCatalog._000013_FEDERATED_SEARCH_SESSION_INIT_END,
								sessionId);
					}
					break;
				default:
					Log.error(MessageCatalog._100016_FEDERATED_SESSION_STARTUP_FAILURE, statusCode);
					throw new InitializationFailedException(statusCode);
			}
		} catch (final IOException exception)	
		{
			Log.error(
					MessageCatalog._100016_FEDERATED_SESSION_STARTUP_FAILURE, 
					exception, 
					HttpStatus.SC_SERVICE_UNAVAILABLE);
			throw new InitializationFailedException(HttpStatus.SC_SERVICE_UNAVAILABLE);
		} finally 
		{
			if (response != null) 
			{
				try 
				{
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}		
	
	/**
	 * Executes a command towards pazpar2.
	 * 
	 * @param commandName the command name.
	 * @param parameters the parameter list.
	 * @throws SessionExpiredException in case the current exception has been expired.
	 * @throws SystemInternalFailureException in case of system failure.
	 * @return the command response.
	 */
	private String command(final String commandName, final String parameters) throws SessionExpiredException, SystemInternalFailureException
	{
		CloseableHttpResponse response = null;
		
		try
		{
			final String request = url + "?command=" + commandName + "&session=" + sessionId + (parameters != null ? "&" + parameters : "");

			Log.debug(request);
			
			final HttpGet method = new HttpGet(request);
			response = pazpar2.execute(method);
			final HttpEntity entity = response.getEntity();
			final int statusCode = response.getStatusLine().getStatusCode();
			switch(statusCode)
			{
				case HttpStatus.SC_OK:
					return EntityUtils.toString(entity, "UTF-8");
				default:
					String pazpar2response = EntityUtils.toString(entity, "UTF-8");
					if (pazpar2response.indexOf(sessionId) != -1)
					{
						Log.debug(
								MessageCatalog._000016_FEDERATED_SEARCH_SESSION_EXPIRED,
								sessionId);
						throw new SessionExpiredException();
					} 
					
					if (pazpar2response.indexOf("<error code=\"7\">") != -1)
					{
						return pazpar2response;
					}
					
					Log.debug(pazpar2response + "" + statusCode);
					throw new SystemInternalFailureException();
			}
		} catch (final IOException exception)	
		{
			Log.error(
					MessageCatalog._100016_FEDERATED_SESSION_STARTUP_FAILURE, 
					exception, 
					HttpStatus.SC_SERVICE_UNAVAILABLE);
			throw new SystemInternalFailureException();
		} finally 
		{
			if (response != null) 
			{
				try 
				{
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}			
	
	/**
	 * Executes a command towards pazpar2.
	 * 
	 * @param commandName the command name.
	 * @param parameters the parameter list.
	 * @throws SessionExpiredException in case the current exception has been expired.
	 * @throws SystemInternalFailureException in case of system failure.
	 * @return the command response.
	 */
	private String command(final String commandName, final HttpServletRequest incomingRequest) throws SessionExpiredException, SystemInternalFailureException
	{
		CloseableHttpResponse response = null;
		
		try
		{
			final StringBuilder requestBuilder = new StringBuilder(url)
				.append("?command=")
				.append(commandName)
				.append("&session=")
				.append(sessionId);
				
			append("num", requestBuilder, incomingRequest);
			append("sort", requestBuilder, incomingRequest);
			append("name", requestBuilder, incomingRequest);
			append("start", requestBuilder, incomingRequest);

			final String request = requestBuilder.toString();
				
			Log.debug(request);
		
			final HttpGet method = new HttpGet(request);
			response = pazpar2.execute(method);
			final HttpEntity entity = response.getEntity();
			final int statusCode = response.getStatusLine().getStatusCode();

			switch(statusCode)
			{
				case HttpStatus.SC_OK:
					return EntityUtils.toString(entity, "UTF-8");
				default:
					final String pazpar2response = EntityUtils.toString(entity, "UTF-8");
					if (pazpar2response.indexOf(sessionId) != -1)
					{
						Log.debug(
								MessageCatalog._000016_FEDERATED_SEARCH_SESSION_EXPIRED,
								sessionId);
						throw new SessionExpiredException();
					} 
					
					if (pazpar2response.indexOf("<error code=\"7\">") != -1)
					{
						return pazpar2response;
					}
					
					Log.debug(pazpar2response + "" + statusCode);
					throw new SystemInternalFailureException();
			}
		} catch (final IOException exception)	
		{
			Log.error(
					MessageCatalog._100016_FEDERATED_SESSION_STARTUP_FAILURE, 
					exception, 
					HttpStatus.SC_SERVICE_UNAVAILABLE);
			throw new SystemInternalFailureException();
		}
	}		

	@Override
	public void ping() throws SystemInternalFailureException
	{
		currentState.ping();		
	}

	@Override
	public synchronized String stat() throws SystemInternalFailureException
	{
		return currentState.stat();
	}

	@Override
	public synchronized void search(final HttpServletRequest request) throws SystemInternalFailureException
	{
		currentState.search(request);
	}
	
	@Override
	public synchronized void disableAndOrEnableTargets(final HttpServletRequest request) throws SystemInternalFailureException 
	{
		currentState.disableAndOrEnableTargets(request);
	}	
	
	/**
	 * Builds a pazpar2 query using the HTTP parameters.
	 * 
	 * @param request the HTTP request.
	 * @return the pazpar2 query.
	 * @throws UnsupportedEncodingException in case UTF-8 is not supported on the runtime platform.
	 */
	private String buildPazPar2Query(final HttpServletRequest request) throws UnsupportedEncodingException
	{
		final String q = request.getParameter(HttpParameter.QUERY);
		final String start = request.getParameter("start");
		
		final StringBuilder queryString = new StringBuilder();

		if (q != null && q.trim().length() != 0)
		{
			queryString.append("query=");
			String indexName = request.getParameter(HttpParameter.QUERY_TYPE);
			
			if (indexName == null) 
			{ 
				indexName = "";
			}
			
			if (indexName.indexOf("author") != -1) 
			{
				indexName = "au=";
			} else if (indexName.indexOf("su") != -1) 
			{ 
				indexName = "su=";
			} else if (indexName.indexOf("title") != -1) 
			{ 
				indexName = "ti=";
			} else if (indexName.equals("ti") || indexName.equals("term") || indexName.equals("su") || indexName.equals("au")) 
			{ 
				indexName += "=";
			} else
			{
				indexName = "";
			}
			
			queryString
				.append(indexName)
				.append(URLEncoder.encode(q.trim() + getLimitToAdditionalQuery(request), "UTF-8"));
		}		
		
		if (start != null && start.trim().length() != 0)
		{
			queryString.append("&start=").append(start);
		}
		
		final String [] filters = request.getParameterValues("f");
		if (filters != null)
		{
			for (String filter : filters)
			{
				if (filter.indexOf("xt:") != -1)
				{
					queryString.append("&filter=" + filter.replaceAll("xt:", "pz:id="));
				}
			}
		}	
				
		append("num", queryString, request);
		append("sort", queryString, request);
		append("name", queryString, request);
		return queryString.toString();
	}
	
	/**
	 * Appends an additional component to a query builder.
	 * 
	 * @param parameterName the name of the parameter.
	 * @param queryStringBuilder the query string builder.
	 * @param request the HTTP request.
	 */
	@SuppressWarnings("deprecation")
	private void append(
			final String parameterName, 
			final StringBuilder queryStringBuilder, 
			final HttpServletRequest request)
	{
		final String [] values = request.getParameterValues(parameterName);
		if (values != null)
		{
			for (String value : values)
			{
				try 
				{
					queryStringBuilder.append("&").append(parameterName).append("=").append(URLEncoder.encode(value, "UTF-8"));
				} catch (Exception exception)
				{
					queryStringBuilder.append("&").append(parameterName).append("=").append(URLEncoder.encode(value));					
				}
			}
		}
	}
	
	/**
	 * Returns constraints that will be applied to a query.
	 * 
	 * @param request the HTTP request.
	 * @return the constraints that will be applied to a query.
	 */
	private String getLimitToAdditionalQuery(final HttpServletRequest request)
	{
		final String [] filters = request.getParameterValues(HttpParameter.FILTER_BY);
		if (filters == null) 
		{
			return IConstants.EMPTY_STRING;
		}
		
		final StringBuilder builder = new StringBuilder();
		for (String filter : filters)
		{
			if (filter.indexOf("xt:") == -1 && (filter.indexOf("su:") != -1 || filter.indexOf("au:") != -1))
			{
				builder.append(" and ").append(filter.replace(":", "="));
			}
		}
		return builder.toString();
	}
}