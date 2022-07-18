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
package com.atc.osee.web.plugin.nullobjects;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Branch;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.plugin.LibraryPlugin;

/**
 * NullObject implementation for library plugin.
 * 
 * @author agazzarini
 * @author ggazzarini
 * @since 1.0
 */
public class NullObjectLibraryPlugin implements LibraryPlugin
{
	private static final List<Library> EMPTY_LIBRARY_LIST = new ArrayList<Library>(0);
	
	@Override
	public List<Library> getMainLibraries()
	{
		return EMPTY_LIBRARY_LIST;
	}

	@Override
	public void init(final DataSource datasource)
	{
	}

	@Override
	public Branch getBranchDetails(final int branchId)
	{
		return null;
	}

	@Override
	public void init(ValueParser configuration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getBranchSelectedLocations(int branchId,
			List<Integer> locList) throws SystemInternalFailureException {
		// TODO Auto-generated method stub
		return null;
	}
}
