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
package com.atc.osee.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.atc.osee.web.HttpParameter;

/**
 * Writer Mode HTTP filter.
 * Checks the incoming HTTP request in order to detect the w(riter) mode parameter.
 * If so, then sets a specific flag that will be used later in web controllers to control output mode.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class WriterModeFilter implements Filter 
{
	@Override
	public void doFilter(
			final ServletRequest request, 
			final ServletResponse response, 
			final FilterChain chain) throws IOException, ServletException 
	{
		HttpServletRequest http = (HttpServletRequest) request;
		http.setAttribute(
				HttpParameter.WRITE_MODE, 
				http.getParameter(HttpParameter.WRITE_MODE));
		chain.doFilter(request, response);
	}
	
	@Override
	public void init(final FilterConfig config) throws ServletException 
	{
		// Nothing to be done here
	}
	
	@Override
	public void destroy() 
	{
		// Nothing to be done here
	}	
}