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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.logic.integration.AbstractDAO;
import com.atc.osee.web.model.Fine;
import com.atc.osee.web.model.Hold;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.model.Loan;

/**
 * Account Data Access Object.
 * 
 * @author ggazzarini
 * @since 1.0
 */
public class AccountDAO extends AbstractDAO 
{	
	private final static String COUNT_BLACKLIST_ENTRIES = "SELECT COUNT(PRSN_NBR) FROM PRSN_CIRTN WHERE PRSN_NBR=? AND PRSN_CIRTN_BLCKL_DTE IS NOT NULL";
	private final static String SELECT_BLACKLIST_BY_LIBRARY = "SELECT PRSN_CIRTN.ORG_NBR, PRSN_CIRTN_BLCKL_DTE, ORG_ENG_NME FROM PRSN_CIRTN,ORG_NME WHERE PRSN_NBR=? AND PRSN_CIRTN_BLCKL_DTE IS NOT NULL AND ORG_NME.ORG_NBR = PRSN_CIRTN.ORG_NBR";
	
	private final static String SELECT_BORROWER_LOANS = 
		"SELECT CIRT_ITM.ORG_NBR, CPY_ID_NBR, ORG_ENG_NME,CIRT_ITM_RCALL_NTFCT_DTE,CIRT_ITM_CHRG_OUT_DTE,CIRT_ITM_DUE_DTE,BRCDE_NBR,TTL_HDG_MAIN_STRNG_TXT, NME_MAIN_ENTRY_STRNG_TXT,CIRT_ITM.BIB_ITM_NBR "+ 
		"FROM CIRT_ITM, S_CACHE_BIB_ITM_DSPLY, ORG_NME "+
		"WHERE PRSN_NBR=? AND CIRT_ITM.BIB_ITM_NBR=S_CACHE_BIB_ITM_DSPLY.BIB_ITM_NBR "+ 
		"AND ORG_NME.ORG_NBR=CIRT_ITM.ORG_NBR "+
		"ORDER BY ORG_ENG_SRT_FORM ASC";
	
	private final static String SELECT_BORROWER_HOLDS = 
		"SELECT CIRTN_HLD.ORG_NBR, CIRTN_HLD.BIB_ITM_NBR, PICKUP.ORG_FR_NME, CIRTN_HLD_TYP_CDE, CIRTN_HLD_CRTE_DTE,TTL_HDG_MAIN_STRNG_TXT, NME_MAIN_ENTRY_STRNG_TXT,TME_HLD_STRT_DTE, TME_HLD_END_DTE "+
		"FROM CIRTN_HLD, ORG_NME ML, ORG_NME PICKUP, S_CACHE_BIB_ITM_DSPLY "+
		"WHERE CIRTN_HLD.ORG_NBR=ML.ORG_NBR AND CIRTN_HLD_LCTN_ORG_NBR=PICKUP.ORG_NBR AND CIRTN_HLD.BIB_ITM_NBR=S_CACHE_BIB_ITM_DSPLY.BIB_ITM_NBR AND PRSN_NBR=? ORDER BY PICKUP.ORG_ENG_SRT_FORM ASC";
	
	private final static String SELECT_BORROWER_FINES =
		"SELECT PRSN_FINE.ORG_NBR, ORG_ENG_NME, PRSN_FINE_CRTE_DTE,PRSN_FINE_BLNC_DUE_AMT,PRSN_FINE_TOTL_FINE_AMT "+
		"FROM PRSN_FINE,ORG_NME "+
		"WHERE PRSN_NBR=? AND PRSN_FINE_TOTL_FINE_AMT<> PRSN_FINE_BLNC_DUE_AMT AND ORG_NME.ORG_NBR=PRSN_FINE.ORG_NBR "+
		"ORDER BY ORG_ENG_SRT_FORM ASC";
	
