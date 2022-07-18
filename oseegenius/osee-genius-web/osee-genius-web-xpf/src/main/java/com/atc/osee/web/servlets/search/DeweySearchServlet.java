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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;

/**
 * Dewey searcher.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class DeweySearchServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void service(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		String dewey = request.getParameter(HttpParameter.QUERY);
		if (dewey != null && dewey.trim().length() != 0)
		{
			switch(dewey.length())
			{
				case 3:
					if ("000".equals(dewey))
					{
						dewey = "0*";
					} else if (dewey.endsWith("00"))
					{
						dewey = dewey.substring(0, 1) + IConstants.STAR;
					} else if (dewey.endsWith("0"))
					{
						dewey = dewey.substring(0, 2) + IConstants.STAR;						
					} else 
					{
						dewey += IConstants.STAR;		
					}
					break;
				default:
					dewey += IConstants.STAR;
			}
			
			StringBuilder query =  new StringBuilder("q=*:*&h=def");
			String [] filters = request.getParameterValues(HttpParameter.FILTER_BY);
			if (filters != null && filters.length > 0)
			{
				for (String filter : filters)
				{
					query.append("&f=").append(filter);
				}	
			}
			query.append("&f=dewey:").append(dewey);
			response.sendRedirect("search?" + query.toString());
		}
	}
}