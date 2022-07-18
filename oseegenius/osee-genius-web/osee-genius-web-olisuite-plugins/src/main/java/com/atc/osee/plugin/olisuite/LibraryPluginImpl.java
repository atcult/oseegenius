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
package com.atc.osee.plugin.olisuite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.logic.integration.NoSuchRecordException;
import com.atc.osee.web.model.Branch;
import com.atc.osee.web.model.DailyOpeningHours;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.plugin.LibraryPlugin;

public class LibraryPluginImpl implements LibraryPlugin
{
	private final static Logger LOGGER = LoggerFactory.getLogger(LibraryPluginImpl.class.getName());
	private LibraryDAO libraryDAO;
	private DataSource datasource;

	static String SELECT_WEEKLY_OPENING_HOURS = "SELECT ORG_HRS_OPRTN_EXT.*, OVR_NGHT_LOAN_DUE_TME "
			+ "FROM ORG_HRS_OPRTN_EXT,ORG_HRS_OPRTN "
			+ "WHERE ORG_HRS_OPRTN_EXT.ORG_NBR=? AND "
			+ "ORG_HRS_OPRTN_EXT.ORG_NBR=ORG_HRS_OPRTN.ORG_NBR(+) AND "
			+ "ORG_HRS_OPRTN_EXT.ORG_HRS_OPRTN_WKDAY_CDE=ORG_HRS_OPRTN.ORG_HRS_OPRTN_WKDAY_CDE(+) "
			+ "ORDER BY ORG_HRS_OPRTN_EXT.ORG_HRS_OPRTN_WKDAY_CDE";

	
//	static String SELECT_LIBRARY_COMMUCATION_DATA = "SELECT PSTL_ADR_ST_NME,PSTL_ADR_ST_NBR, PSTL_ADR_PSTL_CDE,PSTL_ADR_CTY_NME,ADR_PROV_TRTRY_STATE_CDE, ORG_CMCTN_NBR "
//		+ "FROM ORG_CMCTN,ORG_PSTL_ADR "
//		+ "WHERE ORG_PSTL_ADR.ORG_NBR=? AND ORG_PSTL_ADR.ORG_NBR=ORG_CMCTN.ORG_NBR AND ORG_PSTL_ADR.ORG_PSTL_ADR_TYP_CDE=7 AND ORG_CMCTN.ORG_CMCTN_MDM_TYP_CDE=7";
	
	static String SELECT_LIBRARY_COMMUCATION_DATA = "SELECT PSTL_ADR_ST_NME,PSTL_ADR_ST_NBR, PSTL_ADR_PSTL_CDE,PSTL_ADR_CTY_NME,ADR_PROV_TRTRY_STATE_CDE, ORG_CMCTN_NBR, " 
			+"T_LIB_TYP.TBL_SHRT_FR_TXT,PSTL_ADR_REF_DSC "
			+"FROM ORG_CMCTN,ORG_PSTL_ADR, LIB,T_LIB_TYP "
			+"WHERE ORG_PSTL_ADR.ORG_NBR=? AND ORG_PSTL_ADR.ORG_NBR=ORG_CMCTN.ORG_NBR AND " 
			+"ORG_PSTL_ADR.ORG_NBR=LIB.ORG_NBR AND LIB.LIB_TYP_CDE=T_LIB_TYP.TBL_VLU_CDE AND " 
			+"ORG_PSTL_ADR.ORG_PSTL_ADR_TYP_CDE=7 AND ORG_CMCTN.ORG_CMCTN_MDM_TYP_CDE=7";
	
	static String SELECT_FAX_NAMBER ="SELECT ORG_CMCTN_NBR FROM ORG_PSTL_ADR join org_cmctn on ORG_PSTL_ADR.ORG_NBR=ORG_CMCTN.ORG_NBR " 
									  +"AND ORG_PSTL_ADR.ORG_NBR= ? AND ORG_PSTL_ADR.ORG_PSTL_ADR_TYP_CDE=7 AND ORG_CMCTN.ORG_CMCTN_MDM_TYP_CDE=3";
	
	static String SELECT_EMAIL= "SELECT ORG_CMCTN_NBR "
	                            +"FROM ORG_PSTL_ADR join org_cmctn on ORG_PSTL_ADR.ORG_NBR=ORG_CMCTN.ORG_NBR "
	                            +"AND ORG_PSTL_ADR.ORG_NBR=? AND ORG_CMCTN.ORG_CMCTN_TYP_CDE=7 AND ORG_CMCTN.ORG_CMCTN_MDM_TYP_CDE=11";
	
	static String SELECT_URL="SELECT ORG_CMCTN_NBR FROM ORG_CMCTN "+
							 "WHERE ORG_NBR=? AND ORG_CMCTN.ORG_CMCTN_TYP_CDE=7 AND ORG_CMCTN.ORG_CMCTN_MDM_TYP_CDE=4";
	
	static String SELECT_CLOSING_DAYS="SELECT ORG_DTE_CLSE_DTE FROM ORG_DTE_CLSE "
									 +"WHERE EXTRACT(YEAR FROM ORG_DTE_CLSE_DTE) >= EXTRACT(YEAR FROM SYSDATE) AND "
									 +"ORG_DTE_CLSE.ORG_NBR=?";
	
