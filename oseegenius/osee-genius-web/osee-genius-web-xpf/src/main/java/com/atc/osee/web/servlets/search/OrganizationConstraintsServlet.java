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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

import com.atc.osee.web.HttpAttribute;
import com.atc.osee.web.IConstants;
import com.atc.osee.web.model.AdvancedSearchExperience;
import com.atc.osee.web.model.Branch;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.model.LimitFacet;
import com.atc.osee.web.servlets.OseeGeniusServlet;

/**
 * Servlet that handles (view / select) the branch or main library constraints.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class OrganizationConstraintsServlet extends OseeGeniusServlet 
{
	private static final long serialVersionUID = -977345971109555502L;
	 Map<String, Set<Library>> mainLibrariesByCity;
	 
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
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
					allBranches.put(branch.getId(), branch);
				}
				
				String city = mainLibrary.getCity();
				if (isNotNullOrEmptyString(city))
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
		setRequestAttribute(request, "adv", true);
		
		setRequestAttribute(request, HttpAttribute.DONT_SHOW_PERSPECTIVE_BUTTON, true);
		forwardTo(request, response, "/organization_constraints.vm", "libraries_layout.vm");
	}
	
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException 
	{
		AdvancedSearchExperience model = getAdvancedSearchExperience(request);
		String [] libraries = request.getParameterValues("library");
		String [] branches = request.getParameterValues("branch");

		LimitFacet librariesLimit = model.getLibraryConstraints();
		librariesLimit.getValues().clear();

		LimitFacet branchesLimit = model.getBranchConstraints();
		branchesLimit.getValues().clear();

		if (libraries != null)
		{
			FacetField ff = new FacetField(librariesLimit.getName());
			for (String library : libraries)
			{
				librariesLimit.addSelectedEntry(new Count(ff, library, 0L));
			}
		}
		
		if (branches != null)
		{
			FacetField ff = new FacetField(branchesLimit.getName());
			for (String branch : branches)
			{
				branchesLimit.addSelectedEntry(new Count(ff, branch, 0L));
			}
		}
		
		response.sendRedirect("advanced");
	}
	
	/**
	 * Returns the (user) advanced search model.
	 * 
	 * @param request the http request.
	 * @return the (user) advanced search model.
	 */
	private AdvancedSearchExperience getAdvancedSearchExperience(final HttpServletRequest request)
	{
		return getVisit(request).getAdvancedSearchExperience();
	}	
}