	private final static String COUNT_LOANS = "SELECT COUNT(*) FROM CIRT_ITM WHERE PRSN_NBR=?";
	private final static String COUNT_FINES = "SELECT COUNT(*) FROM PRSN_FINE WHERE PRSN_NBR=? AND PRSN_FINE_TOTL_FINE_AMT<> PRSN_FINE_BLNC_DUE_AMT";
	private final static String COUNT_HOLDS = "SELECT COUNT(*) FROM CIRTN_HLD WHERE PRSN_NBR=?";
	
	private final static String PAGED_QUERY_PART_1=
			"SELECT * FROM ( " +
			"SELECT X.*, ROWNUM ROW_NUM " +
			"FROM (";
		
	private final static String PAGED_QUERY_PART_2 =
			") X " +
			"WHERE ROWNUM < ?)  " +
			"WHERE ROW_NUM >= ?";
	
	private final static String SELECT_BORROWER_HISTORY = 
			"SELECT "+
			"S_CACHE_BIB_ITM_DSPLY.BIB_ITM_NBR,TTL_HDG_MAIN_STRNG_TXT,CIRT_ITM_CHRG_OUT_DTE, CIRT_ITM_DUE_DTE, CIRT_ITM_ARCH.BRCDE_NBR,CIRT_ITM_ARCH.ORG_NBR, CIRT_ITM_CHCKIN_BRNCH "+
			",SHLF_LIST_STRNG_TEXT "+
			"FROM  "+
			"CIRT_ITM_ARCH, ORG_NME, S_CACHE_BIB_ITM_DSPLY, CPY_ID, SHLF_LIST_ACS_PNT, SHLF_LIST "+
			"WHERE "+ 
			"CIRT_ITM_ARCH.PRSN_NBR = ? AND "+
			"CIRT_ITM_ARCH.BIB_ITM_NBR = S_CACHE_BIB_ITM_DSPLY.BIB_ITM_NBR AND "+
			"CIRT_ITM_ARCH.ORG_NBR = ORG_NME.ORG_NBR  AND "+  
			"CIRT_ITM_ARCH.CPY_ID_NBR = CPY_ID.CPY_ID_NBR AND "+
			"CIRT_ITM_ARCH.ORG_NBR = CPY_ID.BRNCH_ORG_NBR  AND "+
			"CPY_ID.ORG_NBR = SHLF_LIST_ACS_PNT.ORG_NBR AND "+
			"CPY_ID.SHLF_LIST_KEY_NBR = SHLF_LIST_ACS_PNT.SHLF_LIST_KEY_NBR AND "+
			"CPY_ID.SHLF_LIST_KEY_NBR=SHLF_LIST.SHLF_LIST_KEY_NBR "+
			"ORDER BY CIRT_ITM_CHRG_OUT_DTE DESC ";
	
	private final static String SELECT_PAGED_BORROWER_HISTORY = PAGED_QUERY_PART_1 + SELECT_BORROWER_HISTORY + PAGED_QUERY_PART_2;
	
	private final static String SELECT_BORROWER_HISTORY_BY_YEAR = 
			"SELECT "+
			"TTL_HDG_MAIN_STRNG_TXT,CIRT_ITM_CHRG_OUT_DTE, CIRT_ITM_DUE_DTE, CIRT_ITM_ARCH.BRCDE_NBR,CIRT_ITM_ARCH.ORG_NBR, CIRT_ITM_CHCKIN_BRNCH "+
			",SHLF_LIST_STRNG_TEXT "+
			"FROM  "+
			"CIRT_ITM_ARCH, ORG_NME, S_CACHE_BIB_ITM_DSPLY, CPY_ID, SHLF_LIST_ACS_PNT, SHLF_LIST "+
			"WHERE "+ 
			"CIRT_ITM_ARCH.PRSN_NBR = ? AND "+
			"CIRT_ITM_ARCH.BIB_ITM_NBR = S_CACHE_BIB_ITM_DSPLY.BIB_ITM_NBR AND "+
			"CIRT_ITM_ARCH.ORG_NBR = ORG_NME.ORG_NBR  AND "+  
			"CIRT_ITM_ARCH.CPY_ID_NBR = CPY_ID.CPY_ID_NBR AND "+
			"CIRT_ITM_ARCH.ORG_NBR = CPY_ID.BRNCH_ORG_NBR  AND "+
			"CPY_ID.ORG_NBR = SHLF_LIST_ACS_PNT.ORG_NBR AND "+
			"CPY_ID.SHLF_LIST_KEY_NBR = SHLF_LIST_ACS_PNT.SHLF_LIST_KEY_NBR AND "+
			"CPY_ID.SHLF_LIST_KEY_NBR=SHLF_LIST.SHLF_LIST_KEY_NBR AND "+
			"CIRT_ITM_CHRG_OUT_DTE < ? "+
			"ORDER BY CIRT_ITM_CHRG_OUT_DTE DESC ";
	
	
	

	
	private final static Map<Integer, Library> branches = new HashMap<Integer, Library>();
	
