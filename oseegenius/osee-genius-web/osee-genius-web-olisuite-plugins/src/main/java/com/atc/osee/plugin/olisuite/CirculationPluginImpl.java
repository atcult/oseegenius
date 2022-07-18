package com.atc.osee.plugin.olisuite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.log.Log;
import com.atc.osee.web.model.Fine;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.model.Loan;
import com.atc.osee.web.model.Order;
import com.atc.osee.web.plugin.CirculationPlugin;
import com.atc.osee.web.plugin.HoldCannotBeCreatedException;
import com.atc.osee.web.servlets.circulation.HoldAlreadyExistsException;
import com.atc.osee.web.servlets.circulation.RenewalNotAllowedException;
import com.atc.weloan.logic.core.services.OnlineRemoteService;
import com.atc.weloan.shared.CannotPlaceHoldException;

/**
 * Olisuite implementation for OseeGenius Circulation plugin.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class CirculationPluginImpl implements CirculationPlugin 
{
	private final static Map<String, List<String>> EMPTY_AUTHORITY = new HashMap<String, List<String>>(0);

	private OnlineRemoteService service;
	
	private AccountDAO accountDAO;
	private CirculationDAO circulationDAO;
	private DataSource datasource;
	
	private final static Map<String, String> AUTH_QUERIES = new HashMap<String, String>();
	private final static Map<String, String> AUTH_AVAILABILITY_QUERIES = new HashMap<String, String>();
	
	private final static String VISTA1 = "1000000000000000"; 
										  	
	static 
	{
		AUTH_QUERIES.put(
				"title_browse", 		
				"SELECT B.AUT_NTE_TYP_CDE, (UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'a')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'b')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'u')) AS NOTES " 
				+"FROM AUT A, AUT_NTE B, TTL_HDG C "
				+"WHERE A.AUT_NBR=B.AUT_NBR AND "
				+"C.TTL_HDG_NBR=? AND "
				+"C.TTL_HDG_NBR= A.HDG_NBR AND C.USR_VW_IND="+ VISTA1);
		
		AUTH_AVAILABILITY_QUERIES.put(
				"title_browse", 		
				"SELECT COUNT(*) from AUT, TTL_HDG WHERE AUT.HDG_NBR=? AND AUT.HDG_NBR=TTL_HDG.TTL_HDG_NBR AND TTL_HDG.USR_VW_IND="+ VISTA1);
		AUTH_AVAILABILITY_QUERIES.put(
				"author_browse", 		
				"SELECT COUNT(*) from AUT, NME_HDG WHERE AUT.HDG_NBR=? AND AUT.HDG_NBR=NME_HDG.NME_HDG_NBR AND NME_HDG.USR_VW_IND="+ VISTA1);
		AUTH_AVAILABILITY_QUERIES.put(
				"subject_browse", 		
				"SELECT COUNT(*) from AUT, SBJCT_HDG WHERE AUT.HDG_NBR=? AND AUT.HDG_NBR=SBJCT_HDG.SBJCT_HDG_NBR AND SBJCT_HDG.USR_VW_IND="+ VISTA1);
		AUTH_AVAILABILITY_QUERIES.put(
				"publisher_browse", 		
				"SELECT COUNT(*) from AUT, PUBL_HDG WHERE AUT.HDG_NBR=? AND AUT.HDG_NBR=PUBL_HDG.PUBL_HDG_NBR");
		
		AUTH_QUERIES.put(
				"author_browse", 		
				"SELECT B.AUT_NTE_TYP_CDE, (UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'a')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'b')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'u')) AS NOTES " 
				+"FROM AUT A, AUT_NTE B, NME_HDG C "
				+"WHERE A.AUT_NBR=B.AUT_NBR AND "
				+"C.NME_HDG_NBR=? AND "
				+"C.NME_HDG_NBR= A.HDG_NBR AND C.USR_VW_IND="+ VISTA1);
		
		AUTH_QUERIES.put(
				"subject_browse", 		
				"SELECT B.AUT_NTE_TYP_CDE, (UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'a')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'b')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'u')) AS NOTES " 
				+"FROM AUT A, AUT_NTE B, SBJCT_HDG C "
				+"WHERE A.AUT_NBR=B.AUT_NBR AND "
				+"C.SBJCT_HDG_NBR=? AND "
				+"C.SBJCT_HDG_NBR= A.HDG_NBR AND C.USR_VW_IND="+ VISTA1);
		
		AUTH_QUERIES.put(
				"publisher_browse", 		
				"SELECT B.AUT_NTE_TYP_CDE, (UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'a')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'b')||' '||UTIL.GETSUBFLD(B.AUT_NTE_STRNG_TXT, 'u')) AS NOTES " 
				+"FROM AUT A, AUT_NTE B, PUBL_HDG C "
				+"WHERE A.AUT_NBR=B.AUT_NBR AND "
				+"C.PUBL_HDG_NBR=? AND "
				+"C.PUBL_HDG_NBR= A.HDG_NBR");
	}
	
	private final static String SELECT_FINES_POLICY = "SELECT GLBL_VRBL_VLU FROM S_SYS_GLBL_VRBL WHERE GLBL_VRBL_NME='fine_policy_type'";
	private final static String COUNT_LOAN_HISTORY = "SELECT COUNT(CIRT_ITM_ARCH.CPY_ID_NBR) "
			+"FROM   "
		+"CIRT_ITM_ARCH, ORG_NME, S_CACHE_BIB_ITM_DSPLY, CPY_ID, SHLF_LIST_ACS_PNT, SHLF_LIST " 
		+"WHERE   "
		+"CIRT_ITM_ARCH.PRSN_NBR = ? AND " 
		+"CIRT_ITM_ARCH.BIB_ITM_NBR = S_CACHE_BIB_ITM_DSPLY.BIB_ITM_NBR AND " 
		+"CIRT_ITM_ARCH.ORG_NBR = ORG_NME.ORG_NBR  AND    "
		+"CIRT_ITM_ARCH.CPY_ID_NBR = CPY_ID.CPY_ID_NBR AND  "
		+"CIRT_ITM_ARCH.ORG_NBR = CPY_ID.BRNCH_ORG_NBR  AND " 
		+"CPY_ID.ORG_NBR = SHLF_LIST_ACS_PNT.ORG_NBR AND  "
		+"CPY_ID.SHLF_LIST_KEY_NBR = SHLF_LIST_ACS_PNT.SHLF_LIST_KEY_NBR AND " 
		+" CPY_ID.SHLF_LIST_KEY_NBR=SHLF_LIST.SHLF_LIST_KEY_NBR ";	
	
	@Override
	public void init(final DataSource datasource) 
	{
		accountDAO = new AccountDAO(datasource);
		circulationDAO = new CirculationDAO(datasource);
		this.datasource = datasource;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init(ValueParser configuration) 
	{
		Context olisuiteNamingContext = null;

		String olisuiteInitialNamingFactory = configuration.getString(OLISUITE_NAMING_CONTEXT_FACTORY_PROPERTY_NAME, null);
		String olisuiteProviderURL = configuration.getString(OLISUITE_PROVIDER_URL_PROPERTY_NAME);

		if (olisuiteProviderURL != null && olisuiteInitialNamingFactory != null)
		{
			Hashtable olisuiteEnvironment = new Hashtable();
			olisuiteEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, olisuiteInitialNamingFactory);
			olisuiteEnvironment.put(Context.PROVIDER_URL, olisuiteProviderURL);
			try 
			{ 
				olisuiteNamingContext = new InitialContext(olisuiteEnvironment);
				service = (OnlineRemoteService) olisuiteNamingContext.lookup(configuration.getString(OLISUITE_ONLINE_SERVICE_NAME));
			} catch (Exception exception)
			{
				Log.error("Unable to lookup the OliSuite Borrower management service .", exception);
			}
		}		
	}

	public int countLoans(int borrowerId) throws SystemInternalFailureException 
	{
		try 
		{
			return accountDAO.countLoans(borrowerId);
		} catch (SQLException exception) 
		{
			Log.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
		}
	}

	public int countHolds(int borrowerId) throws SystemInternalFailureException 
	{
		try 
		{
			return accountDAO.countHolds(borrowerId);
		} catch (SQLException exception) 
		{
			Log.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
		}
	}

	public int countFines(int borrowerId) throws SystemInternalFailureException 
	{
		try 
		{
			return accountDAO.countFines(borrowerId);
		} catch (SQLException exception) 
		{
			Log.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
		}
	}

	@Override
	public Map<Library, List<Fine>> findFines(int personNumber) throws SystemInternalFailureException
	{
		try 
		{
			return accountDAO.findFines(personNumber);
		} catch (SQLException exception) 
		{
			Log.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
		}
	}

	@Override
	public Map<Library, List<com.atc.osee.web.model.Hold>> findHolds(int personNumber) throws SystemInternalFailureException
	{
		try 
		{
			return accountDAO.findHolds(personNumber);
		} catch (SQLException exception) 
		{
			Log.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
		}
	}

	@Override
	public Map<Library, List<Loan>> findLoans(int personNumber) throws SystemInternalFailureException
	{
		try 
		{
			return accountDAO.findLoans(personNumber);
		} catch (SQLException exception) 
		{
			Log.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
		}
	}


	public boolean isUsingFinesByMoney() throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_FINES_POLICY);
			rs = statement.executeQuery();
			return (rs.next()) 
					? "0".equals(rs.getString(1))
					: true;
		} catch (Exception exception)
		{
			Log.error("Data access failure", exception);
			throw new SystemInternalFailureException(exception);
		} finally 
		{
			try {  rs.close(); } catch (Exception exception) { }
			try {  statement.close(); } catch (Exception exception) { }
			try {  connection.close(); } catch (Exception exception) { }
		}		
	}

	@Override
	public int countBlackListEntries(int accountId) throws SystemInternalFailureException 
	{
		try 
		{
			return accountDAO.countBlackListEntriesByLibrary(accountId);
		} catch (SQLException exception) 
		{
			Log.error("Data access failure", exception);
			throw new SystemInternalFailureException();
		}
	}

	@Override
	public Map<Library, Date> getBlackListStatusByLibrary(int accountId) throws SystemInternalFailureException 
	{
		try 
		{
			return accountDAO.getBlackListStatusByLibrary(accountId);
		} catch (SQLException exception) 
		{
			Log.error("Data access failure", exception);
			throw new SystemInternalFailureException();
		}
	}
	
	@Override 
	public void placeHold(final int itemId, final int copyId, final int branchId, final int accountId)  throws SystemInternalFailureException, HoldAlreadyExistsException, HoldCannotBeCreatedException
	{
		try 
		{
			service.placeHold(accountId,branchId,copyId);	
			
		} catch (CannotPlaceHoldException exception) 
		{
			if(exception.getReasonCode() == 12){
				throw new HoldAlreadyExistsException();
			}
			else throw new HoldCannotBeCreatedException(exception.getReasonCode());	  
		} catch (com.atc.weloan.shared.SystemInternalFailureException e) 
		{
			e.printStackTrace();
			throw new SystemInternalFailureException();
			
		}catch (com.atc.weloan.shared.core.integration.NoSuchLibraryException exception) 
		{
			exception.printStackTrace();
			throw new SystemInternalFailureException();
		}catch (com.atc.weloan.shared.InvalidEmailFormatException exception) 
		{
			exception.printStackTrace();
			throw new SystemInternalFailureException();

		}catch (com.atc.weloan.shared.NoOnlineServicesAllowedException e) 
		{
			throw new HoldCannotBeCreatedException(997);
		}
		
		
	}
	 
	@Override
	public int renew(final String barcode, int branchId) throws SystemInternalFailureException, RenewalNotAllowedException
	{
		// NON DEVE ESSERE IMPLEMENTATO PER QUESTO PLUGIN
		throw new SystemInternalFailureException();
	}

	@Override
	public List<Order> getItemOrders(final int amicusNumber) throws SystemInternalFailureException 
	{
		try 
		{
			return circulationDAO.getOrders(amicusNumber);
		} catch (SQLException exception) 
		{
			exception.printStackTrace();
			throw new SystemInternalFailureException(exception);
		}
	}
	
	@Override
	public Map<String, List<String>> getAuthorityNotes(final String indexName, final int headingNbr) throws SystemInternalFailureException 
	{
		String query = AUTH_QUERIES.get(indexName);
		if (query == null)
		{
			return EMPTY_AUTHORITY;
		}
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
			
		Map<String, List<String>> notes = null;
			
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(query);
			statement.setInt(1, headingNbr);
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				if (notes == null)
				{
					notes = new TreeMap<String, List<String>>();
				}
					
				String codNote = rs.getString("AUT_NTE_TYP_CDE");
				List<String> notesList = null;
					
				//ci possono essere note ripetute
				if (notes.containsKey(codNote))
				{
					notesList = (List<String>) notes.get(codNote);
					notesList.add(rs.getString("NOTES"));
				}
				else
				{
					notesList = new ArrayList<String>();
					notesList.add(rs.getString("NOTES"));
					notes.put(codNote, notesList);
				}	
			}
			return notes;	
		} catch (SQLException exception) 
		{
			exception.printStackTrace();
			throw new SystemInternalFailureException(exception);
			
		} finally
		{
			if (rs != null) try { connection.close();} catch (Exception ignore) {}
			if (statement != null) try { statement.close();} catch (Exception ignore) {}
			if (connection != null) try { connection.close();} catch (Exception ignore) {}
		}	
	}

	@Override
	public int howManyAvailableCopies(final String id) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement("" +
					"select (select count(*) from CPY_ID WHERE BIB_ITM_NBR="+id+" AND HLDG_STUS_TYP_CDE!='5') - (select count(*) from CIRT_ITM WHERE BIB_ITM_NBR="+id+") FROM DUAL ");
			rs = statement.executeQuery();
			
			return (rs.next()) ? rs.getInt(1) : 0;
		} catch (Exception exception) 
		{
			Log.debug(exception);
			return -1;
			
		} finally
		{
			if (rs != null) try { connection.close();} catch (Exception ignore) {}
			if (statement != null) try { statement.close();} catch (Exception ignore) {}
			if (connection != null) try { connection.close();} catch (Exception ignore) {}
		}	
	}

	@Override
	public int countLoanHistory(int accountId) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(COUNT_LOAN_HISTORY);
			statement.setInt(1, accountId);
			rs = statement.executeQuery();
			return (rs.next()) ? rs.getInt(1) : 0;
		} catch (SQLException exception) 
		{
			throw new SystemInternalFailureException(exception);
			
		} finally
		{
			if (rs != null) try { connection.close();} catch (Exception ignore) {}
			if (statement != null) try { statement.close();} catch (Exception ignore) {}
			if (connection != null) try { connection.close();} catch (Exception ignore) {}
		}
	}

	@Override
	public List<Loan> getLoanHistory(int accountId, int page, int pageSize) throws SystemInternalFailureException 
	{
		int startRow = page * pageSize;
		int endRow = startRow + pageSize;
		return accountDAO.getLoanHistory(accountId, startRow, endRow);
	}

	@Override
	public List<Loan> getLoanHistory(int accountId, int year) throws SystemInternalFailureException 
	{
		return accountDAO.getLoanHistory(accountId, year);
	}

	@Override
	public boolean hasAuthority(int headingNumber, String type) throws SystemInternalFailureException 
	{
		String query = AUTH_AVAILABILITY_QUERIES.get(type);
		if (query == null)
		{
			return false;
		}
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(query);
			statement.setInt(1, headingNumber);
			rs = statement.executeQuery();
			
			return (rs.next()) ? rs.getInt(1) > 0 : false;
		} catch (SQLException exception) 
		{
			throw new SystemInternalFailureException(exception);
			
		} finally
		{
			if (rs != null) try { connection.close();} catch (Exception ignore) {}
			if (statement != null) try { statement.close();} catch (Exception ignore) {}
			if (connection != null) try { connection.close();} catch (Exception ignore) {}
		}	
	}

	@Override
	public void deleteOnlineHold(int accountId, int copyId) throws SystemInternalFailureException 
	{
		try 
		{
			service.deleteOnlineHold(accountId, copyId);
		} catch (com.atc.weloan.shared.SystemInternalFailureException e) 
		{
			throw new SystemInternalFailureException(e);
		}
	}
}