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
package com.atc.osee.plugin.AMICUS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.plugin.AMICUS.integration.LibraryDAO;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.logic.integration.NoSuchRecordException;
import com.atc.osee.web.model.Branch;
import com.atc.osee.web.model.DailyOpeningHours;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.plugin.LibraryPlugin;

public class LibraryPluginImpl implements LibraryPlugin
{
	private final static Logger LOGGER = LoggerFactory
			.getLogger(LibraryPluginImpl.class.getName());
	private LibraryDAO libraryDAO;
	private DataSource datasource;

	static String SELECT_WEEKLY_OPENING_HOURS = "SELECT ORG_HRS_OPRTN_EXT.*, OVR_NGHT_LOAN_DUE_TME "
			+ "FROM ORG_HRS_OPRTN_EXT,ORG_HRS_OPRTN "
			+ "WHERE ORG_HRS_OPRTN_EXT.ORG_NBR=? AND "
			+ "ORG_HRS_OPRTN_EXT.ORG_NBR=ORG_HRS_OPRTN.ORG_NBR(+) AND "
			+ "ORG_HRS_OPRTN_EXT.ORG_HRS_OPRTN_WKDAY_CDE=ORG_HRS_OPRTN.ORG_HRS_OPRTN_WKDAY_CDE(+) "
			+ "ORDER BY ORG_HRS_OPRTN_EXT.ORG_HRS_OPRTN_WKDAY_CDE";

	
	static String SELECT_LIBRARY_COMMUCATION_DATA = "SELECT PSTL_ADR_ST_NME,PSTL_ADR_ST_NBR, PSTL_ADR_PSTL_CDE,PSTL_ADR_CTY_NME,ADR_PROV_TRTRY_STATE_CDE, ORG_CMCTN_NBR "
		+ "FROM ORG_CMCTN,ORG_PSTL_ADR "
		+ "WHERE ORG_PSTL_ADR.ORG_NBR=? AND ORG_PSTL_ADR.ORG_NBR=ORG_CMCTN.ORG_NBR AND ORG_PSTL_ADR.ORG_PSTL_ADR_TYP_CDE=7 AND ORG_CMCTN.ORG_CMCTN_MDM_TYP_CDE=7";

	static String SELECT_LIBRARY_NAME = "SELECT ORG_ENG_NME, LIB_SMBL_CDE  FROM ORG_NME, LIB WHERE ORG_NME.ORG_NBR=? AND ORG_NME.ORG_NBR = LIB.ORG_NBR";
	
	@Override
	public void init(DataSource datasource)
	{
		this.libraryDAO = new LibraryDAO(datasource);
		this.datasource = datasource;
	}

	@Override
	public void init(final ValueParser configuration) 
	{
		// Nothing to be done here...
	}
	
	@Override
	public List<Library> getMainLibraries()
			throws SystemInternalFailureException
	{
		try
		{
			return libraryDAO.getMainLibraries();
		} catch (Exception exception)
		{
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException();
		}
	}

	@Override
	public Branch getBranchDetails(int branchId) throws SystemInternalFailureException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		List<DailyOpeningHours> openingHours = new ArrayList<DailyOpeningHours>();

