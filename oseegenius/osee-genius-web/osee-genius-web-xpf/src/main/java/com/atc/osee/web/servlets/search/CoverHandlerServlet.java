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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.view.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.plugin.CoverPlugin;
import com.atc.osee.web.plugin.cover.OpenLibraryProviderCoverPlugin;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.CoverPluginManagerTool;

/**
 * Cover handling controller.
 * It has a central role in loading covers for (at the moment) bibliographic documents.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class CoverHandlerServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CoverHandlerServlet.class);
	
	/**
	 * Cover retriever interface.
	 * Defines the contract that needs to be implemented by a component which is able
	 * to retrieve covers.
	 * 
	 * @author agazzarini
	 * @since 1.0
	 */
	interface GetCover
	{
		/**
		 * Returns the cover associated with the given ISBN according with a plugin rules.
		 * 
		 * @param isbn the ISBN of the document.
		 * @param an the an of the document.
		 * @param plugin the cover retrieval strategy.
		 * @return the cover associated with the given ISBN according with a plugin rules.
		 */
		String getCoverUrl(String isbn, String an, CoverPlugin plugin, Locale locale);
	}
	
	private Map<String, GetCover> getCoverStrategies = new HashMap<String, GetCover>(3);
	{
		getCoverStrategies.put("S", new GetCover() 
		{
			@Override
			public String getCoverUrl(final String isbn, final String an, final CoverPlugin plugin, Locale locale) 
			{
				return plugin.getSmallCoverUrl(isbn, an, locale);
			}
		});

		getCoverStrategies.put("M", new GetCover() 
		{
			@Override
			public String getCoverUrl(final String isbn, final String an, final CoverPlugin plugin, Locale locale) 
			{
				return plugin.getMediumCoverUrl(isbn, an, locale);
			}
		});

		getCoverStrategies.put("L", new GetCover() 
		{
			@Override
			public String getCoverUrl(final String isbn, final String an, final CoverPlugin plugin, Locale locale) 
			{
				return plugin.getBigCoverUrl(isbn, an, locale);
			}
		});
		
		getCoverStrategies.put(null, new GetCover() 
		{
			@Override
			public String getCoverUrl(final String isbn, final String an, final CoverPlugin plugin, Locale locale) 
			{
				return null;
			}
		});		
	}
	
	/**
	 * Throw in case a cover cannot be found using a given plugin.
	 * 
	 * @author Andrea Gazzarini
	 * @since 1.0
	 */
	class CoverNotAvailableException extends Exception
	{
		private static final long serialVersionUID = -4722899900197477717L;
	}
	
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		// Embedded cover plugin
		String imageUrl = request.getParameter(HttpParameter.IMAGE_URL);
		
		String isbn = request.getParameter(HttpParameter.ISBN);
		String an = request.getParameter(HttpParameter.AN);
		String type = request.getParameter(HttpParameter.COVER_SIZE);
		
		List<CoverPlugin> plugins = getCoverManager(request).getChainOfResponsibility();	

		OutputStream out = response.getOutputStream();		
		try 
		{
			tryToWriteCover(response, imageUrl, out, null); 
		} catch (CoverNotAvailableException exception)
		{
			// Embedded strategy failed (or not specified).
			// Try with plugin chain.

			for (CoverPlugin plugin : plugins)
			{

				try 
				{
					imageUrl = getCoverStrategies.get(type).getCoverUrl(isbn, an, plugin, getVisit(request).getPreferredLocale());
					tryToWriteCover(response, imageUrl, out, plugin); 
					break;
				} catch (CoverNotAvailableException coverNotAvailableException)
				{
					// Nothing to be done here...
				} catch (Exception coverNotAvailableException)
				{
					// Nothing to be done here...					
				}
			}
		}
	}

	/**
	 * Tries to load the cover using the given URL.
	 * If the image is correctly loaded, the it is written to the response output stream,
	 * otherwise an exception is raised and the next plugin of the chain will be invoked.
	 * 
	 * @param response the http response.
	 * @param imageUrl the image URL.
	 * @param out the output stream.
	 * @throws CoverNotAvailableException in case the cover cannot be loaded.
	 */
	private void tryToWriteCover(
			final HttpServletResponse response, 
			final String imageUrl, 
			final OutputStream out,
			CoverPlugin plugin) throws CoverNotAvailableException
	{
		// Sanity check: if the image URL is not valid the return 
		// immediately.
		if (isNullOrEmptyString(imageUrl))
		{
			throw new CoverNotAvailableException();	
		}
		
		HttpURLConnection connection = null;
		
		try 
		{

			URL url = new URL(imageUrl);
			connection = (HttpURLConnection)url.openConnection();
//			connection.setDoOutput(true);
	
			switch(connection.getResponseCode())
			{
				case HttpURLConnection.HTTP_OK:
					// Sanity check: some implementor don't indicate a 
					// missing cover with a 404 but instead return a 1X1 px image.
					if (connection.getContentLength() < 900 && connection.getContentLength() != -1)
					{
						throw new CoverNotAvailableException();
					}
					///// to fix problems with cover for AN 1392614
					//if casalini
					Toolbox toolbox = (org.apache.velocity.tools.Toolbox) getServletContext().getAttribute("org.apache.velocity.tools.Toolbox");
					String instituition = (String) toolbox.get("institution");
					if("CASALINI".equals(instituition)){
						response.getWriter().println(imageUrl);
						return;
					}
					if("Pontificia Università Gregoriana".equals(instituition)){

						 response.setContentType("text/plain");  
						// response.setContentLength(imageUrl.length());
						 out.write(imageUrl.getBytes());
					     out.flush();
						return;
					}
					
					/*
					if (plugin != null && plugin.getClass() == OpenLibraryProviderCoverPlugin.class) {
						
						PrintWriter p = new PrintWriter(out);
						p.println(imageUrl);				
						p.flush();
						return;
					}
					*/

					response.setContentType(connection.getContentType());
					response.setContentLength(connection.getContentLength());
					InputStream in = connection.getInputStream();
		
					byte [] buffer = new byte[1024];
			        int trunkLength;
			        while ((trunkLength = in.read(buffer)) != -1) 
			        {
			            out.write(buffer, 0, trunkLength);
			        }
			        out.flush();

			        break;
				default:
					throw new CoverNotAvailableException();
			} 
		} catch (CoverNotAvailableException exception)
		{
			throw exception;
		} catch (Exception exception)
		{
			LOGGER.debug("Unable to load the image (" + imageUrl + ").", exception);		
			throw new CoverNotAvailableException();
		} finally 
		{
			try 
			{ 
					connection.disconnect();
			} catch (Exception exception)
			{ 
				// Nothing to do
			}					
		}		
	}
	
	/**
	 * Returns the cover plugin manager tool associated with this Osee Genius -W- instance.
	 * 
	 * @param request the http request.
	 * @return the cover plugin manager associated with this Osee Genius -W- instance.
	 */
	protected CoverPluginManagerTool getCoverManager(final HttpServletRequest request)
	{
		return (CoverPluginManagerTool) ServletUtils.findTool(
				IConstants.COVER_KEY, 
				request.getSession().getServletContext());	
	}
}