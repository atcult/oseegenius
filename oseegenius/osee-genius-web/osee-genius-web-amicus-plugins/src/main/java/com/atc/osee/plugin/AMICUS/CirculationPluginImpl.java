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
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atc.osee.plugin.AMICUS.integration.AccountDAO;
import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Fine;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.model.Loan;
import com.atc.osee.web.model.Order;
import com.atc.osee.web.plugin.CirculationPlugin;
import com.atc.osee.web.servlets.circulation.HoldAlreadyExistsException;
import com.atc.osee.web.servlets.circulation.RenewalNotAllowedException;
import com.atc.weloan.logic.core.services.AnotherTimeAndSingleHoldBothExistException;
import com.atc.weloan.logic.core.services.AvailableCopiesCouldFillTheHoldException;
import com.atc.weloan.logic.core.services.AvailableCopyCouldFillTheHoldException;
import com.atc.weloan.logic.core.services.HoldManagementRemoteService;
import com.atc.weloan.logic.core.services.MoreThanOneSingleCopyHoldException;
import com.atc.weloan.logic.core.services.NoSuchTimeHoldPolicyException;
import com.atc.weloan.logic.core.services.NotEnoughTimeException;
import com.atc.weloan.logic.core.services.SimilarHoldAlreadyExistsException;
import com.atc.weloan.logic.core.services.TimeHoldWithInvalidBoundsException;
import com.atc.weloan.logic.core.services.circulation.CirculationRemoteService;
import com.atc.weloan.logic.core.services.circulation.RenewalNotAllowedByInterbranchPolicyException;
import com.atc.weloan.shared.model.Hold;
import com.atc.weloan.shared.model.RenewalResult;

/**
 * AMICUS Circulation plugin implementor.
 * 
 * @author ggazzarini
 * @since 1.0
 */
public class CirculationPluginImpl implements CirculationPlugin
{
	private final static Logger LOGGER = LoggerFactory.getLogger(CirculationPluginImpl.class);
	private final static Random RANDOMIZER = new Random();
	
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
	
	private AccountDAO accountDAO;
	private DataSource datasource;

	private HoldManagementRemoteService holdManagementService;
	private CirculationRemoteService circulationService;
	