	/**
	 * Builds a new DAO with the given datasource.
	 * 
	 * @param datasource the datasource.
	 */
	public AccountDAO(DataSource datasource) 
	{
		super(datasource);
	}	
	
	public int countLoans(int borrowerId) throws SQLException
	{
		return count(borrowerId, COUNT_LOANS);
	}

	public int countHolds(int borrowerId) throws SQLException
	{
		return count(borrowerId, COUNT_HOLDS);
	}

	public int countFines(int borrowerId) throws SQLException
	{
		return count(borrowerId, COUNT_FINES);
	}

	private int count(int borrowerId, String sql) throws SQLException
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, borrowerId);
			rs = statement.executeQuery();
			return (rs.next()) ? rs.getInt(1) : 0;
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception)
		{
			throw new SQLException(exception);
		} finally 
		{
			try {  rs.close(); } catch (Exception exception) { }
			try {  statement.close(); } catch (Exception exception) { }
			try {  connection.close(); } catch (Exception exception) { }
		}
	}

	public Map<Library, List<Loan>> findLoans(int personNumber) throws SQLException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		
		Map<Library, List<Loan>> result = new HashMap<Library, List<Loan>>();
		
		try 
		{
			connection = datasource.getConnection();
			queryStatement = connection.prepareStatement(SELECT_BORROWER_LOANS);
			queryStatement.setInt(1, personNumber);
		
			rs = queryStatement.executeQuery();

			while (rs.next()) 
			{
				addLoan(
						new Loan(
							rs.getDate("CIRT_ITM_RCALL_NTFCT_DTE"),
							rs.getDate("CIRT_ITM_CHRG_OUT_DTE"),
							rs.getDate("CIRT_ITM_DUE_DTE"),
							rs.getString("BRCDE_NBR"),
							rs.getString("TTL_HDG_MAIN_STRNG_TXT"),
							removeTrailingPunctuation(rs.getString("NME_MAIN_ENTRY_STRNG_TXT")),
							rs.getInt("BIB_ITM_NBR"),
							rs.getInt("CPY_ID_NBR"),
							null, 0, null, null, null),
						rs.getInt("ORG_NBR"),
						rs.getString("ORG_ENG_NME"),
						result);
			}	
			return result;
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception) 
		{
			throw new SQLException(exception);
		} finally 
		{
			try { rs.close();} catch (Exception ignore) {}
			try {queryStatement.close();} catch (Exception ignore) {}
			try {connection.close();} catch (Exception ignore) {}
		}
	}
	
	public Map<Library, List<Hold>> findHolds(int personNumber) throws SQLException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		
		Map<Library, List<Hold>> result = new HashMap<Library, List<Hold>>();

		try 
		{
			connection = datasource.getConnection();
			queryStatement = connection.prepareStatement(SELECT_BORROWER_HOLDS);
			queryStatement.setInt(1, personNumber);
		
			rs = queryStatement.executeQuery();

			while (rs.next()) 
			{
			
				
				addHold(
						new Hold(
							rs.getInt("BIB_ITM_NBR"),
							rs.getString("ORG_FR_NME"),
							rs.getInt("CIRTN_HLD_TYP_CDE"),
							rs.getDate("CIRTN_HLD_CRTE_DTE"),
							rs.getString("TTL_HDG_MAIN_STRNG_TXT"),
							removeTrailingPunctuation(rs.getString("NME_MAIN_ENTRY_STRNG_TXT")),
							rs.getDate("TME_HLD_STRT_DTE"),
							rs.getDate("TME_HLD_END_DTE")),
						rs.getInt("ORG_NBR"),
						rs.getString("ORG_FR_NME"),
						result);
			}	
			return result;
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception) 
		{
			throw new SQLException(exception);
		} finally 
		{
			try { rs.close();} catch (Exception ignore) {}
			try {queryStatement.close();} catch (Exception ignore) {}
			try {connection.close();} catch (Exception ignore) {}
		}
	}	
	
	public Map<Library, List<Fine>> findFines(int personNumber) throws SQLException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		
		Map<Library, List<Fine>> result = new HashMap<Library, List<Fine>>();

		try 
		{
			connection = datasource.getConnection();
			queryStatement = connection.prepareStatement(SELECT_BORROWER_FINES);
			queryStatement.setInt(1, personNumber);
		
			rs = queryStatement.executeQuery();

			while (rs.next()) 
			{
				addFine(
						new Fine(
							rs.getDate("PRSN_FINE_CRTE_DTE"),
							rs.getBigDecimal("PRSN_FINE_TOTL_FINE_AMT"),
							rs.getBigDecimal("PRSN_FINE_BLNC_DUE_AMT")),
							rs.getInt("ORG_NBR"),
							rs.getString("ORG_ENG_NME"),							
						result);
			}	
			return result;
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception) 
		{
			throw new SQLException(exception);
		} finally 
		{
			try { rs.close();} catch (Exception ignore) {}
			try {queryStatement.close();} catch (Exception ignore) {}
			try {connection.close();} catch (Exception ignore) {}
		}
	}	
	
	public int countBlackListEntriesByLibrary(final int accountId) throws SQLException
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		
		try 
		{
			connection = datasource.getConnection();
			queryStatement = connection.prepareStatement(COUNT_BLACKLIST_ENTRIES);
			queryStatement.setInt(1, accountId);
			rs = queryStatement.executeQuery();

			return (rs.next()) ? rs.getInt(1) : 0;
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception) 
		{
			throw new SQLException(exception);
		} finally 
		{
			try { rs.close();} catch (Exception ignore) {}
			try {queryStatement.close();} catch (Exception ignore) {}
			try {connection.close();} catch (Exception ignore) {}
		}		
	}
	
	private void addLoan(Loan loan, int branchId, String libraryName, Map<Library, List<Loan>> loansByLibrary)
	{
		Library branch = branches.get(branchId);
		if (branch == null)
		{
			branch = new Library(branchId);
			branch.setName(libraryName);
			branches.put(branchId, branch);
		}
		
		List<Loan> loans = loansByLibrary.get(branch);
		if (loans == null)
		{
			loans = new ArrayList<Loan>();
			loansByLibrary.put(branch,loans);
		}
		loans.add(loan);
	}	
	
	private void addHold(Hold hold, int branchId, String libraryName, Map<Library, List<Hold>> holdsByLibrary )
	{
		Library branch = branches.get(branchId);
		if (branch == null)
		{
			branch = new Library(branchId);
			branch.setName(libraryName);
			branches.put(branchId, branch);
		}
		
		List<Hold> holds = holdsByLibrary.get(branch);
		if (holds == null)
		{
			holds = new ArrayList<Hold>();
			holdsByLibrary.put(branch,holds);
		}
		holds.add(hold);
	}		
	
	private void addFine(Fine fine, int branchId, String libraryName, Map<Library, List<Fine>> finesByLibrary )
	{
		Library branch = branches.get(branchId);
		if (branch == null)
		{
			branch = new Library(branchId);
			branch.setName(libraryName);
			branches.put(branchId, branch);
		}
		
		List<Fine> fines = finesByLibrary.get(branch);
		if (fines == null)
		{
			fines = new ArrayList<Fine>();
			finesByLibrary.put(branch,fines);
		}
		fines.add(fine);
	}			
	
	private String removeTrailingPunctuation(String value)
	{
		return (value!= null && value.endsWith(".")) ? value.substring(0, value.length()-1) : value;
	}

	public Map<Library, Date> getBlackListStatusByLibrary(int accountId) throws SQLException 
	{
		Connection connection = null;
		PreparedStatement queryStatement = null;
		ResultSet rs = null;
		
		Map<Library, Date> blackListEntriesCountByLibrary = new HashMap<Library, Date>();
		
		try 
		{
			connection = datasource.getConnection();
			queryStatement = connection.prepareStatement(SELECT_BLACKLIST_BY_LIBRARY);
			queryStatement.setInt(1, accountId);
			rs = queryStatement.executeQuery();

			while (rs.next()) 
			{
				blackListEntriesCountByLibrary.put(
						new Library(rs.getInt("ORG_NBR"), rs.getString("ORG_ENG_NME"), null),
						rs.getDate("PRSN_CIRTN_BLCKL_DTE"));
			}	
			return blackListEntriesCountByLibrary;
		} catch (SQLException exception) 
		{
			throw exception;
		} catch (Exception exception) 
		{
			throw new SQLException(exception);
		} finally 
		{
			try { rs.close();} catch (Exception ignore) {}
			try {queryStatement.close();} catch (Exception ignore) {}
			try {connection.close();} catch (Exception ignore) {}
		}		
	}
	
	public List<Loan> getLoanHistory(int accountId, int startRow, int endRow) throws SystemInternalFailureException 
	{
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_PAGED_BORROWER_HISTORY);
			statement.setInt(1, accountId);
			statement.setInt(2, endRow);
			statement.setInt(3, startRow);
			
			rs = statement.executeQuery();
			List<Loan> result = new ArrayList<Loan>();
			while (rs.next())
			{
				String shelflist = rs.getString("SHLF_LIST_STRNG_TEXT");
				shelflist = (shelflist != null && shelflist.trim().length() > 2) ? shelflist.substring(2) : null;
				
				Loan loan = new Loan(
						null, 
						rs.getDate("CIRT_ITM_CHRG_OUT_DTE"), 
						rs.getDate("CIRT_ITM_DUE_DTE"), 
						rs.getString("BRCDE_NBR"), 
						rs.getString("TTL_HDG_MAIN_STRNG_TXT"), 
						null, 
						rs.getInt("BIB_ITM_NBR"), 
						-1, 
						shelflist, 
						-1, 
						null, 
						null, 
						null);
				loan.setBranchId(rs.getInt("ORG_NBR"));
				
				result.add(loan);
			}
			return result;
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

	public List<Loan> getLoanHistory(int accountId, int year) throws SystemInternalFailureException 
	{
		Calendar c = Calendar.getInstance();
		c.set(year, 11, 31);
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try 
		{
			connection = datasource.getConnection();
			statement = connection.prepareStatement(SELECT_BORROWER_HISTORY_BY_YEAR);
			statement.setInt(1, accountId);
			statement.setDate(2, new java.sql.Date(c.getTime().getTime()));
			
			rs = statement.executeQuery();
			List<Loan> result = new ArrayList<Loan>();
			while (rs.next())
			{
				String shelflist = rs.getString("SHLF_LIST_STRNG_TEXT");
				shelflist = (shelflist != null && shelflist.trim().length() > 2) ? shelflist.substring(2) : null;
				
				Loan loan = new Loan(
						null, 
						rs.getDate("CIRT_ITM_CHRG_OUT_DTE"), 
						rs.getDate("CIRT_ITM_DUE_DTE"), 
						rs.getString("BRCDE_NBR"), 
						rs.getString("TTL_HDG_MAIN_STRNG_TXT"), 
						null, 
						-1, 
						-1, 
						shelflist, 
						-1, 
						null, 
						null, 
						null);
				loan.setBranchId(rs.getInt("ORG_NBR"));
				loan.setCheckinBranchId(rs.getInt("CIRT_ITM_CHCKIN_BRNCH"));
				result.add(loan);
			}
			return result;
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
}