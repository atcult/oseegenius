package com.atc.osee.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.tools.LicenseTool;

/**
 * Pin / Unpin filters capability.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class PinUnpinFilterServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 6126126521446604587L;

	@Override
	protected void doPost (
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		LicenseTool license = getLicense(request);
		if (license.isPinFiltersEnabled())
		{
			String filter = request.getParameter(HttpParameter.FILTER);
			String action = request.getParameter(HttpParameter.ACTION);
			if (isNotNullOrEmptyString(filter))
			{
				SearchTab tab = getSearchExperience(request).getCurrentTab();
				if ("pin".equals(action))
				{
					tab.pinFilter(filter);
				} else 
				{
					tab.unpinFilter(filter);
				}
			}
		}
	}
}