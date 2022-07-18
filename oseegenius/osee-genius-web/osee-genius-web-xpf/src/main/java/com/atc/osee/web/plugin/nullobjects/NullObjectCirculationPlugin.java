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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.velocity.tools.generic.ValueParser;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Fine;
import com.atc.osee.web.model.Hold;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.model.Loan;
import com.atc.osee.web.model.Order;
import com.atc.osee.web.plugin.CirculationPlugin;
import com.atc.osee.web.servlets.circulation.RenewalNotAllowedException;

/**
 * Null Object plugin implementation for account plugin.
 * 
 * @author agazzarini
 * @author ggazzarini
 * @since 1.0
 */
public class NullObjectCirculationPlugin implements CirculationPlugin
{
	private static final Map<Library, List<Fine>> NO_FINES = new HashMap<Library, List<Fine>>(0);
	private static final Map<Library, List<Hold>> NO_HOLDS = new HashMap<Library, List<Hold>>(0);
	private static final Map<Library, List<Loan>> NO_LOANS = new HashMap<Library, List<Loan>>(0);
	
	@Override
	public Map<Library, List<Fine>> findFines(final int borrowerId) 
	{
		return NO_FINES;
	}

	@Override
	public Map<Library, List<Hold>> findHolds(final int borrowerId) 
	{
		return NO_HOLDS;
	}

	@Override
	public Map<Library, List<Loan>> findLoans(final int borrowerId) 
	{
		return NO_LOANS;
	}
	
	@Override
	public void init(final DataSource datasource) 
	{
		// Nothing to be done here...
	}

	@Override
	public int countFines(final int borrowerId)
	{
		return 0;
	}

	@Override
	public int countHolds(final int borrowerId)
	{
		return 0;
	}

	@Override
	public int countLoans(final int borrowerId)
	{
		return 0;
	}

	@Override
	public boolean isUsingFinesByMoney() 
	{
		return false;
	}

	@Override
	public int countBlackListEntries(int accountId)
	{
		return 0;
	}

	@Override
	public Map<Library, Date> getBlackListStatusByLibrary(int accountId)
	{
		return null;
	}

	@Override
	public void init(ValueParser configuration) 
	{
		// Nothing
	}

	@Override
	public void placeHold(int itemId, int copyId, int branchId, int accountId)
	{
		// Nothing.
	}

	@Override
	public int renew(String barcode, int branchId)
			throws SystemInternalFailureException, RenewalNotAllowedException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Order> getItemOrders(int id)
			throws SystemInternalFailureException {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Loan> getLoanHistory(int id, int page, int pageSize)
			throws SystemInternalFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Loan> getLoanHistory(int id, int year)
			throws SystemInternalFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<String>> getAuthorityNotes(String indexName,
			int headingNbr) throws SystemInternalFailureException {
		// TODO Auto-generated method stub
		return null;
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