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

import java.net.MalformedURLException;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.view.ServletUtils;

import com.atc.osee.logic.search.ISearchEngine;
import com.atc.osee.logic.search.NullObjectSearchEngine;
import com.atc.osee.logic.search.SearchEngine;
import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.log.MessageCatalog;
import com.atc.osee.web.servlets.search.federated.KeepAliveDaemon;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.LicenseTool;
import com.atc.osee.web.util.Utils;

/**
 * Supertype layer of application lifecycle listener.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public abstract class OseeGeniusApplicationLifeCycleListener implements ServletContextListener 
{
	private KeepAliveDaemon keepAliveDaemon;
	
	@Override
	public void contextInitialized(final ServletContextEvent event) 
	{
		Log.info(MessageCatalog._000000_STARTING);

		ServletContext application = event.getServletContext();
		
		configureFederatedSearch(application);
		
		try 
		{
			onContextActivation(application, configureAndGetSearchEngine(application));
		} catch (Exception exception)
		{
			Log.error(MessageCatalog._100023_START_UP_FAILURE, exception);
		}
		
		/****Authority****/
		if(System.getProperty("authorityEnabled")!=null){
			ISearchEngine searchAuthEngine =(ISearchEngine) application.getAttribute(IConstants.SEARCH_ENGINE_AUTH_ATTRIBUTE_NAME);
			if(searchAuthEngine==null){	
				try 
				{
					SolrServer authSearcher = createAndGetSOLRServerReference(application, "auth");	
					if (authSearcher != null)
					{
						searchAuthEngine = new SearchEngine(authSearcher, null);
						application.setAttribute(IConstants.SEARCH_ENGINE_AUTH_ATTRIBUTE_NAME, searchAuthEngine);
					}
				} catch (Exception exception)
				{
					Log.error("OSEE-GENIUS-W: Unable to start Authority Search Engine", exception);
					searchAuthEngine = new NullObjectSearchEngine();
					application.setAttribute(IConstants.SEARCH_ENGINE_AUTH_ATTRIBUTE_NAME, searchAuthEngine);
				}
			}	
		}		
		/****Authority****/
		
		/****Heading****/
		if(System.getProperty("headingEnabled")!=null){
			ISearchEngine searchHeadingEngine =(ISearchEngine) application.getAttribute(IConstants.SEARCH_ENGINE_HEADING_ATTRIBUTE_NAME);
			if(searchHeadingEngine == null){	
				try 
				{
					SolrServer headingSearcher = createAndGetSOLRServerReference(application, "heading");	
					if (headingSearcher != null)
					{
						searchHeadingEngine = new SearchEngine(headingSearcher, null);
						application.setAttribute(IConstants.SEARCH_ENGINE_HEADING_ATTRIBUTE_NAME, searchHeadingEngine);
					}
				} catch (Exception exception)
				{
					Log.error("OSEE-GENIUS-W: Unable to start Authority Search Engine", exception);
					searchHeadingEngine = new NullObjectSearchEngine();
					application.setAttribute(IConstants.SEARCH_ENGINE_HEADING_ATTRIBUTE_NAME, searchHeadingEngine);
				}
			}	
		}		
		/****Heading****/
		
		Log.info(MessageCatalog._000001_RUNNING);
	}

	/**
	 * Configures and creates a search engine (OseeGenius -S-) reference.
	 * 
	 * @param application the OseeGenius -W- application context.
	 * @return a search engine (OseeGenius -S-) reference.
	 */
	private ISearchEngine configureAndGetSearchEngine(final ServletContext application)
	{
		ISearchEngine searchEngine = (ISearchEngine) application.getAttribute(IConstants.SEARCH_ENGINE_ATTRIBUTE_NAME);
		if (searchEngine == null)
		{
			try 
			{
				SolrServer searcher = createAndGetSOLRServerReference(application, null);
				
				if (searcher != null)
				{
					searchEngine = new SearchEngine(searcher, null);
					application.setAttribute(IConstants.SEARCH_ENGINE_ATTRIBUTE_NAME, searchEngine);
				}
			} catch (Exception exception)
			{
				Log.error(MessageCatalog._100022_UNABLE_TO_START_SEARCH_SERVICE, exception);
				searchEngine = new NullObjectSearchEngine();
				application.setAttribute(IConstants.SEARCH_ENGINE_ATTRIBUTE_NAME, searchEngine);
			}
		}
		return searchEngine;
	}

	/**
	 * Configures federated search on this OseeGenius -W- instance.
	 * 
	 * @param application the OseeGenius -W- application context.
	 */
	private void configureFederatedSearch(final ServletContext application)
	{
		String federatedSearchEndpointUrl = Utils.getFederatedSearchEndpointString(application);
		if (federatedSearchEndpointUrl != null && federatedSearchEndpointUrl.trim().length() != 0)
		{
			if (keepAliveDaemon == null)
			{
				CloseableHttpClient client = HttpClients.createDefault();
				
				application.setAttribute(HttpAttribute.FEDERATED_SEARCH_HTTP_CLIENT, client);
				application.setAttribute(HttpAttribute.FEDERATED_SEARCH_ENDPOINT_URL, federatedSearchEndpointUrl);
				
				application.setAttribute(HttpAttribute.ACTIVE_SESSIONS, new HashMap<String, HttpSession>());
				keepAliveDaemon = new KeepAliveDaemon(application);
				keepAliveDaemon.start();
			}			
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) 
	{
		Log.info(MessageCatalog._000002_SHUTTING_DOWN);
		
		if (keepAliveDaemon != null)
		{
			keepAliveDaemon.shutdown();
			keepAliveDaemon.interrupt();
		}
		
		try 
		{
			CloseableHttpClient federatedSearchClient = (CloseableHttpClient) event.getServletContext().getAttribute(HttpAttribute.FEDERATED_SEARCH_HTTP_CLIENT);
			if (federatedSearchClient != null)
			{
				event.getServletContext().removeAttribute(HttpAttribute.FEDERATED_SEARCH_HTTP_CLIENT);
				federatedSearchClient.close();
			}
		} catch (Exception exception) 
		{
			// Nothing to be done here...
		}
		
		try 
		{
			SearchEngine searchEngine = (SearchEngine) event.getServletContext().getAttribute(IConstants.SEARCH_ENGINE_ATTRIBUTE_NAME);
			searchEngine.shutdown();
		} catch (Exception exception) 
		{
			// Nothing to do here...
		}
		
		onContextDeactivation(event.getServletContext());
		
		Log.info(MessageCatalog._000003_SHUT_DOWN);		
	}

	/**
	 * Application listener template method: activation.
	 * A concrete implementor could implements here its own specific behaviour.
	 *  
	 * @param application the servlet context.
	 * @param searchEngine the OseeGenius -S- reference.
	 */
	protected abstract void onContextActivation(ServletContext application, ISearchEngine searchEngine);

	/**
	 * Application listener template method: deactivation
	 * A concrete implementor could implements here its own specific behaviour.
	 *  
	 * @param application the servlet context.
	 */
	protected abstract void onContextDeactivation(ServletContext application);
	
	/**
	 * Returns a reference to a valid search engine.
	 * Depending on the configuration, that can be a single or load a -balanced reference.
	 * 
	 * @param context the web application context.
	 * @param urlParameter the configured parameter.
	 * @return a reference to a valid search engine.
	 * @throws MalformedURLException in case the URL parameter is misconfigured.
	 */
	protected SolrServer createAndGetSOLRServerReference(final ServletContext context, String solrCoreName) throws MalformedURLException
	{
		String urls = Utils.getSearchersString(context);		

		int connectionTimeout = 0;
		int soConnectionTimeout = 0;
		try 
		{ 
			connectionTimeout = Integer.parseInt(System.getProperty(IConstants.SOLR_CONNECTION_TIMEOUT));
			soConnectionTimeout = Integer.parseInt(System.getProperty(IConstants.SOLR_SO_CONNECTION_TIMEOUT));
		} catch (final Exception exception)
		{
			// Ignore and use infinite timeout
		}

		if (urls != null)
		{
			Log.debug(MessageCatalog._000004_IR_URLS, urls);
			
			if(solrCoreName != null){
				urls = urls.replaceAll("main", solrCoreName);
			}
			
			String [] addresses = urls.split(",");
			if (addresses.length > 1)
			{
				final LBHttpSolrServer server = new LBHttpSolrServer(addresses);
				server.setConnectionTimeout(connectionTimeout);				
				server.setSoTimeout(soConnectionTimeout);	
				return server;
			} else
			{
				final HttpSolrServer server = new HttpSolrServer(addresses[0]);
				server.setConnectionTimeout(connectionTimeout);				
				server.setSoTimeout(soConnectionTimeout);	
				return server;
			}
		}
		return null;
	}	
}