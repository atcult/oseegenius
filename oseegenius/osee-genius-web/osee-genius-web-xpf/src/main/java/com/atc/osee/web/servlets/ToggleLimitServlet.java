package com.atc.osee.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.model.LimitFacet;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * Servlet that responds to ajax requests for selecting / deseleting limits.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ToggleLimitServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 6311797245905285202L;
	
	@Override
	protected void service(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/plain");
		String limitName = request.getParameter("l");
		String limitEntryName = request.getParameter("n");
		try {
			int count = Integer.parseInt(request.getParameter("co"));	
			boolean checked = Boolean.valueOf(request.getParameter("c"));
			boolean comingFromAdvancedSearch = Boolean.valueOf(request.getParameter("s"));
			
			LimitFacet limit = getVisit(request).getAdvancedSearchExperience().getLimit(limitName);
			if (limit != null)
			{
				limit.addOrUpdate(limitEntryName, count, checked, !comingFromAdvancedSearch);
			}
		}
		catch (NumberFormatException e) {
			//ignore
		}
	}
}