		try
		{
			connection = datasource.getConnection();
			Branch branch = getBranch(branchId, connection);
			injectCommunicationDetails(branch, connection);
			
			queryStatement = connection.prepareStatement(SELECT_WEEKLY_OPENING_HOURS);
			queryStatement.setInt(1, branchId);
			rs = queryStatement.executeQuery();
			while (rs.next())
			{
				openingHours.add(new DailyOpeningHours(rs
						.getInt("ORG_HRS_OPRTN_WKDAY_CDE"), rs
						.getBoolean("ORG_MORNING_CLOSED"), rs
						.getInt("ORG_HRS_MORNING_OPNG_HRS"), rs
						.getInt("ORG_HRS_MORNING_OPNG_MNTS"), rs
						.getInt("ORG_HRS_MORNING_CLSG_HRS"), rs
						.getInt("ORG_HRS_MORNING_CLSG_MNTS"), rs
						.getBoolean("ORG_AFTERNOON_CLOSED"), rs
						.getInt("ORG_HRS_AFTERNOON_OPNG_HRS"), rs
						.getInt("ORG_HRS_AFTERNOON_OPNG_MNTS"), rs
						.getInt("ORG_HRS_AFTERNOON_CLSG_HRS"), rs
						.getInt("ORG_HRS_AFTERNOON_CLSG_MNTS"), rs
						.getBoolean("ORG_EVENING_CLOSED"), rs
						.getInt("ORG_HRS_EVENING_OPNG_HRS"), rs
						.getInt("ORG_HRS_EVENING_OPNG_MNTS"), rs
						.getInt("ORG_HRS_EVENING_CLSG_HRS"), rs
						.getInt("ORG_HRS_EVENING_CLSG_MNTS")));
			}
			branch.setOpeningHours(openingHours);
			return branch;
		} catch (Exception exception)
		{
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException();
		} finally
		{
			try
			{
				rs.close();
			} catch (Exception ignore)
			{
			}
			try
			{
				queryStatement.close();
			} catch (Exception ignore)
			{
			}
			try
			{
				connection.close();
			} catch (Exception ignore)
			{
			}
		}
	}

	public Branch getBranch(int id, Connection connection) throws SQLException, NoSuchRecordException
	{
		PreparedStatement queryStatement = null;
		ResultSet resultSet = null;
		
		try
		{
			connection = datasource.getConnection();
			queryStatement = connection.prepareStatement(SELECT_LIBRARY_NAME);
			queryStatement.setInt(1, id);

			resultSet = queryStatement.executeQuery();

			if (resultSet.next())
			{
				return new Branch(id, -1, resultSet.getString(1), resultSet.getString(2));
			}
			throw new NoSuchRecordException();
		} catch (SQLException exception)
		{
			throw exception;
		} catch (Exception exception)
		{
			throw new SQLException(exception);
		} finally
		{
			try { resultSet.close();} catch (Exception ignore){}
			try { queryStatement.close(); } catch (Exception ignore) {}
		}
	}
	
	public void injectCommunicationDetails(Branch library, Connection connection) throws SQLException, NoSuchRecordException
	{
		PreparedStatement queryStatement = null;
		ResultSet resultSet = null;

		try
		{
			connection = datasource.getConnection();
			queryStatement = connection.prepareStatement(SELECT_LIBRARY_COMMUCATION_DATA);
			queryStatement.setInt(1, library.getId());

			resultSet = queryStatement.executeQuery();

			if (resultSet.next())
			{
				String street = resultSet.getString("PSTL_ADR_ST_NME");
				String number = resultSet.getString("PSTL_ADR_ST_NBR");
				library.setStreet(street+ ((number != null ? " " + number : "")));

				library.setZipCode(resultSet.getString("PSTL_ADR_PSTL_CDE"));
				library.setCity(resultSet.getString("PSTL_ADR_CTY_NME"));

				library.setProvince(resultSet.getString("ADR_PROV_TRTRY_STATE_CDE"));
				library.setTelephone(resultSet.getString("ORG_CMCTN_NBR"));
			}
		} catch (SQLException exception)
		{
			throw exception;
		} catch (Exception exception)
		{
			throw new SQLException(exception);
		} finally
		{
			try { resultSet.close();} catch (Exception ignore){}
			try { queryStatement.close(); } catch (Exception ignore) {}
		}
	}

	@Override
	public List<String> getBranchSelectedLocations(int branchId,
			List<Integer> locList) throws SystemInternalFailureException {
		//Metodo non implementato in quanto creato per CBT che usa l'altro plugin 
		return null;
	}
}