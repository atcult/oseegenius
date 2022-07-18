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
package com.atc.osee.plugin.AMICUS.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import com.atc.osee.web.logic.integration.AbstractDAO;
import com.atc.osee.web.model.Branch;
import com.atc.osee.web.model.Library;

/**
 * Library (ML or Branch) Data Access Object
 * 
 * @author ggazzarini
 * @since 1.0
 */
public class LibraryDAO extends AbstractDAO
{
	private final static String SELECT_ALL_LIBRARIES = 
		"SELECT ORG.ORG_NBR, ORG_HRCHY.PARNT_ORG_NBR,ORG_NME.ORG_FR_NME, PSTL_ADR_CTY_NME, LIB_SMBL_CDE "+
		"FROM ORG,ORG_NME,ORG_HRCHY,LIB, ORG_PSTL_ADR "+
		"WHERE ORG.ORG_NBR=ORG_NME.ORG_NBR "+
		"AND ORG.ORG_NBR=ORG_PSTL_ADR.ORG_NBR (+)"+
		"AND ORG.ORG_NBR=LIB.ORG_NBR "+
		"AND ORG.ORG_NBR=ORG_HRCHY.ORG_NBR";
	
	/**
	 * Builds a new dao with the given datasource.
	 * 
	 * @param datasource the datasource.
	 */
	public LibraryDAO(DataSource datasource)
	{
		super(datasource);
	}
	
	/**
	 * Returns the main libraries.
	 * 
	 * @return the main libraries.
	 * @throws SQLException in case of database problems.
	 */
	public List<Library> getMainLibraries() throws SQLException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet resultSet = null;
		List<Library> libraries = new ArrayList<Library>();
		List<Branch> branches = new ArrayList<Branch>();
		
		try 
		{
			connection = datasource.getConnection();
			queryStatement = connection.prepareStatement(SELECT_ALL_LIBRARIES);
			resultSet = queryStatement.executeQuery();
			
			while (resultSet.next())
			{
				int id = resultSet.getInt("ORG_NBR");
				int parentId = resultSet.getInt("PARNT_ORG_NBR");
				if (id == 0 || parentId == 0) continue;
				if (id == parentId)
				{
					Library library = new Library();
					library.setId(id);
					library.setSymbol(resultSet.getString("LIB_SMBL_CDE"));
					library.setCity(resultSet.getString("PSTL_ADR_CTY_NME"));
					library.setName(resultSet.getString("ORG_FR_NME"));
					libraries.add(library);					
				} else 
				{
					Branch branch = new Branch(id, parentId,resultSet.getString("ORG_FR_NME"), resultSet.getString("LIB_SMBL_CDE"));
					branches.add(branch);
				}
			}
		} catch (SQLException exception)
		{
			throw exception;
		} catch (Exception exception)
		{
			throw new SQLException(exception);
		} finally 
		{
			try {resultSet.close();} catch(Exception ignore) {}
			try {queryStatement.close();} catch(Exception ignore) {}
			try {connection.close();} catch(Exception ignore) {}
		}		
		
		for (Library library : libraries)
		{
			int id = library.getId();
			for (Iterator<Branch> iterator = branches.iterator(); iterator.hasNext();)
			{
				Branch branch = iterator.next();
				if (branch.getParentId() == id)
				{
					library.addBranch(branch);
					iterator.remove();
				}
			}
		}
		return libraries;
	}	
}