	@Override
	public void init(DataSource datasource)
	{
		accountDAO = new AccountDAO(datasource);
		this.datasource = datasource;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void init(final ValueParser configuration) 
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
				holdManagementService = (HoldManagementRemoteService) olisuiteNamingContext.lookup(configuration.getString(OLISUITE_HOLD_MANAGEMENT_SERVICE_NAME));
				circulationService = (CirculationRemoteService)olisuiteNamingContext.lookup(configuration.getString(OLISUITE_CIRCULATION_SERVICE_NAME));
			} catch (Exception exception)
			{
				LOGGER.error("Unable to lookup the OliSuite Borrower management service.", exception);
			}
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
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException();
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
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException();
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
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException();
		}
	}

	@Override
	public int countFines(int borrowerId) throws SystemInternalFailureException
	{
		try 
		{
			return accountDAO.countFines(borrowerId);
		} catch (SQLException exception) 
		{
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException();
		}
	}

	@Override
	public int countHolds(int borrowerId) throws SystemInternalFailureException
	{
		try 
		{
			return accountDAO.countHolds(borrowerId);
		} catch (SQLException exception) 
		{
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException();
		}
	}

	@Override
	public int countLoans(int borrowerId) throws SystemInternalFailureException
	{
		try 
		{
			return accountDAO.countLoans(borrowerId);
		} catch (SQLException exception) 
		{
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException();
		}
	}

	@Override
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
			LOGGER.error("Data access failure", exception);
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
			LOGGER.error("Data access failure", exception);
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
			LOGGER.error("Data access failure", exception);
			throw new SystemInternalFailureException();
		}
	}
	
	@Override 
	public void placeHold(final int itemId, final int copyId, final int branchId, final int accountId)  throws SystemInternalFailureException, HoldAlreadyExistsException
	{
		int reservedCopyNumber = Integer.MIN_VALUE;
		Hold hold = new Hold(itemId, copyId, reservedCopyNumber, accountId, null, null, branchId, Hold.SINGLE_REGULAR_HOLD);
		hold.setCreationDate(new Date());
		hold.setLocationOrgNumber(branchId);
		try 
		{
			holdManagementService.placeHold(hold, true);
		} catch (com.atc.weloan.shared.SystemInternalFailureException exception) 
		{
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);
		} catch (TimeHoldWithInvalidBoundsException exception) 
		{
			// Should never happen because OG doesn't support time holds.
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);			
		} catch (SimilarHoldAlreadyExistsException exception) 
		{
			throw new HoldAlreadyExistsException();						
		} catch (AvailableCopyCouldFillTheHoldException exception) 
		{
			// Should never happen because we forced the hold
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);			
		} catch (AvailableCopiesCouldFillTheHoldException exception) 
		{
			// Should never happen because we forced the hold
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);			
		} catch (AnotherTimeAndSingleHoldBothExistException exception) 
		{
			// Should never happen because we forced the hold
			LOGGER.error("System failure", exception);
		} catch (NoSuchTimeHoldPolicyException exception) 
		{
			// Should never happen because OG doesn't support time holds.
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);			
		} catch (MoreThanOneSingleCopyHoldException exception) 
		{
			// Amicus doesn't allow a time hold if more than one regular hold exists on the copy.
			// Should never happen because OG doesn't support time holds.
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);		
		} catch (NotEnoughTimeException exception) 
		{
			// Should never happen because OG doesn't support time holds.
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);					
		} catch (Exception exception) 
		{
			// Should never happen because OG doesn't support time holds.
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);					
		}
	}
	 
	@Override
	public int renew(final String barcode, int branchId) throws SystemInternalFailureException, RenewalNotAllowedException
	{
		List<String> barcodes = new ArrayList<String>(1);
		
		try 
		{
			barcodes.add(barcode.trim());
			List<RenewalResult> result = circulationService.renew(barcodes, true, branchId, createSessionId()); 
			if (result != null && result.size() > 0)
			{
				return result.get(0).getReturnCode();
			} else 
			{
				throw new SystemInternalFailureException();
			}
		} catch (RenewalNotAllowedByInterbranchPolicyException exception)
		{
			throw new RenewalNotAllowedException();
		} catch (com.atc.weloan.shared.SystemInternalFailureException exception) 
		{
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);
		} catch (Exception exception) 
		{
			LOGGER.error("System failure", exception);
			throw new SystemInternalFailureException(exception);
		} 
	}
	
	/**
	 * Utility method that creates (temporary) valid AMICUS session id.
	 * 
	 * @return a valid AMICUS session ID.
	 */
	private String createSessionId()
	{
		StringBuilder value = new StringBuilder(RANDOMIZER.nextInt(99999999));
		int howManyPaddings = (8 - value.length());
		for (int i = 0; i < howManyPaddings; i++)
		{
			value.append(' ');
		}
		return value.toString();
	}

	@Override
	public List<Order> getItemOrders(int id) throws SystemInternalFailureException 
	{
		throw new SystemInternalFailureException();
	}

	@Override
	public Map<String, List<String>> getAuthorityNotes(String indexName, int headingNbr) 
	{
		return null;
	}

	@Override
	public int howManyAvailableCopies(String id)
			throws SystemInternalFailureException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int countLoanHistory(int id) throws SystemInternalFailureException {
//		// TODO Auto-generated method stub
//		return 0;
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(COUNT_LOAN_HISTORY);
			statement.setInt(1, id);
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
	public List<Loan> getLoanHistory(int id, int page, int pageSize)
			throws SystemInternalFailureException {
//		// TODO Auto-generated method stub
//		return null;
		int startRow = page * pageSize;
		int endRow = startRow + pageSize;
		return accountDAO.getLoanHistory(id, startRow, endRow);
	}

	@Override
	public List<Loan> getLoanHistory(int id, int year)
			throws SystemInternalFailureException {
//		// TODO Auto-generated method stub
//		return null;
		return accountDAO.getLoanHistory(id, year);
	}

	@Override
	public boolean hasAuthority(int parseInt, String type)
			throws SystemInternalFailureException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deleteOnlineHold(int accountId, int copyId)
			throws SystemInternalFailureException {
		// TODO Auto-generated method stub
	}
}