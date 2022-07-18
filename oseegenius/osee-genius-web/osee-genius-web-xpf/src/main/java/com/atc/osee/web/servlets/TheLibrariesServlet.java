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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.HttpParameter;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Branch;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.LicenseTool;

/**
 * OseeGenius -W- libraries web controller.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class TheLibrariesServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = 1L;
       
	private Map<String, Set<Library>> mainLibrariesByCity;
	
	private boolean isDisabled(int branchId, int [] disabledBranches)
	{
		if (disabledBranches == null) 
		{
			return false;
		}
		
		for (int disabledId : disabledBranches)
		{
			if (branchId == disabledId)
			{
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		if (!isDataserviceAvailable(request))
		{
			forwardTo(request, response, "service-unavailable.vm", "/login_layout.vm");
			return;
		}
		
		LicenseTool license = getLicense(request);
		if (license.isLibraryPluginEnabled())
		{
			ConfigurationTool configuration = getConfiguration(request);
			int [] disabledBranches = configuration.getDisabledBranches();
			
			String branchIdParameter = request.getParameter(HttpParameter.ID);
			
			if (mainLibrariesByCity == null)
			{
				Map<Integer, Object> allBranches = new HashMap<Integer, Object>();
				mainLibrariesByCity = new TreeMap<String, Set<Library>>();
				List<Library> mainLibraries = (List<Library>) request.getSession().getServletContext().getAttribute(IConstants.MAIN_LIBRARIES);
				
				for (Library mainLibrary : mainLibraries)
				{
					allBranches.put(mainLibrary.getId(), mainLibrary);
					for (Branch branch : mainLibrary.getBranches())
					{
						if (!isDisabled(branch.getId(), disabledBranches))
						{
							allBranches.put(branch.getId(), branch);
						}
					}
					
					String city = mainLibrary.getCity();
					if (isNullOrEmptyString(city))
					{
						for (Branch branch : mainLibrary.getBranches())
						{
							if (!isDisabled(branch.getId(), disabledBranches))
							{
								if (isNotNullOrEmptyString(branch.getCity()))
								{
									mainLibrary.setCity(branch.getCity());
									break;
								}
							}
						}						 
					} 
					if (isNotNullOrEmptyString(mainLibrary.getCity()))
					{
						Set<Library> libraries = mainLibrariesByCity.get(mainLibrary.getCity());
						if (libraries == null)
						{
							libraries = new TreeSet<Library>();
							mainLibrariesByCity.put(mainLibrary.getCity(), libraries);	
						}
						libraries.add(mainLibrary);
					}
				}
				request.getSession().getServletContext().setAttribute("branches", allBranches);
			}
			setRequestAttribute(request, "mainLibrariesByCityMap", mainLibrariesByCity);
			
			if (isNotNullOrEmptyString(branchIdParameter))
			{
				try 
				{
					int branchId = Integer.parseInt(branchIdParameter);
					Branch branch = license.getLibraryPlugin().getBranchDetails(branchId);
					setRequestAttribute(request, HttpParameter.BRANCH, branch);
					
				} catch (SystemInternalFailureException exception) 
				{
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);		
				}
			}
			else
			{
				String tab = request.getParameter("tab");
				if(tab == null ||(isNotNullOrEmptyString(tab)&& "map".equals(tab))){
					forwardTo(request, response, "/libraries_map.vm", "libraries_layout.vm");
					return;
				}
			
				setRequestAttribute(request, HttpAttribute.DONT_SHOW_PERSPECTIVE_BUTTON, true);
				
			}
			
			forwardTo(request, response, "/libraries.vm", "libraries_layout.vm");
		}
	}
}