	//static String SELECT_LIBRARY_NAME = "SELECT ORG_ENG_NME, LIB_SMBL_CDE  FROM ORG_NME, LIB WHERE ORG_NME.ORG_NBR=? AND ORG_NME.ORG_NBR = LIB.ORG_NBR";
	
	
	static String SELECT_LIBRARY_NAME ="SELECT ORG_ENG_NME, LIB_SMBL_CDE, ORG_CMCTN_NBR" +
			" FROM ORG_NME JOIN LIB ON ORG_NME.ORG_NBR = LIB.ORG_NBR" +
			" LEFT JOIN  ORG_CMCTN ON ORG_CMCTN.ORG_NBR = ORG_NME.ORG_NBR" +
			" AND ORG_CMCTN.ORG_CMCTN_TYP_CDE = 7 and  ORG_CMCTN.ORG_CMCTN_MDM_TYP_CDE = 2" +
			" WHERE ORG_NME.ORG_NBR=?";
	
	static String SELECT_BRANCH_LOCATION = "SELECT L.ORG_NBR, L.LCTN_NBR, L.STR_NBR, T.LBL_STR_TXT " +
										   "FROM LCTN L JOIN T_TRLTN T ON L.STR_NBR = T.STR_NBR WHERE "+
			                               "L.ORG_NBR=? AND T.LNG_CDE=5";
	
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
			throw new SystemInternalFailureException(exception);
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
			
			branch.setFax(getFaxNumber(branchId));
			branch.setEmail(getEmail(branchId));
			branch.setWebSite(getUrl(branchId));
			branch.setClosingDays(getClosingDays(branchId));
			
			
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
			throw new SystemInternalFailureException(exception);
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
				return new Branch(id, -1, resultSet.getString(1), resultSet.getString(2), resultSet.getString(3));
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
				library.setType_bib(resultSet.getString("TBL_SHRT_FR_TXT"));
				library.setContact(resultSet.getString("PSTL_ADR_REF_DSC"));
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
	

	private String getFaxNumber(int branchId) throws SystemInternalFailureException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		String fax = null;
		
		try
		{
			connection = datasource.getConnection();
			
			queryStatement = connection.prepareStatement(SELECT_FAX_NAMBER);
			queryStatement.setInt(1, branchId);

			rs = queryStatement.executeQuery();

			if (rs.next())
			{
				fax = rs.getString("ORG_CMCTN_NBR");
			}
				
			
			return fax;
		} catch (Exception exception)
		{
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
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
	
	private String getEmail(int branchId) throws SystemInternalFailureException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		String fax = null;
		
		try
		{
			connection = datasource.getConnection();
			
			queryStatement = connection.prepareStatement(SELECT_EMAIL);
			queryStatement.setInt(1, branchId);

			rs = queryStatement.executeQuery();

			if (rs.next())
			{
				fax = rs.getString("ORG_CMCTN_NBR");
			}
				
			
			return fax;
		} catch (Exception exception)
		{
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
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
	
	private String getUrl(int branchId) throws SystemInternalFailureException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		String url = null;
		
		try
		{
			connection = datasource.getConnection();
			
			queryStatement = connection.prepareStatement(SELECT_URL);
			queryStatement.setInt(1, branchId);

			rs = queryStatement.executeQuery();

			if (rs.next())
			{
				url = rs.getString("ORG_CMCTN_NBR");
			}
				
			
			return url;
		} catch (Exception exception)
		{
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
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
	
	private List<Date> getClosingDays(int branchId) throws SystemInternalFailureException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		List<Date> closingDays = new ArrayList<Date>();
		
		try
		{
			connection = datasource.getConnection();
			
			queryStatement = connection.prepareStatement(SELECT_CLOSING_DAYS);
			queryStatement.setInt(1, branchId);

			rs = queryStatement.executeQuery();
			
			while (rs.next())
			{
				closingDays.add(rs.getDate("ORG_DTE_CLSE_DTE"));
			}	
			
			return closingDays;
		} catch (Exception exception)
		{
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
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
	
	public List<String> getBranchSelectedLocations(int branchId, List<Integer> locList) throws SystemInternalFailureException{
		
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		
		List<String> locStringList = new ArrayList<String>();
		
		try
		{
			
			if(locList!=null && !locList.isEmpty()){
				StringBuilder queryString = new StringBuilder(SELECT_BRANCH_LOCATION);
				
				if(locList.size()==1){
					queryString.append(" AND L.LCTN_NBR="+locList.get(0));
				}else{
					queryString.append(" AND L.LCTN_NBR IN ("+locList.get(0));
					for(int i=1; i < locList.size(); i++){
						queryString.append(","+locList.get(i));
					}
					queryString.append(")");
				}
				
				connection = datasource.getConnection();
				
				queryStatement = connection.prepareStatement(queryString.toString());
				queryStatement.setInt(1, branchId);
	
				rs = queryStatement.executeQuery();
				
				while (rs.next())
				{
					locStringList.add(rs.getString("LBL_STR_TXT"));
				}	
			 }	
				return locStringList;
			  
			} catch (Exception exception)
			{
				LOGGER.error("Data access failure", exception);
				throw new SystemInternalFailureException(exception);
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
}