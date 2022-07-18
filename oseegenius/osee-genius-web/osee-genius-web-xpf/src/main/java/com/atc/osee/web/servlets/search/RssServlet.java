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
package com.atc.osee.web.servlets.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.tools.codec.Base64Decoder;

import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.util.Utils;

/**
 * RSS (most commonly expanded as Really Simple Syndication) is a family of web feed formats 
 * used to publish frequently updated works such as blog entries, news headlines, audio, and video
 * in a standardized format.
 * An RSS document (which is called a "feed", "web feed",[3] or "channel") includes full or summarized text, plus 
 * metadata such as publishing dates and authorship. 
 * Web feeds benefit publishers by letting them syndicate content automatically. 
 * They benefit readers who want to subscribe to timely updates from favored websites or to aggregate feeds 
 * from many sites into one place.
 * 
 * @author Giorgio Gazzarini
 * @since 1.0
 */
public class RssServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = -887133806537452716L;
	private Map<Locale, String> stylesheets = new HashMap<Locale, String>();
	{
		stylesheets.put(Locale.ITALIAN, "osee_genius_rss_it.xsl");
		stylesheets.put(Locale.ENGLISH, "osee_genius_rss_en.xsl");
	}
	
	private String []  searchers;
	private Random roundRobin = new Random();
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String queryString = request.getQueryString();
		if (isNullOrEmptyString(queryString))
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
			
		String searchEngineQuery = null;
		
		try {
			searchEngineQuery = new Base64Decoder(queryString).processString();
			if (searchEngineQuery.indexOf("q") == -1)
			{
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		} catch (Exception exception){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
			return;
		}
		
		StringBuilder queryBuilder = new StringBuilder(searchers[roundRobin.nextInt(searchers.length)])
			.append("/select?").append(searchEngineQuery);
		
		String stylesheet = getStylesheet(request.getLocale());
		queryBuilder
			.append("&tr=")
			.append(stylesheet)
			.append("&og=http://oseegenius2.atcult.it/cbt");
		
		HttpURLConnection connection = null;
	
		try 
		{
			URL url = new URL(queryBuilder.toString());
			connection = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
			connection.setDoOutput(true);
			switch(connection.getResponseCode())
			{
				case HttpURLConnection.HTTP_OK:
					response.setContentType("text/xml");
					response.setCharacterEncoding("UTF-8");
					BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
	
					PrintWriter writer = response.getWriter(); 
					
					String line = null;
					while ((line = reader.readLine()) != null) 
					{
						writer.println(line);
					}
					writer.flush();
					break;
				default:
					response.sendError(connection.getResponseCode());
					return;
			} 
		} finally 
		{
			try 
			{ 
				connection.disconnect(); 
			} catch (Exception exception) 
			{
				// Nothing
			}
		}
	}
	
	@Override
	public void init() throws ServletException 
	{
		String urls = Utils.getSearchersString(getServletConfig().getServletContext());
		if (urls != null)
		{
			searchers = urls.split(",");
		}
	}
	
	/**
	 * Returns the stylesheet name associated with the given locale.
	 * 
	 * @param locale the current locale.
	 * @return the stylesheet name associated with the given locale.
	 */
	private String getStylesheet(final Locale locale)
	{
		String stylesheet = stylesheets.get(locale);
		return stylesheet != null ? stylesheet : stylesheets.get(Locale.ITALIAN);
	}
}