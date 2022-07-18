package com.atc.osee.web.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.apache.velocity.tools.Toolbox;

import com.atc.osee.web.IConstants;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.Utility;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.model.Branch;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.plugin.LibraryPlugin;
import com.atc.osee.web.tools.ConfigurationTool;
import com.atc.osee.web.tools.LicenseTool;

/**
 * A OseeGenius -W- lifecycle application listener that loads all main libraries with the corresponding hierarchy.
 * 
 * @author agazzarini
 * @since 1,0
 */
public class MainLibrariesLoaderApplicationLifecycleListener implements ServletContextAttributeListener
{
	@Override
	public void attributeAdded(ServletContextAttributeEvent event) 
	{
		if (Toolbox.KEY.equals(event.getName()))
		{
			Toolbox toolbox = (Toolbox)event.getValue();
			LicenseTool license =  (LicenseTool) toolbox.get(IConstants.LICENSE_KEY);
	
			ConfigurationTool configuration =  (ConfigurationTool) toolbox.get(IConstants.CONFIGURATION_KEY);
			int [] disabledBranches = configuration.getDisabledBranches();
			
			if (license != null && license.isLibraryPluginEnabled())
			{
				try  
				{
					LibraryPlugin plugin = license.getLibraryPlugin();
					List<Library> mainLibraries = plugin.getMainLibraries();
					
					int [] disabledMainLibraries = configuration.getDisabledMainLibraries();
					
					for (Iterator<Library> iterator = mainLibraries.iterator(); iterator.hasNext();) {
						final Library library = (Library) iterator.next();
						if (isDisabled(library.getId(), disabledMainLibraries)) {
							iterator.remove();
						}
					}
					
					Map<String, String> allLibrariesBySymbol = new HashMap<String, String>();
					Map<String, String> mainLibrariesBySymbol = new HashMap<String, String>(mainLibraries.size());
					Map<Integer, String> mainLibrariesById = new HashMap<Integer, String>(mainLibraries.size());
					Map<Integer, Library> mainLibraryByBranchId = new HashMap<Integer, Library>();
					Map<Integer, String> allLibrariesById = new HashMap<Integer, String>();
					Map<Integer, Branch> allBranchesById = new HashMap<Integer, Branch>();
					
					Map<Integer, List<String>> branchesLocation = new HashMap<Integer, List<String>>();
				
					for (Library mainLibrary : mainLibraries)
					{
						mainLibrariesBySymbol.put(mainLibrary.getSymbol(), mainLibrary.getName());
						mainLibrariesById.put(mainLibrary.getId(), mainLibrary.getName());
						allLibrariesById.put(mainLibrary.getId(), mainLibrary.getName());
						
						allLibrariesBySymbol.put(mainLibrary.getSymbol(), mainLibrary.getName());
						
						Iterator itr = mainLibrary.getBranches().iterator();
						
						while(itr.hasNext())
						{
							Branch branch = (Branch)itr.next(); 
							if (!isDisabled(branch.getId(), disabledBranches)){
							
								allLibrariesById.put(branch.getId(), Utility.getTitleCase(branch.getName()));
								allLibrariesBySymbol.put(branch.getSymbol(), Utility.getTitleCase(branch.getName()));		
								mainLibraryByBranchId.put(branch.getId(), mainLibrary);
								allBranchesById.put(branch.getId(), branch);
								
								if(!configuration.getBranchesLocation().isEmpty()){
									if(configuration.getBranchesLocation().containsKey(branch.getId())){
										List<String> locations = plugin.getBranchSelectedLocations(branch.getId(),configuration.getBranchesLocation().get(branch.getId()));
										branchesLocation.put(branch.getId(), locations);
									}
								}
							}else{
								itr.remove();
							}	
						}
					}
					
					ServletContext application = event.getServletContext();
					application.setAttribute(IConstants.LIBRARIES_BY_SIMBOL, mainLibrariesBySymbol);
					application.setAttribute(IConstants.ALL_LIBRARIES_BY_SIMBOL, allLibrariesBySymbol);
					application.setAttribute(IConstants.MAIN_LIBRARIES, mainLibraries);
					application.setAttribute(IConstants.MAIN_LIBRARIES_BY_ID, mainLibrariesById);
					application.setAttribute(IConstants.MAIN_LIBRARIES_BY_BRANCH_ID, mainLibraryByBranchId);
					application.setAttribute(IConstants.ALL_LIBRARIES_BY_ID, allLibrariesById);
					application.setAttribute("abbid", allBranchesById);
					
					if(!branchesLocation.isEmpty()){
						application.setAttribute("branchesLocationList", branchesLocation);
					}
					
				} catch (SystemInternalFailureException exception) 
				{
					Log.error("Unable to load libraries.", exception);
				}
			}
		}
	}
	
	private boolean isDisabled(int toVerifyId, int [] disabledIds)
	{
		if (disabledIds == null) 
		{
			return false;
		}
		
		for (int disabledId : disabledIds)
		{
			if (toVerifyId == disabledId)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent scab) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent scab) {
		// TODO Auto-generated method stub
		
	}
}