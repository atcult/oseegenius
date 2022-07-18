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

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.atc.osee.web.SystemInternalFailureException;
import com.atc.osee.web.model.Fine;
import com.atc.osee.web.model.Hold;
import com.atc.osee.web.model.Library;
import com.atc.osee.web.model.Loan;
import com.atc.osee.web.model.Order;
import com.atc.osee.web.servlets.circulation.HoldAlreadyExistsException;
import com.atc.osee.web.servlets.circulation.RenewalNotAllowedException;

/**
 * OseeGenius Circulation extension point.
 * 
 * @author Giorgio Gazzarini
 * @since 1.0
 */
public interface CirculationPlugin extends DatabasePlugin
{
	/**
	 * Returns the total number of active loans associated with the given account.
	 * 
	 * @param borrowerId the borrower identifier.
	 * @return the total number of active loans associated with the given account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	int countLoans(int borrowerId) throws SystemInternalFailureException;

	/**
	 * Returns the total number of active holds associated with the given account.
	 * 
	 * @param borrowerId the borrower identifier.
	 * @return the total number of active holds associated with the given account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	int countHolds(int borrowerId) throws SystemInternalFailureException;

	/**
	 * Returns the total number of unpaid fines associated with the given account.
	 * 
	 * @param borrowerId the borrower identifier.
	 * @return the total number of unpaid fines associated with the given account.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	int countFines(int borrowerId) throws SystemInternalFailureException;
	
	/**
	 * Returns a map containing loans for a given user, organized by library.
	 * @param borrowerId the borrower identifier.
	 * 
	 * @return a map containing loans for a given user, organized by library.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	Map<Library, List<Loan>> findLoans(int borrowerId) throws SystemInternalFailureException;

	/**
	 * Returns a map containing holds for a given user, organized by library.
	 * @param borrowerId the borrower identifier.
	 * 
	 * @return a map containing holds for a given user, organized by library.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	Map<Library, List<Hold>> findHolds(int borrowerId) throws SystemInternalFailureException;
	
	/**
	 * Returns a map containing finesfor a given user, organized by library.
	 * @param borrowerId the borrower identifier.
	 * 
	 * @return a map containing fines for a given user, organized by library.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	Map<Library, List<Fine>> findFines(int borrowerId) throws SystemInternalFailureException;

	/**
	 * Returns true if the library policy for fines is using money (instead of days).
	 * @throws SystemInternalFailureException in case of system failure.
	 * 
	 * @return true if the library policy for fines is using money (instead of days).
	 */
	boolean isUsingFinesByMoney() throws SystemInternalFailureException;

	/**
	 * Counts in how many blacklist the user has been inserted.
	 * 
	 * @param accountId the account identifier.
	 * @return how many times (and in how many libraries) the user has been inserted in a blacklist.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	int countBlackListEntries(int accountId) throws SystemInternalFailureException;
	
	/**
	 * Returns the blacklist user state by library.
	 * 
	 * @param accountId the account id.
	 * @return the blacklist user state by library.
	 * @throws SystemInternalFailureException in case of system failure.
	 */
	Map<Library, Date> getBlackListStatusByLibrary(int accountId) throws SystemInternalFailureException;

	/**
	 * Places a hold for the item associated with incoming data.
	 * 
	 * @param itemId the item identifier.
	 * @param copyId the copy identifier.
	 * @param branchId the owning branch identifier.
	 * @param accountId the account identifier.
	 * @throws SystemInternalFailureException in case of system failure.
	 * @throws HoldAlreadyExistsException in case a similar holds already has been placed.
	 * @throws HoldCannotBeCreatedException in case the hold cannot be created.
	 */
	void placeHold(int itemId, int copyId, int branchId, int accountId) 
			throws SystemInternalFailureException, HoldAlreadyExistsException, HoldCannotBeCreatedException;

	void deleteOnlineHold(int accountId, int copyId) throws SystemInternalFailureException;	

	 int renew(final String barcode, int branchId) throws SystemInternalFailureException, RenewalNotAllowedException;

	List<Order> getItemOrders(int id) throws SystemInternalFailureException;
	
	Map<String, List<String>> getAuthorityNotes(String indexName, int headingNbr) throws SystemInternalFailureException;

	int howManyAvailableCopies(String id) throws SystemInternalFailureException;

	int countLoanHistory(int id) throws SystemInternalFailureException;

	List<Loan> getLoanHistory(int id, int page, int pageSize) throws SystemInternalFailureException;

	List<Loan> getLoanHistory(int id, int year) throws SystemInternalFailureException;

	boolean hasAuthority(int parseInt, String type) throws SystemInternalFailureException;
}