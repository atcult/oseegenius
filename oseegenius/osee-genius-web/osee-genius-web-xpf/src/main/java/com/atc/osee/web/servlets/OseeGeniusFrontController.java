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
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityLayoutServlet;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.model.Visit;

/**
 * Osee Genius Front Controller.
 * A customized extension of Velocity servlet that handles some common tasks
 * (e.g. i18n) in a centralized way.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class OseeGeniusFrontController extends VelocityLayoutServlet
{
	private static final long serialVersionUID = 7932013472418747724L;
		
	@Override
	public void service(
			final ServletRequest request, 
			final ServletResponse response) throws ServletException, IOException 
	{
		response.setCharacterEncoding("UTF-8");
		((HttpServletRequest)request).getSession(true);
		super.service(request, response);
	}
	
	@Override
	protected void fillContext(final Context ctx, final HttpServletRequest request)
	{
		super.fillContext(ctx, request);
		i18n(ctx, request);
	}
		
	/**
	 * Defines the language that will be used for the current request.
	 * 
	 * @param ctx the tool context.
	 * @param request the http request.
	 */
	private void i18n(final Context ctx, final HttpServletRequest request)
	{
		String language = request.getParameter(HttpParameter.LANGUAGE);
		Visit visit = (Visit) request.getSession(true).getAttribute(HttpAttribute.VISIT);
		if (language != null)
		{
			language=language.replaceAll("[.:,;()><&|^#*/=~'\"-+]", "");
			Locale locale = new Locale(language);
			visit.setPreferredLocale(locale);
		} 
	}
}