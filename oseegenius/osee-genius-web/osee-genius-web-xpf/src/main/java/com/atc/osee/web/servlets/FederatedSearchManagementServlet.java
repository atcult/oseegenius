package com.atc.osee.web.servlets;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.Layout;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.model.SearchTab;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * A servlet that allows a fine-level control on federated search settings.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class FederatedSearchManagementServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = -2619064214597947258L;
	
	@Override
	protected void doGet(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		if(isNotNullOrEmptyString(request.getParameter("detail"))){
			setRequestAttribute(request, "detail", request.getParameter("detail"));
		}
		forwardTo(request, response, "manage_external_targets.vm", Layout.DUMMY_LAYOUT);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(
			final HttpServletRequest request, 
			final HttpServletResponse response) throws ServletException, IOException 
	{
		HttpSession session = request.getSession();
		if (session != null)
		{
			Set<String> targetsEnabled = (Set<String>) request.getSession().getAttribute(HttpAttribute.TARGETS_ENABLED);
			if (targetsEnabled == null)
			{
				targetsEnabled = new HashSet<String>();
				request.getSession().setAttribute(HttpAttribute.TARGETS_ENABLED, targetsEnabled);
			}
			targetsEnabled.clear();
			targetsEnabled.addAll(request.getParameterMap().keySet());

			for (SearchTab tab : getSearchExperience(request).getTabs())
			{
				try 
				{
					tab.getPazpar2().disableAndOrEnableTargets(request);
				} catch (SystemInternalFailureException e) 
				{
					Log.error("Error while managing federated targets.", e);
				}
			}
		}		
	}
}