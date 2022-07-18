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
package com.atc.osee.web.plugin;

import java.util.List;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Branch;
import com.atc.osee.web.model.Library;

/**
 * OseeGenius library plugin.
 * 
 * @author Giorgio Gazzarini
 * @since 1.0
 */
public interface LibraryPlugin extends DatabasePlugin
{
	/**
	 * Returns the list of main libraries.
	 * 
	 * @return the list of main libraries.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	List<Library> getMainLibraries() throws SystemInternalFailureException;

	/**
	 * Returns the details of the branch associated with the given identifier.
	 * 
	 * @param branchId the branch identifier.
	 * @return the branch details.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	Branch getBranchDetails(int branchId) throws SystemInternalFailureException;
	
	/**
	 * Returns the selected list of location of the branch associated with the given identifier.
	 * 
	 * @param branchId the branch identifier.
	 * @return the locations list.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	public List<String> getBranchSelectedLocations(int branchId, List<Integer> locList) throws SystemInternalFailureException;
}