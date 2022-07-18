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
package com.atc.osee.web.servlet.accessibility;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


public class ErrorLayoutFilter implements Filter 
{
	@Override
	public void doFilter(
			final ServletRequest request, 
			final ServletResponse response, 
			final FilterChain chain) throws IOException, ServletException 
	{
		HttpServletRequest http = (HttpServletRequest) request;
		http.setAttribute(
				"accessibility",
				"true");
		chain.doFilter(http, response);
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
