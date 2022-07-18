package com.atc.osee.web.rest;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.atc.osee.web.util.Utils;

public class RestSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private CloseableHttpClient searcher;
	
	private String [] solr;

	private Random randomizer;
	
	@Override
	public void init() throws ServletException 
	{
		randomizer = new Random();
		
		searcher = HttpClients.createDefault();
		final String searchers = Utils.getSearchersString(getServletConfig().getServletContext());
		if (searchers != null) {
			solr = searchers.split(",");
			for (int index = 0; index < solr.length; index++)
			{
				solr [index] = solr [index].replaceFirst("main","main/select?");
			}				
		}
	}	
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		String wt = request.getParameter("wt");
		if ("json".equals(wt)) {
			response.setContentType("application/json");			
		} else
		{
			response.setContentType("text/xml");						
		}
		response.setCharacterEncoding("UTF-8");
		if (solr != null)
		{
			StringBuilder builder = new StringBuilder(solr[randomizer.nextInt(solr.length)]).append(request.getQueryString());
			
			
			HttpGet method = null;
			CloseableHttpResponse httpresponse = null;
			
			try 
			{
				method = new HttpGet(builder.toString());
				httpresponse = searcher.execute(method);
				final HttpEntity entity = httpresponse.getEntity();
				int statusCode = httpresponse.getStatusLine().getStatusCode();
				switch(statusCode)
				{
					case org.apache.http.HttpStatus.SC_OK:
						response.getWriter().println(EntityUtils.toString(entity, "UTF-8"));
						break;
					default:
						EntityUtils.consume(entity);
						response.sendError(statusCode);
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
			searcher.close();
		} catch (Exception ignore)
		{
			// Do nothing
		}
	}	
}
