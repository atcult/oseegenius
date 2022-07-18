package com.atc.osee.web.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.view.ServletUtils;

import com.atc.osee.web.model.AdvancedSearchExperience;
import com.atc.osee.web.model.LimitFacet;
import com.atc.osee.web.model.LimitFacetEntry;
import com.atc.osee.web.servlets.OseeGeniusServlet;
import com.atc.osee.web.tools.BreadcrumbHandlingTool;

/**
 * Servlet that responds to ajax requests for selecting / deseleting limits.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class ToggleLimitServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 6311797245905285202L;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("application/json");
		/*
		 	[
		 		{ 
		 			"name":"library",
		 			"value":"ALA",
		 			"lname": "Biblioteca",
		 			"lvalue": "Ala Bibl Comunale";
		 		},
		 		...
		 	]	
	
		 */
		ResourceBundle messages = ResourceBundle.getBundle("resources", getVisit(request).getPreferredLocale());
		
		ResourceBundle addMessages = null; 
		
		try  
		{
			addMessages = ResourceBundle.getBundle("additional_resources", getVisit(request).getPreferredLocale());
		} catch (Exception ignore) {}
		
		BreadcrumbHandlingTool tool = (BreadcrumbHandlingTool) ServletUtils.findTool("breadcrumbTool", getServletContext());
		
				
		StringBuilder builder = new StringBuilder("{\"limits\": [");
		AdvancedSearchExperience experience = getVisit(request).getAdvancedSearchExperience();
		
		int count = 0;
		
				
		for (Entry<String, LimitFacet> entry : experience.getLimits().entrySet())
		{
			String limitName = entry.getKey();
			for (LimitFacetEntry subentry : entry.getValue().getValues())
			{
				if (subentry.isChecked())
				{
					
					
					if (count > 0) builder.append(",");
					builder
						.append("{")
						.append("\"name\":\"").append(limitName).append("\",")
						.append("\"value\":\"").append(subentry.getName()).append("\",")
						.append("\"lname\":\"").append(tool.getI18nLabel(messages, addMessages,limitName)).append("\",")
						.append("\"lvalue\":\"").append(tool.getI18nLabel(tool.getResourceBundle(request, limitName), subentry.getName())).append("\"}");
					count++;
				}
			}
		}
		
		builder.append("]}");
		response.getWriter().println(builder.toString());
	}	
	
	@Override
	protected void doPost(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/plain");
		String limitName = request.getParameter("l");
		String limitEntryName = request.getParameter("n");		
		int count = Integer.parseInt(request.getParameter("co"));	
		boolean checked = Boolean.valueOf(request.getParameter("c"));
		boolean comingFromAdvancedSearch = Boolean.valueOf(request.getParameter("s"));
	
		
			LimitFacet limit = getVisit(request).getAdvancedSearchExperience().getLimit(limitName);
			if (limit != null)
			{
				limit.addOrUpdate(limitEntryName, count, checked, !comingFromAdvancedSearch);
			}
			
	}
}