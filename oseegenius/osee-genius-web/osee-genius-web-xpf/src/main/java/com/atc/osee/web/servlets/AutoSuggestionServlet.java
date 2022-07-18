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
package com.atc.osee.web.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.tartarus.snowball.ext.LovinsStemmer;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.util.Utils;

/**
 * AutoSuggestion web controller.
 * TODO : to be improved 
 *  
 * @author agazzarini
 * @since 1.0
 */
public class AutoSuggestionServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 1L;
	private CloseableHttpClient autocomplete;
	
	private String [] solr;

	private Random randomizer;
	
	@Override
	public void init() throws ServletException 
	{
		randomizer = new Random();
		
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
		
		autocomplete = HttpClients.createDefault();
		final String searchers = Utils.getSearchersString(getServletConfig().getServletContext());
		if (searchers != null)
		{
			solr = searchers.split(",");
			for (int index = 0; index < solr.length; index++)
			{
				solr [index] = solr [index].replaceFirst("main",getAutocompleteCore()+"/select?q=");
			}				
		}
	}
	
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		if (solr != null)
		{
			StringBuilder builder = new StringBuilder(solr[randomizer.nextInt(solr.length)])
				.append(URLEncoder.encode(request.getParameter("query"), "UTF-8"));
			
			String queryType = request.getParameter("qt");
			if (!"any".equals(queryType)) {
				builder.append("&fq=type:"+queryType);
			}
			// col parameter FOR imss only //
			String logicalView = request.getParameter("col");
			if (logicalView == null) {
					logicalView = (String) request.getSession().getAttribute(HttpAttribute.LOGICAL_VIEW);
			}
			if (logicalView != null)
			{
				builder.append("&fq=logical_view:").append(logicalView);
			}
			
			HttpGet method = null;
			CloseableHttpResponse httpresponse = null;
			
			try 
			{
				method = new HttpGet(builder.toString());
				httpresponse = autocomplete.execute(method);
				final HttpEntity entity = httpresponse.getEntity();
				int statusCode = httpresponse.getStatusLine().getStatusCode();
				switch(statusCode)
				{
					case org.apache.http.HttpStatus.SC_OK:
						response.getWriter().println(EntityUtils.toString(entity, "UTF-8"));
						break;
					default:
						response.getWriter().println("{}");
						EntityUtils.consume(entity);
				}				
			} finally 
			{
				if (httpresponse != null) 
				{
					httpresponse.close();
				}
			}
		} else
		{
			response.getWriter().println("{}");			
		}
	}		
	
	@Override
	public void destroy() 
	{
		super.destroy();
		try 
		{
			autocomplete.close();
		} catch (Exception ignore)
		{
			// Do nothing
		}
	}
	
	protected String getAutocompleteCore(){
		return "autocomplete";
	}
}
