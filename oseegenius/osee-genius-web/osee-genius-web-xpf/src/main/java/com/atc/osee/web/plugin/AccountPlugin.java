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
import com.atc.osee.web.model.Account;
import com.atc.osee.web.model.Subscription;

/**
 * Account plugin.
 * A concrete implementor enables account operation on OseeGenius -W-.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public interface AccountPlugin extends DatabasePlugin
{
	/**
	 * Returns the account associated with the given username.
	 * 
	 * @param username the username.
	 * @return the account associated with the given username.
	 * @throws SystemInternalFailureException in case of system failure.
	 * @throws NoSuchAccountException in case no account is found.
	 */
	 Account getAccount(String username) throws SystemInternalFailureException, NoSuchAccountException;
	 
	 List<Subscription> getAccountSubscriptions(int accountId) throws  SystemInternalFailureException, NoSuchAccountException;
	 
	 /**
	  * Changes a password for a given borrower.
	  * 
	  * @param accountId the account identifier.
	  * @param oldPassword the old password.
	  * @param newPassword the new password.
	  * @throws NoSuchAccountException in case the account id is unknown.
	  * @throws PasswordMismatchException in case the old password doesn't correspond.
	  * @throws BadPasswordException in case the new password doesn't observe security rules.
	  * @throws SystemInternalFailureException in case of system failure.
	  */
	 void changePassword(int accountId, String oldPassword, String newPassword) throws PasswordMismatchException, BadPasswordException, SystemInternalFailureException;
	 
	 /**
	  * Changes the contacts of a given borrower.
	  * 
	  * @param personNumber the account id.
	  * @param address the address.
	  * @param cap the CAP.
	  * @param city the city.
	  * @param prov the state.
	  * @param email the email address.
	  * @param telephone the telephone.
	  * @param mobile the mobile phone number.
	  */
	 void changeContacts(int personNumber, String address, String cap, String city, String prov, String email, String telephone, String mobile) throws SystemInternalFailureException, WrongEmailException;

	 /**
	  * Sends password to the given user 
	  * 
	  * @param id
	  */
	 void sendPassword(int id) throws SystemInternalFailureException, NoSuchAccountException, UserDoesntHaveEmailException;

	boolean isChangePasswordNeeded(String username) throws SystemInternalFailureException;
}